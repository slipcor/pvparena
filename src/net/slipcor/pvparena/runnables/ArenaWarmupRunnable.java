package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.command.PAAJoin;
import net.slipcor.pvparena.command.PAAJoinTeam;
import net.slipcor.pvparena.command.PAASpectate;
import net.slipcor.pvparena.core.Debug;

/**
 * player reset runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to warmup a player
 * 
 * @author slipcor
 * 
 * @version v0.7.19
 * 
 */

public class ArenaWarmupRunnable implements Runnable {
	private final ArenaPlayer player;
	private final String teamName;
	private final Arena arena;
	private final boolean spectator;
	private Debug db = new Debug(40);

	/**
	 * create a timed arena runnable
	 * 
	 * @param p
	 *            the player to reset
	 */
	public ArenaWarmupRunnable(Arena a, ArenaPlayer p, String team, boolean spec) {
		db.i("ArenaWarmupRunnable constructor");
		player = p;
		teamName = team;
		arena = a;
		spectator = spec;
	}

	/**
	 * the run method, warmup the arena player
	 */
	@Override
	public void run() {
		db.i("ArenaWarmupRunnable commiting");
		player.setStatus(Status.WARM);
		if (spectator) {
			(new PAASpectate()).commit(arena, player.get(), null);
		} else if (teamName == null) {
			(new PAAJoin()).commit(arena, player.get(), null);
		} else {
			String[] args = new String[1];
			args[0] = teamName;
			(new PAAJoinTeam()).commit(arena, player.get(), args);
		}
	}
}