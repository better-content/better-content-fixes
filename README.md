# Better Content Fixes

Pack-owned compatibility and runtime patch mod for Forge `1.20.1`.

The former exhaustive Realistic Hands policy is retained under
`quarantine/realistic-hands-exhaustive-policy/`. Runtime behavior keeps only the no-tree-punching
log gate.

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

## Community and support

For modpack and mod discussion, playtest feedback, and bug reports, join the [Better Content Discord](https://discord.gg/EkRnZbzqS9).
