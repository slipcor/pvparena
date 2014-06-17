package net.slipcor.pvparena.classes;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAStartEvent;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.ncloader.NCBLoadable;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;
import net.slipcor.pvparena.runnables.PVPActivateRunnable;
import net.slipcor.pvparena.runnables.SpawnCampRunnable;

/**
 * <pre>
 * PVP Arena Check class
 * </pre>
 * 
 * This class parses a complex check.
 * 
 * It is called staticly to iterate over all needed/possible modules to return
 * one committing module (inside the result) and to make modules listen to the
 * checked events if necessary
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class PACheck {
	private int priority = 0;
	private String error = null;
	private String modName = null;
	private final static Debug DEBUG = new Debug(9);

	/**
	 * 
	 * @return the error message
	 */
	public String getError() {
		return error;
	}

	/**
	 * 
	 * @return the module name returning the current result
	 */
	public String getModName() {
		return modName;
	}

	/**
	 * 
	 * @return the PACR priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * 
	 * @return true if there was an error
	 */
	public boolean hasError() {
		return error != null;
	}

	/**
	 * set the error message
	 * 
	 * @param error
	 *            the error message
	 */
	public void setError(final NCBLoadable loadable, final String error) {
		modName = loadable.getName();
		DEBUG.i(modName + " is setting error to: " + error);
		this.error = error;
		this.priority += 1000;
	}

	/**
	 * set the priority
	 * 
	 * @param priority
	 *            the priority
	 */
	public void setPriority(final NCBLoadable loadable, final int priority) {
		modName = loadable.getName();
		DEBUG.i(modName + " is setting priority to: " + priority);
		this.priority = priority;
	}

	public static boolean handleCommand(final Arena arena,
			final CommandSender sender, final String[] args) {
		int priority = 0;
		PACheck res = new PACheck();

		ArenaGoal commit = null;

		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkCommand(res, args[0]);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commit = null;
			}
		}

		if (res.hasError()) {
			arena.msg(Bukkit.getConsoleSender(),
					Language.parse(arena, MSG.ERROR_ERROR, res.getError()));
			return false;
		}
		if (commit == null) {
			for (ArenaModule am : arena.getMods()) {
				if (am.checkCommand(args[0].toLowerCase())) {
					am.commitCommand(sender, args);
					return true;
				}
			}

			return false;
		}

		commit.commitCommand(sender, args);
		return true;
	}

	public static boolean handleEnd(final Arena arena, final boolean force) {
		arena.getDebugger().i(
				"handleEnd: " + arena.getName() + "; force: " + force);
		int priority = 0;
		PACheck res = new PACheck();

		ArenaGoal commit = null;

		for (ArenaGoal mod : arena.getGoals()) {
			arena.getDebugger().i("checking " + mod.getName());
			res = mod.checkEnd(res);
			if (res.getPriority() > priority && priority >= 0) {
				arena.getDebugger().i("> success and higher priority");
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				arena.getDebugger().i("> fail");
				priority = res.getPriority();
				commit = null;
			}
		}

		if (res.hasError()) {
			arena.msg(Bukkit.getConsoleSender(),
					Language.parse(arena, MSG.ERROR_ERROR, res.getError()));
			if (commit != null) {
				arena.getDebugger().i(
						"error; committing end: " + commit.getName());
				commit.commitEnd(force);
				return true;
			}
			arena.getDebugger().i("error; FALSE!");
			return false;
		}

		if (commit == null) {
			arena.getDebugger().i("FALSE");
			return false;
		}

		arena.getDebugger().i("committing end: " + commit.getName());
		commit.commitEnd(force);
		return true;
	}

	public static int handleGetLives(final Arena arena,
			final ArenaPlayer aPlayer) {

		if (aPlayer.getStatus() == Status.LOUNGE
				|| aPlayer.getStatus() == Status.WATCH) {
			return 0;
		}

		PACheck res = new PACheck();
		int priority = 0;
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.getLives(res, aPlayer);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
			}
		}

		if (res.hasError()) {
			return Math.round(Float.valueOf(res.getError()));
		}
		return 0;
	}

	public static void handleInteract(final Arena arena, final Player player,
			final Cancellable event, final Block clickedBlock) {

		int priority = 0;
		PACheck res = new PACheck();

		ArenaGoal commit = null;

		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkInteract(res, player, clickedBlock);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commit = null;
			}
		}

		if (res.hasError()) {
			arena.msg(Bukkit.getConsoleSender(),
					Language.parse(arena, MSG.ERROR_ERROR, res.getError()));
			return;
		}

		if (commit == null) {
			return;
		}

		event.setCancelled(true);

		commit.commitInteract(player, clickedBlock);
	}

	public static void handleJoin(final Arena arena,
			final CommandSender sender, final String[] args) {

		if (PVPArena.arcade.isPlaying(sender.getName())) {
			String name = PVPArena.arcade.getPlugin(sender.getName());
			arena.msg(sender, Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, name));
			return;
		}
		int priority = 0;
		PACheck res = new PACheck();

		ArenaModule commModule = null;

		for (ArenaModule mod : arena.getMods()) {
			res = mod.checkJoin(sender, res, true);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commModule = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commModule = null;
			}
		}

		if (commModule != null
				&& !ArenaManager.checkJoin((Player) sender, arena)) {
			res.setError(commModule,
					Language.parse(arena, MSG.ERROR_JOIN_REGION));
		}

		if (res.hasError() && !res.getModName().equals("LateLounge")) {
			arena.msg(sender,
					Language.parse(arena, MSG.ERROR_ERROR, res.getError()));
			return;
		}

		if (res.hasError()) {
			arena.msg(sender,
					Language.parse(arena, MSG.NOTICE_NOTICE, res.getError()));
			return;
		}

		ArenaGoal commGoal = null;

		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkJoin(sender, res, args);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commGoal = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commGoal = null;
			}
		}

		if (commGoal != null && !ArenaManager.checkJoin((Player) sender, arena)) {
			res.setError(commGoal, Language.parse(arena, MSG.ERROR_JOIN_REGION));
		}

		if (res.hasError()) {
			arena.msg(sender,
					Language.parse(arena, MSG.ERROR_ERROR, res.getError()));
			return;
		}

		ArenaTeam team = null;

		if (args.length < 1 || (arena.getTeam(args[0]) == null)) {
			// usage: /pa {arenaname} join | join an arena

			team = arena.getTeam(TeamManager.calcFreeTeam(arena));
		} else {
			team = arena.getTeam(args[0]);
		}

		if (team == null && args != null && args.length > 0) {
			arena.msg(sender,
					Language.parse(arena, MSG.ERROR_TEAMNOTFOUND, args[0]));
			return;
		} else if (team == null) {
			arena.msg(sender, Language.parse(arena, MSG.ERROR_JOIN_ARENA_FULL));
			return;
		}

		ArenaPlayer player = ArenaPlayer.parsePlayer(sender.getName());

		ArenaModuleManager.choosePlayerTeam(arena, (Player) sender,
				team.getColoredName());

		arena.markPlayedPlayer(sender.getName());

		player.setPublicChatting(!arena.getArenaConfig().getBoolean(
				CFG.CHAT_DEFAULTTEAM));

		if ((commModule == null) || (commGoal == null)) {

			if (commModule != null) {
				commModule.commitJoin((Player) sender, team);

				ArenaModuleManager.parseJoin(res, arena, (Player) sender, team);
				return;
			}
			if (!ArenaManager.checkJoin((Player) sender, arena)) {
				arena.msg(sender, Language.parse(arena, MSG.ERROR_JOIN_REGION));
				return;
			}
			// both null, just put the joiner to some spawn

			if (!arena.tryJoin((Player) sender, team)) {
				return;
			}

			if (arena.isFreeForAll()) {
				arena.msg(sender,
						arena.getArenaConfig().getString(CFG.MSG_YOUJOINED));
				arena.broadcastExcept(
						sender,
						Language.parse(arena, CFG.MSG_PLAYERJOINED,
								sender.getName()));
			} else {
				arena.msg(
						sender,
						arena.getArenaConfig()
								.getString(CFG.MSG_YOUJOINEDTEAM)
								.replace(
										"%1%",
										team.getColoredName()
												+ ChatColor.COLOR_CHAR + "r"));
				arena.broadcastExcept(
						sender,
						Language.parse(arena, CFG.MSG_PLAYERJOINEDTEAM,
								sender.getName(), team.getColoredName()
										+ ChatColor.COLOR_CHAR + "r"));
			}
			ArenaModuleManager.parseJoin(res, arena, (Player) sender, team);

			PVPArena.instance.getAgm().initiate(arena, (Player) sender);
			ArenaModuleManager.initiate(arena, (Player) sender);

			if (arena.getFighters().size() > 1
					&& arena.getFighters().size() >= arena.getArenaConfig()
							.getInt(CFG.READY_MINPLAYERS)) {
				arena.setFightInProgress(true);
				for (ArenaTeam ateam : arena.getTeams()) {
					SpawnManager.distribute(arena, ateam);
				}

				for (ArenaGoal goal : arena.getGoals()) {
					goal.parseStart();
				}

				for (ArenaModule mod : arena.getMods()) {
					mod.parseStart();
				}
			}

			if (player.getArenaClass() != null && arena.startRunner != null) {
				player.setStatus(Status.READY);
			}

			return;
		}

		commModule.commitJoin((Player) sender, team);

		ArenaModuleManager.parseJoin(res, arena, (Player) sender, team);

		if (player.getArenaClass() != null && arena.startRunner != null) {
			player.setStatus(Status.READY);
		}
	}

	public static void handlePlayerDeath(final Arena arena,
			final Player player, final PlayerDeathEvent event) {
		boolean doesRespawn = true;

		int priority = 0;
		PACheck res = new PACheck();
		arena.getDebugger().i("handlePlayerDeath", player);

		ArenaGoal commit = null;

		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkPlayerDeath(res, player);
			if (res.getPriority() > priority && priority >= 0) {
				arena.getDebugger().i("success and higher priority", player);
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				arena.getDebugger().i("fail", player);
				// fail
				priority = res.getPriority();
				commit = null;
			} else {
				arena.getDebugger().i("else", player);
			}
		}

		if (res.hasError()) {
			arena.getDebugger().i("has error: " + res.getError(), player);
			if (res.getError().equals("0")) {
				doesRespawn = false;
			}
		}

		StatisticsManager.kill(arena, player.getKiller(), player, doesRespawn);
		event.setDeathMessage(null);

		if (player.getKiller() != null) {
			player.getKiller().setFoodLevel(
					player.getKiller().getFoodLevel()
							+ arena.getArenaConfig().getInt(
									CFG.PLAYER_FEEDFORKILL));
		}

		if (!arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
			event.getDrops().clear();
		}

		if (commit == null) {
			arena.getDebugger().i("no mod handles player deaths", player);


			List<ItemStack> returned = null;
			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(
							CFG.PLAYER_DROPSINVENTORY)) {
				returned = InventoryManager.drop(player);
				int exp = event.getDroppedExp();
				event.getDrops().clear();
				if (doesRespawn
						|| arena.getArenaConfig().getBoolean(
								CFG.PLAYER_PREVENTDEATH)) {
					InventoryManager.dropExp(player, exp);
				} else if (arena.getArenaConfig().getBoolean(
						CFG.PLAYER_DROPSEXP)) {
					arena.getDebugger().i("exp: " + exp, player);
					event.setDroppedExp(exp);
				}
			}
			final ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(
					player.getName()).getArenaTeam();

			if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
				/*
				 * if (respawnTeam == null) {
				 * PVPArena.instance.getLogger().severe("respawnTeam!"); } else
				 * if (event.getEntity() == null) {
				 * PVPArena.instance.getLogger().severe("event.getEntity()!"); }
				 * else if (event.getEntity().getLastDamageCause() == null) {
				 * PVPArena.instance.getLogger().severe(
				 * "event.getEntity().getLastDamageCause()!"); }
				 */
				arena.broadcast(Language.parse(arena, MSG.FIGHT_KILLED_BY,
						respawnTeam.colorizePlayer(player) + ChatColor.YELLOW,
						arena.parseDeathCause(player, event.getEntity()
								.getLastDamageCause().getCause(), event
								.getEntity().getKiller())));
			}
			
			ArenaModuleManager.parsePlayerDeath(arena, player, event
					.getEntity().getLastDamageCause());

			if (returned == null) {
				if (arena.isCustomClassAlive()
						|| arena.getArenaConfig().getBoolean(
								CFG.PLAYER_DROPSINVENTORY)) {
						returned = InventoryManager.drop(player);
						event.getDrops().clear();
				} else {
					returned = event.getDrops();
				}
			}
			
			handleRespawn(arena, ArenaPlayer.parsePlayer(player.getName()),
					returned);

			for (ArenaGoal g : arena.getGoals()) {
				g.parsePlayerDeath(player, player.getLastDamageCause());
			}

			return;
		}

		arena.getDebugger().i("handled by: " + commit.getName(), player);
		int exp = event.getDroppedExp();

        commit.commitPlayerDeath(player, doesRespawn, res.getError(), event);
		for (ArenaGoal g : arena.getGoals()) {
			arena.getDebugger().i("parsing death: " + g.getName(), player);
			g.parsePlayerDeath(player, player.getLastDamageCause());
		}

        ArenaModuleManager.parsePlayerDeath(arena, player,
                player.getLastDamageCause());

		if (doesRespawn
				|| arena.getArenaConfig().getBoolean(CFG.PLAYER_PREVENTDEATH)) {
			InventoryManager.dropExp(player, exp);
		} else if (arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSEXP)) {
			event.setDroppedExp(exp);
			arena.getDebugger().i("exp: " + exp, player);
		}
	}

	public static void handleRespawn(final Arena arena,
			final ArenaPlayer aPlayer, final List<ItemStack> drops) {

		for (ArenaModule mod : arena.getMods()) {
			if (mod.tryDeathOverride(aPlayer, drops)) {
				return;
			}
		}
		arena.getDebugger().i("handleRespawn!", aPlayer.getName());
		new InventoryRefillRunnable(arena, aPlayer.get(), drops);
		SpawnManager.respawn(arena, aPlayer, null);
		arena.unKillPlayer(aPlayer.get(),
				aPlayer.get().getLastDamageCause() == null ? null : aPlayer
						.get().getLastDamageCause().getCause(), aPlayer.get()
						.getKiller());

	}

	/**
	 * try to set a flag
	 * 
	 * @param player
	 *            the player trying to set
	 * @param block
	 *            the block being set
	 * @return true if the handling is successful and if the event should be
	 *         cancelled
	 */
	public static boolean handleSetFlag(final Player player, final Block block) {
		final Arena arena = PAA_Region.activeSelections.get(player.getName());

		if (arena == null) {
			return false;
		}

		int priority = 0;
		PACheck res = new PACheck();

		ArenaGoal commit = null;

		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkSetBlock(res, player, block);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commit = null;
			}
		}

		if (res.hasError()) {
			arena.msg(Bukkit.getConsoleSender(),
					Language.parse(arena, MSG.ERROR_ERROR, res.getError()));
			return false;
		}

		if (commit == null) {
			return false;
		}

		return commit.commitSetFlag(player, block);
	}

	public static void handleSpectate(final Arena arena,
			final CommandSender sender) {
		int priority = 0;
		PACheck res = new PACheck();

		arena.getDebugger().i("handling spectator", sender);

		// priority will be set by flags, the max priority will be called

		ArenaModule commit = null;

		for (ArenaModule mod : arena.getMods()) {
			res = mod.checkJoin(sender, res, false);
			if (res.getPriority() > priority && priority >= 0) {
				arena.getDebugger().i("success and higher priority", sender);
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				arena.getDebugger().i("fail", sender);
				priority = res.getPriority();
				commit = null;
			}
		}

		if (res.hasError()) {
			arena.msg(sender,
					Language.parse(arena, MSG.ERROR_ERROR, res.getError()));
			return;
		}

		if (commit == null) {
			arena.getDebugger().i("commit null", sender);
			return;
		}

		commit.commitSpectate((Player) sender);
	}

	public static Boolean handleStart(final Arena arena,
			final CommandSender sender) {
		return handleStart(arena, sender, false);
	}

	public static Boolean handleStart(final Arena arena,
			final CommandSender sender, final boolean force) {
		PACheck res = new PACheck();

		arena.getDebugger().i("handling start!");

		ArenaGoal commit = null;
		int priority = 0;

		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkStart(res);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
				commit = mod;
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
				commit = null;
			}
		}

		if (!force && res.hasError()) {
			arena.getDebugger().i("not forcing and we have error: " + res.getError());
			if (sender == null) {
				arena.msg(Bukkit.getConsoleSender(),
						Language.parse(arena, MSG.ERROR_ERROR, res.getError()));
			} else {
				arena.msg(sender,
						Language.parse(arena, MSG.ERROR_ERROR, res.getError()));
			}
			return null;
		}
		
		if (!force && arena.getFighters().size() < 2
				|| arena.getFighters().size() < arena.getArenaConfig().getInt(
						CFG.READY_MINPLAYERS)) {
			arena.getDebugger().i("not forcing and we have less than minplayers");
			return null;
		}

		final PAStartEvent event = new PAStartEvent(arena);
		Bukkit.getPluginManager().callEvent(event);
		if (!force && event.isCancelled()) {
			arena.getDebugger().i("not forcing and cancelled by other plugin");
			return false;
		}

		arena.getDebugger()
				.i("teleporting all players to their spawns", sender);

		if (commit == null) {
			for (ArenaTeam team : arena.getTeams()) {
				SpawnManager.distribute(arena, team);
			}
		} else {
			commit.commitStart(); // override spawning
		}

		arena.getDebugger().i("teleported everyone!", sender);

		arena.broadcast(Language.parse(arena, MSG.FIGHT_BEGINS));
		arena.setFightInProgress(true);

		for (ArenaGoal x : arena.getGoals()) {
			x.parseStart();
		}

		for (ArenaModule x : arena.getMods()) {
			x.parseStart();
		}

		final SpawnCampRunnable scr = new SpawnCampRunnable(arena, 0);
		arena.spawnCampRunnerID = Bukkit.getScheduler()
				.scheduleSyncRepeatingTask(PVPArena.instance, scr, 100L,
						arena.getArenaConfig().getInt(CFG.TIME_REGIONTIMER));
		scr.setId(arena.spawnCampRunnerID);

		for (ArenaRegion region : arena.getRegions()) {
			if (region.getFlags().size() > 0) {
				region.initTimer();
			} else if (region.getType().equals(RegionType.BATTLE)) {
				region.initTimer();
			}
		}

		if (arena.getArenaConfig().getInt(CFG.TIME_PVP) > 0) {
			arena.pvpRunner = new PVPActivateRunnable(arena, arena
					.getArenaConfig().getInt(CFG.TIME_PVP));
		}

		arena.setStartingTime();
		return true;
	}
}
/*
 * AVAILABLE PACheckResults:
 * 
 * ArenaGoal.checkCommand() => ArenaGoal.commitCommand() ( onCommand() ) >
 * default: nothing
 * 
 * 
 * ArenaGoal.checkEnd() => ArenaGoal.commitEnd() (
 * ArenaGoalManager.checkEndAndCommit(arena) ) < used > 1: PlayerLives > 2:
 * PlayerDeathMatch > 3: TeamLives > 4: TeamDeathMatch > 5: Flags
 * 
 * ArenaGoal.checkInteract() => ArenaGoal.commitInteract() (
 * PlayerListener.onPlayerInteract() ) > 5: Flags
 * 
 * ArenaGoal.checkJoin() => ArenaGoal.commitJoin() ( PAG_Join ) < used >
 * default: tp inside
 * 
 * ArenaGoal.checkPlayerDeath() => ArenaGoal.commitPlayerDeath() (
 * PlayerLister.onPlayerDeath() ) > 1: PlayerLives > 2: PlayerDeathMatch > 3:
 * TeamLives > 4: TeamDeathMatch > 5: Flags
 * 
 * ArenaGoal.checkSetFlag() => ArenaGoal.commitSetFlag() (
 * PlayerListener.onPlayerInteract() ) > 5: Flags
 * 
 * =================================
 * 
 * ArenaModule.checkJoin() ( PAG_Join | PAG_Spectate ) < used > 1:
 * StandardLounge > 2: BattlefieldJoin > default: nothing
 * 
 * ArenaModule.checkStart() ( PAI_Ready | StartRunnable.commit() ) < used >
 * default: tp players to (team) spawns
 */
