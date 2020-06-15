package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.classes.PASpawn;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.managers.SpawnManager;
import org.apache.commons.lang.Validate;

import java.util.HashSet;
import java.util.Set;

public class RespawnRunnable implements Runnable {

    private final Arena arena;
    private final ArenaPlayer player;
    private final String coordName;

    public RespawnRunnable(final Arena arena, final ArenaPlayer player, final String coord) {
        Validate.notNull(arena, "Arena cannot be null!");
        arena.getDebugger().i("RespawnRunnable constructor to spawn " + coord, player.get());
        this.arena = arena;
        this.player = player;
        coordName = coord;
    }

    @Override
    public void run() {
        if (player.get() == null || player.getArenaTeam() == null) {
            PVPArena.instance.getLogger().warning("player null!");
            return;
        }
        arena.getDebugger().i("respawning " + player.getName() + " to " + coordName);

        final PALocation loc = SpawnManager.getSpawnByExactName(arena, coordName);

        if (loc == null) {
            final Set<PASpawn> spawns = new HashSet<>();
            if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
                final String arenaClass = player.getArenaClass().getName();
                spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, player.getArenaTeam().getName() + arenaClass + "spawn"));
            } else if (arena.isFreeForAll()) {
                if ("free".equals(player.getArenaTeam().getName())) {
                    spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, "spawn"));
                } else {
                    spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, player.getArenaTeam().getName()));
                }
            } else {
                spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, player.getArenaTeam().getName() + "spawn"));
            }

            int pos = spawns.size();

            for (final PASpawn spawn : spawns) {
                if (--pos < 0) {
                    this.arena.tpPlayerToCoordName(player, spawn.getName());
                    break;
                }
            }
        } else {
            this.arena.tpPlayerToCoordName(player, coordName);
        }
    }

}
