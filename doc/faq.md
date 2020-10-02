# Frequently Asked Questions

## How I can create a spleef arena?

When you create a minigame with PVPArena, you have to ask yourself "How will work score calculation?". In spleef case,
when a player dies, he looses the match.

So you should use the [playerLives](goals/playerlives.md) goal (enabled by default). Build you arena and configure it
following [this guide](getting-started.md).

You can restore you battlefield (snow ground) using [BlockRestore](mods/blockrestore.md) or 
[Worldedit](mods/worldedit.md) module. Please check [modules documentation](modules.md) to learn more.

<br>

## Can my players use their own inventories?

Yes it is possible, just pass `playerclasses` parameter to `true`. You can do this directly by editing your config file
or use the [`/pa [arena] set`](commands/set.md) command.

Then you can propose player inventories as a class: corresponding class name is `custom`. If you want this class by
default, just set `autoclass` parameter to `custom`.

Finally, if you want to return player inventory as it was before the beginning of the match, pass `customReturnsGear` to
true.

<br>

## How can I use arena commands from a command block?

Maybe you will wish to use buttons, pressure plates and command blocks to allowing players to choose their class, get
ready or leave the arena.

Most of PvPArena commands must be types by players (in order to keep context). So if you want to use plugins commands in
a command block, you will have to use a **sudo** plugin. Utility plugins like 
[EssentialsX](https://www.spigotmc.org/resources/essentialsx.9089/) already include this.

"Sudo" make possible to type a command as if player typed it. For instance, if you a to create a command block to leave 
an arena, use the command `sudo @p pa leave`.

> ⚙ **Technical precision:**  
> Since Minecraft 1.13, spigot based servers does no longer support command selectors (like `@p`). If you want to use it
> you will have to use a plugin like [CommandHook](https://www.spigotmc.org/resources/commandhook.61415/).

<br>

## How to regen my battlefield after a game?

Currently, there are two ways to regen battlefield after a match. You can use either 
[BlockRestore](mods/blockrestore.md) module or [WorldEdit](mods/worldedit.md) module.

Here is a quick list of precisions to support your choice:
* BlockRestore:
    * Reset only blocks broken by players
    * Asynchronous
    * Perfect for arena where few blocks are destroyed (like spleef)
* WorldEdit:
    * Needs WorldEdit plugin (obviously)
    * Regenerates everything
    * Can regen large areas but is synchronous

Check dedicated documentation pages to get more information.

<br>

## Still have questions?

Don't hesitate to [get in touch](../readme.md#support) with us 😉