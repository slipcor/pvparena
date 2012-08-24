package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.neworder.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAA_Spawn extends PAA__Command {

	public PAA_Spawn() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(1,2)))) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse("command.onlyplayers"));
			return;
		}
		
		if (args.length < 2) {
			// usage: /pa {arenaname} spawn [spawnname] | set a spawn

			ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
			
			for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
				if (mod.isActive(arena) && mod.hasSpawn(args[0])) {
					arena.spawnSet(args[0].toLowerCase(), new PALocation(ap.get().getLocation()));
					arena.msg(sender, Language.parse("spawn.set", args[0]));
					return;
				}
			}

			arena.msg(sender, Language.parse("spawn.unknown", args[0]));
			
		} else {
			// usage: /pa {arenaname} spawn [spawnname] remove | remove a spawn
			PALocation loc = Spawns.getCoords(arena, args[0]);
			if (loc == null) {
				arena.msg(sender, Language.parse("spawn.notset", args[0]));
			} else {
				arena.msg(sender, Language.parse("spawn.removed", args[0]));
				arena.spawnUnset(args[0]);
			}
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
