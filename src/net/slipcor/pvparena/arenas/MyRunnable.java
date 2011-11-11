package net.slipcor.pvparena.arenas;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.DebugManager;

/*
 * custom runnable class
 * 
 * author: slipcor
 * 
 * version: v0.3.6 - CTF Arena
 * 
 * history:
 * 
 *     v0.3.5 - Powerups!!
 */

public class MyRunnable implements Runnable {
	private final Arena a;
	DebugManager db = new DebugManager();

	public MyRunnable(Arena a) {
		this.a = a;
		db.i("MyRunnable constructor");
	}

	public void run() {
		db.i("MyRunnable commiting spawn");
		if (a.fightInProgress)
			a.commitSpawn();
		else {
			Bukkit.getServer().getScheduler().cancelTask(a.SPAWN_ID); // deactivate the auto saving task
		}
	}
}
