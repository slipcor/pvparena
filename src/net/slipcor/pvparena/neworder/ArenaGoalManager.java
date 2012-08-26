package net.slipcor.pvparena.neworder;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.nodinchan.ncbukkit.loader.Loader;

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
import net.slipcor.pvparena.goals.GoalTeamLives;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>Arena Goal Manager class</pre>
 * 
 * Loads and manages arena goals
 * 
 * @author slipcor
 * 
 * @version v0.9.0
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
		types.add(new GoalTeamLives());

		for (ArenaGoal type : types) {
			db.i("module ArenaType loaded: " + type.getName() + " (version "
					+ type.version() + ")");
		}
	}
/*
	public void addDefaultTeams(Arena arena, YamlConfiguration config) {
		for (ArenaGoal type : arena.getGoals()) {
			type.addDefaultTeams(arena, config);
		}
	}

	public void addSettings(Arena arena, HashMap<String, String> types) {
		for (ArenaGoal type : arena.getGoals()) {
			type.addSettings(arena, types);
		}
	}*/

	public boolean allowsJoinInBattle(Arena arena) {
		for (ArenaGoal type : arena.getGoals()) {
			if (!type.allowsJoinInBattle(arena)) {
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
			res = mod.checkEnd(arena, res);
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
		
		commit.commitEnd(arena);
		return true;
	}

	public void checkInteract(Arena arena, Player player, Block clickedBlock) {

		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkInteract(arena, res, player, clickedBlock);
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
		
		commit.commitInteract(arena, player, clickedBlock);
	}

	public boolean checkSetFlag(Player player, Block block) {
		Arena arena = PAA_Region.activeSelections.get(player);
		
		if (arena == null) {
			return false;
		}
		
		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkSetFlag(arena, res, player, block);
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
		
		return commit.commitSetFlag(arena, player, block);
	}

	public String checkForMissingSpawns(Arena arena, Set<String> list) {
		for (ArenaGoal type : arena.getGoals()) {
			String error = type.checkForMissingSpawns(arena, list);
			if (error != null) {
				return error;
			}
		}
		return null;
	}
/*
	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
		for (ArenaGoal type : arena.getGoals()) {
			type.commitCommand(arena, sender, args);
		}
	}
*/
	public void configParse(Arena arena, YamlConfiguration config) {
		for (ArenaGoal type : arena.getGoals()) {
			type.configParse(arena, config);
		}
	}
/*
	public HashSet<String> getAddedSpawns() {
		HashSet<String> result = new HashSet<String>();
		for (ArenaGoal type : types) {

			result.addAll(type.getAddedSpawns());
		}
		return result;
	}

*/
	public HashSet<String> getAllGoalNames() {
		HashSet<String> result = new HashSet<String>();

		for (ArenaGoal goal : types) {
			result.add(goal.getName());
		}

		return result;
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
			String result = type.guessSpawn(arena, place);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * hook into language initialisation
	 * 
	 * @param config
	 *            the arena config
	 * /
	public void initLanguage(YamlConfiguration config) {
		for (ArenaGoal type : types) {
			type.initLanguage(config);
		}
	}
*/

	public void onPlayerDeath(Arena arena, Player player, PlayerDeathEvent event) {
		boolean doesRespawn = true;
		
		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkPlayerDeath(arena, res, player);
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
		
		if (!arena.getArenaConfig().getBoolean("allowDrops")) {
			event.getDrops().clear();
		}
		
		if (commit == null) {
			// no mod handles player deaths, default to infinite lives. Respawn player
			
			arena.respawnPlayer(player, event.getEntity().getLastDamageCause().getCause(), player.getKiller());
			
			return;
		}
		
		commit.commitPlayerDeath(arena, player, doesRespawn, res.getError(), event);
	}
	
	/*
	public boolean parseCommand(Arena arena, String s) {
		for (ArenaGoal type : arena.getGoals()) {
			if (type.parseCommand(arena, s)) {
				return true;
			}
		}
		return false;
	}

	public void parseInfo(Arena arena, CommandSender player) {

	}

	public void parseRespawn(Arena a, Player player, ArenaTeam team, int lives,
			DamageCause cause, Entity damager) {
		for (ArenaGoal type : a.getGoals()) {
			type.parseRespawn(a, player, team, lives, cause, damager);
		}
	}
	
	*/

	public String ready(Arena arena) {
		String error = null;
		for (ArenaGoal type : arena.getGoals()) {
			error = type.ready(arena);
			if (error != null) {
				return error;
			}
		}
		return null;
	}
	
	public void reload() {
		types = loader.reload();
		types.add(new GoalTeamLives());

		for (ArenaGoal type : types) {
			db.i("module ArenaType loaded: " + type.getName() + " (version "
					+ type.version() + ")");
		}
	}
	
	public void reset(Arena a, boolean force) {
		for (ArenaGoal type : a.getGoals()) {
			type.reset(a, force);
		}
	}

	public void setDefaults(Arena arena, YamlConfiguration config) {
		for (ArenaGoal type : arena.getGoals()) {
			type.setDefaults(arena, config);
		}
	}

	public void teleportAllToSpawn(Arena arena) {
		int priority = 0;
		PACheckResult res = new PACheckResult();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkTeleportAll(arena, res, true);
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
			arena.msg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_ERROR, "commit NULL in ArenaGoalManager.teleportAllToSpawn()"));
			return;
		}
		
		commit.teleportAllToSpawn(arena);
	}
	
	public void timedEnd(Arena arena) {

		/**
		 * name/team => score points
		 * 
		 * handed over to each module
		 */
		HashMap<String, Double> scores = new HashMap<String, Double>();

		for (ArenaGoal type : arena.getGoals()) {
			scores = type.timedEnd(arena, scores);
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
						PVPArena.instance.getAmm().announceWinner(arena,
								Language.parse(MSG.PLAYER_HAS_WON, p.getName()));
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
					PVPArena.instance.getAmm().announceWinner(arena,
							Language.parse(MSG.PLAYER_HAS_WON, "Team " + team.getName()));
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
		
		new EndRunnable(arena, arena.getArenaConfig().getInt("goal.endtimer"));
	}

	public void unload(Arena arena, Player player) {
		for (ArenaGoal type : arena.getGoals()) {
			type.unload(arena, player);
		}
	}
}
