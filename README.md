# Better Content Fixes

Pack-owned compatibility and runtime patch mod for Forge `1.20.1`.

## Common commands

```bash
./gradlew verifyFast
./gradlew verifyFull
./gradlew stageRuntimeJar
```

## Release artifact

Deploy the reobfuscated jar from:

- `build/libs/bcfixes-<version>.jar`
- `build/libs/bcfixes-<version>-all.jar`

The pack currently tracks the canonical release jar in `mods/`, not source outputs or IDE runtime state.
