package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

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
 * @version v0.8.7
 * 
 */

public class PlayerResetRunnable implements Runnable {
	private final ArenaPlayer p;
	private Debug db = new Debug(40);
	private int id;

	/**
	 * create a timed arena runnable
	 * 
	 * @param p
	 *            the player to reset
	 */
	public PlayerResetRunnable(ArenaPlayer p, int i) {
		id = 0;
		this.p = p;
		db.i("PlayerResetRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("PlayerResetRunnable commiting");
		p.getArena().playerLeave(p.get(), "lose");
		Bukkit.getScheduler().cancelTask(id);
	}
	
	public void setId(int i) {
		id = i;
	}
}