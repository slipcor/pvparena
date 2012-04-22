package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;

/**
 * teams manager class
 * 
 * -
 * 
 * provides commands to deal with teams
 * 
 * @author slipcor
 * 
 * @version v0.7.15
 * 
 */

public class Teams {
	private static Debug db = new Debug(37);

	/**
	 * add a team to an arena
	 * 
	 * @param arenaTeam
	 *            the team to add
	 */
	public static void addTeam(Arena arena, ArenaTeam arenaTeam) {
		arena.getTeams().add(arenaTeam);
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
	 * check if the teams are even
	 * 
	 * @return true if teams have the same amount of players, false otherwise
	 */
	public static boolean checkEven(Arena arena) {
		db.i("checking if teams are even");
		HashMap<String, Integer> counts = new HashMap<String, Integer>();

		// count each team members

		for (ArenaTeam team : arena.getTeams()) {
			db.i(team.getName() + ": " + team.getTeamMembers().size());
		}

		if (counts.size() < 1) {
			db.i("noone in there");
			return false; // noone there => not even
		}

		int temp = -1;
		for (int i : counts.values()) {
			if (temp == -1) {
				temp = i;
				continue;
			}
			if (temp != i) {
				db.i("NOT EVEN");
				return false; // different count => not even
			}
		}
		db.i("EVEN");
		return true; // every team has the same player count!
	}

	/**
	 * assign a player to a team
	 * 
	 * @param player
	 *            the player to assign
	 */
	public static void choosePlayerTeam(Arena arena, Player player) {

		db.i("calculating player team");

		boolean free = arena.type().getName().equals("free");
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().contains(ap)) {
				Arenas.tellPlayer(player, Language.parse("alreadyjoined"),
						arena);
				return;
			}
		}

		String sTeam = free ? "free" : calcFreeTeam(arena);

		db.i(sTeam);

		ArenaTeam aTeam = getTeam(arena, sTeam);

		aTeam.add(ap);

		if (free) {
			arena.tpPlayerToCoordName(player, "lounge");
		} else {
			arena.tpPlayerToCoordName(player, aTeam.getName() + "lounge");
		}
		String coloredTeam = aTeam.colorize();
		Arenas.tellPlayer(
				player,
				Language.parse("youjoined" + (free ? "free" : ""), coloredTeam),
				arena);
		PVPArena.instance.getAmm().choosePlayerTeam(arena, player, coloredTeam);
		arena.tellEveryoneExcept(
				player,
				Language.parse("playerjoined" + (free ? "free" : ""),
						player.getName(), coloredTeam));
	}

	/**
	 * count all teams that have active players
	 * 
	 * @return the number of teams that have active players
	 */
	public static int countActiveTeams(Arena arena) {
		db.i("counting active teams");

		HashSet<String> activeteams = new HashSet<String>();
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.FIGHT)) {
					activeteams.add(team.getName());
					break;
				}
			}
		}
		db.i("result: " + activeteams.size());
		return activeteams.size();
	}

	/**
	 * count all players that have a team
	 * 
	 * @return the team player count
	 */
	public static int countPlayersInTeams(Arena arena) {
		int result = 0;
		for (ArenaTeam team : arena.getTeams()) {
			result += team.getTeamMembers().size();
		}
		db.i("players having a team: " + result);
		return result;
	}

	public static String getNotReadyTeamStringList(Arena arena) {
		String result = "";
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().size() < 1) {
				continue;
			}

			if (!result.equals(""))
				result += ", ";

			for (ArenaPlayer p : team.getTeamMembers()) {
				if (p.getStatus().equals(Status.LOBBY)) {
					if (!result.equals(""))
						result += ", ";
					result += team.colorizePlayer(p.get()) + ChatColor.WHITE;
				} else {
					db.i("player state: " + p.getStatus().name());
				}
			}
		}
		db.i("notreadyteamstringlist: " + result);
		return result;
	}

	/**
	 * parse all teams and join them colored, comma separated
	 * 
	 * @return a colorized, comma separated string
	 */
	public static String getTeamStringList(Arena arena) {
		String result = "";
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().size() < 1) {
				continue;
			}

			if (!result.equals(""))
				result += ", ";

			for (ArenaPlayer p : team.getTeamMembers()) {
				if (!result.equals(""))
					result += ", ";
				result += team.colorizePlayer(p.get()) + ChatColor.WHITE;
			}
		}
		db.i("teamstringlist: " + result);
		return result;
	}

	/**
	 * search for an arena team by player
	 * 
	 * @param player
	 *            the player to find
	 * @return the ArenaTeam instance if found, null otherwise
	 */
	public static ArenaTeam getTeam(Arena arena, ArenaPlayer player) {
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().contains(player)) {
				return team;
			}
		}
		return null;
	}

	/**
	 * search for an arena team by name
	 * 
	 * @param name
	 *            the team name to find
	 * @return the ArenaTeam instance if found, null otherwise
	 */
	public static ArenaTeam getTeam(Arena arena, String name) {
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getName().equalsIgnoreCase(name)) {
				return team;
			}
		}
		return null;
	}

	/**
	 * remove a player from a team
	 * 
	 * @param player
	 *            the player to remove
	 */
	public static void removeTeam(Arena arena, ArenaPlayer player) {
		for (ArenaTeam team : arena.getTeams()) {
			team.remove(player);
		}
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
