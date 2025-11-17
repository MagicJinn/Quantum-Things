# Quantum Things

Quantum Things, a 1.12.2 continuation of Lumien231's [Random Things](https://github.com/lumien231/Random-Things)

## Changes and fixes

### Changes

- Added an ingame config menu.
- Added the ability to configure the chances of certain plants, features and loot to occur.
- Added the ability to configure values concerning the Nature Core.
- Added the ability to configure values concerning the Lotus.
- Added Thermal Expansion Insolator support for Spectre Saplings.
- Added the ability to enable, disable, and add custom Divining Rods, and adjust the range.
- Added Divining Rod support for NetherEnding Ores, Silent's Gems, Galacticraft, Galacticraft Planets and Advent of Ascension
- Added Divining Rod sleeper support for Silent Gear and More Planets. (Sleeper Support is for mods lacking oreDict. Does nothing by default, can be enabled by modpacks).
- Gave Spectre Illuminator LOD levels.
- Made Rain Shields be able to be placed on any block, similar to an end rod.

### Fixes

- Removed the unimplemented and unfinished Sekenada from worldgen.
- Fixed item duplication using the advanced item collector ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/anvil/mixin/UTAnvilCraftFixMixin.java)).
- Fixed anvil crafting voiding items ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/anvil/mixin/UTAnvilCraftFixMixin.java)).
- Fixed teleporting survival mode players to the Spectre dimension on servers could leave the player stalled out in the void ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/teleport/mixin/UTSpectreHandlerMixin.java)).
- Fixed Spectre Illuminator duplication.
- Fixed Spectre Illuminator [smelting full snow blocks](https://bugs.mojang.com/browse/MC/issues/MC-88097).
- Fixed Spectre Illuminator hitbox being inaccurate.
- Improved Spectre Illuminator animation performance.
- Improved Spectre Illuminator position finding performance.
- Fixed Nature Core being able to spawn underwater.
- Fixed Nature and Water Chest not having breaking particles.
- Fixed Magic Beans growing infinitely in Cubic Chunks worlds (limited to 512).
- Fixed Divining Rods not having proper descriptions.
- Fixed ConcurrentModificationException crash when using Redstone Interfaces.
- Fixed `cannot be cast to TileEntityRedstoneObserver` crash.
- Fixed Fertilized Dirt not being recognized as farmland by villagers.
- Fixed a crash where the Block Breaker tried to incorrectly access a block state that was not a Block Breaker.
- Fixed an issue where it would rain in the Spectre dimension.
- Fixed Divining Rods not showing up in Creative search.
- Fixed torches and other attachable blocks being able to be placed on the side of the Rain Shield.