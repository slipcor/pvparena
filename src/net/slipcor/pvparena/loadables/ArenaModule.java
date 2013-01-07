package net.slipcor.pvparena.loadables;

import java.util.HashSet;
import java.util.List;
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
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;


/**
 * <pre>Arena Module class</pre>
 * 
 * The framework for adding modules to an arena
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class ArenaModule extends NCBLoadable implements Cloneable {
	protected Debug db = new Debug(32);
	
	protected Arena arena;
	
	public ArenaModule(String name) {
		super(name);
	}
	
	public void announce(String message, String type) {
	}
	
	public boolean cannotSelectClass(Player player,
			String className) {
		return false;
	}
	
	public boolean checkCommand(String s) {
		return false;
	}
	
	public PACheck checkJoin(CommandSender sender,
			PACheck res, boolean b) {
		return res;
	}
	
	public String checkForMissingSpawns(Set<String> list) {
		return null;
	}
	
	public PACheck checkStart(ArenaPlayer ap,
			PACheck res) {
		return res;
	}
	
	public void choosePlayerTeam(Player player, String coloredTeam) {
	}
	
	public void commitCommand(CommandSender sender, String[] args) {
		throw new IllegalStateException(this.getName());
	}
	
	public boolean commitEnd(ArenaTeam aTeam) {
		return false;
	}
	
	public void commitJoin(Player sender,
			ArenaTeam team) {
		throw new IllegalStateException(this.getName());
	}
	
	public void commitSpectate(Player player) {
		throw new IllegalStateException(this.getName());
	}
	
	public void configParse(YamlConfiguration config) {
	}
	
	public void displayInfo(CommandSender sender) {
	}
	
	public Arena getArena() {
		return arena;
	}
	
	public void giveRewards(Player player) {
	}
	
	public boolean hasSpawn(String string) {
		return false;
	}
	
	public void initiate(Player sender) {
	}
	
	public void lateJoin(Player player) {
	}
	
	public void onBlockBreak(Block block) {
	}
	
	public void onBlockChange(Block block, BlockState state) {
	}
	
	public void onBlockPiston(Block block) {
	}
	
	public void onBlockPlace(Block block, Material mat) {
	}
	
	public void onEntityDamageByEntity(Player attacker,
			Player defender, EntityDamageByEntityEvent event) {
	}
	
	public void onEntityExplode(EntityExplodeEvent event) {
	}
	
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
	}
	
	public void onPaintingBreak(Hanging painting, EntityType type) {
	}
	
	public boolean onPlayerInteract(PlayerInteractEvent event) {
		return false;
	}
	
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
	}
	
	public void onPlayerVelocity(PlayerVelocityEvent event) {
	}
	
	public void parseJoin(CommandSender sender, ArenaTeam team) {
	}

	public void parsePlayerDeath(Player player,
			EntityDamageEvent lastDamageCause) {
	}
	
	public void parseRespawn(Player player, ArenaTeam team,
			DamageCause cause, Entity damager) {
	}
	
	public void parsePlayerLeave(Player player, ArenaTeam team) {
	}

	public void parseStart() {
	}
	
	public void reset(boolean force) {
	}
	
	public void resetPlayer(Player player, boolean force) {
	}
	
	public void setArena(Arena arena) {
		this.arena = arena;
	}
	
	public void timedEnd(HashSet<String> result) {
	}
	
	public boolean toggleEnabled(Arena arena) {
		for (ArenaModule mod : arena.getMods()) {
			if (mod.getName().equals(this.getName())) {
				arena.modRemove(mod);
				return false;
			}
		}
		ArenaModule mod = (ArenaModule) this.clone();
		mod.arena = arena;
		arena.modAdd(mod);
		return true;
	}
	
	public void tpPlayerToCoordName(Player player, String place) {
	}

	public boolean tryDeathOverride(ArenaPlayer ap, List<ItemStack> list) {
		return false;
	}
	
	public void unload(Player player) {
	}
	
	public String version() {
		return "outdated";
	}
}
