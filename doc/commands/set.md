# Set Command

## Description

Sets a specific config node, or lists possible nodes.

## Usage Examples

Command |  Definition
------------- | -------------
/pa test set 0            | get the first page of the arena "test"
/pa test set minPlayers 5 | requires the "test" arena to have 5 players before starting

## Hazards

If you mess up you might change the wrong node, erase parts of the config and render your config useless. So only use this if you know what you're doing.

## Details

A number as only argument lists that page, a node and value tries to set the config node to the given value.

You know know what the node types on the pages mean, but here they are:

- boolean - true or false
- string - a word
- int - a whole number
- double - a decimal (e.g. 0.5)
- tp - a spawn point (exit, old, spectator)
- item - a bukkit ENUM or ID of a material
- items - e.g. class items, see the corresponding page about item definitions

