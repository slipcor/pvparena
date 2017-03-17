![PVP-Arena](/doc/images/logo.png)


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

- Spigot 1.11.2

***

## Downloads

- [spigotmc.org](https://www.spigotmc.org/resources/pvp-arena.16584/)
- [jenkins - dev builds](https://ci2.craftyn.com/job/PVP%20Arena/)

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
- Localized Setup (v1.3):
    - [Team Arena (German)](https://www.youtube.com/watch?v=2KSAk-PvwRM)
- [Region setup (v1.0)](http://www.youtube.com/watch?v=LB4WKdTh4Jg)
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

- v1.3.4.256 - address github issue #285 - catch NPE by checking for missing round argument
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
I use two ways of keeping track of versions. One is the plugin version, the Bukkit Update Checker utilizing the Curse API,
the other setting is for module version checking, let me show you the important config.yml nodes:

    update:
      modules: true
    # should PA check my server (www.slipcor.net) for module versions?
    # !! If you disable that, you have to manually download and install modules! !!

      type: beta #which release state do we want to use?
    # valid values:
    # alpha: update to every dev build
    # beta: update to every beta build
    # release: only update to full release builds
    # everything else will fall back to release

      mode: both #how should we update?
    # valid values:
    # both: announce and download
    # download: download, do not announce
    # announce: only announce, do not download
    # everything else will disable the update check

***

## Metrics

To determine popularity and usage of PVP Arena, plugin installs are automatically tracked by the Metrics plugin tracking system.
It does nothing if you do not have any configured arenas. If you don't want this tracking, edit plugins/PluginMetrics/config.yml and set opt-out to true.

![PVP-Arena Metrics](http://i.mcstats.org/pvparena/Global+Statistics.borderless.png)

### Phoning home

To get a second opinion, the server contacts my private server for information purposes. It sends your port, IP (for proper server counting), and the plugin version.
That's it! If you want to disable that, set "tracker" to false in the config!

***

## Credits

- Deminetix for the very root, the Fight plugin
- Bradley Hilton for the fork until version v0.0.3
- Carbon131 for adding features until version v0.0.5
- Drehverschluss for great support during the v0.6+v0.7 rewrite
- NodinChan for helping me cleaning up my code and for his loader!
- zyxep and Bradley Hilton for the Jenkins
- Oruss7 for the documentation head start

***
