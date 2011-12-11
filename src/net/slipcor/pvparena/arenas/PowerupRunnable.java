/*
 * custom runnable class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.3.14 - timed arena modes
 *     v0.3.6 - CTF Arena
 *     v0.3.5 - Powerups!!
 */

package net.slipcor.pvparena.arenas;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.managers.DebugManager;

public class PowerupRunnable implements Runnable {
	private final Arena a;
	private DebugManager db = new DebugManager();

	public PowerupRunnable(Arena a) {
		this.a = a;
		db.i("PowerupRunnable constructor");
	}

	public void run() {
		db.i("PowerupRunnable commiting spawn");
		if (a.fightInProgress)
			a.calcPowerupSpawn();
		else {
			// deactivate the auto saving task
			Bukkit.getServer().getScheduler().cancelTask(a.SPAWN_ID);
		}
	}
}
