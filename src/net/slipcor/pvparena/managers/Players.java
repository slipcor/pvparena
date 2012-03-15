package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.definitions.Announcement;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.ArenaClassSign;
import net.slipcor.pvparena.definitions.ArenaPlayer;
import net.slipcor.pvparena.definitions.Announcement.type;
import net.slipcor.pvparena.events.PALeaveEvent;

/**
 * player manager class
 * 
 * -
 * 
 * provides access to all arena players and their arena variables
 * 
 * @author slipcor
 * 
 * @version v0.6.35
 * 
 */

public class Players {
	// bets placed mapped to value: BetterName:BetName => Amount
	public static HashMap<String, Double> paPlayersBetAmount = new HashMap<String, Double>();

	static HashMap<String, ArenaPlayer> players = new HashMap<String, ArenaPlayer>();
	static HashMap<ArenaPlayer, String> deadPlayers = new HashMap<ArenaPlayer, String>();

	private static Debug db = new Debug(31);

	/**
	 * parse all teams and join them colored, comma separated
	 * 
	 * @param paTeams
	 *            the team hashmap to parse
	 * @return a colorized, comma separated string
	 */
	public static String getTeamStringList(Arena arena,
			HashMap<String, String> paTeams) {
		String result = "";
		for (ArenaPlayer p : players.values()) {
			if (p.arena == null || !p.arena.equals(arena)) {
				continue;
			}
			if (!p.team.equals("")) {

				if (!result.equals(""))
					result += ", ";
				result += arena.colorizePlayerByTeam(p.get(), p.team) + ChatColor.WHITE;
			} else {

				if (!result.equals(""))
					result += ", ";
				result += ChatColor.GRAY + p.get().getName() + ChatColor.WHITE;
			}
		}
		db.i("teamstringlist: " + result);
		return result;
	}

	/**
	 * get all players stuck into a map [playername]=>[player]
	 * 
	 * @return a map [playername]=>[teamname]
	 */
	public static HashMap<String, String> getPlayerTeamMap(Arena arena) {
		db.i("getTeamPlayerMap:");
		HashMap<String, String> result = new HashMap<String, String>();
		for (ArenaPlayer p : players.values()) {
			if (p.arena == null || !p.arena.equals(arena)) {
				continue;
			}
			if (!p.team.equals("") && !p.spectator) {
				result.put(p.get().getName(), p.team);
				db.i(" - " + p.get().getName() + " => " + p.team);
			}
		}
		return result;
	}

	/**
	 * check if the teams are equal
	 * 
	 * @return true if teams have the same amount of players, false otherwise
	 */
	public static boolean checkEven(Arena arena) {
		db.i("checkinv if teams are even");
		HashMap<String, Integer> counts = new HashMap<String, Integer>();

		// count each team members
		for (ArenaPlayer p : players.values()) {
			if (p.arena == null || !p.arena.equals(arena)) {
				continue;
			}
			if (!p.team.equals("")) {
				if (!counts.containsKey(p.team)) {
					counts.put(p.team, 1);
					db.i(p.team + ": " + 1);
				} else {
					int i = counts.get(p.team);
					counts.put(p.team, i);
					db.i(p.team + ": " + i);
				}
			}
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
	 * count all players that have a team
	 * 
	 * @return the team player count
	 */
	public static int countPlayersInTeams(Arena arena) {
		int result = 0;
		for (ArenaPlayer p : players.values()) {
			if (p.arena == null || !p.arena.equals(arena)) {
				continue;
			}
			db.i("player: " + p.get().getName());
			if (!p.team.equals("")) {
				db.i("- team " + p.team);
				result++;
			}
		}
		db.i("players having a team: " + result);
		return result;
	}

	/**
	 * check if a player is known
	 * 
	 * @param pPlayer
	 *            the player to find
	 * @return true if the player is known, false otherwise
	 */
	public static boolean isPartOf(Arena arena, Player pPlayer) {
		return (players.containsKey(pPlayer.getName())
				&& (players.get(pPlayer.getName()).arena != null) && (players
					.get(pPlayer.getName()).arena.equals(arena)));
	}

	/**
	 * check if an arena is ready
	 * 
	 * @param arena
	 *            the arena to check
	 * @return 1 if the arena is ready 0 if at least one player not ready -1 if
	 *         player is the only player -2 if only one team active -3 if not
	 *         enough players in a team -4 if not enough players -5 if at least
	 *         one player not selected class, -6 if counting down
	 */
	public static int ready(Arena arena) {

		if (countPlayersInTeams(arena) < 2) {
			return -1;
		}
		if (countPlayersInTeams(arena) < arena.cfg.getInt("ready.min")) {
			return -4;
		}

		if (arena.cfg.getDouble("ready.startRatio") > 0) {
			double ratio = arena.cfg.getDouble("ready.startRatio");

			int players = getPlayerTeamMap(arena).size();
			int readyPlayers = arena.paReady.size();

			if (players > 0 && readyPlayers / players >= ratio) {
				return -6;
			}
		}

		if (arena.cfg.getBoolean("ready.checkEach")) {
			for (String sPlayer : getPlayerTeamMap(arena).keySet()) {
				if (!arena.paReady.contains(sPlayer)) {
					return 0;
				}
			}
		}

		if (!arena.getType().equals("free")) {
			boolean onlyone = true;
			List<String> activeteams = new ArrayList<String>(0);
			db.i("ready(): reading playerteammap");
			HashMap<String, String> test = getPlayerTeamMap(arena);
			for (String sPlayer : test.keySet()) {
				db.i("player " + sPlayer);
				if (activeteams.size() < 1) {
					// fresh map
					String team = test.get(sPlayer);
					db.i("is in team " + team);
					activeteams.add(team);
				} else {
					db.i("map not empty");
					// map contains stuff
					if (!activeteams.contains(test.get(sPlayer))) {
						// second team active => OUT!
						onlyone = false;
						break;
					}
				}
			}
			if (onlyone) {
				return -2;
			}
			for (String sTeam : arena.paTeams.keySet()) {
				if (!test.containsValue(sTeam)) {
					db.i("skipping TEAM " + sTeam);
					continue;
				}
				db.i("TEAM " + sTeam);
				if (arena.cfg.getInt("ready.minTeam") > 0
						&& countPlayers(arena, sTeam) < arena.cfg
								.getInt("ready.minTeam")) {
					return -3;
				}
			}
		}
		for (ArenaPlayer p : players.values()) {
			if (p.arena == null || !p.arena.equals(arena)) {
				continue;
			}
			if (!p.team.equals("")) {
				if (p.aClass.equals("")) {
					// player not ready!
					return -5;
				}
			}
		}
		return 1;
	}

	/**
	 * count the players in a team
	 * 
	 * @param sTeam
	 * @return the team player count
	 */
	private static int countPlayers(Arena arena, String sTeam) {
		db.i("counting players in team " + sTeam);
		int result = 0;
		for (ArenaPlayer p : players.values()) {
			if (p.arena == null || !p.arena.equals(arena)) {
				continue;
			}
			db.i(" - player " + p.get().getName() + ", team "
					+ String.valueOf(p.team));
			if (p.team != null && p.team.equals(sTeam)) {
				result++;
			}
		}
		db.i("count result: " + result);
		return result;
	}

	/**
	 * reset an arena
	 * 
	 * @param arena
	 *            the arena to reset
	 * @param force
	 */
	public static void reset(Arena arena, boolean force) {
		db.i("resetting player manager");
		HashSet<ArenaPlayer> pa = new HashSet<ArenaPlayer>();
		for (ArenaPlayer p : players.values()) {
			if (p.arena == null || !p.arena.equals(arena)) {
				continue;
			}
			pa.add(p);
		}

		for (ArenaPlayer p : pa) {
			Player z = p.get();
			if (!force) {
				p.wins++;
			}
			arena.resetPlayer(z, arena.cfg.getString("tp.win", "old"));
			if (!force && !p.spectator && arena.fightInProgress) {
				arena.giveRewards(z); // if we are the winning team, give
										// reward!
			}
			p.destroy();
			if (!p.get().isDead()) {
				p.arena = null;
			}
		}
		if (force) {
			paPlayersBetAmount.clear();
		}
	}

	/**
	 * send a message to every playery
	 * 
	 * @param msg
	 *            the message to send
	 */
	public static void tellEveryone(Arena arena, String msg) {
		db.i("@all: " + msg);
		for (ArenaPlayer p : players.values()) {
			if (p.arena == null || !p.arena.equals(arena)) {
				continue;
			}
			Arenas.tellPlayer(p.get(), msg);
		}
	}

	/**
	 * send a message to every player except the given one
	 * 
	 * @param player
	 *            the player to exclude
	 * @param msg
	 *            the message to send
	 */
	public static void tellEveryoneExcept(Arena arena, Player player, String msg) {
		db.i("@all/" + player.getName() + ": " + msg);
		for (ArenaPlayer p : players.values()) {
			if (p.arena == null || !p.arena.equals(arena)) {
				continue;
			}
			if (p.get().equals(player))
				continue;
			Arenas.tellPlayer(p.get(), msg);
		}
	}

	/**
	 * send a message to every player of a given team
	 * 
	 * @param player
	 *            the team to send to
	 * @param msg
	 *            the message to send
	 * @param player
	 */
	public static void tellTeam(Arena arena, String team, String msg,
			ChatColor c, Player player) {
		if (team.equals("")) {
			return;
		}
		db.i("@" + team + ": " + msg);
		for (ArenaPlayer p : players.values()) {
			if (p.arena == null || !p.arena.equals(arena)) {
				continue;
			}
			if (!p.team.equals(team))
				continue;
			p.get().sendMessage(
					c + "[" + team + "] " + player.getName() + ChatColor.WHITE
							+ ": " + msg);
		}
	}

	/**
	 * return all players
	 * 
	 * @return a hashset of all players
	 */
	public static HashSet<ArenaPlayer> getPlayers(Arena arena) {
		HashSet<ArenaPlayer> result = new HashSet<ArenaPlayer>();
		for (ArenaPlayer p : players.values()) {
			if ((arena != null) && (p.arena == null || !p.arena.equals(arena))) {
				continue;
			}
			result.add(p);
		}
		return result;
	}

	/**
	 * hand over a player's class
	 * 
	 * @param player
	 *            the player to read
	 * @return the player's class name
	 */
	public static String getClass(Player player) {
		return players.get(player.getName()).aClass;
	}

	/**
	 * hand over a player class name
	 * 
	 * @param player
	 *            the player to update
	 * @param s
	 *            a player class name
	 */
	public static void setClass(Player player, String s) {
		players.get(player.getName()).aClass = s;
	}

	/**
	 * hand over a player's deaths
	 * 
	 * @param s
	 *            the player name to read
	 * @return the player's death count
	 */
	public static int getDeaths(String s) {
		return players.get(s).deaths;
	}

	/**
	 * hand over a player's kills
	 * 
	 * @param s
	 *            the player name to read
	 * @return the player's kill count
	 */
	public static int getKills(String s) {
		return players.get(s).kills;
	}

	/**
	 * add a kill to a player
	 * 
	 * @param sKiller
	 *            the player to add one
	 */
	public static void addKill(String sKiller) {
		players.get(sKiller).kills++;
	}

	/**
	 * add a death to a player
	 * 
	 * @param sKilled
	 *            the player to add one
	 */
	public static void addDeath(String sKilled) {
		players.get(sKilled).deaths++;
	}

	/**
	 * hand over a player's team name
	 * 
	 * @param player
	 *            the player to check
	 * @return the player's team name
	 */
	public static String getTeam(Player player) {
		return (players.get(player.getName()) == null) ? "" : players
				.get(player.getName()).team;
	}

	/**
	 * hand over a player's team name
	 * 
	 * @param player
	 *            the player to update
	 * @param s
	 *            the team name
	 */
	public static void setTeam(Player player, String s) {
		players.get(player.getName()).team = s;
	}

	/**
	 * hand over a player's tele pass
	 * 
	 * @param player
	 *            the player to update
	 * @param b
	 *            true if may pass, false otherwise
	 */
	public static void setTelePass(Player player, boolean b) {
		players.get(player.getName()).telePass = b;
	}

	/**
	 * hand over a player's tele pass
	 * 
	 * @param player
	 *            the player to check
	 * @return true if may pass, false otherwise
	 */
	public static boolean getTelePass(Player player) {
		return players.get(player.getName()).telePass;
	}

	/**
	 * add a player to the player map
	 * 
	 * @param player
	 *            the player to add
	 */
	public static void addPlayer(Arena arena, Player player) {
		players.put(player.getName(), new ArenaPlayer(player, arena));
	}

	/**
	 * remove a player from the arena
	 * 
	 * @param player
	 *            the player to remove
	 */
	public static void remove(Arena arena, Player player) {
		PALeaveEvent event = new PALeaveEvent(arena, player, players.get(player
				.getName()).spectator);
		Bukkit.getPluginManager().callEvent(event);

		players.get(player.getName()).arena = null;
	}

	/**
	 * get an ArenaPlayer from a player
	 * 
	 * @param player
	 *            the player to get
	 * @return an ArenaPlayer instance belonging to that player
	 */
	public static ArenaPlayer parsePlayer(Player player) {
		if (Players.players.get(player.getName()) == null) {
			Players.players
					.put(player.getName(), new ArenaPlayer(player, null));
		}
		return Players.players.get(player.getName());
	}

	public static void chooseClass(Arena arena, Player player, Sign sign,
			String className) {

		db.i("forcing player class");

		if (sign != null) {

			boolean classperms = false;
			if (arena.cfg.get("general.classperms") != null) {
				classperms = arena.cfg.getBoolean("general.classperms", false);
			}

			if (classperms) {
				db.i("checking class perms");
				if (!(PVPArena.hasPerms(player, "pvparena.class." + className))) {
					Arenas.tellPlayer(player, Language.parse("classperms"));
					return; // class permission desired and failed =>
							// announce and OUT
				}
			}

			if (arena.cfg.getBoolean("general.signs")) {
				ArenaClassSign.remove(arena.paSigns, player);
				Block block = sign.getBlock();
				ArenaClassSign as = ArenaClassSign.used(block.getLocation(),
						arena.paSigns);
				if (as == null) {
					as = new ArenaClassSign(block.getLocation());
				}
				arena.paSigns.add(as);
				if (!as.add(player)) {
					Arenas.tellPlayer(player, Language.parse("classfull"));
					return;
				}
			}
		}
		Inventories.clearInventory(player);
		Players.setClass(player, className);
		if (className.equalsIgnoreCase("custom")) {
			// if custom, give stuff back
			Inventories.loadInventory(arena, player);
		} else {
			Inventories.givePlayerFightItems(arena, player);
		}
	}

	public static void playerLeave(Arena arena, Player player) {
		db.i("fully removing player from arena");
		ArenaPlayer ap = Players.parsePlayer(player);
		boolean spectator = ap.spectator;

		if (!spectator) {

			Announcement.announce(arena, type.LOSER,
					Language.parse("playerleave", player.getName()));

			Players.tellEveryoneExcept(
					arena,
					player,
					Language.parse("playerleave", arena.colorizePlayerByTeam(player) + ChatColor.YELLOW));

			Arenas.tellPlayer(player, Language.parse("youleave"));
		}
		arena.removePlayer(player, arena.cfg.getString("tp.exit", "exit"));

		ap.destroy();

		if (!spectator && arena.fightInProgress) {
			Ends.checkAndCommit(arena);
		}
	}

	/**
	 * return an understandable representation of a player's death cause
	 * 
	 * @param arena
	 *            the arena the death is happening in
	 * @param player
	 *            the dying player
	 * @param cause
	 *            the cause
	 * @param damager
	 *            an eventual damager entity
	 * @return a colored string
	 */
	public static String parseDeathCause(Arena arena, Player player,
			DamageCause cause, Entity damager) {

		db.i("return a damage name for : "+cause.toString());

		switch (cause) {
		case ENTITY_ATTACK:
			if (damager instanceof Player) {
				ArenaPlayer ap = parsePlayer((Player) damager);
				if (ap != null) {
					return arena.colorizePlayerByTeam(ap.get(), ap.team)
							+ ap.get().getName() + ChatColor.YELLOW;
				}
			}
			return Language.parse("custom");
		case PROJECTILE:
			if (damager instanceof Player) {
				ArenaPlayer ap = parsePlayer((Player) damager);
				if (ap != null) {
					return arena.colorizePlayerByTeam(ap.get(), ap.team) + ChatColor.YELLOW;
				}
			}
			return Language.parse(cause.toString().toLowerCase());
		default:
			return Language.parse(cause.toString().toLowerCase());
		}
	}

	/**
	 * add a dead player to the dead player map
	 * 
	 * @param player
	 *            the player to add
	 * @param location
	 *            the location to respawn
	 */
	public static void addDeadPlayer(ArenaPlayer player, String string) {
		deadPlayers.put(player, string);
	}

	/**
	 * has a player died in the arena?
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the arena has died, false otherwise
	 */
	public static boolean isDead(Player player) {
		for (ArenaPlayer ap : deadPlayers.keySet()) {
			if (ap.get().equals(player)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get the respawn location of a dead player
	 * 
	 * @param player
	 *            the dead player
	 * @return the respawn location
	 */
	public static Location getDeadLocation(Arena arena, Player player) {
		String string = null;
		db.i("fetching dead player's location");
		for (ArenaPlayer ap : deadPlayers.keySet()) {
			db.i("checking player: "+ap.get().getName());
			if (ap.get().equals(player)) {
				db.i("there you are!");
				string = deadPlayers.get(ap);
				db.i("plaayer will spawn at: "+string);
				if (string.equalsIgnoreCase("old")) {
					return ap.location;
				} else {
					return Spawns.getCoords(arena, string);
				}
			}
		}
		return null;
	}

	/**
	 * remove the dead player from the map
	 * 
	 * @param player
	 *            the player to remove
	 */
	public static void removeDeadPlayer(Arena arena, Player player) {
		if (arena != null) {
			arena.resetPlayer(player, arena.cfg.getString("tp.death", "spectator"));
		}
		for (ArenaPlayer ap : deadPlayers.keySet()) {
			if (ap.get().equals(player)) {
				ap.arena.resetPlayer(player, ap.arena.cfg.getString("tp.death", "spectator"));
				deadPlayers.remove(ap);
				ap.arena = null;
				return;
			}
		}
		deadPlayers.remove(player);
	}
}
