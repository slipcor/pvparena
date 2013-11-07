package net.slipcor.pvparena.loadables;

import java.io.File;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.modules.BattlefieldJoin;
import net.slipcor.pvparena.modules.CustomSpawn;
import net.slipcor.pvparena.modules.RegionTool;
import net.slipcor.pvparena.modules.StandardLounge;
import net.slipcor.pvparena.modules.StandardSpectate;
import net.slipcor.pvparena.modules.WarmupJoin;
import net.slipcor.pvparena.ncloader.NCBLoader;

/**
 * <pre>Arena Module Manager class</pre>
 *
 * Loads and manages arena modules
 *
 * @author slipcor
 *
 * @version v0.10.2
 */

public class ArenaModuleManager {
	private List<ArenaModule> mods;
	private final NCBLoader<ArenaModule> loader;
	protected final static Debug DEBUG = new Debug(33);

	/**
	 * create an arena module manager instance
	 *
	 * @param plugin
	 *            the plugin instance
	 */
	public ArenaModuleManager(final PVPArena plugin) {
		final File path = new File(plugin.getDataFolder().toString() + "/mods");
		if (!path.exists()) {
			path.mkdir();
		}
		loader = new NCBLoader<ArenaModule>(plugin, path, new Object[] {});
		mods = loader.load(ArenaModule.class);
		fill();
	}

	private void fill() {
		mods.add(new BattlefieldJoin());
		mods.add(new CustomSpawn());
		mods.add(new RegionTool());
		mods.add(new StandardLounge());
		mods.add(new StandardSpectate());
		mods.add(new WarmupJoin());

		for (ArenaModule mod : mods) {
			mod.onThisLoad();
			DEBUG.i("module ArenaModule loaded: "
					+ mod.getName() + " (version " + mod.version() +")");
		}
	}

	public static void announce(final Arena arena, final String message, final String type) {
		for (ArenaModule mod : arena.getMods()) {
			mod.announce(message, type);
		}
	}

	public static boolean cannotSelectClass(final Arena arena, final Player player,
			final String className) {
		for (ArenaModule mod : arena.getMods()) {
			if (mod.cannotSelectClass(player, className)) {
				return true;
			}
		}
		return false;
	}

	public static String checkForMissingSpawns(final Arena arena, final Set<String> list) {
		String error;
		for (ArenaModule mod : arena.getMods()) {
			error = mod.checkForMissingSpawns(list);
			if (error != null) {
				return error;
			}
		}
		return null;
	}

	public static void choosePlayerTeam(final Arena arena, final Player player, final String coloredTeam) {
		for (ArenaModule mod : arena.getMods()) {
			mod.choosePlayerTeam(player, coloredTeam);
		}
	}

	public static boolean commitEnd(final Arena arena, final ArenaTeam aTeam) {
		for (ArenaModule mod : arena.getMods()) {
			if (mod.commitEnd(aTeam)) {
				return true;
			}
		}
		return false;
	}

	public static void configParse(final Arena arena, final YamlConfiguration config) {
		for (ArenaModule mod : arena.getMods()) {
			mod.configParse(config);
		}
	}

	public static void giveRewards(final Arena arena, final Player player) {
		for (ArenaModule mod : arena.getMods()) {
			mod.giveRewards(player);
		}
	}

	public static void initiate(final Arena arena, final Player sender) {
		for (ArenaModule mod : arena.getMods()) {
			mod.initiate(sender);
		}
	}

	public static void lateJoin(final Arena arena, final Player player) {
		for (ArenaModule mod : arena.getMods()) {
			mod.lateJoin(player);
		}
	}

	public static void onBlockBreak(final Arena arena, final Block block) {
		for (ArenaModule mod : arena.getMods()) {
			mod.onBlockBreak( block);
		}
	}

	public static void onBlockChange(final Arena arena, final Block block, final BlockState state) {
		for (ArenaModule mod : arena.getMods()) {
			mod.onBlockChange(block, state);
		}
	}

	public static void onBlockPiston(final Arena arena, final Block block) {
		for (ArenaModule mod : arena.getMods()) {
			mod.onBlockPiston(block);
		}
	}

	public static void onBlockPlace(final Arena arena, final Block block, final Material mat) {
		for (ArenaModule mod : arena.getMods()) {
			mod.onBlockPlace(block, mat);
		}
	}

	public static void onEntityDamageByEntity(final Arena arena, final Player attacker,
			final Player defender, final EntityDamageByEntityEvent event) {
		for (ArenaModule mod : arena.getMods()) {
			mod.onEntityDamageByEntity(attacker, defender, event);
		}
	}

	public static void onEntityExplode(final Arena arena, final EntityExplodeEvent event) {
		for (ArenaModule mod : arena.getMods()) {
			mod.onEntityExplode(event);
		}
	}

	public static void onEntityRegainHealth(final Arena arena, final EntityRegainHealthEvent event) {
		for (ArenaModule mod : arena.getMods()) {
			mod.onEntityRegainHealth(event);
		}
	}

	public static void onPaintingBreak(final Arena arena, final Hanging painting, final EntityType type) {
		for (ArenaModule mod : arena.getMods()) {
			mod.onPaintingBreak(painting, type);
		}
	}

	public static boolean onPlayerInteract(final Arena arena, final PlayerInteractEvent event) {
		for (ArenaModule mod : arena.getMods()) {
			if (mod.onPlayerInteract(event)) {
				return true;
			}
		}
		return false;
	}

	public static void onPlayerPickupItem(final Arena arena, final PlayerPickupItemEvent event) {
		for (ArenaModule mod : arena.getMods()) {
			mod.onPlayerPickupItem(event);
		}
	}

	public static void onPlayerVelocity(final Arena arena, final PlayerVelocityEvent event) {
		for (ArenaModule mod : arena.getMods()) {
			mod.onPlayerVelocity(event);
		}
	}

	public static void parseJoin(final PACheck res, final Arena arena, final Player sender,
			final ArenaTeam team) {
		for (ArenaModule mod : arena.getMods()) {
			mod.parseJoin(sender, team);
		}
	}

	public static void parsePlayerDeath(final Arena arena, final Player player,
			final EntityDamageEvent cause) {
		for (ArenaModule mod : arena.getMods()) {
			mod.parsePlayerDeath(player, cause);
		}
	}

	public static void parsePlayerLeave(final Arena arena, final Player player, final ArenaTeam team) {
		for (ArenaModule mod : arena.getMods()) {
			mod.parsePlayerLeave(player, team);
		}
	}

	public static void parseRespawn(final Arena arena, final Player player, final ArenaTeam team, final DamageCause cause, final Entity damager) {
		for (ArenaModule mod : arena.getMods()) {
			try {
				mod.parseRespawn(player, team, cause, damager);
			} catch (Exception e) {
				PVPArena.instance.getLogger().warning("Module had NPE on Respawn: " + mod.getName());
			}
		}
	}

	public static void reset(final Arena arena, final boolean force) {
		for (ArenaModule mod : arena.getMods()) {
			mod.reset(force);
		}
	}

	public static void resetPlayer(final Arena arena, final Player player, final boolean force) {
		for (ArenaModule mod : arena.getMods()) {
			mod.resetPlayer(player, force);
		}
	}

	public static void timedEnd(final Arena arena, final Set<String> result) {
		for (ArenaModule mod : arena.getMods()) {
			mod.timedEnd(result);
		}
	}

	public static void tpPlayerToCoordName(final Arena arena, final Player player, final String place) {
		for (ArenaModule mod : arena.getMods()) {
			mod.tpPlayerToCoordName(player, place);
		}
	}

	public static void unload(final Arena arena, final Player player) {
		for (ArenaModule mod : arena.getMods()) {
			mod.unload(player);
		}
	}

	public List<ArenaModule> getAllMods() {
		return mods;
	}

	public ArenaModule getModByName(final String mName) {
		for (ArenaModule mod : mods) {
			if (mod.getName().equalsIgnoreCase(mName)) {
				return mod;
			}
		}
		return null;
	}

	public void reload() {
		mods = loader.reload(ArenaModule.class);
		fill();
	}
}
