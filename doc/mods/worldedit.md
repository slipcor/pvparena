# WorldEdit

## About

This module adds WorldEdit hooking for BATTLE [region](../regions.md) backup, restoring, even automatically.

You can also use this module to design your arena [regions](../regions.md) with a WorldEdit selection (cuboid shape only).

## Setup

This module needs a full server restart to hook into WorldEdit properly, for the first time.

## Config settings

*These settings can be found under `mods.worldedit` node in your arena config file.*

- autoload - automatically load the arena's BATTLE regions after fight (default: false)
- autosave - automatically save the arena's BATTLE regions before fight (default: false)
- regions - specify individual regions rather than all BATTLE regions (default: empty)
- schematicpath - the path where worldedit schematic files will be stored (default: root path)
- replaceair - if true, air blocks will be pasted on restore (default: true)

## Commands

- `/pa [arena] regload [regionname]` \- load the region
- `/pa [arena] regsave [regionname]` \- save the region
- `/pa [arena] regcreate [regionname]` \- create a region based on an active WorldEdit selection
- `/pa [arena] !we autoload` \- toggle general automatic loading
- `/pa [arena] !we autosave` \- toggle general automatic saving


## Use case

You just want to restore your arena after the match ? Just follow these instructions:
- Check the `schematicpath` setting to correspond to your directory tree
- Save your BATTLE region with command `/pa [arena] regsave [regionname]`
- Add the previously saved region name to `regions` setting (directly in your config file or via 
[`/pa set`](../commands/set.md) command).
- Enable autoload with `/pa [arena] !we autoload`

<br>

> ⚙ **Technical precision:**  
> Worldedit pasting may freeze your server due to its working way. 
> So be sure to regen only destroyable/buildable areas to reduce reloading time.
