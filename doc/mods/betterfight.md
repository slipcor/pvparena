# BetterFight

## Description

This mod enhances fighting, by adding one-hit-kill-items and kill streak announcements

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

Apart from the obvious config settings, there are the sounds settings. You can set those nodes to a string value that represents a bukkit ENUM, here is the link:

https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html

Case does not matter :)

## Config settings

- usemessages \- display killstreak messages
- onehititems \- the words that shall define one hit kill items
- resetkillstreakondeath \- should a player being killed start from scratch?
- explodeondeath \- create a little boom when being killed 

## Commands

- `/pa [arena] !bf messages [number]` \- set message for [number]th kill
- `/pa [arena] !bf items [items]` \- set an item string to add deadly items ("fireball,snowball,arrow")
- `/pa [arena] !bf reset` \- toggle killstreakondeath reset 

## Warnings

\-

## Dependencies

\-
