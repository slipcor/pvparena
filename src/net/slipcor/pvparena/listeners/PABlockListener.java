package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.DebugManager;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

/*
 * BlockListener class
 * 
 * author: slipcor
 * 
 * version: v0.3.11 - set regions for lounges, spectator, exit
 * 
 * history:
 *
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.1 - New Arena! FreeFight
 *     v0.3.0 - Multiple Arenas
 * 	   v0.2.1 - cleanup, comments
 */

public class PABlockListener extends BlockListener {
	private DebugManager db = new DebugManager();
	
	public PABlockListener() {}

	public void onBlockBreak(BlockBreakEvent event) {
		Arena arena = ArenaManager.getArenaByRegionLocation(event.getBlock().getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block break inside the arena");
		if ((!(arena.usesProtection)) || (!(arena.disableBlockDamage)))
			return; // we don't need protection => OUT!

		if (arena.blockTnt) {
			event.setCancelled(true);
			return; // if we block TNT (what is the only restriction possible) => CANCEL AND OUT!
		}
		if (event.getBlock().getTypeId() == 46)
			return; // we do not block TNT, so just return if it is TNT
		event.setCancelled(true);
		return; // CANCEL AND OUT! this is protected property xD
	}

	public void onBlockIgnite(BlockIgniteEvent event) {
		Arena arena = ArenaManager.getArenaByRegionLocation(event.getBlock().getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block ignite inside the arena");
		BlockIgniteEvent.IgniteCause cause = event.getCause();
		if ((arena.usesProtection) && (
				((arena.disableLavaFireSpread) && (cause == BlockIgniteEvent.IgniteCause.LAVA))
			 || ((arena.disableAllFireSpread) && (cause == BlockIgniteEvent.IgniteCause.SPREAD)) 
			 || ((arena.blockIgnite)) && (cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL))
			 ) { // if an event happend that we would like to block

			event.setCancelled(true); // ->cancel!
		}
	}

	public void onBlockBurn(BlockBurnEvent event) {
		Arena arena = ArenaManager.getArenaByRegionLocation(event.getBlock().getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block burn inside the arena");
		if ((!(arena.usesProtection)) || (!(arena.disableAllFireSpread)))
			return; // if not an event happend that we would like to block => OUT
		
		event.setCancelled(true); // else->cancel!
		return;
	}

	public void onBlockPlace(BlockPlaceEvent event) {
		Arena arena = ArenaManager.getArenaByRegionLocation(event.getBlock().getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block place inside the arena");
		if ((!(arena.usesProtection)) || (!(arena.disableBlockPlacement)))
			return; // if not an event happend that we would like to block => OUT
		
		event.setCancelled(true);
		return;
	}
}