#!/usr/bin/env python3
"""Smoke-test a production Fabric jar in a fresh, isolated Minecraft 26.3 server."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import socket
import subprocess
import sys
import time
import zipfile


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jar', required=True, type=Path, help='Production mod jar under test')
    parser.add_argument('--mods', required=True, type=Path, help='Directory containing only required dependency jars')
    parser.add_argument('--fixture', action='append', type=Path, default=[], help='Optional test-only fixture jar; repeatable')
    parser.add_argument('--java', type=Path, help='Java 25 executable')
    parser.add_argument('--cache', type=Path, default=Path.home() / '.gradle/caches')
    parser.add_argument('--timeout', type=int, default=240, help='Timeout per lifecycle phase in seconds')
    args = parser.parse_args()
    java = args.java or (Path(os.environ['JAVA_HOME']) / 'bin/java' if 'JAVA_HOME' in os.environ
        else Path(shutil.which('java')) if shutil.which('java') else None)
    if java is None:
        parser.error('Java 25 not found; supply --java')
    version = subprocess.run([str(java), '-version'], capture_output=True, text=True, check=True)
    if not re.search(r'version "25(?:[.\"])', version.stderr + version.stdout):
        parser.error('--java must identify Java 25')
    if not args.mods.is_dir() or not args.jar.is_file():
        parser.error('--jar must be a file and --mods must be a directory')

    root = Path(__file__).resolve().parents[1]
    run = root / 'build' / 'qa-run' / (time.strftime('%Y%m%d-%H%M%S') + '-' + str(os.getpid()))
    run.mkdir(parents=True, exist_ok=False)
    (run / 'mods').mkdir()
    (run / 'libraries').mkdir()
    modules = args.cache / 'modules-2/files-2.1'

    def artifact(group, name, version):
        matches = sorted((modules / group / name / version).glob('*/' + name + '-' + version + '.jar'))
        if not matches:
            raise RuntimeError(f'Missing cached artifact {group}:{name}:{version}')
        if len({sha256(p) for p in matches}) != 1:
            raise RuntimeError(f'Ambiguous cached artifact {group}:{name}:{version}')
        return matches[0]

    audit = {'minecraft': '26.3', 'fabric_loader': '0.19.5', 'run': str(run),
             'java_version': (version.stderr + version.stdout).strip(), 'mods': [], 'libraries': []}
    classpath = []

    def copy_library(source):
        target = run / 'libraries' / source.name
        digest = sha256(source)
        if target.exists() and sha256(target) != digest:
            raise RuntimeError('Conflicting library filename: ' + source.name)
        shutil.copy2(source, target)
        if sha256(target) != digest:
            raise RuntimeError('Library copy checksum mismatch: ' + source.name)
        audit['libraries'].append({'source': str(source.resolve()), 'file': target.name, 'sha256': digest})
        classpath.append(str(target))

    loader = artifact('net.fabricmc', 'fabric-loader', '0.19.5')
    copy_library(loader)
    with zipfile.ZipFile(loader) as archive:
        metadata = json.loads(archive.read('fabric-installer.json'))
    for library in metadata['libraries']['common'] + metadata['libraries']['server']:
        source = artifact(*library['name'].split(':'))
        if hashlib.sha1(source.read_bytes()).hexdigest() != library['sha1']:
            raise RuntimeError('Fabric library checksum mismatch: ' + library['name'])
        copy_library(source)

    server = args.cache / 'fabric-loom/26.3/minecraft-server.jar'
    audit['server_bundle'] = {'source': str(server.resolve()), 'sha256': sha256(server)}
    with zipfile.ZipFile(server) as archive:
        for kind in ['libraries', 'versions']:
            for line in archive.read('META-INF/' + kind + '.list').decode().splitlines():
                digest, name, path = line.split('\t')
                data = archive.read('META-INF/' + kind + '/' + path)
                if hashlib.sha256(data).hexdigest() != digest:
                    raise RuntimeError('Bundled server checksum mismatch: ' + name)
                target = run / 'libraries' / Path(path).name
                if target.exists() and sha256(target) != digest:
                    raise RuntimeError('Conflicting bundled filename: ' + target.name)
                target.write_bytes(data)
                if sha256(target) != digest:
                    raise RuntimeError('Extracted server checksum mismatch: ' + name)
                audit['libraries'].append({'bundle_entry': kind + '/' + path, 'file': target.name, 'sha256': digest})
                classpath.append(str(target))

    mod_ids = set()
    inputs = [(args.jar, 'production')] + [(p, 'dependency') for p in sorted(args.mods.glob('*.jar'))]
    inputs += [(p, 'fixture') for p in args.fixture]
    for source, role in inputs:
        with zipfile.ZipFile(source) as archive:
            metadata = json.loads(archive.read('fabric.mod.json'))
        if metadata['id'] in mod_ids:
            raise RuntimeError('Duplicate mod ID: ' + metadata['id'])
        mod_ids.add(metadata['id'])
        target = run / 'mods' / source.name
        if target.exists():
            raise RuntimeError('Duplicate mod filename: ' + source.name)
        digest = sha256(source)
        shutil.copy2(source, target)
        if sha256(target) != digest:
            raise RuntimeError('Mod copy checksum mismatch: ' + source.name)
        audit['mods'].append({'source': str(source.resolve()), 'file': target.name, 'role': role,
            'id': metadata['id'], 'version': metadata['version'], 'depends': metadata.get('depends', {}), 'sha256': digest})

    snow_checks = 'srm_qa_checks' in mod_ids

    # Bind to localhost on a vacant port. The server uses only this newly created world.
    with socket.socket() as probe:
        probe.bind(('127.0.0.1', 0))
        port = probe.getsockname()[1]
    (run / 'eula.txt').write_text('eula=true\n')
    (run / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=' + str(port) + '\n'
        'online-mode=false\nenforce-secure-profile=false\nview-distance=2\nsimulation-distance=2\n'
        'level-name=qa-world\nwhite-list=false\nmax-players=1\n'
        'pause-when-empty-seconds=0\nmotd=Snow Real Magic disposable QA\n')
    command = [str(java), '-Xms512M', '-Xmx2G', '-XX:ActiveProcessorCount=4',
        '-cp',
        os.pathsep.join(dict.fromkeys(classpath)), 'net.fabricmc.loader.impl.launch.knot.KnotServer', 'nogui']
    audit['command'] = command
    audit['address'] = '127.0.0.1:' + str(port)
    (run / 'launch-audit.json').write_text(json.dumps(audit, indent=2) + '\n')
    print('Run directory:', run, flush=True)
    log_path = run / 'console.log'
    result = {'passed': False, 'startup': False, 'reload': False, 'shutdown': False}
    with log_path.open('w') as log:
        process = subprocess.Popen(command, cwd=run, stdin=subprocess.PIPE, stdout=log,
            stderr=subprocess.STDOUT, text=True)

        def wait_for(pattern, start=0):
            deadline = time.monotonic() + args.timeout
            while time.monotonic() < deadline:
                text = log_path.read_text(errors='replace')
                if re.search(pattern, text[start:]):
                    return len(text)
                if process.poll() is not None:
                    raise RuntimeError(f'Server exited ({process.returncode}) before {pattern}')
                time.sleep(0.25)
            raise RuntimeError('Timed out waiting for ' + pattern)

        def send(command):
            process.stdin.write(command + '\n')
            process.stdin.flush()

        try:
            offset = wait_for(r'Done \([\d.]+s\)!')
            if snow_checks:
                wait_for(r'SRM_QA_PASS startup')
            result['startup'] = True
            send('reload')
            wait_for(r'Loaded \d+ advancements', offset)
            if snow_checks:
                wait_for(r'SRM_QA_PASS reload', offset)
            send('say SRM_QA_RELOAD_COMPLETE')
            wait_for(r'(?:\[Server\]|System chat:) SRM_QA_RELOAD_COMPLETE', offset)
            result['reload'] = True
            send('stop')
            process.wait(timeout=args.timeout)
            result['exit_code'] = process.returncode
            output = log_path.read_text(errors='replace')
            result['shutdown'] = process.returncode == 0 and 'Stopping server' in output
        except (RuntimeError, subprocess.TimeoutExpired, BrokenPipeError) as error:
            result['failure'] = str(error)
        finally:
            if process.poll() is None:
                process.terminate()
                try:
                    process.wait(timeout=15)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait()
            process.stdin.close()
            result.setdefault('exit_code', process.returncode)
            output = log_path.read_text(errors='replace')
            result['errors'] = [line for line in output.splitlines() if re.search(
                r'/(?:ERROR|FATAL)\]|Exception in thread|Mixin apply.*failed|Failed to (?:load|parse)|Error loading|SRM_QA_FAIL', line)]
            result['passed'] = (result['startup'] and result['reload'] and result['shutdown']
                                and not result['errors'] and 'failure' not in result)
            (run / 'result.json').write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps(result, indent=2))
    return 0 if result['passed'] else 1


if __name__ == '__main__':
    sys.exit(main())
