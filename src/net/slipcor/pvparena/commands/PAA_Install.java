package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashSet;
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
        super(new String[]{"pvparena.cmds.install"});
    }

    private static final File FILES_DIR = new File(PVPArena.instance.getDataFolder(),"/files/");

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

        if (args.length == 0) {
            listInstalled(sender);
            return;
        }

        Set<String> modList = getModList();
        if (modList.size() == 0 || modList.contains(args[0].toLowerCase())) {
            String modName = args[0].toLowerCase();
            if (download("pa_m_" + modName + ".jar")) {
                PVPArena.instance.getAmm().reload();
                Arena.pmsg(sender, Language.parse(MSG.INSTALL_DONE, modName));
            } else {
                Arena.pmsg(sender, Language.parse(MSG.ERROR_INSTALL, modName));
            }
        }
    }

    public static Set<String> listInstalled(final CommandSender sender) {
        Arena.pmsg(sender, "--- PVP Arena Version Update information ---");
        Arena.pmsg(sender, "[" + ChatColor.GRAY + "uninstalled" + ChatColor.RESET + " | " + ChatColor.YELLOW + "installed" + ChatColor.RESET + "]");
        Arena.pmsg(sender, ChatColor.GREEN + "--- Installed Arena Mods ---->");
        Set<String> modList = new HashSet<>();

        for (final String modName : getModList()) {
            final ArenaModule mod = PVPArena.instance.getAmm().getModByName(modName);
            Arena.pmsg(sender, (mod != null ? ChatColor.YELLOW : ChatColor.GRAY) + modName + ChatColor.RESET);
        }
        return modList;
    }

    private static Set<String> getModList() {
        Set<String> modList = new HashSet<>();
        for (final File file : FILES_DIR.listFiles()) {
            final String fileName = file.getName();
            if (fileName.startsWith("pa_m_") && fileName.endsWith(".jar")) {
                String modName = fileName.substring(5, fileName.length() - 4);
                modList.add(modName);
            }
        }
        return modList;
    }

    private boolean download(final String file) {
        return download(file, false);
    }

    private boolean download(final String file, final boolean silent) {

        final File source = new File(FILES_DIR, file);

        if (!source.exists()) {
            if (!silent) {
                Arena.pmsg(
                        Bukkit.getConsoleSender(),
                        ChatColor.COLOR_CHAR + "cFile '" + ChatColor.COLOR_CHAR + 'r'
                                + file
                                + ChatColor.COLOR_CHAR + "c' not found. Please extract the file to /files before trying to install!");
            }
            return false;
        }

        String folder = "/mods/";

        try {
            final File destination = new File(PVPArena.instance.getDataFolder()
                    .getPath() + folder + '/' + file);
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
        return Collections.singletonList("install");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!i");
    }

    @Override
    public CommandTree<String> getSubs(final Arena nothing) {
        final CommandTree<String> result = new CommandTree<>(null);

        Set<String> modList = getModList();
        for (final String key : modList) {
            result.define(new String[]{key});
        }
        return result;
    }
}
