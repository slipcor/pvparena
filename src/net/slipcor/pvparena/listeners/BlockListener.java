package net.slipcor.pvparena.listeners;

import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;

import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.commands.PAA_Setup;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionProtection;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.runnables.DamageResetRunnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
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
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

/**
 * <pre>
 * Block Listener class
 * </pre>
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class BlockListener implements Listener {
	private final static Debug DEBUG = new Debug(20);

	private boolean willBeSkipped(final Event event, final Location loc, final RegionProtection rp) {
		Arena arena = ArenaManager
				.getArenaByRegionLocation(new PABlockLocation(loc));

		if (arena == null) {
			// no arena at all
			return true;
		}

		if (arena.isLocked() || !arena.isFightInProgress()) {
			if (event instanceof Cancellable) {
				final Cancellable cEvent = (Cancellable) event;
				cEvent.setCancelled(!(PAA_Edit.activeEdits.containsValue(arena)||PAA_Setup.activeSetups.containsValue(arena)));
			}
			return (PAA_Edit.activeEdits.containsValue(arena)||PAA_Setup.activeSetups.containsValue(arena));
		}

		arena = ArenaManager.getArenaByProtectedRegionLocation(
				new PABlockLocation(loc), rp);

		if (arena == null) {
			return false;
		}

		return PAA_Edit.activeEdits.containsValue(arena);
	}

	protected static boolean isProtected(final Location loc, final Cancellable event,
			final RegionProtection node) {
		final Arena arena = ArenaManager.getArenaByProtectedRegionLocation(
				new PABlockLocation(loc), node);
		if (arena == null) {
			return false;
		}
		
		if (event instanceof PlayerEvent) {
			PlayerEvent e = (PlayerEvent) event;
			
			ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(e.getPlayer().getName());
			
			if (aPlayer.getArena() != null && aPlayer.getArena() != arena) {
				return false; // players in arenas should be caught by their arenas
			}
		}
		
		// debug.i("protection " + node.name() + " enabled and thus cancelling " +
		// event.toString());
		event.setCancelled(true);
		return true;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event) {
		DEBUG.i("onBlockBreak", event.getPlayer());
		if (willBeSkipped(event, event.getBlock().getLocation(),
				RegionProtection.BREAK)) {
			DEBUG.i("willbeskipped. GFYS!!!!", event.getPlayer());
			return;
		}
		
		if(ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.LOST
				|| ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.WATCH
				|| ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.LOUNGE
				|| ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.READY) {
			event.setCancelled(true);
			return;
		}

		final Arena arena = ArenaManager
				.getArenaByRegionLocation(new PABlockLocation(event.getBlock()
						.getLocation()));

		final List<String> list = arena.getArenaConfig().getStringList(
				CFG.LISTS_WHITELIST.getNode() + ".break",
				new ArrayList<String>());

		if (!list.isEmpty()
				&& !list.contains(String.valueOf(event.getBlock().getTypeId()))
				&& !list.contains(String.valueOf(event.getBlock().getType()
						.name()))
				&& !list.contains(String.valueOf(event.getBlock()
						.getTypeId()) + ":" + event.getBlock().getData())
				&& !list.contains(String.valueOf(event.getBlock().getType()
						.name())
						+ ":" + event.getBlock().getData())) {
			arena.msg(
					event.getPlayer(),
					Language.parse(arena, MSG.ERROR_WHITELIST_DISALLOWED,
							Language.parse(arena, MSG.GENERAL_BREAK)));
			// not on whitelist. DENY!
			event.setCancelled(true);
			DEBUG.i("whitelist out", event.getPlayer());
			return;
		}

		if (isProtected(event.getBlock().getLocation(), event,
				RegionProtection.BREAK)) {
			DEBUG.i("isprotected!", event.getPlayer());
			return;
		}
		list.clear();
		list.addAll(arena.getArenaConfig().getStringList(
				CFG.LISTS_BLACKLIST.getNode() + ".break",
				new ArrayList<String>()));

		if (list.contains(String.valueOf(event.getBlock().getTypeId()))
				|| list.contains(String.valueOf(event.getBlock().getType()
						.name()))
				|| list.contains(String.valueOf(event.getBlock().getTypeId())
						+ ":" + event.getBlock().getData())
				|| list.contains(String.valueOf(event.getBlock().getType()
						.name())
						+ ":" + event.getBlock().getData())) {
			arena.msg(
					event.getPlayer(),
					Language.parse(arena, MSG.ERROR_BLACKLIST_DISALLOWED,
							Language.parse(arena, MSG.GENERAL_BREAK)));
			// on blacklist. DENY!
			event.setCancelled(true);
			DEBUG.i("blacklist out", event.getPlayer());
			return;
		}

		DEBUG.i("onBlockBreak !!!", event.getPlayer());

		ArenaModuleManager.onBlockBreak(arena, event.getBlock());


		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
				new DamageResetRunnable(arena, event.getPlayer(), null), 1L);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBurn(final BlockBurnEvent event) {
		final Arena arena = ArenaManager.getArenaByRegionLocation(
				new PABlockLocation(event.getBlock().getLocation()));
		if (arena == null) {
			return; // no arena => out
		}
		if (isProtected(event.getBlock().getLocation(), event,
				RegionProtection.FIRE)) {
			return;
		}

		ArenaModuleManager.onBlockBreak(arena, event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockDecay(final LeavesDecayEvent event) {
		final Block block = event.getBlock();
		final Arena arena = ArenaManager.getArenaByRegionLocation(
				new PABlockLocation(block.getLocation()));

		if (arena == null) {
			return;
		}

		arena.getDebugger().i("block block decaying inside the arena");

		if (isProtected(event.getBlock().getLocation(), event,
				RegionProtection.NATURE)) {
			return;
		}

		ArenaModuleManager.onBlockBreak(arena, event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockFade(final BlockFadeEvent event) {
		if (willBeSkipped(event, event.getBlock().getLocation(),
				RegionProtection.NATURE)) {
			return;
		}

		final Block block = event.getBlock();
		final Arena arena = ArenaManager.getArenaByRegionLocation(
				new PABlockLocation(block.getLocation()));

		if (arena == null) {
			return;
		}

		arena.getDebugger().i("block block fading inside the arena");
		if (isProtected(event.getBlock().getLocation(), event,
				RegionProtection.NATURE)) {
			return;
		}

		ArenaModuleManager.onBlockChange(arena, event.getBlock(),
				event.getNewState());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockFromTo(final BlockFromToEvent event) {
		if (willBeSkipped(event, event.getToBlock().getLocation(),
				RegionProtection.NATURE)) {
			return;
		}

		final Block block = event.getToBlock();
		final Arena arena = ArenaManager.getArenaByRegionLocation(
				new PABlockLocation(block.getLocation()));

		if (arena == null) {
			return;
		}

		// arena.getDebugger().info("block fluids inside the arena");

		if (isProtected(block.getLocation(), event, RegionProtection.NATURE)) {
			return;
		}

		ArenaModuleManager.onBlockBreak(arena, event.getBlock());

		ArenaModuleManager.onBlockPlace(arena, block, Material.AIR);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockForm(final BlockFormEvent event) {
		if (willBeSkipped(event, event.getBlock().getLocation(),
				RegionProtection.NATURE)) {
			return;
		}

		final Block block = event.getBlock();
		final Arena arena = ArenaManager.getArenaByRegionLocation(
				new PABlockLocation(block.getLocation()));
		if (arena == null) {
			return;
		}

		if (isProtected(event.getBlock().getLocation(), event,
				RegionProtection.NATURE)) {
			return;
		}

		ArenaModuleManager.onBlockChange(arena, event.getBlock(),
				event.getNewState());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockGrow(final StructureGrowEvent event) {
		Arena arena = null;

		for (BlockState block : event.getBlocks()) {
			arena = ArenaManager.getArenaByRegionLocation(
					new PABlockLocation(block.getLocation()));
			if (arena != null) {
				break;
			}
		}

		if (arena == null) {
			return; // no arena => out
		}
		for (BlockState block : event.getBlocks()) {
			arena = ArenaManager.getArenaByProtectedRegionLocation(
					new PABlockLocation(block.getLocation()),
					RegionProtection.NATURE);
			if (arena != null) {
				continue;
			}
			if (isProtected(block.getLocation(), event, RegionProtection.NATURE)) {
				return;
			}

			ArenaModuleManager.onBlockChange(arena, block.getBlock(), block);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockIgnite(final BlockIgniteEvent event) {
		if (willBeSkipped(event, event.getBlock().getLocation(),
				RegionProtection.FIRE)) {
			return;
		}
		final Arena arena = ArenaManager.getArenaByRegionLocation(
				new PABlockLocation(event.getBlock().getLocation()));
		if (arena == null) {
			return;
		}
		
		if (arena.getArenaConfig().getBoolean(CFG.PROTECT_ENABLED)
				&& (isProtected(event.getBlock().getLocation(), event,
						RegionProtection.FIRE))) {
			return;
		}
		ArenaModuleManager.onBlockBreak(arena, event.getBlock());
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onExplosionPrime(final ExplosionPrimeEvent event) {
		
		if (willBeSkipped(event, event.getEntity().getLocation(),
				RegionProtection.TNT)) {
			return;
		}
		final Arena arena = ArenaManager.getArenaByRegionLocation(
				new PABlockLocation(event.getEntity().getLocation()));
		// all checks done in willBeSkipped
		ArenaModuleManager.onBlockBreak(arena, event.getEntity().getLocation().getBlock());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
		Arena arena = null;

		for (Block block : event.getBlocks()) {
			arena = ArenaManager.getArenaByRegionLocation(
					new PABlockLocation(block.getLocation()));
			if (arena != null) {
				if (isProtected(event.getBlock().getLocation(), event,
						RegionProtection.PISTON)) {
					return;
				}
				break;
			}
		}

		if (arena == null) {
			return; // no arena => out
		}
		arena.getDebugger().i("block piston extend inside the arena");
		for (Block block : event.getBlocks()) {

			ArenaModuleManager.onBlockPiston(arena, block);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(final BlockPlaceEvent event) {
		if (willBeSkipped(event, event.getBlock().getLocation(),
				RegionProtection.PLACE)) {
			return;
		}
		
		if(ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.LOST
				|| ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.WATCH
				|| ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.LOUNGE
				|| ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.READY) {
			event.setCancelled(true);
			return;
		}
		
		Arena arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event
				.getBlock().getLocation()));
		
		if (event.getBlock().getType().equals(Material.TNT) && arena.getArenaConfig().getBoolean(CFG.PLAYER_AUTOIGNITE)) {
			event.setCancelled(true);
			
			class RunLater implements Runnable {

				@Override
				public void run() {
					event.getPlayer().getInventory().remove(new ItemStack(Material.TNT, 1));
				}
				
			}
			
			Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
			
			event.getBlock().getLocation().getWorld().spawnEntity(
					event.getBlock().getRelative(BlockFace.UP).getLocation(), EntityType.PRIMED_TNT);
			return;
		}
		
		
		List<String> list = new ArrayList<String>();

		list = arena.getArenaConfig().getStringList(
				CFG.LISTS_WHITELIST.getNode() + ".place",
				new ArrayList<String>());

		if (!list.isEmpty()
				&& !list.contains(String.valueOf(event.getBlockPlaced()
					.getTypeId()))
				&& !list.contains(String.valueOf(event.getBlockPlaced()
						.getType().name()))
				&& !list.contains(String.valueOf(event.getBlockPlaced()
						.getTypeId()) + ":" + event.getBlock().getData())
				&& !list.contains(String.valueOf(event.getBlockPlaced()
						.getType().name())
						+ ":" + event.getBlock().getData())) {
			arena.msg(
					event.getPlayer(),
					Language.parse(arena, MSG.ERROR_WHITELIST_DISALLOWED,
							Language.parse(arena, MSG.GENERAL_PLACE)));
			// not on whitelist. DENY!
			event.setCancelled(true);
			return;
		}

		if (isProtected(event.getBlock().getLocation(), event,
				RegionProtection.PLACE)) {
			if (arena.isFightInProgress()
					&& !isProtected(event.getBlock().getLocation(), event,
							RegionProtection.TNT)
					&& event.getBlock().getTypeId() == 46) {

				ArenaModuleManager.onBlockPlace(arena, event.getBlock(), event
						.getBlockReplacedState().getType());
				event.setCancelled(false);
				return; // we do not block TNT, so just return if it is TNT
			}
			return;
		}

		list = arena.getArenaConfig().getStringList(
				CFG.LISTS_BLACKLIST.getNode() + ".place",
				new ArrayList<String>());

		if (list.contains(String.valueOf(event.getBlockPlaced().getTypeId()))
				|| list.contains(String.valueOf(event.getBlockPlaced()
						.getType().name()))
				|| list.contains(String.valueOf(event.getBlockPlaced()
						.getTypeId()) + ":" + event.getBlock().getData())
				|| list.contains(String.valueOf(event.getBlockPlaced()
						.getType().name())
						+ ":" + event.getBlock().getData())) {
			arena.msg(
					event.getPlayer(),
					Language.parse(arena, MSG.ERROR_BLACKLIST_DISALLOWED,
							Language.parse(arena, MSG.GENERAL_PLACE)));
			// on blacklist. DENY!
			event.setCancelled(true);
			return;
		}

		ArenaModuleManager.onBlockPlace(arena, event.getBlock(), event
				.getBlockReplacedState().getType());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(final HangingPlaceEvent event) {
		if (willBeSkipped(event, event.getBlock().getLocation(),
				RegionProtection.PAINTING)) {
			return;
		}

		final Arena arena = ArenaManager.getArenaByProtectedRegionLocation(
				new PABlockLocation(event.getBlock().getLocation()),
				RegionProtection.PAINTING);

		DEBUG.i("painting place", event.getPlayer());

		if (arena == null || isProtected(event.getBlock().getLocation(), event,
				RegionProtection.PAINTING)) {
			return;
		}

		ArenaModuleManager.onBlockPlace(arena, event.getBlock(), event
				.getBlock().getType());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(final HangingBreakEvent event) {
		if (willBeSkipped(event, event.getEntity().getLocation(),
				RegionProtection.PAINTING)) {
			return;
		}

		final Arena arena = ArenaManager.getArenaByProtectedRegionLocation(
				new PABlockLocation(event.getEntity().getLocation()),
				RegionProtection.PAINTING);

		if (isProtected(event.getEntity().getLocation(), event,
				RegionProtection.PAINTING)) {
			return;
		}

		arena.getDebugger().i("painting break inside the arena");
		ArenaModuleManager.onPaintingBreak(arena, event.getEntity(), event
				.getEntity().getType());
	}
}