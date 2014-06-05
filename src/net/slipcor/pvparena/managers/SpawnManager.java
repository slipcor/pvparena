package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PABlock;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.classes.PASpawn;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
import net.slipcor.pvparena.runnables.RespawnRunnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * <pre>Spawn Manager class</pre>
 * 
 * Provides static methods to manage Spawns
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public final class SpawnManager {
	private final static Debug DEBUG = new Debug(27);
	
	private SpawnManager() {
	}
	
	private static String calculateFarSpawn(final String[] taken,
			final Set<PASpawn> available,
			final Set<PASpawn> total) {
		DEBUG.i("--------------------");
		DEBUG.i("calculating a spawn!");
		DEBUG.i("--------------------");
		double diff = 0;
		String far = null;
		for (PASpawn s : available) {
			far = s.getName();
			break;
		}
		DEBUG.i("last resort: " + far);
		
		for (PASpawn s : available) {
			DEBUG.i("> checking " + s);
			double tempDiff = 0;
			for (int i = 0; (i < taken.length) && (taken[i] != null); i++) {
				for (PASpawn tt : total) {
					for (PASpawn aa : available) {
						if (tt.getName().equals(taken[i])
								&& aa.getName().equals(s)) {
							tempDiff += tt.getLocation().getDistanceSquared(aa.getLocation());
							DEBUG.i(">> tempDiff: " + tempDiff);
						}
					}
				}
				
			}
			
			if (tempDiff > diff) {
				DEBUG.i("-> diff");
				diff = tempDiff;
				far = s.getName();
			}
		}
		
		return far;
	}

	/**
	 * @param arena
	 * @param team
	 */
	public static void distribute(final Arena arena, final ArenaTeam team) {
		final Set<ArenaRegion> ars = arena.getRegionsByType(RegionType.SPAWN);
		
		if (!ars.isEmpty()) {
			placeInsideSpawnRegions(arena, team.getTeamMembers(), ars);
			return;
		}
		
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_QUICKSPAWN)) {
			class TeleportLater extends BukkitRunnable {
				private final Set<ArenaPlayer> teamMembers = new HashSet<ArenaPlayer>();
				private final boolean classSpawn;
				private int pos = 0;
				
				private PASpawn[] locations = null;

				TeleportLater(Set<ArenaPlayer> set) {
					for (ArenaPlayer ap : set) {
						this.teamMembers.add(ap);
					}
					classSpawn = arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN);
				}
				
				@Override
				public void run() {
					
					if (locations == null) {
						final Set<PASpawn> spawns = new HashSet<PASpawn>();
						if (arena.isFreeForAll()) {
							if (team.getName().equals("free")) {
								spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, "spawn"));
							} else {
								spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, team.getName()));
							}
						} else {
							spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, team.getName()+"spawn"));
						}
						arena.getDebugger().i("read spawns for '"+team.getName()+"'; size: "+spawns.size());
						locations = new PASpawn[spawns.size()];
						int pos = 0;
						for (PASpawn spawn : spawns) {
							arena.getDebugger().i("- "+spawn.getName());
							locations[pos++] = spawn;
						}
					}
					
					
					
					for (ArenaPlayer ap : teamMembers) {
						if (classSpawn) {
							
							
							Set<PASpawn> spawns = SpawnManager.getPASpawnsStartingWith(arena, team.getName() + ap.getArenaClass().getName() + "spawn");
							
							int pos = (new Random()).nextInt(spawns.size());
							for (PASpawn spawn : spawns) {
								if (--pos < 0) {
									arena.tpPlayerToCoordName(ap.get(), spawn.getName());
									break;
								}
							}
							
						} else {
							arena.tpPlayerToCoordName(ap.get(), locations[pos++ % locations.length].getName());
						}
						ap.setStatus(Status.FIGHT);
						teamMembers.remove(ap);
						return;
					}
					this.cancel();
				}
				
			}
			(new TeleportLater(team.getTeamMembers())).runTaskTimer(PVPArena.instance, 1L, 1L);
			
			return;
		}
		
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_SMARTSPAWN)) {
			distributeSmart(arena, team.getTeamMembers(), team.getName());
			return;
		}
		distributeByOrder(arena, team.getTeamMembers(), team.getName());
	}

	/**
	 * 
	 * @param arena
	 * @param teamMembers
	 */
	@Deprecated
	public static void distribute(final Arena arena, final Set<ArenaPlayer> teamMembers) {
		ArenaTeam team = null;
		
		for (ArenaPlayer ap : teamMembers) {
			team = ap.getArenaTeam();
			distribute(arena, team);
			return;
		}
	}
	
	private static void distributeByOrder(final Arena arena,
			final Set<ArenaPlayer> set, final String string) {
		arena.getDebugger().i("distributeByOrder: " + string);
		if (set == null || set.size() < 1) {
			return;
		}
		
		final Set<PASpawn> spawns = new HashSet<PASpawn>();
		if (arena.isFreeForAll()) {
			if (string.equals("free")) {
				spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, "spawn"));
			} else {
				spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, string));
			}
		} else {
			spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, string+"spawn"));
		}
		
		if (spawns == null || spawns.size() < 1) {
			return;
		}

		class TeleportLater extends BukkitRunnable {
			private final Set<ArenaPlayer> set = new HashSet<ArenaPlayer>();
			private final boolean classSpawn;

			TeleportLater(Set<ArenaPlayer> set) {
				for (ArenaPlayer ap : set) {
					this.set.add(ap);
				}
				classSpawn = arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN);
			}
			
			@Override
			public void run() {
				for (ArenaPlayer ap : set) {
					ap.setStatus(Status.FIGHT);
					if (classSpawn) {
						
						
						Set<PASpawn> spawns = SpawnManager.getPASpawnsStartingWith(arena, ap.getArenaTeam().getName() + ap.getArenaClass().getName() + "spawn");
						
						int pos = (new Random()).nextInt(spawns.size());
						for (PASpawn spawn : spawns) {
							if (--pos < 0) {
								arena.tpPlayerToCoordName(ap.get(), spawn.getName());
								break;
							}
						}
						
					} else {
						for (PASpawn s : spawns) {
							arena.tpPlayerToCoordName(ap.get(), s.getName());
							if (spawns.size() > 1) {
								spawns.remove(s);
							}
							break;
						}
					}
					set.remove(ap);
					return;
				}
				this.cancel();
			}
			
		}
		(new TeleportLater(set)).runTaskTimer(PVPArena.instance, 1L, 1L);
	}
	
	public static void distributeSmart(final Arena arena,
			final Set<ArenaPlayer> set, final String teamNName) {
		arena.getDebugger().i("distributing smart-ish");
		if (set == null || set.size() < 1) {
			return;
		}

		final Set<PASpawn> locations;
		final Set<PASpawn> total_locations;
		
		if (arena.isFreeForAll()) {
			if (teamNName.equals("free")) {
				locations = getPASpawnsStartingWith(arena, "spawn");
				total_locations= getPASpawnsStartingWith(arena, "spawn");
			} else {
				locations = getPASpawnsStartingWith(arena, teamNName);
				total_locations= getPASpawnsStartingWith(arena, teamNName);
			}
		} else {
			locations = getPASpawnsStartingWith(arena, teamNName+"spawn");
			total_locations= getPASpawnsStartingWith(arena, teamNName+"spawn");
		}
		
		if (locations == null || locations.size() < 1) {
			arena.getDebugger().i("null or less than 1! -> OUT!");
			return;
		}

		String[] iteratings = new String[locations.size()];
		
		for (int i = 0; i < total_locations.size(); i++) {
			if (i == 0) {
				PASpawn innerSpawn = null;
				for (PASpawn ss : locations) {
					innerSpawn = ss;
					break;
				}
				iteratings[i] = innerSpawn.getName();
				locations.remove(innerSpawn);
				continue;
			}
			final String spawnName = calculateFarSpawn(iteratings, locations, total_locations);
			iteratings[i] = spawnName;
			for (PASpawn spawn : locations) {
				if (spawn.getName().equals(spawnName)) {
					locations.remove(spawn);
					break;
				}
			}
			
		}
		
		class TeleportLater extends BukkitRunnable {
			private int pos;
			private final String[] iteratings;
			private final Set<ArenaPlayer> set = new HashSet<ArenaPlayer>();

			TeleportLater(Set<ArenaPlayer> set, String[] iteratings) {
				pos = 0;
				for (ArenaPlayer ap : set) {
					this.set.add(ap);
				}
				this.iteratings = iteratings.clone();
			}
			
			@Override
			public void run() {
				for (ArenaPlayer ap : set) {
					ap.setStatus(Status.FIGHT);
					final String spawnName = iteratings[pos++%iteratings.length];
					if (spawnName == null) {
						PVPArena.instance.getLogger().warning("Element #"+pos+" is null: [" + StringParser.joinArray(iteratings, ",") + "]");
					}
					arena.tpPlayerToCoordName(ap.get(), spawnName);
					set.remove(ap);
					return;
				}
				this.cancel();
			}
			
		}
		
		(new TeleportLater(set, iteratings)).runTaskTimer(PVPArena.instance, 1L, 1L);
	}

	/**
	 * @param locs
	 * @param location
	 * @return
	 */
	public static PABlockLocation getBlockNearest(final Set<PABlockLocation> locs,
			final PABlockLocation location) {
		PABlockLocation result = null;

		for (PABlockLocation loc : locs) {
			if (!loc.getWorldName().equals(location.getWorldName())) {
				continue;
			}
			if (result == null
					|| result.getDistanceSquared(location) > loc.getDistanceSquared(location)) {
				result = loc;
			}
		}

		return result;
	}
	
	public static Set<PABlockLocation> getBlocksStartingWith(final Arena arena, final String name) {
		Set<PABlockLocation> result = new HashSet<PABlockLocation>();
		
		for (PABlock block : arena.getBlocks()) {
			if (block.getName().startsWith(name)) {
				result.add(block.getLocation());
			}
		}
		
		return result;
	}
	
	public static Set<PABlockLocation> getBlocksContaining(final Arena arena, final String name) {
		Set<PABlockLocation> result = new HashSet<PABlockLocation>();
		
		for (PABlock block : arena.getBlocks()) {
			if (block.getName().contains(name)) {
				result.add(block.getLocation());
			}
		}
		
		return result;
	}
	
	public static Set<PABlock> getPABlocksContaining(final Arena arena, final String name) {
		Set<PABlock> result = new HashSet<PABlock>();
		
		for (PABlock block : arena.getBlocks()) {
			if (block.getName().contains(name)) {
				result.add(block);
			}
		}
		
		return result;
	}
	
	public static Set<PALocation> getSpawnsContaining(final Arena arena, final String name) {
		Set<PALocation> result = new HashSet<PALocation>();
		
		for (PASpawn spawn : arena.getSpawns()) {
			if (spawn.getName().contains(name)) {
				result.add(spawn.getLocation());
			}
		}
		
		return result;
	}
	
	public static Set<PALocation> getSpawnsStartingWith(final Arena arena, final String name) {
		Set<PALocation> result = new HashSet<PALocation>();
		
		for (PASpawn spawn : arena.getSpawns()) {
			if (spawn.getName().startsWith(name)) {
				result.add(spawn.getLocation());
			}
		}
		
		return result;
	}
	
	public static Set<PASpawn> getPASpawnsStartingWith(final Arena arena, final String name) {
		Set<PASpawn> result = new HashSet<PASpawn>();
		
		for (PASpawn spawn : arena.getSpawns()) {
			if (spawn.getName().startsWith(name)) {
				result.add(spawn);
			}
		}
		
		return result;
	}

	/**
	 * @param arena
	 * @param sTeam
	 * @return
	 */
	@Deprecated
	public static Set<PABlockLocation> getBlocks(final Arena arena, final String sTeam) {
		
		if ("flags".equals(sTeam)) {
			return getBlocksStartingWith(arena, "flag");
		} else {
			return getBlocksStartingWith(arena, sTeam);
		}
	}
	
	public static PABlockLocation getBlockByName(Arena arena, String name) {
		Set<PABlockLocation> spawns = getBlocksStartingWith(arena, name);
		
		int pos = (new Random()).nextInt(spawns.size());
		
		for (PABlockLocation loc : spawns) {
			if (--pos < 0) {
				return loc;
			}
		}
		
		return null;
	}
	
	public static PALocation getSpawnByName(Arena arena, String name) {
		Set<PALocation> spawns = getSpawnsStartingWith(arena, name);
		
		int pos = (new Random()).nextInt(spawns.size());
		
		for (PALocation loc : spawns) {
			if (--pos < 0) {
				return loc.add(0.5, PVPArena.instance.getConfig().getDouble("y-offset"), 0.5);
			}
		}
		
		return null;
	}
	
	public static PABlockLocation getBlockByExactName(Arena arena, String name) {
		for (PABlock spawn : arena.getBlocks()) {
			if (spawn.getName().equals(name)) {
				return spawn.getLocation();
			}
		}
		return null;
	}
	
	public static PALocation getSpawnByExactName(Arena arena, String name) {
		for (PASpawn spawn : arena.getSpawns()) {
			if (spawn.getName().equals(name)) {
				return spawn.getLocation().add(0.5, PVPArena.instance.getConfig().getDouble("y-offset"), 0.5);
			}
		}
		return null;
	}

	/**
	 * get the location from a coord string
	 * 
	 * @param place
	 *            the coord string
	 * @return the location of that string
	 */
	@Deprecated
	public static PALocation getCoords(final Arena arena, final String place) {
		return getSpawnByName(arena, place);
	}

	/**
	 * get the nearest spawn location from a location
	 * 
	 * @param hashSet
	 *            the spawns to check
	 * @param location
	 *            the location to check
	 * @return the spawn location next to the location
	 */
	@Deprecated
	public static PALocation getNearest(final Set<PALocation> hashSet,
			final PALocation location) {
		return null;
	}
	
	/*

	private static PALocation getSmartCoord(final Arena arena) {
		final Set<ArenaPlayer> players = arena.getFighters();
		final Map<String, PALocation> locs = getSpawnMap(arena, "free");
		
		double diff = 0;
		String spawn = null;
		
		for (String s : locs.keySet()) {
			final PALocation loc = locs.get(s);
			final Location bLoc = loc.toLocation();
			double tempDiff = 0;
			for (ArenaPlayer ap : players) {
				if (ap.get() == null) {
					continue;
				}
				tempDiff += bLoc.distance(ap.get().getLocation());
			}
			if (tempDiff > diff) {
				diff = tempDiff;
				spawn = s;
			}
		}
		
		return locs.get(spawn);
	}
	
	*/

	/**
	 * @param arena
	 * @param sTeam
	 * @return
	 */
	@Deprecated
	public static Map<String, PALocation> getSpawnMap(final Arena arena,
			final String string) {

		Set<PASpawn> spawns = null;
		Set<PABlock> blocks = null; 
		
		if (string.equals("turrets")) {
			// containing turret
			blocks = SpawnManager.getPABlocksContaining(arena, "turret");
		} else if (string.equals("pillar")) {
			// containing pillar
			blocks = SpawnManager.getPABlocksContaining(arena, "pillar");
		} else {
			// respawnrelay, anything starting with string
			spawns = SpawnManager.getPASpawnsStartingWith(arena, string);
		}
		
		if (spawns != null) {
			Map<String, PALocation> result = new HashMap<String, PALocation>();
			for (PASpawn s : spawns) {
				result.put(s.getName(), s.getLocation());
			}
			return result;
		} else if (blocks != null) {
			Map<String, PALocation> result = new HashMap<String, PALocation>();
			for (PABlock b : blocks) {
				result.put(b.getName(), new PALocation(b.getLocation().toLocation()));
			}
			return result;
		}
		return null;
	}

	/**
	 * get all (team) spawns of an arena
	 * 
	 * @param arena
	 *            the arena to check
	 * @param sTeam
	 *            a team name or "flags" or "[team]pumpkin" or "[team]flag"
	 * @return a set of possible spawn matches
	 */
	@Deprecated
	public static Set<PALocation> getSpawns(final Arena arena, final String string) {
		Set<PABlockLocation> locs = new HashSet<PABlockLocation>();
		if (string.endsWith("flag")) {
			locs.addAll(SpawnManager.getBlocksStartingWith(arena, string));
		} else if (string.equals("item")) {
			// item drops
			locs.addAll(SpawnManager.getBlocksContaining(arena, "item"));
		} else {
			locs.addAll(getBlocksStartingWith(arena, string+"spawn"));
		}
		Set<PALocation> result = new HashSet<PALocation>();
		for (PABlockLocation bLoc : locs) {
			result.add(new PALocation(bLoc.toLocation()));
		}
		return result;
	}

	/**
	 * calculate the arena center, including all team spawn locations
	 * 
	 * @param arena
	 * @return
	 */
	public static PABlockLocation getRegionCenter(final Arena arena) {
		final Set<PALocation> locs = new HashSet<PALocation>();
		
		ArenaRegion ars = null;
		for (ArenaRegion a : arena.getRegionsByType(RegionType.BATTLE)) {
			ars = a;
			break;
		}
		
		if (ars == null) {
			return new PABlockLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
		}

		final World world = Bukkit.getWorld(ars.getWorldName());
		
		if (world == null) {
			return new PABlockLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
		}
		
		locs.addAll(getSpawnsContaining(arena, "spawn"));
		
		long x = 0;
		long y = 0;
		long z = 0;

		for (PALocation loc : locs) {
			if (!loc.getWorldName().equals(world.getName())) {
				continue;
			}
			x += loc.getX();
			y += loc.getY();
			z += loc.getZ();
		}

		return new PABlockLocation(world.getName(),
				(int) x / locs.size(), (int) y / locs.size(), (int) z / locs.size());
	}
	
	private static void placeInsideSpawnRegion(final Arena arena, final ArenaPlayer aPlayer,
			final ArenaRegion region) {
		int x = region.getShape().getMinimumLocation().getX();
		int y = region.getShape().getMinimumLocation().getY();
		int z = region.getShape().getMinimumLocation().getZ();
		final Random random = new Random();
		
		boolean found = false;
		int attempt = 0;
		
		PABlockLocation loc = null;
		
		while (!found && attempt < 10) {
		
			x += random.nextInt(region.getShape().getMaximumLocation().getX() - 
				region.getShape().getMinimumLocation().getX());
			y += random.nextInt(region.getShape().getMaximumLocation().getY() - 
				region.getShape().getMinimumLocation().getY());
			z += random.nextInt(region.getShape().getMaximumLocation().getZ() - 
				region.getShape().getMinimumLocation().getZ());
		
			loc = new PABlockLocation(region.getShape().getMinimumLocation().getWorldName(), x, y, z);
			attempt++;
			found = region.getShape().contains(loc);
			
		}
		
		final PABlockLocation newLoc = loc;
		
		class RunLater implements Runnable {
			@Override
			public void run() {
				final PALocation temp = aPlayer.getSavedLocation();
				
				Location bLoc = newLoc.toLocation();
				bLoc = bLoc.add(0.5, PVPArena.instance.getConfig().getDouble("y-offset"), 0.5);
				
				while (bLoc.getBlock().getType() != Material.AIR
						&& bLoc.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR
						&& bLoc.getBlock().getRelative(BlockFace.UP, 2).getType() != Material.AIR) {
					bLoc = bLoc.add(0, 1, 0);
				}
	
				aPlayer.setLocation(new PALocation(bLoc));
				
				aPlayer.setStatus(Status.FIGHT);
				arena.tpPlayerToCoordName(aPlayer.get(), "old");
				aPlayer.setLocation(temp);
				
			}
			
		}
		
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
		
	}
	
	private static void placeInsideSpawnRegions(final Arena arena, final Set<ArenaPlayer> set,
			final Set<ArenaRegion> ars) {
		if (arena.isFreeForAll()) {
			for (ArenaPlayer ap : set) {
				int pos = new Random().nextInt(ars.size());
			
				for (ArenaRegion x : ars) {
					if (pos-- == 0) {
						placeInsideSpawnRegion(arena, ap, x);
						break;
					}
				}
			}
		} else {
			String teamName = null;
			for (ArenaPlayer ap : set) {
				if (teamName == null) {
					teamName = ap.getArenaTeam().getName();
				}
				for (ArenaRegion x : ars) {
					if (x.getRegionName().contains(teamName)) {
						placeInsideSpawnRegion(arena, ap, x);
						break;
					}
				}
			}
		}
	}

	/**
	 * is a player near a spawn?
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player to check
	 * @param diff
	 *            the distance to check
	 * @return true if the player is near, false otherwise
	 */
	public static boolean isNearSpawn(final Arena arena, final Player player, final int diff) {

		arena.getDebugger().i("checking if arena is near a spawn", player);
		if (!arena.hasPlayer(player)) {
			return false;
		}
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final ArenaTeam team = aPlayer.getArenaTeam();
		if (team == null) {
			return false;
		}

		Set<PALocation> spawns = new HashSet<PALocation>();
		
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
			spawns.addAll(SpawnManager.getSpawnsContaining(arena, team.getName() + aPlayer.getArenaClass().getName() + "spawn"));
		} else if (arena.isFreeForAll()){
			spawns.addAll(SpawnManager.getSpawnsStartingWith(arena, "spawn"));
		} else {
			spawns.addAll(SpawnManager.getSpawnsStartingWith(arena, team.getName() + "spawn"));
		}

		for (PALocation loc : spawns) {
			if (loc.getDistanceSquared(new PALocation(player.getLocation())) <= diff*diff) {
				arena.getDebugger().i("found near spawn: " + loc.toString(), player);
				return true;
			}
		}
		return false;
	}

	/**
	 * @param arena
	 * @param aPlayer
	 */
	@Deprecated
	public static void respawn(final Arena arena, final ArenaPlayer aPlayer) {
		respawn(arena, aPlayer, null);
	}

	public static void respawn(final Arena arena, final ArenaPlayer aPlayer, String overrideSpawn) {

		if (arena == null) {
			PVPArena.instance.getLogger().warning("Arena is null for player " + aPlayer + " while respawning!");
			return;
		}
		
		if (overrideSpawn == null) {
			
			
			if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)
					&& (arena.isFreeForAll() == aPlayer.getArenaTeam().getName().equals("free"))) {
				
				// we want a class spawn and the arena is either not FFA or the player is in the FREE team
				
				final Set<PASpawn> spawns = SpawnManager.getPASpawnsStartingWith(arena, aPlayer.getArenaTeam().getName()+aPlayer.getArenaClass().getName()+"spawn");
				int pos = (new Random()).nextInt(spawns.size());
				for (PASpawn spawn : spawns) {
					if (--pos <= 0) {
						Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RespawnRunnable(arena, aPlayer, spawn.getName()), 1L);
						aPlayer.setStatus(Status.FIGHT);
						return;
					}
				}
				
				return;
			}
			
			final Set<ArenaRegion> ars = arena.getRegionsByType(RegionType.SPAWN);
			if (!ars.isEmpty()) {
				final Set<ArenaPlayer> team = new HashSet<ArenaPlayer>();
				team.add(aPlayer);
				placeInsideSpawnRegions(arena, team, ars);
				
				return;
			}
			if (arena.isFreeForAll()) {
				
				
				if (!arena.getArenaConfig().getBoolean(CFG.GENERAL_SMARTSPAWN)
						|| (!aPlayer.getArenaTeam().getName().equals("free"))) {
					
					// either we generally don't need smart spawning or the player is not in the "free" team ;)
					
					// i.e. just put him randomly
					
					Set<PASpawn> spawns = new HashSet<PASpawn>();
					
					if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
						String arenaClass = aPlayer.getArenaClass().getName();
						spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, aPlayer.getArenaTeam().getName()+arenaClass+"spawn"));
					} else if (aPlayer.getArenaTeam().getName().equals("free")) {
						spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, "spawn"));
					} else {
						spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, aPlayer.getArenaTeam().getName()));
					}
					
					
					int pos = (new Random()).nextInt(spawns.size());
					for (PASpawn spawn : spawns) {
						if (--pos <= 0) {
							Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RespawnRunnable(arena, aPlayer, spawn.getName()), 1L);
							aPlayer.setStatus(Status.FIGHT);
							return;
						}
					}
				}
				
				// we need a smart spawn

				arena.getDebugger().i("we need smart!");
				
				final Set<PASpawn> spawns = SpawnManager.getPASpawnsStartingWith(arena, "spawn");
				
				final Set<PALocation> pLocs = new HashSet<PALocation>();
				
				for (ArenaPlayer app : aPlayer.getArenaTeam().getTeamMembers()) {
					if (app.getName().equals(aPlayer.getName())) {
						continue;
					}
					pLocs.add(new PALocation(app.get().getLocation()));
					arena.getDebugger().i("pos of " + app.getName() + new PALocation(app.get().getLocation()).toString());
				}
				arena.getDebugger().i("pLocs.size: " + pLocs.size());
				
				// pLocs now contains the other player's positions
				
				final Map<PALocation, Double> diffs = new HashMap<PALocation, Double>();
				
				double max = 0;
				
				for (PASpawn spawnLoc : spawns) {
					double sum = 90000;
					for (PALocation playerLoc : pLocs) {
						if (spawnLoc.getLocation().getWorldName().equals(playerLoc.getWorldName())) {
							sum = Math.min(sum, spawnLoc.getLocation().getDistanceSquared(playerLoc));
						}
					}
					max = Math.max(sum, max);
					diffs.put(spawnLoc.getLocation(), sum);
					arena.getDebugger().i("spawnLoc: " + spawnLoc.getName() + ":" + sum);
				}
				arena.getDebugger().i("max = " + max);
				
				for (PALocation loc : diffs.keySet()) {
					if (diffs.get(loc) == max) {
						for (PASpawn spawn : spawns) {
							if (spawn.getLocation().equals(loc)) {
								Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RespawnRunnable(arena, aPlayer, spawn.getName()), 1L);
								return;
							}
						}
						
					}
				}
				
				return;
			}
			
			final Set<PASpawn> spawns = SpawnManager.getPASpawnsStartingWith(arena, aPlayer.getArenaTeam().getName()+"spawn");
			int pos = (new Random()).nextInt(spawns.size());
			for (PASpawn spawn : spawns) {
				if (--pos <= 0) {
					Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RespawnRunnable(arena, aPlayer, spawn.getName()), 1L);
					aPlayer.setStatus(Status.FIGHT);
					return;
				}
			}
			
		}
		
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RespawnRunnable(arena, aPlayer, overrideSpawn), 1L);
		aPlayer.setStatus(Status.FIGHT);

	}

	/**
	 * set an arena coord to a given block
	 * 
	 * @param loc
	 *            the location to save
	 * @param place
	 *            the coord name to save the location to
	 */
	public static void setBlock(final Arena arena, final PABlockLocation loc, final String place) {
		// "x,y,z,yaw,pitch"

		final String spawnName = Config.parseToString(loc);
		arena.getDebugger().i("setting spawn " + place + " to " + spawnName);
		arena.getArenaConfig().setManually("spawns." + place, spawnName);
		arena.getArenaConfig().save();
		arena.addBlock(new PABlock(loc, place));
	}

	public static void loadSpawns(Arena arena, Config cfg) {
		Set<String> spawns = cfg.getKeys("spawns");
		if (spawns == null) {
			return;
		}
		
		for (String name : spawns) {
			String value = (String) cfg.getUnsafe("spawns."+name);
			String[] parts = value.split(",");
			
			if (parts.length != 4 && parts.length != 6) {
				throw new IllegalArgumentException(
						"Input string must contain world, x, y, and z: " + name);
			}
			
			if (parts.length == 4) {
				// PABlockLocation
				arena.addBlock(new PABlock(Config.parseBlockLocation(value), name));
			} else {
				// PALocation
				arena.addSpawn(new PASpawn(Config.parseLocation(value), name));
			}
		}
		
	}
}
