package net.slipcor.pvparena.loadables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.ncloader.NCBLoadable;
import net.slipcor.pvparena.regions.CuboidRegion;
import net.slipcor.pvparena.regions.CylindricRegion;
import net.slipcor.pvparena.regions.SphericRegion;
import net.slipcor.pvparena.runnables.RegionRunnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;


/**
 * <pre>Arena Region Shape class</pre>
 * 
 * The framework for adding region shapes to an arena
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public abstract class ArenaRegionShape extends NCBLoadable implements Cloneable {
	protected static Debug db = new Debug(34);
	protected String world;
	protected RegionShape shape;
	private Arena arena;
	private String name;
	private RegionType type;
	private int tickID = -1;
	private HashSet<RegionFlag> flags = new HashSet<RegionFlag>();
	private HashSet<RegionProtection> protections = new HashSet<RegionProtection>();
	private HashMap<String, Location> playerNameLocations = new HashMap<String, Location>();
	
	private static HashSet<Material> noWools = new HashSet<Material>();

	protected final PABlockLocation[] locs;

	static {
		noWools.add(Material.CHEST);
	}
	
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
	 * JOIN   => the join region
	 * SPAWN  => the spawn region
	 * </pre>
	 */
	public static enum RegionType {
		CUSTOM, WATCH, LOUNGE, BATTLE, JOIN, SPAWN;

		public static RegionType guessFromName(String name) {
			name = name.toUpperCase();
			for (RegionType rt : values()) {
				if (name.endsWith(rt.name()) || name.startsWith(rt.name())) {
					return rt;
				}
			}
			return CUSTOM;
		}
	}

	/**
	 * RegionFlag for tick events
	 * 
	 * <pre>
	 * NOCAMP -   players not moving will be damaged
	 * DEATH -    players being here will die
	 * WIN -      players being here will win
	 * LOSE -     players being here will lose
	 * NODAMAGE - players being here will receive no damage
	 * </pre>
	 */
	public static enum RegionFlag {
		NOCAMP, DEATH, WIN, LOSE, NODAMAGE
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
		BREAK, FIRE, MOBS, NATURE, PAINTING, PISTON, PLACE, TNT, TNTBREAK, DROP, INVENTORY, PICKUP
	}

	/**
	 * region position for physical orientation
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

	/**
	 * check if an arena has overlapping battlefield region with another arena
	 * 
	 * @param a1
	 *            the arena to check
	 * @param a2
	 *            the arena to check
	 * @return true if it does not overlap, false otherwise
	 */
	public static boolean checkRegion(Arena a1, Arena a2) {

		HashSet<ArenaRegionShape> ars1 = a1.getRegionsByType(RegionType.BATTLE);
		HashSet<ArenaRegionShape> ars2 = a2.getRegionsByType(RegionType.BATTLE);
		
		if (ars1.size() < 0 || ars2.size() < 1) {
			return true;
		}
		
		for (ArenaRegionShape ar1 : ars1) {
			for (ArenaRegionShape ar2 : ars2) {
				if (ar1.overlapsWith(ar2)) {
					return false;
				}
			}
		}
		
		return true;
	}

	/**
	 * check if other running arenas are interfering with this arena
	 * 
	 * @return true if no running arena is interfering with this arena, false
	 *         otherwise
	 */
	public static boolean checkRegions(Arena arena) {
		if (!arena.getArenaConfig().getBoolean(CFG.USES_OVERLAPCHECK)) {
			return true;
                }
		db.i("checking regions");

		return ArenaManager.checkRegions(arena);
	}

	/**
	 * check if an admin tries to set an arena position
	 * 
	 * @param event
	 *            the interact event to hand over
	 * @param player
	 *            the player interacting
	 * @return true if the position is being saved, false otherwise
	 */
	public static boolean checkRegionSetPosition(PlayerInteractEvent event,
			Player player) {
		if (!PAA_Region.activeSelections.containsKey(player.getName())) {
			return false;
		}
		Arena arena = PAA_Region.activeSelections.get(player.getName());
		if (arena != null
				&& (PVPArena.hasAdminPerms(player) || (PVPArena.hasCreatePerms(
						player, arena)))
				&& (player.getItemInHand() != null)
				&& (player.getItemInHand().getTypeId() == arena
						.getArenaConfig().getInt(CFG.GENERAL_WAND))) {
			// - modify mode is active
			// - player has admin perms
			// - player has wand in hand
			db.i("modify&adminperms&wand", player);
			ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				ap.setSelection(event.getClickedBlock().getLocation(), false);
				arena.msg(player, Language.parse(MSG.REGION_POS1));
				event.setCancelled(true); // no destruction in creative mode :)
				return true; // left click => pos1
			}

			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				ap.setSelection(event.getClickedBlock().getLocation(), true);
				arena.msg(player, Language.parse(MSG.REGION_POS2));
				return true; // right click => pos2
			}
		}
		return false;
	}

	public static ArenaRegionShape create(Arena arena, String name,
			RegionShape shape, PABlockLocation[] locs) {
		
		db.i("public static ArenaRegionShape create");
		if (shape.equals(RegionShape.CUBOID)) {
			return new CuboidRegion(arena, name, locs);
		} else if (shape.equals(RegionShape.SPHERIC)) {
			return new SphericRegion(arena, name, locs);
		} else if (shape.equals(RegionShape.CYLINDRIC)) {
			return new CylindricRegion(arena, name, locs);
		}
		throw new UnsupportedOperationException("Arena Shape unknown: " + shape);
	}

	public static RegionShape getShapeFromShapeName(String string) {
		for (RegionShape r : RegionShape.values()) {
			if (r.name().startsWith(string.toUpperCase())) {
				return r;
			}
		}
		return RegionShape.CUBOID;
	}

	/**
	 * is a player to far away to join?
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is too far away, false otherwise
	 */
	public static boolean tooFarAway(Arena arena, Player player) {
		int joinRange = arena.getArenaConfig().getInt(CFG.JOIN_RANGE);
		if (joinRange < 1) {
			return false;
                }
		HashSet<ArenaRegionShape> ars = arena.getRegionsByType(RegionType.BATTLE);
		
		if (ars.size() < 1) {
			return SpawnManager.getRegionCenter(arena).getDistance(
					new PABlockLocation(player.getLocation())) > joinRange;
		}
		
		for (ArenaRegionShape ar : ars) {
			if (!ar.tooFarAway(joinRange,
				player.getLocation())) {
				return false;
			}
		}
		
		return true;
	}

	public ArenaRegionShape(Arena arena, String name, PABlockLocation[] locs) {
		super("cuboid");
		this.setName(name);
		this.world = locs[0].getWorldName();
		this.locs = locs;
		this.setArena(arena);

		this.type = RegionType.CUSTOM;
	}

	public ArenaRegionShape(Arena arena, String name, PABlockLocation[] locs,
			String shape) {
		super("cuboid");
		this.setName(name);
		this.world = locs[0].getWorldName();
		this.locs = locs;
		this.setArena(arena);
		
		this.shape = ArenaRegionShapeManager.getShapeByName(shape);
		this.type = RegionType.CUSTOM;
	}

	public ArenaRegionShape(String regionShape) {
		super(regionShape);
		world = null;
		locs = new PABlockLocation[2];
		this.type = RegionType.CUSTOM;
	}

	public abstract boolean contains(PABlockLocation loc);

	public abstract PABlockLocation getCenter();

	public abstract PABlockLocation getMaximumLocation();

	public abstract PABlockLocation getMinimumLocation();

	public abstract boolean overlapsWith(ArenaRegionShape other);

	public abstract void showBorder(Player player);

	public abstract boolean tooFarAway(int joinRange, Location location);

	public void applyFlags(int f) {
		for (RegionFlag rf : RegionFlag.values())
			if ((f & (int)Math.pow(2, rf.ordinal())) != 0) {
				flags.add(rf);
                        }
	}

	public void applyProtections(int p) {
		for (RegionProtection rp : RegionProtection.values()) {
			if ((p & (int)Math.pow(2, rp.ordinal())) != 0) {
				protections.add(rp);
			} else {
				protections.remove(rp);
			}
		}
	}
	
	
	@Override
	public ArenaRegionShape clone() {
		return (ArenaRegionShape) super.clone();
	}

	public void displayInfo(CommandSender sender) {
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

	public Arena getArena() {
		return arena;
	}

	public HashSet<RegionFlag> getFlags() {
		return flags;
	}

	public PABlockLocation[] getLocs() {
		return locs;
	}

	public HashSet<RegionProtection> getProtections() {
		return protections;
	}

	public String getRegionName() {
		return name;
	}

	public RegionShape getShape() {
		return shape;
	}

	public RegionType getType() {
		return type;
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

	public void initTimer() {
		if (!getArena().isFightInProgress() && !this.type.equals(RegionType.JOIN)) {
			getArena().setFightInProgress(true);
		}

		RegionRunnable rr = new RegionRunnable(this);
		tickID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, rr,
				getArena().getArenaConfig().getInt(CFG.TIME_REGIONTIMER) * 1L,
				getArena().getArenaConfig().getInt(CFG.TIME_REGIONTIMER) * 1L);
		rr.setId(tickID);
	}
	
	protected boolean isInNoWoolSet(Block b) {
		return noWools.contains(b.getType());
	}

	public boolean isInRange(int offset, PABlockLocation loc) {
		if (!world.equals(loc.getWorldName())) {
			return false;
                }

		PABlockLocation bvdiff = getCenter();

		return (offset < bvdiff.getDistance(loc));
	}

	public void onThisLoad() {
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
				getArena().msg(Bukkit.getConsoleSender(),
						"&cWarning! RegionProtection is null!");
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

		return true;
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
		if (getWorld() == null || getWorld().getEntities() == null) {
			return;
		}
		
		Iterator<Entity> ie = getWorld().getEntities().iterator();
		
		while (ie.hasNext()) {
			Entity e = ie.next();
			if ((e instanceof Player)
					|| (!contains(new PABlockLocation(e.getLocation()
							.getWorld().getName(), e.getLocation().getBlockX(),
							e.getLocation().getBlockY(), e.getLocation()
									.getBlockZ())))) {
				continue;
			}
			e.remove();
		}
		Bukkit.getScheduler().cancelTask(tickID);
		tickID = -1;
	}

	public void saveToConfig() {
		arena.getArenaConfig().setManually("arenaregion." + name, Config.parseToString(this, flags, protections));
		arena.getArenaConfig().save();
	}

	public void setArena(Arena arena) {
		this.arena = arena;
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
					Arena.pmsg(ap.get(), Language.parse(MSG.NOTICE_YOU_DEATH));
					ap.get().setLastDamageCause(new EntityDamageEvent(ap.get(), DamageCause.CUSTOM, 1000));
					ap.get().damage(1000);
				}
			}
			if (flags.contains(RegionFlag.WIN)) {
				if (this.contains(pLoc)) {
					for (ArenaTeam team : arena.getTeams()) {
						if (!arena.isFreeForAll() && team.getTeamMembers().contains(ap)) {
							// skip winning team
							continue;
						}
						for (ArenaPlayer ap2 : team.getTeamMembers()) {
							if (arena.isFreeForAll() && ap2.getName().equals(ap.getName())) {
								continue;
							}
							if (ap2.getStatus().equals(Status.FIGHT)) {
								Bukkit.getWorld(world).strikeLightningEffect(
										ap2.get().getLocation());
								EntityDamageEvent e = new EntityDamageEvent(
										ap2.get(), DamageCause.LIGHTNING, 10);
								PlayerListener.finallyKillPlayer(arena,
										ap2.get(), e);
							}
						}
						return;
					}
				}
			}
			if (flags.contains(RegionFlag.LOSE)) {
				if (this.contains(pLoc)) {
					if (arena.isFreeForAll()) {
						if (ap.getStatus().equals(Status.FIGHT)) {
							Bukkit.getWorld(world).strikeLightningEffect(
									ap.get().getLocation());
							EntityDamageEvent e = new EntityDamageEvent(
									ap.get(), DamageCause.LIGHTNING, 10);
							PlayerListener.finallyKillPlayer(arena,
									ap.get(), e);
						}
					} else {
						for (ArenaTeam team : arena.getTeams()) {
							if (!team.getTeamMembers().contains(ap)) {
								// skip winner
								continue;
							}
							for (ArenaPlayer ap2 : team.getTeamMembers()) {
								if (ap2.getStatus().equals(Status.FIGHT)) {
									Bukkit.getWorld(world).strikeLightningEffect(
											ap2.get().getLocation());
									EntityDamageEvent e = new EntityDamageEvent(
											ap2.get(), DamageCause.LIGHTNING, 10);
									PlayerListener.finallyKillPlayer(arena,
											ap2.get(), e);
								}
							}
							return;
						}
					}
				}
			}
			if (flags.contains(RegionFlag.NOCAMP)) {
				if (this.contains(pLoc)) {
					Location loc = playerNameLocations.get(ap.getName());
					if (loc == null) {
						Arena.pmsg(ap.get(),
								Language.parse(MSG.NOTICE_YOU_NOCAMP));
					} else {
						if (loc.distance(ap.get().getLocation()) < 3) {
							ap.get().setLastDamageCause(new EntityDamageEvent(ap.get(), DamageCause.CUSTOM, 1000));
							ap.get().damage(
									arena.getArenaConfig().getInt(
											CFG.DAMAGE_SPAWNCAMP));
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
					Arena.pmsg(ap.get(), Language.parse(MSG.NOTICE_YOU_ESCAPED));
					if (arena.getArenaConfig().getBoolean(CFG.GENERAL_LEAVEDEATH)) {
						ap.get().setLastDamageCause(new EntityDamageEvent(ap.get(), DamageCause.CUSTOM, 1000));
						//ap.get().setHealth(0);
						ap.get().damage(1000);
					} else {
						arena.playerLeave(ap.get(), CFG.TP_EXIT, false);
					}
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
					Arena.pmsg(ap.get(), Language.parse(MSG.NOTICE_YOU_ESCAPED));
					arena.playerLeave(ap.get(), CFG.TP_EXIT, false);
				}
			}
			if (type.equals(RegionType.LOUNGE)) {
				HashSet<ArenaPlayer> plyrs = new HashSet<ArenaPlayer>();
				for (ArenaPlayer ap2 : arena.getEveryone()) {
					plyrs.add(ap2);
				}

				if (!ap.getStatus().equals(Status.READY) && !ap.getStatus().equals(Status.LOUNGE)) {
					continue;
				}

				if (!this.contains(pLoc)) {
					Arena.pmsg(ap.get(), Language.parse(MSG.NOTICE_YOU_ESCAPED));
					arena.playerLeave(ap.get(), CFG.TP_EXIT, false);
				}
			}
		}
		if (arena.getArenaConfig().getBoolean(CFG.JOIN_FORCE) &&
				type.equals(RegionType.JOIN) &&
				!arena.isFightInProgress() &&
				!arena.isLocked()) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				ArenaPlayer ap = ArenaPlayer.parsePlayer(p.getName());
				if (ap.getArena() != null) {
					continue;
				}
				if (this.contains(new PABlockLocation(p.getLocation()))) {
					PAG_Join cmd = new PAG_Join();
					cmd.commit(arena, p, new String[]{name.replace("-join", "")});
				}
			}
		}
	}

	public void toggleActivity() {
		throw new IllegalStateException("Module not up to date: " + getName());
	}

	public String update(String key, String value) {
		// usage: /pa {arenaname} region [regionname] radius [number]
		// usage: /pa {arenaname} region [regionname] height [number]
		// usage: /pa {arenaname} region [regionname] position [position]
		// usage: /pa {arenaname} region [regionname] flag [flag]
		// usage: /pa {arenaname} region [regionname] type [regiontype]
		
		if (key.toLowerCase().equals("height")) {
			int h = 0;
			try {
				h = Integer.parseInt(value);
			} catch (Exception e) {
				return Language.parse(MSG.ERROR_NOT_NUMERIC, value);
			}

			getLocs()[0].setY(getCenter().getY() - (h >> 1));
			getLocs()[1].setY(getLocs()[0].getY() + h);

			return Language.parse(MSG.REGION_HEIGHT, value);
		} else if (key.toLowerCase().equals("radius")) {
			int r = 0;
			try {
				r = Integer.parseInt(value);
			} catch (Exception e) {
				return Language.parse(MSG.ERROR_NOT_NUMERIC, value);
			}

			PABlockLocation loc = getCenter();

			getLocs()[0].setX(loc.getX() - r);
			getLocs()[0].setY(loc.getY() - r);
			getLocs()[0].setZ(loc.getZ() - r);

			getLocs()[1].setX(loc.getX() + r);
			getLocs()[1].setY(loc.getY() + r);
			getLocs()[1].setZ(loc.getZ() + r);

			return Language.parse(MSG.REGION_RADIUS, value);
		} else if (key.toLowerCase().equals("position")) {
			return null; // TODO insert function to align the arena based on a position setting.
			//TODO see SETUP.creole
		}

		return Language.parse(MSG.ERROR_ARGUMENT, key,
				"height | radius | position");
	}

	public String version() {
		return "OUTDATED";
	}
}
