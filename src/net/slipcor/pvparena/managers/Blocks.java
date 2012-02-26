package net.slipcor.pvparena.managers;

import java.util.HashMap;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Arena;
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
 * @version v0.6.15
 * 
 */

public class Blocks {
	public static HashMap<Location, ArenaBlock> blocks = new HashMap<Location, ArenaBlock>();

	private Debug db = new Debug(24);
	/**
	 * get all blocks that have to be reset (arena wise)
	 * 
	 * @param arena
	 *            the arena to check
	 * @return a map of location=>block to reset
	 */
	private static HashMap<Location, ArenaBlock> getBlocks(Arena arena) {
		HashMap<Location, ArenaBlock> result = new HashMap<Location, ArenaBlock>();

		for (Location l : blocks.keySet()) {
			if (blocks.get(l).arena.equals(arena.name)
					|| blocks.get(l).arena.equals("")) {
				result.put(l, blocks.get(l));
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
		if (!blocks.containsKey(block.getLocation())) {
			blocks.put(block.getLocation(), new ArenaBlock(block, type));
		}
	}
}
