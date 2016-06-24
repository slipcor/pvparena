package net.slipcor.pvparena.core;

import net.slipcor.pvparena.PVPArena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Updater extends Thread {
    private final boolean files;

    private final UpdateMode mode;
    private final UpdateType type;

    private final Plugin plugin;
    private final File file;

    private final int major;
    private final int minor;

    private final List<UpdateInstance> instances = new ArrayList<>();

    protected enum UpdateMode {
        OFF, ANNOUNCE, DOWNLOAD, BOTH;

        public static UpdateMode getBySetting(final String setting) {
            final String lcSetting = setting.toLowerCase();
            if (lcSetting.contains("ann")) {
                return ANNOUNCE;
            }
            if (lcSetting.contains("down") || lcSetting.contains("load")) {
                return DOWNLOAD;
            }
            if ("both".equals(lcSetting)) {
                return BOTH;
            }
            return OFF;
        }
    }

    protected enum UpdateType {
        ALPHA, BETA, RELEASE;

        public static UpdateType getBySetting(final String setting) {
            if ("beta".equalsIgnoreCase(setting)) {
                return BETA;
            }
            if ("alpha".equalsIgnoreCase(setting)) {
                return ALPHA;
            }
            return RELEASE;
        }
    }

    public Updater(final Plugin plugin, final File file) {
        super();

        String version = Bukkit.getServer().getBukkitVersion();

        String[] chunks;
        try {
            chunks = version.split("-")[0].split("\\.");
        } catch (Exception e) {
            chunks = new String[]{"1","9"};
        }
        int a,b;
        try {
            a = Integer.parseInt(chunks[0]);
        } catch (Exception e) {
            a = 1;
        }
        major = a;
        try {
            b = Integer.parseInt(chunks[1]);
        } catch (Exception e) {
            b = 9;
        }
        minor = b;

        this.plugin = plugin;
        this.file = file;
        this.files = plugin.getConfig().getBoolean("update.modules");

        mode = UpdateMode.getBySetting(plugin.getConfig().getString("update.mode", "both"));

        if (mode == UpdateMode.OFF) {
            type = UpdateType.RELEASE;
        } else {
            instances.clear();
            type = UpdateType.getBySetting(plugin.getConfig().getString("update.type", "beta"));
            instances.add(new UpdateInstance("pvparena", false));
            if (files) {
                final File zipFolder = new File(plugin.getDataFolder(), "files");
                zipFolder.mkdir();
                if (type == UpdateType.RELEASE) {
                    instances.add(new UpdateInstance("pafiles", true));
                } else {
                    instances.add(new UpdateInstance("pa_goals", true));
                    instances.add(new UpdateInstance("pa_mods", true));
                }
            }
            start();
        }

        if (plugin.getConfig().getBoolean("update.modules", true)) {
            try {
                final File destination = plugin.getDataFolder();
                if (!destination.exists()) {
                    destination.mkdirs();

                }

                final File lib = new File(destination, "install.yml");

                plugin.getLogger().info("Downloading module update file...");
                final URL url = new URL(
                        "http://pa.slipcor.net/getYML.php?major="+major+"&minor="+minor);
                final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                final FileOutputStream output = new FileOutputStream(lib);
                output.getChannel().transferFrom(rbc, 0, 1 << 24);
                plugin.getLogger().info("Downloaded module update file");
                output.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    class UpdateInstance {


        private byte updateDigit;
        private String vOnline;
        private String vThis;
        private String pluginName;
        private String url;
        private final boolean zip;

        private boolean msg;
        private boolean outdated;

        UpdateInstance(String checkName, boolean isZip) {
            pluginName = checkName;
            zip = isZip;
        }

        /**
         * calculate the message variables based on the versions
         */
        private void calculateVersions() {
            final String[] aOnline = vOnline.split("\\.");
            final String[] aThis = vThis.split("\\.");
            outdated = false;


            for (int i = 0; i < aOnline.length && i < aThis.length; i++) {
                try {
                    final int iOnline = Integer.parseInt(aOnline[i]);
                    final int iThis = Integer.parseInt(aThis[i]);
                    if (iOnline == iThis) {
                        msg = false;
                        continue;
                    }
                    msg = true;
                    outdated = iOnline > iThis;
                    updateDigit = (byte) i;
                    message(Bukkit.getConsoleSender(), this);
                    return;
                } catch (final Exception e) {
                    calculateRadixString(aOnline[i], aThis[i], i);
                    return;
                }
            }
        }
        /**
         * calculate a version part based on letters
         *
         * @param sOnline the online letter(s)
         * @param sThis   the local letter(s)
         */
        private void calculateRadixString(final String sOnline, final String sThis,
                                          final int pos) {
            try {
                final int iOnline = Integer.parseInt(sOnline, 36);
                final int iThis = Integer.parseInt(sThis, 36);
                if (iOnline == iThis) {
                    msg = false;
                    return;
                }
                msg = true;
                outdated = iOnline > iThis;
                updateDigit = (byte) pos;
                message(Bukkit.getConsoleSender(), this);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * colorize a given string based on a char
         *
         * @param string the string to colorize
         * @return a colorized string
         */
        private String colorize(final String string) {
            final StringBuffer result;
            if (updateDigit == 0) {
                result = new StringBuffer(ChatColor.RED.toString());
            } else if (updateDigit == 1) {
                result = new StringBuffer(ChatColor.GOLD.toString());
            } else if (updateDigit == 2) {
                result = new StringBuffer(ChatColor.YELLOW.toString());
            } else if (updateDigit == 3) {
                result = new StringBuffer(ChatColor.BLUE.toString());
            } else {
                result = new StringBuffer(ChatColor.GREEN.toString());
            }
            result.append(string);
            result.append(ChatColor.WHITE);
            return result.toString();
        }

        public void runMe() {

            try {

                String version = "";

                URL website = new URL("http://pa.slipcor.net/versioncheck.php?plugin="+pluginName+"&type="+type.toString().toLowerCase()+"&major="+major+"&minor="+minor);
                URLConnection connection = website.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    version = inputLine;
                    break;
                }
                in.close();
                vOnline = version.replace("v", "");

                url = "https://www.spigotmc.org/resources/pvp-arena.16584/";

                website = new URL("http://pa.slipcor.net/versioncheck.php?plugin="+pluginName+"&link=true&type="+type.toString().toLowerCase()+"&major="+major+"&minor="+minor);
                connection = website.openConnection();
                in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));

                while ((inputLine = in.readLine()) != null) {
                    url = inputLine;
                    break;
                }
                in.close();

                vThis = plugin.getDescription().getVersion().replace("v", "");

                calculateVersions();

            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void message(final CommandSender player, UpdateInstance instance) {
        try {
            if (!instance.msg) {
                return;
            }

            if (instance.outdated) {
                if (!(player instanceof Player) && mode != UpdateMode.ANNOUNCE) {
                    // not only announce, download!
                    final File updateFolder = Bukkit.getServer().getUpdateFolderFile();
                    if (!updateFolder.exists()) {
                        updateFolder.mkdirs();
                    }
                    final File pluginFile = new File(updateFolder, file.getName());
                    if (pluginFile.exists() && !instance.zip) {
                        pluginFile.delete();
                    }

                    final File zipFile = new File(updateFolder, plugin.getName() + "_files_"+instance.pluginName+".zip");
                    if (zipFile.exists()) {
                        zipFile.delete();
                    }

                    if (instance.zip) {
                        downloadAndUnpack(instance.url, zipFile);
                    } else {
                        final URL url = new URL(instance.url);
                        final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                        final FileOutputStream output = new FileOutputStream(pluginFile);
                        output.getChannel().transferFrom(rbc, 0, 1 << 24);
                        output.close();
                    }

                }
                if (instance.zip) {
                    if (instances.size() > 2) {
                        player.sendMessage(instance.pluginName + " " + instance.colorize('v' + instance.vThis)
                                + ", an outdated version! Latest: " + ChatColor.COLOR_CHAR + 'a' + 'v' + instance.vOnline);
                    } else {
                        player.sendMessage("PVP Arena Files " + instance.colorize('v' + instance.vThis)
                                + ", an outdated version! Latest: " + ChatColor.COLOR_CHAR + 'a' + 'v' + instance.vOnline);
                    }
                } else {
                    player.sendMessage("You are using " + instance.colorize('v' + instance.vThis)
                            + ", an outdated version! Latest: " + ChatColor.COLOR_CHAR + 'a' + 'v' + instance.vOnline);
                    if (files) {
                        if (instances.size() > 2) {
                            player.sendMessage("The results for the files are as follows:");
                        }
                    }
                }

                if (mode == UpdateMode.ANNOUNCE) {
                    player.sendMessage(instance.url);
                } else {
                    if (!instance.zip) {
                        class RunLater implements Runnable {
                            @Override
                            public void run() {
                                player.sendMessage("The plugin has been updated, please restart the server!");
                            }
                        }
                        Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 60L);
                    }
                }
            } else {
                if (instance.zip) {
                    if (instances.size() > 2) {
                        player.sendMessage(instance.pluginName + " " + instance.colorize('v' + instance.vThis)
                                + ", an experimental version! Latest stable: " + ChatColor.COLOR_CHAR + 'a' + 'v'
                                + instance.vOnline);
                    } else {
                        player.sendMessage("PVP Arena Files " + instance.colorize('v' + instance.vThis)
                                + ", an experimental version! Latest stable: " + ChatColor.COLOR_CHAR + 'a' + 'v'
                                + instance.vOnline);
                    }
                } else {
                    player.sendMessage("You are using " + instance.colorize('v' + instance.vThis)
                            + ", an experimental version! Latest stable: " + ChatColor.COLOR_CHAR + 'a' + 'v'
                            + instance.vOnline);
                    if (files) {
                        if (instances.size() > 2) {
                            player.sendMessage("The results for the files are as follows:");
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * message a player if the version is different
     *
     * @param player the player to message
     */
    public void message(final CommandSender player) {
        class DownloadLater implements Runnable {

            @Override
            public void run() {
                for (final UpdateInstance instance : instances) {
                    message(player, instance);
                }
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new DownloadLater());
    }

    private void downloadAndUnpack(String zipURL, File zipFile) throws IOException {
        final URL url2 = new URL(zipURL);
        final ReadableByteChannel rbc2 = Channels.newChannel(url2.openStream());
        final FileOutputStream output2 = new FileOutputStream(zipFile);
        output2.getChannel().transferFrom(rbc2, 0, 1 << 24);
        output2.close();
        unzip(zipFile.getCanonicalPath());
    }

    @Override
    public void run() {
        if (mode == null || mode == UpdateMode.OFF) {
            System.out.print(Language.parse(Language.MSG.LOG_UPDATE_DISABLED));
            return;
        }

        System.out.print(Language.parse(Language.MSG.LOG_UPDATE_ENABLED));
        for (UpdateInstance instance : instances) {
            instance.runMe();
        }
    }

    /**
     * Part of Zip-File-Extractor, modified by Gravity for use with Updater.
     *
     * @param file the location of the file to extract.
     */
    private void unzip(final String file) {
        try {
            final File fSourceZip = new File(file);
            final String zipPath = file.substring(0, file.length() - 4);

            final ZipFile zipFile = new ZipFile(fSourceZip);

            final Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                final ZipEntry entry = e.nextElement();
                final File destinationFilePath = new File(zipPath, entry.getName());
                destinationFilePath.getParentFile().mkdirs();
                if (entry.isDirectory()) {
                } else {
                    final BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    int b;
                    final byte[] buffer = new byte[1024];
                    final FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    final BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
                    while ((b = bis.read(buffer, 0, 1024)) != -1) {
                        bos.write(buffer, 0, b);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                    final String name = destinationFilePath.getName();
                    if (name.endsWith(".jar")) {
                        destinationFilePath.renameTo(new File(plugin.getDataFolder().getParent(), plugin.getDataFolder() + File.separator + "files" + File.separator + name));
                    }
                }
            }
            zipFile.close();

            // Move any plugin data folders that were included to the right place, Bukkit won't do this for us.
            for (final File dFile : new File(zipPath).listFiles()) {
                if (dFile.isDirectory()) {
                    for (final File cFile : dFile.listFiles()) // Loop through all the files in the new dir
                    {
                        final File destFile = new File(
                                plugin.getDataFolder().getCanonicalPath() +
                                        File.separator + "files" +
                                        File.separator + cFile.getName());

                        destFile.delete();
                        cFile.renameTo(destFile);
                    }
                }
                dFile.delete();
            }
            new File(zipPath).delete();
            fSourceZip.delete();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        new File(file).delete();
    }
}
