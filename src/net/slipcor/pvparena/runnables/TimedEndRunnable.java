package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language.MSG;

/**
 * <pre>Arena Runnable class "TimedEnd"</pre>
 * 
 * An arena timer to end the arena match after a certain amount of time
 * 
 * @author slipcor
 * 
 * @version v0.9.8
 */

public class TimedEndRunnable extends ArenaRunnable {
	private Debug db = new Debug(42);
	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public TimedEndRunnable(Arena a, int i) {
		super(MSG.TIMER_ENDING_IN.getNode(), i, null, a, false);
		db.i("TimedEndRunnable constructor");
		a.endRunner = this;
	}
	
	@Override
	public void commit() {
		db.i("TimedEndRunnable commiting");
		if (arena.isFightInProgress()) {
			PVPArena.instance.getAgm().timedEnd(arena);
		}
		arena.endRunner = null;
		if (arena.realEndRunner != null) {
			arena.realEndRunner.cancel();
			arena.realEndRunner = null;
		}
	}
	
	@Override
	protected void warn() {
		PVPArena.instance.getLogger().warning("TimedEndRunnable not scheduled yet!");
	}
}
