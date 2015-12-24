*Note that `/pvparena` and `/pa` are the same. Furthermore, those commands only work as-is if you have only one arena, OR entered edit mode with an arena if you have more than one arena:
`/pa [arenaname] edit`*

## Command list

Click on a command to view more information about it.

### Global Admin Commands
_(Permission: pvparena.admin)_

Command | Shorthand | Definition
------------- | ------------- | -------------
/pa debug | /pa !d | Debugs nodes
/pa reload | /pa !r | Reload arena configs
/pa install | /pa !i | Installs a module
/pa import | /pa !imp | Imports 0.8 arenas
/pa uninstall | /pa !ui | Uninstalls a module
/pa update | /pa !u | Updates a module

### Arena Administration Commands

_(Permission: pvparena.admin OR ownership AND pvparena.create)_

Command | Shorthand | Definition
------------- | ------------- | -------------
/pa blacklist (or whitelist) | /pa !bl (!wl) | Manage arena blacklist or whitelists
/pa check | /pa !ch | Checks an arena configuration
/pa class | /pa !cl | Manage arena classes
/pa create | /pa !c | Creates an arena
/pa disable | /pa !dis | Disables an arena.
/pa edit | /pa !e | Toggles editing of an arena
/pa enable | /pa !en | Enables an arena.
/pa gamemode | /pa !gm | Change the general gamemode of an arena
/pa goal | /pa !g | Manage arena goals
/pa playerjoin | /pa !pj | Make a player join
/pa protection | /pa !p | Manages arena protections
/pa region | /pa !rg | Manages arena regions
/pa regionflags | /pa !rf | Manages arena flags
/pa regiontype | /pa !rt | Changes a region type
/pa regions | /pa !rs | Debugs regions | ^
/pa reload | /pa !rl | Reload arena configs
/pa remove | /pa !rm | Removes an arena.
/pa round | /pa !rd | Manages arena rounds
/pa set | /pa !s | Set an arena config setting
/pa setowner | /pa !so | Sets the owner of an arena
/pa spawn | /pa !sp | Manage arena spawns
/pa start | /pa !go | Force starts an arena.
/pa stop | /pa !st | Force stops an arena.
/pa teams | /pa !ts | Manages arena teams
/pa teleport | /pa !tp | Teleports you to an arena spawnpoint
/pa togglemod [module] | /pa !tm | Activates/Deactivates module
/pa blacklist (or whitelist) | /pa !bl (!wl) | Manage arena blacklist or whitelists

### Arena Standard Commands

_(Permission: pvparena.user (defaults to true))_

Command | Shorthand | Definition
------------- | ------------- | -------------
/pa arenaclass | /pa -ac | Changes your class, if allowed (/pa -ac [classname])
/pa arenalist | /pa -ls | List available arenas (red: disabled, yellow: edit, green: running)
/pa chat | /pa -c | Sets arena chat mode
/pa info | /pa -i | Displays the active modules of an arena and its settings
/pa join {team} | /pa -j | Joins an arena
/pa help | /pa -h | Basic help. Splits into subsections.
/pa leave | /pa -l | Leave an arena
/pa ready | /pa -r | Readys you up or lists who is ready
/pa spectate | /pa -s | Spectate an arena
/pa stats | /pa -s | Shows [arena/global] statistics
/pa version | /pa -v | Shows detailed version information