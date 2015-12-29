# Regiontype Command

## Description

This command sets the region type which is essential for its functionality.

## Usage Examples

Command |  Definition
------------- | -------------
/pa ctf regiontype exit EXIT   | set the "ctf" region "exit" type to EXIT
/pa ctf !rt battlefield BATTLE | set the "ctf" region "battlefield" to BATTLE

## Details

The region types include:

- CUSTOM => a default / module added region
- WATCH => the spectator region
- LOUNGE => the ready lounge region
- BATTLE => the battlefield region
- EXIT => the exit region
- JOIN => the join region
- SPAWN => the spawn region
- BL_INV => blacklist inventory access
- WL_INV => whitelist inventory access 

### BL_INV

This region type blocks chest access for every team whichs name is in the region name, same for classes

example names:

    xxbluexx => disallows the blue team
    RedSwordsman => disallows the red team and any Swordsman 

### WL_INV

This region type restricts chest access to teams / classes which names are part of the region name

example names:

    RedBlueRanger => allows Red and Blue and Rangers to access chests
    %infected% => allows the infected (class) to access chests 

## Todo

add functionality like protection to the other region types
