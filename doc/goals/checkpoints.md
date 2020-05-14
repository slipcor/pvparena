# CheckPoints

## Description

CheckPoints is designed for free for all game mode!

It requires you to set spawns that players have to reach, and in case they get lost, they can get back to the latest checkpoint with /pa checkpoint.

First player to reach every checkpoint up to the last (in order) wins!

## Setup

Spawns have to be added. In order to do that, use `/pa [arenaname] checkpoint [number]`. This sets checkpoint number [number].
Make sure you start with 1 and don't forget to add every single number, or else it will not be possible to win :P

## Config Settings  

- cpclaimrange => how near need players to be? (default: 5)
- cplives => number of checkpoints to reach
- cptickinterval => the amount of ticks to wait before checking for position (default: 20 = 1 second)

## Warnings

This game mode has to check for player's position. Based on the player and checkpoint count this can lag your server. But, how else should I determine a claimed checkpoint? :p

## Supported Game Modes

Free for all
