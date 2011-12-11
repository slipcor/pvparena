/*
 * statistics manager class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.3.10 - CraftBukkit #1337 config version, rewrite
 *     v0.3.9 - Permissions, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.1 - New Arena! FreeFight
 *     v0.3.0 - Multiple Arenas
 *     v0.2.1 - cleanup, comments
 *     v0.2.0 - language support
 *     v0.1.12 - display stats
 */

package net.slipcor.pvparena.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arenas.Arena;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class StatsManager {
	private static DebugManager db = new DebugManager();

	/*
	 * retrieve config and create one if it doesn't exist
	 */
	private static YamlConfiguration getConfig(String file, Arena arena) {
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/" + file + ".yml");
		boolean bNew = false;
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
				bNew = true; // mark as new
			} catch (Exception e) {
				PVPArena.lang.log_error("filecreateerror", file);
			}

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		if (bNew) { // if marked as new, create default entries

			for (String sTeam : arena.paTeams.keySet()) {
				config.addDefault("wins." + sTeam + ".slipcor", 0);
				config.addDefault("losses." + sTeam + ".slipcor", 0);
			}
			config.options().copyDefaults(true);
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return config;
	}

	/*
	 * returns a map of all players like: slipcor: 2 slipcor_: 3 ....
	 * 
	 * this example means: slipcor lost 2 times and won 3 times
	 */
	public static Map<String, Integer> getPlayerStats(Arena arena) {
		YamlConfiguration config = getConfig("stats_" + arena.name, arena);
		db.i("fetching player stats for arena " + arena.name);
		// map to sum up the  players
		Map<String, Integer> players = new HashMap<String, Integer>();
		// tempmap => iteration
		Map<String, Object> team = new HashMap<String, Object>();
		int sum = 0;
		for (String sTeam : arena.paTeams.keySet()) {
			team = (Map<String, Object>) config.getConfigurationSection(
					"wins." + sTeam).getValues(true);
			for (String rName : team.keySet()) {
				sum = 0;
				if (players.get(rName + "_") != null)
					sum = players.get(rName + "_"); // if exists: read entry

				sum += (Integer) team.get(rName);
				db.i(rName + "_ => " + sum);
				// put the player into the map, together with the count
				players.put(rName + "_", sum);
			}
			team = (Map<String, Object>) config.getConfigurationSection(
					"losses." + sTeam).getValues(true);
			for (String rName : team.keySet()) {
				sum = 0;
				if (players.get(rName) != null)
					sum = players.get(rName); // if exists: read entry

				sum += (Integer) team.get(rName);
				db.i(rName + " => " + sum);
				// put the player into the map, together with the count
				players.put(rName, sum);
			}
		}
		return players;
	}

	/*
	 * returns a string containing team statistics like:
	 * 
	 * 2;3;4;5
	 * 
	 * this example means: blue won 2 times, lost 3 times ; red won 4 times,
	 * lost 5 times
	 */

	public static String getTeamStats(Arena arena) {
		db.i("fetching team stats for arena " + arena.name);
		YamlConfiguration config = getConfig("stats_" + arena.name, arena);

		String result = "";
		Map<String, Object> team = new HashMap<String, Object>();

		for (String sTeam : arena.paTeams.keySet()) {
			team = (Map<String, Object>) config.getConfigurationSection(
					"wins." + sTeam).getValues(true);
			int count = 0;
			for (Object rVal : team.values()) {
				count += (Integer) rVal; // sum up the values, append the sum
			}
			result += count + ";";
			team = (Map<String, Object>) config.getConfigurationSection(
					"losses." + sTeam).getValues(true);
			count = 0;
			for (Object rVal : team.values()) {
				count += (Integer) rVal; // sum up the values, append the sum
			}
			result += String.valueOf(count) + ";";
			db.i(sTeam + ": " + count);
		}
		return result;
	}

	/*
	 * add a win stat to the player and the team
	 */
	public static void addWinStat(Player player, String color, Arena arena) {
		if (color.equals(""))
			addStat(player, true, arena);
		else
			addStat(player, color, true, arena);
	}

	/*
	 * add a lose stat to the player and the team
	 */
	public static void addLoseStat(Player player, String color, Arena arena) {
		if (color.equals(""))
			addStat(player, false, arena);
		else
			addStat(player, color, false, arena);
	}

	/*
	 * add a stat to the player and the team
	 */
	private static void addStat(Player player, String color, boolean win,
			Arena arena) {
		db.i("adding stat: player " + player.getName() + "; color: " + color
				+ "; win: " + String.valueOf(win) + "; arena: " + arena.name);
		String sName = ArenaManager.getArenaNameByPlayer(player);
		YamlConfiguration config = getConfig("stats_" + sName, arena);

		String c = arena.paTeams.get(color);
		if (c == null) {
			PVPArena.lang.log_warning("teamnotfound", color);
			return; // invalid team
		}
		String path = (win ? "wins." : "losses.") + color + "."
				+ player.getName();
		int sum = 0;
		if (config.get(path) != null)
			sum += config.getInt(path, 0); // fetch the sum if available
		sum++; // sum up, add, save
		config.set(path, Integer.valueOf(sum));
		try {
			config.save(new File("plugins/pvparena/stats_" + sName + ".yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * add a stat to the player and the team
	 */
	private static void addStat(Player player, boolean win, Arena arena) {
		db.i("adding stat: player " + player.getName() + "; win: "
				+ String.valueOf(win) + "; arena: " + arena.name);
		String sName = ArenaManager.getArenaNameByPlayer(player);
		YamlConfiguration config = getConfig("stats_" + sName, arena);

		String path = (win ? "wins." : "losses.") + player.getName();
		int sum = 0;
		if (config.get(path) != null)
			sum += config.getInt(path, 0); // fetch the sum if available
		sum++; // sum up, add, save
		config.set(path, Integer.valueOf(sum));
		try {
			config.save(new File("plugins/pvparena/stats_" + sName + ".yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
