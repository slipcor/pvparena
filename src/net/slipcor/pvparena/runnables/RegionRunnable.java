package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.neworder.ArenaRegion;

/**
 * region runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to commit region specific things
 * 
 * @author slipcor
 * 
 * @version v0.8.7
 * 
 */

public class RegionRunnable implements Runnable {
	private final ArenaRegion r;
	private Debug db = new Debug(49);
	private int id;

	/**
	 * create a region runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public RegionRunnable(ArenaRegion r, int i) {
		id = 0;
		this.r = r;
		db.i("RegionRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("RegionRunnable commiting");
		if (r.arena.fightInProgress) {
			r.tick();
		} else {
			Bukkit.getScheduler().cancelTask(id);
		}
	}
	
	public void setId(int i) {
		id = i;
	}
}
