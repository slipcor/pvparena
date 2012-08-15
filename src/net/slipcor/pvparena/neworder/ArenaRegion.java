package net.slipcor.pvparena.neworder;

import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.events.PALoseEvent;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionShape;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionType;
import net.slipcor.pvparena.regions.CuboidRegion;
import net.slipcor.pvparena.runnables.RegionRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nodinchan.ncbukkit.loader.Loadable;

public abstract class ArenaRegion extends Loadable {
	protected String world;
	private Arena arena;
	private String name;
	private RegionShape shape;
	private RegionType type;
	private HashSet<RegionFlag> flags;
	private HashSet<RegionProtection> protections;
	private HashMap<String, Location> playerNameLocations = new HashMap<String, Location>();
	
	protected final PABlockLocation[] locs;

	public static enum RegionShape {
		CUBOID, SPHERIC, CYLINDRIC;
	}

	/**
	 * RegionType for physical orientation
	 * 
	 * <pre>
	 * CUSTOM => a module added region
	 * WATCH  => the spectator region
	 * LOUNGE => the ready lounge region
	 * BATTLE => the battlefield region
	 * EXIT   => the exit region
	 * JOIN   => the join region
	 * </pre>
	 */
	public static enum RegionType {
		CUSTOM, WATCH, LOUNGE, BATTLE, EXIT, JOIN
	}

	/**
	 * RegionFlag for tick events
	 * 
	 * <pre>
	 * NOCAMP - players not moving will be damaged
	 * DEATH -  players being here will die
	 * WIN -    players being here will win
	 * LOSE -   players being here will lose
	 * </pre>
	 */
	public static enum RegionFlag {
		NOCAMP, DEATH, WIN, LOSE
	}
	
	/**
	 * RegionProtection
	 * 
	 * <pre>
	 * BREAK - Block break
	 * FIRE - Fire
	 * MOBS - Mob spawning
	 * NATURE - Environment changes (leaves, shrooms, water, lava)
	 * PAINTING - Painting placement/destruction
	 * PISTON - Piston triggering
	 * PLACE - Block placement
	 * REDSTONE - Redstone current change
	 * TNT - TNT usage
	 * TNTBREAK - TNT block break
	 * </pre>
	 */
	public static enum RegionProtection {
		BREAK, FIRE, MOBS, NATURE, PAINTING, PISTON, PLACE, TNT, TNTBREAK
	}
	
	/**
	 * <pre>
	 * CENTER = in the battlefield center
	 * NORTH = north end of the battlefield
	 * EAST = east end of the battlefield
	 * SOUTH = south end of the battlefield
	 * WEST = west end of the battlefield
	 * TOP = on top of the battlefield
	 * BOTTOM = under the battlefield
	 * INSIDE = inside the battlefield
	 * OUTSIDE = around the battlefield 
	 * </pre>
	 */
	public static enum RegionPosition {
		CENTER, NORTH, EAST, SOUTH, WEST, TOP, BOTTOM, INSIDE, OUTSIDE
	}

	public ArenaRegion(Arena arena, String name, PABlockLocation[] locs) {
		super("cuboid");
		this.setName(name);
		this.world = locs[0].getWorldName();
		this.locs = locs;
		this.setArena(arena);
	}

	public ArenaRegion(Arena arena, String name, PABlockLocation[] locs, String shape) {
		super("cuboid");
		this.setName(name);
		this.world = locs[0].getWorldName();
		this.locs = locs;
		this.setArena(arena);
		PVPArena.instance.getArsm();
		this.shape = ArenaRegionShapeManager.getShapeByName(shape);
	}

	public ArenaRegion(String regionShape) {
		super(regionShape);
		world = null;
		locs = new PABlockLocation[2];
	}

	public abstract boolean contains(PABlockLocation loc);

	public abstract PABlockLocation getCenter();

	public abstract PABlockLocation getMaximumLocation();

	public abstract PABlockLocation getMinimumLocation();

	public abstract boolean overlapsWith(ArenaRegion other);
	
	public abstract void showBorder(Player player);
	
	public abstract boolean tooFarAway(int joinRange, Location location);

	public void displayInfo(CommandSender sender) {
		// TODO Auto-generated method stub
		
	}
	
	public void flagAdd(RegionFlag rf) {
		flags.add(rf);
	}

	public boolean flagToggle(RegionFlag rf) {
		if (flags.contains(rf)) {
			flags.remove(rf);
		} else {
			flags.add(rf);
		}
		return flags.contains(rf);
	}
	
	public void flagRemove(RegionFlag rf) {
		flags.remove(rf);
	}

	public HashSet<RegionFlag> getFlags() {
		return flags;
	}
	
	public String getVersion() {
		return "OUTDATED";
	}

	public World getWorld() {
		return Bukkit.getWorld(world);
	}

	public String getWorldName() {
		return world;
	}

	public boolean isInRange(int offset, PABlockLocation loc) {
		if (!world.equals(loc.getWorldName()))
			return false;

		PABlockLocation bvdiff = getCenter();

		return (offset < bvdiff.getDistance(loc));
	}
	
	public void protectionAdd(RegionProtection rp) {
		if (rp == null) {
			protectionSetAll(true);
			return;
		}
		protections.add(rp);
	}
	
	public boolean protectionSetAll(Boolean b) {
		
		for (RegionProtection rp : RegionProtection.values()) {
			if (rp == null) {
				getArena().msg(Bukkit.getConsoleSender(), "&cWarning! RegionProtection is null!");
				return false;
			}
			if (b == null) {
				protectionToggle(rp);
			} else if (b.booleanValue()) {
				protectionAdd(rp);
			} else {
				protectionRemove(rp);
			}
		}
		
		return b;
	}

	public boolean protectionToggle(RegionProtection rp) {
		if (rp == null) {
			return protectionSetAll(null);
		}
		if (protections.contains(rp)) {
			protections.remove(rp);
		} else {
			protections.add(rp);
		}
		return protections.contains(rp);
	}
	
	public void protectionRemove(RegionProtection rp) {
		if (rp == null) {
			protectionSetAll(false);
			return;
		}
		protections.remove(rp);
	}

	public void reset() {
		removeEntities();
	}

	public void removeEntities() {
		if (getWorld() == null) {
			return;
		} else if (getWorld().getEntities() == null) {
			return;
		}
		for (Entity e : getWorld().getEntities()) {
			if ((e instanceof Player)
					|| (!contains(new PABlockLocation(e.getLocation().getWorld().getName(), e.getLocation().getBlockX(), e.getLocation().getBlockY(), e.getLocation().getBlockZ())))) {
				continue;
			}
			e.remove();
		}
		// TODO cancel region timer
	}
	
	public void timerStart() {
		// TODO insert proper timer wrapper that knows when it should be off
	}

	public static RegionShape getShapeFromShapeName(String string) {
		for (RegionShape r : RegionShape.values()) {
			if (r.name().startsWith(string.toUpperCase())) {
				return r;
			}
		}
		return RegionShape.CUBOID;
	}

	public static ArenaRegion create(Arena arena, String name, RegionShape shape,
			PABlockLocation[] locs) {
		return new CuboidRegion(arena, name, locs);
	}

	public String update(String key, String value) {
		if (key.toLowerCase().equals("height")) {
			int h = 0;
			try {
				h = Integer.parseInt(value);
			} catch (Exception e) {
				return Language.parse("error.numeric", value); //TODO lang
			}
			
			getLocs()[0].setY(getCenter().getY()-(h >> 1));
			getLocs()[1].setY(getLocs()[0].getY() + h);

			return Language.parse("region.height", value); //TODO lang
		} else if (key.toLowerCase().equals("radius")) {
			int r = 0;
			try {
				r = Integer.parseInt(value);
			} catch (Exception e) {
				return Language.parse("error.numeric", value); //TODO lang
			}
			
			PABlockLocation loc = getCenter();

			getLocs()[0].setX(loc.getX()-r);
			getLocs()[0].setY(loc.getY()-r);
			getLocs()[0].setZ(loc.getZ()-r);

			getLocs()[1].setX(loc.getX()+r);
			getLocs()[1].setY(loc.getY()+r);
			getLocs()[1].setZ(loc.getZ()+r);

			return Language.parse("region.radius", value); //TODO lang
		} else if (key.toLowerCase().equals("position")) {
			return null; //TODO FIX!
		}
		
		return Language.parse("error.argument", key, "height | radius | position"); //TODO lang
	}

	public RegionType getType() {
		return type;
	}

	public void initTimer() {
		if (!getArena().isFightInProgress()) {
			getArena().setFightInProgress(true);
			System.out.print("[PA-debug] fight not yet in progress...");
		}
		RegionRunnable rr = new RegionRunnable(this,0);
		int TICK_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, rr,
				getArena().getArenaConfig().getInt("region.timer") * 1L,
				getArena().getArenaConfig().getInt("region.timer") * 1L);
		rr.setId(TICK_ID);
	}

	public Arena getArena() {
		return arena;
	}

	public void setArena(Arena arena) {
		this.arena = arena;
	}

	public RegionShape getShape() {
		return shape;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setShape(RegionShape shape) {
		this.shape = shape;
	}

	public void setType(RegionType type) {
		this.type = type;
	}
	

	public void tick() {
		for (ArenaPlayer ap : arena.getFighters()) {
			PABlockLocation pLoc = new PABlockLocation(ap.get().getLocation());
			if (flags.contains(RegionFlag.DEATH)) {
				if (this.contains(pLoc)) {
					Arenas.tellPlayer(ap.get(), Language.parse("deathregion"));
					arena.playerLeave(ap.get(), "lose");
					PALoseEvent e = new PALoseEvent(arena, ap.get());
					Bukkit.getPluginManager().callEvent(e);
				}
			}
			if (flags.contains(RegionFlag.WIN)) {
				if (this.contains(pLoc)) {
					for (ArenaTeam team : arena.getTeams()) {
						if (team.getTeamMembers().contains(ap)) {
							// skip winner
							continue;
						}
						for (ArenaPlayer ap2 : team.getTeamMembers()) {
							if (ap2.getStatus().equals(Status.FIGHT)) {
								Bukkit.getWorld(world).strikeLightningEffect(ap2.get().getLocation());
								EntityDamageEvent e = new EntityDamageEvent(ap2.get(), DamageCause.LIGHTNING, 10);
								PlayerListener.commitPlayerDeath(arena, ap2.get(), e);
							}
						}
						return;
					}
				}
			}
			if (flags.contains(RegionFlag.LOSE)) {
				if (this.contains(pLoc)) {
					for (ArenaTeam team : arena.getTeams()) {
						if (!team.getTeamMembers().contains(ap)) {
							// skip winner
							continue;
						}
						for (ArenaPlayer ap2 : team.getTeamMembers()) {
							if (ap2.getStatus().equals(Status.FIGHT)) {
								Bukkit.getWorld(world).strikeLightningEffect(ap2.get().getLocation());
								EntityDamageEvent e = new EntityDamageEvent(ap2.get(), DamageCause.LIGHTNING, 10);
								PlayerListener.commitPlayerDeath(arena, ap2.get(), e);
							}
						}
						return;
					}
				}
			}
			if (flags.contains(RegionFlag.NOCAMP)) {
				if (this.contains(pLoc)) {
					Location loc = playerNameLocations.get(ap.getName());
					if (loc == null) {
						Arenas.tellPlayer(ap.get(),
								Language.parse("nocampregion"));
					} else {
						if (loc.distance(ap.get().getLocation()) < 3) {
							ap.get().damage(
									arena.getArenaConfig().getInt("region.spawncampdamage"));
						}
					}
					playerNameLocations.put(ap.getName(), ap.get()
							.getLocation().getBlock().getLocation());
				} else {
					playerNameLocations.remove(ap.getName());
				}
			}
			if (type.equals(RegionType.BATTLE)) {
				if (!ap.getStatus().equals(Status.FIGHT)) {
					continue;
				}
				
				if (!this.contains(pLoc)) {
					Arenas.tellPlayer(ap.get(), Language.parse("youescaped"));
					arena.playerLeave(ap.get(), "exit");
				}
			}
			if (type.equals(RegionType.WATCH)) {
				HashSet<ArenaPlayer> plyrs = new HashSet<ArenaPlayer>();
				for (ArenaPlayer ap2 : arena.getFighters()) {
					plyrs.add(ap2);
				}

				if (!ap.getStatus().equals(Status.WATCH)) {
					continue;
				}
				
				if (!this.contains(pLoc)) {
					Arenas.tellPlayer(ap.get(), Language.parse("youescaped"));
					arena.playerLeave(ap.get(), "exit");
				}
			}
		}
	}
	
	public String version() {
		return "OUTDATED";
	}

	public PABlockLocation[] getLocs() {
		return locs;
	}
}
