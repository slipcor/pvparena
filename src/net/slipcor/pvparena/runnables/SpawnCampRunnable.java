package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;

/**
 * <pre>Arena Runnable class "SpawnCamp"</pre>
 * 
 * An arena timer to punish spawn campers
 * 
 * @author slipcor
 * 
 * @version v0.9.8
 */

public class SpawnCampRunnable implements Runnable {
	private final Arena a;
	private Debug db = new Debug(44);
	private int id;

	/**
	 * create a spawn camp runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public SpawnCampRunnable(Arena a, int i) {
		id = 0;
		this.a = a;
		db.i("SpawnCampRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("SpawnCampRunnable commiting");
		if (a.isFightInProgress() && a.getArenaConfig().getBoolean(CFG.PROTECT_PUNISH))
			a.spawnCampPunish();
		else {
			// deactivate the auto saving task
			Bukkit.getServer().getScheduler().cancelTask(id);
			a.SPAWNCAMP_ID = -1;
		}
	}
	
	public void setId(int i) {
		id = i;
	}
}
