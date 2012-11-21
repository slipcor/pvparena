package net.slipcor.pvparena.loadables;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.ncloader.NCBLoadable;

/**
 * <pre>Arena Goal class</pre>
 * 
 * The framework for adding goals to an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.8
 */

public class ArenaGoal extends NCBLoadable implements Cloneable {
	protected static Debug db = new Debug(30);
	protected Arena arena;

	/**
	 * create an arena type instance
	 * 
	 * @param sName
	 *            the arena type name
	 */
	public ArenaGoal(Arena arena, String sName) {
		super(sName);
		this.arena = arena;
	}
	
	

	/**
	 * does the arena type allow joining in battle?
	 */
	public boolean allowsJoinInBattle() {
		return false;
	}

	public PACheck checkCommand(PACheck res, String string) {
		return res;
	}

	public PACheck checkEnd(PACheck res) {
		return res;
	}

	/**
	 * check if all necessary spawns are set
	 * 
	 * @param list
	 *            the list of all set spawns
	 * @return null if ready, error message otherwise
	 */
	public String checkForMissingSpawns(Set<String> list) {
		return null;
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
	public PACheck checkInteract(PACheck res, Player player, Block clickedBlock) {
		return res;
	}

	public PACheck checkJoin(CommandSender sender, PACheck res, String[] args) {
		return res;
	}

	/**
	 * notify the goal of a player death, return higher priority if goal should handle the death as WIN/LOSE
	 * @param arena the arena
	 * @param player the dying player
	 * @return a PACheckResult instance to hand forth for parsing
	 */
	public PACheck checkPlayerDeath(PACheck res, Player player) {
		return res;
	}

	/**
	 * 
	 * @param arena
	 * @param res
	 * @param player
	 * @param block
	 * @return
	 */
	public PACheck checkSetFlag(PACheck res, Player player, Block block) {
		return res;
	}

	public PACheck checkStart(PACheck res) {
		return res;
	}
	
	@Override
	public ArenaGoal clone() {
		return new ArenaGoal(this.arena, this.getName());
	}

	public void commitCommand(CommandSender sender, String[] args) {
		throw new IllegalStateException();
	}

	public void commitEnd(boolean force) {
		throw new IllegalStateException();
	}

	public void commitInteract(Player player, Block clickedBlock) {
		throw new IllegalStateException();
	}
	
	public void commitPlayerDeath(Player player,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		throw new IllegalStateException();
	}

	public boolean commitSetFlag(Player player, Block block) {
		throw new IllegalStateException();
	}
	
	public void commitStart() {
		throw new IllegalStateException();
	}

	/**
	 * hook into the config parsing
	 * @param config 
	 */
	public void configParse(YamlConfiguration config) {
	}
	
	public void disconnect(ArenaPlayer player) {
	}

	public void displayInfo(CommandSender sender) {
	}

	public PACheck getLives(PACheck res, ArenaPlayer ap) {
		return res;
	}

	/**
	 * guess the spawn name from a given string
	 * 
	 * @param place
	 *            the string to check
	 * @return the proper spawn name
	 */
	public String guessSpawn(String place) {
		return null;
	}

	public boolean hasSpawn(String string) {
		return false;
	}

	public void initate(Player player) {
	}

	public void parseLeave(Player player) {
	}
	
	public void parsePlayerDeath(Player player,
			EntityDamageEvent lastDamageCause) {
	}

	public void parseStart() {
	}

	/**
	 * check if the arena is ready
	 * 
	 * @param arena
	 *            the arena to check
	 * @return null if ready, error message otherwise
	 */
	public String ready() {
		return null;
	}

	/**
	 * hook into an arena reset
	 * @param a the arena being reset
	 * @param force
	 *            is the resetting forced?
	 */
	public void reset(boolean force) {
	}

	public void setArena(Arena a) {
		arena = a;
	}

	public void setDefaults(YamlConfiguration config) {
	}
	
	public void setPlayerLives(int value) {
	}

	public void setPlayerLives(ArenaPlayer ap, int value) {
	}

	public HashMap<String, Double> timedEnd(HashMap<String, Double> scores) {
		return scores;
	}

	
	/**
	 * hook into arena player unloading
	 * 
	 * @param player
	 *            the player to unload
	 */
	public void unload(Player player) {
	}

	public String version() {
		return "outdated";
	}
}
