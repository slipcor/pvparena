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
	private final Arena a;
	private Debug db = new Debug(40);

	private int count = 0;

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 * @param i
	 */
	public EndRunnable(Arena a, int i, int iid) {
		super("resetexact", i, null, a, false);
		this.a = a;
		count = i + 1;
		db.i("EndRunnable constructor");
		Bukkit.getScheduler().cancelTask(a.END_ID);
		a.END_ID = -1;
	}

	@Override
	protected void commit() {
		db.i("EndRunnable commiting");
		a.reset(false);
		Bukkit.getScheduler().cancelTask(a.REALEND_ID);
		a.REALEND_ID = -1;
		Bukkit.getScheduler().cancelTask(a.END_ID);
		a.END_ID = -1;
		Bukkit.getScheduler().cancelTask(id);
	}
}
