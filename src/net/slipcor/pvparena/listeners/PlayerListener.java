package net.slipcor.pvparena.listeners;

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
import net.slipcor.pvparena.managers.Regions;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.neworder.ArenaType;
import net.slipcor.pvparena.runnables.PlayerResetRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
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
 * @version v0.7.11
 * 
 */

public class PlayerListener implements Listener {
	private Debug db = new Debug(21);

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(PlayerChatEvent event) {

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
				event.getPlayer());
		event.setCancelled(true);
	}

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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
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
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		db.i("onPlayerInteract");

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
			db.i("returning: no class");
			// fighting player inside the lobby!
			event.setCancelled(true);
			return;
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

				if (!arena.type().allowsRandomSpawns()) {
					arena.tpPlayerToCoordName(player, team.getName() + "spawn");
				} else {
					arena.tpPlayerToCoordName(player, "spawn");
				}
				arena.playerCount++;
				PVPArena.instance.getAmm().lateJoin(arena, player);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		ArenaPlayer.parsePlayer(player).setArena(null);
		// instantiate and/or reset a player. This fixes issues with leaving
		// players
		// and makes sure every player is an arenaplayer ^^

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
		arena.playerLeave(player);
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
		arena.playerLeave(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null && !ap.isDead()) {
			return; // no fighting player => OUT
		}
		db.i("onPlayerRespawn: fighting player");

		if (ap.isDead()) {
			db.i("respawning dead player");
			arena = ap.getArena();
			if (arena == null) {
				System.out.print("Dead player without proper Arena: "
						+ ap.getName());
			} else {
				Location loc = arena.getDeadLocation(player);
				if (loc != null) {
					event.setRespawnLocation(loc);
				} else {
					event.setRespawnLocation(Spawns.getCoords(arena, "exit"));
				}
			}
			if (arena == null) {
				return;
			}
			
			arena.removeDeadPlayer(player);
			Bukkit.getScheduler().scheduleAsyncDelayedTask(PVPArena.instance,
					new PlayerResetRunnable(ap), 20L);
			return;
		}

		db.i("respawning player");
		Location l;

		if (arena.cfg.getString("tp.death", "spectator").equals("old")) {
			db.i("=> old location");
			l = arena.getPlayerOldLocation(player);
		} else {
			db.i("=> 'config=>death' location");
			l = Spawns.getCoords(arena,
					arena.cfg.getString("tp.death", "spectator"));
		}
		event.setRespawnLocation(l);

		arena.removePlayer(player,
				arena.cfg.getString("tp.death", "spectator"), false);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT

		db.i("onPlayerTeleport: fighting player (uncancel)");
		event.setCancelled(false); // fighting player - first recon NOT to
									// cancel!

		PVPArena.instance.getAmm().onPlayerTeleport(arena, event);

		if (ArenaPlayer.parsePlayer(player).getTelePass()
				|| PVPArena.hasPerms(player, "pvparena.telepass"))
			return; // if allowed => OUT

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