package net.slipcor.pvparena.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.PAA__Command;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionProtection;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * <pre>Arena Manager class</pre>
 * 
 * Provides static methods to manage Arenas
 * 
 * @author slipcor
 * 
 * @version v0.9.9
 */

public class ArenaManager {
	private static Map<String, Arena> arenas = new HashMap<String, Arena>();
	public static Debug db = new Debug(24);

	/**
	 * check for arena end and commit it, if true
	 * 
	 * @param arena
	 *            the arena to check
	 * @return true if the arena ends
	 */
	public static boolean checkAndCommit(Arena arena, boolean force) {
		db.i("checking for arena end");
		if (!arena.isFightInProgress()) {
			db.i("no fight, no end ^^");
			return false;
		}

		return PACheck.handleEnd(arena, force);
	}

	/**
	 * check if join region is set and if player is inside, if so
	 * 
	 * @param player
	 *            the player to check
	 * @return true if not set or player inside, false otherwise
	 */
	public static boolean checkJoin(Player player, Arena a) {
		boolean found = false;
		for (ArenaRegionShape region : a.getRegions()) {
			if (region.getType().equals(RegionType.JOIN)) {
				found = true;
				if (region.contains(new PABlockLocation(player.getLocation()))) {
					return true;
				}
			}
		}
		return !found; // no join region set
	}

	/**
	 * check if an arena has interfering regions with other arenas
	 * 
	 * @param arena
	 *            the arena to check
	 * @return true if no running arena interfering, false otherwise
	 */
	public static boolean checkRegions(Arena arena) {
		for (Arena a : arenas.values()) {
			if (a.equals(arena))
				continue;

			if ((a.isFightInProgress()) && !ArenaRegionShape.checkRegion(a, arena)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * count the arenas
	 * 
	 * @return the arena count
	 */
	public static int count() {
		return arenas.size();
	}

	/**
	 * search the arenas by arena name
	 * 
	 * @param sName
	 *            the arena name
	 * @return an arena instance if found, null otherwise
	 */
	public static Arena getArenaByName(String sName) {
		if (sName == null || sName.equals("")) {
			return null;
		}
		sName = sName.toLowerCase();
		Arena a = arenas.get(sName);
		if (a != null) {
			return a;
		}
		for (String s : arenas.keySet()) {
			if (s.endsWith(sName)) {
				return arenas.get(s);
			}
		}
		for (String s : arenas.keySet()) {
			if (s.startsWith(sName)) {
				return arenas.get(s);
			}
		}
		for (String s : arenas.keySet()) {
			if (s.contains(sName)) {
				return arenas.get(s);
			}
		}
		return null;
	}

	/**
	 * search the arenas by location
	 * 
	 * @param location
	 *            the location to find
	 * @return an arena instance if found, null otherwise
	 */
	public static Arena getArenaByRegionLocation(PABlockLocation location) {
		for (Arena arena : arenas.values()) {
			for (ArenaRegionShape region : arena.getRegions()) {
				if (region.contains(location) && !arena.isLocked())
					return arena;
			}
		}
		return null;
	}

	public static Arena getArenaByProtectedRegionLocation(
			PABlockLocation location, RegionProtection rp) {
		for (Arena arena : arenas.values()) {
			if (!arena.getArenaConfig().getBoolean(CFG.PROTECT_ENABLED)) {
				continue;
			}
			for (ArenaRegionShape region : arena.getRegions()) {
				if (region.contains(location) && region.getProtections().contains(rp))
					return arena;
			}
		}
		return null;
	}

	public static HashSet<Arena> getArenasByRegionLocation(PABlockLocation location) {
		HashSet<Arena> result = new HashSet<Arena>();
		for (Arena arena : arenas.values()) {
			if (arena.isLocked()) {
				continue;
			}
			for (ArenaRegionShape region : arena.getRegions()) {
				if (region.contains(location)) {
					result.add(arena);
				}
			}
		}
		return result;
	}

	/**
	 * return the arenas
	 * @return
	 */
	public static HashSet<Arena> getArenas() {
		HashSet<Arena> as = new HashSet<Arena>();
		for (Arena a : arenas.values()) {
			as.add(a);
		}
		return as;
	}

	/**
	 * return the first arena
	 * 
	 * @return the first arena instance
	 */
	public static Arena getFirst() {
		for (Arena arena : arenas.values()) {
			return arena;
		}
		return null;
	}

	/**
	 * get all arena names
	 * 
	 * @return a string with all arena names joined with comma
	 */
	public static String getNames() {
		return StringParser.joinSet(arenas.keySet(), ", ");
	}

	/**
	 * load all configs in the PVP Arena folder
	 */
	public static void load_arenas() {
		db.i("loading arenas...");
		try {
			File path = new File(PVPArena.instance.getDataFolder().getPath(),"arenas");
			File[] f = path.listFiles();
			int i;
			for (i = 0; i < f.length; i++) {
				if (!f[i].isDirectory() && f[i].getName().contains(".yml")) {
					String sName = f[i].getName().replace("config_", "");
					sName = sName.replace(".yml", "");
					String error = checkForMissingGoals(sName);
					if (error == null) {
						db.i("arena: " + sName);
						loadArena(sName);
					} else {
						System.out
								.print("[PVP Arena] "
										+ Language.parse(MSG.ERROR_GOAL_NOTFOUND,
												error));
						System.out
								.print("[PVP Arena] "
										+ Language.parse(MSG.GOAL_INSTALLING,
												error));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		PVPArena.instance.getAmm().load_arenas();
	}

	/**
	 * load a specific arena
	 * 
	 * @param configFile
	 *            the file to load
	 */
	public static Arena loadArena(String configFile) {
		db.i("loading arena " + configFile);
		Arena arena = new Arena(configFile);
		arenas.put(arena.getName().toLowerCase(), arena);
		return arena;
	}

	/**
	 * try loading an arena
	 * 
	 * @param name
	 *            the arena name to load
	 * @return 
	 */
	private static String checkForMissingGoals(String name) {
		db.i("check for missing goals: " + name);
		File file = new File(PVPArena.instance.getDataFolder() + "/arenas/"
				+ name + ".yml");
		if (!file.exists()) {
			return "file does not exist";
		}
		Config cfg = new Config(file);
		
		cfg.load();
		List<String> list = cfg.getStringList(CFG.LISTS_GOALS.getNode(), new ArrayList<String>());
		
		if (list.size() < 1) {
			return null;
		}
		
		for (String goal : list) {

			ArenaGoal type = PVPArena.instance.getAgm().getType(goal);
			
			if (type == null) {
				return goal;
			}

		}
		
		return null;
	}

	/**
	 * reset all arenas
	 */
	public static void reset(boolean force) {
		for (Arena arena : arenas.values()) {
			db.i("resetting arena " + arena.getName());
			arena.reset(force);
		}
	}

	/**
	 * send a message to a single player
	 * 
	 * @param player
	 *            the player to send to
	 * @param msg
	 *            the message to send
	 */
	public static void tellPlayer(CommandSender player, String msg) {
		if (player == null) {
			return;
		}
		player.sendMessage(ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE
				+ msg);
	}

	/**
	 * try to join an arena via sign click
	 * 
	 * @param event
	 *            the PlayerInteractEvent
	 * @param player
	 *            the player trying to join
	 */
	public static void trySignJoin(PlayerInteractEvent event, Player player) {
		db.i("onInteract: sign check");
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			if (block.getState() instanceof Sign) {
				Sign sign = (Sign) block.getState();
				if (sign.getLine(0).equalsIgnoreCase("[arena]")) {
					String sName = sign.getLine(1).toLowerCase();
					String[] newArgs = null;
					Arena a = arenas.get(sName);
					if (sign.getLine(2) != null
							&& a.getTeam(sign.getLine(2)) != null) {
						newArgs = new String[1];
						newArgs[0] = sign.getLine(2);
					}
					if (a == null) {
						ArenaManager.tellPlayer(player,
								Language.parse(MSG.ERROR_ARENA_NOTFOUND, sName));
						return;
					}
					PAA__Command command = new PAG_Join();
					command.commit(a, player, newArgs);
					return;
				}
			}
		}
	}

	/**
	 * unload and delete an arena
	 * 
	 * @param string
	 *            the arena name to unload
	 */
	public static void unload(String string) {
		string = string.toLowerCase();
		Arena a = arenas.get(string);
		db.i("unloading arena " + a.getName());
		a.stop(true);
		arenas.remove(string);
		a.getArenaConfig().delete();

		File path = new File(PVPArena.instance.getDataFolder().getPath() + "/stats_" + string + ".yml");
		path.delete();
		a = null;
	}

	public static void removeArena(Arena arena, boolean deleteConfig) {
		arena.stop(true);
		arenas.remove(arena.getName().toLowerCase());
		if (deleteConfig)
			arena.getArenaConfig().delete();
		arena = null;
	}
}
