# Minecraft 26.3 Fabric port

This experimental port starts from Snownee/SnowRealMagic `26.1-fabric` commit
`519993642969841626a0c75931543da9ef588a84` (Minecraft 26.1.2, mod 26.0.3),
the newest Minecraft-version Fabric branch available when the port was made.
It targets Minecraft **26.3**, Java **25**, Fabric Loader **0.19.5**, and Fabric API
**0.161.0+26.3**. The artifact is `SnowRealMagic-mc26.3-Fabric-26.0.3-port.2+26.3.jar`.

## Dependency and build

Kiwi is mandatory. There was no published Minecraft 26.3 Kiwi artifact, so this
port requires the companion [Kiwi port PR](https://github.com/Snownee/Kiwi/pull/116).
Its source is available at [THENATHE/Kiwi, port/fabric-26.3](https://github.com/THENATHE/Kiwi/tree/port/fabric-26.3).
Build the companion library first using Java 25:

```sh
git clone --branch port/fabric-26.3 https://github.com/THENATHE/Kiwi.git ../kiwi-26.3
(cd ../kiwi-26.3 && bash gradlew build)
bash gradlew clean build -PkiwiJar=../kiwi-26.3/build/libs/Kiwi-mc26.3-Fabric-26.0.20-port.1+26.3.jar
```

`kiwiJar` supplies both the compile dependency and annotation processor. Without
it, Gradle uses the upstream Kiwi coordinate for API compilation only; that older
artifact is not suitable for the production Minecraft 26.3 runtime. Install the
companion Kiwi JAR and Fabric API alongside Snow! Real Magic!.

Cloth Config 26.3.159 and Mod Menu 21.0.0 provide optional configuration UI.
Optional compatibility code now compiles against Jade 26.3.3, Diagonal Fences and
Diagonal Walls 26.3.0, Diagonal Blocks 26.3.0, and Puzzles Lib 26.3.8. Serene Seasons
and GlitchCore use exact Fabric Modrinth release IDs `V9PxJPuw` and `aaUghyGp`,
respectively, to avoid ambiguous same-number NeoForge Maven artifacts.

## Implementation

- Adapt loot entry codecs to the new single-entry container and optional holder
  condition/modifier model; retain contained-block loot generation.
- Propagate `BonemealSource`, migrate prediction-aware server drops, registry
  names, block-based slab classification, and current data-generation APIs.
- Update renderer geometry invalidation and quad emitter shading/glint methods.
- Update world-generation and block-item injection signatures, and remove an
  unused invoker for the obsolete block destroy-particle signature.
- Preserve snow around failed 2x2 sapling growth using the new TreeGrower flow,
  unwrapping saplings for matching and restoring their raw blocks and snow layers
  after a failed growth attempt.
- Gate optional Diagonal Blocks and Serene Seasons mixins on the installed mods.
  The base mod can launch without either optional integration.

## Validation

`build` succeeds with Java 25 and the companion Kiwi JAR, including Loom access
widener validation. Production Fabric 26.3 dedicated-server runs pass startup,
datapack reload, and clean shutdown with only Fabric API and Kiwi installed.
The test-only fixture additionally verifies:

- conversion and contained-block preservation for oak fences, cobblestone walls,
  oak slabs, and oak stairs;
- two retained layers for layered variants, the slab snow overlay and stair
  decoration behavior inherited from upstream;
- correct original-block item drops through the ported loot entry;
- failed, ceiling-obstructed 2x2 dark-oak growth restores all four contained
  saplings and their two snow layers;
- all checks repeat successfully after datapack reload.

The production client was also launched with Kiwi and Cloth Config: resources
loaded, the Camera, SnowLayerBlock and Variant mixin targets were explicitly
loaded, and both Kiwi and Snow! Real Magic! configuration screens rendered.
The client then joined the isolated server world, synchronized all four snowy
block entities, rendered them for 120 world ticks and exited normally. A captured
screenshot was inspected for the snowy fence, wall, slab and stairs.
The same core server regressions also pass with official Diagonal Fences,
Diagonal Walls, Puzzles Lib and Forge Config API Port installed.
A combined server run with Immersive Snow Reloaded, official Serene Seasons and
GlitchCore also passed winter/summer commands, datapack reload and shutdown.
These checks are automated smoke/regression coverage, not a claim of exhaustive
manual gameplay or shader/modpack compatibility.

## Reproduce server checks

The QA scripts require a populated Gradle cache from the build, including the
Minecraft 26.3 server bundle and Fabric Loader libraries. Place only the matching
Kiwi and Fabric API JARs in `build/qa-deps/` and use Java 25:

```sh
python3 qa/build_fixture.py \
  --jar build/libs/SnowRealMagic-mc26.3-Fabric-26.0.3-port.2+26.3.jar \
  --mods build/qa-deps --javac "$JAVA_HOME/bin/javac"
python3 qa/server_smoke.py \
  --jar build/libs/SnowRealMagic-mc26.3-Fabric-26.0.3-port.2+26.3.jar \
  --mods build/qa-deps --fixture build/qa-fixture/srm-qa-checks.jar \
  --java "$JAVA_HOME/bin/java"
```

Each run creates an isolated world, binds to localhost on an unused port, records
artifact hashes in `launch-audit.json`, and writes `result.json` and `console.log`
under `build/qa-run/`. The fixture creates test blocks and must never be installed
in a real world or distributed as a runtime dependency.
