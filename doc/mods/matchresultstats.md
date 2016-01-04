# MatchResultStats

## Description

This mod adds statistics to the game. It requires a MySQL database, which you have to also check yourself. No GUI added!

A table will be created, but the database has to exist.

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

You need to set a spawn called "relay" -> `/pa [arena] spawn relay`

## Config settings ( config.yml !!! NOT per arena! )

- MySQLhost \- the SQL hostname
- MySQLuser \- the database user
- MySQLpass \- the user password
- MySQLdb \- the database name
- MySQLtable \- the table name
- MySQLport \- the SQL port

## Commands

- `/pa [arena] !ss reset [playername]` \- reset someone's stats

## Warnings

\-

## Dependencies

\-
