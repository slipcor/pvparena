package net.slipcor.pvparena.definitions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Announcement.type;
import net.slipcor.pvparena.managers.Commands;
import net.slipcor.pvparena.managers.ArenaConfigs;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Players;
import net.slipcor.pvparena.managers.Powerups;
import net.slipcor.pvparena.managers.Settings;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;
import net.slipcor.pvparena.runnables.EndRunnable;
import net.slipcor.pvparena.runnables.PowerupRunnable;
import net.slipcor.pvparena.runnables.TimedEndRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.util.Vector;

/**
 * arena class
 * 
 * -
 * 
 * contains >general< arena methods and variables
 * 
 * @author slipcor
 * 
 * @version v0.6.2
 * 
 */

public class Arena {

	// global statics: region modify blocks all child arenas
	public static String regionmodify = "";

	// protected static: Debug manager (same for all child Arenas)
	protected static final Debug db = new Debug();

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

	public HashMap<String, Integer> paLives = new HashMap<String, Integer>(); // flags
	/**
	 * TeamName => PlayerName
	 */
	public HashMap<String, String> paTeamFlags = null;
	public HashMap<String, ItemStack> paHeadGears = null;

	// regions an arena has defined: RegionName => Region
	public final HashMap<String, ArenaRegion> regions = new HashMap<String, ArenaRegion>();
	public final HashSet<String> paReady = new HashSet<String>();
	public final HashSet<String> paChat = new HashSet<String>();
	public final HashSet<ArenaSign> paSigns = new HashSet<ArenaSign>();

	public Powerups pum;
	public Settings sm;
	public Players pm = new Players();
	public String name = "default";
	public String owner = "%server%";

	public int powerupDiff; // powerup trigger cap
	public int powerupDiffI = 0; // powerup trigger count

	public Location pos1; // temporary position 1 (region select)
	public Location pos2; // temporary position 2 (region select)

	// arena status
	public boolean fightInProgress = false;

	// arena settings
	public boolean usesPowerups;
	public boolean preventDeath;

	// Runnable IDs
	public int SPAWN_ID = -1;
	public int END_ID = -1;
	public int BOARD_ID = -1;

	public Config cfg;

	public boolean betPossible;

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
	 * arena constructor
	 * 
	 * @param name
	 *            the arena name
	 */
	public Arena(String name, String type) {
		this.name = name;

		db.i("loading Arena " + name);
		File file = new File("plugins/pvparena/config_" + name + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cfg = new Config(file);
		cfg.load();
		ArenaConfigs.configParse(this, cfg, type);

		cfg.save();

	}

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
		if (!cfg.getBoolean("general.enabled")
				&& !PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, this))) {
			PVPArena.lang.parse("arenadisabled");
			return true;
		}
		db.i("parsing command: " + db.formatStringArray(args));

		if (args == null || args.length < 1) {
			return Commands.parseJoin(this, player);
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("enable")) {
				return Commands.parseToggle(this, player, "enabled");
			} else if (args[0].equalsIgnoreCase("disable")) {
				return Commands.parseToggle(this, player, "disabled");
			} else if (args[0].equalsIgnoreCase("reload")) {
				return Commands.parseReload(player);
			} else if (args[0].equalsIgnoreCase("check")) {
				return Commands.parseCheck(this, player);
			} else if (args[0].equalsIgnoreCase("info")) {
				return Commands.parseInfo(this, player);
			} else if (args[0].equalsIgnoreCase("list")) {
				return Commands.parseList(this, player);
			} else if (args[0].equalsIgnoreCase("watch")) {
				return Commands.parseSpectate(this, player);
			} else if (args[0].equalsIgnoreCase("users")) {
				return Commands.parseUsers(this, player);
			} else if (args[0].equalsIgnoreCase("chat")) {
				return Commands.parseChat(this, player);
			} else if (args[0].equalsIgnoreCase("region")) {
				return Commands.parseRegion(this, player);
			} else if (paTeams.get(args[0]) != null) {
				return Commands.parseJoinTeam(this, player, args[0]);
			} else if (PVPArena.hasAdminPerms(player)
					|| (PVPArena.hasCreatePerms(player, this))) {
				return Commands.parseAdminCommand(this, player, args[0]);
			} else {
				return Commands.parseJoin(this, player);
			}
		} else if (args.length == 3 && args[0].equalsIgnoreCase("bet")) {
			return Commands.parseBetCommand(this, player, args);
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

		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, this))) {
			Arenas.tellPlayer(
					player,
					PVPArena.lang.parse("nopermto",
							PVPArena.lang.parse("admin")));
			return false;
		}

		if (!checkRegionCommand(args[1])) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("invalidcmd", "504"));
			return false;
		}

		if (args.length == 2) {

			if (args[0].equalsIgnoreCase("region")) {

				// pa [name] region [regionname]
				if (Arena.regionmodify.equals("")) {
					Arenas.tellPlayer(player,
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

				String s = realMin.getBlockX() + "," + realMin.getBlockY()
						+ "," + realMin.getBlockZ() + "," + realMax.getBlockX()
						+ "," + realMax.getBlockY() + "," + realMax.getBlockZ();

				cfg.set("regions." + args[1], s);
				regions.put(args[1], new ArenaRegion(args[1], pos1, pos2, true));
				pos1 = null;
				pos2 = null;
				cfg.save();

				Arena.regionmodify = "";
				Arenas.tellPlayer(player, PVPArena.lang.parse("regionsaved"));
				return true;

			} else if (args[0].equalsIgnoreCase("remove")) {
				// pa [name] remove [spawnname]
				cfg.set("spawns." + args[1], null);
				cfg.save();
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("spawnremoved", args[1]));
				return true;
			}
		}

		if (args.length != 3) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("invalidcmd", "505"));
			return false;
		}

		if (args[2].equalsIgnoreCase("remove")) {
			if (cfg.get("regions." + args[1]) != null) {
				cfg.set("regions." + args[1], null);
				cfg.save();
				Arena.regionmodify = "";
				Arenas.tellPlayer(player, PVPArena.lang.parse("regionremoved"));
			} else {
				Arenas.tellPlayer(player,
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
		int joinRange = cfg.getInt("join.range", 0);
		if (joinRange < 1)
			return false;
		if (regions.get("battlefield") == null) {
			Bukkit.getLogger().warning(
					"[PVP Arena] JoinRange set, but Battlefield not set!");
			return false;
		}
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
		if (!this.cfg.getBoolean("periphery.checkRegions", false))
			return true;
		db.i("checking regions");

		return Arenas.checkRegions(this);
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
				&& (arena.regions.get("battlefield") != null)) {
			return !arena.regions.get("battlefield").overlapsWith(
					regions.get("battlefield"));
		}
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
		if (place.equals("spawn") || place.equals("popup")) {
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
		} else if (cfg.get("spawns." + place) == null) {
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

	/**
	 * supply a player with class items and eventually wool head
	 * 
	 * @param player
	 *            the player to supply
	 */
	public void givePlayerFightItems(Player player) {
		String playerClass = pm.getClass(player);
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
		if (cfg.getBoolean("game.woolHead", false)) {
			String sTeam = pm.getTeam(player);
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
		if (HELMETS_TYPE.contains(type)) {
			inv.setHelmet(stack);
		} else if (CHESTPLATES_TYPE.contains(type)) {
			inv.setChestplate(stack);
		} else if (LEGGINGS_TYPE.contains(type)) {
			inv.setLeggings(stack);
		} else if (BOOTS_TYPE.contains(type)) {
			inv.setBoots(stack);
		}
	}

	/**
	 * teleport all players to their respective spawn
	 */
	public void teleportAllToSpawn() {
		for (String p : pm.getPlayerTeamMap().keySet()) {
			Player z = Bukkit.getServer().getPlayer(p);
			if (!cfg.getBoolean("arenatype.randomSpawn", false)) {
				tpPlayerToCoordName(z, pm.getPlayerTeamMap().get(p) + "spawn");
			} else {
				tpPlayerToCoordName(z, "spawn");
			}
			setPermissions(z);
		}
		init_arena();
		int timed = cfg.getInt("goal.timed");
		if (timed > 0) {
			db.i("arena timing!");
			// initiate autosave timer
			END_ID = Bukkit
					.getServer()
					.getScheduler()
					.scheduleSyncDelayedTask(
							Bukkit.getServer().getPluginManager()
									.getPlugin("pvparena"),
							new TimedEndRunnable(this), timed * 20);
		}
		db.i("teleported everyone!");
		if (usesPowerups) {
			db.i("using powerups : " + cfg.getString("game.powerups", "off")
					+ " : " + powerupDiff);
			if (cfg.getString("game.powerups", "off").startsWith("time")
					&& powerupDiff > 0) {
				db.i("powerup time trigger!");
				powerupDiff = powerupDiff * 20; // calculate ticks to seconds
				// initiate autosave timer
				SPAWN_ID = Bukkit
						.getServer()
						.getScheduler()
						.scheduleSyncRepeatingTask(
								Bukkit.getServer().getPluginManager()
										.getPlugin("pvparena"),
								new PowerupRunnable(this), powerupDiff,
								powerupDiff);
			}
		}

	}

	private void setPermissions(Player p) {
		HashMap<String, Boolean> perms = getTempPerms();
		if (perms == null || perms.isEmpty())
			return;

		ArenaPlayer player = pm.parsePlayer(p);
		PermissionAttachment pa = p.addAttachment(Bukkit.getServer()
				.getPluginManager().getPlugin("pvparena"));
		player.tempPermissions.add(pa);
		for (String entry : perms.keySet()) {
			pa.setPermission(entry, perms.get(entry));
		}
	}

	private void removePermissions(Player p) {
		ArenaPlayer player = pm.parsePlayer(p);
		if (player == null || player.tempPermissions == null) {
			return;
		}
		for (PermissionAttachment pa : player.tempPermissions) {
			if (pa != null) {
				pa.remove();
			}
		}
	}

	/**
	 * get the permissions map
	 * 
	 * @return
	 */
	private HashMap<String, Boolean> getTempPerms() {
		HashMap<String, Boolean> result = new HashMap<String, Boolean>();

		if (cfg.getYamlConfiguration().getConfigurationSection("perms.default") != null) {
			List<String> list = cfg.getStringList("perms.default",
					new ArrayList<String>());
			for (String key : list) {
				result.put(key.replace("-", "").replace("^", ""),
						(key.startsWith("^") || key.startsWith("-")));
			}
		}

		return result;
	}

	public boolean isCustomClassActive() {
		for (ArenaPlayer p : pm.getPlayers()) {
			if (p.getClass().equals("custom")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * [FLAG] take away one life of a team
	 * 
	 * @param team
	 *            the team name to take away
	 */
	protected void reduceLivesCheckEndAndCommit(String team) {
		if (paLives.get(team) != null) {
			int i = paLives.get(team) - 1;
			if (i > 0) {
				paLives.put(team, i);
			} else {
				paLives.remove(team);
				CommitEnd(team, false);
			}
		}
	}

	/**
	 * get the team name of the flag a player holds
	 * 
	 * @param player
	 *            the player to check
	 * @return a team name
	 */
	protected String getHeldFlagTeam(String player) {
		db.i("getting held FLAG of player " + player);
		for (String sTeam : paTeamFlags.keySet()) {
			db.i("team " + sTeam + " is in " + paTeamFlags.get(sTeam)
					+ "s hands");
			if (player.equals(paTeamFlags.get(sTeam))) {
				return sTeam;
			}
		}
		return null;
	}

	/**
	 * parse player interaction
	 * 
	 * @param player
	 *            the player to parse
	 * @param block
	 *            the clicked block
	 */
	public void checkInteract(Player player, Block block) {

		boolean pumpkin = cfg.getBoolean("arenatype.pumpkin");

		if (block == null) {
			return;
		}

		if (pumpkin && !block.getType().equals(Material.PUMPKIN)) {
			return;
		} else if (!pumpkin && !block.getType().equals(Material.WOOL)) {
			return;
		}
		String type = null;
		if (pumpkin) {
			type = "pumpkin";
		} else {
			type = "flag";
		}
		db.i(type + " click!");

		Vector vLoc;
		String sTeam;
		Vector vFlag = null;

		if (paTeamFlags.containsValue(player.getName())) {
			db.i("player " + player.getName() + " has got a " + type);
			vLoc = block.getLocation().toVector();
			sTeam = pm.getTeam(player);
			if (this.getCoords(sTeam + type) != null) {
				vFlag = this.getCoords(sTeam + type).toVector();
			} else {
				db.i(sTeam + type + " = null");
			}

			db.i("player is in the team " + sTeam);
			if ((vFlag != null && vLoc.distance(vFlag) < 2)) {

				db.i("player is at his " + type);
				String flagTeam = getHeldFlagTeam(player.getName());

				db.i("the " + type + " belongs to team " + flagTeam);

				String scFlagTeam = ChatColor.valueOf(paTeams.get(flagTeam))
						+ flagTeam + ChatColor.YELLOW;
				String scPlayer = ChatColor.valueOf(paTeams.get(sTeam))
						+ player.getName() + ChatColor.YELLOW;

				pm.tellEveryone(PVPArena.lang.parse(type + "homeleft",
						scPlayer, scFlagTeam,
						String.valueOf(paLives.get(flagTeam) - 1)));
				paTeamFlags.remove(flagTeam);

				player.getInventory().setHelmet(
						paHeadGears.get(player.getName()).clone());
				paHeadGears.remove(player.getName());

				reduceLivesCheckEndAndCommit(flagTeam);
			}
		} else {
			for (String team : paTeams.keySet()) {
				String playerTeam = pm.getTeam(player);
				if (team.equals(playerTeam))
					continue;
				if (!pm.getPlayerTeamMap().containsValue(team))
					continue; // dont check for inactive teams
				if (paTeamFlags.containsKey(team)) {
					continue; // already taken
				}
				db.i("checking for " + type + " of team " + team);
				vLoc = player.getLocation().toVector();
				if (this.getCoords(team + type) != null) {
					vFlag = this.getCoords(team + type).toVector();
				}
				if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
					db.i(type + " found!");
					db.i("vFlag: " + vFlag.toString());
					String scTeam = ChatColor.valueOf(paTeams.get(team)) + team
							+ ChatColor.YELLOW;
					String scPlayer = ChatColor
							.valueOf(paTeams.get(playerTeam))
							+ player.getName() + ChatColor.YELLOW;
					pm.tellEveryone(PVPArena.lang.parse(type + "grab",
							scPlayer, scTeam));

					paHeadGears.put(player.getName(), player.getInventory()
							.getHelmet().clone());
					player.getInventory().setHelmet(
							block.getState().getData().toItemStack().clone());

					paTeamFlags.put(team, player.getName());
					return;
				}
			}
		}
	}

	/*
	 * set the pumpkin to the selected block
	 */
	public void setFlag(Player player, Block block) {
		if (block == null) {
			return;
		}
		boolean pumpkin = cfg.getBoolean("arenatype.pumpkin");

		if (pumpkin && !block.getType().equals(Material.PUMPKIN)) {
			return;
		} else if (!pumpkin && !block.getType().equals(Material.WOOL)) {
			return;
		}

		String type = null;
		if (pumpkin) {
			type = "pumpkin";
		} else {
			type = "flag";
		}

		String sName = Arena.regionmodify.replace(this.name + ":", "");

		Location location = block.getLocation();

		Integer x = location.getBlockX();
		Integer y = location.getBlockY();
		Integer z = location.getBlockZ();
		Float yaw = location.getYaw();
		Float pitch = location.getPitch();

		String s = x.toString() + "," + y.toString() + "," + z.toString() + ","
				+ yaw.toString() + "," + pitch.toString();

		cfg.set("spawns." + sName + type, s);

		cfg.save();
		Arenas.tellPlayer(player, PVPArena.lang.parse("set" + type, sName));

		Arena.regionmodify = "";
	}

	/**
	 * check a dying player if he held a flag, drop it, if so
	 * 
	 * @param player
	 *            the player to check
	 */
	public void checkEntityDeath(Player player) {
		boolean pumpkin = cfg.getBoolean("arenatype.pumpkin");

		String type = null;
		if (pumpkin) {
			type = "pumpkin";
		} else {
			type = "flag";
		}

		String flagTeam = getHeldFlagTeam(player.getName());
		if (flagTeam != null) {
			String scFlagTeam = ChatColor.valueOf(paTeams.get(flagTeam))
					+ flagTeam + ChatColor.YELLOW;
			String scPlayer = ChatColor
					.valueOf(paTeams.get(pm.getTeam(player)))
					+ player.getName() + ChatColor.YELLOW;
			PVPArena.lang.parse(type + "save", scPlayer, scFlagTeam);
			paTeamFlags.remove(flagTeam);
		}
	}

	/**
	 * method for CTF arena to override
	 */
	public void init_arena() {
		for (String sTeam : this.paTeams.keySet()) {
			if (pm.getPlayerTeamMap().containsValue(sTeam)) {
				// team is active
				this.paLives.put(sTeam, this.cfg.getInt("game.lives", 3));
			}
		}
	}

	/**
	 * prepare a player's inventory, back it up and clear it
	 * 
	 * @param player
	 *            the player to save
	 */
	public void prepareInventory(Player player) {

		ArenaPlayer p = pm.parsePlayer(player);
		p.savedInventory = player.getInventory().getContents().clone();
		p.savedArmor = player.getInventory().getArmorContents().clone();
		clearInventory(player);
	}

	/**
	 * save player variables
	 * 
	 * @param player
	 *            the player to save
	 */
	public void saveMisc(Player player) {
		ArenaPlayer p = pm.parsePlayer(player);
		p.exhaustion = player.getExhaustion();
		p.fireticks = player.getFireTicks();
		p.foodlevel = player.getFoodLevel();
		p.health = player.getHealth();
		p.saturation = player.getSaturation();
		p.location = player.getLocation();
		p.gamemode = player.getGameMode().getValue();

		if (cfg.getBoolean("messages.colorNick", true)) {
			p.displayname = player.getDisplayName();
		}
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
			Arenas.tellPlayer(p, "unrecognized item: " + s);
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

		boolean free = !cfg.getBoolean("arenatype.teams");

		if (pm.getPlayerTeamMap().containsKey(player.getName())) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("alreadyjoined"));
		}

		String team = free ? "free" : calcFreeTeam();
		pm.setTeam(player, team);

		if (free) {
			tpPlayerToCoordName(player, "lounge");
		} else {
			tpPlayerToCoordName(player, team + "lounge");
		}
		Arenas.tellPlayer(player, PVPArena.lang.parse("youjoined"
				+ (free ? "free" : ""), ChatColor.valueOf(paTeams.get(team))
				+ team));
		Announcement.announce(this, type.JOIN, PVPArena.lang.parse(
				"playerjoined" + (free ? "free" : ""), player.getName(),
				ChatColor.valueOf(paTeams.get(team)) + team));
		pm.tellEveryoneExcept(player, PVPArena.lang.parse("playerjoined"
				+ (free ? "free" : ""), player.getName(),
				ChatColor.valueOf(paTeams.get(team)) + team));
	}

	/**
	 * calculate the team that needs players the most
	 * 
	 * @return the team name
	 */
	public String calcFreeTeam() {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();

		// spam the available teams into a map counting the members
		for (String team : pm.getPlayerTeamMap().values()) {
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
			db.i("String s: " + s + "; max: " + cfg.getInt("ready.max"));
			if (counts.get(s) < cfg.getInt("ready.max")
					|| cfg.getInt("ready.max") == 0) {
				full = false;
				break;
			}
		}

		if (full) {
			// full => OUT!
			return null;
		}

		HashSet<String> free = new HashSet<String>();

		int max = cfg.getInt("ready.maxTeam");
		max = max == 0 ? Integer.MAX_VALUE : max;
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
	 * 
	 * @param set
	 *            the set to search
	 * @return one empty team name
	 */
	private String returnEmptyTeam(Set<String> set) {
		HashSet<String> empty = new HashSet<String>();
		for (String s : paTeams.keySet()) {
			db.i("team: " + s);
			if (set.contains(s)) {
				db.i("done");
				continue;
			}
			empty.add(s);
		}
		db.i("empty.size: " + empty.size());
		if (empty.size() == 1) {
			for (String s : empty) {
				db.i("return: " + s);
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
		pm.addPlayer(player);
		saveMisc(player); // save player health, fire tick, hunger etc
		playersetHealth(player, cfg.getInt("start.health", 0));
		player.setFireTicks(0);
		player.setFoodLevel(cfg.getInt("start.foodLevel", 20));
		player.setSaturation(cfg.getInt("start.saturation", 20));
		player.setExhaustion((float) cfg.getDouble("start.exhaustion", 0.0));
		player.setGameMode(GameMode.getByValue(0));
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
			if (cfg.getBoolean("messages.defaultChat")
					&& cfg.getBoolean("messages.chat")) {
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
												.ordinal(), 16).toLowerCase();
			}
		}
		if (!color.equals("") && cfg.getBoolean("messages.colorNick", true))
			colorizePlayer(player, color);

		pm.setTelePass(player, true);
		player.teleport(getCoords(place));
		pm.setTelePass(player, false);
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
		for (ArenaRegion reg : regions.values()) {
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
	 * [FLAG] commit the arena end
	 * 
	 * @param team
	 *            the team name
	 * @param win
	 *            winning team?
	 */
	protected void CommitEnd(String team, boolean win) {
		Set<String> set = pm.getPlayerTeamMap().keySet();
		Iterator<String> iter = set.iterator();
		if (!team.equals("$%&/")) {
			while (iter.hasNext()) {
				Object o = iter.next();
				db.i("precessing: " + o.toString());
				Player z = Bukkit.getServer().getPlayer(o.toString());
				if (!win && pm.getPlayerTeamMap().get(z.getName()).equals(team)) {
					pm.parsePlayer(z).losses++;
					resetPlayer(z, cfg.getString("tp.lose", "old"));
				}
			}

			if (paLives.size() > 1) {
				return;
			}
		}

		String winteam = "";
		set = pm.getPlayerTeamMap().keySet();
		iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			db.i("praecessing: " + o.toString());
			Player z = Bukkit.getServer().getPlayer(o.toString());
			if (paLives.containsKey(pm.getPlayerTeamMap().get(z.getName()))) {
				if (winteam.equals("")) {
					team = pm.getPlayerTeamMap().get(z.getName());
				}
				pm.parsePlayer(z).wins++;
				resetPlayer(z, cfg.getString("tp.win", "old"));
				giveRewards(z); // if we are the winning team, give reward!
				winteam = team;
			}
		}
		if (paTeams.get(winteam) != null) {
			Announcement.announce(this, type.WINNER,
					PVPArena.lang.parse("teamhaswon", "Team " + winteam));
			pm.tellEveryone(PVPArena.lang.parse("teamhaswon",
					ChatColor.valueOf(paTeams.get(winteam)) + "Team " + winteam));
		} else {
			System.out.print(winteam);
		}

		paLives.clear();
		Bukkit.getScheduler().scheduleSyncDelayedTask(
				Bukkit.getServer().getPluginManager().getPlugin("pvparena"),
				new EndRunnable(this), 15 * 20L);
	}

	/**
	 * checks if the arena is over, if an end has to be committed
	 * 
	 * @return true if we ended the game just yet, false otherwise
	 */
	public boolean checkEndAndCommit() {
		if (!cfg.getBoolean("arenatype.teams")) {
			if (pm.getPlayerTeamMap().size() > 1) {
				return false;
			}

			Set<String> set = pm.getPlayerTeamMap().keySet();
			Iterator<String> iter = set.iterator();
			while (iter.hasNext()) {
				Object o = iter.next();

				Announcement.announce(
						this,
						type.WINNER,
						PVPArena.lang.parse("playerhaswon",
								ChatColor.WHITE + o.toString()));
				pm.tellEveryone(PVPArena.lang.parse("playerhaswon",
						ChatColor.WHITE + o.toString()));
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(
					Bukkit.getServer().getPluginManager().getPlugin("pvparena"),
					new EndRunnable(this), 15 * 20L);
			return true;
		}
		if (cfg.getBoolean("arenatype.flags")) {

			if (pm.countPlayersInTeams() < 2) {
				String team = "$%&/";
				if (pm.countPlayersInTeams() != 0)
					for (String t : pm.getPlayerTeamMap().values()) {
						team = t;
						break;
					}
				CommitEnd(team, true);
			}
			return false;

		}

		if (!this.fightInProgress)
			return false;
		List<String> activeteams = new ArrayList<String>(0);
		String team = "";
		HashMap<String, String> test = pm.getPlayerTeamMap();
		for (String sPlayer : test.keySet()) {
			if (activeteams.size() < 1) {
				// fresh map
				team = test.get(sPlayer);
				activeteams.add(team);
				db.i("team set to " + team);
			} else {
				// map contains stuff
				if (!activeteams.contains(test.get(sPlayer))) {
					// second team active => OUT!
					return false;
				}
			}
		}

		Announcement.announce(this, type.WINNER,
				PVPArena.lang.parse("teamhaswon", "Team " + team));
		pm.tellEveryone(PVPArena.lang.parse("teamhaswon",
				ChatColor.valueOf(paTeams.get(team)) + "Team " + team));

		Set<String> set = pm.getPlayerTeamMap().keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			String sPlayer = iter.next();

			Player z = Bukkit.getServer().getPlayer(sPlayer);
			if (!pm.getPlayerTeamMap().get(z.getName()).equals(team)) {
				pm.parsePlayer(z).losses++;
				resetPlayer(z, cfg.getString("tp.lose", "old"));
			}
		}

		if (PVPArena.eco != null) {
			for (String nKey : pm.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (paTeams.get(nSplit[1]) == null
						|| paTeams.get(nSplit[1]).equals("free"))
					continue;

				if (nSplit[1].equalsIgnoreCase(team)) {
					double amount = pm.paPlayersBetAmount.get(nKey) * 2;

					MethodAccount ma = PVPArena.eco.getAccount(nSplit[0]);
					if (ma == null) {
						db.s("Account not found: " + nSplit[0]);
						return true;
					}
					ma.add(amount);
					try {
						Arenas.tellPlayer(
								Bukkit.getPlayer(nSplit[0]),
								PVPArena.lang.parse("youwon",
										PVPArena.eco.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(
				Bukkit.getServer().getPluginManager().getPlugin("pvparena"),
				new EndRunnable(this), 15 * 20L);
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
		pm.setTeam(player, "");
		pm.remove(player);
		if (cfg.getBoolean("general.signs")) {
			ArenaSign.remove(paSigns, player);
		}
	}

	/**
	 * reset a player to his pre-join values
	 * 
	 * @param player
	 * @param string
	 */
	public void resetPlayer(Player player, String string) {
		db.i("resetting player: " + player.getName());

		removePermissions(player);
		ArenaPlayer ap = pm.parsePlayer(player);
		player.setFireTicks(ap.fireticks);
		player.setFoodLevel(ap.foodlevel);
		player.setHealth(ap.health);
		player.setSaturation(ap.saturation);
		player.setGameMode(GameMode.getByValue(ap.gamemode));
		if (cfg.getBoolean("messages.colorNick", true)) {
			player.setDisplayName(ap.displayname);
		}
		pm.setTelePass(player, true);
		db.i("string = " + string);
		if (string.equalsIgnoreCase("old")) {
			player.teleport(ap.location);
		} else {
			Location l = getCoords(string);
			player.teleport(l);
		}
		pm.setTelePass(player, false);

		String sClass = "exit";
		if (!pm.getClass(player).equals("")) {
			sClass = pm.getClass(player);
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
		for (ArenaPlayer p : pm.getPlayers()) {
			removePlayer(p.get(), "spectator");
		}
		reset(true);
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
		if (this.pum == null)
			return;

		db.i("pm is not null");
		if (this.pum.puTotal.size() <= 0)
			return;

		db.i("totals are filled");
		Random r = new Random();
		int i = r.nextInt(this.pum.puTotal.size());

		for (Powerup p : this.pum.puTotal) {
			if (--i > 0)
				continue;
			commitPowerupItemSpawn(p.item);
			pm.tellEveryone(PVPArena.lang.parse("serverpowerup", p.name));
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
		if (cfg.getBoolean("game.dropSpawn")) {
			dropItemOnSpawn(item);
		} else {
			regions.get("battlefield").dropItemRandom(item);
		}
	}

	private void dropItemOnSpawn(Material item) {
		Location aim = getCoords("popup").getBlock().getRelative(BlockFace.UP)
				.getLocation();

		db.i("dropping item on spawn.");
		Bukkit.getWorld(this.getWorld()).dropItem(aim, new ItemStack(item, 1));

	}

	/**
	 * read the saved player location
	 * 
	 * @param player
	 *            the player to check
	 * @return the saved location
	 */
	public Location getPlayerOldLocation(Player player) {
		ArenaPlayer ap = pm.parsePlayer(player);
		return ap.location;
	}

	/**
	 * reset player variables and teleport again
	 * 
	 * @param player
	 *            the player to access
	 * @param lives
	 *            the lives to set and display
	 */
	public void respawnPlayer(Player player, int lives) {
		playersetHealth(player, cfg.getInt("start.health", 0));
		player.setFireTicks(0);
		player.setFoodLevel(cfg.getInt("start.foodLevel", 20));
		player.setSaturation(cfg.getInt("start.saturation", 20));
		player.setExhaustion((float) cfg.getDouble("start.exhaustion", 0.0));

		if (cfg.getBoolean("game.refillInventory")
				&& !pm.getClass(player).equals("custom")) {
			clearInventory(player);
			givePlayerFightItems(player);
		}

		String sTeam = pm.getTeam(player);
		String color = paTeams.get(sTeam);

		if (cfg.getBoolean("arenatype.flags")) {
			pm.tellEveryone(PVPArena.lang.parse("killed",
					ChatColor.valueOf(color) + player.getName()
							+ ChatColor.YELLOW));
			tpPlayerToCoordName(player, sTeam + "spawn");

			checkEntityDeath(player);
		} else {

			pm.tellEveryone(PVPArena.lang.parse("lostlife", ChatColor.valueOf(color)
					+ player.getName() + ChatColor.YELLOW,
					String.valueOf(lives)));
			if (!cfg.getBoolean("arenatype.randomSpawn", false)
					&& color != null && !sTeam.equals("free")) {
				tpPlayerToCoordName(player, sTeam + "spawn");
			} else {
				tpPlayerToCoordName(player, "spawn");
			}
			paLives.put(player.getName(), lives);
		}
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
			return;
		}

		String n = color + player.getName();

		player.setDisplayName(n.replaceAll("(&([a-f0-9]))", "§$2"));
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
				iKills = pm.getKills(sTeam);
			} catch (Exception e) {
			}

			try {
				iDeaths = pm.getDeaths(sTeam);
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
			if (result.contains(team)) {
				Announcement.announce(this, type.WINNER,
						PVPArena.lang.parse("teamhaswon", "Team " + team));
				pm.tellEveryone(PVPArena.lang.parse("teamhaswon",
						ChatColor.valueOf(paTeams.get(team)) + "Team " + team));
			}

		}

		for (ArenaPlayer p : pm.getPlayers()) {

			Player z = p.get();
			if (!result.contains(p.team)) {
				pm.parsePlayer(z).losses++;
				resetPlayer(z, cfg.getString("tp.lose", "old"));
			}
			p = null;
		}

		if (PVPArena.eco != null) {
			for (String nKey : pm.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (paTeams.get(nSplit[1]) == null
						|| paTeams.get(nSplit[1]).equals("free"))
					continue;

				if (result.contains(nSplit[1])) {
					double amount = pm.paPlayersBetAmount.get(nKey) * 2;

					MethodAccount ma = PVPArena.eco.getAccount(nSplit[0]);
					if (ma == null) {
						db.s("Account not found: " + nSplit[0]);
						continue;
					}
					ma.add(amount);
					try {
						Arenas.tellPlayer(
								Bukkit.getPlayer(nSplit[0]),
								PVPArena.lang.parse("youwon",
										PVPArena.eco.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(
				Bukkit.getServer().getPluginManager().getPlugin("pvparena"),
				new EndRunnable(this), 15 * 20L);
	}

	/**
	 * reload player inventories from saved variables
	 * 
	 * @param player
	 */
	public void loadInventory(Player player) {
		if (player == null) {
			return;
		}
		if (player.getInventory() == null) {
			return;
		}

		ArenaPlayer p = pm.parsePlayer(player);

		if (p.savedInventory == null) {
			return;
		}
		player.getInventory().setContents(p.savedInventory);
		player.getInventory().setArmorContents(p.savedArmor);
	}

	/**
	 * give customized rewards to players
	 * 
	 * @param player
	 *            the player to give the reward
	 */
	public void giveRewards(Player player) {
		if (PVPArena.eco != null) {
			for (String nKey : pm.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (nSplit[1].equalsIgnoreCase(player.getName())) {
					double amount = pm.paPlayersBetAmount.get(nKey) * 4; //TODO: config

					MethodAccount ma = PVPArena.eco.getAccount(nSplit[0]);
					ma.add(amount);
					try {
						Announcement.announce(this, type.PRIZE, PVPArena.lang
								.parse("awarded", PVPArena.eco.format(cfg
										.getInt("money.reward", 0))));
						Arenas.tellPlayer(
								Bukkit.getPlayer(nSplit[0]),
								PVPArena.lang.parse("youwon",
										PVPArena.eco.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}
			}
		}

		if ((PVPArena.eco != null) && (cfg.getInt("money.reward", 0) > 0)) {
			MethodAccount ma = PVPArena.eco.getAccount(player.getName());
			ma.add(cfg.getInt("money.reward", 0));
			Arenas.tellPlayer(
					player,
					PVPArena.lang.parse("awarded",
							PVPArena.eco.format(cfg.getInt("money.reward", 0))));
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
				Arenas.tellPlayer(player, PVPArena.lang.parse("invfull"));
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
	public void reset(boolean force) {
		clearArena();
		paReady.clear();
		paChat.clear();
		for (ArenaSign as : paSigns) {
			as.clear();
		}
		paSigns.clear();
		fightInProgress = false;
		pm.reset(this, force);
		if (SPAWN_ID > -1)
			Bukkit.getScheduler().cancelTask(SPAWN_ID);
		SPAWN_ID = -1;
		if (END_ID > -1)
			Bukkit.getScheduler().cancelTask(END_ID);
		END_ID = -1;
		if (BOARD_ID > -1)
			Bukkit.getScheduler().cancelTask(BOARD_ID);
		BOARD_ID = -1;
	}

	/**
	 * return the arena type
	 * 
	 * @return the arena type name
	 */
	public String getType() {
		if (cfg.getBoolean("arenatype.pumpkin")) {
			return "pumpkin";
		} else if (cfg.getBoolean("arenatype.flags")) {
			return "ctf";
		} else if (!cfg.getBoolean("arenatype.teams")) {
			return "free";
		}
		return "teams";
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
