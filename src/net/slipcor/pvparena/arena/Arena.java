package net.slipcor.pvparena.arena;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PAClassSign;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.classes.PARoundMap;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.events.PAEndEvent;
import net.slipcor.pvparena.events.PAExitEvent;
import net.slipcor.pvparena.events.PAJoinEvent;
import net.slipcor.pvparena.events.PALeaveEvent;
import net.slipcor.pvparena.events.PALoseEvent;
import net.slipcor.pvparena.events.PAWinEvent;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;
import net.slipcor.pvparena.managers.ConfigurationManager;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.StartRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * <pre>
 * Arena class
 * </pre>
 * 
 * contains >general< arena methods and variables
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class Arena {

	private final static Debug DEBUG = new Debug(3);
	private Debug debug = null;
	private final Set<ArenaClass> classes = new HashSet<ArenaClass>();
	private final Set<ArenaGoal> goals = new HashSet<ArenaGoal>();
	private final Set<ArenaModule> mods = new HashSet<ArenaModule>();
	private final Set<ArenaRegionShape> regions = new HashSet<ArenaRegionShape>();
	private final Set<PAClassSign> signs = new HashSet<PAClassSign>();
	private final Set<ArenaTeam> teams = new HashSet<ArenaTeam>();
	private final Set<String> playedPlayers = new HashSet<String>();
	private PARoundMap rounds;

	private static String globalprefix = "PVP Arena";
	private final String name;
	private String prefix = "PVP Arena";
	private String owner = "%server%";

	// arena status
	private boolean fightInProgress = false;
	private boolean locked = false;
	private boolean free = false;
	private boolean valid = false;
	private int startCount = 0;
	private int round = 0;

	// Runnable IDs
	public BukkitRunnable endRunner = null;
	public BukkitRunnable pvpRunner = null;
	public BukkitRunnable realEndRunner = null;
	public BukkitRunnable startRunner = null;
	public int spawnCampRunnerID = -1;
	
	public boolean gaveRewards = false;

	private Config cfg;

	public Arena(final String name) {
		this.name = name;

		getDebugger().i("loading Arena " + name);
		final File file = new File(PVPArena.instance.getDataFolder().getPath()
				+ "/arenas/" + name + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cfg = new Config(file);
		valid = ConfigurationManager.configParse(this, cfg);
	}

	public void addClass(final String className, final ItemStack[] items) {
		classes.add(new ArenaClass(className, items));
	}

	public void addRegion(final ArenaRegionShape region) {
		this.regions.add(region);
		getDebugger().i("loading region: "+region.getRegionName());
		if (region.getType().equals(RegionType.JOIN)) {
			if (getArenaConfig().getBoolean(CFG.JOIN_FORCE)) {
				region.initTimer();
			}
		} else if (region.getType().equals(RegionType.WATCH)) {
			region.initTimer();
		}
	}

	public void broadcast(final String msg) {
		getDebugger().i("@all: " + msg);
		final Set<ArenaPlayer> players = getEveryone();
		for (ArenaPlayer p : players) {
			if (p.getArena() == null || !p.getArena().equals(this)) {
				continue;
			}
			this.msg(p.get(), msg);
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
	public void broadcastColored(final String msg, final ChatColor color,
			final Player player) {
		synchronized(this) {
			broadcast(color + player.getName() + ChatColor.WHITE + ": " + msg);
		}
	}

	/**
	 * send a message to every player except the given one
	 * 
	 * @param sender
	 *            the player to exclude
	 * @param msg
	 *            the message to send
	 */
	public void broadcastExcept(final CommandSender sender, final String msg) {
		getDebugger().i("@all/" + sender.getName() + ": " + msg, sender);
		final Set<ArenaPlayer> players = getEveryone();
		for (ArenaPlayer p : players) {
			if (p.getArena() == null || !p.getArena().equals(this)) {
				continue;
			}
			if (p.getName().equals(sender.getName())) {
				continue;
			}
			msg(p.get(), msg);
		}
	}

	public void chooseClass(final Player player, final Sign sign, final String className) {

		getDebugger().i("choosing player class", player);

		if (sign != null) {

			getDebugger().i("checking class perms", player);
			if (getArenaConfig().getBoolean(CFG.PERMS_EXPLICITCLASS)
					&& !(player.hasPermission("pvparena.class." + className))) {
				this.msg(player,
						Language.parse(MSG.ERROR_NOPERM_CLASS, className));
				return; // class permission desired and failed =>
						// announce and OUT
			}

			if (getArenaConfig().getBoolean(CFG.USES_CLASSSIGNSDISPLAY)) {
				PAClassSign.remove(signs, player);
				final Block block = sign.getBlock();
				PAClassSign classSign = PAClassSign.used(block.getLocation(), signs);
				if (classSign == null) {
					classSign = new PAClassSign(block.getLocation());
					signs.add(classSign);
				}
				if (!classSign.add(player)) {
					this.msg(player,
							Language.parse(MSG.ERROR_CLASS_FULL, className));
					return;
				}
			}

			if (ArenaModuleManager.cannotSelectClass(this, player, className)) {
				return;
			}
			if (startRunner != null) {
				ArenaPlayer.parsePlayer(player.getName()).setStatus(Status.READY);
			}
		}
		InventoryManager.clearInventory(player);
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		if (aPlayer.getArena() == null) {
			PVPArena.instance.getLogger().warning(
					"failed to set class " + className + " to player "
							+ player.getName());
			return;
		}
		aPlayer.setArenaClass(className);
		if (className.equalsIgnoreCase("custom")) {
			// if custom, give stuff back
			ArenaPlayer.reloadInventory(this, player);
		} else {
			ArenaPlayer.givePlayerFightItems(this, player);
		}
	}

	public void clearRegions() {
		for (ArenaRegionShape region : regions) {
			region.reset();
		}
	}

	/**
	 * initiate the arena start countdown
	 */
	public void countDown() {
		if (startRunner != null || this.isFightInProgress()) {
			if (this.getClass(getArenaConfig().getString(CFG.READY_AUTOCLASS)) == null && !this.isFightInProgress()) {
				startRunner.cancel();
				startRunner = null;
				broadcast(Language.parse(MSG.TIMER_COUNTDOWN_INTERRUPTED));
			}
			return;
		}

		new StartRunnable(this, getArenaConfig()
				.getInt(CFG.TIME_STARTCOUNTDOWN));
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
		getDebugger().i("counting ready players: " + sum);
		return sum;
	}

	public Config getArenaConfig() {
		return cfg;
	}

	public ArenaClass getClass(final String className) {
		for (ArenaClass ac : classes) {
			if (ac.getName().equalsIgnoreCase(className)) {
				return ac;
			}
		}
		return null;
	}

	public Set<ArenaClass> getClasses() {
		return classes;
	}
	
	public Debug getDebugger() {
		if (debug == null) {
			debug = new Debug(this);
		}
		return debug;
	}

	/**
	 * hand over everyone being part of the arena
	 * 
	 */
	public Set<ArenaPlayer> getEveryone() {

		final Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();

		for (ArenaPlayer ap : ArenaPlayer.getAllArenaPlayers()) {
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
	public Set<ArenaPlayer> getFighters() {

		final Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();

		for (ArenaTeam team : getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				players.add(ap);
			}
		}
		return players;
	}

	public Set<ArenaGoal> getGoals() {
		return round == 0 ? goals : rounds.getGoals(round);
	}

	public Set<ArenaModule> getMods() {
		return mods;
	}

	public String getName() {
		return name;
	}

	public String getOwner() {
		return owner;
	}
	
	public Set<String> getPlayedPlayers() {
		return playedPlayers;
	}

	public String getPrefix() {
		return prefix;
	}

	public Material getReadyBlock() {
		Material mMat = Material.IRON_BLOCK;
		getDebugger().i("reading ready block");
		try {
			mMat = Material.getMaterial(getArenaConfig()
					.getInt(CFG.READY_BLOCK));
			if (mMat == Material.AIR) {
				mMat = Material.getMaterial(getArenaConfig().getString(
						CFG.READY_BLOCK));
			}
			getDebugger().i("mMat now is " + mMat.name());
		} catch (Exception e) {
			getDebugger().i("exception reading ready block");
			final String sMat = getArenaConfig().getString(CFG.READY_BLOCK);
			try {
				mMat = Material.getMaterial(sMat);
				getDebugger().i("mMat now is " + mMat.name());
			} catch (Exception e2) {
				Language.logWarn(MSG.ERROR_MAT_NOT_FOUND, sMat);
			}
		}
		return mMat;
	}

	public ArenaRegionShape getRegion(final String name) {
		for (ArenaRegionShape region : regions) {
			if (region.getRegionName().equalsIgnoreCase(name)) {
				return region;
			}
		}
		return null;
	}

	public Set<ArenaRegionShape> getRegions() {
		return regions;
	}

	public int getRound() {
		return round;
	}

	public int getRoundCount() {
		return rounds.getCount();
	}

	public PARoundMap getRounds() {
		return rounds;
	}

	public ArenaTeam getTeam(final String name) {
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
	public Set<ArenaTeam> getTeams() {
		return teams;
	}

	/**
	 * hand over all teams
	 * 
	 * @return the arena teams
	 */
	public Set<String> getTeamNames() {
		final Set<String> result = new HashSet<String>();
		for (ArenaTeam team : teams) {
			result.add(team.getName());
		}
		return result;
	}

	/**
	 * hand over all teams
	 * 
	 * @return the arena teams
	 */
	public Set<String> getTeamNamesColored() {
		final Set<String> result = new HashSet<String>();
		for (ArenaTeam team : teams) {
			result.add(team.getColoredName());
		}
		return result;
	}

	public String getWorld() {
		ArenaRegionShape ars = null;

		for (ArenaRegionShape arss : this.getRegionsByType(RegionType.BATTLE)) {
			ars = arss;
			break;
		}

		if (ars != null) {
			return ars.getWorldName();
		}

		return Bukkit.getWorlds().get(0).getName();
	}

	/**
	 * give customized rewards to players
	 * 
	 * @param player
	 *            the player to give the reward
	 */
	public void giveRewards(final Player player) {
		if (gaveRewards) {
			return;
		}

		getDebugger().i("giving rewards to " + player.getName(), player);

		ArenaModuleManager.giveRewards(this, player);
		final String sItems = getArenaConfig().getString(CFG.ITEMS_REWARDS, "none");

		String[] items = sItems.split(",");
		if ("none".equals(sItems)) {
			items = null;
		}
		final boolean isRandom = getArenaConfig().getBoolean(CFG.ITEMS_RANDOM);
		final Random rRandom = new Random();

		final PAWinEvent dEvent = new PAWinEvent(this, player, items);
		Bukkit.getPluginManager().callEvent(dEvent);
		items = dEvent.getItems();
		
		getDebugger().i("start " + startCount + " - minplayers: " + cfg.getInt(CFG.ITEMS_MINPLAYERS), player);

		if (items == null || items.length < 1
				|| cfg.getInt(CFG.ITEMS_MINPLAYERS) > startCount) {
			return;
		}

		final int randomItem = rRandom.nextInt(items.length);

		for (int i = 0; i < items.length; ++i) {
			final ItemStack stack = StringParser.getItemStackFromString(items[i]);
			if (stack == null) {
				PVPArena.instance.getLogger().warning(
						"unrecognized item: " + items[i]);
				continue;
			}
			if (isRandom && i != randomItem) {
				continue;
			}
			try {
				player.getInventory().setItem(
						player.getInventory().firstEmpty(), stack);
			} catch (Exception e) {
				this.msg(player, Language.parse(MSG.ERROR_INVENTORY_FULL));
				return;
			}
		}
	}

	public void goalAdd(final ArenaGoal goal) {
		final ArenaGoal nugoal = (ArenaGoal) goal.clone();

		for (ArenaGoal g : goals) {
			if (goal.getName().equals(g.getName())) {
				return;
			}
		}

		nugoal.setArena(this);

		goals.add(nugoal);
		updateGoals();
	}

	public void goalRemove(final ArenaGoal goal) {
		final ArenaGoal nugoal = (ArenaGoal) goal.clone();
		nugoal.setArena(this);

		goals.remove(nugoal);
		updateGoals();
	}

	public boolean goalToggle(final ArenaGoal goal) {
		final ArenaGoal nugoal = (ArenaGoal) goal.clone();
		nugoal.setArena(this);

		boolean contains = false;
		ArenaGoal removeGoal = nugoal;

		for (ArenaGoal g : goals) {
			if (g.getName().equals(goal.getName())) {
				contains = true;
				removeGoal = g;
				break;
			}
		}

		if (contains) {
			goals.remove(removeGoal);
			updateGoals();
			return false;
		} else {
			goals.add(nugoal);
			updateGoals();
		}
		return true;
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
				getDebugger().i("custom class active: true");
				return true;
			}
		}
		getDebugger().i("custom class active: false");
		return false;
	}

	public boolean hasAlreadyPlayed(final String playerName) {
		return playedPlayers.contains(playerName);
	}

	public void hasNotPlayed(final ArenaPlayer player) {
		playedPlayers.remove(player.getName());
	}

	public boolean hasPlayer(final Player player) {
		for (ArenaTeam team : teams) {
			if (team.hasPlayer(player)) {
				return true;
			}
		}
		return this.equals(ArenaPlayer.parsePlayer(player.getName()).getArena());
	}

	public void increasePlayerCount() {
		startCount++;
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

	public boolean isValid() {
		return valid;
	}

	public void markPlayedPlayer(final String playerName) {
		playedPlayers.add(playerName);
	}

	public void modAdd(final ArenaModule mod) {
		mods.add(mod);
		updateMods();
	}

	public void modRemove(final ArenaModule mod) {
		mods.remove(mod);
		updateMods();
	}

	public void msg(final CommandSender sender, final String msg) {
		if (sender == null || msg == null || msg.length() < 1) {
			return;
		}
		getDebugger().i("@" + sender.getName() + ": " + msg);
		sender.sendMessage(Language.parse(MSG.MESSAGES_GENERAL, prefix, msg));
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
	public String parseDeathCause(final Player player, final DamageCause cause,
			final Entity damager) {

		if (cause == null) {
			return Language.parse(MSG.DEATHCAUSE_CUSTOM);
		}

		getDebugger().i("return a damage name for : " + cause.toString(), player);
		ArenaPlayer aPlayer = null;
		ArenaTeam team = null;

		getDebugger().i("damager: " + damager, player);

		if (damager instanceof Player) {
			aPlayer = ArenaPlayer.parsePlayer(((Player) damager).getName());
			team = aPlayer.getArenaTeam();
		}

		final EntityDamageEvent lastDamageCause = player.getLastDamageCause();

		switch (cause) {
		case ENTITY_ATTACK:
			if ((damager instanceof Player) && (team != null)) {
				return team.colorizePlayer(aPlayer.get()) + ChatColor.YELLOW;
			}

			try {
				getDebugger().i("last damager: "
						+ ((EntityDamageByEntityEvent) lastDamageCause)
								.getDamager().getType(), player);
				return Language.parse(MSG.getByName("DEATHCAUSE_"
						+ ((EntityDamageByEntityEvent) lastDamageCause)
								.getDamager().getType().name()));
			} catch (Exception e) {

				return Language.parse(MSG.DEATHCAUSE_CUSTOM);
			}
		case ENTITY_EXPLOSION:
			try {
				getDebugger().i("last damager: "
						+ ((EntityDamageByEntityEvent) lastDamageCause)
								.getDamager().getType(), player);
				return Language.parse(MSG.getByName("DEATHCAUSE_"
						+ ((EntityDamageByEntityEvent) lastDamageCause)
								.getDamager().getType().name()));
			} catch (Exception e) {

				return Language.parse(MSG.DEATHCAUSE_ENTITY_EXPLOSION);
			}
		case PROJECTILE:
			if ((damager instanceof Player) && (team != null)) {
				return team.colorizePlayer(aPlayer.get()) + ChatColor.YELLOW;
			}
			try {

				getDebugger().i("last damager: "
						+ ((Projectile) ((EntityDamageByEntityEvent) lastDamageCause)
								.getDamager()).getShooter().getType(), player);
				return Language
						.parse(MSG
								.getByName("DEATHCAUSE_"
										+ ((Projectile) ((EntityDamageByEntityEvent) lastDamageCause)
												.getDamager()).getShooter()
												.getType().name()));
			} catch (Exception e) {

				return Language.parse(MSG.DEATHCAUSE_PROJECTILE);
			}
		default:
			break;
		}
		MSG string = MSG.getByName("DEATHCAUSE_"
				+ cause.toString());
		if (string == null) {
			PVPArena.instance.getLogger().warning("Unknown cause: " + cause.toString());
			string = MSG.DEATHCAUSE_VOID;
		}
		return Language.parse(string);
	}

	public static void pmsg(final CommandSender sender, final String msg) {
		if (sender == null || msg == null || msg.length() < 1) {
			return;
		}
		DEBUG.i("@" + sender.getName() + ": " + msg, sender);
		sender.sendMessage(Language.parse(MSG.MESSAGES_GENERAL, globalprefix, msg));
	}

	/**
	 * a player leaves from the arena
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the leaving player
	 * @param b
	 */
	public void playerLeave(final Player player, final CFG location, final boolean silent) {
		if (player == null) {
			return;
		}
		for (ArenaGoal goal : getGoals()) {
			goal.parseLeave(player);
		}

		if (!fightInProgress) {
			startCount--;
			playedPlayers.remove(player.getName());
		}
		getDebugger().i("fully removing player from arena", player);
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		if (!silent) {

			final ArenaTeam team = aPlayer.getArenaTeam();
			if (team == null) {

				broadcastExcept(
						player,
						Language.parse(MSG.FIGHT_PLAYER_LEFT, player.getName()
								+ ChatColor.YELLOW));
			} else {
				ArenaModuleManager.parsePlayerLeave(this, player, team);

				broadcastExcept(
						player,
						Language.parse(MSG.FIGHT_PLAYER_LEFT,
								team.colorizePlayer(player) + ChatColor.YELLOW));
			}
			this.msg(player, Language.parse(MSG.NOTICE_YOU_LEFT));
		}

		removePlayer(player, getArenaConfig().getString(location), false,
				silent);

		if (startRunner != null && getArenaConfig().getInt(CFG.READY_MINPLAYERS) > 0 &&
				getFighters().size() <= getArenaConfig().getInt(CFG.READY_MINPLAYERS)) {
			startRunner.cancel();
			broadcast(Language.parse(MSG.TIMER_COUNTDOWN_INTERRUPTED));
			startRunner = null;
		}

		if (isFightInProgress()) {
			ArenaManager.checkAndCommit(this, silent);
		}
		
		aPlayer.reset();
	}

	/**
	 * check if an arena is ready
	 * 
	 * @param arena
	 *            the arena to check
	 * @return null if ok, error message otherwise
	 */
	public String ready() {
		getDebugger().i("ready check !!");

		final int players = TeamManager.countPlayersInTeams(this);
		if (players < 2) {
			return Language.parse(MSG.ERROR_READY_1_ALONE);
		}
		if (players < getArenaConfig().getInt(CFG.READY_MINPLAYERS)) {
			return Language.parse(MSG.ERROR_READY_4_MISSING_PLAYERS);
		}

		if (getArenaConfig().getBoolean(CFG.READY_CHECKEACHPLAYER)) {
			for (ArenaTeam team : getTeams()) {
				for (ArenaPlayer ap : team.getTeamMembers()) {
					if (!ap.getStatus().equals(Status.READY)) {
						return Language
								.parse(MSG.ERROR_READY_0_ONE_PLAYER_NOT_READY);
					}
				}
			}
		}

		if (!free) {
			final Set<String> activeTeams = new HashSet<String>();

			for (ArenaTeam team : getTeams()) {
				for (ArenaPlayer ap : team.getTeamMembers()) {
					if (!getArenaConfig().getBoolean(CFG.READY_CHECKEACHTEAM)
							|| ap.getStatus().equals(Status.READY)) {
						activeTeams.add(team.getName());
						break;
					}
				}
			}

			if (activeTeams.size() < 2) {
				return Language.parse(MSG.ERROR_READY_2_TEAM_ALONE);
			}
		}

		final String error = PVPArena.instance.getAgm().ready(this);
		if (error != null) {
			return error;
		}

		for (ArenaTeam team : getTeams()) {
			for (ArenaPlayer p : team.getTeamMembers()) {
				if (p.get() == null) {
					continue;
				}
				getDebugger().i("checking class: " + p.get().getName(), p.get());

				if (p.getArenaClass() == null) {
					getDebugger().i("player has no class", p.get());
					// player no class!
					PVPArena.instance.getLogger().warning("Player no class: " + p.get());
					return Language
							.parse(MSG.ERROR_READY_5_ONE_PLAYER_NO_CLASS);
				}
			}
		}
		final int readyPlayers = countReadyPlayers();

		if (players > readyPlayers) {
			final double ratio = getArenaConfig().getDouble(CFG.READY_NEEDEDRATIO);
			getDebugger().i("ratio: " + ratio);
			if (ratio > 0) {
				final double aRatio = Float.valueOf(readyPlayers)
						/ Float.valueOf(players);
				if ((players > 0) && (aRatio >= ratio)) {
					return "";
				}
			}
			return Language.parse(MSG.ERROR_READY_0_ONE_PLAYER_NOT_READY);
		}
		return null;
	}

	/**
	 * call event when a player is exiting from an arena (by plugin)
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void callExitEvent(final Player player) {
		final PAExitEvent exitEvent = new PAExitEvent(this, player);
		Bukkit.getPluginManager().callEvent(exitEvent);
	}

	/**
	 * call event when a player is leaving an arena (on his own)
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void callLeaveEvent(final Player player) {
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final PALeaveEvent event = new PALeaveEvent(this, player, aPlayer.getStatus()
				.equals(Status.FIGHT));
		Bukkit.getPluginManager().callEvent(event);
	}

	public void removeClass(final String string) {
		for (ArenaClass ac : classes) {
			if (ac.getName().equals(string)) {
				classes.remove(ac);
				return;
			}
		}
	}

	/**
	 * remove a player from the arena
	 * 
	 * @param player
	 *            the player to reset
	 * @param tploc
	 *            the coord string to teleport the player to
	 */
	public void removePlayer(final Player player, final String tploc, final boolean soft,
			final boolean force) {
		getDebugger().i("removing player " + player.getName() + (soft ? " (soft)" : "")
				+ ", tp to " + tploc, player);
		resetPlayer(player, tploc, soft, force);

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		if (!soft && aPlayer.getArenaTeam() != null) {
			aPlayer.getArenaTeam().remove(aPlayer);
		}

		callExitEvent(player);
		if (getArenaConfig().getBoolean(CFG.USES_CLASSSIGNSDISPLAY)) {
			PAClassSign.remove(signs, player);
		}

//		if (getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE)) {
//			aPlayer.setArena(null);
//		}

		player.setNoDamageTicks(60);
	}

	/**
	 * reset an arena
	 * 
	 * @param force
	 */
	public void resetPlayers(final boolean force) {
		getDebugger().i("resetting player manager");
		final Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
		int fighters = 0;
		for (ArenaTeam team : this.getTeams()) {
			for (ArenaPlayer p : team.getTeamMembers()) {
				getDebugger().i("player: " + p.getName(), p.get());
				if (p.getArena() == null || !p.getArena().equals(this)) {
					/*
					if (p.getArenaTeam() != null) {
						p.getArenaTeam().remove(p);
						getDebugger().info("> removed", p.get());
					}*/
					getDebugger().i("> skipped", p.get());
					continue;
				} else {
					getDebugger().i("> added", p.get());
					if (p.getStatus() == Status.FIGHT) {
						fighters++;
					}
					players.add(p);
				}
			}
		}

		for (ArenaPlayer p : players) {
			
			p.debugPrint();
			if (p.getStatus() != null && p.getStatus().equals(Status.FIGHT)
					&& !(fighters > players.size()/2)) {
				final Player player = p.get();

				if (!force) {
					p.addWins();
				}
				this.callExitEvent(player);
				resetPlayer(player, getArenaConfig().getString(CFG.TP_WIN, "old"),
						false, force);
				if (!force && p.getStatus().equals(Status.FIGHT)
						&& isFightInProgress() && !gaveRewards) {
					giveRewards(player); // if we are the winning team, give
									// reward!
				}
			} else if (p.getStatus() != null
					&& (p.getStatus().equals(Status.DEAD) || p.getStatus()
							.equals(Status.LOST))) {

				PALoseEvent loseEvent = new PALoseEvent(this, p.get());
				Bukkit.getPluginManager().callEvent(loseEvent);

				Player player = p.get();
				if (!force) {
					p.addLosses();
				}
				this.callExitEvent(player);
				resetPlayer(player, getArenaConfig().getString(CFG.TP_LOSE, "old"),
						false, force);
			} else {
				this.callExitEvent(p.get());
				resetPlayer(p.get(),
						getArenaConfig().getString(CFG.TP_LOSE, "old"), false,
						force);
			}

			p.reset();
		}
		for (ArenaPlayer player : ArenaPlayer.getAllArenaPlayers()) {
			if (this.equals(player.getArena()) && player.getStatus() == Status.WATCH) {

				this.callExitEvent(player.get());
				resetPlayer(player.get(),
						getArenaConfig().getString(CFG.TP_EXIT, "old"), false,
						force);
				player.setArena(null);
			}
		}
		gaveRewards = true;
	}

	/**
	 * reset an arena
	 */
	public void reset(final boolean force) {

		PAEndEvent event = new PAEndEvent(this);
		Bukkit.getPluginManager().callEvent(event);

		getDebugger().i("resetting arena; force: " + force);
		for (PAClassSign as : signs) {
			as.clear();
		}
		signs.clear();
		playedPlayers.clear();
		resetPlayers(force);
		setFightInProgress(false);

		if (endRunner != null) {
			endRunner.cancel();
		}
		endRunner = null;
		if (realEndRunner != null) {
			realEndRunner.cancel();
		}
		realEndRunner = null;
		if (pvpRunner != null) {
			pvpRunner.cancel();
		}
		pvpRunner = null;

		ArenaModuleManager.reset(this, force);
		clearRegions();
		PVPArena.instance.getAgm().reset(this, force);
		
		round = 0;
	}

	/**
	 * reset a player to his pre-join values
	 * 
	 * @param player
	 * @param string
	 * @param soft
	 *            if location should be preserved (another tp incoming)
	 */
	private void resetPlayer(final Player player, final String string, final boolean soft,
			final boolean force) {
		if (player == null) {
			return;
		}
		getDebugger().i("resetting player: " + player.getName() + (soft ? "(soft)" : ""),
				player);
/*
		class RemoveRunner implements Runnable {
			private final Entity e;
			RemoveRunner(Entity e) {
				this.e = e;
			}
			@Override
			public void run() {
				e.remove();
			}

		}
		*/
		
		
		for (Entity entity : player.getNearbyEntities(2.0, 2.0, 2.0)) {
			if (entity instanceof Projectile) {
				entity.remove();
			}
		}
		
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		if (aPlayer.getState() != null) {
			aPlayer.getState().unload();
		}

		ArenaModuleManager.resetPlayer(this, player, force);

		String sClass = "";
		if (aPlayer.getArenaClass() != null) {
			sClass = aPlayer.getArenaClass().getName();
		}

		if (!sClass.equalsIgnoreCase("custom")) {
			InventoryManager.clearInventory(player);
			ArenaPlayer.reloadInventory(this, player);
		}

		getDebugger().i("string = " + string, player);
		aPlayer.setTelePass(true);
		if (string.equalsIgnoreCase("old")) {
			getDebugger().i("tping to old", player);
			if (aPlayer.getLocation() != null) {
				getDebugger().i("location is fine", player);
				final PALocation loc = aPlayer.getLocation();
				player.teleport(loc.toLocation());
				player
						.setNoDamageTicks(
								getArenaConfig().getInt(
										CFG.TIME_TELEPORTPROTECT) * 20);
			}
		} else {
			final PALocation loc = SpawnManager.getCoords(this, string);
			if (loc == null) {
				PVPArena.instance.getLogger().warning("Spawn null: " + string);
			} else {
				player.teleport(loc.toLocation());
				aPlayer.setTelePass(false);
			}
			player.setNoDamageTicks(
							getArenaConfig().getInt(
									CFG.TIME_TELEPORTPROTECT) * 20);
		}
		if (!soft) {
			aPlayer.setLocation(null);
		}
	}

	/**
	 * reset player variables
	 * 
	 * @param player
	 *            the player to access
	 */
	public void unKillPlayer(final Player player, final DamageCause cause, final Entity damager) {

		getDebugger().i("respawning player " + player.getName(), player);
		PlayerState.playersetHealth(player,
				getArenaConfig().getInt(CFG.PLAYER_HEALTH, 20));
		player.setFoodLevel(getArenaConfig().getInt(CFG.PLAYER_FOODLEVEL, 20));
		player.setSaturation(getArenaConfig().getInt(CFG.PLAYER_SATURATION, 20));
		player.setExhaustion((float) getArenaConfig().getDouble(
				CFG.PLAYER_EXHAUSTION, 0.0));
		player.setVelocity(new Vector());
		player.setFallDistance(0);

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final ArenaTeam team = aPlayer.getArenaTeam();

		if (team == null) {
			return;
		}

		PlayerState.removeEffects(player);

		ArenaModuleManager.parseRespawn(this, player, team, cause, damager);

		player.setFireTicks(0);
		player.setNoDamageTicks(cfg.getInt(CFG.TIME_TELEPORTPROTECT) * 20);
	}

	public void selectClass(final ArenaPlayer aPlayer, final String cName) {
		for (ArenaClass c : classes) {
			if (c.getName().equalsIgnoreCase(cName)) {
				aPlayer.setArenaClass(c);
				aPlayer.setArena(this);
				aPlayer.createState(aPlayer.get());
				InventoryManager.clearInventory(aPlayer.get());
				ArenaClass.equip(aPlayer.get(), c.getItems());
				msg(aPlayer.get(), Language.parse(MSG.CLASS_PREVIEW, c.getName()));
				return;
			}
		}
		msg(aPlayer.get(), Language.parse(MSG.ERROR_CLASS_NOT_FOUND, cName));
	}

	public void setArenaConfig(final Config cfg) {
		this.cfg = cfg;
	}

	public void setFightInProgress(final boolean fightInProgress) {
		this.fightInProgress = fightInProgress;
	}

	public void setFree(final boolean isFree) {
		free = isFree;
		if (free && (cfg.getUnsafe("teams.free") == null)) {
			teams.clear();
			teams.add(new ArenaTeam("free", "WHITE"));
		} else if (free) {
			teams.clear();
			teams.add(new ArenaTeam("free", (String) cfg
					.getUnsafe("teams.free")));
		}
		cfg.set(CFG.GENERAL_TYPE, isFree ? "free" : "none");
		cfg.save();
	}

	public void setOwner(final String owner) {
		this.owner = owner;
	}

	public void setLocked(final boolean locked) {
		this.locked = locked;
	}

	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

	/**
	 * damage every actively fighting player for being near a spawn
	 */
	public void spawnCampPunish() {

		final Map<Location, ArenaPlayer> players = new HashMap<Location, ArenaPlayer>();

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
			final String sTeam = team.getName();
			for (PALocation spawnLoc : SpawnManager.getSpawns(this, sTeam)) {
				for (Location playerLoc : players.keySet()) {
					if (spawnLoc.getDistanceSquared(new PALocation(playerLoc)) < 9) {
						players.get(playerLoc)
								.get()
								.setLastDamageCause(
										new EntityDamageEvent(players.get(
												playerLoc).get(),
												DamageCause.CUSTOM, 1000));
						players.get(playerLoc)
								.get()
								.damage(getArenaConfig().getInt(
										CFG.DAMAGE_SPAWNCAMP));
					}
				}
			}
		}
	}

	public void spawnSet(final String node, final PALocation paLocation) {
		cfg.setManually("spawns." + node, Config.parseToString(paLocation));
		cfg.save();
	}

	public void spawnUnset(final String node) {
		cfg.setManually("spawns." + node, null);
		cfg.save();
	}

	/**
	 * initiate the arena start
	 */
	public void start() {
		getDebugger().i("start()");
		gaveRewards = false;
		startRunner = null;
		if (isFightInProgress()) {
			getDebugger().i("already in progress! OUT!");
			return;
		}
		int sum = 0;
		for (ArenaTeam team : getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.LOUNGE)
						|| ap.getStatus().equals(Status.READY)) {
					sum++;
				}
			}
		}
		getDebugger().i("sum == " + sum);
		final String errror = ready();
		if (errror == null || errror.equals("")) {
			getDebugger().i("START!");
			PACheck.handleStart(this, null);
			setFightInProgress(true);
		} else {
			PVPArena.instance.getLogger().info(errror);
			for (ArenaPlayer ap : getFighters()) {
				getDebugger().i("removing player " + ap.getName());
				playerLeave(ap.get(), CFG.TP_EXIT, false);
			}
			reset(false);
		}
	}

	public void stop(final boolean force) {
		for (ArenaPlayer p : getFighters()) {
			this.playerLeave(p.get(), CFG.TP_EXIT, true);
		}
		reset(force);
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
	public void tellTeam(final String sTeam, final String msg, final ChatColor color,
			final Player player) {
		final ArenaTeam team = this.getTeam(sTeam);
		if (team == null) {
			return;
		}
		getDebugger().i("@" + team.getName() + ": " + msg, player);
		synchronized(this) {
			for (ArenaPlayer p : team.getTeamMembers()) {
				if (player == null) {
					p.get().sendMessage(
							color + "[" + team.getName() + "]"+ ChatColor.WHITE
									+ ": " + msg);
				} else {
					p.get().sendMessage(
							color + "[" + team.getName() + "] " + player.getName() + ChatColor.WHITE
									+ ": " + msg);
				}
			}
		}
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * teleport a given player to the given coord string
	 * 
	 * @param player
	 *            the player to teleport
	 * @param place
	 *            the coord string
	 */
	public void tpPlayerToCoordName(final Player player, final String place) {
		getDebugger().i("teleporting " + player + " to coord " + place, player);

		if (player.isInsideVehicle()) {
			player.getVehicle().eject();
		}

		if (place.endsWith("lounge")
				&& getArenaConfig().getBoolean(CFG.CHAT_DEFAULTTEAM)
				&& getArenaConfig().getBoolean(CFG.CHAT_ENABLED)) {
			final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
			aPlayer.setPublicChatting(true);
		}

		ArenaModuleManager.tpPlayerToCoordName(this, player, place);

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		if ("spectator".equals(place)) {
			if (getFighters().contains(aPlayer)) {
				aPlayer.setStatus(Status.LOST);
			} else {
				aPlayer.setStatus(Status.WATCH);
			}
		}
		PALocation loc = SpawnManager.getCoords(this, place);
		if ("old".equals(place)) {
			loc = aPlayer.getLocation();
		}
		if (loc == null) {
			PVPArena.instance.getLogger().warning("Spawn null : " + place);
			return;
		}
		
		aPlayer.setTelePass(true);
		player.teleport(loc.toLocation());
		player.setNoDamageTicks(cfg.getInt(CFG.TIME_TELEPORTPROTECT) * 20);
		aPlayer.setTelePass(false);
	}

	/**
	 * last resort to put a player into an arena (when no goal/module wants to)
	 * 
	 * @param player the player to put
	 * @param team the arena team to put into
	 * 
	 * @return true if joining successful
	 */
	public boolean tryJoin(final Player player, final ArenaTeam team) {
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

		getDebugger().i("trying to join player " + player.getName(), player);

		if (aPlayer.getStatus().equals(Status.NULL)) {
			// joining DIRECTLY - save loc !!
			aPlayer.setLocation(new PALocation(player.getLocation()));
		} else {
			// should not happen; just make sure it does not. If noone reports this
			// for some time, we can remove this check. It should never happen
			// anything different. Just saying.
			PVPArena.instance.getLogger().warning("Status not null for tryJoin: " + player.getName());
		}
		
		if (aPlayer.getArenaClass() == null) {
			String autoClass = cfg.getString(CFG.READY_AUTOCLASS);
			
			if (autoClass != null && autoClass.contains(":") && autoClass.contains(";")) {
				String[] definitions = autoClass.split(";");
				autoClass = definitions[definitions.length-1]; // set default
				
				Map<String, ArenaClass> classes = new HashMap<String, ArenaClass>();
				
				for (String definition : definitions) {
					if (!definition.contains(":")) {
						continue;
					}
					String[] var = definition.split(":");
					ArenaClass aClass = getClass(var[1]);
					if (aClass != null) {
						classes.put(var[0], aClass);
					}
				}
				
				if (classes.containsKey(team.getName())) {
					autoClass = classes.get(team.getName()).getName();
				}
			}
			
			if (autoClass != null && !autoClass.equals("none")
					&& getClass(autoClass) == null) {
				msg(player, Language.parse(MSG.ERROR_CLASS_NOT_FOUND,
						"autoClass"));
				return false;
			}
		}

		aPlayer.setArena(this);
		team.add(aPlayer);
		aPlayer.setStatus(Status.FIGHT);
		tpPlayerToCoordName(player, (isFreeForAll() ? "" : team.getName())
				+ "spawn");

		if (aPlayer.getState() == null) {
			
			final Arena arena = aPlayer.getArena();

			final PAJoinEvent event = new PAJoinEvent(arena, player, false);
			Bukkit.getPluginManager().callEvent(event);

			aPlayer.createState(player);
			ArenaPlayer.backupAndClearInventory(arena, player);
			aPlayer.dump();
			
			
			if (aPlayer.getArenaTeam() != null && aPlayer.getArenaClass() == null) {
				final String autoClass = arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);
				if (autoClass != null && !autoClass.equals("none") && arena.getClass(autoClass) != null) {
					arena.chooseClass(player, null, autoClass);
				}
				if (autoClass == null) {
					arena.msg(player, Language.parse(MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
					return true;
				}
			}
		}
		return true;
	}

	/**
	 * Setup an arena based on legacy goals:
	 * 
	 * <pre>
	 * teams - team lives arena
	 * teamdm - team deathmatch arena
	 * dm - deathmatch arena
	 * free - deathmatch arena
	 * ctf - capture the flag arena
	 * ctp - capture the pumpkin arena
	 * spleef - free for all with teamkill off
	 * sabotage - destroy TNT inside the other team's base
	 * tank - all vs one!
	 * liberation - free willy!
	 * infect - infect (catchy, huh?)!
	 * food - food!
	 * </pre>
	 * 
	 * @param string
	 *            legacy goal
	 */
	public boolean getLegacyGoals(final String goalName) {
		setFree(false);
		final String lcName = goalName.toLowerCase();

		if ("teams".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("TeamLives"));
		} else if ("teamdm".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("TeamDeathMatch"));
		} else if ("dm".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm()
					.getGoalByName("PlayerDeathMatch"));
			this.setFree(true);
		} else if ("free".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("PlayerLives"));
			this.setFree(true);
		} else if ("spleef".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("PlayerLives"));
			this.setFree(true);
			this.getArenaConfig().set(CFG.PERMS_TEAMKILL, false);
		} else if ("ctf".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("Flags"));
		} else if ("ctp".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("Flags"));
			cfg.set(CFG.GOAL_FLAGS_FLAGTYPE, "PUMPKIN");
			cfg.save();
		} else if ("tank".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("Tank"));
			this.setFree(true);
			cfg.save();
		} else if ("sabotage".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("Sabotage"));
			cfg.save();
		} else if ("infect".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("Infect"));
			this.setFree(true);
			cfg.save();
		} else if ("liberation".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("Liberation"));
			cfg.save();
		} else if ("food".equals(lcName)) {
			goalAdd(PVPArena.instance.getAgm().getGoalByName("Food"));
			cfg.save();
		} else {
			return false;
		}

		updateGoals();
		return true;
	}

	public Set<ArenaRegionShape> getRegionsByType(final RegionType regionType) {
		Set<ArenaRegionShape> result = new HashSet<ArenaRegionShape>();
		for (ArenaRegionShape rs : regions) {
			if (rs.getType().equals(regionType)) {
				result.add(rs);
			}
		}
		return result;
	}

	public void setRoundMap(final List<String> list) {
		if (list == null) {
			rounds = new PARoundMap(this, new ArrayList<Set<String>>());
		} else {
			final List<Set<String>> outer = new ArrayList<Set<String>>();
			for (String round : list) {
				String[] split = round.split("|");
				final HashSet<String> inner = new HashSet<String>();
				for (String s : split) {
					inner.add(s);
				}
				outer.add(inner);
			}
			rounds = new PARoundMap(this, outer);
		}
	}

	public void setRound(final int value) {
		round = value;
	}

	public static void pmsg(final CommandSender sender, final String[] msgs) {
		for (String s : msgs) {
			pmsg(sender, s);
		}
	}

	private void updateGoals() {
		final List<String> list = new ArrayList<String>();

		for (ArenaGoal goal : goals) {
			list.add(goal.getName());
		}

		cfg.set(CFG.LISTS_GOALS, list);
		cfg.save();
	}

	private void updateMods() {
		final List<String> list = new ArrayList<String>();

		for (ArenaModule mod : mods) {
			list.add(mod.getName());
		}

		cfg.set(CFG.LISTS_MODS, list);
		cfg.save();
	}

	public void updateRounds() {
		final List<String> result = new ArrayList<String>();

		for (int i = 0; i < rounds.getCount(); i++) {
			result.add(StringParser.joinSet(rounds.getGoals(i), "|"));
		}

		cfg.setManually("rounds", result);
		cfg.save();
	}
}
