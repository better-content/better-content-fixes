# Better Content Fixes

Pack-owned compatibility and runtime patch mod for Forge `1.20.1`.

Runtime behavior includes pack-owned compatibility fixes, including weight-based ReHooked mob grappling.

## Common commands

```bash
./gradlew verifyFast
./gradlew verifyFull
./gradlew stageRuntimeJar
```

## Release artifact

Deploy the reobfuscated jar from:

- `build/libs/better-content-fixes-<version>.jar`
- `build/libs/better-content-fixes-<version>-all.jar`

The pack currently tracks the canonical release jar in `mods/`, not source outputs or IDE runtime state.

## Community and support

For modpack and mod discussion, playtest feedback, and bug reports, join the [Better Content Discord](https://discord.gg/EkRnZbzqS9).

## Canonical identity

- Repository and Gradle project: `better-content-fixes`
- Mod ID and resource namespace: `better_content_fixes`
- Maven group: `com.bettercontent`
- Runtime artifact: `build/libs/better-content-fixes-<version>.jar`

The canonical identity is a clean break. Legacy mod IDs, resource namespaces, configuration paths, commands, network channels, and saved-data keys are not migrated or aliased.
