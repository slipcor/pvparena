# Beacons

## Description

The Beacons goal is designed to use teams. As always, it defaults to red and blue.

It activates GLASS (to set) that can be claimed by players. To indicate claim status, a Beacon block will be spawned below the block (and remain there!!)
In order to claim, they have to stand near the active beacon light, alone or at least only one team.
Beacons can be unclaimed if a different team or multiple teams are too close to a claimed beacon.

A claimed beacon gives points every few seconds (tickinterval) that add up to a score, the first team to have enough points wins.

## Setup

The GLASS blocks have to be added. In order to do that, use `/pa [arenaname] beacon`. This toggles edit mode.
Don't forget to exit it again after setting the beacons. Set them by clicking the GLASS blocks which will color according to claim status.

## Config settings  

- spamoffset => after how many updates should the arena announce? (default: 3)
- claimrange => how near need players to be? (default: 3)
- blives => claim lives ( max points to achieve )
- tickinterval => the amount of ticks to wait before doing an update (default: 60 = 3 seconds)
- tickreward => the amount of points to give for each score (default: 1)
- changeseconds => the amount of seconds after which the server will calculate a new active beacon (default: 30, set to -1 to disable)
- changeonclaim => change the active beacon as soon as it is claimed (and add the score)

## Warnings

This game mode has to check for player's position. Based on the player count this can lag your server. But, how else should I determine a claimed beacon? :p

## Supported Game Modes

Supports both game modes, but we suggest you use the team game mode!
