package net.slipcor.pvparena.definitions;

import java.util.HashMap;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.Statistics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

/**
 * arena board class
 * 
 * @author slipcor
 * 
 * @version v0.6.2
 * 
 */

public class ArenaBoard {

	public static final Debug db = new Debug();
	
	private Location location;
	public Arena arena;

	public Statistics.type sortBy = Statistics.type.KILLS;

	private HashMap<Statistics.type, ArenaBoardColumn> columns = new HashMap<Statistics.type, ArenaBoardColumn>();

	public ArenaBoard(Location loc, Arena a) {
		location = loc;
		arena = a;

		Bukkit.getLogger().info("constructing");
		construct();
	}

	private void construct() {
		Location l = location;
		int border = 10;
		try {
			Sign s = (Sign) l.getBlock().getState();
			BlockFace bf = getRightDirection(s);
			Bukkit.getLogger().info("parsing signs:");
			do {
				Statistics.type t = null;
				try {
					t = Statistics.getTypeBySignLine(s.getLine(0));
				} catch (Exception e) {
					// nothing
				}

				columns.put(t, new ArenaBoardColumn(this, l));
				Bukkit.getLogger().info("putting column");
				l = l.getBlock().getRelative(bf).getLocation();
				s = (Sign) l.getBlock().getState();
			} while (border-- > 0);
		} catch (Exception e) {
			// no more signs, out!
		}
	}

	private BlockFace getRightDirection(Sign s) {
		byte data = s.getRawData();

		if (data == 2)
			return BlockFace.NORTH;
		if (data == 3)
			return BlockFace.SOUTH;
		if (data == 4)
			return BlockFace.WEST;
		if (data == 5)
			return BlockFace.EAST;

		return null;
	}

	public void update() {
		db.i("ArenaBoard update()");
		for (Statistics.type t : Statistics.type.values()) {
			db.i("checking stat: "+t.name());
			if (!columns.containsKey(t)) {
				continue;
			}
			db.i("found! reading!");
			String[] s = Statistics.read(Statistics.getStats(this.arena, sortBy), t);
			columns.get(t).write(s);
		}
	}
}
