package net.slipcor.pvparena.managers;

import java.util.HashMap;

import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.ArenaBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Blocks {
	public static HashMap<Location, ArenaBlock> blocks = new HashMap<Location, ArenaBlock>();

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

	public static void resetBlocks(Arena arena) {
		HashMap<Location, ArenaBlock> removals = getBlocks(arena);
		for (Location l : removals.keySet()) {
			removals.get(l).reset();
			blocks.remove(l);
		}
	}

	public static void saveBlock(Block block) {
		if (!blocks.containsKey(block.getLocation())) {
			blocks.put(block.getLocation(), new ArenaBlock(block));
		}
	}

	public static void saveBlock(Block block, Material type) {
		if (!blocks.containsKey(block.getLocation())) {
			blocks.put(block.getLocation(), new ArenaBlock(block, type));
		}
	}
}
