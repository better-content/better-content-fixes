# Exhaustive Realistic Hands Policy

This directory preserves the retired block-by-block and tool-by-tool policy from the original
Realistic Hands implementation. It is outside `src/main`, is not packaged into the runtime jar,
and must not be treated as active behavior.

The live policy intentionally retains only the early-game no-tree-punching gate. Logs require an
item in `forge:tools/axes`; terrain, stone, ores, plants, leaves, and decorative blocks use normal
Forge harvesting behavior.
