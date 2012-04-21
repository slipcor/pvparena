package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.Commands;

/**
 * player reset runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to warmup a player
 * 
 * @author slipcor
 * 
 * @version v0.7.13
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
			Commands.parseSpectate(arena, player.get());
		} else if (teamName == null) {
			Commands.parseJoin(arena, player.get());
		} else {
			Commands.parseJoinTeam(arena, player.get(), teamName);
		}
	}
}