package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import org.bukkit.Bukkit;

/**
 * <pre>Arena Runnable class "SpawnCamp"</pre>
 * <p/>
 * An arena timer to punish spawn campers
 *
 * @author slipcor
 * @version v0.9.8
 */

public class SpawnCampRunnable implements Runnable {
    private final Arena arena;
    //	private final static Debug DEBUG = new Debug(44);
    private int iID;

    /**
     * create a spawn camp runnable
     *
     * @param arena the arena we are running in
     */
    public SpawnCampRunnable(final Arena arena) {
        iID = 0;
        this.arena = arena;
        arena.getDebugger().i("SpawnCampRunnable constructor");
    }

    /**
     * the run method, commit arena end
     */
    @Override
    public void run() {
        arena.getDebugger().i("SpawnCampRunnable commiting");
        if (arena.isFightInProgress() && arena.getArenaConfig().getBoolean(CFG.PROTECT_PUNISH)) {
            arena.spawnCampPunish();
        } else {
            // deactivate the auto saving task
            Bukkit.getServer().getScheduler().cancelTask(iID);
            arena.spawnCampRunnerID = -1;
        }
    }

    public void setId(final int runID) {
        iID = runID;
    }
}
