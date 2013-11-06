package net.slipcor.pvparena.loadables;

import java.util.HashSet;
import java.util.Set;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.ncloader.NCBLoadable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * <pre>
 * Arena Region Shape class
 * </pre>
 * 
 * The framework for adding region shapes to an arena
 * 
 * @author slipcor
 */

public abstract class ArenaRegionShape extends NCBLoadable implements Cloneable {
	
	public ArenaRegionShape(String name) {
		super(name);
		world = null;
		locs = new PABlockLocation[2];
		this.type = RegionType.CUSTOM;
	}
	
	/**
	 * 
	 * START LEGACY STUFF
	 */
	@Deprecated
	private Arena arena;
	@Deprecated
	private Set<RegionProtection> protections = new HashSet<RegionProtection>();
	@Deprecated
	private Set<RegionFlag> flags = new HashSet<RegionFlag>();
	@Deprecated
	private String name;
	

	@Deprecated
	protected static Debug debug = new Debug(34);
	@Deprecated
	protected String world;
	@Deprecated
	protected ArenaRegionShape.RegionShape shape;
	@Deprecated
	protected PABlockLocation[] locs;
	@Deprecated
	protected RegionType type;
	@Deprecated
	public static enum RegionShape {
		CUBOID, SPHERIC, CYLINDRIC;
	}
	@Deprecated
	public static enum RegionType {
		CUSTOM, WATCH, LOUNGE, BATTLE, JOIN, SPAWN, BL_INV, WL_INV;

		public static RegionType guessFromName(final String regionName) {
			final String name = regionName.toUpperCase();
			for (RegionType rt : values()) {
				if (name.endsWith(rt.name()) || name.startsWith(rt.name())) {
					return rt;
				}
			}
			return CUSTOM;
		}
	}
	@Deprecated
	public static enum RegionFlag {
		NOCAMP, DEATH, WIN, LOSE, NODAMAGE
	}
	@Deprecated
	public static enum RegionProtection {
		BREAK, FIRE, MOBS, NATURE, PAINTING, PISTON, PLACE, TNT, TNTBREAK, DROP, INVENTORY, PICKUP
	}
	@Deprecated
	public static enum RegionPosition {
		CENTER, NORTH, EAST, SOUTH, WEST, TOP, BOTTOM, INSIDE, OUTSIDE
	}
	@Deprecated
	public static boolean checkRegion(final Arena region1, final Arena region2) {
		return ArenaRegion.checkRegion(region1, region2);
	}
	@Deprecated
	public static boolean checkRegions(final Arena arena) {
		return ArenaRegion.checkRegions(arena);
	}
	@Deprecated
	public static boolean checkRegionSetPosition(final PlayerInteractEvent event,
			final Player player) {
		return ArenaRegion.checkRegionSetPosition(event, player);
	}

	@Deprecated
	public static ArenaRegionShape create(final Arena arena, final String name,
			final RegionShape shape, final PABlockLocation[] locs) {
		return new ArenaRegion(arena, name, ArenaRegionShapeManager.getShapeByName(shape.name()), locs).getShape();
	}

	@Deprecated
	public static RegionShape getShapeFromShapeName(final String string) {
		for (RegionShape r : RegionShape.values()) {
			if (r.name().startsWith(string.toUpperCase())) {
				return r;
			}
		}
		return RegionShape.CUBOID;
	}
	@Deprecated
	public static boolean tooFarAway(final Arena arena, final Player player) {
		return ArenaRegion.tooFarAway(arena, player);
	}

	@Deprecated
	public ArenaRegionShape(final Arena arena, final String name, final PABlockLocation[] locs) {
		super("cuboid");
		this.setName(name);
		this.world = locs[0].getWorldName();
		this.locs = locs.clone();
		this.setArena(arena);

		this.type = RegionType.CUSTOM;
	}

	@Deprecated
	public ArenaRegionShape(final Arena arena, final String name, final PABlockLocation[] locs,
			final String shape) {
		super("cuboid");
		this.setName(name);
		this.world = locs[0].getWorldName();
		this.locs = locs.clone();
		this.setArena(arena);

		this.shape = ArenaRegionShapeManager.getShapeByName(shape).getShape();
		this.type = RegionType.CUSTOM;
	}

	@Deprecated
	public void applyFlags(final int flags) {
		this.getArena().getRegion(this.getName()).applyFlags(flags);
	}

	@Deprecated
	public void applyProtections(final int protections) {
		this.getArena().getRegion(this.getName()).applyProtections(protections);
	}

	@Deprecated
	public void flagAdd(final RegionFlag regionFlag) {
		this.getArena().getRegion(this.getName()).flagAdd(ArenaRegion.RegionFlag.valueOf(regionFlag.name()));
	}

	@Deprecated
	public boolean flagToggle(RegionFlag regionFlag) {
		return this.getArena().getRegion(this.getName()).flagToggle(ArenaRegion.RegionFlag.valueOf(regionFlag.name()));
	}

	@Deprecated
	public void flagRemove(final RegionFlag regionFlag) {
		this.getArena().getRegion(this.getName()).flagRemove(ArenaRegion.RegionFlag.valueOf(regionFlag.name()));
	}

	@Deprecated
	public Arena getArena() {
		return arena;
	}

	@Deprecated
	public Set<RegionFlag> getFlags() {
		return flags;
	}

	@Deprecated
	public PABlockLocation[] getLocs() {
		return locs.clone();
	}

	@Deprecated
	public Set<RegionProtection> getProtections() {
		return protections;
	}

	@Deprecated
	public String getRegionName() {
		return name;
	}

	@Deprecated
	public RegionShape getShape() {
		return shape;
	}

	@Deprecated
	public RegionType getType() {
		return type;
	}

	@Deprecated
	public World getWorld() {
		return Bukkit.getWorld(world);
	}

	@Deprecated
	public String getWorldName() {
		return world;
	}

	@Deprecated
	public void initTimer() {
		this.getArena().getRegion(this.getName()).initTimer();
	}

	@Deprecated
	protected boolean isInNoWoolSet(final Block block) {
		return this.getArena().getRegion(this.getName()).isInNoWoolSet(block);
	}

	@Deprecated
	public boolean isInRange(final int offset, final PABlockLocation loc) {
		if (!world.equals(loc.getWorldName())) {
			return false;
		}

		return (offset*offset < getCenter().getDistanceSquared(loc));
	}

	@Deprecated
	public void protectionAdd(final RegionProtection regionProtection) {
		if (regionProtection == null) {
			protectionSetAll(true);
			return;
		}
		protections.add(regionProtection);
	}

	@Deprecated
	public boolean protectionSetAll(final Boolean value) {
		for (RegionProtection rp : RegionProtection.values()) {
			if (rp == null) {
				getArena().msg(Bukkit.getConsoleSender(),
						"&cWarning! RegionProtection is null!");
				return false;
			}
			if (value == null) {
				protectionToggle(rp);
			} else if (value.booleanValue()) {
				protectionAdd(rp);
			} else {
				protectionRemove(rp);
			}
		}

		return true;
	}

	@Deprecated
	public boolean protectionToggle(final RegionProtection regionProtection) {
		if (regionProtection == null) {
			return protectionSetAll(null);
		}
		if (protections.contains(regionProtection)) {
			protections.remove(regionProtection);
		} else {
			protections.add(regionProtection);
		}
		return protections.contains(regionProtection);
	}

	@Deprecated
	public void protectionRemove(final RegionProtection regionProtection) {
		if (regionProtection == null) {
			protectionSetAll(false);
			return;
		}
		protections.remove(regionProtection);
	}

	@Deprecated
	public void reset() {
		removeEntities();
	}

	@Deprecated
	public void removeEntities() {
		this.getArena().getRegion(this.getName()).removeEntities();
	}

	@Deprecated
	public void saveToConfig() {
		this.getArena().getRegion(this.getName()).saveToConfig();
	}

	@Deprecated
	public final void setArena(final Arena arena) {
		this.arena = arena;
	}

	@Deprecated
	public final void setName(final String name) {
		this.name = name;
	}

	@Deprecated
	public void setShape(final RegionShape shape) {
		this.shape = shape;
	}

	@Deprecated
	public void setType(final RegionType type) {
		this.type = type;
	}

	@Deprecated
	public void tick() {
		this.getArena().getRegion(this.getName()).tick();
	}

	@Deprecated
	public String update(final String key, final String value) {
		return this.getArena().getRegion(this.getName()).update(key, value);
	}
	
	/**
	 * 
	 * END LEGACY STUFF
	 */

	public abstract boolean contains(PABlockLocation loc);

	public abstract PABlockLocation getCenter();

	public abstract PABlockLocation getMaximumLocation();

	public abstract PABlockLocation getMinimumLocation();

	public abstract boolean overlapsWith(ArenaRegion other);

	public abstract void showBorder(Player player);

	public abstract boolean tooFarAway(int joinRange, Location location);

	@Override
	public ArenaRegionShape clone() {
		return (ArenaRegionShape) super.clone();
	}

	public void displayInfo(final CommandSender sender) {
	}

	public String getVersion() {
		return "OUTDATED";
	}

	public void onThisLoad() {
	}

	public void toggleActivity() {
		throw new IllegalStateException("Module not up to date: " + getName());
	}

	public String version() {
		return "OUTDATED";
	}

	public abstract void move(BlockFace direction, int parseInt);

	public abstract void extend(BlockFace direction, int parseInt);

	public abstract void initialize(ArenaRegion region);
}
