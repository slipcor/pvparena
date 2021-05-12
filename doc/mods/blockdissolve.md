# BlockDissolve

## About

Blocks under the player will dissolve just few milliseconds after they walk on it.
Use it with [PlayerLives](../goals/playerlives.md) goal to create TNT Run arenas or 
spice up your spleef games !

## Config settings

- modules.blockdissolve.materials \- the material to dissolve (default: SNOW and each kind of WOOL)
- modules.blockdissolve.startseconds \- the seconds to count down before the match starts (default: 10)
- modules.blockdissolve.ticks \- the ticks after what time the block under the player should dissolve (20 ticks = 1 second)
  (default: 40). 

<br>

> **🚩 Tips:**  
>- BlockDissolve is compatible with [BlockRestore](./blockrestore.md), so you can use it to regen the floor after each game.
>- If you want to create a TNT Run arena, set ticks parameter to **8**
