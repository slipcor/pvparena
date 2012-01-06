package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.arenas.CTFArena;
import net.slipcor.pvparena.arenas.PumpkinArena;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.DebugManager;
import net.slipcor.pvparena.managers.StatsManager;
import net.slipcor.pvparena.powerups.Powerup;
import net.slipcor.pvparena.powerups.PowerupEffect;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
 * @version v0.5.4
 * 
 */

public class PAEntityListener extends EntityListener {
	private DebugManager db = new DebugManager();

	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		Entity e = event.getEntity();
		if (e instanceof Player) {
			Player player = (Player) e;

			Arena arena = ArenaManager.getArenaByPlayer(player);
			if (arena == null)
				return;

			db.i("onEntityDeath: fighting player");
			if (!arena.playerManager.getTeam(player).equals("")) {
				event.getDrops().clear();

				commitPlayerDeath(arena, player, event);
			}
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

		String sTeam = arena.playerManager.getTeam(player);
		String color = arena.paTeams.get(sTeam);
		if (!color.equals("") && !color.equals("free")) {
			arena.playerManager.tellEveryone(PVPArena.lang.parse("killed",
					ChatColor.valueOf(color) + player.getName()
							+ ChatColor.YELLOW));
		} else {
			arena.playerManager.tellEveryone(PVPArena.lang.parse("killed",
					ChatColor.WHITE + player.getName() + ChatColor.YELLOW));
		}

		StatsManager.addLoseStat(player, sTeam, arena);
		arena.playerManager.setTeam(player, ""); // needed so player does not
													// get found when dead
		arena.playerManager.setRespawn(player, true);
		arena.tpPlayerToCoordName(player, "spectator");

		if (arena.getType().equals("ctf")) {
			CTFArena ca = (CTFArena) arena;
			db.i("ctf arena");
			ca.checkEntityDeath(player);
		} else if (arena.getType().equals("pumpkin")) {
			PumpkinArena pa = (PumpkinArena) arena;
			db.i("pumpkin arena");
			pa.checkEntityDeath(player);
		}

		if (arena.timed > 0) {
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
			if (arena.getType().equals("ctf") || arena.getType().equals("pumpkin")) {
				db.i("timed ctf/pumpkin arena");
				sKilled = player.getName();
				if (damager != null) {
					sKiller = damager.getName();
					db.i("killer: " + sKiller);
				}
			} else {
				sKilled = arena.playerManager.getTeam(player);
				if (damager != null) {
					sKiller = arena.playerManager.getTeam(damager);
				}
			}
			if (damager != null) {
				if (arena.playerManager.getKills(sKiller) > 0) {
					db.i("killer killed already");
					arena.playerManager.addKill(sKiller);
				} else {
					db.i("first kill");
					arena.playerManager.addKill(sKiller);
				}
			}

			if (arena.playerManager.getDeaths(sKilled) > 0) {
				db.i("already died");
				arena.playerManager.addDeath(sKilled);
			} else {
				db.i("first death");
				arena.playerManager.addDeath(sKilled);
			}
		}
		if (arena.usesPowerups) {
			if (arena.cfg.getString("general.powerups", "off").startsWith(
					"death")) {
				db.i("calculating powerup trigger death");
				arena.powerupDiffI = ++arena.powerupDiffI % arena.powerupDiff;
				if (arena.powerupDiffI == 0) {
					arena.calcPowerupSpawn();
				}
			}
		}

		if (arena.checkEndAndCommit())
			return;
	}

	/**
	 * parsing of damage: Entity vs Entity
	 * 
	 * @param event
	 *            the triggering event
	 */
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity p1 = event.getDamager();
		Entity p2 = event.getEntity();

		db.i("onEntityDamageByEntity: cause: " + event.getCause().name());

		if (event.getCause() == DamageCause.BLOCK_EXPLOSION) {

			db.i("onEntityDamageByEntity: fighting player");
			if ((p2 == null) || (!(p2 instanceof Player))) {
				return;
			}
			db.i("damaged entity is player");
			Player defender = (Player) p2;
			Arena arena = ArenaManager.getArenaByPlayer(defender);
			if (arena == null)
				return;

			db.i("onEntityDamageByBLOCKDAMAGE: fighting player");

			if (arena.playerManager.getTeam(defender).equals(""))
				return;

			db.i("processing damage!");
			if (arena.pm != null) {
				db.i("committing powerup triggers");
				Powerup p = arena.pm.puActive.get(defender);
				if ((p != null) && (p.canBeTriggered()))
					p.commit(null, defender, event);

			}

			if (event.getDamage() >= defender.getHealth()) {
				db.i("damage >= health => death");
				byte lives = 3;

				lives = arena.playerManager.getLives(defender);
				db.i("lives before death: " + lives);
				if (lives < 1) {
					if (!arena.preventDeath) {
						return; // player died => commit death!
					}
					db.i("faking player death");

					commitPlayerDeath(arena, defender, event);
				} else {
					lives--;
					arena.respawnPlayer(defender, lives);
				}
				event.setCancelled(true);
			}
			return;
		}

		if (event.getCause() == DamageCause.PROJECTILE) {
			p1 = ((Projectile) p1).getShooter();
		}

		if ((p1 == null) || (!(p1 instanceof Player)))
			return; // attacker no player
		Arena arena = ArenaManager.getArenaByPlayer((Player) p1);
		if (arena == null)
			return;

		db.i("onEntityDamageByEntity: fighting player");
		if ((p2 == null) || (!(p2 instanceof Player))) {
			return;
		}
		db.i("both entities are players");
		Player attacker = (Player) p1;
		Player defender = (Player) p2;

		if ((arena.playerManager.getTeam(attacker).equals(""))
				|| (arena.playerManager.getTeam(defender).equals("")))
			return;

		db.i("both players part of the arena");
		if ((!arena.cfg.getBoolean("general.teamkill", false))
				&& (arena.playerManager.getTeam(attacker))
						.equals(arena.playerManager.getTeam(defender))) {
			// no team fights!
			db.i("team hit, cancel!");
			event.setCancelled(true);
			return;
		}

		// here it comes, process the damage!

		db.i("processing damage!");
		if (arena.pm != null) {
			db.i("committing powerup triggers");
			Powerup p = arena.pm.puActive.get(attacker);
			if ((p != null) && (p.canBeTriggered()))
				p.commit(attacker, defender, event);

			p = arena.pm.puActive.get(defender);
			if ((p != null) && (p.canBeTriggered()))
				p.commit(attacker, defender, event);

		}
		if (event.getDamage() >= defender.getHealth()) {
			db.i("damage >= health => death");
			byte lives = 3;

			lives = arena.playerManager.getLives(defender);
			db.i("lives before death: " + lives);
			if (lives < 1) {
				if (!arena.preventDeath) {
					return; // player died => commit death!
				}
				db.i("faking player death");

				commitPlayerDeath(arena, defender, event);
			} else {
				lives--;
				arena.respawnPlayer(defender, lives);
			}
			event.setCancelled(true);
		}

	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) {
			return; // respect other plugins
		}

		if (event instanceof EntityDamageByEntityEvent) {
			onEntityDamageByEntity((EntityDamageByEntityEvent) event);
			return; // hand over damage event
		}

		Entity p1 = event.getEntity();

		if ((p1 == null) || (!(p1 instanceof Player)))
			return; // no player

		Arena arena = ArenaManager.getArenaByPlayer((Player) p1);
		if (arena == null)
			return;

		db.i("onEntityDamage: fighting player");
		if (!arena.fightInProgress) {
			return;
		}

		Player player = (Player) p1;
		if (arena.playerManager.getTeam(player).equals(""))
			return;

		// here it comes, process the damage!
		if (event.getDamage() >= player.getHealth()) {
			db.i("damage >= health => death");
			byte lives = 3;

			lives = arena.playerManager.getLives(player);
			db.i("lives before death: " + lives);
			if (lives < 1) {
				if (!arena.preventDeath) {
					return; // player died => commit death!
				}
				db.i("faking player death");

				commitPlayerDeath(arena, player, event);
			} else {
				lives--;
				arena.respawnPlayer(player, lives);
			}
			event.setCancelled(true);
		}

	}

	@Override
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {

		if (event.isCancelled()) {
			return; // respect other plugins
		}

		Entity p1 = event.getEntity();

		if ((p1 == null) || (!(p1 instanceof Player)))
			return; // no player

		Arena arena = ArenaManager.getArenaByPlayer((Player) p1);
		if (arena == null)
			return;

		db.i("onEntityRegainHealth => fighing player");
		if (!arena.fightInProgress) {
			return;
		}

		Player player = (Player) p1;
		if (arena.playerManager.getTeam(player).equals(""))
			return;

		if (arena.pm != null) {
			Powerup p = arena.pm.puActive.get(player);
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

	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		db.i("explosion");

		Arena arena = ArenaManager
				.getArenaByRegionLocation(event.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("explosion inside an arena");
		if ((!(arena.cfg.getBoolean("protection.enabled", true)))
				|| (!(arena.cfg.getBoolean("protection.blockdamage", true)))
				|| (!(event.getEntity() instanceof TNTPrimed)))
			return;

		event.setCancelled(true); // ELSE => cancel event
	}
}