There are currently four ways of adding to the game:
# PVP Arena API

The API features basic access to several things, based on very few requests. If you need something, give me a shout!
## PVP Arena Modules

### Installation

Unzip the module files (files tab, "PA Files v\*.\*.\*") into the /pvparena/files folder and install them via
`/pa install [modname]`, **activate per arena via**
`/pa [arenaname] !tm [modname]`

### PVP Arena Goals

Create ways to win the game or lose the game!

Goal | Description
------------- | -------------
[Beacons](goals/beacons.md) | Stand near beacons and claim them to win!
[BlockDestroy](goals/blockdestroy.md) | Destroy blocks (pre-installed)
[CheckPoints](goals/checkpoints.md) | Reach checkpoints in order to win (pre-installed)
[Domination](goals/domination.md) | Dominate flag positions (pre-installed)
[Flags](goals/flags.md) | Capture flags and bring 'em home (pre-installed)
[Food](goals/food.md) | Cook food and bring it home (pre-installed)
[Infect](goals/infect.md) | Infect people to win / kill infected players (pre-installed)
[Liberation](goals/liberation.md) | Jail dead players, possibility to unjail! (pre-installed)
[PhysicalFlags](goals/physicalflags.md) | Capture flags physically and bring 'em home (pre-installed)
[Pillars](goals/pillars.md) | Capture pillars by clicking/destroying!
[PlayerDeathMatch](goals/playerdeathmatch.md) | Player kills win (pre-installed)
[PlayerKillReward](goals/playerkillreward.md) | Player get better gears when killing (pre-installed)
[Rescue](goals/rescue.md) | Rescue a trapped Entity
[PlayerLives](goals/playerlives.md) | Player deaths lose (pre-installed)
[Sabotage](goals/sabotage.md) | Ignite TNT (pre-installed)
[Tank](goals/tank.md) | all vs one (pre-installed)
[TeamDeathConfirm](goals/teamdeathconfirm.md) | Confirmed Team kills win (pre-installed)
[TeamDeathMatch](goals/teamdeathmatch.md) | Team kills win (pre-installed)
[TeamLives](goals/teamlives.md) | Team deaths lose (pre-installed)
[Time](goals/time.md) | Time ends the arena (pre-installed)

### PVP Arena Mods

Hook into many different aspects of the game!

Mod | Description
------------- | -------------
[AfterMatch](mods/aftermatch.md) | could also be called "Sudden Death"
[Announcements](mods/announcements.md) | announce events happening
[ArenaBoards](mods/arenaboards.md) | stats display
[ArenaMaps](mods/arenamaps.md) | never lose yourself ever again!
[AutoSneak](mods/autosneak.md) | automatically hide player nametags by forcing sneak mode
[AutoVote](mods/autovote.md) | automatism
[BanKick](mods/bankick.md) | secure your arenas!
[BattlefieldGuard](mods/battlefieldguard.md) | secure your battlefield
[BattlefieldManager](mods/battlefieldmanager.md) | manage your battlefield
[BetterClasses](mods/betterclasses.md) | add potion effects to classes
[BetterGears](mods/bettergears.md) | give team colored leather
[BetterFight](mods/betterfight.md) | kill streaks and one-hit-kill items!
[BetterKillstreaks](mods/betterkillstreaks.md) | even more detailed kill streaks!
[BlockDissolve](mods/blockdissolve.md) | dissolve blocks under fighting players
[BlockRestore](mods/blockrestore.md) | restore the battlefield
[ChestFiller](mods/chestfiller.md) | fill battlefield chests with customizable content!
[ColorTeams](mods/colorteams.md) | color players!
[Duel](mods/duel.md) | duel someone!
[EventActions](mods/eventactions.md) | do stuff when stuff happens
[Factions](mods/factions.md) | fix pvp not working
[FallingAnvils](mods/fallinganvils.md) | spawn killer anvils dropping from the sky
[FixInventoryLoss](mods/fixinventoryloss.md) | prevent loss by gamemode / inventory check
[FlySpectate](mods/flyspectate.md) | have players spectating a fight in fly mode
[Items](mods/items.md) | spawn (random) items
[LateLounge](mods/latelounge.md) | keep playing until enough ppl are joining
[MatchResultStats](mods/matchresultstats.md) | keep stats of player games, who won, who lost?
[PlayerFinder](mods/playerfinder.md) | allow players to find others with a compass
[Points](mods/points.md) | allow to restrict certain classes to require players to fight for better classes
[PowerUps](mods/powerups.md) | exactly that
[RealSpectate](mods/realspectate.md) | spectate the game, CounterStrike style!
[RedstoneTriggers](mods/redstonetriggers.md) | add win/lose triggered by redstone
[RespawnRelay](mods/respawnrelay.md) | add a relay for respawning players
[ScoreBoards](mods/scoreboards.md) | ScoreBoards!
[SinglePlayerSupport](mods/singleplayersupport.md) | Allow players to use an arena on their own!
[Skins](mods/skins.md) | add custom skins to teams/classes
[SpecialJoin](mods/specialjoin.md) | join via buttons, levers, etc
[Spectate](mods/spectate.md) | use the new 1.8 SPECTATOR mode to allow flying and POV spectating
[Squads](mods/squads.md) | add squads to the game, basically only showing players belonging together apart from teams and classes.
[StartFreeze](mods/startfreeze.md) | freeze players at start
[TeamSizeRestrict](mods/teamsizerestrict.md) | a small mod to restrict the size of specific teams
[Titles](mods/titles.md) | send messages to players as the "title" command would do
[TempPerms](mods/tempperms.md) | add temporary perms
[Turrets](mods/turrets.md) | add turrets where players fire projectiles
[Vault](mods/vault.md) | add economy
[Walls](mods/walls.md) | define wall regions to simulate "The Walls"
[WorldEdit](mods/worldedit.md) | backup/restore regions
[WorldGuard](mods/worldguard.md) | import region definitions from WorldGuard

### PVP Arena Region Shapes

Create region shapes to customize your arena! Default: COBOID!

Shape| Description
------------- | -------------
[CUBOID](shapes/cuboid.md) | a standard region that all should know
[CYLINDRIC](shapes/cylindric.md) | a standing can/barrel region
[SPHERIC](shapes/spheric.md) | a sphere/ball