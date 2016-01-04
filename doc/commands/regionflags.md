# Regionflags Command

## Description

This command manages the regions' behaviour by enabling/disabling flags.

## Usage Examples

Command |  Definition
------------- | -------------
/pa ctf regionflag death1 DEATH | toggle the DEATH regionflag for the region "death1" of the arena "ctf"
/pa ctf !rf win2 WIN true       | enable the WIN regionflag for the region "win2" of the arena "ctf"

## Details

There are several region flags that can be set:

- NOCAMP - move in here, or get punished !
- DEATH - come in, die !
- WIN - come in, WIN !
- LOSE - come in, LOSE !
- NODAMAGE - players are invincible ! 

Giving no second argument will just toggle the flag status and show you the result, valid arguments to activate/deactivate include:

on | 1 | true || off | 0 | false
