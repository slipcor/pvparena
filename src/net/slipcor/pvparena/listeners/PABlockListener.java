package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.managers.ArenaManager;

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
 * version: v0.3.1 - New Arena! FreeFight
 * 
 * history:
 *
 *     v0.3.0 - Multiple Arenas
 * 	   v0.2.1 - cleanup, comments
 */

public class PABlockListener extends BlockListener {

	public PABlockListener() {}

	public void onBlockBreak(BlockBreakEvent event) {
		Arena arena = ArenaManager.getArenaByBattlefieldLocation(event.getBlock().getLocation());
		if (arena == null)
			return; // no arena => out
		
		if ((!(arena.protection)) || (!(arena.disableblockdamage)))
			return; // we don't need protection => OUT!

		if (arena.blocktnt) {
			event.setCancelled(true);
			return; // if we block TNT (what is the only restriction possible) => CANCEL AND OUT!
		}
		if (event.getBlock().getTypeId() == 46)
			return; // we do not block TNT, so just return if it is TNT
		event.setCancelled(true);
		return; // CANCEL AND OUT! this is protected property xD
	}

	public void onBlockIgnite(BlockIgniteEvent event) {
		Arena arena = ArenaManager.getArenaByBattlefieldLocation(event.getBlock().getLocation());
		if (arena == null)
			return; // no arena => out
		
		BlockIgniteEvent.IgniteCause cause = event.getCause();
		if ((arena.protection) && (
				((arena.disablelavafirespread) && (cause == BlockIgniteEvent.IgniteCause.LAVA))
			 || ((arena.disableallfirespread) && (cause == BlockIgniteEvent.IgniteCause.SPREAD)) 
			 || ((arena.blocklighter)) && (cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL))
			 ) { // if an event happend that we would like to block

			event.setCancelled(true); // ->cancel!
		}
	}

	public void onBlockBurn(BlockBurnEvent event) {
		Arena arena = ArenaManager.getArenaByBattlefieldLocation(event.getBlock().getLocation());
		if (arena == null)
			return; // no arena => out
		
		if ((!(arena.protection)) || (!(arena.disableallfirespread)))
			return; // if not an event happend that we would like to block => OUT
		
		event.setCancelled(true); // else->cancel!
		return;
	}

	public void onBlockPlace(BlockPlaceEvent event) {
		Arena arena = ArenaManager.getArenaByBattlefieldLocation(event.getBlock().getLocation());
		if (arena == null)
			return; // no arena => out
		
		if ((!(arena.protection)) || (!(arena.disableblockplacement)))
			return; // if not an event happend that we would like to block => OUT
		
		event.setCancelled(true);
		return;
	}
}