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
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.definitions.Announcement.type;
import net.slipcor.pvparena.managers.Blocks;
import net.slipcor.pvparena.managers.Configs;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Flags;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Players;
import net.slipcor.pvparena.managers.Powerups;
import net.slipcor.pvparena.managers.Settings;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;
import net.slipcor.pvparena.runnables.BoardRunnable;
import net.slipcor.pvparena.runnables.DominationRunnable;
import net.slipcor.pvparena.runnables.PowerupRunnable;
import net.slipcor.pvparena.runnables.StartRunnable;
import net.slipcor.pvparena.runnables.TimedEndRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
 * @version v0.6.21
 * 
 */

public class Arena {
	private Debug db = new Debug(8);

	// global statics: region modify blocks all child arenas
	public static String regionmodify = "";

	// available arena classes mapped to their items: ClassName => itemString
	public final HashMap<String, ItemStack[]> paClassItems = new HashMap<String, ItemStack[]>();
	// available teams mapped to color: TeamName => ColorString
	public final HashMap<String, String> paTeams = new HashMap<String, String>();

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
	public final HashSet<String> paReady = new HashSet<String>();
	public final HashSet<String> paChat = new HashSet<String>();
	public final HashSet<ArenaClassSign> paSigns = new HashSet<ArenaClassSign>();

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
	public boolean edit = false;

	// arena settings
	public boolean usesPowerups;

	// Runnable IDs
	public int SPAWN_ID = -1;
	public int END_ID = -1;
	public int BOARD_ID = -1;
	public int START_ID = -1;

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
		db.i("teleporting all players to their spawns");
		for (String p : pm.getPlayerTeamMap().keySet()) {
			Player z = Bukkit.getServer().getPlayer(p);
			if (!cfg.getBoolean("arenatype.randomSpawn", false)) {
				tpPlayerToCoordName(z, pm.getPlayerTeamMap().get(p) + "spawn");
			} else {
				tpPlayerToCoordName(z, "spawn");
			}
			setPermissions(z);
			playerCount++;
		}

		if (cfg.getBoolean("arenatype.flags")
				|| cfg.getBoolean("arenatype.deathmatch")) {
			Flags.init_arena(this);
		}
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

		ArenaPlayer player = Players.parsePlayer(this, p);
		PermissionAttachment pa = p.addAttachment(PVPArena.instance);
		player.tempPermissions.add(pa);
		for (String entry : perms.keySet()) {
			pa.setPermission(entry, perms.get(entry));
		}
	}

	/**
	 * remove temporary permissions from a player
	 * 
	 * @param p
	 *            the player to reset
	 */
	private void removePermissions(Player p) {
		ArenaPlayer player = Players.parsePlayer(this, p);
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
		for (ArenaPlayer p : pm.getPlayers()) {
			if (!p.spectator && p.getClass().equals("custom")) {
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

		ArenaPlayer p = Players.parsePlayer(this, player);
		p.exhaustion = player.getExhaustion();
		p.fireticks = player.getFireTicks();
		p.foodlevel = player.getFoodLevel();
		p.health = player.getHealth();
		p.saturation = player.getSaturation();
		p.location = player.getLocation();
		p.gamemode = player.getGameMode().getValue();
		p.potionEffects = player.getActivePotionEffects();

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
		for (PotionEffect pe : player.getActivePotionEffects()) {
			player.removePotionEffect(pe.getType());
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
		if (place.equals("spectator")) {
			Players.parsePlayer(this, player).spectator = true;
			Players.parsePlayer(this, player).team = "";
		}
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
		pm.setTeam(player, "");
		pm.remove(player);
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

		removePermissions(player);
		ArenaPlayer ap = Players.parsePlayer(this, player);
		player.setFireTicks(ap.fireticks > 0 ? ap.fireticks : 1);
		player.setFoodLevel(ap.foodlevel);
		player.setHealth(ap.health);
		player.setSaturation(ap.saturation);
		player.setGameMode(GameMode.getByValue(ap.gamemode));
		if (cfg.getBoolean("messages.colorNick", true)) {
			player.setDisplayName(ap.displayname);
		}

		for (PotionEffect pe : player.getActivePotionEffects()) {
			player.removePotionEffect(pe.getType());
		}

		player.addPotionEffects(ap.potionEffects);

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
		db.i("forcing arena to stop");
		for (ArenaPlayer p : pm.getPlayers()) {
			removePlayer(p.get(), "spectator");
			p.spectator = true;
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
			pm.tellEveryone(Language.parse("serverpowerup", p.name));
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
		ArenaPlayer ap = Players.parsePlayer(this, player);
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
			pm.tellEveryone(Language.parse("killedby", ChatColor.valueOf(color)
					+ player.getName() + ChatColor.YELLOW,
					Players.parseDeathCause(this, player, cause, damager)));
			tpPlayerToCoordName(player, sTeam + "spawn");

			Flags.checkEntityDeath(this, player);
		} else if (!cfg.getBoolean("arenatype.deathmatch")) {
			pm.tellEveryone(Language.parse("killedbylives",
					ChatColor.valueOf(color) + player.getName()
							+ ChatColor.YELLOW,
					Players.parseDeathCause(this, player, cause, damager),
					String.valueOf(lives)));
			paLives.put(player.getName(), lives);
		}
		if (!cfg.getBoolean("arenatype.randomSpawn", false) && color != null
				&& !sTeam.equals("free")) {
			tpPlayerToCoordName(player, sTeam + "spawn");
		} else {
			tpPlayerToCoordName(player, "spawn");
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
		db.i("colorizing player " + player.getName() + "; color " + color);

		if (color.equals("")) {
			player.setDisplayName(player.getName());

			if (PVPArena.spoutHandler != null)
				SpoutManager.getAppearanceManager().setGlobalTitle(player,
						player.getName());

			return;
		}

		String n = color + player.getName();
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
		if (PVPArena.eco != null) {
			for (String nKey : pm.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (nSplit[1].equalsIgnoreCase(player.getName())) {
					double playerFactor = playerCount
							* cfg.getDouble("money.betPlayerWinFactor");

					if (playerFactor <= 0) {
						playerFactor = 1;
					}

					playerFactor *= cfg.getDouble("money.betWinFactor");

					double amount = pm.paPlayersBetAmount.get(nKey)
							* playerFactor;

					MethodAccount ma = PVPArena.eco.getAccount(nSplit[0]);
					ma.add(amount);
					try {
						Announcement.announce(this, type.PRIZE, Language.parse(
								"awarded", PVPArena.eco.format(cfg.getInt(
										"money.reward", 0))));
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

		if ((PVPArena.eco != null) && (cfg.getInt("money.reward", 0) > 0)) {
			MethodAccount ma = PVPArena.eco.getAccount(player.getName());
			ma.add(cfg.getInt("money.reward", 0));
			Arenas.tellPlayer(
					player,
					Language.parse("awarded",
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
		db.i("resetting arena; force: " + String.valueOf(force));
		clearArena();
		paReady.clear();
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
		pm.reset(this, force);
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
	 * return the arena type
	 * 
	 * @return the arena type name
	 */
	public String getType() {
		if (!cfg.getBoolean("arenatype.teams")) {
			return "free";
		}
		if (cfg.getBoolean("arenatype.flags")) {
			if (cfg.getBoolean("arenatype.domination")) {
				return "dom";
			}
			if (cfg.getBoolean("arenatype.pumpkin")) {
				return "pumpkin";
			}
			return "ctf";
		}
		if (cfg.getBoolean("arenatype.deathmatch")) {
			return "dm";
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

	/**
	 * handle a deathmatch frag
	 * 
	 * @param attacker
	 *            the player to count a frag
	 */
	public void deathMatch(Player attacker) {
		if (!cfg.getBoolean("arenatype.deathmatch")) {
			return; // no deathmatch, out!
		}
		db.i("handling deathmatch flag");

		String sTeam = pm.getTeam(attacker);
		if (sTeam.equals("")) {
			return; // no team => out
		}

		if (Flags.reduceLivesCheckEndAndCommit(this, sTeam)) {
			return;
		}
		String sColoredPlayer = ChatColor.valueOf(paTeams.get(sTeam))
				+ attacker.getName() + ChatColor.YELLOW;

		pm.tellEveryone(Language.parse("frag", sColoredPlayer,
				String.valueOf(cfg.getInt("game.lives") - paLives.get(sTeam))));
	}

	public int countActiveTeams() {
		db.i("counting active teams");
		List<String> activeteams = new ArrayList<String>(0);
		HashMap<String, String> test = pm.getPlayerTeamMap();
		for (String sPlayer : test.keySet()) {
			db.i("player " + sPlayer);
			if (activeteams.size() < 1) {
				// fresh map
				String team = test.get(sPlayer);
				db.i("is in team " + team);
				activeteams.add(team);
			} else {
				// map contains stuff
				if (!activeteams.contains(test.get(sPlayer))) {
					activeteams.add(test.get(sPlayer));
				}
			}
		}
		db.i("result: " + activeteams.size());
		return activeteams.size();
	}

	public void countDown() {
		if (START_ID != -1 || this.fightInProgress) {
			return;
		}
		
		long duration = 20L*5;
		START_ID = Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new StartRunnable(this), duration);
		this.pm.tellEveryone(Language.parse("starting"));
	}

	public void start() {
		START_ID = -1;
		
		teleportAllToSpawn();
		fightInProgress = true;
		pm.tellEveryone(Language.parse("begin"));
		Announcement.announce(this, type.START,
				Language.parse("begin"));
	}
}
