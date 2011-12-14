/*
 * arena class
 * 
 * author: slipcor
 * 
 * version: v0.4.4 - Random spawns per team, not shared
 * 
 * history:
 * 
 *     v0.4.3 - max / min bet
 *     v0.4.1 - command manager, arena information and arena config check
 *     v0.4.0 - mayor rewrite, improved help
 *     v0.3.14 - timed arena modes
 *     v0.3.12 - set flag positions
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
 */

package net.slipcor.pvparena.arenas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.getspout.spoutapi.SpoutManager;

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
	public final HashMap<String, String> paClassItems = new HashMap<String, String>();
	// available teams mapped to color: TeamName => ColorString
	public final HashMap<String, String> paTeams = new HashMap<String, String>();
	// regions an arena has defined: RegionName => Region
	public final HashMap<String, PARegion> regions = new HashMap<String, PARegion>();

	public PowerupManager pm;
	public PlayerManager playerManager = new PlayerManager();
	public File configFile;
	public String name = "default";
	public String powerupTrigger; // either "kills" or "time"
	public String sTPexit; // teleport setting for leaving the arena
	public String sTPdeath; // teleport setting for dying
	public String sTPwin;
	public String sTPlose;
	public int powerupDiff; // powerup trigger cap
	public int powerupDiffI = 0; // powerup trigger count
	public int timed = 0; // timed arena? (<1 => false, else: limit in seconds)
	public int wand; // item id to setup the arena
	public int maxLives;
	public Location pos1; // temporary position 1 (region select)
	public Location pos2; // temporary position 2 (region select)

	// arena status
	public boolean enabled = true;
	public boolean fightInProgress = false;

	// arena settings
	public boolean usesPowerups;
	public boolean usesProtection;
	public boolean disableAllFireSpread;
	public boolean disableBlockPlacement;
	public boolean disableBlockDamage;
	public boolean disableIgnite;
	public boolean disableLavaFireSpread;
	public boolean disableTnt;
	public boolean forceEven;
	public boolean forceWoolHead;
	public boolean teamKilling;
	public boolean manuallySelectTeams;
	public boolean randomlySelectTeams;
	public boolean randomSpawn = false;
	public boolean checkExitRegion = false;
	public boolean checkSpectatorRegion = false;
	public boolean checkLoungesRegion = false;
	public boolean preventDeath = true;

	public String rewardItems;
	public int entryFee;
	public int rewardAmount;
	public int joinRange;
	public boolean checkRegions;

	public double maxbet = 0;
	public double minbet = 0;

	// Runnable IDs
	int SPAWN_ID = -1;
	int END_ID = -1;

	/*
	 * private variables
	 */
	private final HashMap<Player, ItemStack[]> savedInventories = new HashMap<Player, ItemStack[]>();
	private final HashMap<Player, ItemStack[]> savedArmories = new HashMap<Player, ItemStack[]>();
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

	/*
	 * Standard constructor
	 * 
	 * - hand over plugin instance and arena name - open or create a new
	 * configuration file - parse the arena config
	 */
	public Arena(String name) {
		this.name = name;

		db.i("loading Arena " + name);

		new File("plugins/pvparena").mkdir();
		configFile = new File("plugins/pvparena/config_" + name + ".yml");
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				PVPArena.lang.log_error("filecreateerror", "config_" + name);
			}
		ConfigManager.configParse(this, configFile);
	}

	/*
	 * Minumum constructor
	 * 
	 * used by the child arena types
	 */
	public Arena() {
	}

	//
	// GENERAL
	//

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

	/*
	 * parse the onCommand variables
	 * 
	 * - /pa - /pa [enable|disable|reload|list] - /pa [watch|bet|teams|users] -
	 * admin commands - region stuff
	 */
	public boolean parseCommand(Player player, String[] args) {
		if (!enabled && !PVPArena.instance.hasAdminPerms(player)) {
			PVPArena.lang.parse("arenadisabled");
			return true;
		}
		db.i("parsing command: " + db.formatStringArray(args));

		if (args == null || args.length < 1) {
			return CommandManager.parseJoin(this, player);
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("enable")) {
				return CommandManager.parseToggle(this, player, "enable");
			} else if (args[0].equalsIgnoreCase("disable")) {
				return CommandManager.parseToggle(this, player, "disable");
			} else if (args[0].equalsIgnoreCase("reload")) {
				return CommandManager.parseReload(player);
			} else if (args[0].equalsIgnoreCase("check")) {
				return CommandManager.parseCheck(this, player);
			} else if (args[0].equalsIgnoreCase("info")) {
				return CommandManager.parseInfo(this, player);
			} else if (args[0].equalsIgnoreCase("list")) {
				return CommandManager.parseList(this, player);
			} else if (args[0].equalsIgnoreCase("watch")) {
				return CommandManager.parseWatch(this, player);
			} else if (args[0].equalsIgnoreCase("teams")) {
				return CommandManager.parseTeams(this, player);
			} else if (args[0].equalsIgnoreCase("users")) {
				return CommandManager.parseUsers(this, player);
			} else if (args[0].equalsIgnoreCase("region")) {
				return CommandManager.parseRegion(this, player);
			} else if (paTeams.get(args[0]) != null) {
				return CommandManager.parseJoinTeam(this, player, args[0]);
			} else if (PVPArena.instance.hasAdminPerms(player)) {
				return CommandManager.parseAdminCommand(this, player, args[0]);
			} else {
				ArenaManager.tellPlayer(player,
						PVPArena.lang.parse("invalidcmd", "502"));
				return false;
			}
		} else if (args.length == 3 && args[0].equalsIgnoreCase("bet")) {
			return CommandManager.parseBetCommand(this, player, args);
		}

		if (!PVPArena.instance.hasAdminPerms(player)) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("invalidcmd", "503"));
			return false;
		}

		/*
		 * remaining commands: pa [name] region [regionname] pa [name] region
		 * remove [regionname]
		 */

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

		if (!checkRegionCommand(args[1])) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("invalidcmd", "504"));
			return false;
		}

		if (args.length == 2) {
			// pa [name] region [regionname]
			if (Arena.regionmodify.equals("")) {
				ArenaManager.tellPlayer(player,
						PVPArena.lang.parse("regionnotbeingset", name));
				return true;
			}
			
			Vector realMin = new Vector(
					Math.min(pos1.getBlockX(), pos2.getBlockX()),
					Math.min(pos1.getBlockY(), pos2.getBlockY()),
					Math.min(pos1.getBlockZ(), pos2.getBlockZ()));
			Vector realMax = new Vector(
					Math.max(pos1.getBlockX(), pos2.getBlockX()),
					Math.max(pos1.getBlockY(), pos2.getBlockY()),
					Math.max(pos1.getBlockZ(), pos2.getBlockZ()));

			config.set("protection.regions." + args[1] + ".min", realMin.getX()
					+ ", " + realMin.getY() + ", " + realMin.getZ());
			config.set("protection.regions." + args[1] + ".max", realMax.getX()
					+ ", " + realMax.getY() + ", " + realMax.getZ());
			config.set("protection.regions." + args[1] + ".world", player
					.getWorld().getName());
			regions.put(args[1], new PARegion(args[1], pos1, pos2));
			pos1 = null;
			pos2 = null;
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Arena.regionmodify = "";
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("regionsaved"));
			return true;
		}

		if (args.length != 3) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("invalidcmd", "505"));
			return false;
		}

		if (args[2].equalsIgnoreCase("remove")) {
			if (config.get("protection.regions." + args[1]) != null) {
				config.set("protection.regions." + args[1], null);
				try {
					config.save(configFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
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

	/*
	 * returns "is player too far away"
	 */
	public boolean tooFarAway(Player player) {
		if (joinRange < 1)
			return false;

		if (regions.get("battlefield") == null)
			return false;

		if (!this.regions.get("battlefield").getWorld()
				.equals(player.getWorld()))
			return true;

		db.i("checking join range");
		Vector bvmin = regions.get("battlefield").getMin().toVector();
		Vector bvmax = regions.get("battlefield").getMax().toVector();
		Vector bvdiff = (Vector) bvmin.getMidpoint(bvmax);

		return (joinRange < bvdiff.distance(player.getLocation().toVector()));
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
				&& arena.regions.get("battlefield").getWorld()
						.equals(this.regions.get("battlefield").getWorld()))
			return !arena.regions.get("battlefield").contains(
					regions.get("battlefield")
							.getMin()
							.toVector()
							.midpoint(
									regions.get("battlefield").getMax()
											.toVector()));

		return true;
	}

	/*
	 * get location from place
	 */
	public Location getCoords(String place) {
		db.i("get coords: " + place);
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

			db.i("searching for spawns");

			HashMap<String, Object> coords = (HashMap<String, Object>) config
					.getConfigurationSection("coords").getValues(false);
			for (String name : coords.keySet()) {
				if (name.startsWith(place)) {
					locs.put(i++, name);
					db.i("found match: "+name);
				}
			}

			Random r = new Random();

			place = locs.get(r.nextInt(locs.size()));
		}
		if (config.get("coords." + place) == null) {
			if (!place.contains("spawn")) {
				db.i("place not found!");
				return null;
			}
			//no exact match: assume we have multiple spawnpoints
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			db.i("searching for team spawns");
			
			HashMap<String, Object> coords = (HashMap<String, Object>) config
					.getConfigurationSection("coords").getValues(false);
			for (String name : coords.keySet()) {
				if (name.startsWith(place)) {
					locs.put(i++, name);
					db.i("found match: "+name);
				}
			}

			if (locs.size() < 1) {
				return null;
			}
			Random r = new Random();

			place = locs.get(r.nextInt(locs.size()));
		}
		Double x = config.getDouble("coords." + place + ".x", 0.0D);
		Double y = config.getDouble("coords." + place + ".y", 0.0D);
		Double z = config.getDouble("coords." + place + ".z", 0.0D);
		Float yaw = (float) config.getDouble("coords." + place + ".yaw");
		Float pitch = (float) config.getDouble("coords." + place + ".pitch");
		World world = Bukkit.getServer().getWorld(
				config.getString("coords." + place + ".world"));
		return new Location(world, x.doubleValue(), y.doubleValue(),
				z.doubleValue(), yaw.floatValue(), pitch.floatValue());
	}

	//
	// ARENA PREPARE
	//

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
		config.set("coords." + place + ".x", Double.valueOf(location.getX()));
		config.set("coords." + place + ".y", Double.valueOf(location.getY()));
		config.set("coords." + place + ".z", Double.valueOf(location.getZ()));
		config.set("coords." + place + ".yaw", Float.valueOf(location.getYaw()));
		config.set("coords." + place + ".pitch",
				Float.valueOf(location.getPitch()));
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * fetch overflow sign
	 * 
	 * (blank sign under class sign)
	 */
	public Sign getNext(Sign sign) {
		try {
			return (Sign) sign.getBlock().getRelative(BlockFace.DOWN)
					.getState();
		} catch (Exception e) {
			return null;
		}
	}

	//
	// ARENA START
	//

	/*
	 * give the player the items of his class if woolhead: replace head gear
	 * with colored wool
	 */
	public void givePlayerFightItems(Player player) {
		String playerClass = playerManager.getClass(player);
		String rawItems = paClassItems.get(playerClass);
		db.i("giving items '" + rawItems + "' to player '" + player.getName()
				+ "', class '" + playerClass + "'");

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
			String sTeam = playerManager.getTeam(player);
			String color = paTeams.get(sTeam);
			db.i("forcing woolhead: " + sTeam + "/" + color);
			player.getInventory().setHelmet(
					new ItemStack(Material.WOOL, 1,
							getColorShortFromColorENUM(color)));
		}
	}

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
	 * teleport every fighting player to each spawn
	 */
	public void teleportAllToSpawn() {
		for (String p : playerManager.getPlayerTeamMap().keySet()) {
			Player z = Bukkit.getServer().getPlayer(p);
			if (!randomSpawn) {
				tpPlayerToCoordName(z, playerManager.getPlayerTeamMap().get(p)
						+ "spawn");
			} else {
				tpPlayerToCoordName(z, "spawn");
			}
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
			db.i("using powerups : " + powerupTrigger + " : " + powerupDiff);
			if (powerupTrigger.equals("time") && powerupDiff > 0) {
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
	 * save player variables
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
		savedPlayerVars.put(player, tempMap);
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
		db.w("unrecognized itemstack: " + s);
		return null;
	}

	/*
	 * stick a player into a team, based on calcFreeTeam
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

	/*
	 * calculate a team that needs a player
	 */
	private String calcFreeTeam() {
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
		// counts: TEAMNAME, TEAMPLAYERCOUNT
		db.i("counts now has size " + counts.size());
		List<String> notmax = new ArrayList<String>(0);

		int lastInt = -1;
		String lastStr = "";

		// add teams to notmax that don't need players because they have more
		// than others
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
	 * prepare a player by saving player values and setting every variable to
	 * starting values
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
		playerManager.addPlayer(player);
	}

	/*
	 * prepare a player inventory for arena start
	 */
	public void prepareInventory(Player player) {
		saveInventory(player);
		clearInventory(player);
	}

	//
	// ARENA RUNTIME
	//

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
				color = "&"
						+ Integer
								.toString(
										ChatColor.valueOf(paTeams.get(color))
												.getCode(), 16).toLowerCase();
			}
		}
		if (!color.equals(""))
			colorizePlayer(player, color);

		playerManager.setTelePass(player, true);
		player.teleport(getCoords(place));
		playerManager.setTelePass(player, false);
	}

	/*
	 * return "vector is inside an arena region"
	 */
	public boolean contains(Vector pt) {
		db.i("----------------CONTAINS-------------");
		db.i("checking for vector: x: " + pt.getBlockX() + ", y:"
				+ pt.getBlockY() + ", z: " + pt.getBlockZ());
		if (regions.get("battlefield") != null) {
			db.i("checking battlefield");
			if (regions.get("battlefield").contains(pt)) {
				return true;
			}
		}
		if (checkExitRegion && regions.get("exit") != null) {
			db.i("checking exit region");
			if (regions.get("exit").contains(pt)) {
				return true;
			}
		}
		if (checkSpectatorRegion && regions.get("spectator") != null) {
			db.i("checking spectator region");
			if (regions.get("spectator").contains(pt)) {
				return true;
			}
		}
		if (!checkLoungesRegion) {
			return false;
		}
		db.i("checking regions:");
		for (PARegion reg : regions.values()) {
			if (!reg.name.endsWith("lounge"))
				continue;

			db.i(" - " + reg.name);
			if (reg.contains(pt)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * return "only one player/team alive"
	 * 
	 * - if only one player/team is alive: - announce winning team - teleport
	 * everyone out - give rewards - check for bets won
	 */
	public boolean checkEndAndCommit() {
		if (!this.fightInProgress)
			return false;
		List<String> activeteams = new ArrayList<String>(0);
		String team = "";
		
		for (String sTeam : playerManager.getPlayerTeamMap().keySet()) {
			if (activeteams.size() < 1) {
				// fresh map
				team = playerManager.getPlayerTeamMap().get(sTeam);
				activeteams.add(team);
				db.i("team set to " + team);
			} else {
				// map contains stuff
				if (!activeteams.contains(playerManager.getPlayerTeamMap().get(
						sTeam))) {
					// second team active => OUT!
					return false;
				}
			}
		}
		playerManager.tellEveryone(PVPArena.lang.parse("teamhaswon",
				ChatColor.valueOf(paTeams.get(team)) + "Team " + team));
		//TODO clear signs here?
		Set<String> set = playerManager.getPlayerTeamMap().keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			String sPlayer = iter.next();

			Player z = Bukkit.getServer().getPlayer(sPlayer);
			if (playerManager.getPlayerTeamMap().get(z.getName()).equals(team)) {
				StatsManager.addWinStat(z, team, this);
				resetPlayer(z, sTPwin);
				giveRewards(z); // if we are the winning team, give reward!
			} else {
				StatsManager.addLoseStat(z, team, this);
				resetPlayer(z, sTPlose);
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

	/*
	 * remove a player
	 */
	public void removePlayer(Player player, String tploc) {
		resetPlayer(player, tploc);
		playerManager.setTeam(player, "");
		playerManager.setClass(player, "");
		playerManager.remove(player);
	}

	/*
	 * player reset function
	 * 
	 * - load player vars - teleport player back - reset inventory
	 */
	@SuppressWarnings("unchecked")
	public void resetPlayer(Player player, String string) {
		db.i("resetting player: " + player.getName());
		HashMap<String, String> tSM = (HashMap<String, String>) savedPlayerVars
				.get(player);
		
		if (tSM == null) {
			db.w("------------");
			db.w("--hack fix--");
			db.w("------------");
			return;
		}
		
		
		//if (tSM != null) {
			//try {
				player.setExhaustion(Float.parseFloat(tSM.get("EXHAUSTION")));
			//} catch (Exception e) {
				//System.out.println("[PVP Arena] player '" + player.getName()
				//		+ "' had no valid EXHAUSTION entry!");
			//}
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
					System.out.println("[PVP Arena] player '"
							+ player.getName()
							+ "' had no valid LOCATION entry!");
				}
			} else {
				Location l = getCoords(string);
				player.teleport(l);
			}
			playerManager.setTelePass(player, false);
			savedPlayerVars.remove(player);
		/*} else {
			System.out.println("[PVP Arena] player '" + player.getName()
					+ "' had no savedmisc entries!");
		}*/
		colorizePlayer(player, "");
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

	/*
	 * force an arena to stop
	 */
	public void forcestop() {
		for (PAPlayer p : playerManager.getPlayers()) {
			removePlayer(p.getPlayer(), "spectator");
		}
		reset();
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
			ArenaManager.tellPublic(PVPArena.lang
					.parse("serverpowerup", p.name));
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

		int posx = diffx == 0 ? pos1.getBlockX() : (int) ((diffx / Math
				.abs(diffx)) * r.nextInt(Math.abs(diffx)) + pos2.getX());
		int posy = diffy == 0 ? pos1.getBlockY() : (int) ((diffx / Math
				.abs(diffy)) * r.nextInt(Math.abs(diffy)) + pos2.getY());
		int posz = diffz == 0 ? pos1.getBlockZ() : (int) ((diffx / Math
				.abs(diffz)) * r.nextInt(Math.abs(diffz)) + pos2.getZ());

		pos1.getWorld().dropItem(
				new Location(pos1.getWorld(), posx, posy + 1, posz),
				new ItemStack(item, 1));
	}

	/*
	 * read and return location from player's player vars
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

	/*
	 * reset player variables and teleport to spawn
	 */
	public void respawnPlayer(Player player, byte lives) {

		player.setHealth(20);
		player.setFireTicks(0);
		player.setFoodLevel(20);
		player.setSaturation(20);
		player.setExhaustion(0);
		String sTeam = playerManager.getTeam(player);
		String color = paTeams.get(sTeam);
		if (!randomSpawn && color != null && !sTeam.equals("free")) {
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
				resetPlayer(z, sTPwin);
				giveRewards(z); // if we are the winning team, give reward!
			} else {
				StatsManager.addLoseStat(z, p.getTeam(), this);
				resetPlayer(z, sTPlose);
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

	/*
	 * clean all signs
	 */
	public void cleanSigns() {
		for (PAPlayer p : playerManager.getPlayers()) {
			if (p.getSignLocation() == null) {
				continue;
			}
			Sign sign = (Sign) p.getSignLocation().getBlock().getState();
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
	 * load player inventory from map
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
		if (savedInventories.get(player) == null) {
			PVPArena.instance.log.severe("savedInventories.get(" + player
					+ ") = null!");
			return;
		}
		player.getInventory().setContents(
				(ItemStack[]) savedInventories.get(player));
		player.getInventory().setArmorContents(
				(ItemStack[]) savedArmories.get(player));
	}

	/*
	 * give rewards to player
	 * 
	 * - money - items
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

		if ((PVPArena.instance.getMethod() != null) && (rewardAmount > 0)) {
			MethodAccount ma = PVPArena.instance.getMethod().getAccount(
					player.getName());
			ma.add(rewardAmount);
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("awarded",
					PVPArena.instance.getMethod().format(rewardAmount)));
		}

		if (rewardItems.equals("none"))
			return;
		String[] items = rewardItems.split(",");
		for (int i = 0; i < items.length; ++i) {
			ItemStack stack = getItemStackFromString(items[i]);
			try {
				player.getInventory().setItem(
						player.getInventory().firstEmpty(), stack);
			} catch (Exception e) {
				ArenaManager.tellPlayer(player, PVPArena.lang.parse("invfull"));
				return;
			}
		}
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
		}
		;
		if (config.get("protection.regions") == null) {
			db.i("Region not set, skipping 1!");
			return;
		} else if (regions.get("battlefield") == null) {
			db.i("Region not set, skipping 2!");
			return;
		}
		World world = regions.get("battlefield").getWorld();
		for (Entity e : world.getEntities()) {
			if (((!(e instanceof Item)) && (!(e instanceof Arrow)))
					|| (!(regions.get("battlefield").contains(e.getLocation()
							.toVector()))))
				continue;
			e.remove();
		}
	}

	/*
	 * reset an arena
	 */
	public void reset() {
		cleanSigns();
		clearArena();
		fightInProgress = false;
		playerManager.reset(this);
		if (SPAWN_ID > -1)
			Bukkit.getScheduler().cancelTask(SPAWN_ID);
		SPAWN_ID = -1;
		if (END_ID > -1)
			Bukkit.getScheduler().cancelTask(END_ID);
		END_ID = -1;
	}

	public String getType() {
		return "team";
	}
}
