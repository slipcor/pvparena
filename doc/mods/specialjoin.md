# SpecialJoin

## Description

This module adds several new ways to enter an arena, people can join by hitting a sign, lever, button.

If you use the join sign, the fourth line will represent the arena status and the amount of players:

- red \- disabled
- gold \- waiting
- green \- running 


## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

You need to activate selection of a thing to become a join trigger, with `/pa [arena] setjoin` \- then hit what should become a join trigger. 

Supported blocks are:

- LEVER
- BUTTON
- WOOD_PLATE
- STONE_PLATE
- SIGN 

Layouts are as follows:

JOIN SIGN

1: ignored
2: [teamname] \#if not valid: random
3: \#needs to be blank
4: ignored

SPECTATE SIGN

1: ignored
2: ignored
3: ANYTHING
4: ignored


## Config settings

- showplayers \- show participating / max players 

## Commands


- `/pa [arena] setjoin` \- prepare for trigger selection 

## Warnings

\-

## Dependencies

\-
