package net.slipcor.pvparena.arenas;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.managers.DebugManager;

/*
 * timed arena runnable class
 * 
 * author: slipcor
 * 
 * version: v0.3.14 - timed arena modes
 * 
 * history:
 *
 *     v0.3.14 - timed arena modes
 */

public class TimedEndRunnable implements Runnable {
	private final Arena a;
	DebugManager db = new DebugManager();

	public TimedEndRunnable(Arena a) {
		this.a = a;
		db.i("TimedEndRunnable constructor");
	}

	public void run() {
		db.i("TimedEndRunnable commiting spawn");
		if (a.fightInProgress)
			a.timedEnd();
		else {
			Bukkit.getServer().getScheduler().cancelTask(a.END_ID); // deactivate the auto saving task
		}
	}
}
