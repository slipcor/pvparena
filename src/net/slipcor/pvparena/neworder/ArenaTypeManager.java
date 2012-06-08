package net.slipcor.pvparena.neworder;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.nodinchan.ncloader.Loader;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arenas.teams.TeamArena;
import net.slipcor.pvparena.core.Debug;

/**
 * arena type manager class
 * 
 * -
 * 
 * loads arena types into PVP Arena
 * 
 * @author slipcor
 * 
 * @version v0.8.7
 * 
 */

public class ArenaTypeManager {
	private List<ArenaType> types;
	private final Loader<ArenaType> loader;
	protected Debug db = new Debug(52);
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
		loader = new Loader<ArenaType>(plugin, path, new Object[] {});
		types = loader.load();
		types.add(new TeamArena());

		for (ArenaType type : types) {
			db.i("module ArenaType loaded: "
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
			if (type.getName().equalsIgnoreCase(tName)) {
				return type;
			}
		}
		return null;
	}

	public List<ArenaType> getTypes() {
		return types;
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
	
	public void reload() {
		types = loader.reload();
		types.add(new TeamArena());

		for (ArenaType type : types) {
			db.i("module ArenaType loaded: "
					+ type.getName() + " (version " + type.version() +")");
		}
	}
}
