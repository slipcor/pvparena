package net.slipcor.pvparena.command;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Configs;
import net.slipcor.pvparena.managers.Regions;
import net.slipcor.pvparena.managers.Teams;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PAA_Command {
	protected Debug db = new Debug(50);
	public static PAA_Command parseCommand(String s, Arena arena) {
		if (s == null) {
			return new PAAJoin();
		}
		
		if (s.equalsIgnoreCase("enable")) {
			return new PAAEnable();
		} else if (s.equalsIgnoreCase("disable")) {
			return new PAADisable();
		} else if (s.equalsIgnoreCase("reload")) {
			return new PAAReload();
		} else if (s.equalsIgnoreCase("edit")) {
			return new PAAEdit();
		} else if (s.equalsIgnoreCase("chat")) {
			return new PAAChat();
		} else if (s.equalsIgnoreCase("check")) {
			return new PAACheck();
		} else if (s.equalsIgnoreCase("info")) {
			return new PAAInfo();
		} else if (s.equalsIgnoreCase("leave")) {
			return new PAALeave();
		} else if (s.equalsIgnoreCase("list")) {
			return new PAAList();
		} else if (s.equalsIgnoreCase("watch")
				|| s.equalsIgnoreCase("spectate")) {
			return new PAASpectate();
		} else if (s.equalsIgnoreCase("readylist")) {
			return new PAAReadyList();
		} else if (s.equalsIgnoreCase("region")) {
			return new PAARegion();
		} else if (s.equalsIgnoreCase("stats")) {
			return new PAAStats();
		} else if (s.equalsIgnoreCase("tp")) {
			return new PAATeleport();
		} else if (s.equalsIgnoreCase("remove")) {
			return new PAARemove();
		} else if (s.equalsIgnoreCase("borders")) {
			return new PAABorders();
		} else if (s.equalsIgnoreCase("set")) {
			return new PAASet();
		} else if (s.equalsIgnoreCase("forcestop")) {
			return new PAAForceStop();
		} else if (isSpawnCommand(arena, s)) {
			return new PAASpawn();
		} else if (Teams.getTeam(arena, s) != null) {
			return new PAAJoinTeam();
		} else if (PVPArena.instance.getAmm().parseCommand(s)) {
			return new PAAModuleCommand();
		} else if (arena.type().parseCommand(s)) {
			return new PAATypeCommand();
		} else {
			return new PAAJoin();
		}
	}
	
	private static boolean isSpawnCommand(Arena arena, String spawn) {
		HashSet<String> spawns = new HashSet<String>();

		spawns.add("spectator");
		spawns.add("exit");
		
		HashSet<String> otherSpawns = PVPArena.instance.getAmm().getAddedSpawns();
		for (String s : otherSpawns) {
			spawns.add(s);
		}
		otherSpawns = arena.type().getAddedSpawns();
		for (String s : otherSpawns) {
			spawns.add(s);
		}
		
		spawns = PAASpawn.correctSpawns(arena, spawns);
		
		for (String s : spawns) {
			if (spawn.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

	protected boolean checkArgs(CommandSender sender, String[] args, int required) {
		int check = args.length;
		if (check == required) {
			return true;
		}
		Arenas.tellPlayer(sender, Language.parse("args", String.valueOf(check), String.valueOf(required)));
		return false;
	}
	
	protected boolean checkArgs(CommandSender sender, String[] args, int required1, int required2) {
		int check = args.length;
		if (check == required1 || check == required2) {
			return true;
		}
		Arenas.tellPlayer(sender, Language.parse("args", String.valueOf(check), required1 + " | " + required2));
		return false;
	}
	
	protected boolean checkArgs(CommandSender sender, String[] args, int required1, int required2, int required3) {
		int check = args.length;
		if (check == required1 || check == required2 || check == required3) {
			return true;
		}
		Arenas.tellPlayer(sender, Language.parse("args", String.valueOf(check), required1 + " | " + required2 + " | " + required3));
		return false;
	}

	public abstract void commit(Arena arena, CommandSender sender, String[] args);
	public abstract String getName();

	/**
	 * check various methods to see if the player may join the arena
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player to check
	 * @return true if the player may join, false otherwise
	 */
	public boolean checkJoin(Arena arena, Player player) {
		String error = Configs.isSetup(arena);
		if (error != null) {
			Arenas.tellPlayer(player, Language.parse("arenanotsetup", error),
					arena);
			return false;
		}
	
		if (!PVPArena.hasPerms(player, arena)) {
			Arenas.tellPlayer(player, Language.parse("permjoin"), arena);
			return false;
		}
	
		if (Arenas.getArenaByPlayer(player) != null) {
			Arenas.tellPlayer(player, Language.parse("alreadyjoined"), arena);
			return false;
		}
	
		if (player.isInsideVehicle()) {
			Arenas.tellPlayer(player, Language.parse("insidevehicle"), arena);
			return false;
		}
	
		if (!Arenas.checkJoin(player)) {
			Arenas.tellPlayer(player, Language.parse("notjoinregion"), arena);
			return false;
		}
	
		if (arena.fightInProgress) {
			if (arena.cfg.getBoolean("join.inbattle")) {
				return true;
			}
			Arenas.tellPlayer(player, Language.parse("fightinprogress"), arena);
			return false;
		}
	
		if (Regions.tooFarAway(arena, player)) {
			Arenas.tellPlayer(player, Language.parse("joinrange"), arena);
			return false;
		}
	
		if (!PVPArena.instance.getAmm().checkJoin(arena, player)) {
			return false;
		}
	
		if (arena.START_ID != -1) {
			if (!arena.cfg.getBoolean("join.onCountdown")) {
				Arenas.tellPlayer(player, Language.parse("fightinprogress"),
						arena);
				return false;
			}
			Bukkit.getScheduler().cancelTask(arena.START_ID);
			db.i("player joining, cancelling start timer");
			arena.START_ID = -1;
		}
	
		return true;
	}
}
