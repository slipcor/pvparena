package craftyn.pvparena;

import com.iConomy.iConomy;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

/*
 * ServerListener class
 * 
 * author: craftyn
 * editor: slipcor
 * 
 * version: v0.2.0 - language support
 * 
 * history:
 * 		v0.0.0 - copypaste
 */

public class PAServerListener extends ServerListener {
	private PVPArena plugin;

	public PAServerListener(PVPArena instance) {
		this.plugin = instance;
	}

	public void onPluginDisable(PluginDisableEvent event) {
		if ((PVPArena.iConomy == null) || (!(event.getPlugin().getDescription().getName().equals("iConomy"))))
			return;
		PVPArena.iConomy = null;
		PVPArena.lang.log_info("iconomyoff");
	}

	public void onPluginEnable(PluginEnableEvent event) {
		if (PVPArena.iConomy == null) {
			Plugin iConomy = this.plugin.getServer().getPluginManager().getPlugin("iConomy");
			if ((iConomy == null) || (!(iConomy.isEnabled())))
				return;
			PVPArena.iConomy = (iConomy) iConomy;
			PVPArena.lang.log_info("iconomyon");
		}
	}
}