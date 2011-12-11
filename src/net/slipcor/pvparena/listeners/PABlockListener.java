/*
 * block listener class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.3.11 - set regions for lounges, spectator, exit
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.1 - New Arena! FreeFight
 *     v0.3.0 - Multiple Arenas
 * 	   v0.2.1 - cleanup, comments
 */

package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.DebugManager;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PABlockListener extends BlockListener {
	private DebugManager db = new DebugManager();

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		Arena arena = ArenaManager.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block break inside the arena");
		if ((!(arena.usesProtection)) || (!(arena.disableBlockDamage)))
			return; // we don't need protection => OUT!

		if (arena.disableTnt) {
			event.setCancelled(true);
			return; // if we block TNT (what is the only restriction possible)
					// => CANCEL AND OUT!
		}
		if (event.getBlock().getTypeId() == 46)
			return; // we do not block TNT, so just return if it is TNT
		event.setCancelled(true);
		return; // CANCEL AND OUT! this is protected property xD
	}

	@Override
	public void onBlockIgnite(BlockIgniteEvent event) {
		Arena arena = ArenaManager.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block ignite inside the arena");
		BlockIgniteEvent.IgniteCause cause = event.getCause();
		if ((arena.usesProtection)
				&& (((arena.disableLavaFireSpread) && (cause == BlockIgniteEvent.IgniteCause.LAVA))
						|| ((arena.disableAllFireSpread) && (cause == BlockIgniteEvent.IgniteCause.SPREAD)) || ((arena.disableIgnite))
						&& (cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL))) {
			// if an event happened that we would like to block
			event.setCancelled(true); // ->cancel!
		}
	}

	@Override
	public void onBlockBurn(BlockBurnEvent event) {
		Arena arena = ArenaManager.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block burn inside the arena");
		if ((!(arena.usesProtection)) || (!(arena.disableAllFireSpread)))
			// if not an event happend that we would like to block => OUT
			return;

		event.setCancelled(true); // else->cancel!
		return;
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		Arena arena = ArenaManager.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block place inside the arena");
		if ((!(arena.usesProtection)) || (!(arena.disableBlockPlacement)))
			// if not an event happend that we would like to block => OUT
			return;

		event.setCancelled(true);
		return;
	}
}