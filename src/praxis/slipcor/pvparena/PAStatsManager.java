package praxis.slipcor.pvparena;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

/*
 * Statistics class
 * 
 * author: slipcor
 * 
 * version: v0.2.1 - cleanup, comments
 * 
 * history:
 *
 *    v0.2.0 - language support
 *    v0.1.12 - display stats
 * 
 * todo:
 *    - prepare for multiarena => hand over arenaname via constructor to paste it into getConfig ;)
 *    - change from static to dynamic => each arena has one statistic
 *
 */

public class PAStatsManager {
	
	/*
	 * Function that retrieves the config and creates one if it does not exist
	 */
	private static Configuration getConfig(String file) {
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/" + file + ".yml");
		boolean bNew = false;
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
				bNew = true; // mark as new
			} catch (Exception e) {
				PVPArena.lang.log_error("filecreateerror",file);
			}

		Configuration config = new Configuration(configFile);
		config.load();
		if (bNew) { // if marked as new, create default entries
			config.setProperty("wins.blue.slipcor", 0);
			config.setProperty("wins.red.slipcor", 0);
			config.setProperty("losses.blue.slipcor", 0);
			config.setProperty("losses.red.slipcor", 0);
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
	public static Map<String,Integer> getPlayerStats() {
		Configuration config = getConfig("stats");
		Map <String, Integer> players = new HashMap<String, Integer>(); // map to sum up the players

		Map<String, Integer> team = (Map<String, Integer>) config.getProperty("wins.blue"); // tempmap => iteration 
		int sum = 0;
		for (String bName : team.keySet()) {
			sum = 0;
			if (players.get(bName+"_") != null)
				sum = players.get(bName+"_"); // if exists: read entry
			
			sum += team.get(bName);
			players.put(bName+"_", sum); // put the player into the map, together with the count
		}
		team = (Map<String, Integer>) config.getProperty("losses.blue");
		for (String bName : team.keySet()) {
			sum = 0;
			if (players.get(bName) != null)
				sum = players.get(bName); // if exists: read entry
			
			sum += team.get(bName);
			players.put(bName, sum); // put the player into the map, together with the count
		}
		team = (Map<String, Integer>) config.getProperty("wins.red");
		for (String rName : team.keySet()) {
			sum = 0;
			if (players.get(rName+"_") != null)
				sum = players.get(rName+"_"); // if exists: read entry
			
			sum += team.get(rName);
			players.put(rName+"_", sum); // put the player into the map, together with the count
		}
		team = (Map<String, Integer>) config.getProperty("losses.red");
		for (String rName : team.keySet()) {
			sum = 0;
			if (players.get(rName) != null)
				sum = players.get(rName); // if exists: read entry
			
			sum += team.get(rName);
			players.put(rName, sum); // put the player into the map, together with the count
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
	public static String getTeamStats() {
		Configuration config = getConfig("stats");
		
		String result = "";
		Map<String, Integer> team = (Map<String, Integer>) config.getProperty("wins.blue");
		int blue = 0;
		for (int bVal : team.values()) {
			blue += bVal; // sum up the values, append the sum
		}
		result += blue + ";";
		team = (Map<String, Integer>) config.getProperty("losses.blue");
		blue = 0;
		for (int bVal : team.values()) {
			blue += bVal; // sum up the values, append the sum
		}
		result += blue + ";";
		team = (Map<String, Integer>) config.getProperty("wins.red");
		int red = 0;
		for (int rVal : team.values()) {
			red += rVal; // sum up the values, append the sum
		}
		result += red + ";";
		team = (Map<String, Integer>) config.getProperty("losses.red");
		red = 0;
		for (int rVal : team.values()) {
			red += rVal; // sum up the values, append the sum
		}
		result += String.valueOf(red);
		return result;		
	}
	
	/*
	 * Function that adds a win stat to the player and the team
	 */
	public static void addWinStat(Player player, String color) {
		addStat(player, color, true);
	}
	
	/*
	 * Function that adds a lose stat to the player and the team
	 */
	public static void addLoseStat(Player player, String color) {
		addStat(player, color, false);
	}
	
	/*
	 *  Function that adds a stat to the player and the team
	 */
	private static void addStat(Player player, String color, boolean win) {
		Configuration config = getConfig("stats");
		
		if (!color.equals("red") && !color.equals("blue")) {
			PVPArena.lang.log_warning("teamnotfound",color);
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
}
