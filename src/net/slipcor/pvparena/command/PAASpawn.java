package net.slipcor.pvparena.command;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Spawns;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAASpawn extends PAA_Command {	
	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {

		if (!checkArgs(sender, args, 1, 2)) {
			return;
		}

		if (args.length > 1) {
			// pa [spawnname] remove
			arena.cfg.set("spawns." + args[1], null);
			arena.cfg.save();
			Arenas.tellPlayer(sender, Language.parse("spawnremoved", args[1]),
					arena);
			return;
		}

		if (!(sender instanceof Player)) {
			Arenas.tellPlayer(sender, Language.parse("onlyplayers"));
			return;
		}
		
		Player player = (Player) sender;
		
		if (!player.getWorld().getName().equals(arena.getWorld())) {
			Arenas.tellPlayer(player,
					Language.parse("notsameworld", arena.getWorld()), arena);
			return;
		}
		
		if (args[0].equalsIgnoreCase("spectator")) {
			Spawns.setCoords(arena, player, "spectator");
			Arenas.tellPlayer(player, Language.parse("setspectator"), arena);
		} else if (args[0].equalsIgnoreCase("exit")) {
			Spawns.setCoords(arena, player, "exit");
			Arenas.tellPlayer(player, Language.parse("setexit"), arena);
		} else {
			HashSet<String> spawns = new HashSet<String>();
			
			db.i("adding module spawns");
			
			spawns = PVPArena.instance.getAmm().getAddedSpawns();
			for (String s : spawns) {
				spawns.add(s);
				db.i("adding spawn " + s);
			}
			spawns = PAASpawn.correctSpawns(arena, spawns);
			
			for (String s : spawns) {
				if (s.startsWith(args[0])) {
					//PVPArena.instance.getAmm().c
					return;
				}
			}

			db.i("adding type spawns ");
			
			spawns = arena.type().getAddedSpawns();
			for (String s : spawns) {
				spawns.add(s);
				db.i("adding spawn " + s);
			}
			spawns = PAASpawn.correctSpawns(arena, spawns);
			
			for (String s : spawns) {
				if (args[0].startsWith(s)) {
					arena.type().commitCommand(arena, player, args);
				}
			}
		}
	}

	protected static HashSet<String> correctSpawns(Arena arena,
			HashSet<String> spawns) {
		HashSet<String> result = new HashSet<String>();
		
		for (String s : spawns) {
			if (s.contains("%team%")) {
				s = s.replace("%team%", "");
				for (ArenaTeam team : arena.getTeams()) {
					result.add(team.getName() + s);
				}
			} else {
				result.add(s);
			}
		}
		
		return result;
	}

	@Override
	public String getName() {
		return "PAASpawn";
	}
}
