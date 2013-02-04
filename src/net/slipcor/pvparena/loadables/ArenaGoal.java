package net.slipcor.pvparena.loadables;

import java.util.HashMap;
import java.util.Map;
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
 * @version v0.10.0
 */

public class ArenaGoal extends NCBLoadable {
	protected Debug debug = new Debug(30);
	protected Arena arena;
	protected Map<String, Integer> lifeMap = null;

	/**
	 * create an arena type instance
	 * 
	 * @param sName
	 *            the arena type name
	 */
	public ArenaGoal(final String sName) {
		super(sName);
	}

	/**
	 * does the arena type allow joining in battle?
	 */
	public boolean allowsJoinInBattle() {
		return false;
	}

	public PACheck checkCommand(final PACheck res, final String string) {
		return res;
	}

	public PACheck checkEnd(final PACheck res) {
		return res;
	}

	/**
	 * check if all necessary spawns are set
	 * 
	 * @param list
	 *            the list of all set spawns
	 * @return null if ready, error message otherwise
	 */
	public String checkForMissingSpawns(final Set<String> list) {
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
	public PACheck checkInteract(final PACheck res, final Player player, final Block clickedBlock) {
		return res;
	}

	public PACheck checkJoin(final CommandSender sender, final PACheck res, final String[] args) {
		return res;
	}

	/**
	 * notify the goal of a player death, return higher priority if goal should handle the death as WIN/LOSE
	 * @param player the dying player
	 * @return a PACheckResult instance to hand forth for parsing
	 */
	public PACheck checkPlayerDeath(final PACheck res, final Player player) {
		return res;
	}

	/**
	 * 
	 * @param res
	 * @param player
	 * @param block
	 * @return
	 */
	public PACheck checkSetBlock(final PACheck res, final Player player, final Block block) {
		return res;
	}

	public PACheck checkStart(final PACheck res) {
		return res;
	}

	public void commitCommand(final CommandSender sender, final String[] args) {
		throw new IllegalStateException(this.getName());
	}

	public void commitEnd(final boolean force) {
		throw new IllegalStateException(this.getName());
	}

	public void commitInteract(final Player player, final Block clickedBlock) {
		throw new IllegalStateException(this.getName());
	}
	
	public void commitPlayerDeath(final Player player,
			final boolean doesRespawn, final String error, final PlayerDeathEvent event) {
		throw new IllegalStateException(this.getName());
	}

	public boolean commitSetFlag(final Player player, final Block block) {
		throw new IllegalStateException(this.getName());
	}
	
	public void commitStart() {
		throw new IllegalStateException(this.getName());
	}

	/**
	 * hook into the config parsing
	 * @param config 
	 */
	public void configParse(final YamlConfiguration config) {
	}
	
	public void disconnect(final ArenaPlayer player) {
	}

	public void displayInfo(final CommandSender sender) {
	}

	protected Map<String, Integer> getLifeMap() {
		if (lifeMap == null) {
			lifeMap = new HashMap<String, Integer>();
		}
		return lifeMap;
	}
	
	public PACheck getLives(final PACheck res, final ArenaPlayer player) {
		return res;
	}
	
	/**
	 * guess the spawn name from a given string
	 * 
	 * @param place
	 *            the string to check
	 * @return the proper spawn name
	 */
	public String guessSpawn(final String place) {
		return null;
	}

	public boolean hasSpawn(final String string) {
		return false;
	}

	public void initate(final Player player) {
	}

	public void onThisLoad() {
	}

	public void parseLeave(final Player player) {
	}
	
	public void parsePlayerDeath(final Player player,
			final EntityDamageEvent lastDamageCause) {
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
	public void reset(final boolean force) {
	}

	public void setArena(final Arena arena) {
		this.arena = arena;
	}

	public void setDefaults(final YamlConfiguration config) {
	}
	
	public void setPlayerLives(final int value) {
	}

	public void setPlayerLives(final ArenaPlayer player, final int value) {
	}

	public Map<String, Double> timedEnd(final Map<String, Double> scores) {
		return scores;
	}
	/**
	 * hook into arena player unloading
	 * 
	 * @param player
	 *            the player to unload
	 */
	public void unload(final Player player) {
	}

	public String version() {
		return "outdated";
	}
}
