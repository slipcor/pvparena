package net.slipcor.pvparena.regions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaRegionShape;

/**
 * <pre>
 * Arena Region Shape class "cylindric"
 * </pre>
 * 
 * Defines a cylindric region, including overlap checks and contain checks
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class CylindricRegion extends ArenaRegionShape {

	private final Set<Block> border = new HashSet<Block>();

	public CylindricRegion() {
		super("cylindric");
		shape = RegionShape.CYLINDRIC;
		initialize();
	}

	public CylindricRegion(final Arena arena, final String name, final PABlockLocation[] locs) {
		super(arena, name, locs, "cylindric");
		debug = new Debug(201);
		initialize();
	}

	@Override
	public String version() {
		return "v0.10.0.0";
	}

	/**
	 * sanitize a pair of locations
	 * 
	 * @param lMin
	 *            the minimum point
	 * @param lMax
	 *            the maximum point
	 * @return a recalculated pair of locations
	 */
	private PABlockLocation[] sanityCheck(final PABlockLocation lMin,
			final PABlockLocation lMax) {
		final boolean x = (lMin.getX() > lMax.getX());
		final boolean y = (lMin.getY() > lMax.getY());
		final boolean z = (lMin.getZ() > lMax.getZ());

		if (!(x | y | z)) {
			return new PABlockLocation[] { lMin, lMax };
		}
		final PABlockLocation l1 = new PABlockLocation(lMin.getWorldName(), x ? lMax.getX()
				: lMin.getX(), y ? lMax.getY() : lMin.getY(), z ? lMax.getZ()
				: lMin.getZ());
		final PABlockLocation l2 = new PABlockLocation(lMin.getWorldName(), x ? lMin.getX()
				: lMax.getX(), y ? lMin.getY() : lMax.getY(), z ? lMin.getZ()
				: lMax.getZ());

		return new PABlockLocation[] { l1, l2 };
	}

	public final void initialize() {
		final PABlockLocation[] sane = sanityCheck(getLocs()[0], getLocs()[1]);
		locs[0] = sane[0];
		locs[1] = sane[1];
	}

	@Override
	public boolean overlapsWith(final ArenaRegionShape paRegion) {
		if (paRegion.getShape().equals(RegionShape.CUBOID)) {
			// we are cylinder and search for intersecting cuboid

			final PABlockLocation thisCenter = this.locs[1].getMidpoint(this.locs[0]);
			final PABlockLocation thatCenter = paRegion.getLocs()[1]
					.getMidpoint(paRegion.getLocs()[0]);

			if (locs[1].getY() < paRegion.getLocs()[0].getY()) {
				return false;
			}
			if (locs[0].getY() > paRegion.getLocs()[1].getY()) {
				return false;
			}

			thisCenter.setY(thatCenter.getY());

			if (contains(thatCenter)) {
				return true; // the cube is inside!
			}

			final Double thisRadius = getLocs()[0].getDistance(getLocs()[1]) / 2;

			final PABlockLocation offset = thisCenter.pointTo(thatCenter, thisRadius);
			// offset is pointing from this to that

			return paRegion.contains(offset);

		} else if (paRegion.getShape().equals(RegionShape.SPHERIC)) {
			// we are cylinder and search for intersecting sphere

			final PABlockLocation thisCenter = this.locs[1].getMidpoint(this.locs[0]);
			final PABlockLocation thatCenter = paRegion.getLocs()[1]
					.getMidpoint(paRegion.getLocs()[0]);

			final Double thatRadius = paRegion.getLocs()[0].getDistance(paRegion
					.getLocs()[1]) / 2;

			if (contains(thatCenter)) {
				return true; // the sphere is inside!
			}

			final PABlockLocation offset = thatCenter.pointTo(thisCenter, thatRadius);
			// offset is pointing from that to this

			return this.contains(offset);
		} else if (paRegion.getShape().equals(RegionShape.CYLINDRIC)) {
			// we are cylinder and search for intersecting cylinder

			final PABlockLocation thisCenter = this.locs[1].getMidpoint(this.locs[0]);
			final PABlockLocation thatCenter = paRegion.getLocs()[1]
					.getMidpoint(paRegion.getLocs()[0]);

			if (locs[1].getY() < paRegion.getLocs()[0].getY()) {
				return false;
			}
			if (locs[0].getY() > paRegion.getLocs()[1].getY()) {
				return false;
			}

			thisCenter.setY(thatCenter.getY());

			if (contains(thatCenter)) {
				return true; // the cylinder is inside!
			}

			final Double thatRadius = paRegion.getLocs()[0].getDistance(paRegion
					.getLocs()[1]) / 2;
			final Double thisRadius = getLocs()[0].getDistance(getLocs()[1]) / 2;

			return thisCenter.getDistance(thisCenter) <= (thatRadius + thisRadius);
		} else {
			PVPArena.instance.getLogger()
					.warning(
							"Region Shape not supported: "
									+ paRegion.getShape().name());
		}
		return false;
	}

	@Override
	public void showBorder(final Player player) {

		final PABlockLocation lowercenter = new PABlockLocation(getCenter()
				.toLocation());
		final PABlockLocation center = new PABlockLocation(lowercenter.toLocation());
		final PABlockLocation uppercenter = new PABlockLocation(
				lowercenter.toLocation());

		lowercenter.setY(getMinimumLocation().getY());
		uppercenter.setY(getMaximumLocation().getY());

		final World world = Bukkit.getWorld(this.world);

		border.clear();

		final Double radius = getRadius();

		final Double radiusSquared = radius * radius;

		for (int x = 0; x <= Math.ceil(radius + 1 / 2); x++) {
			final int z = (int) Math.abs(Math.sqrt(radiusSquared - (x * x)));

			border.add(new Location(world, center.getX() + x, center.getY(),
					center.getZ() + z).getBlock());
			border.add(new Location(world, center.getX() - x, center.getY(),
					center.getZ() + z).getBlock());
			border.add(new Location(world, center.getX() + x, center.getY(),
					center.getZ() - z).getBlock());
			border.add(new Location(world, center.getX() - x, center.getY(),
					center.getZ() - z).getBlock());

			border.add(new Location(world, lowercenter.getX() + x, lowercenter
					.getY(), lowercenter.getZ() + z).getBlock());
			border.add(new Location(world, lowercenter.getX() - x, lowercenter
					.getY(), lowercenter.getZ() + z).getBlock());
			border.add(new Location(world, lowercenter.getX() + x, lowercenter
					.getY(), lowercenter.getZ() - z).getBlock());
			border.add(new Location(world, lowercenter.getX() - x, lowercenter
					.getY(), lowercenter.getZ() - z).getBlock());

			border.add(new Location(world, uppercenter.getX() + x, uppercenter
					.getY(), uppercenter.getZ() + z).getBlock());
			border.add(new Location(world, uppercenter.getX() - x, uppercenter
					.getY(), uppercenter.getZ() + z).getBlock());
			border.add(new Location(world, uppercenter.getX() + x, uppercenter
					.getY(), uppercenter.getZ() - z).getBlock());
			border.add(new Location(world, uppercenter.getX() - x, uppercenter
					.getY(), uppercenter.getZ() - z).getBlock());
		}
		for (int z = 0; z <= Math.ceil(radius + 1 / 2); z++) {
			final int x = (int) Math.abs(Math.sqrt(radiusSquared - (z * z)));

			border.add(new Location(world, center.getX() + x, center.getY(),
					center.getZ() + z).getBlock());
			border.add(new Location(world, center.getX() - x, center.getY(),
					center.getZ() + z).getBlock());
			border.add(new Location(world, center.getX() + x, center.getY(),
					center.getZ() - z).getBlock());
			border.add(new Location(world, center.getX() - x, center.getY(),
					center.getZ() - z).getBlock());

			border.add(new Location(world, lowercenter.getX() + x, lowercenter
					.getY(), lowercenter.getZ() + z).getBlock());
			border.add(new Location(world, lowercenter.getX() - x, lowercenter
					.getY(), lowercenter.getZ() + z).getBlock());
			border.add(new Location(world, lowercenter.getX() + x, lowercenter
					.getY(), lowercenter.getZ() - z).getBlock());
			border.add(new Location(world, lowercenter.getX() - x, lowercenter
					.getY(), lowercenter.getZ() - z).getBlock());

			border.add(new Location(world, uppercenter.getX() + x, uppercenter
					.getY(), uppercenter.getZ() + z).getBlock());
			border.add(new Location(world, uppercenter.getX() - x, uppercenter
					.getY(), uppercenter.getZ() + z).getBlock());
			border.add(new Location(world, uppercenter.getX() + x, uppercenter
					.getY(), uppercenter.getZ() - z).getBlock());
			border.add(new Location(world, uppercenter.getX() - x, uppercenter
					.getY(), uppercenter.getZ() - z).getBlock());
		}

		for (Block b : border) {
			if (!isInNoWoolSet(b)) {
				player.sendBlockChange(b.getLocation(), Material.WOOL, (byte) 0);
			}
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
				new Runnable() {

					@Override
					public void run() {
						for (Block b : border) {
							player.sendBlockChange(b.getLocation(),
									b.getTypeId(), b.getData());
						}
						border.clear();
					}

				}, 100L);
	}

	private Double getRadius() {
		return (double) ((getLocs()[1].getX() == getLocs()[0].getX()) ? ((getLocs()[1]
				.getZ() - getLocs()[0].getZ()) / 2)
				: ((getLocs()[1].getX() - getLocs()[0].getX()) / 2));
	}

	@Override
	public boolean contains(final PABlockLocation loc) {
		if (this.getLocs()[0] == null || this.getLocs()[1] == null
				|| loc == null || !loc.getWorldName().equals(world)) {
			return false; // no arena, no container or not in the same world
		}
		if (loc.getY() > this.getLocs()[1].getY()) {
			return false;
		}
		if (loc.getY() < this.getLocs()[0].getY()) {
			return false;
		}

		final PABlockLocation thisCenter = this.locs[1].getMidpoint(this.locs[0]);

		final PABlockLocation a = new PABlockLocation(this.locs[1].toLocation());
		a.setY(locs[0].getY());

		final Double thisRadius = getLocs()[0].getDistance(a) / 2;
		thisCenter.setY(loc.getY());

		return loc.getDistance(thisCenter) <= thisRadius;
	}

	@Override
	public PABlockLocation getCenter() {
		return new PABlockLocation(locs[0].getMidpoint(locs[1]).toLocation());
	}

	@Override
	public PABlockLocation getMaximumLocation() {
		final Double thisRadius = getRadius();
		final PABlockLocation result = getCenter();
		result.setX((int) (result.getX() + thisRadius));
		result.setY(getLocs()[1].getY());
		result.setZ((int) (result.getZ() + thisRadius));
		return result;
	}

	@Override
	public PABlockLocation getMinimumLocation() {
		final Double thisRadius = getRadius();
		final PABlockLocation result = getCenter();
		result.setX((int) (result.getX() - thisRadius));
		result.setY(getLocs()[0].getY());
		result.setZ((int) (result.getZ() - thisRadius));
		return result;
	}

	@Override
	public boolean tooFarAway(final int joinRange, final Location location) {
		final PABlockLocation cLoc = getCenter();
		cLoc.setY(location.getBlockY());
		final PABlockLocation reach = new PABlockLocation(location).pointTo(cLoc,
				(double) joinRange);

		return contains(reach);
	}
}
