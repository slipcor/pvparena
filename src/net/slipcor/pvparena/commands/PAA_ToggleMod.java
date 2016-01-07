package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * <pre>PVP Arena ACTIVATE Command class</pre>
 * <p/>
 * A command to activate modules
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_ToggleMod extends AbstractArenaCommand {

    public PAA_ToggleMod() {
        super(new String[]{"pvparena.cmds.togglemod"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args,
                new Integer[]{1})) {
            return;
        }

        // pa [arenaname] togglemod [module]

        final String name = args[0].toLowerCase();
        final ArenaModule mod = PVPArena.instance.getAmm().getModByName(name);
        if (mod != null) {
            arena.msg(sender, Language.parse(arena, MSG.SET_DONE, mod.getName(), String.valueOf(mod.toggleEnabled(arena))));
            if (mod.isMissingBattleRegion(arena)) {
                arena.msg(sender, Language.parse(arena, MSG.TOGGLEMOD_NOTICE));
            }
            return;
        }
        arena.msg(sender, Language.parse(arena, MSG.ERROR_UNKNOWN_MODULE, args[0]));
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.TOGGLEMOD));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("togglemod");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!tm");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        for (final String string : PVPArena.instance.getAgm().getAllGoalNames()) {
            result.define(new String[]{string});
        }
        for (final ArenaModule mod : PVPArena.instance.getAmm().getAllMods()) {
            result.define(new String[]{mod.getName()});
        }
        return result;
    }
}
