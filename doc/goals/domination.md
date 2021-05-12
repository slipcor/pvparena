# Domination

> ℹ This goal is designed for **team** gamemode

## Description

The game is simple :

There are one or several flags that can be claimed by players.
When players are in the range of a flag, a load bar appears and they will claim the flag after few seconds.
Obviously, if a player on another team come also within the flag range, loading stops.

When a flag is claimed, it take team color. Player of other team can get it back by the same process.
In this case, flag will be released in a first time (it takes white color) and only then it will take color of
second team.

Each claimed flag gives points every few seconds (tickinterval) that add up to a score, the first team to have enough 
points wins.

## Setup

Flags have to be added.In order to do that, use `/pa [arenaname] flag`. This toggles edit mode. 
Don't forget to type command again in order to exit edit mode after setting the flags. 
Set them by clicking the flag type (WOOL by default).

Given that flag must be able to change color, you can use the following blocks as flagtype 
(color prefix doesn't matter):                                                                                  
* WHITE_BANNER
* WHITE_CARPET
* WHITE_CONCRETE
* WHITE_CONCRETE_POWDER 
* WHITE_GLAZED_TERRACOTTA 	
* WHITE_SHULKER_BOX
* WHITE_STAINED_GLASS 
* WHITE_STAINED_GLASS_PANE
* WHITE_TERRACOTTA
* WHITE_WALL_BANNER

I suggest you to try glass block with a beacon bottom the flag. When flag will be claimed, glass blocks will change its
color, altering beacon light ray in the same time :wink:

## Config settings  

- spamoffset => after how many updates should the arena announce? (default: 3)
- claimrange => how near need players to be? (default: 3)
- dlives => domination lives (max points). (default: 10)
- onlywhenmore => only score when more than half of the points are claimed. (default: false)
- particlecircle => creates a circle of particles around each flag to mark capture radius. (default: true)
- tickinterval => the amount of ticks to wait before doing an update. (default: 60 = 3 seconds)
- tickreward => the amount of points to give for each score. (default: 1)

<br>

> ⚙ **Technical precision:**  
> This goal has to check for player's position. Based on the player and checkpoint count this can lag your server. 
> Unfortunately, there is no other way to determine a claimed checkpoint.

