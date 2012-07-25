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

public class EndRunnable implements Runnable {
	private final Arena a;
	private Debug db = new Debug(40);
	private int id;

	private int count = 0;
	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 * @param i 
	 */
	public EndRunnable(Arena a, int i, int iid) {
		id = 0;
		this.a = a;
		count = i+1;
		db.i("EndRunnable constructor");
		Bukkit.getScheduler().cancelTask(a.END_ID);
		a.END_ID = -1;
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
			Bukkit.getScheduler().cancelTask(a.END_ID);
			a.END_ID = -1;
			Bukkit.getScheduler().cancelTask(id);
		}
	}
	
	private void commit() {
		db.i("EndRunnable commiting");
		a.reset(false);
	}
	
	public void setId(int i) {
		id = i;
	}
}
