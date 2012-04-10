package net.slipcor.pvparena.neworder;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import com.nodinchan.loader.Loader;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;

public class ArenaModuleManager {
	private final List<ArenaModule> modules;
	
	public ArenaModuleManager(PVPArena instance) {
		File path = new File(instance.getDataFolder().toString() + "/modules");
		if (!path.exists()) {
			path.mkdir();
		}
		modules = new Loader<ArenaModule>(instance, path, new Object[] {}).load();
	}
	
	public ArenaModule getModule(String tName) {
		for (ArenaModule type : modules) {
			System.out.print("type: " +type.getName() );
			if (type.getName().equals(tName)) {
				return type;
			}
		}
		return null;
	}

	public void onSignChange(SignChangeEvent event) {
		for (ArenaModule mod : modules) {
			mod.onSignChange(event);
		}
	}

	public boolean onPlayerInteract(PlayerInteractEvent event) {
		for (ArenaModule mod : modules) {
			if (mod.onPlayerInteract(event)) {
				return true;
			}
		}
		return false;
	}

	public void configParse(Arena arena, YamlConfiguration config, String type) {
		for (ArenaModule mod : modules) {
			mod.configParse(arena, config, type);
		}
	}

	public void teleportAllToSpawn(Arena arena) {
		for (ArenaModule mod : modules) {
			mod.teleportAllToSpawn(arena);
		}
	}

	public void reset(Arena arena, boolean force) {
		for (ArenaModule mod : modules) {
			mod.reset(arena, force);
		}
	}

	public void tpPlayerToCoordName(Arena arena, Player player, String place) {
		for (ArenaModule mod : modules) {
			mod.tpPlayerToCoordName(arena, player, place);
		}
	}

	public void onPlayerTeleport(Arena arena, PlayerTeleportEvent event) {
		for (ArenaModule mod : modules) {
			mod.onPlayerTeleport(arena, event);
		}
	}

	public void onEnable() {
		for (ArenaModule mod : modules) {
			mod.onEnable();
		}
	}

	public boolean hasPerms(Player player, String perms) {
		if (player.hasPermission(perms)) {
			return true;
		}
		for (ArenaModule mod : modules) {
			if (mod.hasPerms(player, perms)) {
				return true;
			}
		}
		return false;
	}

	public void giveRewards(Arena arena, Player player) {
		for (ArenaModule mod : modules) {
			mod.giveRewards(arena, player);
		}
	}

	public void parseJoin(Arena arena, Player player, String coloredTeam) {
		for (ArenaModule mod : modules) {
			mod.parseJoin(arena, player, coloredTeam);
		}
	}

	public boolean checkJoin(Arena arena, Player player) {
		for (ArenaModule mod : modules) {
			if (!mod.checkJoin(arena, player)) {
				return false;
			}
		}
		return true;
	}

	public boolean parseCommand(Arena arena, Player player, String[] args) {
		for (ArenaModule mod : modules) {
			if (mod.parseCommand(arena, player, args)) {
				return true;
			}
		}
		return false;
	}

	public boolean checkAndCommit(Arena arena, ArenaTeam aTeam) {
		for (ArenaModule mod : modules) {
			if (mod.checkAndCommit(arena, aTeam)) {
				return true;
			}
		}
		return false;
	}

	public void timedEnd(Arena arena, HashSet<String> result) {
		
	}

	public void onEntityDamageByEntity(Arena arena, Player attacker,
			Player defender, EntityDamageByEntityEvent event) {
		for (ArenaModule mod: modules) {
			mod.onEntityDamageByEntity(arena, attacker, defender, event);
		}
	}

	public void onEntityDamageByBlockDamage(Arena arena, Player defender,
			EntityDamageByEntityEvent event) {
		for (ArenaModule mod: modules) {
			mod.onEntityDamageByBlockDamage(arena, defender, event);
		}
	}

	public void onEntityRegainHealth(Arena arena, EntityRegainHealthEvent event) {
		for (ArenaModule mod: modules) {
			mod.onEntityRegainHealth(arena, event);
		}
	}

	public void parseMove(PlayerMoveEvent event, Arena arena) {
		for (ArenaModule mod: modules) {
			mod.parseMove(arena, event);
		}
	}

	public void onPlayerPickupItem(Arena arena, PlayerPickupItemEvent event) {
		for (ArenaModule mod: modules) {
			mod.onPlayerPickupItem(arena, event);
		}
	}

	public void onPlayerVelocity(Arena arena, PlayerVelocityEvent event) {
		for (ArenaModule mod: modules) {
			mod.onPlayerVelocity(arena, event);
		}
	}

	public void parseInfo(Arena arena, Player player) {
		for (ArenaModule mod: modules) {
			mod.parseInfo(arena, player);
		}
	}

	public void lateJoin(Arena arena, Player player) {
		for (ArenaModule mod: modules) {
			mod.lateJoin(arena, player);
		}
	}

	public void resetPlayer(Arena arena, Player player) {
		for (ArenaModule mod: modules) {
			mod.resetPlayer(arena, player);
		}
	}

	public void onBlockBreak(Arena arena, BlockBreakEvent event) {
		for (ArenaModule mod: modules) {
			mod.onBlockBreak(arena, event);
		}
	}

	public void onBlockPlace(Arena arena, BlockPlaceEvent event) {
		for (ArenaModule mod: modules) {
			mod.onBlockPlace(arena, event);
		}
	}

	public void onEntityExplode(Arena arena, EntityExplodeEvent event) {
		for (ArenaModule mod: modules) {
			mod.onEntityExplode(arena, event);
		}
	}

	public void commitPlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
		for (ArenaModule mod: modules) {
			mod.commitPlayerDeath(arena, player, cause);
		}
	}

	public void choosePlayerTeam(Arena arena, Player player, String coloredTeam) {
		for (ArenaModule mod: modules) {
			mod.choosePlayerTeam(arena, player, coloredTeam);
		}
	}

	public void playerLeave(Arena arena, Player player, ArenaTeam team) {
		for (ArenaModule mod: modules) {
			mod.playerLeave(arena, player, team);
		}
	}

	public void announceWinner(Arena arena, String message) {
		for (ArenaModule mod: modules) {
			mod.announceWinner(arena, message);
		}
	}

	public void announcePrize(Arena arena, String message) {
		for (ArenaModule mod: modules) {
			mod.announcePrize(arena, message);
		}
	}

	public void announceLoser(Arena arena, String message) {
		for (ArenaModule mod: modules) {
			mod.announceLoser(arena, message);
		}
	}
}
