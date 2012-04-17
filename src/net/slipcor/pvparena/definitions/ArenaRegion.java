package net.slipcor.pvparena.definitions;

import java.util.HashMap;
import java.util.Random;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * region class
 * 
 * -
 * 
 * contains region methods and variables for quicker region access
 * 
 * @author slipcor
 * 
 * @version v0.7.10
 * 
 */

public class ArenaRegion {
	private Debug db = new Debug(15);
	private boolean cuboid;
	protected Vector min;
	protected Vector max;
	protected World world;
	public String name;
	private regionType type;
	private HashMap<Location, ItemStack[]> chests = new HashMap<Location, ItemStack[]>();
	private HashMap<Location, ItemStack[]> furnaces = new HashMap<Location, ItemStack[]>();
	private HashMap<Location, ItemStack[]> dispensers = new HashMap<Location, ItemStack[]>();

	public static enum regionType {
		CUBOID, SPHERIC
	}

	/**
	 * create a PVP Arena region instance
	 * 
	 * @param sName
	 *            the region name
	 * @param lMin
	 *            the region minimum location
	 * @param lMax
	 *            the region maximum location
	 */
	public ArenaRegion(String sName, Location lMin, Location lMax,
			regionType type) {
		name = sName;

		if (type.equals(regionType.CUBOID)) {
			Location[] sane = sanityCheck(lMin, lMax);
			lMin = sane[0].clone();
			lMax = sane[1].clone();
			cuboid = true;
		} else {
			cuboid = false;
		}
		min = lMin.toVector();
		max = lMax.toVector();
		world = lMin.getWorld();
		this.type = type;
		db.i("created region: " + sName + " - "
				+ (type.equals(regionType.CUBOID) ? "cuboid" : "sphere"));
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

	/**
	 * is a location inside the PVP Arena region?
	 * 
	 * @param vec
	 *            the vector to check
	 * @return
	 */
	public boolean contains(Location loc) {
		if (min == null || max == null || loc == null
				|| loc.getWorld() != world)
			return false; // no arena, no container or not in the same world
		Vector vec = loc.toVector();

		if (cuboid) {

			db.i("checking region " + name + ": "
					+ String.valueOf(vec.isInAABB(min, max)));
			db.i("(" + vec.toString() + " isInAABB " + min.toString() + "/"
					+ max.toString() + ")");
			return vec.isInAABB(min, max);
		} else {
			return vec.distance(min.getMidpoint(max)) <= (min.distance(max) / 2);
		}
	}

	/**
	 * is a location farther away than a given length?
	 * 
	 * @param offset
	 *            the length to check
	 * @param loc
	 *            the location to check
	 * @return true if the location is more than offset blocks away, false
	 *         otherwise
	 */
	public boolean tooFarAway(int offset, Location loc) {
		if (!world.equals(loc.getWorld()))
			return true;

		db.i("checking join range");
		Vector bvdiff = (Vector) min.getMidpoint(max);

		return (offset < bvdiff.distance(loc.toVector()));
	}

	/**
	 * does the region overlap with another given region?
	 * 
	 * @param paRegion
	 *            the other region
	 * @return true if the regions overlap, false otherwise
	 */
	public boolean overlapsWith(ArenaRegion paRegion) {
		db.i("checking if " + name + " overlaps with " + paRegion.name);
		if (this.cuboid && paRegion.cuboid) {
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
		} else if (!this.cuboid && !paRegion.cuboid) {
			// compare 2 spheres
			db.i("compare 2 spheres");
			Vector thisCenter = this.max.getMidpoint(this.min);
			Vector thatCenter = paRegion.max.getMidpoint(paRegion.min);

			double thisRadius = this.max.distance(min) / 2;
			double thatRadius = paRegion.max.distance(paRegion.min) / 2;

			db.i("dist: " + thisCenter.distance(thatCenter) + "; thisRadius: "
					+ thisRadius + "; thatRadius: " + thatRadius);

			return thisCenter.distance(thatCenter) < (thisRadius + thatRadius);

		} else if (this.cuboid && !paRegion.cuboid) {
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
		} else {
			db.i("link=>other way round!");
			return paRegion.overlapsWith(this); // just check the other freaking
												// way round!
		}
	}

	/**
	 * drop an item in a random region position
	 * 
	 * @param item
	 */
	public void dropItemRandom(Material item) {

		db.i("dropping item " + item.toString());
		int diffx = (int) (min.getX() - max.getX());
		int diffy = (int) (min.getY() - max.getY());
		int diffz = (int) (min.getZ() - max.getZ());

		Random r = new Random();

		int posx = 0;
		int posy = 0;
		int posz = 0;

		if (cuboid) {
			posx = diffx == 0 ? min.getBlockX() : (int) ((diffx / Math
					.abs(diffx)) * r.nextInt(Math.abs(diffx)) + max.getX());
			posy = diffy == 0 ? min.getBlockY() : (int) ((diffx / Math
					.abs(diffy)) * r.nextInt(Math.abs(diffy)) + max.getY());
			posz = diffz == 0 ? min.getBlockZ() : (int) ((diffx / Math
					.abs(diffz)) * r.nextInt(Math.abs(diffz)) + max.getZ());
		} else {
			Vector thisCenter = this.max.getMidpoint(this.min);
			double thisRadius = this.max.distance(min) / 2;

			Vector spawnPosition = thisCenter.add(Vector.getRandom()
					.normalize().multiply(r.nextDouble() * thisRadius));

			while (spawnPosition.toLocation(world).getBlock().getType() == null
					|| spawnPosition.toLocation(world).getBlock().getType()
							.equals(Material.AIR)) {
				spawnPosition.add(new Vector(0, 1, 0));
			}
			spawnPosition.add(new Vector(0, 1, 0)); // 1 above ground

			posx = spawnPosition.getBlockX();
			posy = spawnPosition.getBlockY();
			posz = spawnPosition.getBlockZ();

		}

		world.dropItem(new Location(world, posx, posy + 1, posz),
				new ItemStack(item, 1));
	}

	/**
	 * restore the region (atm just remove all arrows and items)
	 */
	public void restore() {
		db.i("restoring region " + name);
		if (world == null) {
			PVPArena.instance.getLogger().severe(
					"[PA-debug] world is null in region " + name);
			return;
		} else if (world.getEntities() == null) {
			return;
		}
		for (Entity e : world.getEntities()) {
			if (((!(e instanceof Item)) && (!(e instanceof Arrow)))
					|| (!contains(e.getLocation())))
				continue;
			e.remove();
		}
	}

	public void showBorder(Player player) {
		if (cuboid) {
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
		} else {
			// TODO - sphere generation
		}
	}

	public regionType getType() {
		return type;
	}

	public void saveChests() {
		chests.clear();
		furnaces.clear();
		dispensers.clear();
		int x;
		int y;
		int z;
		if (type.equals(regionType.CUBOID)) {

			for (x = min.getBlockX(); x <= max.getBlockX(); x++) {
				for (y = min.getBlockY(); y <= max.getBlockY(); y++) {
					for (z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
						Block b = world.getBlockAt(x, y, z);
						if (b.getType() == Material.CHEST) {
							Chest c = (Chest) b.getState();

							chests.put(b.getLocation(), c.getInventory()
									.getContents().clone());
						} else if (b.getType() == Material.FURNACE) {
							Furnace c = (Furnace) b.getState();

							furnaces.put(b.getLocation(), c.getInventory()
									.getContents().clone());
						} else if (b.getType() == Material.DISPENSER) {
							Dispenser c = (Dispenser) b.getState();

							dispensers.put(b.getLocation(), c.getInventory()
									.getContents().clone());
						}
						
					}
				}

			}
		} else if (type.equals(regionType.SPHERIC)) {
			for (x = min.getBlockX(); x <= max.getBlockX(); x++) {
				for (y = min.getBlockY(); y <= max.getBlockY(); y++) {
					for (z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
						Block b = world.getBlockAt(x, y, z);
						if ((b.getType() != Material.CHEST) &&
								(b.getType() != Material.FURNACE) &&
								(b.getType() != Material.DISPENSER)) {
							continue;
						}
						if (!contains(b.getLocation())) {
							continue;
						}
						if ((b.getType() != Material.CHEST)) {
							Chest c = (Chest) b.getState();

							chests.put(b.getLocation(), c.getInventory()
									.getContents().clone());
						} else if (b.getType() != Material.FURNACE) {
							Furnace f = (Furnace) b.getState();
							
							furnaces.put(b.getLocation(), f.getInventory()
									.getContents().clone());
						} else if (b.getType() != Material.DISPENSER) {
							Dispenser d = (Dispenser) b.getState();
							
							dispensers.put(b.getLocation(), d.getInventory()
									.getContents().clone());
						}
					}
				}

			}
		}
	}

	public void restoreChests() {
		db.i("restoring chests");
		for (Location loc : chests.keySet()) {
			try {
				db.i("trying to restore chest: " + loc.toString());
				((Chest) world.getBlockAt(loc).getState()).getInventory()
						.setContents(chests.get(loc));
				db.i("success!");
			} catch (Exception e) {
				//
			}
		}
		for (Location loc : dispensers.keySet()) {
			try {
				db.i("trying to restore dispenser: " + loc.toString());
				((Dispenser) world.getBlockAt(loc).getState()).getInventory()
						.setContents(dispensers.get(loc));
				db.i("success!");
			} catch (Exception e) {
				//
			}
		}
		for (Location loc : furnaces.keySet()) {
			try {
				db.i("trying to restore furnace: " + loc.toString());
				((Furnace) world.getBlockAt(loc).getState()).getInventory()
						.setContents(furnaces.get(loc));
				db.i("success!");
			} catch (Exception e) {
				//
			}
		}
	}
}