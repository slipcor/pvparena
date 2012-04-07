package net.slipcor.pvparena.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.ArenaBoard;
import net.slipcor.pvparena.definitions.ArenaRegion;

/**
 * config manager class
 * 
 * -
 * 
 * provides access to the config file
 * 
 * @author slipcor
 * 
 * @version v0.6.41
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
			type = arena.getType();
		}

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
		config.addDefault("game.hideName", Boolean.valueOf(false));
		config.addDefault("game.lives", Integer.valueOf(3));
		config.addDefault("game.mustbesafe", Boolean.valueOf(true));
		config.addDefault("game.preventDeath", Boolean.valueOf(true));
		config.addDefault("game.powerups", "off");
		config.addDefault("game.teamKill", Boolean.valueOf(type.equals("free")));
		config.addDefault("game.woolHead", Boolean.valueOf(false));
		config.addDefault("game.woolFlagHead", Boolean.valueOf(false));
		config.addDefault("game.refillInventory", Boolean.valueOf(false));
		config.addDefault("game.weaponDamage", Boolean.valueOf(true));

		config.addDefault("messages.language", "en");
		config.addDefault("messages.colorNick", Boolean.valueOf(true));
		config.addDefault("messages.chat", Boolean.valueOf(true));
		config.addDefault("messages.defaultChat", Boolean.valueOf(false));
		config.addDefault("messages.onlyChat", Boolean.valueOf(false));

		config.addDefault("general.classperms", Boolean.valueOf(false));
		config.addDefault("general.enabled", Boolean.valueOf(true));
		config.addDefault("general.signs", Boolean.valueOf(true));
		config.addDefault("general.item-rewards", "none");
		config.addDefault("general.prefix", "PVP Arena");
		config.addDefault("general.random-reward", Boolean.valueOf(false));

		config.addDefault("join.explicitPermission", Boolean.valueOf(false));
		config.addDefault("join.manual", Boolean.valueOf(!type.equals("free")));
		config.addDefault("join.random", Boolean.valueOf(true));
		config.addDefault("join.onCountdown", Boolean.valueOf(false));
		config.addDefault("join.forceeven", Boolean.valueOf(false));
		config.addDefault("join.inbattle", Boolean.valueOf(false));

		config.addDefault("arenatype.randomSpawn", Boolean.valueOf(false));
		config.addDefault("goal.timed", Integer.valueOf(0));

		config.addDefault("join.range", Integer.valueOf(0));
		config.addDefault("periphery.checkRegions", Boolean.valueOf(false));

		config.addDefault("money.entry", Integer.valueOf(0));
		config.addDefault("money.reward", Integer.valueOf(0));
		config.addDefault("money.minbet", Double.valueOf(0));
		config.addDefault("money.maxbet", Double.valueOf(0));
		config.addDefault("money.betWinFactor", Double.valueOf(1));
		config.addDefault("money.betTeamWinFactor", Double.valueOf(1));
		config.addDefault("money.betPlayerWinFactor", Double.valueOf(1));

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

		config.addDefault("announcements.join", Boolean.valueOf(false));
		config.addDefault("announcements.start", Boolean.valueOf(false));
		config.addDefault("announcements.end", Boolean.valueOf(false));
		config.addDefault("announcements.winner", Boolean.valueOf(false));
		config.addDefault("announcements.loser", Boolean.valueOf(false));
		config.addDefault("announcements.prize", Boolean.valueOf(false));
		config.addDefault("announcements.radius", Integer.valueOf(0));
		config.addDefault("announcements.color", "AQUA");

		config.addDefault("arenatype.teams",
				Boolean.valueOf(!type.equals("free")));
		config.addDefault(
				"arenatype.flags",
				Boolean.valueOf(type.equals("ctf") || type.equals("pumpkin")
						|| type.equals("dom")));
		config.addDefault("arenatype.pumpkin",
				Boolean.valueOf(type.equals("pumpkin")));
		config.addDefault("arenatype.deathmatch",
				Boolean.valueOf(type.equals("dm")));
		config.addDefault("arenatype.domination",
				Boolean.valueOf(type.equals("dom")));

		if (!type.equals("free") || cfg.getBoolean("arenatype.teams")) {
			if (cfg.get("teams") == null) {
				db.i("no teams defined, adding custom red and blue!");
				cfg.getYamlConfiguration().addDefault("teams.red",
						ChatColor.RED.name());
				cfg.getYamlConfiguration().addDefault("teams.blue",
						ChatColor.BLUE.name());
			}
			if (cfg.getBoolean("game.woolFlagHead")
					&& (cfg.get("flagColors") == null)) {
				db.i("no flagheads defined, adding white and black!");
				config.addDefault("flagColors.red", "WHITE");
				config.addDefault("flagColors.blue", "BLACK");
			}
		} else if (cfg.get("teams") == null) {
			cfg.getYamlConfiguration().addDefault("teams.free",
					ChatColor.WHITE.name());
		}

		config.options().copyDefaults(true);

		cfg.set("cfgver", "0.6.15.0");
		cfg.save();
		cfg.load();

		Map<String, Object> classes = config.getConfigurationSection(
				"classitems").getValues(false);
		arena.paClassItems.clear();
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

			arena.paClassItems.put(className, items);
			db.i("adding class items to class " + className);
		}

		HashMap<String, Object> powerups = new HashMap<String, Object>();
		if (config.getConfigurationSection("powerups") != null) {
			HashMap<String, Object> map = (HashMap<String, Object>) config
					.getConfigurationSection("powerups").getValues(false);
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			HashMap<String, Object> map3 = new HashMap<String, Object>();
			db.i("parsing powerups");
			for (String key : map.keySet()) {
				// key e.g. "OneUp"
				map2 = (HashMap<String, Object>) config
						.getConfigurationSection("powerups." + key).getValues(
								false);
				HashMap<String, Object> temp_map = new HashMap<String, Object>();
				for (String kkey : map2.keySet()) {
					// kkey e.g. "dmg_receive"
					if (kkey.equals("item")) {
						temp_map.put(kkey, String.valueOf(map2.get(kkey)));
						db.i(key + " => " + kkey + " => "
								+ String.valueOf(map2.get(kkey)));
					} else {
						db.i(key + " => " + kkey + " => "
								+ parseList(map3.values()));
						map3 = (HashMap<String, Object>) config
								.getConfigurationSection(
										"powerups." + key + "." + kkey)
								.getValues(false);
						temp_map.put(kkey, map3);
					}
				}
				powerups.put(key, temp_map);
			}

			arena.pum = new Powerups(powerups);

		}
		arena.sm = new Settings(arena);
		if (cfg.getString("general.owner") != null) {
			arena.owner = cfg.getString("general.owner");
		}
		String pu = config.getString("game.powerups", "off");

		arena.usesPowerups = true;
		String[] ss = pu.split(":");
		if (pu.startsWith("death")) {
			// arena.powerupTrigger = "death";
			arena.powerupDiff = Integer.parseInt(ss[1]);
		} else if (pu.startsWith("time")) {
			// arena.powerupTrigger = "time";
			arena.powerupDiff = Integer.parseInt(ss[1]);
		} else {
			arena.usesPowerups = false;
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
			arena.paTeams.put(sTeam, (String) tempMap.get(sTeam));
			db.i("added team " + sTeam + " => " + arena.paTeams.get(sTeam));
		}
		if (arena.cfg.getBoolean("arenatype.flags")) {
			arena.paTeamFlags = new HashMap<String, String>();
		}
		if (arena.cfg.getBoolean("arenatype.flags")) {
			arena.paHeadGears = new HashMap<String, ItemStack>();
		}
		if (!cfg.getBoolean("usesTeams") && type.equals("free")) {

			db.i("FreeFight Arena default overrides");

			cfg.set("game.teamKill", true);
			cfg.set("join.manual", false);
			cfg.set("join.random", true);
			cfg.set("game.woolHead", false);
			cfg.set("join.forceeven", false);
			cfg.set("arenatype.randomSpawn", true);
			cfg.set("teams", null);
			cfg.set("teams.free", "WHITE");
			cfg.save();
		}
		if (config.get("spawns") != null) {
			db.i("checkinf for leaderboard");
			if (config.get("spawns.leaderboard") != null) {
				db.i("leaderboard exists");
				Location loc = Config.parseLocation(
						Bukkit.getWorld(arena.getWorld()),
						config.getString("spawns.leaderboard"));

				Arenas.boards.put(loc, new ArenaBoard(loc, arena));
			}
		}
		arena.prefix = cfg.getString("general.prefix");
	}

	/**
	 * turn a collection of objects into a comma separated string
	 * 
	 * @param values
	 *            the collection
	 * @return the comma separated string
	 */
	private static String parseList(Collection<Object> values) {
		String s = "";
		for (Object o : values) {
			if (!s.equals("")) {
				s += ",";
			}
			try {
				s += String.valueOf(o);
				db.i("a");
			} catch (Exception e) {
				db.i("b");
				s += o.toString();
			}
		}
		return s;
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

		if (arena.cfg.getBoolean("arenatype.flags")) {
			String type = arena.getType();

			if (type.equals("dom")) {
				boolean contains = false;
				for (String s : list) {
					if (s.startsWith("flag")) {
						contains = true;
						break;
					}
				}
				if (!contains) {
					return "flags not set";
				}
			} else {

				type = type.equals("pumpkin") ? type : "flag";
				for (String team : arena.paTeams.keySet()) {
					if (!list.contains(team + type)) {
						boolean found = false;
						for (String s : list) {
							if (s.startsWith(team) && s.endsWith(type)) {
								found = true;
								break;
							}
						}
						if (!found)
							return team + type + " not set";
					}
				}
			}
		}

		if (arena.getType().equals("free")) {
			return isFreesetup(arena, list);
		}

		if (arena.cfg.getBoolean("arenatype.randomSpawn", false)) {

			// now we need a spawn and lounge for every team

			db.i("parsing random");

			Iterator<String> iter = list.iterator();
			int spawns = 0;
			int lounges = 0;
			while (iter.hasNext()) {
				String s = iter.next();
				db.i("parsing '" + s + "'");
				if (s.equals("lounge") && arena.getType().equals("team"))
					continue; // skip except for FREE
				if (s.startsWith("spawn"))
					spawns++;
				if (s.endsWith("lounge"))
					lounges++;
			}
			if (spawns > 3 && lounges >= arena.paTeams.size()) {
				return null;
			}

			return spawns + "/" + 4 + "x spawn ; " + lounges + "/"
					+ arena.paTeams.size() + "x lounge";
		} else {
			// not random! we need teams * 2 (lounge + spawn) + exit + spectator
			db.i("parsing not random");
			Iterator<String> iter = list.iterator();
			int spawns = 0;
			int lounges = 0;
			HashSet<String> setTeams = new HashSet<String>();
			while (iter.hasNext()) {
				String s = iter.next();
				db.i("parsing '" + s + "'");
				db.i("spawns: " + spawns + "; lounges: " + lounges);
				if (s.endsWith("spawn") && (!s.equals("spawn"))) {
					spawns++;
				} else if (s.endsWith("lounge") && (!s.equals("lounge"))) {
					lounges++;
				} else if (s.contains("spawn") && (!s.equals("spawn"))) {
					String[] temp = s.split("spawn");
					if (arena.paTeams.get(temp[0]) != null) {
						if (setTeams.contains(arena.paTeams.get(temp[0]))) {
							db.i("team already set");
							continue;
						}
						db.i("adding team");
						setTeams.add(arena.paTeams.get(temp[0]));
						spawns++;
					}
				}
			}
			if (spawns == arena.paTeams.size()
					&& lounges == arena.paTeams.size()) {
				return null;
			}

			return spawns + "/" + arena.paTeams.size() + "x spawn ; " + lounges
					+ "/" + arena.paTeams.size() + "x lounge";
		}
	}

	/**
	 * check if a free arena is configured completely
	 * 
	 * @param arena
	 *            the arena to check
	 * @param list
	 *            the defined spawn points
	 * @return an error string if there is something missing, null otherwise
	 */
	private static String isFreesetup(Arena arena, Set<String> list) {
		if (!list.contains("lounge"))
			return "lounge not set";
		Iterator<String> iter = list.iterator();
		int spawns = 0;
		while (iter.hasNext()) {
			String s = iter.next();
			if (s.startsWith("spawn"))
				spawns++;
		}
		if (spawns > 3) {
			return null;
		}

		return "not enough spawns (" + spawns + ")";
	}
}
