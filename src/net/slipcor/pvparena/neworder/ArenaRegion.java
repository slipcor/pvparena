package net.slipcor.pvparena.neworder;

import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.events.PALoseEvent;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.runnables.RegionRunnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
 * @version v0.8.10
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

	private HashMap<String, Location> playerNameLocations = new HashMap<String, Location>();

	public static enum RegionShape {
		CUBOID, SPHERIC, CYLINDRIC;
	}

	public static enum RegionType {
		CUSTOM, JOIN, SPECTATOR, LOUNGE, BATTLEFIELD, EXIT, NOCAMP, DEATH, WIN, LOSE
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
	 * 
	 *            public ArenaRegion(String sName, Location lMin, Location lMax,
	 *            regionType type) {
	 */

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
			if (((!(e instanceof Item)) && (!(e instanceof Arrow))
					&& (!(e instanceof Creature)) && (!(e instanceof Animals)))
					|| (!contains(e.getLocation())))
				continue;
			e.remove();
		}
		Bukkit.getScheduler().cancelTask(TICK_ID);
		TICK_ID = -1;
	}

	public void showBorder(Player player) {
	}

	public Location getAbsoluteMinimum() {
		return null;
	}

	public Location getAbsoluteMaximum() {
		return null;
	}

	public RegionShape getShape() {
		return shape;
	}

	public RegionType getType() {
		return type;
	}

	public void initialize() {
	}

	public void initTimer() {
		if (!arena.fightInProgress) {
			arena.fightInProgress = true;
			System.out.print("[PA-debug] fight not yet in progress...");
		}
		RegionRunnable rr = new RegionRunnable(this,0);
		TICK_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, rr,
				arena.cfg.getInt("region.timer") * 1L,
				arena.cfg.getInt("region.timer") * 1L);
		rr.setId(TICK_ID);
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
					arena.playerLeave(ap.get(), "lose");
					PALoseEvent e = new PALoseEvent(arena, ap.get());
					Bukkit.getPluginManager().callEvent(e);
				}
			} else if (type.equals(RegionType.WIN)) {
				if (this.contains(ap.get().getLocation())) {
					for (ArenaTeam team : arena.getTeams()) {
						if (team.getTeamMembers().contains(ap)) {
							// skip winner
							continue;
						}
						for (ArenaPlayer ap2 : team.getTeamMembers()) {
							if (ap2.getStatus().equals(Status.FIGHT)) {
								world.strikeLightningEffect(ap2.get().getLocation());
								EntityDamageEvent e = new EntityDamageEvent(ap2.get(), DamageCause.LIGHTNING, 10);
								PlayerListener.commitPlayerDeath(arena, ap2.get(), e);
							}
						}
						return;
					}
				}
			} else if (type.equals(RegionType.LOSE)) {
				if (this.contains(ap.get().getLocation())) {
					for (ArenaTeam team : arena.getTeams()) {
						if (!team.getTeamMembers().contains(ap)) {
							// skip winner
							continue;
						}
						for (ArenaPlayer ap2 : team.getTeamMembers()) {
							if (ap2.getStatus().equals(Status.FIGHT)) {
								world.strikeLightningEffect(ap2.get().getLocation());
								EntityDamageEvent e = new EntityDamageEvent(ap2.get(), DamageCause.LIGHTNING, 10);
								PlayerListener.commitPlayerDeath(arena, ap2.get(), e);
							}
						}
						return;
					}
				}
			} else if (type.equals(RegionType.NOCAMP)) {
				if (this.contains(ap.get().getLocation())) {
					Location loc = playerNameLocations.get(ap.getName());
					if (loc == null) {
						Arenas.tellPlayer(ap.get(),
								Language.parse("nocampregion"));
					} else {
						if (loc.distance(ap.get().getLocation()) < 3) {
							ap.get().damage(
									arena.cfg.getInt("region.spawncampdamage"));
						}
					}
					playerNameLocations.put(ap.getName(), ap.get()
							.getLocation().getBlock().getLocation());
				} else {
					playerNameLocations.remove(ap.getName());
				}
			} else if (type.equals(RegionType.BATTLEFIELD)) {
				if (!arena.regions.containsKey("battlefield")) {
					db.i("region battlefield not set, aborting quit check");
					return;
				}
				
				HashSet<ArenaPlayer> plyrs = new HashSet<ArenaPlayer>();
				for (ArenaPlayer ap2 : arena.getPlayers()) {
					plyrs.add(ap2);
				}

				for (ArenaPlayer ap2 : plyrs) {
					if (!ap2.getStatus().equals(Status.FIGHT)) {
						continue;
					}
					if (!this.contains(ap.get().getLocation())) {
						Arenas.tellPlayer(ap.get(), Language.parse("youescaped"));
						arena.playerLeave(ap2.get(), "exit");
					}
				}
			} else if (type.equals(RegionType.SPECTATOR)) {
				if (!arena.regions.containsKey("spectator")) {
					db.i("region spectator not set, aborting quit check");
					return;
				}
				HashSet<ArenaPlayer> plyrs = new HashSet<ArenaPlayer>();
				for (ArenaPlayer ap2 : arena.getPlayers()) {
					plyrs.add(ap2);
				}

				for (ArenaPlayer ap2 : plyrs) {
					if (!ap2.getStatus().equals(Status.WATCH)) {
						continue;
					}
					if (!this.contains(ap.get().getLocation())) {
						Arenas.tellPlayer(ap.get(), Language.parse("youescaped"));
						arena.playerLeave(ap2.get(), "exit");
					}
				}
			}
		}
	}

	public void reset() {
		Bukkit.getScheduler().cancelTask(TICK_ID);
	}

	public World getWorld() {
		return world;
	}
}