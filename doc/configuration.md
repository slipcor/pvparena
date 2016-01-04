# Configuration File

This is a default configuation file. All parameters can be changed via in-game commands ([/pa set](commands/set.md)).

    configversion: 1.0.6.198
    chat:
      colorNick: true               
      defaultTeam: false
      enabled: true
      onlyPrivate: false
      toGlobal: none
    cmds:
      defaultjoin: true
      whitelist: []
    damage:
      armor: true
      fromOutsiders: false
      spawncamp: 1
      weapons: true
    general:
      classspawn: false
      classSwitchAfterRespawn: false
      customReturnsGear: false
      enabled: true
      gm: 0
      leavedeath: false
      lang: none
      owner: server
      quickspawn: true
      prefix: PVP Arena
      showRemainingLives: true
      smartspawn: false
      time: -1
      type: none
      wand: 280
    goal:   # This part depends on gamemode and goals
      livesPerPlayer: false
      endCountDown: 5
      teamlives:
        tlives: 10
    items:
      excludeFromDrops: none
      keepOnRespawn: none
      minplayers: 2
      random: true
      rewards: none
      takeOutOfGame: none
    join:
      range: 0
      forceregionjoin: false
      onlyifhasplayed: false
    block:
      blacklist: []
      whitelist: []
    goals:
    - TeamLives
    mods:
    - BattlefieldJoin
    - StandardSpectate
    - StandardLounge
    msg:
      lounge: Welcome to the arena lounge! Hit a class sign and then the iron block to flag yourself as ready!
      playerjoined: '%1% joined the Arena!'
      playerjoinedteam: '%1% joined team %2%!'
      starting: Arena is starting! Type &e/pa %1% to join!
      youjoined: You have joined the FreeForAll Arena!
      youjoinedteam: You have joined team %1%!
    perms:
      alwaysJoinInBattle: false
      explicitArenaNeeded: false
      explicitClassNeeded: false
      fly: false
      loungeinteract: false
      joinInBattle: false
      joinWithScoreboard: true
      teamkill: true
      specTalk: true
    player:
      autoIgniteTNT: false
      clearInventory: NONE
      dropsEXP: false
      dropsInventory: false
      exhaustion: 0.0
      hungerforkill: 0
      foodLevel: 20
      health: -1
      healforkill: false
      hunger: true
      mayChangeArmor: true
      maxhealth: -1
      preventDeath: true
      refillInventory: true
      saturation: 20
      quickloot: false
    protection:
      enabled: true
      punish: false
      spawn: 0
    ready:
      autoClass: Pyro   # default class when player enter to the lobby
      block: 42         # default : iron block 
      checkEachPlayer: false
      checkEachTeam: true
      enforceCountdown: false
      minPlayers: 2
      maxPlayers: 0
      maxTeam: 0
      neededRatio: 0.5
    time:
      startCountDown: 10
      regionTimer: 10
      teleportProtect: 3
      resetDelay: -1
      warmupCountDown: 0
      pvp: 0
    tp:             # teleport location when ... (old is last player's location)    
      death: old
      exit: old
      lose: old
      win: old
    uses:
      classSignsDisplay: false
      deathMessages: true
      evenTeams: false
      ingameClassSwitch: false  # players can change class via "/pa arenaclass Pyro" for example
      invisibilityfix: false
      evilinvisibilityfix: false
      playerclasses: false
      overlapCheck: true
      teamrewards: false
      woolHead: false
    flagColors:
      red: WHITE
      blue: BLACK
    classitems:
      Ranger: 261,262:64,298,299,300,301
      Swordsman: 276,306,307,308,309
      Tank: 272,310,311,312,313
      Pyro: 259,46:3,298,299,300,301
    teams:
      red: RED
      blue: BLUE
    spawns:         # do not modify directly, use "/pa spawn" commands
      bluespawn: labo,24,4,-13,-266.99993896484375,10.64997673034668
      redspawn: labo,17,4,-13,-89.84992218017578,10.500003814697266
      redlounge: labo,15,4,-13,272.699951171875,0.7499973177909851
      bluelounge: labo,26,4,-13,90.74996185302734,4.049993515014648
      spectator: labo,20,4,-7,180.2999725341797,26.549985885620117
    arenaregion:    # do not modify directly, use "/pa region" commands
      test: labo,16,1,-17,25,8,-9,cuboid,0,5054,BATTLE