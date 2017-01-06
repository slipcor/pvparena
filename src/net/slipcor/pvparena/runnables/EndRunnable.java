package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>Arena Runnable class "End"</pre>
 * <p/>
 * An arena timer counting down to the end of an arena
 *
 * @author slipcor
 * @version v0.9.8
 */

public class EndRunnable extends ArenaRunnable {
//	private final static Debug DEBUG = new Debug(40);

    /**
     * create a timed arena runnable
     *
     * @param arena   the arena we are running in
     * @param seconds the seconds it will run
     */
    public EndRunnable(final Arena arena, final int seconds) {
        super(MSG.TIMER_RESETTING_IN.getNode(), seconds, null, arena, false);
        arena.getDebugger().i("EndRunnable constructor");
        if (arena.endRunner != null) {
            arena.endRunner.cancel();
            arena.endRunner = null;
        }
        if (arena.realEndRunner != null) {
            arena.realEndRunner.cancel();
        }
        arena.realEndRunner = this;
    }

    @Override
    protected void commit() {
        arena.getDebugger().i("EndRunnable commiting");

        arena.setRound(arena.getRound() + 1);

        if (arena.getRound() >= arena.getRoundCount()) {
            arena.getDebugger().i("rounds done!");

            arena.reset(false);
            if (arena.realEndRunner != null) {
                arena.realEndRunner = null;
            }
            if (arena.endRunner != null) {
                arena.endRunner.cancel();
                arena.endRunner = null;
            }
        } else {
            arena.getDebugger().i("Starting round #" + arena.getRound());

            if (arena.realEndRunner != null) {
                arena.realEndRunner = null;
            }
            if (arena.endRunner != null) {
                arena.endRunner.cancel();
                arena.endRunner = null;
            }

            Boolean check = PACheck.handleStart(arena, null, true);
            if (check == null || !check) {
                return;
            }

            for (final ArenaPlayer ap : arena.getFighters()) {
                arena.unKillPlayer(ap.get(), ap.get().getLastDamageCause().getCause(),
                        ap.get().getLastDamageCause().getEntity());

                final List<ItemStack> items = new ArrayList<>();

                for (final ItemStack is : ap.get().getInventory().getContents()) {
                    if (is == null) {
                        continue;
                    }
                    items.add(is.clone());
                }
                new InventoryRefillRunnable(arena, ap.get(), items);
            }
        }
    }

    @Override
    protected void warn() {
        PVPArena.instance.getLogger().warning("EndRunnable not scheduled yet!");
    }
}
