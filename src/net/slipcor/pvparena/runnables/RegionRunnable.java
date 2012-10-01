package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaRegionShape;

/**
 * <pre>Arena Runnable class "Region"</pre>
 * 
 * An arena timer to commit region specific checks
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class RegionRunnable implements Runnable {
	private final ArenaRegionShape r;
	private Debug db = new Debug(49);
	private int id;

	/**
	 * create a region runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public RegionRunnable(ArenaRegionShape paRegion) {
		this.r = paRegion;
		db.i("RegionRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("RegionRunnable commiting");
		if (r.getArena().isFightInProgress()) {
			r.tick();
		} else {
			Bukkit.getScheduler().cancelTask(id);
		}
	}
	
	public void setId(int i) {
		id = i;
	}
}
