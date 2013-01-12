package net.slipcor.pvparena.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.PlayerState;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Update;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionProtection;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.TeamManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

/**
 * <pre>Player Listener class</pre>
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class PlayerListener implements Listener {
	private static Debug db = new Debug(23);

	private boolean checkAndCommitCancel(Arena arena, Player player, Cancellable event) {
		if (!(event instanceof PlayerInteractEvent)) {
			return false;
		}
		PlayerInteractEvent e = (PlayerInteractEvent) event;
		Material mat = e.getClickedBlock().getType();
		Material check = arena == null ? Material.IRON_BLOCK : arena.getReadyBlock();
		if (mat == Material.SIGN || mat == Material.SIGN_POST || mat == Material.WALL_SIGN || mat == check) {
			db.i("signs and ready blocks allowed!", player);
			db.i("> false", player);
			return false;
		}
		
		db.i("checkAndCommitCancel", player);
		if (arena == null || player.hasPermission("pvparena.admin")) {
			db.i("no arena or admin", player);
			db.i("> false", player);
			return false;
		}
		
		if (arena != null && !arena.isFightInProgress()) {
			db.i("arena != null and fight in progress => cancel", player);
			db.i("> true", player);
			event.setCancelled(true);
			return true;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		
		if (ap.getStatus() != Status.FIGHT) {
			db.i("not fighting => cancel", player);
			db.i("> true", player);
			event.setCancelled(true);
			return true;
		}

		db.i("> false", player);
		return false;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {

		Player player = event.getPlayer();

		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

		if (arena == null) {
			return; // no fighting player => OUT
		}
		ArenaTeam team = ap.getArenaTeam();
		if (team == null) {
			return; // no fighting player => OUT
		}
		db.i("fighting player chatting!", player);
		String sTeam = team.getName();

		if (!arena.getArenaConfig().getBoolean(CFG.CHAT_ONLYPRIVATE)) {
			if (!arena.getArenaConfig().getBoolean(CFG.CHAT_ENABLED)) {
				return; // no chat editing
			}

			if (ap.isPublicChatting()) {
				return; // player not privately chatting
			}

			arena.tellTeam(sTeam, event.getMessage(), team.getColor(),
					event.getPlayer());
			event.setCancelled(true);
			return;
		}

		if (arena.getArenaConfig().getBoolean(CFG.CHAT_ENABLED)
				&& !ap.isPublicChatting()) {
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
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();

		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null || player.isOp()) {
			return; // no fighting player => OUT
		}

		List<String> list = PVPArena.instance.getConfig().getStringList(
				"whitelist");
		list.add("pa");
		list.add("pvparena");
		db.i("checking command whitelist", player);

		for (String s : list) {
			if (event.getMessage().startsWith("/" + s)) {
				db.i("command allowed: " + s, player);
				return;
			}
		}
		
		list = arena.getArenaConfig().getStringList(CFG.LISTS_CMDWHITELIST.getNode(), new ArrayList<String>());
		
		if (list == null || list.size() < 1) {
			list = new ArrayList<String>();
			list.add("ungod");
			arena.getArenaConfig().set(CFG.LISTS_CMDWHITELIST, list);
			arena.getArenaConfig().save();
		}
		
		list.add("pa");
		list.add("pvparena");
		db.i("checking command whitelist", player);

		for (String s : list) {
			if (event.getMessage().startsWith("/" + s)) {
				db.i("command allowed: " + s, player);
				return;
			}
		}
		
		db.i("command blocked: " + event.getMessage(), player);
		arena.msg(player, Language.parse(MSG.ERROR_COMMAND_BLOCKED, event.getMessage()));
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		Arena arena = ap.getArena();
		if (arena == null) {
			return; // no fighting player => OUT
                }
		if (ap.getStatus().equals(Status.READY) || ap.getStatus().equals(Status.LOUNGE)) {
			event.setCancelled(true);
			arena.msg(player, (Language.parse(MSG.NOTICE_NO_DROP_ITEM)));
			return;
		}
		if (!BlockListener.isProtected(player.getLocation(), event, RegionProtection.DROP)) {
			return; // no drop protection
		}

		db.i("onPlayerDropItem: fighting player", player);
		arena.msg(player, (Language.parse(MSG.NOTICE_NO_DROP_ITEM)));
		event.setCancelled(true);
		// cancel the drop event for fighting players, with message
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
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
	public static void finallyKillPlayer(Arena arena, Player player, Event eEvent) {
		EntityDamageEvent cause = null;

		if (eEvent instanceof EntityDeathEvent) {
			cause = player.getLastDamageCause();
		} else if (eEvent instanceof EntityDamageEvent) {
			cause = ((EntityDamageEvent) eEvent);
		}

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		ArenaTeam team = ap.getArenaTeam();
		
		String playerName = (team != null) ? team.colorizePlayer(player) : player.getName();
		if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
			arena.broadcast(Language.parse(
					MSG.FIGHT_KILLED_BY,
					playerName + ChatColor.YELLOW,
					arena.parseDeathCause(player, cause.getCause(),
							ArenaPlayer.getLastDamagingPlayer(cause))));
		}
		if (arena.isCustomClassAlive()
				|| arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
			InventoryManager.drop(player);
		}
		
		if (ArenaPlayer.parsePlayer(player.getName()).getArenaClass() == null || !ArenaPlayer.parsePlayer(player.getName()).getArenaClass().getName().equalsIgnoreCase("custom")) {
			InventoryManager.clearInventory(player);
		}
		
		arena.removePlayer(player, arena.getArenaConfig().getString(CFG.TP_DEATH), true, false);
		
		ap.setStatus(Status.LOST);
		ap.addDeath();
		
		PlayerState.fullReset(arena, player);
		
		if (ArenaManager.checkAndCommit(arena, false)) {
			return;
                }
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		db.i("onPlayerInteract", player);
		
		if (event.getAction().equals(Action.PHYSICAL)) {
			db.i("returning: physical", player);
			return;
		}
		
		db.i("event pre cancelled: " + String.valueOf(event.isCancelled()), player);
		
		Arena arena = null;
		
		if (event.hasBlock()) {
			arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getClickedBlock().getLocation()));
			if (checkAndCommitCancel(arena, event.getPlayer(), event)) {
				return;
			}
		}
		
		
		if (arena != null && ArenaModuleManager.onPlayerInteract(arena, event)) {
			db.i("returning: #1", player);
			return;
		}

		if (PACheck.handleSetFlag(player, event.getClickedBlock())) {
			db.i("returning: #2", player);
			event.setCancelled(true);
			return;
		}

		if (ArenaRegionShape.checkRegionSetPosition(event, player)) {
			db.i("returning: #3", player);
			return;
		}

		arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null) {
			db.i("returning: #4", player);
			ArenaManager.trySignJoin(event, player);
			return;
		}

		PACheck.handleInteract(arena, player, event, event.getClickedBlock());

		db.i("event post cancelled: " + String.valueOf(event.isCancelled()), player);
		
		if (arena.isFightInProgress() && !PVPArena.instance.getAgm().allowsJoinInBattle(arena)) {
			db.i("exiting! fight in progress AND no INBATTLEJOIN arena!", player);
			return;
		}

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		ArenaTeam team = ap.getArenaTeam();

		if (!ap.getStatus().equals(Status.FIGHT)) {
			db.i("cancelling: no class", player);
			// fighting player inside the lobby!
			event.setCancelled(true);
		}

		if (team == null) {
			db.i("returning: no team", player);
			return;
		}

		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			db.i("player team: " + team.getName(), player);
			if (block.getState() instanceof Sign) {
				db.i("sign click!", player);
				Sign sign = (Sign) block.getState();

				if (((sign.getLine(0).equalsIgnoreCase("custom"))
						|| (arena.getClass(sign.getLine(0)) != null)) && (team != null)) {

					arena.chooseClass(player, sign, sign.getLine(0));
				} else {
					db.i("|"+sign.getLine(0)+"|", player);
					db.i(String.valueOf(arena.getClass(sign.getLine(0))), player);
					db.i(String.valueOf(team), player);
				}
				return;
			}

			db.i("block click!", player);

			Material mMat = arena.getReadyBlock();
			db.i("clicked " + block.getType().name() + ", is it " + mMat.name()
					+ "?", player);
			if (block.getTypeId() == mMat.getId()) {
				db.i("clicked ready block!", player);
				if (ap.getClass().equals("")) {
					return; // not chosen class => OUT
				}
				if (arena.START_ID != null) {
					return; // counting down => OUT
				}
				if (ap.getStatus() != Status.LOUNGE) {
					return;
				}

				db.i("===============", player);
				db.i("===== class: " + ap.getClass() + " =====", player);
				db.i("===============", player);

				if (!arena.isFightInProgress()) {
					if (!ap.getStatus().equals(Status.READY)) {
						arena.msg(player, Language.parse(MSG.READY_DONE));
						arena.broadcast(Language.parse(MSG.PLAYER_READY, ap.getArenaTeam().colorizePlayer(ap.get())));
					}
					ap.setStatus(Status.READY);
					if (ap.getArenaTeam().isEveryoneReady()) {
						arena.broadcast(Language.parse(MSG.TEAM_READY, ap.getArenaTeam().getColoredName()));
					}

					if (arena.getArenaConfig().getBoolean(CFG.USES_EVENTEAMS)) {
						if (!TeamManager.checkEven(arena)) {
							arena.msg(player, Language.parse(MSG.NOTICE_WAITING_EQUAL));
							return; // even teams desired, not done => announce
						}
					}

					if (!ArenaRegionShape.checkRegions(arena)) {
						arena.msg(player, Language.parse(MSG.NOTICE_WAITING_FOR_ARENA));
						return;
					}


					String error = arena.ready();

					if (error == null) {
						arena.start();
					} else if (error.equals("")) {
						arena.countDown();
					} else {
						arena.msg(player, error);
					}
					return;
				}

				if (arena.isFreeForAll()) {
					arena.tpPlayerToCoordName(player, "spawn");
				} else {
					arena.tpPlayerToCoordName(player, team.getName() + "spawn");
				}
				ArenaPlayer.parsePlayer(player.getName()).setStatus(Status.FIGHT);
				
				ArenaModuleManager.lateJoin(arena, player);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		if (player.isDead()) {
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		
		ap.setArena(null);
		// instantiate and/or reset a player. This fixes issues with leaving
		// players
		// and makes sure every player is an arenaplayer ^^

		ap.readDump();
		Arena a = ap.getArena();
		
		if (a != null) {
			a.playerLeave(player, CFG.TP_EXIT, true);
		}
		
		if (!player.isOp()) {
			return; // no OP => OUT
		}
		db.i("OP joins the game", player);
		Update.message(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKicked(PlayerKickEvent event) {
		Player player = event.getPlayer();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null) {
			return; // no fighting player => OUT
                }
		arena.playerLeave(player, CFG.TP_EXIT, false);
	}
	

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		ap.setArena(null);
		// instantiate and/or reset a player. This fixes issues with leaving
		// players and makes sure every player is an arenaplayer ^^

		ap.readDump();
		Arena a = ap.getArena();
		if (a != null) {
			a.playerLeave(player, CFG.TP_EXIT, true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();

		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null || !BlockListener.isProtected(player.getLocation(), event, RegionProtection.PICKUP)) {
			return; // no fighting player or no powerups => OUT
                }
		ArenaModuleManager.onPlayerPickupItem(arena, event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null) {
			return; // no fighting player => OUT
                }
		arena.playerLeave(player, CFG.TP_EXIT, false);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		
		if (arena == null) {
			arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getTo()));
			if (arena == null) {
				return; // no fighting player and no arena location => OUT
			}
		}

		db.i("onPlayerTeleport: fighting player '"+event.getPlayer().getName()+"' (uncancel)", player);
		event.setCancelled(false); // fighting player - first recon NOT to
									// cancel!

		db.i("aimed location: " + event.getTo().toString(), player);
		
		if (ArenaPlayer.parsePlayer(player.getName()).getTelePass() 
				|| player.hasPermission("pvparena.telepass")) {
			return; // if allowed => OUT
                }
		db.i("telepass: no!!", player);
		
		HashSet<ArenaRegionShape> regions = arena.getRegionsByType(RegionType.BATTLE);
		
		if (regions == null || regions.size() < 0) {
			return;
		}
		
		boolean from = false;
		boolean to = false;
		
		for (ArenaRegionShape r : regions) {
			from = from?true:r.contains(new PABlockLocation(event.getFrom()));
			to = to?true:r.contains(new PABlockLocation(event.getTo()));
		}
		
		if (from && to) {
			// teleport inside the arena, allow!
			return;
		}

		db.i("onPlayerTeleport: no tele pass, cancelling!", player);
		event.setCancelled(true); // cancel and tell
		arena.msg(player, Language.parse(MSG.NOTICE_NO_TELEPORT));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		Player player = event.getPlayer();

		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null) {
			return; // no fighting player or no powerups => OUT
                }
		ArenaModuleManager.onPlayerVelocity(arena, event);
	}

}