package net.slipcor.pvparena.neworder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nodinchan.ncbukkit.loader.Loadable;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * arena module class
 * 
 * -
 * 
 * defines a new arena mode for PVP Arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 * 
 */

public class ArenaGoal extends Loadable {
	protected final static Debug db = new Debug(45);

	/**
	 * create an arena type instance
	 * 
	 * @param sName
	 *            the arena type name
	 */
	public ArenaGoal(String sName) {
		super(sName);
	}

	/**
	 * cloning an arena goal
	 */
	public Loadable clone() {
		ArenaGoal at = (ArenaGoal) super.clone();
		return at;
	}

	/**
	 * add default teams
	 * 
	 * @param config
	 *            the config to add the teams to
	 */
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
	 */
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
	 */
	public boolean checkAndCommit(Arena arena) {
		db.i("[TEAMS]");

		ArenaTeam aTeam = null;

		if (Teams.countActiveTeams(arena) > 1) {
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
					Language.parse("teamhaswon", "Team " + aTeam.getName()));

			arena.broadcast(Language.parse("teamhaswon", aTeam.getColor()
					+ "Team " + aTeam.getName()));
		}

		if (PVPArena.instance.getAmm().commitEnd(arena, aTeam)) {
			return true;
		}
		EndRunnable er = new EndRunnable(arena, arena.getArenaConfig().getInt("goal.endtimer"),0);
		arena.REALEND_ID = Bukkit.getScheduler()
				.scheduleSyncRepeatingTask(PVPArena.instance,
						er, 20L, 20L);
		er.setId(arena.REALEND_ID);
		return true;
	}

	/**
	 * hook into a player death
	 * 
	 * @param player
	 *            the dying player
	 */
	public void checkEntityDeath(Arena arena, Player player) {
		return;
	}

	/**
	 * hook into an interacting player
	 * 
	 * @param player
	 *            the interacting player
	 * @param clickedBlock
	 *            the block being clicked
	 */
	public void checkInteract(Arena arena, Player player, Block clickedBlock) {
		return;
	}

	/**
	 * hook into a set flag check
	 * 
	 * @param block
	 *            the clicked block
	 * @param player
	 *            the player clicking
	 * @return true if a flag was set
	 */
	public static boolean checkSetFlag(Block block, Player player) {
		if (block == null || !PAA_Region.activeSelections.containsKey(player.getName())) {
			return false;
		}
		Arena arena = PAA_Region.activeSelections.get(player.getName());
		
		if (arena == null) {
			return false;
		}
		
		return PVPArena.instance.getAgm().checkSetFlag(arena, player, block);
	}

	/**
	 * hook into a set flag check
	 * @param arena 
	 * 
	 * @param player
	 *            the player clicking
	 * @param block
	 *            the block being clicked
	 * @return true if a flag was set
	 */
	protected boolean checkSetFlag(Arena arena, Player player, Block block) {
		return false;
	}

	/**
	 * check if all necessary spawns are set
	 * 
	 * @param list
	 *            the list of all set spawns
	 * @return null if ready, error message otherwise
	 */
	public String checkSpawns(Arena arena, Set<String> list) {
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
				if (Teams.getTeam(arena, temp[0]) != null) {
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

	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Language.parse("onlyplayers");
			return;
		}

		Player player = (Player) sender;

		if (args[0].startsWith("spawn") || args[0].equals("spawn")) {
			arena.msg(sender, Language.parse("errorspawnfree", args[0]));
			return;
		}

		if (args[0].contains("spawn")) {
			String[] split = args[0].split("spawn");
			String sName = split[0];
			if (Teams.getTeam(arena, sName) == null) {
				arena.msg(sender, Language.parse("arenateamunknown", sName));
				return;
			}

			Spawns.setCoords(arena, player, args[0]);
			arena.msg(player, Language.parse("setspawn", sName));
		}

		if (args[0].equals("lounge")) {
			arena.msg(sender, Language.parse("errorloungefree", args[0]));
			return;
		}

		if (args[0].contains("lounge")) {
			String[] split = args[0].split("lounge");
			String sName = split[0];
			if (Teams.getTeam(arena, sName) == null) {
				arena.msg(sender, Language.parse("arenateamunknown", sName));
				return;
			}

			Spawns.setCoords(arena, player, args[0]);
			arena.msg(player, Language.parse("loungeset", sName));
		}
	}

	/**
	 * hook into the config parsing
	 */
	public void configParse(Arena arena) {
		return;
	}

	public HashSet<String> getAddedSpawns() {
		HashSet<String> result = new HashSet<String>();

		result.add("%team%spawn");
		result.add("%team%lounge");

		return result;
	}

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
	 */
	public void initLanguage(YamlConfiguration config) {
	}
	
	public boolean isFreeForAll() {
		return false;
	}

	/**
	 * is the given command a failed lounge command?
	 * 
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command
	 * @return true if an error occured
	 */
	public boolean isLoungesCommand(Arena arena, Player player, String cmd) {

		if (!player.getWorld().getName().equals(arena.getWorld())) {
			arena.msg(player, Language.parse("notsameworld", arena.getWorld()));
			return true;
		}

		if (cmd.equalsIgnoreCase("lounge")) {
			arena.msg(player, Language.parse("errorloungefree"));
			return true;
		}

		if (cmd.endsWith("lounge")) {
			String sTeam = cmd.replace("lounge", "");
			if (Teams.getTeam(arena, sTeam) != null) {
				Spawns.setCoords(arena, player, cmd);
				arena.msg(player, Language.parse("setlounge", sTeam));
				return true;
			}
			arena.msg(player, Language.parse("invalidcmd", "506"));
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
	 */
	public boolean isSpawnsCommand(Arena arena, Player player, String cmd) {
		if (!player.getWorld().getName().equals(arena.getWorld())) {
			arena.msg(player, Language.parse("notsameworld", arena.getWorld()));
			return true;
		}

		if (cmd.startsWith("spawn") || cmd.equals("spawn")) {
			arena.msg(player, Language.parse("errorspawnfree", cmd));
			return true;
		}

		if (cmd.contains("spawn")) {
			String[] split = cmd.split("spawn");
			String sName = split[0];
			if (Teams.getTeam(arena, sName) == null) {
				return false;
			}

			Spawns.setCoords(arena, player, cmd);
			arena.msg(player, Language.parse("setspawn", sName));
			return true;
		}

		if (cmd.startsWith("powerup")) {
			Spawns.setCoords(arena, player, cmd);
			arena.msg(player, Language.parse("setspawn", cmd));
			return true;
		}
		return false;
	}

	/**
	 * check if the arena is ready
	 * 
	 * @param arena
	 *            the arena to check
	 * @return 1 if ready, negative result otherwise
	 */
	public int ready(Arena arena) {
		HashSet<String> activeTeams = new HashSet<String>();

		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers())
				if (!arena.getArenaConfig().getBoolean("ready.checkEachTeam") || ap.getStatus().equals(Status.READY)) {
					activeTeams.add(team.getName());
				}
		}

		if (activeTeams.size() < 2) {
			return -2;
		}
		
		return 1;
	}

	/**
	 * hook into a team losing lives
	 * 
	 * @param team
	 *            the team losing lives
	 * @return true if the arena is over
	 */
	public boolean reduceLivesCheckEndAndCommit(Arena arena, String team) {
		return false;
	}

	public boolean parseCommand(Arena arena, String s) {
		if (s.contains("spawn")) {
			String[] split = s.split("spawn");
			String sName = split[0];
			if (Teams.getTeam(arena, sName) == null) {
				return false;
			}
			return true;
		}
		if (s.contains("lounge")) {
			String[] split = s.split("lounge");
			String sName = split[0];
			if (Teams.getTeam(arena, sName) == null) {
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
	 */
	public void parseRespawn(Arena arena, Player respawnPlayer, ArenaTeam respawnTeam,
			int lives, DamageCause cause, Entity damager) {
		arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
				+ "spawn");
		return;
	}

	/**
	 * hook into an arena reset
	 * @param a the arena being reset
	 * @param force
	 *            is the resetting forced?
	 */
	public void reset(Arena arena, boolean force) {
		return;
	}

	/**
	 * initiate an arena
	 * @param a 
	 */
	public void teleportAllToSpawn(Arena arena) {
		
	}

	/**
	 * hook into the timed end
	 */
	public void timed(Arena arena) {
		
		int i;

		int max = -1;

		HashMap<String, Integer> result = new HashMap<String, Integer>();
		db.i("timed end!");

		HashSet<String> modresult = new HashSet<String>();
		/**
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.getStatus().equals(Status.FIGHT)) {
					continue;
				}

				modresult.add(ap.getName());
				int sum = 0;
				if (result.get(team.getName()) != null) {
					sum += result.get(team.getName());
				}

				sum += arena.lives.get(ap.getName());

				result.put(team.getName(), sum);
			}
		}

		HashSet<String> realresult = new HashSet<String>();
		db.i("timed end!");

		for (String sTeam : result.keySet()) {
			i = result.get(sTeam);

			if (i > max) {
				realresult = new HashSet<String>();
				realresult.add(sTeam);
				max = i;
			} else if (i == max) {
				realresult.add(sTeam);
			}

		}

		for (ArenaTeam team : arena.getTeams()) {
			if (realresult.contains(team.getName())) {
				PVPArena.instance.getAmm().announceWinner(arena,
						Language.parse("teamhaswon", "Team " + team.getName()));
				arena.tellEveryone(Language.parse("teamhaswon", team.getColor()
						+ "Team " + team.getName()));
			}
			if (!realresult.contains(team.getName())) {
				for (ArenaPlayer p : team.getTeamMembers()) {
					if (!p.getStatus().equals(Status.FIGHT)) {
						continue;
					}
					p.losses++;
					arena.tpPlayerToCoordName(p.get(), "spectator");
					modresult.remove(p.getName());

				}
			}
		}
		
		*/
		
		//TODO: calculate a sort of SCORE inside each goal

		PVPArena.instance.getAmm().timedEnd(arena, modresult);
		EndRunnable er = new EndRunnable(arena, arena.getArenaConfig().getInt("goal.endtimer"),0);
		arena.REALEND_ID = Bukkit.getScheduler()
				.scheduleSyncRepeatingTask(PVPArena.instance,
						er, 20L, 20L);
		er.setId(arena.REALEND_ID);
	}

	/**
	 * does the arena type use flags?
	 */
	public boolean usesFlags() {
		return false;
	}

	public String version() {
		return "outdated";
	}

	/**
	 * hook into arena player unloading
	 * 
	 * @param player
	 *            the player to unload
	 */
	public void unload(Arena arena, Player player) {
	}

	public void displayInfo(Arena arena, CommandSender sender) {
		// TODO Auto-generated method stub
		
	}
}
