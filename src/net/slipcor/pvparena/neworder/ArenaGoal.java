package net.slipcor.pvparena.neworder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nodinchan.ncbukkit.loader.Loadable;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>Arena Goal class</pre>
 * 
 * The framework for adding goals to an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class ArenaGoal extends Loadable {
	protected static Debug db = new Debug(30);

	/**
	 * create an arena type instance
	 * 
	 * @param sName
	 *            the arena type name
	 */
	public ArenaGoal(String sName) {
		super(sName);
	}

	/*
	
	
	/**
	 * add default teams
	 * 
	 * @param config
	 *            the config to add the teams to
	 * /
	public void addDefaultTeams(Arena arena, YamlConfiguration config) {
		if (arena.getArenaConfig().get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			arena.getArenaConfig().getYamlConfiguration().addDefault("teams.red",
					ChatColor.RED.name());
			arena.getArenaConfig().getYamlConfiguration().addDefault("teams.blue",
					ChatColor.BLUE.name());
		}
		if (arena.getArenaConfig().getBoolean("game.woolFlagHead")
				&& (arena.getArenaConfig().get("flagColors") == null)) {
			db.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
	}

	/**
	 * hook into adding the settings
	 * 
	 * @param types
	 *            the settings map
	 * /
	public void addSettings(Arena arena, HashMap<String, String> types) {
	}

	/**
	 * does the arena type allow joining in battle?
	 */
	public boolean allowsJoinInBattle(Arena arena) {
		return false;
	}

	/**
	 * check if the arena match is over
	 * 
	 * @return true if the match is over
	 * /
	public boolean checkAndCommit(Arena arena) {
		db.i("[TEAMS]");

		ArenaTeam aTeam = null;

		if (TeamManager.countActiveTeams(arena) > 1) {
			return false;
		}

		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.FIGHT)) {
					aTeam = team;
					break;
				}
			}
		}

		if (aTeam != null) {
			PVPArena.instance.getAmm().announceWinner(arena,
					Language.parse(MSG.TEAM_HAS_WON, "Team " + aTeam.getName()));

			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON, aTeam.getColor()
					+ "Team " + aTeam.getName()));
		}

		if (PVPArena.instance.getAmm().commitEnd(arena, aTeam)) {
			return true;
		}
		new EndRunnable(arena, arena.getArenaConfig().getInt("goal.endtimer"));
		return true;
	}
*/


	public PACheckResult checkEnd(Arena arena, PACheckResult res) {
		return res;
	}

	/**
	 * check if all necessary spawns are set
	 * 
	 * @param list
	 *            the list of all set spawns
	 * @return null if ready, error message otherwise
	 */
	public String checkForMissingSpawns(Arena arena, Set<String> list) {
		// not random! we need teams * 2 (lounge + spawn) + exit + spectator
		db.i("parsing not random");
		Iterator<String> iter = list.iterator();
		int spawns = 0;
		int lounges = 0;
		HashSet<String> setTeams = new HashSet<String>();
		while (iter.hasNext()) {
			String s = iter.next();
			db.i("parsing '" + s + "'");
			db.i("spawns: " + spawns + "; lounges: " + lounges);
			if (s.endsWith("spawn") && (!s.equals("spawn"))) {
				spawns++;
			} else if (s.endsWith("lounge") && (!s.equals("lounge"))) {
				lounges++;
			} else if (s.contains("spawn") && (!s.equals("spawn"))) {
				String[] temp = s.split("spawn");
				if (arena.getTeam(temp[0]) != null) {
					if (setTeams.contains(temp[0])) {
						db.i("team already set");
						continue;
					}
					db.i("adding team");
					setTeams.add(temp[0]);
					spawns++;
				}
			}
		}
		if (spawns == arena.getTeams().size()
				&& lounges == arena.getTeams().size()) {
			return null;
		}

		return spawns + "/" + arena.getTeams().size() + "x spawn ; " + lounges
				+ "/" + arena.getTeams().size() + "x lounge";
	}

	/**
	 * hook into an interacting player
	 * @param res 
	 * 
	 * @param player
	 *            the interacting player
	 * @param clickedBlock
	 *            the block being clicked
	 * @return 
	 */
	public PACheckResult checkInteract(Arena arena, PACheckResult res, Player player, Block clickedBlock) {
		return res;
	}

	/**
	 * notify the goal of a player death, return higher priority if goal should handle the death as WIN/LOSE
	 * @param arena the arena
	 * @param player the dying player
	 * @return a PACheckResult instance to hand forth for parsing
	 */
	public PACheckResult checkPlayerDeath(Arena arena, PACheckResult res, Player player) {
		return res;
	}


	public PACheckResult checkTeleportAll(Arena arena, PACheckResult res,
			boolean b) {
		return res;
	}
	
	
	/**
	 * hook into a set flag check
	 * 
	 * @param block
	 *            the clicked block
	 * @param player
	 *            the player clicking
	 * @return true if a flag was set
	 * /
	public static boolean checkSetFlag(Block block, Player player) {
		if (block == null || !PAA_Region.activeSelections.containsKey(player.getName())) {
			return false;
		}
		Arena arena = PAA_Region.activeSelections.get(player.getName());
		
		if (arena == null) {
			return false;
		}
		
		return PVPArena.instance.getAgm().checkSetFlag(arena, player, block);
	}*/

	/**
	 * 
	 * @param arena
	 * @param res
	 * @param player
	 * @param block
	 * @return
	 */
	protected PACheckResult checkSetFlag(Arena arena, PACheckResult res, Player player, Block block) {
		return res;
	}
/*
	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Language.parse(MSG.ERROR_ONLY_PLAYERS);
			return;
		}

		Player player = (Player) sender;

		if (args[0].startsWith("spawn") || args[0].equals("spawn")) {
			arena.msg(sender, Language.parse(MSG.ERROR_SPAWNFREE, args[0]));
			return;
		}

		if (args[0].contains("spawn")) {
			String[] split = args[0].split("spawn");
			String sName = split[0];
			if (arena.getTeam(sName) == null) {
				arena.msg(sender, Language.parse(MSG.ERROR_TEAMNOTFOUND, sName));
				return;
			}

			SpawnManager.setCoords(arena, player, args[0]);
			arena.msg(player, Language.parse(MSG.SPAWN_SET, sName));
		}

		if (args[0].equals("lounge")) {
			arena.msg(sender, Language.parse(MSG.ERROR_LOUNGEFREE, args[0]));
			return;
		}

		if (args[0].contains("lounge")) {
			String[] split = args[0].split("lounge");
			String sName = split[0];
			if (arena.getTeam(sName) == null) {
				arena.msg(sender, Language.parse(MSG.ERROR_TEAMNOTFOUND, sName));
				return;
			}

			SpawnManager.setCoords(arena, player, args[0]);
			arena.msg(player, Language.parse(MSG.SPAWN_TEAMLOUNGE, sName));
		}
	}
*/

	public void commitEnd(Arena arena) {
	}

	public void commitInteract(Arena arena, Player player, Block clickedBlock) {
	}
	
	public void commitPlayerDeath(Arena arena, Player player,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
	}

	public boolean commitSetFlag(Arena arena, Player player, Block block) {
		return false;
	}

	/**
	 * hook into the config parsing
	 * @param config 
	 */
	public void configParse(Arena arena, YamlConfiguration config) {
		return;
	}

	public void displayInfo(Arena arena, CommandSender sender) {
		
	}
/*
	public HashSet<String> getAddedSpawns() {
		HashSet<String> result = new HashSet<String>();

		result.add("%team%spawn");
		result.add("%team%lounge");

		return result;
	}*/

	/**
	 * guess the spawn name from a given string
	 * 
	 * @param place
	 *            the string to check
	 * @return the proper spawn name
	 */
	public String guessSpawn(Arena arena, String place) {
		if (!place.contains("spawn")) {
			db.i("place not found!");
			return null;
		}
		// no exact match: assume we have multiple spawnpoints
		HashMap<Integer, String> locs = new HashMap<Integer, String>();
		int i = 0;

		db.i("searching for team spawns");

		HashMap<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(i++, name);
				db.i("found match: " + name);
			}
		}

		if (locs.size() < 1) {
			return null;
		}
		Random r = new Random();

		place = locs.get(r.nextInt(locs.size()));

		return place;
	}

	/**
	 * hook into the language initialisation
	 * 
	 * @param config
	 *            the language config
	 * /
	public void initLanguage(YamlConfiguration config) {
	}

	/**
	 * is the given command a failed lounge command?
	 * 
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command
	 * @return true if an error occured
	 * /
	public boolean isLoungesCommand(Arena arena, Player player, String cmd) {

		if (cmd.equalsIgnoreCase("lounge")) {
			arena.msg(player, Language.parse(MSG.ERROR_LOUNGEFREE));
			return true;
		}

		if (cmd.endsWith("lounge")) {
			String sTeam = cmd.replace("lounge", "");
			if (arena.getTeam(sTeam) != null) {
				SpawnManager.setCoords(arena, player, cmd);
				arena.msg(player, Language.parse(MSG.SPAWN_TEAMLOUNGE, sTeam));
				return true;
			}
			arena.msg(player, Language.parse(MSG.ERROR_COMMAND_INVALID, "506"));
			return true;
		}
		return false;
	}

	/**
	 * is the given command a failed spawn command?
	 * 
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command
	 * @return true if an error occured
	 * /
	public boolean isSpawnsCommand(Arena arena, Player player, String cmd) {

		if (cmd.startsWith("spawn") || cmd.equals("spawn")) {
			arena.msg(player, Language.parse(MSG.ERROR_SPAWNFREE, cmd));
			return true;
		}

		if (cmd.contains("spawn")) {
			String[] split = cmd.split("spawn");
			String sName = split[0];
			if (arena.getTeam(sName) == null) {
				return false;
			}

			SpawnManager.setCoords(arena, player, cmd);
			arena.msg(player, Language.parse(MSG.SPAWN_SET, sName));
			return true;
		}

		if (cmd.startsWith("powerup")) {
			SpawnManager.setCoords(arena, player, cmd);
			arena.msg(player, Language.parse(MSG.SPAWN_SET, cmd));
			return true;
		}
		return false;
	}
	
	*/

	/**
	 * check if the arena is ready
	 * 
	 * @param arena
	 *            the arena to check
	 * @return null if ready, error message otherwise
	 */
	public String ready(Arena arena) {
		return null;
	}

	/**
	 * hook into a team losing lives
	 * 
	 * @param team
	 *            the team losing lives
	 * @return true if the arena is over
	 * /
	public boolean reduceLivesCheckEndAndCommit(Arena arena, String team) {
		return false;
	}

	public boolean parseCommand(Arena arena, String s) {
		if (s.contains("spawn")) {
			String[] split = s.split("spawn");
			String sName = split[0];
			if (arena.getTeam(sName) == null) {
				return false;
			}
			return true;
		}
		if (s.contains("lounge")) {
			String[] split = s.split("lounge");
			String sName = split[0];
			if (arena.getTeam(sName) == null) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * parse a player respawn
	 * @param a 
	 * 
	 * @param respawnPlayer
	 *            the respawning player
	 * @param respawnTeam
	 *            the team the player belongs to
	 * @param lives
	 *            the lives left
	 * @param cause
	 *            the last damage cause
	 * @param damager
	 *            the player dealing the damage
	 * /
	public void parseRespawn(Arena arena, Player respawnPlayer, ArenaTeam respawnTeam,
			int lives, DamageCause cause, Entity damager) {
		arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
				+ "spawn");
		return;
	}
*/
	/**
	 * hook into an arena reset
	 * @param a the arena being reset
	 * @param force
	 *            is the resetting forced?
	 */
	public void reset(Arena arena, boolean force) {
		return;
	}

	public void setDefaults(Arena arena, YamlConfiguration config) {
		
	}

	/**
	 * initiate an arena
	 * @param a 
	 */
	public void teleportAllToSpawn(Arena arena) {
		
	}

	public HashMap<String, Double> timedEnd(Arena arena,
			HashMap<String, Double> scores) {
		return scores;
	}

	
	/**
	 * hook into arena player unloading
	 * 
	 * @param player
	 *            the player to unload
	 */
	public void unload(Arena arena, Player player) {
	}

	public String version() {
		return "outdated";
	}
}
