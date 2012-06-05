package net.slipcor.pvparena.managers;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.command.PAAJoin;
import net.slipcor.pvparena.command.PAA_Command;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaType;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * arena manager class
 * 
 * -
 * 
 * provides access to the arenas, search them, trigger events, communication
 * etc.
 * 
 * @author slipcor
 * 
 * @version v0.7.19
 * 
 */

public class Arenas {
	private static Map<String, Arena> arenas = new HashMap<String, Arena>();
	private static Debug db = new Debug(23);

	/**
	 * check for arena end and commit it, if true
	 * 
	 * @param arena
	 *            the arena to check
	 * @return true if the arena ends
	 */
	public static boolean checkAndCommit(Arena arena) {
		db.i("checking for arena end");
		if (!arena.fightInProgress) {
			db.i("no fight, no end ^^");
			return false;
		}

		return arena.type().checkAndCommit();
	}

	/**
	 * check if join region is set and if player is inside, if so
	 * 
	 * @param player
	 *            the player to check
	 * @return true if not set or player inside, false otherwise
	 */
	public static boolean checkJoin(Player player) {
		for (Arena a : arenas.values()) {
			for (String rName : a.regions.keySet()) {
				if (rName.equals("join")) {
					return a.regions.get(rName).contains(player.getLocation());
				}
			}
		}
		return true; // no join region set
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

			if ((a.fightInProgress) && !Regions.checkRegion(a, arena)) {
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
	 * search the arenas by player
	 * 
	 * @param pPlayer
	 *            the player to find
	 * @return the arena instance if found, null otherwise
	 */
	public static Arena getArenaByPlayer(Player pPlayer) {
		return ArenaPlayer.parsePlayer(pPlayer).getArena();
	}

	/**
	 * search the arenas by location
	 * 
	 * @param location
	 *            the location to find
	 * @return an arena instance if found, null otherwise
	 */
	public static Arena getArenaByRegionLocation(Location location) {
		for (Arena arena : arenas.values()) {
			if (arena.contains(location))
				return arena;
		}
		return null;
	}

	/**
	 * search the arenas by player
	 * 
	 * @param pPlayer
	 *            the player to find
	 * @return the arena name if found, null otherwise
	 */
	public static String getArenaNameByPlayer(Player pPlayer) {
		for (Arena arena : arenas.values()) {
			if (arena.isPartOf(pPlayer))
				return arena.name;
		}
		return null;
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
		String result = "";
		for (String sName : arenas.keySet())
			result += (result.equals("") ? "" : ", ") + sName;
		db.i("arenas: " + result);
		return result;
	}

	/**
	 * load all configs in the PVP Arena folder
	 */
	public static void load_arenas() {
		db.i("loading arenas...");
		try {
			File path = PVPArena.instance.getDataFolder();
			File[] f = path.listFiles();
			int i;
			for (i = 0; i < f.length; i++) {
				if (!f[i].isDirectory() && f[i].getName().contains("config_")) {
					String sName = f[i].getName().replace("config_", "");
					sName = sName.replace(".yml", "");
					String arenaType = preParse(sName);
					if (arenaType == null) {
						db.i("arena: " + sName);
						loadArena(sName, arenaType);
						// this is on purpose, I want to call with NULL :p
					} else {
						System.out
								.print("[PVP Arena] "
										+ Language.parse("arenatypeunknown",
												arenaType));
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
	 * @param type
	 *            the arena type
	 */
	public static Arena loadArena(String configFile, String type) {
		db.i("loading arena " + configFile + " (" + type + ")");
		Arena arena = new Arena(configFile, type);
		arenas.put(arena.name, arena);
		return arena;
	}

	/**
	 * try loading an arena
	 * 
	 * @param name
	 *            the arena name to load
	 * @return the arena type name if successful, null otherwise
	 */
	private static String preParse(String name) {
		db.i("pre-Parsing Arena " + name);
		File file = new File(PVPArena.instance.getDataFolder() + "/config_"
				+ name + ".yml");
		if (!file.exists()) {
			return "file does not exist";
		}
		Config cfg = new Config(file);
		cfg.load();
		String arenaType = cfg.getString("general.type",
				"please redo your arena");

		ArenaType type = PVPArena.instance.getAtm().getType(arenaType);

		return type == null ? arenaType : null;
	}

	/**
	 * reset all arenas
	 */
	public static void reset(boolean force) {
		for (Arena arena : arenas.values()) {
			db.i("resetting arena " + arena.name);
			arena.reset(force);
		}
	}

	/**
	 * send a message to a single player
	 * 
	 * @param sender
	 *            the player to send to
	 * @param msg
	 *            the message to send
	 * @param a
	 *            the arena sending this message
	 */
	public static void tellPlayer(CommandSender sender, String msg, Arena a) {
		db.i("@" + sender.getName() + ": " + msg);
		sender.sendMessage(ChatColor.YELLOW + "[" + a.prefix + "] "
				+ ChatColor.WHITE + msg);
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
					String sName = sign.getLine(1);
					String[] newArgs = null;
					Arena a = arenas.get(sName);
					if (sign.getLine(2) != null
							&& Teams.getTeam(a, sign.getLine(2)) != null) {
						newArgs = new String[1];
						newArgs[0] = sign.getLine(2);
					}
					if (a == null) {
						Arenas.tellPlayer(player,
								Language.parse("arenanotexists", sName));
						return;
					}
					PAA_Command command = new PAAJoin();
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
		Arena a = arenas.get(string);
		db.i("unloading arena " + a.name);
		a.forcestop();
		arenas.remove(string);
		a.cfg.delete();

		File path = new File("plugins/pvparena/stats_" + string + ".yml");
		path.delete();
		a = null;
	}
}
