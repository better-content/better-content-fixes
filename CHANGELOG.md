# Changelog

## Unreleased

- Serialized Dynamic Trees 1.4.10 Poisson-disc chunk-data access on its existing provider monitor, preventing C2ME asynchronous chunk loads from corrupting the shared cache without disabling asynchronous I/O pack-wide.
- Added a Skyrim-style dynamic survival HUD with independent five-second holds and half-second fades for vanilla, Thirst Was Taken, and Cold Sweat elements, plus danger latching and sneak-to-peek behavior.
- Allowed ReHooked grapples to attach to mobs and tug both endpoints according to entity size and knockback resistance.
- Rebuilt and fully resynchronized materialized TConstruct tools after login datapack sync so durability and other dynamic stats are authoritative on the first client inventory update.
- Restored Polymorph recipe selection and persistent choices in TConstruct Crafting Stations.
- Added configurable same-direction double-tap rolling through Combat Roll, suppression while sneaking, a rebindable in-game toggle key, and replacement of vanilla forward double-tap sprint while preserving the sprint key.
- Capped Explosion Overhaul concussion hold and accumulated durations at 45 seconds for large and repeated blasts.
- Restored vanilla daylight burning for phantoms while keeping other daylight-sensitive mobs protected.
- Replaced random wandering-trader arrivals with one recurring themed world visitor on a two-day initial and five-day repeat schedule.
- Allowed AmbientSounds effects rejected by Minecraft's full sound-channel pool to retry instead of remaining permanently silent.
- Prevented Weather2 custom fog from overriding an active Oculus shader pack's sky and fog rendering.
- Restored vanilla hopper extraction from Sophisticated Storage barrels and limited barrels while preserving their input/output rules.
- Hid the obsolete TConstruct Part Builder pattern slot while routing sand-cast crafting through its visible input.
- Standardized the repository, artifact, package, and mod namespace under the canonical Better Content identity; this is a clean break without legacy aliases or migration.
