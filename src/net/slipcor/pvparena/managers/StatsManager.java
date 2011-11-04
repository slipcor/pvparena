package net.slipcor.pvparena.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.PVPArenaPlugin;
import net.slipcor.pvparena.arenas.Arena;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;


/*
 * Statistics class
 * 
 * author: slipcor
 * 
 * version: v0.3.1 - New Arena! FreeFight
 * 
 * history:
 *
 *     v0.3.0 - Multiple Arenas
 *     v0.2.1 - cleanup, comments
 *     v0.2.0 - language support
 *     v0.1.12 - display stats
 *
 */

public class StatsManager {
	
	/*
	 * Function that retrieves the config and creates one if it does not exist
	 */
	private static Configuration getConfig(String file, Arena arena) {
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/" + file + ".yml");
		boolean bNew = false;
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
				bNew = true; // mark as new
			} catch (Exception e) {
				PVPArenaPlugin.lang.log_error("filecreateerror",file);
			}

		Configuration config = new Configuration(configFile);
		config.load();
		if (bNew) { // if marked as new, create default entries
			
			for (String sTeam : arena.fightTeams.keySet()) {
				config.setProperty("wins." + sTeam + ".slipcor", 0);
				config.setProperty("losses." + sTeam + ".slipcor", 0);
			}
		}
		return config;
	}
		
	/*
	 * Function that returns a map of all players in that form:
	 * slipcor: 2
	 * slipcor_: 3
	 * ....
	 * 
	 * This example means: slipcor lost 2 times and won 3 times
	 */	
	@SuppressWarnings("unchecked")
	public static Map<String,Integer> getPlayerStats(String sName, Arena arena) {
		Configuration config = getConfig("stats_"+sName, arena);
		Map <String, Integer> players = new HashMap<String, Integer>(); // map to sum up the players

		Map<String, Integer> team = new HashMap<String, Integer>(); // tempmap => iteration 
		int sum = 0;
		for (String sTeam : arena.fightTeams.keySet()) {
			team = (Map<String, Integer>) config.getProperty("wins." + sTeam);
			for (String rName : team.keySet()) {
				sum = 0;
				if (players.get(rName+"_") != null)
					sum = players.get(rName+"_"); // if exists: read entry
				
				sum += team.get(rName);
				players.put(rName+"_", sum); // put the player into the map, together with the count
			}
			team = (Map<String, Integer>) config.getProperty("losses" + sTeam);
			for (String rName : team.keySet()) {
				sum = 0;
				if (players.get(rName) != null)
					sum = players.get(rName); // if exists: read entry
				
				sum += team.get(rName);
				players.put(rName, sum); // put the player into the map, together with the count
			}
		}
		return players;
	}
	
	/*
	 * Function that returns a String containing team statistics in that form:
	 * 
	 * 2;3;4;5
	 * 
	 * This example means: blue won 2 times, lost 3 times ; red won 4 times, lost 5 times
	 * 
	 * Note that this counts player wins, not "team wins" in general.
	 */
	
	@SuppressWarnings("unchecked")
	public static String getTeamStats(String sName, Arena arena) {
		
		Configuration config = getConfig("stats_"+sName, arena);
		
		String result = "";
		Map<String, Integer> team = new HashMap<String, Integer>();

		for (String sTeam : arena.fightTeams.keySet()) {
			team = (Map<String, Integer>) config.getProperty("wins." + sTeam);
			int count = 0;
			for (int rVal : team.values()) {
				count += rVal; // sum up the values, append the sum
			}
			result += count + ";";
			team = (Map<String, Integer>) config.getProperty("losses." + sTeam);
			count = 0;
			for (int rVal : team.values()) {
				count += rVal; // sum up the values, append the sum
			}
			result += String.valueOf(count) + ";";
		}
		return result;		
	}
	
	/*
	 * Function that adds a win stat to the player and the team
	 */
	public static void addWinStat(Player player, String color, Arena arena) {
		if (color.equals(""))
			addStat(player, true, arena);
		else
			addStat(player, color, true, arena);
	}
	
	/*
	 * Function that adds a lose stat to the player and the team
	 */
	public static void addLoseStat(Player player, String color, Arena arena) {
		if (color.equals(""))
			addStat(player, false, arena);
		else
			addStat(player, color, false, arena);
	}
	
	/*
	 *  Function that adds a stat to the player and the team
	 */
	private static void addStat(Player player, String color, boolean win, Arena arena) {
		String sName = ArenaManager.getArenaNameByPlayer(player);
		Configuration config = getConfig("stats_"+sName, arena);
		
		String c = arena.fightTeams.get(color);
		if (c == null) {
			PVPArenaPlugin.lang.log_warning("teamnotfound",color);
			return; // invalid team
		}
		String path = (win?"wins.":"losses.") + color + "." + player.getName();
		int sum = 0;
		if (config.getProperty(path) != null)
			sum += config.getInt(path, 0); // fetch the sum if available
		sum++;                             // sum up, add, save
		config.setProperty(path, Integer.valueOf(sum));
		config.save();
	}
	
	/*
	 *  Function that adds a stat to the player and the team
	 */
	private static void addStat(Player player, boolean win, Arena arena) {
		String sName = ArenaManager.getArenaNameByPlayer(player);
		Configuration config = getConfig("stats_"+sName, arena);
		
		String path = (win?"wins.":"losses.") + player.getName();
		int sum = 0;
		if (config.getProperty(path) != null)
			sum += config.getInt(path, 0); // fetch the sum if available
		sum++;                             // sum up, add, save
		config.setProperty(path, Integer.valueOf(sum));
		config.save();
	}
}
