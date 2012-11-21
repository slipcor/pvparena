package net.slipcor.pvparena.loadables;

import java.util.HashSet;
import java.util.Set;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.ncloader.NCBLoadable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;


/**
 * <pre>Arena Module class</pre>
 * 
 * The framework for adding modules to an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.8
 */

public class ArenaModule extends NCBLoadable implements Cloneable {
	protected Debug db = new Debug(32);

	/**
	 * create an arena module instance
	 * 
	 * @param name
	 *            the module name
	 */
	public ArenaModule(String name) {
		super(name);
	}
	
	public void announce(Arena arena, String message, String type) {
		
	}

	public boolean checkCommand(String s) {
		return false;
	}

	public PACheck checkJoin(Arena arena, CommandSender sender,
			PACheck res, boolean b) {
		return res;
	}

	public String checkForMissingSpawns(Arena arena, Set<String> list) {
		return null;
	}

	public PACheck checkStart(Arena arena, ArenaPlayer ap,
			PACheck res) {
		return res;
	}

	/**
	 * hook into a player choosing a team
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the choosing player
	 * @param coloredTeam
	 *            the colored team name being chosen
	 */
	public void choosePlayerTeam(Arena arena, Player player, String coloredTeam) {
	}

	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
		throw new IllegalStateException();
	}

	/**
	 * hook into the arena end
	 * 
	 * @param arena
	 *            the arena ending
	 * @param aTeam
	 *            the winning team
	 * @return true if an error occured and further processing should be avoided
	 */
	public boolean commitEnd(Arena arena, ArenaTeam aTeam) {
		return false;
	}

	public void commitJoin(Arena arena, Player sender,
			ArenaTeam team) {
		throw new IllegalStateException();
	}

	/**
	 * hook into death of a player
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the dying player
	 * @param cause
	 *            the EntityDamageEvent leading to death
	 */
	public void commitPlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
		throw new IllegalStateException();
	}

	public void commitSpectate(Arena arena, Player player) {
		throw new IllegalStateException();
	}

	/**
	 * hook into arena config parsing
	 * 
	 * @param arena
	 *            the arena being parsed
	 * @param config
	 *            the config being parsed
	 */
	public void configParse(Arena arena, YamlConfiguration config) {
	}

	public void displayInfo(Arena arena, CommandSender sender) {
	}
	
	/**
	 * hook into giving players the rewards
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the player being rewarded
	 */
	public void giveRewards(Arena arena, Player player) {
	}

	/**
	 * hook into the permissions check
	 * 
	 * @param player
	 *            the player being checked
	 * @param perms
	 *            the node being checked
	 * @return true or false if permission is found and further processing should be
	 *         avoided, null if ignored
	 */
	public Boolean hasPerms(CommandSender player, String perms) {
		return null;
	}

	/**
	 * check if a module needs a certain spawn
	 * @param string the spawn to find
	 * @return true if the spawn is found, false otherwise
	 */
	public boolean hasSpawn(Arena arena, String string) {
		return false;
	}

	/**
	 * check if the module is activated for that arena, this is very much needed to
	 * ensure modules don't affect arenas where it's not wanted
	 * @param arena the arena to check
	 * @return true if the module is used, false otherwise
	 */
	public boolean isActive(Arena arena) {
		throw new IllegalArgumentException("Module needs \"isActive\": " + getName());
	}


	/**
	 * hook into joining of a player while the fight is running
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the joining player
	 */
	public void lateJoin(Arena arena, Player player) {
	}

	/**
	 * hook into loading of all arenas
	 */
	public void load_arenas() {
	}

	/**
	 * hook into an arena BlockBreakEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the BlockBreakEvent
	 */
	public void onBlockBreak(Arena arena, Block block) {
	}

	public void onBlockChange(Arena arena, Block block, BlockState state) {
	}

	public void onBlockPiston(Arena arena, Block block) {
	}

	/**
	 * hook into an arena BlockPlaceEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the BlockPlaceEvent
	 */
	public void onBlockPlace(Arena arena, Block block, Material mat) {
	}

	/**
	 * hook into the plugin onEnable method
	 */
	public void onEnable() {
	}

	/**
	 * hook into an arena EntityDamageByEntityEvent (TNT)
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param defender
	 *            the player receiving damage
	 * @param event
	 *            the EntityDamageByEntityEvent
	 */
	public void onEntityDamageByBlockDamage(Arena arena, Player defender,
			EntityDamageByEntityEvent event) {
	}

	/**
	 * hook into an arena EntityDamageByEntityEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param attacker
	 *            the player dealing damage
	 * @param defender
	 *            the player receiving damage
	 * @param event
	 *            the EntityDamageByEntityEvent
	 */
	public void onEntityDamageByEntity(Arena arena, Player attacker,
			Player defender, EntityDamageByEntityEvent event) {
	}

	/**
	 * hook into an arena EntityExplodeEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the EntityExplodeEvent
	 */
	public void onEntityExplode(Arena arena, EntityExplodeEvent event) {
	}

	/**
	 * hook into an arena EntityRegainHealthEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the EntityRegainHealthEvent
	 */
	public void onEntityRegainHealth(Arena arena, EntityRegainHealthEvent event) {
	}

	public void onPaintingBreak(Arena arena, Painting painting, EntityType type) {
		
	}

	/**
	 * hook into the PlayerInteractEvent
	 * 
	 * @param event
	 *            the PlayerInteractEvent
	 * @return true if a valid interaction was found and further processing
	 *         should be aborted
	 */
	public boolean onPlayerInteract(PlayerInteractEvent event) {
		return false;
	}

	/**
	 * hook into an arena PlayerPickupItemEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the PlayerPickupItemEvent
	 */
	public void onPlayerPickupItem(Arena arena, PlayerPickupItemEvent event) {
	}

	/**
	 * hook into an arena PlayerTeleportEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the PlayerTeleportEvent
	 */
	public void onPlayerTeleport(Arena arena, PlayerTeleportEvent event) {
	}

	/**
	 * hook into an arena PlayerVelocityEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the PlayerVelocityEvent
	 */
	public void onPlayerVelocity(Arena arena, PlayerVelocityEvent event) {
	}

	/**
	 * hook into the SignChangeEvent
	 * 
	 * @param event
	 *            the SignChangeEvent
	 */
	public void onSignChange(SignChangeEvent event) {
	}

	/**
	 * hook into a player joining a team
	 * 
	 * @param arena
	 *            the area where this happens
	 * @param sender
	 *            the joining player
	 * @param team
	 *            the colored team name
	 */
	public void parseJoin(Arena arena, CommandSender sender, ArenaTeam team) {
	}

	public void parseLeave(Arena arena, Player player) {
	}

	public void parseMove(Arena arena, PlayerMoveEvent event) {
	}

	public void parsePlayerDeath(Arena arena, Player player,
			EntityDamageEvent lastDamageCause) {
	}

	public void parseRespawn(Arena arena, Player player, ArenaTeam team,
			DamageCause cause, Entity damager) {
	}

	/**
	 * hook into a player leaving the arena
	 * 
	 * @param arena
	 *            the arena being left
	 * @param player
	 *            the leaving player
	 * @param team
	 *            the team the player was in before
	 */
	public void playerLeave(Arena arena, Player player, ArenaTeam team) {
	}

	/**
	 * hook into an arena reset
	 * 
	 * @param arena
	 *            the arena being reset
	 * @param force
	 *            true, if the arena is being forcefully reset
	 */
	public void reset(Arena arena, boolean force) {
	}

	/**
	 * hook into the reset of a player
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the player being reset
	 */
	public void resetPlayer(Arena arena, Player player, boolean force) {
	}

	/**
	 * hook into a timed arena end
	 * 
	 * @param arena
	 *            the arena ending
	 * @param result
	 *            the remaining players
	 */
	public void timedEnd(Arena arena, HashSet<String> result) {
	}

	/**
	 * hook into teleportation of an arena player
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the player being teleported
	 * @param place
	 *            the place being teleported to
	 */
	public void tpPlayerToCoordName(Arena arena, Player player, String place) {
	}

	/**
	 * hook into arena player unloading
	 * 
	 * @param player the player to unload
	 */
	public void unload(Player player) {
	}
	
	public String version() {
		return "outdated";
	}
}
