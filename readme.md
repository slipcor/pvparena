# PVP-Arena


**Enhance your server by adding a new dimension of PVP battles!**

Create fully customisable, moddable, flexible arenas, develop your own arena goal or mod that totally changes the game as you wish. 
This flexibility is achieved on the one hand by a module loader created by NodinChan which loads arena goals (/pvparena/goals) and arena mods (/pvparena/mods) which enhance the gameplay just limited by your imagination, on the other hand it features an API, which still is a WIP due to lack of requests. I will enhance it as feature/hook requests arise.


***


## Features

- Multiple arenas
- Battlefield regions
- Customizable classes
- Player-state saving
- Arena regions
- In-game configuration access
- Arena disable
- Leader boards
- Spawn protection
- Flag coloring
- Inventory drops
- Announcements
- Arena end timer

***

## Functions

- Enhancing PVP experience

***

## Dependencies

-  Bukkit 1.7.9

***

## How to install

- Stop your server
- Place in plugins folder
- Run a first time to create config folder
- Configure if you wish to
- Done!

***

## Documentation

- [creation](doc/creation.md)

***

## Changelog

- v1.3.0.552 - add CRAFT RegionProtection to prevent item crafting
- v1.3.0.551 - add damage.fromOutsiders (false) to allow players (and other entities) to hurt fighters
- v1.3.0.550 - properly check for some things before adding players to a class via command
- v1.3.0.549 - try to fix long usernames staying on signs
- v1.3.0.548 - prevent a NPE
- v1.3.0.547 - call the leave event - I am shocked noone noticed that until yesterday
- v1.3.0.546 - reverse all the things and do what I promised the last 2 commits
- v1.3.0.545 - fix books! (for real)
- v1.3.0.544 - fix books!
- v1.3.0.543 - trying again
- v1.3.0.542 - attempt to fix some data reading issues (for ink sacks and wool blocks)
- v1.3.0.541 - fix some display issues about max team players
- v1.3.0.540 - attempt to fix special character issues
- v1.3.0.539 - allow to take suicides into account for TDM
- v1.3.0.538 - still on github issue #78 - never remove perms!
- v1.3.0.537 - fix missing perms message, addressing github issue #78
- v1.3.0.536 - finish github issue #78 - typo messup
- v1.3.0.535 - address github issue #78 - add language nodes for missing perms
- v1.3.0.534 - prevent tamed animals belonging to an arena player from teleporting
- v1.3.0.533 - finally fix github issue #76 - inventory reset messup
- v1.3.0.532 - address github issue #76
- v1.3.0.531 - address github issue #64
- v1.3.0.530 - add "classSwitchAfterRespawn", defaulting to false
- v1.3.0.529 - revert #523 - I don't care anymore - this HAS to fix it, otherwise I give up on development :P
- v1.3.0.528 - revert #519 - second attempt at fixing an issue on several implementations
- v1.3.0.527 - revert #518 - this fixed dbo issue #912 (duplications on Cauldron) but caused items disappearing on other implementations
- v1.3.0.526 - add more config settings for PlayerKillReward
- v1.3.0.525 - address DBO issue #923
- v1.3.0.524 - address DBO issue #921
- v1.3.0.522 - fix integer/integer divisions -.-
- v1.3.0.521 - clarify custom class determination debug
- v1.3.0.520 - clarify damage debug values
- v1.3.0.519 - forcefully place players where they joined if the player is disconnecting
- v1.3.0.518 - forcefully remove items when a player leaves the arena
- v1.3.0.517 - address DBO issue #907 - don't try to teleport null/dead players
- v1.3.0.516 - try fixing NOCAMP damage
- v1.3.0.515 - [IDEA] various fixes
- v1.3.0.511 - address DBO issue #888 - reset chat color if desired
- v1.3.0.510 - add mobs fighting by your side. Give classes a spawn egg with displayname "SPAWN"
- v1.3.0.509 - properly build #507 prefix
- v1.3.0.507 - add materialprefixes to the global config, for special Material names (bukkit:SAND)
- v1.3.0.506 - add '*' command for the whitelist to allow all commands
- v1.3.0.505 - add per command permissions - defaulting to old behaviour
- v1.3.0.504 - don't drop inventory if not desired
- v1.3.0.503 - fix the UUID interpretation; use player names for creation, not the UUID -.-
- v1.3.0.502 - how about we actually RUN the runnable?
- v1.3.0.501 - force /pa leave on final player death if no specate module present
- v1.3.0.500 - address ticket #869, #884, #792
-- add time.resetDelay (default: -1 --> off) - delay for resetting players (TP & inventory)
-- support single SPAWN regions for team matches
-- support lore and displayname for keepItems
- v1.3.0.499 - address ticket 792, 879, 881
-- attempt to fix the Food Goal to properly handle player deaths
-- require explicit perms for /pa arenaclass (if desired)
- v1.3.0.498 - properly initiate late joining PlayerDeathMatch players
- v1.3.0.497 - check for explicit class perms, even though we have no Sign!
- v1.3.0.496 - minor fixes
- v1.3.0.495 - add Command Tab support; Big Command rewrite!!

***

## Todo

- plugin
-- calculate a winner based on ROUND results
- modules
- goals
-- tournament arenas ; rounds switch through arenas
-- siege -> bring PACKET from A to B || prevent

***

## Credits
- Deminetix for the very root, the Fight plugin
- Bradley Hilton for the fork until version v0.0.3
- Carbon131 for adding features until version v0.0.5
- Drehverschluss for great support during the v0.6+v0.7 rewrite
- NodinChan for helping me cleaning up my code and for his loader!
- zyxep for the Jenkins

***
