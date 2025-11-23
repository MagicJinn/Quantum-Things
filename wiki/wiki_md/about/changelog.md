---
title: Changelog
category: about
---

# Changelog

This page documents the changes and fixes made in Quantum Things, compared to the original Random Things mod, in reverse chronological order.

## 1.0.2 (in development)

### Changes

- Added a config option to configure the maximum number of animals allowed within the Nature Core's Animal Spawning radius.
- Added the ability to configure the Spectre Energy Injector capacity, Spectre Coil/Charger transfer rates, and whether the Genesis Spectre Coil generates energy or transfers it.
- Made the ID Card crafting recipe shapeless.
- Re-added Spectre Armor (WIP).

### Fixes

- Fixed Biome Sensor not working when held in off-hand.
- Fixed Nature Core, Bonemealing and Animal Spawning not being centered on the Nature Core (introduced in 1.0.0).
- Fixed plate item entities being way too large when dropped as items.

## v1.0.1

### Fixes

- Fixed a crash when the Eclipsed Clock tried to access a font renderer that was not available.

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

- Removed the unimplemented and unfinished Sekenada from worldgen.

#### Item Collection

- Fixed item duplication using the advanced item collector ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/anvil/mixin/UTAnvilCraftFixMixin.java)).
- Fixed anvil crafting voiding items ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/anvil/mixin/UTAnvilCraftFixMixin.java)).

#### Spectre Dimension

- Fixed teleporting survival mode players to the Spectre Dimension on servers could leave the player stalled out in the void ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/teleport/mixin/UTSpectreHandlerMixin.java)).
- Fixed an issue where it would rain in the Spectre Dimension.

#### Spectre Illuminator

- Fixed Spectre Illuminator duplication.
- Fixed Spectre Illuminator [smelting full snow blocks](https://bugs.mojang.com/browse/MC/issues/MC-88097).
- Fixed Spectre Illuminator hitbox being inaccurate.
- Improved Spectre Illuminator animation performance.
- Improved Spectre Illuminator position finding performance.

#### Nature Core

- Fixed Nature Core being able to spawn underwater.

#### Blocks

- Fixed Nature and Water Chest not having breaking particles.
- Fixed Fertilized Dirt not being recognized as farmland by villagers.
- Fixed torches and other attachable blocks being able to be placed on the side of the Rain Shield.
- Fixed Rain Shield duplication.

#### Redstone

- Fixed ConcurrentModificationException crash when using Redstone Interfaces.
- Fixed a crash where the Redstone Observer tried to incorrectly access a block state that was not a Redstone Observer.

#### Other

- Fixed Magic Beans growing infinitely in Cubic Chunks worlds (limited to 512).
- Fixed Divining Rods not having proper descriptions.
- Fixed Divining Rods not showing up in Creative search.
- Fixed a crash where the Block Breaker tried to incorrectly access a block state that was not a Block Breaker.
- Fixed a crash when Biome Stone tried to access a biome that was not registered with BiomeDictionary.
