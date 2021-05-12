package net.slipcor.pvparena.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract class with shared methods fo updaters
 */
public abstract class AbstractUpdater implements Runnable {
    protected final UpdateMode updateMode;
    protected List<String> updateMsgList;
    protected Runnable toRunOnDisable;
    protected static final Logger LOG = PVPArena.instance.getLogger();

    /**
     * Constructs a AbstractUpdater instance
     * @param updateMsgList Reference to UpdateChecker message list
     * @param configNode YML config node to get update setting
     */
    public AbstractUpdater(List<String> updateMsgList, String configNode) {
        FileConfiguration config = PVPArena.instance.getConfig();
        this.updateMode = UpdateMode.getBySetting(config.getString(configNode, UpdateMode.ANNOUNCE.name()));
        this.updateMsgList = updateMsgList;
    }

    /**
     * Runs an updater if it's not disabled
     */
    public void run() {
        try {
            if(this.updateMode != UpdateMode.OFF) {
                runUpdater();
            }
        } catch (IOException e) {
            LOG.warning("Unable to connect to api.github.com");
        }
    }

    public void runOnDisable() {
        if(this.toRunOnDisable != null) {
            this.toRunOnDisable.run();
        }
    }

    /**
     * Runs  an updater implementation
     * @throws IOException Exception if api.github.com is unreachable
     */
    protected abstract void runUpdater() throws IOException;

    /**
     * Returns release version from API json
     * @param jsonObject API json
     * @return version as string
     */
    protected static String getOnlineVersionFromJson(JsonObject jsonObject) {
        String tagName = jsonObject.get("tag_name").getAsString();
        return tagName.replace("v", "");
    }

    /**
     * Returns release download URL from API json
     * @param jsonObject API json
     * @return download URL as string
     */
    protected static String getDownloadUrlFromJson(JsonObject jsonObject) {
        JsonArray jsonArray = jsonObject.getAsJsonArray("assets");
        JsonObject assetArray = jsonArray.get(0).getAsJsonObject();
        return assetArray.get("browser_download_url").getAsString();
    }

    /**
     * Returns release filename from API json
     * @param jsonObject API json
     * @return filename as string
     */
    protected static String getFilenameFromJson(JsonObject jsonObject) {
        JsonArray jsonArray = jsonObject.getAsJsonArray("assets");
        JsonObject assetArray = jsonArray.get(0).getAsJsonObject();
        return assetArray.get("name").getAsString();
    }

    /**
     * Returns a JsonObject view of Api result stream
     * @param inputStream InputStream returned by Api connection
     * @return JsonObject
     * @throws IOException Exception on json parsing
     */
    protected static JsonObject getVersionJson(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        JsonElement jsonElement = new JsonParser().parse(in);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        in.close();
        return jsonObject;
    }

    /**
     * Returns announce message of new available update
     * @param args Args for stringFormatter
     * @return Announce message
     */
    protected static String getAnnounceMessage(String... args) {
        return Language.parse(MSG.UPDATER_ANNOUNCE, args);
    }

    /**
     * Returns update success message
     * @param args Args for stringFormatter
     * @return success message
     */
    protected static String getSuccessMessage(String... args) {
        return Language.parse(MSG.UPDATER_SUCCESS, args);
    }

    /**
     * Checks if current version is up to date
     * @param currentVersion version currently installed as x.x.x format
     * @param newVersion latest version available as x.x.x format
     * @return true if current version is up to date, false otherwise
     */
    protected static boolean isUpToDate(String currentVersion, String newVersion) {
        String[] fullCurrentVerArr = currentVersion.split("-");
        boolean isSnapshot = currentVersion.contains("SNAPSHOT");
        String[] currentVerArr = fullCurrentVerArr[0].split("\\.");
        String[] newVerArr = newVersion.split("\\.");
        int currentVerVal = 0;
        int newVerVal = 0;

        final int versionLen = 3;
        for(int i = 0; i < versionLen; i++) {
            Long currentVerChunk = Long.valueOf(currentVerArr[i]);
            Long newVerChunk = Long.valueOf(newVerArr[i]);
            currentVerVal += currentVerChunk * Math.pow(10, versionLen - i);
            newVerVal += newVerChunk * Math.pow(10, versionLen - i);
        }

        if(currentVerVal == newVerVal) {
            //Release > snapshot if there are the same number
            return !isSnapshot;
        }
        return currentVerVal > newVerVal;
    }
}
