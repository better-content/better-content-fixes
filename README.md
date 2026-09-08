# Better Content Fixes

Pack-owned compatibility and runtime patch mod for Forge `1.20.1`.

The Better Caves compatibility hook clips only block-carve attempts outside a chunk's legal vertical
range. This preserves Better Caves and C2ME generation at valid heights while preventing Better Caves
`2.0.6` from writing negative `CarvingMask` indices in zero-minimum dimensions such as
`lostcities:lostcity`.

Runtime behavior includes pack-owned compatibility fixes, including permanent Epic Fight Battle mode,
ParCool directional double-tap dodge and recommended control migration, first-person player-limb hiding
that preserves Epic Fight held-item animations, automatic affirmative Explosion Overhaul scan decisions,
weight-based ReHooked mob grappling, and the pinned Dynamic Trees Aether 1.3.3 obsolete-branch tag repair.

The client control profile moves ParCool Fast Run to Shift, ParCool's contextual mouse actions to Mouse 5,
Ping Wheel to Mouse 4, and Epic Fight lock-on to Caps Lock. Existing customized bindings are preserved;
only exact legacy defaults are migrated once. ParCool's direct R dodge remains available.

When Better Content Threads is installed, accepted server-side ParCool starts for the configured dodge,
fast-run, and contextual actions emit optional learning signals. The first action opens one persistent
episode; only a different qualifying action completes it, using the original correlation token.

On a client with EMI and TConstruct, a profile that has no `emi.json` starts with the TConstruct
Part Builder and Tinker Station pinned in EMI. The file is created before EMI initializes. Existing
EMI data is never parsed, merged, or replaced, so players can remove either favorite permanently.

Sleeping Overhaul's timelapse keeps simulating the world but is paced to 800 ticks per second by
default, making a full 12,000-tick night take about 15 seconds. Ordinary item pickup extends one
block horizontally without changing vertical reach, and moving living entities have one 10% roll
per block entry to remove vanilla grass or tall grass. These policies are configurable in the
common Better Content Fixes config.

When Better Content Threads is installed, each participating sleeper emits one correlated
`sleep_started/night` to `sleep_finished/simulated_time` episode across a successfully simulated night.

The foreground tick governor records active server-tick p50/p95/p99/max over a 100-tick window.
When p95 exceeds 50 ms or a tick exceeds 100 ms, it uses Distant Horizons 2.4.5's API to pause only
optional distant generation. It clears only its own in-memory override after 600 consecutive ticks
at p95 40 ms or better, never rewrites DH configuration, and never enables a user-disabled setting.
Use the permission-level-2 `/better_content_fixes performance_status` command to inspect the current
window and governor state. C2ME scheduling and IO settings are not changed by this service.

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
