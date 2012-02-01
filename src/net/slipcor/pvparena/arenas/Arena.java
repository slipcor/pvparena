package net.slipcor.pvparena.arenas;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.slipcor.pvparena.PAPlayer;
import net.slipcor.pvparena.PARegion;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.CommandManager;
import net.slipcor.pvparena.managers.ConfigManager;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.DebugManager;
import net.slipcor.pvparena.managers.PlayerManager;
import net.slipcor.pvparena.managers.PowerupManager;
import net.slipcor.pvparena.managers.SettingManager;
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
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.util.Vector;
import org.getspout.spoutapi.SpoutManager;

/**
 * arena class
 * 
 * -
 * 
 * contains >general< arena methods and variables
 * 
 * @author slipcor
 * 
 * @version v0.5.11
 * 
 */

public abstract class Arena {

	// global statics: region modify blocks all child arenas
	public static String regionmodify = "";

	// protected static: Debug manager (same for all child Arenas)
	protected static final DebugManager db = new DebugManager();

	// private statics: item definitions
	private static final List<Material> ARMORS_TYPE = new LinkedList<Material>();
	private static final List<Material> HELMETS_TYPE = new LinkedList<Material>();
	private static final List<Material> CHESTPLATES_TYPE = new LinkedList<Material>();
	private static final List<Material> LEGGINGS_TYPE = new LinkedList<Material>();
	private static final List<Material> BOOTS_TYPE = new LinkedList<Material>();
	
	/*
	 * arena maps, contain the arena data
	 */
	// available arena classes mapped to their items: ClassName => itemString
	public final HashMap<String, ItemStack[]> paClassItems = new HashMap<String, ItemStack[]>();
	// available teams mapped to color: TeamName => ColorString
	public final HashMap<String, String> paTeams = new HashMap<String, String>();
	// regions an arena has defined: RegionName => Region
	public final HashMap<String, PARegion> regions = new HashMap<String, PARegion>();
	public final HashSet<String> paReady = new HashSet<String>();
	public final HashSet<String> paChat = new HashSet<String>();

	public PowerupManager pm;
	public SettingManager sm;
	public PlayerManager playerManager = new PlayerManager();
	public String name = "default";
	public String owner = "%server%";

	public int powerupDiff; // powerup trigger cap
	public int powerupDiffI = 0; // powerup trigger count
	public int timed = 0; // timed arena? (<1 => false, else: limit in seconds)

	public Location pos1; // temporary position 1 (region select)
	public Location pos2; // temporary position 2 (region select)
	
	// arena status
	public boolean fightInProgress = false;

	// arena settings
	public boolean usesPowerups;
	public boolean preventDeath = true; // TODO: fix and change ^^

	// Runnable IDs
	int SPAWN_ID = -1;
	int END_ID = -1;

	public Config cfg;

	/*
	 * private variables
	 */
	private final HashMap<String, ItemStack[]> savedInventories = new HashMap<String, ItemStack[]>();
	private final HashMap<String, ItemStack[]> savedArmories = new HashMap<String, ItemStack[]>();
	private final HashMap<Player, PermissionAttachment> tempPermissions = new HashMap<Player, PermissionAttachment>();
	// player mapped to misc vars: PlayerName => miscObjectVars
	public final HashMap<Player, Object> savedPlayerVars = new HashMap<Player, Object>();

	// static filling of the items array
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

	/**
	 * basic arena constructor
	 * 
	 * @param name
	 *            the arena name
	 */
	public Arena(String name) {
		this.name = name;

		db.i("loading Arena " + name);

		cfg = new Config(new File("plugins/pvparena/config_" + name + ".yml"));
		cfg.load();
		ConfigManager.configParse(this, cfg);
	}

	/**
	 * empty arena constructor, used by sub-arenas
	 */
	public Arena() {
	}

	//
	// GENERAL
	//

	/**
	 * retrieve a material from a string
	 * 
	 * @param string
	 *            the string to parse
	 * @return the material
	 */
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
			db.w("unrecognized material: " + string);
		}
		return mat;
	}

	/**
	 * parse commands
	 * 
	 * @param player
	 *            the player committing the commands
	 * @param args
	 *            the command arguments
	 * @return false if the command help should be displayed, true otherwise
	 */
	public boolean parseCommand(Player player, String[] args) {
		if (!cfg.getBoolean("general.enabled") && !PVPArena.instance.hasAdminPerms(player) && !(PVPArena.instance.hasCreatePerms(player, this))) {
			PVPArena.lang.parse("arenadisabled");
			return true;
		}
		db.i("parsing command: " + db.formatStringArray(args));

		if (args == null || args.length < 1) {
			return CommandManager.parseJoin(this, player);
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("enable")) {
				return CommandManager.parseToggle(this, player, "enabled");
			} else if (args[0].equalsIgnoreCase("disable")) {
				return CommandManager.parseToggle(this, player, "disabled");
			} else if (args[0].equalsIgnoreCase("reload")) {
				return CommandManager.parseReload(player);
			} else if (args[0].equalsIgnoreCase("check")) {
				return CommandManager.parseCheck(this, player);
			} else if (args[0].equalsIgnoreCase("info")) {
				return CommandManager.parseInfo(this, player);
			} else if (args[0].equalsIgnoreCase("list")) {
				return CommandManager.parseList(this, player);
			} else if (args[0].equalsIgnoreCase("watch")) {
				return CommandManager.parseSpectate(this, player);
			} else if (args[0].equalsIgnoreCase("teams")) {
				return CommandManager.parseTeams(this, player);
			} else if (args[0].equalsIgnoreCase("users")) {
				return CommandManager.parseUsers(this, player);
			} else if (args[0].equalsIgnoreCase("chat")) {
				return CommandManager.parseChat(this, player);
			} else if (args[0].equalsIgnoreCase("region")) {
				return CommandManager.parseRegion(this, player);
			} else if (paTeams.get(args[0]) != null) {
				return CommandManager.parseJoinTeam(this, player, args[0]);
			} else if (PVPArena.instance.hasAdminPerms(player) || (PVPArena.instance.hasCreatePerms(player,this))) {
				return CommandManager.parseAdminCommand(this, player, args[0]);
			} else {
				return CommandManager.parseJoin(this, player);
			}
		} else if (args.length == 3 && args[0].equalsIgnoreCase("bet")) {
			return CommandManager.parseBetCommand(this, player, args);
		} else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
			// pa [name] set [node] [value]
			sm.set(player, args[1], args[2]);
			return true;
		} else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
			// pa [name] set [page]
			int i = 1;
			try {
				i = Integer.parseInt(args[1]);
			} catch (Exception e) {
				// nothing
			}
			sm.list(player, i);
			return true;
		}

		if (!PVPArena.instance.hasAdminPerms(player) && !(PVPArena.instance.hasCreatePerms(player,this))) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("nopermto", PVPArena.lang.parse("admin")));
			return false;
		}

		if (!checkRegionCommand(args[1])) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("invalidcmd", "504"));
			return false;
		}

		if (args.length == 2) {
			
			if (args[0].equalsIgnoreCase("region")) {
				
				// pa [name] region [regionname]
				if (Arena.regionmodify.equals("")) {
					ArenaManager.tellPlayer(player,
							PVPArena.lang.parse("regionnotbeingset", name));
					return true;
				}
	
				Vector realMin = new Vector(Math.min(pos1.getBlockX(),
						pos2.getBlockX()), Math.min(pos1.getBlockY(),
						pos2.getBlockY()), Math.min(pos1.getBlockZ(),
						pos2.getBlockZ()));
				Vector realMax = new Vector(Math.max(pos1.getBlockX(),
						pos2.getBlockX()), Math.max(pos1.getBlockY(),
						pos2.getBlockY()), Math.max(pos1.getBlockZ(),
						pos2.getBlockZ()));
	
				String s = realMin.getBlockX() + "," + realMin.getBlockY() + ","
						+ realMin.getBlockZ() + "," + realMax.getBlockX() + ","
						+ realMax.getBlockY() + "," + realMax.getBlockZ();
	
				cfg.set("regions." + args[1], s);
				regions.put(args[1], new PARegion(args[1], pos1, pos2));
				pos1 = null;
				pos2 = null;
				cfg.save();
	
				Arena.regionmodify = "";
				ArenaManager.tellPlayer(player, PVPArena.lang.parse("regionsaved"));
				return true;
				
			} else if (args[0].equalsIgnoreCase("remove")) {
				// pa [name] remove [spawnname]
				cfg.set("spawns." + args[1], null);
				cfg.save();
				ArenaManager.tellPlayer(player, PVPArena.lang.parse("spawnremoved", args[1]));
				return true;
			}
		}

		if (args.length != 3) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("invalidcmd", "505"));
			return false;
		}

		if (args[2].equalsIgnoreCase("remove")) {
			if (cfg.get("regions." + args[1]) != null) {
				cfg.set("regions." + args[1], null);
				cfg.save();
				Arena.regionmodify = "";
				ArenaManager.tellPlayer(player,
						PVPArena.lang.parse("regionremoved"));
			} else {
				ArenaManager.tellPlayer(player,
						PVPArena.lang.parse("regionnotremoved"));
			}

		}
		return true;
	}

	/**
	 * check if a given string is a valid region command
	 * 
	 * @param s
	 *            the string to check
	 * @return true if the command is valid, false otherwise
	 */
	private boolean checkRegionCommand(String s) {
		db.i("checking region command: " + s);
		if (s.equals("exit") || s.equals("spectator")
				|| s.equals("battlefield")) {
			return true;
		}
		if (this.getType().equals("free")) {
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

	/**
	 * is a player to far away to join?
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player is too far away, false otherwise
	 */
	public boolean tooFarAway(Player player) {
		int joinRange = cfg.getInt("general.joinrange", 0);
		if (joinRange < 1)
			return false;
		if (regions.get("battlefield") == null)
			return false;
		return regions.get("battlefield").tooFarAway(joinRange,
				player.getLocation());
	}

	/**
	 * check if other running arenas are interfering with this arena
	 * 
	 * @return true if no running arena is interfering with this arena, false
	 *         otherwise
	 */
	public boolean checkRegions() {
		if (!this.cfg.getBoolean("general.checkRegions", false))
			return true;
		db.i("checking regions");

		return ArenaManager.checkRegions(this);
	}

	/**
	 * check if an arena has overlapping battlefield region with another arena
	 * 
	 * @param arena
	 *            the arena to check
	 * @return true if it does not overlap, false otherwise
	 */
	public boolean checkRegion(Arena arena) {
		if ((regions.get("battlefield") != null)
				&& (arena.regions.get("battlefield") != null))
			return !arena.regions.get("battlefield").overlapsWith(
					regions.get("battlefield"));

		return true;
	}

	/**
	 * get the location from a coord string
	 * 
	 * @param place
	 *            the coord string
	 * @return the location of that string
	 */
	public Location getCoords(String place) {
		db.i("get coords: " + place);
		World world = Bukkit.getWorld(cfg.getString("general.world", Bukkit
				.getWorlds().get(0).getName()));
		if (place.equals("spawn")) {
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			db.i("searching for spawns");

			HashMap<String, Object> coords = (HashMap<String, Object>) cfg
					.getYamlConfiguration().getConfigurationSection("spawns")
					.getValues(false);
			for (String name : coords.keySet()) {
				if (name.startsWith(place)) {
					locs.put(i++, name);
					db.i("found match: " + name);
				}
			}

			Random r = new Random();

			place = locs.get(r.nextInt(locs.size()));
		}
		if (cfg.get("spawns." + place) == null) {
			if (!place.contains("spawn")) {
				db.i("place not found!");
				return null;
			}
			// no exact match: assume we have multiple spawnpoints
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			db.i("searching for team spawns");

			HashMap<String, Object> coords = (HashMap<String, Object>) cfg
					.getYamlConfiguration().getConfigurationSection("spawns")
					.getValues(false);
			for (String name : coords.keySet()) {
				if (name.startsWith(place)) {
					locs.put(i++, name);
					db.i("found match: " + name);
				}
			}

			if (locs.size() < 1) {
				return null;
			}
			Random r = new Random();

			place = locs.get(r.nextInt(locs.size()));
		}

		String sLoc = cfg.getString("spawns." + place, null);
		db.i("parsing location: " + sLoc);
		return Config.parseLocation(world, sLoc);
	}

	//
	// ARENA PREPARE
	//

	/**
	 * set an arena coord to a player's position
	 * 
	 * @param player
	 *            the player saving the coord
	 * @param place
	 *            the coord name to save the location to
	 */
	public void setCoords(Player player, String place) {
		// "x,y,z,yaw,pitch"

		Location location = player.getLocation();

		Integer x = location.getBlockX();
		Integer y = location.getBlockY();
		Integer z = location.getBlockZ();
		Float yaw = location.getYaw();
		Float pitch = location.getPitch();

		String s = x.toString() + "," + y.toString() + "," + z.toString() + ","
				+ yaw.toString() + "," + pitch.toString();

		cfg.set("spawns." + place, s);

		cfg.save();
	}

	//
	// ARENA START
	//

	/**
	 * supply a player with class items and eventually wool head
	 * 
	 * @param player
	 *            the player to supply
	 */
	public void givePlayerFightItems(Player player) {
		String playerClass = playerManager.getClass(player);
		db.i("giving items to player '" + player.getName() + "', class '"
				+ playerClass + "'");

		ItemStack[] items = paClassItems.get(playerClass);

		for (int i = 0; i < items.length; ++i) {
			ItemStack stack = items[i];
			if (ARMORS_TYPE.contains(stack.getType())) {
				equipArmorPiece(stack, player.getInventory());
			} else {
				player.getInventory().addItem(new ItemStack[] { stack });
			}
		}
		if (cfg.getBoolean("general.woolhead", false)) {
			String sTeam = playerManager.getTeam(player);
			String color = paTeams.get(sTeam);
			db.i("forcing woolhead: " + sTeam + "/" + color);
			player.getInventory().setHelmet(
					new ItemStack(Material.WOOL, 1,
							getColorShortFromColorENUM(color)));
		}
	}

	/**
	 * calculate a color short from a color enum
	 * 
	 * @param color
	 *            the string to parse
	 * @return the color short
	 */
	private short getColorShortFromColorENUM(String color) {

		/*
		 * DyeColor supports: WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME,
		 * PINK, GRAY, SILVER, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK;
		 */
		for (DyeColor dc : DyeColor.values()) {
			if (dc.name().equalsIgnoreCase(color))
				return dc.getData();
		}

		return (short) 0;
	}

	/**
	 * equip an armor item to the respective slot
	 * 
	 * @param stack
	 *            the item to equip
	 * @param inv
	 *            the player's inventory
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

	/**
	 * teleport all players to their respective spawn
	 */
	public void teleportAllToSpawn() {
		for (String p : playerManager.getPlayerTeamMap().keySet()) {
			Player z = Bukkit.getServer().getPlayer(p);
			if (!cfg.getBoolean("general.randomSpawn", false)) {
				tpPlayerToCoordName(z, playerManager.getPlayerTeamMap().get(p)
						+ "spawn");
			} else {
				tpPlayerToCoordName(z, "spawn");
			}
			setPermissions(z);
		}
		init_arena();

		if (timed > 0) {
			db.i("arena timing!");
			// initiate autosave timer
			END_ID = Bukkit
					.getServer()
					.getScheduler()
					.scheduleSyncRepeatingTask(PVPArena.instance,
							new TimedEndRunnable(this), timed * 20, timed * 20);
		}
		db.i("teleported everyone!");
		if (usesPowerups) {
			db.i("using powerups : " + cfg.getString("general.powerups", "off")
					+ " : " + powerupDiff);
			if (cfg.getString("general.powerups", "off").startsWith("time")
					&& powerupDiff > 0) {
				db.i("powerup time trigger!");
				powerupDiff = powerupDiff * 20; // calculate ticks to seconds
				// initiate autosave timer
				SPAWN_ID = Bukkit
						.getServer()
						.getScheduler()
						.scheduleSyncRepeatingTask(PVPArena.instance,
								new PowerupRunnable(this), powerupDiff,
								powerupDiff);
			}
		}

	}

	private void setPermissions(Player p) {
		HashMap<String,Boolean> perms = getTempPerms();
        if (perms == null || perms.isEmpty()) return;

        PermissionAttachment pa = p.addAttachment(PVPArena.instance);
        tempPermissions.put(p,pa);
        for (String entry : perms.keySet()) {
            pa.setPermission(entry, perms.get(entry));
        }
	}
	
	private void removePermissions(Player p) {
		if (tempPermissions.get(p) == null) return;
        
        for (PermissionAttachment pa : tempPermissions.values()) {
            if (pa != null) {
            	pa.remove();
            }
        }
	}
	
	/**
	 * get the permissions map
	 * @return
	 */
	private HashMap<String, Boolean> getTempPerms() {
		HashMap<String, Boolean> result = new HashMap<String, Boolean>();

		if (cfg.getYamlConfiguration().getConfigurationSection("perms.default") != null) {
			List<String> list = cfg.getStringList("perms.default", new ArrayList<String>());
			for (String key : list) {
				result.put(key.replace("-", "").replace("^", ""), (key.startsWith("^") || key.startsWith("-")));
			}
		}
		
		return result;
	}
	
	public boolean isCustomClassActive() {
		for (PAPlayer p : playerManager.getPlayers()) {
			if (p.getClass().equals("custom")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * dud method for CTF arena to override
	 */
	public void init_arena() {
		// nothing to see here
	}

	/**
	 * prepare a player's inventory, back it up and clear it
	 * 
	 * @param player
	 *            the player to save
	 */
	public void prepareInventory(Player player) {
		savedInventories.put(player.getName(), player.getInventory()
				.getContents());
		savedArmories.put(player.getName(), player.getInventory()
				.getArmorContents());
		clearInventory(player);
	}

	/**
	 * save player variables
	 * 
	 * @param player
	 *            the player to save
	 */
	public void saveMisc(Player player) {
		HashMap<String, String> tempMap = new HashMap<String, String>();

		Location lLoc = player.getLocation();
		String sLoc = lLoc.getWorld().getName() + "/" + lLoc.getBlockX() + "/"
				+ lLoc.getBlockY() + "/" + lLoc.getBlockZ() + "/";

		tempMap.put("EXHAUSTION", String.valueOf(player.getExhaustion()));
		tempMap.put("FIRETICKS", String.valueOf(player.getFireTicks()));
		tempMap.put("FOODLEVEL", String.valueOf(player.getFoodLevel()));
		tempMap.put("HEALTH", String.valueOf(player.getHealth()));
		tempMap.put("SATURATION", String.valueOf(player.getSaturation()));
		tempMap.put("LOCATION", sLoc);
		tempMap.put("GAMEMODE", String.valueOf(player.getGameMode().getValue()));
		if (cfg.getBoolean("general.colorNick", true)) {
			tempMap.put("DISPLAYNAME", player.getDisplayName());
		}
		savedPlayerVars.put(player, tempMap);
	}

	/**
	 * construct an itemstack out of a string
	 * 
	 * @param s
	 *            the formatted string: [itemid/name][~[dmg]]~[data]:[amount]
	 * @return the itemstack
	 */
	public ItemStack getItemStackFromString(String s, Player p) {

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
		if (mat != null) {
			if (temp.length == 1) {
				// [itemid/name]:[amount]
				return new ItemStack(mat, amount);
			}
			dmg = Short.parseShort(temp[1]);
			if (temp.length == 2) {
				// [itemid/name]~[dmg]:[amount]
				return new ItemStack(mat, amount, dmg);
			}
			data = Byte.parseByte(temp[2]);
			if (temp.length == 3) {
				// [itemid/name]~[dmg]~[data]:[amount]
				return new ItemStack(mat, amount, dmg, data);
			}
		}
		if (p == null) {
			db.w("unrecognized item: " + s);
		} else {
			ArenaManager.tellPlayer(p, "unrecognized item: " + s);
		}
		return null;
	}

	/**
	 * assign a player to a team
	 * 
	 * @param player
	 *            the player to assign
	 */
	public void chooseColor(Player player) {
		if (playerManager.getTeam(player).equals("")) {
			String team = calcFreeTeam();
			
			db.i("team found: " + team);
			tpPlayerToCoordName(player, team + "lounge");
			playerManager.setTeam(player, team);
			ArenaManager.tellPlayer(
					player,
					PVPArena.lang.parse("youjoined",
							ChatColor.valueOf(paTeams.get(team)) + team));
			playerManager.tellEveryoneExcept(player, PVPArena.lang.parse(
					"playerjoined", player.getName(),
					ChatColor.valueOf(paTeams.get(team)) + team));
		} else {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("alreadyjoined"));
		}
	}

	/**
	 * calculate the team that needs players the most
	 * 
	 * @return the team name
	 */
	public String calcFreeTeam() {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();

		// spam the available teams into a map counting the members
		for (String team : playerManager.getPlayerTeamMap().values()) {
			if (!counts.containsKey(team)) {
				counts.put(team, 1);
				db.i("team " + team + " found");
			} else {
				int i = counts.get(team);
				counts.put(team, ++i);
				db.i("team " + team + " updated to " + i);
			}
		}
		// counts contains TEAMNAME => PLAYERCOUNT
		
		if (counts.size() < paTeams.size()) {
			// there is a team without members, calculate one of those
			return returnEmptyTeam(counts.keySet());
		}
		
		boolean full = true;
		
		for (String s : paTeams.keySet()) {
			// check if we are full
			db.i("String s: "+s+"; max: "+cfg.getInt("general.readyMax"));
			if (counts.get(s) < cfg.getInt("general.readyMax") || cfg.getInt("general.readyMax") == 0) {
				full = false;
				break;
			}
		}
		
		if (full) {
			// full => OUT!
			return null;
		}
		
		HashSet<String> free = new HashSet<String>();
		
		int max = cfg.getInt("general.readyMaxTeam");
		max = max==0?Integer.MAX_VALUE:max;
		// calculate the max value down to the minimum
		for (String s : counts.keySet()) {
			int i = counts.get(s);
			if (i < max) {
				free.clear();
				free.add(s);
				max = i;
			} else if (i == max) {
				free.add(s);
			}
		}
		
		// free now has the minimum teams
		
		if (free.size() == 1) {
			for (String s : free) {
				return s;
			}
		}

		Random r = new Random();
		int rand = r.nextInt(free.size());
		for (String s : free) {
			if (rand-- == 0) {
				return s;
			}
		}
		
		return null;
	}
	
	/**
	 * return all empty teams
	 * @param set the set to search
	 * @return one empty team name
	 */
	private String returnEmptyTeam(Set<String> set) {
		HashSet<String> empty = new HashSet<String>();
		for (String s : paTeams.keySet()) {
			db.i("team: "+s);
			if (set.contains(s)) {
				db.i("done");
				continue;
			}
			empty.add(s);
		}
		db.i("empty.size: "+empty.size());
		if (empty.size() == 1) {
			for (String s : empty) {
				db.i("return: "+s);
				return s;
			}
		}

		Random r = new Random();
		int rand = r.nextInt(empty.size());
		for (String s : empty) {
			if (rand-- == 0) {
				return s;
			}
		}
		
		return null;
	}

	/**
	 * prepare a player for fighting. Setting all values to start value
	 * 
	 * @param player
	 */
	public void prepare(Player player) {
		db.i("preparing player: " + player.getName());
		saveMisc(player); // save player health, fire tick, hunger etc
		playersetHealth(player, cfg.getInt("general.startHealth", 0));
		player.setFireTicks(0);
		player.setFoodLevel(cfg.getInt("general.startFoodLevel", 20));
		player.setSaturation(cfg.getInt("general.startSaturation", 20));
		player.setExhaustion((float) cfg.getDouble("general.startExhaustion",
				0.0));
		player.setGameMode(GameMode.getByValue(0));
		playerManager.addPlayer(player);
	}

	/**
	 * health setting method. Implemented for heroes to work right
	 * 
	 * @param p
	 *            the player to set
	 * @param value
	 *            the health value
	 */
	protected void playersetHealth(Player p, int value) {
		if (Bukkit.getServer().getPluginManager().getPlugin("Heroes") == null) {
			p.setHealth(value);
		}
		int current = p.getHealth();
		int regain = value - current;
		
		EntityRegainHealthEvent event = new EntityRegainHealthEvent(p, regain,
				RegainReason.CUSTOM);
		Bukkit.getPluginManager().callEvent(event);
	}

	//
	// ARENA RUNTIME
	//

	/**
	 * teleport a given player to the given coord string
	 * 
	 * @param player
	 *            the player to teleport
	 * @param place
	 *            the coord string
	 */
	public void tpPlayerToCoordName(Player player, String place) {
		String color = "";
		if (place.endsWith("lounge")) {
			// at the start of the match
			if (cfg.getBoolean("general.defaultchat") && cfg.getBoolean("general.chat")) {
				paChat.add(player.getName());
			}
			if (place.equals("lounge"))
				color = "&f";
			else {
				color = place.replace("lounge", "");
				color = "&"
						+ Integer
								.toString(
										ChatColor.valueOf(paTeams.get(color))
												.getCode(), 16).toLowerCase();
			}
		}
		if (!color.equals("") && cfg.getBoolean("general.colorNick", true))
			colorizePlayer(player, color);

		playerManager.setTelePass(player, true);
		player.teleport(getCoords(place));
		playerManager.setTelePass(player, false);
	}

	/**
	 * is location inside one of our regions?
	 * 
	 * @param loc
	 *            the location to check
	 * @return true if the location is in one of our regions, false otherwise
	 */
	public boolean contains(Location loc) {
		Vector pt = loc.toVector();
		db.i("----------------CONTAINS-------------");
		db.i("checking for vector: x: " + pt.getBlockX() + ", y:"
				+ pt.getBlockY() + ", z: " + pt.getBlockZ());
		if (regions.get("battlefield") != null) {
			db.i("checking battlefield");
			if (regions.get("battlefield").contains(
					pt.toLocation(loc.getWorld()))) {
				return true;
			}
		}
		if (cfg.getBoolean("protection.checkExit", false)
				&& regions.get("exit") != null) {
			db.i("checking exit region");
			if (regions.get("exit").contains(pt.toLocation(loc.getWorld()))) {
				return true;
			}
		}
		if (cfg.getBoolean("protection.checkSpectator", false)
				&& regions.get("spectator") != null) {
			db.i("checking spectator region");
			if (regions.get("spectator")
					.contains(pt.toLocation(loc.getWorld()))) {
				return true;
			}
		}
		if (!cfg.getBoolean("protection.checkLounges", false)) {
			return false;
		}
		db.i("checking regions:");
		for (PARegion reg : regions.values()) {
			if (!reg.name.endsWith("lounge"))
				continue;

			db.i(" - " + reg.name);
			if (reg.contains(pt.toLocation(loc.getWorld()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * checks if the arena is over, if an end has to be committed
	 * 
	 * @return true if we ended the game just yet, false otherwise
	 */
	public boolean checkEndAndCommit() {
		if (!this.fightInProgress)
			return false;
		List<String> activeteams = new ArrayList<String>(0);
		String team = "";

		for (String sPlayer : playerManager.getPlayerTeamMap().keySet()) {
			if (activeteams.size() < 1) {
				// fresh map
				team = playerManager.getPlayerTeamMap().get(sPlayer);
				activeteams.add(team);
				db.i("team set to " + team);
			} else {
				// map contains stuff
				if (!activeteams.contains(playerManager.getPlayerTeamMap().get(
						sPlayer))) {
					// second team active => OUT!
					return false;
				}
			}
		}
		playerManager.tellEveryone(PVPArena.lang.parse("teamhaswon",
				ChatColor.valueOf(paTeams.get(team)) + "Team " + team));

		Set<String> set = playerManager.getPlayerTeamMap().keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			String sPlayer = iter.next();

			Player z = Bukkit.getServer().getPlayer(sPlayer);
			if (playerManager.getPlayerTeamMap().get(z.getName()).equals(team)) {
				StatsManager.addWinStat(z, team, this);
				resetPlayer(z, cfg.getString("tp.win", "old"));
				giveRewards(z); // if we are the winning team, give reward!
			} else {
				StatsManager.addLoseStat(z, team, this);
				resetPlayer(z, cfg.getString("tp.lose", "old"));
			}
			playerManager.setClass(z, "");
		}

		if (PVPArena.instance.getMethod() != null) {
			for (String nKey : playerManager.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (paTeams.get(nSplit[1]) == null
						|| paTeams.get(nSplit[1]).equals("free"))
					continue;

				if (nSplit[1].equalsIgnoreCase(team)) {
					double amount = playerManager.paPlayersBetAmount.get(nKey) * 2;

					MethodAccount ma = PVPArena.instance.getMethod()
							.getAccount(nSplit[0]);
					if (ma == null) {
						db.s("Account not found: " + nSplit[0]);
						return true;
					}
					ma.add(amount);
					try {
						ArenaManager.tellPlayer(Bukkit.getPlayer(nSplit[0]),
								PVPArena.lang.parse("youwon", PVPArena.instance
										.getMethod().format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}
			}
		}
		reset();
		return true;
	}

	/**
	 * remove a player from the arena
	 * 
	 * @param player
	 *            the player to reset
	 * @param tploc
	 *            the coord string to teleport the player to
	 */
	public void removePlayer(Player player, String tploc) {
		resetPlayer(player, tploc);
		playerManager.setTeam(player, "");
		playerManager.setClass(player, "");
		playerManager.remove(player);
	}

	/**
	 * reset a player to his pre-join values
	 * 
	 * @param player
	 * @param string
	 */
	@SuppressWarnings("unchecked")
	public void resetPlayer(Player player, String string) {
		db.i("resetting player: " + player.getName());
		HashMap<String, String> tSM = (HashMap<String, String>) savedPlayerVars
				.get(player);
		
		removePermissions(player);
		
		if (tSM == null) {
			db.w("------------");
			db.w("--hack fix--");
			db.w("------------");
			return;
		}

		try {
			player.setFireTicks(Integer.parseInt(tSM.get("FIRETICKS")));
		} catch (Exception e) {
			System.out.println("[PVP Arena] player '" + player.getName()
					+ "' had no valid FIRETICKS entry!");
		}
		try {
			player.setFoodLevel(Integer.parseInt(tSM.get("FOODLEVEL")));
		} catch (Exception e) {
			System.out.println("[PVP Arena] player '" + player.getName()
					+ "' had no valid FOODLEVEL entry!");
		}
		try {
			player.setHealth(Integer.parseInt(tSM.get("HEALTH")));
		} catch (Exception e) {
			System.out.println("[PVP Arena] player '" + player.getName()
					+ "' had no valid HEALTH entry!");
		}
		try {
			player.setSaturation(Float.parseFloat(tSM.get("SATURATION")));
		} catch (Exception e) {
			System.out.println("[PVP Arena] player '" + player.getName()
					+ "' had no valid SATURATION entry!");
		}
		try {
			player.setGameMode(GameMode.getByValue(Integer.parseInt(tSM
					.get("GAMEMODE"))));
		} catch (Exception e) {
			System.out.println("[PVP Arena] player '" + player.getName()
					+ "' had no valid EXHAUSTION entry!");
		}
		if (cfg.getBoolean("general.colorNick", true)) {
			try {
				player.setDisplayName(tSM.get("DISPLAYNAME"));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName()
						+ "' had no valid DISPLAYNAME entry!");
				colorizePlayer(player, "");
			}
		}
		playerManager.setTelePass(player, true);
		db.i("string = " + string);
		if (string.equalsIgnoreCase("old")) {
			try {
				String sLoc = tSM.get("LOCATION");
				String[] aLoc = sLoc.split("/");
				Location lLoc = new Location(Bukkit.getWorld(aLoc[0]),
						Double.parseDouble(aLoc[1]),
						Double.parseDouble(aLoc[2]),
						Double.parseDouble(aLoc[3]));
				player.teleport(lLoc);
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName()
						+ "' had no valid LOCATION entry!");
			}
		} else {
			Location l = getCoords(string);
			player.teleport(l);
		}
		playerManager.setTelePass(player, false);
		savedPlayerVars.remove(player);

		String sClass = "exit";
		if (playerManager.getRespawn(player) != null) {
			sClass = playerManager.getRespawn(player);
		} else if (!playerManager.getClass(player).equals("")) {
			sClass = playerManager.getClass(player);
		}
		if (!sClass.equalsIgnoreCase("custom")) {
			clearInventory(player);
			loadInventory(player);
		}
	}

	/**
	 * force stop an arena
	 */
	public void forcestop() {
		for (PAPlayer p : playerManager.getPlayers()) {
			removePlayer(p.getPlayer(), "spectator");
		}
		reset();
	}

	/**
	 * fully clear a player's inventory
	 * 
	 * @param player
	 *            the player to clear
	 */
	public void clearInventory(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setBoots(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
	}

	/**
	 * calculate a powerup and commit it
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
			ArenaManager.tellPublic(PVPArena.lang
					.parse("serverpowerup", p.name));
			return;
		}

	}

	/**
	 * commit the powerup item spawn
	 * 
	 * @param item
	 *            the material to spawn
	 */
	private void commitPowerupItemSpawn(Material item) {
		db.i("dropping item?");
		if (regions.get("battlefield") == null)
			return;
		regions.get("battlefield").dropItemRandom(item);
	}

	/**
	 * read the saved player location
	 * 
	 * @param player
	 *            the player to check
	 * @return the saved location
	 */
	@SuppressWarnings("unchecked")
	public Location getPlayerOldLocation(Player player) {
		HashMap<String, String> tSM = (HashMap<String, String>) savedPlayerVars
				.get(player);
		if (tSM != null) {

			try {
				String sLoc = tSM.get("LOCATION");
				String[] aLoc = sLoc.split("/");
				Location lLoc = new Location(Bukkit.getWorld(aLoc[0]),
						Double.parseDouble(aLoc[1]),
						Double.parseDouble(aLoc[2]),
						Double.parseDouble(aLoc[3]));
				return lLoc;
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName()
						+ "' had no valid LOCATION entry!");
			}

		} else {
			System.out.println("[PVP Arena] player '" + player.getName()
					+ "' had no savedmisc entries!");
		}
		return null;
	}

	/**
	 * reset player variables and teleport again
	 * 
	 * @param player
	 *            the player to access
	 * @param lives
	 *            the lives to set and display
	 */
	public void respawnPlayer(Player player, byte lives) {

		playersetHealth(player, cfg.getInt("general.startHealth", 0));
		player.setFireTicks(0);
		player.setFoodLevel(cfg.getInt("general.startFoodLevel", 20));
		player.setSaturation(cfg.getInt("general.startSaturation", 20));
		player.setExhaustion((float) cfg.getDouble("general.start", 0.0));

		if (cfg.getBoolean("general.refillInventory")
				&& !playerManager.getClass(player).equals("custom")) {
			clearInventory(player);
			givePlayerFightItems(player);
		}

		String sTeam = playerManager.getTeam(player);
		String color = paTeams.get(sTeam);
		if (!cfg.getBoolean("general.randomSpawn", false) && color != null
				&& !sTeam.equals("free")) {
			playerManager.tellEveryone(PVPArena.lang.parse("lostlife",
					ChatColor.valueOf(color) + player.getName()
							+ ChatColor.YELLOW, String.valueOf(lives)));
			tpPlayerToCoordName(player, sTeam + "spawn");
		} else {
			playerManager.tellEveryone(PVPArena.lang.parse("lostlife",
					ChatColor.WHITE + player.getName() + ChatColor.YELLOW,
					String.valueOf(lives)));
			tpPlayerToCoordName(player, "spawn");
		}
		playerManager.setLives(player, lives);
	}

	/**
	 * add a team color to a player name
	 * 
	 * @param player
	 *            the player to colorize
	 * @param color
	 *            the color string to parse
	 */
	public void colorizePlayer(Player player, String color) {
		if (color.equals("")) {
			player.setDisplayName(player.getName());

			if (PVPArena.instance.getSpoutHandler() != null)
				SpoutManager.getAppearanceManager().setGlobalTitle(player,
						player.getName());

			return;
		}

		String n = color + player.getName();

		player.setDisplayName(n.replaceAll("(&([a-f0-9]))", "§$2"));

		if (PVPArena.instance.getSpoutHandler() != null)
			SpoutManager.getAppearanceManager().setGlobalTitle(player,
					n.replaceAll("(&([a-f0-9]))", "§$2"));
	}

	/**
	 * end the arena due to timing
	 */
	public void timedEnd() {
		int iKills;
		int iDeaths;

		int max = -1;
		HashSet<String> result = new HashSet<String>();

		for (String sTeam : paTeams.keySet()) {
			iKills = 0;
			iDeaths = 0;

			try {
				iKills = playerManager.getKills(sTeam);
			} catch (Exception e) {
			}

			try {
				iDeaths = playerManager.getDeaths(sTeam);
			} catch (Exception e) {
			}

			if ((iKills - iDeaths) > max) {
				result = new HashSet<String>();
				result.add(sTeam);
			} else if ((iKills - iDeaths) == max) {
				result.add(sTeam);
			}
		}

		for (String team : result) {
			if (result.contains(team))
				playerManager.tellEveryone(PVPArena.lang.parse("teamhaswon",
						ChatColor.valueOf(paTeams.get(team)) + "Team " + team));

		}

		for (PAPlayer p : playerManager.getPlayers()) {

			Player z = p.getPlayer();
			if (result.contains(p.getTeam())) {
				StatsManager.addWinStat(z, p.getTeam(), this);
				resetPlayer(z, cfg.getString("tp.win", "old"));
				giveRewards(z); // if we are the winning team, give reward!
			} else {
				StatsManager.addLoseStat(z, p.getTeam(), this);
				resetPlayer(z, cfg.getString("tp.lose", "old"));
			}
			p.setClass(null);
		}

		if (PVPArena.instance.getMethod() != null) {
			for (String nKey : playerManager.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (paTeams.get(nSplit[1]) == null
						|| paTeams.get(nSplit[1]).equals("free"))
					continue;

				if (result.contains(nSplit[1])) {
					double amount = playerManager.paPlayersBetAmount.get(nKey) * 2;

					MethodAccount ma = PVPArena.instance.getMethod()
							.getAccount(nSplit[0]);
					if (ma == null) {
						db.s("Account not found: " + nSplit[0]);
						continue;
					}
					ma.add(amount);
					try {
						ArenaManager.tellPlayer(Bukkit.getPlayer(nSplit[0]),
								PVPArena.lang.parse("youwon", PVPArena.instance
										.getMethod().format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}
			}
		}
		reset();
	}

	//
	// ARENA CLEANUP
	//

	/**
	 * reload player inventories from saved variables
	 * 
	 * @param player
	 */
	public void loadInventory(Player player) {
		if (player == null) {
			PVPArena.instance.log.severe("player = null!");
			return;
		}
		if (player.getInventory() == null) {
			PVPArena.instance.log.severe("player.getInventory() = null!");
			return;
		}
		if (savedInventories == null) {
			PVPArena.instance.log.severe("savedInventories = null!");
			return;
		}
		if (savedInventories.get(player.getName()) == null) {
			PVPArena.instance.log.severe("savedInventories.get(" + player
					+ ") = null!");
			return;
		}
		player.getInventory().setContents(
				(ItemStack[]) savedInventories.get(player.getName()));
		player.getInventory().setArmorContents(
				(ItemStack[]) savedArmories.get(player.getName()));
	}

	/**
	 * give customized rewards to players
	 * 
	 * @param player
	 *            the player to give the reward
	 */
	public void giveRewards(Player player) {
		if (PVPArena.instance.getMethod() != null) {
			for (String nKey : playerManager.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (nSplit[1].equalsIgnoreCase(player.getName())) {
					double amount = playerManager.paPlayersBetAmount.get(nKey) * 4;

					MethodAccount ma = PVPArena.instance.getMethod()
							.getAccount(nSplit[0]);
					ma.add(amount);
					try {
						ArenaManager.tellPlayer(Bukkit.getPlayer(nSplit[0]),
								PVPArena.lang.parse("youwon", PVPArena.instance
										.getMethod().format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}
			}
		}

		if ((PVPArena.instance.getMethod() != null)
				&& (cfg.getInt("money.reward", 0) > 0)) {
			MethodAccount ma = PVPArena.instance.getMethod().getAccount(
					player.getName());
			ma.add(cfg.getInt("money.reward", 0));
			ArenaManager.tellPlayer(player, PVPArena.lang.parse(
					"awarded",
					PVPArena.instance.getMethod().format(
							cfg.getInt("money.reward", 0))));
		}
		String sItems = cfg.getString("general.item-rewards", "none");
		if (sItems.equals("none"))
			return;
		String[] items = sItems.split(",");
		for (int i = 0; i < items.length; ++i) {
			ItemStack stack = getItemStackFromString(items[i], null);
			try {
				player.getInventory().setItem(
						player.getInventory().firstEmpty(), stack);
			} catch (Exception e) {
				ArenaManager.tellPlayer(player, PVPArena.lang.parse("invfull"));
				return;
			}
		}
	}

	/**
	 * restore an arena if region is set
	 */
	public void clearArena() {
		if (cfg.get("regions") == null) {
			db.i("Region not set, skipping 1!");
			return;
		} else if (regions.get("battlefield") == null) {
			db.i("Region not set, skipping 2!");
			return;
		}
		regions.get("battlefield").restore();
	}

	/**
	 * reset an arena
	 */
	public void reset() {
		clearArena();
		paReady.clear();
		paChat.clear();
		fightInProgress = false;
		playerManager.reset(this);
		if (SPAWN_ID > -1)
			Bukkit.getScheduler().cancelTask(SPAWN_ID);
		SPAWN_ID = -1;
		if (END_ID > -1)
			Bukkit.getScheduler().cancelTask(END_ID);
		END_ID = -1;
	}

	/**
	 * return the arena type
	 * 
	 * @return the arena type name
	 */
	public String getType() {
		return "team";
	}

	/**
	 * return the arena world
	 * 
	 * @return the world name
	 */
	public String getWorld() {
		return cfg.getString("general.world");
	}

	/**
	 * set the arena world
	 * 
	 * @param sWorld
	 *            the world name
	 */
	public void setWorld(String sWorld) {
		cfg.set("general.world", sWorld);
		cfg.save();
	}
}
