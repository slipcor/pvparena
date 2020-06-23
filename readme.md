![PVP-Arena](/doc/images/logo.png)

***
**IF YOU'RE UPGRADING FROM 1.14.x VERSION OR BELOW, PLEASE READ [UPGRADE DOCUMENTATION](doc/update-version.md)**
***

**Enhance your server by adding a new dimension of PVP battles!**

Create fully customizable, moddable, flexible arenas, develop your own arena goal or mod that totally changes the game as you wish.
This flexibility is achieved on the one hand by a module loader created by NodinChan which loads arena goals (/pvparena/goals) and arena mods (/pvparena/mods) which enhance the gameplay just limited by your imagination, on the other hand it features an API, which still is a WIP due to lack of requests. I will enhance it as feature/hook requests arise.


***


## Features

- Multiple arenas
- Battlefield regions
- Customizable classes
- Player-state saving
- Arena regions
- In-game configuration access
- Arena disable
- Leader boards
- Spawn protection
- Flag coloring
- Inventory drops
- Announcements
- Arena end timer

***

## Dependencies

- Spigot 1.13+

***

## Downloads

- [spigotmc.org](https://www.spigotmc.org/resources/pvp-arena.16584/)
- [Dev builds on Jenkins](https://ci.craftyn.com/view/Spigot%20PVP%20Arena/)

***

## How to install

- Stop your server
- Place jar in plugins folder
- Run a first time to create config folder
- Configure if you wish to
- Done !

***

## Documentation

- [Creation](doc/creation.md)
- [Commands](doc/commands.md)
- [Enhancements](doc/enhancements.md)
- [Items](doc/items.md)
- [Languages](doc/languages.md)
- [Permissions](doc/permissions.md)
- [Regions](doc/regions.md)
- [Configuration](doc/configuration.md)

***

## Video Tutorials

- Basic Setup (v1.3):
    - [Team Arena](https://www.youtube.com/watch?v=PT0piAyVMIw)
    - [Free For All Arena](https://www.youtube.com/watch?v=bYNtxGxVGfE)
    - [Region Tutorial (Shapes)](https://www.youtube.com/watch?v=jWdWbwRg9zY)
    - [Region Tutorial (Protections)](https://youtu.be/WFIZ7ZskPVc)
- Localized Setup (v1.3):
    - [Team Arena (German)](https://www.youtube.com/watch?v=2KSAk-PvwRM)
- Goal Tutorials (v1.3):
    - [BlockDestroy](https://www.youtube.com/watch?v=i7Fpuh_O5O8)
    - [CheckPoints](https://www.youtube.com/watch?v=anO_tYwcKsg)
    - [Domination](https://www.youtube.com/watch?v=_Ngq5xBlLsk)
- [CTF (v1.0)](http://www.youtube.com/watch?v=SuL78bce-f0)
- [DeathMatch (v1.0)](http://www.youtube.com/watch?v=KqBueDNbpD8)
- [Food Block Destroy (v1.0)](http://www.youtube.com/watch?v=ntloY1BTKHQ)
- [FreeForAll (v1.0)](http://www.youtube.com/watch?v=xBIxHoKMu98)
- [Spleef (v1.0)](http://www.youtube.com/watch?v=DRmLNXEAs_4)
- [Pillar Domination (v1.0)](http://www.youtube.com/watch?v=Xi7yNURxAjw)
- [TeamDeathMatch (v1.0)](http://www.youtube.com/watch?v=rQ1ljlc6SJM)

Users tutorials :

- [TeamDeathMatch (v1.0)](http://www.youtube.com/watch?v=Jw6E8s2kiKw)

***

## Changelog

- v1.13.5 - address #355 - set some more scoreboard settings to hopefully get colors going
- [read more](doc/changelog.md)

***

## Todo

- plugin
  - [ ] calculate a winner based on ROUND results
- modules
- goals
  - [ ] tournament arenas ; rounds switch through arenas
  - [ ] siege -> bring PACKET from A to B || prevent

***

## Update Checker
If you wan't you can be informed of plugin or modules updates. Each release version was pushed on github since 1.14.0.
The update checker will call the github APIs and announce an update to OPs on login. You can configure it to 
automatically download updates.

```yaml
    update:
      plugin: announce
      modules: announce
    # valid values:
    # download: download updates and announce when update is installed
    # announce: only announce, do not download
    # everything else will disable the update check
```

***

## Telemetry

PVPArena uses bStats to get statistics about basic information like plugin version, java version,
kind of used Minecraft server, etc. You can disable it in the dedicated config file `plugins/bStats/config.yml`

***

## Credits

- SlipCor, the wonderful guy who created this plugin
- Deminetix for the very root, the Fight plugin
- Bradley Hilton for the fork until version v0.0.3
- Carbon131 for adding features until version v0.0.5
- Drehverschluss for great support during the v0.6+v0.7 rewrite
- NodinChan for helping me cleaning up my code and for his loader!
- zyxep and Bradley Hilton for the Jenkins
- Oruss7 for the documentation head start

***
