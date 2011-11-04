package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.PVPArenaPlugin;
import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.StatsManager;

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

/*
 * EntityListener class
 * 
 * author: slipcor
 * 
 * version: v0.3.3 - Random spawns possible for every arena
 * 
 * history:
 *
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

	public PAEntityListener() {}

	public void onEntityDeath(EntityDeathEvent event) {
		Entity e = event.getEntity();
		if (e instanceof Player) {
			Player player = (Player) e;

			Arena arena = ArenaManager.getArenaByPlayer(player);
			if (arena == null)
				return;
			
			if (arena.fightUsersTeam.containsKey(player.getName())) {
				event.getDrops().clear();
				String sTeam = arena.fightUsersTeam.get(player.getName());
				String color = arena.fightTeams.get(sTeam);
				if (color != null) {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("killed", ChatColor.valueOf(color) + player.getName() + ChatColor.YELLOW));
				} else {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("killed", ChatColor.WHITE + player.getName() + ChatColor.YELLOW));
				}
				StatsManager.addLoseStat(player, sTeam, arena);
				arena.fightUsersTeam.remove(player.getName()); // needed so player does not get found when dead
				arena.fightUsersRespawn.put(player.getName(), arena.fightUsersClass.get(player.getName()));
				
				if (arena.checkEnd())
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

		if ((!(arena.fightInProgress))
	        || (p2 == null)
			|| (!(p2 instanceof Player))) {
			return;
		}
		Player attacker = (Player) p1;
		Player defender = (Player) p2;
		if ((arena.fightUsersTeam.get(attacker.getName()) == null)
			|| (arena.fightUsersTeam.get(defender.getName()) == null))
			return;

		if ((!arena.teamkilling) && ((String) arena.fightUsersTeam.get(attacker.getName()))
				.equals(arena.fightUsersTeam.get(defender.getName()))) {
			// no team fights!
			event.setCancelled(true);
			return;
		}

		// here it comes, process the damage!
		if (event.getDamage() >= defender.getHealth()) {
			byte lives = 3;

			lives = arena.fightUsersLives.get(defender.getName());
			if (lives < 1) {
				return; // player died
			} else if (lives > 0) {

				defender.setHealth(20);
				defender.setFireTicks(0);
				defender.setFoodLevel(20);
				defender.setSaturation(20);
				defender.setExhaustion(0);
				lives--;
				String color = arena.fightTeams.get(arena.fightUsersTeam.get(defender.getName()));
				if (!arena.randomSpawn && color != null) {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("lostlife", ChatColor.valueOf(color) + defender.getName() + ChatColor.YELLOW, String.valueOf(lives)));
					arena.goToWaypoint(defender, color + "spawn");
				} else {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("lostlife", ChatColor.WHITE + defender.getName() + ChatColor.YELLOW, String.valueOf(lives)));
					arena.goToWaypoint(defender, "spawn");
				}
				arena.fightUsersLives.put(defender.getName(), lives);
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

		if (!arena.fightInProgress) {
			return;
		}
		
		Player player = (Player) p1;
		if (arena.fightUsersTeam.get(player.getName()) == null)
			return;

		// here it comes, process the damage!
		if (event.getDamage() >= player.getHealth()) {
			byte lives = 3;

			lives = arena.fightUsersLives.get(player.getName());
			if (lives < 1) {
				return; // player died
			} else if (lives > 0) {

				player.setHealth(20);
				player.setFireTicks(0);
				player.setFoodLevel(20);
				player.setSaturation(20);
				player.setExhaustion(0);
				lives--;
				String color = arena.fightTeams.get(arena.fightUsersTeam.get(player.getName()));
				if (!arena.randomSpawn && color != null) {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("lostlife", ChatColor.valueOf(color) + player.getName() + ChatColor.YELLOW, String.valueOf(lives)));
					arena.goToWaypoint(player, color + "spawn");
				} else {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("lostlife", ChatColor.WHITE + player.getName() + ChatColor.YELLOW, String.valueOf(lives)));
					arena.goToWaypoint(player, "spawn");
				}
				arena.fightUsersLives.put(player.getName(), lives);
				event.setCancelled(true);
				return;
			}
		}
		
	}

	public void onEntityExplode(EntityExplodeEvent event) {
		Arena arena = ArenaManager.getArenaByBattlefieldLocation(event.getLocation());
		if (arena == null)
			return; // no arena => out
		
		if ((!(arena.protection)) || (!(arena.blocktnt)) || (!(event.getEntity() instanceof TNTPrimed)))
			return;
		
		event.setCancelled(true); //ELSE => cancel event
	}
}