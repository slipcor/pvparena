package net.slipcor.pvparena.loadables;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.nodinchan.ncbukkit.loader.Loader;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.goals.GoalFlags;
import net.slipcor.pvparena.goals.GoalPlayerDeathMatch;
import net.slipcor.pvparena.goals.GoalPlayerLives;
import net.slipcor.pvparena.goals.GoalTeamDeathMatch;
import net.slipcor.pvparena.goals.GoalTeamLives;
import net.slipcor.pvparena.goals.GoalTime;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>Arena Goal Manager class</pre>
 * 
 * Loads and manages arena goals
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class ArenaGoalManager {
	private List<ArenaGoal> types;
	private final Loader<ArenaGoal> loader;
	protected Debug db = new Debug(31);

	/**
	 * create an arena type instance
	 * 
	 * @param plugin
	 *            the plugin instance
	 */
	public ArenaGoalManager(PVPArena plugin) {
		File path = new File(plugin.getDataFolder().toString() + "/arenas");
		if (!path.exists()) {
			path.mkdir();
		}
		loader = new Loader<ArenaGoal>(plugin, path, new Object[] {});
		types = loader.load();
		types.add(new GoalFlags(null));
		types.add(new GoalPlayerDeathMatch(null));
		types.add(new GoalPlayerLives(null));
		types.add(new GoalTeamDeathMatch(null));
		types.add(new GoalTeamLives(null));
		types.add(new GoalTime(null));

		for (ArenaGoal type : types) {
			db.i("module ArenaType loaded: " + type.getName() + " (version "
					+ type.version() + ")");
		}
	}

	public boolean allowsJoinInBattle(Arena arena) {
		for (ArenaGoal type : arena.getGoals()) {
			if (!type.allowsJoinInBattle()) {
				return false;
			}
		}
		return true;
	}

	public boolean checkAndCommit(Arena arena) {
		
		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkEnd(res);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commit = null;
			}
		}
		
		if (res.hasError()) {
			arena.msg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_ERROR, res.getError()));
			return false;
		}
		
		if (commit == null) {
			return false;
		}
		
		commit.commitEnd();
		return true;
	}

	public ArenaGoal checkCommand(Arena arena, String string) {
		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkCommand(res, string);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commit = null;
			}
		}
		
		if (res.hasError()) {
			arena.msg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_ERROR, res.getError()));
		}
		return commit;
	}

	public void checkInteract(Arena arena, Player player, Block clickedBlock) {

		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkInteract(res, player, clickedBlock);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commit = null;
			}
		}
		
		if (res.hasError()) {
			arena.msg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_ERROR, res.getError()));
			return;
		}
		
		if (commit == null) {
			return;
		}
		
		commit.commitInteract(player, clickedBlock);
	}

	public boolean checkSetFlag(Player player, Block block) {
		Arena arena = PAA_Region.activeSelections.get(player.getName());
		
		if (arena == null) {
			return false;
		}
		
		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkSetFlag(res, player, block);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commit = null;
			}
		}
		
		if (res.hasError()) {
			arena.msg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_ERROR, res.getError()));
			return false;
		}
		
		if (commit == null) {
			return false;
		}
		
		return commit.commitSetFlag(player, block);
	}

	public String checkForMissingSpawns(Arena arena, Set<String> list) {
		for (ArenaGoal type : arena.getGoals()) {
			String error = type.checkForMissingSpawns(list);
			if (error != null) {
				return error;
			}
		}
		return null;
	}

	public void configParse(Arena arena, YamlConfiguration config) {
		for (ArenaGoal type : arena.getGoals()) {
			type.configParse(config);
		}
	}

	public HashSet<String> getAllGoalNames() {
		HashSet<String> result = new HashSet<String>();

		for (ArenaGoal goal : types) {
			result.add(goal.getName());
		}

		return result;
	}
	public int getLives(Arena arena, ArenaPlayer ap) {
		PACheckResult res = new PACheckResult();
		int priority = 0;
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.getLives(res, ap);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
			}
		}
		
		if (res.hasError()) {
			return Integer.valueOf(res.getError());
		}
		return 0;
	}

	/**
	 * find an arena type by arena type name
	 * 
	 * @param tName
	 *            the type name to find
	 * @return the arena type if found, null otherwise
	 */
	public ArenaGoal getType(String tName) {
		for (ArenaGoal type : types) {
			if (type.getName().equalsIgnoreCase(tName)) {
				return type;
			}
		}
		return null;
	}


	public List<ArenaGoal> getTypes() {
		return types;
	}

	public String guessSpawn(Arena arena, String place) {
		for (ArenaGoal type : arena.getGoals()) {
			String result = type.guessSpawn(place);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public void initiate(Arena arena, Player player) {
		System.out.print("initiating " + player.getName());
		for (ArenaGoal type : arena.getGoals()) {
			type.initate(player);
		}
	}

	public void onPlayerDeath(Arena arena, Player player, PlayerDeathEvent event) {
		boolean doesRespawn = true;
		
		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkPlayerDeath(res, player);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commit = null;
			}
		}
		
		if (res.hasError()) {
			// lives
			if (res.getError().equals("0")) {
				doesRespawn = false;
			}
		}

		StatisticsManager.kill(arena, player.getLastDamageCause().getEntity(), player, doesRespawn);
		event.setDeathMessage(null);
		
		if (!arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
			event.getDrops().clear();
		}
		
		if (commit == null) {
			// no mod handles player deaths, default to infinite lives. Respawn player
			
			arena.unKillPlayer(player, event.getEntity().getLastDamageCause().getCause(), player.getKiller());
			
			return;
		}
		
		commit.commitPlayerDeath(player, doesRespawn, res.getError(), event);
	}
	
	public String ready(Arena arena) {
		String error = null;
		for (ArenaGoal type : arena.getGoals()) {
			error = type.ready();
			if (error != null) {
				return error;
			}
		}
		return null;
	}
	
	public void reload() {
		types = loader.reload();
		types.add(new GoalTeamLives(null));

		for (ArenaGoal type : types) {
			db.i("module ArenaType loaded: " + type.getName() + " (version "
					+ type.version() + ")");
		}
	}
	
	public void reset(Arena arena, boolean force) {
		for (ArenaGoal type : arena.getGoals()) {
			type.reset(force);
		}
	}

	public void setDefaults(Arena arena, YamlConfiguration config) {
		for (ArenaGoal type : arena.getGoals()) {
			type.setDefaults(config);
		}
	}
	
	public void setPlayerLives(Arena arena, int value) {
		for (ArenaGoal type : arena.getGoals()) {
			type.setPlayerLives(value);
		}
	}
	
	public void setPlayerLives(Arena arena, ArenaPlayer ap, int value) {
		for (ArenaGoal type : arena.getGoals()) {
			type.setPlayerLives(ap, value);
		}
	}

	public void teleportAllToSpawn(Arena arena) {
		for (ArenaGoal mod : arena.getGoals()) {
			mod.teleportAllToSpawn();
		}
	}
	
	public void timedEnd(Arena arena) {

		/**
		 * name/team => score points
		 * 
		 * handed over to each module
		 */
		HashMap<String, Double> scores = new HashMap<String, Double>();

		for (ArenaGoal type : arena.getGoals()) {
			scores = type.timedEnd(scores);
		}

		HashSet<String> winners = new HashSet<String>();

		if (!arena.isFreeForAll()) {
			// check all teams
			double maxScore = 0;

			for (String team : arena.getTeamNames()) {
				if (scores.containsKey(team)) {
					double teamScore = scores.get(team);

					if (teamScore > maxScore) {
						maxScore = teamScore;
						winners.clear();
						winners.add(team);
					} else if (teamScore == maxScore) {
						winners.add(team);
					}
				}
			}
		} else {
			winners.add("free");
		}

		if (winners.size() > 1) {
			HashSet<String> preciseWinners = new HashSet<String>();

			// several teams have max score!!^
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
				} else if (sum > maxSum) {
					preciseWinners.clear();
					preciseWinners.add(team.getName());
				}
			}

			winners = preciseWinners.size() > 0 ? preciseWinners : winners;
		}

		if (arena.isFreeForAll()) {
			HashSet<String> preciseWinners = new HashSet<String>();

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
						preciseWinners.add(team.getName());
					} else if (sum > maxSum) {
						preciseWinners.clear();
						preciseWinners.add(team.getName());
					}
				}
			}
			winners = preciseWinners;
		}

		PVPArena.instance.getAmm().timedEnd(arena, winners);

		if (arena.isFreeForAll()) {
			for (ArenaTeam team : arena.getTeams()) {
				for (ArenaPlayer p : team.getTeamMembers()) {
					if (winners.contains(p.getName())) {
						PVPArena.instance.getAmm().announce(arena, Language.parse(MSG.PLAYER_HAS_WON, p.getName()), "WINNER");
						arena.broadcast(Language.parse(MSG.PLAYER_HAS_WON, p.getName()));
					} else {
						if (!p.getStatus().equals(Status.FIGHT)) {
							continue;
						}
						p.addLosses();
						arena.tpPlayerToCoordName(p.get(), "spectator");
					}
				}
			}
		} else {
			for (ArenaTeam team : arena.getTeams()) {
				if (winners.contains(team.getName())) {
					PVPArena.instance.getAmm().announce(arena, Language.parse(MSG.PLAYER_HAS_WON, "Team " + team.getName()), "WINNER");
					arena.broadcast(Language.parse(MSG.PLAYER_HAS_WON, team.getColor()
							+ "Team " + team.getName()));
				} else {
					for (ArenaPlayer p : team.getTeamMembers()) {
						if (!p.getStatus().equals(Status.FIGHT)) {
							continue;
						}
						p.addLosses();
						arena.tpPlayerToCoordName(p.get(), "spectator");

					}
				}
			}
		}
		
		new EndRunnable(arena, arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
	}

	public void unload(Arena arena, Player player) {
		for (ArenaGoal type : arena.getGoals()) {
			type.unload(player);
		}
	}
}
