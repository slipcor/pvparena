package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * <pre>PVP Arena START Command class</pre>
 * <p/>
 * A command to start an arena
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Start extends AbstractArenaCommand {

    public PAA_Start() {
        super(new String[]{"pvparena.cmds.start"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{0})) {
            return;
        }

        arena.start(true);
        arena.msg(sender, Language.parse(arena, MSG.ARENA_START_DONE));
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.START));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("start");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!go");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        return new CommandTree<>(null);
    }
}
