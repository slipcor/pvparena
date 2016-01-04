*Note that `/pvparena` and `/pa` are the same. Furthermore, those commands only work as-is if you have only one arena, OR entered edit mode with an arena if you have more than one arena:
`/pa [arenaname] edit`*

## Command list

Click on a command to view more information about it.

### Global Admin Commands
_(Permission: pvparena.admin)_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/pa debug](commands/debug.md) | /pa !d | Debugs nodes
[/pa reload](commands/reload.md) | /pa !r | Reload arena configs
[/pa install](commands/install.md) | /pa !i | Installs a module
[/pa import](commands/import.md) | /pa !imp | Imports 0.8 arenas
[/pa uninstall](commands/uninstall.md) | /pa !ui | Uninstalls a module
[/pa update](commands/update.md) | /pa !u | Updates a module

### Arena Administration Commands

_(Permission: pvparena.admin OR ownership AND pvparena.create)_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/pa blacklist \(or whitelist\)](commands/blacklist.md) | /pa !bl (!wl) | Manage arena blacklists or whitelists
[/pa check](commands/check.md) | /pa !ch | Checks an arena configuration
[/pa class](commands/class.md) | /pa !cl | Manage arena classes
[/pa create](commands/create.md) | /pa !c | Creates an arena
[/pa disable](commands/disable.md) | /pa !dis | Disables an arena.
/pa edit | /pa !e | Toggles editing of an arena
[/pa enable](commands/enable.md) | /pa !en | Enables an arena.
[/pa gamemode](commands/gamemode.md) | /pa !gm | Change the general gamemode of an arena
[/pa goal](commands/goal.md) | /pa !g | Manage arena goals
[/pa playerjoin](commands/playerjoin.md) | /pa !pj | Make a player join
[/pa protection](commands/protection.md) | /pa !p | Manages arena protections
[/pa region](commands/region.md) | /pa !rg | Manages arena regions
[/pa regionflags](commands/regionflags.md) | /pa !rf | Manages arena flags
[/pa regiontype](commands/regiontype.md) | /pa !rt | Changes a region type
[/pa regions](commands/regions.md) | /pa !rs | Debugs regions | ^
[/pa reload](commands/reload.md) | /pa !rl | Reload arena configs
[/pa remove](commands/remove.md) | /pa !rm | Removes an arena.
[/pa round](commands/round.md) | /pa !rd | Manages arena rounds
[/pa set](commands/set.md) | /pa !s | Set an arena config setting
[/pa setowner](commands/setowner.md) | /pa !so | Sets the owner of an arena
[/pa spawn](commands/spawn.md) | /pa !sp | Manage arena spawns
[/pa start](commands/start.md) | /pa !go | Force starts an arena.
[/pa stop](commands/stop.md) | /pa !st | Force stops an arena.
[/pa teams](commands/teams.md) | /pa !ts | Manages arena teams
[/pa teleport](commands/teleport.md) | /pa !tp | Teleports you to an arena spawnpoint
[/pa togglemod \[module\]](commands/togglemod.md) | /pa !tm | Activates/Deactivates module

### Arena Standard Commands

_(Permission: pvparena.user (defaults to true))_

Command | Shorthand | Definition
------------- | ------------- | -------------
/pa arenaclass | /pa -ac | Changes your class, if allowed (/pa -ac [classname])
/pa list | none | List available arenas (red: disabled, yellow: edit, green: running)
/pa chat | /pa -c | Sets arena chat mode
/pa info | /pa -i | Displays the active modules of an arena and its settings
/pa join {team} | /pa -j | Joins an arena
/pa help | /pa -h | Basic help. Splits into subsections.
/pa leave | /pa -l | Leave an arena
/pa ready | /pa -r | Readys you up or lists who is ready
/pa spectate | /pa -s | Spectate an arena
[/pa stats](commands/stats.md) | /pa -s | Shows [arena/global] statistics
/pa version | /pa -v | Shows detailed version information