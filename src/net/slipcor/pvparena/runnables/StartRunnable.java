package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

/**
 * arena start runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to start the arena it belongs to
 * 
 * @author slipcor
 * 
 * @version v0.8.7
 * 
 */

public class StartRunnable implements Runnable {
	private final Arena a;
	private Debug db = new Debug(43);
	private int id;

	private int count = 0;
	/**
	 * create a timed arena start runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public StartRunnable(Arena a, int i, int iid) {
		this.a = a;
		id = 0;
		count = i+1;
		db.i("StartRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		TimerInfo.spam("startinginexact", --count, null, a, false);
		if (count == 0) {
			Bukkit.getScheduler().cancelTask(a.START_ID);
			commit();
		} if (count < 0) {
			System.out.print("running");
		}
	}

	private void commit() {
		db.i("StartRunnable commiting");
		Bukkit.getScheduler().cancelTask(id);
		a.start();
	}
	
	public void setId(int i) {
		id = i;
	}
}
