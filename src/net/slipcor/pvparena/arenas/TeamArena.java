package net.slipcor.pvparena.arenas;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.util.config.Configuration;

/*
 * Standard Blue/Red Team Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.4 - Customisable Teams
 * 
 * history:
 *
 *     v0.3.1 - New Arena! FreeFight
 * 
 */

public class TeamArena extends Arena {
	@SuppressWarnings("unchecked")
	public TeamArena(String sName) {
		super(sName);
		
		Map<String, String> fT = new HashMap<String, String>();
		fT.put("red",ChatColor.RED.name());
		fT.put("blue",ChatColor.BLUE.name());
		
		Configuration config = new Configuration(configFile);
		config.load();
		if (config.getProperty("teams.custom") == null) {
			config.setProperty("teams.custom", fT);
			config.save();
		}
		this.fightTeams = (Map<String, String>) config.getProperty("teams.custom");
		/*
		 * teams:
		 *     red: RED
		 *     blue: BLUE
		 */
		
		/*
		 * teams:
		 *     Rangers: GREEN
		 *     Mages: DARK_PURPLE
		 *     Knights: GREY
		 */
	}
}
