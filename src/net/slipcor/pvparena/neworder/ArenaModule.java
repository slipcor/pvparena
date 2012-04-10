package net.slipcor.pvparena.neworder;

import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;

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

import com.nodinchan.loader.Loadable;

public class ArenaModule extends Loadable {
	protected Debug db = new Debug(46);
	
	public ArenaModule(String name) {
		super(name);
	}

	public void onSignChange(SignChangeEvent event) {
		
	}

	public boolean onPlayerInteract(PlayerInteractEvent event) {
		return false;
	}
	
	public void configParse(Arena arena, YamlConfiguration config,
			String type) {
	}
	
	public void teleportAllToSpawn(Arena arena) {
	}
	
	public void reset(Arena arena, boolean force) {
	}
	
	public void tpPlayerToCoordName(Arena arena, Player player, String place) {
	}
	
	public void onPlayerTeleport(Arena arena, PlayerTeleportEvent event) {
	}
	
	public void onEnable() {
	}
	
	public boolean hasPerms(Player player, String perms)  {
		return false;
	}
		

	public void giveRewards(Arena arena, Player player) {
	}

	public void parseJoin(Arena arena, Player player, String coloredTeam) {
	}

	public boolean checkJoin(Arena arena, Player player) {
		return true;
	}

	public boolean parseCommand(Arena arena, Player player, String[] args) {
		return false;
	}

	public boolean checkAndCommit(Arena arena, ArenaTeam aTeam) {
		return false;
	}

	public void timedEnd(Arena arena, HashSet<String> result) {
	}

	public void onEntityDamageByBlockDamage(Arena arena, Player defender,
			EntityDamageByEntityEvent event) {
	}
	
	public void onEntityDamageByEntity(Arena arena, Player attacker,
			Player defender, EntityDamageByEntityEvent event) {
	}

	public void onEntityRegainHealth(Arena arena, EntityRegainHealthEvent event) {
	}

	public void parseMove(Arena arena, PlayerMoveEvent event) {
	}

	public void onPlayerPickupItem(Arena arena, PlayerPickupItemEvent event) {
	}

	public void onPlayerVelocity(Arena arena, PlayerVelocityEvent event) {
	}

	public void parseInfo(Arena arena, Player player) {
	}

	public void lateJoin(Arena arena, Player player) {
	}

	public void resetPlayer(Arena arena, Player player) {
	}

	public void onBlockBreak(Arena arena, BlockBreakEvent event) {
	}

	public void onBlockPlace(Arena arena, BlockPlaceEvent event) {
	}

	public void onEntityExplode(Arena arena, EntityExplodeEvent event) {
	}

	public void commitPlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
	}

	public void choosePlayerTeam(Arena arena, Player player, String coloredTeam) {
	}

	public void playerLeave(Arena arena, Player player, ArenaTeam team) {
	}

	public void announceWinner(Arena arena, String message) {
	}

	public void announcePrize(Arena arena, String message) {
	}

	public void announceLoser(Arena arena, String message) {
	}
}
