package net.slipcor.pvparena.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
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
 * @version v0.6.41
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
			return Spawns.getRegionCenter(arena).distance(player.getLocation()) > joinRange;
		}
		return arena.regions.get("battlefield").tooFarAway(joinRange,
				player.getLocation());
	}

	/**
	 * check if an admin tries to set an arena position
	 * 
	 * @param event
	 *            the interact event to hand over
	 * @param player
	 *            the player interacting
	 * @return true if the position is being saved, false otherwise
	 */
	public static boolean checkRegionSetPosition(PlayerInteractEvent event,
			Player player) {
		Arena arena = Arenas.getArenaByName(Arena.regionmodify);
		if (arena != null
				&& (PVPArena.hasAdminPerms(player) || (PVPArena.hasCreatePerms(
						player, arena)))
				&& (player.getItemInHand() != null)
				&& (player.getItemInHand().getTypeId() == arena.cfg.getInt(
						"setup.wand", 280))) {
			// - modify mode is active
			// - player has admin perms
			// - player has wand in hand
			db.i("modify&adminperms&wand");
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				arena.pos1 = event.getClickedBlock().getLocation();
				Arenas.tellPlayer(player, Language.parse("pos1"), arena.prefix);
				event.setCancelled(true); // no destruction in creative mode :)
				return true; // left click => pos1
			}

			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				arena.pos2 = event.getClickedBlock().getLocation();
				Arenas.tellPlayer(player, Language.parse("pos2"), arena.prefix);
				return true; // right click => pos2
			}
		}
		return false;
	}
}
