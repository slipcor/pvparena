package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.arenas.CTFArena;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;

/*
 * EntityListener class
 * 
 * author: slipcor
 * 
 * version: v0.3.9 - Permissions, rewrite
 * 
 * history:
 *
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.6 - CTF Arena
 *     v0.3.5 - Powerups!!
 *     v0.3.3 - Random spawns possible for every arena
 *     v0.3.1 - New Arena! FreeFight
 *     v0.3.0 - Multiple Arenas
 *     v0.2.1 - cleanup, comments
 *     v0.2.0 - language support
 *     v0.1.12 - display stats
 *     v0.1.11 - fix for bows?
 *     v0.1.8 - lives!
 *     v0.1.5 - class choosing not toggling
 *     v0.1.2 - class permission requirement
 *
 */

public class PAEntityListener extends EntityListener {
	private DebugManager db = new DebugManager();

	public PAEntityListener() {}

	public void onEntityDeath(EntityDeathEvent event) {
		Entity e = event.getEntity();
		if (e instanceof Player) {
			Player player = (Player) e;

			Arena arena = ArenaManager.getArenaByPlayer(player);
			if (arena == null)
				return;

			db.i("onEntityDeath: fighting player");
			if (arena.paPlayersTeam.containsKey(player.getName())) {
				event.getDrops().clear();
				String sTeam = arena.paPlayersTeam.get(player.getName());
				String color = (String) arena.paTeams.get(sTeam);
				if (!color.equals("free")) {
					arena.tellEveryone(PVPArena.lang.parse("killed", ChatColor.valueOf(color) + player.getName() + ChatColor.YELLOW));
				} else {
					arena.tellEveryone(PVPArena.lang.parse("killed", ChatColor.WHITE + player.getName() + ChatColor.YELLOW));
				}
				StatsManager.addLoseStat(player, sTeam, arena);
				arena.paPlayersTeam.remove(player.getName()); // needed so player does not get found when dead
				arena.paPlayersRespawn.put(player.getName(), arena.paPlayersClass.get(player.getName()));
				
				if (arena instanceof CTFArena) {
					CTFArena ca = (CTFArena) arena;
					db.i("ctf arena");
					ca.checkEntityDeath(player);
				}
				
				if (arena.usesPowerups) {
					if (arena.powerupCause.equals("death")) {
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
		}
	}

	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity p1 = event.getDamager();
		Entity p2 = event.getEntity();

		if (event.getCause() == DamageCause.PROJECTILE) {
			p1 = ((Projectile)p1).getShooter();
		}
		if ((p1 == null) || (!(p1 instanceof Player)))
			return; // attacker no player
		Arena arena = ArenaManager.getArenaByPlayer((Player) p1);
		if (arena == null)
			return;

		db.i("onEntityDamageByEntity: fighting player");
		if ((!(arena.fightInProgress))
	        || (p2 == null)
			|| (!(p2 instanceof Player))) {
			return;
		}
		db.i("fight in progress and both entities are players");
		Player attacker = (Player) p1;
		Player defender = (Player) p2;
		if ((arena.paPlayersTeam.get(attacker.getName()) == null)
			|| (arena.paPlayersTeam.get(defender.getName()) == null))
			return;

		db.i("both players part of the arena");
		if ((!arena.teamKilling) && ((String) arena.paPlayersTeam.get(attacker.getName()))
				.equals(arena.paPlayersTeam.get(defender.getName()))) {
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

			lives = arena.paPlayersLives.get(defender.getName());
			if (lives < 1) {
				return; // player died
			} else if (lives > 0) {

				lives--;
				arena.respawnPlayer(defender, lives);
				event.setCancelled(true);
				return;
			}
		}

	}

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

		db.i("onEntityDamege: fighting player");
		if (!arena.fightInProgress) {
			return;
		}
		
		Player player = (Player) p1;
		if (arena.paPlayersTeam.get(player.getName()) == null)
			return;

		// here it comes, process the damage!
		if (event.getDamage() >= player.getHealth()) {
			db.i("damage >= health => death");
			byte lives = 3;

			lives = arena.paPlayersLives.get(player.getName());
			if (lives < 1) {
				return; // player died
			} else if (lives > 0) {

				arena.respawnPlayer(player, lives);
				event.setCancelled(true);
				return;
			}
		}
		
	}
	
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
		if (arena.paPlayersTeam.get(player.getName()) == null)
			return;
		
		if (arena.pm != null) {
			Powerup p = arena.pm.puActive.get(player);
			if (p != null) {
				if (p.canBeTriggered()) {
					if (p.active(PowerupEffect.classes.HEAL)) {
						event.setCancelled(true);
						p.commit(event);
					}
				}
			}
			
		}
	}

	public void onEntityExplode(EntityExplodeEvent event) {
		Arena arena = ArenaManager.getArenaByBattlefieldLocation(event.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("explosion inside an arena");
		if ((!(arena.usesProtection)) || (!(arena.blockTnt)) || (!(event.getEntity() instanceof TNTPrimed)))
			return;
		
		event.setCancelled(true); //ELSE => cancel event
	}
}