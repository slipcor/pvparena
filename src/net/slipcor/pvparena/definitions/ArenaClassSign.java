package net.slipcor.pvparena.definitions;

import java.util.HashSet;

import net.slipcor.pvparena.core.Debug;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * arena sign class
 * 
 * @author slipcor
 * 
 * @version v0.6.15
 * 
 */

public class ArenaClassSign {
	Location location;
	private Debug db = new Debug(13);

	/**
	 * create an arena class sign instance
	 * 
	 * @param loc
	 *            the location the sign resides
	 */
	public ArenaClassSign(Location loc) {
		location = loc;
		db.i("adding arena class sign: " + location.toString());
		this.clear();
	}

	/**
	 * add a player name to a sign
	 * 
	 * @param player
	 *            the player name to add
	 * @return true if successful, false otherwise
	 */
	public boolean add(Player player) {
		return setFreeLine(player.getName());
	}

	/**
	 * clear the sign contents
	 */
	public void clear() {
		try {
			Sign s = (Sign) location.getBlock().getState();
			s.setLine(2, "");
			s.setLine(3, "");
			s.update();
			this.clearNext();
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * clear the next sign
	 */
	private void clearNext() {
		try {
			Sign s = (Sign) location.getBlock().getRelative(BlockFace.DOWN)
					.getState();
			s.setLine(0, "");
			s.setLine(1, "");
			s.setLine(2, "");
			s.setLine(3, "");
			s.update();
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * remove a player from all signs he may be on
	 * 
	 * @param paSigns
	 *            the signs to check
	 * @param player
	 *            the player to remove
	 */
	public static void remove(HashSet<ArenaClassSign> paSigns, Player player) {
		for (ArenaClassSign s : paSigns) {
			s.remove(player.getName());
		}
	}

	/**
	 * remove a player name from a string
	 * 
	 * @param name
	 *            the name to remove
	 */
	private void remove(String name) {
		try {
			Sign s = (Sign) location.getBlock().getState();
			for (int i = 2; i < 4; i++) {
				if (s.getLine(i) != null && s.getLine(i).equals(name)) {
					s.setLine(i, "");
				}
			}
			s.update();
			s = (Sign) location.getBlock().getRelative(BlockFace.DOWN)
					.getState();
			for (int i = 0; i < 4; i++) {
				if (s.getLine(i) != null && s.getLine(i).equals(name)) {
					s.setLine(i, "");
				}
			}
			s.update();
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * add a player name to the first free line on a sign group
	 * 
	 * @param name
	 *            the name to set
	 * @return true if successful, false otherwise
	 */
	private boolean setFreeLine(String name) {
		try {
			Sign s = (Sign) location.getBlock().getState();
			for (int i = 2; i < 4; i++) {
				if (s.getLine(i) == null || s.getLine(i).equals("")) {
					s.setLine(i, name);
					s.update();
					return true;
				}
			}
			s = (Sign) location.getBlock().getRelative(BlockFace.DOWN)
					.getState();
			for (int i = 0; i < 4; i++) {
				if (s.getLine(i) == null || s.getLine(i).equals("")) {
					s.setLine(i, name);
					s.update();
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * check if a location already is reserved by a class sign
	 * 
	 * @param loc
	 *            the location to check
	 * @param paSigns
	 *            the set of signs to check against
	 * @return the sign instance if reserved, null otherwise
	 */
	public static ArenaClassSign used(Location loc,
			HashSet<ArenaClassSign> paSigns) {
		for (ArenaClassSign sign : paSigns) {
			if (sign.location.equals(loc)) {
				return sign;
			}
		}
		return null;
	}
}
