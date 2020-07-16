# Pillars

## Description

This activates pillars being defined by a wool block / pillar. People can then click/destroy them. Future versions will include actually physically bringing pillar blocks to your base to score

## Setup

You have to define pillars. Prepare a WOOL block, this will be the base of the pillar. This base will be the operational block and will react to:

- clicking
- breaking
- placing against 

(based on your settings)

In order to do that, use /pa [arenaname] [teamname]pillar* - this enables setting. Finally set the block by clicking the WOOL block.

Note that there are three kinds of pillars:

- empty pillar
  - starts empty (duh ;) )
  - gains points for everyone, when claimed
  - naming: Any name containing "pillar" and NOT containing a team name! 
- team pillar (filled)
  - starts with the predefined fill height, claimed by the team name
  - gains no points for the initial claimed team
  - naming: Any name starting with the team name and containing "pillar" 
- team pillar (not filled)
  - starts empty, only gives points to the team name contained, if claimed
  - naming: Any name not starting with the team name, containing pillar, and containing the team name that should NOT get points 


## Config settings

- announcetick (true) - should the score be publicly announced?
- breakable (true) - should you break the base to unclaim and place to claim?
- claimall (false) - should a team win by claiming all pillars?
- onlyfree (true) - only get points for free pillars (only the first type) 

- announceoffset (3) - how many ticks should pass before announcing the current scores?
- emptyheight (1) - how high should free pillars start?
- maxclicks (10) - how many clicks do people need to claim/unclaim? (overrides break allowing!! set to 0 for breaking ;) )
- maxheight (5) - what is the full height of a pillar?
- pillives (10) - how many points does a team need to win?
- teamheight (2) - how high should a claimed pillar be at start?
- tickpoints (1) - points per pillar per tick (in the future you can disable this, but you shouldnt do this now ;) )
- tickinterval (20) - the interval (in ticks, 20 per second!) to check for pillar points 

## Supported Game Modes

Only supports team game mode!

## YouTube video

[click me](http://www.youtube.com/watch?v=Xi7yNURxAjw)
