package net.slipcor.pvparena.definitions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.definitions.Announcement.type;
import net.slipcor.pvparena.managers.Configs;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Flags;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Players;
import net.slipcor.pvparena.managers.Powerups;
import net.slipcor.pvparena.managers.Settings;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;
import net.slipcor.pvparena.runnables.PowerupRunnable;
import net.slipcor.pvparena.runnables.TimedEndRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
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
	public static final Debug db = new Debug();

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
		Configs.configParse(this, cfg, type);

		cfg.save();

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

		if (cfg.getBoolean("arenatype.flags")) {
			Flags.init_arena(this);
		}
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
		player.teleport(Spawns.getCoords(this, place));
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
			Location l = Spawns.getCoords(this, string);
			player.teleport(l);
		}
		pm.setTelePass(player, false);

		String sClass = "exit";
		if (!pm.getClass(player).equals("")) {
			sClass = pm.getClass(player);
		}
		if (!sClass.equalsIgnoreCase("custom")) {
			Inventories.clearInventory(player);
			Inventories.loadInventory(this, player);
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
		Location aim = Spawns.getCoords(this, "popup").getBlock()
				.getRelative(BlockFace.UP).getLocation();

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
			Inventories.clearInventory(player);
			Inventories.givePlayerFightItems(this, player);
		}

		String sTeam = pm.getTeam(player);
		String color = paTeams.get(sTeam);

		if (cfg.getBoolean("arenatype.flags")) {
			pm.tellEveryone(PVPArena.lang.parse("killed",
					ChatColor.valueOf(color) + player.getName()
							+ ChatColor.YELLOW));
			tpPlayerToCoordName(player, sTeam + "spawn");

			Flags.checkEntityDeath(this, player);
		} else {

			pm.tellEveryone(PVPArena.lang.parse("lostlife",
					ChatColor.valueOf(color) + player.getName()
							+ ChatColor.YELLOW, String.valueOf(lives)));
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
					double amount = pm.paPlayersBetAmount.get(nKey) * 4; // TODO:
																			// config

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
			ItemStack stack = StringParser.getItemStackFromString(items[i]);
			if (stack == null) {
				db.w("unrecognized item: " + items[i]);
				continue;
			}
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
		if (paTeamFlags != null) {
			paTeamFlags.clear();
		}
		if (paHeadGears != null) {
			paHeadGears.clear();
		}
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
