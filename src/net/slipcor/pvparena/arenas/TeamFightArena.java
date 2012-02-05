package net.slipcor.pvparena.arenas;

import java.util.Map;

import net.slipcor.pvparena.definitions.Arena;

import org.bukkit.ChatColor;

/**
 * standard team arena class
 * 
 * -
 * 
 * contains >Team< arena methods and variables
 * 
 * @author slipcor
 * 
 * @version v0.6.0
 * 
 */

public class TeamFightArena extends Arena {

	/**
	 * construct a team fight arena
	 * 
	 * @param sName
	 *            the arena name
	 */
	public TeamFightArena(String sName) {
		super(sName);

		if (cfg.get("teams.custom") == null && cfg.get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			cfg.getYamlConfiguration().addDefault("teams.red",
					ChatColor.RED.name());
			cfg.getYamlConfiguration().addDefault("teams.blue",
					ChatColor.BLUE.name());
			cfg.getYamlConfiguration().options().copyDefaults(true);
			cfg.reloadMaps();
		}
		cfg.save();
		Map<String, Object> tempMap = (Map<String, Object>) cfg
				.getYamlConfiguration().getConfigurationSection("teams")
				.getValues(true);

		for (String sTeam : tempMap.keySet()) {
			this.paTeams.put(sTeam, (String) tempMap.get(sTeam));
			db.i("added team " + sTeam + " => " + this.paTeams.get(sTeam));
		}
	}
}
