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
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegionShape;

/**
 * <pre>
 * Configuration Manager class
 * </pre>
 * 
 * Provides static methods to manage Configurations
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public final class ConfigurationManager {
	private final static Debug DEBUG = new Debug(25);

	private ConfigurationManager() {
	}
	/**
	 * create a config manager instance
	 * 
	 * @param arena
	 *            the arena to load
	 * @param cfg
	 *            the configuration
	 */
	public static boolean configParse(final Arena arena, final Config cfg) {
		if (!cfg.load()) {
			return false;
		}
		final YamlConfiguration config = cfg.getYamlConfiguration();

		if (cfg.getString(CFG.GENERAL_TYPE, "null") == null
				|| cfg.getString(CFG.GENERAL_TYPE, "null").equals("null")) {
			cfg.createDefaults();
		} else {
			// opening existing arena
			arena.setFree(cfg.getString(CFG.GENERAL_TYPE).equals("free"));

			for (CFG c : CFG.values()) {
				if (cfg.getUnsafe(c.getNode()) == null) {
					cfg.createDefaults();
					break;
				}
			}

			List<String> list = cfg.getStringList(CFG.LISTS_GOALS.getNode(),
					new ArrayList<String>());
			for (String type : list) {
				ArenaGoal aType = PVPArena.instance.getAgm()
						.getGoalByName(type);
				aType = (ArenaGoal) aType.clone();
				aType.setArena(arena);
				arena.goalAdd(aType);
			}

			list = cfg.getStringList(CFG.LISTS_MODS.getNode(),
					new ArrayList<String>());
			for (String mod : list) {
				ArenaModule aMod = PVPArena.instance.getAmm().getModByName(mod);
				aMod = (ArenaModule) aMod.clone();
				aMod.setArena(arena);
				aMod.toggleEnabled(arena);
			}

		}

		if (config.get("classitems") == null) {
			if (PVPArena.instance.getConfig().get("classitems") == null) {
				config.addDefault("classitems.Ranger",
						"261,262:64,298,299,300,301");
				config.addDefault("classitems.Swordsman", "276,306,307,308,309");
				config.addDefault("classitems.Tank", "272,310,311,312,313");
				config.addDefault("classitems.Pyro", "259,46:3,298,299,300,301");
			} else {
				for (String key : PVPArena.instance.getConfig().getKeys(false)) {
					config.addDefault("classitems." + key, PVPArena.instance
							.getConfig().get("classitems." + key));
				}
			}
		}

		PVPArena.instance.getAgm().setDefaults(arena, config);

		config.options().copyDefaults(true);

		cfg.set(CFG.Z, "0.9.0.65");
		cfg.save();
		cfg.load();

		final Map<String, Object> classes = config.getConfigurationSection(
				"classitems").getValues(false);
		arena.getClasses().clear();
		DEBUG.i("reading class items");
		for (String className : classes.keySet()) {
			String sItemList = "";

			try {
				sItemList = (String) classes.get(className);
			} catch (Exception e) {
				Bukkit.getLogger().severe(
						"[PVP Arena] Error while parsing class, skipping: "
								+ className);
				continue;
			}
			final String[] sItems = sItemList.split(",");
			final ItemStack[] items = new ItemStack[sItems.length];

			for (int i = 0; i < sItems.length; i++) {
				items[i] = StringParser.getItemStackFromString(sItems[i]);
				if (items[i] == null) {
					PVPArena.instance.getLogger().warning(
							"unrecognized item: " + items[i]);
				}
			}
			arena.addClass(className, items);
			DEBUG.i("adding class items to class " + className);
		}
		arena.addClass("custom", StringParser.getItemStacksFromString("0"));
		arena.setOwner(cfg.getString(CFG.GENERAL_OWNER));
		arena.setLocked(!cfg.getBoolean(CFG.GENERAL_ENABLED));
		arena.setFree(cfg.getString(CFG.GENERAL_TYPE).equals("free"));
		if (config.getConfigurationSection("arenaregion") == null) {
			DEBUG.i("arenaregion null");
		} else {
			DEBUG.i("arenaregion not null");
			final Map<String, Object> regs = config.getConfigurationSection(
					"arenaregion").getValues(false);
			for (String rName : regs.keySet()) {
				DEBUG.i("arenaregion '" + rName + "'");
				final ArenaRegionShape region = Config.parseRegion(arena, config,
						rName);

				if (region == null) {
					PVPArena.instance.getLogger().severe(
							"Error while loading arena, region null: " + rName);
				} else if (region.getWorld() == null) {
					PVPArena.instance.getLogger().severe(
							"Error while loading arena, world null: " + rName);
				} else {
					arena.addRegion(region);
				}
			}
		}
		arena.setRoundMap(config.getStringList("rounds"));

		cfg.save();

		PVPArena.instance.getAgm().configParse(arena, config);

		if (cfg.getYamlConfiguration().getConfigurationSection("teams") == null) {
			if (arena.isFreeForAll()) {
				config.set("teams.free", "WHITE");
			} else {
				config.set("teams.red", "RED");
				config.set("teams.blue", "BLUE");
			}
		}

		cfg.reloadMaps();

		final Map<String, Object> tempMap = (Map<String, Object>) cfg
				.getYamlConfiguration().getConfigurationSection("teams")
				.getValues(true);

		if (arena.isFreeForAll()) {
			arena.getArenaConfig().set(CFG.PERMS_TEAMKILL, true);
			arena.getArenaConfig().save();
		} else {
			for (String sTeam : tempMap.keySet()) {
				final ArenaTeam team = new ArenaTeam(sTeam,
						(String) tempMap.get(sTeam));
				arena.getTeams().add(team);
				DEBUG.i("added team " + team.getName() + " => "
						+ team.getColorCodeString());
			}
		}

		ArenaModuleManager.configParse(arena, config);
		cfg.save();
		cfg.reloadMaps();

		arena.setPrefix(cfg.getString(CFG.GENERAL_PREFIX));
		return true;
	}

	/**
	 * check if an arena is configured completely
	 * 
	 * @param arena
	 *            the arena to check
	 * @return an error string if there is something missing, null otherwise
	 */
	public static String isSetup(final Arena arena) {
		arena.getArenaConfig().load();

		if (arena.getArenaConfig().getUnsafe("spawns") == null) {
			return "no spawns set";
		}

		if (PAA_Edit.activeEdits.containsValue(arena)) {
			return "edit mode!";
		}

		final Set<String> list = arena.getArenaConfig().getYamlConfiguration()
				.getConfigurationSection("spawns").getValues(false).keySet();

		final String sExit = arena.getArenaConfig().getString(CFG.TP_EXIT);
		if (!sExit.equals("old") && !list.contains(sExit)) {
			return "Exit Spawn ('" + sExit + "') not set!";
		}
		final String sWin = arena.getArenaConfig().getString(CFG.TP_WIN);
		if (!sWin.equals("old") && !list.contains(sWin)) {
			return "Win Spawn ('" + sWin + "') not set!";
		}
		final String sLose = arena.getArenaConfig().getString(CFG.TP_LOSE);
		if (!sLose.equals("old") && !list.contains(sLose)) {
			return "Lose Spawn ('" + sLose + "') not set!";
		}
		final String sDeath = arena.getArenaConfig().getString(CFG.TP_DEATH);
		if (!sDeath.equals("old") && !list.contains(sDeath)) {
			return "Death Spawn ('" + sDeath + "') not set!";
		}

		String error = ArenaModuleManager.checkForMissingSpawns(arena, list);
		if (error != null) {
			return Language.parse(MSG.ERROR_MISSING_SPAWN, error);
		}
		error = PVPArena.instance.getAgm().checkForMissingSpawns(arena, list);
		if (error != null) {
			return Language.parse(MSG.ERROR_MISSING_SPAWN, error);
		}
		return null;
	}
}
