package net.slipcor.pvparena.classes;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.PlayerDeathEvent;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAStartEvent;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.ncloader.NCBLoadable;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;
import net.slipcor.pvparena.runnables.SpawnCampRunnable;

/**
 * <pre>PVP Arena Check class</pre>
 * 
 * This class parses a complex check.
 * 
 * It is called staticly to iterate over all needed/possible modules
 * to return one committing module (inside the result) and to make
 * modules listen to the checked events if necessary
 * 
 * @author slipcor
 * 
 * @version v0.9.8
 */

public class PACheck {
	private int priority = 0;
	private String error = null;
	private String modName = null;
	private static Debug db = new Debug(9);
	
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
	 * @param error the error message
	 */
	public void setError(NCBLoadable loadable, String error) {
		modName = loadable.getName();
		db.i(modName + " is setting error to: " + error);
		this.error = error;
		this.priority += 1000;
	}
	
	/**
	 * set the priority
	 * @param priority the priority
	 */
	public void setPriority(NCBLoadable loadable, int priority) {
		modName = loadable.getName();
		db.i(modName + " is setting priority to: " + priority);
		this.priority = priority;
	}
	
	public static boolean handleCommand(Arena arena, CommandSender sender, String[] args) {
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
			arena.msg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_ERROR, res.getError()));
			return false;
		}
		if (commit == null) {
			for (ArenaModule am : PVPArena.instance.getAmm().getModules()) {
				if (am.isActive(arena)) {
					if (am.checkCommand(args[0])) {
						am.commitCommand(arena, sender, args);
						return true;
					}
				}
			}
			
			return false;
		}
		
		commit.commitCommand(sender, args);
		return true;
	}
	
	public static boolean handleEnd(Arena arena, boolean force) {
		int priority = 0;
		PACheck res = new PACheck();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkEnd(res);
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
			arena.msg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_ERROR, res.getError()));
			return false;
		}
		
		if (commit == null) {
			return false;
		}
		
		commit.commitEnd(force);
		return true;
	}
	
	public static int handleGetLives(Arena arena, ArenaPlayer ap) {
		PACheck res = new PACheck();
		int priority = 0;
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.getLives(res, ap);
			if (res.getPriority() > priority && priority >= 0) {
				// success and higher priority
				priority = res.getPriority();
			} else if (res.getPriority() < 0 || priority < 0) {
				// fail
				priority = res.getPriority();
			}
		}
		
		if (res.hasError()) {
			return Integer.valueOf(res.getError());
		}
		return 0;
	}

	public static void handleInteract(Arena arena, Player player, Cancellable event, Block clickedBlock) {

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
			arena.msg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_ERROR, res.getError()));
			return;
		}
		
		if (commit == null) {
			return;
		}
		
		event.setCancelled(true);
		
		commit.commitInteract(player, clickedBlock);
	}

	public static void handleJoin(Arena arena, CommandSender sender, String[] args) {
		int priority = 0;
		PACheck res = new PACheck();
				
			ArenaModule commModule = null;
			
			for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
				if (mod.isActive(arena)) {
					res = mod.checkJoin(arena, sender, res, true);
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
			}
			
			if (res.hasError() && !res.getModName().equals("LateLounge")) {
				arena.msg(sender, Language.parse(MSG.ERROR_ERROR, res.getError()));
				return;
			}
			
			if (res.hasError()) {
				arena.msg(sender, Language.parse(MSG.NOTICE_NOTICE, res.getError()));
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
			
			if (res.hasError()) {
				arena.msg(sender, Language.parse(MSG.ERROR_ERROR, res.getError()));
				return;
			}
			
			if (args.length < 1 || (arena.getTeam(args[0]) == null)) {
				// usage: /pa {arenaname} join | join an arena

				args = new String[]{TeamManager.calcFreeTeam(arena)};
			}
			
			ArenaTeam team = arena.getTeam(args[0]);
			
			if (team == null && args != null) {
				arena.msg(sender, Language.parse(MSG.ERROR_TEAMNOTFOUND, args[0]));
				return;
			} else if (team == null) {
				arena.msg(sender, Language.parse(MSG.ERROR_JOIN_ARENA_FULL));
				return;
			}
			
			arena.markPlayedPlayer(sender.getName());
			
			if ((commModule == null) || (commGoal == null)) {
				if (commModule != null) {
					commModule.commitJoin(arena, (Player) sender, team);
					PVPArena.instance.getAmm().parseJoin(res, arena, (Player) sender, team);
					return;
				}
				
				// both null, just put the joiner to some spawn
				
				if (!arena.tryJoin((Player) sender, team)) {
					return;
				}
				
				if (arena.isFreeForAll()) {
					arena.msg(sender, arena.getArenaConfig().getString(CFG.MSG_YOUJOINED));
					arena.broadcastExcept(sender, Language.parse(arena, CFG.MSG_PLAYERJOINED, sender.getName()));
				} else {
					arena.msg(sender, arena.getArenaConfig().getString(CFG.MSG_YOUJOINEDTEAM).replace("%1%", team.getColoredName() + "§r"));
					arena.broadcastExcept(sender, Language.parse(arena, CFG.MSG_PLAYERJOINEDTEAM, sender.getName(), team.getColoredName() + "§r"));
				}
				
				PVPArena.instance.getAgm().initiate(arena, (Player) sender);
				
				if (arena.getFighters().size() == 2) {
					arena.broadcast(Language.parse(MSG.FIGHT_BEGINS));
					arena.setFightInProgress(true);
					for (ArenaPlayer p : arena.getFighters()) {
						if (p.getName().equals(sender.getName())) {
							continue;
						}
						arena.tpPlayerToCoordName(p.get(), (arena.isFreeForAll()?"":p.getArenaTeam().getName())
								+ "spawn");
					}
				}
				
				return;
			}

			commModule.commitJoin(arena, (Player) sender, team);
			PVPArena.instance.getAmm().parseJoin(res, arena, (Player) sender, team);
	}

	public static void handlePlayerDeath(Arena arena, Player player, PlayerDeathEvent event) {
		boolean doesRespawn = true;
		
		int priority = 0;
		PACheck res = new PACheck();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkPlayerDeath(res, player);
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
			// lives
			if (res.getError().equals("0")) {
				doesRespawn = false;
			}
		}

		StatisticsManager.kill(arena, player.getLastDamageCause().getEntity(), player, doesRespawn);
		event.setDeathMessage(null);
		
		if (!arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
			event.getDrops().clear();
		}
		
		if (commit == null) {
			// no mod handles player deaths, default to infinite lives. Respawn player
			
			new InventoryRefillRunnable(arena, player, event.getDrops());

			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(player);
				event.getDrops().clear();
			}
			
			arena.unKillPlayer(player, event.getEntity().getLastDamageCause().getCause(), player.getKiller());
			return;
		}
		
		commit.commitPlayerDeath(player, doesRespawn, res.getError(), event);
		for (ArenaGoal g : arena.getGoals()) {
			g.parsePlayerDeath(player, player.getLastDamageCause());
		}
				
		for (ArenaModule m : PVPArena.instance.getAmm().getModules()) {
			if (m.isActive(arena)) {
				m.parsePlayerDeath(arena, player, player.getLastDamageCause());
			}
		}
	}

	public static boolean handleSetFlag(Player player, Block block) {
		Arena arena = PAA_Region.activeSelections.get(player.getName());
		
		if (arena == null) {
			return false;
		}
		
		int priority = 0;
		PACheck res = new PACheck();
		
		ArenaGoal commit = null;
		
		for (ArenaGoal mod : arena.getGoals()) {
			res = mod.checkSetFlag(res, player, block);
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
			arena.msg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_ERROR, res.getError()));
			return false;
		}
		
		if (commit == null) {
			return false;
		}
		
		return commit.commitSetFlag(player, block);
	}

	public static void handleSpectate(Arena arena, CommandSender sender) {
		int priority = 0;
		PACheck res = new PACheck();
		
		// priority will be set by flags, the max priority will be called
		
		ArenaModule commit = null;
		
		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			if (mod.isActive(arena)) {
				res = mod.checkJoin(arena, sender, res, false);
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
		}
		
		if (res.hasError()) {
			arena.msg(sender, Language.parse(MSG.ERROR_ERROR, res.getError()));
			return;
		}
		
		if (commit == null) {
			return;
		}
		
		commit.commitSpectate(arena, (Player) sender);
		/*
		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			if (mod.isActive(arena)) {
				commit.parseSpectate(arena, (Player) sender);
			}
		}*/
	}

	public static void handleStart(Arena arena, CommandSender sender) {
		PACheck res = new PACheck();

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
		
		if (res.hasError()) {
			if (sender == null) {
				sender = Bukkit.getConsoleSender();
			}
			arena.msg(sender, Language.parse(MSG.ERROR_ERROR, res.getError()));
			return;
		}
		
		PAStartEvent event = new PAStartEvent(arena);
		Bukkit.getPluginManager().callEvent(event);
		
		db.i("teleporting all players to their spawns");

		if (commit != null) {
			commit.commitStart(); // override spawning
		} else {
		
			if (!arena.isFreeForAll()) {
				for (ArenaTeam team : arena.getTeams()) {
					for (ArenaPlayer ap : team.getTeamMembers()) {
						arena.tpPlayerToCoordName(ap.get(), team.getName() + "spawn");
						ap.setStatus(Status.FIGHT);
					}
				}
			} else {
				//TODO replace with better way
				for (ArenaTeam team : arena.getTeams()) {
					for (ArenaPlayer ap : team.getTeamMembers()) {
						if (arena.isFreeForAll()) {
							arena.tpPlayerToCoordName(ap.get(), "spawn");
							ap.setStatus(Status.FIGHT);
						}
					}
				}
			}
		}
		
		for (ArenaGoal goal : arena.getGoals()) {
			goal.parseStart();
		}
		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			if (mod.isActive(arena)) {
				mod.teleportAllToSpawn(arena);
			}
		}

		db.i("teleported everyone!");

		arena.broadcast(Language.parse(MSG.FIGHT_BEGINS));

		SpawnCampRunnable scr = new SpawnCampRunnable(arena, 0);
		arena.SPAWNCAMP_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, scr, 100L,
				arena.getArenaConfig().getInt(CFG.TIME_REGIONTIMER));
		scr.setId(arena.SPAWNCAMP_ID);

		for (ArenaRegionShape region : arena.getRegions()) {
			if (region.getFlags().size() > 0) {
				region.initTimer();
			} else if (region.getType().equals(RegionType.BATTLE)) {
				region.initTimer();
			}
		}
	}
}
/*
 * AVAILABLE PACheckResults:
 * 
 * ArenaGoal.checkCommand() => ArenaGoal.commitCommand()
 * ( onCommand() )
 * > default: nothing
 * 
 * 
 * ArenaGoal.checkEnd() => ArenaGoal.commitEnd()
 * ( ArenaGoalManager.checkEndAndCommit(arena) ) < used
 * > 1: PlayerLives
 * > 2: PlayerDeathMatch
 * > 3: TeamLives
 * > 4: TeamDeathMatch
 * > 5: Flags
 * 
 * ArenaGoal.checkInteract() => ArenaGoal.commitInteract()
 * ( PlayerListener.onPlayerInteract() )
 * > 5: Flags
 * 
 * ArenaGoal.checkJoin() => ArenaGoal.commitJoin()
 * ( PAG_Join ) < used
 * > default: tp inside
 * 
 * ArenaGoal.checkPlayerDeath() => ArenaGoal.commitPlayerDeath()
 * ( PlayerLister.onPlayerDeath() )
 * > 1: PlayerLives
 * > 2: PlayerDeathMatch
 * > 3: TeamLives
 * > 4: TeamDeathMatch
 * > 5: Flags
 * 
 * ArenaGoal.checkSetFlag() => ArenaGoal.commitSetFlag()
 * ( PlayerListener.onPlayerInteract() )
 * > 5: Flags
 * 
 * =================================
 * 
 * ArenaModule.checkJoin()
 * ( PAG_Join | PAG_Spectate ) < used
 * > 1: StandardLounge
 * > 2: BattlefieldJoin
 * > default: nothing
 * 
 * ArenaModule.checkStart()
 * ( PAI_Ready | StartRunnable.commit() ) < used
 * > default: tp players to (team) spawns
 * 
 */
