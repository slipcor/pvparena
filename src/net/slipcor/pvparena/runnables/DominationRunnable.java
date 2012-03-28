package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Flags;
import net.slipcor.pvparena.managers.Players;

/**
 * domination runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to handle
 * 
 * @author slipcor
 * 
 * @version v0.7.0
 * 
 */

public class DominationRunnable implements Runnable {
	public final boolean take;
	public final Location loc;
	public int ID = -1;
	private final Arena arena;
	public final String team;
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
				Players.tellEveryone(
						arena,
						Language.parse("domscore", arena.getTeam(team).colorize()
								+ ChatColor.YELLOW));
			} else {
				// flag unclaimed! claim!
				arena.paFlags.put(loc, team);
				long interval = 20L * 5;
				
				Players.tellEveryone(
						arena,
						Language.parse("domclaiming", arena.getTeam(team).colorize()
								+ ChatColor.YELLOW));
				
				DominationRunnable running = new DominationRunnable(arena,
						take, loc, team);
				running.ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
						PVPArena.instance, running, interval, interval);
				arena.paRuns.put(loc, running);
			}
		} else {
			// unclaim
			if (arena.paRuns.containsKey(loc)) {
				Players.tellEveryone(
						arena,
						Language.parse("domunclaiming", arena.getTeam(arena.paRuns.get(loc).team).colorize()
								+ ChatColor.YELLOW));
				
				int run_id = arena.paRuns.get(loc).ID;
				Bukkit.getScheduler().cancelTask(run_id);
			}
		}
	}

	public boolean noOneThere(int checkDistance) {
		for (ArenaPlayer p : Players.getPlayers(arena)) {
			if (p.get().getLocation().distance(loc) < checkDistance) {
				return false;
			}
		}
		return true;
	}
}
