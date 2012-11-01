package net.slipcor.pvparena.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.YamlConfiguration;
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
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * block listener class
 * 
 * -
 * 
 * PVP Arena Block Listener
 * 
 * @author slipcor
 * 
 * @version v0.8.9
 * 
 */

public class BlockListener implements Listener {
	private Debug db = new Debug(18);
	private static HashSet<String> chunks = new HashSet<String>();

	private boolean willBeSkipped(boolean cancelled, Event event, Location loc) {
		Arena arena = Arenas.getArenaByRegionLocation(loc);
		if (arena == null) {
			return true;
		}
		if (cancelled) {
			db.i("already cancelled: " + event.getEventName());
			return true;
		}
		return arena.edit;
	}

	private boolean isProtected(Arena arena, Cancellable event, String node) {
		/*
		if (!arena.fightInProgress) {
			db.i("not fighting. cancelling!");
			event.setCancelled(true);
			return true;
		}
		*/
		if (arena.cfg.getBoolean("protection.enabled")
				&& arena.cfg.getBoolean("protection." + node)) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation())) {
			return;
		}

		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());

		if (isProtected(arena, event, "blockdamage")) {
			return;
		}

		List<String> list = new ArrayList<String>();

		list = arena.cfg.getStringList("blocks.whitelist", list);

		if (list.size() > 0) {
			// WHITELIST!!!!!!!!!

			if (!list.contains(String.valueOf(event.getBlock().getTypeId()))) {
				event.getPlayer().sendMessage("not contained, out!");
				// not on whitelist. DENY!
				event.setCancelled(true);
				return;
			}
		} else {

			list = arena.cfg.getStringList("blocks.blacklist", list);

			if (list.contains(String.valueOf(event.getBlock().getTypeId()))) {
				event.getPlayer().sendMessage("blacklist contains");
				// on blacklist. DENY!
				event.setCancelled(true);
				return;
			}

		}
		PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
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

		if (isProtected(arena, event, "firespread")) {
			return;
		}

		PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockDecay(LeavesDecayEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation())) {
			return;
		}

		Block block = event.getBlock();
		Arena arena = Arenas.getArenaByRegionLocation(block.getLocation());

		db.i("block block decaying inside the arena");

		if (isProtected(arena, event, "decay")) {
			return;
		}

		PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockFade(BlockFadeEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation())) {
			return;
		}

		Block block = event.getBlock();
		Arena arena = Arenas.getArenaByRegionLocation(block.getLocation());

		db.i("block block fading inside the arena");
		if (isProtected(arena, event, "fade")) {
			return;
		}
		PVPArena.instance.getAmm().onBlockChange(arena, event.getBlock(),
				event.getNewState());
	}

	public void onBlockFromTo(BlockFromToEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getToBlock()
				.getLocation())) {
			return;
		}

		Block block = event.getToBlock();
		Arena arena = Arenas.getArenaByRegionLocation(block.getLocation());

		db.i("block fluids inside the arena");

		if (isProtected(arena, event, "fluids")) {
			return;
		}

		PVPArena.instance.getAmm().onBlockBreak(arena, event.getBlock());
		PVPArena.instance.getAmm().onBlockPlace(arena, block, Material.AIR);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockForm(BlockFormEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation())) {
			return;
		}

		Block block = event.getBlock();
		Arena arena = Arenas.getArenaByRegionLocation(block.getLocation());

		db.i("block block forming inside the arena");

		if (isProtected(arena, event, "form")) {
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
			arena = Arenas.getArenaByRegionLocation(block.getLocation());
			if (arena != null) {
				break;
			}
		}

		if (arena == null)
			return; // no arena => out

		for (BlockState block : event.getBlocks()) {
			arena = Arenas.getArenaByRegionLocation(block.getLocation());
			if (arena != null) {
				continue;
			}
			if (isProtected(arena, event, "grow")) {
				return;
			}
			PVPArena.instance.getAmm().onBlockChange(arena, block.getBlock(),
					block);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getBlock()
				.getLocation())) {
			return;
		}
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());

		db.i("block ignite inside the arena");
		event.setCancelled(!arena.fightInProgress);
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
				if (isProtected(arena, event, "piston")) {
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
				.getLocation())) {
			return;
		}
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());

		if (isProtected(arena, event, "blockplace")) {
			if (arena.fightInProgress &&
					!arena.cfg.getBoolean("protection.tnt", true) &&
					event.getBlock().getTypeId() == 46) {
				PVPArena.instance.getAmm().onBlockPlace(arena,
						event.getBlock(),
						event.getBlockReplacedState().getType());
				event.setCancelled(false);
				return; // we do not block TNT, so just return if it is TNT
			}
			return;
		}


		List<String> list = new ArrayList<String>();

		list = arena.cfg.getStringList("blocks.whitelist", list);

		if (list.size() > 0) {
			// WHITELIST!!!!!!!!!

			if (!list.contains(String.valueOf(event.getBlockPlaced().getTypeId()))) {
				event.getPlayer().sendMessage("not contained, out!");
				// not on whitelist. DENY!
				event.setCancelled(true);
				return;
			}
		} else {

			list = arena.cfg.getStringList("blocks.blacklist", list);

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
				.getLocation())) {
			return;
		}

		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());

		db.i("painting place inside the arena");

		if (isProtected(arena, event, "painting")) {
			return;
		}
		
		PVPArena.instance.getAmm().onBlockPlace(arena, event.getBlock(),
				event.getBlock().getType());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(PaintingBreakEvent event) {
		if (willBeSkipped(event.isCancelled(), event, event.getPainting()
				.getLocation())) {
			return;
		}

		Arena arena = Arenas.getArenaByRegionLocation(event.getPainting()
				.getLocation());

		if (isProtected(arena, event, "painting")) {
			return;
		}
		
		db.i("painting break inside the arena");
		PVPArena.instance.getAmm().onPaintingBreak(arena,
				event.getPainting(), event.getPainting().getType());
	}
	
	@EventHandler()
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (event.isCancelled()) {
			return;
		}
		event.setCancelled(chunks.contains(toString(event.getChunk())));
	}

	private static String toString(Chunk chunk) {
		return chunk.getWorld().getName() + ":" + chunk.getX() + "/" + chunk.getZ();
	}

	@EventHandler()
	public void onSignChange(SignChangeEvent event) {
		PVPArena.instance.getAmm().onSignChange(event);
	}

	public static void keepChunks(World w, YamlConfiguration config) {
		if (config.getConfigurationSection("spawns") == null) {
			return;
		}
		Set<String> spawns = config.getConfigurationSection("spawns").getKeys(false);
		
		for (String node : spawns) {
			Location loc = Config.parseLocation(w, config.getString("spawns." + node));
			chunks.add(toString(loc.getChunk()));
			loc.getChunk().load();
		}
	}
}