package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.register.payment.Methods;

import org.bukkit.Bukkit;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

/**
 * server listener class
 * 
 * -
 * 
 * PVP Arena Server Listener
 * 
 * @author slipcor
 * 
 * @version v0.4.0
 * 
 */

public class PAServerListener extends ServerListener {
	private Methods methods = null;

	public PAServerListener() {
		this.methods = new Methods();
	}

	@SuppressWarnings("static-access")
	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		// Check to see if the plugin thats being disabled is the one we are
		// using
		if (this.methods != null && this.methods.hasMethod()) {
			Boolean check = this.methods.checkDisabled(event.getPlugin());

			if (check) {
				PVPArena.instance.setMethod(null);
				PVPArena.lang.log_info("iconomyoff");
			}
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		// Check to see if we need a payment method
		if (!this.methods.hasMethod()) {
			if (this.methods.setMethod(Bukkit.getServer().getPluginManager())) {
				PVPArena.instance.setMethod(this.methods.getMethod());
				PVPArena.lang.log_info("iconomyon");
			} else {
				PVPArena.lang.log_info("iconomyoff");
			}
		}
	}

}