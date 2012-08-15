package net.slipcor.pvparena.neworder;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nodinchan.ncbukkit.loader.Loader;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arenas.teams.TeamArena;
import net.slipcor.pvparena.core.Debug;

/**
 * arena type manager class
 * 
 * -
 * 
 * loads arena types into PVP Arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 * 
 */

public class ArenaGoalManager {
	private List<ArenaGoal> types;
	private final Loader<ArenaGoal> loader;
	protected Debug db = new Debug(52);
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
		types.add(new TeamArena());

		for (ArenaGoal type : types) {
			db.i("module ArenaType loaded: "
					+ type.getName() + " (version " + type.version() +")");
		}
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

	/**
	 * hook into language initialisation
	 * 
	 * @param config
	 *            the arena config
	 */
	public void initLanguage(YamlConfiguration config) {
		for (ArenaGoal type : types) {
			type.initLanguage(config);
		}
	}
	
	public void reload() {
		types = loader.reload();
		types.add(new TeamArena());

		for (ArenaGoal type : types) {
			db.i("module ArenaType loaded: "
					+ type.getName() + " (version " + type.version() +")");
		}
	}

	public void reset(Arena a, boolean force) {
		for (ArenaGoal type : a.getGoals()) {
			type.reset(a, force);
		}
	}

	public void parseRespawn(Arena a, Player player, ArenaTeam team, int lives,
			DamageCause cause, Entity damager) {
		for (ArenaGoal type : a.getGoals()) {
			type.parseRespawn(a, player, team, lives, cause, damager);
		}
	}

	public void teleportAllToSpawn(Arena arena) {
		for (ArenaGoal type : arena.getGoals()) {
			type.teleportAllToSpawn(arena);
		}
	}

	public boolean checkSetFlag(Arena a, Player player, Block block) {
		for (ArenaGoal type : a.getGoals()) {
			if (type.checkSetFlag(a, player, block)) {
				return true;
			}
		}
		return false;
	}

	public void unload(Arena arena, Player player) {
		for (ArenaGoal type : arena.getGoals()) {
			type.unload(arena, player);
		}
	}

	public boolean parseCommand(Arena arena, String s) {
		for (ArenaGoal type : arena.getGoals()) {
			if (type.parseCommand(arena, s)) {
				return true;
			}
		}
		return false;
	}

	public HashSet<String> getAddedSpawns() {
		HashSet<String> result = new HashSet<String>();
		for (ArenaGoal type : types) {
			
			result.addAll(type.getAddedSpawns());
		}
		return result;
	}

	public void parseInfo(Arena arena, CommandSender player) {
		
	}

	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
		for (ArenaGoal type : arena.getGoals()) {
			type.commitCommand(arena, sender, args);
		}
	}

	public void checkEntityDeath(Arena arena, Player player) {
		for (ArenaGoal type : arena.getGoals()) {
			type.checkEntityDeath(arena, player);
		}
		
	}

	public void checkInteract(Arena arena, Player player, Block clickedBlock) {
		for (ArenaGoal type : arena.getGoals()) {
			type.checkInteract(arena, player, clickedBlock);
		}
	}

	public boolean allowsJoinInBattle(Arena arena) {
		for (ArenaGoal type : arena.getGoals()) {
			if (!type.allowsJoinInBattle(arena)) {
				return false;
			}
		}
		return true;
	}

	public boolean checkAndCommit(Arena arena) {
		for (ArenaGoal type : arena.getGoals()) {
			if (type.checkAndCommit(arena)) {
				return true;
			}
		}
		return false;
	}

	public void addDefaultTeams(Arena arena, YamlConfiguration config) {
		for (ArenaGoal type : arena.getGoals()) {
			type.addDefaultTeams(arena, config);
		}
	}

	public void configParse(Arena arena) {
		for (ArenaGoal type : arena.getGoals()) {
			type.configParse(arena);
		}
	}

	public String checkSpawns(Arena arena, Set<String> list) {
		for (ArenaGoal type : arena.getGoals()) {
			String error = type.checkSpawns(arena, list);
			if (error != null) {
				return error;
			}
		}
		return null;
	}

	public void addSettings(Arena arena, HashMap<String, String> types) {
		for (ArenaGoal type : arena.getGoals()) {
			type.addSettings(arena, types);
		}
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

	public void timed(Arena arena) {
		for (ArenaGoal type : arena.getGoals()) {
			type.timed(arena);
		}
	}

	public HashSet<String> getAllGoalNames() {
		HashSet<String> result = new HashSet<String>();
		
		for (ArenaGoal goal : types) {
			result.add(goal.getName());
		}
		
		return result;
	}
}
