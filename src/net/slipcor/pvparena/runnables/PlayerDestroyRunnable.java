package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;

/**
 * <pre>Arena Runnable class "PlayerDestroy"</pre>
 * 
 * An arena timer to reset a player
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class PlayerDestroyRunnable implements Runnable {
	private final ArenaPlayer p;
	private Debug db = new Debug(40);
	private int id;

	/**
	 * create a timed arena runnable
	 * 
	 * @param p
	 *            the player to reset
	 */
	public PlayerDestroyRunnable(ArenaPlayer p) {
		id = 0;
		this.p = p;
		db.i("PlayerDestroyRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("PlayerDestroyRunnable commiting");
		Bukkit.getScheduler().cancelTask(id);
		p.reset();
	}
	
	public void setId(int i) {
		id = i;
	}
}