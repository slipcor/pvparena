package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

/**
 * timed arena runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to end the arena it is running in
 * 
 * @author slipcor
 * 
 * @version v0.8.7
 * 
 */

public class TimedEndRunnable implements Runnable {
	private final Arena arena;
	private Debug db = new Debug(42);
	private int id;

	private int count = 0;
	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public TimedEndRunnable(Arena a, int i, int iid) {
		id = 0;
		this.arena = a;
		count = i+1;
		db.i("TimedEndRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		TimerInfo.spam("endingexact", --count, null, arena, false);
		if (count <= 0) {
			commit();
			Bukkit.getScheduler().cancelTask(arena.END_ID);
			arena.END_ID = -1;
			Bukkit.getScheduler().cancelTask(arena.REALEND_ID);
			arena.REALEND_ID = -1;
		}
	}
	
	private void commit() {
		db.i("TimedEndRunnable commiting");
		if (arena.isFightInProgress())
			PVPArena.instance.getAtm().timed(arena);
		else {
			// deactivate the auto saving task
			Bukkit.getServer().getScheduler().cancelTask(id);
		}
		this.hashCode();
	}
	
	public void setId(int i) {
		id = i;
	}
}
