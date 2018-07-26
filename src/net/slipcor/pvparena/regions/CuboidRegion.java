package net.slipcor.pvparena.regions;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre>
 * Arena Region Shape class "cuboid"
 * </pre>
 * <p/>
 * Defines a cuboid region, including overlap checks and contain checks
 *
 * @author slipcor
 */

public class CuboidRegion extends ArenaRegionShape {

    private final Set<Block> border = new HashSet<>();
    private ArenaRegion region;

    public CuboidRegion() {
        super("cuboid");
    }

    @Override
    public String version() {
        return PVPArena.instance.getDescription().getVersion();
    }

    /**
     * sanitize a pair of locations
     *
     * @param lMin the minimum point
     * @param lMax the maximum point
     * @return a recalculated pair of locations
     */
    private PABlockLocation[] sanityCheck(final PABlockLocation lMin,
                                          final PABlockLocation lMax) {
        final boolean x = lMin.getX() > lMax.getX();
        final boolean y = lMin.getY() > lMax.getY();
        final boolean z = lMin.getZ() > lMax.getZ();

        if (!(x | y | z)) {
            return new PABlockLocation[]{lMin, lMax};
        }
        final PABlockLocation l1 = new PABlockLocation(lMin.getWorldName(), x ? lMax.getX()
                : lMin.getX(), y ? lMax.getY() : lMin.getY(), z ? lMax.getZ()
                : lMin.getZ());
        final PABlockLocation l2 = new PABlockLocation(lMin.getWorldName(), x ? lMin.getX()
                : lMax.getX(), y ? lMin.getY() : lMax.getY(), z ? lMin.getZ()
                : lMax.getZ());

        if (l1.getX() == l2.getX()) {
            l2.setX(l2.getX()+1);
        }

        if (l1.getY() == l2.getY()) {
            l2.setY(l2.getY()+1);
        }

        if (l1.getZ() == l2.getZ()) {
            l2.setZ(l2.getZ()+1);
        }

        return new PABlockLocation[]{l1, l2};
    }

    @Override
    public boolean hasVolume() {
        return region != null &&
                region.locs[0] != null && region.locs[1] != null &&
                region.locs[0].getX() != region.locs[1].getX() &&
                region.locs[0].getY() != region.locs[1].getY() &&
                region.locs[0].getZ() != region.locs[1].getZ();
    }

    @Override
    public final void initialize(final ArenaRegion region) {
        this.region = region;
        final PABlockLocation[] sane = sanityCheck(region.locs[0], region.locs[1]);
        region.locs[0] = sane[0];
        region.locs[1] = sane[1];
    }

    @Override
    public boolean overlapsWith(final ArenaRegion paRegion) {
        if (!getMinimumLocation().getWorldName().equals(
                paRegion.getShape().getMinimumLocation().getWorldName())) {
            return false;
        }
        if (paRegion.getShape() instanceof CuboidRegion) {
            // compare 2 cuboids
            if (getMinimumLocation().getX() > paRegion.locs[1].getX()
                    || getMinimumLocation().getY() > paRegion.locs[1].getY()
                    || getMinimumLocation().getZ() > paRegion.locs[1].getZ()) {
                return false;
            }
            return !(paRegion.locs[0].getX() > getMaximumLocation().getX()
                    || paRegion.locs[0].getY() > getMaximumLocation().getY()
                    || paRegion.locs[0].getZ() > getMaximumLocation().getZ());
        }
        if (paRegion.getShape() instanceof SphericRegion) {
            // we are cube and search for intersecting sphere

            final PABlockLocation thisCenter = getMaximumLocation().getMidpoint(getMinimumLocation());
            final PABlockLocation thatCenter = paRegion.locs[1]
                    .getMidpoint(paRegion.locs[0]);

            final Double thatRadius = paRegion.locs[0].getDistance(paRegion
                    .locs[1]) / 2;

            if (contains(thatCenter)) {
                return true; // the sphere is inside!
            }

            final PABlockLocation offset = thatCenter.pointTo(thisCenter, thatRadius);
            // offset is pointing from that to this

            return this.contains(offset);
        }
        if (paRegion.getShape() instanceof CylindricRegion) {
            // we are cube and search for intersecting cylinder

            final PABlockLocation thisCenter = getMaximumLocation().getMidpoint(
                    getMinimumLocation());
            final PABlockLocation thatCenter = paRegion.locs[1]
                    .getMidpoint(paRegion.locs[0]);

            if (getMaximumLocation().getY() < paRegion.locs[0].getY()) {
                return false;
            }
            if (getMinimumLocation().getY() > paRegion.locs[1].getY()) {
                return false;
            }

            thisCenter.setY(thatCenter.getY());

            if (contains(thatCenter)) {
                return true; // the sphere is inside!
            }

            final Double thatRadius = paRegion.locs[0].getDistance(paRegion
                    .locs[1]) / 2;

            final PABlockLocation offset = thatCenter.pointTo(thisCenter, thatRadius);
            // offset is pointing from that to this

            return this.contains(offset);
        }
        PVPArena.instance.getLogger()
                .warning(
                        "Region Shape not supported: "
                                + paRegion.getShape().getName());
        return false;
    }

    @Override
    public void showBorder(final Player player) {

        final Location min = getMinimumLocation().toLocation();
        final Location max = getMaximumLocation().toLocation();
        final World w = Bukkit.getWorld(region.getWorldName());

        border.clear();

        // move along exclusive x, create miny+maxy+minz+maxz
        for (int x = min.getBlockX() + 1; x < max.getBlockX(); x++) {
            border.add(new Location(w, x, min.getBlockY(), min.getBlockZ())
                    .getBlock());
            border.add(new Location(w, x, min.getBlockY(), max.getBlockZ())
                    .getBlock());
            border.add(new Location(w, x, max.getBlockY(), min.getBlockZ())
                    .getBlock());
            border.add(new Location(w, x, max.getBlockY(), max.getBlockZ())
                    .getBlock());
        }
        // move along exclusive y, create minx+maxx+minz+maxz
        for (int y = min.getBlockY() + 1; y < max.getBlockY(); y++) {
            border.add(new Location(w, min.getBlockX(), y, min.getBlockZ())
                    .getBlock());
            border.add(new Location(w, min.getBlockX(), y, max.getBlockZ())
                    .getBlock());
            border.add(new Location(w, max.getBlockX(), y, min.getBlockZ())
                    .getBlock());
            border.add(new Location(w, max.getBlockX(), y, max.getBlockZ())
                    .getBlock());
        }
        // move along inclusive z, create minx+maxx+miny+maxy
        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
            border.add(new Location(w, min.getBlockX(), min.getBlockY(), z)
                    .getBlock());
            border.add(new Location(w, min.getBlockX(), max.getBlockY(), z)
                    .getBlock());
            border.add(new Location(w, max.getBlockX(), min.getBlockY(), z)
                    .getBlock());
            border.add(new Location(w, max.getBlockX(), max.getBlockY(), z)
                    .getBlock());
        }

        for (final Block b : border) {
            if (!region.isInNoWoolSet(b)) {
                player.sendBlockChange(b.getLocation(), Material.WHITE_WOOL.createBlockData());
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
                new Runnable() {

                    @Override
                    public void run() {
                        for (final Block b : border) {
                            player.sendBlockChange(b.getLocation(),
                                    b.getType().createBlockData());
                        }
                        border.clear();
                    }

                }, 100L);
    }

    @Override
    public boolean contains(final PABlockLocation loc) {
        if (getMinimumLocation() == null || getMaximumLocation() == null
                || loc == null || !loc.getWorldName().equals(region.getWorldName())) {
            return false; // no arena, no container or not in the same world
        }
        return loc.isInAABB(getMinimumLocation(), getMaximumLocation());
    }

    @Override
    public PABlockLocation getCenter() {
        return getMinimumLocation().getMidpoint(getMaximumLocation());
    }

    @Override
    public List<PABlockLocation> getContainBlockCheckList() {
        final List<PABlockLocation> result = new ArrayList<>();

        result.add(region.locs[0]); // == 0

        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                region.locs[1].getX(),
                region.locs[0].getY(),
                region.locs[0].getZ())); // == 1
        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                region.locs[0].getX(),
                region.locs[1].getY(),
                region.locs[0].getZ())); // == 2
        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                region.locs[1].getX(),
                region.locs[1].getY(),
                region.locs[0].getZ())); // == 3
        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                region.locs[0].getX(),
                region.locs[1].getY(),
                region.locs[0].getZ())); // == 4
        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                region.locs[1].getX(),
                region.locs[0].getY(),
                region.locs[1].getZ())); // == 5
        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                region.locs[0].getX(),
                region.locs[1].getY(),
                region.locs[1].getZ())); // == 6

        result.add(region.locs[1]); // == 7
/*
        getRegion().getArena().getDebugger().i("CUBOID blockCheckList");

        for (PABlockLocation block : result) {
            getRegion().getArena().getDebugger().i(block.toString());
        }*/

        return result;
    }

    @Override
    public PABlockLocation getMaximumLocation() {
        return region.locs[1];
    }

    @Override
    public PABlockLocation getMinimumLocation() {
        return region.locs[0];
    }

    ArenaRegion getRegion() {
        return region;
    }

    @Override
    public boolean tooFarAway(final int joinRange, final Location location) {
        final PABlockLocation reach = new PABlockLocation(location).pointTo(
                getCenter(), (double) joinRange);

        return contains(reach);
    }

    @Override
    public void move(final BlockFace direction, final int value) {
        final int diffX = direction.getModX();
        final int diffY = direction.getModY();
        final int diffZ = direction.getModZ();

        if (diffX == 0 && diffY == 0 && diffZ == 0) {
            return;
        }
        region.locs[0] = new PABlockLocation(region.locs[0].toLocation().add(diffX * value, diffY * value, diffZ * value));
        region.locs[1] = new PABlockLocation(region.locs[1].toLocation().add(diffX * value, diffY * value, diffZ * value));
    }

    @Override
    public void extend(final BlockFace direction, final int value) {
        final int diffX = direction.getModX();
        final int diffY = direction.getModY();
        final int diffZ = direction.getModZ();

        if (diffX == 0 && diffY == 0 && diffZ == 0) {
            return;
        }

        if (diffX > 0) {
            region.locs[1] = new PABlockLocation(region.locs[1].toLocation().add(diffX * value, 0, 0));
        } else if (diffX < 0) {
            region.locs[0] = new PABlockLocation(region.locs[0].toLocation().subtract(diffX * value, 0, 0));
        }

        if (diffY > 0) {
            region.locs[1] = new PABlockLocation(region.locs[1].toLocation().add(0, diffY * value, 0));
        } else if (diffY < 0) {
            region.locs[0] = new PABlockLocation(region.locs[0].toLocation().subtract(0, diffY * value, 0));
        }

        if (diffZ > 0) {
            region.locs[1] = new PABlockLocation(region.locs[1].toLocation().add(0, 0, diffZ * value));
        } else if (diffZ < 0) {
            region.locs[0] = new PABlockLocation(region.locs[0].toLocation().subtract(0, 0, diffZ * value));
        }

    }
}
