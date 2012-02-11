package net.slipcor.pvparena.definitions;

import java.util.HashMap;
import net.slipcor.pvparena.managers.Statistics;

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
	
	private Location location;
	private Arena arena;
	
	public Statistics.type sortBy = Statistics.type.KILLS;
	
	private HashMap<Statistics.type,ArenaBoardColumn> columns = new HashMap<Statistics.type,ArenaBoardColumn>();
	
	public ArenaBoard(Location loc, Arena a) {
		location = loc;
		arena = a;
		
		construct();
	}

	private void construct() {
		Location l = location;
		try {
			Sign s = (Sign) l.getBlock().getState();
			BlockFace bf = getRightDirection(s);
			
			do {
				Statistics.type t = null;
				try {
					t = Statistics.getTypeBySignLine(s.getLine(0));
				} catch(Exception e) {
					// nothing
				}
				
				columns.put(t, new ArenaBoardColumn(this, l));
				
				l = l.getBlock().getRelative(bf).getLocation();
			} while (true);
		} catch(Exception e) {
			//no more signs, out!
		}
	}
	
	private BlockFace getRightDirection(Sign s)
    {
        byte data = s.getRawData();

        if (data == 2) return BlockFace.NORTH;
        if (data == 3) return BlockFace.SOUTH;
        if (data == 4) return BlockFace.WEST;
        if (data == 5) return BlockFace.EAST;
        
        return null;
    }
	
	public void update() {
		for (Statistics.type t : Statistics.type.values()) {
			if (!columns.containsKey(t)) {
				continue;
			}
			String[] s = Statistics.read(Statistics.getStats(this.arena, t),t);
			columns.get(t).write(s);
		}
	}
}
