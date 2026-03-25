---
title: Time in a Bottle
category: items
---

# Time in a Bottle

## Description

---

This item allows you to accelerate the rate at which blocks tick. It passively collects time while in your inventory which you can consume by right clicking a block with it. The first click requires 30 Seconds, the second 60, the third 120 the fourth 240 and the fifth 480. The speed at which the block ticks doubles with each click. The effect lasts 30 seconds. Time is stored globally per player, so all your bottles draw from the same pool.

## Commands

You can use the following commands to manage the stored time in a bottle:

- `/rt timeinabottle <playername> <add|query|set|subtract> [number]`

By default, number is interpreted as seconds. You can use the following suffixes to specify the unit: s, m, h, d. The command can be used by any players with operator permissions, and is meant to replace the old nbt system, where time could be read and changed from the item nbt.

### Examples

- `/rt timeinabottle player add 10m` - Add 10 minutes to player's Time in a Bottle.
- `/rt timeinabottle player query` - Query the stored time in a bottle for player.
- `/rt timeinabottle player set 1h` - Set the stored time in a bottle for player to 1 hour.
- `/rt timeinabottle player subtract 30s` - Subtract 30 seconds from player's Time in a Bottle.

## Crafting

---

![](../images/crafting_time-in-a-bottle.webp)

## Videos

---

<video controls>
  <source src="../videos/time-in-a-bottle.mp4" type="video/mp4">
</video>
