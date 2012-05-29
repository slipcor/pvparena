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
 * @version v0.8.4
 * 
 */

public class EndRunnable implements Runnable {
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
	public EndRunnable(Arena a, int i) {
		this.a = a;
		count = i;
		db.i("EndRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		TimerInfo.spam("resetexact", --count, null, a, false);
		if (count <= 0) {
			commit();
			Bukkit.getScheduler().cancelTask(a.REALEND_ID);
			a.REALEND_ID = -1;
		}
	}
	
	private void commit() {
		db.i("EndRunnable commiting");
		a.reset(false);
	}
}
