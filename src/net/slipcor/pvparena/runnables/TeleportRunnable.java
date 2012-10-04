package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.managers.SpawnManager;

/**
 * <pre>Arena Runnable class "Teleport"</pre>
 * 
 * An arena timer to teleport a player back
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class TeleportRunnable implements Runnable {
	final String string;
	final String player;
	final Arena arena;
	
	public TeleportRunnable(Arena a, ArenaPlayer ap, String s) {
		string = s;
		player = ap.getName();
		arena = a;
	}

	@Override
	public void run() {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		if (ap == null || ap.get() == null) {
			PVPArena.instance.getLogger().warning("ArenaPlayer NULL: " + player);
			return;
		}
		if (string.equalsIgnoreCase("old")) {
			if (ap.getLocation() != null) {
				PALocation loc = ap.getLocation();
				System.out.print("=>" + loc.toString());
				ap.get().teleport(loc.toLocation());
			}
		} else {
			PALocation l = SpawnManager.getCoords(arena, string);
			ap.get().teleport(l.toLocation());
		}
	}

}