package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PAPlayer;
import net.slipcor.pvparena.arenas.Arena;

/**
 * player manager class
 * 
 * -
 * 
 * provides access to all arena players and their arena variables
 * 
 * @author slipcor
 * 
 * @version v0.5.3
 * 
 */

public class PlayerManager {
	// bets placed mapped to value: BetterName:BetName => Amount
	public HashMap<String, Double> paPlayersBetAmount = new HashMap<String, Double>();

	private HashMap<String, PAPlayer> players = new HashMap<String, PAPlayer>();
	private HashMap<String, Integer> kills = new HashMap<String, Integer>();
	private HashMap<String, Integer> deaths = new HashMap<String, Integer>();
	private DebugManager db = new DebugManager();

	/**
	 * parse all teams and join them colored, comma separated
	 * 
	 * @param paTeams
	 *            the team hashmap to parse
	 * @return a colorized, comma separated string
	 */
	public String getTeamStringList(HashMap<String, String> paTeams) {
		String result = "";
		for (PAPlayer p : players.values()) {
			if (!p.getTeam().equals("")) {

				if (!result.equals(""))
					result += ", ";
				result += ChatColor.valueOf(paTeams.get(p.getTeam()))
						+ p.getPlayer().getName() + ChatColor.WHITE;
			}
		}
		return result;
	}

	/**
	 * get all players stuck into a map [playername]=>[player]
	 * 
	 * @return a map [playername]=>[player]
	 */
	public HashMap<String, String> getPlayerTeamMap() {
		HashMap<String, String> result = new HashMap<String, String>();
		for (PAPlayer p : players.values()) {
			if (!p.getTeam().equals("")) {
				result.put(p.getPlayer().getName(), p.getTeam());
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
		HashMap<String, Integer> counts = new HashMap<String, Integer>();

		// count each team members
		for (PAPlayer p : players.values()) {
			if (!p.getTeam().equals("")) {
				if (!counts.containsKey(p.getTeam())) {
					counts.put(p.getTeam(), 1);
				} else {
					int i = counts.get(p.getTeam());
					counts.put(p.getTeam(), i);
				}
			}
		}

		if (counts.size() < 1)
			return false; // noone there => not even

		int temp = -1;
		for (int i : counts.values()) {
			if (temp == -1) {
				temp = i;
				continue;
			}
			if (temp != i)
				return false; // different count => not even
		}
		return true; // every team has the same player count!
	}

	/**
	 * count all players that have a team
	 * 
	 * @return the team player count
	 */
	public int countPlayersInTeams() {
		int result = 0;
		for (PAPlayer p : players.values()) {
			if (!p.getTeam().equals("")) {
				result++;
			}
		}
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
	 * @return true if the arena is ready, false otherwise
	 */
	public boolean ready(Arena arena) {
		if (countPlayersInTeams() < 2) {
			return false;
		}
		if (!arena.getType().equals("free")) {
			boolean onlyone = true;
			List<String> activeteams = new ArrayList<String>(0);
			for (String sTeam : getPlayerTeamMap().keySet()) {
				if (activeteams.size() < 1) {
					// fresh map
					String team = getPlayerTeamMap().get(sTeam);
					activeteams.add(team);
				} else {
					// map contains stuff
					if (!activeteams.contains(getPlayerTeamMap().get(sTeam))) {
						// second team active => OUT!
						onlyone = false;
						break;
					}
				}
			}
			if (onlyone) {
				return false;
			}
		}
		for (PAPlayer p : players.values()) {
			if (!p.getTeam().equals("")) {
				if (p.getFightClass().equals("")) {
					// player not ready!
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * reset an arena
	 * 
	 * @param arena
	 *            the arena to reset
	 */
	public void reset(Arena arena) {
		HashSet<PAPlayer> pa = new HashSet<PAPlayer>();
		for (PAPlayer p : players.values()) {
			pa.add(p);
		}

		for (PAPlayer p : pa) {
			arena.removePlayer(p.getPlayer(),
					arena.cfg.getString("tp.exit", "exit"));
			p.setTeam(null);
			p.setClass(null);
			p.setLives((byte) 0);
			// p.setSignLocation(null);
			paPlayersBetAmount.clear();
		}
		players.clear();
	}

	/**
	 * send a message to every playery
	 * 
	 * @param msg
	 *            the message to send
	 */
	public void tellEveryone(String msg) {
		db.i("@all: " + msg);
		for (PAPlayer p : players.values()) {
			p.getPlayer().sendMessage(
					ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE + msg);
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
		for (PAPlayer p : players.values()) {
			if (p.getPlayer().equals(player))
				continue;
			p.getPlayer().sendMessage(
					ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE + msg);
		}
	}

	/**
	 * return all players
	 * 
	 * @return a hashset of all players
	 */
	public HashSet<PAPlayer> getPlayers() {
		HashSet<PAPlayer> result = new HashSet<PAPlayer>();
		for (PAPlayer p : players.values()) {
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
		return players.get(player.getName()).getFightClass();
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
		players.get(player.getName()).setClass(s);
	}

	/**
	 * hand over a player's deaths
	 * 
	 * @param s
	 *            the player name to read
	 * @return the player's death count
	 */
	public int getDeaths(String s) {
		return deaths.get(s);
	}

	/**
	 * hand over a player's kills
	 * 
	 * @param s
	 *            the player name to read
	 * @return the player's kill count
	 */
	public int getKills(String s) {
		return kills.get(s);
	}

	/**
	 * add a kill to a player
	 * 
	 * @param sKiller
	 *            the player to add one
	 */
	public void addKill(String sKiller) {
		if (kills.get(sKiller) != null) {
			kills.put(sKiller, kills.get(sKiller) + 1);
		} else {
			kills.put(sKiller, 1);
		}
	}

	/**
	 * add a death to a player
	 * 
	 * @param sKilled
	 *            the player to add one
	 */
	public void addDeath(String sKilled) {
		if (deaths.get(sKilled) != null) {
			deaths.put(sKilled, deaths.get(sKilled) + 1);
		} else {
			deaths.put(sKilled, 1);
		}
	}

	/**
	 * hand over a player's lives
	 * 
	 * @param player
	 *            the player to check
	 * @return the lives
	 */
	public byte getLives(Player player) {
		return players.get(player.getName()).getLives();
	}

	/**
	 * hand over a player's lives
	 * 
	 * @param player
	 *            the player to update
	 * @param l
	 *            the lives
	 */
	public void setLives(Player player, byte l) {
		players.get(player.getName()).setLives(l);
	}

	/**
	 * hand over a player's respawn
	 * 
	 * @param player
	 *            the player to update
	 * @param s
	 *            true:set | false:unset
	 */
	public void setRespawn(Player player, boolean s) {
		players.get(player.getName()).setRespawn(s);
	}

	/**
	 * hand over a player's respawn
	 * 
	 * @param player
	 *            the player to check
	 * @return the player's respawn
	 */
	public String getRespawn(Player player) {
		return players.get(player.getName()).getRespawn();
	}

	/**
	 * hand over a player's team name
	 * 
	 * @param player
	 *            the player to check
	 * @return the player's team name
	 */
	public String getTeam(Player player) {
		return (players.get(player.getName()) == null) ? "" : players.get(
				player.getName()).getTeam();
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
		players.get(player.getName()).setTeam(s);
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
		players.get(player.getName()).setTelePass(b);
	}

	/**
	 * hand over a player's tele pass
	 * 
	 * @param player
	 *            the player to check
	 * @return true if may pass, false otherwise
	 */
	public boolean getTelePass(Player player) {
		return players.get(player.getName()).getTelePass();
	}

	/**
	 * add a player to the player map
	 * 
	 * @param player
	 *            the player to add
	 */
	public void addPlayer(Player player) {
		players.put(player.getName(), new PAPlayer(player));
	}

	/**
	 * remove a player from the player map
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void remove(Player player) {
		players.remove(player.getName());
	}
}
