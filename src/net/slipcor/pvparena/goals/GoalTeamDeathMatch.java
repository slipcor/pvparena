package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>
 * Arena Goal class "TeamDeathMatch"
 * </pre>
 * 
 * The second Arena Goal. Arena Teams have lives. When every life is lost, the
 * team is teleported to the spectator spawn to watch the rest of the fight.
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class GoalTeamDeathMatch extends ArenaGoal {
	public GoalTeamDeathMatch() {
		super("TeamDeathMatch");
		debug = new Debug(104);
	}

	private final Map<String, Integer> lives = new HashMap<String, Integer>(); // flags

	@Override
	public String version() {
		return "v0.10.3.0";
	}

	private final static int PRIORITY = 5;

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	@Override
	public PACheck checkEnd(final PACheck res) {
		if (res.getPriority() > PRIORITY) {
			return res;
		}

		final int count = TeamManager.countActiveTeams(arena);

		if (count == 1) {
			res.setPriority(this, PRIORITY); // yep. only one team left. go!
		} else if (count == 0) {
			res.setError(this, MSG.ERROR_NOTEAMFOUND.toString());
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(final Set<String> list) {
		for (ArenaTeam team : arena.getTeams()) {
			final String sTeam = team.getName();
			if (!list.contains(team + "spawn")) {
				boolean found = false;
				for (String s : list) {
					if (s.startsWith(sTeam) && s.endsWith("spawn")) {
						found = true;
						break;
					}
				}
				if (!found) {
					return team.getName() + "spawn not set";
				}
			}
		}
		return null;
	}

	@Override
	public PACheck checkJoin(final CommandSender sender, final PACheck res, final String[] args) {
		if (res.getPriority() >= PRIORITY) {
			return res;
		}

		final int maxPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);
		final int maxTeamPlayers = arena.getArenaConfig().getInt(
				CFG.READY_MAXTEAMPLAYERS);

		if (maxPlayers > 0 && arena.getFighters().size() >= maxPlayers) {
			res.setError(this, Language.parse(MSG.ERROR_JOIN_ARENA_FULL));
			return res;
		}

		if (args == null || args.length < 1) {
			return res;
		}

		if (!arena.isFreeForAll()) {
			final ArenaTeam team = arena.getTeam(args[0]);

			if (team != null && maxTeamPlayers > 0
						&& team.getTeamMembers().size() >= maxTeamPlayers) {
				res.setError(this, Language.parse(MSG.ERROR_JOIN_TEAM_FULL));
				return res;
			}
		}

		res.setPriority(this, PRIORITY);
		return res;
	}

	@Override
	public PACheck checkPlayerDeath(final PACheck res, final Player player) {
		if (res.getPriority() <= PRIORITY && player.getKiller() != null
				&& arena.hasPlayer(player.getKiller())) {
			res.setPriority(this, PRIORITY);
		}
		return res;
	}

	@Override
	public void commitEnd(final boolean force) {
		debug.i("[TEAMS]");

		ArenaTeam aTeam = null;

		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.FIGHT)) {
					aTeam = team;
					break;
				}
			}
		}

		if (aTeam != null && !force) {

			ArenaModuleManager.announce(
					arena,
					Language.parse(MSG.TEAM_HAS_WON, aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW), "WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON, aTeam.getColor()
					+ "Team " + aTeam.getName() + ChatColor.YELLOW));
		}

		if (ArenaModuleManager.commitEnd(arena, aTeam)) {
			return;
		}
		new EndRunnable(arena, arena.getArenaConfig().getInt(
				CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitPlayerDeath(final Player respawnPlayer, final boolean doesRespawn,
			final String error, final PlayerDeathEvent event) {
		if (respawnPlayer.getKiller() == null) {
			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(
							CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(respawnPlayer);
				event.getDrops().clear();
			}

			PACheck.handleRespawn(arena,
					ArenaPlayer.parsePlayer(respawnPlayer.getName()),
					event.getDrops());

			return;
		}

		final ArenaTeam respawnTeam = ArenaPlayer
				.parsePlayer(respawnPlayer.getName()).getArenaTeam();
		final ArenaTeam killerTeam = ArenaPlayer.parsePlayer(
				respawnPlayer.getKiller().getName()).getArenaTeam();
		reduceLives(arena, killerTeam);

		if (lives.get(killerTeam.getName()) != null) {
			if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
				arena.broadcast(Language.parse(
						MSG.FIGHT_KILLED_BY_REMAINING_TEAM_FRAGS,
						respawnTeam.colorizePlayer(respawnPlayer)
								+ ChatColor.YELLOW, arena.parseDeathCause(
								respawnPlayer, event.getEntity()
										.getLastDamageCause().getCause(), event
										.getEntity().getKiller()), String
								.valueOf(lives.get(killerTeam.getName())),
						killerTeam.getColoredName()));
			}

			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(
							CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(respawnPlayer);
				event.getDrops().clear();
			}

			PACheck.handleRespawn(arena,
					ArenaPlayer.parsePlayer(respawnPlayer.getName()),
					event.getDrops());

		}
	}

	@Override
	public void configParse(final YamlConfiguration config) {
		if (config.get("flagColors") == null) {
			debug.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
	}

	@Override
	public void displayInfo(final CommandSender sender) {
		sender.sendMessage("teams: "
				+ StringParser.joinSet(arena.getTeamNamesColored(), "§r, "));
		sender.sendMessage("lives: "
				+ arena.getArenaConfig().getInt(CFG.GOAL_TDM_LIVES));
	}

	@Override
	public PACheck getLives(final PACheck res, final ArenaPlayer aPlayer) {
		if (!res.hasError() && res.getPriority() <= PRIORITY) {
			res.setError(
					this,
					String.valueOf(arena.getArenaConfig()
									.getInt(CFG.GOAL_TDM_LIVES) - (lives
									.containsKey(aPlayer.getArenaTeam().getName()) ? lives
									.get(aPlayer.getArenaTeam().getName()) : 0)));
		}
		return res;
	}

	@Override
	public String guessSpawn(final String place) {
		if (!place.contains("spawn")) {
			debug.i("place not found!");
			return null;
		}
		// no exact match: assume we have multiple spawnpoints
		final Map<Integer, String> locs = new HashMap<Integer, String>();
		int pos = 0;

		debug.i("searching for team spawns");

		final Map<String, Object> coords = (HashMap<String, Object>) arena
				.getArenaConfig().getYamlConfiguration()
				.getConfigurationSection("spawns").getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(pos++, name);
				debug.i("found match: " + name);
			}
		}

		if (locs.size() < 1) {
			return null;
		}
		final Random random = new Random();

		return locs.get(random.nextInt(locs.size()));
	}

	@Override
	public boolean hasSpawn(final String string) {
		for (String teamName : arena.getTeamNames()) {
			if (string.toLowerCase().startsWith(
					teamName.toLowerCase() + "spawn")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void initate(final Player player) {
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		if (lives.get(aPlayer.getArenaTeam().getName()) == null) {
			lives.put(aPlayer.getArenaTeam().getName(), arena.getArenaConfig()
					.getInt(CFG.GOAL_TDM_LIVES));
		}
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	private void reduceLives(final Arena arena, final ArenaTeam team) {
		final int iLives = this.lives.get(team.getName());

		if (iLives <= 1) {
			for (ArenaTeam otherTeam : arena.getTeams()) {
				if (otherTeam.equals(team)) {
					continue;
				}
				lives.remove(otherTeam.getName());
				for (ArenaPlayer ap : otherTeam.getTeamMembers()) {
					if (ap.getStatus().equals(Status.FIGHT)) {
						ap.setStatus(Status.LOST);
						arena.removePlayer(ap.get(), CFG.TP_LOSE.toString(),
								true, false);
					}
				}
			}
			PACheck.handleEnd(arena, false);
			return;
		}

		lives.put(team.getName(), iLives - 1);
	}

	@Override
	public void reset(final boolean force) {
		lives.clear();
	}

	@Override
	public void setDefaults(final YamlConfiguration config) {
		if (arena.isFreeForAll()) {
			return;
		}

		if (config.get("teams.free") != null) {
			config.set("teams", null);
		}
		if (config.get("teams") == null) {
			debug.i("no teams defined, adding custom red and blue!");
			config.addDefault("teams.red", ChatColor.RED.name());
			config.addDefault("teams.blue", ChatColor.BLUE.name());
		}
		if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)
				&& (config.get("flagColors") == null)) {
			debug.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
	}

	@Override
	public void parseStart() {
		for (ArenaTeam team : arena.getTeams()) {
			this.lives.put(team.getName(),
					arena.getArenaConfig().getInt(CFG.GOAL_TDM_LIVES));
		}
	}

	@Override
	public Map<String, Double> timedEnd(final Map<String, Double> scores) {
		double score;

		for (ArenaTeam team : arena.getTeams()) {
			score = arena.getArenaConfig().getInt(CFG.GOAL_TDM_LIVES)
					- (lives.containsKey(team.getName()) ? lives.get(team
							.getName()) : 0);
			if (scores.containsKey(team)) {
				scores.put(team.getName(), scores.get(team.getName()) + score);
			} else {
				scores.put(team.getName(), score);
			}
		}

		return scores;
	}

	@Override
	public void unload(final Player player) {
		if (allowsJoinInBattle()) {
			arena.hasNotPlayed(ArenaPlayer.parsePlayer(player.getName()));
		}
	}
}
