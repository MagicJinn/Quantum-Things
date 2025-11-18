---
title: Creative Player Interface
category: blocks
---

# Creative Player Interface

## Description

---

When Open Computers is installed this block provides a component called “creative_player_interface” with the following functions:

- getPlayerName() : Returns the name of the player that’s currently connected to this player interface.
- getPlayerUUID() : Returns the uuid of the player that’s currently connected to this player interface.
- setPlayerName(username) : Sets the name of the player that’s connected to the player interface.
- setPlayerUUID(uuid) : Sets the uuid of the player that’s currently connected to the player interface.
- isCurrentlyConnected(): Returns a boolean that’s based on whether the player interface is currently connected to the player’s inventory. (False if the player is for example offline)