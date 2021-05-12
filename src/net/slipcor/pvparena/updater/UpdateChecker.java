package net.slipcor.pvparena.updater;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts updaters and displays messages when player joins server
 */
public class UpdateChecker {
    private List<String> updateMsgList;
    private PluginUpdater pluginUpdater;

    /**
     * Start plugin and modules updater
     * @param pluginJarFile plugin jar file
     */
    public UpdateChecker(File pluginJarFile) {
        this.updateMsgList = new ArrayList<>();

        this.pluginUpdater = new PluginUpdater(this.updateMsgList, pluginJarFile);
        ModulesUpdater modulesUpdater = new ModulesUpdater(this.updateMsgList);
        new Thread(this.pluginUpdater).start();
        new Thread(modulesUpdater).start();
    }

    /**
     * Run methods for plugin updater during plugin disabling
     */
    public void runOnDisable() {
        this.pluginUpdater.runOnDisable();
    }

    /**
     * Send update message to players (OPs) on login
     * @param player player who joins server
     */
    public void displayMessage(final Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new Runnable() {
            @Override
            public void run() {
                for(String message : updateMsgList) {
                    player.sendMessage(Language.parse(Language.MSG.MESSAGES_GENERAL, "PVP Arena", message));
                }
            }
        }, 20L);

    }
}
