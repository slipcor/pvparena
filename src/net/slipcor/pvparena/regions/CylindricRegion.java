package net.slipcor.pvparena.regions;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaRegionShape;

/**
 * <pre>Arena Region Shape class "cylindric"</pre>
 * 
 * Defines a cylindric region, including overlap checks and contain checks 
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class CylindricRegion extends ArenaRegionShape {
	public CylindricRegion() {
		super("cylindric");
	}
	
	public CylindricRegion(Arena arena, String name, PABlockLocation[] locs) {
		super(arena, name, locs, "cylindric");
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
			// we are cylinder and search for intersecting cuboid

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
				return true; // the cube is inside!
			}

			Double thisRadius = getLocs()[0].getDistance(getLocs()[1])/2;

			PABlockLocation offset = thisCenter.pointTo(thatCenter, thisRadius);
			// offset is pointing from this to that
			
			return paRegion.contains(offset);
			
		} else if (paRegion.getShape().equals(RegionShape.SPHERIC)) {
			// we are cylinder and search for intersecting sphere

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
			// we are cylinder and search for intersecting cylinder

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
				return true; // the cylinder is inside!
			}

			Double thatRadius = paRegion.getLocs()[0].getDistance(paRegion.getLocs()[1])/2;
			Double thisRadius = getLocs()[0].getDistance(getLocs()[1])/2;

			return thisCenter.getDistance(thisCenter) <= (thatRadius+thisRadius);
		} else {
			PVPArena.instance.getLogger().warning("Region Shape not supported: " + paRegion.getShape().name());
		}
		return false;
	}

	@Override
	public void showBorder(Player player) {
		/*no clue
		*/
	}

	@Override
	public boolean contains(PABlockLocation loc) {
		if (this.getLocs()[0] == null || this.getLocs()[1] == null || loc == null
				|| !loc.getWorldName().equals(world))
			return false; // no arena, no container or not in the same world

		if (loc.getY() > this.getLocs()[1].getY()) {
			return false;
		}
		if (loc.getY() < this.getLocs()[0].getY()) {
			return false;
		}

		PABlockLocation thisCenter = this.locs[1].getMidpoint(this.locs[0]);
		Double thisRadius = getLocs()[0].getDistance(getLocs()[1])/2;
		thisCenter.setY(loc.getY());
		
		return loc.getDistance(thisCenter) <= thisRadius;
	}

	@Override
	public PABlockLocation getCenter() {
		return locs[0].getMidpoint(locs[1]);
	}

	@Override
	public PABlockLocation getMaximumLocation() {
		Double thisRadius = getLocs()[0].getDistance(getLocs()[1])/2;
		PABlockLocation result = getLocs()[0];
		result.setX((int) (result.getX()-thisRadius));
		result.setZ((int) (result.getZ()-thisRadius));
		return result;
	}

	@Override
	public PABlockLocation getMinimumLocation() {
		Double thisRadius = getLocs()[0].getDistance(getLocs()[1])/2;
		PABlockLocation result = getLocs()[1];
		result.setX((int) (result.getX()+thisRadius));
		result.setZ((int) (result.getZ()+thisRadius));
		return result;
	}

	@Override
	public boolean tooFarAway(int joinRange, Location location) {
		PABlockLocation cLoc = getCenter();
		cLoc.setY(location.getBlockY());
		PABlockLocation reach = (new PABlockLocation(location)).pointTo(cLoc, (double) joinRange);
		
		return contains(reach);
	}
}
