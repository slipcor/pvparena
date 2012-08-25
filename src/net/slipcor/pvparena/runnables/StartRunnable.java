package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

/**
 * <pre>Arena Runnable class "Start"</pre>
 * 
 * An arena timer to start the arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class StartRunnable extends ArenaRunnable {
	private Debug db = new Debug(43);

	/**
	 * create a timed arena start runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public StartRunnable(Arena a, int i) {
		super("startinginexact", i, null, a, false);
		db.i("StartRunnable constructor");
		a.START_ID = id;
	}

	@Override
	protected void commit() {
		Bukkit.getScheduler().cancelTask(arena.START_ID);
		db.i("StartRunnable commiting");
		Bukkit.getScheduler().cancelTask(id);
		arena.start();
	}
}
