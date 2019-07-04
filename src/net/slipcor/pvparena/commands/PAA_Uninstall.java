package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <pre>PVP Arena UNINSTALL Command class</pre>
 * <p/>
 * A command to uninstall modules
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Uninstall extends AbstractGlobalCommand {

    public PAA_Uninstall() {
        super(new String[]{"pvparena.cmds.uninstall"});
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            return;
        }

        if (!argCountValid(sender, args,
                new Integer[]{0, 1})) {
            return;
        }

        // pa install
        // pa install ctf

        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(PVPArena.instance.getDataFolder().getPath() + "/install.yml");
        } catch (final Exception e) {
        }

        if (args.length == 0 || config.get(args[0]) != null) {
            PAA_Install.listInstalled(sender);
            return;
        }

        final String name = args[0].toLowerCase();
        final ArenaModule mod = PVPArena.instance.getAmm().getModByName(name);
        if (mod != null) {
            if (remove("pa_m_" + mod.getName().toLowerCase() + ".jar")) {
                PVPArena.instance.getAmm().reload();
                Arena.pmsg(sender, Language.parse(MSG.UNINSTALL_DONE, mod.getName()));
                return;
            }
            Arena.pmsg(sender, Language.parse(MSG.ERROR_UNINSTALL, mod.getName()));
            FileConfiguration cfg = PVPArena.instance.getConfig();
            List<String> toDelete = cfg.getStringList("todelete");
            if (toDelete == null){
                toDelete = new ArrayList<>();
            }
            toDelete.add("pa_m_" + mod.getName().toLowerCase() + ".jar");
            cfg.set("todelete", toDelete);
            PVPArena.instance.saveConfig();
            Arena.pmsg(sender, Language.parse(MSG.ERROR_UNINSTALL2));
        }
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    public static boolean remove(final String file) {
        String folder = null;
        if (file.startsWith("pa_g")) {
            folder = "/goals/";
        } else if (file.startsWith("pa_m")) {
            folder = "/mods/";
        }
        if (folder == null) {
            PVPArena.instance.getLogger().severe("unable to fetch file: " + file);
            return false;
        }
        final File destination = new File(PVPArena.instance.getDataFolder().getPath()
                + folder);

        final File destFile = new File(destination, file);

        boolean exists = destFile.exists();
        boolean deleted = false;
        if (exists) {
            deleted = destFile.delete();
            if (!deleted) {
                PVPArena.instance.getLogger().severe("could not delete file: " + file);
            }
        } else {
            PVPArena.instance.getLogger().warning("file does not exist: " + file);
        }

        return exists && deleted;
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.UNINSTALL));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("uninstall");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!ui");
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
