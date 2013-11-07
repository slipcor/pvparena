package net.slipcor.pvparena.loadables;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.goals.*;
import net.slipcor.pvparena.ncloader.NCBLoader;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * <pre>
 * Arena Goal Manager class
 * </pre>
 *
 * Loads and manages arena goals
 *
 * @author slipcor
 *
 * @version v0.10.2
 */

public class ArenaGoalManager {
	private List<ArenaGoal> types;
	private final NCBLoader<ArenaGoal> loader;
	protected static final Debug DEBUG = new Debug(31);

	/**
	 * create an arena type instance
	 *
	 * @param plugin
	 *            the plugin instance
	 */
	public ArenaGoalManager(final PVPArena plugin) {
		final File path = new File(plugin.getDataFolder().toString() + "/goals");
		if (!path.exists()) {
			path.mkdir();
		}
		loader = new NCBLoader<ArenaGoal>(plugin, path, new Object[] {});
		types = loader.load(ArenaGoal.class);
		fill();
	}

	private void fill() {
		types.add(new GoalBlockDestroy());
		types.add(new GoalDomination());
		types.add(new GoalFlags());
		types.add(new GoalFood());
		types.add(new GoalInfect());
		types.add(new GoalLiberation());
		types.add(new GoalPhysicalFlags());
		types.add(new GoalPlayerDeathMatch());
		types.add(new GoalPlayerKillReward());
		types.add(new GoalPlayerLives());
		types.add(new GoalSabotage());
		types.add(new GoalTank());
		types.add(new GoalTeamDeathMatch());
		types.add(new GoalTeamLives());
		types.add(new GoalTime());

		for (ArenaGoal type : types) {
			type.onThisLoad();
			DEBUG.i("module ArenaType loaded: " + type.getName() + " (version "
					+ type.version() + ")");
		}
	}

	public boolean allowsJoinInBattle(final Arena arena) {
		for (ArenaGoal type : arena.getGoals()) {
			if (!type.allowsJoinInBattle()) {
				return false;
			}
		}
		return true;
	}

	public String checkForMissingSpawns(final Arena arena,
			final Set<String> list) {
		for (ArenaGoal type : arena.getGoals()) {
			final String error = type.checkForMissingSpawns(list);
			if (error != null) {
				return error;
			}
		}
		return null;
	}

	public void configParse(final Arena arena, final YamlConfiguration config) {
		for (ArenaGoal type : arena.getGoals()) {
			type.configParse(config);
		}
	}

	public Set<String> getAllGoalNames() {
		final Set<String> result = new HashSet<String>();

		for (ArenaGoal goal : types) {
			result.add(goal.getName());
		}

		return result;
	}

	public List<ArenaGoal> getAllGoals() {
		return types;
	}

	/**
	 * find an arena type by arena type name
	 *
	 * @param tName
	 *            the type name to find
	 * @return the arena type if found, null otherwise
	 */
	public ArenaGoal getGoalByName(final String tName) {
		for (ArenaGoal type : types) {
			if (type.getName().equalsIgnoreCase(tName)) {
				return type;
			}
		}
		return null;
	}

	public void initiate(final Arena arena, final Player player) {
		arena.getDebugger().i("initiating " + player.getName(), player);
		for (ArenaGoal type : arena.getGoals()) {
			type.initate(player);
		}
	}

	public String ready(final Arena arena) {
		arena.getDebugger().i("AGM ready!?!");
		String error;
		for (ArenaGoal type : arena.getGoals()) {
			error = type.ready();
			if (error != null) {

				arena.getDebugger().i("type error:" + type.getName());
				return error;
			}
		}
		return null;
	}

	public void refillInventory(Arena arena, Player player) {
		if (player == null) {
			return;
		}
		for (ArenaGoal type : arena.getGoals()) {
			type.refillInventory(player);
		}
	}

	public void reload() {
		types = loader.reload(ArenaGoal.class);
		fill();
	}

	public void reset(final Arena arena, final boolean force) {
		for (ArenaGoal type : arena.getGoals()) {
			type.reset(force);
		}
	}

	public void setDefaults(final Arena arena, final YamlConfiguration config) {
		for (ArenaGoal type : arena.getGoals()) {
			type.setDefaults(config);
		}
	}

	public void setPlayerLives(final Arena arena, final int value) {
		for (ArenaGoal type : arena.getGoals()) {
			type.setPlayerLives(value);
		}
	}

	public void setPlayerLives(final Arena arena, final ArenaPlayer player,
			final int value) {
		for (ArenaGoal type : arena.getGoals()) {
			type.setPlayerLives(player, value);
		}
	}

	public void timedEnd(final Arena arena) {

		/**
		 * name/team => score points
		 *
		 * handed over to each module
		 */

		arena.getDebugger().i("timed end!");

		Map<String, Double> scores = new HashMap<String, Double>();

		for (ArenaGoal type : arena.getGoals()) {
			arena.getDebugger().i("scores: " + type.getName());
			scores = type.timedEnd(scores);
		}

		final Set<String> winners = new HashSet<String>();

		if (arena.isFreeForAll()) {
			winners.add("free");
			arena.getDebugger().i("adding FREE");
		} else if (arena.getArenaConfig().getString(CFG.GOAL_TIME_WINNER)
				.equals("none")) {
			// check all teams
			double maxScore = 0;

			boolean notEveryone = false;

			for (String team : arena.getTeamNames()) {
				if (scores.containsKey(team)) {
					final double teamScore = scores.get(team);

					if (teamScore > maxScore) {

						if (!winners.isEmpty()) {
							notEveryone = true;
						}

						maxScore = teamScore;
						winners.clear();
						winners.add(team);
						arena.getDebugger().i("clear and add team " + team);
					} else if (teamScore == maxScore) {
						winners.add(team);
						arena.getDebugger().i("add team " + team);
					}
				}
			}

			if (!notEveryone) {
				winners.clear(); // noone wins.
			}
		} else {
			winners.add(arena.getArenaConfig().getString(CFG.GOAL_TIME_WINNER));
			arena.getDebugger().i("added winner!");
		}

		if (winners.size() > 1) {
			arena.getDebugger().i("more than 1");
			final Set<String> preciseWinners = new HashSet<String>();

			// several teams have max score!!
			double maxSum = 0;
			double sum = 0;
			for (ArenaTeam team : arena.getTeams()) {
				if (!winners.contains(team.getName())) {
					continue;
				}

				sum = 0;

				for (ArenaPlayer ap : team.getTeamMembers()) {
					if (scores.containsKey(ap.getName())) {
						sum += scores.get(ap.getName());
					}
				}

				if (sum == maxSum) {
					preciseWinners.add(team.getName());
					arena.getDebugger().i("adddding " + team.getName());
				} else if (sum > maxSum) {
					maxSum = sum;
					preciseWinners.clear();
					preciseWinners.add(team.getName());
					arena.getDebugger().i(
							"clearing and adddding + " + team.getName());
				}
			}

			if (!preciseWinners.isEmpty()) {
				winners.clear();
				winners.addAll(preciseWinners);
			}
		}

		if (arena.isFreeForAll()) {
			arena.getDebugger().i("FFAAA");
			final Set<String> preciseWinners = new HashSet<String>();

			for (ArenaTeam team : arena.getTeams()) {
				if (!winners.contains(team.getName())) {
					continue;
				}

				double maxSum = 0;

				for (ArenaPlayer ap : team.getTeamMembers()) {
					double sum = 0;
					if (scores.containsKey(ap.getName())) {
						sum = scores.get(ap.getName());
					}
					if (sum == maxSum) {
						preciseWinners.add(ap.getName());
						arena.getDebugger().i("ffa adding " + ap.getName());
					} else if (sum > maxSum) {
						maxSum = sum;
						preciseWinners.clear();
						preciseWinners.add(ap.getName());
						arena.getDebugger().i(
								"ffa clr & adding " + ap.getName());
					}
				}
			}
			winners.clear();

			if (preciseWinners.size() != arena.getPlayedPlayers().size()) {
				winners.addAll(preciseWinners);
			}
		}

		ArenaModuleManager.timedEnd(arena, winners);

		if (arena.isFreeForAll()) {
			for (ArenaTeam team : arena.getTeams()) {
				final Set<ArenaPlayer> apSet = new HashSet<ArenaPlayer>();
				for (ArenaPlayer p : team.getTeamMembers()) {
					apSet.add(p);
				}

				for (ArenaPlayer p : apSet) {
					if (winners.isEmpty()) {
						arena.removePlayer(p.get(), arena.getArenaConfig()
								.getString(CFG.TP_LOSE), true, false);
					} else {
						if (winners.contains(p.getName())) {

							ArenaModuleManager.announce(
									arena,
									Language.parse(arena, MSG.PLAYER_HAS_WON,
											p.getName()), "WINNER");
							arena.broadcast(Language.parse(arena, MSG.PLAYER_HAS_WON,
									p.getName()));
						} else {
							if (!p.getStatus().equals(Status.FIGHT)) {
								continue;
							}
							p.addLosses();
							p.setStatus(Status.LOST);
						}
					}
				}
			}
			if (winners.isEmpty()) {
				ArenaModuleManager.announce(arena,
						Language.parse(arena, MSG.FIGHT_DRAW), "WINNER");
				arena.broadcast(Language.parse(arena, MSG.FIGHT_DRAW));
			}
		} else if (!winners.isEmpty()) {

			boolean hasBroadcasted = false;
			for (ArenaTeam team : arena.getTeams()) {
				if (winners.contains(team.getName())) {
					if (!hasBroadcasted) {
						ArenaModuleManager.announce(
								arena,
								Language.parse(arena, MSG.TEAM_HAS_WON,
										team.getName()), "WINNER");
						arena.broadcast(Language.parse(arena, MSG.TEAM_HAS_WON,
								team.getColor() + team.getName()));
						hasBroadcasted = true;
					}
				} else {

					final Set<ArenaPlayer> apSet = new HashSet<ArenaPlayer>();
					for (ArenaPlayer p : team.getTeamMembers()) {
						apSet.add(p);
					}
					for (ArenaPlayer p : apSet) {
						if (!p.getStatus().equals(Status.FIGHT)) {
							continue;
						}
						p.addLosses();
						if (!hasBroadcasted) {
							for (String winTeam : winners) {
								ArenaModuleManager.announce(arena, Language
										.parse(arena, MSG.TEAM_HAS_WON, winTeam), "WINNER");

								ArenaTeam winningTeam = arena.getTeam(winTeam);

								if (winningTeam != null) {
									arena.broadcast(Language.parse(arena, MSG.TEAM_HAS_WON,
										winningTeam.getColor() + winTeam));
								} else {
									PVPArena.instance.getLogger().severe("Winning team is NULL: " + winTeam);
								}
							}
							hasBroadcasted = !hasBroadcasted;
						}

						p.setStatus(Status.LOST);
					}
				}
			}
		} else {
			ArenaModuleManager.announce(arena, Language.parse(arena, MSG.FIGHT_DRAW),
					"WINNER");
			arena.broadcast(Language.parse(arena, MSG.FIGHT_DRAW));
			arena.reset(true);
			return;
		}
		/*
		 * for (ArenaPlayer player : arena.getEveryone()) { if
		 * (player.getStatus() == Status.FIGHT) { player.setStatus(Status.LOST);
		 * } }
		 */

		arena.reset(false); // TODO: try to establish round compatibility with
							// new EndRunnable();
	}

	public void unload(final Arena arena, final Player player) {
		for (ArenaGoal type : arena.getGoals()) {
			type.unload(player);
		}
	}

	public void disconnect(final Arena arena, final ArenaPlayer player) {
		if (arena == null) {
			return;
		}
		for (ArenaGoal type : arena.getGoals()) {
			type.disconnect(player);
		}
	}

	public static void lateJoin(Arena arena, Player player) {
		for (ArenaGoal goal : arena.getGoals()) {
			goal.lateJoin(player);
		}
	}
}
