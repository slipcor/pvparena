package net.slipcor.pvparena.listeners;

import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Update;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Regions;
import net.slipcor.pvparena.managers.Statistics;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.neworder.ArenaType;
import net.slipcor.pvparena.runnables.InventoryRestoreRunnable;
import net.slipcor.pvparena.runnables.PlayerResetRunnable;

import org.bukkit.Bukkit;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

/**
 * player listener class
 * 
 * -
 * 
 * PVP Arena Player Listener
 * 
 * @author slipcor
 * 
 * @version v0.8.12
 * 
 */

public class PlayerListener implements Listener {
	private static Debug db = new Debug(21);
/*
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {

		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);

		if (arena == null) {
			return; // no fighting player => OUT
		}
		ArenaTeam team = Teams.getTeam(arena, ap);
		if (team == null) {
			return; // no fighting player => OUT
		}
		db.i("fighting player chatting!");
		String sTeam = team.getName();

		if (!arena.cfg.getBoolean("messages.onlyChat")) {
			if (!arena.cfg.getBoolean("messages.chat")) {
				return; // no chat editing
			}

			if (!arena.chatters.contains(player.getName())) {
				return; // player not chatting
			}

			arena.tellTeam(sTeam, event.getMessage(), team.getColor(),
					event.getPlayer());
			event.setCancelled(true);
			return;
		}

		if (arena.cfg.getBoolean("messages.chat")
				&& arena.chatters.contains(player.getName())) {
			arena.tellTeam(sTeam, event.getMessage(), team.getColor(),
					event.getPlayer());
			event.setCancelled(true);
			return;
		}

		arena.tellEveryoneColored(event.getMessage(), team.getColor(),
				event.getPlayer()); //
		event.setCancelled(true);
	}
*/
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
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
		
		list = arena.cfg.getYamlConfiguration().getStringList(
				"whitelist");
		
		if (list == null || list.size() < 1) {
			list = new ArrayList<String>();
			list.add("ungod");
			arena.cfg.getYamlConfiguration().set("whitelist", list);
			arena.cfg.save();
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
		Arenas.tellPlayer(player, ChatColor.RED + event.getMessage(), arena);
		event.setCancelled(true);
	}
	
/*
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKicked(PlayerDisconnectEvent event) {
		Player player = event.getPlayer();
		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT
		arena.playerLeave(player, "exit");
	}*/

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		event.getItemDrop();
		Player player = event.getPlayer();
		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT

		db.i("onPlayerDropItem: fighting player");
		Arenas.tellPlayer(player, (Language.parse("dropitem")), arena);
		event.setCancelled(true);
		// cancel the drop event for fighting players, with message
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return;
	
		int lives = 0;
		try {
			lives = arena.type().getLives(player);
		} catch(Exception e) {
			
		}
		
		Statistics.kill(arena, (player.getLastDamageCause() == null) ? player : player.getLastDamageCause().getEntity(), player, (lives >= 1));
		
		db.i("lives before death: " + lives);
		if (lives < 1) {
			if (!arena.cfg.getBoolean("game.preventDeath")) {
				return; // stop
				//player died => commit death!
			}
			db.i("faking player death");

			commitPlayerDeath(arena, player, event);
		} else {
			lives--;
			InventoryRestoreRunnable irr = new InventoryRestoreRunnable(arena, player, event.getDrops(),0);
			irr.setId(Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, irr, 1L));
			arena.respawnPlayer(player, lives, (player.getLastDamageCause() == null) ? DamageCause.SUICIDE : event.getEntity().getLastDamageCause().getCause(), player.getKiller());
		}
		
		event.getDrops().clear();
		event.setDeathMessage(null);
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
	public static void commitPlayerDeath(Arena arena, Player player, Event eEvent) {
		EntityDamageEvent cause = null;

		if (eEvent instanceof EntityDeathEvent) {
			cause = player.getLastDamageCause();
		} else if (eEvent instanceof EntityDamageEvent) {
			cause = ((EntityDamageEvent) eEvent);
		}
		//EntityListener.addBurningPlayer(player);
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		ArenaTeam team = Teams.getTeam(arena, ap);
		String playerName;
		if (team != null) {
			playerName = team.colorizePlayer(player);
		} else {
			playerName = player.getName();
		}
		PVPArena.instance.getAmm().commitPlayerDeath(arena, player, cause);
		arena.tellEveryone(Language.parse(
				"killedby",
				playerName + ChatColor.YELLOW,
				arena.parseDeathCause(player, cause.getCause(),
						ArenaPlayer.getLastDamagingPlayer(cause))));
		if (arena.isCustomClassActive()
				|| arena.cfg.getBoolean("game.allowDrops")) {
			Inventories.drop(player);
		}
		if (ArenaPlayer.parsePlayer(player).getaClass() == null || !ArenaPlayer.parsePlayer(player).getaClass().getName().equalsIgnoreCase("custom")) {
			Inventories.clearInventory(player);
		}

		arena.tpPlayerToCoordName(player, "spectator");
		
		ap.setStatus(Status.LOSES);
		
		arena.prepare(player, true, true);
		
		arena.type().checkEntityDeath(player);
		PlayerResetRunnable prr = new PlayerResetRunnable(ap,0, player.getLocation());
		prr.setId(Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, prr, 20L));

		if (arena.cfg.getInt("goal.timed") > 0) {
			db.i("timed arena!");
			Player damager = null;

			if (eEvent instanceof EntityDeathEvent) {
				EntityDeathEvent event = (EntityDeathEvent) eEvent;
				if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
					try {
						EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) event
								.getEntity().getLastDamageCause();
						damager = (Player) ee.getDamager();
						db.i("damager found in arg 2");
					} catch (Exception ex) {

					}
				}
			} else if (eEvent instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) eEvent;
				try {
					damager = (Player) event.getDamager();
					db.i("damager found in arg 3");
				} catch (Exception ex) {

				}
			}
			String sKiller = "";

			db.i("timed ctf/pumpkin arena");
			if (damager != null) {
				sKiller = damager.getName();
				db.i("killer: " + sKiller);
				
				ArenaPlayer apd = ArenaPlayer.parsePlayer(damager);
				if (apd.getKills() > 0) {
					db.i("killer killed already");
					apd.addKill();
				} else {
					db.i("first kill");
					apd.addKill();
				}

				ArenaPlayer apk = ArenaPlayer.parsePlayer(damager);
				if (apk.getDeaths() > 0) {
					db.i("already died");
					apk.addDeath();
				} else {
					db.i("first death");
					apk.addDeath();
					arena.betPossible = false;
				}
			}
		}

		if (Arenas.checkAndCommit(arena))
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
		
		if (PVPArena.instance.getAmm().onPlayerInteract(event)) {
			db.i("returning: #1");
			return;
		}

		if (Regions.checkRegionSetPosition(event, player)) {
			db.i("returning: #2");
			return;
		}

		if (ArenaType.checkSetFlag(event.getClickedBlock(), player)) {
			db.i("returning: #3");
			return;
		}

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null) {
			db.i("returning: #4");
			Arenas.trySignJoin(event, player);
			return;
		}

		arena.type().checkInteract(player, event.getClickedBlock());

		if (arena.fightInProgress && !arena.type().allowsJoinInBattle()) {
			db.i("exiting! fight in progress AND no INBATTLEJOIN arena!");
			return;
		}

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		ArenaTeam team = Teams.getTeam(arena, ap);

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

				if ((sign.getLine(0).equalsIgnoreCase("custom"))
						|| arena.classExists(sign.getLine(0)) && (team != null)) {

					arena.forceChooseClass(player, sign, sign.getLine(0));
				}
				return;
			}

			db.i("block click!");

			Material mMat = Material.IRON_BLOCK;
			if (arena.cfg.get("ready.block") != null) {
				db.i("reading ready block");
				try {
					mMat = Material
							.getMaterial(arena.cfg.getInt("ready.block"));
					if (mMat == Material.AIR)
						mMat = Material.getMaterial(arena.cfg
								.getString("ready.block"));
					db.i("mMat now is " + mMat.name());
				} catch (Exception e) {
					db.i("exception reading ready block");
					String sMat = arena.cfg.getString("ready.block");
					try {
						mMat = Material.getMaterial(sMat);
						db.i("mMat now is " + mMat.name());
					} catch (Exception e2) {
						Language.log_warning("matnotfound", sMat);
					}
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

				if (!arena.fightInProgress && arena.START_ID == -1) {

					if (arena.cfg.getBoolean("join.forceEven", false)) {
						if (!Teams.checkEven(arena)) {
							Arenas.tellPlayer(player,
									Language.parse("waitequal"), arena);
							return; // even teams desired, not done => announce
						}
					}

					if (!Regions.checkRegions(arena)) {
						Arenas.tellPlayer(player,
								Language.parse("checkregionerror"), arena);
						return;
					}

					ArenaPlayer.parsePlayer(player).setStatus(Status.READY);

					int ready = arena.ready();

					db.i("===============");
					db.i("===== ready: " + ready + " =====");
					db.i("===============");

					if (ready == 0) {
						Arenas.tellPlayer(player, Language.parse("notready"),
								arena);
						return; // team not ready => announce
					} else if (ready == -1) {
						Arenas.tellPlayer(player, Language.parse("notready1"),
								arena);
						return; // team not ready => announce
					} else if (ready == -2) {
						Arenas.tellPlayer(player, Language.parse("notready2"),
								arena);
						return; // team not ready => announce
					} else if (ready == -3) {
						Arenas.tellPlayer(player, Language.parse("notready3"),
								arena);
						return; // team not ready => announce
					} else if (ready == -4) {
						Arenas.tellPlayer(player, Language.parse("notready4"),
								arena);
						return; // arena not ready => announce
					} else if (ready == -5) {
						Arenas.tellPlayer(player, Language.parse("notready5"),
								arena);
						return; // arena not ready => announce
					} else if (ready == -6) {
						arena.countDown();
						return; // arena ready => countdown
					}
					arena.start();
					return;
				}

				if (arena.type().isFreeForAll()) {
					arena.tpPlayerToCoordName(player, "spawn");
				} else {
					arena.tpPlayerToCoordName(player, team.getName() + "spawn");
				}
				ArenaPlayer.parsePlayer(player).setStatus(Status.FIGHT);
				arena.playerCount++;
				PVPArena.instance.getAmm().lateJoin(arena, player);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		
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
		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT
		arena.playerLeave(player, "exit");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null) {
			return; // no fighting player => OUT
		}

		arena.type().parseMove(player);
		PVPArena.instance.getAmm().parseMove(arena, event);
	}
	

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		
		ap.setArena(null);
		// instantiate and/or reset a player. This fixes issues with leaving
		// players
		// and makes sure every player is an arenaplayer ^^

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

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player or no powerups => OUT

		PVPArena.instance.getAmm().onPlayerPickupItem(arena, event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT

		arena.playerLeave(player, "exit");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Arena arena = Arenas.getArenaByPlayer(player);
		
		if (arena == null) {
			arena = Arenas.getArenaByRegionLocation(event.getTo());
			if (arena == null) {
				return; // no fighting player and no arena location => OUT
			}
		}

		db.i("onPlayerTeleport: fighting player '"+event.getPlayer().getName()+"' (uncancel)");
		event.setCancelled(false); // fighting player - first recon NOT to
									// cancel!

		PVPArena.instance.getAmm().onPlayerTeleport(arena, event);

		db.i("aimed location: " + event.getTo().toString());
		
		if (ArenaPlayer.parsePlayer(player).getTelePass()
				|| PVPArena.hasPerms(player, "pvparena.telepass"))
			return; // if allowed => OUT
		
		db.i("telepass: no!!");
		
		if (arena.regions.containsKey("battlefield")) {
			if (arena.regions.get("battlefield").contains(event.getFrom())
					&& arena.regions.get("battlefield").contains(event.getTo())) {
				return; // teleporting inside the arena: allowed!
			}
		}

		db.i("onPlayerTeleport: no tele pass, cancelling!");
		event.setCancelled(true); // cancel and tell
		Arenas.tellPlayer(player, Language.parse("usepatoexit"), arena);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player or no powerups => OUT

		PVPArena.instance.getAmm().onPlayerVelocity(arena, event);
	}

}