package net.slipcor.pvparena.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.PlayerState;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PASpawn;
import net.slipcor.pvparena.commands.PAA_Setup;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.Updater;
import net.slipcor.pvparena.core.Updater.UpdateResult;
import net.slipcor.pvparena.loadables.ArenaGoalManager;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionProtection;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.TeamManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

/**
 * <pre>
 * Player Listener class
 * </pre>
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class PlayerListener implements Listener {
	private final static Debug DEBUG = new Debug(23);

	private boolean checkAndCommitCancel(final Arena arena, final Player player,
			final Cancellable event) {
		
		if(willBeCancelled(player, event)) {
			return true;
		}
		
		if (!(event instanceof PlayerInteractEvent)) {
			return false;
		}
		final PlayerInteractEvent pie = (PlayerInteractEvent) event;
		final Material mat = pie.getClickedBlock().getType();
		final Material check = arena == null ? Material.IRON_BLOCK : arena
				.getReadyBlock();
		if (mat == Material.SIGN || mat == Material.SIGN_POST
				|| mat == Material.WALL_SIGN || mat == check) {
			DEBUG.i("signs and ready blocks allowed!", player);
			DEBUG.i("> false", player);
			return false;
		}

		DEBUG.i("checkAndCommitCancel", player);
		if (arena == null || player.hasPermission("pvparena.admin")) {
			DEBUG.i("no arena or admin", player);
			DEBUG.i("> false", player);
			return false;
		}

		if (arena != null) {
			if (arena.getArenaConfig().getBoolean(CFG.PERMS_LOUNGEINTERACT)) {
				return false;
			}
			if (!arena.isFightInProgress()) {
				arena.getDebugger().i("arena != null and fight not in progress => cancel", player);
				arena.getDebugger().i("> true", player);

				PACheck.handleInteract(arena, player, pie, pie.getClickedBlock());
				event.setCancelled(true);
				return true;
			}
			
		}

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

		if (aPlayer.getStatus() != Status.FIGHT) {
			DEBUG.i("not fighting => cancel", player);
			DEBUG.i("> true", player);
			event.setCancelled(true);
			return true;
		}

		DEBUG.i("> false", player);
		return false;
	}

	private boolean willBeCancelled(Player player, Cancellable event) {
		if(ArenaPlayer.parsePlayer(player.getName()).getStatus() == Status.LOST) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {

		final Player player = event.getPlayer();
		
		if (PAA_Setup.activeSetups.containsKey(player.getName())) {
			PAA_Setup.chat(player, event.getMessage());
			return;
		}

		final Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

		if (arena == null) {
			return; // no fighting player => OUT
		}
		final ArenaTeam team = aPlayer.getArenaTeam();
		if (team == null ||
				(aPlayer.getStatus() == Status.DEAD && aPlayer.get() == null) ||
				aPlayer.getStatus() == Status.LOST ||
				aPlayer.getStatus() == Status.WATCH) {
			if (!arena.getArenaConfig().getBoolean(CFG.PERMS_SPECTALK)) {
				event.setCancelled(true);
			}
			return; // no fighting player => OUT
		}
		arena.getDebugger().i("fighting player chatting!", player);
		final String sTeam = team.getName();

		if (!arena.getArenaConfig().getBoolean(CFG.CHAT_ONLYPRIVATE)) {
			if (!arena.getArenaConfig().getBoolean(CFG.CHAT_ENABLED)) {
				return; // no chat editing
			}

			if (aPlayer.isPublicChatting()) {
				return; // player not privately chatting
			}

			arena.tellTeam(sTeam, event.getMessage(), team.getColor(),
					event.getPlayer());
			event.setCancelled(true);
			return;
		}

		if (arena.getArenaConfig().getBoolean(CFG.CHAT_ENABLED)
				&& !aPlayer.isPublicChatting()) {
			arena.tellTeam(sTeam, event.getMessage(), team.getColor(),
					event.getPlayer());
			event.setCancelled(true);
			return;
		}

		arena.broadcastColored(event.getMessage(), team.getColor(),
				event.getPlayer()); //
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
		final Player player = event.getPlayer();
		
		if (PAA_Setup.activeSetups.containsKey(player.getName())) {
			PAA_Setup.chat(player, event.getMessage().substring(1));
			return;
		}

		final Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null || player.isOp() || PVPArena.hasAdminPerms(player)
				|| PVPArena.hasCreatePerms(player, arena)) {
			return; // no fighting player => OUT
		}

		final List<String> list = PVPArena.instance.getConfig().getStringList(
				"whitelist");
		list.add("pa");
		list.add("pvparena");
		arena.getDebugger().i("checking command whitelist", player);

		for (String s : list) {
			if (event.getMessage().startsWith("/" + s)) {
				arena.getDebugger().i("command allowed: " + s, player);
				return;
			}
		}

		list.clear();
		list.addAll(arena.getArenaConfig().getStringList(
				CFG.LISTS_CMDWHITELIST.getNode(), new ArrayList<String>()));

		if (list == null || list.size() < 1) {
			list.clear();
			list.add("ungod");
			arena.getArenaConfig().set(CFG.LISTS_CMDWHITELIST, list);
			arena.getArenaConfig().save();
		}

		list.add("pa");
		list.add("pvparena");
		arena.getDebugger().i("checking command whitelist", player);

		for (String s : list) {
			if (event.getMessage().startsWith("/" + s)) {
				arena.getDebugger().i("command allowed: " + s, player);
				return;
			}
		}

		arena.getDebugger().i("command blocked: " + event.getMessage(), player);
		arena.msg(player,
				Language.parse(arena, MSG.ERROR_COMMAND_BLOCKED, event.getMessage()));
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
		final Player player = event.getPlayer();
		
		if (willBeCancelled(player, event)) {
			return;
		}
		
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final Arena arena = aPlayer.getArena();
		if (arena == null) {
			return; // no fighting player => OUT
		}
		if (aPlayer.getStatus().equals(Status.READY)
				|| aPlayer.getStatus().equals(Status.LOUNGE)) {
			event.setCancelled(true);
			arena.msg(player, Language.parse(arena, MSG.NOTICE_NO_DROP_ITEM));
			return;
		}
		if (!BlockListener.isProtected(player.getLocation(), event,
				RegionProtection.DROP)) {
			return; // no drop protection
		}

		arena.getDebugger().i("onPlayerDropItem: fighting player", player);
		arena.msg(player, Language.parse(arena, MSG.NOTICE_NO_DROP_ITEM));
		event.setCancelled(true);
		// cancel the drop event for fighting players, with message
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null) {
			return;
		}
		PACheck.handlePlayerDeath(arena, player, event);
	}

	/**
	 * pretend a player death
	 * 
	 * @param arena
	 *            the arena the player is playing in
	 * @param player
	 *            the player to kill
	 * @param eEvent
	 *            the event triggering the death
	 */
	public static void finallyKillPlayer(final Arena arena, final Player player,
			final Event eEvent) {
		EntityDamageEvent cause = null;

		if (eEvent instanceof EntityDeathEvent) {
			cause = player.getLastDamageCause();
		} else if (eEvent instanceof EntityDamageEvent) {
			cause = ((EntityDamageEvent) eEvent);
		}

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final ArenaTeam team = aPlayer.getArenaTeam();

		final String playerName = (team == null) ? player.getName() : team.colorizePlayer(player);
		if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
			arena.broadcast(Language.parse(arena,
					MSG.FIGHT_KILLED_BY,
					playerName + ChatColor.YELLOW,
					arena.parseDeathCause(player, cause.getCause(),
							ArenaPlayer.getLastDamagingPlayer(cause, player))));
		}
		if (arena.isCustomClassAlive()
				|| arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
			InventoryManager.drop(player);
		}

		if (ArenaPlayer.parsePlayer(player.getName()).getArenaClass() == null
				|| !ArenaPlayer.parsePlayer(player.getName()).getArenaClass()
						.getName().equalsIgnoreCase("custom")) {
			InventoryManager.clearInventory(player);
		}

		arena.removePlayer(player,
				arena.getArenaConfig().getString(CFG.TP_DEATH), true, false);

		aPlayer.setStatus(Status.LOST);
		aPlayer.addDeath();

		PlayerState.fullReset(arena, player);

		if (ArenaManager.checkAndCommit(arena, false)) {
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerHunger(final FoodLevelChangeEvent event) {
		if (event.getEntityType() != EntityType.PLAYER) {
			return;
		}
		
		Player player = (Player)event.getEntity();
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		
		if (ap.getStatus() == Status.READY || ap.getStatus() == Status.LOUNGE) {
			event.setCancelled(true);
		} else if (ap.getArena() != null && !ap.getArena().getArenaConfig().getBoolean(CFG.PLAYER_HUNGER)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		DEBUG.i("onPlayerInteract", player);

		if (event.getAction().equals(Action.PHYSICAL)) {
			DEBUG.i("returning: physical", player);
			return;
		}

		DEBUG.i("event pre cancelled: " + event.isCancelled(),
				player);

		Arena arena = null;

		if (event.hasBlock()) {
			DEBUG.i("block: " + event.getClickedBlock().getType().name(), player);
			
			arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(
					event.getClickedBlock().getLocation()));
			if (checkAndCommitCancel(arena, event.getPlayer(), event)) {
				if (arena != null) {
					PACheck.handleInteract(arena, player, event, event.getClickedBlock());
				}
				return;
			}
		}

		if (arena != null && ArenaModuleManager.onPlayerInteract(arena, event)) {
			DEBUG.i("returning: #1", player);
			return;
		}

		if (PACheck.handleSetFlag(player, event.getClickedBlock())) {
			DEBUG.i("returning: #2", player);
			event.setCancelled(true);
			return;
		}

		if (ArenaRegion.checkRegionSetPosition(event, player)) {
			DEBUG.i("returning: #3", player);
			return;
		}

		arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null) {
			DEBUG.i("returning: #4", player);
			ArenaManager.trySignJoin(event, player);
			return;
		}

		PACheck.handleInteract(arena, player, event, event.getClickedBlock());

		arena.getDebugger().i("event post cancelled: " + event.isCancelled(),
				player);

		if (arena.isFightInProgress()
				&& !PVPArena.instance.getAgm().allowsJoinInBattle(arena)) {
			arena.getDebugger().i("exiting! fight in progress AND no INBATTLEJOIN arena!",
					player);
			return;
		}

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final ArenaTeam team = aPlayer.getArenaTeam();

		if (!aPlayer.getStatus().equals(Status.FIGHT)) {
			arena.getDebugger().i("cancelling: not fighting", player);
			// fighting player inside the lobby!
			event.setCancelled(true);
		}

		if (team == null) {
			arena.getDebugger().i("returning: no team", player);
			return;
		}

		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) ||
				event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			final Block block = event.getClickedBlock();
			arena.getDebugger().i("player team: " + team.getName(), player);
			if (block.getState() instanceof Sign) {
				arena.getDebugger().i("sign click!", player);
				final Sign sign = (Sign) block.getState();

				if (((sign.getLine(0).equalsIgnoreCase("custom")) || (arena
						.getClass(sign.getLine(0)) != null)) && (team != null)) {

					arena.chooseClass(player, sign, sign.getLine(0));
				} else {
					arena.getDebugger().i("|" + sign.getLine(0) + "|", player);
					arena.getDebugger().i(String.valueOf(arena.getClass(sign.getLine(0))),
							player);
					arena.getDebugger().i(String.valueOf(team), player);
				}
				return;
			}

			arena.getDebugger().i("block click!", player);

			final Material mMat = arena.getReadyBlock();
			arena.getDebugger().i("clicked " + block.getType().name() + ", is it " + mMat.name()
					+ "?", player);
			if (block.getTypeId() == mMat.getId()) {
				arena.getDebugger().i("clicked ready block!", player);
				if (aPlayer.getArenaClass() == null || aPlayer.getArenaClass().equals("")) {
					arena.msg(player, Language.parse(arena, MSG.ERROR_READY_NOCLASS));
					return; // not chosen class => OUT
				}
				if (arena.startRunner != null) {
					return; // counting down => OUT
				}
				if (aPlayer.getStatus() != Status.LOUNGE && aPlayer.getStatus() != Status.READY) {
					return;
				}
				
				boolean alreadyReady = aPlayer.getStatus() == Status.READY;

				arena.getDebugger().i("===============", player);
				arena.getDebugger().i("===== class: " + aPlayer.getArenaClass() + " =====", player);
				arena.getDebugger().i("===============", player);

				if (!arena.isFightInProgress()) {
					if (!aPlayer.getStatus().equals(Status.READY)) {
						arena.msg(player, Language.parse(arena, MSG.READY_DONE));
						if (!alreadyReady) {
							arena.broadcast(Language.parse(arena, MSG.PLAYER_READY, aPlayer
								.getArenaTeam().colorizePlayer(aPlayer.get())));
						}
					}
					aPlayer.setStatus(Status.READY);
					if (!alreadyReady && aPlayer.getArenaTeam().isEveryoneReady()) {
						arena.broadcast(Language.parse(arena, MSG.TEAM_READY, aPlayer
								.getArenaTeam().getColoredName()));
					}

					if (arena.getArenaConfig().getBoolean(CFG.USES_EVENTEAMS)
							&& !TeamManager.checkEven(arena)) {
							arena.msg(player,
									Language.parse(arena, MSG.NOTICE_WAITING_EQUAL));
						return; // even teams desired, not done => announce
					}

					if (!ArenaRegion.checkRegions(arena)) {
						arena.msg(player,
								Language.parse(arena, MSG.NOTICE_WAITING_FOR_ARENA));
						return;
					}

					final String error = arena.ready();

					if (error == null) {
						arena.start();
					} else if (error.equals("")) {
						arena.countDown();
					} else {
						arena.msg(player, error);
					}
					return;
				}

				final Set<PASpawn> spawns = new HashSet<PASpawn>();
				if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
					String arenaClass = aPlayer.getArenaClass().getName();
					spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, team.getName()+arenaClass+"spawn"));
				} else if (arena.isFreeForAll()) {
					if (team.getName().equals("free")) {
						spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, "spawn"));
					} else {
						spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, team.getName()));
					}
				} else {
					spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, team.getName()+"spawn"));
				}
				
				int pos = (new Random()).nextInt(spawns.size()); 
				
				for (PASpawn spawn : spawns) {

					if (--pos < 0) {
						arena.tpPlayerToCoordName(player, spawn.getName());
						break;
					}
				}
				
				ArenaPlayer.parsePlayer(player.getName()).setStatus(
						Status.FIGHT);

				ArenaModuleManager.lateJoin(arena, player);
				ArenaGoalManager.lateJoin(arena, player);
			} else if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
				arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(block.getLocation()));
				if (arena != null) {
				
					Set<ArenaRegion> bl_regions = arena.getRegionsByType(RegionType.BL_INV);
					out: if (!event.isCancelled() && bl_regions != null && !bl_regions.isEmpty()) {
						for (ArenaRegion region : bl_regions) {
							if (region.getShape().contains(new PABlockLocation(block.getLocation()))) {
								if (region.getRegionName().toLowerCase().contains(team.getName().toLowerCase())
										|| region.getRegionName().toLowerCase().contains(
												aPlayer.getArenaClass().getName().toLowerCase())) {
									event.setCancelled(true);
									break out;
								}
							}
						}
					}
					Set<ArenaRegion> wl_regions = arena.getRegionsByType(RegionType.WL_INV);
					out: if (!event.isCancelled() && wl_regions != null && !wl_regions.isEmpty()) {
						event.setCancelled(true);
						for (ArenaRegion region : wl_regions) {
							if (region.getShape().contains(new PABlockLocation(block.getLocation()))) {
								if (region.getRegionName().toLowerCase().contains(team.getName().toLowerCase())
										|| region.getRegionName().toLowerCase().contains(
												aPlayer.getArenaClass().getName().toLowerCase())) {
									event.setCancelled(false);
									break out;
								}
							}
						}
					}
				
				
					if (!event.isCancelled() && arena.getArenaConfig().getBoolean(CFG.PLAYER_QUICKLOOT)) {
						Chest c = (Chest) block.getState();
						InventoryManager.transferItems(player, c.getBlockInventory());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		if (player.isDead()) {
			return;
		}

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

		aPlayer.setArena(null);
		// instantiate and/or reset a player. This fixes issues with leaving
		// players
		// and makes sure every player is an arenaplayer ^^

		aPlayer.readDump();
		final Arena arena = aPlayer.getArena();

		if (arena != null) {
			arena.playerLeave(player, CFG.TP_EXIT, true);
		}

		if (!player.isOp()) {
			return; // no OP => OUT
		}
		DEBUG.i("OP joins the game", player);
		if (PVPArena.instance.getUpdater() != null) {
			UpdateResult test = PVPArena.instance.getUpdater().getResult();
			Updater updater = PVPArena.instance.getUpdater();
			if (test == UpdateResult.UPDATE_AVAILABLE) {
				Arena.pmsg(player, updater.getLatestName() + " is available!");
				Arena.pmsg(player, "http://dev.bukkit.org/bukkit-plugins/pvparena/files");
			} else if (test == UpdateResult.SUCCESS) {
				Arena.pmsg(player, "The plugin has been updated, please restart the server!");
				
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKicked(final PlayerKickEvent event) {
		final Player player = event.getPlayer();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null) {
			return; // no fighting player => OUT
		}
		arena.playerLeave(player, CFG.TP_EXIT, false);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		aPlayer.setArena(null);
		// instantiate and/or reset a player. This fixes issues with leaving
		// players and makes sure every player is an arenaplayer ^^

		aPlayer.readDump();
		final Arena arena = aPlayer.getArena();
		if (arena != null) {
			arena.playerLeave(player, CFG.TP_EXIT, true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
		final Player player = event.getPlayer();
		
		if (willBeCancelled(player, event)) {
			return;
		}

		final Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null
				|| !BlockListener.isProtected(player.getLocation(), event,
						RegionProtection.PICKUP)) {
			return; // no fighting player or no powerups => OUT
		}
		ArenaModuleManager.onPlayerPickupItem(arena, event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null) {
			return; // no fighting player => OUT
		}
		arena.playerLeave(player, CFG.TP_EXIT, false);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();

		if (arena == null) {
			if (event.getTo() == null) {
				
				PVPArena.instance.getLogger().warning("Player teleported to NULL: "  + event.getPlayer());
				
				return;
			}
			arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(
					event.getTo()));
			
			if (arena == null) {
				return; // no fighting player and no arena location => OUT
			}
			
			Set<ArenaRegion> regs = arena.getRegionsByType(RegionType.BATTLE);
			boolean contained = false;
			for (ArenaRegion reg : regs) {
				if (reg.getShape().contains(new PABlockLocation(event.getTo()))) {
					contained = true;
					break;
				}
			}
			if (!contained) {
				return;
			}
		}

		arena.getDebugger().i("onPlayerTeleport: fighting player '"
				+ event.getPlayer().getName() + "' (uncancel)", player);
		event.setCancelled(false); // fighting player - first recon NOT to
									// cancel!

		arena.getDebugger().i("aimed location: " + event.getTo().toString(), player);

		if (ArenaPlayer.parsePlayer(player.getName()).isTelePass()
				|| player.hasPermission("pvparena.telepass")) {
			return; // if allowed => OUT
		}
		arena.getDebugger().i("telepass: no!!", player);

		Set<ArenaRegion> regions = arena
				.getRegionsByType(RegionType.BATTLE);

		if (regions == null || regions.size() < 0) {
			return;
		}

		for (ArenaRegion r : regions) {
			if (r.getShape().contains(new PABlockLocation(event.getTo()))) {
				// teleport inside the arena, allow!
				return;
			}
		}

		arena.getDebugger().i("onPlayerTeleport: no tele pass, cancelling!", player);
		event.setCancelled(true); // cancel and tell
		arena.msg(player, Language.parse(arena, MSG.NOTICE_NO_TELEPORT));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerVelocity(final PlayerVelocityEvent event) {
		final Player player = event.getPlayer();

		final Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null) {
			return; // no fighting player or no powerups => OUT
		}
		ArenaModuleManager.onPlayerVelocity(arena, event);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerVelocity(final ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			final Player player = (Player) event.getEntity().getShooter();
			final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
			final Arena arena = aPlayer.getArena();
			if (arena == null) {
				return; // no fighting player => OUT
			}
			if (aPlayer.getStatus() == Status.FIGHT || aPlayer.getStatus() == Status.NULL) {
				return;
			}
			event.setCancelled(true);
		}
	}

}