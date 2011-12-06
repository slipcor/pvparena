package net.slipcor.pvparena.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.arenas.CTFArena;
import net.slipcor.pvparena.arenas.TeamArena;
import net.slipcor.pvparena.arenas.FreeArena;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/*
 * Arena Manager class
 * 
 * author: slipcor
 * 
 * version: v0.3.14 - timed arena modes
 * 
 * history:
 *
 *     v0.3.11 - set regions for lounges, spectator, exit
 *     v0.3.9 - Permissions, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.6 - CTF Arena
 *     v0.3.5 - Powerups!!
 *     v0.3.1 - New Arena! FreeFight
 *     v0.3.0 - Multiple Arenas
 * 
 */

public class ArenaManager {
	static Map<String, Arena> arenas = new HashMap<String, Arena>();
	private static DebugManager db = new DebugManager();
	
	/*
	 * load arena with given name and type
	 */
	public static void loadArena(String configFile, String type) {
		Arena arena;
		db.i("loading arena " + configFile + " (" + type + ")");
		if (type.equals("free"))
			arena = new FreeArena(configFile);
		else if (type.equals("ctf"))
			arena = new CTFArena(configFile);
		else
			arena = new TeamArena(configFile);
		
		arenas.put(arena.name, arena);
	}
	
	/*
	 * find player, return arena name
	 */
	public static String getArenaNameByPlayer(Player pPlayer) {
		for (Arena arena : arenas.values()) {
			if (arena.paPlayersClass.containsKey(pPlayer.getName())
					|| arena.paPlayersTeam.containsKey(pPlayer.getName()))
				return arena.name;
		}
		return null;
	}
	
	/*
	 * find player, return arena
	 */
	public static Arena getArenaByPlayer(Player pPlayer) {
		for (Arena arena : arenas.values()) {
			if (arena.paPlayersClass.containsKey(pPlayer.getName())
					|| arena.paPlayersTeam.containsKey(pPlayer.getName()))
				return arena;
		}
		return null;
	}
	
	/*
	 * find location, return arena
	 */
	public static Arena getArenaByRegionLocation(Location location) { 
		for (Arena arena : arenas.values()) {
			boolean inside = arena.contains(new Vector(location.getX(), location.getY(),location.getZ()));
			if (inside)
				return arena;
		}
		return null;
	}
	
	/*
	 * find name, return arena
	 */
	public static Arena getArenaByName(String sName) {
		return arenas.get(sName);
	}

	/*
	 * load all configs in the PVP Arena folder
	 */
	public static void load_arenas() {
		int done = 0;
		db.i("loading arenas...");
		try {
			File path = new File("plugins/pvparena");
			File[] f = path.listFiles();
			int i;
			for(i=0; i<f.length; i++){
				if(!f[i].isDirectory() && f[i].getName().contains("config_")) {
					String sName = f[i].getName().replace("config_", "");
					sName = sName.replace(".yml", "");
					db.i("standard arena: "+sName);
					loadArena(sName,"");
					done++;
	            }
			}
			for(i=0; i<f.length; i++){
				if(!f[i].isDirectory() && f[i].getName().contains("config.free_")) {
					String sName = f[i].getName().replace("config.free_", "");
					sName = sName.replace(".yml", "");
					db.i("free arena: "+sName);
					loadArena(sName,"free");
					done++;
	            }
			}
			for(i=0; i<f.length; i++){
				if(!f[i].isDirectory() && f[i].getName().contains("config.ctf_")) {
					String sName = f[i].getName().replace("config.ctf_", "");
					sName = sName.replace(".yml", "");
					db.i("ctf arena: " +sName);
					loadArena(sName,"ctf");
					done++;
	            }
			}
		} catch (Exception e) {
			e.printStackTrace();
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
			loadArena("default","");
		}
	}

	/*
	 * reset all arenas
	 */
	public static void reset() {
		for (Arena arena : arenas.values()) {
			db.i("resetting arena " + arena.name);
			arena.reset();
			arena.paPlayersClass.clear();
		}
	}
	
	/*
	 * return a list of all arena names
	 */
	public static String getNames() {
		String result = "";
		for (String sName : arenas.keySet())
			result += (result.equals("")?"":", ") + sName;
		db.i("arenas: " + result);
		return result;
	}

	/*
	 * unload and delete an arena
	 */
	public static void unload(String string) {
		Arena a = arenas.get(string);
		db.i("unloading arena " + a.name);
		a.forcestop();
		arenas.remove(string);
		File path;
		if (a instanceof FreeArena) {
			path = new File("plugins/pvparena/config.free_" + string + ".yml");
		} else if (a instanceof CTFArena) {
			path = new File("plugins/pvparena/config.ctf_" + string + ".yml");
		} else {
			path = new File("plugins/pvparena/config_" + string + ".yml");
		}
		path.delete();
		path = new File("plugins/pvparena/stats_" + string + ".yml");
		path.delete();
	}

	/*
	 * powerup tick, tick each arena that uses powerups
	 */
	public static void powerupTick() {
		for (Arena arena : arenas.values()) {
			if (arena.pm == null)
				continue;
			db.i("ticking: arena " + arena.name);
			arena.pm.tick();
		}
	}

	/*
	 * return arena count
	 */
	public static int count() {
		return arenas.size();
	}

	/*
	 * return the first arena
	 */
	public static Arena getFirst() {
		for (Arena arena : arenas.values()) {
			return arena;
		}
		return null;
	}

	/*
	 * returns "is no running arena interfering with given arena"
	 */
	public static boolean checkRegions(Arena arena) {
		for (Arena a : arenas.values()) {
			if (a.equals(arena))
				continue;
			
			if ((a.fightInProgress) && !a.checkRegion(arena)) {
				return false;
			}	
		}
		return true;
	}
}
