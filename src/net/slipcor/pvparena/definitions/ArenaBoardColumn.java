package net.slipcor.pvparena.definitions;

import net.slipcor.pvparena.core.Debug;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

/**
 * arena board column class
 * 
 * @author slipcor
 * 
 * @version v0.6.15
 * 
 */

public class ArenaBoardColumn {
	protected ArenaBoard board;
	private Location location;
	private Debug db = new Debug(11);

	private ArenaBoardSign[] signs = new ArenaBoardSign[5];

	/**
	 * create an arena board column instance
	 * 
	 * @param ab
	 *            the arena board to hook to
	 * @param loc
	 *            the location of the column header
	 */
	public ArenaBoardColumn(ArenaBoard ab, Location loc) {
		board = ab;
		location = loc;

		db.i("fetching sign column");
		fetchSigns();
	}

	/**
	 * fetch sub signs and attach them to the sign array
	 */
	private void fetchSigns() {
		Location l = location.getBlock().getRelative(BlockFace.DOWN)
				.getLocation();
		int i = 0;
		try {
			do {
				Sign s = (Sign) l.getBlock().getState();
				s.setLine(0, "");
				s.setLine(1, "");
				s.setLine(2, "");
				s.setLine(3, "");
				s.update();
				signs[i] = (new ArenaBoardSign(this, l));
				l = l.getBlock().getRelative(BlockFace.DOWN).getLocation();
			} while (++i < 5);
		} catch (Exception e) {
			// no more signs, out!
		}
	}

	/**
	 * write a string array to the signs
	 * 
	 * @param s
	 *            the string array to save
	 */
	public void write(String[] s) {
		int i = 0;

		for (ArenaBoardSign abs : signs) {
			if (abs == null) {
				return;
			}
			int ii = 0;
			while (i < s.length && ii < 4) {
				abs.set(ii++, s[i++]);
			}
			abs.update();
		}
	}
}
