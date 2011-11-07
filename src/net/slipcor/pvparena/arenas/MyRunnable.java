package net.slipcor.pvparena.arenas;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArenaPlugin;

/*
 * custom runnable class
 * 
 * author: slipcor
 * 
 * version: v0.3.5 - Powerups!!
 * 
 * history:
 * 
 *     v0.3.5 - Powerups!!
 */

public class MyRunnable implements Runnable {
	private final Arena a;

	public MyRunnable(Arena a) {
		this.a = a;
		PVPArenaPlugin.instance.log.info("MyRunnable constructor");
	}

	public void run() {
		PVPArenaPlugin.instance.log.info("MyRunnable commiting spawn");
		if (a.fightInProgress)
			a.commitSpawn();
		else {
			Bukkit.getServer().getScheduler().cancelTask(a.SPAWN_ID); // deactivate the auto saving task
		}
	}
}
