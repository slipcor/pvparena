package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <pre>PVP Arena DISABLE Command class</pre>
 * <p/>
 * A command to disable an arena
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Disable extends AbstractArenaCommand {

    public PAA_Disable() {
        super(new String[]{"pvparena.cmds.disable"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{0})) {
            return;
        }

        final PAA_Stop cmd = new PAA_Stop();
        cmd.commit(arena, sender, new String[0]);

        arena.getArenaConfig().set(CFG.GENERAL_ENABLED, false);
        arena.getArenaConfig().save();
        arena.setLocked(true);

        for (String key : ArenaManager.getShortcutValues().keySet()) {
            if (arena.equals(ArenaManager.getShortcutValues().get(key))) {
                ArenaManager.advance(key);
                break;
            }
        }

        arena.msg(sender, Language.parse(arena, MSG.ARENA_DISABLE_DONE));
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.DISABLE));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("disable");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!dis", "!off");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        return new CommandTree<>(null);
    }
}
