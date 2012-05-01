package net.slipcor.pvparena.neworder;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import com.nodinchan.ncloader.Loader;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
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
 * @version v0.7.18
 * 
 */

public class ArenaRegionManager {
	private final List<ArenaRegion> regions;
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
		regions = new Loader<ArenaRegion>(plugin, path, new Object[] {}).load();
		regions.add(new Cuboid());

		for (ArenaRegion mod : regions) {
			System.out.print("[PVP Arena] module ArenaRegion loaded: "
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
			if (region.getName().equals(mName)) {
				return region;
			}
		}
		return null;
	}

	public static RegionShape getShapeByName(String string) {
		for (RegionShape shape: RegionShape.values()) {
			if (shape.name().startsWith(string.toUpperCase().substring(0,2))) {
				return shape;
			}
		}
		return null;
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
				
				if (name.equals("battlefield")) {
					result.setType(RegionType.BATTLEFIELD);
				} else if (name.equals("spectator")) {
					result.setType(RegionType.SPECTATOR);
				} else if (name.equals("exit")) {
					result.setType(RegionType.EXIT);
				} else if (name.endsWith("lounge")) {
					result.setType(RegionType.LOUNGE);
				} else if (name.equals("join")) {
					result.setType(RegionType.JOIN);
				} else if (name.startsWith("death")) {
					result.setType(RegionType.DEATH);
				} else if (name.startsWith("nocamp")) {
					result.setType(RegionType.NOCAMP);
				} else {
					result.setType(RegionType.CUSTOM);
				}
				return result;
			}
		}
		return null;
	}
	
	public ArenaRegion newRegion(String name, RegionShape shape) {
		System.out.print("new Region (load)");
		for (ArenaRegion region : regions) {
			System.out.print("compare: " + region.getShape() + " == " + shape);
			if (region.getShape().equals(shape)) {
				ArenaRegion result = region.clone();
				result.name = name;
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
		
		System.out.print("shape: " + shape);
		
		ArenaRegion region = PVPArena.instance.getArm().newRegion(regionName, shape);
		
		if (region != null) {
			region.arena = arena;
			region.set(Bukkit.getWorld(arena.getWorld()), coords);
		}
		return region;
	}

	private static RegionShape getShapeByCoords(String coords) {
		String[] vars = coords.split(",");
		
		if (vars.length < 7) {
			return RegionShape.CUBOID;
		} else {
			return getShapeByName(vars[vars.length-1]);
		}
	}

	public List<ArenaRegion> getRegions() {
		return regions;
	}
}
