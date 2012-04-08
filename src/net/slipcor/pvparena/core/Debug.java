package net.slipcor.pvparena.core;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;

import org.bukkit.Bukkit;

/**
 * debug manager class
 * 
 * -
 * 
 * provides methods for logging when in debug mode
 * 
 * @author slipcor
 * 
 * @version v0.6.40
 * 
 */

public class Debug {
	public static boolean override = false;

	private static String prefix = "[PA-debug] ";
	private static HashSet<Integer> check = new HashSet<Integer>();
	private static byte level = 3;

	private int id = 0;

	/**
	 * Debug constructor
	 * 
	 * @param i
	 *            the debug id to check
	 */
	public Debug(int i) {
		id = i;
	}

	/**
	 * does this class debug?
	 * 
	 * @return true if debugs, false otherwise
	 */
	private boolean debugs() {
		return override || check.contains(id) || check.contains(666);
	}

	/**
	 * log a message as prefixed INFO
	 * 
	 * @param s
	 *            the message
	 */
	public void i(String s) {
		if (!debugs() || level < 1)
			return;
		Bukkit.getLogger().info(prefix + s);
	}

	/**
	 * log a message as prefixed WARNING
	 * 
	 * @param s
	 *            the message
	 */
	public void w(String s) {
		if (!debugs() || level < 2)
			return;
		Bukkit.getLogger().warning(prefix + s);
	}

	/**
	 * log a message as prefixed SEVERE
	 * 
	 * @param s
	 *            the message
	 */
	public void s(String s) {
		if (!debugs() || level < 3)
			return;
		Bukkit.getLogger().severe(prefix + s);
	}

	/**
	 * read a string array and return a readable string
	 * 
	 * @param s
	 *            the string array
	 * @return a string, the array elements joined with comma
	 */
	public String formatStringArray(String[] s) {
		if (s == null)
			return "NULL";
		String result = "";
		for (int i = 0; i < s.length; i++) {
			result = result + (result.equals("") ? "" : ",") + s[i];
		}
		return result;
	}

	public static void load(PVPArena instance) {
		Debug.check.clear();
		level = 0;
		String debugs = instance.getConfig().getString("debug");
		if (!debugs.equals("none")) {
			if (debugs.equals("all") || debugs.equals("full")) {
				Debug.check.add(666);
				System.out.print("debugging EVERYTHING");
				level = (byte) 3;
			} else {
				String[] sIds = debugs.split(",");
				for (String s : sIds) {
					try {
						Debug.check.add(Integer.valueOf(s));
						System.out.print("debugging: " + s);
					} catch (Exception e) {
						System.out.print("debug load error: " + s);
					}
					if (s.equals("i")) {
						level = (byte) 1;
					} else if (s.equals("w")) {
						level = (byte) 2;
					} else if (s.equals("s")) {
						level = (byte) 3;
					}
				}
			}
		}
	}
}

// debug ids:
// 1 - PVPArena
// 2 - PVPArenaAPI
// 3 - Help
// 4 - StringParser
// 5 - Tracker
// 6 - Update
// 7 - AnnounceMent
// 8 - Arena
// 9 - ArenaBlock
// 10 - ArenaBoard
// 11 - ArenaBoardColumn
// 12 - ArenaBoardSign
// 13 - ArenaClassSign
// 14 - ArenaPlayer
// 15 - ArenaRegion
// 16 - Powerup
// 17 - PowerupEffect
// 18 - BlockListener
// 19 - CustomListener
// 20 - EntityListener
// 21 - PlayerListener
//
// 23 - Arenas
// 24 - Blocks
// 25 - Commands
// 26 - Configs
// 27 - Dominate
// 28 - Ends
// 29 - Flags
// 30 - Inventories
// 31 - Players
// 32 - Powerups
// 33 - Regions
// 34 - Settings
// 35 - Spawns
// 36 - Statistics
// 37 - Teams
// 38 - BoardRunnable
// 39 - DominationRunnable
// 40 - EndRunnable
// 41 - PowerupRunnable
// 42 - TimedEndRunnable
// 43 - StartRunnable
// 44 - SpawnCampRunnable

