package net.slipcor.pvparena.api;

import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.ArenaManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * <pre>API class</pre>
 * 
 * provides import lightweight access to PVP Arena methods
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PVPArenaAPI {
	private static Debug db = new Debug(2);

	/**
	 * get the arena a player is in (fighting or spectating)
	 * 
	 * @param player
	 *            the player to check
	 * @return the arena name if part of an arena, "" otherwise
	 */
	public static String getArenaName(Player player) {
		db.i("API: get arena of player: " + player.getName());
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		return (arena == null) ? "" : arena.getName();
	}

	/**
	 * get the arena a location is in
	 * 
	 * @param location
	 *            the location to check
	 * @return the arena name if part of an arena, "" otherwise
	 */
	public static String getArenaNameByLocation(Location location) {
		db.i("API: get arena of location: " + location.toString());
		Arena arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(location));
		return (arena == null) ? "" : arena.getName();
	}

	/**
	 * get the arenas a location is in
	 * 
	 * @param location
	 *            the location to check
	 * @return the arena name if part of an arena, "" otherwise
	 */
	public static HashSet<String> getArenaNamesByLocation(Location location) {
		db.i("API: get arena of location: " + location.toString());
		HashSet<Arena> arenas = ArenaManager.getArenasByRegionLocation(new PABlockLocation(location));
		
		HashSet<String> result = new HashSet<String>();
		
		for (Arena a : arenas) {
			result.add(a.getName());
		}
		
		return result;
	}
}
