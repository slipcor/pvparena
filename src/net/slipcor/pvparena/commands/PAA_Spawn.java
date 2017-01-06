package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.classes.PASpawn;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.managers.SpawnManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre>PVP Arena SPAWN Command class</pre>
 * <p/>
 * A command to set / remove arena spawns
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Spawn extends AbstractArenaCommand {
    private static final Set<String> spawns = new HashSet<>();

    static {
        spawns.add("exit");
        spawns.add("spectator");
    }

    public PAA_Spawn() {
        super(new String[]{"pvparena.cmds.spawn"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{1, 2, 5})) {
            return;
        }

        if (!(sender instanceof Player)) {
            Arena.pmsg(sender, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
            return;
        }

        if (args.length < 2) {
            // usage: /pa {arenaname} spawn [spawnname] | set a spawn

            final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sender.getName());

            if (spawns.contains(args[0])) {
                commitSet(arena, sender, new PALocation(aPlayer.get().getLocation()), args[0]);
                return;
            }

            for (final ArenaModule mod : arena.getMods()) {
                if (mod.hasSpawn(args[0])) {
                    commitSet(arena, sender, new PALocation(aPlayer.get().getLocation()), args[0]);
                    return;
                }
            }

            if (arena.getGoals().isEmpty()) {
                arena.msg(sender, Language.parse(arena, MSG.ERROR_NO_GOAL));
                return;
            }

            for (final ArenaGoal mod : arena.getGoals()) {
                if (mod.hasSpawn(args[0])) {
                    commitSet(arena, sender, new PALocation(aPlayer.get().getLocation()), args[0]);
                    return;
                }
            }

            arena.msg(sender, Language.parse(arena, MSG.ERROR_SPAWN_UNKNOWN, args[0]));

        } else if ("remove".equalsIgnoreCase(args[1])) {
            // usage: /pa {arenaname} spawn [spawnname] remove | remove a spawn
            final PALocation loc = SpawnManager.getSpawnByExactName(arena, args[0]);
            if (loc == null) {
                arena.msg(sender, Language.parse(arena, MSG.SPAWN_NOTSET, args[0]));
            } else {
                arena.msg(sender, Language.parse(arena, MSG.SPAWN_REMOVED, args[0]));
                arena.spawnUnset(args[0]);
            }
        } else if ("offset".equalsIgnoreCase(args[1]) && args.length>4) {
            // usage: /pa {arenaname} spawn [spawnname] offset X Y Z | offset a spawn
            final PALocation loc = SpawnManager.getSpawnByExactName(arena, args[0]);
            if (loc == null) {
                arena.msg(sender, Language.parse(arena, MSG.SPAWN_UNKNOWN, args[0]));
            } else {
                if (arena.getOffset(args[0]) != null) {
                    arena.removeOffset(args[0]);
                }
                double x,y,z;

                try {
                    x = Double.parseDouble(args[2]);
                } catch (Exception e) {
                    arena.msg(sender, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, args[2], "decimal"));
                    return;
                }

                try {
                    y = Double.parseDouble(args[3]);
                } catch (Exception e) {
                    arena.msg(sender, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, args[3], "decimal"));
                    return;
                }

                try {
                    z = Double.parseDouble(args[4]);
                } catch (Exception e) {
                    arena.msg(sender, Language.parse(arena, MSG.ERROR_ARGUMENT_TYPE, args[4], "decimal"));
                    return;
                }

                arena.setOffset(args[0], x, y, z);

                arena.msg(sender, Language.parse(arena, MSG.SPAWN_OFFSET, args[0],
                        String.format("%.1f", x)+", "+String.format("%.1f", y)+", "+String.format("%.1f", z)+" (x, y, z)"));
            }
        } else {
            displayHelp(sender);
        }
    }

    private void commitSet(final Arena arena, final CommandSender sender, final PALocation loc, final String name) {
        arena.spawnSet(name, loc);
        arena.msg(sender, Language.parse(arena, MSG.SPAWN_SET, name));
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.SPAWN));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("spawn");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!sp");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        for (final String spawn : spawns) {
            result.define(new String[]{spawn});
        }

        if (arena == null) {
            return result;
        }
        for (final PASpawn spawn : arena.getSpawns()) {
            result.define(new String[]{spawn.getName()});
        }
        return result;
    }
}
