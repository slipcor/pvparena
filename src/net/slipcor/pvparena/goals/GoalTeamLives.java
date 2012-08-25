package net.slipcor.pvparena.goals;

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

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.neworder.ArenaGoal;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>Arena Goal class "PlayerLives"</pre>
 * 
 * The second Arena Goal. Arena Teams have lives. When every life is lost, the team
 * is teleported to the spectator spawn to watch the rest of the fight.
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class GoalTeamLives extends ArenaGoal {
	public GoalTeamLives() {
		super("teams");
		db = new Debug(101);
	}
	private final HashMap<String, Integer> lives = new HashMap<String, Integer>(); // flags

	@Override
	public String version() {
		return "v0.9.0.0";
	}
	
	@Override
	public void addDefaultTeams(Arena arena, YamlConfiguration config) {
		if (arena.getArenaConfig().get("teams.free") != null) {
			arena.getArenaConfig().set("teams",null);
		}
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

	@Override
	public void addSettings(Arena arena, HashMap<String, String> types) {
	}

	@Override
	public boolean allowsJoinInBattle(Arena arena) {
		return false;
	}

	@Override
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

	@Override
	public void checkEntityDeath(Arena arena, Player player) {
		return;
	}

	@Override
	public void checkInteract(Arena arena, Player player, Block clickedBlock) {
		return;
	}

	@Override
	public PACheckResult checkPlayerDeath(Arena arena, Player player) {
		return null;
	}

	@Override
	protected boolean checkSetFlag(Arena arena, Player player, Block block) {
		return false;
	}

	@Override
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

	@Override
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

	@Override
	public void commitPlayerDeath(Arena arena, Player player,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
	}

	@Override
	public void configParse(Arena arena) {
		return;
	}

	@Override
	public void displayInfo(Arena arena, CommandSender sender) {
		
	}

	@Override
	public HashSet<String> getAddedSpawns() {
		HashSet<String> result = new HashSet<String>();

		result.add("%team%spawn");
		result.add("%team%lounge");

		return result;
	}

	@Override
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

	@Override
	public void initLanguage(YamlConfiguration config) {
	}

	@Override
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

	@Override
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

	@Override
	public String ready(Arena arena) {
		return null;
	}

	@Override
	public boolean reduceLivesCheckEndAndCommit(Arena arena, String team) {
		return false;
	}

	@Override
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

	@Override
	public void parseRespawn(Arena arena, Player respawnPlayer, ArenaTeam respawnTeam,
			int lives, DamageCause cause, Entity damager) {

		arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY_REMAINING,
				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
				arena.parseDeathCause(respawnPlayer, cause, damager),
				String.valueOf(lives)));
		this.lives.put(respawnPlayer.getName(), lives);
		arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
				+ "spawn");
	}

	
	private int reduceLives(Arena arena, Player player, int lives) {
		lives = this.lives.get(player.getName());
		db.i("lives before death: " + lives);
		return lives;
	}

	@Override
	public void reset(Arena arena, boolean force) {
		return;
	}

	@Override
	public void teleportAllToSpawn(Arena arena) {
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				this.lives
						.put(ap.getName(), arena.getArenaConfig().getInt("game.lives", 3));
			}
		}
	}

	@Override
	public HashMap<String, Double> timedEnd(Arena arena,
			HashMap<String, Double> scores) {
		return scores;
	}

	@Override
	public void unload(Arena arena, Player player) {
	}
}
