# Protection command

## Description

Manage protections of a certain arena region

## Usage Examples

Command |  Definition
------------- | -------------
/pa main protection battlefield DROP     | toggle the DROP protection of the arena "main"s region called "battlefield"
/pa hunger !p battlefield INVENTORY true | enable the INVENTORY protection of the arena "hunger"s region called "battlefield"

## Details

Erroneous protection flag usage will show you the supported flags, they include: (definition: when active)

- BREAK - prevent player block breaking
- FIRE - prevent fire spreading/burning
- MOBS - prevent mob spawning
- NATURE - prevent water flow/growth
- PAINTING - prevent painting/itemframe breakage
- PISTON - prevent piston usage
- PLACE - prevent player block placing
- TNT - prevent tnt interaction
- TNTBREAK - prevent tnt block damage (explosion still hurts)
- DROP - prevent player item dropping
- INVENTORY - prevent inventory interaction
- PICKUP - prevent player item pickup 

Always verify your protections with `/pa [arenaname] !rs [regionname]`