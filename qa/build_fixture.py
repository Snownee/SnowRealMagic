#!/usr/bin/env python3
"""Compile the optional runtime snow fixture against the production artifact."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--jar', required=True, type=Path)
parser.add_argument('--mods', required=True, type=Path)
parser.add_argument('--javac', type=Path, default=Path(os.environ['JAVA_HOME']) / 'bin/javac'
    if 'JAVA_HOME' in os.environ else Path(shutil.which('javac') or 'javac'))
parser.add_argument('--cache', type=Path, default=Path.home() / '.gradle/caches')
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
out = root / 'build/qa-fixture'
classes = out / 'classes'
libs = out / 'libraries'
if classes.exists():
    shutil.rmtree(classes)
classes.mkdir(parents=True, exist_ok=True)
libs.mkdir(parents=True, exist_ok=True)
classpath = []

def add_jar(path):
    classpath.append(str(path.resolve()))
    with zipfile.ZipFile(path) as archive:
        for entry in archive.namelist():
            if entry.endswith('.jar'):
                data = archive.read(entry)
                nested = libs / (hashlib.sha256(data).hexdigest()[:16] + '-' + Path(entry).name)
                nested.write_bytes(data)
                add_jar(nested)

add_jar(args.jar)
for path in sorted(args.mods.glob('*.jar')):
    add_jar(path)
loader = next((args.cache / 'modules-2/files-2.1/net.fabricmc/fabric-loader/0.19.5').glob('*/fabric-loader-0.19.5.jar'))
add_jar(loader)
with zipfile.ZipFile(args.cache / 'fabric-loom/26.3/minecraft-server.jar') as archive:
    for kind in ['libraries', 'versions']:
        for line in archive.read('META-INF/' + kind + '.list').decode().splitlines():
            digest, name, path = line.split('\t')
            data = archive.read('META-INF/' + kind + '/' + path)
            if hashlib.sha256(data).hexdigest() != digest:
                raise RuntimeError('Server bundle checksum mismatch: ' + name)
            target = libs / Path(path).name
            target.write_bytes(data)
            classpath.append(str(target))
subprocess.run([str(args.javac), '--release', '25', '-proc:none', '-implicit:none',
    '-sourcepath', str(root / 'qa/fixture'), '-cp', os.pathsep.join(classpath),
    '-d', str(classes), str(root / 'qa/fixture/SnowChecks.java')], check=True)
metadata = {'schemaVersion': 1, 'id': 'srm_qa_checks', 'version': '1.0.0',
    'name': 'Snow Real Magic QA checks (test only)', 'environment': 'server',
    'entrypoints': {'main': ['srm.qa.SnowChecks']},
    'depends': {'snowrealmagic': '*', 'fabric-api': '*', 'minecraft': '26.3'}}
jar = out / 'srm-qa-checks.jar'
with zipfile.ZipFile(jar, 'w', zipfile.ZIP_DEFLATED) as archive:
    archive.writestr('fabric.mod.json', json.dumps(metadata, indent=2) + '\n')
    for path in sorted(classes.rglob('*.class')):
        archive.write(path, path.relative_to(classes))
print(jar)
