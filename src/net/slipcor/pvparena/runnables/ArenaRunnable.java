package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <pre>Arena Runnable class</pre>
 * <p/>
 * The interface for arena timers
 *
 * @author slipcor
 * @version v0.10.0.1
 */

public abstract class ArenaRunnable extends BukkitRunnable {

    protected static final Map<Integer, String> MESSAGES = new HashMap<>();
    final String sSeconds = Language.parse(MSG.TIME_SECONDS);
    final String sMinutes = Language.parse(MSG.TIME_MINUTES);

    protected final String message;
    protected Integer seconds;
    protected final String sPlayer;
    protected final Arena arena;
    protected final Boolean global;

    /**
     * Spam the message of the remaining time to... someone, probably:
     *
     * @param message the Language.parse("**") String to wrap
     * @param arena   the arena to spam to (!global) or to exclude (global)
     * @param player  the player to spam to (!global && !arena) or to exclude (global && arena)
     * @param seconds the seconds remaining
     * @param global  the trigger to generally spam to everyone or to specific arenas/players
     */
    protected ArenaRunnable(final String message, final Integer seconds, final Player player, final Arena arena, final Boolean global) {
        super();
        this.message = message;
        this.seconds = ArenaModuleManager.parseStartCountDown(seconds, message, arena, global);
        sPlayer = player == null ? null : player.getName();
        this.arena = arena;
        this.global = global;

        ConfigurationSection section = null;

        if (arena == null) {
            if (Language.getConfig() != null) {
                section = Language.getConfig().getConfigurationSection("time_intervals");
            }
        } else {
            section = arena.getArenaConfig().getYamlConfiguration().getConfigurationSection("time_intervals");
        }
        if (section == null) {
            PVPArena.instance.getLogger().warning("Language strings 'time_intervals' not found, loading defaults!");
            MESSAGES.put(1, "1..");
            MESSAGES.put(2, "2..");
            MESSAGES.put(3, "3..");
            MESSAGES.put(4, "4..");
            MESSAGES.put(5, "5..");
            MESSAGES.put(10, "10 " + sSeconds);
            MESSAGES.put(20, "20 " + sSeconds);
            MESSAGES.put(30, "30 " + sSeconds);
            MESSAGES.put(60, "60 " + sSeconds);
            MESSAGES.put(120, "2 " + sMinutes);
            MESSAGES.put(180, "3 " + sMinutes);
            MESSAGES.put(240, "4 " + sMinutes);
            MESSAGES.put(300, "5 " + sMinutes);
            MESSAGES.put(600, "10 " + sMinutes);
            MESSAGES.put(1200, "20 " + sMinutes);
            MESSAGES.put(1800, "30 " + sMinutes);
            MESSAGES.put(2400, "40 " + sMinutes);
            MESSAGES.put(3000, "50 " + sMinutes);
            MESSAGES.put(3600, "60 " + sMinutes);
        } else {
            for (String key : section.getKeys(true)) {
                String content = section.getString(key);
                try {
                    Integer value = Integer.parseInt(key);
                    MESSAGES.put(value, content.replace("%m", sMinutes).replace("%s", sSeconds));
                } catch (Exception e) {

                }
            }
        }

        runTaskTimer(PVPArena.instance, 20L, 20L);
    }

    protected void spam() {
        if ((message == null) || (MESSAGES.get(seconds) == null)) {
            return;
        }
        final MSG msg = MSG.getByNode(message);
        if (msg == null) {
            PVPArena.instance.getLogger().warning("MSG not found: " + message);
            return;
        }
        final String message = seconds > 5 ? Language.parse(arena, msg, MESSAGES.get(seconds)) : MESSAGES.get(seconds);
        if (global) {
            final Collection<? extends Player> players = Bukkit.getOnlinePlayers();

            for (final Player p : players) {
                try {
                    if (arena != null && arena.hasPlayer(p)) {
                        continue;
                    }
                    if (p.getName().equals(sPlayer)) {
                        continue;
                    }
                    Arena.pmsg(p, message);
                } catch (final Exception e) {
                }
            }

            return;
        }
        if (arena != null) {
            final Set<ArenaPlayer> players = arena.getFighters();
            for (final ArenaPlayer ap : players) {
                if (ap.getName().equals(sPlayer)) {
                    continue;
                }
                if (ap.get() != null) {
                    if (!ArenaModuleManager.checkCountOverride(arena, ap.get(), message)) {
                        arena.msg(ap.get(), message);
                    }
                }
            }
            return;
        }

        if (Bukkit.getPlayer(sPlayer) != null) {
            final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sPlayer);
            if (aPlayer.getArena() == null) {
                Arena.pmsg(Bukkit.getPlayer(sPlayer), message);
            } else {
                if (!ArenaModuleManager.checkCountOverride(aPlayer.getArena(), aPlayer.get(), message)) {
                    aPlayer.getArena().msg(aPlayer.get(), message);
                }
            }
        }
    }

    @Override
    public void run() {
        spam();
        if (seconds <= 0) {
            commit();
            try {
                cancel();
            } catch (final IllegalStateException e) {
                warn();
            }
        }
        seconds--;
    }

    protected abstract void warn();

    protected abstract void commit();
}
