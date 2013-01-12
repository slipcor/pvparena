package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

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

public class SpawnManager {
	private static Debug db = new Debug(27);

	private static String calculateFarSpawn(String[] taken,
			HashMap<String, PALocation> available,
			HashMap<String, PALocation> total) {
		db.i("--------------------");
		db.i("calculating a spawn!");
		db.i("--------------------");
		double diff = 0;
		String far = null;
		for (String s : available.keySet()) {
			far = s;
			break;
		}
		db.i("last resort: " + far);
		
		for (String s : available.keySet()) {
			db.i("> checking " + s);
			double tempDiff = 0;
			for (int i = 0; (i < taken.length) && (taken[i] != null); i++) {
				tempDiff += total.get(taken[i]).getDistance(available.get(s));
				db.i(">> tempDiff: " + tempDiff);
			}
			
			if (tempDiff > diff) {
				db.i("-> diff");
				diff = tempDiff;
				far = s;
			}
		}
		
		return far;
	}

	public static void distribute(Arena arena, ArenaTeam team) {
		HashSet<ArenaRegionShape> ars = arena.getRegionsByType(RegionType.SPAWN);
		
		if (ars.size() > 0) {

			handle(arena, team.getTeamMembers(), ars);
			
			return;
		}
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_QUICKSPAWN)) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				arena.tpPlayerToCoordName(ap.get(), team.getName() + "spawn");
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

	public static void distribute(Arena arena, HashSet<ArenaPlayer> teamMembers) {
		HashSet<ArenaRegionShape> ars = arena.getRegionsByType(RegionType.SPAWN);
		
		if (ars.size() > 0) {

			handle(arena, teamMembers, ars);
			return;
		}
		
		ArenaTeam at = null;
		
		for (ArenaPlayer ap : teamMembers) {
			at = ap.getArenaTeam();
			break;
		}
		
		String t = arena.isFreeForAll()?"":at.getName();
		
		if (!arena.isFreeForAll() || arena.getArenaConfig().getBoolean(CFG.GENERAL_QUICKSPAWN)) {
			for (ArenaPlayer ap : teamMembers) {
				arena.tpPlayerToCoordName(ap.get(), t + "spawn");
				ap.setStatus(Status.FIGHT);
			}
			return;
		}
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_SMARTSPAWN)) {
			distributeSmart(arena, teamMembers, at.getName());
			return;
		}
		distributeByOrder(arena, teamMembers, at.getName());
	}
	
	private static void distributeByOrder(Arena arena,
			HashSet<ArenaPlayer> teamMembers, String string) {
		if (teamMembers == null || teamMembers.size() < 1) {
			return;
		}
		
		HashMap<String, PALocation> locs = getSpawnMap(arena, string);
		
		if (locs == null || locs.size() < 1) {
			return;
		}
		
		HashSet<String> removals = new HashSet<String>();
		
		for (String s : locs.keySet()) {
			if (!s.contains("spawn")) {
				removals.add(s);
			}
		}
		
		for (String s : removals) {
			locs.remove(s);
		}
		
		for (ArenaPlayer ap : teamMembers) {
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
	
	private static void distributeSmart(Arena arena,
			HashSet<ArenaPlayer> teamMembers, String string) {
		if (teamMembers == null || teamMembers.size() < 1) {
			return;
		}

		HashMap<String, PALocation> locs = getSpawnMap(arena, string);
		HashMap<String, PALocation> total = getSpawnMap(arena, string);
		
		if (locs == null || locs.size() < 1) {
			return;
		}
		
		HashSet<String> removals = new HashSet<String>();
		
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
				String s = null;
				for (String ss : locs.keySet()) {
					s = ss;
					break;
				}
				iteratings[i] = s;
				locs.remove(s);
				continue;
			}
			String s = calculateFarSpawn(iteratings, locs, total);
			iteratings[i] = s;
			locs.remove(s);
		}
		int i = 0;
		for (ArenaPlayer ap : teamMembers) {
			ap.setStatus(Status.FIGHT);
			String s = iteratings[i++%iteratings.length];
			if (s == null) {
				PVPArena.instance.getLogger().warning("Element #"+i+" is null: [" + StringParser.joinArray(iteratings, ",") + "]");
			}
			arena.tpPlayerToCoordName(ap.get(), s);
		}
	}

	public static PABlockLocation getBlockNearest(HashSet<PABlockLocation> hashSet,
			PABlockLocation location) {
		PABlockLocation result = null;

		for (PABlockLocation loc : hashSet) {
			if (result == null
					|| result.getDistance(location) > loc.getDistance(location)) {
				result = loc;
			}
		}

		return result;
	}

	public static HashSet<PABlockLocation> getBlocks(Arena arena, String sTeam) {
		db.i("reading blocks of arena " + arena + " (" + sTeam + ")");
		HashSet<PABlockLocation> result = new HashSet<PABlockLocation>();

		HashMap<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
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
				
					String sName = sTeam.replace("tnt", "");
					db.i("checking if " + name + " starts with " + sName);
					if (!name.startsWith(sName)) {
						continue;
					}
				}
			} else if (sTeam.endsWith("flag")) {
				if (name.contains("tnt") || name.contains("block") || name.contains("lounge") || name.contains("spawn")) {
					continue;
				}
				String sName = sTeam.replace("flag", "");
				db.i("checking if " + name + " starts with " + sName);
				if (!name.startsWith(sName)) {
					continue;
				}
			} else if (sTeam.endsWith("block")) {
				if (name.contains("tnt") || name.contains("flag") || name.contains("lounge") || name.contains("spawn")) {
					continue;
				}
				String sName = sTeam.replace("block", "");
				db.i("checking if " + name + " starts with " + sName);
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
			db.i(" - " + name);
			String sLoc = String.valueOf(arena.getArenaConfig().getUnsafe("spawns." + name));
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
	public static PALocation getCoords(Arena arena, String place) {
		db.i("get coords: " + place);
		
		if (place.equals("spawn") || place.equals("powerup")) {
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			db.i("searching for spawns");

			HashMap<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
					.getYamlConfiguration().getConfigurationSection("spawns")
					.getValues(false);
			for (String name : coords.keySet()) {
				if (name.startsWith(place)) {
					locs.put(i++, name);
					db.i("found match: " + name);
				}
			}
			
			if (arena.getArenaConfig().getBoolean(CFG.GENERAL_SMARTSPAWN)) {
				return getSmartCoord(arena);
			}

			Random r = new Random();

			place = locs.get(r.nextInt(locs.size()));
		} else if (arena.getArenaConfig().getUnsafe("spawns." + place) == null) {
			db.i("guessing spawn");
			place = PVPArena.instance.getAgm().guessSpawn(arena, place);
			if (place == null) {
				return null;
			}
		}

		String sLoc = String.valueOf(arena.getArenaConfig().getUnsafe("spawns." + place));
		db.i("parsing location: " + sLoc);
		PALocation result;
		if (place.contains("flag") || place.startsWith("switch")) {
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
	public static PALocation getNearest(HashSet<PALocation> hashSet,
			PALocation location) {
		PALocation result = null;

		for (PALocation loc : hashSet) {
			if (result == null
					|| result.getDistance(location) > loc.getDistance(location)) {
				result = loc;
			}
		}

		return result;
	}

	private static PALocation getSmartCoord(Arena arena) {
		HashSet<ArenaPlayer> players = arena.getFighters();
		HashMap<String, PALocation> locs = getSpawnMap(arena, "free");
		
		double diff = 0;
		String spawn = null;
		
		for (String s : locs.keySet()) {
			PALocation loc = locs.get(s);
			Location bLoc = loc.toLocation();
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

	public static HashMap<String, PALocation> getSpawnMap(Arena arena,
			String sTeam) {
		db.i("reading spawns of arena " + arena + " (" + sTeam + ")");
		HashMap<String, PALocation> result = new HashMap<String, PALocation>();

		HashMap<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
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
			} else if (name.endsWith("flag") || name.endsWith("pumpkin")) {
				String sName = sTeam.replace("flag", "");
				sName = sName.replace("pumpkin", "");
				db.i("checking if " + name + " starts with " + sName);
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
			db.i(" - " + name);
			String sLoc = String.valueOf(arena.getArenaConfig().getUnsafe("spawns." + name));
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
	public static HashSet<PALocation> getSpawns(Arena arena, String sTeam) {
		HashMap<String, PALocation> spawns = getSpawnMap(arena, sTeam);
		
		HashSet<PALocation> result = new HashSet<PALocation>();
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
	public static PABlockLocation getRegionCenter(Arena arena) {
		HashSet<PALocation> locs = new HashSet<PALocation>();
		
		ArenaRegionShape ars = null;
		for (ArenaRegionShape a : arena.getRegionsByType(RegionType.BATTLE)) {
			ars = a;
			break;
		}
		
		if (ars == null) {
			return new PABlockLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
		}

		World w = Bukkit.getWorld(ars.getWorldName());
		
		if (w == null) {
			return new PABlockLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
		}
		
		for (ArenaTeam team : arena.getTeams()) {
			String sTeam = team.getName();
			for (PALocation loc : getSpawns(arena, sTeam)) {
				locs.add(loc);
			}
		}

		long x = 0;
		long y = 0;
		long z = 0;

		for (PALocation loc : locs) {
			if (!loc.getWorldName().equals(w.getName())) {
				continue;
			}
			x += loc.getX();
			y += loc.getY();
			z += loc.getZ();
		}

		return new PABlockLocation(w.getName(),
				(int) x / locs.size(), (int) y / locs.size(), (int) z / locs.size());
	}
	
	private static void handle(Arena arena, HashSet<ArenaPlayer> teamMembers,
			HashSet<ArenaRegionShape> ars) {
		if (arena.isFreeForAll()) {
			for (ArenaPlayer ap : teamMembers) {
				int i = (new Random()).nextInt(ars.size());
			
				for (ArenaRegionShape x : ars) {
					if (i-- == 0) {
						spawnRandomly(arena, ap, x);
						break;
					}
				}
			}
		} else {
			String teamName = null;
			for (ArenaPlayer ap : teamMembers) {
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
	public static boolean isNearSpawn(Arena arena, Player player, int diff) {
		db.i("checking if arena is near a spawn", player);
		if (!arena.hasPlayer(player)) {
			return false;
		}
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		ArenaTeam team = ap.getArenaTeam();
		if (team == null) {
			return false;
		}

		HashSet<PALocation> spawns = getSpawns(arena, team.getName());

		for (PALocation loc : spawns) {
			if (loc.getDistance(new PALocation(player.getLocation())) <= diff) {
				db.i("found near spawn: " + loc.toString(), player);
				return true;
			}
		}

		return false;
	}

	public static void respawn(final Arena arena, final ArenaPlayer ap) {
		HashSet<ArenaRegionShape> ars = arena.getRegionsByType(RegionType.SPAWN);
		
		if (ars.size() > 0) {
			HashSet<ArenaPlayer> team = new HashSet<ArenaPlayer>();
			team.add(ap);
			handle(arena, team, ars);
			
			return;
		}
		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_SMARTSPAWN) && arena.isFreeForAll()) {
			HashSet<PALocation> pLocs = new HashSet<PALocation>();
			
			for (ArenaPlayer app : ap.getArenaTeam().getTeamMembers()) {
				if (app.getName().equals(ap.getName())) {
					continue;
				}
				pLocs.add(new PALocation(app.get().getLocation()));
			}
			
			HashMap<String, PALocation> locs = SpawnManager.getSpawnMap(arena, "free");
			
			HashMap<PALocation, Double> diffs = new HashMap<PALocation, Double>();
			
			double max = 0;
			
			for (PALocation spawnLoc : locs.values()) {
				double d = 0;
				for (PALocation playerLoc : pLocs) {
					d += spawnLoc.getDistance(playerLoc);
				}
				max = Math.max(d, max);
				diffs.put(spawnLoc, d);
			}
			
			for (PALocation loc : diffs.keySet()) {
				if (diffs.get(loc) == max) {
					for (String s : locs.keySet()) {
						if (locs.get(s).equals(loc)) {
							arena.tpPlayerToCoordName(ap.get(), s);
							return;
						}
					}
					
				}
			}
			
			return;
		}
		
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RespawnRunnable(arena, ap), 1L);
		ap.setStatus(Status.FIGHT);
		return;
	}

	/**
	 * set an arena coord to a given block
	 * 
	 * @param loc
	 *            the location to save
	 * @param place
	 *            the coord name to save the location to
	 */
	public static void setBlock(Arena arena, PABlockLocation loc, String place) {
		// "x,y,z,yaw,pitch"

		String s = Config.parseToString(loc);

		db.i("setting spawn " + place + " to " + s);

		arena.getArenaConfig().setManually("spawns." + place, s);

		arena.getArenaConfig().save();
	}

	private static void spawnRandomly(Arena arena, ArenaPlayer ap,
			ArenaRegionShape ars) {
		int x = ars.getMinimumLocation().getX();
		int y = ars.getMinimumLocation().getY();
		int z = ars.getMinimumLocation().getZ();
		Random r = new Random();
		
		boolean found = false;
		int attempt = 0;
		
		PABlockLocation loc = null;
		
		while (found == false && attempt < 10) {
		
			x += r.nextInt(ars.getMaximumLocation().getX() - 
				ars.getMinimumLocation().getX());
			y += r.nextInt(ars.getMaximumLocation().getY() - 
				ars.getMinimumLocation().getY());
			z += r.nextInt(ars.getMaximumLocation().getZ() - 
				ars.getMinimumLocation().getZ());
		
			loc = new PABlockLocation(ars.getMinimumLocation().getWorldName(), x, y, z);
			attempt++;
			found = ars.contains(loc);
			
		}
		
		PALocation temp = ap.getLocation();
		
		Location bLoc = loc.toLocation();
		
		while (bLoc.getBlock().getType() != Material.AIR
				&& bLoc.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR
				&& bLoc.getBlock().getRelative(BlockFace.UP, 2).getType() != Material.AIR) {
			bLoc = bLoc.add(0, 1, 0);
		}
		
		ap.setLocation(new PALocation(bLoc));
		
		ap.setStatus(Status.FIGHT);
		arena.tpPlayerToCoordName(ap.get(), "old");
		ap.setLocation(temp);
	}
}
