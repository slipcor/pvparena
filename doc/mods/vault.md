# Vault

## Description

The Vault module adds an economy hook to provide several things. Join fee, money reward, betting, etc...

## Installation

Unzip the module files (files tab, "PA Files v*.*.*") into the /pvparena/files folder and install them via

- `/pa install [modname]`, activate per arena via
- `/pa [arenaname] !tm [modname]`

## Setup

After enabling this module for the first time, the server needs a full restart to activate the Vault hook.

## Config settings

- betpot \- should bets be added to a pot that will be fully shared among winners? (default: false)
- bettime \- the maximum time in seconds after a match has started a player can place bets (default: 60)
- betWinFactor \- the factor a winning bet is multiplied with (default: 1.0)
- betWinTeamFactor \- the factor a winning bet is multiplied with for each initial team (default: 1.0)
- betWinPlayerFactor \- the factor a winning bet is multiplied with for each initial player (default: 1.0)
- entryfee \- the amount players have to pay to join the arena (default: 0)
- killreward \- amount given for killing a player (default: 0)
- maxbet \- maximum bet amount (default: 0)
- minbet \- minimum bet amount (default: 0)
- minplaytime \- minimum play time to be awarded (in seconds; default: 0)
- winPot \- should the entry fee be added to a pool that will be split among winners at the end? (default: false)
- winFactor \- the factor a winning player reward is multiplied with (default: 2)
- winreward \- the plain reward a winning player gets
- winrewardPlayerFactor \- the per player factor for rewards
- reward.playerDeath \- a reward for being killed
- reward.playerKill \- a reward for killing
- reward.playerScore \- a reward for scoring (blockdestroy destruct, food deliver, etc)
- reward.trigger \- a reward for triggering something that might cause the end (claim, flag break)
- reward.playerWin \- a reward for winning 

## Commands


- `/pa [arenaname] bet [name] [amount]` \- bet [amount] on team / player 

## Warnings

\-

## Dependencies

Vault
