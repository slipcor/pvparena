# Round Command

> ⚠ This command has NOT been tested at ALL! Use at your own risks.

## Description

This command is a WIP to manage "round behaviour" in arenas.

## Usage

Command |  Definition
------------- | -------------
/pa [arena] round | list rounds of an arena 
/pa [arena] round [roundNumber] | list goals of an arena round
/pa [arena] round [roundNumber] [goal] | toggle a specific goal for a round of an arena

Example:
- `/pa free round` - list rounds of arena "free"
- `/pa free round 1 ` - list goals of round 1 of arena "free"
- `/pa free round 1 Tank` - toggle the Tank goal for the first round of the arena "free"

## Details

A round consists of a sub-setting of all goals that an arena has activated. So to have multiple rounds, you can either have all goals, but you MUST add them one at a time, or you add specific goals per round. Example:

- round 1 : FREE
- round 2 : TANK 

## Todo

Add proper winning calculation AFTER all rounds are done
