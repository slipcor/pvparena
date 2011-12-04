package net.slipcor.pvparena.arenas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.slipcor.pvparena.PARegion;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.DebugManager;
import net.slipcor.pvparena.managers.PowerupManager;
import net.slipcor.pvparena.managers.StatsManager;
import net.slipcor.pvparena.powerups.Powerup;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

/*
 * Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.12 - set flag positions
 * 
 * history:
 *
 *     v0.3.11 - set regions for lounges, spectator, exit
 *     v0.3.10 - CraftBukkit #1337 config version, rewrite
 *     v0.3.9 - Permissions, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.7 - Bugfixes
 *     v0.3.6 - CTF Arena
 *     v0.3.5 - Powerups!!
 *     v0.3.4 - Customisable Teams
 *     v0.3.3 - Random spawns possible for every arena
 *     v0.3.2 - Classes now can store up to 6 players
 *     v0.3.1 - New Arena! FreeFight
 * 
 */

public abstract class Arena {
	
	/******************
	 * public statics *
	 ******************/
	
	public static String regionmodify = ""; // only one Arena can be in modify mode
	

	/***********
	 * publics *
	 ***********/
	
	public final HashMap<String, String> paClasses = new HashMap<String, String>();
	public final HashMap<String, String> paPlayersTeam = new HashMap<String, String>();
	public final HashMap<String, String> paPlayersClass = new HashMap<String, String>();
	public final HashMap<String, Location> paSignsLocation = new HashMap<String, Location>();
	public final HashMap<String, String> paPlayersRespawn = new HashMap<String, String>();
	public final HashMap<String, String> paPlayersTelePass = new HashMap<String, String>();
	public final HashMap<String, Byte> paPlayersLives = new HashMap<String, Byte>();
	
	public Map<String, Object> paTeams = new HashMap<String, Object>();
	public PowerupManager pm;
	public String name = "default";
	public String powerupCause;
	public String sTPexit;
	public String sTPdeath;
	public int powerupDiff;
	public int powerupDiffI = 0;
	public int wand;
	
	public boolean disableBlockPlacement;
	public boolean disableBlockDamage;
	public boolean disableAllFireSpread;
	public boolean disableLavaFireSpread;
	public boolean blockTnt;
	public boolean blockIgnite;
	public boolean forceEven;
	public boolean teamKilling;
	public boolean manuallySelectTeams;
	public boolean randomlySelectTeams;
	public boolean fightInProgress = false;
	public boolean enabled = true;
	public boolean randomSpawn = false;
	public boolean usesPowerups;
	public boolean usesProtection;

	public boolean checkExitRegion = false;
	public boolean checkSpectatorRegion = false;
	public boolean checkLoungesRegion = false;
	
	int SPAWN_ID;

	/*************
	 * protected *
	 *************/
	public Location pos1;
	public Location pos2; 
	
	protected int maxLives;
	protected String sTPwin;
	protected String sTPlose;
	public File configFile;
	protected boolean forceWoolHead;
	
	/*******************
	 * private statics *
	 *******************/
	
	private static final List<Material> ARMORS_TYPE = new LinkedList<Material>();
	private static final List<Material> HELMETS_TYPE = new LinkedList<Material>();
	private static final List<Material> CHESTPLATES_TYPE = new LinkedList<Material>();
	private static final List<Material> LEGGINGS_TYPE = new LinkedList<Material>();
	private static final List<Material> BOOTS_TYPE = new LinkedList<Material>();
	protected static final DebugManager db = new DebugManager();
	
	/************
	 * privates *
	 ************/
	
	private final HashMap<Player, ItemStack[]> savedInventories = new HashMap<Player, ItemStack[]>();
	private final HashMap<Player, ItemStack[]> savedArmories = new HashMap<Player, ItemStack[]>();
	private final HashMap<Player, Object> savedPlayerVars = new HashMap<Player, Object>();
	private final HashMap<String, Double> paPlayersBetAmount = new HashMap<String, Double>();
	public final HashMap<String, PARegion> regions = new HashMap<String, PARegion>();
	
	private PVPArena plugin;
	private String rewardItems;
	private int entryFee;
	private int rewardAmount;
	private int joinRange;
	private boolean checkRegions;
	
	static {
		HELMETS_TYPE.add(Material.LEATHER_HELMET);
		HELMETS_TYPE.add(Material.GOLD_HELMET);
		HELMETS_TYPE.add(Material.CHAINMAIL_HELMET);
		HELMETS_TYPE.add(Material.IRON_HELMET);
		HELMETS_TYPE.add(Material.DIAMOND_HELMET);

		CHESTPLATES_TYPE.add(Material.LEATHER_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.GOLD_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.CHAINMAIL_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.IRON_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.DIAMOND_CHESTPLATE);

		LEGGINGS_TYPE.add(Material.LEATHER_LEGGINGS);
		LEGGINGS_TYPE.add(Material.GOLD_LEGGINGS);
		LEGGINGS_TYPE.add(Material.CHAINMAIL_LEGGINGS);
		LEGGINGS_TYPE.add(Material.IRON_LEGGINGS);
		LEGGINGS_TYPE.add(Material.DIAMOND_LEGGINGS);

		BOOTS_TYPE.add(Material.LEATHER_BOOTS);
		BOOTS_TYPE.add(Material.GOLD_BOOTS);
		BOOTS_TYPE.add(Material.CHAINMAIL_BOOTS);
		BOOTS_TYPE.add(Material.IRON_BOOTS);
		BOOTS_TYPE.add(Material.DIAMOND_BOOTS);

		ARMORS_TYPE.addAll(HELMETS_TYPE);
		ARMORS_TYPE.addAll(CHESTPLATES_TYPE);
		ARMORS_TYPE.addAll(LEGGINGS_TYPE);
		ARMORS_TYPE.addAll(BOOTS_TYPE);
	}
	
	/*
	 * Standard constructor
	 * 
	 * - hand over plugin instance and arena name
	 * - open or create a new configuration file
	 * - parse the arena config
	 */
	public Arena(String name, PVPArena plugin) {
		this.plugin = plugin;
		this.name = name;
		db.i("loading arena "+name);

		new File("plugins/pvparena").mkdir();
		configFile = new File("plugins/pvparena/config_" + name + ".yml");
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				PVPArena.lang.log_error("filecreateerror","config_" + name);
			}
		configParse("arena");
	}
	
	/*
	 * Minumum constructor
	 * 
	 * used by the child arena types
	 */
	public Arena() { }

	/*
	 * parse the arena config
	 * 
	 * - read saved config file
	 * - apply default settings if necessary
	 * - read values out of the config
	 * - saveRegionPos()
	 */
	protected void configParse(String s) {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		config.addDefault("classes.Ranger.items","261,262:64,298,299,300,301");
		config.addDefault("classes.Swordsman.items", "276,306,307,308,309");
		config.addDefault("classes.Tank.items", "272,310,311,312,313");
		config.addDefault("classes.Pyro.items", "259,46:2,298,299,300,301");
	
		config.addDefault("general.readyblock","IRON_BLOCK");
		config.addDefault("general.lives",Integer.valueOf(3));
		config.addDefault("general.language","en");
		config.addDefault("general.tp.win","old"); // old || exit || spectator
		config.addDefault("general.tp.lose","old"); // old || exit || spectator
		config.addDefault("general.tp.exit","exit"); // old || exit || spectator
		config.addDefault("general.tp.death","spectator"); // old || exit || spectator
		config.addDefault("general.classperms",Boolean.valueOf(false)); // require permissions for a class
		
		if (!s.equals("free")) {
			config.addDefault("general.woolhead",Boolean.valueOf(false)); // enforce a wool head in case we dont have Spout installed
			config.addDefault("general.forceeven",Boolean.valueOf(false)); // require even teams
		}
		
		config.addDefault("rewards.entry-fee", Integer.valueOf(0));
		config.addDefault("rewards.amount", Integer.valueOf(0));
		config.addDefault("rewards.items", "none");

		config.addDefault("protection.enabled", Boolean.valueOf(false));
		config.addDefault("protection.wand", Integer.valueOf(280));
		config.addDefault("protection.player.disable-block-placement",Boolean.valueOf(true));
		config.addDefault("protection.player.disable-block-damage",Boolean.valueOf(true));
		config.addDefault("protection.fire.disable-all-fire-spread",Boolean.valueOf(true));
		config.addDefault("protection.fire.disable-lava-fire-spread",Boolean.valueOf(true));
		config.addDefault("protection.ignition.block-tnt",Boolean.valueOf(true));
		config.addDefault("protection.ignition.block-lighter",Boolean.valueOf(true));
		config.addDefault("protection.checkExitRegion", Boolean.valueOf(false));
		config.addDefault("protection.checkSpectatorRegion", Boolean.valueOf(false));
		config.addDefault("protection.checkLoungesRegion", Boolean.valueOf(false));

		config.addDefault("general.randomSpawn",Boolean.valueOf(false));

		config.addDefault("general.joinrange", Integer.valueOf(0));
		config.addDefault("general.powerups", "off"); // off | death:[diff] | time:[diff]
		
		if (!s.equals("free") && config.get("teams") == null) {
			config.addDefault("teams.team-killing-enabled",Boolean.valueOf(false));
			config.addDefault("teams.manually-select-teams",Boolean.valueOf(false));
			config.addDefault("teams.randomly-select-teams",Boolean.valueOf(true));
		}
		config.addDefault("general.checkRegions",Boolean.valueOf(false));
		config.options().copyDefaults(true);
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Map<String, Object> classes = config.getConfigurationSection("classes").getValues(false);
		paClasses.clear();
		for (String className : classes.keySet()) {
			paClasses.put(className,
					config.getString("classes." + className + ".items", null));
		}
		
		HashMap<String, Object> powerups = new HashMap<String, Object>();
		if (config.getConfigurationSection("powerups") != null) {
			HashMap<String, Object> map = (HashMap<String, Object>) config.getConfigurationSection("powerups").getValues(false);
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			HashMap<String, Object> map3 = new HashMap<String, Object>();
			
			for(String key : map.keySet()) {
				// key e.g. "OneUp"
				map2 = (HashMap<String, Object>) config.getConfigurationSection("powerups." + key).getValues(false);
				HashMap<String, Object> temp_map = new HashMap<String, Object>(); 
				for (String kkey : map2.keySet()) {
					// kkey e.g. "dmg_receive"
					if (kkey.equals("item")) {
						temp_map.put(kkey, String.valueOf(map2.get(kkey)));
					} else {
						map3 = (HashMap<String, Object>) config.getConfigurationSection("powerups." + key + "." + kkey).getValues(false);
						temp_map.put(kkey, map3);
					}
				}
				powerups.put(key, temp_map);
			}
			
			
			pm = new PowerupManager(powerups);
		}
		
		entryFee = config.getInt("rewards.entry-fee", 0);
		rewardAmount = config.getInt("rewards.amount", 0);
		rewardItems = config.getString("rewards.items", "none");

		teamKilling = config.getBoolean("teams.team-killing-enabled", false);
		manuallySelectTeams = config.getBoolean("teams.manually-select-teams",false);
		randomlySelectTeams = config.getBoolean("teams.randomly-select-teams",true);
		
		usesProtection = config.getBoolean("protection.enabled", true);
		wand = config.getInt("protection.wand", 280);
		disableBlockPlacement = config.getBoolean("protection.player.disable-block-placement", true);
		disableBlockDamage = config.getBoolean("protection.player.disable-block-damage", true);
		disableAllFireSpread = config.getBoolean("protection.fire.disable-all-fire-spread", true);
		disableLavaFireSpread = config.getBoolean("protection.fire.disable-lava-fire-spread", true);
		blockTnt = config.getBoolean("protection.ignition.block-tnt", true);
		blockIgnite = config.getBoolean("protection.ignition.block-lighter",true);
		
		checkExitRegion = config.getBoolean("protection.checkExitRegion", false);
		checkSpectatorRegion = config.getBoolean("protection.checkSpectatorRegion", false);
		checkLoungesRegion = config.getBoolean("protection.checkLoungesRegion", false);
		
		maxLives = config.getInt("general.lives", 3);
		joinRange = config.getInt("general.joinrange", 0);
		checkRegions = config.getBoolean("general.checkRegions", false);
		forceWoolHead = config.getBoolean("general.woolhead", false);
		String pu = config.getString("general.powerups","off");

		usesPowerups = true;
		String[] ss = pu.split(":");
		if (pu.startsWith("death")) {
			powerupCause = "death";
			powerupDiff = Integer.parseInt(ss[1]);
		} else if (pu.startsWith("time")) {
			powerupCause = "time";
			powerupDiff = Integer.parseInt(ss[1]);
		} else {
			usesPowerups = false;
		}
		
		sTPwin   = config.getString("general.tp.win","old"); // old || exit || spectator
		sTPlose  = config.getString("general.tp.lose","old"); // old || exit || spectator
		sTPexit  = config.getString("general.tp.exit","exit"); // old || exit || spectator
		sTPdeath = config.getString("general.tp.death","spectator"); // old || exit || spectator
		forceEven = config.getBoolean("general.forceeven", false);
		randomSpawn = config.getBoolean("general.randomSpawn", false);

		if (config.getConfigurationSection("protection.regions") != null) {
			Map<String, Object> regs = config.getConfigurationSection("protection.regions").getValues(false);
			for (String rName : regs.keySet()) {
				regions.put(rName, getRegionFromConfigNode(rName, config));
			}
		} else if (config.get("protection.region") != null) {
			String[] min1 = config.getString("protection.region.min").split(", ");
			String[] max1 = config.getString("protection.region.max").split(", ");
			String world = config.getString("protection.region.world");
			Location min = new Location(Bukkit.getWorld(world), new Double(min1[0]).doubleValue(),
					new Double(min1[1]).doubleValue(),
					new Double(min1[2]).doubleValue());
			Location max = new Location(Bukkit.getWorld(world), new Double(max1[0]).doubleValue(),
					new Double(max1[1]).doubleValue(),
					new Double(max1[2]).doubleValue());
			
			regions.put("battlefield", new PARegion("battlefield",min,max));

			Vector v1 = min.toVector();
			Vector v2 = max.toVector();
			config.set("protection.regions.battlefield.min", v1.getX() + ", " + v1.getY() + ", " + v1.getZ());
			config.set("protection.regions.battlefield.max", v2.getX() + ", " + v2.getY() + ", " + v2.getZ());
			config.set("protection.regions.battlefield.world", world);
			config.set("protection.region", null);
			
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * setup check
	 * 
	 * returns null if setup correct
	 * returns string if not
	 */
	private PARegion getRegionFromConfigNode(String string, YamlConfiguration config) {
		
		String[] min1 = config.getString("protection.regions.battlefield.min").split(", ");
		String[] max1 = config.getString("protection.regions.battlefield.max").split(", ");
		String world = config.getString("protection.regions.battlefield.world");
		Location min = new Location(Bukkit.getWorld(world), new Double(min1[0]).doubleValue(),
				new Double(min1[1]).doubleValue(),
				new Double(min1[2]).doubleValue());
		Location max = new Location(Bukkit.getWorld(world), new Double(max1[0]).doubleValue(),
				new Double(max1[1]).doubleValue(),
				new Double(max1[2]).doubleValue());
		
		return new PARegion(string, min, max);
	}

	public String isSetup() {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		if (config.get("coords") == null) {
			return "no coords set";
		}

		Set<String> list =  config.getConfigurationSection("coords").getValues(false).keySet();
		
		// we need the 2 that every arena has
		
		if (!list.contains("spectator"))
			return "spectator not set";
		if (!list.contains("exit"))
			return "exit not set";
		
		if (randomSpawn) {
			
			// now we need a spawn and lounge for every team 
			
			Iterator<String> iter = list.iterator();
			int spawns = 0;
			int lounges = 0;
			while (iter.hasNext()) {
				String s = iter.next();
				if (s.equals("lounge"))
					continue; // ctf setup remains, skip!
				if (s.startsWith("spawn"))
					spawns++;
				if (s.endsWith("lounge"))
					lounges++;
			}
			if (spawns > 3 && lounges >= paTeams.size()) {
				return null;
			}

			return spawns + "/" + 4 + "x spawn ; " + lounges + "/" + paTeams.size() + "x lounge";
		} else {
			// not random! we need teams * 2 (lounge + spawn) + exit + spectator
			Iterator<String> iter = list.iterator();
			int spawns = 0;
			int lounges = 0;
			while (iter.hasNext()) {
				String s = iter.next();
				if (s.endsWith("spawn") && (!s.equals("spawn")))
					spawns++;
				if (s.endsWith("lounge") && (!s.equals("lounge")))
					lounges++;
			}
			if (spawns == paTeams.size() && lounges == paTeams.size()) {
				return null;
			}

			return spawns + "/" + paTeams.size() + "x spawn ; " + lounges + "/" + paTeams.size() + "x lounge";
		}
		
	}

	/*
	 * give the player the items of his class
	 * if woolhead: replace head gear with colored wool
	 */
	public void givePlayerFightItems(Player player) {
		String playerClass = (String) paPlayersClass.get(player.getName());
		String rawItems = (String) paClasses.get(playerClass);
		db.i("giving items '"+rawItems+"' to player '"+player.getName()+"', class '"+playerClass+"'");
		
		String[] items = rawItems.split(",");

		for (int i = 0; i < items.length; ++i) {
			ItemStack stack = getItemStackFromString(items[i]);
			if (ARMORS_TYPE.contains(stack.getType())) {
				equipArmorPiece(stack, player.getInventory());
			} else {
				player.getInventory().addItem(new ItemStack[] { stack });
			}
		}
		if (forceWoolHead) {
			String sTeam = paPlayersTeam.get(player.getName());
			String color = (String) paTeams.get(sTeam);
			db.i("forcing woolhead: "+sTeam + "/" + color);
			player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, getColorShortFromColorENUM(color)));
		}
	}
	
	private short getColorShortFromColorENUM(String color) {
		
		/*
		 *  DyeColor supports:
		 *  WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK,
		 *  GRAY, SILVER, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK;
		 */
		for (DyeColor dc : DyeColor.values()) {
			if (dc.name().equalsIgnoreCase(color))
				return dc.getData();
		}
		
		return (short) 0;
	}

	/*
	 * equip an ItemStack to the corresponding armor slot
	 */
	public void equipArmorPiece(ItemStack stack, PlayerInventory inv) {
		Material type = stack.getType();
		if (HELMETS_TYPE.contains(type))
			inv.setHelmet(stack);
		else if (CHESTPLATES_TYPE.contains(type))
			inv.setChestplate(stack);
		else if (LEGGINGS_TYPE.contains(type))
			inv.setLeggings(stack);
		else if (BOOTS_TYPE.contains(type))
			inv.setBoots(stack);
	}
	
	/*
	 * clean all signs
	 */
	public void cleanSigns() {
		for (String s : paSignsLocation.keySet()) {
			Sign sign = (Sign) paSignsLocation.get(s).getBlock().getState();
			sign.setLine(2, "");
			sign.setLine(3, "");
			if (!sign.update()) {
				db.w("Sign update failed - a");
				if (!sign.update(true))
					db.s("Sign force update failed - a");
				else
					db.i("Sign force update successful - a");
			}
			
			sign = getNext(sign);
			
			if (sign != null) {
				sign.setLine(0, "");
				sign.setLine(1, "");
				sign.setLine(2, "");
				sign.setLine(3, "");
				if (!sign.update()) {
					db.w("Sign update failed - b");
					if (!sign.update(true))
						db.s("Sign force update failed - b");
					else
						db.i("Sign force update successful - b");
				}
			}
		}
	}
	
	/*
	 * clean the sign a player is on
	 */
	public void cleanPlayerSign(String player) {
		for (String s : paSignsLocation.keySet()) {
			boolean updated = false;
			Sign sign = (Sign) paSignsLocation.get(s).getBlock().getState();
			if (sign.getLine(2).equals(player)) {
				sign.setLine(2, "");
				updated = true;
			}
			if (sign.getLine(3).equals(player)) {
				sign.setLine(3, "");
				updated = true;
			}
			if (updated && !sign.update()) {
				db.w("Sign update failed - 1");
				if (!sign.update(true))
					db.s("Sign force update failed - 1");
				else
					db.i("Sign force update successful - 1");
			}
			updated = false;
			sign = getNext(sign);
			
			if (sign != null) {
				for (int i = 0; i < 4 ; i ++) {
					if (sign.getLine(i).equals(player)) {
						sign.setLine(i, "");
						updated = true;
					}
				}
				if (updated && !sign.update()) {
					db.w("Sign update failed - 2");
					if (!sign.update(true))
						db.s("Sign force update failed - 2");
					else
						db.i("Sign force update successful - 2");
				}
			}
		}
	}

	/*
	 * fetch overflow sign
	 * 
	 * (blank sign under class sign)
	 */
	public Sign getNext(Sign sign) {
		try {
			return (Sign) sign.getBlock().getRelative(BlockFace.DOWN).getState();
		} catch (Exception e) {
			return null;
		}
	}

	/*
	 * return "everyone has chosen a class"
	 */
	public boolean ready() {
		for (String p : paPlayersTeam.keySet()) {
			if (!paPlayersClass.containsKey(p)) {
				// a member is NOT ready!
				return false;
			}
		}
		return paPlayersTeam.size() > 1; // at least 2 ppl need to be in there for an arena to start
	}

	/*
	 * tell every fighting player
	 */
	public void tellEveryone(String msg) {
		db.i("@all: "+msg);
		for (String p : paPlayersTeam.keySet()) {
			Player z = Bukkit.getServer().getPlayer(p.toString());
			z.sendMessage(ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE + msg);
		}
	}
	
	/*
	 * tell every fighting player except given player
	 */
	public void tellEveryoneExcept(Player player, String msg) {
		db.i("@all/"+player.getName()+": "+msg);
		for (String p : paPlayersTeam.keySet()) {
			if (p.equals(player.getName()))
				continue;
			Player z = Bukkit.getServer().getPlayer(p.toString());
			z.sendMessage(ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE + msg);
		}
	}
	
	/*
	 * teleport every fighting player to each spawn
	 */
	public void teleportAllToSpawn() {
		for (String p : paPlayersTeam.keySet()) {
			Player z = Bukkit.getServer().getPlayer(p);
			if (!randomSpawn) {
				tpPlayerToCoordName(z, paPlayersTeam.get(p) + "spawn");
			} else {
				tpPlayerToCoordName(z, "spawn");
			}
		}
		init_arena();
		db.i("teleported everyone!");
		if (usesPowerups) {
			db.i("using powerups : " + powerupCause + " : " + powerupDiff);
			if (powerupCause.equals("time") && powerupDiff > 0){
				db.i("powerup time trigger!");
				powerupDiff = powerupDiff*20; // calculate ticks to seconds
			    // initiate autosave timer
			    SPAWN_ID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,new MyRunnable(this),powerupDiff,powerupDiff);
			}
		}
		
	}
	
	/*
	 * ghost method for CTF to override
	 */
	public void init_arena() {
		// nothing to see here
	}

	/*
	 * save player inventory to map
	 */
	public void saveInventory(Player player) {
		savedInventories.put(player, player.getInventory().getContents());
		savedArmories.put(player, player.getInventory().getArmorContents());
	}

	/*
	 * load player inventory from map
	 */
	public void loadInventory(Player player) {
		player.getInventory().setContents((ItemStack[]) savedInventories.get(player));
		player.getInventory().setArmorContents((ItemStack[]) savedArmories.get(player));
	}

	/*
	 * save player variables
	 */
	public void saveMisc(Player player) {
		HashMap<String, String> tempMap = new HashMap<String, String>();
		
		Location lLoc = player.getLocation();
		String sLoc = lLoc.getWorld().getName() + "/" + lLoc.getBlockX() + "/" + lLoc.getBlockY() + "/" + lLoc.getBlockZ() + "/";
		
		tempMap.put("EXHAUSTION", String.valueOf(player.getExhaustion()));
		tempMap.put("FIRETICKS", String.valueOf(player.getFireTicks()));
		tempMap.put("FOODLEVEL", String.valueOf(player.getFoodLevel()));
		tempMap.put("HEALTH", String.valueOf(player.getHealth()));
		tempMap.put("SATURATION", String.valueOf(player.getSaturation()));
		tempMap.put("LOCATION", sLoc);
		tempMap.put("GAMEMODE", String.valueOf(player.getGameMode().getValue()));
		savedPlayerVars.put(player, tempMap);
	}

	/*
	 * give rewards to player
	 * 
	 * - money
	 * - items
	 */
	public void giveRewards(Player player) {
		if (PVPArena.getMethod() != null) {
			for (String nKey : paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");
				
				if (nSplit[1].equalsIgnoreCase(player.getName())) {
					double amount = paPlayersBetAmount.get(nKey)*4;

					MethodAccount ma = PVPArena.getMethod().getAccount(nSplit[0]);
					ma.add(amount);
					try {
						tellPlayer(Bukkit.getPlayer(nSplit[0]), PVPArena.lang.parse("youwon",PVPArena.getMethod().format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}				
			}			
		}
		
		if ((PVPArena.getMethod() != null) && (rewardAmount > 0)) {
			MethodAccount ma = PVPArena.getMethod().getAccount(player.getName());
			ma.add(rewardAmount);
			tellPlayer(player,PVPArena.lang.parse("awarded",PVPArena.getMethod().format(rewardAmount)));
		}

		if (rewardItems.equals("none"))
			return;
		String[] items = rewardItems.split(",");
		for (int i = 0; i < items.length; ++i) {
			ItemStack stack = getItemStackFromString(items[i]);
			try {
				player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
			} catch (Exception e) {
				tellPlayer(player,PVPArena.lang.parse("invfull"));
				return;
			}
		}
	}

	/*
	 * read a string and return a valid item id
	 */
	private ItemStack getItemStackFromString(String s) {
		
		// [itemid/name]~[dmg]~[data]:[amount]
		
		short dmg = 0;
		byte data = 0;
		int amount = 1;
		Material mat = null;
		
		String[] temp = s.split(":");
		
		if (temp.length > 1) {
			amount = Integer.parseInt(temp[1]);
		}
		temp = temp[0].split("~");

		mat = parseMat(temp[0]);
		if (temp.length == 1) {
			//[itemid/name]:[amount]
			return new ItemStack(mat, amount);
		}
		dmg = Short.parseShort(temp[1]);
		if (temp.length == 2) {
			//[itemid/name]~[dmg]:[amount]
			return new ItemStack(mat, amount, dmg);
		}
		data = Byte.parseByte(temp[2]);
		if (temp.length == 3) {
			//[itemid/name]~[dmg]~[data]:[amount]
			return new ItemStack(mat, amount, dmg, data);
		}
		db.w("unrecognized itemstack: " + s);
		return null;
	}
	
	private Material parseMat(String string) {
		Material mat;
		try {
			mat = Material.getMaterial(Integer.parseInt(string));
			if (mat == null) {
				mat = Material.getMaterial(string);
			}
		} catch (Exception e) {
			mat = Material.getMaterial(string);
		}
		if (mat == null) {
			db.w("unrecognized material: "+string);
		}
		return mat;
	}

	/*
	 * remove all entities from an arena region
	 */
	public void clearArena() {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		};
		if (config.get("protection.regions") == null) {
			db.i("Region not set, skipping 1!");
			return;
		} else if (regions.get("battlefield") == null) {
			db.i("Region not set, skipping 2!");
			return;
		}
		World world = regions.get("battlefield").getWorld();
		for (Entity e : world.getEntities()) {
			if (((!(e instanceof Item)) && (!(e instanceof Arrow))) || (!(regions.get("battlefield").contains(e.getLocation().toVector()))))
				continue;
			e.remove();
		}
	}

	/*
	 * teleport a given player to the given coord string
	 */
	public void tpPlayerToCoordName(Player player, String place) {
		String color = "";
		if (place.endsWith("lounge")) {
			if (place.equals("lounge"))
				color = "&f";
			else {
				color = place.replace("lounge", "");
				color = "&" + Integer.toString(ChatColor.valueOf((String) paTeams.get(color)).getCode(), 16).toLowerCase();
			}
		}
		if (!color.equals(""))
			PVPArena.colorizePlayer(player, color);
		
		paPlayersTelePass.put(player.getName(), "yes");
		player.teleport(getCoords(place));
		paPlayersTelePass.remove(player.getName());
	}

	/*
	 * return "vector is inside an arena region"
	 */
	public boolean contains(Vector pt) {

		if (regions.get("battlefield") != null) {
			if (regions.get("battlefield").contains(pt)) {
				return true;
			}
		}
		if (checkExitRegion && regions.get("exit") != null) {
			if (regions.get("exit").contains(pt)) {
				return true;
			}
		}
		if (checkSpectatorRegion && regions.get("spectator") != null) {
			if (regions.get("spectator").contains(pt)) {
				return true;
			}
		}
		if (!checkLoungesRegion) {
			return false;
		}
		
		for (PARegion reg : regions.values()) {
			if (reg.contains(pt)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * return "only one player/team alive"
	 * 
	 * - if only one player/team is alive:
	 *   - announce winning team
	 *   - teleport everyone out
	 *   - give rewards
	 *   - check for bets won
	 */
	public boolean checkEndAndCommit() {
		if (!this.fightInProgress)
			return false;
		List<String> activeteams = new ArrayList<String>(0);
		String team = "";
		for (String sTeam : paPlayersTeam.keySet()) {
			if (activeteams.size() < 1) {
				// fresh map
				team = paPlayersTeam.get(sTeam);
				activeteams.add(team);
				db.i("team set to " + team);
			} else {
				// map contains stuff
				if (!activeteams.contains(paPlayersTeam.get(sTeam))) {
					// second team active => OUT!
					return false;
				}
			}
		}
		tellEveryone(PVPArena.lang.parse("teamhaswon",ChatColor.valueOf((String) paTeams.get(team)) + "Team " + team));
		
		Set<String> set = paPlayersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			
			Player z = Bukkit.getServer().getPlayer(o.toString());
			if (paPlayersClass.get(z.getName()).equals(team)) {
				StatsManager.addWinStat(z, team, this);
				resetPlayer(z, sTPwin);
				giveRewards(z); // if we are the winning team, give reward!
			} else {
				StatsManager.addLoseStat(z, team, this);
				resetPlayer(z, sTPlose);
			}
			paPlayersClass.remove(z.getName());
		}

		if (PVPArena.getMethod() != null) {
			for (String nKey : paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");
				
				if (paTeams.get(nSplit[1]) == null || paTeams.get(nSplit[1]).equals("free"))
					continue;
				
				if (nSplit[1].equalsIgnoreCase(team)) {
					double amount = paPlayersBetAmount.get(nKey)*2;

					MethodAccount ma = PVPArena.getMethod().getAccount(nSplit[0]);
					if (ma == null) {
						db.s("Account not found: "+nSplit[0]);
						return true;
					}
					ma.add(amount);
					try {
						tellPlayer(Bukkit.getPlayer(nSplit[0]), PVPArena.lang.parse("youwon",PVPArena.getMethod().format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}				
			}			
		}	
		reset();
		return true;
	}
	
	/*
	 * remove a player 
	 */
	public void removePlayer(Player player, String tploc) {
		resetPlayer(player, tploc);		
		paPlayersTeam.remove(player.getName());
		paPlayersClass.remove(player.getName());
		cleanPlayerSign(player.getName());
	}

	/*
	 * player reset function
	 * 
	 * - load player vars
	 * - teleport player back
	 * - reset inventory
	 */
	@SuppressWarnings("unchecked")
	public void resetPlayer(Player player, String string) {
		db.i("resetting player: "+player.getName());
		HashMap<String, String> tSM = (HashMap<String, String>) savedPlayerVars.get(player);
		if (tSM != null) {
			try {
				player.setExhaustion(Float.parseFloat(tSM.get("EXHAUSTION")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid EXHAUSTION entry!");
			}
			try {
				player.setFireTicks(Integer.parseInt(tSM.get("FIRETICKS")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid FIRETICKS entry!");
			}
			try {
				player.setFoodLevel(Integer.parseInt(tSM.get("FOODLEVEL")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid FOODLEVEL entry!");
			}
			try {
				player.setHealth(Integer.parseInt(tSM.get("HEALTH")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid HEALTH entry!");
			}
			try {
				player.setSaturation(Float.parseFloat(tSM.get("SATURATION")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid SATURATION entry!");
			}
			try {
				player.setGameMode(GameMode.getByValue(Integer.parseInt(tSM.get("GAMEMODE"))));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid EXHAUSTION entry!");
			}
			paPlayersTelePass.put(player.getName(), "yes");
			db.i("string = "+ string);
			if (string.equalsIgnoreCase("old")) {
				try {
					String sLoc = tSM.get("LOCATION");
					String[] aLoc = sLoc.split("/");
					Location lLoc = new Location(Bukkit.getWorld(aLoc[0]), Double.parseDouble(aLoc[1]), Double.parseDouble(aLoc[2]), Double.parseDouble(aLoc[3]));
					player.teleport(lLoc);
				} catch (Exception e) {
					System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid LOCATION entry!");
				}
			} else {
				Location l = getCoords(string);
				player.teleport(l);
			}
			paPlayersTelePass.remove(player.getName());
			savedPlayerVars.remove(player);
		} else {
			System.out.println("[PVP Arena] player '" + player.getName() + "' had no savedmisc entries!");
		}
		PVPArena.colorizePlayer(player, "");
		String sClass = "exit";
		if (paPlayersRespawn.get(player.getName()) != null) {
			sClass = paPlayersRespawn.get(player.getName());
		} else if (paPlayersClass.get(player.getName()) != null) {
			sClass = paPlayersClass.get(player.getName());
		}
		if (!sClass.equalsIgnoreCase("custom")) {
			clearInventory(player);
			loadInventory(player);
		}
	}

	/*
	 * force an arena to stop
	 */
	public void forcestop() {
		Set<String> set = paPlayersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = Bukkit.getServer().getPlayer(o.toString());
			removePlayer(z, "spectator");
		}
		reset();
		paPlayersClass.clear();
	}

	/*
	 * reset an arena
	 */
	public void reset() {
		cleanSigns();
		clearArena();
		fightInProgress = false;
		paPlayersTeam.clear();
		paPlayersLives.clear();
		paPlayersBetAmount.clear();
		paSignsLocation.clear();
	}
	
	/*
	 * stick a player into a team, based on calcFreeTeam
	 */
	public void chooseColor(Player player) {
		if (!(paPlayersTeam.containsKey(player.getName()))) {
			String team = calcFreeTeam();
			db.i("team found: " + team);
			tpPlayerToCoordName(player, team + "lounge");
			paPlayersTeam.put(player.getName(), team);
			tellPlayer(player, PVPArena.lang.parse("youjoined", ChatColor.valueOf((String) paTeams.get(team)) + team));
			tellEveryoneExcept(player, PVPArena.lang.parse("playerjoined", player.getName(), ChatColor.valueOf((String) paTeams.get(team)) + team));
		} else {
			tellPlayer(player, PVPArena.lang.parse("alreadyjoined"));
		}
	}
	
	/*
	 * calculate a team that needs a player
	 */
	private String calcFreeTeam() {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		
		// spam the available teams into a map counting the members
		for (String team : paPlayersTeam.values()) {
			if (!counts.containsKey(team)) {
				counts.put(team, 1);
				db.i("team " + team + " found");
			} else {
				int i = counts.get(team);
				counts.put(team, ++i);
				db.i("team " + team + " updated to " + i);
			}
		}
		// counts: TEAMNAME, TEAMPLAYERCOUNT
		db.i("counts now has size " + counts.size());
		List<String> notmax = new ArrayList<String>(0);
		
		int lastInt = -1;
		String lastStr = "";
		
		// add teams to notmax that don't need players because they have more than others
		for (String team : counts.keySet()) {
			if (lastInt == -1) {
				lastStr = team;
				lastInt = counts.get(team);
				db.i("first team found: " + team);
				continue;
			}
			int thisInt = counts.get(team);
			db.i("next team found: " + team);
			if (thisInt < lastInt) {
				// this team has space!
				notmax.add(team);
				db.i("this team has space: " + team);
				lastStr = team;
				lastInt = counts.get(team);
			} else if (thisInt > lastInt) {
				// last team had space
				notmax.add(lastStr);
				db.i("last team had space: " + lastStr);
				lastStr = team;
				lastInt = counts.get(team);
			}
		}
		// notmax: TEAMNAME
		if (notmax.size() < 1) { // no team added
			db.i("notmax < 1");
			if (counts.size() != 1) {
				// empty or equal => add all teams!
				db.i("lastStr empty");
				for (String xxx : paTeams.keySet())
					notmax.add(xxx);
			} else {
				// notmax empty because first team was the only team
				db.i("only one team! reverting!");

				List<String> max = new ArrayList<String>();
				
				for (String xxx : paTeams.keySet())
					if (!lastStr.equals(xxx)) {
						max.add(xxx);
						db.i("adding to max: " + xxx);
					}
				max.remove(lastStr);
				
				

				db.i("revert done, commit! " + max.size());
				Random r = new Random();
				
				int rand = r.nextInt(max.size());
				
				Iterator<String> itt = max.iterator();
				while (itt.hasNext()) {
					String s = itt.next();
					if (rand-- == 0) {
						return s;
					}
				}
				return null;
			}
		}
		// commit notmax selection
		
		db.i("no revert, commit! " + notmax.size());
		Random r = new Random();
		
		int rand = r.nextInt(notmax.size());
		
		Iterator<String> itt = notmax.iterator();
		while (itt.hasNext()) {
			String s = itt.next();
			if (rand-- == 0) {
				return s;
			}
		}
		db.i("error - returning null");
		return null;
	}

	/*
	 * parse the onCommand variables
	 * 
	 * - /pa
	 * - /pa [enable|disable|reload|list]
	 * - /pa [watch|bet|teams|users]
	 * - admin commands
	 * - region stuff
	 */
	public boolean parseCommand(Player player, String[] args) {
		if (!enabled && !PVPArena.hasAdminPerms(player)) {
			PVPArena.lang.parse("arenadisabled");
			return true;
		}
		db.i("parsing command: " + db.formatStringArray(args));
		
		if (args == null || args.length < 1) {
			// just /pa or /pvparena
			String error = isSetup();
			if (error != null) {
				tellPlayer(player, PVPArena.lang.parse("arenanotsetup",error));
				return true;
			}
			if (!PVPArena.hasPerms(player)) {
				tellPlayer(player, PVPArena.lang.parse("permjoin"));
				return true;
			}
			if (!randomlySelectTeams) {
				tellPlayer(player, PVPArena.lang.parse("selectteam"));
				return true;
			}
			if (savedPlayerVars.containsKey(player)) {
				tellPlayer(player, PVPArena.lang.parse("alreadyjoined"));
				return true;
			}
			if (fightInProgress) {
				tellPlayer(player, PVPArena.lang.parse("fightinprogress"));
				return true;
			}
			if (tooFarAway(player)) {
				tellPlayer(player, PVPArena.lang.parse("joinrange"));
				return true;
			}
			if (PVPArena.getMethod() != null) {
				MethodAccount ma = PVPArena.getMethod().getAccount(player.getName());
				if (ma == null) {
					db.s("Account not found: "+player.getName());
					return true;
				}
				if(!ma.hasEnough(entryFee)){
					// no money, no entry!
					tellPlayer(player, PVPArena.lang.parse("notenough", PVPArena.getMethod().format(entryFee)));
					return true;
	            }
			}
			
			prepare(player);
			paPlayersLives.put(player.getName(), (byte) maxLives);
			if ((PVPArena.getMethod() != null) && (entryFee > 0)) {
				MethodAccount ma = PVPArena.getMethod().getAccount(player.getName());
				ma.subtract(entryFee);
			}
			chooseColor(player);
			prepareInventory(player);
			return true;
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("enable")) {
				if (!PVPArena.hasAdminPerms(player)) {
					tellPlayer(player, PVPArena.lang.parse("nopermto", PVPArena.lang.parse("enable")));
					return true;
				}
				enabled = true;
				tellPlayer(player, PVPArena.lang.parse("enabled"));
				return true;
			} else if (args[0].equalsIgnoreCase("disable")) {
				if (!PVPArena.hasAdminPerms(player)) {
					tellPlayer(player, PVPArena.lang.parse("nopermto", PVPArena.lang.parse("disable")));
					return true;
				}
				enabled = false;
				tellPlayer(player, PVPArena.lang.parse("disabled"));
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!PVPArena.hasAdminPerms(player)) {
					tellPlayer(player, PVPArena.lang.parse("nopermto", PVPArena.lang.parse("reload")));
					return true;
				}
				plugin.load_config();
				tellPlayer(player, PVPArena.lang.parse("reloaded"));
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				if ((paPlayersTeam == null) || (paPlayersTeam.size() < 1)) {
					tellPlayer(player, PVPArena.lang.parse("noplayer"));
					return true;
				}
				String plrs = "";
				for (String sPlayer : paPlayersTeam.keySet()) {
					if (!plrs.equals(""))
						plrs +=", ";
					plrs += ChatColor.valueOf((String) paTeams.get(paPlayersTeam.get(sPlayer))) + sPlayer + ChatColor.WHITE;
				}
				tellPlayer(player, PVPArena.lang.parse("players") + ": " + plrs);
				return true;
			} else if (args[0].equalsIgnoreCase("watch")) {

				String error = isSetup();
				if (error != null) {
					tellPlayer(player, PVPArena.lang.parse("arenanotsetup",error));
					return true;
				}
				if (paPlayersTeam.containsKey(player.getName())) {
					tellPlayer(player, PVPArena.lang.parse("alreadyjoined"));
					return true;
				}
				tpPlayerToCoordName(player, "spectator");
				tellPlayer(player, PVPArena.lang.parse("specwelcome"));
				return true;
			} else if (paTeams.get(args[0]) != null) {
				

				// /pa [team] or /pvparena [team]

				String error = isSetup();
				if (error != null) {
					tellPlayer(player, PVPArena.lang.parse("arenanotsetup",error));
					return true;
				}
				if (!PVPArena.hasPerms(player)) {
					tellPlayer(player, PVPArena.lang.parse("permjoin"));
					return true;
				}
				if (!(manuallySelectTeams)) {
					tellPlayer(player, PVPArena.lang.parse("notselectteam"));
					return true;
				}
				if (savedPlayerVars.containsKey(player)) {
					tellPlayer(player, PVPArena.lang.parse("alreadyjoined"));
					return true;
				}
				if (fightInProgress) {
					tellPlayer(player, PVPArena.lang.parse("fightinprogress"));
					return true;
				}
				if (tooFarAway(player)) {
					tellPlayer(player, PVPArena.lang.parse("joinrange"));
					return true;
				}
				
				if (PVPArena.getMethod() != null) {
					MethodAccount ma = PVPArena.getMethod().getAccount(player.getName());
					if (ma == null) {
						db.s("Account not found: "+player.getName());
						return true;
					}
					if(!ma.hasEnough(entryFee)){
						// no money, no entry!
						tellPlayer(player, PVPArena.lang.parse("notenough", PVPArena.getMethod().format(entryFee)));
						return true;
		            }
				}

				prepare(player);
				paPlayersLives.put(player.getName(), (byte) maxLives);
				
				if ((PVPArena.getMethod() != null) && (entryFee > 0)) {
					MethodAccount ma = PVPArena.getMethod().getAccount(player.getName());
					ma.subtract(entryFee);
				}

				tpPlayerToCoordName(player, args[0] + "lounge");
				paPlayersTeam.put(player.getName(), args[0]);
				tellPlayer(player, PVPArena.lang.parse("youjoined", ChatColor.valueOf((String) paTeams.get(args[0])) + args[0]));
				tellEveryoneExcept(player, PVPArena.lang.parse("playerjoined", player.getName(), ChatColor.valueOf((String) paTeams.get(args[0])) + args[0]));
				
			} else if (PVPArena.hasAdminPerms(player)) {
				return parseAdminCommand(args, player);
			} else {
				tellPlayer(player, PVPArena.lang.parse("invalidcmd","502"));
				return false;
			}
			return true;
		} else if (args.length == 3 && args[0].equalsIgnoreCase("bet")) {
			// /pa bet [name] [amount]
			if (!paPlayersTeam.containsKey(player.getName())) {
				tellPlayer(player, PVPArena.lang.parse("betnotyours"));
				return true;
			}
			
			if (PVPArena.getMethod() == null)
				return true;
			
			if (!(paTeams.get(args[1]) != null) && !paPlayersTeam.containsKey(args[1])) {
				tellPlayer(player, PVPArena.lang.parse("betoptions"));
				return true;
			}
			
			double amount = 0;
			
			try {
				amount = Double.parseDouble(args[2]);
			} catch (Exception e) {
				tellPlayer(player, PVPArena.lang.parse("invalidamount",args[2]));
				return true;
			}
			MethodAccount ma = PVPArena.getMethod().getAccount(player.getName());
			if (ma == null) {
				db.s("Account not found: "+player.getName());
				return true;
			}
			if(!ma.hasEnough(entryFee)){
				// no money, no entry!
				tellPlayer(player, PVPArena.lang.parse("notenough",PVPArena.getMethod().format(amount)));
				return true;
            }
			ma.subtract(amount);
			tellPlayer(player, PVPArena.lang.parse("betplaced", args[1]));
			paPlayersBetAmount.put(player.getName() + ":" + args[1], amount);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("teams")) {
			String team[] = StatsManager.getTeamStats(args[1], this).split(";");
			int i = 0;
			for (String sTeam : paTeams.keySet())
				player.sendMessage(PVPArena.lang.parse("teamstat", ChatColor.valueOf((String) paTeams.get(sTeam)) + sTeam, team[i++], team[i++]));
			return true;
		} else if (args[0].equalsIgnoreCase("users")) {
			// wins are suffixed with "_"
			Map<String, Integer> players = StatsManager.getPlayerStats(args[1], this);
			
			int wcount = 0;
	
			for (String name : players.keySet())
				if (name.endsWith("_"))
					wcount++;
			
			String[][] wins = new String[wcount][2];
			String[][] losses = new String[players.size()-wcount][2];
			int iw = 0;
			int il = 0;
		
			for (String name : players.keySet()) {
				if (name.endsWith("_")) {
					// playername_ => win
					wins[iw][0] = name.substring(0, name.length()-1);
					wins[iw++][1] = String.valueOf(players.get(name));
				} else {
					// playername => lose
					losses[il][0] = name;
					losses[il++][1] = String.valueOf(players.get(name));
				}
			}
			wins = sort(wins);
			losses = sort(losses);
			tellPlayer(player, PVPArena.lang.parse("top5wins"));
			
			for (int w=0; w<wins.length && w < 5 ; w++) {
				tellPlayer(player, wins[w][0] + ": " + wins[w][1] + " " + PVPArena.lang.parse("wins"));
			}
			
	
			tellPlayer(player, "------------");
			tellPlayer(player, PVPArena.lang.parse("top5lose"));
			
			for (int l=0; l<losses.length && l < 5 ; l++) {
				tellPlayer(player, losses[l][0] + ": " + losses[l][1] + " " + PVPArena.lang.parse("losses"));
			}
			return true;
		}
		
		if (!PVPArena.hasAdminPerms(player)) {
			tellPlayer(player, PVPArena.lang.parse("invalidcmd","503"));
			return false;
		}
		
		if ((args.length < 2) || (!args[0].equalsIgnoreCase("region"))) {
			tellPlayer(player, PVPArena.lang.parse("invalidcmd","504"));
			return false;
		}

		File f = new File("plugins/pvparena", "config_" + name + ".yml");
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		};
		
		if (args[1].equalsIgnoreCase("set")) {
			if (!Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArena.lang.parse("regionalreadybeingset", Arena.regionmodify));
				return true;
			}
			Arena.regionmodify = name;
			tellPlayer(player, PVPArena.lang.parse("regionset"));
			return true;
		} else if ((args[1].equalsIgnoreCase("modify"))
				|| (args[1].equalsIgnoreCase("edit"))) {
			if (!Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArena.lang.parse("regionalreadybeingset", Arena.regionmodify));
				return true;
			}
			if (config.get("protection.region") != null) {
				Arena.regionmodify = name;
				tellPlayer(player, PVPArena.lang.parse("regionmodify"));
			} else {
				tellPlayer(player, PVPArena.lang.parse("noregionset"));
			}
			return true;
		}
		if (args.length != 3) {
			tellPlayer(player, PVPArena.lang.parse("invalidcmd","505"));
			return false;
		}
		
		if (!checkRegionCommand(args[2])) {
			tellPlayer(player, PVPArena.lang.parse("invalidcmd","506"));
			return false;
		}
		
		if (args[1].equalsIgnoreCase("save")) {
			if (Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArena.lang.parse("regionnotbeingset", name));
				return true;
			}
			Vector v1 = pos1.toVector();
			Vector v2 = pos2.toVector();
			config.set("protection.regions."+args[2]+".min", v1.getX() + ", " + v1.getY() + ", " + v1.getZ());
			config.set("protection.regions."+args[2]+".max", v2.getX() + ", " + v2.getY() + ", " + v2.getZ());
			config.set("protection.regions."+args[2]+".world", player
					.getWorld().getName());
			regions.put(args[2], new PARegion(args[2], pos1, pos2));
			pos1 = null;
			pos2 = null;
			try {
				config.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Arena.regionmodify = "";
			tellPlayer(player, PVPArena.lang.parse("regionsaved"));
		} else if (args[1].equalsIgnoreCase("remove")) {
			if (config.get("protection.regions."+args[2]) != null) {
				config.set("protection.regions."+args[2], null);
				try {
					config.save(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Arena.regionmodify = "";
				tellPlayer(player, PVPArena.lang.parse("regionremoved"));
			} else {
				tellPlayer(player, PVPArena.lang.parse("regionnotremoved"));
			}

		}
		return true;
	}

	private boolean checkRegionCommand(String s) {
		db.i("checking region command: "+s);
		if (s.equals("exit") || s.equals("spectator") || s.equals("battlefield")) {
			return true;
		}
		if (this instanceof CTFArena) {
			if (s.equals("lounge")) {
				return true;
			}
		} else {
			for (String sName : paTeams.keySet()) {
				if (s.equals(sName + "lounge")) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * prepare a player by saving player values and setting every variable to starting values
	 */
	public void prepare(Player player) {
		db.i("preparing player: " + player.getName());
		saveMisc(player); // save player health, fire tick, hunger etc
		player.setHealth(20);
		player.setFireTicks(0);
		player.setFoodLevel(20);
		player.setSaturation(20);
		player.setExhaustion(0);
		player.setGameMode(GameMode.getByValue(0));
	}
	
	/*
	 * prepare a player inventory for arena start
	 */
	private void prepareInventory(Player player) {
		saveInventory(player);
		clearInventory(player);
	}
	
	/*
	 * returns "is player too far away"
	 */
	private boolean tooFarAway(Player player) {
		if (joinRange < 1)
			return false;
		
		if (regions.get("battlefield") == null)
			return false;
		
		if (!this.regions.get("battlefield").getWorld().equals(player.getWorld()))
			return true;

		db.i("checking join range");
		Vector bvmin = regions.get("battlefield").getMin().toVector();
		Vector bvmax = regions.get("battlefield").getMax().toVector();
		Vector bvdiff = (Vector) bvmin.getMidpoint(bvmax);
		
		return (joinRange < bvdiff.distance(player.getLocation().toVector()));
	}

	/*
	 * process administration commands
	 * 
	 * - check for known/required location names
	 *   - set locations
	 */
	boolean parseAdminCommand(String[] args, Player player) {

		db.i("parsing admin command: "+db.formatStringArray(args));
		if (args[0].equalsIgnoreCase("spectator")) {
			setCoords(player, "spectator");
			tellPlayer(player, PVPArena.lang.parse("setspectator"));
		} else if (args[0].equalsIgnoreCase("exit")) {
			setCoords(player, "exit");
			tellPlayer(player, PVPArena.lang.parse("setexit"));
		} else if (args[0].equalsIgnoreCase("forcestop")) {
			if (fightInProgress) {
				forcestop();
				tellPlayer(player, PVPArena.lang.parse("forcestop"));
			} else {
				tellPlayer(player, PVPArena.lang.parse("nofight"));
			}
		} else if (args[0].equalsIgnoreCase("forcestop")) {
			if (fightInProgress) {
				forcestop();
				tellPlayer(player, PVPArena.lang.parse("forcestop"));
			} else {
				tellPlayer(player, PVPArena.lang.parse("nofight"));
			}
		} else if (randomSpawn && (args[0].startsWith("spawn"))) {
			setCoords(player, args[0]);
			tellPlayer(player, PVPArena.lang.parse("setspawn", args[0]));
		} else {
			// no random or not trying to set custom spawn
			if ((!isLoungeCommand(args,player)) && (!isSpawnCommand(args, player)) && (!isCustomCommand(args,player))) {
				tellPlayer(player, PVPArena.lang.parse("invalidcmd","501"));
				return false;
			}
			// else: command lounge or spawn :)
		}
		return true;
	}

	public boolean isCustomCommand(String[] args, Player player) {
		return false;
	}

	/*
	 * returns "is spawn-set command"
	 */
	public boolean isSpawnCommand(String[] args, Player player) {
		if (args[0].endsWith("spawn")) {
			String sName = args[0].replace("spawn", "");
			if (paTeams.get(sName) == null)
				return false;

			setCoords(player, args[0]);
			tellPlayer(player, PVPArena.lang.parse("setspawn", sName));
			return true;
		}
		return false;
	}

	/*
	 * returns "is lounge-set command"
	 */
	public boolean isLoungeCommand(String[] args, Player player) {
		if (args[0].endsWith("lounge")) {
			String color = args[0].replace("lounge", "");
			if (paTeams.containsKey(color)) {
				setCoords(player, args[0]);
				tellPlayer(player, PVPArena.lang.parse("setlounge", color));
				return true;
			}
			tellPlayer(player, PVPArena.lang.parse("invalidcmd","506"));
			return true;
		}
		return false;
	}
	
	/*
	 * tell everyone on the server
	 */
	public static void tellPublic(String msg) {
		db.i("broadcast: "+msg);
		Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE + msg);
	}

	/*
	 * tell a specific player
	 */
	public static void tellPlayer(Player player, String msg) {
		db.i("@" + player.getName() + ": "+msg);
		player.sendMessage(ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE + msg);
	}

	/*
	 * clear a player's inventory
	 */
	public void clearInventory(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setBoots(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
	}

	/*
	 * take a string, sort it by first dimension and return it
	 */
	public static String[][] sort(String[][] x) {
		boolean undone=true;
		String temp;
		String itemp;
		
		while (undone){
			undone = false;
			for (int i=0; i < x.length-1; i++) 
				if (Integer.parseInt(x[i][1]) < Integer.parseInt(x[i+1][1])) {                      
					temp       = x[i][1];
					x[i][1]       = x[i+1][1];
					x[i+1][1]     = temp;
                    
					itemp       = x[i][0];
					x[i][0]       = x[i+1][0];
					x[i+1][0]     = itemp;
					undone = true;
				}          
		} 
		return x;
	}

	/*
	 * returns "are the team counts equal?"
	 */
	public boolean checkEven() {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		
		// count each team members
		for (String team : paPlayersTeam.values()) {
			if (!counts.containsKey(team)) {
				counts.put(team, 1);
			} else {
				int i = counts.get(team);
				counts.put(team, i);
			}
		}
		
		if (counts.size() < 1)
			return false; // noone there => not even
		
		int temp = -1;
		for (int i : counts.values()) {
			if (temp == -1) {
				temp = i;
				continue;
			}
			if (temp != i)
				return false; // different count => not even
		}
		return true; // every team as the same player count!
	}

	/*
	 * calculate Powerup item spawn
	 */
	public void calcPowerupSpawn() {
		db.i("committing");
		if (this.pm == null) 
			return;
		
		db.i("pm is not null");
		if (this.pm.puTotal.size() <= 0)
			return;
		
		db.i("totals are filled");
		Random r = new Random();
		int i = r.nextInt(this.pm.puTotal.size());

		for (Powerup p : this.pm.puTotal) {
			if (--i > 0)
				continue;
			commitPowerupItemSpawn(p.item);
			Arena.tellPublic(PVPArena.lang.parse("serverpowerup",p.name));
			return;
		}

	}

	/*
	 * commit the Powerup item spawn
	 */
	private void commitPowerupItemSpawn(Material item) {
		db.i("dropping item?");
		if (regions.get("battlefield") == null)
			return;
		Location pos1 = regions.get("battlefield").getMin();
		Location pos2 = regions.get("battlefield").getMax();
		
		db.i("dropping item");
		int diffx = (int) (pos1.getX() - pos2.getX());
		int diffy = (int) (pos1.getY() - pos2.getY());
		int diffz = (int) (pos1.getZ() - pos2.getZ());
		
		Random r = new Random();

		int posx = diffx==0?pos1.getBlockX():(int) ((diffx / Math.abs(diffx)) * r.nextInt(Math.abs(diffx)) + pos2.getX());
		int posy = diffy==0?pos1.getBlockY():(int) ((diffx / Math.abs(diffy)) * r.nextInt(Math.abs(diffy)) + pos2.getY());
		int posz = diffz==0?pos1.getBlockZ():(int) ((diffx / Math.abs(diffz)) * r.nextInt(Math.abs(diffz)) + pos2.getZ());
		
		pos1.getWorld().dropItem(new Location(pos1.getWorld(),posx,posy+1,posz), new ItemStack(item,1));
	}

	/*
	 * read and return location from player's player vars
	 */
	@SuppressWarnings("unchecked")
	public Location getPlayerOldLocation(Player player) {
		HashMap<String, String> tSM = (HashMap<String, String>) savedPlayerVars.get(player);
		if (tSM != null) {

			try {
				String sLoc = tSM.get("LOCATION");
				String[] aLoc = sLoc.split("/");
				Location lLoc = new Location(Bukkit.getWorld(aLoc[0]), Double.parseDouble(aLoc[1]), Double.parseDouble(aLoc[2]), Double.parseDouble(aLoc[3]));
				return lLoc;
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid LOCATION entry!");
			}
			
		} else {
			System.out.println("[PVP Arena] player '" + player.getName() + "' had no savedmisc entries!");
		}
		return null;
	}

	/*
	 * set place to player position
	 */
	public void setCoords(Player player, String place) {
		Location location = player.getLocation();
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		config.set("coords." + place + ".world", location.getWorld().getName());
		config.set("coords." + place + ".x",Double.valueOf(location.getX()));
		config.set("coords." + place + ".y",Double.valueOf(location.getY()));
		config.set("coords." + place + ".z",Double.valueOf(location.getZ()));
		config.set("coords." + place + ".yaw",Float.valueOf(location.getYaw()));
		config.set("coords." + place + ".pitch",Float.valueOf(location.getPitch()));
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * get location from place
	 */
	public Location getCoords(String place) {
		db.i("get coords: "+place);
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		if (place.equals("spawn")) {
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			HashMap<String, Object> coords = (HashMap<String, Object>) config.getConfigurationSection("coords").getValues(false);
			for (String name : coords.keySet())
				if (name.startsWith("spawn"))
					locs.put(i++, name);
					
			Random r = new Random();
			
			place = locs.get(r.nextInt(locs.size()));
		}
		if (config.get("coords." + place) == null)
			return null;
		Double x = config.getDouble("coords." + place + ".x", 0.0D);
		Double y = config.getDouble("coords." + place + ".y", 0.0D);
		Double z = config.getDouble("coords." + place + ".z", 0.0D);
		Float yaw = (float) config.getDouble("coords." + place + ".yaw");
		Float pitch = (float) config.getDouble("coords." + place + ".pitch");
		World world = Bukkit.getServer().getWorld(config.getString("coords." + place + ".world"));
		return new Location(world, x.doubleValue(), y.doubleValue(),z.doubleValue(), yaw.floatValue(), pitch.floatValue());
	}

	/*
	 * reset player variables and teleport to spawn
	 */
	public void respawnPlayer(Player player, byte lives) {

		player.setHealth(20);
		player.setFireTicks(0);
		player.setFoodLevel(20);
		player.setSaturation(20);
		player.setExhaustion(0);
		String sTeam = paPlayersTeam.get(player.getName());
		String color = (String) paTeams.get(sTeam);
		if (!randomSpawn && color != null && !paPlayersTeam.get(player.getName()).equals("free")) {
			tellEveryone(PVPArena.lang.parse("lostlife", ChatColor.valueOf(color) + player.getName() + ChatColor.YELLOW, String.valueOf(lives)));
			tpPlayerToCoordName(player, sTeam + "spawn");
		} else {
			tellEveryone(PVPArena.lang.parse("lostlife", ChatColor.WHITE + player.getName() + ChatColor.YELLOW, String.valueOf(lives)));
			tpPlayerToCoordName(player, "spawn");
		}
		paPlayersLives.put(player.getName(), lives);
	}

	/*
	 * returns "is no running arena interfering with THIS arena"
	 */
	public boolean checkRegions() {
		if (!this.checkRegions)
			return true;
		db.i("checking regions");
		
		return ArenaManager.checkRegions(this);
	}

	public boolean checkRegion(Arena arena) {
		if ((regions.get("battlefield") != null)
				&& (arena.regions.get("battlefield") != null)
				&& arena.regions.get("battlefield").getWorld().equals(
						this.regions.get("battlefield").getWorld()))
			return !arena.regions.get("battlefield").contains(regions.get("battlefield").getMin().toVector().midpoint(regions.get("battlefield").getMax().toVector()));
		
		return true;
	}
}
