package net.slipcor.pvparena.updater;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Starts updaters and displays messages when player joins server
 */
public class UpdateChecker {
    private List<String> updateMsgList;

    /**
     * Start plugin and modules updater
     * @param plugin PVP Arena instance
     */
    public UpdateChecker(Plugin plugin) {
        this.updateMsgList = new ArrayList<>();

        PluginUpdater pluginUpdater = new PluginUpdater(plugin, this.updateMsgList);
        ModulesUpdater modulesUpdater = new ModulesUpdater(plugin, this.updateMsgList);
        new Thread(pluginUpdater).start();
        new Thread(modulesUpdater).start();
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
