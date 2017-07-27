# EventActions

## Description

This mod is quite complex, it adds many ways to react to game events, the events are (case matters!):

- classchange
- death
- end
- exit
- join
- kill
- leave
- lose
- start
- win

The actions that can be taken are:

- cmd \- server command
- pcmd \- player command
- brc \- broadcast
- abrc \- broadcast inside the arena
- power \- power a block (needs solid block underneath!)
- msg \- message to player
- clear \- clear a region of drops ("all" for all regions) 

### Placeholders

The following placeholders can be used to get dynamic output in your Commands/messages

- %class% \- the class a player has chosen
- %arena% \- the arena (name) where the event is happening
- %player% \- (except START and END!!) \- the player in the event
- %team% \- (except START and END!!) the (colored) player's team name
- %color% \- (except START and END!!) the player's team color (to colorize a message)
- %players% \- the arena players (fighters), sorted and colored by teams


## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

The mod needs a full server restart to activate the listener registration.

## Config settings

Here is an example template. You need to copy this into the arena configuration, on its own, not under a node.
```
event:
  join:
  - cmd<=>deop %player%
  - brc<=>Join %arena%!
  - msg<=>Welcome to %arena%!
```

so, "event" has to be without indents, it is the main node, followed by the inner node which represents the event when the action is issued.

Each event can have multiple contents, they have to be one line, action and value , separated by '<=>'

Note that %player% only makes sense in events that are about a player. Joining, killing and leaving, but not starting ;)

## Commands

`/pa [arena] setpower`

This command activates and deactivates block selection. Every block you click in this selection mode will receive redstone power on the given event. 

Note that this replaces the existing block with a redstone torch. Attached things will break, the torch needs a block underneath to work

## Warnings

\-

## Dependencies

\-
