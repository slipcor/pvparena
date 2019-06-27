package net.slipcor.pvparena.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.slipcor.pvparena.PVPArena;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractUpdater implements Runnable {
    protected final Plugin plugin;
    protected final UpdateMode updateMode;
    protected List<String> updateMsgList;
    protected static final Logger LOG = PVPArena.instance.getLogger();

    public AbstractUpdater(Plugin plugin, List<String> updateMsgList, String configNode) {
        this.plugin = plugin;
        this.updateMode = UpdateMode.getBySetting(plugin.getConfig().getString(configNode, UpdateMode.ANNOUNCE.name()));
        this.updateMsgList = updateMsgList;
    }

    public void run() {
        try {
            if(this.updateMode != UpdateMode.OFF) {
                runUpdater();
            }
        } catch (IOException e) {
            LOG.warning("Unable to connect to api.github.com");
        }
    }

    protected abstract void runUpdater() throws IOException;

    protected static String getOnlineVersionFromJson(JsonObject jsonObject) {
        String tagName = jsonObject.get("tag_name").getAsString();
        return tagName.replace("v", "");
    }

    protected static String getDownloadUrlFromJson(JsonObject jsonObject) {
        JsonArray jsonArray = jsonObject.getAsJsonArray("assets");
        JsonObject assetArray = jsonArray.get(0).getAsJsonObject();
        return assetArray.get("browser_download_url").getAsString();
    }

    protected static String getFilenameFromJson(JsonObject jsonObject) {
        JsonArray jsonArray = jsonObject.getAsJsonArray("assets");
        JsonObject assetArray = jsonArray.get(0).getAsJsonObject();
        return assetArray.get("name").getAsString();
    }

    protected static JsonObject getVersionJson(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        JsonElement jsonElement = new JsonParser().parse(in);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        in.close();
        return jsonObject;
    }

    protected static String getAnnounceMessage(Object... args) {
        return String.format("%s %s is now available ! Your version: %s", args);
    }

    protected static String getPluginSuccessMessage(String version) {
        return String.format("PVP Arena has been updated to %s. Restart your server to apply update.", version);
    }

    protected static String getModulesSuccessMessage(String version) {
        return String.format("PVP Arena Modules have been updated to %s. Run /pa update to apply new version", version);
    }

    protected static boolean isUpToDate(String currentVersion, String newVersion) {
        String[] fullCurrentVerArr = currentVersion.split("-SNAPSHOT");
        boolean isSnapshot = fullCurrentVerArr.length > 1;
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
