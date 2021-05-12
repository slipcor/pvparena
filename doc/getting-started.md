# Getting Started

<br>

> **🚩 Syntax tip:**  
> [required] indicates a required parameter  
> (optional) indicates an optional parameter
> 


## Foreword: what's an arena?

Before creating your first arena, you have to understand what's.  
Arena is immaterial, it's a game configuration you create with goals, teams, classes, etc. It references your arena
config file created at `/plugins/pvparena/yourArenaName/config.yml`. So your arena defines how your game takes place.

In this arena, only two things are bind to locations: spawn point and regions. So if you need to move your arena in 
another place, don't destroy it, just redefine your spawn points and your regions.

<br>

## 1. Create the arena

Just type this command to create your arena:

`/pa create [newArenaName] (free)`

By default your arena will work with a team system. If you add the `free` option, your arena will work on a *Free for
all* (FFA) game mode.

> **🚩 Tip:**  
> This parameter can be changed using [/pa gamemode](commands/gamemode.md) command or in your config file by 
> setting `general.type` parameter to `free` or `none`. 

<br>

## 2. Set goals for the arena

By default, your arena will use [TeamLives](goals/teamlives.md) goal if your arena is in team mode and 
[PlayerLives](goals/playerlives.md) goal otherwise. If you're ok with this, go to the next point, otherwise please
continue reading.

You can choose a custom goal [in the list](goals.md) and set it for your arena with the command:
`/pa [arenaName] goal [goalName]`

Each goal has its own setup, so take the time to read [documentation](goals.md) of the goal you want to use.

You will find more information about this command [on this link](commands/goal.md).

> **Reminder:**  
> Don't forget to remove _TeamLives_ goal if you don't use it 😉

<br>

## 3. Set spawn points

Now you have to create game spawn points by using this command:  
`/pa [arenaName] spawn [spawnType]`

##### Team arenas
For team arenas, spawn types are: `[team]spawn`, `[team]lounge`, `spectator`, `exit`.  
So by default you need: 2 spawns (red & blue) / 2 lounges (red & blue) / 1 spectator zone / 1 exit.

##### Free arenas
For **free** (FFA) arenas, spawn types are: `spawn[x]`, `lounge`, `spectator`, `exit`.  
By default you need: 4 spawns (spawn1, spawn2, spawn3, spawn4) / 1 lounge / 1 spectator zone / 1 exit.

<br>

> **🚩 Tips:**
>- In free arenas, you can create as many spawn points as you want.
>- You can use `/pa spawn` command again to move a spawn point

<br>

## 4. Create the battle region

> *This step is optional but really useful in mostly configurations*

Now create the battle region with this command:

`/pa [arenaName] region`

It enables selection mode. Equip your hand with a **stick** and set your region with left and right click. 

Then type :

`/pa [arenaName] region [yourNewRegionName]`

Finally, specify your region type :

`/pa [arenaName] regionType [regionName] BATTLE`

> **🚩 Tips:**
> - By default your region is protected from block destruction and placing
> - Get a look to [the region documentation page](regions.md) to improve your arena regions

<br>

## 5. Place required items in the lounge

By default, four classes already exist : Swordsman, Tank, Pyro and Ranger.  
You can chose to keep these classes or create new ones with the the [class command](commands/class.md).

Then simply place signs near your **lounge** spawn point(s) and write the class names on the first line.

Place the signs in each lobby, and an iron block (configurable). 
The iron block is the default ready block that players can click on when they are ready. The match begins
when all players
are ready.

> **🚩 Tips:**
> - Players can can choose their class with `/pa arenaclass [className]` command
> - You can set a default class using the config parameter `autoClass`
> - Players can also be ready typing `/pa ready`, that's why ready block is not mandatory

<br>

## 6. Join the arena!

Your first arena was created! Join the game with:

`/pa [arenaName] (join) (teamName)`

> **🚩 Tip:**  
> If you just type `/pa [arenaName]` your team will be randomly selected.