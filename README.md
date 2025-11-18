# Quantum Things

[![Modrinth: Quantum Things](https://img.shields.io/badge/Modrinth-Quantum_Things-00ae5d?logo=modrinth)](https://modrinth.com/mod/quantum-things)
[![CurseForge: Quantum Things](https://img.shields.io/badge/CurseForge-Quantum_Things-f16437?logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/quantum-things)

## Quantum Things, a 1.12.2 continuation of Lumien231's [Random Things](https://github.com/lumien231/Random-Things).

### WARNING: When upgrading from Random Things, make sure to delete the randomthings.cfg file, and re-check all config options. Some may have changed, been reset, removed or added

![Quantum Things](icon.png)

**The majority of the credit goes to Lumien231, who created the absolutely monolithic Random Things (MIT). Additional credit goes to UniversalTweaks (MIT) for 3 of the bug fixes.**

## About Random Things

Random Things is a miscellaneous mod that adds a diverse collection of utility items, blocks, and gameplay enhancements. It includes features such as automated item collection, crop growth acceleration with Fertilized Dirt, a personal pocket dimension called the Spectre Dimension, Spectre Coils for wireless energy transfer, Divining Rods for locating ores, various redstone utilities, decorative blocks, and numerous quality-of-life improvements. The mod is designed to complement other mods by adding convenience features and new gameplay without adhering to a specific theme. To put it in Lumien's words:

> Random Things is a collection of features that i thought would be neat. The mod doesn't really have a central topic so it's best played alongside other mods.

The goal of Quantum Things is to provide continued support for Random Things, such as new features, bug and crash fixes, and compatibility with other mods, while saying true to the original design goal and intent of the mod.

## Changes and fixes

### Changes

- Added an ingame config menu.
- Added the ability to configure the chances of certain plants, features and loot to occur.
- Added the ability to configure values concerning the Nature Core.
- Added the ability to configure values concerning the Lotus.
- Added the ability to enable or disable the Spectre Sapling.
- Added the ability to enable or disable the Spectre Dimension.
- Added Thermal Expansion Insolator support for Spectre Saplings.
- Added Bonsai Trees support for Fertilized Dirt as a Bonsai Pot soil.
- Added a Quartz Divining Rod.
- Added the ability to enable, disable, and add custom Divining Rods, and adjust the range.
- Added Divining Rod support for NetherEnding Ores, Silent's Gems, Galacticraft, Galacticraft Planets, Advent of Ascension, Aether and DivineRPG.
- Added Divining Rod sleeper support for Silent Gear, More Planets, Aether and Aether II. (Sleeper Support is for mods lacking oreDict. Does nothing by default, can be enabled by modpacks).
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
- Fixed a crash where the Redstone Observer tried to incorrectly access a block state that was not a Redstone Observer.
- Fixed Fertilized Dirt not being recognized as farmland by villagers.
- Fixed a crash where the Block Breaker tried to incorrectly access a block state that was not a Block Breaker.
- Fixed an issue where it would rain in the Spectre dimension.
- Fixed Divining Rods not showing up in Creative search.
- Fixed torches and other attachable blocks being able to be placed on the side of the Rain Shield.
- Fixed Rain Shield duplication.
- Fixed a crash when Biome Stone tried to access a biome that was not registered with BiomeDictionary.

### Reporting Issues

If you encounter any issues, please report them to the [issue tracker](https://github.com/MagicJinn/Quantum-Things/issues). Do **not** report issues to the original Random Things repository. Lumien231 is no longer actively developing Random Things, and new issues on the original repository are unlikely to be addressed.