# Upgrading from 1.14.x

If you want to upgrade your arena configurations to PVPArena 1.15 version or above, please update
these two things :

## Goal PhysicalFlags

If you use *physicalFlags* goal for your arenas, just rename `flags` block to `physicalFlags` in
your config file.

Example : 

*Replace this*
```yaml
goal:
  endCountDown: 5
  flags:
    flives: 3
    mustBeSafe: true
    woolFlagHead: true
    effect: none
    flagType: WHITE_WOOL
```
*with this*
```yaml
goal:
  endCountDown: 5
  physicalFlags:
    flives: 3
    mustBeSafe: true
    woolFlagHead: true
    effect: none
    flagType: WHITE_WOOL
```

## Goal checkpoints

In checkpoint goal, checkpoint indexes started at 0, now they start at 1. Just edit your config
file and decrease all checkpoints number by one in `spawns` block.

Example : 

*Replace this*
```yaml
spawns:
  checkpoint0: world,299,64,1276,-357.7568359375,4.2000017166137695
  checkpoint1: world,299,64,1276,11.695333480834961,22.20012664794922
  checkpoint2: world,300,64,1281,-5.704666614532471,23.700136184692383
  checkpoint3: world,297,64,1285,14.695302963256836,50.40021514892578
  checkpoint4: world,298,64,1289,53.69533920288086,54.75028610229492
  checkpoint5: world,295,64,1292,1.795335054397583,46.80023193359375
```
*with this*
```yaml
spawns:
  checkpoint1: world,299,64,1276,-357.7568359375,4.2000017166137695
  checkpoint2: world,299,64,1276,11.695333480834961,22.20012664794922
  checkpoint3: world,300,64,1281,-5.704666614532471,23.700136184692383
  checkpoint4: world,297,64,1285,14.695302963256836,50.40021514892578
  checkpoint5: world,298,64,1289,53.69533920288086,54.75028610229492
  checkpoint6: world,295,64,1292,1.795335054397583,46.80023193359375
```

## PowerUps Module

In powerups mod, items and effects configuration was previously in a dedicated `powerups` bloc at root of your arena
config files. Just move this block content into a the following path `modules.powerups.items`

Example : 

*Replace this*
```yaml
powerups:
  Heal:
    item: BREAD
    health:
      diff: 3
  Repair:
    item: WORKBENCH
    repair:
      items: helmet,chestplate,leggins,boots
      factor: 0.2
# Lines below have no importance
teams:
  red: RED
  blue: BLUE
```
*with this*
```yaml
modules:
  powerups:
    dropspawn: true
    usage: death:1
    items:
      Heal:
        item: BREAD
        health:
          diff: 3
      Repair:
        item: WORKBENCH
        repair:
          items: helmet,chestplate,leggins,boots
          factor: 0.2
# Lines below have no importance
teams:
  red: RED
  blue: BLUE
```