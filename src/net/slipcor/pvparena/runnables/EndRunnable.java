package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

/**
 * arena ending runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to end the arena it is running in
 * 
 * @author slipcor
 * 
 * @version v0.8.11
 * 
 */

public class EndRunnable extends ArenaRunnable {
	private Debug db = new Debug(40);

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 * @param i
	 */
	public EndRunnable(Arena a, int i) {
		super("resetexact", i, null, a, false);
		db.i("EndRunnable constructor");
		Bukkit.getScheduler().cancelTask(a.END_ID);
		a.END_ID = -1;
		arena.REALEND_ID = id;
	}

	@Override
	protected void commit() {
		db.i("EndRunnable commiting");
		arena.reset(false);
		Bukkit.getScheduler().cancelTask(arena.REALEND_ID);
		arena.REALEND_ID = -1;
		Bukkit.getScheduler().cancelTask(arena.END_ID);
		arena.END_ID = -1;
		Bukkit.getScheduler().cancelTask(id);
	}
}
