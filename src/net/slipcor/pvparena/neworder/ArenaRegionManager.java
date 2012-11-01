package net.slipcor.pvparena.neworder;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.ncloader.NCLoader;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionShape;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionType;
import net.slipcor.pvparena.regions.Cuboid;

/**
 * arena region manager class
 * 
 * -
 * 
 * loads region modules into PVP Arena
 * 
 * @author slipcor
 * 
 * @version v0.8.10
 * 
 */

public class ArenaRegionManager {
	private List<ArenaRegion> regions;
	private final NCLoader<ArenaRegion> loader;
	Debug db = new Debug(45);

	/**
	 * create an arena region manager instance
	 * 
	 * @param plugin
	 *            the plugin instance
	 */
	public ArenaRegionManager(PVPArena plugin) {
		File path = new File(plugin.getDataFolder().toString() + "/regions");
		if (!path.exists()) {
			path.mkdir();
		}
		loader = new NCLoader<ArenaRegion>(plugin, path, new Object[] {});
		regions = loader.load();
		regions.add(new Cuboid());

		for (ArenaRegion mod : regions) {
			db.i("module ArenaRegion loaded: "
					+ mod.getName() + " (version " + mod.version() +")");
		}
	}

	/**
	 * search modules by module name
	 * 
	 * @param mName
	 *            the module name to find
	 * @return the module if found, null otherwise
	 */
	public ArenaRegion getModule(String mName) {
		for (ArenaRegion region : regions) {
			if (region.getName().equalsIgnoreCase(mName)) {
				return region;
			}
		}
		return null;
	}

	private static RegionShape getShapeByCoords(String coords) {
		String[] vars = coords.split(",");
		
		if (vars.length < 7) {
			return RegionShape.CUBOID;
		} else {
			return getShapeByName(vars[vars.length-1]);
		}
	}

	public static RegionShape getShapeByName(String string) {
		for (RegionShape shape: RegionShape.values()) {
			if (shape.name().startsWith(string.toUpperCase().substring(0,2))) {
				return shape;
			}
		}
		return null;
	}

	public List<ArenaRegion> getRegions() {
		return regions;
	}

	public ArenaRegion newRegion(String name, Arena arena, Location pos1,
			Location pos2, RegionShape shape) {
		for (ArenaRegion region : regions) {
			if (region.getShape().equals(shape)) {
				ArenaRegion result = region.clone();
				result.name = name;
				result.min = pos1.toVector();
				result.max = pos2.toVector();
				result.arena = arena;
				result.world = pos1.getWorld();

				setRegionTypeByName(result, name);
				
				result.initialize();
				return result;
			}
		}
		return null;
	}

	public ArenaRegion newRegion(String name, RegionShape shape) {
		for (ArenaRegion region : regions) {
			if (region.getShape().equals(shape)) {
				ArenaRegion result = region.clone();
				result.name = name;
				setRegionTypeByName(result, name);
				return result;
			}
		}
		return null;
	}

	public ArenaRegion readRegionFromConfig(String regionName,
			YamlConfiguration config, Arena arena) {
		db.i("reading config region: " + arena.name + "=>" + regionName);
		String coords = config.getString("regions." + regionName);
		
		ArenaRegion.RegionShape shape = ArenaRegionManager.getShapeByCoords(coords);
		
		ArenaRegion region = PVPArena.instance.getArm().newRegion(regionName, shape);
		
		if (region != null) {
			region.arena = arena;
			region.set(Bukkit.getWorld(arena.getWorld()), coords);
		}
		return region;
	}
	
	public void reload() {
		regions = loader.reload();
		regions.add(new Cuboid());

		for (ArenaRegion mod : regions) {
			db.i("module ArenaRegion loaded: "
					+ mod.getName() + " (version " + mod.version() +")");
		}
	}
	
	private void setRegionTypeByName(ArenaRegion r, String s) {
		if (s.equals("battlefield")) {
			r.setType(RegionType.BATTLEFIELD);
		} else if (s.equals("spectator")) {
			r.setType(RegionType.SPECTATOR);
		} else if (s.equals("exit")) {
			r.setType(RegionType.EXIT);
		} else if (s.endsWith("lounge")) {
			r.setType(RegionType.LOUNGE);
		} else if (s.equals("join")) {
			r.setType(RegionType.JOIN);
		} else if (s.startsWith("death")) {
			r.setType(RegionType.DEATH);
		} else if (s.startsWith("nocamp")) {
			r.setType(RegionType.NOCAMP);
		} else if (s.startsWith("win")) {
			r.setType(RegionType.WIN);
		} else if (s.startsWith("lose")) {
			r.setType(RegionType.LOSE);
		} else {
			r.setType(RegionType.CUSTOM);
		}
	}
}
