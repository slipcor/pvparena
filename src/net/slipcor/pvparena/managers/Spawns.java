package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.definitions.Arena;

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
 * @version v0.6.3
 * 
 */

public class Spawns {

	/**
	 * get the location from a coord string
	 * 
	 * @param place
	 *            the coord string
	 * @return the location of that string
	 */
	public static Location getCoords(Arena arena, String place) {
		Arena.db.i("get coords: " + place);
		World world = Bukkit.getWorld(arena.cfg.getString("general.world",
				Bukkit.getWorlds().get(0).getName()));
		if (place.equals("spawn") || place.equals("powerup")) {
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			Arena.db.i("searching for spawns");

			HashMap<String, Object> coords = (HashMap<String, Object>) arena.cfg
					.getYamlConfiguration().getConfigurationSection("spawns")
					.getValues(false);
			for (String name : coords.keySet()) {
				if (name.startsWith(place)) {
					locs.put(i++, name);
					Arena.db.i("found match: " + name);
				}
			}

			Random r = new Random();

			place = locs.get(r.nextInt(locs.size()));
		} else if (arena.cfg.get("spawns." + place) == null) {
			if (!place.contains("spawn")) {
				Arena.db.i("place not found!");
				return null;
			}
			// no exact match: assume we have multiple spawnpoints
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			Arena.db.i("searching for team spawns");

			HashMap<String, Object> coords = (HashMap<String, Object>) arena.cfg
					.getYamlConfiguration().getConfigurationSection("spawns")
					.getValues(false);
			for (String name : coords.keySet()) {
				if (name.startsWith(place)) {
					locs.put(i++, name);
					Arena.db.i("found match: " + name);
				}
			}

			if (locs.size() < 1) {
				return null;
			}
			Random r = new Random();

			place = locs.get(r.nextInt(locs.size()));
		}

		String sLoc = arena.cfg.getString("spawns." + place, null);
		Arena.db.i("parsing location: " + sLoc);
		return Config.parseLocation(world, sLoc);
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

		arena.cfg.set("spawns." + place, s);

		arena.cfg.save();
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

		arena.cfg.set("spawns." + place, s);

		arena.cfg.save();
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
		if (!arena.pm.existsPlayer(player)) {
			return false;
		}
		if (arena.pm.getTeam(player).equals("")) {
			return false;
		}

		HashSet<Location> spawns = getSpawns(arena, arena.pm.getTeam(player));

		for (Location loc : spawns) {
			if (loc.distance(player.getLocation()) <= diff) {
				return true;
			}
		}

		return false;
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
	public static HashSet<Location> getSpawns(Arena arena, String sTeam) {
		HashSet<Location> result = new HashSet<Location>();

		HashMap<String, Object> coords = (HashMap<String, Object>) arena.cfg
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);
		World world = Bukkit.getWorld(arena.getWorld());
		for (String name : coords.keySet()) {
			if (sTeam.equals("flags")) {
				if (!name.startsWith("flag")) {
					continue;
				}
			} else if (name.endsWith("flag") || name.endsWith("pumpkin")) {
				if (!name.equals(sTeam)) {
					continue;
				}
			}
			String sLoc = arena.cfg.getString("spawns." + name, null);
			result.add(Config.parseLocation(world, sLoc));
		}

		return result;
	}
}
