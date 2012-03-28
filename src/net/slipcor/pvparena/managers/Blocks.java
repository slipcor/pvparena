package net.slipcor.pvparena.managers;

import java.util.HashMap;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.ArenaBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * block manager class
 * 
 * -
 * 
 * manages the blocks to be reset after the match
 * 
 * @author slipcor
 * 
 * @version v0.7.0
 * 
 */

public class Blocks {
	public static HashMap<Location, ArenaBlock> blocks = new HashMap<Location, ArenaBlock>();

	private static Debug db = new Debug(24);

	/**
	 * get all blocks that have to be reset (arena wise)
	 * 
	 * @param arena
	 *            the arena to check
	 * @return a map of location=>block to reset
	 */
	private static HashMap<Location, ArenaBlock> getBlocks(Arena arena) {
		HashMap<Location, ArenaBlock> result = new HashMap<Location, ArenaBlock>();

		db.i("reading all arenablocks");
		for (Location l : blocks.keySet()) {
			if (blocks.get(l).arena.equals(arena.name)
					|| blocks.get(l).arena.equals("")) {
				result.put(l, blocks.get(l));
				db.i(" - " + l.toString());
			}
		}

		return result;
	}

	/**
	 * reset all blocks belonging to an arena
	 * 
	 * @param arena
	 *            the arena to reset
	 */
	public static void resetBlocks(Arena arena) {
		db.i("resetting blocks");
		HashMap<Location, ArenaBlock> removals = getBlocks(arena);
		for (Location l : removals.keySet()) {
			removals.get(l).reset();
			blocks.remove(l);
		}
	}

	/**
	 * save a block to be restored (block destroy)
	 * 
	 * @param block
	 *            the block to save
	 */
	public static void saveBlock(Block block) {
		db.i("save block at " + block.getLocation().toString());
		if (!blocks.containsKey(block.getLocation())) {
			blocks.put(block.getLocation(), new ArenaBlock(block));
		}
	}

	/**
	 * save a block to be restored (block place)
	 * 
	 * @param block
	 *            the block to save
	 * @param type
	 *            the material to override
	 */
	public static void saveBlock(Block block, Material type) {
		db.i("save block at " + block.getLocation().toString());
		db.i(" - type: " + type.toString());
		if (!blocks.containsKey(block.getLocation())) {
			blocks.put(block.getLocation(), new ArenaBlock(block, type));
		}
	}
}
