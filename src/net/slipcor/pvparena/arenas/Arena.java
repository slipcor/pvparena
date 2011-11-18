package net.slipcor.pvparena.arenas;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

/*
 * Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.9 - Permissions, rewrite
 * 
 * history:
 *
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
	
	public Map<String, String> paTeams = new HashMap<String, String>();
	public PowerupManager pm;
	public String name = "default";
	public String powerupCause;
	public String sTPexit;
	public String sTPdeath;
	public Location pos1;
	public Location pos2;
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
	public boolean fightInProgress = false;
	public boolean enabled = true;
	public boolean randomSpawn = false;
	public boolean usesPowerups;
	public boolean usesProtection;
	
	int SPAWN_ID;

	/*************
	 * protected *
	 *************/
	
	protected int maxLives;
	protected String sTPwin;
	protected String sTPlose;
	protected File configFile;
	protected boolean forceWoolHead;
	
	/*******************
	 * private statics *
	 *******************/
	
	private static final List<Material> ARMORS_TYPE = new LinkedList<Material>();
	private static final List<Material> HELMETS_TYPE = new LinkedList<Material>();
	private static final List<Material> CHESTPLATES_TYPE = new LinkedList<Material>();
	private static final List<Material> LEGGINGS_TYPE = new LinkedList<Material>();
	private static final List<Material> BOOTS_TYPE = new LinkedList<Material>();
	private static final DebugManager db = new DebugManager();
	
	/************
	 * privates *
	 ************/
	
	private final HashMap<Player, ItemStack[]> savedInventories = new HashMap<Player, ItemStack[]>();
	private final HashMap<Player, ItemStack[]> savedArmories = new HashMap<Player, ItemStack[]>();
	private final HashMap<Player, Object> savedPlayerVars = new HashMap<Player, Object>();
	private final HashMap<String, Double> paPlayersBetAmount = new HashMap<String, Double>();
	
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

		db.i("creating arena " + name);
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
	public Arena() {
		
	}

	/*
	 * parse the arena config
	 * 
	 * - read saved config file
	 * - apply default settings if necessary
	 * - read values out of the config
	 * - saveRegionPos()
	 */
	protected void configParse(String s) {
		Configuration config = new Configuration(configFile);
		config.load();

		if (config.getKeys("classes") == null) {
			config.setProperty("classes.Ranger.items","261,262:64,298,299,300,301");
			config.setProperty("classes.Swordsman.items", "276,306,307,308,309");
			config.setProperty("classes.Tank.items", "272,310,311,312,313");
			config.setProperty("classes.Pyro.items", "259,46:2,298,299,300,301");
		}
		if (config.getKeys("general") == null) {
			config.setProperty("general.readyblock","IRON_BLOCK");
			config.setProperty("general.lives",Integer.valueOf(3));
			config.setProperty("general.language","en");
			config.setProperty("general.tp.win","old"); // old || exit || spectator
			config.setProperty("general.tp.lose","old"); // old || exit || spectator
			config.setProperty("general.tp.exit","exit"); // old || exit || spectator
			config.setProperty("general.tp.death","spectator"); // old || exit || spectator
			config.setProperty("general.classperms",Boolean.valueOf(false)); // require permissions for a class
			if (!s.equals("free")) {
				config.setProperty("general.woolhead",Boolean.valueOf(false)); // enforce a wool head in case we dont have Spout installed
				config.setProperty("general.forceeven",Boolean.valueOf(false)); // require even teams
			}
		}
		if (config.getKeys("rewards") == null) {
			config.setProperty("rewards.entry-fee", Integer.valueOf(0));
			config.setProperty("rewards.amount", Integer.valueOf(0));
			config.setProperty("rewards.items", "none");
		}
		if (config.getKeys("protection") == null) {
			config.setProperty("protection.enabled", Boolean.valueOf(false));
			config.setProperty("protection.wand", Integer.valueOf(280));
			config.setProperty("protection.player.disable-block-placement",Boolean.valueOf(true));
			config.setProperty("protection.player.disable-block-damage",Boolean.valueOf(true));
			config.setProperty("protection.fire.disable-all-fire-spread",Boolean.valueOf(true));
			config.setProperty("protection.fire.disable-lava-fire-spread",Boolean.valueOf(true));
			config.setProperty("protection.ignition.block-tnt",Boolean.valueOf(true));
			config.setProperty("protection.ignition.block-lighter",Boolean.valueOf(true));
		}
		if (config.getProperty("general.randomSpawn") == null) {
			config.setProperty("general.randomSpawn",Boolean.valueOf(false));
		}
		if (config.getProperty("general.joinrange") == null) {
			config.setProperty("general.joinrange", Integer.valueOf(0));
			config.setProperty("general.powerups", "off"); // off | death:[diff] | time:[diff]
		}
		if (!s.equals("free") && config.getKeys("teams") == null) {
			config.setProperty("teams.team-killing-enabled",Boolean.valueOf(false));
			config.setProperty("teams.manually-select-teams",Boolean.valueOf(false));
		}
		if (config.getProperty("general.checkRegions") == null) {
			config.setProperty("general.checkRegions",Boolean.valueOf(false));
		}
		config.save();
		List<?> classes = config.getKeys("classes");
		paClasses.clear();
		for (int i = 0; i < classes.size(); ++i) {
			String className = (String) classes.get(i);
			paClasses.put(className,
					config.getString("classes." + className + ".items", null));
		}
		
		HashMap<String, Object> powerups = (HashMap<String, Object>) config.getProperty("powerups");

		if (powerups != null)
			pm = new PowerupManager(powerups);
		else
			db.i("no powerups loaded");
		
		entryFee = config.getInt("rewards.entry-fee", 0);
		rewardAmount = config.getInt("rewards.amount", 0);
		rewardItems = config.getString("rewards.items", "none");

		teamKilling = config.getBoolean("teams.team-killing-enabled", false);
		manuallySelectTeams = config.getBoolean("teams.manually-select-teams",false);

		usesProtection = config.getBoolean("protection.enabled", true);
		wand = config.getInt("protection.wand", 280);
		disableBlockPlacement = config.getBoolean("protection.player.disable-block-placement", true);
		disableBlockDamage = config.getBoolean("protection.player.disable-block-damage", true);
		disableAllFireSpread = config.getBoolean("protection.fire.disable-all-fire-spread", true);
		disableLavaFireSpread = config.getBoolean("protection.fire.disable-lava-fire-spread", true);
		blockTnt = config.getBoolean("protection.ignition.block-tnt", true);
		blockIgnite = config.getBoolean("protection.ignition.block-lighter",true);

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
		
		if (config.getString("protection.region.min") == null
				|| config.getString("protection.region.max") == null)
			return; // no arena, no container
		
		String[] min1 = config.getString("protection.region.min").split(", ");
		String[] max1 = config.getString("protection.region.max").split(", ");
		String world = config.getString("protection.region.world");
		Location min = new Location(Bukkit.getWorld(world), new Double(min1[0]).doubleValue(),
				new Double(min1[1]).doubleValue(),
				new Double(min1[2]).doubleValue());
		Location max = new Location(Bukkit.getWorld(world), new Double(max1[0]).doubleValue(),
				new Double(max1[1]).doubleValue(),
				new Double(max1[2]).doubleValue());
		pos1 = min;
		pos2 = max;
	}

	/*
	 * setup check
	 * 
	 * returns null if setup correct
	 * returns string if not
	 */
	public String isSetup() {
		Configuration config = new Configuration(configFile);
		config.load();
		if (config.getKeys("coords") == null) {
			return "coords";
		}
		if (pos1 == null || pos2 == null) {
			return "region";
		}

		List<String> list = config.getKeys("coords");
		if (randomSpawn) {
			// random spawn, we need the 2 that every arena has
			if (!list.contains("spectator"))
				return "spectator";
			if (!list.contains("exit"))
				return "exit";
			
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

		String[] items = rawItems.split(",");

		for (int i = 0; i < items.length; ++i) {
			String item = items[i];
			String[] itemDetail = item.split(":");
			if (itemDetail.length == 2) {
				int x = Integer.parseInt(itemDetail[0]);
				int y = Integer.parseInt(itemDetail[1]);
				ItemStack stack = new ItemStack(x, y);
				if (ARMORS_TYPE.contains(stack.getType())) {
					equipArmorPiece(stack, player.getInventory());
				} else {
					player.getInventory().addItem(new ItemStack[] { stack });
				}
			} else {
				int x = Integer.parseInt(itemDetail[0]);
				ItemStack stack = new ItemStack(x, 1);
				if (ARMORS_TYPE.contains(stack.getType())) {
					equipArmorPiece(stack, player.getInventory());
				} else {
					player.getInventory().addItem(new ItemStack[] { stack });
				}
			}
		}
		if (forceWoolHead) {
			String sTeam = paPlayersTeam.get(player.getName());
			String color = paTeams.get(sTeam);
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
		for (String p : paPlayersTeam.keySet()) {
			Player z = Bukkit.getServer().getPlayer(p.toString());
			z.sendMessage(PVPArena.lang.parse("msgprefix") + ChatColor.WHITE + msg);
		}
	}
	
	/*
	 * tell every fighting player except given player
	 */
	public void tellEveryoneExcept(Player player, String msg) {
		for (String p : paPlayersTeam.keySet()) {
			if (p.equals(player.getName()))
				continue;
			Player z = Bukkit.getServer().getPlayer(p.toString());
			z.sendMessage(PVPArena.lang.parse("msgprefix") + ChatColor.WHITE + msg);
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
		db.i("teleported!");
		if (usesPowerups) {
			db.i("using powerups : " + powerupCause + " : " + powerupDiff);
			if (powerupCause.equals("time") && powerupDiff > 0){
				db.i("everything ready. go for it!");
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
			String item = items[i];
			String[] itemDetail = item.split(":");
			if (itemDetail.length == 2) {
				int x = parseMat(itemDetail[0]);
				int y = Integer.parseInt(itemDetail[1]);
				ItemStack stack = new ItemStack(x, y);
				try {
					player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
				} catch (Exception e) {
					tellPlayer(player,PVPArena.lang.parse("invfull"));
					return;
				}
			} else {
				int x = parseMat(itemDetail[0]);
				ItemStack stack = new ItemStack(x, 1);
				try {
					player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
				} catch (Exception e) {
					tellPlayer(player,PVPArena.lang.parse("invfull"));
					return;
				}
			}
		}
	}

	/*
	 * read a string and return a valid item id
	 */
	//TODO: read and return durability!
	private int parseMat(String s) {
		try {
			int i = Integer.parseInt(s);
			return i;
		} catch (Exception e) {
			try {

				int j = Material.getMaterial(s).getId();
				return j;
			} catch (Exception e2) {
				return 0;
			}
		}
	}

	/*
	 * remove all entities from an arena region
	 */
	public void clearArena() {
		Configuration config = new Configuration(configFile);
		config.load();
		if (config.getKeys("protection.region") == null)
			return;
		World world = pos1.getWorld();
		for (Entity e : world.getEntities()) {
			if (((!(e instanceof Item)) && (!(e instanceof Arrow)))
					|| (!(contains(e.getLocation().toVector()))))
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
				color = "&" + Integer.toString(ChatColor.valueOf(paTeams.get(color)).getCode(), 16).toLowerCase();
			}
		}
		if (!color.equals(""))
			PVPArena.colorizePlayer(player, color);
		
		paPlayersTelePass.put(player.getName(), "yes");
		player.teleport(getCoords(place));
		paPlayersTelePass.remove(player.getName());
	}
	
	/*
	 * calculate and return the pos1 vector
	 */
	private Vector getMinimumPoint() {
		return new Vector(Math.min(pos1.getX(), pos2.getX()), Math.min(
				pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
	}

	/*
	 * calculate and return the pos2 vector
	 */
	private Vector getMaximumPoint() {
		return new Vector(Math.max(pos1.getX(), pos2.getX()), Math.max(
				pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
	}
	
	/*
	 * return "vector is inside the arena region"
	 */
	public boolean contains(Vector pt) {
		Configuration config = new Configuration(configFile);
		config.load();
		
		if (pos1 == null
				|| pos2 == null)
			return false; // no arena, no container
		
		return pt.isInAABB(pos1.toVector(), pos2.toVector());
		
		/*
		
		BlockVector min = (BlockVector) pos1.toVector();
		BlockVector max = (BlockVector) pos2.toVector();
		int x = pt.getBlockX();
		int y = pt.getBlockY();
		int z = pt.getBlockZ();
		
		return ((x >= min.getBlockX()) && (x <= max.getBlockX())
				&& (y >= min.getBlockY()) && (y <= max.getBlockY())
				&& (z >= min.getBlockZ()) && (z <= max.getBlockZ()));*/
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
		tellEveryone(PVPArena.lang.parse("teamhaswon",ChatColor.valueOf(paTeams.get(team)) + "Team " + team));
		
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
			tpPlayerToCoordName(player, team + "lounge");
			paPlayersTeam.put(player.getName(), team);
			tellPlayer(player, PVPArena.lang.parse("youjoined", ChatColor.valueOf(paTeams.get(team)) + team));
			tellEveryoneExcept(player, PVPArena.lang.parse("playerjoined", player.getName(), ChatColor.valueOf(paTeams.get(team)) + team));
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
		
		if (args.length < 1) {
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
			if (manuallySelectTeams) {
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
			
			if (isInventoryEmpty(player)) {
				if ((PVPArena.getMethod() != null) && (entryFee > 0)) {
					MethodAccount ma = PVPArena.getMethod().getAccount(player.getName());
					ma.subtract(entryFee);
				}
				chooseColor(player);
			} else {
				tellPlayer(player, PVPArena.lang.parse("alreadyjoined"));
			}
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
					plrs += ChatColor.valueOf(paTeams.get(paPlayersTeam.get(sPlayer))) + sPlayer + ChatColor.WHITE;
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
				
				if (isInventoryEmpty(player)) {
					if ((PVPArena.getMethod() != null) && (entryFee > 0)) {
						MethodAccount ma = PVPArena.getMethod().getAccount(player.getName());
						ma.subtract(entryFee);
					}

					tpPlayerToCoordName(player, args[0] + "lounge");
					paPlayersTeam.put(player.getName(), args[0]);
					tellPlayer(player, PVPArena.lang.parse("youjoined", ChatColor.valueOf(paTeams.get(args[0])) + args[0]));
					tellEveryoneExcept(player, PVPArena.lang.parse("playerjoined", player.getName(), ChatColor.valueOf(paTeams.get(args[0])) + args[0]));
					
				} else {
					tellPlayer(player, PVPArena.lang.parse("alreadyjoined"));
				}
			} else if (PVPArena.hasAdminPerms(player)) {
				return parseAdminCommand(args, player);
			} else {
				tellPlayer(player, PVPArena.lang.parse("invalidcmd","502"));
				return false;
			}
			return true;
		} else if (args.length == 3) {
			// /pa bet [name] [amount]
			if (!args[0].equalsIgnoreCase("bet")) {
				tellPlayer(player, PVPArena.lang.parse("invalidcmd","503"));
				return false;
			}
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
				player.sendMessage(PVPArena.lang.parse("teamstat", ChatColor.valueOf(paTeams.get(sTeam)) + sTeam, team[i++], team[i++]));
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
			tellPlayer(player, PVPArena.lang.parse("invalidcmd","504"));
			return false;
		}
		
		if ((args.length != 2) || (!args[0].equalsIgnoreCase("region"))) {
			tellPlayer(player, PVPArena.lang.parse("invalidcmd","505"));
			return false;
		}

		Configuration config = new Configuration(new File("plugins/pvparena", "config_" + name + ".yml"));
		config.load();
		
		if (args[1].equalsIgnoreCase("set")) {
			if (!Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArena.lang.parse("regionalreadybeingset", Arena.regionmodify));
				return true;
			}
			if (config.getKeys("protection.region") == null) {
				Arena.regionmodify = name;
				tellPlayer(player, PVPArena.lang.parse("regionset"));
			} else {
				tellPlayer(player, PVPArena.lang.parse("regionalreadyset"));
			}
		} else if ((args[1].equalsIgnoreCase("modify"))
				|| (args[1].equalsIgnoreCase("edit"))) {
			if (!Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArena.lang.parse("regionalreadybeingset", Arena.regionmodify));
				return true;
			}
			if (config.getKeys("protection.region") != null) {
				Arena.regionmodify = name;
				tellPlayer(player, PVPArena.lang.parse("regionmodify"));
			} else {
				tellPlayer(player, PVPArena.lang.parse("noregionset"));
			}
		} else if (args[1].equalsIgnoreCase("save")) {
			if (Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArena.lang.parse("regionnotbeingset", name));
				return true;
			}
			if ((pos1 == null) || (pos2 == null)) {
				tellPlayer(player, PVPArena.lang.parse("set2points"));
			} else {
				config.setProperty("protection.region.min",
						getMinimumPoint().getX() + ", "
								+ getMinimumPoint().getY() + ", "
								+ getMinimumPoint().getZ());
				config.setProperty("protection.region.max",
						getMaximumPoint().getX() + ", "
								+ getMaximumPoint().getY() + ", "
								+ getMaximumPoint().getZ());
				config.setProperty("protection.region.world", player
						.getWorld().getName());
				config.save();
				Arena.regionmodify = "";
				tellPlayer(player, PVPArena.lang.parse("regionsaved"));
			}
		} else if (args[1].equalsIgnoreCase("remove")) {
			if (config.getKeys("protection.region") != null) {
				config.removeProperty("protection.region");
				config.save();
				Arena.regionmodify = "";
				tellPlayer(player, PVPArena.lang.parse("regionremoved"));
			} else {
				tellPlayer(player, PVPArena.lang.parse("regionnotremoved"));
			}

		} else {
			tellPlayer(player, PVPArena.lang.parse("invalidcmd","506"));
			return false;
		}
		
		return true;
	}

	/*
	 * prepare a player by saving player values and setting every variable to starting values
	 */
	public void prepare(Player player) {
		saveInventory(player);
		clearInventory(player);
		saveMisc(player); // save player health, fire tick, hunger etc
		player.setHealth(20);
		player.setFireTicks(0);
		player.setFoodLevel(20);
		player.setSaturation(20);
		player.setExhaustion(0);
		player.setGameMode(GameMode.getByValue(0));
	}
	
	/*
	 * returns "is player too far away"
	 */
	private boolean tooFarAway(Player player) {
		if (joinRange < 1)
			return false;
		
		if (!this.pos1.getWorld().equals(player.getWorld()))
			return true;
		
		Vector bvmin = this.pos1.toVector();
		Vector bvmax = this.pos2.toVector();
		Vector bvdiff = (BlockVector) bvmin.getMidpoint(bvmax);
		
		return (joinRange < bvdiff.distance(player.getLocation().toVector()));
	}

	/*
	 * process administration commands
	 * 
	 * - check for known/required location names
	 *   - set locations
	 */
	boolean parseAdminCommand(String[] args, Player player) {

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
			if ((!isLoungeCommand(args,player)) && (!isSpawnCommand(args, player))) {
				tellPlayer(player, PVPArena.lang.parse("invalidcmd","501"));
				return false;
			}
			// else: command lounge or spawn :)
		}
		return true;
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
		Bukkit.getServer().broadcastMessage(PVPArena.lang.parse("msgprefix") + ChatColor.WHITE + msg);
	}

	/*
	 * tell a specific player
	 */
	public static void tellPlayer(Player player, String msg) {
		player.sendMessage(PVPArena.lang.parse("msgprefix") + ChatColor.WHITE + msg);
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
     * returns "player inventory is empty"
     */
	public static boolean isInventoryEmpty(Player player) {
		ItemStack[] invContents = player.getInventory().getContents();
		ItemStack[] armContents = player.getInventory().getArmorContents();
		int invNullCounter = 0;
		int armNullCounter = 0;
		for (int i = 0; i < invContents.length; ++i) {
			if (invContents[i] == null) {
				++invNullCounter;
			}
		}
		for (int i = 0; i < armContents.length; ++i) {
			if (armContents[i].getType() == Material.AIR) {
				++armNullCounter;
			}
		}
		return ((invNullCounter == invContents.length) && (armNullCounter == armContents.length));
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
		Configuration config = new Configuration(configFile);
		config.load();
		config.setProperty("coords." + place + ".world", location.getWorld()
				.getName());
		config.setProperty("coords." + place + ".x",
				Double.valueOf(location.getX()));
		config.setProperty("coords." + place + ".y",
				Double.valueOf(location.getY()));
		config.setProperty("coords." + place + ".z",
				Double.valueOf(location.getZ()));
		config.setProperty("coords." + place + ".yaw",
				Float.valueOf(location.getYaw()));
		config.setProperty("coords." + place + ".pitch",
				Float.valueOf(location.getPitch()));
		config.save();
	}
	
	/*
	 * get location from place
	 */
	@SuppressWarnings("unchecked")
	public Location getCoords(String place) {
		Configuration config = new Configuration(configFile);
		config.load();
		
		if (place.equals("spawn")) {
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			HashMap<String, Object> coords = (HashMap<String, Object>) config.getProperty("coords");
			for (String name : coords.keySet())
				if (name.startsWith("spawn"))
					locs.put(i++, name);
					
			Random r = new Random();
			
			place = locs.get(r.nextInt(locs.size()));
		}
		Double x = Double.valueOf(config.getDouble("coords." + place + ".x", 0.0D));
		Double y = Double.valueOf(config.getDouble("coords." + place + ".y", 0.0D));
		Double z = Double.valueOf(config.getDouble("coords." + place + ".z", 0.0D));
		Float yaw = new Float(config.getString("coords." + place + ".yaw"));
		Float pitch = new Float(config.getString("coords." + place + ".pitch"));
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
		String color = paTeams.get(sTeam);
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
		
		return ArenaManager.checkRegions(this);
	}

	public boolean checkRegion(Arena arena) {
		if (arena.pos1 != null && arena.pos1.getWorld().equals(this.pos1.getWorld()))
			return !arena.contains(pos1.toVector().midpoint(pos2.toVector()));
		
		return true;
	}
}
