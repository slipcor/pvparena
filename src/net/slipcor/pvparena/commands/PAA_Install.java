package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * <pre>
 * PVP Arena INSTALL Command class
 * </pre>
 * <p/>
 * A command to install modules
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Install extends AbstractGlobalCommand {

    public PAA_Install() {
        super(new String[]{"pvparena.cmd.install"});
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            return;
        }

        if (!argCountValid(sender, args, new Integer[]{0, 1})) {
            return;
        }

        // pa install
        // pa install ctf

        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(PVPArena.instance.getDataFolder().getPath()
                    + "/install.yml");
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }

        if (args.length == 0) {
            listVersions(sender, config, null);
            return;
        }

        if (config.get(args[0]) != null) {
            listVersions(sender, config, args[0]);
            return;
        }

        Set<String> list;

        list = config.getConfigurationSection("goals").getKeys(false);
        if (list.contains(args[0].toLowerCase())) {
            for (final String key : list) {
                if (key.equalsIgnoreCase(args[0])) {
                    if (download("pa_g_" + key + ".jar")) {
                        PVPArena.instance.getAgm().reload();
                        Arena.pmsg(sender,
                                Language.parse(MSG.INSTALL_DONE, key));
                        return;
                    }
                    Arena.pmsg(sender, Language.parse(MSG.ERROR_INSTALL, key));
                    return;
                }
            }
        }

        list = config.getConfigurationSection("mods").getKeys(false);
        if (list.contains(args[0].toLowerCase())) {
            for (final String key : list) {
                if (key.equalsIgnoreCase(args[0])) {
                    if (download("pa_m_" + key + ".jar")) {
                        PVPArena.instance.getAmm().reload();
                        Arena.pmsg(sender,
                                Language.parse(MSG.INSTALL_DONE, key));
                        return;
                    }
                    Arena.pmsg(sender, Language.parse(MSG.ERROR_INSTALL, key));
                    return;
                }
            }
        }
    }

    private void listVersions(final CommandSender sender, final YamlConfiguration cfg,
                              final String sub) {
        Arena.pmsg(sender, "--- PVP Arena Version Update information ---");
        Arena.pmsg(sender, "[" + ChatColor.COLOR_CHAR + "7uninstalled" + ChatColor.COLOR_CHAR + "r | " + ChatColor.COLOR_CHAR + "einstalled" + ChatColor.COLOR_CHAR + "r]");
        Arena.pmsg(sender, "[" + ChatColor.COLOR_CHAR + "coutdated" + ChatColor.COLOR_CHAR + "r | " + ChatColor.COLOR_CHAR + "alatest version" + ChatColor.COLOR_CHAR + "r]");
        if (sub == null || "goals".equalsIgnoreCase(sub)) {
            Arena.pmsg(sender, ChatColor.COLOR_CHAR + "c--- Arena Goals ----> /goals");
            final Set<String> entries = cfg.getConfigurationSection("goals").getKeys(
                    false);
            for (final String key : entries) {
                final String latest = cfg.getString("goals." + key);
                final ArenaGoal goal = PVPArena.instance.getAgm().getGoalByName(key);
                final boolean installed = goal != null;
                String version = null;
                if (installed) {
                    version = goal.version();
                }
                Arena.pmsg(sender, (installed ? ChatColor.COLOR_CHAR + "e" : ChatColor.COLOR_CHAR + "7")
                        + key
                        + ChatColor.COLOR_CHAR + "r - "
                        + (installed ? latest.equals(version) ? ChatColor.COLOR_CHAR + "a" : ChatColor.COLOR_CHAR + "c"
                        : "") + version + ChatColor.COLOR_CHAR + "f(" + latest + ")");
            }
        }
        if (sub == null || "mods".equalsIgnoreCase(sub)) {
            Arena.pmsg(sender, ChatColor.COLOR_CHAR + "a--- Arena Mods ----> /mods");
            final Set<String> entries = cfg.getConfigurationSection("mods").getKeys(
                    false);
            for (final String key : entries) {
                final String latest = cfg.getString("mods." + key);
                final ArenaModule mod = PVPArena.instance.getAmm().getModByName(key);
                final boolean installed = mod != null;
                String version = null;
                if (installed) {
                    version = mod.version();
                }
                Arena.pmsg(sender, (installed ? ChatColor.COLOR_CHAR + "e" : ChatColor.COLOR_CHAR + "7")
                        + key
                        + ChatColor.COLOR_CHAR + "r - "
                        + (installed ? latest.equals(version) ? ChatColor.COLOR_CHAR + "a" : ChatColor.COLOR_CHAR + "c"
                        : "") + version + ChatColor.COLOR_CHAR + "f(" + latest + ")");
            }

        }
    }

    private boolean download(final String file) {

        final File source = new File(PVPArena.instance.getDataFolder().getPath()
                + "/files/" + file);

        if (!source.exists()) {
            Arena.pmsg(
                    Bukkit.getConsoleSender(),
                    ChatColor.COLOR_CHAR + "cFile '" + ChatColor.COLOR_CHAR + "r"
                            + file
                            + ChatColor.COLOR_CHAR + "c' not found. Please extract the file to /files before trying to install!");
            return false;
        }

        String folder = null;
        if (file.startsWith("pa_g")) {
            folder = "/goals/";
        } else if (file.startsWith("pa_m")) {
            folder = "/mods/";
        }
        if (folder == null) {
            PVPArena.instance.getLogger()
                    .severe("unable to save file: " + file);
            return false;
        }
        try {
            final File destination = new File(PVPArena.instance.getDataFolder()
                    .getPath() + folder + "/" + file);
            final FileInputStream stream = new FileInputStream(source);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) > 0) {
                baos.write(buffer, 0, bytesRead);
            }

            final FileOutputStream fos = new FileOutputStream(destination);
            fos.write(baos.toByteArray());
            fos.close();

            PVPArena.instance.getLogger().info("Installed module " + file);
            stream.close();
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.INSTALL));
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("install");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!i");
    }

    @Override
    public CommandTree<String> getSubs(final Arena nothing) {
        final CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"mods"});
        result.define(new String[]{"goals"});
        for (final String string : PVPArena.instance.getAgm().getAllGoalNames()) {
            result.define(new String[]{string});
        }
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(PVPArena.instance.getDataFolder().getPath()
                    + "/install.yml");
        } catch (final Exception e) {
            e.printStackTrace();
            return result;
        }

        Set<String> list;

        list = config.getConfigurationSection("goals").getKeys(false);

        for (final String key : list) {
            result.define(new String[]{key});
        }

        list = config.getConfigurationSection("mods").getKeys(false);
        for (final String key : list) {
            result.define(new String[]{key});
        }
        return result;
    }
}
