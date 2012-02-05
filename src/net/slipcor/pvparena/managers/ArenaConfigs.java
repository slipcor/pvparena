package net.slipcor.pvparena.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Arena;
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
 * @version v0.6.0
 * 
 */

public class ArenaConfigs {
	private static Debug db = new Debug();

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
		
		config.addDefault("classitems.Ranger", "261,262:64,298,299,300,301");
		config.addDefault("classitems.Swordsman", "276,306,307,308,309");
		config.addDefault("classitems.Tank", "272,310,311,312,313");
		config.addDefault("classitems.Pyro", "259,46:3,298,299,300,301");

		config.addDefault("tp.win", "old");
		config.addDefault("tp.lose", "old");
		config.addDefault("tp.exit", "exit");
		config.addDefault("tp.death", "spectator");

		config.addDefault("general.wand", Integer.valueOf(280));
		config.addDefault("general.readyblock", "IRON_BLOCK");
		config.addDefault("general.lives", Integer.valueOf(3));
		config.addDefault("general.language", "en");
		config.addDefault("general.classperms", Boolean.valueOf(false));
		config.addDefault("general.preventDeath", Boolean.valueOf(true));

		config.addDefault("general.randomSpawn", Boolean.valueOf(false));
		config.addDefault("general.timed", Integer.valueOf(0));

		config.addDefault("general.joinrange", Integer.valueOf(0));
		config.addDefault("general.powerups", "off");
		config.addDefault("general.checkRegions", Boolean.valueOf(false));

		config.addDefault("money.entry", Integer.valueOf(0));
		config.addDefault("money.reward", Integer.valueOf(0));
		config.addDefault("money.minbet", Double.valueOf(0));
		config.addDefault("money.maxbet", Double.valueOf(0));

		config.addDefault("protection.enabled", Boolean.valueOf(true));
		config.addDefault("protection.blockplace", Boolean.valueOf(true));
		config.addDefault("protection.blockdamage", Boolean.valueOf(true));
		config.addDefault("protection.firespread", Boolean.valueOf(true));
		config.addDefault("protection.lavafirespread",
				Boolean.valueOf(true));
		config.addDefault("protection.tnt", Boolean.valueOf(true));
		config.addDefault("protection.lighter", Boolean.valueOf(true));
		config.addDefault("protection.checkExit", Boolean.valueOf(false));
		config.addDefault("protection.checkSpectator",
				Boolean.valueOf(false));
		config.addDefault("protection.checkLounges", Boolean.valueOf(false));

		if (!arena.getType().equals("free") && config.get("teams") == null) {
			config.addDefault("general.teamkill", Boolean.valueOf(false));
			config.addDefault("general.manual", Boolean.valueOf(true));
			config.addDefault("general.random", Boolean.valueOf(true));
		}
		
		if (!arena.getType().equals("free")) {
			config.addDefault("general.woolhead", Boolean.valueOf(false));
			config.addDefault("general.forceeven", Boolean.valueOf(false));
		}
		config.addDefault("general.refillInventory", Boolean.valueOf(false));

		config.addDefault("general.startHealth", Integer.valueOf(20));
		config.addDefault("general.startFoodLevel", Integer.valueOf(20));
		config.addDefault("general.startSaturation", Integer.valueOf(20));
		config.addDefault("general.startExhaustion", Float.valueOf(0));

		config.addDefault("general.colorNick", Boolean.valueOf(true));
		config.addDefault("general.readyCheckEach", Boolean.valueOf(true));
		config.addDefault("general.readyMin", Integer.valueOf(2));
		config.addDefault("general.readyMax", Integer.valueOf(0));
		config.addDefault("general.readyMinTeam", Integer.valueOf(1));
		config.addDefault("general.readyMaxTeam", Integer.valueOf(0));
		config.addDefault("general.enabled", Boolean.valueOf(true));
		config.addDefault("general.chat", Boolean.valueOf(true));
		config.addDefault("general.defaultchat", Boolean.valueOf(false));
		config.options().copyDefaults(true);
		
		cfg.set("cfgver", "0.5.11.0");
		cfg.save();

		Map<String, Object> classes = config.getConfigurationSection(
				"classitems").getValues(false);
		arena.paClassItems.clear();
		for (String className : classes.keySet()) {
			String s = (String) classes.get(className);
			String[] ss = s.split(",");
			ItemStack[] items = new ItemStack[ss.length];

			for (int i = 0; i < ss.length; i++) {
				items[i] = arena.getItemStackFromString(ss[i], null);
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

			arena.pm = new Powerups(powerups);
		}
		arena.sm = new Settings(arena);
		if (cfg.getString("general.owner") != null) {
			arena.owner = cfg.getString("general.owner");
		}
		String pu = config.getString("general.powerups", "off");

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

		arena.timed = config.getInt("general.timed", 0);

		if (config.getConfigurationSection("regions") != null) {
			Map<String, Object> regs = config
					.getConfigurationSection("regions").getValues(false);
			for (String rName : regs.keySet()) {
				arena.regions.put(rName,
						getRegionFromConfigNode(rName, config, arena));
			}
		}
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
		Location[] l = Config.parseCuboid(world, coords);

		return new ArenaRegion(string, l[0], l[1]);
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

		Set<String> list = arena.cfg.getYamlConfiguration()
				.getConfigurationSection("spawns").getValues(false).keySet();

		// we need the 2 that every arena has

		if (!list.contains("spectator"))
			return "spectator not set";
		if (!list.contains("exit"))
			return "exit not set";

		if (arena.getType().equals("free")) {
			return isFreesetup(arena, list);
		}

		if (arena.cfg.getBoolean("general.randomSpawn", false)) {

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
