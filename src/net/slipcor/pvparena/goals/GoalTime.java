package net.slipcor.pvparena.goals;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.runnables.TimedEndRunnable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.util.HashSet;
import java.util.Set;

/**
 * <pre>Arena Goal class "Time"</pre>
 * <p/>
 * Time is ticking ^^
 *
 * @author slipcor
 */

public class GoalTime extends ArenaGoal {

    private TimedEndRunnable ter;

    public GoalTime() {
        super("Time");
        debug = new Debug(106);
    }

    @Override
    public String version() {
        return PVPArena.instance.getDescription().getVersion();
    }

    @Override
    public boolean allowsJoinInBattle() {
        return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
    }

    void commitEnd() {
        if (ter != null) {
            ter.commit();
        }
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage("timer: " + StringParser.colorVar(arena.getArenaConfig().getInt(CFG.GOAL_TIME_END)));
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public void parseStart() {
        final int timed = arena.getArenaConfig().getInt(CFG.GOAL_TIME_END);
        if (timed > 0) {
            arena.getDebugger().i("arena timing!");
            // initiate autosave timer
            ter = new TimedEndRunnable(arena, timed, this);
        }
    }

    @Override
    public void reset(final boolean force) {
        if (ter != null) {
            ter.commit();
            ter = null;
        }
    }

    @Override
    public void unload(final Player player) {
        class RunLater implements Runnable {

            @Override
            public void run() {
                commitEnd();
            }

        }
        if (arena.getFighters().size() < 2) {
            try {
                Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
            } catch (IllegalPluginAccessException ex) {

            }
            return;
        }
        if (arena.isFreeForAll()) {
            return;
        }

        final Set<ArenaTeam> teams = new HashSet<>();

        for (final ArenaPlayer aPlayer : arena.getFighters()) {
            if (aPlayer.getStatus() == Status.FIGHT) {
                teams.add(aPlayer.getArenaTeam());
                if (teams.size() > 1) {
                    return;
                }
            }

        }
        if (teams.size() < 2) {
            try {
                Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
            } catch (IllegalPluginAccessException ex) {

            }
        }
    }
}
