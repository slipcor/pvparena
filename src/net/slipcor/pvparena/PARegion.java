/*
 * region class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.3.11 - set regions for lounges, spectator, exit
 * 
 */

package net.slipcor.pvparena;

import net.slipcor.pvparena.managers.DebugManager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class PARegion {
	public String name;
	private DebugManager db = new DebugManager();
	private Location min;
	private Location max;
	
	public PARegion(String sName, Location lMin, Location lMax) {
		name = sName;
		min = lMin;
		max = lMax;
	}

	public boolean contains(Vector vec) {
		if (min == null || max == null)
			return false; // no arena, no container
		db.i("checking region "+name+": "+String.valueOf(vec.isInAABB(min.toVector(), max.toVector())));
		return vec.isInAABB(min.toVector(), max.toVector());
	}

	public Location getMin() {
		return min;
	}

	public Location getMax() {
		return max;
	}

	public World getWorld() {
		return min.getWorld();
	}
}
