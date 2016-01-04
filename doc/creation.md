
## Basic creation

_\[Required] (Optional) Arena name is optional but if you have more than one arena it is required._

### 1. Create the arena.

`/pa create [Arena Name] (Legacy Type)`

Valid types are: 
- team 
- teamdm 
- dm 
- free 
- ctf 
- ctp 
- spleef 
- tank
- sabotage

### 2. Set spawns for the arena.

/pa (Arena Name) spawn [spawntype]
Types are: [team]spawn / [team]lounge / spectator / spawn[x] (ffa only)

By default you need: 2 spawns (red & blue) / 2 lounges (red & blue) / spectator zone.

### 3. Create the battle region.

`/pa (Arena Name) region`

The default region setting tool is a stick. Set your region with left and right click. 

Then:

`/pa (Arena Name) region [Region Name] (Region Shape)`

Valid shape types: 

- cuboid 
- spheric 
- cylindric

### 4. Place required items in the lounge

Simply place the signs and on the first line put the class names.

Default classes are: Swordsman / Tank / Pyro / Ranger

Add more classes with the [class command](commands/class.md)

Place the signs in each lobby, and an iron block (configurable). The iron block is the default ready block, and you push it when your ready to start the match.

:triangular_flag_on_post: Tip : You can set a default class using the parameter `AutoLoad`.

### 5. Join the arena!

`/pa [Arena Name] (join) (teamname)`


See the [video tutorial](https://www.youtube.com/watch?v=yyPJ6vlv09s)