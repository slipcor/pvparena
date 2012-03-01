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
 * @version v0.6.15
 * 
 */

public class Regions {

	private final static Debug db = new Debug(33);

	/**
	 * check if an arena has overlapping battlefield region with another arena
	 * 
	 * @param a1
	 *            the arena to check
	 * @param a2
	 *            the arena to check
	 * @return true if it does not overlap, false otherwise
	 */
	public static boolean checkRegion(Arena a1, Arena a2) {
		if ((a1.regions.get("battlefield") != null)
				&& (a2.regions.get("battlefield") != null)) {
			db.i("checking battlefield region overlapping");
			return !a2.regions.get("battlefield").overlapsWith(
					a1.regions.get("battlefield"));
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
					"[PVP Arena] join range set, but battlefield not set!");
			return false;
		}
		return arena.regions.get("battlefield").tooFarAway(joinRange,
				player.getLocation());
	}
}
