package net.slipcor.pvparena.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Updater extends Thread {

    private boolean msg;
    private boolean outdated;
    private final boolean files;

    private byte updateDigit = -1;

    private String vOnline;
    private String vThis;

    private String pluginURL;
    private String zipURL;

    private final UpdateMode mode;
    private final UpdateType type;

    private final Plugin plugin;
    private final File file;

    private final int id;

    private enum UpdateMode {
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

    private enum UpdateType {
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

        public static boolean matchType(final Updater updater, final String updateType) {
            switch (updater.type) {
                case ALPHA:
                    return true;
                case BETA:
                    return "beta".equalsIgnoreCase(updateType) || "release".equalsIgnoreCase(updateType);
                default:
                    return "release".equalsIgnoreCase(updateType);
            }
        }
    }

    public Updater(final Plugin plugin, final File file, final boolean files) {
        super();
        this.plugin = plugin;
        this.file = file;
        id = 41652;
        this.files = files;

        mode = UpdateMode.getBySetting(plugin.getConfig().getString("update.mode", "both"));
        if (mode == UpdateMode.OFF) {
            type = UpdateType.RELEASE;
        } else {
            type = UpdateType.getBySetting(plugin.getConfig().getString("update.type", "beta"));
        }

        if (files) {
            final File zipFolder = new File(plugin.getDataFolder(), "files");
            zipFolder.mkdir();

            init();
        }
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
                message(Bukkit.getConsoleSender());
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
            message(Bukkit.getConsoleSender());
        } catch (final Exception e) {
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

    /**
     * message a player if the version is different
     *
     * @param player the player to message
     */
    public boolean message(final CommandSender player) {
        if (!msg) {
            return false;
        }

        if (outdated) {
            if (!(player instanceof Player) && mode != UpdateMode.ANNOUNCE) {
                // not only announce, download!
                final File updateFolder = Bukkit.getServer().getUpdateFolderFile();
                if (!updateFolder.exists()) {
                    updateFolder.mkdirs();
                }
                final File pluginFile = new File(updateFolder, file.getName());
                if (pluginFile.exists()) {
                    pluginFile.delete();
                }

                final File zipFile = new File(updateFolder, plugin.getName() + "_files.zip");
                if (zipFile.exists()) {
                    zipFile.delete();
                }

                try {
                    final URL url = new URL(pluginURL);
                    final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                    final FileOutputStream output = new FileOutputStream(pluginFile);
                    output.getChannel().transferFrom(rbc, 0, 1 << 24);
                    output.close();

                    if (files) {

                        final URL url2 = new URL(zipURL);
                        final ReadableByteChannel rbc2 = Channels.newChannel(url2.openStream());
                        final FileOutputStream output2 = new FileOutputStream(zipFile);
                        output2.getChannel().transferFrom(rbc2, 0, 1 << 24);
                        output2.close();

                        unzip(zipFile.getCanonicalPath());
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
            player.sendMessage("You are using " + colorize('v' + vThis)
                    + ", an outdated version! Latest: " + ChatColor.COLOR_CHAR + 'a' + 'v' + vOnline);
            if (mode == UpdateMode.ANNOUNCE) {
                player.sendMessage(pluginURL);
            } else {
                player.sendMessage("The plugin has been updated, please restart the server!");
            }
        } else {
            player.sendMessage("You are using " + colorize('v' + vThis)
                    + ", an experimental version! Latest stable: " + ChatColor.COLOR_CHAR + 'a' + 'v'
                    + vOnline);
        }
        return true;
    }

    public void announce(final CommandSender sender) {
        if (!message(sender)) {
            sender.sendMessage("You are using the latest version!");
            return;
        }

        if (mode != UpdateMode.DOWNLOAD) {
            sender.sendMessage("New version available: " + pluginURL);
        }
    }

    final void init() {
        if (plugin.getConfig().getBoolean("update.modules", true)) {
            try {
                final File destination = plugin.getDataFolder();
                if (!destination.exists()) {
                    destination.mkdirs();

                }

                final File lib = new File(destination, "install.yml");

                plugin.getLogger().info("Downloading module update file...");
                final URL url = new URL(
                        "http://pa.slipcor.net/install.yml");
                final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                final FileOutputStream output = new FileOutputStream(lib);
                output.getChannel().transferFrom(rbc, 0, 1 << 24);
                plugin.getLogger().info("Downloaded module update file");
                output.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        if (mode == UpdateMode.OFF) {
            return;
        }
        start();
    }

    @Override
    public void run() {
        if (mode == null || mode == UpdateMode.OFF) {
            System.out.print("LOG_UPDATE_DISABLED");
            return;
        }
        System.out.print("LOG_UPDATE_ENABLED");
        try {
            final URLConnection connection = new URL("https://api.curseforge.com/servermods/files?projectIds=" + id).openConnection();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            final String response = bufferedReader.readLine();

            final JSONArray array = (JSONArray) JSONValue.parse(response);

            if (array.size() < 1) {
                System.out.print("No files found");
                return;
            }

            pluginURL = null;
            zipURL = null;
            final String serverVersion = Bukkit.getVersion().split("\\(")[1].replace(")", "").split(" ")[1];

            boolean foundFile = false;
            for (int i = array.size() - 1; i > 0; i--) {

                /**
                 * "downloadUrl":"http:\/\/servermods.cursecdn.com\/files\/634\/870\/pvparena.jar",
                 * "fileName":"pvparena.jar",
                 * "gameVersion":"CB 1.3.1-R2.0",
                 * "name":"PVP Arena v0.9.1.4",
                 * "releaseType":"beta"
                 *
                 * git-Bukkit-1.7.2-R0.3-25-g5fc3995-b3051jnks (MC: 1.7.8)
                 */

                final JSONObject value = (JSONObject) array.get(i);
                final String type = (String) value.get("releaseType");
                if (!UpdateType.matchType(this, type)) {
                    continue;
                }

                final String gameVersion = ((String) value.get("gameVersion")).split(" ")[1].split("-")[0];
                if (compareDeeply(serverVersion, gameVersion) < 0) {
                    continue; // outdated server
                }

                final String fileName = (String) value.get("fileName");

                if (!foundFile && fileName.endsWith(".jar") && pluginURL == null) {

                    String sOnlineVersion = (String) value.get("name");
                    final String sThisVersion = plugin.getDescription().getVersion();

                    if (sOnlineVersion.contains(" ")) {
                        final String[] split = sOnlineVersion.split(" ");
                        for (final String aSplit : split) {
                            if (aSplit.contains(".")) {
                                sOnlineVersion = aSplit;
                                break;
                            }
                        }
                    }

                    vOnline = sOnlineVersion.replace("v", "");
                    vThis = sThisVersion.replace("v", "");

                    final String min_version = "1.2.2.428";
                    if (compareDeeply(vThis, min_version) < 0) {
                        continue; // version before the updater. Do not update!
                    }

                    if (mode == UpdateMode.DOWNLOAD || mode == UpdateMode.BOTH) {
                        pluginURL = (String) value.get("downloadUrl");
                    }

                    foundFile = true;

                    if (zipURL != null) {
                        calculateVersions();
                        return; // found both, done
                    }
                } else if (fileName.endsWith(".zip")) {
                    zipURL = (String) value.get("downloadUrl");
                    if (pluginURL != null) {
                        calculateVersions();
                        return;
                    }
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private int compareDeeply(final String a, final String b) {

        final String[] sA = a.split("\\.");
        final String[] sB = b.split("\\.");

        for (int i = 0; i < 10; i++) {
            if (i == sA.length && i == sB.length) {
                return 0;
            }
            if (i == sA.length) {
                return 1;
            }
            if (i == sB.length) {
                return -1;
            }
            final int iA = Integer.parseInt(sA[i], 36);
            final int iB = Integer.parseInt(sB[i], 36);

            if (iA > iB) {
                return 1;
            }

            if (iB > iA) {
                return -1;
            }
        }
        return 0;
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
