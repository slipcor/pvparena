# ChestFiller

## Description

This mod adds random chest contents. It places stuff you can configure inside chests being inside the arena battlefield.

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

Either set chests specifically with the "fillchest" subcommand or just start an arena, and the module will gather all chests in all battle regions.
The chests will be filled - at each round start - with a random itemstack count between min and max.
So the default will spam items 1-10, 0-5 out of those. There is a chance that the same item will be given twice, this chance increases when lowering the items or raising the max count.

## Config settings

- modules.chestfiller.cfitems \- the items, separated by comma
- modules.chestfiller.cfmaxitems \- maximum items being selected from the items
- modules.chestfiller.cfminitems \- minimum items being selected from the items
- modules.chestfiller.chestlocation \- a chest location to read the items list from (overrides "cfitems"!)
- modules.chestfiller.clear \- should every chest be cleared before being filled each round?

## Commands

- `/pa [arena] !cf chest` \- set a chest to get the inventory from (overrides cfitems)
- `/pa [arena] !cf fillchest` \- set a chest to fill (adds to the list of chests to be filled)
- `/pa [arena] !cf clear` \- clear the list of chests to be filled

## Warnings

\-

## Dependencies

\-
