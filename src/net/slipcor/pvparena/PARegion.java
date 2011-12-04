package net.slipcor.pvparena;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/*
 * Region class
 * 
 * author: slipcor
 * 
 * version: v0.3.11 - set regions for lounges, spectator, exit
 * 
 * history:
 *
 *     v0.3.11 - set regions for lounges, spectator, exit
 * 
 */

public class PARegion {
	public String name;
	private Location min;
	private Location max;
	
	public PARegion(String sName, Location lMin, Location lMax) {
		name = sName;
		min = lMin;
		max = lMax;
	}

	public World getWorld() {
		return min.getWorld();
	}

	public Location getMin() {
		return min;
	}

	public Location getMax() {
		return max;
	}

	public boolean contains(Vector vec) {
		if (min == null || max == null)
			return false; // no arena, no container
		return vec.isInAABB(min.toVector(), max.toVector());
	}
}
