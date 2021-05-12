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
 * Arena Region Shape class "spheric"
 * </pre>
 * <p/>
 * Defines a spheric region, including overlap checks and contain checks
 *
 * @author slipcor
 */

public class SphericRegion extends ArenaRegionShape {

    private final Set<Block> border = new HashSet<>();
    private ArenaRegion region;

    public SphericRegion() {
        super("spheric");
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
        return new PABlockLocation[]{l1, l2};
    }

    @Override
    public boolean hasVolume() {
        return getRadius() > 1;
    }

    @Override
    public void initialize(final ArenaRegion region) {
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
            if (region.locs[0].getX() > paRegion.locs[1].getX()
                    || region.locs[0].getY() > paRegion.locs[1].getY()
                    || region.locs[0].getZ() > paRegion.locs[1].getZ()) {
                return false;
            }
            return !(paRegion.locs[0].getX() > region.locs[1].getX()
                    || paRegion.locs[0].getY() > region.locs[1].getY()
                    || paRegion.locs[0].getZ() > region.locs[1].getZ());
        }
        if (paRegion.getShape() instanceof SphericRegion) {
            // we are cube and search for intersecting sphere

            final PABlockLocation thisCenter = region.locs[1].getMidpoint(
                    region.locs[0]);
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

            final PABlockLocation thisCenter = region.locs[1].getMidpoint(
                    region.locs[0]);
            final PABlockLocation thatCenter = paRegion.locs[1]
                    .getMidpoint(paRegion.locs[0]);

            if (region.locs[1].getY() < paRegion.locs[0].getY()) {
                return false;
            }
            if (region.locs[0].getY() > paRegion.locs[1].getY()) {
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
        final PABlockLocation center = new PABlockLocation(getCenter().toLocation());

        final World world = Bukkit.getWorld(region.getWorldName());

        border.clear();

        final PABlockLocation minimum = new PABlockLocation(region.locs[1].toLocation());
        minimum.setY(region.locs[0].getY());

        final Double radius = getRadius();

        final Double radiusSquared = radius * radius;

        // ------------------------------
        // ---------- Y CIRCLE ----------
        // ------------------------------

        for (int x = 0; x <= Math.ceil(radius + 1d / 2); x++) {
            final int z = (int) Math.abs(Math.sqrt(radiusSquared - x * x));

            border.add(new Location(world, center.getX() + x, center.getY(),
                    center.getZ() + z).getBlock());
            border.add(new Location(world, center.getX() - x, center.getY(),
                    center.getZ() + z).getBlock());
            border.add(new Location(world, center.getX() + x, center.getY(),
                    center.getZ() - z).getBlock());
            border.add(new Location(world, center.getX() - x, center.getY(),
                    center.getZ() - z).getBlock());
        }

        for (int z = 0; z <= Math.ceil(radius + 1d / 2); z++) {
            final int x = (int) Math.abs(Math.sqrt(radiusSquared - z * z));

            border.add(new Location(world, center.getX() + x, center.getY(),
                    center.getZ() + z).getBlock());
            border.add(new Location(world, center.getX() - x, center.getY(),
                    center.getZ() + z).getBlock());
            border.add(new Location(world, center.getX() + x, center.getY(),
                    center.getZ() - z).getBlock());
            border.add(new Location(world, center.getX() - x, center.getY(),
                    center.getZ() - z).getBlock());
        }

        // ------------------------------
        // ---------- Z CIRCLE ----------
        // ------------------------------

        for (int y = 0; y <= Math.ceil(radius + 1d / 2); y++) {
            final int x = (int) Math.abs(Math.sqrt(radiusSquared - y * y));

            border.add(new Location(world, center.getX() + x, center.getY() + y,
                    center.getZ()).getBlock());
            border.add(new Location(world, center.getX() - x, center.getY() + y,
                    center.getZ()).getBlock());
            border.add(new Location(world, center.getX() + x, center.getY() - y,
                    center.getZ()).getBlock());
            border.add(new Location(world, center.getX() - x, center.getY() - y,
                    center.getZ()).getBlock());

        }

        for (int x = 0; x <= Math.ceil(radius + 1d / 2); x++) {
            final int y = (int) Math.abs(Math.sqrt(radiusSquared - x * x));

            border.add(new Location(world, center.getX() + x, center.getY() + y,
                    center.getZ()).getBlock());
            border.add(new Location(world, center.getX() - x, center.getY() + y,
                    center.getZ()).getBlock());
            border.add(new Location(world, center.getX() + x, center.getY() - y,
                    center.getZ()).getBlock());
            border.add(new Location(world, center.getX() - x, center.getY() - y,
                    center.getZ()).getBlock());

        }

        // ------------------------------
        // ---------- X CIRCLE ----------
        // ------------------------------

        for (int y = 0; y <= Math.ceil(radius + 1d / 2); y++) {
            final int z = (int) Math.abs(Math.sqrt(radiusSquared - y * y));

            border.add(new Location(world, center.getX(), center.getY() + y,
                    center.getZ() + z).getBlock());
            border.add(new Location(world, center.getX(), center.getY() - y,
                    center.getZ() + z).getBlock());
            border.add(new Location(world, center.getX(), center.getY() + y,
                    center.getZ() - z).getBlock());
            border.add(new Location(world, center.getX(), center.getY() - y,
                    center.getZ() - z).getBlock());
        }

        for (int z = 0; z <= Math.ceil(radius + 1d / 2); z++) {
            final int y = (int) Math.abs(Math.sqrt(radiusSquared - z * z));

            border.add(new Location(world, center.getX(), center.getY() + y,
                    center.getZ() + z).getBlock());
            border.add(new Location(world, center.getX(), center.getY() - y,
                    center.getZ() + z).getBlock());
            border.add(new Location(world, center.getX(), center.getY() + y,
                    center.getZ() - z).getBlock());
            border.add(new Location(world, center.getX(), center.getY() - y,
                    center.getZ() - z).getBlock());
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
        if (region.locs[0] == null || region.locs[1] == null
                || loc == null || !loc.getWorldName().equals(region.getWorldName())) {
            return false; // no arena, no container or not in the same world
        }
        return loc.getDistanceSquared(getCenter()) <= getRadiusSquared();
    }

    private Double getRadius() {
        return region.locs[0].getDistance(region.locs[1]) / 2;
    }

    private Double getRadiusSquared() {
        return region.locs[0].getDistanceSquared(region.locs[1]) / 4;
    }

    private ArenaRegion getRegion() {
        return region;
    }

    @Override
    public PABlockLocation getCenter() {
        return region.locs[0].getMidpoint(region.locs[1]);
    }

    @Override
    public List<PABlockLocation> getContainBlockCheckList() {
        List<PABlockLocation> result = new ArrayList<>();

        final PABlockLocation center = getCenter();
        final int radius = (int) Math.floor(getRadius());

        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                center.getX()-radius,
                center.getY(),
                center.getZ())); // == 0
        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                center.getX()+radius,
                center.getY(),
                center.getZ())); // == 1
        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                center.getX(),
                center.getY()-radius,
                center.getZ())); // == 2
        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                center.getX(),
                center.getY()+radius,
                center.getZ())); // == 3
        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                center.getX(),
                center.getY(),
                center.getZ()-radius)); // == 4
        result.add(new PABlockLocation(region.locs[0].getWorldName(),
                center.getX(),
                center.getY(),
                center.getZ()+radius)); // == 5
/*
        getRegion().getArena().getDebugger().i("SPHERIC blockCheckList");

        for (PABlockLocation block : result) {
            getRegion().getArena().getDebugger().i(block.toString());
        }*/

        return result;
    }

    @Override
    public PABlockLocation getMaximumLocation() {
        final int r = (int) Math.round(getRadius());
        final PABlockLocation result = new PABlockLocation(getCenter().toLocation());
        result.setX(result.getX() + r);
        result.setY(result.getY() + r);
        result.setZ(result.getZ() + r);
        return result;
    }

    @Override
    public PABlockLocation getMinimumLocation() {
        final int r = (int) Math.round(getRadius());
        final PABlockLocation result = new PABlockLocation(getCenter().toLocation());
        result.setX(result.getX() - r);
        result.setY(result.getY() - r);
        result.setZ(result.getZ() - r);
        return result;
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
        region.locs[0] = new PABlockLocation(region.locs[0].toLocation().subtract(diffX * value, diffY * value, diffZ * value));
        region.locs[1] = new PABlockLocation(region.locs[1].toLocation().add(diffX * value, diffY * value, diffZ * value));
    }
}
