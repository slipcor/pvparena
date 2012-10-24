package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language.MSG;

/**
 * <pre>Arena Runnable class "Start"</pre>
 * 
 * An arena timer to start the arena
 * 
 * @author slipcor
 * 
 * @version v0.9.3
 */

public class StartRunnable extends ArenaRunnable {
	private Debug db = new Debug(43);

	/**
	 * create a timed arena start runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public StartRunnable(Arena a, int i) {
		super(MSG.ARENA_STARTING_IN.getNode(), i, null, a, false);
		db.i("StartRunnable constructor: " + id);
		a.START_ID = id;
	}

	@Override
	protected void commit() {
		Bukkit.getScheduler().cancelTask(arena.START_ID);
		db.i("StartRunnable commiting");
		arena.start();
	}
	
	@Override
	protected void warn() {
		PVPArena.instance.getLogger().warning("StartRunnable not scheduled yet!");
	}
}
