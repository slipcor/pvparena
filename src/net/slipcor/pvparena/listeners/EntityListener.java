package net.slipcor.pvparena.listeners;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.definitions.Announcement;
import net.slipcor.pvparena.definitions.Announcement.type;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.Powerup;
import net.slipcor.pvparena.definitions.PowerupEffect;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Blocks;
import net.slipcor.pvparena.managers.Ends;
import net.slipcor.pvparena.managers.Flags;
import net.slipcor.pvparena.managers.Inventories;
import net.slipcor.pvparena.managers.Players;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.managers.Statistics;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftThrownPotion;
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

/**
 * entity listener class
 * 
 * -
 * 
 * PVP Arena Entity Listener
 * 
 * @author slipcor
 * 
 * @version v0.6.40
 * 
 */

public class EntityListener implements Listener {
	private Debug db = new Debug(20);

	static HashSet<Player> burningPlayers = new HashSet<Player>();

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
					|| !arena.cfg.getBoolean("game.allowDrops")) {
				db.i("clearing drops");
				event.getDrops().clear();
			}

			commitPlayerDeath(arena, player, event);
		}
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

		EntityListener.addBurningPlayer(player);
		String sTeam = Players.getTeam(player);
		Announcement.announce(arena, type.LOSER, Language.parse("killedby",
				player.getName(), Players.parseDeathCause(arena, player, player
						.getLastDamageCause().getCause(), Players.getLastDamagingPlayer(player.getLastDamageCause()))));
		Players.tellEveryone(arena, Language.parse("killedby",
				arena.colorizePlayerByTeam(player, sTeam) + ChatColor.YELLOW,
				Players.parseDeathCause(arena, player, player
						.getLastDamageCause().getCause(), Players.getLastDamagingPlayer(player.getLastDamageCause()))));

		Players.parsePlayer(player).losses++;
		Players.setTeam(player, ""); // needed so player does not
										// get found when dead

		if (arena.isCustomClassActive()
				&& arena.cfg.getBoolean("game.allowDrops")) {
			Inventories.drop(player);
		}
		player.getInventory().clear();

		arena.tpPlayerToCoordName(player, "spectator");

		if (arena.cfg.getBoolean("arenatype.flags")) {
			Flags.checkEntityDeath(arena, player);
		}

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
			String sKilled = "";

			db.i("timed ctf/pumpkin arena");
			sKilled = player.getName();
			if (damager != null) {
				sKiller = damager.getName();
				db.i("killer: " + sKiller);
			}
			
			if (damager != null) {
				if (Players.getKills(sKiller) > 0) {
					db.i("killer killed already");
					Players.addKill(sKiller);
				} else {
					db.i("first kill");
					Players.addKill(sKiller);
				}
			}

			if (Players.getDeaths(sKilled) > 0) {
				db.i("already died");
				Players.addDeath(sKilled);
			} else {
				db.i("first death");
				Players.addDeath(sKilled);
				arena.betPossible = false;
			}
		}
		if (arena.usesPowerups) {
			if (arena.cfg.getString("game.powerups", "off").startsWith("death")) {
				db.i("calculating powerup trigger death");
				arena.powerupDiffI = ++arena.powerupDiffI % arena.powerupDiff;
				if (arena.powerupDiffI == 0) {
					arena.calcPowerupSpawn();
				}
			}
		}

		if (Ends.checkAndCommit(arena))
			return;
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

		db.i("onEntityDamageByEntity: cause: " + event.getCause().name() + " : " + event.getDamager().toString() + " => " + event.getEntity().toString());

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

			if (Players.getPlayerTeamMap(arena).get(defender.getName()) == null) {
				return;
			}

			db.i("onEntityDamageByBLOCKDAMAGE: fighting player");

			db.i("processing damage!");

			if (arena.pum != null) {
				db.i("committing powerup triggers");
				Powerup p = arena.pum.puActive.get(defender);
				if ((p != null) && (p.canBeTriggered()))
					p.commit(null, defender, event);

			}

			if (event.getDamage() >= defender.getHealth()) {
				db.i("damage >= health => death");
				int lives = 3;

				lives = arena.paLives.get(defender.getName());
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
			db.i("=> " + p1.toString());
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
			// event.setCancelled(true);
			return;
		}
		db.i("both entities are players");
		Player attacker = (Player) p1;
		Player defender = (Player) p2;

		if (Players.getTeam(defender).equals("")
				|| Players.parsePlayer(attacker) == null) {
			event.setCancelled(true);
			return;
		}

		db.i("both players part of the arena");

		if (PVPArena.instance.getConfig().getBoolean("onlyPVPinArena")) {
			event.setCancelled(false); // uncancel events for regular no PVP
										// servers
		}

		if ((!arena.cfg.getBoolean("game.teamKill", false))
				&& (Players.getTeam(attacker))
						.equals(Players.getTeam(defender))) {
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

		// TODO calculate armor

		// here it comes, process the damage!

		db.i("processing damage!");
		if (arena.pum != null) {
			db.i("committing powerup triggers");
			Powerup p = arena.pum.puActive.get(attacker);
			if ((p != null) && (p.canBeTriggered()))
				p.commit(attacker, defender, event);

			p = arena.pum.puActive.get(defender);
			if ((p != null) && (p.canBeTriggered()))
				p.commit(attacker, defender, event);

		}

		Statistics.damage(arena, attacker, defender, event.getDamage());

		if (event.getDamage() >= defender.getHealth()) {
			db.i("damage >= health => death");
			int lives = 3;

			Statistics.kill(arena, attacker, defender, (lives > 0));

			lives = arena.paLives.get(defender.getName());
			db.i("lives before death: " + lives);
			if (lives < 1) {
				if (!arena.cfg.getBoolean("game.preventDeath")) {
					return; // player died => commit death!
				}
				db.i("faking player death");

				commitPlayerDeath(arena, defender, event);
			} else {
				lives--;
				arena.deathMatch(attacker);
				arena.respawnPlayer(defender, lives, event.getCause(), attacker);
			}
			event.setCancelled(true);
		}

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamage(EntityDamageEvent event) {
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

		if (Players.getTeam(player).equals("")
				|| Players.parsePlayer(player).spectator) {
			event.setCancelled(true);
			return;
		}

		Statistics.damage(arena, null, player, event.getDamage());

		// TODO calculate damage and armor
		// here it comes, process the damage!
		if (event.getDamage() >= player.getHealth()) {
			db.i("damage >= health => death");
			int lives = 3;

			Statistics.kill(arena, null, player, (lives > 0));
			if (!arena.cfg.getBoolean("arenatype.flags")) {
				lives = arena.paLives.get(player.getName());
				db.i("lives before death: " + lives);
			}
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
		if (Players.getTeam(player).equals(""))
			return;

		if (arena.pum != null) {
			Powerup p = arena.pum.puActive.get(player);
			if (p != null) {
				if (p.canBeTriggered()) {
					if (p.isEffectActive(PowerupEffect.classes.HEAL)) {
						event.setCancelled(true);
						p.commit(event);
					}
				}
			}

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
			if (arena.fightInProgress) {
				for (Block b : event.blockList()) {
					Blocks.saveBlock(b);
				}
			}
			return;
		}

		event.setCancelled(true); // ELSE => cancel event
	}

	public static void addBurningPlayer(Player player) {
		burningPlayers.add(player);
	}
}