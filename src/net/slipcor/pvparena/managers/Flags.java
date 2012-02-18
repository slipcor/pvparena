package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.runnables.DominationRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * flag manager class
 * 
 * -
 * 
 * provides commands to deal with flags/pumpkins
 * 
 * @author slipcor
 * 
 * @version v0.6.2
 * 
 */

public class Flags {

	// protected static: Debug manager (same for all child Arenas)
	public static final Debug db = new Debug();
	
	/**
	 * [FLAG] take away one life of a team
	 * 
	 * @param team
	 *            the team name to take away
	 */
	public static void reduceLivesCheckEndAndCommit(Arena arena, String team) {
		if (arena.paLives.get(team) != null) {
			int i = arena.paLives.get(team) - 1;
			if (i > 0) {
				arena.paLives.put(team, i);
			} else {
				arena.paLives.remove(team);
				Ends.commit(arena, team, false);
			}
		}
	}

	/**
	 * get the team name of the flag a player holds
	 * 
	 * @param player
	 *            the player to check
	 * @return a team name
	 */
	protected static String getHeldFlagTeam(Arena arena, String player) {
		db.i("getting held FLAG of player " + player);
		for (String sTeam : arena.paTeamFlags.keySet()) {
			db.i("team " + sTeam + " is in " + arena.paTeamFlags.get(sTeam)
					+ "s hands");
			if (player.equals(arena.paTeamFlags.get(sTeam))) {
				return sTeam;
			}
		}
		return null;
	}

	/**
	 * parse player interaction
	 * 
	 * @param player
	 *            the player to parse
	 * @param block
	 *            the clicked block
	 */
	public static void checkInteract(Arena arena, Player player, Block block) {

		boolean pumpkin = arena.cfg.getBoolean("arenatype.pumpkin");

		if (block == null) {
			return;
		}

		if (pumpkin && !block.getType().equals(Material.PUMPKIN)) {
			return;
		} else if (!pumpkin && !block.getType().equals(Material.WOOL)) {
			return;
		}
		String type = null;
		if (pumpkin) {
			type = "pumpkin";
		} else {
			type = "flag";
		}
		db.i(type + " click!");

		Vector vLoc;
		String sTeam;
		Vector vFlag = null;

		if (arena.paTeamFlags.containsValue(player.getName())) {
			db.i("player " + player.getName() + " has got a " + type);
			vLoc = block.getLocation().toVector();
			sTeam = arena.pm.getTeam(player);
			db.i("block: " + vLoc.toString());
			if (Spawns.getCoords(arena, sTeam + type) != null) {
				vFlag = Spawns.getCoords(arena, sTeam + type).toVector();
			} else {
				db.i(sTeam + type + " = null");
			}

			db.i("player is in the team " + sTeam);
			if ((vFlag != null && vLoc.distance(vFlag) < 2)) {

				db.i("player is at his " + type);
				
				
				if (arena.paTeamFlags.containsKey(sTeam)) {
					db.i("the "+ type + " of the own team is taken!");
					
					if (arena.cfg.getBoolean("game.mustbesafe")) {
						db.i("cancelling");
						
						Arenas.tellPlayer(player, PVPArena.lang.parse(type+"notsafe"));
						return;
					}
				}
				
				
				String flagTeam = getHeldFlagTeam(arena, player.getName());

				db.i("the " + type + " belongs to team " + flagTeam);

				String scFlagTeam = ChatColor.valueOf(arena.paTeams
						.get(flagTeam)) + flagTeam + ChatColor.YELLOW;
				String scPlayer = ChatColor.valueOf(arena.paTeams.get(sTeam))
						+ player.getName() + ChatColor.YELLOW;
				
				try {
				
				arena.pm.tellEveryone(PVPArena.lang.parse(type + "homeleft",
						scPlayer, scFlagTeam,
						String.valueOf(arena.paLives.get(flagTeam) - 1)));
				arena.paTeamFlags.remove(flagTeam);
				} catch (Exception e) {
					Bukkit.getLogger().severe("[PVP Arena] team unknown/no lives: "+flagTeam);
				}
				takeFlag(arena.paTeams.get(flagTeam), false, pumpkin,
						Spawns.getCoords(arena, flagTeam + type));

				player.getInventory().setHelmet(
						arena.paHeadGears.get(player.getName()).clone());
				arena.paHeadGears.remove(player.getName());

				reduceLivesCheckEndAndCommit(arena, flagTeam);
			}
		} else {
			for (String team : arena.paTeams.keySet()) {
				String playerTeam = arena.pm.getTeam(player);
				if (team.equals(playerTeam))
					continue;
				if (!arena.pm.getPlayerTeamMap().containsValue(team))
					continue; // dont check for inactive teams
				if (arena.paTeamFlags.containsKey(team)) {
					continue; // already taken
				}
				db.i("checking for " + type + " of team " + team);
				vLoc = block.getLocation().toVector();
				db.i("block: " + vLoc.toString());
				if (Spawns.getCoords(arena, team + type) != null) {
					vFlag = Spawns.getCoords(arena, team + type).toVector();
				}
				if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
					db.i(type + " found!");
					db.i("vFlag: " + vFlag.toString());
					String scTeam = ChatColor.valueOf(arena.paTeams.get(team))
							+ team + ChatColor.YELLOW;
					String scPlayer = ChatColor.valueOf(arena.paTeams
							.get(playerTeam))
							+ player.getName()
							+ ChatColor.YELLOW;
					arena.pm.tellEveryone(PVPArena.lang.parse(type + "grab",
							scPlayer, scTeam));

					arena.paHeadGears.put(player.getName(), player
							.getInventory().getHelmet().clone());
					player.getInventory().setHelmet(
							block.getState().getData().toItemStack().clone());

					takeFlag(arena.paTeams.get(team), true, pumpkin,
							block.getLocation());

					arena.paTeamFlags.put(team, player.getName());
					return;
				}
			}
		}
	}

	private static void takeFlag(String flagColor, boolean take,
			boolean pumpkin, Location lBlock) {
		if (pumpkin) {
			return;
		}

		if (take) {
			lBlock.getBlock().setData(
					StringParser.getColorDataFromENUM("WHITE"));
		} else {
			lBlock.getBlock().setData(
					StringParser.getColorDataFromENUM(flagColor));
		}
	}

	/*
	 * set the pumpkin to the selected block
	 */
	public static void setFlag(Arena arena, Player player, Block block) {
		if (block == null) {
			return;
		}
		boolean pumpkin = arena.cfg.getBoolean("arenatype.pumpkin");

		if (pumpkin && !block.getType().equals(Material.PUMPKIN)) {
			return;
		} else if (!pumpkin && !block.getType().equals(Material.WOOL)) {
			return;
		}

		String type = null;
		if (pumpkin) {
			type = "pumpkin";
		} else {
			type = "flag";
		}

		String sName = Arena.regionmodify.replace(arena.name + ":", "");

		Location location = block.getLocation();

		Integer x = location.getBlockX();
		Integer y = location.getBlockY();
		Integer z = location.getBlockZ();
		Float yaw = location.getYaw();
		Float pitch = location.getPitch();

		String s = x.toString() + "," + y.toString() + "," + z.toString() + ","
				+ yaw.toString() + "," + pitch.toString();

		arena.cfg.set("spawns." + sName + type, s);

		arena.cfg.save();
		Arenas.tellPlayer(player, PVPArena.lang.parse("set" + type, sName));

		Arena.regionmodify = "";
	}

	/**
	 * check a dying player if he held a flag, drop it, if so
	 * 
	 * @param player
	 *            the player to check
	 */
	public static void checkEntityDeath(Arena arena, Player player) {
		boolean pumpkin = arena.cfg.getBoolean("arenatype.pumpkin");

		String type = null;
		if (pumpkin) {
			type = "pumpkin";
		} else {
			type = "flag";
		}

		String flagTeam = getHeldFlagTeam(arena, player.getName());
		if (flagTeam != null) {
			String scFlagTeam = ChatColor.valueOf(arena.paTeams.get(flagTeam))
					+ flagTeam + ChatColor.YELLOW;
			String scPlayer = ChatColor.valueOf(arena.paTeams.get(arena.pm
					.getTeam(player))) + player.getName() + ChatColor.YELLOW;
			arena.pm.tellEveryone(PVPArena.lang.parse(type + "save", scPlayer,
					scFlagTeam));
			arena.paTeamFlags.remove(flagTeam);
			if (arena.paHeadGears != null) {
				player.getInventory().setHelmet(
						arena.paHeadGears.get(player.getName()).clone());
				arena.paHeadGears.remove(player.getName());
			}

			takeFlag(arena.paTeams.get(flagTeam), false, pumpkin,
					Spawns.getCoords(arena, flagTeam + type));
			
		}
	}

	/**
	 * method for CTF arena to override
	 */
	public static void init_arena(Arena arena) {
		for (String sTeam : arena.paTeams.keySet()) {
			if (arena.pm.getPlayerTeamMap().containsValue(sTeam)) {
				// team is active
				arena.paLives.put(sTeam, arena.cfg.getInt("game.lives", 3));
			}
		}
		if (arena.cfg.getBoolean("arenatype.domination")) {
			arena.paFlags = new HashMap<Location, String>();
		}
	}

	public static void parseMove(Arena arena, Player player) {
		// TODO check for being near a flag, check if taken, commit runnables if so:
		
		
		/*
		 * - noone there and not taken: takerunnable 10 seconds
		 * - another team there and not taken: return
		 * 
		 * - noone there and enemy taken: takerunnable 10 seconds
		 * - another team there and taken: takerunnable (false), untake!
		 * 
		 */
		
		if (arena.pm.parsePlayer(player).spectator) {
			return; // spectator or dead. OUT
		}
		
		for (Location loc : Spawns.getSpawns(arena, "flags")) {
			
			int checkDistance = 5;
			
			if (player.getLocation().distance(loc) > checkDistance) {
				continue;
			}
			// player is at spawn location
			
			HashSet<String> teams = arena.pm.checkLocationPresentTeams(loc, player, checkDistance);
			
			if (arena.paFlags.containsKey(loc)) {
				// flag taken - is there anyone?
				if (arena.paFlags.get(loc).equals(arena.pm.parsePlayer(player).team)) {
					// taken by own team, NEXT!
					//TODO: cancel unclaim event when moving outside of the arena and own flag
					//      and no other team here
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
					if (teams.size()<1) {
						DominationRunnable running = new DominationRunnable(arena, false, loc, "");
						long interval = 20L * 60 * 10;
						Bukkit.getScheduler().scheduleSyncDelayedTask(
								PVPArena.instance,
								running, interval);
						arena.paRuns.put(loc, running);
					}
				}
			} else {
				// flag not taken, is there anyone else?
				if (teams.size() < 1) {
					// noone there! initiate take runnable
					DominationRunnable running = new DominationRunnable(arena, true, loc, arena.pm.parsePlayer(player).team);
					long interval = 20L * 60 * 10;
					Bukkit.getScheduler().scheduleSyncDelayedTask(
							PVPArena.instance,
							running, interval);
					arena.paRuns.put(loc, running);
				}
			}
		}
		
	}
}
