package net.slipcor.pvparena.regions;

import java.util.HashSet;

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
 * <pre>Arena Region Shape class "spheric"</pre>
 * 
 * Defines a spheric region, including overlap checks and contain checks 
 * 
 * @author slipcor
 * 
 * @version v0.9.9
 */

public class SphericRegion extends ArenaRegionShape {
	
	HashSet<Block> border = new HashSet<Block>();
	
	public SphericRegion() {
		super("spheric");
		shape = RegionShape.SPHERIC;
	}
	
	public SphericRegion(Arena arena, String name, PABlockLocation[] locs) {
		super(arena, name, locs, "spheric");
		db = new Debug(201);
	}

	@Override
	public String version() {
		return "v0.9.9.18";
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
			PVPArena.instance.getLogger().warning("Region Shape not supported: " + paRegion.getShape().name());
		}
		return false;
	}

	@Override
	public void showBorder(final Player player) {
		PABlockLocation center = new PABlockLocation(getCenter().toLocation());
		
		World w = Bukkit.getWorld(this.world);
		
		border.clear();
		
		PABlockLocation a = new PABlockLocation(this.locs[1].toLocation());
		a.setY(locs[0].getY());
		
		Double radius = getRadius();
		
		Double radiusSquared = radius * radius;
		
		// ------------------------------
		// ---------- Y CIRCLE ----------
		// ------------------------------
		
		for (int x = 0; x <= Math.ceil(radius+1/2); x++) {
			int z = (int) Math.abs(Math.sqrt(radiusSquared - (x*x)));

			border.add((new Location(w, center.getX() + x, center.getY(), center.getZ() + z)).getBlock());
			border.add((new Location(w, center.getX() - x, center.getY(), center.getZ() + z)).getBlock());
			border.add((new Location(w, center.getX() + x, center.getY(), center.getZ() - z)).getBlock());
			border.add((new Location(w, center.getX() - x, center.getY(), center.getZ() - z)).getBlock());
		}
		
		for (int z = 0; z <= Math.ceil(radius+1/2); z++) {
			int x = (int) Math.abs(Math.sqrt(radiusSquared - (z*z)));
			
			border.add((new Location(w, center.getX() + x, center.getY(), center.getZ() + z)).getBlock());
			border.add((new Location(w, center.getX() - x, center.getY(), center.getZ() + z)).getBlock());
			border.add((new Location(w, center.getX() + x, center.getY(), center.getZ() - z)).getBlock());
			border.add((new Location(w, center.getX() - x, center.getY(), center.getZ() - z)).getBlock());
		}

		// ------------------------------
		// ---------- Z CIRCLE ----------
		// ------------------------------
		
		for (int y = 0; y <= Math.ceil(radius+1/2); y++) {
			int x = (int) Math.abs(Math.sqrt(radiusSquared - (y*y)));
			
			border.add((new Location(w, center.getX() + x, center.getY() + y, center.getZ())).getBlock());
			border.add((new Location(w, center.getX() - x, center.getY() + y, center.getZ())).getBlock());
			border.add((new Location(w, center.getX() + x, center.getY() - y, center.getZ())).getBlock());
			border.add((new Location(w, center.getX() - x, center.getY() - y, center.getZ())).getBlock());

		}
		
		for (int x = 0; x <= Math.ceil(radius+1/2); x++) {
			int y = (int) Math.abs(Math.sqrt(radiusSquared - (x*x)));

			border.add((new Location(w, center.getX() + x, center.getY() + y, center.getZ())).getBlock());
			border.add((new Location(w, center.getX() - x, center.getY() + y, center.getZ())).getBlock());
			border.add((new Location(w, center.getX() + x, center.getY() - y, center.getZ())).getBlock());
			border.add((new Location(w, center.getX() - x, center.getY() - y, center.getZ())).getBlock());

		}

		// ------------------------------
		// ---------- X CIRCLE ----------
		// ------------------------------
		
		for (int y = 0; y <= Math.ceil(radius+1/2); y++) {
			int z = (int) Math.abs(Math.sqrt(radiusSquared - (y*y)));

			border.add((new Location(w, center.getX(), center.getY() + y, center.getZ() + z)).getBlock());
			border.add((new Location(w, center.getX(), center.getY() - y, center.getZ() + z)).getBlock());
			border.add((new Location(w, center.getX(), center.getY() + y, center.getZ() - z)).getBlock());
			border.add((new Location(w, center.getX(), center.getY() - y, center.getZ() - z)).getBlock());
		}
		
		for (int z = 0; z <= Math.ceil(radius+1/2); z++) {
			int y = (int) Math.abs(Math.sqrt(radiusSquared - (z*z)));
			
			border.add((new Location(w, center.getX(), center.getY() + y, center.getZ() + z)).getBlock());
			border.add((new Location(w, center.getX(), center.getY() - y, center.getZ() + z)).getBlock());
			border.add((new Location(w, center.getX(), center.getY() + y, center.getZ() - z)).getBlock());
			border.add((new Location(w, center.getX(), center.getY() - y, center.getZ() - z)).getBlock());
		}
		
		for (Block b : border) {
			if (!isInNoWoolSet(b)) 
				player.sendBlockChange(b.getLocation(), Material.WOOL, (byte) 0);
		}
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new Runnable() {

			@Override
			public void run() {
				for (Block b : border) {
					player.sendBlockChange(b.getLocation(), b.getTypeId(), b.getData());
				}
				border.clear();
			}
			
		}, 100L);
	}

	@Override
	public boolean contains(PABlockLocation loc) {
		if (this.getLocs()[0] == null || this.getLocs()[1] == null || loc == null
				|| !loc.getWorldName().equals(world))
			return false; // no arena, no container or not in the same world
		return loc.getDistance(this.getCenter()) <= getRadius();
	}

	private Double getRadius() {
		return getLocs()[0].getDistance(getLocs()[1])/2;
	}

	@Override
	public PABlockLocation getCenter() {
		return locs[0].getMidpoint(locs[1]);
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
	public boolean tooFarAway(int joinRange, Location location) {
		PABlockLocation reach = (new PABlockLocation(location)).pointTo(getCenter(), (double) joinRange);
		
		return contains(reach);
	}
}
