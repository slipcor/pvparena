# Spawn Command

## Description

Set an arena spawn to your current position, including orientation !

## Usage Examples

Command |  Definition
------------- | -------------
/pa ctf spawn redspawn   | sets the red team's spawn of the arena "ctf"
/pa free spawn spawnEAST | sets "spawnEAST" of the arena "free"

## Details

If you get a message "spawn unknown", this is probably because you did not install / activate a goal / module ; be sure that you install and activate stuff you want to add, 
e.g. the "Flags" goal, or the "StandardSpectate" module...

## Spawn Offset

Since v1.3.1.31 you can define unique offsets for each spawn name, in order to not be placed on the block center but rather one edge:

- /pa {arenaname} spawn [spawnname] offset X Y Z

For example 0.5 0 0.5 as X Y Z would work setting you on an edge. You might want to keep F3 at hand to see if you actually have to add or subtract to get to the right edge.