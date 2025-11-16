# Quantum Things

Quantum Things, a 1.12.2 continuation of Lumien231's [Random Things](https://github.com/lumien231/Random-Things)

## Changes and fixes

### Changes

- Added an ingame config menu.
- Added the ability to configure the chances of certain plants, features and loot to occur.
- Added the ability to configure values concerning the Nature Core.
- Added the ability to configure values concerning the Lotus.
- Added the ability to enable, disable, and add custom divining rods, and adjust the range.

### Fixes

- Removed the unimplemented and unfinished Sekenada from worldgen.
- Fixed item duplication using the advanced item collector ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/anvil/mixin/UTAnvilCraftFixMixin.java)).
- Fixed anvil crafting voiding items ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/anvil/mixin/UTAnvilCraftFixMixin.java)).
- Fixed teleporting survival mode players to the Spectre dimension on servers could leave the player stalled out in the void ([courtesy of UniversalTweaks](https://github.com/ACGaming/UniversalTweaks/blob/main/src/main/java/mod/acgaming/universaltweaks/mods/randomthings/teleport/mixin/UTSpectreHandlerMixin.java)).
- Fixed Spectre Illuminator duplication.
- Fixed Spectre Illuminator [smelting full snow blocks](https://bugs.mojang.com/browse/MC/issues/MC-88097).
- Fixed Nature Core being able to spawn underwater.
- Fixed Nature and Water Chest not having breaking particles.
- Fixed Magic Beans growing infinitely in Cubic Chunks worlds (limited to 512).
- Fixes Divining Rods not having proper descriptions.