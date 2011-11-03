package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.PVPArenaPlugin;
import net.slipcor.pvparena.register.payment.Methods;

import org.bukkit.Bukkit;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

/*
 * ServerListener class
 * 
 * author: slipcor
 * 
 * version: v0.3.1 - New Arena! FreeFight
 * 
 * history:
 *
 *     v0.3.0 - Multiple Arenas
 * 	   v0.2.1 - cleanup, comments
 * 	   v0.2.0 - language support
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
        // Check to see if the plugin thats being disabled is the one we are using
        if (this.methods != null && this.methods.hasMethod()) {
            Boolean check = this.methods.checkDisabled(event.getPlugin());

            if(check) {
            	PVPArenaPlugin.method = null;
        		PVPArenaPlugin.lang.log_info("iconomyoff");
            }
        }
    }

    @SuppressWarnings("static-access")
	@Override
    public void onPluginEnable(PluginEnableEvent event) {
        // Check to see if we need a payment method
        if (!this.methods.hasMethod()) {
            if(this.methods.setMethod(Bukkit.getServer().getPluginManager())) {
            	PVPArenaPlugin.method = this.methods.getMethod();
                PVPArenaPlugin.lang.log_info("iconomyon"); 
            } else {
    			PVPArenaPlugin.lang.log_info("iconomyoff");
            }
        }
    }
	
}