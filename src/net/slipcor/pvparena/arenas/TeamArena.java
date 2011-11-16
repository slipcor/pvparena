package net.slipcor.pvparena.arenas;

import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.PVPArena;

import org.bukkit.ChatColor;
import org.bukkit.util.config.Configuration;

/*
 * Standard Blue/Red Team Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.9 - Permissions, rewrite
 * 
 * history:
 *
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.5 - Powerups!!
 *     v0.3.4 - Customisable Teams
 *     v0.3.1 - New Arena! FreeFight
 * 
 */

public class TeamArena extends Arena {
	
	/*
	 * team fight constructor
	 * 
	 * - open or create a new configuration file
	 * - parse the arena config
	 */
	@SuppressWarnings("unchecked")
	public TeamArena(String sName, PVPArena plugin) {
		super(sName, plugin);
		
		Map<String, String> fT = new HashMap<String, String>();
		fT.put("red",ChatColor.RED.name());
		fT.put("blue",ChatColor.BLUE.name());
		
		Configuration config = new Configuration(configFile);
		config.load();
		if (config.getProperty("teams.custom") == null) {
			config.setProperty("teams.custom", fT);
			config.save();
		}
		this.paTeams = (Map<String, String>) config.getProperty("teams.custom");
	}
}
