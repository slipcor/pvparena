package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;

/**
 * <pre>
 * Arena Team Manager class
 * </pre>
 * 
 * Provides static methods to manage Arena Teams
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class TeamManager {
	private final static Debug DEBUG = new Debug(29);

	/**
	 * calculate the team that needs players the most
	 * 
	 * @return the team name
	 */
	public static String calcFreeTeam(final Arena arena) {
		arena.getDebugger().i("calculating free team");
		final Map<String, Integer> counts = new HashMap<String, Integer>();

		// spam the available teams into a map counting the members

		for (ArenaTeam team : arena.getTeams()) {
			final int count = team.getTeamMembers().size();

			if (count > 0) {
				counts.put(team.getName(), count);
				arena.getDebugger().i("team " + team.getName() + " contains " + count);
			}
		}

		// counts contains TEAMNAME => PLAYERCOUNT

		if (counts.size() < arena.getTeams().size()) {
			// there is a team without members, calculate one of those
			return returnEmptyTeam(arena, counts.keySet());
		}

		boolean full = true;

		for (ArenaTeam team : arena.getTeams()) {
			final String teamName = team.getName();
			// check if we are full
			arena.getDebugger().i("String s: " + teamName + "; max: "
					+ arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS));
			if (counts.get(teamName) < arena.getArenaConfig().getInt(
					CFG.READY_MAXPLAYERS)
					|| arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS) == 0) {
				full = false;
				break;
			}
		}

		if (full) {
			// full => OUT!
			return null;
		}

		final Set<String> free = new HashSet<String>();

		int max = arena.getArenaConfig().getInt(CFG.READY_MAXTEAMPLAYERS);
		max = max == 0 ? Integer.MAX_VALUE : max;
		// calculate the max value down to the minimum
		for (String s : counts.keySet()) {
			final int count = counts.get(s);
			if (count < max) {
				free.clear();
				free.add(s);
				max = count;
			} else if (count == max) {
				free.add(s);
			}
		}

		// free now has the minimum teams

		if (free.size() == 1) {
			for (String s : free) {
				return s;
			}
		}
		
		if (free.size() < 1) {
			return null;
		}

		final Random random = new Random();
		int rand = random.nextInt(free.size());
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
	public static boolean checkEven(final Arena arena) {
		arena.getDebugger().i("checking if teams are even");
		final Map<String, Integer> counts = new HashMap<String, Integer>();

		// count each team members

		for (ArenaTeam team : arena.getTeams()) {
			arena.getDebugger().i(team.getName() + ": " + team.getTeamMembers().size());
			counts.put(team.getName(), team.getTeamMembers().size());
		}

		if (counts.size() < 1) {
			arena.getDebugger().i("noone in there");
			return false; // noone there => not even
		}

		int temp = -1;
		for (int i : counts.values()) {
			if (temp == -1) {
				temp = i;
				continue;
			}
			if (temp != i) {
				arena.getDebugger().i("NOT EVEN");
				return false; // different count => not even
			}
		}
		arena.getDebugger().i("EVEN");
		return true; // every team has the same player count!
	}

	/**
	 * assign a player to a team
	 * 
	 * @param player
	 *            the player to assign
	 */
	public static void choosePlayerTeam(final Arena arena, final Player player) {

		arena.getDebugger().i("calculating player team", player);

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().contains(aPlayer)) {
				arena.getDebugger().i("TeamManager", player);
				arena.msg(
						player,
						Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF,
								team.getColoredName()));
				return;
			}
		}

		final String sTeam = arena.isFreeForAll() ? "free" : calcFreeTeam(arena);

		arena.getDebugger().i(sTeam, player);

		final ArenaTeam aTeam = arena.getTeam(sTeam);
		aPlayer.setArena(arena);
		aTeam.add(aPlayer);

		if (arena.isFreeForAll()) {
			arena.tpPlayerToCoordName(player, "lounge");
			arena.msg(player,
					arena.getArenaConfig().getString(CFG.MSG_YOUJOINED));
			arena.broadcastExcept(
					player,
					Language.parse(arena, CFG.MSG_PLAYERJOINED,
							player.getName()));
		} else {
			arena.tpPlayerToCoordName(player, aTeam.getName() + "lounge");
			arena.msg(player,
					arena.getArenaConfig().getString(CFG.MSG_YOUJOINEDTEAM)
							.replace("%1%", aTeam.getColoredName() + "§r"));
			arena.broadcastExcept(
					player,
					Language.parse(arena, CFG.MSG_PLAYERJOINEDTEAM,
							player.getName(), aTeam.getColoredName() + "§r"));
		}
	}

	/**
	 * count all teams that have active players
	 * 
	 * @return the number of teams that have active players
	 */
	public static int countActiveTeams(final Arena arena) {
		arena.getDebugger().i("counting active teams");

		final Set<String> activeteams = new HashSet<String>();
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.FIGHT)) {
					activeteams.add(team.getName());
					break;
				}
			}
		}
		arena.getDebugger().i("result: " + activeteams.size());
		return activeteams.size();
	}

	/**
	 * count all players that have a team
	 * 
	 * @return the team player count
	 */
	public static int countPlayersInTeams(final Arena arena) {
		int result = 0;
		for (ArenaTeam team : arena.getTeams()) {
			result += team.getTeamMembers().size();
		}
		arena.getDebugger().i("players having a team: " + result);
		return result;
	}

	public static String getNotReadyTeamStringList(final Arena arena) {
		final StringBuffer result = new StringBuffer("");
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().size() < 1) {
				continue;
			}

			if (!result.equals("")) {
				result.append(", ");
			}
			for (ArenaPlayer p : team.getTeamMembers()) {
				if (p.getStatus().equals(Status.LOUNGE)) {
					if (!result.equals("")) {
						result.append(", ");
					}
					result.append(team.colorizePlayer(p.get()));
					result.append(ChatColor.WHITE.toString());
				} else {
					arena.getDebugger().i("player state: " + p.getStatus().name(), p.getName());
				}
			}
		}
		arena.getDebugger().i("notreadyteamstringlist: " + result);
		return result.toString();
	}

	/**
	 * parse all teams and join them colored, comma separated
	 * 
	 * @return a colorized, comma separated string
	 */
	public static String getTeamStringList(final Arena arena) {
		final StringBuffer result = new StringBuffer("");
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().size() < 1) {
				continue;
			}

			if (!result.equals("")) {
				result.append(", ");
			}
			for (ArenaPlayer p : team.getTeamMembers()) {
				if (!result.equals("")) {
					result.append(", ");
				}
				result.append(team.colorizePlayer(p.get()));
				result.append(ChatColor.WHITE.toString());
			}
		}
		arena.getDebugger().i("teamstringlist: " + result);
		return result.toString();
	}

	/**
	 * return all empty teams
	 * 
	 * @param set
	 *            the set to search
	 * @return one empty team name
	 */
	private static String returnEmptyTeam(final Arena arena, final Set<String> set) {
		arena.getDebugger().i("choosing an empty team");
		final Set<String> empty = new HashSet<String>();
		for (ArenaTeam team : arena.getTeams()) {
			final String teamName = team.getName();
			arena.getDebugger().i("team: " + teamName);
			if (set.contains(teamName)) {
				continue;
			}
			empty.add(teamName);
		}
		arena.getDebugger().i("empty.size: " + empty.size());
		if (empty.size() == 1) {
			for (String s : empty) {
				arena.getDebugger().i("return: " + s);
				return s;
			}
		}

		final Random random = new Random();
		int rand = random.nextInt(empty.size());
		for (String s : empty) {
			if (rand-- == 0) {
				return s;
			}
		}

		return null;
	}
}
