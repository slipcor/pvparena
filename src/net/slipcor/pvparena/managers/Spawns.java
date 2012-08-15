package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * spawn manager class
 * 
 * -
 * 
 * provides commands to deal with spawns
 * 
 * @author slipcor
 * 
 * @version v0.8.2
 * 
 */

public class Spawns {
	private static Debug db = new Debug(35);

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

			Random r = new Random();

			place = locs.get(r.nextInt(locs.size()));
		} else if (arena.getArenaConfig().get("spawns." + place) == null) {
			place = PVPArena.instance.getAtm().guessSpawn(arena, place);
			if (place == null) {
				return null;
			}
		}

		String sLoc = arena.getArenaConfig().getString("spawns." + place, null);
		db.i("parsing location: " + sLoc);
		return Config.parseWorldLocation(sLoc).add(0.5, 0, 0.5);
	}

	/**
	 * get the nearest spawn location from a location
	 * 
	 * @param spawns
	 *            the spawns to check
	 * @param location
	 *            the location to check
	 * @return the spawn location next to the location
	 */
	public static Location getNearest(HashSet<Location> spawns,
			Location location) {
		Location result = null;

		for (Location loc : spawns) {
			if (result == null
					|| result.distance(location) > loc.distance(location)) {
				result = loc;
			}
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
		db.i("reading spawns of arena " + arena + " (" + sTeam + ")");
		HashSet<PALocation> result = new HashSet<PALocation>();

		HashMap<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);

		for (String name : coords.keySet()) {
			if (sTeam.equals("flags")) {
				if (!name.startsWith("flag")) {
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
			} else if (sTeam.endsWith("flag") || sTeam.endsWith("pumpkin")) {
				continue;
			}
			db.i(" - " + name);
			String sLoc = arena.getArenaConfig().getString("spawns." + name, null);
			result.add(Config.parseWorldLocation( sLoc));
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
			x += loc.getX();
			y += loc.getY();
			z += loc.getZ();
		}

		

		return new PABlockLocation(arena.getWorld(),(int) x / locs.size(),(int) y / locs.size(),(int) z / locs.size());
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
		db.i("checking if arena is near a spawn");
		if (!arena.isPartOf(player)) {
			return false;
		}
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		ArenaTeam team = Teams.getTeam(arena, ap);
		if (team == null) {
			return false;
		}

		HashSet<PALocation> spawns = getSpawns(arena, team.getName());

		for (PALocation loc : spawns) {
			if (loc.getDistance(new PALocation(player.getLocation())) <= diff) {
				db.i("found near spawn: " + loc.toString());
				return true;
			}
		}

		return false;
	}

	/**
	 * set an arena coord to a player's position
	 * 
	 * @param player
	 *            the player saving the coord
	 * @param place
	 *            the coord name to save the location to
	 */
	public static void setCoords(Arena arena, Player player, String place) {
		// "x,y,z,yaw,pitch"

		Location location = player.getLocation();

		Integer x = location.getBlockX();
		Integer y = location.getBlockY();
		Integer z = location.getBlockZ();
		Float yaw = location.getYaw();
		Float pitch = location.getPitch();

		String s = x.toString() + "," + y.toString() + "," + z.toString() + ","
				+ yaw.toString() + "," + pitch.toString();

		db.i("setting spawn " + place + " to " + s.toString());

		arena.getArenaConfig().set("spawns." + place, s);

		arena.getArenaConfig().save();
	}

	/**
	 * set an arena coord to a given location
	 * 
	 * @param loc
	 *            the location to save
	 * @param place
	 *            the coord name to save the location to
	 */
	public static void setCoords(Arena arena, Location loc, String place) {
		// "x,y,z,yaw,pitch"

		Integer x = loc.getBlockX();
		Integer y = loc.getBlockY();
		Integer z = loc.getBlockZ();
		Float yaw = loc.getYaw();
		Float pitch = loc.getPitch();

		String s = x.toString() + "," + y.toString() + "," + z.toString() + ","
				+ yaw.toString() + "," + pitch.toString();

		db.i("setting spawn " + place + " to " + s.toString());

		arena.getArenaConfig().set("spawns." + place, s);

		arena.getArenaConfig().save();
	}
}
