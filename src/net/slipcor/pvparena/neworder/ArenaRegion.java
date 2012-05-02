package net.slipcor.pvparena.neworder;

import java.util.HashMap;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.runnables.RegionRunnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.nodinchan.ncloader.Loadable;

/**
 * region class
 * 
 * -
 * 
 * contains region methods and variables for quicker region access
 * 
 * @author slipcor
 * 
 * @version v0.7.18
 * 
 */

public class ArenaRegion extends Loadable {
	protected Debug db = new Debug(15);
	
	public Vector min;
	public Vector max;
	
	public Arena arena;
	
	protected World world;
	public String name;
	private RegionShape shape;
	private RegionType type;
	
	private int TICK_ID = -1;
	
	private HashMap<Location, ItemStack[]> chests = new HashMap<Location, ItemStack[]>();
	private HashMap<Location, ItemStack[]> furnaces = new HashMap<Location, ItemStack[]>();
	private HashMap<Location, ItemStack[]> dispensers = new HashMap<Location, ItemStack[]>();

	private HashMap<Player, Location> playerLocations = new HashMap<Player, Location>();
	
	public static enum RegionShape {
		CUBOID, SPHERIC;
	}
	
	public static enum RegionType {
		CUSTOM, JOIN, SPECTATOR, LOUNGE, BATTLEFIELD, EXIT, NOCAMP, DEATH 
	}
	
	public ArenaRegion clone() {
		try {
			ArenaRegion at = (ArenaRegion) super.clone();
			at.setShape(getShape());
			return at; 
		} catch (CloneNotSupportedException e) {
			System.out.println(e);
			return null;
		}
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
	 
	public ArenaRegion(String sName, Location lMin, Location lMax,
			regionType type) {*/
	
	public ArenaRegion(String shape) {
		super(shape);
	}
	
	public String version() {
		return "OUTDATED";
	}

	/**
	 * is a location inside the PVP Arena region?
	 * 
	 * @param vec
	 *            the vector to check
	 * @return
	 */
	public boolean contains(Location loc) {
		return false;
	}

	private ItemStack[] cloneIS(ItemStack[] contents) {
		ItemStack[] result = new ItemStack[contents.length];
		
		for (int i=0; i<result.length; i++) {
			if (contents[i] == null) {
				continue;
			}
			ItemStack is = contents[i];
			result[i] = new ItemStack(is.getType(), is.getAmount(), is.getDurability(), is.getData().getData());
			
			for (Enchantment ench : is.getEnchantments().keySet()) {
				result[i].addUnsafeEnchantment(ench, is.getEnchantments().get(ench));
			}
		}
		
		return result;
	}

	private ItemStack cloneIS(ItemStack is) {
		return is.clone();
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
		return false;
	}

	/**
	 * drop an item in a random region position
	 * 
	 * @param item
	 */
	public void dropItemRandom(Material item) {
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
		Bukkit.getScheduler().cancelTask(TICK_ID);
		TICK_ID = -1;
	}

	public void showBorder(Player player) {
	}

	public RegionShape getShape() {
		return shape;
	}

	public RegionType getType() {
		return type;
	}
	
	public void initTimer() {
		TICK_ID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(PVPArena.instance, new RegionRunnable(this), arena.cfg.getInt("region.spawncampdamage") * 1L, arena.cfg.getInt("region.spawncampdamage") * 1L);
	}

	public void saveChests() {
		chests.clear();
		furnaces.clear();
		dispensers.clear();
		int x;
		int y;
		int z;
		if (shape.equals(RegionShape.CUBOID)) {

			for (x = min.getBlockX(); x <= max.getBlockX(); x++) {
				for (y = min.getBlockY(); y <= max.getBlockY(); y++) {
					for (z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
						Block b = world.getBlockAt(x, y, z);
						if (b.getType() == Material.CHEST) {
							Chest c = (Chest) b.getState();

							chests.put(b.getLocation(), cloneIS(c.getInventory()
									.getContents()));
						} else if (b.getType() == Material.FURNACE) {
							Furnace c = (Furnace) b.getState();

							furnaces.put(b.getLocation(), cloneIS(c.getInventory()
									.getContents()));
						} else if (b.getType() == Material.DISPENSER) {
							Dispenser c = (Dispenser) b.getState();

							dispensers.put(b.getLocation(), cloneIS(c.getInventory()
									.getContents()));
						}
						
					}
				}

			}
		} else if (shape.equals(RegionShape.SPHERIC)) {
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

							chests.put(b.getLocation(), cloneIS(c.getInventory()
									.getContents()));
						} else if (b.getType() != Material.FURNACE) {
							Furnace f = (Furnace) b.getState();
							
							furnaces.put(b.getLocation(), cloneIS(f.getInventory()
									.getContents()));
						} else if (b.getType() != Material.DISPENSER) {
							Dispenser d = (Dispenser) b.getState();
							
							dispensers.put(b.getLocation(), cloneIS(d.getInventory()
									.getContents()));
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
				Inventory inv = ((Chest) world.getBlockAt(loc).getState()).getInventory();
				inv.clear();
				inv.setContents(chests.get(loc));
				db.i("success!");
			} catch (Exception e) {
				//
			}
		}
		for (Location loc : dispensers.keySet()) {
			try {
				db.i("trying to restore dispenser: " + loc.toString());
				
				Inventory inv = ((Dispenser) world.getBlockAt(loc).getState()).getInventory();
				inv.clear();
				for (ItemStack is : chests.get(loc)) {
					if (is != null) {
						inv.addItem(cloneIS(is));
					}
				}
				db.i("success!");
			} catch (Exception e) {
				//
			}
		}
		for (Location loc : furnaces.keySet()) {
			try {
				db.i("trying to restore furnace: " + loc.toString());
				((Furnace) world.getBlockAt(loc).getState()).getInventory()
						.setContents(cloneIS(furnaces.get(loc)));
				db.i("success!");
			} catch (Exception e) {
				//
			}
		}
	}

	public void set(World world, String coords) {
	}
	
	protected void setShape(RegionShape shape) {
		this.shape = shape;
	}

	public void setType(RegionType type) {
		this.type = type;
	}
	
	public void tick() {
		for (ArenaPlayer ap : arena.getPlayers()) {
			if (type.equals(RegionType.DEATH)) {
				if (this.contains(ap.get().getLocation())) {
					Arenas.tellPlayer(ap.get(), Language.parse("deathregion"));
					arena.playerLeave(ap.get());
				}
			} else if (type.equals(RegionType.NOCAMP)) {
				if (this.contains(ap.get().getLocation())) {
					Location loc = playerLocations.get(ap.get());
					if (loc == null) {
						Arenas.tellPlayer(ap.get(), Language.parse("nocampregion"));
					} else {
						if (loc.distance(ap.get().getLocation()) < 3) {
							ap.get().damage(arena.cfg.getInt("region.spawncampdamage"));
						}
					}
					playerLocations.put(ap.get(), ap.get().getLocation().getBlock().getLocation());
				} else {
					playerLocations.remove(ap.get());
				}
			}
		}
	}
}