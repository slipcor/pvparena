package net.slipcor.pvparena.arena;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.definitions.ArenaClassSign;
import net.slipcor.pvparena.events.PAEndEvent;
import net.slipcor.pvparena.events.PAExitEvent;
import net.slipcor.pvparena.events.PAJoinEvent;
import net.slipcor.pvparena.events.PALeaveEvent;
import net.slipcor.pvparena.events.PAStartEvent;
import net.slipcor.pvparena.managers.Configs;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Settings;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionType;
import net.slipcor.pvparena.neworder.ArenaType;
import net.slipcor.pvparena.runnables.SpawnCampRunnable;
import net.slipcor.pvparena.runnables.StartRunnable;
import net.slipcor.pvparena.runnables.TimedEndRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
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
 * @version v0.8.4
 * 
 */

public class Arena {
	private Debug db = new Debug(8);
	private final HashSet<ArenaTeam> teams = new HashSet<ArenaTeam>();
	private final HashSet<ArenaClass> classes = new HashSet<ArenaClass>();

	public final HashSet<ArenaClassSign> signs = new HashSet<ArenaClassSign>();
	public final HashSet<String> chatters = new HashSet<String>();
	public final HashMap<String, Integer> lives = new HashMap<String, Integer>(); // flags
	public final HashMap<String, ArenaRegion> regions = new HashMap<String, ArenaRegion>();

	private ArenaType type;

	// global statics: region modify blocks all child arenas
	public static String regionmodify = "";

	public Settings sm;
	public String name = "default";
	public String prefix = "PVP Arena";
	public String owner = "%server%";

	public Location pos1; // temporary position 1 (region select)
	public Location pos2; // temporary position 2 (region select)

	// arena status
	public boolean fightInProgress = false;
	public boolean edit = false;

	// Runnable IDs
	public int END_ID = -1;
	public int REALEND_ID = -1;
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
	 * @param type
	 *            the arena type
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
	 * add an ArenaClass to the arena
	 * 
	 * @param className
	 *            the class name
	 * @param items
	 *            the class items
	 */
	public void addClass(String className, ItemStack[] items) {
		classes.add(new ArenaClass(className, items));
	}

	/**
	 * does a class exist?
	 * 
	 * @param className
	 *            the name to find
	 * @return true, if the class exists
	 */
	public boolean classExists(String className) {
		for (ArenaClass ac : classes) {
			if (ac.getName().equalsIgnoreCase(className)) {
				return true;
			}
		}
		return false;
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
		for (ArenaRegion region : regions.values()) {
			region.reset();
		}
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
	 * initiate the arena start countdown
	 */
	public void countDown() {
		if (START_ID != -1 || this.fightInProgress) {
			Bukkit.getScheduler().cancelTask(START_ID);
			START_ID = -1;
			if (!this.fightInProgress) {
				tellEveryone(Language.parse("countdowninterrupt"));
			}
			return;
		}

		int duration = cfg.getInt("start.countdown");
		START_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, new StartRunnable(this, duration), 20L, 20L);
		tellEveryone(Language.parse("startingin", String.valueOf(cfg.getInt("start.countdown"))));
	}

	/**
	 * count all players being ready
	 * 
	 * @param arena
	 *            the arena to count
	 * @return the number of ready players
	 */
	public int countReadyPlayers() {
		int sum = 0;
		for (ArenaTeam team : getTeams()) {
			for (ArenaPlayer p : team.getTeamMembers()) {
				if (p.getStatus() == Status.READY) {
					sum++;
				}
			}
		}
		db.i("counting ready players: " + sum);
		return sum;
	}

	/**
	 * force choose a player class
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the player to set
	 * @param sign
	 *            the arena sign belonging to that class name
	 * @param className
	 *            the class name
	 */
	public void forceChooseClass(Player player, Sign sign, String className) {

		db.i("forcing player class");

		if (sign != null) {

			boolean classperms = false;
			if (cfg.get("general.classperms") != null) {
				classperms = cfg.getBoolean("general.classperms", false);
			}

			if (classperms) {
				db.i("checking class perms");
				if (!(PVPArena.hasPerms(player, "pvparena.class." + className))) {
					Arenas.tellPlayer(player, Language.parse("classperms"),
							this);
					return; // class permission desired and failed =>
							// announce and OUT
				}
			}

			if (cfg.getBoolean("general.signs")) {
				ArenaClassSign.remove(signs, player);
				Block block = sign.getBlock();
				ArenaClassSign as = ArenaClassSign.used(block.getLocation(),
						signs);
				if (as == null) {
					as = new ArenaClassSign(block.getLocation());
				}
				signs.add(as);
				if (!as.add(player)) {
					Arenas.tellPlayer(player, Language.parse("classfull"), this);
					return;
				}
			}
		}
		Inventories.clearInventory(player);
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		if (ap.getArena() == null) {
			System.out.print("[PA-debug] failed to set class " + className
					+ " to player " + player.getName());
			return;
		}
		ap.setClass(className);
		if (className.equalsIgnoreCase("custom")) {
			// if custom, give stuff back
			Inventories.loadInventory(this, player);
		} else {
			Inventories.givePlayerFightItems(this, player);
		}
	}

	/**
	 * force stop an arena
	 */
	public void forcestop() {
		db.i("forcing arena to stop");
		for (ArenaPlayer p : getPlayers()) {
			removePlayer(p.get(), "spectator", false);
			p.setStatus(Status.WATCH);
		}
		reset(true);
	}

	/**
	 * get all classes
	 * 
	 * @return all ArenaClass instances
	 */
	public HashSet<ArenaClass> getClasses() {
		return classes;
	}

	/**
	 * get the respawn location of a dead player
	 * 
	 * @param player
	 *            the dead player
	 * @return the respawn location
	 */
	public Location getDeadLocation(Player player) {
		String string = null;
		db.i("fetching dead player's location");
		for (ArenaPlayer ap : ArenaPlayer.deadPlayers.keySet()) {
			db.i("checking player: " + ap.get().getName());
			if (ap.get().equals(player)) {
				db.i("there you are!");
				string = ArenaPlayer.deadPlayers.get(ap);
				db.i("plaayer will spawn at: " + string);
				if (string.equalsIgnoreCase("old")) {
					return ap.location;
				} else {
					return Spawns.getCoords(this, string);
				}
			}
		}
		return null;
	}

	/**
	 * fetch a dead arena player
	 * 
	 * @param player
	 *            the player to fetch
	 * @return the instance of the dead arena player
	 */
	public ArenaPlayer getDeadPlayer(Player player) {
		for (ArenaPlayer ap : ArenaPlayer.deadPlayers.keySet()) {
			if (ap.get().equals(player)) {
				db.i("successfully fetching dead player");
				return ap;
			}
		}
		return null;
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
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		return ap.location;
	}

	/**
	 * hand over all players
	 * 
	 * @return all players
	 */
	public HashSet<ArenaPlayer> getPlayers() {

		HashSet<ArenaPlayer> players = new HashSet<ArenaPlayer>();

		for (ArenaTeam team : getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				players.add(ap);
			}
		}
		return players;
	}

	/**
	 * hand over all teams
	 * 
	 * @return the arena teams
	 */
	public HashSet<ArenaTeam> getTeams() {
		return teams;
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
	 * give customized rewards to players
	 * 
	 * @param player
	 *            the player to give the reward
	 */
	public void giveRewards(Player player) {
		db.i("giving rewards to " + player.getName());
		PVPArena.instance.getAmm().giveRewards(this, player);
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
				Arenas.tellPlayer(player, Language.parse("invfull"), this);
				return;
			}
		}
	}

	/**
	 * check if a custom class player is alive
	 * 
	 * @return true if there is a custom class player alive, false otherwise
	 */
	public boolean isCustomClassActive() {
		for (ArenaPlayer p : getPlayers()) {
			if (p.getStatus().equals(Status.FIGHT)
					&& p.getClass().equals("custom")) {
				db.i("custom class active: true");
				return true;
			}
		}
		db.i("custom class active: false");
		return false;
	}

	/**
	 * check if a player is known
	 * 
	 * @param pPlayer
	 *            the player to find
	 * @return true if the player is known, false otherwise
	 */
	public boolean isPartOf(Player pPlayer) {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(pPlayer);
		return getPlayers().contains(ap);
	}

	/**
	 * check for arena region quitters, make them leave if possible and
	 * necessary
	 */
	public void leaveCheck() {
		if (!this.regions.containsKey("battlefield")) {
			db.i("region battlefield not set, aborting quit check");
			return;
		}
		if (!this.regions.containsKey("spectator")) {
			db.i("region spectator not set, aborting quit check");
			return;
		}
		HashSet<ArenaPlayer> plyrs = new HashSet<ArenaPlayer>();
		for (ArenaPlayer ap : getPlayers()) {
			plyrs.add(ap);
		}

		for (ArenaPlayer ap : plyrs) {
			if (!this.contains(ap.get().getLocation())) {
				playerLeave(ap.get());
			}
		}
	}

	/**
	 * return an understandable representation of a player's death cause
	 * 
	 * @param player
	 *            the dying player
	 * @param cause
	 *            the cause
	 * @param damager
	 *            an eventual damager entity
	 * @return a colored string
	 */
	public String parseDeathCause(Player player, DamageCause cause,
			Entity damager) {

		db.i("return a damage name for : " + cause.toString());
		ArenaPlayer ap = null;
		ArenaTeam team = null;

		db.i("damager: " + damager);

		if (damager instanceof Player) {
			ap = ArenaPlayer.parsePlayer((Player) damager);
			team = Teams.getTeam(this, ap);
		}

		switch (cause) {
		case ENTITY_ATTACK:
			if ((damager instanceof Player) && (team != null)) {
				return team.colorizePlayer(ap.get()) + ChatColor.YELLOW;
			}
			return Language.parse("custom");
		case PROJECTILE:
			if ((damager instanceof Player) && (team != null)) {
				return team.colorizePlayer(ap.get()) + ChatColor.YELLOW;
			}
			return Language.parse(cause.toString().toLowerCase());
		default:
			return Language.parse(cause.toString().toLowerCase());
		}
	}

	/**
	 * a player leaves from the arena
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the leaving player
	 */
	public void playerLeave(Player player) {
		db.i("fully removing player from arena");
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);

		boolean fighter = ap.getStatus().equals(Status.FIGHT);

		if (fighter) {
			ArenaTeam team = Teams.getTeam(this, ap);
			if (team != null) {
				PVPArena.instance.getAmm().playerLeave(this, player, team);
	
				tellEveryoneExcept(
						player,
						Language.parse("playerleave", team.colorizePlayer(player)
								+ ChatColor.YELLOW));
			}
			Arenas.tellPlayer(player, Language.parse("youleave"), this);
		}
		removePlayer(player, cfg.getString("tp.exit", "exit"), false);

		if (START_ID != -1) {
			Bukkit.getScheduler().cancelTask(START_ID);
			tellEveryone(Language.parse("countdowninterrupt"));
			START_ID = -1;
		}
		ap.reset();

		if (fighter && fightInProgress) {
			Arenas.checkAndCommit(this);
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
	 * prepare a player for fighting. Setting all values to start value
	 * 
	 * @param player
	 * @param ending
	 */
	public void prepare(Player player, boolean spectate, boolean ending) {
		if (ending) {
			db.i("putting a player to the spectator spawn");
		} else {
			PAJoinEvent event = new PAJoinEvent(this, player, spectate);
			Bukkit.getPluginManager().callEvent(event);
			db.i("preparing player: " + player.getName());

			ArenaPlayer ap = ArenaPlayer.parsePlayer(player);

			ap.setArena(this);

			saveMisc(player); // save player health, fire tick, hunger etc
		}

		playersetHealth(player, cfg.getInt("start.health", 0));
		player.setFireTicks(0);
		player.setFoodLevel(cfg.getInt("start.foodLevel", 20));
		player.setSaturation(cfg.getInt("start.saturation", 20));
		player.setExhaustion((float) cfg.getDouble("start.exhaustion", 0.0));
		player.setLevel(0);
		player.setExp(0);
		player.setGameMode(GameMode.getByValue(0));
		for (PotionEffect pe : player.getActivePotionEffects()) {
			//player.addPotionEffect(new PotionEffect(pe.getType(), 0, 0));
			player.removePotionEffect(pe.getType());
		}

	}

	/**
	 * check if an arena is ready
	 * 
	 * @param arena
	 *            the arena to check
	 * @return 1 if the arena is ready 0 if at least one player not ready -1 if
	 *         player is the only player -2 if only one team active -3 if not
	 *         enough players in a team -4 if not enough players -5 if at least
	 *         one player not selected class, -6 if counting down
	 */
	public int ready() {
		int players = Teams.countPlayersInTeams(this);
		if (players < 2) {
			return -1;
		}
		if (players < cfg.getInt("ready.min")) {
			return -4;
		}

		if (cfg.getBoolean("ready.checkEach")) {
			for (ArenaTeam team : getTeams()) {
				for (ArenaPlayer ap : team.getTeamMembers())
					if (!ap.getStatus().equals(Status.READY)) {
						return 0;
					}
			}
		}

		int arenaTypeCheck = type.ready(this);
		if (arenaTypeCheck != 1) {
			return arenaTypeCheck;
		}

		for (ArenaTeam team : getTeams()) {
			for (ArenaPlayer p : team.getTeamMembers()) {
				db.i("checking class: " + p.get().getName());

				if (p.getaClass() == null) {
					db.i("player has no class");
					// player no class!
					return -5;
				}
			}
		}
		int readyPlayers = countReadyPlayers();

		if (players > readyPlayers) {
			double ratio = cfg.getDouble("ready.startRatio");
			db.i("ratio: " + String.valueOf(ratio));
			if (ratio > 0) {
				double aRatio = Float.valueOf(readyPlayers) / Float.valueOf(players);
				if ((players > 0) && (aRatio >= ratio)) {
					return -6;
				}
			}
			return 0;
		}
		return 1;
	}

	/**
	 * remove a player from the arena
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void remove(Player player) {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		PALeaveEvent event = new PALeaveEvent(this, player, ap.getStatus()
				.equals(Status.FIGHT));
		Bukkit.getPluginManager().callEvent(event);
		PAExitEvent exitEvent = new PAExitEvent(this, player);
		Bukkit.getPluginManager().callEvent(exitEvent);
		if (!ap.isDead())
			ArenaPlayer.parsePlayer(player).setArena(null);
	}

	/**
	 * remove the dead player from the map
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void removeDeadPlayer(Player player) {
		resetPlayer(player, cfg.getString("tp.death", "spectator"), false);
		ArenaPlayer tempAP = null;
		for (ArenaPlayer ap : ArenaPlayer.deadPlayers.keySet()) {
			if (ap.get().equals(player)) {
				tempAP = ap;
				if (ap.getArena() != null) {
					ap.getArena().resetPlayer(
							player,
							ap.getArena().cfg
									.getString("tp.death", "spectator"), false);
					ap.setArena(null);
				} else {
					System.out.print("[PA-debug] Arena NULL: "
							+ player.getName());
				}
				break;
			}
		}
		ArenaPlayer.deadPlayers.remove(tempAP);
	}

	/**
	 * remove a player from the arena
	 * 
	 * @param player
	 *            the player to reset
	 * @param tploc
	 *            the coord string to teleport the player to
	 */
	public void removePlayer(Player player, String tploc, boolean soft) {
		db.i("removing player " + player.getName() + (soft ? " (soft)" : "")
				+ ", tp to " + tploc);
		resetPlayer(player, tploc, soft);

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		if (!soft) {
			Teams.removeTeam(this, ap);
		}
		remove(player);
		if (cfg.getBoolean("general.signs")) {
			ArenaClassSign.remove(signs, player);
		}
	}

	/**
	 * reset an arena
	 * 
	 * @param force
	 */
	public void reset_players(boolean force) {
		db.i("resetting player manager");
		HashSet<ArenaPlayer> pa = new HashSet<ArenaPlayer>();
		for (ArenaTeam team : this.getTeams()) {
			for (ArenaPlayer p : team.getTeamMembers()) {
				db.i("player: " + p.getName());
				if (p.getArena() == null || !p.getArena().equals(this)) {
					continue;
				} else {
					pa.add(p);
				}
			}
		}

		for (ArenaPlayer p : pa) {
			Player z = p.get();
			if (!force) {
				p.wins++;
			}
			resetPlayer(z, cfg.getString("tp.win", "old"), false);
			if (!force && p.getStatus().equals(Status.FIGHT) && fightInProgress) {
				giveRewards(z); // if we are the winning team, give
								// reward!
			}
			p.reset();
		}
	}

	/**
	 * reset an arena
	 */
	public void reset(boolean force) {

		PAEndEvent event = new PAEndEvent(this);
		Bukkit.getPluginManager().callEvent(event);

		db.i("resetting arena; force: " + String.valueOf(force));
		chatters.clear();
		for (ArenaClassSign as : signs) {
			as.clear();
		}
		signs.clear();
		reset_players(force);
		fightInProgress = false;
		if (END_ID > -1)
			Bukkit.getScheduler().cancelTask(END_ID);
		END_ID = -1;
		if (REALEND_ID > -1)
			Bukkit.getScheduler().cancelTask(REALEND_ID);
		REALEND_ID = -1;

		PVPArena.instance.getAmm().reset(this, force);
		clearArena();
		type.reset(force);

		this.playerCount = 0;
		this.teamCount = 0;
	}

	/**
	 * reset a player to his pre-join values
	 * 
	 * @param player
	 * @param string
	 * @param soft
	 */
	private void resetPlayer(Player player, String string, boolean soft) {
		db.i("resetting player: " + player.getName() + (soft ? "(soft)" : ""));

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		if (player.isDead() && !ap.isDead()) {
			db.i("player is dead");
			ap.addDeadPlayer(string);
			return;
		}
		if (ap.getState() != null) {
			ap.getState().unload();
		}
		PVPArena.instance.getAmm().resetPlayer(this, player);

		db.i("string = " + string);
		ap.setTelePass(true);
		if (string.equalsIgnoreCase("old")) {
			player.teleport(ap.location);
		} else {
			Location l = Spawns.getCoords(this, string);
			player.teleport(l);
		}

		String sClass = "";
		if (ap.getClass() != null) {
			sClass = ap.getClass().getName();
		}
		if (!sClass.equalsIgnoreCase("custom")) {
			Inventories.clearInventory(player);
			Inventories.loadInventory(this, player);
		}
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
		player.setVelocity(new Vector());

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		ArenaTeam team = Teams.getTeam(this, ap);

		if (team == null) {
			return;
		}

		type.parseRespawn(player, team, lives, cause, damager);
		
		PVPArena.instance.getAmm().parseRespawn(this, player, team, lives, cause, damager);

		player.setFireTicks(0);
		player.setNoDamageTicks(60);
		//EntityListener.addBurningPlayer(player);
	}

	/**
	 * save player variables
	 * 
	 * @param player
	 *            the player to save
	 */
	public void saveMisc(Player player) {
		db.i("saving player vars: " + player.getName());

		ArenaPlayer.parsePlayer(player).createState(player);
	}

	/**
	 * set the ArenaType
	 * 
	 * @param type
	 *            the ArenaType to set
	 */
	public void setType(ArenaType type) {
		this.type = type;
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
	 * damage every actively fighting player for being near a spawn
	 */
	public void spawnCampPunish() {

		HashMap<Location, ArenaPlayer> players = new HashMap<Location, ArenaPlayer>();

		for (ArenaPlayer ap : getPlayers()) {
			if (!ap.getStatus().equals(Status.FIGHT)) {
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
						players.get(playerLoc).get().damage(
								cfg.getInt("region.spawncampdamage"));
					}
				}
			}
		}
	}

	/**
	 * initiate the arena start
	 */
	public void start() {
		START_ID = -1;

		teleportAllToSpawn();
		fightInProgress = true;
	}

	/**
	 * teleport all players to their respective spawn
	 */
	public void teleportAllToSpawn() {

		PAStartEvent event = new PAStartEvent(this);
		Bukkit.getPluginManager().callEvent(event);

		db.i("teleporting all players to their spawns");
		for (ArenaTeam team : teams) {
			if (!type.isFreeForAll()) {
				for (ArenaPlayer ap : team.getTeamMembers()) {
					tpPlayerToCoordName(ap.get(), team.getName() + "spawn");
					ap.setStatus(Status.FIGHT);
					playerCount++;
				}
			}
		}

		type.initiate();

		int timed = cfg.getInt("goal.timed");
		if (timed > 0) {
			db.i("arena timing!");
			// initiate autosave timer
			END_ID = Bukkit
					.getServer()
					.getScheduler()
					.scheduleSyncRepeatingTask(PVPArena.instance,
							new TimedEndRunnable(this, timed), 20, 20);
		}

		tellEveryone(Language.parse("begin"));

		PVPArena.instance.getAmm().teleportAllToSpawn(this);

		db.i("teleported everyone!");

		teamCount = Teams.countActiveTeams(this);
		SPAWNCAMP_ID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(
				PVPArena.instance, new SpawnCampRunnable(this), 100L, cfg.getInt("region.timer")*20L);

		for (ArenaRegion region : regions.values()) {
			if (region.getType().equals(RegionType.DEATH)) {
				region.initTimer();
			} else if (region.getType().equals(RegionType.NOCAMP)) {
				region.initTimer();
			}
		}
	}

	/**
	 * send a message to every playery
	 * 
	 * @param msg
	 *            the message to send
	 */
	public void tellEveryone(String msg) {
		db.i("@all: " + msg);
		HashSet<ArenaPlayer> players = getPlayers();
		for (ArenaPlayer p : players) {
			if (p.getArena() == null || !p.getArena().equals(this)) {
				continue;
			}
			Arenas.tellPlayer(p.get(), msg, this);
		}
	}

	/**
	 * send a message to every player, prefix player name and ChatColor
	 * 
	 * @param player
	 *            the team to send to
	 * @param msg
	 *            the message to send
	 * @param player
	 */
	public void tellEveryoneColored(String msg, ChatColor c, Player player) {
		tellEveryone(c + player.getName() + ChatColor.WHITE + ": " + msg);
	}

	/**
	 * send a message to every player except the given one
	 * 
	 * @param player
	 *            the player to exclude
	 * @param msg
	 *            the message to send
	 */
	public void tellEveryoneExcept(Player player, String msg) {
		db.i("@all/" + player.getName() + ": " + msg);
		HashSet<ArenaPlayer> players = getPlayers();
		for (ArenaPlayer p : players) {
			if (p.getArena() == null || !p.getArena().equals(this)) {
				continue;
			}
			if (p.get().equals(player))
				continue;
			Arenas.tellPlayer(p.get(), msg, this);
		}
	}

	/**
	 * send a message to every player of a given team
	 * 
	 * @param player
	 *            the team to send to
	 * @param msg
	 *            the message to send
	 * @param player
	 */
	public void tellTeam(String sTeam, String msg, ChatColor c, Player player) {
		ArenaTeam team = Teams.getTeam(this, sTeam);
		if (team == null) {
			return;
		}
		sTeam = team.getName();
		db.i("@" + sTeam + ": " + msg);
		for (ArenaPlayer p : team.getTeamMembers()) {
			p.get().sendMessage(
					c + "[" + sTeam + "] " + player.getName() + ChatColor.WHITE
							+ ": " + msg);
		}
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
				chatters.add(player.getName());
			}
		}

		PVPArena.instance.getAmm().tpPlayerToCoordName(this, player, place);

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		if (place.equals("spectator")) {
			if (getPlayers().contains(ap)) {
				ap.setStatus(Status.LOSES);
			} else {
				ap.setStatus(Status.WATCH);
			}
		}
		Location loc = Spawns.getCoords(this, place);
		if (loc == null) {
			System.out.print("[PA-debug] Spawn null : " + place);
			return;
		}
		ap.setTelePass(true);
		player.teleport(loc);
		ap.setTelePass(false);
	}

	/**
	 * hand over the ArenaType
	 * 
	 * @return the ArenaType instance
	 */
	public ArenaType type() {
		return this.type;
	}
}
