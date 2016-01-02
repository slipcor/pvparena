package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.commands.PAG_Spectate;
import net.slipcor.pvparena.core.Language.MSG;

/**
 * <pre>Arena Runnable class "Warmup"</pre>
 * <p/>
 * An arena timer to count down a warming up player
 *
 * @author slipcor
 * @version v0.10.2
 */

public class ArenaWarmupRunnable extends ArenaRunnable {
    private final ArenaPlayer player;
    private final String teamName;
    private final boolean spectator;
//	private final static Debug DEBUG = new Debug(40);

    private final Arena wArena;

    /**
     * create a timed arena runnable
     *
     * @param player the player to reset
     */
    public ArenaWarmupRunnable(final Arena arena, final ArenaPlayer player, final String team, final boolean spectator, final int seconds) {
        super(MSG.TIMER_WARMINGUP.getNode(), seconds, player.get(), null, false);
        arena.getDebugger().i("ArenaWarmupRunnable constructor", player.getName());
        this.player = player;
        teamName = team;
        this.spectator = spectator;
        wArena = arena;
    }

    @Override
    protected void commit() {
        wArena.getDebugger().i("ArenaWarmupRunnable commiting", player.getName());
        player.setStatus(Status.WARM);
        if (spectator) {
            wArena.hasNotPlayed(player);
            (new PAG_Spectate()).commit(wArena, player.get(), null);
        } else if (teamName == null) {
            wArena.hasNotPlayed(player);
            (new PAG_Join()).commit(wArena, player.get(), null);
        } else {
            wArena.hasNotPlayed(player);
            final String[] args = new String[1];
            args[0] = teamName;
            (new PAG_Join()).commit(wArena, player.get(), args);
        }
    }

    @Override
    protected void warn() {
        PVPArena.instance.getLogger().warning("ArenaWarmupRunnable not scheduled yet!");
    }
}