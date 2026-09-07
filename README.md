# Better Content Fixes

Pack-owned compatibility and runtime patch mod for Forge `1.20.1`.

Runtime behavior includes pack-owned compatibility fixes, including permanent Epic Fight Battle mode,
ParCool directional double-tap dodge and recommended control migration, first-person player-limb hiding
that preserves Epic Fight held-item animations, automatic affirmative Explosion Overhaul scan decisions,
weight-based ReHooked mob grappling, and the pinned Dynamic Trees Aether 1.3.3 obsolete-branch tag repair.

The client control profile moves ParCool Fast Run to Shift, ParCool's contextual mouse actions to Mouse 5,
Ping Wheel to Mouse 4, and Epic Fight lock-on to Caps Lock. Existing customized bindings are preserved;
only exact legacy defaults are migrated once. ParCool's direct R dodge remains available.

## Common commands

```bash
./gradlew verifyFast
./gradlew verifyFull
./gradlew stageRuntimeJar
```

## Release artifact

Deploy the reobfuscated jar from:

- `build/libs/better-content-fixes-<version>.jar`

The pack currently tracks the canonical release jar in `mods/`, not source outputs or IDE runtime state.

## Community and support

For modpack and mod discussion, playtest feedback, and bug reports, join the [Better Content Discord](https://discord.gg/EkRnZbzqS9).

## Canonical identity

- Repository and Gradle project: `better-content-fixes`
- Mod ID and resource namespace: `better_content_fixes`
- Maven group: `com.bettercontent`
- Runtime artifact: `build/libs/better-content-fixes-<version>.jar`

The canonical identity is a clean break. Legacy mod IDs, resource namespaces, configuration paths, commands, network channels, and saved-data keys are not migrated or aliased.
