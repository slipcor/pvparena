package net.slipcor.pvparena.loadables;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.modules.BattlefieldJoin;
import net.slipcor.pvparena.modules.RegionTool;
import net.slipcor.pvparena.modules.StandardLounge;
import net.slipcor.pvparena.modules.StandardSpectate;
import net.slipcor.pvparena.ncloader.NCBLoader;

/**
 * <pre>Arena Module Manager class</pre>
 * 
 * Loads and manages arena modules
 * 
 * @author slipcor
 * 
 * @version v0.9.3
 */

public class ArenaModuleManager {
	protected Debug db = new Debug(33);
	private List<ArenaModule> modules;
	private final NCBLoader<ArenaModule> loader;

	/**
	 * create an arena module manager instance
	 * 
	 * @param plugin
	 *            the plugin instance
	 */
	public ArenaModuleManager(PVPArena plugin) {
		File path = new File(plugin.getDataFolder().toString() + "/mods");
		if (!path.exists()) {
			path.mkdir();
		}
		loader = new NCBLoader<ArenaModule>(plugin, path, new Object[] {});
		modules = loader.load();
		modules.add(new BattlefieldJoin());
		modules.add(new RegionTool());
		modules.add(new StandardLounge());
		modules.add(new StandardSpectate());

		for (ArenaModule mod : modules) {
			db.i("module ArenaModule loaded: "
					+ mod.getName() + " (version " + mod.version() +")");
		}
	}

	public void announce(Arena arena, String message, String type) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.announce(arena, message, type);
		}
	}
	
	public String checkForMissingSpawns(Arena arena, Set<String> list) {
		String error = null;
		for (ArenaModule mod : modules) {
			if (!mod.isActive(arena)) {
				continue;
			}
			error = mod.checkForMissingSpawns(arena, list);
			if (error != null) {
				return error;
			}
		}
		return null;
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.choosePlayerTeam(arena, player, coloredTeam);
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena) && mod.commitEnd(arena, aTeam)) {
				return true;
			}
		}
		return false;
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
		for (ArenaModule mod : modules) {
			mod.configParse(arena, config);
		}
	}

	/**
	 * search modules by module name
	 * 
	 * @param mName
	 *            the module name to find
	 * @return the module if found, null otherwise
	 */
	public ArenaModule getModule(String mName) {
		for (ArenaModule mod : modules) {
			if (mod.getName().equalsIgnoreCase(mName)) {
				return mod;
			}
		}
		return null;
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.giveRewards(arena, player);
		}
	}

	/**
	 * hook into the permissions check
	 * 
	 * @param player
	 *            the player being checked
	 * @param perms
	 *            the node being checked
	 * @return true if permission is found and further processing should be
	 *         avoided
	 */
	public boolean hasPerms(Arena arena, CommandSender player, String perms) {
		Boolean has = null;
		for (ArenaModule mod : modules) {
			if (!mod.isActive(arena)) {
				continue;
			}
			has = mod.hasPerms(player, perms);
			if (has != null) {
				return has;
			}
		}
		return player.hasPermission(perms);
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.lateJoin(arena, player);
		}
	}

	public void load_arenas() {
		for (ArenaModule mod : modules) {
			mod.load_arenas();
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onBlockBreak(arena, block);
		}
	}

	public void onBlockChange(Arena arena, Block block, BlockState state) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onBlockChange(arena, block, state);
		}
	}

	public void onBlockPiston(Arena arena, Block block) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onBlockPiston(arena, block);
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onBlockPlace(arena, block, mat);
		}
	}

	/**
	 * hook into the plugin onEnable method
	 */
	public void onEnable() {
		for (ArenaModule mod : modules) {
			mod.onEnable();
		}
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
	public void onEntityDamageByBlockDamage(Arena arena, Player defender,
			EntityDamageByEntityEvent event) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onEntityDamageByBlockDamage(arena, defender, event);
		}
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
	public void onEntityDamageByEntity(Arena arena, Player attacker,
			Player defender, EntityDamageByEntityEvent event) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onEntityDamageByEntity(arena, attacker, defender, event);
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onEntityExplode(arena, event);
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onEntityRegainHealth(arena, event);
		}
	}

	public void onPaintingBreak(Arena arena, Painting painting, EntityType type) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onPaintingBreak(arena, painting, type);
		}
	}

	/**
	 * hook into the PlayerInteractEvent
	 * 
	 * @param event
	 *            the PlayerInteractEvent
	 * @return true if a valid interaction was found and further processing
	 *         should be aborted
	 */
	public boolean onPlayerInteract(Arena arena, PlayerInteractEvent event) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena) && mod.onPlayerInteract(event)) {
				return true;
			}
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onPlayerPickupItem(arena, event);
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onPlayerTeleport(arena, event);
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onPlayerVelocity(arena, event);
		}
	}

	/**
	 * hook into the SignChangeEvent
	 * 
	 * @param event
	 *            the SignChangeEvent
	 */
	public void onSignChange(Arena arena, SignChangeEvent event) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.onSignChange(event);
		}
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
	public void parsePlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.parsePlayerDeath(arena, player, cause);
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.playerLeave(arena, player, team);
		}
	}
	
	public void reload() {
		modules = loader.reload();

		for (ArenaModule mod : modules) {
			db.i("module ArenaModule loaded: "
					+ mod.getName() + " (version " + mod.version() +")");
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.reset(arena, force);
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.resetPlayer(arena, player, force);
		}
	}

	/**
	 * hook into the starting game
	 * 
	 * @param arena
	 *            the starting arena
	 */
	public void teleportAllToSpawn(Arena arena) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.teleportAllToSpawn(arena);
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.timedEnd(arena, result);
		}
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
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.tpPlayerToCoordName(arena, player, place);
		}
	}
	
	public void unload() {
		loader.unload();
	}

	/**
	 * hook into arena player unloading
	 * 
	 * @param player the player to unload
	 */
	public void unload(Player player) {
		for (ArenaModule mod : modules) {
			mod.unload(player);
		}
	}

	public List<ArenaModule> getModules() {
		return modules;
	}

	public boolean parseCommand(Arena arena, String s) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena) && mod.checkCommand(s)) {
				return true;
			}
		}
		return false;
	}

	public void parseRespawn(Arena arena, Player player, ArenaTeam team, DamageCause cause, Entity damager) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.parseRespawn(arena, player, team, cause, damager);
		}
	}

	public void parseJoin(PACheck res, Arena arena, Player sender,
			ArenaTeam team) {
		for (ArenaModule mod : modules) {
			if (mod.isActive(arena))
				mod.parseJoin(arena, sender, team);
		}
	}
}
