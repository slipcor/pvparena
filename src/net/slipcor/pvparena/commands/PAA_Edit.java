package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>PVP Arena EDIT Command class</pre>
 * <p/>
 * A command to toggle an arena's edit mode
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Edit extends AbstractArenaCommand {

    public static Map<String, Arena> activeEdits = new HashMap<String, Arena>();

    public PAA_Edit() {
        super(new String[]{"pvparena.cmd.edit"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!this.hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{0})) {
            return;
        }

        String msg;

        if (PAA_Edit.activeEdits.containsValue(arena)) {
            activeEdits.remove(sender.getName());
            msg = Language.parse(arena, MSG.ARENA_EDIT_DISABLED, arena.getName());
        } else {
            if (arena.isFightInProgress()) {
                final PAA_Stop cmd = new PAA_Stop();
                cmd.commit(arena, sender, new String[0]);
            }
            activeEdits.put(sender.getName(), arena);
            msg = Language.parse(arena, MSG.ARENA_EDIT_ENABLED, arena.getName());
        }
        arena.msg(sender, msg);
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.EDIT));
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("edit");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!e");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        return new CommandTree<String>(null);
    }
}
