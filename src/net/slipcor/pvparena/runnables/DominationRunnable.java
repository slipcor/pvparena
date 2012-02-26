package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.managers.Flags;

/**
 * domination runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to handle
 * 
 * @author slipcor
 * 
 * @version v0.6.15
 * 
 */

public class DominationRunnable implements Runnable {
	public final boolean take;
	public int ID = -1;
	private final Arena arena;
	private final Location loc;
	private final String team;
	private Debug db = new Debug(39);

	/**
	 * create a dominateion runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public DominationRunnable(Arena a, boolean b, Location l, String s) {
		arena = a;
		take = b;
		team = s;
		loc = l;
		db.i("Domination constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("DominationRunnable commiting");
		if (take) {
			// claim a flag for the team
			if (arena.paFlags.containsKey(loc)) {
				// flag claimed! add score!
				Flags.reduceLivesCheckEndAndCommit(arena, team);
			} else {
				// flag unclaimed! claim!
				arena.paFlags.put(loc, team);
				long interval = 20L * 5;
				DominationRunnable running = new DominationRunnable(arena,
						take, loc, team);
				running.ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
						PVPArena.instance, running, interval, interval);
				arena.paRuns.put(loc, running);
			}
		} else {
			// unclaim
			if (arena.paRuns.containsKey(loc)) {
				int run_id = arena.paRuns.get(loc).ID;
				Bukkit.getScheduler().cancelTask(run_id);
			}
		}
	}
}
