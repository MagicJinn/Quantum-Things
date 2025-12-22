---
title: Changelog
category: about
---

# Changelog

This page documents the changes and fixes made in Quantum Things, compared to the original Random Things mod, in reverse chronological order.

## 1.0.6 (In Development)

### Changes

- Added the ability to configure a blacklist of entities that cannot be captured by the Summoning Pendulum.
- Creative players can now capture any entity with the Summoning Pendulum, bypassing all restrictions.
- Added a failure sound when the Summoning Pendulum blocks the capture of an entity.
  
### Fixes

- Fixed the Summoning Pendulum being able to capture entities that are targeting you, but are not classified as monsters.
- Fixed the Entity Detector not properly updating its power level through blocks when the power mode changed (introduced in 1.0.5).
- Fixed a crash when the Item Filter tried to compare to an empty item stack.
- Fixed Spectre Energy Injectors accepting infinite energy when confronted with high energy inputs, causing them to eat the energy.

## 1.0.5

### Changes

- Added an Imbuing Station recipe for Spectre Armor, requiring a Diamond Armor piece and 3 Spectre Ingots (NBT is transferred).
- Added a Proportional power mode to the Entity Detector.

### Fixes

- Further improved Spectre Illuminator performance.
- Improved Spectre Illuminator isIlluminated() performance [courtesy of Desoroxxx](https://github.com/MagicJinn/Quantum-Things/pull/17).
- Improved Spectre Illuminator setIlluminated() performance.
- Fixed the Imbuing Station not dropping its contents when broken.
- Fixed a crash when the Chunk Analyzer tried to scan a block that doesn't have a valid metadata.
- Fixed incorrect Divining Rod descriptions (introduced in 1.0.0).
- Fixed Entity Detector having no clear difference between Weak and Strong power modes.
- Fixed Spectre Leaves having their Decay flag set to true when placed.
- Fixed Spectre Leaves dropping Evil Tears when destroyed (introduced in 1.0.0).

## 1.0.4

### Fixes

- Fixed a STUPID crash immediately when starting a server. STUPID STUPID STUPID (Introduced in 1.0.3).

## 1.0.3

### Changes

- Added the ability to configure whether Golden Chickens should produce gold ingots automatically (legacy), or only when fed gold ore (current).
- Added the ability to configure the maximum number of blocks a Potion Vaporizer can affect.
- Reduced the number of particles spawned by the Potion Vaporizer by several orders of magnitude.
- Changed the Ancient Furnace to change blocks in a circular area around it, rather than a diamond shape.
- Re-added Spectre Armor (with no recipe, to be added later, or by modpacks).
- Added the ability to configure the transparency effect of the Spectre Armor.

### Fixes

- Fixed Golden Chickens being able to consume seeds, even though they cannot be bred.
- Improved the performance of the Potion Vaporizer room detection algorithm.
- Fixed a typo in the language file (brickss > bricks).
- Updated the translation in the language files. (Machine translated, may contain errors, please report any issues)
- Fixed Potion Vaporizer not dropping its contents when broken.
- Fixed Custom Workbench dropping its item when broken in Creative Mode.
- Fixed Runic Dust breaking all pieces at once in Creative Mode instead of one piece at a time.
- Fixed Runic Dust dropping its item when broken in Creative Mode.
- Fixed Precious Emeralds being removed from your inventory when giving them to villagers in creative mode.
- Fixed beans not being able to be planted on non-vanilla blocks, even if the block could sustain plants.

## 1.0.2

### Changes

- Added a config option to configure the maximum number of animals allowed within the Nature Core's Animal Spawning radius.
- Added the ability to configure the Spectre Energy Injector capacity, Spectre Coil/Charger transfer rates, and whether the Genesis Spectre Coil generates energy or transfers it.
- Made the ID Card crafting recipe shapeless.
- Re-added Spectre Armor (WIP).
- Re-added Biome Painter and Biome Capsule from 1.7.10.
- Re-added Obsidian Stick from 1.7.10, now used for Spectre Tools and the Biome Painter.
- Added the Spectre Hoe.

### Fixes

- Fixed Biome Sensor not working when held in off-hand.
- Fixed Nature Core, Bonemealing and Animal Spawning not being centered on the Nature Core (introduced in 1.0.0).
- Fixed plate item entities being way too large when dropped as items.
- Fixed a bug that caused player graves to not be created in Corail Tombstone (introduced in 1.0.0).

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

- Removed the unimplemented and unfinished Sekenada from worldgen.
- Fixed item duplication using the advanced item collector ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/anvil/mixin/UTAnvilCraftFixMixin.java)).
- Fixed anvil crafting voiding items ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/anvil/mixin/UTAnvilCraftFixMixin.java)).
- Fixed teleporting survival mode players to the Spectre Dimension on servers could leave the player stalled out in the void ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/teleport/mixin/UTSpectreHandlerMixin.java)).
- Fixed an issue where it would rain in the Spectre Dimension.
- Fixed Spectre Illuminator duplication.
- Fixed Spectre Illuminator [smelting full snow blocks](https://bugs.mojang.com/browse/MC/issues/MC-88097).
- Fixed Spectre Illuminator hitbox being inaccurate.
- Improved Spectre Illuminator animation performance.
- Improved Spectre Illuminator position finding performance.
- Fixed Nature Core being able to spawn underwater.
- Fixed Nature and Water Chest not having breaking particles.
- Fixed Fertilized Dirt not being recognized as farmland by villagers.
- Fixed torches and other attachable blocks being able to be placed on the side of the Rain Shield.
- Fixed Rain Shield duplication.
- Fixed ConcurrentModificationException crash when using Redstone Interfaces.
- Fixed a crash where the Redstone Observer tried to incorrectly access a block state that was not a Redstone Observer.
- Fixed Magic Beans growing infinitely in Cubic Chunks worlds (limited to 512).
- Fixed Divining Rods not having proper descriptions.
- Fixed Divining Rods not showing up in Creative Search.
- Fixed a crash where the Block Breaker tried to incorrectly access a block state that was not a Block Breaker.
- Fixed a crash when Biome Stone tried to access a biome that was not registered with BiomeDictionary.
