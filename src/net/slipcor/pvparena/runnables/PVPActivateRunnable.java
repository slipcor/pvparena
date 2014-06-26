package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language.MSG;

/**
 * <pre>Arena Runnable class "PVPActivate"</pre>
 * <p/>
 * An arena timer to count down until pvp is enabled
 *
 * @author slipcor
 * @version v0.10.1
 */

public class PVPActivateRunnable extends ArenaRunnable {
//	private final static Debug DEBUG = new Debug(49);

    /**
     * create a pvp activates runnable
     */
    public PVPActivateRunnable(final Arena arena, final int seconds) {
        super(MSG.TIMER_PVPACTIVATING.getNode(), seconds, null, arena, false);
        arena.getDebugger().i("PVPActivateRunnable constructor");
    }

    @Override
    protected void commit() {
        arena.pvpRunner = null;
    }

    @Override
    protected void warn() {
        PVPArena.instance.getLogger().warning("PVPActivateRunnable not scheduled yet!");
    }
}