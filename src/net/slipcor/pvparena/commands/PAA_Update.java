package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.ncloader.NCBLoadable;
import org.bukkit.command.CommandSender;

import java.io.File;
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
        if (!hasPerms(sender)) {
            return;
        }

        final Set<NCBLoadable> modules = new HashSet<NCBLoadable>(PVPArena.instance.getAmm().getAllMods());

        if (!modules.isEmpty()) {
            for (final NCBLoadable mod : modules) {
                if (mod.isInternal()) {
                    continue;
                }

                final File destination = new File(PVPArena.instance.getDataFolder().getPath()
                        + "/files/");
                final File destFileM = new File(destination, "pa_m_" + mod.getName().toLowerCase() + ".jar");

                if (!destFileM.exists()) {
                    continue;
                }

                final PAA_Uninstall uninstall = new PAA_Uninstall();
                if (!uninstall.hasPerms(sender)) {
                    return;
                }
                uninstall.commit(sender, new String[]{mod.getName()});
                final PAA_Install install = new PAA_Install();
                if (!install.hasPerms(sender)) {
                    return;
                }
                install.commit(sender, new String[]{mod.getName()});
            }
        }
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
        for (final ArenaModule mod : PVPArena.instance.getAmm().getAllMods()) {
            result.define(new String[]{mod.getName()});
        }
        return result;
    }
}
