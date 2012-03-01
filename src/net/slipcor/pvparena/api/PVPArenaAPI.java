package net.slipcor.pvparena.api;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * api class
 * 
 * -
 * 
 * provides import lightweight access to PVP Arena methods
 * 
 * @author slipcor
 * 
 * @version v0.6.15
 * 
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
		Arena arena = Arenas.getArenaByPlayer(player);
		return (arena == null) ? "" : arena.name;
	}

	/**
	 * get the arena a location is in (based on arena region check settings!)
	 * 
	 * @param location
	 *            the location to check
	 * @return the arena name if part of an arena, "" otherwise
	 */
	public static String getArenaNameByLocation(Location location) {
		db.i("API: get arena of location: " + location.toString());
		Arena arena = Arenas.getArenaByRegionLocation(location);
		return (arena == null) ? "" : arena.name;
	}
}
