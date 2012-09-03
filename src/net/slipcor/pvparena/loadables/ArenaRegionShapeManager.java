package net.slipcor.pvparena.loadables;

import java.io.File;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import com.nodinchan.ncbukkit.loader.Loader;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionShape;
import net.slipcor.pvparena.regions.CuboidRegion;

/**
 * <pre>Arena Region Shape Manager class</pre>
 * 
 * Loads and manages arena region shapes
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class ArenaRegionShapeManager {
	private List<ArenaRegionShape> regions;
	private final Loader<ArenaRegionShape> loader;
	Debug db = new Debug(35);

	/**
	 * create an arena region manager instance
	 * 
	 * @param plugin
	 *            the plugin instance
	 */
	public ArenaRegionShapeManager(PVPArena plugin) {
		File path = new File(plugin.getDataFolder().toString() + "/regionshapes");
		if (!path.exists()) {
			path.mkdir();
		}
		loader = new Loader<ArenaRegionShape>(plugin, path, new Object[] {});
		regions = loader.load();
		regions.add(new CuboidRegion());

		for (ArenaRegionShape mod : regions) {
			db.i("module ArenaRegion loaded: "
					+ mod.getName() + " (version " + mod.getVersion() +")");
		}
	}

	/**
	 * search modules by module name
	 * 
	 * @param mName
	 *            the module name to find
	 * @return the module if found, null otherwise
	 */
	public ArenaRegionShape getModule(String mName) {
		for (ArenaRegionShape region : regions) {
			if (region.getName().equalsIgnoreCase(mName)) {
				return region;
			}
		}
		return null;
	}

	private static RegionShape getShapeByCoordDefinition(String coords) {
		return getShapeByName(coords.split(",")[0]);
	}

	public static RegionShape getShapeByName(String string) {
		for (RegionShape shape: RegionShape.values()) {
			if (shape.name().startsWith(string.toUpperCase().substring(0,2))) {
				return shape;
			}
		}
		return null;
	}

	public List<ArenaRegionShape> getRegions() {
		return regions;
	}

	public ArenaRegionShape newRegion(String name, Arena arena, Location pos1,
			Location pos2, RegionShape shape) {
		for (ArenaRegionShape region : regions) {
			if (region.getShape().equals(shape)) {
				PABlockLocation[] locs = new PABlockLocation[2];
				locs[0] = new PABlockLocation(pos1);
				locs[1] = new PABlockLocation(pos2);
				
				ArenaRegionShape result = ArenaRegionShape.create(arena, name, shape, locs);
				
				return result;
			}
		}
		return null;
	}

	public ArenaRegionShape newRegion(String name, RegionShape shape) {
		for (ArenaRegionShape region : regions) {
			if (region.getShape().equals(shape)) {
				ArenaRegionShape result = (ArenaRegionShape) region.clone();
				result.setName(name);
				result.setShape(shape);
				return result;
			}
		}
		return null;
	}

	public ArenaRegionShape readRegionFromConfig(String regionName,
			YamlConfiguration config, Arena arena) {
		db.i("reading config region: " + arena.getName() + "=>" + regionName);
		String coords = config.getString("arenaregions." + regionName);
		
		ArenaRegionShape.RegionShape shape = ArenaRegionShapeManager.getShapeByCoordDefinition(coords);
		
		PABlockLocation[] locs = Config.parseRegion(coords);
		
		ArenaRegionShape region = ArenaRegionShape.create(arena, regionName, shape, locs);
		
		
		return region;
	}
	
	public void reload() {
		regions = loader.reload();
		regions.add(new CuboidRegion());

		for (ArenaRegionShape mod : regions) {
			db.i("module ArenaRegion loaded: "
					+ mod.getName() + " (version " + mod.getVersion() +")");
		}
	}
}
