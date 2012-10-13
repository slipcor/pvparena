package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.commands.PAG_Spectate;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language.MSG;

/**
 * <pre>Arena Runnable class "Warmup"</pre>
 * 
 * An arena timer to count down a warming up player
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class ArenaWarmupRunnable extends ArenaRunnable {
	private final ArenaPlayer player;
	private final String teamName;
	private final boolean spectator;
	private Debug db = new Debug(40);
	
	/**
	 * create a timed arena runnable
	 * 
	 * @param p
	 *            the player to reset
	 */
	public ArenaWarmupRunnable(Arena a, ArenaPlayer p, String team, boolean spec, int i) {
		super(MSG.TIMER_WARMINGUP.getNode(), i, p.get(), a, false);
		db.i("ArenaWarmupRunnable constructor");
		player = p;
		teamName = team;
		spectator = spec;
	}
	
	@Override
	protected void commit() {
		db.i("ArenaWarmupRunnable commiting");
		player.setStatus(Status.WARM);
		if (spectator) {
			(new PAG_Spectate()).commit(arena, player.get(), null);
		} else if (teamName == null) {
			(new PAG_Join()).commit(arena, player.get(), null);
		} else {
			String[] args = new String[1];
			args[0] = teamName;
			(new PAG_Join()).commit(arena, player.get(), args);
		}
	}
}