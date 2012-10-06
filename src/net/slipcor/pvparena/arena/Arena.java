package net.slipcor.pvparena.arena;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
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
import net.slipcor.pvparena.events.PALeaveEvent;
import net.slipcor.pvparena.events.PALoseEvent;
import net.slipcor.pvparena.events.PAStartEvent;
import net.slipcor.pvparena.events.PAWinEvent;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;
import net.slipcor.pvparena.managers.ConfigurationManager;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.PlayerDestroyRunnable;
import net.slipcor.pvparena.runnables.PlayerStateCreateRunnable;
import net.slipcor.pvparena.runnables.SpawnCampRunnable;
import net.slipcor.pvparena.runnables.StartRunnable;
import net.slipcor.pvparena.runnables.TeleportRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * <pre>Arena class</pre>
 * 
 * contains >general< arena methods and variables
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class Arena {
	private Debug db = new Debug(3);
	private final HashSet<ArenaClass> classes = new HashSet<ArenaClass>();
	private final HashSet<ArenaGoal> goals = new HashSet<ArenaGoal>();
	private final HashSet<ArenaRegionShape> regions = new HashSet<ArenaRegionShape>();
	private final HashSet<PAClassSign> signs = new HashSet<PAClassSign>();
	private final HashSet<ArenaTeam> teams = new HashSet<ArenaTeam>();
	private PARoundMap rounds;

	private static String globalprefix = "PVP Arena";
	private String name = "default";
	private String prefix = "PVP Arena";
	private String owner = "%server%";

	// arena status
	private boolean fightInProgress = false;
	private boolean locked = false;
	private boolean free = false;
	private int startCount = 0;
	private int round = 0;

	// Runnable IDs
	public int END_ID = -1;
	public int REALEND_ID = -1;
	public int START_ID = -1;
	public int SPAWNCAMP_ID = -1;

	private Config cfg;

	public Arena(String name) {
		this.name = name;

		db.i("loading Arena " + name);
		File file = new File(PVPArena.instance.getDataFolder().getPath() + "/arenas/" + name + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cfg = new Config(file);
		ConfigurationManager.configParse(this, cfg);
	}

	public void addClass(String className, ItemStack[] items) {
		classes.add(new ArenaClass(className, items));
	}

	public void addRegion(ArenaRegionShape region) {
		this.regions.add(region);
	}

	public void broadcast(String msg) {
		db.i("@all: " + msg);
		HashSet<ArenaPlayer> players = getEveryone();
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
	public synchronized void broadcastColored(String msg, ChatColor c,
			Player player) {
		broadcast(c + player.getName() + ChatColor.WHITE + ": " + msg);
	}

	/**
	 * send a message to every player except the given one
	 * 
	 * @param sender
	 *            the player to exclude
	 * @param msg
	 *            the message to send
	 */
	public void broadcastExcept(CommandSender sender, String msg) {
		db.i("@all/" + sender.getName() + ": " + msg);
		HashSet<ArenaPlayer> players = getFighters();
		for (ArenaPlayer p : players) {
			if (p.getArena() == null || !p.getArena().equals(this)) {
				continue;
			}
			if (p.get().getName().equals(sender.getName()))
				continue;
			this.msg(p.get(), msg);
		}
	}

	public void chooseClass(Player player, Sign sign, String className) {

		db.i("choosing player class");

		if (sign != null) {

			if (getArenaConfig().getBoolean(CFG.PERMS_EXPLICITCLASS)) {
				db.i("checking class perms");
				if (!(player.hasPermission("pvparena.class." + className))) {
					this.msg(player, Language.parse(MSG.ERROR_NOPERM_CLASS, className));
					return; // class permission desired and failed =>
							// announce and OUT
				}
			}

			if (getArenaConfig().getBoolean(CFG.USES_CLASSSIGNSDISPLAY)) {
				PAClassSign.remove(signs, player);
				Block block = sign.getBlock();
				PAClassSign as = PAClassSign.used(block.getLocation(),
						signs);
				if (as == null) {
					as = new PAClassSign(block.getLocation());
				}
				signs.add(as);
				if (!as.add(player)) {
					this.msg(player, Language.parse(MSG.ERROR_CLASS_FULL));
					return;
				}
			}
		}
		InventoryManager.clearInventory(player);
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		if (ap.getArena() == null) {
			PVPArena.instance.getLogger().warning("failed to set class " + className
					+ " to player " + player.getName());
			return;
		}
		ap.setArenaClass(className);
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
		if (START_ID != -1 || this.isFightInProgress()) {
			Bukkit.getScheduler().cancelTask(START_ID);
			START_ID = -1;
			if (!this.isFightInProgress()) {
				broadcast(Language.parse(MSG.TIMER_COUNTDOWN_INTERRUPTED));
			}
			return;
		}
		
		new StartRunnable(this, getArenaConfig().getInt(CFG.TIME_STARTCOUNTDOWN));
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
		return round == 0 ? goals : rounds.getGoals(round);
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

	public ArenaRegionShape getRegion(String name) {
		for (ArenaRegionShape region : regions) {
			if (region.getRegionName().equalsIgnoreCase(name)) {
				return region;
			}
		}
		return null;
	}

	public HashSet<ArenaRegionShape> getRegions() {
		return regions;
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
	 * hand over all teams
	 * 
	 * @return the arena teams
	 */
	public HashSet<String> getTeamNamesColored() {
		HashSet<String> result = new HashSet<String>();
		for (ArenaTeam team : teams) {
			result.add(team.getColoredName());
		}
		return result;
	}

	/**
	 * return the arena world
	 * 
	 * @return the world name
	 */
	public String getWorld() {
		return getArenaConfig().getString(CFG.LOCATION_WORLD);
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
		String sItems = getArenaConfig().getString(CFG.ITEMS_REWARDS,
				"none");
		
		String[] items = sItems.split(",");
		if (sItems.equals("none")) {
			items = null;
		}
		boolean random = getArenaConfig().getBoolean(CFG.ITEMS_RANDOM);
		Random r = new Random();

		PAWinEvent dEvent = new PAWinEvent(this, player, items);
		Bukkit.getPluginManager().callEvent(dEvent);
		items = dEvent.getItems();
		
		if (items == null || items.length < 1 || cfg.getInt(CFG.ITEMS_MINPLAYERS) > startCount) {
			return;
		}

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
				this.msg(player, Language.parse(MSG.ERROR_INVENTORY_FULL));
				return;
			}
		}
	}

	public void goalAdd(ArenaGoal goal) {
		ArenaGoal nugoal = (ArenaGoal) goal.clone();
		nugoal.setArena(this);
		
		goals.add(nugoal);
		updateGoals();
	}

	public void goalRemove(ArenaGoal goal) {
		ArenaGoal nugoal = (ArenaGoal) goal.clone();
		nugoal.setArena(this);
		
		goals.remove(nugoal);
		updateGoals();
	}

	public boolean goalToggle(ArenaGoal goal) {
		ArenaGoal nugoal = (ArenaGoal) goal.clone();
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
		return this.equals(ArenaPlayer.parsePlayer(p.getName()).getArena());
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

	public void msg(CommandSender sender, String msg) {
		ArenaManager.db.i("@" + sender.getName() + ": " + msg);
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
			ap = ArenaPlayer.parsePlayer(((Player) damager).getName());
			team = ap.getArenaTeam();
		}

		switch (cause) {
		case ENTITY_ATTACK:
			if ((damager instanceof Player) && (team != null)) {
				return team.colorizePlayer(ap.get()) + ChatColor.YELLOW;
			}
			return Language.parse(MSG.DEATHCAUSE_CUSTOM);
		case PROJECTILE:
			if ((damager instanceof Player) && (team != null)) {
				return team.colorizePlayer(ap.get()) + ChatColor.YELLOW;
			}
			return Language.parse(MSG.getByName("DEATHCAUSE_" + cause.toString()));
		default:
			return Language.parse(MSG.getByName("DEATHCAUSE_" + cause.toString()));
		}
	}

	public static void pmsg(CommandSender sender, String msg) {
		ArenaManager.db.i("@" + sender.getName() + ": " + msg);
		sender.sendMessage(ChatColor.YELLOW + "[" + globalprefix + "] "
				+ ChatColor.WHITE + msg);
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
		if (!fightInProgress) {
			startCount--;
		}
		db.i("fully removing player from arena");
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

		boolean fighter = ap.getStatus().equals(Status.FIGHT);

		if (fighter) {
			ArenaTeam team = ap.getArenaTeam();
			if (team != null) {
				PVPArena.instance.getAmm().playerLeave(this, player, team);

				if (!location.equals("exit")) {
					broadcastExcept(
							player,
							Language.parse(MSG.FIGHT_PLAYER_LEFT,
									team.colorizePlayer(player)
											+ ChatColor.YELLOW));
				}
			}
			this.msg(player, Language.parse(MSG.NOTICE_YOU_LEFT));
		}
		removePlayer(player, getArenaConfig().getString(CFG.getByNode("tp." + location)),
				false);

		if (START_ID != -1) {
			Bukkit.getScheduler().cancelTask(START_ID);
			broadcast(Language.parse(MSG.TIMER_COUNTDOWN_INTERRUPTED));
			START_ID = -1;
		}
		PlayerDestroyRunnable pdr = new PlayerDestroyRunnable(ap);
		int i = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, pdr, 5L, 5L);
		pdr.setId(i);

		if (fighter && isFightInProgress()) {
			ArenaManager.checkAndCommit(this);
		}
	}

	/**
	 * check if an arena is ready
	 * 
	 * @param arena
	 *            the arena to check
	 * @return null if ok, error message otherwise
	 */
	public String ready() {
		
		int players = TeamManager.countPlayersInTeams(this);
		if (players < 2) {
			return Language.parse(MSG.ERROR_READY_1_ALONE);
		}
		if (players < getArenaConfig().getInt(CFG.READY_MINPLAYERS)) {
			return Language.parse(MSG.ERROR_READY_4_MISSING_PLAYERS);
		}

		if (getArenaConfig().getBoolean(CFG.READY_CHECKEACHPLAYER)) {
			for (ArenaTeam team : getTeams()) {
				for (ArenaPlayer ap : team.getTeamMembers())
					if (!ap.getStatus().equals(Status.READY)) {
						return Language.parse(MSG.ERROR_READY_0_ONE_PLAYER_NOT_READY);
					}
			}
		}
		
		if (!free) {
			HashSet<String> activeTeams = new HashSet<String>();

			for (ArenaTeam team : getTeams()) {
				for (ArenaPlayer ap : team.getTeamMembers())
					if (!getArenaConfig().getBoolean(CFG.READY_CHECKEACHTEAM) || ap.getStatus().equals(Status.READY)) {
						activeTeams.add(team.getName());
					}
			}

			if (activeTeams.size() < 2) {
				return Language.parse(MSG.ERROR_READY_2_TEAM_ALONE);
			}
		}

		String error = PVPArena.instance.getAgm().ready(this);
		if (error != null) {
			return error;
		}

		for (ArenaTeam team : getTeams()) {
			for (ArenaPlayer p : team.getTeamMembers()) {
				db.i("checking class: " + p.get().getName());

				if (p.getArenaClass() == null) {
					db.i("player has no class");
					// player no class!
					return Language.parse(MSG.ERROR_READY_5_ONE_PLAYER_NO_CLASS);
				}
			}
		}
		int readyPlayers = countReadyPlayers();

		if (players > readyPlayers) {
			double ratio = getArenaConfig().getDouble(CFG.READY_NEEDEDRATIO);
			db.i("ratio: " + String.valueOf(ratio));
			if (ratio > 0) {
				double aRatio = Float.valueOf(readyPlayers)
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
	 * remove a player from the arena
	 * 
	 * @param player
	 *            the player to remove
	 */
	public void remove(Player player) {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		PALeaveEvent event = new PALeaveEvent(this, player, ap.getStatus()
				.equals(Status.FIGHT));
		Bukkit.getPluginManager().callEvent(event);
		PAExitEvent exitEvent = new PAExitEvent(this, player);
		Bukkit.getPluginManager().callEvent(exitEvent);
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

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		if (!soft && ap.getArenaTeam() != null) {
			ap.getArenaTeam().remove(ap);
		}
		remove(player);
		if (getArenaConfig().getBoolean(CFG.USES_CLASSSIGNSDISPLAY)) {
			PAClassSign.remove(signs, player);
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
					p.addWins();
				}
				resetPlayer(z, getArenaConfig().getString(CFG.TP_WIN, "old"),
						false);
				if (!force && p.getStatus().equals(Status.FIGHT)
						&& isFightInProgress()) {
					giveRewards(z); // if we are the winning team, give
									// reward!
				}
				PlayerDestroyRunnable pdr = new PlayerDestroyRunnable(p);
				int i = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, pdr, 5L, 5L);
				pdr.setId(i);
			} else if (p.getStatus() != null
					&& (p.getStatus().equals(Status.DEAD) || p.getStatus()
							.equals(Status.LOST))) {

				PALoseEvent e = new PALoseEvent(this, p.get());
				Bukkit.getPluginManager().callEvent(e);

				Player z = p.get();
				if (!force) {
					p.addLosses();
				}
				resetPlayer(z, getArenaConfig().getString(CFG.TP_LOSE, "old"),
						false);
				PlayerDestroyRunnable pdr = new PlayerDestroyRunnable(p);
				int i = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, pdr, 5L, 5L);
				pdr.setId(i);
			} else {
				resetPlayer(p.get(),
						getArenaConfig().getString(CFG.TP_LOSE, "old"), false);
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
		for (PAClassSign as : signs) {
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
		PVPArena.instance.getAgm().reset(this, force);
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

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		if (ap.getState() != null) {
			ap.getState().unload();
		}
		PVPArena.instance.getAmm().resetPlayer(this, player);

		String sClass = "";
		if (ap.getArenaClass() != null) {
			sClass = ap.getArenaClass().getName();
		}

		if (!sClass.equalsIgnoreCase("custom")) {
			InventoryManager.clearInventory(player);
			ArenaPlayer.reloadInventory(this, player);
		}
		
		db.i("string = " + string);
		ap.setTelePass(true);
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new TeleportRunnable(this, ap, string), 2L);
	}

	/**
	 * reset player variables
	 * 
	 * @param player
	 *            the player to access
	 */
	public void unKillPlayer(Player player, DamageCause cause,
			Entity damager) {
		db.i("respawning player " + player.getName());
		PlayerState.playersetHealth(player, getArenaConfig().getInt(CFG.PLAYER_HEALTH, 20));
		player.setFoodLevel(getArenaConfig().getInt(CFG.PLAYER_FOODLEVEL, 20));
		player.setSaturation(getArenaConfig().getInt(CFG.PLAYER_SATURATION, 20));
		player.setExhaustion((float) getArenaConfig().getDouble(CFG.PLAYER_EXHAUSTION, 0.0));
		player.setVelocity(new Vector());

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		ArenaTeam team = ap.getArenaTeam();

		if (team == null) {
			return;
		}

		PlayerState.removeEffects(player);
		
		PVPArena.instance.getAmm().parseRespawn(this, player, team,
				cause, damager);

		player.setFireTicks(0);
		player.setNoDamageTicks(60);
	}

	public void selectClass(ArenaPlayer ap, String cName) {
		for (ArenaClass c : classes) {
			if (c.getName().equalsIgnoreCase(cName)) {
				ap.setArenaClass(c);
				ArenaClass.equip(ap.get(), c.getItems());
				msg(ap.get(), Language.parse(MSG.CLASS_PREVIEW, c.getName()));
			}
		}
		msg(ap.get(), Language.parse(MSG.ERROR_CLASS_NOT_FOUND, cName));
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

	/**
	 * set the arena world
	 * 
	 * @param sWorld
	 *            the world name
	 */
	public void setWorld(String sWorld) {
		getArenaConfig().set(CFG.LOCATION_WORLD, sWorld);
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
			for (PALocation spawnLoc : SpawnManager.getSpawns(this, sTeam)) {
				for (Location playerLoc : players.keySet()) {
					if (spawnLoc.getDistance(new PALocation(playerLoc)) < 3) {
						players.get(playerLoc)
								.get()
								.damage(getArenaConfig().getInt(CFG.DAMAGE_SPAWNCAMP));
					}
				}
			}
		}
	}

	public void spawnSet(String node, PALocation paLocation) {
		cfg.setManually("spawns." + node, Config.parseToString(paLocation));
		cfg.save();
	}

	public void spawnUnset(String node) {
		cfg.setManually("spawns." + node, null);
		cfg.save();
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
				if (ap.getStatus().equals(Status.LOUNGE)
						|| ap.getStatus().equals(Status.READY)) {
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
		}

		PVPArena.instance.getAgm().teleportAllToSpawn(this);

		broadcast(Language.parse(MSG.FIGHT_BEGINS));

		PVPArena.instance.getAmm().teleportAllToSpawn(this);

		db.i("teleported everyone!");

		SpawnCampRunnable scr = new SpawnCampRunnable(this, 0);
		SPAWNCAMP_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, scr, 100L,
				getArenaConfig().getInt(CFG.TIME_REGIONTIMER) * 20L);
		scr.setId(SPAWNCAMP_ID);

		for (ArenaRegionShape region : regions) {
			if (region.getFlags().size() > 0) {
				region.initTimer();
			} else if (region.getType().equals(RegionType.BATTLE)) {
				region.initTimer();
			}
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
	public synchronized void tellTeam(String sTeam, String msg, ChatColor c,
			Player player) {
		ArenaTeam team = this.getTeam(sTeam);
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
			if (getArenaConfig().getBoolean(CFG.CHAT_DEFAULTPRIVATE)
					&& getArenaConfig().getBoolean(CFG.CHAT_ENABLED)) {
				ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
				ap.setChatting(true);
			}
		}

		PVPArena.instance.getAmm().tpPlayerToCoordName(this, player, place);

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		if (place.equals("spectator")) {
			if (getFighters().contains(ap)) {
				ap.setStatus(Status.LOST);
			} else {
				ap.setStatus(Status.WATCH);
			}
		}
		PALocation loc = SpawnManager.getCoords(this, place);
		if (loc == null) {
			PVPArena.instance.getLogger().warning("Spawn null : " + place);
			return;
		}
		ap.setTelePass(true);
		player.teleport(loc.toLocation());
		ap.setTelePass(false);
	}

	public boolean tryJoin(Player player, ArenaTeam team) {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

		db.i("trying to join player " + player.getName());
		
		if (ap.getStatus().equals(Status.NULL)) {
			// joining DIRECTLY - save loc !!
			ap.setLocation(new PALocation(player.getLocation()));
		}
		
		if (ap.getArenaClass() == null) {
			String autoClass = cfg.getString(CFG.READY_AUTOCLASS);
			if (autoClass != null && !autoClass.equals("none")) {
				if (getClass(autoClass) == null) {
					msg(player, Language.parse(MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
					return false;
				}
			}
		}
		
		ap.setArena(this);
		team.add(ap);
		ap.setStatus(Status.FIGHT);
		tpPlayerToCoordName(player, (isFreeForAll()?"":team.getName()) + "spawn");
		
		Bukkit.getScheduler().scheduleAsyncDelayedTask(PVPArena.instance, new PlayerStateCreateRunnable(ap, player), 2L);
		return true;
	}

	private void updateGoals() {
		List<String> list = new ArrayList<String>();
		
		for (ArenaGoal goal : goals) {
			list.add(goal.getName());
		}
		
		cfg.set(CFG.LISTS_GOALS, list);
		cfg.save();
	}

	/**
	 * Setup an arena based on legacy goals:
	 * <pre>
	 * teams - team lives arena
	 * teamdm - team deathmatch arena
	 * dm - deathmatch arena
	 * free - deathmatch arena
	 * ctf - capture the flag arena
	 * ctp - capture the pumpkin arena
	 * spleef - free for all with teamkill off
	 * </pre>
	 * @param string legacy goal
	 */
	public void getLegacyGoals(String string) {
		setFree(false);
		
		if (string.equals("teams")) {
			goalAdd(PVPArena.instance.getAgm().getType("TeamLives"));
		} else if (string.equals("teamdm")) {
			goalAdd(PVPArena.instance.getAgm().getType("TeamDeathMatch"));
		} else if (string.equals("dm")) {
			goalAdd(PVPArena.instance.getAgm().getType("PlayerDeathMatch"));
			this.setFree(true);
		} else if (string.equals("free")) {
			goalAdd(PVPArena.instance.getAgm().getType("PlayerLives"));
			this.setFree(true);
		} else if (string.equals("spleef")) {
			goalAdd(PVPArena.instance.getAgm().getType("PlayerLives"));
			this.setFree(true);
			this.getArenaConfig().set(CFG.PERMS_TEAMKILL, false);
		} else if (string.equals("ctf")) {
			goalAdd(PVPArena.instance.getAgm().getType("Flags"));
		} else if (string.equals("ctp")) {
			goalAdd(PVPArena.instance.getAgm().getType("Flags"));
			for (ArenaGoal goal : getGoals()) {
				if (goal.getName().equals("Flags")) {
					String[] x = {"flagtype","PUMPKIN"};
					goal.commitCommand(Bukkit.getConsoleSender(), x);
				}
			}
		}
		
		updateGoals();
	}

	public HashSet<ArenaRegionShape> getRegionsByType(RegionType battle) {
		HashSet<ArenaRegionShape> result = new HashSet<ArenaRegionShape>();
		for (ArenaRegionShape rs : regions) {
			if (rs.getType().equals(battle))
				result.add(rs);
		}
		return result;
	}

	public void setRoundMap(ConfigurationSection cs) {
		// TODO Auto-generated method stub
		String seriously = "fix this";
		rounds = new PARoundMap(this, new HashSet<HashSet<String>>());
	}

	public void setRound(int i) {
		round = i;
	}
}
