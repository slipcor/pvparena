package praxis.slipcor.pvparena;

import org.bukkit.Bukkit;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import praxis.pvparena.register.payment.Methods;

/*
 * ServerListener class
 * 
 * author: slipcor
 * 
 * version: v0.2.1 - cleanup, comments
 * 
 * history:
 * 		v0.2.0 - language support
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
            	PVPArena.method = null;
        		PVPArena.lang.log_info("iconomyoff");
            }
        }
    }

    @SuppressWarnings("static-access")
	@Override
    public void onPluginEnable(PluginEnableEvent event) {
        // Check to see if we need a payment method
        if (!this.methods.hasMethod()) {
            if(this.methods.setMethod(Bukkit.getServer().getPluginManager())) {
            	PVPArena.method = this.methods.getMethod();
                PVPArena.lang.log_info("iconomyon"); 
            } else {
    			PVPArena.lang.log_info("iconomyoff");
            }
        }
    }
	
}