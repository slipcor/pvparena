package net.slipcor.pvparena.updater;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

public class PluginUpdater extends AbstractUpdater {

    private final static String API_URL = "https://api.github.com/repos/Eredrim/pvparena/releases/latest";
    private static final String CONFIG_NODE = "update.mode";

    public PluginUpdater(Plugin plugin, List<String> msgList) {
        super(plugin, msgList, CONFIG_NODE);
    }

    protected void runUpdater() throws IOException {
        String currentVersion = plugin.getDescription().getVersion().replace("v", "");
        URL githubApi = new URL(API_URL);
        URLConnection connection = githubApi.openConnection();
        JsonObject versionJson = getVersionJson(connection.getInputStream());
        String onlineVersion = getOnlineVersionFromJson(versionJson);

        if(isUpToDate(currentVersion, onlineVersion)) {
            LOG.info("PVP Arena is up to date");
        } else {
            String updateInfo = getAnnounceMessage("PVP Arena", onlineVersion, currentVersion);
            LOG.info(updateInfo);
            if(this.updateMode == UpdateMode.ANNOUNCE) {
                this.updateMsgList.add(updateInfo);
            } else if(this.updateMode == UpdateMode.DOWNLOAD ) {
                String filename = getFilenameFromJson(versionJson);
                LOG.info("Downloading update...");
                try {
                    downloadPlugin(getDownloadUrlFromJson(versionJson), filename);
                    String updateSuccess = getPluginSuccessMessage(onlineVersion);
                    LOG.info(updateSuccess);
                    this.updateMsgList.add(updateSuccess);
                } catch (IOException e) {
                    LOG.warning("Error during plugin update");
                    e.printStackTrace();
                }
            }
        }
    }

    private static void downloadPlugin(String downloadUrlStr, String filename) throws IOException {
        URL downloadUrl = new URL(downloadUrlStr);
        final File updateFolder = Bukkit.getServer().getUpdateFolderFile();
        if (!updateFolder.exists()) {
            updateFolder.mkdirs();
        }
        final File pluginFile = new File(updateFolder, filename);
        if (pluginFile.exists()) {
            pluginFile.delete();
        }
        ReadableByteChannel readableByteChannel = Channels.newChannel(downloadUrl.openStream());
        FileOutputStream outputStream = new FileOutputStream(pluginFile);
        outputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        outputStream.close();
    }
}
