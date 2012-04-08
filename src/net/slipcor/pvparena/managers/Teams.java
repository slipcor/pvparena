package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.entity.Player;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.definitions.Announcement;
import net.slipcor.pvparena.definitions.Announcement.type;

/**
 * teams manager class
 * 
 * -
 * 
 * provides commands to deal with teams
 * 
 * @author slipcor
 * 
 * @version v0.7.0
 * 
 */

public class Teams {
	private static Debug db = new Debug(37);

	/**
	 * assign a player to a team
	 * 
	 * @param player
	 *            the player to assign
	 */
	public static void choosePlayerTeam(Arena arena, Player player) {

		db.i("calculating player team");

		boolean free = !arena.cfg.getBoolean("arenatype.teams");
		ArenaPlayer ap = Players.parsePlayer(player);
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().contains(ap)) {
				Arenas.tellPlayer(player, Language.parse("alreadyjoined"), arena);
				return;
			}
		}
		

		String sTeam = free ? "free" : calcFreeTeam(arena);

		ArenaTeam aTeam = arena.getTeam(sTeam);
		

		if (free) {
			arena.tpPlayerToCoordName(player, "lounge");
		} else {
			arena.tpPlayerToCoordName(player, aTeam.getName() + "lounge");
		}
		String coloredTeam = aTeam.colorize();
		Arenas.tellPlayer(
				player,
				Language.parse("youjoined" + (free ? "free" : ""),
						coloredTeam), arena);
		Announcement.announce(
				arena,
				type.JOIN,
				Language.parse("playerjoined" + (free ? "free" : ""),
						player.getName(),
						coloredTeam));
		Players.tellEveryoneExcept(
				arena,
				player,
				Language.parse("playerjoined" + (free ? "free" : ""),
						player.getName(),
						coloredTeam));
	}

	/**
	 * calculate the team that needs players the most
	 * 
	 * @return the team name
	 */
	public static String calcFreeTeam(Arena arena) {
		db.i("calculating free team");
		HashMap<String, Integer> counts = new HashMap<String, Integer>();

		// spam the available teams into a map counting the members
		
		for (ArenaTeam team : arena.getTeams()) {
			int count = team.getTeamMembers().size();
			
			if (count > 0) {
				counts.put(team.getName(), count);
				db.i("team " + team.getName() + " contains " + count);
			}
		}
		
		// counts contains TEAMNAME => PLAYERCOUNT

		if (counts.size() < arena.getTeams().size()) {
			// there is a team without members, calculate one of those
			return returnEmptyTeam(arena, counts.keySet());
		}

		boolean full = true;

		for (ArenaTeam team : arena.getTeams()) {
			String s = team.getName();
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
		db.i("choosing an empty team");
		HashSet<String> empty = new HashSet<String>();
		for (ArenaTeam team : arena.getTeams()) {
			String s = team.getName();
			db.i("team: " + s);
			if (set.contains(s)) {
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
