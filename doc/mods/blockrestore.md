# BlockRestore

## About

This mod activates BATTLE [region](../regions.md) restoring after the match.

## Config settings

- hard \- the mod will restore EVERY block of your battle region, regardless of a known changed state (default: false)
- offset \- the time in TICKS (1/20 second) that the scheduler waits for the next block to be replaced (default: 1)
- restoreblocks \- restore blocks (default: true)
- restorechests \- restore chest content (default: false) 

## Commands

- `/pa [arena] !br hard` \- toggle the hard setting
- `/pa [arena] !br restorechests` \- toggle the restorechests setting
- `/pa [arena] !br clearinv` \- clear saved chest locations
- `/pa [arena] !br offset X` \- set the restore offset in TICKS! 

<br>

> **🚩 Tips:**  
> - If you add new chests to your map, don't forget to register them with `/pa [arena] !br clearinv`.
> - BlockRestore is designed for simple block destruction and small areas. For other usages, please prefer 
> [WorldEdit](./worldedit.md) mod. 

<br>

> ⚙ **Technical precision:**  
> Chest restoring lags badly for the first time, because it searches the BATTLE region(s) for chests and saves location 
> of each of them.



