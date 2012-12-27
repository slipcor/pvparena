package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language.MSG;

/**
 * <pre>Arena Runnable class "PVPActivate"</pre>
 * 
 * An arena timer to count down until pvp is enabled
 * 
 * @author slipcor
 * 
 * @version v0.10.1
 */

public class PVPActivateRunnable extends ArenaRunnable {
	private Debug db = new Debug(49);
	
	/**
	 * create a pvp activate runnable
	 */
	public PVPActivateRunnable(Arena a, int i) {
		super(MSG.TIMER_PVPACTIVATING.getNode(), i, null, a, false);
		db.i("PVPActivateRunnable constructor");
	}
	
	@Override
	protected void commit() {
		arena.PVP_ID = null;
	}
	
	@Override
	protected void warn() {
		PVPArena.instance.getLogger().warning("PVPActivateRunnable not scheduled yet!");
	}
}