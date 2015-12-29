# Gamemode command

## Description

The gamemode defines the general play mode. Free For All or Team Play ?

## Usage Examples
Command |  Definition
------------- | -------------
/pa ctf gamemode ctf | set the gamemode of arena "ctf" to "team"
/pa free !gm free    | set the gamemode of arena "free" to "free"

## Hazards

Messing up here results in strange game logic interpretations, because several things are based on the decision if we have teams or not.

## Details

The only real word needed is "free", everything else sets the arena to teams, because the only real free arena is the FREE arena, and, well, all others (except spleef which basically is a no-pvp-FFA) are team arena modes.