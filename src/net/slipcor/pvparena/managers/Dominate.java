package net.slipcor.pvparena.managers;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.ArenaPlayer;
import net.slipcor.pvparena.runnables.DominationRunnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * domination manager class
 * 
 * -
 * 
 * provides commands to deal with domination mode
 * 
 * @author slipcor
 * 
 * @version v0.6.21
 * 
 */

public class Dominate {

	private static Debug db = new Debug(27);

	/**
	 * check a moving player for nearby flags
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player to check
	 */
	public static void parseMove(Arena arena, Player player) {
		if (Players.parsePlayer(arena, player).spectator) {
			return; // spectator or dead. OUT
		}
		
		// player is alive and no spectator
		
		int checkDistance = 5;
		boolean found = false;
		
		for (Location loc : Spawns.getSpawns(arena, "flags")) {
			// check every flag location
			
			if (player.getLocation().distance(loc) > checkDistance) {
				continue; // not near the flag. NEXT
			}
			
			found = true; // mark a found flag!
			// player is at spawn location

			HashSet<String> teams = Dominate.checkLocationPresentTeams(loc,
					player, checkDistance);

			// teams now contains all (other) teams near the flag
			
			if (arena.paFlags.containsKey(loc)) {
				
				// flag is taken. by whom?
				
				if (arena.paFlags.get(loc).equals(
						Players.parsePlayer(arena, player).team)) {
					// taken by own team, NEXT!
					continue;
				}

				// taken by other team!

				// TODO logic? -.-
				
				if (arena.paRuns.containsKey(loc)) {
					if (arena.paRuns.get(loc).take) {
						db.i("runnable is trying to score, abort");
						int del_id = arena.paRuns.get(loc).ID;
						Bukkit.getScheduler().cancelTask(del_id);

						arena.paRuns.remove(loc);
					}
					// if runnable is !take, we are trying to
				} else {
					// no runnable - start one if no enemy player near!
					if (teams.size() < 1) {
						DominationRunnable running = new DominationRunnable(
								arena, false, loc, "");
						long interval = 20L * 10;
						Bukkit.getScheduler().scheduleSyncDelayedTask(
								PVPArena.instance, running, interval);
						arena.paRuns.put(loc, running);
					}
				}
			} else {
				// flag not taken, is there anyone else?
				if (teams.size() < 1) {
					// noone there! initiate take runnable

					if (arena.paRuns.containsKey(loc)) {
						return;
					}

					DominationRunnable running = new DominationRunnable(arena,
							true, loc, Players.parsePlayer(arena, player).team);
					long interval = 20L * 10;
					Bukkit.getScheduler().scheduleSyncDelayedTask(
							PVPArena.instance, running, interval);
					arena.paRuns.put(loc, running);
				}
			}
		}
		if (!found) {
			// player is not near any flag. Check if player
			// team tried to claim any flags
			// and cancel if no players around
			for (DominationRunnable run : arena.paRuns.values()) {
				if (run.noOneThere(checkDistance)) {
					Bukkit.getScheduler().cancelTask(run.ID);
					arena.paRuns.remove(run.loc);
					return;
				}
			}
		}
	}

	/**
	 * return a hashset of players names being near a specified location, except
	 * one player
	 * 
	 * @param loc
	 *            the location to check
	 * @param player
	 *            the player to exclude
	 * @param distance
	 *            the distance in blocks
	 * @return a set of player names
	 */
	public static HashSet<String> checkLocationPresentTeams(Location loc,
			Player player, int distance) {
		HashSet<String> result = new HashSet<String>();
		Arena arena = Arenas.getArenaByPlayer(player);
		String sTeam = Players.parsePlayer(arena, player).team;

		for (ArenaPlayer p : arena.pm.players.values()) {
			if (p.get().getLocation().distance(loc) > distance) {
				continue;
			}

			if (p.get().getName().equals(player.getName())) {
				continue;
			}

			if (p.team.equals(sTeam)) {
				continue;
			}

			result.add(p.team);
		}

		return result;
	}
}
