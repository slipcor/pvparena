![PVP-Arena](doc/images/logo.png)

<p align="center">
    <b>
        IF YOU'RE UPGRADING FROM 1.14.x VERSION OR BELOW, PLEASE READ 
        <a href="doc/update-version.md">UPGRADE DOCUMENTATION</a>
    </b>
</p>

***
[What is PVP Arena?](#What-is-PVP-Arena?) | [Dependencies](#Dependencies) | [Downloads](#Downloads) | 
[Installation](#Installation) | [Documentation](#Documentation) | [Update Checker](#Update-Checker) | 
[Telemetry](#Telemetry) | [Credits](#Credits)
***
<br>

## What is PVP Arena?

PVP Arena is a plugin for Spigot based servers which enables creation of customizable fight and mini-games arenas.
Define your own teams, classes, lobbies, spawn points, messages, gear colors, rewards and even game modes!
Theses game modes (included in the plugin) can combined with tens of modules to enhance your gameplay!
In addition, all arenas can be protected with an embedded protection system based on regions.

Anyway, here's a quick (non exhaustive) list of plugin features:
- Multiple arenas
- Arena regions and protections
- Player-state saving
- Customizable classes
- Customizable spawn points, lobbies and spectator zones
- In-game configuration access and simple config files
- Arena disabling
- Scoreboards
- Spawn protection
- Flag coloring
- Inventory drops
- Announcements
- Time limited match
- Battlefield regeneration

So take time to read the docs, it's full of useful information 😉
***

## Dependencies

- Spigot 1.13+
- Java 8+

***

## Downloads

PVP Arena release version can be downloaded on following pages:
- [PVP Arena - SpigotMC](https://www.spigotmc.org/resources/pvp-arena.16584/)
- [Github releases page](https://github.com/Eredrim/pvparena/releases)

Development builds (experimental) can be downloaded on Jenkins:
- [Jenkins dev builds](https://ci.craftyn.com/view/Spigot%20PVP%20Arena/)

***

## Installation

Place PVP Arena `.jar` file in the plugin repository of your server and restart. 

***

## Documentation

- [Getting started](doc/getting-started.md)
- [Commands](doc/commands.md)
- [Permissions](doc/permissions.md)
- [Regions](doc/regions.md)
- [Goals](doc/goals.md)
- [Modules](doc/modules.md)
- [Items](doc/items.md)
- [Languages](doc/languages.md)
- [Configuration](doc/configuration.md)

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
kind of used Minecraft server, etc. You can disable it in the dedicated config file `/plugins/bStats/config.yml`

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
