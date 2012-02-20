package net.slipcor.pvparena.definitions;

import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 * arena block class
 * 
 * -
 * 
 * defines a block to recreate on match end
 * 
 * @version v0.6.3
 * 
 * @author slipcor
 * 
 */
public class ArenaBlock {
	public String arena;
	private final Location location;
	public final Material material;
	private final byte data;
	private final String[] lines;

	/**
	 * create an arena block instance (blockdestroy)
	 * 
	 * @param block
	 *            the block to copy
	 */
	public ArenaBlock(Block block) {
		location = block.getLocation();
		material = block.getType();
		data = block.getData();
		try {
			arena = Arenas.getArenaByRegionLocation(location).name;
		} catch (Exception e) {
			arena = "";
		}
		if (block.getState() instanceof Sign) {
			lines = ((Sign) block.getState()).getLines();
		} else {
			lines = null;
		}
	}

	/**
	 * create an arena block instance (blockplace)
	 * 
	 * @param block
	 *            the block to copy
	 * @param type
	 *            the Material to override (the Material before placing)
	 */
	public ArenaBlock(Block block, Material type) {
		location = block.getLocation();
		try {
			arena = Arenas.getArenaByRegionLocation(location).name;
		} catch (Exception e) {
			arena = "";
		}
		material = type;
		data = block.getData();
		lines = null;
	}

	/**
	 * reset an arena block
	 */
	public void reset() {
		Block b = location.getBlock();
		b.setType(material);
		b.setData(data);
		if (lines != null) {
			int i = 0;
			for (String s : lines) {
				if (s != null) {
					((Sign) b.getState()).setLine(i, s);
				}
				i++;
			}
		}
	}
}
