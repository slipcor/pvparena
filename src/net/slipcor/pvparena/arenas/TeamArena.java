/*
 * standard team arena class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.3.10 - CraftBukkit #1337 config version, rewrite
 *     v0.3.9 - Permissions, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.5 - Powerups!!
 *     v0.3.4 - Customisable Teams
 *     v0.3.1 - New Arena! FreeFight
 */

package net.slipcor.pvparena.arenas;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class TeamArena extends Arena {

	/*
	 * team fight constructor
	 * 
	 * - open or create a new configuration file - parse the arena config
	 */
	public TeamArena(String sName) {
		super(sName);

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		config.addDefault("teams.custom.red", ChatColor.RED.name());
		config.addDefault("teams.custom.blue", ChatColor.BLUE.name());
		config.options().copyDefaults(true);
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, Object> tempMap = (Map<String, Object>) config
				.getConfigurationSection("teams.custom").getValues(true);

		for (String sTeam : tempMap.keySet()) {
			this.paTeams.put(sTeam, (String) tempMap.get(sTeam));
		}
	}
}
