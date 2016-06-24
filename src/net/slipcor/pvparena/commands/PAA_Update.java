package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.ncloader.NCBLoadable;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre>PVP Arena UPDATE Command class</pre>
 * <p/>
 * A command to update modules
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Update extends AbstractGlobalCommand {

    public PAA_Update() {
        super(new String[]{"pvparena.cmds.update"});
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!PVPArena.instance.getConfig().getBoolean("update.modules", true)) {
            Arena.pmsg(sender, ChatColor.DARK_RED+ Language.parse(Language.MSG.ERROR_MODULE_UPDATE));
            return;
        }

        final Set<NCBLoadable> modules = new HashSet<>();

        if (args.length < 1 || "mods".equals(args[0])) {
            modules.addAll(PVPArena.instance.getAmm().getAllMods());
        } else if (args.length < 1 || "goals".equals(args[0])) {
            modules.addAll(PVPArena.instance.getAgm().getAllGoals());
        } else if (args.length < 1 || "regionshapes".equals(args[0])) {
            modules.addAll(PVPArena.instance.getArsm().getRegions());
        }

        if (!modules.isEmpty()) {
            for (final NCBLoadable mod : modules) {
                if (mod.isInternal()) {
                    continue;
                }
                final PAA_Uninstall uninstall = new PAA_Uninstall();
                uninstall.commit(sender, new String[]{mod.getName()});
                final PAA_Install install = new PAA_Install();
                install.commit(sender, new String[]{mod.getName()});
            }
            return;
        }

        final PAA_Uninstall uninstall = new PAA_Uninstall();
        uninstall.commit(sender, args);
        final PAA_Install install = new PAA_Install();
        install.commit(sender, args);
    }


    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.UPDATE));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("update");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!u");
    }

    @Override
    public CommandTree<String> getSubs(final Arena nothing) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"mods"});
        result.define(new String[]{"goals"});
        for (final String string : PVPArena.instance.getAgm().getAllGoalNames()) {
            result.define(new String[]{string});
        }
        for (final ArenaModule mod : PVPArena.instance.getAmm().getAllMods()) {
            result.define(new String[]{mod.getName()});
        }
        return result;
    }
}
