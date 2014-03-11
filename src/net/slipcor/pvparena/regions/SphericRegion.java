package net.slipcor.pvparena.regions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegionShape;

/**
 * <pre>
 * Arena Region Shape class "spheric"
 * </pre>
 * 
 * Defines a spheric region, including overlap checks and contain checks
 * 
 * @author slipcor
 */

public class SphericRegion extends ArenaRegionShape {

	private final Set<Block> border = new HashSet<Block>();
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
	 * @param lMin
	 *            the minimum point
	 * @param lMax
	 *            the maximum point
	 * @return a recalculated pair of locations
	 */
	private PABlockLocation[] sanityCheck(final PABlockLocation lMin,
			final PABlockLocation lMax) {
		boolean x = (lMin.getX() > lMax.getX());
		boolean y = (lMin.getY() > lMax.getY());
		boolean z = (lMin.getZ() > lMax.getZ());

		if (!(x | y | z)) {
			return new PABlockLocation[] { lMin, lMax };
		}
		final PABlockLocation l1;
		final PABlockLocation l2;

		l1 = new PABlockLocation(lMin.getWorldName(), x ? lMax.getX()
				: lMin.getX(), y ? lMax.getY() : lMin.getY(), z ? lMax.getZ()
				: lMin.getZ());
		l2 = new PABlockLocation(lMin.getWorldName(), x ? lMin.getX()
				: lMax.getX(), y ? lMin.getY() : lMax.getY(), z ? lMin.getZ()
				: lMax.getZ());
		return new PABlockLocation[] { l1, l2 };
	}

	@Override
	public void initialize(ArenaRegion region) {
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
			if (getRegion().locs[0].getX() > paRegion.locs[1].getX()
					|| getRegion().locs[0].getY() > paRegion.locs[1].getY()
					|| getRegion().locs[0].getZ() > paRegion.locs[1].getZ()) {
				return false;
			}
			if (paRegion.locs[0].getX() > getRegion().locs[1].getX()
					|| paRegion.locs[0].getY() > getRegion().locs[1].getY()
					|| paRegion.locs[0].getZ() > getRegion().locs[1].getZ()) {
				return false;
			}
			return true;
		} else if (paRegion.getShape() instanceof SphericRegion) {
			// we are cube and search for intersecting sphere

			final PABlockLocation thisCenter = getRegion().locs[1].getMidpoint(
					getRegion().locs[0]);
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
		} else if (paRegion.getShape() instanceof CylindricRegion) {
			// we are cube and search for intersecting cylinder

			final PABlockLocation thisCenter = getRegion().locs[1].getMidpoint(
					getRegion().locs[0]);
			final PABlockLocation thatCenter = paRegion.locs[1]
					.getMidpoint(paRegion.locs[0]);

			if (getRegion().locs[1].getY() < paRegion.locs[0].getY()) {
				return false;
			}
			if (getRegion().locs[0].getY() > paRegion.locs[1].getY()) {
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
		} else {
			PVPArena.instance.getLogger()
					.warning(
							"Region Shape not supported: "
									+ paRegion.getShape().getName());
		}
		return false;
	}

	@Override
	public void showBorder(final Player player) {
		final PABlockLocation center = new PABlockLocation(getCenter().toLocation());

		final World world = Bukkit.getWorld(getRegion().getWorldName());

		border.clear();

		final PABlockLocation minimum = new PABlockLocation(getRegion().locs[1].toLocation());
		minimum.setY(getRegion().locs[0].getY());

		final Double radius = getRadius();

		final Double radiusSquared = radius * radius;

		// ------------------------------
		// ---------- Y CIRCLE ----------
		// ------------------------------

		for (int x = 0; x <= Math.ceil(radius + 1 / 2); x++) {
			int z = (int) Math.abs(Math.sqrt(radiusSquared - (x * x)));

			border.add(new Location(world, center.getX() + x, center.getY(),
					center.getZ() + z).getBlock());
			border.add(new Location(world, center.getX() - x, center.getY(),
					center.getZ() + z).getBlock());
			border.add(new Location(world, center.getX() + x, center.getY(),
					center.getZ() - z).getBlock());
			border.add(new Location(world, center.getX() - x, center.getY(),
					center.getZ() - z).getBlock());
		}

		for (int z = 0; z <= Math.ceil(radius + 1 / 2); z++) {
			int x = (int) Math.abs(Math.sqrt(radiusSquared - (z * z)));

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

		for (int y = 0; y <= Math.ceil(radius + 1 / 2); y++) {
			int x = (int) Math.abs(Math.sqrt(radiusSquared - (y * y)));

			border.add(new Location(world, center.getX() + x, center.getY() + y,
					center.getZ()).getBlock());
			border.add(new Location(world, center.getX() - x, center.getY() + y,
					center.getZ()).getBlock());
			border.add(new Location(world, center.getX() + x, center.getY() - y,
					center.getZ()).getBlock());
			border.add(new Location(world, center.getX() - x, center.getY() - y,
					center.getZ()).getBlock());

		}

		for (int x = 0; x <= Math.ceil(radius + 1 / 2); x++) {
			int y = (int) Math.abs(Math.sqrt(radiusSquared - (x * x)));

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

		for (int y = 0; y <= Math.ceil(radius + 1 / 2); y++) {
			int z = (int) Math.abs(Math.sqrt(radiusSquared - (y * y)));

			border.add(new Location(world, center.getX(), center.getY() + y,
					center.getZ() + z).getBlock());
			border.add(new Location(world, center.getX(), center.getY() - y,
					center.getZ() + z).getBlock());
			border.add(new Location(world, center.getX(), center.getY() + y,
					center.getZ() - z).getBlock());
			border.add(new Location(world, center.getX(), center.getY() - y,
					center.getZ() - z).getBlock());
		}

		for (int z = 0; z <= Math.ceil(radius + 1 / 2); z++) {
			int y = (int) Math.abs(Math.sqrt(radiusSquared - (z * z)));

			border.add(new Location(world, center.getX(), center.getY() + y,
					center.getZ() + z).getBlock());
			border.add(new Location(world, center.getX(), center.getY() - y,
					center.getZ() + z).getBlock());
			border.add(new Location(world, center.getX(), center.getY() + y,
					center.getZ() - z).getBlock());
			border.add(new Location(world, center.getX(), center.getY() - y,
					center.getZ() - z).getBlock());
		}

		for (Block b : border) {
			if (!getRegion().isInNoWoolSet(b)) {
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

	@Override
	public boolean contains(final PABlockLocation loc) {
		if (getRegion().locs[0] == null || getRegion().locs[1] == null
				|| loc == null || !loc.getWorldName().equals(getRegion().getWorldName())) {
			return false; // no arena, no container or not in the same world
		}
		return loc.getDistanceSquared(this.getCenter()) <= getRadiusSquared();
	}

	private Double getRadius() {
		return getRegion().locs[0].getDistance(getRegion().locs[1]) / 2;
	}

	private Double getRadiusSquared() {
		return getRegion().locs[0].getDistanceSquared(getRegion().locs[1]) / 4;
	}
	
	private ArenaRegion getRegion() {
		return region;
	}

	@Override
	public PABlockLocation getCenter() {
		return getRegion().locs[0].getMidpoint(getRegion().locs[1]);
	}

	@Override
	public PABlockLocation getMaximumLocation() {
		int r = (int) Math.round(getRadius());
		PABlockLocation result = new PABlockLocation(getCenter().toLocation());
		result.setX(result.getX() + r);
		result.setY(result.getY() + r);
		result.setZ(result.getZ() + r);
		return result;
	}

	@Override
	public PABlockLocation getMinimumLocation() {
		int r = (int) Math.round(getRadius());
		PABlockLocation result = new PABlockLocation(getCenter().toLocation());
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
	public void move(BlockFace direction, int value) {
		final int diffX = direction.getModX();
		final int diffY = direction.getModY();
		final int diffZ = direction.getModZ();
		
		if (diffX == 0 && diffY == 0 && diffZ == 0) {
			return;
		}
		region.locs[0] = new PABlockLocation(region.locs[0].toLocation().add(diffX*value, diffY*value, diffZ*value));
		region.locs[1] = new PABlockLocation(region.locs[1].toLocation().add(diffX*value, diffY*value, diffZ*value));
	}

	@Override
	public void extend(BlockFace direction, int value) {
		final int diffX = direction.getModX();
		final int diffY = direction.getModY();
		final int diffZ = direction.getModZ();
		
		if (diffX == 0 && diffY == 0 && diffZ == 0) {
			return;
		}
		region.locs[0] = new PABlockLocation(region.locs[0].toLocation().subtract(diffX*value, diffY*value, diffZ*value));
		region.locs[1] = new PABlockLocation(region.locs[1].toLocation().add(diffX*value, diffY*value, diffZ*value));
	}
}
