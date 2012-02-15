package net.slipcor.pvparena.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Arena;

/**
 * region manager class
 * 
 * -
 * 
 * provides commands to save win/lose stats to a yml file
 * 
 * @author slipcor
 * 
 * @version v0.6.2
 * 
 */

public class Regions {

	private final static Debug db = new Debug();

	/**
	 * check if an arena has overlapping battlefield region with another arena
	 * 
	 * @param arena2
	 *            TODO
	 * @param arena
	 *            the arena to check
	 * @return true if it does not overlap, false otherwise
	 */
	public static boolean checkRegion(Arena arena2, Arena arena) {
		if ((arena2.regions.get("battlefield") != null)
				&& (arena.regions.get("battlefield") != null)) {
			return !arena.regions.get("battlefield").overlapsWith(
					arena2.regions.get("battlefield"));
		}
		return true;
	}

	/**
	 * check if other running arenas are interfering with this arena
	 * 
	 * @return true if no running arena is interfering with this arena, false
	 *         otherwise
	 */
	public static boolean checkRegions(Arena arena) {
		if (!arena.cfg.getBoolean("periphery.checkRegions", false))
			return true;
		db.i("checking regions");

		return Arenas.checkRegions(arena);
	}

	/**
	 * is a player to far away to join?
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is too far away, false otherwise
	 */
	public static boolean tooFarAway(Arena arena, Player player) {
		int joinRange = arena.cfg.getInt("join.range", 0);
		if (joinRange < 1)
			return false;
		if (arena.regions.get("battlefield") == null) {
			Bukkit.getLogger().warning(
					"[PVP Arena] JoinRange set, but Battlefield not set!");
			return false;
		}
		return arena.regions.get("battlefield").tooFarAway(joinRange,
				player.getLocation());
	}
}
