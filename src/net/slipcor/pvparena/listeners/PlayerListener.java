package net.slipcor.pvparena.listeners;

import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.PlayerState;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.Update;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionProtection;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
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
 * @version v0.9.2
 */

public class PlayerListener implements Listener {
	private static Debug db = new Debug(23);

	@EventHandler(priority = EventPriority.LOWEST)
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
		db.i("fighting player chatting!");
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

	@EventHandler(priority = EventPriority.LOWEST)
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
		db.i("checking command whitelist");

		for (String s : list) {
			if (event.getMessage().startsWith("/" + s)) {
				db.i("command allowed: " + s);
				return;
			}
		}
		
		list = arena.getArenaConfig().getStringList(CFG.LISTS_CMDWHITELIST.getNode(), new ArrayList<String>());
		
		if (list == null || list.size() < 1) {
			list = new ArrayList<String>();
			list.add("ungod");
			arena.getArenaConfig().set(CFG.LISTS_WHITELIST, list);
			arena.getArenaConfig().save();
		}
		
		list.add("pa");
		list.add("pvparena");
		db.i("checking command whitelist");

		for (String s : list) {
			if (event.getMessage().startsWith("/" + s)) {
				db.i("command allowed: " + s);
				return;
			}
		}
		
		db.i("command blocked: " + event.getMessage());
		arena.msg(player, Language.parse(MSG.ERROR_COMMAND_BLOCKED, event.getMessage()));
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null)
			return; // no fighting player => OUT
		if (!BlockListener.isProtected(player.getLocation(), event, RegionProtection.DROP)) {
			return; // no drop protection
		}

		db.i("onPlayerDropItem: fighting player");
		arena.msg(player, (Language.parse(MSG.NOTICE_NO_DROP_ITEM)));
		event.setCancelled(true);
		// cancel the drop event for fighting players, with message
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null)
			return;
	
		PVPArena.instance.getAgm().onPlayerDeath(arena, player, event);
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
		
		PVPArena.instance.getAmm().commitPlayerDeath(arena, player, cause);
		arena.broadcast(Language.parse(
				MSG.FIGHT_KILLED_BY,
				playerName + ChatColor.YELLOW,
				arena.parseDeathCause(player, cause.getCause(),
						ArenaPlayer.getLastDamagingPlayer(cause))));
		
		if (arena.isCustomClassAlive()
				|| arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
			InventoryManager.drop(player);
		}
		
		if (ArenaPlayer.parsePlayer(player.getName()).getArenaClass() == null || !ArenaPlayer.parsePlayer(player.getName()).getArenaClass().getName().equalsIgnoreCase("custom")) {
			InventoryManager.clearInventory(player);
		}
		
		arena.removePlayer(player, CFG.TP_DEATH.toString(), true);
		
		ap.setStatus(Status.LOST);
		ap.addDeath();
		
		PlayerState.fullReset(arena, player);
		
		if (ArenaManager.checkAndCommit(arena))
			return;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		db.i("onPlayerInteract");
		
		
		
		if (event.getAction().equals(Action.PHYSICAL)) {
			db.i("returning: physical");
			return;
		}
		
		Arena arena = null;
		
		if (event.hasBlock()) {
			arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getClickedBlock().getLocation()));
		}
		
		if (arena != null && PVPArena.instance.getAmm().onPlayerInteract(arena, event)) {
			db.i("returning: #1");
			return;
		}

		if (PVPArena.instance.getAgm().checkSetFlag(player, event.getClickedBlock())) {
			db.i("returning: #2");
			return;
		}

		if (ArenaRegionShape.checkRegionSetPosition(event, player)) {
			db.i("returning: #3");
			return;
		}

		arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null) {
			db.i("returning: #4");
			ArenaManager.trySignJoin(event, player);
			return;
		}

		PVPArena.instance.getAgm().checkInteract(arena, player, event.getClickedBlock());

		if (arena.isFightInProgress() && !PVPArena.instance.getAgm().allowsJoinInBattle(arena)) {
			db.i("exiting! fight in progress AND no INBATTLEJOIN arena!");
			return;
		}

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		ArenaTeam team = ap.getArenaTeam();

		if (!ap.getStatus().equals(Status.FIGHT)) {
			db.i("cancelling: no class");
			// fighting player inside the lobby!
			event.setCancelled(true);
		}

		if (team == null) {
			db.i("returning: no team");
			return;
		}

		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			db.i("player team: " + team.getName());
			if (block.getState() instanceof Sign) {
				db.i("sign click!");
				Sign sign = (Sign) block.getState();

				if (((sign.getLine(0).equalsIgnoreCase("custom"))
						|| (arena.getClass(sign.getLine(0)) != null)) && (team != null)) {

					arena.chooseClass(player, sign, sign.getLine(0));
				}
				return;
			}

			db.i("block click!");

			Material mMat = Material.IRON_BLOCK;
			db.i("reading ready block");
			try {
				mMat = Material
						.getMaterial(arena.getArenaConfig().getInt(CFG.READY_BLOCK));
				if (mMat == Material.AIR)
					mMat = Material.getMaterial(arena.getArenaConfig()
							.getString(CFG.READY_BLOCK));
				db.i("mMat now is " + mMat.name());
			} catch (Exception e) {
				db.i("exception reading ready block");
				String sMat = arena.getArenaConfig().getString(CFG.READY_BLOCK);
				try {
					mMat = Material.getMaterial(sMat);
					db.i("mMat now is " + mMat.name());
				} catch (Exception e2) {
					Language.log_warning(MSG.ERROR_MAT_NOT_FOUND, sMat);
				}
			}
			db.i("clicked " + block.getType().name() + ", is it " + mMat.name()
					+ "?");
			if (block.getTypeId() == mMat.getId()) {
				db.i("clicked ready block!");
				if (ap.getClass().equals("")) {
					return; // not chosen class => OUT
				}
				if (arena.START_ID != -1) {
					return; // counting down => OUT
				}

				db.i("===============");
				db.i("===== class: " + ap.getClass() + " =====");
				db.i("===============");

				if (!arena.isFightInProgress() && arena.START_ID == -1) {

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

					ArenaPlayer.parsePlayer(player.getName()).setStatus(Status.READY);

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
				PVPArena.instance.getAmm().lateJoin(arena, player);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
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
			a.playerLeave(player, "exit");
		}
		
		if (!player.isOp()) {
			return; // no OP => OUT
		}
		db.i("OP joins the game");
		Update.message(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKicked(PlayerKickEvent event) {
		Player player = event.getPlayer();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null)
			return; // no fighting player => OUT
		arena.playerLeave(player, "exit");
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
			a.playerLeave(player, "exit");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();

		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null || BlockListener.isProtected(player.getLocation(), event, RegionProtection.PICKUP))
			return; // no fighting player or no powerups => OUT

		PVPArena.instance.getAmm().onPlayerPickupItem(arena, event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null)
			return; // no fighting player => OUT
		
		arena.playerLeave(player, "exit");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		
		if (arena == null) {
			arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getTo()));
			if (arena == null) {
				return; // no fighting player and no arena location => OUT
			}
		}

		db.i("onPlayerTeleport: fighting player '"+event.getPlayer().getName()+"' (uncancel)");
		event.setCancelled(false); // fighting player - first recon NOT to
									// cancel!

		PVPArena.instance.getAmm().onPlayerTeleport(arena, event);

		db.i("aimed location: " + event.getTo().toString());
		
		if (ArenaPlayer.parsePlayer(player.getName()).getTelePass()
				|| player.hasPermission("pvparena.telepass"))
			return; // if allowed => OUT
		
		db.i("telepass: no!!");
		
		if (arena.getRegion("battlefield") != null) {
			if (arena.getRegion("battlefield").contains(new PABlockLocation(event.getFrom()))
					&& arena.getRegion("battlefield").contains(new PABlockLocation(event.getTo()))) {
				return; // teleporting inside the arena: allowed!
			}
		}

		db.i("onPlayerTeleport: no tele pass, cancelling!");
		event.setCancelled(true); // cancel and tell
		arena.msg(player, Language.parse(MSG.NOTICE_NO_TELEPORT));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();

		Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();
		if (arena == null)
			return; // no fighting player or no powerups => OUT

		PVPArena.instance.getAmm().onPlayerVelocity(arena, event);
	}

}