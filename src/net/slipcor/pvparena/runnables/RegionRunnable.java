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
	private final ArenaRegionShape region;
	private final static Debug DEBUG = new Debug(49);
	private int iID;

	/**
	 * create a region runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public RegionRunnable(final ArenaRegionShape paRegion) {
		this.region = paRegion;
		DEBUG.i("RegionRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		if (!Debug.override) {
			DEBUG.i("RegionRunnable commiting");
		}
		/*
		 * J - is a join region I - is a fight in progress? T - should a region
		 * tick be run? --------------------------- JI - T 00 - 0 : no join
		 * region, no game, no tick 01 - 1 : no join region, game, tick for
		 * other region type 10 - 1 : join region! no game! tick so ppl can
		 * join! 11 - 0 : join region! game! no tick, ppl are done joining
		 */
		if (region.getType().equals(RegionType.JOIN) == region.getArena()
				.isFightInProgress()) {
			Bukkit.getScheduler().cancelTask(iID);
		} else if (!region.getType().equals(RegionType.JOIN)) {
			region.tick();
		}
	}

	public void setId(final int runID) {
		iID = runID;
	}
}
