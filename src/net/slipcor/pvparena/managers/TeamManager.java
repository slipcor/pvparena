package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config.CFG;

import java.util.*;

/**
 * <pre>
 * Arena Team Manager class
 * </pre>
 * <p/>
 * Provides static methods to manage Arena Teams
 *
 * @author slipcor
 * @version v0.10.2
 */

public final class TeamManager {
    private TeamManager() {}

    /**
     * calculate the team that needs players the most
     *
     * @return the team name
     */
    public static String calcFreeTeam(final Arena arena) {
        arena.getDebugger().i("calculating free team");
        final Map<String, Integer> counts = new HashMap<>();

        // spam the available teams into a map counting the members

        for (final ArenaTeam team : arena.getTeams()) {
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

        for (final ArenaTeam team : arena.getTeams()) {
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

        final Set<String> free = new HashSet<>();

        int max = arena.getArenaConfig().getInt(CFG.READY_MAXTEAMPLAYERS);
        max = max == 0 ? Integer.MAX_VALUE : max;
        // calculate the max value down to the minimum
        for (final Map.Entry<String, Integer> stringIntegerEntry : counts.entrySet()) {
            final int count = stringIntegerEntry.getValue();
            if (count < max) {
                free.clear();
                free.add(stringIntegerEntry.getKey());
                max = count;
            } else if (count == max) {
                free.add(stringIntegerEntry.getKey());
            }
        }

        // free now has the minimum teams

        if (free.size() == 1) {
            for (final String s : free) {
                return s;
            }
        }

        if (free.size() < 1) {
            return null;
        }

        final Random random = new Random();
        int rand = random.nextInt(free.size());
        for (final String s : free) {
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
        final Map<String, Integer> counts = new HashMap<>();

        // count each team members

        for (final ArenaTeam team : arena.getTeams()) {
            arena.getDebugger().i(team.getName() + ": " + team.getTeamMembers().size());
            counts.put(team.getName(), team.getTeamMembers().size());
        }

        if (counts.size() < 1) {
            arena.getDebugger().i("noone in there");
            return false; // noone there => not even
        }

        int temp = -1;
        for (final int i : counts.values()) {
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
     * count all teams that have active players
     *
     * @return the number of teams that have active players
     */
    public static int countActiveTeams(final Arena arena) {
        arena.getDebugger().i("counting active teams");

        final Set<String> activeteams = new HashSet<>();
        for (final ArenaTeam team : arena.getTeams()) {
            for (final ArenaPlayer ap : team.getTeamMembers()) {
                if (ap.getStatus() == Status.FIGHT) {
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
        for (final ArenaTeam team : arena.getTeams()) {
            result += team.getTeamMembers().size();
        }
        arena.getDebugger().i("players having a team: " + result);
        return result;
    }

    /**
     * return all empty teams
     *
     * @param set the set to search
     * @return one empty team name
     */
    private static String returnEmptyTeam(final Arena arena, final Set<String> set) {
        arena.getDebugger().i("choosing an empty team");
        final Set<String> empty = new HashSet<>();
        for (final ArenaTeam team : arena.getTeams()) {
            final String teamName = team.getName();
            arena.getDebugger().i("team: " + teamName);
            if (set.contains(teamName)) {
                continue;
            }
            empty.add(teamName);
        }
        arena.getDebugger().i("empty.size: " + empty.size());
        if (empty.size() == 1) {
            for (final String s : empty) {
                arena.getDebugger().i("return: " + s);
                return s;
            }
        }

        final Random random = new Random();
        int rand = random.nextInt(empty.size());
        for (final String s : empty) {
            if (rand-- == 0) {
                return s;
            }
        }

        return null;
    }
}
