package praxis.slipcor.pvparena.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import praxis.slipcor.pvparena.PAArena;

/*
 * Arena Manager class
 * 
 * author: slipcor
 * 
 * version: v0.3.0 - Multiple Arenas
 * 
 * history:
 * 		v0.3.0 - Multiple Arenas
 * 
 */

public class ArenaManager {
	static Map<String, PAArena> arenas = new HashMap<String, PAArena>();
	
	public static void loadArena(String configFile) {
		PAArena arena = new PAArena(configFile);
		arenas.put(arena.name, arena);
	}
	
	public static String getArenaNameByPlayer(Player pPlayer) {
		for (PAArena arena : arenas.values()) {
			if (arena.fightUsersClass.containsKey(pPlayer.getName())
					|| arena.fightUsersTeam.containsKey(pPlayer.getName()))
				return arena.name;
		}
		return null;
	}
	public static PAArena getArenaByPlayer(Player pPlayer) {
		for (PAArena arena : arenas.values()) {
			if (arena.fightUsersClass.containsKey(pPlayer.getName())
					|| arena.fightUsersTeam.containsKey(pPlayer.getName()))
				return arena;
		}
		return null;
	}
	
	public static PAArena getArenaByBattlefieldLocation(Location location) {
		for (PAArena arena : arenas.values()) {
			boolean inside = arena.contains(new Vector(location.getX(), location.getY(),location.getZ()));
			if (inside)
				return arena;
		}
		return null;
	}
	
	public static PAArena getArenaByName(String sName) {
		return arenas.get(sName);
	}

	public static void load_arenas() {
		int done = 0;
		try {
			File path = new File("plugins/pvparena");
			File[] f = path.listFiles();
			int i;
			for(i=0; i<f.length; i++){
				if(!f[i].isDirectory() && f[i].getName().contains("config_")) {
					String sName = f[i].getName().replace("config_", "");
					sName = sName.replace(".yml", "");
					loadArena(sName);
					done++;
	            }
			}
		} catch (Exception e) {
			return;
		}
		if (done == 0) {
			File path = new File("plugins/pvparena/config.yml"); // legacy import
			if (path.exists()) {
				path.renameTo( new File("plugins/pvparena/config_default.yml"));
				path = new File("plugins/pvparena/stats.yml");
				if (path.exists()) {
					path.renameTo( new File("plugins/pvparena/stats_default.yml"));
				}
			}
			loadArena("default");
		}
	}

	public static void reset() {
		for (PAArena arena : arenas.values()) {
			arena.reset();
			arena.fightUsersClass.clear();
		}
	}
	
	public static String getNames() {
		String result = "";
		for (String sName : arenas.keySet())
			result += (result.equals("")?"":", ") + sName;
		return result;
	}

	public static void unload(String string) {
		PAArena a = arenas.get(string);
		a.forcestop();
		arenas.remove(string);
		File path = new File("plugins/pvparena/config_" + string + ".yml");
		path.delete();
		path = new File("plugins/pvparena/stats_" + string + ".yml");
		path.delete();
	}
}
