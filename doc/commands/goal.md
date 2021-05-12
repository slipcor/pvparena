# Goal command

## Description

The goal command is used to activate certain goals, to enhance your arena or disable goals to narrow down the goals.

## Usage

Command |  Definition
------------- | -------------
/pa [arena] goal [goal] (true/false) | Toggle a goal for an arena

Example: `/pa free goal Time`  - Enable the goal "Time" for the arena "free"

## Details

You will receive a list of valid (installed) arena goals when trying to activate an unknown goal, so e.g. /pa [arenaname] !g list will show you possible goals.
Giving no second argument will just toggle the goal status and show you the result, valid arguments to activate/deactivate include:

on | 1 | true || off | 0 | false

> 🚩 **Tip:**  
> You can see active goals of your arena with command `/pa [arena] info`