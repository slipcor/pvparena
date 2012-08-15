package net.slipcor.pvparena.arena;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.definitions.ArenaClassSign;
import net.slipcor.pvparena.events.PAEndEvent;
import net.slipcor.pvparena.events.PAExitEvent;
import net.slipcor.pvparena.events.PAJoinEvent;
import net.slipcor.pvparena.events.PALeaveEvent;
import net.slipcor.pvparena.events.PALoseEvent;
import net.slipcor.pvparena.events.PAStartEvent;
import net.slipcor.pvparena.events.PAWinEvent;
import net.slipcor.pvparena.managers.Configs;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Settings;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.neworder.ArenaGoal;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionType;
import net.slipcor.pvparena.runnables.SpawnCampRunnable;
import net.slipcor.pvparena.runnables.StartRunnable;
import net.slipcor.pvparena.runnables.TimedEndRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
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
 * @version v0.9.0
 * 
 */

public class Arena {
	private Debug db = new Debug(3);
	private final HashSet<ArenaTeam> teams = new HashSet<ArenaTeam>();
	private final HashSet<ArenaClass> classes = new HashSet<ArenaClass>();
	private final HashSet<ArenaGoal> goals = new HashSet<ArenaGoal>();

	private final HashSet<ArenaClassSign> signs = new HashSet<ArenaClassSign>();
	private final HashSet<ArenaRegion> regions = new HashSet<ArenaRegion>();
	
	private Settings sm;

	private static String globalprefix = "PVP Arena";
	private String name = "default";
	private String prefix = "PVP Arena";
	private String owner = "%server%";

	// arena status
	private boolean fightInProgress = false;
	private boolean locked = false;
	private boolean free = false;

	// Runnable IDs
	public int END_ID = -1;
	public int REALEND_ID = -1;
	public int START_ID = -1;
	public int SPAWNCAMP_ID = -1;

	private Config cfg;

	public Arena(String name) {
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
		setArenaConfig(new Config(file));
		getArenaConfig().load();
		Configs.configParse(this, getArenaConfig());

		getArenaConfig().save();

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

	public void addGoal(ArenaGoal goal) {
		goals.add(goal);
	}

	/**
	 * send a message to every playery
	 * 
	 * @param msg
	 *            the message to send
	 */
	public void broadcast(String msg) {
		db.i("@all: " + msg);
		HashSet<ArenaPlayer> players = getFighters();
		for (ArenaPlayer p : players) {
			if (p.getArena() == null || !p.getArena().equals(this)) {
				continue;
			}
			this.msg(p.get(), msg);
		}
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
	public void chooseClass(Player player, Sign sign, String className) {

		db.i("forcing player class");

		if (sign != null) {

			boolean classperms = false;
			if (getArenaConfig().get("general.classperms") != null) {
				classperms = getArenaConfig().getBoolean("general.classperms", false);
			}

			if (classperms) {
				db.i("checking class perms");
				if (!(PVPArena.hasPerms(player, "pvparena.class." + className))) {
					this.msg(player, Language.parse("classperms"));
					return; // class permission desired and failed =>
							// announce and OUT
				}
			}

			if (getArenaConfig().getBoolean("general.signs")) {
				ArenaClassSign.remove(signs, player);
				Block block = sign.getBlock();
				ArenaClassSign as = ArenaClassSign.used(block.getLocation(),
						signs);
				if (as == null) {
					as = new ArenaClassSign(block.getLocation());
				}
				signs.add(as);
				if (!as.add(player)) {
					this.msg(player, Language.parse("classfull"));
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
			ArenaPlayer.reloadInventory(this, player);
		} else {
			ArenaPlayer.givePlayerFightItems(this, player);
		}
	}

	public void clearRegions() {
		for (ArenaRegion region : regions) {
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
		PABlockLocation paloc = new PABlockLocation(loc);
		db.i("checking regions:");
		for (ArenaRegion reg : regions) {
			if (reg.contains(paloc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * initiate the arena start countdown
	 */
	public void countDown() {
		if (START_ID != -1 || this.isFightInProgress()) {
			Bukkit.getScheduler().cancelTask(START_ID);
			START_ID = -1;
			if (!this.isFightInProgress()) {
				broadcast(Language.parse("countdowninterrupt"));
			}
			return;
		}

		int duration = getArenaConfig().getInt("start.countdown");
		StartRunnable sr = new StartRunnable(this, duration, 0);
		START_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, sr, 20L, 20L);
		sr.setId(START_ID);
		broadcast(Language.parse("startingin", String.valueOf(getArenaConfig().getInt("start.countdown"))));
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

	public Config getArenaConfig() {
		return cfg;
	}
	
	public ArenaClass getClass(String className) {
		for (ArenaClass ac : classes) {
			if (ac.getName().equalsIgnoreCase(className)) {
				return ac;
			}
		}
		return null;
	}
	
	public HashSet<ArenaClass> getClasses() {
		return classes;
	}

	/**
	 * hand over everyone being part of the arena
	 * 
	 */
	public HashSet<ArenaPlayer> getEveryone() {

		HashSet<ArenaPlayer> players = new HashSet<ArenaPlayer>();

		for (ArenaPlayer ap : ArenaPlayer.getPlayers()) {
			if (this.equals(ap.getArena())) {
				players.add(ap);
			}
		}
		return players;
	}

	/**
	 * hand over all players being member of a team
	 * 
	 */
	public HashSet<ArenaPlayer> getFighters() {

		HashSet<ArenaPlayer> players = new HashSet<ArenaPlayer>();

		for (ArenaTeam team : getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				players.add(ap);
			}
		}
		return players;
	}
	
	public HashSet<ArenaGoal> getGoals() {
		return goals;
	}

	public String getName() {
		return name;
	}

	public String getOwner() {
		return owner;
	}

	public String getPrefix() {
		return prefix;
	}
	
	public ArenaRegion getRegion(String name) {
		for (ArenaRegion region : regions) {
			if (region.getName().equalsIgnoreCase(name)) {
				return region;
			}
		}
		return null;
	}

	public HashSet<ArenaRegion> getRegions() {
		return regions;
	}

	public Settings getSettingsManager() {
		return sm;
	}
	
	public ArenaTeam getTeam(String name) {
		for (ArenaTeam team : getTeams()) {
			if (team.getName().equalsIgnoreCase(name)) {
				return team;
			}
		}
		return null;
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
	 * hand over all teams
	 * 
	 * @return the arena teams
	 */
	public HashSet<String> getTeamNames() {
		HashSet<String> result = new HashSet<String>();
		for (ArenaTeam team : teams) {
			result.add(team.getName());
		}
		return result;
	}

	/**
	 * return the arena world
	 * 
	 * @return the world name
	 */
	public String getWorld() {
		return getArenaConfig().getString("general.world");
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
		String sItems = getArenaConfig().getString("general.item-rewards", "none");
		if (sItems.equals("none"))
			return;
		String[] items = sItems.split(",");
		boolean random = getArenaConfig().getBoolean("general.random-reward");
		Random r = new Random();
		
		PAWinEvent dEvent = new PAWinEvent(this, player, items);
		Bukkit.getPluginManager().callEvent(dEvent);
		
		items = dEvent.getItems();
		
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
				this.msg(player, Language.parse("invfull"));
				return;
			}
		}
	}
	
	public void goalAdd(ArenaGoal goal) {
		goals.add(goal);
	}

	public void goalRemove(ArenaGoal goal) {
		goals.remove(goal);
	}

	public boolean goalToggle(ArenaGoal goal) {
		if (goals.contains(goal)) {
			goals.remove(goal);
		} else {
			goals.add(goal);
		}
		return goals.contains(goal);
	}

	/**
	 * check if a custom class player is alive
	 * 
	 * @return true if there is a custom class player alive, false otherwise
	 */
	public boolean isCustomClassAlive() {
		for (ArenaPlayer p : getFighters()) {
			if (p.getStatus().equals(Status.FIGHT)
					&& p.getClass().equals("custom")) {
				db.i("custom class active: true");
				return true;
			}
		}
		db.i("custom class active: false");
		return false;
	}
	
	public boolean hasPlayer(Player p) {
		for (ArenaTeam team : teams) {
			if (team.hasPlayer(p)) {
				return true;
			}
		}
		return false;
	}

	public boolean isFightInProgress() {
		return fightInProgress;
	}

	public boolean isFreeForAll() {
		return free;
	}

	public boolean isLocked() {
		return locked;
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
		return getFighters().contains(ap);
	}

	public void msg(CommandSender sender, String msg) {
		Arenas.db.i("@" + sender.getName() + ": " + msg);
		sender.sendMessage(ChatColor.YELLOW + "[" + prefix + "] "
				+ ChatColor.WHITE + msg);
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
	public void playerLeave(Player player, String location) {
		db.i("fully removing player from arena");
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);

		boolean fighter = ap.getStatus().equals(Status.FIGHT);

		if (fighter) {
			ArenaTeam team = Teams.getTeam(this, ap);
			if (team != null) {
				PVPArena.instance.getAmm().playerLeave(this, player, team);
	
				if (!location.equals("exit")) {
				tellEveryoneExcept(
						player,
						Language.parse("playerleave", team.colorizePlayer(player)
								+ ChatColor.YELLOW));
				} 
			}
			this.msg(player, Language.parse("youleave"));
		}
		removePlayer(player, getArenaConfig().getString("tp." + location), false);

		if (START_ID != -1) {
			Bukkit.getScheduler().cancelTask(START_ID);
			broadcast(Language.parse("countdowninterrupt"));
			START_ID = -1;
		}
		ap.reset();

		if (fighter && isFightInProgress()) {
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

		playersetHealth(player, getArenaConfig().getInt("start.health", 0));
		player.setFireTicks(0);
		player.setFoodLevel(getArenaConfig().getInt("start.foodLevel", 20));
		player.setSaturation(getArenaConfig().getInt("start.saturation", 20));
		player.setExhaustion((float) getArenaConfig().getDouble("start.exhaustion", 0.0));
		player.setLevel(0);
		player.setExp(0);
		player.setGameMode(GameMode.getByValue(0));
		PlayerState.removeEffects(player);

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
		
		//TODO return an error string or null if ok
		int players = Teams.countPlayersInTeams(this);
		if (players < 2) {
			return -1;
		}
		if (players < getArenaConfig().getInt("ready.min")) {
			return -4;
		}

		if (getArenaConfig().getBoolean("ready.checkEach")) {
			for (ArenaTeam team : getTeams()) {
				for (ArenaPlayer ap : team.getTeamMembers())
					if (!ap.getStatus().equals(Status.READY)) {
						return 0;
					}
			}
		}
		//TODO
		/*
		int arenaTypeCheck = type.ready(this);
		if (arenaTypeCheck != 1) {
			return arenaTypeCheck;
		}*/

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
			double ratio = getArenaConfig().getDouble("ready.startRatio");
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

	public void removeClass(String string) {
		for (ArenaClass ac : classes) {
			if (ac.equals(string)) {
				classes.remove(ac);
				return;
			}
		}
	}

	/**
	 * remove the dead player from the map
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void removeDeadPlayer(Player player) {
		resetPlayer(player, getArenaConfig().getString("tp.death", "spectator"), false);
		ArenaPlayer tempAP = null;
		for (ArenaPlayer ap : ArenaPlayer.deadPlayers.keySet()) {
			if (ap.get().equals(player)) {
				tempAP = ap;
				if (ap.getArena() != null) {
					ap.getArena().resetPlayer(
							player,
							ap.getArena().getArenaConfig()
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
		if (getArenaConfig().getBoolean("general.signs")) {
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
			if (p.getStatus() != null && p.getStatus().equals(Status.FIGHT)) {
				Player z = p.get();
				
				if (!force) {
					p.wins++;
				}
				resetPlayer(z, getArenaConfig().getString("tp.win", "old"), false);
				if (!force && p.getStatus().equals(Status.FIGHT) && isFightInProgress()) {
					giveRewards(z); // if we are the winning team, give
									// reward!
				}
				p.reset();
			} else if (p.getStatus() != null && (p.getStatus().equals(Status.DEAD) || p.getStatus().equals(Status.LOST))){
				
				PALoseEvent e = new PALoseEvent(this, p.get());
				Bukkit.getPluginManager().callEvent(e);
				
				Player z = p.get();
				if (!force) {
					p.losses++;
				}
				resetPlayer(z, getArenaConfig().getString("tp.lose", "old"), false);
				p.reset();
			} else {
				resetPlayer(p.get(), getArenaConfig().getString("tp.lose", "exit"), false);
			}
			
			
		}
	}

	/**
	 * reset an arena
	 */
	public void reset(boolean force) {

		PAEndEvent event = new PAEndEvent(this);
		Bukkit.getPluginManager().callEvent(event);

		db.i("resetting arena; force: " + String.valueOf(force));
		for (ArenaClassSign as : signs) {
			as.clear();
		}
		signs.clear();
		reset_players(force);
		setFightInProgress(false);
		
		if (END_ID > -1)
			Bukkit.getScheduler().cancelTask(END_ID);
		END_ID = -1;
		if (REALEND_ID > -1)
			Bukkit.getScheduler().cancelTask(REALEND_ID);
		REALEND_ID = -1;
		
		PVPArena.instance.getAmm().reset(this, force);
		clearRegions();
		PVPArena.instance.getAtm().reset(this, force);
	}

	/**
	 * reset a player to his pre-join values
	 * 
	 * @param player
	 * @param string
	 * @param soft
	 */
	private void resetPlayer(Player player, String string, boolean soft) {
		if (player == null) {
			return;
		}
		db.i("resetting player: " + player.getName() + (soft ? "(soft)" : ""));

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		if (ap.getState() != null) {
			ap.getState().unload(player);
		}
		PVPArena.instance.getAmm().resetPlayer(this, player);

		String sClass = "";
		if (ap.getaClass() != null) {
			sClass = ap.getaClass().getName();
		}
		
		if (!sClass.equalsIgnoreCase("custom")) {
			Inventories.clearInventory(player);
			ArenaPlayer.reloadInventory(this, player);
		}
		
		db.i("string = " + string);
		ap.setTelePass(true);
		if (string.equalsIgnoreCase("old")) {
			player.teleport(ap.location.getLocation());
		} else {
			PALocation l = Spawns.getCoords(this, string);
			player.teleport(l.getLocation());
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
		playersetHealth(player, getArenaConfig().getInt("start.health", 0));
		player.setFoodLevel(getArenaConfig().getInt("start.foodLevel", 20));
		player.setSaturation(getArenaConfig().getInt("start.saturation", 20));
		player.setExhaustion((float) getArenaConfig().getDouble("start.exhaustion", 0.0));
		player.setVelocity(new Vector());

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		ArenaTeam team = Teams.getTeam(this, ap);

		if (team == null) {
			return;
		}

		PVPArena.instance.getAtm().parseRespawn(this, player, team, lives, cause, damager);
		
		PlayerState.removeEffects(player);
		
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
	
	public void selectClass(ArenaPlayer ap, String cName) {
		for (ArenaClass c : classes) {
			if (c.getName().equalsIgnoreCase(cName)) {
				ap.setArenaClass(c);
				ArenaClass.equip(ap.get(), c.getItems());
				msg(ap.get(), Language.parse("class.yourclass", c.getName())); //TODO lang
			}
		}
		msg(ap.get(), Language.parse("class.notfound", cName)); //TODO lang
	}

	public void setArenaConfig(Config cfg) {
		this.cfg = cfg;
	}

	public void setFightInProgress(boolean fightInProgress) {
		this.fightInProgress = fightInProgress;
	}

	public void setFree(boolean b) {
		free = b;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSettingsManager(Settings sm) {
		this.sm = sm;
	}

	/**
	 * set the arena world
	 * 
	 * @param sWorld
	 *            the world name
	 */
	public void setWorld(String sWorld) {
		getArenaConfig().set("general.world", sWorld);
		getArenaConfig().save();
	}

	/**
	 * damage every actively fighting player for being near a spawn
	 */
	public void spawnCampPunish() {

		HashMap<Location, ArenaPlayer> players = new HashMap<Location, ArenaPlayer>();

		for (ArenaPlayer ap : getFighters()) {
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
			for (PALocation spawnLoc : Spawns.getSpawns(this, sTeam)) {
				for (Location playerLoc : players.keySet()) {
					if (spawnLoc.getDistance(new PALocation(playerLoc)) < 3) {
						players.get(playerLoc).get().damage(
								getArenaConfig().getInt("region.spawncampdamage"));
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
		if (isFightInProgress()) {
			return;
		}
		int sum = 0;
		for (ArenaTeam team : getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.LOUNGE) || ap.getStatus().equals(Status.READY)) {
					sum++;
				}
			}
		}
		if (sum < 2) {
			for (ArenaPlayer ap : getFighters()) {
				playerLeave(ap.get(), "exit");
			}
		} else {
			teleportAllToSpawn();
			setFightInProgress(true);
		}
	}
	
	public void stop(boolean force) {
		for (ArenaPlayer p : getFighters()) {
			this.playerLeave(p.get(), "exit");
		}
		reset(force);
	}

	/**
	 * teleport all players to their respective spawn
	 */
	public void teleportAllToSpawn() {

		PAStartEvent event = new PAStartEvent(this);
		Bukkit.getPluginManager().callEvent(event);

		db.i("teleporting all players to their spawns");
		if (!isFreeForAll()) {
			for (ArenaTeam team : teams) {
				for (ArenaPlayer ap : team.getTeamMembers()) {
					tpPlayerToCoordName(ap.get(), team.getName() + "spawn");
					ap.setStatus(Status.FIGHT);
				}
			}
		} else {
			
		}
		//TODO move to FFA-like goals 
		

		PVPArena.instance.getAtm().teleportAllToSpawn(this);

		int timed = getArenaConfig().getInt("goal.timed");
		if (timed > 0) {
			db.i("arena timing!");
			// initiate autosave timer
			TimedEndRunnable ter = new TimedEndRunnable(this, timed, 0);
			END_ID = Bukkit
					.getServer()
					.getScheduler()
					.scheduleSyncRepeatingTask(PVPArena.instance,
							ter, 20, 20);
			ter.setId(END_ID);
		}

		broadcast(Language.parse("begin"));

		PVPArena.instance.getAmm().teleportAllToSpawn(this);

		db.i("teleported everyone!");

		//TODO teamCount = Teams.countActiveTeams(this);
		SpawnCampRunnable scr = new SpawnCampRunnable(this,0);
		SPAWNCAMP_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, scr, 100L, getArenaConfig().getInt("region.timer")*20L);
		scr.setId(SPAWNCAMP_ID);

		for (ArenaRegion region : regions) {
			if (region.getFlags().size() > 0) {
				region.initTimer();
			} else if (region.getType().equals(RegionType.BATTLE)) {
				region.initTimer();
			}
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
	public synchronized void tellEveryoneColored(String msg, ChatColor c, Player player) {
		broadcast(c + player.getName() + ChatColor.WHITE + ": " + msg);
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
		HashSet<ArenaPlayer> players = getFighters();
		for (ArenaPlayer p : players) {
			if (p.getArena() == null || !p.getArena().equals(this)) {
				continue;
			}
			if (p.get().equals(player))
				continue;
			this.msg(p.get(), msg);
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
	public synchronized void tellTeam(String sTeam, String msg, ChatColor c, Player player) {
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
		
		if (player.isInsideVehicle()) {
			player.getVehicle().eject();
		}
		
		if (place.endsWith("lounge")) {
			// at the start of the match
			if (getArenaConfig().getBoolean("messages.defaultChat")
					&& getArenaConfig().getBoolean("messages.chat")) {
				ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
				ap.setChatting(true);
			}
		}

		PVPArena.instance.getAmm().tpPlayerToCoordName(this, player, place);

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		if (place.equals("spectator")) {
			if (getFighters().contains(ap)) {
				ap.setStatus(Status.LOST);
			} else {
				ap.setStatus(Status.WATCH);
			}
		}
		PALocation loc = Spawns.getCoords(this, place);
		if (loc == null) {
			System.out.print("[PA-debug] Spawn null : " + place);
			return;
		}
		ap.setTelePass(true);
		player.teleport(loc.getLocation());
		ap.setTelePass(false);
	}

	public static void pmsg(CommandSender sender, String msg) {
		Arenas.db.i("@" + sender.getName() + ": " + msg);
		sender.sendMessage(ChatColor.YELLOW + "[" + globalprefix + "] "
				+ ChatColor.WHITE + msg);
	}

	public void addRegion(ArenaRegion create) {
		this.regions.add(create);
	}

	public void spawnSet(String node, PALocation paLocation) {
		cfg.set("spawns." + node, Config.parseToString(paLocation));
	}

	public void spawnUnset(String node) {
		cfg.set("spawns." + node, null);
	}

	public PALocation getSpawn(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRandomTeam() {
		// TODO Auto-generated method stub
		return null;
	}

	public void getLegacyGoals(String string) {
		// TODO Auto-generated method stub
		
	}
}
