package net.slipcor.pvparena.arena;

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
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.definitions.Announcement;
import net.slipcor.pvparena.definitions.ArenaClassSign;
import net.slipcor.pvparena.definitions.ArenaRegion;
import net.slipcor.pvparena.definitions.Powerup;
import net.slipcor.pvparena.events.PAEndEvent;
import net.slipcor.pvparena.events.PAJoinEvent;
import net.slipcor.pvparena.events.PAStartEvent;
import net.slipcor.pvparena.listeners.EntityListener;
import net.slipcor.pvparena.managers.Blocks;
import net.slipcor.pvparena.managers.Configs;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Players;
import net.slipcor.pvparena.managers.Powerups;
import net.slipcor.pvparena.managers.Settings;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.neworder.ArenaType;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;
import net.slipcor.pvparena.runnables.BoardRunnable;
import net.slipcor.pvparena.runnables.DominationRunnable;
import net.slipcor.pvparena.runnables.PowerupRunnable;
import net.slipcor.pvparena.runnables.SpawnCampRunnable;
import net.slipcor.pvparena.runnables.StartRunnable;
import net.slipcor.pvparena.runnables.TimedEndRunnable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
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
 * @version v0.7.0
 * 
 */

public class Arena {
	private Debug db = new Debug(8);
	private final HashSet<ArenaPlayer> players = new HashSet<ArenaPlayer>();
	private final HashSet<ArenaTeam> teams = new HashSet<ArenaTeam>();
	private final HashSet<ArenaClass> classes = new HashSet<ArenaClass>();

	private final ArenaType type;

	// global statics: region modify blocks all child arenas
	public static String regionmodify = "";

	public HashMap<String, Integer> paLives = new HashMap<String, Integer>(); // flags
	/**
	 * TeamName => PlayerName
	 */
	public HashMap<String, String> paTeamFlags = null;
	public HashMap<Location, String> paFlags = null;
	public HashMap<Location, DominationRunnable> paRuns = new HashMap<Location, DominationRunnable>();
	public HashMap<String, ItemStack> paHeadGears = null;

	// regions an arena has defined: RegionName => Region
	public final HashMap<String, ArenaRegion> regions = new HashMap<String, ArenaRegion>();
	public final HashSet<String> paChat = new HashSet<String>();
	public final HashSet<ArenaClassSign> paSigns = new HashSet<ArenaClassSign>();

	public Powerups pum;
	public Settings sm;
	public String name = "default";
	public String owner = "%server%";

	public int powerupDiff; // powerup trigger cap
	public int powerupDiffI = 0; // powerup trigger count

	public Location pos1; // temporary position 1 (region select)
	public Location pos2; // temporary position 2 (region select)

	// arena status
	public boolean fightInProgress = false;
	public boolean edit = false;

	// arena settings
	public boolean usesPowerups;

	// Runnable IDs
	public int SPAWN_ID = -1;
	public int END_ID = -1;
	public int BOARD_ID = -1;
	public int START_ID = -1;
	public int SPAWNCAMP_ID = -1;

	public Config cfg;

	public boolean betPossible;

	public int playerCount = 0;
	public int teamCount = 0;

	/**
	 * arena constructor
	 * 
	 * @param name
	 *            the arena name
	 */
	public Arena(String name, String type) {
		this.name = name;

		ArenaType aType = PVPArena.instance.getAtm().getType(type);

		this.type = aType.cloneThis();

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

		PAStartEvent event = new PAStartEvent(this);
		Bukkit.getPluginManager().callEvent(event);

		db.i("teleporting all players to their spawns");
		for (ArenaTeam team : teams) {
			for (ArenaPlayer ap : team.getTeamMembers()) {

				if (!type.allowsRandomSpawns()) {
					tpPlayerToCoordName(ap.get(), team.getName() + "spawn");
				} else {
					tpPlayerToCoordName(ap.get(), "spawn");
				}
				setPermissions(ap.get());
				playerCount++;
			}
		}

		type.init();

		int timed = cfg.getInt("goal.timed");
		if (timed > 0) {
			db.i("arena timing!");
			// initiate autosave timer
			END_ID = Bukkit
					.getServer()
					.getScheduler()
					.scheduleSyncDelayedTask(PVPArena.instance,
							new TimedEndRunnable(this), timed * 20);
		}
		this.BOARD_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, new BoardRunnable(this), 100L, 100L);
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
						.scheduleSyncRepeatingTask(PVPArena.instance,
								new PowerupRunnable(this), powerupDiff,
								powerupDiff);
			}
		}
		teamCount = countActiveTeams();
		SPAWNCAMP_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, new SpawnCampRunnable(this), 100L, 20L);
	}

	/**
	 * set temporary permissions for a player
	 * 
	 * @param p
	 *            the player to set
	 */
	public void setPermissions(Player p) {
		HashMap<String, Boolean> perms = getTempPerms();
		if (perms == null || perms.isEmpty())
			return;

		ArenaPlayer player = Players.parsePlayer(p);
		PermissionAttachment pa = p.addAttachment(PVPArena.instance);

		for (String entry : perms.keySet()) {
			pa.setPermission(entry, perms.get(entry));
		}
		p.recalculatePermissions();
		player.tempPermissions.add(pa);
	}

	/**
	 * remove temporary permissions from a player
	 * 
	 * @param p
	 *            the player to reset
	 */
	private void removePermissions(Player p) {
		ArenaPlayer player = Players.parsePlayer(p);
		if (player == null || player.tempPermissions == null) {
			return;
		}
		for (PermissionAttachment pa : player.tempPermissions) {
			if (pa != null) {
				pa.remove();
			}
		}
		p.recalculatePermissions();
	}

	/**
	 * get the permissions map
	 * 
	 * @return the temporary permissions map
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

	/**
	 * check if a custom class player is alive
	 * 
	 * @return true if there is a custom class player alive, false otherwise
	 */
	public boolean isCustomClassActive() {
		for (ArenaPlayer p : Players.getPlayers(this)) {
			if (!p.isSpectator() && p.getClass().equals("custom")) {
				db.i("custom class active: true");
				return true;
			}
		}
		db.i("custom class active: false");
		return false;
	}

	/**
	 * save player variables
	 * 
	 * @param player
	 *            the player to save
	 */
	public void saveMisc(Player player) {
		db.i("saving player vars: " + player.getName());

		ArenaPlayer p = Players.parsePlayer(player);
		p.createState(player);
	}

	/**
	 * prepare a player for fighting. Setting all values to start value
	 * 
	 * @param player
	 */
	public void prepare(Player player, boolean spectate) {
		PAJoinEvent event = new PAJoinEvent(this, player, spectate);
		Bukkit.getPluginManager().callEvent(event);

		db.i("preparing player: " + player.getName());

		ArenaPlayer ap = Players.parsePlayer(player);

		ap.setArena(this);
		this.addPlayer(ap);

		saveMisc(player); // save player health, fire tick, hunger etc
		playersetHealth(player, cfg.getInt("start.health", 0));
		player.setFireTicks(0);
		player.setFoodLevel(cfg.getInt("start.foodLevel", 20));
		player.setSaturation(cfg.getInt("start.saturation", 20));
		player.setExhaustion((float) cfg.getDouble("start.exhaustion", 0.0));
		player.setLevel(0);
		player.setExp(0);
		player.setGameMode(GameMode.getByValue(0));
		for (PotionEffect pe : player.getActivePotionEffects()) {
			player.addPotionEffect(new PotionEffect(pe.getType(), 0, 0));
		}

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
		db.i("setting health to " + value + "/20");
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
		db.i("teleporting " + player + " to coord " + place);
		if (place.endsWith("lounge")) {
			// at the start of the match
			if (cfg.getBoolean("messages.defaultChat")
					&& cfg.getBoolean("messages.chat")) {
				paChat.add(player.getName());
			}
		}

		if (cfg.getBoolean("messages.colorNick", true))
			colorizePlayer(player, "a");
		if (place.equals("spectator")) {
			ArenaPlayer ap = Players.parsePlayer(player);
			ap.setSpectator(true);
		}
		Location loc = Spawns.getCoords(this, place);
		if (loc == null) {
			System.out.print("[PA-debug] Spawn null : " + place);
			return;
		}
		Players.setTelePass(player, true);
		player.teleport(loc);
		Players.setTelePass(player, false);
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
		db.i("CONTAINS: checking for vector: x: " + pt.getBlockX() + ", y:"
				+ pt.getBlockY() + ", z: " + pt.getBlockZ());
		if (regions.get("battlefield") != null) {
			db.i("checking battlefield");
			if (regions.get("battlefield").contains(loc)) {
				return true;
			}
		}
		if (cfg.getBoolean("protection.checkExit", false)
				&& regions.get("exit") != null) {
			db.i("checking exit region");
			if (regions.get("exit").contains(loc)) {
				return true;
			}
		}
		if (cfg.getBoolean("protection.checkSpectator", false)
				&& regions.get("spectator") != null) {
			db.i("checking spectator region");
			if (regions.get("spectator").contains(loc)) {
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
			if (reg.contains(loc)) {
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
		db.i("removing player " + player.getName() + " (soft), tp to " + tploc);
		resetPlayer(player, tploc);

		ArenaPlayer ap = Players.parsePlayer(player);
		this.removeTeam(ap);
		Players.remove(this, player);
		if (cfg.getBoolean("general.signs")) {
			ArenaClassSign.remove(paSigns, player);
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

		if (player.isDead() && !Players.isDead(player)) {
			db.i("player is dead");
			Players.addDeadPlayer(Players.parsePlayer(player), string);
			return;
		}

		removePermissions(player);
		ArenaPlayer ap = Players.parsePlayer(player);

		ap.getState().unload();

		db.i("string = " + string);
		Players.setTelePass(player, true);
		if (string.equalsIgnoreCase("old")) {
			player.teleport(ap.location);
		} else {
			Location l = Spawns.getCoords(this, string);
			player.teleport(l);
		}

		String sClass = "";
		if (Players.getClass(player) != null) {
			sClass = Players.getClass(player).getName();
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
		db.i("forcing arena to stop");
		for (ArenaPlayer p : Players.getPlayers(this)) {
			removePlayer(p.get(), "spectator");
			p.setSpectator(true);
		}
		reset(true);
	}

	/**
	 * calculate a powerup and commit it
	 */
	public void calcPowerupSpawn() {
		db.i("powerups?");
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
			Players.tellEveryone(this, Language.parse("serverpowerup", p.name));
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

	/**
	 * drop an item at a powerup spawn point
	 * 
	 * @param item
	 *            the item to drop
	 */
	private void dropItemOnSpawn(Material item) {
		db.i("calculating item spawn location");
		Location aim = Spawns.getCoords(this, "powerup").getBlock()
				.getRelative(BlockFace.UP).getLocation();

		db.i("dropping item on spawn: " + aim.toString());
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
		db.i("reading old location of player " + player.getName());
		ArenaPlayer ap = Players.parsePlayer(player);
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
	public void respawnPlayer(Player player, int lives, DamageCause cause,
			Entity damager) {
		db.i("respawning player " + player.getName());
		playersetHealth(player, cfg.getInt("start.health", 0));
		player.setFoodLevel(cfg.getInt("start.foodLevel", 20));
		player.setSaturation(cfg.getInt("start.saturation", 20));
		player.setExhaustion((float) cfg.getDouble("start.exhaustion", 0.0));

		if (cfg.getBoolean("game.refillInventory")
				&& !Players.getClass(player).equals("custom")) {
			Inventories.clearInventory(player);
			Inventories.givePlayerFightItems(this, player);
		}

		ArenaTeam team = this.getTeam(Players.parsePlayer(player));

		if (team == null) {
			return;
		}

		type.parseRespawn(player, team, lives, cause, damager);

		if (!type.allowsRandomSpawns() && !team.getName().equals("free")) {
			tpPlayerToCoordName(player, team.getName() + "spawn");
		} else {
			tpPlayerToCoordName(player, "spawn");
		}
		player.setFireTicks(0);
		player.setNoDamageTicks(60);
		EntityListener.addBurningPlayer(player);
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
		db.i("colorizing player " + player.getName() + "; color " + color);

		if (color != null && color.equals("")) {
			player.setDisplayName(player.getName());

			if (PVPArena.spoutHandler != null)
				SpoutManager.getAppearanceManager().setGlobalTitle(player,
						player.getName());

			return;
		} else if (color == null) {
			if (PVPArena.spoutHandler != null)
				SpoutManager.getAppearanceManager().setGlobalTitle(player, " ");

			return;
		}
		ArenaTeam team = this.getTeam(Players.parsePlayer(player));
		String n = team.getColorString() + player.getName();
		player.setDisplayName(n.replaceAll("(&([a-f0-9]))", "§$2"));
		if (PVPArena.spoutHandler != null)
			SpoutManager.getAppearanceManager().setGlobalTitle(player,
					n.replaceAll("(&([a-f0-9]))", "§$2"));
	}

	/**
	 * give customized rewards to players
	 * 
	 * @param player
	 *            the player to give the reward
	 */
	public void giveRewards(Player player) {
		db.i("giving rewards to " + player.getName());
		if (PVPArena.economy != null) {
			for (String nKey : Players.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (nSplit[1].equalsIgnoreCase(player.getName())) {
					double playerFactor = playerCount
							* cfg.getDouble("money.betPlayerWinFactor");

					if (playerFactor <= 0) {
						playerFactor = 1;
					}

					playerFactor *= cfg.getDouble("money.betWinFactor");

					double amount = Players.paPlayersBetAmount.get(nKey)
							* playerFactor;

					PVPArena.economy.depositPlayer(nSplit[0], amount);
					try {
						Announcement.announce(
								this,
								Announcement.type.PRIZE,
								Language.parse("awarded",
										PVPArena.economy.format(amount)));
						Arenas.tellPlayer(
								Bukkit.getPlayer(nSplit[0]),
								Language.parse("youwon",
										PVPArena.economy.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}
			}
		} else if (PVPArena.eco != null) {
			for (String nKey : Players.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (nSplit[1].equalsIgnoreCase(player.getName())) {
					double playerFactor = playerCount
							* cfg.getDouble("money.betPlayerWinFactor");

					if (playerFactor <= 0) {
						playerFactor = 1;
					}

					playerFactor *= cfg.getDouble("money.betWinFactor");

					double amount = Players.paPlayersBetAmount.get(nKey)
							* playerFactor;

					MethodAccount ma = PVPArena.eco.getAccount(nSplit[0]);
					ma.add(amount);
					try {
						Announcement.announce(
								this,
								Announcement.type.PRIZE,
								Language.parse("awarded",
										PVPArena.eco.format(amount)));
						Arenas.tellPlayer(
								Bukkit.getPlayer(nSplit[0]),
								Language.parse("youwon",
										PVPArena.eco.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}
			}
		}

		if (cfg.getInt("money.reward", 0) > 0) {
			if (PVPArena.economy != null) {
				PVPArena.economy.depositPlayer(player.getName(),
						cfg.getInt("money.reward", 0));
				Arenas.tellPlayer(player, Language.parse("awarded",
						PVPArena.economy.format(cfg.getInt("money.reward", 0))));
			} else if (PVPArena.eco != null) {
				MethodAccount ma = PVPArena.eco.getAccount(player.getName());
				ma.add(cfg.getInt("money.reward", 0));
				Arenas.tellPlayer(player, Language.parse("awarded",
						PVPArena.eco.format(cfg.getInt("money.reward", 0))));
			}
		}
		String sItems = cfg.getString("general.item-rewards", "none");
		if (sItems.equals("none"))
			return;
		String[] items = sItems.split(",");
		boolean random = cfg.getBoolean("general.random-reward");
		Random r = new Random();
		int randomItem = r.nextInt(items.length);
		for (int i = 0; i < items.length; ++i) {
			ItemStack stack = StringParser.getItemStackFromString(items[i]);
			if (stack == null) {
				db.w("unrecognized item: " + items[i]);
				continue;
			}
			if (random && i != randomItem) {
				continue;
			}
			try {
				player.getInventory().setItem(
						player.getInventory().firstEmpty(), stack);
			} catch (Exception e) {
				Arenas.tellPlayer(player, Language.parse("invfull"));
				return;
			}
		}
	}

	/**
	 * restore an arena if region is set
	 */
	public void clearArena() {
		db.i("clearing arena");
		if (cfg.get("regions") == null) {
			db.i("Region not set, skipping!");
			return;
		} else if (regions.get("battlefield") == null) {
			db.i("Battlefield region not set, skipping!");
			return;
		}
		regions.get("battlefield").restore();
	}

	/**
	 * reset an arena
	 */
	public void reset(boolean force) {

		PAEndEvent event = new PAEndEvent(this);
		Bukkit.getPluginManager().callEvent(event);

		db.i("resetting arena; force: " + String.valueOf(force));
		clearArena();
		paChat.clear();
		for (ArenaClassSign as : paSigns) {
			as.clear();
		}
		paSigns.clear();
		if (paTeamFlags != null) {
			paTeamFlags.clear();
		}
		if (paHeadGears != null) {
			paHeadGears.clear();
		}
		Players.reset(this, force);
		fightInProgress = false;
		if (SPAWN_ID > -1)
			Bukkit.getScheduler().cancelTask(SPAWN_ID);
		SPAWN_ID = -1;
		if (END_ID > -1)
			Bukkit.getScheduler().cancelTask(END_ID);
		END_ID = -1;
		if (BOARD_ID > -1)
			Bukkit.getScheduler().cancelTask(BOARD_ID);
		BOARD_ID = -1;

		Blocks.resetBlocks(this);

		if (paRuns == null || paRuns.size() < 1) {
			return;
		}

		for (DominationRunnable run : paRuns.values()) {
			Bukkit.getScheduler().cancelTask(run.ID);
		}
		paRuns.clear();
		this.playerCount = 0;
		this.teamCount = 0;
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

	public int countActiveTeams() {
		db.i("counting active teams");

		HashSet<String> activeteams = new HashSet<String>();
		for (ArenaTeam team : teams) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.isSpectator()) {
					activeteams.add(team.getName());
					break;
				}
			}
		}
		db.i("result: " + activeteams.size());
		return activeteams.size();
	}

	public void countDown() {
		if (START_ID != -1 || this.fightInProgress) {
			Bukkit.getScheduler().cancelTask(START_ID);
			START_ID = -1;
			return;
		}

		long duration = 20L * 5;
		START_ID = Bukkit.getScheduler().scheduleSyncDelayedTask(
				PVPArena.instance, new StartRunnable(this), duration);
		Players.tellEveryone(this, Language.parse("starting"));
	}

	public void start() {
		START_ID = -1;

		teleportAllToSpawn();
		fightInProgress = true;
		Players.tellEveryone(this, Language.parse("begin"));
		Announcement.announce(this, Announcement.type.START,
				Language.parse("begin"));
	}

	public void spawnCampPunish() {

		HashMap<Location, ArenaPlayer> players = new HashMap<Location, ArenaPlayer>();

		for (ArenaPlayer ap : Players.getPlayers(this)) {
			if (ap.isSpectator()) {
				continue;
			}
			players.put(ap.get().getLocation(), ap);
		}

		for (ArenaTeam team : teams) {
			if (team.getTeamMembers().size() < 1) {
				continue;
			}
			String sTeam = team.getName();
			for (Location spawnLoc : Spawns.getSpawns(this, sTeam)) {
				for (Location playerLoc : players.keySet()) {
					if (spawnLoc.distance(playerLoc) < 3) {
						players.get(playerLoc).get().damage(1);
					}
				}
			}
		}
	}

	public HashSet<ArenaPlayer> getPlayers() {
		return players;
	}

	public void removePlayer(ArenaPlayer player) {
		this.players.remove(player);
	}

	public void addPlayer(ArenaPlayer player) {
		this.players.add(player);
	}

	public HashSet<ArenaTeam> getTeams() {
		return teams;
	}

	public ArenaTeam getTeam(ArenaPlayer player) {
		for (ArenaTeam team : teams) {
			if (team.getTeamMembers().contains(player)) {
				return team;
			}
		}
		return null;
	}

	public ArenaTeam getTeam(String name) {
		for (ArenaTeam team : teams) {
			if (team.getName().equalsIgnoreCase(name)) {
				return team;
			}
		}
		return null;
	}

	public void removeTeam(ArenaPlayer player) {
		for (ArenaTeam team : teams) {
			team.remove(player);
		}
	}

	public void addTeam(ArenaTeam arenaTeam) {
		teams.add(arenaTeam);
	}

	public HashSet<ArenaClass> getClasses() {
		return classes;
	}

	public boolean classExists(String line) {
		for (ArenaClass ac : classes) {
			if (ac.getName().equalsIgnoreCase(line)) {
				return true;
			}
		}
		return false;
	}

	public void addClass(String className, ItemStack[] items) {
		classes.add(new ArenaClass(className, items));
	}

	public ArenaType type() {
		return this.type;
	}

	public void checkForQuitters() {
		if (!this.regions.containsKey("battlefield")) {
			db.i("region battlefield not set, aborting quit check");
			return;
		}
		if (!this.regions.containsKey("spectator")) {
			db.i("region spectator not set, aborting quit check");
			return;
		}
		for (ArenaPlayer ap : Players.getPlayers(this)) {
			if (!this.contains(ap.get().getLocation())) {
				Players.playerLeave(this, ap.get());
			}
		}
	}
}
