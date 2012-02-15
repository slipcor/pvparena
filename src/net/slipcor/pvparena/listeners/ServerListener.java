package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.register.payment.Methods;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * server listener class
 * 
 * -
 * 
 * PVP Arena Server Listener
 * 
 * @author slipcor
 * 
 * @version v0.6.2
 * 
 */

public class ServerListener implements Listener {
	private Methods methods = null;

	public ServerListener() {
		this.methods = new Methods();
	}

	@SuppressWarnings("static-access")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginDisable(PluginDisableEvent event) {
		// Check to see if the plugin thats being disabled is the one we are
		// using
		if (this.methods != null && this.methods.hasMethod()) {
			Boolean check = this.methods.checkDisabled(event.getPlugin());

			if (check) {
				PVPArena.eco = null;
				PVPArena.lang.log_info("iconomyoff");
			}
		}
	}

	@SuppressWarnings("static-access")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event) {
		// Check to see if we need a payment method
		if (!this.methods.hasMethod()) {
			if (this.methods.setMethod(Bukkit.getServer().getPluginManager())) {
				PVPArena.eco = this.methods.getMethod();
				PVPArena.lang.log_info("iconomyon");
			} else {
				PVPArena.lang.log_info("iconomyoff");
			}
		}
	}
}