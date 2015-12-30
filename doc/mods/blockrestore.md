# BlockRestore

## Description

This mod activates BATTLE region restoring, after the match.

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

\-

## Config settings

- hard \- should the mod restore EVERY block, regardless of a known chaged state? (default: false)
- offset \- the time in TICKS (1/20 second) that the scheduler waits for the next block to be replaced (default: 1)
- restoreblocks \- restore blocks (default: true)
- restorechests \- restore chest content (default: false) 

## Commands

- `/pa [arena] !br hard` \- toggle the hard setting
- `/pa [arena] !br restorechests` \- toggle the restorechests setting
- `/pa [arena] !br clearinv` \- clear saved chest locations
- `/pa [arena] !br offset X` \- set the restore offset in TICKS! 

## Warnings

Chest restoring lags badly for the first time, because it searches the BATTLE region(s) for chests, saves the locations and from then on it's simple. If you add more chests or remove some, you will want to run the clearinv command so chests are not forgotten or blocks are treated as chests

## Dependencies

\-
