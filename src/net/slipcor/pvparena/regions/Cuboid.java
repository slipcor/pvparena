package net.slipcor.pvparena.regions;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.neworder.ArenaRegion;

public class Cuboid extends ArenaRegion {
	public Cuboid() {
		super("cuboid");
		this.setShape(RegionShape.CUBOID);
	}

	@Override
	public String version() {
		return "v0.8.11.25";
	}
	
	@Override
	public void set(World world, String coords) {
		Location[] pos = Config.parseCuboid(world, coords);
		this.world = world;
		
		Location[] sane = sanityCheck(pos[0], pos[1]);
		min = sane[0].clone().toVector();
		max = sane[1].clone().toVector().add(new Vector(1,1,1));
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
	private Location[] sanityCheck(Location lMin, Location lMax) {
		db.i("santizing locations");
		boolean x = (lMin.getBlockX() > lMax.getBlockX());
		boolean y = (lMin.getBlockY() > lMax.getBlockY());
		boolean z = (lMin.getBlockZ() > lMax.getBlockZ());

		if (!(x | y | z)) {
			return new Location[] { lMin, lMax };
		}
		Location l1;
		Location l2;

		l1 = new Location(lMin.getWorld(), x ? lMax.getBlockX()
				: lMin.getBlockX(), y ? lMax.getBlockY() : lMin.getBlockY(),
				z ? lMax.getBlockZ() : lMin.getBlockZ());
		l2 = new Location(lMin.getWorld(), x ? lMin.getBlockX()
				: lMax.getBlockX(), y ? lMin.getBlockY() : lMax.getBlockY(),
				z ? lMin.getBlockZ() : lMax.getBlockZ());
		return new Location[] { l1, l2 };
	}
	
	@Override
	public boolean contains(Location loc) {
		if (min == null || max == null || loc == null
				|| loc.getWorld() != world)
			return false; // no arena, no container or not in the same world
		Vector vec = loc.toVector();

		db.i("checking region " + name + ": "
				+ String.valueOf(vec.isInAABB(min, max)));
		db.i("(" + vec.toString() + " isInAABB " + min.toString() + "/"
				+ max.toString() + ")");
		return vec.isInAABB(min, max);
	}
	
	@Override
	public Location getAbsoluteMinimum() {
		return this.min.toLocation(world);
	}
	
	@Override
	public Location getAbsoluteMaximum() {
		return this.max.toLocation(world);
	}

	public void initialize() {
		Location[] sane = sanityCheck(this.min.toLocation(world), this.max.toLocation(world));
		min = sane[0].clone().toVector();
		max = sane[1].clone().toVector();
	}

	@Override
	public boolean overlapsWith(ArenaRegion paRegion) {
		db.i("checking if " + name + " overlaps with " + paRegion.name);
		if (paRegion.getShape().equals(RegionShape.CUBOID)) {
			// compare 2 cuboids
			db.i("compare 2 cuboids");
			if (min.getX() > paRegion.max.getX()
					|| min.getY() > paRegion.max.getY()
					|| min.getZ() > paRegion.max.getZ()) {
				db.i("pos greater than other region");
				return false;
			}
			if (paRegion.min.getX() > max.getX()
					|| paRegion.min.getY() > max.getY()
					|| paRegion.min.getZ() > max.getZ()) {
				db.i("pos smaller than other region");
				return false;
			}
			db.i("min ^ max overlap!");
			return true;
		} else if (paRegion.getShape().equals(RegionShape.SPHERIC)) {
			// we are cube and search for intersecting sphere

			db.i("compare local cube to other sphere");

			Vector thisCenter = this.max.getMidpoint(this.min);
			Vector thatCenter = paRegion.max.getMidpoint(paRegion.min);

			if (contains(thatCenter.toLocation(world))) {
				db.i("cuboid is INSIDE sphere");
				return true; // the sphere is inside!
			}

			Vector diff = thatCenter.subtract(thisCenter); // diff is pointing
															// from that to this
			db.i("checking calculated vector");
			return this.contains(diff.normalize().toLocation(world));
		} else if (paRegion.getShape().equals(RegionShape.CYLINDRIC)) {
			// we are cube and search for intersecting cylinder

			db.i("compare local cube to other cylinder");

			Vector thisCenter = this.max.getMidpoint(this.min);
			Vector thatCenter = paRegion.max.getMidpoint(paRegion.min);
			
			if (this.max.getY() < paRegion.getAbsoluteMinimum().getY()) {
				return false;
			}
			if (this.min.getY() > paRegion.getAbsoluteMaximum().getY()) {
				return false;
			}
			
			thisCenter.setY(thatCenter.getY());

			if (contains(thatCenter.toLocation(world))) {
				db.i("cuboid is INSIDE sphere");
				return true; // the sphere is inside!
			}

			Vector diff = thatCenter.subtract(thisCenter); // diff is pointing
															// from that to this
			db.i("checking calculated vector");
			return this.contains(diff.normalize().toLocation(world));
		} else {
			System.out.print("Region Shape not supported: " + paRegion.getShape().name());
		}
		return false;
	}

	@Override
	public void dropItemRandom(Material item) {

		db.i("dropping item " + item.toString());
		int diffx = (int) (min.getX() - max.getX());
		int diffy = (int) (min.getY() - max.getY());
		int diffz = (int) (min.getZ() - max.getZ());

		Random r = new Random();

		int posx = 0;
		int posy = 0;
		int posz = 0;

		posx = diffx == 0 ? min.getBlockX() : (int) ((diffx / Math
				.abs(diffx)) * r.nextInt(Math.abs(diffx)) + max.getX());
		posy = diffy == 0 ? min.getBlockY() : (int) ((diffx / Math
				.abs(diffy)) * r.nextInt(Math.abs(diffy)) + max.getY());
		posz = diffz == 0 ? min.getBlockZ() : (int) ((diffx / Math
				.abs(diffz)) * r.nextInt(Math.abs(diffz)) + max.getZ());

		world.dropItem(new Location(world, posx, posy + 1, posz),
				new ItemStack(item, 1));
	}
	
	@Override
	public void showBorder(Player player) {
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
	}
}
