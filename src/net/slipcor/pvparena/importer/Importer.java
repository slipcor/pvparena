package net.slipcor.pvparena.importer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShapeManager;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;
import net.slipcor.pvparena.managers.ArenaManager;

/**
 * <pre>
 * PVP Arena IMPORT class
 * </pre>
 * 
 * A command to import from v0.8 to v0.9+
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public final class Importer {
	/**
	 * old => new
	 */
	private static final Map<String, String> CONTENT = new HashMap<String, String>();

	static {
		CONTENT.put("classitems", "classitems");
		CONTENT.put("tp", "tp");
		CONTENT.put("setup.wand", "general.wand");

		CONTENT.put("game.allowDrops", "player.dropsInventory");
		CONTENT.put("game.dropSpawn", "modules.powerups.dropspawn");
		CONTENT.put("game.preventDeath", "player.preventDeath");
		CONTENT.put("game.teamKill", "perms.teamkill");
		CONTENT.put("game.refillInventory", "player.refillInventory");
		CONTENT.put("game.weaponDamage", "damage.weapons");
		CONTENT.put("game.mustbesafe", "goal.flags.mustBeSafe");
		CONTENT.put("game.woolFlagHead", "goal.flags.woolFlagHead");
		CONTENT.put("game.woolHead", "uses.woolHead");
		CONTENT.put("game.hideName", "modules.colorteams.hidename");

		CONTENT.put("messages.chat", "chat.enabled");
		CONTENT.put("messages.defaultChat", "chat.onlyPrivate");
		CONTENT.put("messages.onlyChat", "chat.onlyPrivate");
		CONTENT.put("messages.colorNick", "chat.colorNick");
		CONTENT.put("general.classperms", "perms.explicitClassNeeded");

		CONTENT.put("general.enabled", "general.enabled");
		CONTENT.put("general.restoreChests",
				"modules.blockrestore.restorechests");
		CONTENT.put("general.signs", "uses.classSignsDisplay");
		CONTENT.put("general.type", "general.type");
		CONTENT.put("general.item-rewards", "items.rewards");
		CONTENT.put("general.random-reward", "items.random");
		CONTENT.put("general.prefix", "general.prefix");
		CONTENT.put("general.cmdfailjoin", "cmds.defaultjoin");
		CONTENT.put("general.tpnodamageseconds", "time.teleportProtect");
		CONTENT.put("general.world", "location.world");
		CONTENT.put("general.owner", "general.owner");

		CONTENT.put("region.spawncampdamage", "damage.spawncamp");
		CONTENT.put("region.timer", "time.regionTimer");

		CONTENT.put("join.explicitPermission", "perms.explicitArenaNeeded");
		CONTENT.put("join.forceeven", "uses.evenTeams");
		CONTENT.put("join.inbattle", "perms.joinInBattle");
		CONTENT.put("join.range", "join.range");
		CONTENT.put("join.warmup", "time.warmupCountDown");
		CONTENT.put("join.emptyInventory", "modules.fixinventoryloss.gamemode");
		CONTENT.put("join.gamemodeSurvival",
				"modules.fixinventoryloss.inventory");

		CONTENT.put("goal.timed", "goal.time.timedend");
		CONTENT.put("goal.endtimer", "goal.endCountDown");

		CONTENT.put("periphery.checkRegions", "uses.overlapCheck");

		CONTENT.put("protection.spawn", "protection.spawn");
		CONTENT.put("protection.enabled", "protection.enabled");
		CONTENT.put("protection.punish", "protection.punish");

		CONTENT.put("start.countdown", "time.startCountDown");
		CONTENT.put("start.health", "player.health");
		CONTENT.put("start.foodLevel", "player.foodLevel");
		CONTENT.put("start.saturation", "player.saturation");
		CONTENT.put("start.exhaustion", "player.exhaustion");

		CONTENT.put("ready.autoclass", "ready.autoClass");
		CONTENT.put("ready.block", "ready.block");
		CONTENT.put("ready.checkEach", "ready.checkEachPlayer");
		CONTENT.put("ready.checkEachTeam", "ready.checkEachTeam");
		CONTENT.put("ready.min", "ready.min");
		CONTENT.put("ready.max", "ready.max");
		CONTENT.put("ready.maxTeam", "ready.maxTeam");
		CONTENT.put("ready.startRatio", "ready.neededRatio");

		CONTENT.put("teams", "teams");

		CONTENT.put("factions.support", "modules.factions.factive");
		CONTENT.put("blockRestore", "modules.blockrestore");
		CONTENT.put("announcements", "modules.announcements");
		CONTENT.put("arenavote", "modules.arenavote");

		CONTENT.put("betterfight.activate", "modules.betterfight.bfactive");

		CONTENT.put("money.entry", "modules.vault.entryfee");
		CONTENT.put("money.reward", "modules.vault.winreward");
		CONTENT.put("money.killreward", "modules.vault.killreward");
		CONTENT.put("money.minbet", "modules.vault.minbet");
		CONTENT.put("money.maxbet", "modules.vault.maxbet");
		CONTENT.put("money.betWinFactor", "modules.vault.betWinFactor");
		CONTENT.put("money.betTeamWinFactor", "modules.vault.betWinTeamFactor");
		CONTENT.put("money.betPlayerWinFactor",
				"modules.vault.betWinPlayerFactor");
		CONTENT.put("money.usePot", "modules.vault.winPot");
		CONTENT.put("money.winFactor", "modules.vault.winFactor");

		CONTENT.put("aftermatch", "modules.aftermatch");

		CONTENT.put("maps.playerPosition", "modules.arenamaps.aligntoplayer");
		CONTENT.put("maps.showSpawns", "modules.arenamaps.showspawns");
		CONTENT.put("maps.showPlayers", "modules.arenamaps.showplayers");
		CONTENT.put("maps.showLives", "modules.arenamaps.showlives");

		CONTENT.put("latelounge.latelounge", "modules.latelounge.llactive");

		CONTENT.put("colors.requireSpout", "modules.colorteams.spoutonly");
		CONTENT.put("colors.tagapi", "modules.colorteams.tagapi");
		CONTENT.put("whitelist", "cmds.whitelist");
	}
	
	private Importer() {
		
	}

	/**
	 * 
	 * @param a
	 *            the arena to save to
	 * @param cfg
	 *            the config to load
	 */
	@SuppressWarnings("deprecation")
	public static void commitImport(final String arenaName, final YamlConfiguration cfg) {

		final Arena arena = new Arena(arenaName);
		arena.getLegacyGoals(cfg.getString("general.type"));
		ArenaManager.loadArena(arena.getName());

		for (String node : CONTENT.keySet()) {
			final String newNode = parseToNew(node);
			arena.getArenaConfig().setManually(newNode, cfg.get(node));
		}

		HashMap<String, Object> coords = (HashMap<String, Object>) cfg
				.getConfigurationSection("spawns").getValues(false);
		final String world = cfg.getString("general.world");

		for (String name : coords.keySet()) {

			final String sLoc = String.valueOf(cfg.getString("spawns." + name));
			arena.spawnSet(name, Config.parseOldLocation(sLoc, world));
		}

		coords = (HashMap<String, Object>) cfg.getConfigurationSection(
				"regions").getValues(false);

		for (String name : coords.keySet()) {

			final ArenaRegionShape ars = hackRegion(arena, name,
					(String) coords.get(name), world);

			arena.addRegion(ars);
			ars.saveToConfig();
		}
		arena.getArenaConfig().save();
	}

	public static String parseToNew(final String oldNode) {
		return CONTENT.get(oldNode);
	}

	public static String parseToOld(final String newNode) {
		for (Entry<String, String> e : CONTENT.entrySet()) {
			if (e.getValue().equals(newNode)) {
				return e.getKey();
			}
		}
		return null;
	}

	private static ArenaRegionShape hackRegion(final Arena arena, final String name, final String coords,
			final String worldName) {
		final String[] parts = coords.split(",");

		// battlefield: 1570,53,-3608,1618,100,-3560[,sphere]

		if (parts.length < 6) {
			throw new IllegalArgumentException(
					"Input string must contain only x1, y1, z1, x2, y2, z2[, shape]: "
							+ coords);
		}
		RegionShape shape = null;

		if (parts.length == 6
				|| ArenaRegionShapeManager.getShapeByName(parts[6]) == null) {
			shape = RegionShape.CUBOID;
		} else if (parts.length > 6) {
			shape = ArenaRegionShapeManager.getShapeByName(parts[6]);
		}

		final Integer x1 = Config.parseInteger(parts[0]);
		final Integer y1 = Config.parseInteger(parts[1]);
		final Integer z1 = Config.parseInteger(parts[2]);
		final Integer x2 = Config.parseInteger(parts[3]);
		final Integer y2 = Config.parseInteger(parts[4]);
		final Integer z2 = Config.parseInteger(parts[5]);

		if (x1 == null || y1 == null || z1 == null || x2 == null || y2 == null
				|| z2 == null) {
			throw new IllegalArgumentException(
					"Some of the parsed values are null!");
		}
		final PABlockLocation[] locs = { new PABlockLocation(worldName, x1, y1, z1),
				new PABlockLocation(worldName, x2, y2, z2) };

		final ArenaRegionShape region = ArenaRegionShape.create(arena, name, shape, locs);
		region.setType(RegionType.guessFromName(name));
		if (region.getType().equals(RegionType.BATTLE)) {
			region.protectionSetAll(true);
		}
		region.saveToConfig();

		// "world,x1,y1,z1,x2,y2,z2,shape,FLAGS,PROTS,TYPE"

		return region;
	}
}
