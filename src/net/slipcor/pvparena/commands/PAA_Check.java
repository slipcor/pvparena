package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * <pre>PVP Arena CHECK Command class</pre>
 * <p/>
 * A command to check an arena config
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Check extends AbstractArenaCommand {

    public PAA_Check() {
        super(new String[]{"pvparena.cmds.check"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{0})) {
            return;
        }

        boolean hasError = false;

        for (final CFG c : CFG.getValues()) {
            if (c == null || c.getNode() == null) {
                continue;
            }
            try {
                if ("string".equals(c.getType())) {
                    final String value = arena.getArenaConfig().getString(c);
                    arena.msg(sender, "correct " + c.getType() + ": " + value);
                } else if ("boolean".equals(c.getType())) {
                    final boolean value = arena.getArenaConfig().getBoolean(c);
                    arena.msg(sender, "correct " + c.getType() + ": " + value);
                } else if ("int".equals(c.getType())) {
                    final int value = arena.getArenaConfig().getInt(c);
                    arena.msg(sender, "correct " + c.getType() + ": " + value);
                } else if ("double".equals(c.getType())) {
                    final double value = arena.getArenaConfig().getDouble(c);
                    arena.msg(sender, "correct " + c.getType() + ": " + value);
                }
            } catch (final Exception e) {
                arena.msg(sender, Language.parse(arena, MSG.ERROR_ERROR, c.getNode()));
                hasError = true;
            }
        }

        if (!hasError) {
            arena.msg(sender, Language.parse(arena, MSG.CHECK_DONE));
        }
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.CHECK));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("check");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!ch");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        return new CommandTree<>(null);
    }
}
