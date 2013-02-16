package net.slipcor.pvparena.loadables;

import java.io.File;
import java.util.List;

import org.bukkit.Location;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionShape;
import net.slipcor.pvparena.ncloader.NCBLoader;
import net.slipcor.pvparena.regions.CuboidRegion;
import net.slipcor.pvparena.regions.CylindricRegion;
import net.slipcor.pvparena.regions.SphericRegion;

/**
 * <pre>Arena Region Shape Manager class</pre>
 * 
 * Loads and manages arena region shapes
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class ArenaRegionShapeManager {
	private List<ArenaRegionShape> regions;
	private final NCBLoader<ArenaRegionShape> loader;
	private static final Debug DEBUG = new Debug(35);

	/**
	 * create an arena region manager instance
	 * 
	 * @param plugin
	 *            the plugin instance
	 */
	public ArenaRegionShapeManager(final PVPArena plugin) {
		final File path = new File(plugin.getDataFolder().toString() + "/regionshapes");
		if (!path.exists()) {
			path.mkdir();
		}
		loader = new NCBLoader<ArenaRegionShape>(plugin, path, new Object[] {});
		regions = loader.load();
		//regions.add(new CuboidRegion());

		for (ArenaRegionShape mod : regions) {
			DEBUG.i("module ArenaRegion loaded: "
					+ mod.getName() + " (version " + mod.getVersion() +")");
		}
	}

	private void fill() {
		regions.add(new CuboidRegion());
		regions.add(new CylindricRegion());
		regions.add(new SphericRegion());

		try {
			for (ArenaRegionShape mod : regions) {
				mod.onThisLoad();
				DEBUG.i("module ArenaRegion loaded: "
						+ mod.getName() + " (version " + mod.getVersion() +")");
			}
		} catch (ClassCastException cce) {
			String[] split = cce.getMessage().split(" ");
			String[] classSplit = split[0].split(".");
			String modName = classSplit[classSplit.length-1];

			PVPArena.instance.getLogger().severe("You tried to load '" + modName + "' as a region shape. Please put it into the correct folder!");
			PVPArena.instance.getLogger().severe("Aborting region shape loading!");
		}
	}

	/**
	 * search modules by module name
	 * 
	 * @param mName
	 *            the module name to find
	 * @return the module if found, null otherwise
	 */
	public ArenaRegionShape getModule(final String mName) {
		for (ArenaRegionShape region : regions) {
			if (region.getName().equalsIgnoreCase(mName)) {
				return region;
			}
		}
		return null;
	}

	public static RegionShape getShapeByName(final String string) {
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

	public ArenaRegionShape newRegion(final String name, final Arena arena, final Location pos1,
			final Location pos2, final RegionShape shape) {
		for (ArenaRegionShape region : regions) {
			if (region.getShape().equals(shape)) {
				final PABlockLocation[] locs = new PABlockLocation[2];
				locs[0] = new PABlockLocation(pos1);
				locs[1] = new PABlockLocation(pos2);
				
				final ArenaRegionShape result = ArenaRegionShape.create(arena, name, shape, locs);
				
				return result;
			}
		}
		return null;
	}

	public ArenaRegionShape newRegion(final String name, final RegionShape shape) {
		for (ArenaRegionShape region : regions) {
			if (region.getShape().equals(shape)) {
				final ArenaRegionShape result = (ArenaRegionShape) region.clone();
				result.setName(name);
				result.setShape(shape);
				return result;
			}
		}
		return null;
	}

	public void reload() {
		regions = loader.reload();
		fill();
	}
}
