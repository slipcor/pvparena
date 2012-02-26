package net.slipcor.pvparena.definitions;

import net.slipcor.pvparena.core.Debug;

import org.bukkit.Location;
import org.bukkit.block.Sign;

/**
 * arena board sign class
 * 
 * @author slipcor
 * 
 * @version v0.6.15
 * 
 */

public class ArenaBoardSign {
	protected ArenaBoardColumn column;
	private Location location;
	private Debug db = new Debug(12);

	/**
	 * create an arena board sign instance
	 * 
	 * @param abc
	 *            the arena board column to hook to
	 * @param loc
	 *            the location where the sign resides
	 */
	public ArenaBoardSign(ArenaBoardColumn abc, Location loc) {
		column = abc;
		location = loc;
	}

	/**
	 * set a line
	 * 
	 * @param i
	 *            the line to set
	 * @param string
	 *            the string to set
	 */
	public void set(int i, String string) {
		((Sign) location.getBlock().getState()).setLine(i, string);
	}

	/**
	 * update the sign
	 */
	public void update() {
		((Sign) location.getBlock().getState()).update();
	}
}
