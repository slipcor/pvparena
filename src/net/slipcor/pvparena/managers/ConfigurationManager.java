package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaRegionShape;

/**
 * <pre>Configuration Manager class</pre>
 * 
 * Provides static methods to manage Configurations
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class ConfigurationManager {
	private static Debug db = new Debug(25);

	/**
	 * create a config manager instance
	 * 
	 * @param arena
	 *            the arena to load
	 * @param cfg
	 *            the configuration
	 */
	public static void configParse(Arena arena, Config cfg) {
		cfg.load();
		YamlConfiguration config = cfg.getYamlConfiguration();

		if (cfg.getString(CFG.GENERAL_TYPE, "null") != null && !cfg.getString(CFG.GENERAL_TYPE, "null").equals("null")) {
			// opening existing arena
			arena.setFree(cfg.getString(CFG.GENERAL_TYPE).equals("free"));
			
			if (cfg.getUnsafe(CFG.MODULES_STANDARDSPECTATE_ACTIVE.getNode()) == null) {
				cfg.createDefaults();
			}
			
			List<String> list = cfg.getStringList(CFG.LISTS_GOALS.getNode(), new ArrayList<String>());
			for (String type : list) {
				ArenaGoal aType = PVPArena.instance.getAgm().getType(type);
				aType = aType.clone();
				aType.setArena(arena);
				arena.goalAdd(aType);
			}
			
		} else {
			cfg.createDefaults();
		}

		if (config.get("classitems") == null) {
			if (PVPArena.instance.getConfig().get("classitems") != null) {
				for (String key : PVPArena.instance.getConfig().getKeys(false)) {
					config.addDefault("classitems."+key, PVPArena.instance.getConfig().get("classitems."+key));
				}
			} else {
				config.addDefault("classitems.Ranger", "261,262:64,298,299,300,301");
				config.addDefault("classitems.Swordsman", "276,306,307,308,309");
				config.addDefault("classitems.Tank", "272,310,311,312,313");
				config.addDefault("classitems.Pyro", "259,46:3,298,299,300,301");
			}
		}
		/*
		config.addDefault("tp.win", "old");
		config.addDefault("tp.lose", "old");
		config.addDefault("tp.exit", "exit");
		config.addDefault("tp.death", "spectator");

		config.addDefault("setup.wand", Integer.valueOf(280));

		config.addDefault("game.allowDrops", Boolean.valueOf(true));
		config.addDefault("game.dropSpawn", Boolean.valueOf(false));
		config.addDefault("game.lives", Integer.valueOf(3));
		config.addDefault("game.preventDeath", Boolean.valueOf(true));
		config.addDefault("game.teamKill", Boolean.valueOf(arena.isFreeForAll()));
		config.addDefault("game.refillInventory", Boolean.valueOf(false));
		config.addDefault("game.weaponDamage", Boolean.valueOf(true));
		config.addDefault("game.mustbesafe", Boolean.valueOf(false));
		config.addDefault("game.woolFlagHead", Boolean.valueOf(false));

		config.addDefault("messages.language", "en");
		config.addDefault("messages.chat", Boolean.valueOf(true));
		config.addDefault("messages.defaultChat", Boolean.valueOf(false));
		config.addDefault("messages.onlyChat", Boolean.valueOf(false));

		config.addDefault("general.classperms", Boolean.valueOf(false));
		config.addDefault("general.enabled", Boolean.valueOf(true));
		config.addDefault("general.restoreChests", Boolean.valueOf(false));
		config.addDefault("general.signs", Boolean.valueOf(true));
		config.addDefault("general.type", arena.isFreeForAll()?"free":"team");
		config.addDefault("general.item-rewards", "none");
		config.addDefault("general.random-reward", Boolean.valueOf(false));
		config.addDefault("general.prefix", "PVP Arena");
		config.addDefault("general.cmdfailjoin", Boolean.valueOf(true));

		config.addDefault("region.spawncampdamage", Integer.valueOf(1));
		config.addDefault("region.timer", Integer.valueOf(20));
		
		config.addDefault("join.explicitPermission", Boolean.valueOf(false));
		config.addDefault("join.manual", Boolean.valueOf(!arena.isFreeForAll()));
		config.addDefault("join.random", Boolean.valueOf(true));
		config.addDefault("join.onCountdown", Boolean.valueOf(false));
		config.addDefault("join.forceeven", Boolean.valueOf(false));
		config.addDefault("join.inbattle", Boolean.valueOf(false));
		config.addDefault("join.range", Integer.valueOf(0));
		config.addDefault("join.warmup", Integer.valueOf(0));
		
		config.addDefault("arenatype.randomSpawn", arena.isFreeForAll());
		config.addDefault("goal.timed", Integer.valueOf(0));
		config.addDefault("goal.endtimer", Integer.valueOf(20));

		config.addDefault("periphery.checkRegions", Boolean.valueOf(false));

		config.addDefault("protection.spawn", Integer.valueOf(3));
		config.addDefault("protection.restore", Boolean.valueOf(true));
		config.addDefault("protection.enabled", Boolean.valueOf(true));
		
		config.addDefault("protection.blockplace", Boolean.valueOf(true));
		config.addDefault("protection.blockdamage", Boolean.valueOf(true));
		config.addDefault("protection.blocktntdamage", Boolean.valueOf(true));
		config.addDefault("protection.decay", Boolean.valueOf(true));
		config.addDefault("protection.drop", Boolean.valueOf(true));
		config.addDefault("protection.fade", Boolean.valueOf(true));
		config.addDefault("protection.form", Boolean.valueOf(true));
		config.addDefault("protection.fluids", Boolean.valueOf(true));
		config.addDefault("protection.firespread", Boolean.valueOf(true));
		config.addDefault("protection.grow", Boolean.valueOf(true));
		config.addDefault("protection.lavafirespread", Boolean.valueOf(true));
		config.addDefault("protection.lighter", Boolean.valueOf(true));
		config.addDefault("protection.painting", Boolean.valueOf(true));
		config.addDefault("protection.piston", Boolean.valueOf(true));
		config.addDefault("protection.punish", Boolean.valueOf(false));
		config.addDefault("protection.tnt", Boolean.valueOf(true));
		
		config.addDefault("protection.checkExit", Boolean.valueOf(false));
		config.addDefault("protection.checkSpectator", Boolean.valueOf(false));
		config.addDefault("protection.checkLounges", Boolean.valueOf(false));
		config.addDefault("protection.inventory", Boolean.valueOf(false));

		config.addDefault("delays.giveitems", Integer.valueOf(0));
		config.addDefault("delays.inventorysave", Integer.valueOf(0));
		config.addDefault("delays.inventoryprepare", Integer.valueOf(0));
		config.addDefault("delays.playerdestroy", Integer.valueOf(0));

		config.addDefault("start.countdown", Integer.valueOf(5));
		config.addDefault("start.health", Integer.valueOf(20));
		config.addDefault("start.foodLevel", Integer.valueOf(20));
		config.addDefault("start.saturation", Integer.valueOf(20));
		config.addDefault("start.exhaustion", Float.valueOf(0));

		config.addDefault("ready.startRatio", Float.valueOf((float) 0.5));
		config.addDefault("ready.block", "IRON_BLOCK");
		config.addDefault("ready.checkEach", Boolean.valueOf(true));
		config.addDefault("ready.checkEachTeam", Boolean.valueOf(true));
		config.addDefault("ready.min", Integer.valueOf(2));
		config.addDefault("ready.max", Integer.valueOf(0));
		config.addDefault("ready.minTeam", Integer.valueOf(1));
		config.addDefault("ready.maxTeam", Integer.valueOf(0));
		config.addDefault("ready.autoclass", "none");
		config.addDefault("ready.startRatio", Float.valueOf((float) 0.5));

		config.addDefault("lang.youjoin", "Welcome to the Arena!");
		config.addDefault("lang.playerjoin", "Player %1% joined team %2%");
		*/
		PVPArena.instance.getAgm().setDefaults(arena, config);

		config.options().copyDefaults(true);

		cfg.set(CFG.Z, "0.9.0.65");
		cfg.save();
		cfg.load();

		Map<String, Object> classes = config.getConfigurationSection(
				"classitems").getValues(false);
		arena.getClasses().clear();
		db.i("reading class items");
		for (String className : classes.keySet()) {
			String s = "";
			
			try {
				s = (String) classes.get(className);
			} catch (Exception e) {
				Bukkit.getLogger().severe("[PVP Arena] Error while parsing class, skipping: " + className);
				continue;
			}
			String[] ss = s.split(",");
			ItemStack[] items = new ItemStack[ss.length];

			for (int i = 0; i < ss.length; i++) {
				items[i] = StringParser.getItemStackFromString(ss[i]);
				if (items[i] == null) {
					db.w("unrecognized item: " + items[i]);
				}
			}
			arena.addClass(className, items);
			db.i("adding class items to class " + className);
		}
		arena.addClass("custom", StringParser.getItemStacksFromString("0"));
		if (cfg.getString(CFG.GENERAL_OWNER) != null) {
			arena.setOwner(CFG.GENERAL_OWNER.toString());
		}
		if (config.getConfigurationSection("arenaregion") != null) {
			Map<String, Object> regs = config
					.getConfigurationSection("arenaregion").getValues(false);
			for (String rName : regs.keySet()) {
				ArenaRegionShape region = Config.parseRegion(arena, config, rName);
				
				if (region == null) {
					PVPArena.instance.getLogger().severe("Error while loading arena, region null: " + rName);
				} else if (region.getWorld() == null) {
					PVPArena.instance.getLogger().severe("Error while loading arena, world null: " + rName);
				} else {
					arena.getRegions().add(region);
				}
			}
		}
		
		cfg.save();

		PVPArena.instance.getAgm().configParse(arena, config);
		PVPArena.instance.getAmm().configParse(arena, config);
		
		if (cfg.getYamlConfiguration().getConfigurationSection("teams") == null) {
			if (arena.isFreeForAll()) {
				config.set("teams.free", "WHITE");
			} else {
				config.set("teams.red", "RED");
				config.set("teams.blue", "BLUE");
			}
		}
		
		cfg.reloadMaps();

		
		Map<String, Object> tempMap = (Map<String, Object>) cfg
				.getYamlConfiguration().getConfigurationSection("teams")
				.getValues(true);

		if (arena.isFreeForAll()) {
			arena.getTeams().add(new ArenaTeam("free", "WHITE"));
			arena.getArenaConfig().set(CFG.PERMS_TEAMKILL, true);
			arena.getArenaConfig().save();
		} else {
			for (String sTeam : tempMap.keySet()) {
				ArenaTeam team = new ArenaTeam(sTeam, (String) tempMap.get(sTeam));
				arena.getTeams().add(team);
				db.i("added team " + team.getName() + " => "
						+ team.getColorCodeString());
			}
		}

		arena.setPrefix(cfg.getString(CFG.GENERAL_PREFIX));
	}

	/**
	 * check if an arena is configured completely
	 * 
	 * @param arena
	 *            the arena to check
	 * @return an error string if there is something missing, null otherwise
	 */
	public static String isSetup(Arena arena) {
		arena.getArenaConfig().load();

		if (arena.getArenaConfig().getUnsafe("spawns") == null) {
			return "no spawns set";
		}

		if (arena.isLocked()) {
			return "edit mode!";
		}

		Set<String> list = arena.getArenaConfig().getYamlConfiguration()
				.getConfigurationSection("spawns").getValues(false).keySet();

		String sExit = arena.getArenaConfig().getString(CFG.TP_EXIT);
		if (!sExit.equals("old") && !list.contains(sExit))
			return "Exit Spawn ('"+sExit+"') not set!";

		String sWin = arena.getArenaConfig().getString(CFG.TP_WIN);
		if (!sWin.equals("old") && !list.contains(sWin))
			return "Win Spawn ('"+sWin+"') not set!";

		String sLose = arena.getArenaConfig().getString(CFG.TP_LOSE);
		if (!sLose.equals("old") && !list.contains(sLose))
			return "Lose Spawn ('"+sLose+"') not set!";

		String sDeath = arena.getArenaConfig().getString(CFG.TP_DEATH);
		if (!sDeath.equals("old") && !list.contains(sDeath))
			return "Death Spawn ('"+sDeath+"') not set!";
		
		String error = PVPArena.instance.getAmm().checkForMissingSpawns(arena, list);
		if (error != null) {
			return error;
		}
		return PVPArena.instance.getAgm().checkForMissingSpawns(arena, list);
	}
}
