package net.slipcor.pvparena.loadables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.ncloader.NCBLoader;
import net.slipcor.pvparena.regions.CuboidRegion;
import net.slipcor.pvparena.regions.CylindricRegion;
import net.slipcor.pvparena.regions.SphericRegion;

import java.io.File;
import java.util.List;

/**
 * <pre>Arena Region Shape Manager class</pre>
 * <p/>
 * Loads and manages arena region shapes
 *
 * @author slipcor
 * @version v0.9.1
 */

public class ArenaRegionShapeManager {
    private static List<ArenaRegionShape> regions;
    private final NCBLoader<ArenaRegionShape> loader;
    private static final Debug DEBUG = new Debug(35);

    /**
     * create an arena region manager instance
     *
     * @param plugin the plugin instance
     */
    public ArenaRegionShapeManager(final PVPArena plugin) {
        final File path = new File(plugin.getDataFolder() + "/regionshapes");
        if (!path.exists()) {
            path.mkdir();
        }
        loader = new NCBLoader<>(plugin, path);
        regions = loader.load(ArenaRegionShape.class);
        fill();
    }

    private void fill() {
        regions.add(new CuboidRegion());
        regions.add(new CylindricRegion());
        regions.add(new SphericRegion());

        for (final ArenaRegionShape mod : regions) {
            mod.onThisLoad();
            DEBUG.i("module ArenaRegionShape loaded: "
                    + mod.getName() + " (version " + mod.getVersion() + ')');
        }
    }

    /**
     * search modules by module name
     *
     * @param mName the module name to find
     * @return the module if found, null otherwise
     */
    public ArenaRegionShape getModule(final String mName) {
        for (final ArenaRegionShape region : regions) {
            if (region.getName().equalsIgnoreCase(mName)) {
                return region;
            }
        }
        return null;
    }

    public static ArenaRegionShape getShapeByName(final String string) {
        for (final ArenaRegionShape shape : regions) {
            if (shape.getName().toUpperCase().startsWith(string.toUpperCase().substring(0, 2))) {
                return shape;
            }
        }
        return null;
    }

    public List<ArenaRegionShape> getRegions() {
        return regions;
    }


    public void reload() {
        regions = loader.reload(ArenaRegionShape.class);
        fill();
    }
}
