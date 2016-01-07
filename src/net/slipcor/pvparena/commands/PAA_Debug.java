package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * <pre>PVP Arena DEBUG Command class</pre>
 * <p/>
 * A command to toggle debugging
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Debug extends AbstractGlobalCommand {

    public PAA_Debug() {
        super(new String[]{"pvparena.cmds.debug"});
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            return;
        }

        if (!argCountValid(sender, args, new Integer[]{0, 1})) {
            return;
        }

        if (args.length > 0) {
            PVPArena.instance.getConfig().set("debug", args[0]);
        }

        Debug.load(PVPArena.instance, sender);
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.DEBUG));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("debug");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!d");
    }

    @Override
    public CommandTree<String> getSubs(final Arena nothing) {
        final CommandTree<String> result = new CommandTree<>(null);
        for (final Arena arena : ArenaManager.getArenas()) {
            result.define(new String[]{arena.getName()});
        }
        result.define(new String[]{"all"});
        result.define(new String[]{"none"});
        result.define(new String[]{"off"});
        return result;
    }
}
