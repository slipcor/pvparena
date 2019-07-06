package net.slipcor.pvparena.updater;

import com.google.gson.JsonObject;
import net.slipcor.pvparena.core.Language;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import static net.slipcor.pvparena.core.Language.*;

/**
 * Manage modules versions and updates
 */
public class ModulesUpdater extends AbstractUpdater {

    private final static String API_URL = "https://api.github.com/repos/Eredrim/pvparena_modules/releases/latest";
    private static final String CONFIG_NODE = "update.modules";

    /**
     * Construct a modules updater
     * @param plugin PVP Arena instance
     * @param msgList Reference to UpdateChecker message list
     */
    public ModulesUpdater(Plugin plugin, List<String> msgList) {
        super(plugin, msgList, CONFIG_NODE);
    }

    /**
     * Run modules updaters : checks version and downloads update according to config
     * @throws IOException Exception if can't connect to github API
     */
    protected void runUpdater() throws IOException {
        String currentVersion = this.getModulesVersion();
        URL githubApi = new URL(API_URL);
        URLConnection connection = githubApi.openConnection();
        JsonObject versionJson = getVersionJson(connection.getInputStream());
        String onlineVersion = getOnlineVersionFromJson(versionJson);

        if(isUpToDate(currentVersion, onlineVersion)) {
            LOG.info("PVP Arena modules are up to date");
        } else {
            String updateInfo = getAnnounceMessage(MSG.UPDATER_MODULES.toString(), onlineVersion, currentVersion);
            LOG.info(updateInfo);
            if(this.updateMode == UpdateMode.ANNOUNCE) {
                this.updateMsgList.add(updateInfo);
            } else if(this.updateMode == UpdateMode.DOWNLOAD ) {
                String filename = getFilenameFromJson(versionJson);
                LOG.info("Downloading modules update...");
                try {
                    this.downloadAndUnpackModules(getDownloadUrlFromJson(versionJson), filename);
                    String updateSuccess = getSuccessMessage(MSG.UPDATER_MODULES.toString(), onlineVersion);
                    LOG.info(updateSuccess);
                    this.updateMsgList.add(updateSuccess);
                } catch (IOException e) {
                    LOG.warning("Error during modules update");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get current modules version based on version.lock
     * @return version string (x.x.x format)
     */
    private String getModulesVersion() {
        File lockFile = new File(this.getFilesFolder(), "version.lock");
        if(lockFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(lockFile.toPath(), Charset.defaultCharset());
                return lines.get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "0.0.0";
    }

    /**
     * Downloads new module package and unpack it
     * @param downloadUrlStr Download url
     * @param filename Packge file name
     * @throws IOException
     */
    private void downloadAndUnpackModules(String downloadUrlStr, String filename) throws IOException {
        URL downloadUrl = new URL(downloadUrlStr);
        File zipFile = new File(this.plugin.getDataFolder(), filename);
        if (zipFile.exists()) {
            zipFile.delete();
        }
        ReadableByteChannel readableByteChannel = Channels.newChannel(downloadUrl.openStream());
        FileOutputStream outputStream = new FileOutputStream(zipFile);
        outputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        outputStream.close();
        deleteDirectory(this.getFilesFolder());
        ZipUtil.unzip(zipFile, this.plugin.getDataFolder());
        zipFile.delete();
    }

    /**
     * Returns PVP Arena "files" folder
     * @return "files" folder
     */
    private File getFilesFolder() {
        return new File(this.plugin.getDataFolder().getPath() + "/files");
    }

    /**
     * Removes recusively a directory
     * @param directoryToBeDeleted directory to be deleted
     */
    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
