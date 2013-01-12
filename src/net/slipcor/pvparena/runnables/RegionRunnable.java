package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;

/**
 * <pre>
 * Arena Runnable class "Region"
 * </pre>
 * 
 * An arena timer to commit region specific checks
 * 
 * @author slipcor
 * 
 * @version v0.9.9
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
		if (!Debug.override) {
			db.i("RegionRunnable commiting");
		}
		/*
		 * J - is a join region I - is a fight in progress? T - should a region
		 * tick be run? --------------------------- JI - T 00 - 0 : no join
		 * region, no game, no tick 01 - 1 : no join region, game, tick for
		 * other region type 10 - 1 : join region! no game! tick so ppl can
		 * join! 11 - 0 : join region! game! no tick, ppl are done joining
		 */
		if (r.getType().equals(RegionType.JOIN) != r.getArena()
				.isFightInProgress()) {
			r.tick();
		} else if (!r.getType().equals(RegionType.JOIN)) {
			Bukkit.getScheduler().cancelTask(id);
		}
	}

	public void setId(int i) {
		id = i;
	}
}
