package praxis.slipcor.pvparena.listeners;

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

import praxis.slipcor.pvparena.PAArena;
import praxis.slipcor.pvparena.PVPArena;
import praxis.slipcor.pvparena.managers.ArenaManager;
import praxis.slipcor.pvparena.managers.StatsManager;

/*
 * EntityListener class
 * 
 * author: slipcor
 * 
 * version: v0.3.0 - Multiple Arenas
 * 
 * history:
 *
 *    v0.2.1 - cleanup, comments
 *    v0.2.0 - language support
 *    v0.1.12 - display stats
 *    v0.1.11 - fix for bows?
 *    v0.1.8 - lives!
 *    v0.1.5 - class choosing not toggling
 *    v0.1.2 - class permission requirement
 *
 */

public class PAEntityListener extends EntityListener {

	public PAEntityListener() {}

	public void onEntityDeath(EntityDeathEvent event) {
		Entity e = event.getEntity();
		if (e instanceof Player) {
			Player player = (Player) e;

			PAArena arena = ArenaManager.getArenaByPlayer(player);
			if (arena == null)
				return;
			
			if (arena.fightUsersTeam.containsKey(player.getName())) {
				event.getDrops().clear();
				String color = "";
				if (arena.fightUsersTeam.get(player.getName()) == "red") {
					arena.tellEveryone(PVPArena.lang.parse("killed", ChatColor.RED + player.getName() + ChatColor.WHITE));
					arena.redTeam -= 1;
					color = "red";
				} else {
					arena.tellEveryone(PVPArena.lang.parse("killed", ChatColor.BLUE + player.getName() + ChatColor.WHITE));
					arena.blueTeam -= 1;
					color = "blue";
				}
				StatsManager.addLoseStat(player, color);
				arena.fightUsersTeam.remove(player.getName());
				arena.fightUsersRespawn.put(player.getName(), arena.fightUsersClass.get(player.getName()));
				if (arena.checkEnd())
					return;

				arena.removePlayer(player, arena.sTPexit);
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
		PAArena arena = ArenaManager.getArenaByPlayer((Player) p1);
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
					arena.tellEveryone(PVPArena.lang.parse("lostlife", ChatColor.RED + defender.getName() + ChatColor.WHITE, String.valueOf(lives)));
					arena.goToWaypoint(defender, "redspawn");

				} else {
					arena.tellEveryone(PVPArena.lang.parse("lostlife", ChatColor.BLUE + defender.getName() + ChatColor.WHITE, String.valueOf(lives)));
					arena.goToWaypoint(defender, "bluespawn");
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
	}

	public void onEntityExplode(EntityExplodeEvent event) {
		PAArena arena = ArenaManager.getArenaByBattlefieldLocation(event.getLocation());
		if (arena == null)
			return; // no arena => out
		
		if ((!(arena.protection)) || (!(arena.blocktnt)) || (!(event.getEntity() instanceof TNTPrimed)))
			return;
		
		event.setCancelled(true); //ELSE => cancel event
	}
}