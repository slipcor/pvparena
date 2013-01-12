package net.slipcor.pvparena.listeners;

import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
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
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * <pre>Block Listener class</pre>
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class BlockListener implements Listener {
	private static Debug db = new Debug(20);

	private boolean willBeSkipped(Event event, Location loc, RegionProtection rp) {
		Arena arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(loc));
		
		if (arena == null) {
			// no arena at all 
			return true;
		}
		
		if (arena.isLocked() || !arena.isFightInProgress()) {
			if (event instanceof Cancellable) {
				Cancellable c = (Cancellable) event;
				c.setCancelled(!PAA_Edit.activeEdits.containsValue(arena));
			}
			return PAA_Edit.activeEdits.containsValue(arena);
		}
		
		arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(loc), rp);
		
		if (arena == null) {
			return false;
		}

		return PAA_Edit.activeEdits.containsValue(arena);
	}

	static boolean isProtected(Location loc, Cancellable event, RegionProtection node) {
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(loc), node);
		if (arena == null) {
			return false;
		}
		//db.i("protection " + node.name() + " enabled and thus cancelling " + event.toString());
		event.setCancelled(true);
		return true;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		db.i("onBlockBreak", event.getPlayer());
		if (willBeSkipped(event, event.getBlock()
				.getLocation(), RegionProtection.BREAK)) {
			db.i("willbeskipped. GFYS!!!!", event.getPlayer());
			return;
		}

		List<String> list = new ArrayList<String>();
		
		Arena arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getBlock().getLocation()));
		
		list = arena.getArenaConfig().getStringList(CFG.LISTS_WHITELIST.getNode() + ".break", new ArrayList<String>());

		if (list.size() > 0) {
			// WHITELIST!!!!!!!!!

			if (!list.contains(String.valueOf(event.getBlock().getTypeId()))
					&& !list.contains(String.valueOf(event.getBlock().getType().name()))
					&& !list.contains(String.valueOf(event.getBlock().getTypeId()) + ":" + event.getBlock().getData())
					&& !list.contains(String.valueOf(event.getBlock().getType().name()) + ":" + event.getBlock().getData())) {
				arena.msg(event.getPlayer(), Language.parse(MSG.ERROR_WHITELIST_DISALLOWED, Language.parse(MSG.GENERAL_BREAK)));
				// not on whitelist. DENY!
				event.setCancelled(true);
				db.i("whitelist out", event.getPlayer());
				return;
			}
		}

		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.BREAK)) {
			db.i("isprotected!", event.getPlayer());
			return;
		}

		list = arena.getArenaConfig().getStringList(CFG.LISTS_BLACKLIST.getNode() + ".break", new ArrayList<String>());

		if (list.contains(String.valueOf(event.getBlock().getTypeId()))
				|| list.contains(String.valueOf(event.getBlock().getType().name()))
				|| list.contains(String.valueOf(event.getBlock().getTypeId()) + ":" + event.getBlock().getData())
				|| list.contains(String.valueOf(event.getBlock().getType().name()) + ":" + event.getBlock().getData())) {
			arena.msg(event.getPlayer(), Language.parse(MSG.ERROR_BLACKLIST_DISALLOWED, Language.parse(MSG.GENERAL_BREAK)));
			// on blacklist. DENY!
			event.setCancelled(true);
			db.i("blacklist out", event.getPlayer());
			return;
		}

		db.i("onBlockBreak !!!", event.getPlayer());
		
		ArenaModuleManager.onBlockBreak(arena, event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getBlock()
				.getLocation()), RegionProtection.FIRE);
		if (arena == null) {
			return; // no arena => out

		//db.i("block burn inside the arena");
                }
		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.FIRE)) {
			return;
		}

		
		ArenaModuleManager.onBlockBreak(arena, event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockDecay(LeavesDecayEvent event) {
		if (willBeSkipped(event, event.getBlock()
				.getLocation(), RegionProtection.NATURE)) {
			return;
		}

		Block block = event.getBlock();
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);

		if (arena == null) {
			db.i("block decaying inside the arena, not protected");
			return;
		}
		
		db.i("block block decaying inside the arena");

		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.NATURE)) {
			return;
		}

		
		ArenaModuleManager.onBlockBreak(arena, event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockFade(BlockFadeEvent event) {
		if (willBeSkipped(event, event.getBlock()
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
		
		ArenaModuleManager.onBlockChange(arena, event.getBlock(),
				event.getNewState());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent event) {
		if (willBeSkipped(event, event.getToBlock()
				.getLocation(), RegionProtection.NATURE)) {
			return;
		}

		Block block = event.getToBlock();
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);

		if (arena == null) {
			return;
		}
		
		//db.i("block fluids inside the arena");

		if (isProtected(block.getLocation(), event, RegionProtection.NATURE)) {
			return;
		}

		
		ArenaModuleManager.onBlockBreak(arena, event.getBlock());
		
		ArenaModuleManager.onBlockPlace(arena, block, Material.AIR);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		if (willBeSkipped(event, event.getBlock()
				.getLocation(), RegionProtection.NATURE)) {
			return;
		}

		Block block = event.getBlock();
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);
		if (arena == null) {
			//db.i("block forming not inside the arena");
			return;
		}
		//db.i("block block forming inside the arena");

		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.NATURE)) {
			return;
		}

		
		ArenaModuleManager.onBlockChange(arena, event.getBlock(),
				event.getNewState());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockGrow(StructureGrowEvent event) {
		Arena arena = null;

		for (BlockState block : event.getBlocks()) {
			arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);
			if (arena != null) {
				break;
			}
		}

		if (arena == null) {
			return; // no arena => out
                }
		for (BlockState block : event.getBlocks()) {
			arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(block.getLocation()), RegionProtection.NATURE);
			if (arena != null) {
				continue;
			}
			if (isProtected(block.getLocation(), event, RegionProtection.NATURE)) {
				return;
			}
			
			ArenaModuleManager.onBlockChange(arena, block.getBlock(),
					block);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (willBeSkipped(event, event.getBlock()
				.getLocation(), RegionProtection.FIRE)) {
			return;
		}
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getBlock()
				.getLocation()), RegionProtection.FIRE);

		if (arena == null) {
			return;
		}
		
		db.i("block ignite inside the arena", event.getPlayer());
		event.setCancelled(!arena.isFightInProgress());
		//BlockIgniteEvent.IgniteCause cause = event.getCause();
		if (arena.getArenaConfig().getBoolean(CFG.PROTECT_ENABLED)
				&& (isProtected(event.getBlock().getLocation(), event, RegionProtection.FIRE))) {
			// if an event happened that we would like to block
			event.setCancelled(true); // ->cancel!
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
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

		if (arena == null) {
			return; // no arena => out
                }
		db.i("block piston extend inside the arena");
		for (Block block : event.getBlocks()) {
			
			ArenaModuleManager.onBlockPiston(arena, block);
		}
		return;
		
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (willBeSkipped(event, event.getBlock()
				.getLocation(), RegionProtection.PLACE)) {
			return;
		}
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getBlock()
				.getLocation()), RegionProtection.PLACE);

		arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getBlock()
				.getLocation()));
		List<String> list = new ArrayList<String>();

		list = arena.getArenaConfig().getStringList(CFG.LISTS_WHITELIST.getNode() + ".place", new ArrayList<String>());

		if (list.size() > 0) {
			// WHITELIST!!!!!!!!!

			if (!list.contains(String.valueOf(event.getBlockPlaced().getTypeId())) &&
					!list.contains(String.valueOf(event.getBlockPlaced().getType().name()))
					&& !list.contains(String.valueOf(event.getBlockPlaced().getTypeId()) + ":" + event.getBlock().getData())
					&& !list.contains(String.valueOf(event.getBlockPlaced().getType().name()) + ":" + event.getBlock().getData())) {
				arena.msg(event.getPlayer(), Language.parse(MSG.ERROR_WHITELIST_DISALLOWED, Language.parse(MSG.GENERAL_PLACE)));
				// not on whitelist. DENY!
				event.setCancelled(true);
				return;
			}
		}

		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.PLACE)) {
			if (arena.isFightInProgress() &&
					!isProtected(event.getBlock().getLocation(), event, RegionProtection.TNT) &&
					event.getBlock().getTypeId() == 46) {
				
				ArenaModuleManager.onBlockPlace(arena,
						event.getBlock(),
						event.getBlockReplacedState().getType());
				event.setCancelled(false);
				return; // we do not block TNT, so just return if it is TNT
			}
			return;
		}

		list = arena.getArenaConfig().getStringList(CFG.LISTS_BLACKLIST.getNode() + ".place", new ArrayList<String>());

		if (list.contains(String.valueOf(event.getBlockPlaced().getTypeId())) ||
				list.contains(String.valueOf(event.getBlockPlaced().getType().name()))
				|| list.contains(String.valueOf(event.getBlockPlaced().getTypeId()) + ":" + event.getBlock().getData())
				|| list.contains(String.valueOf(event.getBlockPlaced().getType().name()) + ":" + event.getBlock().getData())) {
			arena.msg(event.getPlayer(), Language.parse(MSG.ERROR_BLACKLIST_DISALLOWED, Language.parse(MSG.GENERAL_PLACE)));
			// on blacklist. DENY!
			event.setCancelled(true);
			return;
		}
		
		
		ArenaModuleManager.onBlockPlace(arena, event.getBlock(),
				event.getBlockReplacedState().getType());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(HangingPlaceEvent event) {
		if (willBeSkipped(event, event.getBlock()
				.getLocation(), RegionProtection.PAINTING)) {
			return;
		}

		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getBlock()
				.getLocation()), RegionProtection.PAINTING);

		db.i("painting place inside the arena", event.getPlayer());

		if (isProtected(event.getBlock().getLocation(), event, RegionProtection.PAINTING)) {
			return;
		}
		
		
		ArenaModuleManager.onBlockPlace(arena, event.getBlock(),
				event.getBlock().getType());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(HangingBreakEvent event) {
		if (willBeSkipped(event, event.getEntity()
				.getLocation(), RegionProtection.PAINTING)) {
			return;
		}

		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getEntity()
				.getLocation()), RegionProtection.PAINTING);

		if (isProtected(event.getEntity().getLocation(), event, RegionProtection.PAINTING)) {
			return;
		}
		
		db.i("painting break inside the arena");
		ArenaModuleManager.onPaintingBreak(arena,
				event.getEntity(), event.getEntity().getType());
	}
}