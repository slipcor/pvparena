# BetterClasses

## Description

This mod adds more to your classes, POTION effects and maximum player count!

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

\-

## Config settings

modules
- betterclasses
  - permEffects:
    - Tank: SLOW:3
    - Swordsman: none
    - Pyro: FIRE_RESISTANCE:1
  - maxPlayers
    - Tank: 2
    - Pyro: 4
  - neededEXPLevel:
    - Ranger: 0
    - custom: 0
    - Tank: 0
    - Swordsman: 0
    - Pyro: 0
  - respawnCommand:
    - Ranger: '/give %player% minecraft:arrow 1'

## Commands

- `/pa !bc [name]` \- show info about that class
- `/pa !bc [name] add [def]` \- add a potion effect (e.g. "add SLOW 2")
- `/pa !bc [name] remove [type]` \- remove all potion effects of type [type]
- `/pa !bc [name] clear` \- remove all potion effects from that class
- `/pa !bc [name] respawncommand [command] \- set a classes respawn command (empty to remove)`

## Potion Effect Types

[click me](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html)

## Dependencies

\-
