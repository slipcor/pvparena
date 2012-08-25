package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

/**
 * <pre>Arena Runnable class "TimedEnd"</pre>
 * 
 * An arena timer to end the arena match after a certain amount of time
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class TimedEndRunnable extends ArenaRunnable {
	private Debug db = new Debug(42);
	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public TimedEndRunnable(Arena a, int i) {
		super("endingexact", i, null, a, false);
		a.END_ID = id;
		db.i("TimedEndRunnable constructor");
	}
	
	@Override
	protected void commit() {
		db.i("TimedEndRunnable commiting");
		if (arena.isFightInProgress())
			PVPArena.instance.getAgm().timed(arena);
		else {
			// deactivate the auto saving task
			Bukkit.getServer().getScheduler().cancelTask(id);
		}
		Bukkit.getScheduler().cancelTask(arena.END_ID);
		arena.END_ID = -1;
		Bukkit.getScheduler().cancelTask(arena.REALEND_ID);
		arena.REALEND_ID = -1;
	}
}
