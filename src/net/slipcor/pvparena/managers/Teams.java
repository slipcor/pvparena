package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Announcement;
import net.slipcor.pvparena.definitions.Announcement.type;
import net.slipcor.pvparena.definitions.Arena;

/**
 * teams manager class
 * 
 * -
 * 
 * provides commands to deal with teams
 * 
 * @author slipcor
 * 
 * @version v0.6.3
 * 
 */

public class Teams {
	private static Debug db = new Debug();

	/**
	 * assign a player to a team
	 * 
	 * @param player
	 *            the player to assign
	 */
	public static void chooseColor(Arena arena, Player player) {

		boolean free = !arena.cfg.getBoolean("arenatype.teams");

		if (arena.pm.getPlayerTeamMap().containsKey(player.getName())) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("alreadyjoined"));
		}

		String team = free ? "free" : calcFreeTeam(arena);
		arena.pm.setTeam(player, team);

		if (free) {
			arena.tpPlayerToCoordName(player, "lounge");
		} else {
			arena.tpPlayerToCoordName(player, team + "lounge");
		}
		Arenas.tellPlayer(player, PVPArena.lang.parse("youjoined"
				+ (free ? "free" : ""),
				ChatColor.valueOf(arena.paTeams.get(team)) + team));
		Announcement.announce(arena, type.JOIN, PVPArena.lang.parse(
				"playerjoined" + (free ? "free" : ""), player.getName(),
				ChatColor.valueOf(arena.paTeams.get(team)) + team));
		arena.pm.tellEveryoneExcept(player, PVPArena.lang.parse("playerjoined"
				+ (free ? "free" : ""), player.getName(),
				ChatColor.valueOf(arena.paTeams.get(team)) + team));
	}

	/**
	 * calculate the team that needs players the most
	 * 
	 * @return the team name
	 */
	public static String calcFreeTeam(Arena arena) {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();

		// spam the available teams into a map counting the members
		for (String team : arena.pm.getPlayerTeamMap().values()) {
			if (!counts.containsKey(team)) {
				counts.put(team, 1);
				db.i("team " + team + " found");
			} else {
				int i = counts.get(team);
				counts.put(team, ++i);
				db.i("team " + team + " updated to " + i);
			}
		}
		// counts contains TEAMNAME => PLAYERCOUNT

		if (counts.size() < arena.paTeams.size()) {
			// there is a team without members, calculate one of those
			return returnEmptyTeam(arena, counts.keySet());
		}

		boolean full = true;

		for (String s : arena.paTeams.keySet()) {
			// check if we are full
			db.i("String s: " + s + "; max: " + arena.cfg.getInt("ready.max"));
			if (counts.get(s) < arena.cfg.getInt("ready.max")
					|| arena.cfg.getInt("ready.max") == 0) {
				full = false;
				break;
			}
		}

		if (full) {
			// full => OUT!
			return null;
		}

		HashSet<String> free = new HashSet<String>();

		int max = arena.cfg.getInt("ready.maxTeam");
		max = max == 0 ? Integer.MAX_VALUE : max;
		// calculate the max value down to the minimum
		for (String s : counts.keySet()) {
			int i = counts.get(s);
			if (i < max) {
				free.clear();
				free.add(s);
				max = i;
			} else if (i == max) {
				free.add(s);
			}
		}

		// free now has the minimum teams

		if (free.size() == 1) {
			for (String s : free) {
				return s;
			}
		}

		Random r = new Random();
		int rand = r.nextInt(free.size());
		for (String s : free) {
			if (rand-- == 0) {
				return s;
			}
		}

		return null;
	}

	/**
	 * return all empty teams
	 * 
	 * @param set
	 *            the set to search
	 * @return one empty team name
	 */
	private static String returnEmptyTeam(Arena arena, Set<String> set) {
		HashSet<String> empty = new HashSet<String>();
		for (String s : arena.paTeams.keySet()) {
			db.i("team: " + s);
			if (set.contains(s)) {
				db.i("done");
				continue;
			}
			empty.add(s);
		}
		db.i("empty.size: " + empty.size());
		if (empty.size() == 1) {
			for (String s : empty) {
				db.i("return: " + s);
				return s;
			}
		}

		Random r = new Random();
		int rand = r.nextInt(empty.size());
		for (String s : empty) {
			if (rand-- == 0) {
				return s;
			}
		}

		return null;
	}
}
