package net.slipcor.pvparena.definitions;

import org.bukkit.Location;
import org.bukkit.block.Sign;

/**
 * arena board sign class
 * 
 * @author slipcor
 * 
 * @version v0.6.2
 * 
 */

public class ArenaBoardSign {
	protected ArenaBoardColumn column;
	private Location location;
	
	public ArenaBoardSign(ArenaBoardColumn abc, Location loc) {
		column = abc;
		location = loc;
	}

	public void set(int i, String string) {
		((Sign) location.getBlock().getState()).setLine(i,string);
	}

	public void update() {
		((Sign) location.getBlock().getState()).update();
	}
}
