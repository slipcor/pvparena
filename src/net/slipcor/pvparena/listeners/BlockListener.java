package net.slipcor.pvparena.listeners;

import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Teams;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

/**
 * block listener class
 * 
 * -
 * 
 * PVP Arena Block Listener
 * 
 * @author slipcor
 * 
 * @version v0.7.17
 * 
 */

public class BlockListener implements Listener {
	private Debug db = new Debug(18);

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			db.i("oBBE cancelled");
			return;
		}
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null
				|| arena.edit
						|| (!(arena.cfg.getBoolean("protection.enabled", true))) || (!(arena.cfg
							.getBoolean("protection.blockdamage", true)))) {
			if (arena == null || arena.edit ) {
				return;
			}
			
			List<String> list = new ArrayList<String>();
			
			list = arena.cfg.getStringList("blocks.whitelist", list);
			
			if (list.size() > 0) {
				// WHITELIST!!!!!!!!!
				
				if (!list.contains(String.valueOf(event.getBlock().getTypeId()))) {
					// not on whitelist. DENY!
					event.setCancelled(true);
					return;
				}
			} else {
			
				list = arena.cfg.getStringList("blocks.blacklist", list);
				
				if (list.contains(String.valueOf(event.getBlock().getTypeId()))) {
					// on blacklist. DENY!
					event.setCancelled(true);
					return;
				}
			
			}
			PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
			return; // we don't need protection => OUT!
		}

		event.setCancelled(true);
		return;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block burn inside the arena");
		if ((!(arena.cfg.getBoolean("protection.enabled", true)))
				|| (!(arena.cfg.getBoolean("protection.fire.firespread", true)))) {
			// if not an event happened that we would like to block => OUT
			PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
			return;
		}

		event.setCancelled(true); // else->cancel!
		return;
	}

	public void onBlockFromTo(BlockFromToEvent event) {
		Block block = event.getToBlock();

		if (event.isCancelled()) {
			db.i("oBFTE cancelled");
			return;
		}
		Arena arena = Arenas.getArenaByRegionLocation(block.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block fluids inside the arena");
		if (arena.edit || (!(arena.cfg.getBoolean("protection.enabled", true)))
				|| (!(arena.cfg.getBoolean("protection.fluids", true)))) {
			PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
			PVPArena.instance.getAmm().onBlockPlace(arena, block, Material.AIR);
			return; // we don't need protection => OUT!
		}

		event.setCancelled(true);
		return;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block ignite inside the arena");

		if (Teams.countPlayersInTeams(arena) < 1)
			return; // no players, no game, no protection!

		BlockIgniteEvent.IgniteCause cause = event.getCause();
		if ((arena.cfg.getBoolean("protection.enabled", true))
				&& (((arena.cfg.getBoolean("protection.lavafirespread", true)) && (cause == BlockIgniteEvent.IgniteCause.LAVA))
						|| ((arena.cfg
								.getBoolean("protection.firespread", true)) && (cause == BlockIgniteEvent.IgniteCause.SPREAD)) || ((arena.cfg
						.getBoolean("protection.lighter", true)) && (cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL)))) {
			// if an event happened that we would like to block
			event.setCancelled(true); // ->cancel!
		}
	}

	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if (event.isCancelled()) {
			db.i("oBPEE cancelled");
			return;
		}

		Arena arena = null;

		for (Block block : event.getBlocks()) {
			arena = Arenas.getArenaByRegionLocation(block.getLocation());
			if (arena != null) {
				break;
			}
		}

		if (arena == null)
			return; // no arena => out

		db.i("block piston extend inside the arena");
		if (arena.edit || (!(arena.cfg.getBoolean("protection.enabled", true)))
				|| (!(arena.cfg.getBoolean("protection.piston", true)))) {

			for (Block block : event.getBlocks()) {
				PVPArena.instance.getAmm().onBlockPiston(arena, block);
			}
			// if not an event happened that we would like to block => OUT
			return;
		}
		event.setCancelled(true);
		return;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			db.i("oBPE cancelled");
			return;
		}
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block place inside the arena");
		if (arena.edit || (!(arena.cfg.getBoolean("protection.enabled", true)))
				|| (!(arena.cfg.getBoolean("protection.blockplace", true)))) {
			PVPArena.instance.getAmm().onBlockPlace(arena, event.getBlock(),
					event.getBlockReplacedState().getType());
			// if not an event happened that we would like to block => OUT
			return;
		}

		if (!arena.cfg.getBoolean("protection.tnt", true)
				&& event.getBlock().getTypeId() == 46) {
			if (arena.fightInProgress) {
				PVPArena.instance.getAmm().onBlockPlace(arena,
						event.getBlock(),
						event.getBlockReplacedState().getType());
			}
			return; // we do not block TNT, so just return if it is TNT
		}
		event.setCancelled(true);
		return;
	}

	@EventHandler()
	public void onSignChange(SignChangeEvent event) {
		PVPArena.instance.getAmm().onSignChange(event);
	}
}