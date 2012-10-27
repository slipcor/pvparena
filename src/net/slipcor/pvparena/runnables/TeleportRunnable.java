package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.SpawnManager;

/**
 * <pre>Arena Runnable class "Teleport"</pre>
 * 
 * An arena timer to teleport a player back
 * 
 * @author slipcor
 * 
 * @version v0.9.4
 */

public class TeleportRunnable implements Runnable {
	final String string;
	final String player;
	final Arena arena;
	
	Debug db = new Debug(77);
	
	public TeleportRunnable(Arena a, ArenaPlayer ap, String s) {
		db.i("TeleportRunnable: " + a.getName() + " | " + ap.getName() + " => " + s);
		string = s;
		player = ap.getName();
		arena = a;
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this, 10L);
	}

	@Override
	public void run() {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		if (ap == null || ap.get() == null) {
			db.i("ArenaPlayer NULL: " + player);
			return;
		}
		if (string.equalsIgnoreCase("old")) {
			db.i("tping to old");
			if (ap.getLocation() != null) {
				db.i("location is fine");
				PALocation loc = ap.getLocation();
				ap.get().teleport(loc.toLocation());
				ap.get().setNoDamageTicks(60);
			}
		} else {
			PALocation l = SpawnManager.getCoords(arena, string);
			ap.get().teleport(l.toLocation());
			ap.get().setNoDamageTicks(60);
		}
	}

}
