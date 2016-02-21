# WorldEdit

## Description

This module adds WorldEdit hooking for BATTLE region backup, restoring, even automatically

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

This module needs a full server restart to hook into WorldEdit properly, for the first time.

## Config settings

- autoload \- automatically load the arena's BATTLE regions after fight
- autosave \- automatically save the arena's BATTLE regions before fight
- regions \- specify individual regions rather than all BATTLE regions

## Commands


- `/pa [arena] regload [regionname] {filename}` \- load the region
- `/pa [arena] regsave [regionname] {filename}` \- save the region
- `/pa [arena] !we autoload` \- toggle general automatic loading
- `/pa [arena] !we autosave` \- toggle general automatic saving
- `/pa [arena] !we region` \- toggle specific region loading/saving
(using this command creates a list of arena regions that will be saved/loaded)

## Warnings

\-

## Dependencies

\-
