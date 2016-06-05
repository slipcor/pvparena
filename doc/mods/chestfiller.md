# ChestFiller

## Description

This mod adds random chest contents. It places stuff you can configure inside chests being inside the arena battlefield.

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

The mod needs a full server restart to properly alter the configuration. After the restart, you will see a configuration block, as described below. The chests will be filled with a random itemstack count between min and max. So the default will spam items 1-10, 0-5 out of those. There is a rare chance that the same item will be given twice, this chance increases when lowering the items or rising the max count.

## Config settings

- modules.chestfiller.cfitems \- the items, separated by comma
- modules.chestfiller.cfmaxitems \- maximum items being selected from the items
- modules.chestfiller.cfminitems \- minimum items being selected from the items
- modules.chestfiller.chestlocation \- minimum items being selected from the items

modules:
- chestfiller:
  - cfitems: '230,123'
  - cfmaxitems: 2
  - cfminitems: 1
  - chestlocation: none

## Commands

- `/pa [arena] !cf chest` \- set a chest to get the inventory from (overrides cfitems)
- `/pa [arena] !cf clear` \- toggle the restorechests setting

## Warnings

\-

## Dependencies

\-
