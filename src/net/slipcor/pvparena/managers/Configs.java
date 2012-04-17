package net.slipcor.pvparena.managers;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.definitions.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaType;

/**
 * config manager class
 * 
 * -
 * 
 * provides access to the config file
 * 
 * @author slipcor
 * 
 * @version v0.7.9
 * 
 */

public class Configs {
	private static Debug db = new Debug(26);

	/**
	 * create a config manager instance
	 * 
	 * @param arena
	 *            the arena to load
	 * @param cfg
	 *            the configuration
	 * @param type
	 */
	public static void configParse(Arena arena, Config cfg, String type) {
		cfg.load();
		YamlConfiguration config = cfg.getYamlConfiguration();

		if (type == null) {
			// opening existing arena
			type = cfg.getString("general.type", "teams");
		}
		ArenaType aType = PVPArena.instance.getAtm().getType(type);

		arena.setType((ArenaType) aType.clone());
		arena.type().setArena(arena);

		if (config.get("classitems") == null) {
			config.addDefault("classitems.Ranger", "261,262:64,298,299,300,301");
			config.addDefault("classitems.Swordsman", "276,306,307,308,309");
			config.addDefault("classitems.Tank", "272,310,311,312,313");
			config.addDefault("classitems.Pyro", "259,46:3,298,299,300,301");
		}
		config.addDefault("tp.win", "old");
		config.addDefault("tp.lose", "old");
		config.addDefault("tp.exit", "exit");
		config.addDefault("tp.death", "spectator");

		config.addDefault("setup.wand", Integer.valueOf(280));

		config.addDefault("game.allowDrops", Boolean.valueOf(true));
		config.addDefault("game.dropSpawn", Boolean.valueOf(false));
		config.addDefault("game.lives", Integer.valueOf(3));
		config.addDefault("game.preventDeath", Boolean.valueOf(true));
		config.addDefault("game.teamKill", Boolean.valueOf(type.equals("free")));
		config.addDefault("game.refillInventory", Boolean.valueOf(false));
		config.addDefault("game.weaponDamage", Boolean.valueOf(true));

		config.addDefault("messages.language", "en");
		config.addDefault("messages.chat", Boolean.valueOf(true));
		config.addDefault("messages.defaultChat", Boolean.valueOf(false));
		config.addDefault("messages.onlyChat", Boolean.valueOf(false));

		config.addDefault("general.type", type);
		config.addDefault("general.classperms", Boolean.valueOf(false));
		config.addDefault("general.enabled", Boolean.valueOf(true));
		config.addDefault("general.restoreChests", Boolean.valueOf(false));
		config.addDefault("general.signs", Boolean.valueOf(true));
		config.addDefault("general.item-rewards", "none");
		config.addDefault("general.random-reward", Boolean.valueOf(false));

		config.addDefault("join.explicitPermission", Boolean.valueOf(false));
		config.addDefault("join.manual", Boolean.valueOf(!type.equals("free")));
		config.addDefault("join.random", Boolean.valueOf(true));
		config.addDefault("join.onCountdown", Boolean.valueOf(false));
		config.addDefault("join.forceeven", Boolean.valueOf(false));
		config.addDefault("join.inbattle", Boolean.valueOf(false));

		config.addDefault("arenatype.randomSpawn", type.equals("free"));
		config.addDefault("goal.timed", Integer.valueOf(0));
		config.addDefault("goal.endtimer", Integer.valueOf(20));

		config.addDefault("join.range", Integer.valueOf(0));
		config.addDefault("periphery.checkRegions", Boolean.valueOf(false));

		config.addDefault("protection.spawn", Integer.valueOf(3));
		config.addDefault("protection.restore", Boolean.valueOf(true));
		config.addDefault("protection.enabled", Boolean.valueOf(true));
		config.addDefault("protection.blockplace", Boolean.valueOf(true));
		config.addDefault("protection.blockdamage", Boolean.valueOf(true));
		config.addDefault("protection.firespread", Boolean.valueOf(true));
		config.addDefault("protection.lavafirespread", Boolean.valueOf(true));
		config.addDefault("protection.tnt", Boolean.valueOf(true));
		config.addDefault("protection.lighter", Boolean.valueOf(true));
		config.addDefault("protection.punish", Boolean.valueOf(false));
		config.addDefault("protection.checkExit", Boolean.valueOf(false));
		config.addDefault("protection.checkSpectator", Boolean.valueOf(false));
		config.addDefault("protection.checkLounges", Boolean.valueOf(false));
		config.addDefault("protection.inventory", Boolean.valueOf(false));

		config.addDefault("start.health", Integer.valueOf(20));
		config.addDefault("start.foodLevel", Integer.valueOf(20));
		config.addDefault("start.saturation", Integer.valueOf(20));
		config.addDefault("start.exhaustion", Float.valueOf(0));

		config.addDefault("ready.startRatio", Float.valueOf((float) 0.5));
		config.addDefault("ready.block", "IRON_BLOCK");
		config.addDefault("ready.checkEach", Boolean.valueOf(true));
		config.addDefault("ready.min", Integer.valueOf(2));
		config.addDefault("ready.max", Integer.valueOf(0));
		config.addDefault("ready.minTeam", Integer.valueOf(1));
		config.addDefault("ready.maxTeam", Integer.valueOf(0));
		config.addDefault("ready.autoclass", "none");
		config.addDefault("ready.startRatio", Float.valueOf((float) 0.5));

		arena.type().addDefaultTeams(config);

		config.options().copyDefaults(true);

		cfg.set("cfgver", "0.7.8.0");
		cfg.save();
		cfg.load();

		Map<String, Object> classes = config.getConfigurationSection(
				"classitems").getValues(false);
		arena.getClasses().clear();
		db.i("reading class items");
		for (String className : classes.keySet()) {
			String s = (String) classes.get(className);
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
		arena.sm = new Settings(arena);
		if (cfg.getString("general.owner") != null) {
			arena.owner = cfg.getString("general.owner");
		}
		if (config.getConfigurationSection("regions") != null) {
			Map<String, Object> regs = config
					.getConfigurationSection("regions").getValues(false);
			for (String rName : regs.keySet()) {
				arena.regions.put(rName,
						getRegionFromConfigNode(rName, config, arena));
			}
		}

		Map<String, Object> tempMap = (Map<String, Object>) cfg
				.getYamlConfiguration().getConfigurationSection("teams")
				.getValues(true);

		for (String sTeam : tempMap.keySet()) {
			ArenaTeam team = new ArenaTeam(sTeam, (String) tempMap.get(sTeam));
			Teams.addTeam(arena, team);
			db.i("added team " + team.getName() + " => "
					+ team.getColorString());
		}

		arena.type().configParse();

		PVPArena.instance.getAmm().configParse(arena, config, type);

		arena.prefix = cfg.getString("general.prefix", "PVP Arena");
	}

	/**
	 * region creation
	 * 
	 * @param string
	 *            the region node name
	 * @param config
	 *            the config to check
	 * @param arena
	 *            the arena to check
	 * @return an error string if a node is missing, null otherwise
	 */
	private static ArenaRegion getRegionFromConfigNode(String string,
			YamlConfiguration config, Arena arena) {
		db.i("reading config region: " + arena.name + "=>" + string);
		String coords = config.getString("regions." + string);
		World world = Bukkit.getWorld(arena.getWorld());
		Location[] l = Config.parseShere(world, coords);

		ArenaRegion.regionType type = ArenaRegion.regionType.SPHERIC;

		if (l == null) {
			l = Config.parseCuboid(world, coords);
			type = ArenaRegion.regionType.CUBOID;
		}

		return new ArenaRegion(string, l[0], l[1], type);
	}

	/**
	 * check if an arena is configured completely
	 * 
	 * @param arena
	 *            the arena to check
	 * @return an error string if there is something missing, null otherwise
	 */
	public static String isSetup(Arena arena) {
		arena.cfg.load();

		if (arena.cfg.get("spawns") == null) {
			return "no spawns set";
		}

		if (arena.edit) {
			return "edit mode!";
		}

		Set<String> list = arena.cfg.getYamlConfiguration()
				.getConfigurationSection("spawns").getValues(false).keySet();

		// we need the 2 that every arena has

		if (!list.contains("spectator"))
			return "spectator not set";
		if (!list.contains("exit"))
			return "exit not set";

		return arena.type().checkSpawns(list);
	}
}
