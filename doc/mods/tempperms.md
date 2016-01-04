# TempPerms

## Description

This module activates temporary permissins during a match.

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

Either administrate via command OR create a config block inside the arena config, like this:

perms:
- default:
  - everyone.has.this
  - ^everyone.doesnt.have.this
- blue:
  - only.blue.has.this
    - blue.does.not.have.this
- tank:
  - tank.has.this
  - ^tank.does.not.have.this

Note that "-node" and "^node" are the same, just to clarify that you can use both :)

## Config settings

\-

## Commands

- `/pa [arena] !tps` \- list perms
- `/pa [arena] !tps add [perm]` \- add permission
- `/pa [arena] !tps rem [perm]` \- remove permission
- `/pa [arena] !tps [name]` \- list perms for class/team
- `/pa [arena] !tps [name] add [perm]` \- add permission for class/team
- `/pa [arena] !tps [name] rem [perm]` \- remove permission from class/team 

## Warnings

Your permissions plugin needs to properly handle PermissionAttachments. If you experience this not working, double check if you GAVE superior permissions or SUB permissions that you didnt take away (explicitly), to be sure, add/remove eventual .* nodes and all subnodes.

## Dependencies

Any superperms compatible permissions plugin
