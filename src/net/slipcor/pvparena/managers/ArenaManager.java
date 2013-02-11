package net.slipcor.pvparena.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
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

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * <pre>
 * Arena Manager class
 * </pre>
 * 
 * Provides static methods to manage Arenas
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public final class ArenaManager {
	private final static Map<String, Arena> ARENAS = new HashMap<String, Arena>();
	private final static Debug DEBUG = new Debug(24);
	
	private ArenaManager() {
	}
	/**
	 * check for arena end and commit it, if true
	 * 
	 * @param arena
	 *            the arena to check
	 * @return true if the arena ends
	 */
	public static boolean checkAndCommit(final Arena arena, final boolean force) {
		DEBUG.i("checking for arena end");
		if (!arena.isFightInProgress()) {
			DEBUG.i("no fight, no end ^^");
			return false;
		}

		return PACheck.handleEnd(arena, force);
	}

	/**
	 * try loading an arena
	 * 
	 * @param name
	 *            the arena name to load
	 * @return
	 */
	private static String checkForMissingGoals(final String name) {
		DEBUG.i("check for missing goals: " + name);
		final File file = new File(PVPArena.instance.getDataFolder() + "/arenas/"
				+ name + ".yml");
		if (!file.exists()) {
			return "file does not exist";
		}
		final Config cfg = new Config(file);

		cfg.load();
		final List<String> list = cfg.getStringList(CFG.LISTS_GOALS.getNode(),
				new ArrayList<String>());

		if (list.size() < 1) {
			return null;
		}

		for (String goal : list) {

			final ArenaGoal type = PVPArena.instance.getAgm().getGoalByName(goal);

			if (type == null) {
				return goal;
			}

		}

		return null;
	}

	/**
	 * check if join region is set and if player is inside, if so
	 * 
	 * @param player
	 *            the player to check
	 * @return true if not set or player inside, false otherwise
	 */
	public static boolean checkJoin(final Player player, final Arena arena) {
		boolean found = false;
		for (ArenaRegionShape region : arena.getRegions()) {
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
	public static boolean checkRegions(final Arena arena) {
		for (Arena a : ARENAS.values()) {
			if (a.equals(arena)) {
				continue;
			}
			if ((a.isFightInProgress())
					&& !ArenaRegionShape.checkRegion(a, arena)) {
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
		return ARENAS.size();
	}

	/**
	 * search the arenas by arena name
	 * 
	 * @param sName
	 *            the arena name
	 * @return an arena instance if found, null otherwise
	 */
	public static Arena getArenaByName(final String name) {
		if (name == null || name.equals("")) {
			return null;
		}
		final String sName = name.toLowerCase();
		final Arena arena = ARENAS.get(sName);
		if (arena != null) {
			return arena;
		}
		for (String s : ARENAS.keySet()) {
			if (s.endsWith(sName)) {
				return ARENAS.get(s);
			}
		}
		for (String s : ARENAS.keySet()) {
			if (s.startsWith(sName)) {
				return ARENAS.get(s);
			}
		}
		for (String s : ARENAS.keySet()) {
			if (s.contains(sName)) {
				return ARENAS.get(s);
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
	public static Arena getArenaByRegionLocation(final PABlockLocation location) {
		for (Arena arena : ARENAS.values()) {
			if (arena.isLocked()) {
				continue;
			}
			for (ArenaRegionShape region : arena.getRegions()) {
				if (region.contains(location)) {
					return arena;
				}
			}
		}
		return null;
	}

	public static Arena getArenaByProtectedRegionLocation(
			final PABlockLocation location, final RegionProtection regionProtection) {
		for (Arena arena : ARENAS.values()) {
			if (!arena.getArenaConfig().getBoolean(CFG.PROTECT_ENABLED)) {
				continue;
			}
			for (ArenaRegionShape region : arena.getRegions()) {
				if (region.contains(location)
						&& region.getProtections().contains(regionProtection)) {
					return arena;
				}
			}
		}
		return null;
	}

	public static Set<Arena> getArenasByRegionLocation(
			final PABlockLocation location) {
		final Set<Arena> result = new HashSet<Arena>();
		for (Arena arena : ARENAS.values()) {
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
	 * 
	 * @return
	 */
	public static Set<Arena> getArenas() {
		final Set<Arena> arenas = new HashSet<Arena>();
		for (Arena a : ARENAS.values()) {
			arenas.add(a);
		}
		return arenas;
	}

	/**
	 * return the first arena
	 * 
	 * @return the first arena instance
	 */
	public static Arena getFirst() {
		for (Arena arena : ARENAS.values()) {
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
		return StringParser.joinSet(ARENAS.keySet(), ", ");
	}

	/**
	 * load all configs in the PVP Arena folder
	 */
	public static void load_arenas() {
		DEBUG.i("loading arenas...");
		try {
			final File path = new File(PVPArena.instance.getDataFolder().getPath(),
					"arenas");
			final File[] file = path.listFiles();
			int pos;
			for (pos = 0; pos < file.length; pos++) {
				if (!file[pos].isDirectory() && file[pos].getName().contains(".yml")) {
					String sName = file[pos].getName().replace("config_", "");
					sName = sName.replace(".yml", "");
					final String error = checkForMissingGoals(sName);
					if (error == null) {
						DEBUG.i("arena: " + sName);
						loadArena(sName);
					} else {
						PVPArena.instance.getLogger().warning(Language.parse(MSG.ERROR_GOAL_NOTFOUND, error));
						PVPArena.instance.getLogger().warning(Language.parse(MSG.GOAL_INSTALLING, error));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * load a specific arena
	 * 
	 * @param configFile
	 *            the file to load
	 */
	public static Arena loadArena(final String configFile) {
		DEBUG.i("loading arena " + configFile);
		final Arena arena = new Arena(configFile);
		
		if (!arena.isValid()) {
			Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_ARENACONFIG, configFile));
			return null;
		}
		
		ARENAS.put(arena.getName().toLowerCase(), arena);
		return arena;
	}

	public static void removeArena(Arena arena, final boolean deleteConfig) {
		arena.stop(true);
		ARENAS.remove(arena.getName().toLowerCase());
		if (deleteConfig) {
			arena.getArenaConfig().delete();
		}
		final File path = new File(PVPArena.instance.getDataFolder().getPath()
				+ "/stats_" + arena.getName() + ".yml");
		path.delete();
		arena = null;
	}

	/**
	 * reset all arenas
	 */
	public static void reset(final boolean force) {
		for (Arena arena : ARENAS.values()) {
			DEBUG.i("resetting arena " + arena.getName());
			arena.reset(force);
		}
	}

	/**
	 * try to join an arena via sign click
	 * 
	 * @param event
	 *            the PlayerInteractEvent
	 * @param player
	 *            the player trying to join
	 */
	public static void trySignJoin(final PlayerInteractEvent event, final Player player) {
		DEBUG.i("onInteract: sign check", player);
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			final Block block = event.getClickedBlock();
			if (block.getState() instanceof Sign) {
				final Sign sign = (Sign) block.getState();
				if (sign.getLine(0).equalsIgnoreCase("[arena]")) {
					final String sName = sign.getLine(1).toLowerCase();
					String[] newArgs = new String[0];
					final Arena arena = ARENAS.get(sName);
					if (sign.getLine(2) != null
							&& arena.getTeam(sign.getLine(2)) != null) {
						newArgs = new String[1];
						newArgs[0] = sign.getLine(2);
					}
					if (arena == null) {
						Arena.pmsg(player,
								Language.parse(MSG.ERROR_ARENA_NOTFOUND, sName));
						return;
					}
					final AbstractArenaCommand command = new PAG_Join();
					command.commit(arena, player, newArgs);
					return;
				}
			}
		}
	}
}
