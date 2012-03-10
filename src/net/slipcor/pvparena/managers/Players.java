package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
 * @version v0.6.26
 * 
 */

public class Players {
	// bets placed mapped to value: BetterName:BetName => Amount
	public HashMap<String, Double> paPlayersBetAmount = new HashMap<String, Double>();
	private final Arena arena;
	
	HashMap<String, ArenaPlayer> players = new HashMap<String, ArenaPlayer>();
	private static Debug db = new Debug(31);

	public Players(Arena a) {
		arena = a;
	}
	
	/**
	 * parse all teams and join them colored, comma separated
	 * 
	 * @param paTeams
	 *            the team hashmap to parse
	 * @return a colorized, comma separated string
	 */
	public String getTeamStringList(HashMap<String, String> paTeams) {
		String result = "";
		for (ArenaPlayer p : players.values()) {
			if (!p.team.equals("")) {

				if (!result.equals(""))
					result += ", ";
				result += ChatColor.valueOf(paTeams.get(p.team))
						+ p.get().getName() + ChatColor.WHITE;
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
	public HashMap<String, String> getPlayerTeamMap() {
		db.i("getTeamPlayerMap:");
		HashMap<String, String> result = new HashMap<String, String>();
		for (ArenaPlayer p : players.values()) {
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
	public boolean checkEven() {
		db.i("checkinv if teams are even");
		HashMap<String, Integer> counts = new HashMap<String, Integer>();

		// count each team members
		for (ArenaPlayer p : players.values()) {
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
	public int countPlayersInTeams() {
		int result = 0;
		for (ArenaPlayer p : players.values()) {
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
	public boolean existsPlayer(Player pPlayer) {
		return players.containsKey(pPlayer.getName());
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
	public int ready(Arena arena) {

		if (countPlayersInTeams() < 2) {
			return -1;
		}
		if (countPlayersInTeams() < arena.cfg.getInt("ready.min")) {
			return -4;
		}

		if (arena.cfg.getDouble("ready.startRatio") > 0) {
			double ratio = arena.cfg.getDouble("ready.startRatio");
			
			int players = getPlayerTeamMap().size();
			int readyPlayers = arena.paReady.size();
			
			if (readyPlayers / players >= ratio) {
				return -6;
			}
		}

		if (arena.cfg.getBoolean("ready.checkEach")) {
			for (String sPlayer : getPlayerTeamMap().keySet()) {
				if (!arena.paReady.contains(sPlayer)) {
					return 0;
				}
			}
		}

		if (!arena.getType().equals("free")) {
			boolean onlyone = true;
			List<String> activeteams = new ArrayList<String>(0);
			db.i("ready(): reading playerteammap");
			HashMap<String, String> test = getPlayerTeamMap();
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
						&& countPlayers(sTeam) < arena.cfg
								.getInt("ready.minTeam")) {
					return -3;
				}
			}
		}
		for (ArenaPlayer p : players.values()) {
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
	private int countPlayers(String sTeam) {
		db.i("counting players in team " + sTeam);
		int result = 0;
		for (ArenaPlayer p : players.values()) {
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
	public void reset(Arena arena, boolean force) {
		db.i("resetting player manager");
		HashSet<ArenaPlayer> pa = new HashSet<ArenaPlayer>();
		for (ArenaPlayer p : players.values()) {
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
			p = null;
		}
		players.clear();
		paPlayersBetAmount.clear();
	}

	/**
	 * send a message to every playery
	 * 
	 * @param msg
	 *            the message to send
	 */
	public void tellEveryone(String msg) {
		db.i("@all: " + msg);
		for (ArenaPlayer p : players.values()) {
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
	public void tellEveryoneExcept(Player player, String msg) {
		db.i("@all/" + player.getName() + ": " + msg);
		for (ArenaPlayer p : players.values()) {
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
	public void tellTeam(String team, String msg, ChatColor c, Player player) {
		if (team.equals("")) {
			return;
		}
		db.i("@" + team + ": " + msg);
		for (ArenaPlayer p : players.values()) {
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
	public HashSet<ArenaPlayer> getPlayers() {
		HashSet<ArenaPlayer> result = new HashSet<ArenaPlayer>();
		for (ArenaPlayer p : players.values()) {
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
	public String getClass(Player player) {
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
	public void setClass(Player player, String s) {
		players.get(player.getName()).aClass = s;
	}

	/**
	 * hand over a player's deaths
	 * 
	 * @param s
	 *            the player name to read
	 * @return the player's death count
	 */
	public int getDeaths(String s) {
		return players.get(s).deaths;
	}

	/**
	 * hand over a player's kills
	 * 
	 * @param s
	 *            the player name to read
	 * @return the player's kill count
	 */
	public int getKills(String s) {
		return players.get(s).kills;
	}

	/**
	 * add a kill to a player
	 * 
	 * @param sKiller
	 *            the player to add one
	 */
	public void addKill(String sKiller) {
		players.get(sKiller).kills++;
	}

	/**
	 * add a death to a player
	 * 
	 * @param sKilled
	 *            the player to add one
	 */
	public void addDeath(String sKilled) {
		players.get(sKilled).deaths++;
	}

	/**
	 * hand over a player's team name
	 * 
	 * @param player
	 *            the player to check
	 * @return the player's team name
	 */
	public String getTeam(Player player) {
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
	public void setTeam(Player player, String s) {
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
	public void setTelePass(Player player, boolean b) {
		players.get(player.getName()).telePass = b;
	}

	/**
	 * hand over a player's tele pass
	 * 
	 * @param player
	 *            the player to check
	 * @return true if may pass, false otherwise
	 */
	public boolean getTelePass(Player player) {
		return players.get(player.getName()).telePass;
	}

	/**
	 * add a player to the player map
	 * 
	 * @param player
	 *            the player to add
	 */
	public void addPlayer(Player player) {
		players.put(player.getName(), new ArenaPlayer(player));
	}

	/**
	 * remove a player from the player map
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void remove(Player player) {
		PALeaveEvent event = new PALeaveEvent(arena, player, players.get(player.getName()).spectator);
		Bukkit.getPluginManager().callEvent(event);
		
		players.remove(player.getName());
	}

	/**
	 * get an ArenaPlayer from a player
	 * 
	 * @param player
	 *            the player to get
	 * @return an ArenaPlayer instace belonging to that player
	 */
	public static ArenaPlayer parsePlayer(Arena arena, Player player) {
		return arena.pm.players.get(player.getName());
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
		arena.pm.setClass(player, className);
		if (className.equalsIgnoreCase("custom")) {
			// if custom, give stuff back
			Inventories.loadInventory(arena, player);
		} else {
			Inventories.givePlayerFightItems(arena, player);
		}
	}

	public static void playerLeave(Arena arena, Player player) {
		db.i("fully removing player from arena");
		ArenaPlayer ap = Players.parsePlayer(arena, player);
		boolean spectator = ap.spectator;

		String color = arena.paTeams.get(arena.pm.getTeam(player));

		if (!spectator) {

			Announcement.announce(arena, type.LOSER,
					Language.parse("playerleave", player.getName()));

			arena.pm.tellEveryoneExcept(
					player,
					Language.parse("playerleave", ChatColor.valueOf(color)
							+ player.getName() + ChatColor.YELLOW));

			Arenas.tellPlayer(player, Language.parse("youleave"));
		}
		arena.removePlayer(player, arena.cfg.getString("tp.exit", "exit"));

		ap.destroy();
		ap = null;

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

		db.i("return a ");

		switch (cause) {
		case ENTITY_ATTACK:
			if (damager instanceof Player) {
				ArenaPlayer ap = parsePlayer(arena, (Player) damager);
				if (ap != null) {
					return ChatColor.valueOf(arena.paTeams.get(ap.team))
							+ ap.get().getName() + ChatColor.YELLOW;
				}
			}
			return Language.parse("custom");
		case PROJECTILE:
			if (damager instanceof Player) {
				ArenaPlayer ap = parsePlayer(arena, (Player) damager);
				if (ap != null) {
					return ChatColor.valueOf(arena.paTeams.get(ap.team))
							+ ap.get().getName() + ChatColor.YELLOW;
				}
			}
			return Language.parse(cause.toString().toLowerCase());
		default:
			return Language.parse(cause.toString().toLowerCase());
		}
	}
}
