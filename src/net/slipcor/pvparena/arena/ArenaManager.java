package net.slipcor.pvparena.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ArenaManager {
	
	private static List<Arena> arenas = new ArrayList<Arena>();
	private static List<ArenaClass> classes = new ArrayList<ArenaClass>();
	private static List<ArenaPlayer> players = new ArrayList<ArenaPlayer>();
	
	// Why Lists, not HashSets? otherwise, will be done.
	
	public static void createArena(String name, String type) {
		Arena arena = new Arena(name, type);
		arenas.add(arena);
	}
	
	public static void createClass(String name, Map<Material, Integer> amounts) {
		ArenaClass aClass = new ArenaClass(name, amounts);
		classes.add(aClass);
	}
	
	public static void createPlayer(Player player, Arena arena) {
		ArenaPlayer aPlayer = new ArenaPlayer(player, arena);
		players.add(aPlayer);
	}
	
	public static ArenaClass getArenaClass(String name) {
		for (ArenaClass aClass : classes) {
			if (aClass.getName().equalsIgnoreCase(name))
				return aClass;
		}
		
		return null;
	}
}