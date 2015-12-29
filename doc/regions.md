# Regions

Regions allow further functions to be added to your game play.

## Region Creation

All following commands assume you either have edit mode enabled or just one arena in place !

Start creating an arena region with `/pa !r`
Now select a region by holding your arena wand (a STICK by default) and left click for position 1 and right click for position 2. Detailed information about the special region shapes, i.e. which points to select, check out the tutorial.

After selecting those points, use `/pa !r [regionname] [regionshape]` to save the region.
Region Shapes

The following (self explanatory) region shapes exist:
- CUBOID (default)
- SPHERIC
- CYLINDRIC

Of course, different shapes cover different areas. Note that a cylinder means a room like a can, so a standing cylinder.

## Region Types

Set a region's type with `/pa !rt [regionname] [regiontype]`

The following Region Types exist:

- CUSTOM (default) => does nothing
- WATCH => is the place where spectators should be, BattleFieldGuard will kick spectators not being part inside of one WATCH region
- LOUNGE => is the place where fighters select their class. It currently has no use, but might do in the future
- BATTLE => the most important type. It adds battlefield reservation (for overlapping arenas, only caring about that actually battle region), region protection, restoring and check if fighters are in the right place.
- EXIT => the region where players should be after exiting the arena, no functionality atm
- JOIN => the region where players should be when joining, see Configuration page, enforcement is disabled by default
- SPAWN => a spawn region where players are randomly placed in when spawning or respawning 

## Region Flags

Further customisation of region functionality with `/pa !rf [regionname] [regionflag]`
Those are valid Region Flags:

- NOCAMP - players that don't move inside this region will be hurt
- DEATH - players entering this region will die
- WIN - players entering this region will WIN the game
- LOSE - players entering this region will LOSE the game
- NODAMAGE - players being in this region will receive no damage 

## Region Protection

The BATTLE Region parses region protection flags (other regions can assigned protections, too, but atm they don't really use them). Set them via `/pa !p [regionname] [protection]` - you can add on, off, yes, no, true, false to specify a setting, or just as just told to toggle the state. Note that there is an "ALL" protection node that triggers/sets all protection nodes
Valid protections are:

- BREAK - Block breaking
- FIRE - Fire (spreading)
- MOBS - Mob spawning
- NATURE - Environment changes (leaves, shrooms, water, lava)
- PAINTING - Painting placement/destruction
- PISTON - Piston triggering
- PLACE - Block placement
- REDSTONE - Redstone current change
- TNT - TNT usage
- TNTBREAK - TNT block break

