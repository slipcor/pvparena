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
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;
import net.slipcor.pvparena.runnables.RespawnRunnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

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
			final Map<String, PALocation> available,
			final Map<String, PALocation> total) {
		DEBUG.i("--------------------");
		DEBUG.i("calculating a spawn!");
		DEBUG.i("--------------------");
		double diff = 0;
		String far = null;
		for (String s : available.keySet()) {
			far = s;
			break;
		}
		DEBUG.i("last resort: " + far);
		
		for (String s : available.keySet()) {
			DEBUG.i("> checking " + s);
			double tempDiff = 0;
			for (int i = 0; (i < taken.length) && (taken[i] != null); i++) {
				tempDiff += total.get(taken[i]).getDistance(available.get(s));
				DEBUG.i(">> tempDiff: " + tempDiff);
			}
			
			if (tempDiff > diff) {
				DEBUG.i("-> diff");
				diff = tempDiff;
				far = s;
			}
		}
		
		return far;
	}

	public static void distribute(final Arena arena, final ArenaTeam team) {
		final Set<ArenaRegionShape> ars = arena.getRegionsByType(RegionType.SPAWN);
		
		if (!ars.isEmpty()) {

			handle(arena, team.getTeamMembers(), ars);
			
			return;
		}
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_QUICKSPAWN)) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
					arena.tpPlayerToCoordName(ap.get(), team.getName() + ap.getArenaClass().getName() + "spawn");
				} else {
					arena.tpPlayerToCoordName(ap.get(), team.getName() + "spawn");
				}
				ap.setStatus(Status.FIGHT);
			}
			return;
		}
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_SMARTSPAWN)) {
			distributeSmart(arena, team.getTeamMembers(), team.getName() + "spawn");
			return;
		}
		distributeByOrder(arena, team.getTeamMembers(), team.getName() + "spawn");
	}

	public static void distribute(final Arena arena, final Set<ArenaPlayer> teamMembers) {
		final Set<ArenaRegionShape> ars = arena.getRegionsByType(RegionType.SPAWN);
		
		if (!ars.isEmpty()) {

			handle(arena, teamMembers, ars);
			return;
		}
		
		ArenaTeam team = null;
		
		for (ArenaPlayer ap : teamMembers) {
			team = ap.getArenaTeam();
			break;
		}
		
		final String teamPrefix = arena.isFreeForAll()?"":team.getName();
		
		if (!arena.isFreeForAll() || arena.getArenaConfig().getBoolean(CFG.GENERAL_QUICKSPAWN)) {
			for (ArenaPlayer ap : teamMembers) {

				if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
					arena.tpPlayerToCoordName(ap.get(), teamPrefix + ap.getArenaClass().getName() + "spawn");
				} else {
					arena.tpPlayerToCoordName(ap.get(), teamPrefix + "spawn");
				}
				ap.setStatus(Status.FIGHT);
			}
			return;
		}
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_SMARTSPAWN)) {
			distributeSmart(arena, teamMembers, team.getName());
			return;
		}
		distributeByOrder(arena, teamMembers, team.getName());
	}
	
	private static void distributeByOrder(final Arena arena,
			final Set<ArenaPlayer> set, final String string) {
		if (set == null || set.size() < 1) {
			return;
		}
		
		final Map<String, PALocation> locs = getSpawnMap(arena, string);
		
		if (locs == null || locs.size() < 1) {
			return;
		}
		
		final Set<String> removals = new HashSet<String>();
		
		for (String s : locs.keySet()) {
			if (!s.contains("spawn")) {
				removals.add(s);
			}
		}
		
		for (String s : removals) {
			locs.remove(s);
		}
		
		for (ArenaPlayer ap : set) {
			ap.setStatus(Status.FIGHT);
			if (locs.size() < 1) {
				arena.tpPlayerToCoordName(ap.get(), string);
				continue;
			}
			
			for (String s : locs.keySet()) {
				arena.tpPlayerToCoordName(ap.get(), s);
				locs.remove(s);
				break;
			}
		}
	}
	
	private static void distributeSmart(final Arena arena,
			final Set<ArenaPlayer> set, final String string) {
		if (set == null || set.size() < 1) {
			return;
		}

		final Map<String, PALocation> locs = getSpawnMap(arena, string);
		final Map<String, PALocation> total = getSpawnMap(arena, string);
		
		if (locs == null || locs.size() < 1) {
			return;
		}
		
		final Set<String> removals = new HashSet<String>();
		
		for (String s : locs.keySet()) {
			if (!s.contains("spawn")) {
				removals.add(s);
			}
		}
		
		for (String s : removals) {
			locs.remove(s);
			total.remove(s);
		}

		String[] iteratings = new String[locs.size()];
		
		for (int i = 0; i < total.size(); i++) {
			if (i == 0) {
				String locName = null;
				for (String ss : locs.keySet()) {
					locName = ss;
					break;
				}
				iteratings[i] = locName;
				locs.remove(locName);
				continue;
			}
			final String spawnName = calculateFarSpawn(iteratings, locs, total);
			iteratings[i] = spawnName;
			locs.remove(spawnName);
		}
		int pos = 0;
		for (ArenaPlayer ap : set) {
			ap.setStatus(Status.FIGHT);
			final String spawnName = iteratings[pos++%iteratings.length];
			if (spawnName == null) {
				PVPArena.instance.getLogger().warning("Element #"+pos+" is null: [" + StringParser.joinArray(iteratings, ",") + "]");
			}
			arena.tpPlayerToCoordName(ap.get(), spawnName);
		}
	}

	public static PABlockLocation getBlockNearest(final Set<PABlockLocation> locs,
			final PABlockLocation location) {
		PABlockLocation result = null;

		for (PABlockLocation loc : locs) {
			if (result == null
					|| result.getDistance(location) > loc.getDistance(location)) {
				result = loc;
			}
		}

		return result;
	}

	public static Set<PABlockLocation> getBlocks(final Arena arena, final String sTeam) {
		DEBUG.i("reading blocks of arena " + arena + " (" + sTeam + ")");
		final Set<PABlockLocation> result = new HashSet<PABlockLocation>();

		final Map<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);

		for (String name : coords.keySet()) {
			if (sTeam.equals("flags")) {
				if (!name.startsWith("flag")) {
					continue;
				}
			} else if (sTeam.endsWith("tnt")) {
				if (!name.contains("tnt")) {
					continue;
				}
				if (!name.equals("tnt")) {
				
					final String sName = sTeam.replace("tnt", "");
					DEBUG.i("checking if " + name + " starts with " + sName);
					if (!name.startsWith(sName)) {
						continue;
					}
				}
			} else if (sTeam.endsWith("flag")) {
				if (name.contains("tnt") || name.contains("block") || name.contains("lounge") || name.contains("spawn")) {
					continue;
				}
				final String sName = sTeam.replace("flag", "");
				DEBUG.i("checking if " + name + " starts with " + sName);
				if (!name.startsWith(sName)) {
					continue;
				}
			} else if (sTeam.endsWith("block")) {
				if (name.contains("tnt") || name.contains("flag") || name.contains("lounge") || name.contains("spawn")) {
					continue;
				}
				final String sName = sTeam.replace("block", "");
				DEBUG.i("checking if " + name + " starts with " + sName);
				if (!name.startsWith(sName)) {
					continue;
				}
			} else if (sTeam.equals("free")) {
				if (!name.startsWith("spawn")) {
					continue;
				}
			} else if (name.contains("lounge")) {
				continue;
			} else if (sTeam.endsWith("flag") || sTeam.endsWith("flag") || sTeam.endsWith("pumpkin")) {
				continue;
			}
			DEBUG.i(" - " + name);
			final String sLoc = String.valueOf(arena.getArenaConfig().getUnsafe("spawns." + name));
			result.add(Config.parseBlockLocation( sLoc));
		}

		return result;
	}

	/**
	 * get the location from a coord string
	 * 
	 * @param place
	 *            the coord string
	 * @return the location of that string
	 */
	public static PALocation getCoords(final Arena arena, final String place) {
		DEBUG.i("get coords: " + place);
		
		String newPlace = place;
		
		if (place.equals("spawn") || place.equals("powerup")) {
			final Map<Integer, String> locs = new HashMap<Integer, String>();
			int pos = 0;

			DEBUG.i("searching for spawns");

			final Map<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
					.getYamlConfiguration().getConfigurationSection("spawns")
					.getValues(false);
			for (String name : coords.keySet()) {
				if (name.startsWith(place)) {
					locs.put(pos++, name);
					DEBUG.i("found match: " + name);
				}
			}
			
			if (arena.getArenaConfig().getBoolean(CFG.GENERAL_SMARTSPAWN)) {
				return getSmartCoord(arena);
			}

			final Random random = new Random();

			newPlace = locs.get(random.nextInt(locs.size()));
		} else if (arena.getArenaConfig().getUnsafe("spawns." + place) == null) {
			DEBUG.i("guessing spawn");
			newPlace = PVPArena.instance.getAgm().guessSpawn(arena, place);
			if (newPlace == null) {
				return null;
			}
		}

		final String sLoc = String.valueOf(arena.getArenaConfig().getUnsafe("spawns." + newPlace));
		DEBUG.i("parsing location: " + sLoc);
		PALocation result;
		if (newPlace.contains("flag") || newPlace.startsWith("switch")) {
			result = new PALocation(Config.parseBlockLocation(sLoc).toLocation());
		} else {
			result = Config.parseLocation(sLoc);
		}
		return result.add(0.5, 0.5, 0.5);
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
	public static PALocation getNearest(final Set<PALocation> hashSet,
			final PALocation location) {
		PALocation result = null;

		for (PALocation loc : hashSet) {
			if (result == null
					|| result.getDistance(location) > loc.getDistance(location)) {
				result = loc;
			}
		}

		return result;
	}

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

	public static Map<String, PALocation> getSpawnMap(final Arena arena,
			final String sTeam) {
		DEBUG.i("reading spawns of arena " + arena + " (" + sTeam + ")");
		final Map<String, PALocation> result = new HashMap<String, PALocation>();

		final Map<String, Object> coords = arena.getArenaConfig()
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);

		for (String name : coords.keySet()) {
			if (sTeam.equals("flags")) {
				if (!name.startsWith("flag")) {
					continue;
				}
			} else if (sTeam.equals("blocks")) {
				if (!name.contains("block")) {
					continue;
				}
			} else if (sTeam.equals("turrets")) {
				if (!name.contains("turret")) {
					continue;
				}
			} else if (sTeam.contains("block")) {
				if (!name.startsWith(sTeam)) {
					continue;
				}
			} else if (sTeam.equals("pillar")) {
				if (!name.contains("pillar")) {
					continue;
				}
			} else if (name.endsWith("flag") || name.endsWith("pumpkin")) {
				String sName = sTeam.replace("flag", "");
				sName = sName.replace("pumpkin", "");
				DEBUG.i("checking if " + name + " starts with " + sName);
				if (!name.startsWith(sName)) {
					continue;
				}
			} else if (sTeam.equals("free")) {
				if (!name.startsWith("spawn")) {
					continue;
				}
			} else if (name.contains("lounge")) {
				continue;
			} else if (sTeam.equals("item") && !name.startsWith("item")) {
				continue;
			} else if (sTeam.endsWith("flag") || sTeam.endsWith("pumpkin")) {
				continue;
			}
			DEBUG.i(" - " + name);
			final String sLoc = String.valueOf(arena.getArenaConfig().getUnsafe("spawns." + name));
			result.put(name, Config.parseLocation(sLoc));
		}

		return result;
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
	public static Set<PALocation> getSpawns(final Arena arena, final String sTeam) {
		final Map<String, PALocation> spawns = getSpawnMap(arena, sTeam);
		
		final Set<PALocation> result = new HashSet<PALocation>();
		for (PALocation l : spawns.values()) {
			result.add(l);
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
		
		ArenaRegionShape ars = null;
		for (ArenaRegionShape a : arena.getRegionsByType(RegionType.BATTLE)) {
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
		
		for (ArenaTeam team : arena.getTeams()) {
			final String sTeam = team.getName();
			for (PALocation loc : getSpawns(arena, sTeam)) {
				locs.add(loc);
			}
		}

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
	
	private static void handle(final Arena arena, final Set<ArenaPlayer> set,
			final Set<ArenaRegionShape> ars) {
		if (arena.isFreeForAll()) {
			for (ArenaPlayer ap : set) {
				int pos = new Random().nextInt(ars.size());
			
				for (ArenaRegionShape x : ars) {
					if (pos-- == 0) {
						spawnRandomly(arena, ap, x);
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
				for (ArenaRegionShape x : ars) {
					if (x.getRegionName().contains(teamName)) {
						spawnRandomly(arena, ap, x);
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
		DEBUG.i("checking if arena is near a spawn", player);
		if (!arena.hasPlayer(player)) {
			return false;
		}
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final ArenaTeam team = aPlayer.getArenaTeam();
		if (team == null) {
			return false;
		}

		final Set<PALocation> spawns = getSpawns(arena, team.getName());

		for (PALocation loc : spawns) {
			if (loc.getDistance(new PALocation(player.getLocation())) <= diff) {
				DEBUG.i("found near spawn: " + loc.toString(), player);
				return true;
			}
		}

		return false;
	}

	public static void respawn(final Arena arena, final ArenaPlayer aPlayer) {
		final Set<ArenaRegionShape> ars = arena.getRegionsByType(RegionType.SPAWN);
		
		if (!ars.isEmpty()) {
			final Set<ArenaPlayer> team = new HashSet<ArenaPlayer>();
			team.add(aPlayer);
			handle(arena, team, ars);
			
			return;
		}
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_SMARTSPAWN) && arena.isFreeForAll()) {
			final Set<PALocation> pLocs = new HashSet<PALocation>();
			
			for (ArenaPlayer app : aPlayer.getArenaTeam().getTeamMembers()) {
				if (app.getName().equals(aPlayer.getName())) {
					continue;
				}
				pLocs.add(new PALocation(app.get().getLocation()));
			}
			
			final Map<String, PALocation> locs = SpawnManager.getSpawnMap(arena, "free");
			
			final Map<PALocation, Double> diffs = new HashMap<PALocation, Double>();
			
			double max = 0;
			
			for (PALocation spawnLoc : locs.values()) {
				double sum = 0;
				for (PALocation playerLoc : pLocs) {
					sum += spawnLoc.getDistance(playerLoc);
				}
				max = Math.max(sum, max);
				diffs.put(spawnLoc, sum);
			}
			
			for (PALocation loc : diffs.keySet()) {
				if (diffs.get(loc) == max) {
					for (String s : locs.keySet()) {
						if (locs.get(s).equals(loc)) {
							Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RespawnRunnable(arena, aPlayer, s), 1L);
							return;
						}
					}
					
				}
			}
			
			return;
		}
		
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RespawnRunnable(arena, aPlayer, null), 1L);
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

		DEBUG.i("setting spawn " + place + " to " + spawnName);

		arena.getArenaConfig().setManually("spawns." + place, spawnName);

		arena.getArenaConfig().save();
	}

	private static void spawnRandomly(final Arena arena, final ArenaPlayer aPlayer,
			final ArenaRegionShape ars) {
		int x = ars.getMinimumLocation().getX();
		int y = ars.getMinimumLocation().getY();
		int z = ars.getMinimumLocation().getZ();
		final Random random = new Random();
		
		boolean found = false;
		int attempt = 0;
		
		PABlockLocation loc = null;
		
		while (!found && attempt < 10) {
		
			x += random.nextInt(ars.getMaximumLocation().getX() - 
				ars.getMinimumLocation().getX());
			y += random.nextInt(ars.getMaximumLocation().getY() - 
				ars.getMinimumLocation().getY());
			z += random.nextInt(ars.getMaximumLocation().getZ() - 
				ars.getMinimumLocation().getZ());
		
			loc = new PABlockLocation(ars.getMinimumLocation().getWorldName(), x, y, z);
			attempt++;
			found = ars.contains(loc);
			
		}
		
		final PABlockLocation newLoc = loc;
		
		class RunLater implements Runnable {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				final PALocation temp = aPlayer.getLocation();
				
				Location bLoc = newLoc.toLocation();
				bLoc.add(0.5, 0.5, 0.5);
				
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
}
