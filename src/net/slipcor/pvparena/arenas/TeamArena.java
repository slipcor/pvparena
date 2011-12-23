/*
 * standard team arena class
 * 
 * author: slipcor
 * 
 * version: v0.4.1 - command manager, arena information and arena config check
 * 
 * history:
 * 
 *     v0.4.0 - mayor rewrite, improved help
 *     v0.3.10 - CraftBukkit #1337 config version, rewrite
 *     v0.3.9 - Permissions, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.5 - Powerups!!
 *     v0.3.4 - Customisable Teams
 *     v0.3.1 - New Arena! FreeFight
 */

package net.slipcor.pvparena.arenas;

import java.util.Map;

import org.bukkit.ChatColor;

public class TeamArena extends Arena {

	/*
	 * team fight constructor
	 * 
	 * - open or create a new configuration file - parse the arena config
	 */
	public TeamArena(String sName) {
		super(sName);

		
		if (cfg.get("teams.custom") == null && cfg.get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			cfg.getYamlConfiguration().addDefault("teams.red", ChatColor.RED.name());
			cfg.getYamlConfiguration().addDefault("teams.blue", ChatColor.BLUE.name());
			cfg.getYamlConfiguration().options().copyDefaults(true);
			cfg.reloadMaps();
		}
		cfg.save();
		Map<String, Object> tempMap = (Map<String, Object>) cfg.getYamlConfiguration()
				.getConfigurationSection("teams").getValues(true);

		for (String sTeam : tempMap.keySet()) {
			this.paTeams.put(sTeam, (String) tempMap.get(sTeam));
			db.i("added team " + sTeam + " => " + this.paTeams.get(sTeam));
		}
	}
}
