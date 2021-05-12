package net.slipcor.pvparena.modules;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * <pre>
 * Arena Module class "StandardLounge"
 * </pre>
 * <p/>
 * Enables joining to lounges instead of the battlefield
 *
 * @author slipcor
 */

public class StandardSpectate extends ArenaModule {

    public StandardSpectate() {
        super("StandardSpectate");
        debug = new Debug(301);
    }

    private static final int PRIORITY = 2;

    @Override
    public String version() {
        return PVPArena.instance.getDescription().getVersion();
    }

    @Override
    public String checkForMissingSpawns(final Set<String> list) {
        return list.contains("spectator") ? null : "spectator not set";
    }

    @Override
    public PACheck checkJoin(final CommandSender sender, final PACheck res, final boolean join) {
        if (join) {
            return res;
        }

        final ArenaPlayer arenaPlayer = ArenaPlayer.parsePlayer(sender.getName());
        if (arenaPlayer.getArena() != null) {
            res.setError(this, Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, arenaPlayer.getArena().getName()));
        }

        if (res.getPriority() < PRIORITY) {
            res.setPriority(this, PRIORITY);
        }
        return res;
    }

    @Override
    public void commitSpectate(final Player player) {
        // standard join --> lounge
        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
        aPlayer.setLocation(new PALocation(player.getLocation()));

        aPlayer.setArena(this.arena);
        aPlayer.setStatus(Status.WATCH);

        this.arena.tpPlayerToCoordNameForJoin(aPlayer, "spectator", true);
        this.arena.msg(player, Language.parse(this.arena, MSG.NOTICE_WELCOME_SPECTATOR));

        if (aPlayer.getState() == null) {

            final Arena arena = aPlayer.getArena();

            aPlayer.createState(player);
            ArenaPlayer.backupAndClearInventory(arena, player);
            aPlayer.dump();


            if (aPlayer.getArenaTeam() != null && aPlayer.getArenaClass() == null) {
                final String autoClass =
                        arena.getArenaConfig().getBoolean(CFG.USES_PLAYERCLASSES) ?
                                arena.getClass(player.getName()) != null ? player.getName() : arena.getArenaConfig().getString(CFG.READY_AUTOCLASS)
                                : arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);

                if (autoClass != null && !"none".equals(autoClass) && arena.getClass(autoClass) != null) {
                    arena.chooseClass(player, null, autoClass);
                }
                if (autoClass == null) {
                    arena.msg(player, Language.parse(arena, MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
                }
            }
        }
    }

    @Override
    public boolean hasSpawn(final String string) {
        return "spectator".equalsIgnoreCase(string);
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}