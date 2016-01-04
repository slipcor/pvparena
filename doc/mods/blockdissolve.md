# BlockDissolve

## Description

This mod adds another layer of tension. Blocks under the player will dissolve, great addition to spleef arenas!

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

The mod needs a full server restart to properly alter the configuration. After the restart, you will see a configuration block, as described below. The chests will be filled with a random itemstack count between min and max. So the default will spam items 1-10, 0-5 out of those. There is a rare chance that the same item will be given twice, this chance increases when lowering the items or rising the max count.

## Config settings

- modules.blockdissolve.materials \- the material to dissolve data value / color is ignored
- modules.blockdissolve.startseconds \- the seconds to count down before the match starts
- modules.blockdissolve.ticks \- the ticks after what time the block under the player should dissolve (20 ticks = 1 second)

## Commands

\-

## Warnings

\-

## Dependencies

\-
