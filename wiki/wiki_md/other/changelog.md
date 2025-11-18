---
title: Changelog
category: other
---

# Changelog

This page documents the changes and fixes made in Quantum Things, compared to the original Random Things mod, in reverse chronological order.

## v1.0.1

### Fixes

- **Fixed a crash when the Eclipsed Clock tried to access a font renderer that was not available**.

## v1.0.0

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

#### World Generation

- **Removed the unimplemented and unfinished Sekenada from worldgen** - Cleaned up incomplete features.

#### Item Collection

- **Fixed item duplication using the advanced item collector** - Prevents exploits with the advanced item collector. *(Courtesy of UniversalTweaks)*
- **Fixed anvil crafting voiding items** - Items are no longer lost when using anvil crafting. *(Courtesy of UniversalTweaks)*

#### Spectre Dimension

- **Fixed teleporting survival mode players to the Spectre dimension on servers could leave the player stalled out in the void** - Improved teleportation reliability. *(Courtesy of UniversalTweaks)*
- **Fixed an issue where it would rain in the Spectre dimension** - The Spectre Dimension now properly prevents weather.

#### Spectre Illuminator

- **Fixed Spectre Illuminator duplication** - Prevents duplication exploits.
- **Fixed Spectre Illuminator smelting full snow blocks** - Resolves [MC-88097](https://bugs.mojang.com/browse/MC/issues/MC-88097).
- **Fixed Spectre Illuminator hitbox being inaccurate** - Improved interaction accuracy.
- **Improved Spectre Illuminator animation performance** - Better frame rates when using Spectre Illuminators.
- **Improved Spectre Illuminator position finding performance** - Faster chunk lighting calculations.

#### Nature Core

- **Fixed Nature Core being able to spawn underwater** - Nature Cores now only spawn in appropriate locations.

#### Blocks

- **Fixed Nature and Water Chest not having breaking particles** - Visual feedback when breaking these chests.
- **Fixed Fertilized Dirt not being recognized as farmland by villagers** - Villagers can now properly recognize and use Fertilized Dirt.
- **Fixed torches and other attachable blocks being able to be placed on the side of the Rain Shield** - Rain Shields now have proper collision detection.
- **Fixed Rain Shield duplication** - Prevents duplication exploits.

#### Redstone

- **Fixed ConcurrentModificationException crash when using Redstone Interfaces** - Improved thread safety.
- **Fixed a crash where the Redstone Observer tried to incorrectly access a block state that was not a Redstone Observer** - Better error handling.

#### Other

- **Fixed Magic Beans growing infinitely in Cubic Chunks worlds** - Limited growth to 512 blocks to prevent performance issues.
- **Fixed Divining Rods not having proper descriptions** - Improved item tooltips.
- **Fixed Divining Rods not showing up in Creative search** - All rods are now searchable in Creative mode.
- **Fixed a crash where the Block Breaker tried to incorrectly access a block state that was not a Block Breaker** - Better error handling.
- **Fixed a crash when Biome Stone tried to access a biome that was not registered with BiomeDictionary** - Improved compatibility with custom biomes.

