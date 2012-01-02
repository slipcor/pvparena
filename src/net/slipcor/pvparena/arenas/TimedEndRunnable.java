package net.slipcor.pvparena.arenas;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.managers.DebugManager;

/**
 * timed arena runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to end the arena it is running in
 * 
 * @author slipcor
 * 
 * @version v0.4.0
 * 
 */

public class TimedEndRunnable implements Runnable {
	private final Arena a;
	private DebugManager db = new DebugManager();

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public TimedEndRunnable(Arena a) {
		this.a = a;
		db.i("TimedEndRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("TimedEndRunnable commiting spawn");
		if (a.fightInProgress)
			a.timedEnd();
		else {
			// deactivate the auto saving task
			Bukkit.getServer().getScheduler().cancelTask(a.END_ID);
		}
	}
}
