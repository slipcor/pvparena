package net.slipcor.pvparena.regions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.neworder.ArenaRegionShape;

/**
 * <pre>Arena Region Shape class "spheric"</pre>
 * 
 * Defines a spheric region, including overlap checks and contain checks 
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class SphericRegion extends ArenaRegionShape {
	public SphericRegion() {
		super("sheric");
	}
	
	public SphericRegion(Arena arena, String name, PABlockLocation[] locs) {
		super(arena, name, locs, "spheric");
		db = new Debug(201);
	}

	@Override
	public String version() {
		return "v0.9.0.0";
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
	private PABlockLocation[] sanityCheck(PABlockLocation lMin, PABlockLocation lMax) {
		boolean x = (lMin.getX() > lMax.getX());
		boolean y = (lMin.getY() > lMax.getY());
		boolean z = (lMin.getZ() > lMax.getZ());

		if (!(x | y | z)) {
			return new PABlockLocation[] { lMin, lMax };
		}
		PABlockLocation l1;
		PABlockLocation l2;

		l1 = new PABlockLocation(lMin.getWorldName(), x ? lMax.getX()
				: lMin.getX(), y ? lMax.getY() : lMin.getY(),
				z ? lMax.getZ() : lMin.getZ());
		l2 = new PABlockLocation(lMin.getWorldName(), x ? lMin.getX()
				: lMax.getX(), y ? lMin.getY() : lMax.getY(),
				z ? lMin.getZ() : lMax.getZ());
		return new PABlockLocation[] { l1, l2 };
	}

	public void initialize() {
		PABlockLocation[] sane = sanityCheck(getLocs()[0], getLocs()[1]);
		getLocs()[0] = sane[0];
		getLocs()[1] = sane[1];
	}

	@Override
	public boolean overlapsWith(ArenaRegionShape paRegion) {
		if (paRegion.getShape().equals(RegionShape.CUBOID)) {
			// compare 2 cuboids
			if (locs[0].getX() > paRegion.getLocs()[1].getX()
					|| locs[0].getY() > paRegion.getLocs()[1].getY()
					|| locs[0].getZ() > paRegion.getLocs()[1].getZ()) {
				return false;
			}
			if (paRegion.getLocs()[0].getX() > locs[1].getX()
					|| paRegion.getLocs()[0].getY() > locs[1].getY()
					|| paRegion.getLocs()[0].getZ() > locs[1].getZ()) {
				return false;
			}
			return true;
		} else if (paRegion.getShape().equals(RegionShape.SPHERIC)) {
			// we are cube and search for intersecting sphere

			PABlockLocation thisCenter = this.locs[1].getMidpoint(this.locs[0]);
			PABlockLocation thatCenter = paRegion.getLocs()[1].getMidpoint(paRegion.getLocs()[0]);
			
			Double thatRadius = paRegion.getLocs()[0].getDistance(paRegion.getLocs()[1])/2;

			if (contains(thatCenter)) {
				return true; // the sphere is inside!
			}

			PABlockLocation offset = thatCenter.pointTo(thisCenter, thatRadius);
			// offset is pointing from that to this
			
			return this.contains(offset);
		} else if (paRegion.getShape().equals(RegionShape.CYLINDRIC)) {
			// we are cube and search for intersecting cylinder

			PABlockLocation thisCenter = this.locs[1].getMidpoint(this.locs[0]);
			PABlockLocation thatCenter = paRegion.getLocs()[1].getMidpoint(paRegion.getLocs()[0]);
			
			if (locs[1].getY() < paRegion.getLocs()[0].getY()) {
				return false;
			}
			if (locs[0].getY() > paRegion.getLocs()[1].getY()) {
				return false;
			}
			
			thisCenter.setY(thatCenter.getY());

			if (contains(thatCenter)) {
				return true; // the sphere is inside!
			}

			Double thatRadius = paRegion.getLocs()[0].getDistance(paRegion.getLocs()[1])/2;

			PABlockLocation offset = thatCenter.pointTo(thisCenter, thatRadius);
			// offset is pointing from that to this
			
			return this.contains(offset);
		} else {
			System.out.print("Region Shape not supported: " + paRegion.getShape().name());
		}
		return false;
	}

	@Override
	public void showBorder(Player player) {
		/*
		// move along exclusive x, create miny+maxy+minz+maxz
		for (int x = min.getBlockX() + 1; x < max.getBlockX(); x++) {
			player.sendBlockChange(new Location(world, x, min.getBlockY(),
					min.getBlockZ()), Material.WOOL, (byte) 0);
			player.sendBlockChange(new Location(world, x, min.getBlockY(),
					max.getBlockZ()), Material.WOOL, (byte) 0);
			player.sendBlockChange(new Location(world, x, max.getBlockY(),
					min.getBlockZ()), Material.WOOL, (byte) 0);
			player.sendBlockChange(new Location(world, x, max.getBlockY(),
					max.getBlockZ()), Material.WOOL, (byte) 0);
		}
		// move along exclusive y, create minx+maxx+minz+maxz
		for (int y = min.getBlockY() + 1; y < max.getBlockY(); y++) {
			player.sendBlockChange(new Location(world, min.getBlockX(), y,
					min.getBlockZ()), Material.WOOL, (byte) 0);
			player.sendBlockChange(new Location(world, min.getBlockX(), y,
					max.getBlockZ()), Material.WOOL, (byte) 0);
			player.sendBlockChange(new Location(world, max.getBlockX(), y,
					min.getBlockZ()), Material.WOOL, (byte) 0);
			player.sendBlockChange(new Location(world, max.getBlockX(), y,
					max.getBlockZ()), Material.WOOL, (byte) 0);
		}
		// move along inclusive z, create minx+maxx+miny+maxy
		for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
			player.sendBlockChange(
					new Location(world, min.getBlockX(), min.getBlockY(), z),
					Material.WOOL, (byte) 0);
			player.sendBlockChange(
					new Location(world, min.getBlockX(), max.getBlockY(), z),
					Material.WOOL, (byte) 0);
			player.sendBlockChange(
					new Location(world, max.getBlockX(), min.getBlockY(), z),
					Material.WOOL, (byte) 0);
			player.sendBlockChange(
					new Location(world, max.getBlockX(), max.getBlockY(), z),
					Material.WOOL, (byte) 0);
		}
		*/
	}

	@Override
	public boolean contains(PABlockLocation loc) {
		if (this.getLocs()[0] == null || this.getLocs()[1] == null || loc == null
				|| !loc.getWorldName().equals(world))
			return false; // no arena, no container or not in the same world
		return loc.isInAABB(getLocs()[0],getLocs()[1]);
	}

	@Override
	public PABlockLocation getCenter() {
		return locs[0].getMidpoint(locs[1]);
	}

	@Override
	public PABlockLocation getMaximumLocation() {
		return getLocs()[0];
	}

	@Override
	public PABlockLocation getMinimumLocation() {
		return getLocs()[1];
	}

	@Override
	public boolean tooFarAway(int joinRange, Location location) {
		PABlockLocation reach = (new PABlockLocation(location)).pointTo(getCenter(), (double) joinRange);
		
		return contains(reach);
	}
}
