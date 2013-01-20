package net.slipcor.pvparena.loadables;

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
	protected static Debug debug = new Debug(32);
	
	protected Arena arena;
	
	public ArenaModule(final String name) {
		super(name);
	}
	
	public void announce(final String message, final String type) {
	}
	
	public boolean cannotSelectClass(final Player player,
			final String className) {
		return false;
	}
	
	public boolean checkCommand(final String firstArgument) {
		return false;
	}
	
	public PACheck checkJoin(final CommandSender sender,
			final PACheck res, final boolean isSpectating) {
		return res;
	}
	
	public String checkForMissingSpawns(final Set<String> list) {
		return null;
	}
	
	public PACheck checkStart(final ArenaPlayer aPlayer,
			final PACheck res) {
		return res;
	}
	
	public void choosePlayerTeam(final Player player, final String coloredTeam) {
	}
	
	public void commitCommand(final CommandSender sender, final String[] args) {
		throw new IllegalStateException(this.getName());
	}
	
	public boolean commitEnd(final ArenaTeam aTeam) {
		return false;
	}
	
	public void commitJoin(final Player sender,
			final ArenaTeam team) {
		throw new IllegalStateException(this.getName());
	}
	
	public void commitSpectate(final Player player) {
		throw new IllegalStateException(this.getName());
	}
	
	public void configParse(final YamlConfiguration config) {
	}
	
	public void displayInfo(final CommandSender sender) {
	}
	
	public Arena getArena() {
		return arena;
	}
	
	public void giveRewards(final Player player) {
	}
	
	public boolean hasSpawn(final String string) {
		return false;
	}
	
	public void initiate(final Player sender) {
	}
	
	public void lateJoin(final Player player) {
	}
	
	public void onBlockBreak(final Block block) {
	}
	
	public void onBlockChange(final Block block, final BlockState state) {
	}
	
	public void onBlockPiston(final Block block) {
	}
	
	public void onBlockPlace(final Block block, final Material mat) {
	}
	
	public void onEntityDamageByEntity(final Player attacker,
			final Player defender, final EntityDamageByEntityEvent event) {
	}
	
	public void onEntityExplode(final EntityExplodeEvent event) {
	}
	
	public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
	}
	
	public void onPaintingBreak(final Hanging painting, final EntityType type) {
	}
	
	public boolean onPlayerInteract(final PlayerInteractEvent event) {
		return false;
	}
	
	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
	}
	
	public void onPlayerVelocity(final PlayerVelocityEvent event) {
	}

	public void onThisLoad() {
	}
	
	public void parseJoin(final CommandSender sender, final ArenaTeam team) {
	}

	public void parsePlayerDeath(final Player player,
			final EntityDamageEvent lastDamageCause) {
	}
	
	public void parseRespawn(final Player player, final ArenaTeam team,
			final DamageCause cause, final Entity damager) {
	}
	
	public void parsePlayerLeave(final Player player, final ArenaTeam team) {
	}

	public void parseStart() {
	}
	
	public void reset(final boolean force) {
	}
	
	public void resetPlayer(final Player player, final boolean force) {
	}
	
	public void setArena(final Arena arena) {
		this.arena = arena;
	}
	
	public void timedEnd(final Set<String> result) {
	}
	
	public boolean toggleEnabled(final Arena arena) {
		for (ArenaModule mod : arena.getMods()) {
			if (mod.getName().equals(this.getName())) {
				arena.modRemove(mod);
				return false;
			}
		}
		final ArenaModule mod = (ArenaModule) this.clone();
		mod.arena = arena;
		arena.modAdd(mod);
		return true;
	}
	
	public void tpPlayerToCoordName(final Player player, final String place) {
	}

	public boolean tryDeathOverride(final ArenaPlayer aPlayer, final List<ItemStack> list) {
		return false;
	}
	
	public void unload(final Player player) {
	}
	
	public String version() {
		return "outdated";
	}
}
