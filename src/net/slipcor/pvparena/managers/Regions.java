package net.slipcor.pvparena.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;

/**
 * region manager class
 * 
 * -
 * 
 * provides commands to save win/lose stats to a yml file
 * 
 * @author slipcor
 * 
 * @version v0.7.0
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
		if ((a1.getRegion("battlefield") != null)
				&& (a2.getRegion("battlefield") != null)) {
			db.i("checking battlefield region overlapping");
			return !a2.getRegion("battlefield").overlapsWith(
					a1.getRegion("battlefield"));
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
		if (!arena.getArenaConfig().getBoolean("periphery.checkRegions", false))
			return true;
		db.i("checking regions");

		return Arenas.checkRegions(arena);
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
		if (!PAA_Region.activeSelections.containsKey(player.getName())) {
			return false;
		}
		Arena arena = PAA_Region.activeSelections.get(player.getName());
		if (arena != null
				&& (PVPArena.hasAdminPerms(player) || (PVPArena.hasCreatePerms(
						player, arena)))
				&& (player.getItemInHand() != null)
				&& (player.getItemInHand().getTypeId() == arena.getArenaConfig().getInt(
						"setup.wand", 280))) {
			// - modify mode is active
			// - player has admin perms
			// - player has wand in hand
			db.i("modify&adminperms&wand");
			ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				ap.pos1 = event.getClickedBlock().getLocation();
				arena.msg(player, Language.parse("pos1"));
				event.setCancelled(true); // no destruction in creative mode :)
				return true; // left click => pos1
			}

			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				ap.pos2 = event.getClickedBlock().getLocation();
				arena.msg(player, Language.parse("pos2"));
				return true; // right click => pos2
			}
		}
		return false;
	}

	/**
	 * is a player to far away to join?
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is too far away, false otherwise
	 */
	public static boolean tooFarAway(Arena arena, Player player) {
		int joinRange = arena.getArenaConfig().getInt("join.range", 0);
		if (joinRange < 1)
			return false;
		if (arena.getRegion("battlefield") == null) {
			return Spawns.getRegionCenter(arena).getDistance(new PABlockLocation(player.getLocation())) > joinRange;
		}
		return arena.getRegion("battlefield").tooFarAway(joinRange,
				player.getLocation());
	}
}
