# Bound To Matter Fixes

Pack-owned compatibility and runtime patch mod for Forge `1.20.1`.

## Common commands

```bash
./gradlew test
./gradlew runGameTestServer
./gradlew clean build reobfJar
```

## Release artifact

Deploy the reobfuscated jar from:

- `build/libs/btmfixes-<version>.jar`
- `build/libs/btmfixes-<version>-all.jar`

The pack currently tracks the canonical release jar in `mods/`, not source outputs or IDE runtime state.
