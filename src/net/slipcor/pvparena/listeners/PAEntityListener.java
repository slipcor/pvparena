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
 * version: v0.3.1 - New Arena! FreeFight
 * 
 * history:
 *
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
				String color = "";
				if (arena.fightUsersTeam.get(player.getName()) == "red") {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("killed", ChatColor.RED + player.getName() + ChatColor.YELLOW));
					arena.redTeam -= 1;
					color = "red";
				} else if (arena.fightUsersTeam.get(player.getName()) == "blue") {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("killed", ChatColor.BLUE + player.getName() + ChatColor.YELLOW));
					arena.blueTeam -= 1;
					color = "blue";
				} else {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("killed", ChatColor.WHITE + player.getName() + ChatColor.YELLOW));
				}
				StatsManager.addLoseStat(player, color);
				arena.fightUsersTeam.remove(player.getName());
				arena.fightUsersRespawn.put(player.getName(), arena.fightUsersClass.get(player.getName()));
				arena.removePlayer(player, arena.sTPdeath);
				arena.checkEnd();
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
				System.out.print("lives < 1");
				return; // player died
			} else if (lives > 0) {

				defender.setHealth(20);
				defender.setFireTicks(0);
				defender.setFoodLevel(20);
				defender.setSaturation(20);
				defender.setExhaustion(0);
				lives--;
				if (arena.fightUsersTeam.get(defender.getName()) == "red") {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("lostlife", ChatColor.RED + defender.getName() + ChatColor.YELLOW, String.valueOf(lives)));
					arena.goToWaypoint(defender, "redspawn");
				} else if (arena.fightUsersTeam.get(defender.getName()) == "blue") {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("lostlife", ChatColor.BLUE + defender.getName() + ChatColor.YELLOW, String.valueOf(lives)));
					arena.goToWaypoint(defender, "bluespawn");
				} else {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("lostlife", ChatColor.WHITE + defender.getName() + ChatColor.YELLOW, String.valueOf(lives)));
					arena.goToWaypoint(defender, "spawn");
				}
				System.out.print("life lost");
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
				System.out.print("lives < 1");
				return; // player died
			} else if (lives > 0) {

				player.setHealth(20);
				player.setFireTicks(0);
				player.setFoodLevel(20);
				player.setSaturation(20);
				player.setExhaustion(0);
				lives--;
				if (arena.fightUsersTeam.get(player.getName()) == "red") {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("lostlife", ChatColor.RED + player.getName() + ChatColor.YELLOW, String.valueOf(lives)));
					arena.goToWaypoint(player, "redspawn");
				} else if (arena.fightUsersTeam.get(player.getName()) == "blue") {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("lostlife", ChatColor.BLUE + player.getName() + ChatColor.YELLOW, String.valueOf(lives)));
					arena.goToWaypoint(player, "bluespawn");
				} else {
					arena.tellEveryone(PVPArenaPlugin.lang.parse("lostlife", ChatColor.WHITE + player.getName() + ChatColor.YELLOW, String.valueOf(lives)));
					arena.goToWaypoint(player, "spawn");
				}
				System.out.print("life lost");
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