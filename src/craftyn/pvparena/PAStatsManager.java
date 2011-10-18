package craftyn.pvparena;

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
 * version: v0.2.0 - language support
 * 
 * history:
 *
 *    v0.1.12 - display stats
 *
 */

public class PAStatsManager {
	@SuppressWarnings("unchecked")
	public static Map<String,Integer> getPlayerStats() {
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/stats.yml");
		boolean bNew = false;
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
				bNew = true;
			} catch (Exception e) {
				PVPArena.lang.log_error("filecreateerror","stats");
			}

		Configuration config = new Configuration(configFile);
		config.load();
		if (bNew) {
			config.setProperty("wins.blue.slipcor", 0);
			config.setProperty("wins.red.slipcor", 0);
			config.setProperty("losses.blue.slipcor", 0);
			config.setProperty("losses.red.slipcor", 0);
		}
		Map <String, Integer> players = new HashMap<String, Integer>();

		Map<String, Integer> team = (Map<String, Integer>) config.getProperty("wins.blue");
		int sum = 0;
		for (String bName : team.keySet()) {
			sum = 0;
			if (players.get(bName+"_") != null)
				sum = players.get(bName+"_");
			
			sum += team.get(bName);
			players.put(bName+"_", sum);
		}
		team = (Map<String, Integer>) config.getProperty("losses.blue");
		for (String bName : team.keySet()) {
			sum = 0;
			if (players.get(bName) != null)
				sum = players.get(bName);
			
			sum += team.get(bName);
			players.put(bName, sum);
		}
		team = (Map<String, Integer>) config.getProperty("wins.red");
		for (String rName : team.keySet()) {
			sum = 0;
			if (players.get(rName+"_") != null)
				sum = players.get(rName+"_");
			
			sum += team.get(rName);
			players.put(rName+"_", sum);
		}
		team = (Map<String, Integer>) config.getProperty("losses.red");
		for (String rName : team.keySet()) {
			sum = 0;
			if (players.get(rName) != null)
				sum = players.get(rName);
			
			sum += team.get(rName);
			players.put(rName, sum);
		}
		return players;
	}
	
	@SuppressWarnings("unchecked")
	public static String getTeamStats() {
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/stats.yml");
		boolean bNew = false;
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
				bNew = true;
			} catch (Exception e) {
				PVPArena.lang.log_error("filecreateerror","stats");
			}

		Configuration config = new Configuration(configFile);
		config.load();
		if (bNew) {
			config.setProperty("wins.blue.slipcor", 0);
			config.setProperty("wins.red.slipcor", 0);
			config.setProperty("losses.blue.slipcor", 0);
			config.setProperty("losses.red.slipcor", 0);
		}
		String result = "";
		Map<String, Integer> team = (Map<String, Integer>) config.getProperty("wins.blue");
		int blue = 0;
		for (int bVal : team.values()) {
			blue += bVal;
		}
		result += blue + ";";
		team = (Map<String, Integer>) config.getProperty("losses.blue");
		blue = 0;
		for (int bVal : team.values()) {
			blue += bVal;
		}
		result += blue + ";";
		team = (Map<String, Integer>) config.getProperty("wins.red");
		int red = 0;
		for (int rVal : team.values()) {
			red += rVal;
		}
		result += red + ";";
		team = (Map<String, Integer>) config.getProperty("losses.red");
		red = 0;
		for (int rVal : team.values()) {
			red += rVal;
		}
		result += String.valueOf(red);
		return result;		
	}
	
	public static void addWinStat(Player player, String color) {
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/stats.yml");
		boolean bNew = false;
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
				bNew = true;
			} catch (Exception e) {
				PVPArena.lang.log_error("filecreateerror","stats");
			}

		Configuration config = new Configuration(configFile);
		config.load();
		if (bNew) {
			config.setProperty("wins.blue.slipcor", 0);
			config.setProperty("wins.red.slipcor", 0);
			config.setProperty("losses.blue.slipcor", 0);
			config.setProperty("losses.red.slipcor", 0);
		}
		if (!color.equals("red") && !color.equals("blue")) {
			PVPArena.lang.log_warning("teamnotfound",color);
			return;
		}
		String path = "wins." + color + "." + player.getName();
		int sum = 0;
		if (config.getProperty(path) != null)
			sum += config.getInt(path, 0);
		sum++;
		config.setProperty(path, Integer.valueOf(sum));
		config.save();
	}
	
	public static void addLoseStat(Player player, String color) {
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/stats.yml");
		boolean bNew = false;
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
				bNew = true;
			} catch (Exception e) {
				PVPArena.lang.log_error("filecreateerror","stats");
			}

		Configuration config = new Configuration(configFile);
		config.load();
		if (bNew) {
			config.setProperty("wins.blue.slipcor", 0);
			config.setProperty("wins.red.slipcor", 0);
			config.setProperty("losses.blue.slipcor", 0);
			config.setProperty("losses.red.slipcor", 0);
		}
		if (!color.equals("red") && !color.equals("blue")) {
			PVPArena.lang.log_warning("teamnotfound",color);
			return;
		}
		String path = "losses." + color + "." + player.getName();
		int sum = 0;
		if (config.getProperty(path) != null)
			sum += config.getInt(path, 0);
		sum++;
		config.setProperty(path, Integer.valueOf(sum));
		config.save();
	}
}
