package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAGoalEvent;
import net.slipcor.pvparena.goals.GoalTime;
import org.bukkit.Bukkit;

/**
 * <pre>Arena Runnable class "TimedEnd"</pre>
 * <p/>
 * An arena timer to end the arena match after a certain amount of time
 *
 * @author slipcor
 * @version v0.9.8
 */

public class TimedEndRunnable extends ArenaRunnable {
    //private static final Debug DEBUG = new Debug(42);
    private final GoalTime goal;

    /**
     * create a timed arena runnable
     *
     * @param arena    the arena we are running in
     * @param goalTime the ArenaGoal
     */
    public TimedEndRunnable(final Arena arena, final int seconds, final GoalTime goalTime) {
        super(MSG.TIMER_ENDING_IN.getNode(), seconds, null, arena, false);
        arena.getDebugger().i("TimedEndRunnable constructor");
        arena.endRunner = this;
        this.goal = goalTime;
    }

    @Override
    public void commit() {
        arena.getDebugger().i("TimedEndRunnable commiting");
        final PAGoalEvent gEvent = new PAGoalEvent(arena, goal, "");
        Bukkit.getPluginManager().callEvent(gEvent);
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
