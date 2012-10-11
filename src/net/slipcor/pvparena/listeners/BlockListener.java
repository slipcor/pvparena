package net.slipcor.pvparena.listeners;

import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionProtection;
import net.slipcor.pvparena.managers.ArenaManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * <pre>Block Listener class</pre>
 * 
 * @author slipcor
 * 
 * @version v0.9.3
 */

public class BlockListener implements Listener {
	private static Debug db = new Debug(20);

	private boolean willBeSkipped(boolean cancelled, Event event, Location loc, RegionProtection rp) {
		Arena arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(loc));
		
		if (arena == null) {
			db.i("not a region: " + event.getEventName());
			return true;
		}
		
		arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(loc), rp);
		
		if (arena == null) {
			arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(loc));
			if (arena == null) {
				return false;
			}

			return arena.isLocked();
		}
		if (cancelled) {
			db.i("already cancelled: " + event.getEventName());
			return true;
		}
		
		return arena.isLocked();
	}

	static boolean isProtected(Location loc, Cancellable event, RegionProtection node) {
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(loc), node);
		if (arena == null) {
			return false;
		}
		db.i("protection " + node.name() + " enabled and thus cancelling " + event.toString());
		event.setCancelled(true);
		return true;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		db.i("onBlockBreak");
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation(), RegionProtection.BREAK)) {
			db.i("willbeskipped. GFYS!!!!");
			return;
		}
		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.BREAK)) {
			db.i("isprotected!");
			return;
		}

		List<String> list = new ArrayList<String>();
		
		Arena arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getBlock().getLocation()));
		
		list = arena.getArenaConfig().getStringList(CFG.LISTS_WHITELIST.getNode(), list);

		if (list.size() > 0) {
			// WHITELIST!!!!!!!!!

			if (!list.contains(String.valueOf(event.getBlock().getTypeId()))) {
				event.getPlayer().sendMessage("not contained, out!");
				// not on whitelist. DENY!
				event.setCancelled(true);
				db.i("whitelist out");
				return;
			}
		} else {

			list = arena.getArenaConfig().getStringList(CFG.LISTS_BLACKLIST.getNode(), list);

			if (list.contains(String.valueOf(event.getBlock().getTypeId()))) {
				event.getPlayer().sendMessage("blacklist contains");
				// on blacklist. DENY!
				event.setCancelled(true);
				db.i("blacklist out");
				return;
			}

		}
		db.i("onBlockBreak !!!");
		PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getBlock()
				.getLocation()), RegionProtection.FIRE);
		if (arena == null)
			return; // no arena => out

		db.i("block burn inside the arena");

		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.FIRE)) {
			return;
		}

		PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockDecay(LeavesDecayEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation(), RegionProtection.NATURE)) {
			return;
		}

		Block block = event.getBlock();
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);

		db.i("block block decaying inside the arena");

		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.NATURE)) {
			return;
		}

		PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockFade(BlockFadeEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation(), RegionProtection.NATURE)) {
			return;
		}

		Block block = event.getBlock();
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);

		if (arena == null) {
			db.i("not inside an arena");
			return;
		}
		
		db.i("block block fading inside the arena");
		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.NATURE)) {
			return;
		}
		PVPArena.instance.getAmm().onBlockChange(arena, event.getBlock(),
				event.getNewState());
	}

	public void onBlockFromTo(BlockFromToEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getToBlock()
				.getLocation(), RegionProtection.NATURE)) {
			return;
		}

		Block block = event.getToBlock();
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);

		db.i("block fluids inside the arena");

		if (isProtected(block.getLocation(), event, RegionProtection.NATURE)) {
			return;
		}

		PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
		PVPArena.instance.getAmm().onBlockPlace(arena, block, Material.AIR);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockForm(BlockFormEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation(), RegionProtection.NATURE)) {
			return;
		}

		Block block = event.getBlock();
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);
		if (arena == null) {
			db.i("block forming not inside the arena");
			return;
		}
		db.i("block block forming inside the arena");

		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.NATURE)) {
			return;
		}

		PVPArena.instance.getAmm().onBlockChange(arena, event.getBlock(),
				event.getNewState());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockGrow(StructureGrowEvent event) {
		if (event.isCancelled()) {
			db.i("oSGE cancelled");
			return;
		}
		Arena arena = null;

		for (BlockState block : event.getBlocks()) {
			arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);
			if (arena != null) {
				break;
			}
		}

		if (arena == null)
			return; // no arena => out

		for (BlockState block : event.getBlocks()) {
			arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);
			if (arena != null) {
				continue;
			}
			if (isProtected(block.getLocation(), event, RegionProtection.NATURE)) {
				return;
			}
			PVPArena.instance.getAmm().onBlockChange(arena, block.getBlock(),
					block);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation(), RegionProtection.FIRE)) {
			return;
		}
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getBlock()
				.getLocation()), RegionProtection.FIRE);

		if (arena == null) {
			return;
		}
		
		db.i("block ignite inside the arena");
		event.setCancelled(!arena.isFightInProgress());
		//BlockIgniteEvent.IgniteCause cause = event.getCause();
		if (arena.getArenaConfig().getBoolean(CFG.PROTECT_ENABLED)
				&& (isProtected(event.getBlock().getLocation(), event, RegionProtection.FIRE))) {
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
			arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.PISTON);
			if (arena != null) {
				if (isProtected(event.getBlock().getLocation(), event, RegionProtection.PISTON)) {
					return;
				}
				break;
			}
		}

		if (arena == null)
			return; // no arena => out

		db.i("block piston extend inside the arena");
		for (Block block : event.getBlocks()) {
			PVPArena.instance.getAmm().onBlockPiston(arena, block);
		}
		return;
		
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation(), RegionProtection.PLACE)) {
			return;
		}
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getBlock()
				.getLocation()), RegionProtection.PLACE);

		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.PLACE)) {
			if (arena.isFightInProgress() &&
					!isProtected(event.getBlock().getLocation(), event, RegionProtection.TNT) &&
					event.getBlock().getTypeId() == 46) {
				PVPArena.instance.getAmm().onBlockPlace(arena,
						event.getBlock(),
						event.getBlockReplacedState().getType());
				event.setCancelled(false);
				return; // we do not block TNT, so just return if it is TNT
			}
			return;
		}

		arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getBlock()
				.getLocation()));
		List<String> list = new ArrayList<String>();

		list = arena.getArenaConfig().getStringList(CFG.LISTS_WHITELIST.getNode(), list);

		if (list.size() > 0) {
			// WHITELIST!!!!!!!!!

			if (!list.contains(String.valueOf(event.getBlockPlaced().getTypeId()))) {
				event.getPlayer().sendMessage("not contained, out!");
				// not on whitelist. DENY!
				event.setCancelled(true);
				return;
			}
		} else {

			list = arena.getArenaConfig().getStringList(CFG.LISTS_BLACKLIST.getNode(), list);

			if (list.contains(String.valueOf(event.getBlockPlaced().getTypeId()))) {
				event.getPlayer().sendMessage("blacklist contains");
				// on blacklist. DENY!
				event.setCancelled(true);
				return;
			}

		}
		PVPArena.instance.getAmm().onBlockPlace(arena, event.getBlock(),
				event.getBlockReplacedState().getType());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(PaintingPlaceEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation(), RegionProtection.PAINTING)) {
			return;
		}

		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getBlock()
				.getLocation()), RegionProtection.PAINTING);

		db.i("painting place inside the arena");

		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.PAINTING)) {
			return;
		}
		
		PVPArena.instance.getAmm().onBlockPlace(arena, event.getBlock(),
				event.getBlock().getType());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(PaintingBreakEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getPainting()
				.getLocation(), RegionProtection.PAINTING)) {
			return;
		}

		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getPainting()
				.getLocation()), RegionProtection.PAINTING);

		if (isProtected(event.getPainting().getLocation(), event, RegionProtection.PAINTING)) {
			return;
		}
		
		db.i("painting break inside the arena");
		PVPArena.instance.getAmm().onPaintingBreak(arena,
				event.getPainting(), event.getPainting().getType());
	}
}