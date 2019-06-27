package net.slipcor.pvparena.updater;

import net.slipcor.pvparena.PVPArena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class UpdateChecker {
    private List<String> updateMsgList;

    public UpdateChecker(Plugin plugin) {
        this.updateMsgList = new ArrayList<>();

        PluginUpdater pluginUpdater = new PluginUpdater(plugin, this.updateMsgList);
        ModulesUpdater modulesUpdater = new ModulesUpdater(plugin, this.updateMsgList);
        new Thread(pluginUpdater).start();
        new Thread(modulesUpdater).start();
    }

    public void displayMessage(final Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new Runnable() {
            @Override
            public void run() {
                for(String message : updateMsgList) {
                    player.sendMessage(String.format("%s=- %s -=%s", ChatColor.GREEN, message, ChatColor.RESET));
                }
            }
        }, 20L);

    }
}
