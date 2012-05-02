package net.slipcor.pvparena.listeners;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.managers.Statistics;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;

/**
 * entity listener class
 * 
 * -
 * 
 * PVP Arena Entity Listener
 * 
 * @author slipcor
 * 
 * @version v0.7.19
 * 
 */

public class EntityListener implements Listener {
	private Debug db = new Debug(20);

	static HashSet<Player> burningPlayers = new HashSet<Player>();

	public static void addBurningPlayer(Player player) {
		burningPlayers.add(player);
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
	private void commitPlayerDeath(Arena arena, Player player, Event eEvent) {
		EntityDamageEvent cause = null;

		if (eEvent instanceof EntityDeathEvent) {
			cause = player.getLastDamageCause();
		} else if (eEvent instanceof EntityDamageEvent) {
			cause = ((EntityDamageEvent) eEvent);
		}
		EntityListener.addBurningPlayer(player);
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		ArenaTeam team = Teams.getTeam(arena, ap);
		PVPArena.instance.getAmm().commitPlayerDeath(arena, player, cause);
		arena.tellEveryone(Language.parse(
				"killedby",
				team.colorizePlayer(player) + ChatColor.YELLOW,
				arena.parseDeathCause(player, cause.getCause(),
						ArenaPlayer.getLastDamagingPlayer(cause))));

		if (arena.isCustomClassActive()
				|| arena.cfg.getBoolean("game.allowDrops")) {
			Inventories.drop(player);
		}
		Inventories.clearInventory(player);

		arena.tpPlayerToCoordName(player, "spectator");
		
		ap.setStatus(Status.LOSES);
		
		arena.prepare(player, true, true);
		
		arena.type().checkEntityDeath(player);

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
			}

			if (damager != null) {
				ArenaPlayer apd = ArenaPlayer.parsePlayer(damager);
				if (apd.getKills() > 0) {
					db.i("killer killed already");
					apd.addKill();
				} else {
					db.i("first kill");
					apd.addKill();
				}
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

		if (Arenas.checkAndCommit(arena))
			return;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event instanceof EntityDamageByEntityEvent) {
			return;
		}

		if (event.isCancelled()) {
			return; // respect other plugins
		}

		Entity p1 = event.getEntity();

		if ((p1 == null) || (!(p1 instanceof Player)))
			return; // no player

		Arena arena = Arenas.getArenaByPlayer((Player) p1);
		if (arena == null)
			return;

		db.i("onEntityDamage: fighting player");
		if (!arena.fightInProgress) {
			return;
		}

		Player player = (Player) p1;

		if (burningPlayers.contains(player)
				&& (event.getCause().equals(DamageCause.FIRE_TICK))) {
			player.setFireTicks(0);
			event.setCancelled(true);
			burningPlayers.remove(player);
			return;
		}

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		ArenaTeam team = Teams.getTeam(arena, ap);
		/*
		db.i("team: " + team);
		db.i("status: " + ap.getStatus().name());
		db.i("REALEND: " + arena.REALEND_ID);
		*/
		if (team == null || ap.getStatus().equals(Status.LOSES) || arena.REALEND_ID != -1) {
			event.setCancelled(true);
			return;
		}

		Statistics.damage(arena, null, player, event.getDamage());
		
		int reduction = calcArmorDamageReduction(event, player);
		
		// here it comes, process the damage!
		if (event.getDamage() - reduction >= player.getHealth()) {
			db.i("damage >= health => death");
			int lives = 3;

			Statistics.kill(arena, null, player, (lives > 0));
			lives = arena.type().reduceLives(player, lives);
			if (lives < 1) {
				if (!arena.cfg.getBoolean("game.preventDeath")) {
					return; // player died => commit death!
				}
				db.i("faking player death");

				commitPlayerDeath(arena, player, event);
			} else {
				lives--;
				arena.respawnPlayer(player, lives, event.getCause(), null);
			}
			event.setCancelled(true);
		}

	}

	private int calcArmorDamageReduction(EntityDamageEvent event, Player player) {
		/**
		 * full damage reduction:
		 * 
		 * CHAIN
		 * 7=>3
		 * 11=>10
		 * => 9%
		 * 
		 * 
		 * GOLD
		 * 11->6
		 *  9->5
		 *  7->4
		 *  7->3
		 * => 42%
		 *  
		 * 
		 * LEATHER
		 * 7->6
		 * => 14%
		 * 
		 * IRON
		 * 11->4
		 * 10->4
		 *  8->3
		 * 	7->3
		 * => 50%
		 *  
		 * DIAMOND
		 * 10->2
		 *  9->2
		 *  8->2
		 *  7->2
		 *  => 75%
		 * 
		 * -------------------------
		 * full reduction: 5/7 ~ 70%
		 * -------------------------
		 * 
		 * 
		 * HELMET
		 * 
		 * 7->6
		 * 11->10
		 * =>10% ==> [ 1/7 % ]
		 * 
		 * CHEST
		 * 
		 * 7->5
		 * 8->5
		 * 11->8
		 * =>25% ==> [ 25/70 % ]
		 * 
		 * LEGS
		 * 
		 * 7->5
		 * 11->8
		 * 10->8
		 * =>20% => [ 2/7 % ]
		 * 
		 * BOOTS
		 * 
		 * 7->6
		 * 8->7
		 * 9->8
		 * 11->9
		 * =>10% ==> [ 1/7 % ]
		 * 
		 */
		
		int chainfull = 9;
		int goldfull = 42;
		int leatherfull = 14;
		int ironfull = 50;
		int diamondfull = 75;

		float helmfactor = 1/7;
		float chestfactor = 25/70;
		float legfactor = 2/7;
		float bootfactor = 1/7;
		
		float reduction = 0.0f;

		if (player.getInventory().getHelmet() != null) {
			ItemStack item = player.getInventory().getHelmet();
			float adding = helmfactor;
			if (item.getType().toString().startsWith("CHAIN")) {
				reduction += adding * chainfull;
			} else if (item.getType().toString().startsWith("GOLD")) {
				reduction += adding * goldfull;
			} else if (item.getType().toString().startsWith("LEATHER")) {
				reduction += adding * leatherfull;
			} else if (item.getType().toString().startsWith("IRON")) {
				reduction += adding * ironfull;
			} else if (item.getType().toString().startsWith("DIAMOND")) {
				reduction += adding * diamondfull;
			}
		}
		if (player.getInventory().getChestplate() != null) {
			ItemStack item = player.getInventory().getChestplate();
			float adding = chestfactor;
			if (item.getType().toString().startsWith("CHAIN")) {
				reduction += adding * chainfull;
			} else if (item.getType().toString().startsWith("GOLD")) {
				reduction += adding * goldfull;
			} else if (item.getType().toString().startsWith("LEATHER")) {
				reduction += adding * leatherfull;
			} else if (item.getType().toString().startsWith("IRON")) {
				reduction += adding * ironfull;
			} else if (item.getType().toString().startsWith("DIAMOND")) {
				reduction += adding * diamondfull;
			}
		}
		if (player.getInventory().getLeggings() != null) {
			ItemStack item = player.getInventory().getLeggings();
			float adding = legfactor;
			if (item.getType().toString().startsWith("CHAIN")) {
				reduction += adding * chainfull;
			} else if (item.getType().toString().startsWith("GOLD")) {
				reduction += adding * goldfull;
			} else if (item.getType().toString().startsWith("LEATHER")) {
				reduction += adding * leatherfull;
			} else if (item.getType().toString().startsWith("IRON")) {
				reduction += adding * ironfull;
			} else if (item.getType().toString().startsWith("DIAMOND")) {
				reduction += adding * diamondfull;
			}
		}
		if (player.getInventory().getBoots() != null) {
			ItemStack item = player.getInventory().getBoots();
			float adding = bootfactor;
			if (item.getType().toString().startsWith("CHAIN")) {
				reduction += adding * chainfull;
			} else if (item.getType().toString().startsWith("GOLD")) {
				reduction += adding * goldfull;
			} else if (item.getType().toString().startsWith("LEATHER")) {
				reduction += adding * leatherfull;
			} else if (item.getType().toString().startsWith("IRON")) {
				reduction += adding * ironfull;
			} else if (item.getType().toString().startsWith("DIAMOND")) {
				reduction += adding * diamondfull;
			}
		}
		
		return (int) reduction;
	}


	/**
	 * parsing of damage: Entity vs Entity
	 * 
	 * @param event
	 *            the triggering event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

		if (event.isCancelled()) {
			return;
		}

		Entity p1 = event.getDamager();
		Entity p2 = event.getEntity();

		db.i("onEntityDamageByEntity: cause: " + event.getCause().name()
				+ " : " + event.getDamager().toString() + " => "
				+ event.getEntity().toString());

		if (event.getCause() == DamageCause.BLOCK_EXPLOSION) {

			db.i("onEntityDamageByEntity: fighting player");
			if ((p2 == null) || (!(p2 instanceof Player))) {
				return;
			}
			db.i("damaged entity is player");
			Player defender = (Player) p2;
			Arena arena = Arenas.getArenaByPlayer(defender);
			if (arena == null)
				return;

			db.i("onEntityDamageByBLOCKDAMAGE: arena player");

			boolean inTeam = false;
			ArenaPlayer ap = ArenaPlayer.parsePlayer(defender);

			for (ArenaTeam team : arena.getTeams()) {
				if (team.getTeamMembers().contains(ap)) {
					inTeam = true;
					break;
				}
			}

			if (!inTeam) {
				return;
			}

			db.i("onEntityDamageByBLOCKDAMAGE: fighting player");

			db.i("processing damage!");

			PVPArena.instance.getAmm().onEntityDamageByBlockDamage(arena,
					defender, event);

			if (event.getDamage() >= defender.getHealth()) {
				db.i("damage >= health => death");
				int lives = 3;

				lives = arena.type().getLives(defender);
				db.i("lives before death: " + lives);
				if (lives < 1) {
					if (!arena.cfg.getBoolean("game.preventDeath")) {
						return; // player died => commit death!
					}
					db.i("faking player death");

					commitPlayerDeath(arena, defender, event);
				} else {
					lives--;
					arena.respawnPlayer(defender, lives, event.getCause(), null);
				}
				event.setCancelled(true);
			}
			return;
		}

		if (p1 instanceof Projectile) {
			db.i("parsing projectile");
			p1 = ((Projectile) p1).getShooter();
			db.i("=> " + String.valueOf(p1));
		}

		if (event.getEntity() instanceof Wolf) {
			Wolf wolf = (Wolf) event.getEntity();
			if (wolf.getOwner() != null) {
				try {
					p1 = (Entity) wolf.getOwner();
				} catch (Exception e) {
					// wolf belongs to dead player or whatnot
				}
			}
		}

		if ((p1 != null && p2 != null) && p1 instanceof Player
				&& p2 instanceof Player) {
			if (PVPArena.instance.getConfig().getBoolean("onlyPVPinArena")) {
				event.setCancelled(true); // cancel events for regular no PVP
											// servers
			}
		}

		if ((p2 == null) || (!(p2 instanceof Player))) {
			return;
		}

		Arena arena = Arenas.getArenaByPlayer((Player) p2);
		if (arena == null) {
			// defender no arena player => out
			return;
		}

		db.i("onEntityDamageByEntity: fighting player");

		if ((p1 == null) || (!(p1 instanceof Player))) {
			// attacker no player => out!
			commit(arena, p1, (Player) p2, event);
			return;
		}
		db.i("both entities are players");
		Player attacker = (Player) p1;
		Player defender = (Player) p2;

		boolean defTeam = false;
		boolean attTeam = false;
		ArenaPlayer apDefender = ArenaPlayer.parsePlayer(defender);
		ArenaPlayer apAttacker = ArenaPlayer.parsePlayer(attacker);

		for (ArenaTeam team : arena.getTeams()) {
			defTeam = defTeam ? true : team.getTeamMembers().contains(
					apDefender);
			attTeam = attTeam ? true : team.getTeamMembers().contains(
					apAttacker);
		}

		if (!defTeam || !attTeam || arena.REALEND_ID != -1) {
			event.setCancelled(true);
			return;
		}

		db.i("both players part of the arena");

		if (PVPArena.instance.getConfig().getBoolean("onlyPVPinArena")) {
			event.setCancelled(false); // uncancel events for regular no PVP
										// servers
		}

		if ((!arena.cfg.getBoolean("game.teamKill", false))
				&& (Teams.getTeam(arena, apAttacker)).equals(Teams.getTeam(
						arena, apDefender))) {
			// no team fights!
			db.i("team hit, cancel!");
			event.setCancelled(true);
			return;
		}

		if (!arena.fightInProgress) {
			// fight not started, cancel!
			event.setCancelled(true);
			return;
		}

		if (arena.cfg.getBoolean("game.weaponDamage")) {
			if ((attacker.getItemInHand() != null)
					&& (attacker.getItemInHand().getType() != null)
					&& (attacker.getItemInHand().getType() != Material.AIR)) {
				attacker.getItemInHand().setDurability((byte) 0);
			}
		}

		if (arena.cfg.getInt("protection.spawn") > 0) {
			if (Spawns.isNearSpawn(arena, defender,
					arena.cfg.getInt("protection.spawn"))) {
				// spawn protection!
				db.i("spawn protection! damage cancelled!");
				event.setCancelled(true);
				return;
			}
		}

		// here it comes, process the damage!

		db.i("processing damage!");

		PVPArena.instance.getAmm().onEntityDamageByEntity(arena, attacker,
				defender, event);

		Statistics.damage(arena, attacker, defender, event.getDamage());
		
		int reduction = calcArmorDamageReduction(event, defender);
		
		// here it comes, process the damage!
		if (event.getDamage() - reduction >= defender.getHealth()) {
			db.i("damage >= health => death");
			int lives = 3;

			Statistics.kill(arena, attacker, defender, (lives > 0));

			lives = arena.type().getLives(defender);
			db.i("lives before death: " + lives);
			if (lives < 1) {
				if (!arena.cfg.getBoolean("game.preventDeath")) {
					return; // player died => commit death!
				}
				db.i("faking player death");

				commitPlayerDeath(arena, defender, event);
			} else {
				lives--;
				arena.respawnPlayer(defender, lives, event.getCause(), attacker);
			}
			event.setCancelled(true);
		}

	}

	private void commit(Arena arena, Entity attacker, Player defender, EntityDamageByEntityEvent event) {
		db.i("processing damage!");

		PVPArena.instance.getAmm().onEntityDamageByEntity(arena, null,
				defender, event);

		Statistics.damage(arena, attacker, defender, event.getDamage());

		if (event.getDamage() >= defender.getHealth()) {
			db.i("damage >= health => death");
			int lives = 3;

			Statistics.kill(arena, attacker, defender, (lives > 0));

			lives = arena.type().getLives(defender);
			db.i("lives before death: " + lives);
			if (lives < 1) {
				if (!arena.cfg.getBoolean("game.preventDeath")) {
					return; // player died => commit death!
				}
				db.i("faking player death");

				commitPlayerDeath(arena, defender, event);
			} else {
				lives--;
				arena.respawnPlayer(defender, lives, event.getCause(), attacker);
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeath(EntityDeathEvent event) {
		Entity e = event.getEntity();
		if (e instanceof Player) {
			Player player = (Player) e;

			Arena arena = Arenas.getArenaByPlayer(player);
			if (arena == null)
				return;

			db.i("onEntityDeath: fighting player");
			if (!arena.isCustomClassActive()
					&& !arena.cfg.getBoolean("game.allowDrops")) {
				db.i("clearing drops");
				event.getDrops().clear();
			}

			commitPlayerDeath(arena, player, event);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityExplode(EntityExplodeEvent event) {
		db.i("explosion");

		Arena arena = Arenas.getArenaByRegionLocation(event.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("explosion inside an arena");
		if ((!(arena.cfg.getBoolean("protection.enabled", true)))
				|| (!(arena.cfg.getBoolean("protection.blockdamage", true)))
				|| (!(event.getEntity() instanceof TNTPrimed))) {
			PVPArena.instance.getAmm().onEntityExplode(arena, event);
			return;
		}

		event.setCancelled(true); // ELSE => cancel event
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {

		if (event.isCancelled()) {
			return; // respect other plugins
		}

		Entity p1 = event.getEntity();

		if ((p1 == null) || (!(p1 instanceof Player)))
			return; // no player

		Arena arena = Arenas.getArenaByPlayer((Player) p1);
		if (arena == null)
			return;

		db.i("onEntityRegainHealth => fighing player");
		if (!arena.fightInProgress) {
			return;
		}

		Player player = (Player) p1;

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		ArenaTeam team = Teams.getTeam(arena, ap);

		if (team == null) {
			return;
		}

		PVPArena.instance.getAmm().onEntityRegainHealth(arena, event);

	}
}