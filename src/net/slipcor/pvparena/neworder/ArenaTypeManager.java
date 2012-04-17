package net.slipcor.pvparena.neworder;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.nodinchan.loader.Loader;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arenas.teams.TeamArena;

/**
 * arena type manager class
 * 
 * -
 * 
 * loads arena types into PVP Arena
 * 
 * @author slipcor
 * 
 * @version v0.7.9
 * 
 */

public class ArenaTypeManager {
	private final List<ArenaType> types;

	/**
	 * create an arena type instance
	 * 
	 * @param plugin
	 *            the plugin instance
	 */
	public ArenaTypeManager(PVPArena plugin) {
		File path = new File(plugin.getDataFolder().toString() + "/arenas");
		if (!path.exists()) {
			path.mkdir();
		}
		types = new Loader<ArenaType>(plugin, path, new Object[] {}).load();
		types.add(new TeamArena());

		for (ArenaType type : types) {
			System.out.print("[PVP Arena] module ArenaType loaded: "
					+ type.getName() + " (version " + type.version() +")");
		}
	}

	/**
	 * find an arena type by arena type name
	 * 
	 * @param tName
	 *            the type name to find
	 * @return the arena type if found, null otherwise
	 */
	public ArenaType getType(String tName) {
		for (ArenaType type : types) {
			if (type.getName().equals(tName)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * hook into language initialisation
	 * 
	 * @param config
	 *            the arena config
	 */
	public void initLanguage(YamlConfiguration config) {
		for (ArenaType type : types) {
			type.initLanguage(config);
		}
	}
}
