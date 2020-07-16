# Permission nodes

The following nodes can be used:

Node |  Definition
------------- | -------------
pvparena.* | Gives access to all commands
pvparena.admin | Allows you to create and administrate arenas (default: op)
pvparena.create| Allows you to create and administrate your arenas (default: op)
pvparena.telepass| Allows you to teleport while in an arena (default: op)
pvparena.user | Allows you to use the arena (default: true)


### Specific class permissions

If you activate `explicitClassNeeded` you have to add permissions e.g. (proper class name case !!)

Node |  Definition
------------- | -------------
pvparena.class.Ranger | Give Ranger class permission
pvparena.class.Tank |  Give Tank class permission

### Specific arena permissions

If you activate `explicitArenaNeeded` you have to add permissions e.g. (all lowercase!)

Node |  Definition
------------- | -------------
pvparena.join.ctf | Give ctf join permission
pvparena.join.spleef | Give spleef join permission

PVP Arena uses the SuperPerms interface, i.e. default bukkit permissions interface.