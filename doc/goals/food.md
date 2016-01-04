# Food

## Description

Your players are hungry! The first team gathering enough cooked food items (of their type) wins the game.

The following food types exist:

- RAW_BEEF => COOKED_BEEF
- RAW_CHICKEN => COOKED_CHICKEN
- RAW_FISH => COOKED_FISH
- POTATO_ITEM => BAKED_POTATO
- PORK => GRILLED_PORK 

Note: the module does NOT include coal or furnaces, you have to manage that on your own :)

## Setup

You have to prepare chests. To define those, use `/pa [arenaname] [teamname]foodchest` \- this enables setting. 

Finish the setting by clicking the chest that should be the team's chest. 

This chest will be checked for incoming and outgoing food items (of the team type) You can optionally prepare furnaces so that a team can ONLY use this furnace. 

Set this by `/pa [arenaname] [teamname]foodfurnace` and hit the furnace. Teams not having a corresponding furnace will be able to access all of them. Note that one furnace can be set multiple times ! 

So red/blue can share a furnace and green/yellow :) Just set the same spot for multiple teams.

## Config Settings

- fmaxitems \- the item count that triggers win (default: 50)
- fplayeritems \- the item count players receive on start and respawn \- (default: 50)
- fteamitems \- the item count the team receives on start, divided by team members \- (default: 100) 

## Supported Game Modes

Only supports team game mode!

## YouTube video

[click me](http://www.youtube.com/watch?v=ntloY1BTKHQ)
