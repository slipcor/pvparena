package net.slipcor.pvparena.importer;

import java.util.HashMap;
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
 * <pre>PVP Arena IMPORT class</pre>
 * 
 * A command to import from v0.8 to v0.9+
 * 
 * @author slipcor
 * 
 * @version v0.9.8
 */

public class Importer {
	/**
	 * old => new
	 */
	private static HashMap<String, String> content = new HashMap<String, String>();
	
	static {
		content.put("classitems", "classitems");
		content.put("tp", "tp");
		content.put("setup.wand", "general.wand");
		
		content.put("game.allowDrops", "player.dropsInventory");
		content.put("game.dropSpawn", "modules.powerups.dropspawn");
		content.put("game.preventDeath", "player.preventDeath");
		content.put("game.teamKill", "perms.teamkill");
		content.put("game.refillInventory", "player.refillInventory");
		content.put("game.weaponDamage", "damage.weapons");
		content.put("game.mustbesafe", "goal.flags.mustBeSafe");
		content.put("game.woolFlagHead", "goal.flags.woolFlagHead");
		content.put("game.woolHead", "uses.woolHead");
		content.put("game.hideName", "modules.colorteams.hidename");
		
		content.put("messages.chat", "chat.enabled");
		content.put("messages.defaultChat", "chat.onlyPrivate");
		content.put("messages.onlyChat", "chat.onlyPrivate");
		content.put("messages.colorNick", "chat.colorNick");
		content.put("general.classperms", "perms.explicitClassNeeded");
		
		content.put("general.enabled", "general.enabled");
		content.put("general.restoreChests", "modules.blockrestore.restorechests");
		content.put("general.signs", "uses.classSignsDisplay");
		content.put("general.type", "general.type");
		content.put("general.item-rewards", "items.rewards");
		content.put("general.random-reward", "items.random");
		content.put("general.prefix", "general.prefix");
		content.put("general.cmdfailjoin", "cmds.defaultjoin");
		content.put("general.tpnodamageseconds", "time.teleportProtect");
		content.put("general.world", "location.world");
		content.put("general.owner", "general.owner");
		
		content.put("region.spawncampdamage", "damage.spawncamp");
		content.put("region.timer", "time.regionTimer");

		content.put("join.explicitPermission", "perms.explicitArenaNeeded");
		content.put("join.forceeven", "uses.evenTeams");
		content.put("join.inbattle", "perms.joinInBattle");
		content.put("join.range", "join.range");
		content.put("join.warmup", "time.warmupCountDown");
		content.put("join.emptyInventory", "modules.fixinventoryloss.gamemode");
		content.put("join.gamemodeSurvival", "modules.fixinventoryloss.inventory");
		
		content.put("goal.timed", "goal.time.timedend");
		content.put("goal.endtimer", "goal.endCountDown");
		
		content.put("periphery.checkRegions", "uses.overlapCheck");

		content.put("protection.spawn", "protection.spawn");
		content.put("protection.enabled", "protection.enabled");
		content.put("protection.blockplace", "protection.blockplace");
		content.put("protection.blockdamage", "protection.blockdamage");
		content.put("protection.punish", "protection.punish");
		content.put("protection.decay", "protection.decay");
		content.put("protection.blocktntdamage", "protection.tntblockdamage");
		content.put("protection.fade", "protection.fade");
		content.put("protection.form", "protection.form");
		content.put("protection.fluids", "protection.fluids");
		content.put("protection.firespread", "protection.firespread");
		content.put("protection.grow", "protection.grow");
		content.put("protection.inventory", "protection.inventory");
		content.put("protection.lavafirespread", "protection.lavafirespread");
		content.put("protection.lighter", "protection.lighter");
		content.put("protection.painting", "protection.painting");
		content.put("protection.pickup", "protection.pickup");
		content.put("protection.piston", "protection.piston");
		content.put("protection.tnt", "protection.tnt");

		content.put("start.countdown", "time.startCountDown");
		content.put("start.health", "player.health");
		content.put("start.foodLevel", "player.foodLevel");
		content.put("start.saturation", "player.saturation");
		content.put("start.exhaustion", "player.exhaustion");

		content.put("ready.autoclass", "ready.autoClass");
		content.put("ready.block", "ready.block");
		content.put("ready.checkEach", "ready.checkEachPlayer");
		content.put("ready.checkEachTeam", "ready.checkEachTeam");
		content.put("ready.min", "ready.min");
		content.put("ready.max", "ready.max");
		content.put("ready.maxTeam", "ready.maxTeam");
		content.put("ready.startRatio", "ready.neededRatio");
		
		content.put("teams", "teams");

		content.put("factions.support", "modules.factions.factive");
		content.put("blockRestore", "modules.blockrestore");
		content.put("announcements", "modules.announcements");
		content.put("arenavote", "modules.arenavote");

		content.put("betterfight.activate", "modules.betterfight.bfactive");

		content.put("money.entry", "modules.vault.entryfee");
		content.put("money.reward", "modules.vault.winreward");
		content.put("money.killreward", "modules.vault.killreward");
		content.put("money.minbet", "modules.vault.minbet");
		content.put("money.maxbet", "modules.vault.maxbet");
		content.put("money.betWinFactor", "modules.vault.betWinFactor");
		content.put("money.betTeamWinFactor", "modules.vault.betWinTeamFactor");
		content.put("money.betPlayerWinFactor", "modules.vault.betWinPlayerFactor");
		content.put("money.usePot", "modules.vault.winPot");
		content.put("money.winFactor", "modules.vault.winFactor");
		
		content.put("aftermatch", "modules.aftermatch");

		content.put("maps.playerPosition", "modules.arenamaps.aligntoplayer");
		content.put("maps.showSpawns", "modules.arenamaps.showspawns");
		content.put("maps.showPlayers", "modules.arenamaps.showplayers");
		content.put("maps.showLives", "modules.arenamaps.showlives");
		
		content.put("latelounge.latelounge", "modules.latelounge.llactive");

		content.put("colors.requireSpout", "modules.colorteams.spoutonly");
		content.put("colors.tagapi", "modules.colorteams.tagapi");
		content.put("whitelist", "cmds.whitelist");
	}
	
	/**
	 * 
	 * @param a the arena to save to
	 * @param cfg the config to load
	 */
	public static void commitImport(String arenaName, YamlConfiguration cfg) {
		
		Arena a = new Arena(arenaName);
		a.getLegacyGoals(cfg.getString("general.type"));
		ArenaManager.loadArena(a.getName());
		
		for (String node : content.keySet()) {
			String newNode = parseToNew(node);
			a.getArenaConfig().setManually(newNode, cfg.get(node));
		}
		
		HashMap<String, Object> coords = (HashMap<String, Object>) cfg.getConfigurationSection("spawns")
				.getValues(false);
		String world = cfg.getString("general.world");

		for (String name : coords.keySet()) {
			
			String sLoc = String.valueOf(cfg.getString("spawns." + name));
			a.spawnSet(name, Config.parseOldLocation(sLoc, world));
		}
		
		
		coords = (HashMap<String, Object>) cfg.getConfigurationSection("regions")
				.getValues(false);

		for (String name : coords.keySet()) {

			ArenaRegionShape ars = hackRegion(a, name, (String) coords.get(name), world);
			
			a.addRegion(ars);
			ars.saveToConfig();
		}
		a.getArenaConfig().save();
	}
	
	
	public static String parseToNew(String oldNode) {
		return content.get(oldNode);
	}
	
	public static String parseToOld(String newNode) {
		for (Entry<String, String> e : content.entrySet()) {
			if (e.getValue().equals(newNode)) {
				return e.getKey();
			}
		}
		return null;
	}
	
	static ArenaRegionShape hackRegion(Arena arena, String name, String coords, String worldName) {
		String[] parts = coords.split(","); 

		// battlefield: 1570,53,-3608,1618,100,-3560[,sphere]
		
		if (parts.length < 6)
			throw new IllegalArgumentException(
					"Input string must contain only x1, y1, z1, x2, y2, z2[, shape]: " + coords);
		
		RegionShape rs = null;
		
		if (parts.length == 6 || ArenaRegionShapeManager.getShapeByName(parts[6]) == null) {
			rs = RegionShape.CUBOID;
		} else if (parts.length > 6) {
			rs = ArenaRegionShapeManager.getShapeByName(parts[6]);
		}
		
		Integer x1 = Config.parseInteger(parts[0]);
		Integer y1 = Config.parseInteger(parts[1]);
		Integer z1 = Config.parseInteger(parts[2]);
		Integer x2 = Config.parseInteger(parts[3]);
		Integer y2 = Config.parseInteger(parts[4]);
		Integer z2 = Config.parseInteger(parts[5]);
		Integer flags = 0;
		Integer prots = 0;

		if (x1 == null || y1 == null || z1 == null || x2 == null || y2 == null
				|| z2 == null || flags == null || prots == null)
			throw new NullPointerException(
					"Some of the parsed values are null!");
		PABlockLocation[] l = { new PABlockLocation(worldName, x1, y1, z1),
				new PABlockLocation(worldName, x2, y2, z2) };
		
		ArenaRegionShape region = ArenaRegionShape.create(arena, name, rs, l);
		region.setType(RegionType.guessFromName(name));
		if (region.getType().equals(RegionType.BATTLE)) {
			region.protectionSetAll(true);
		}
		region.saveToConfig();

		// "world,x1,y1,z1,x2,y2,z2,shape,FLAGS,PROTS,TYPE"
		
		return region;
	}
}
