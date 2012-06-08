package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;

/**
 * player reset runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to reset a player
 * 
 * @author slipcor
 * 
 * @version v0.7.0
 * 
 */

public class PlayerResetRunnable implements Runnable {
	private final ArenaPlayer p;
	private Debug db = new Debug(40);

	/**
	 * create a timed arena runnable
	 * 
	 * @param p
	 *            the player to reset
	 */
	public PlayerResetRunnable(ArenaPlayer p) {
		this.p = p;
		db.i("PlayerResetRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("PlayerResetRunnable commiting");
		//p.reset();
		p.getArena().playerLeave(p.get(), "lose");
	}
}