package net.slipcor.pvparena.managers;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
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
 * @version v0.6.3
 * 
 */

public class Dominate {

	/**
	 * check a moving player for nearby flags
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player to check
	 */
	public static void parseMove(Arena arena, Player player) {

		/*
		 * - noone there and not taken: takerunnable 10 seconds - another team
		 * there and not taken: return
		 * 
		 * - noone there and enemy taken: takerunnable 10 seconds - another team
		 * there and taken: takerunnable (false), untake!
		 */

		if (Players.parsePlayer(arena, player).spectator) {
			return; // spectator or dead. OUT
		}

		for (Location loc : Spawns.getSpawns(arena, "flags")) {

			int checkDistance = 5;

			if (player.getLocation().distance(loc) > checkDistance) {
				continue;
			}
			// player is at spawn location

			HashSet<String> teams = Dominate.checkLocationPresentTeams(loc,
					player, checkDistance);

			if (arena.paFlags.containsKey(loc)) {
				// flag taken - is there anyone?
				if (arena.paFlags.get(loc).equals(
						Players.parsePlayer(arena, player).team)) {
					// taken by own team, NEXT!
					// TODO: cancel unclaim event when moving outside of the
					// arena and own flag
					// and no other team here
					continue;
				}

				// taken by other team!

				if (arena.paRuns.containsKey(loc)) {
					if (arena.paRuns.get(loc).take) {
						// runnable is trying to score
						// abort
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
