package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;

/**
 * <pre>Arena Runnable class "PlayerDestroy"</pre>
 * 
 * An arena timer to reset a player
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class PlayerDestroyRunnable implements Runnable {
	private final ArenaPlayer player;
	private static final Debug DEBUG = new Debug(40);

	/**
	 * create a timed arena runnable
	 * 
	 * @param player
	 *            the player to reset
	 */
	public PlayerDestroyRunnable(final ArenaPlayer player) {
		this.player = player;
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this, 5L);
		DEBUG.i("PlayerDestroyRunnable constructor", player.getName());
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		DEBUG.i("PlayerDestroyRunnable commiting", player.getName());
		player.reset();
	}
}