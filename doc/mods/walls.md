# Walls

## Description

Yep. It's walls. Simple as can be. Make walls disappear ingame and reappear afterwards.

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

You need to create regions that define the walls. Just hit the walls you might have prepared anyways, top left, bottom right. 

If you have a standard "The Walls" setup, you will need 2 regions, one for the X axis and one for the Z axis. 

Do NOT(!) call any region in a way that it contains the word "wall", because that is what determins the filling. 

Name them as you wish. "wallX", "wall2", regions with that name will be found and used.

## Config settings

- wallseconds \- the seconds that the walls will stay. Default: 300, so 5 minutes!
- wallmaterial \- the material the wall is made of. Default: SAND 

## Commands

- `/pa [arena] wallseconds 1000` \- set the walls timer to 1000 seconds (short: !ww)
- `/pa [arena] wallmaterial STONE` \- set the walls material to smoothstone (short: !wm)

## Warnings

Do NOT(!) call any region in a way that it contains the word "wall", because that is what determins the filling.

## Dependencies

\-
