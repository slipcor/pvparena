package net.slipcor.pvparena.definitions;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * arena sign class
 * 
 * @author slipcor
 * 
 * @version v0.6.2
 * 
 */

public class ArenaSign {
	Location location;
	
	public ArenaSign(Location loc) {
		location = loc;
		this.clear();
	}

	void clear() {
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

	private void clearNext() {
		try {
			Sign s = (Sign) location.getBlock().getRelative(BlockFace.DOWN).getState();
			s.setLine(0, "");
			s.setLine(1, "");
			s.setLine(2, "");
			s.setLine(3, "");
			s.update();
		} catch (Exception e) {
			return;
		}
	}

	public static ArenaSign used(Location loc, HashSet<ArenaSign> paSigns) {
		for (ArenaSign sign : paSigns) {
			if (sign.location.equals(loc)) {
				return sign;
			}
		}
		return null;
	}

	public boolean add(Player player) {
		return setFreeLine(player.getName());
	}

	private boolean setFreeLine(String name) {
		try {
			Sign s = (Sign) location.getBlock().getState();
			for (int i=2;i<4;i++) {
				if (s.getLine(i) == null || s.getLine(i).equals("")) {
					s.setLine(i, name);
					s.update();
					return true;
				}
			}
			s = (Sign) location.getBlock().getRelative(BlockFace.DOWN).getState();
			for (int i=0;i<4;i++) {
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

	public static void remove(HashSet<ArenaSign> paSigns, Player player) {
		for (ArenaSign s : paSigns) {
			s.remove(player.getName());
		}
	}

	private void remove(String name) {
		try {
			Sign s = (Sign) location.getBlock().getState();
			for (int i=2;i<4;i++) {
				if (s.getLine(i) != null && s.getLine(i).equals(name)) {
					s.setLine(i, "");
				}
			}
			s.update();
			s = (Sign) location.getBlock().getRelative(BlockFace.DOWN).getState();
			for (int i=0;i<4;i++) {
				if (s.getLine(i) != null && s.getLine(i).equals(name)) {
					s.setLine(i, "");
				}
			}
			s.update();
		} catch (Exception e) {
			return;
		}
	}
}
