# Domination

## Description

Domination is designed to use teams. As always, it defaults to red and blue. 

It activates Flags (to set) that can be claimed by players. In order to do that, they have to stand near that flag, 
alone or at least only one team. Flags can be unclaimed if a different team or multiple teams are too close to a claimed flag. 

Each claimed flag gives points every 3 seconds that add up to a score, the first team to have enough points wins.

## Setup

Flags have to be added.In order to do that, use `/pa [arenaname] flag`. This toggles edit mode. 
Don't forget to exit it again after setting the flags. Set them by clicking the flag type (WOOL by default). 
Don't click with your wand, just click with your hand or anything else.

## Config Settings  

- spamoffset => after how many updates should the arena announce? (default: 3)
- claimrange => how near need players to be? (default: 3)
- dlives => domination lives ( max points )
- onlywhenmore => only score when more than half of the points are claimed
- tickinterval => the amount of ticks to wait before doing an update (default: 60 = 3 seconds)
- tickreward => the amount of points to give for each score (default: 1) 

## Warnings

This game mode has to check for player's position. Based on the player count this can lag your server. But, how else should I determine a claimed flag? :p

## Supported Game Modes

Supports both game modes, but we suggest you use the team game mode!

## YouTube video

[click me](http://www.youtube.com/watch?v=Xi7yNURxAjw)
