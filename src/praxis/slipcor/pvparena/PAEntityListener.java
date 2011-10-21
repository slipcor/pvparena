package praxis.slipcor.pvparena;

import java.io.File;
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
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

/*
 * EntityListener class
 * 
 * author: slipcor
 * 
 * version: v0.2.1 - cleanup, comments
 * 
 * history:
 *
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
		if (!(e instanceof Player))
			return; // no player => OUT
		Player player = (Player) e;

		if (!PVPArena.fightUsersTeam.containsKey(player.getName()))
			return; // no fighting player => OUT
		
		if (!(player.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
			return; // no PVP
		}
		EntityDamageByEntityEvent lastEvent = (EntityDamageByEntityEvent) player.getLastDamageCause();
		
		Entity p1 = lastEvent.getDamager();
		Entity p2 = lastEvent.getEntity();
		
		if (lastEvent.getCause() == DamageCause.PROJECTILE) {
			p1 = ((Projectile)p1).getShooter(); // override if using e.g. bow
		}
		
		if ((!(PVPArena.fightInProgress))
			|| (p1 == null) || (!(p1 instanceof Player)) || (p2 == null)
			|| (!(p2 instanceof Player)))
			return; // no fight in progress or either entity not a player => OUT


		byte lives = 3;
		
		lives = PVPArena.fightUsersLives.get(player.getName());
		if (lives < 1) {
			// player died
			event.getDrops().clear();
			if (PVPArena.fightUsersTeam.get(player.getName()) == "red") {
				PVPArena.tellEveryone(PVPArena.lang.parse("killed", ChatColor.RED + player.getName() + ChatColor.WHITE));
				PVPArena.redTeam -= 1;
				PAStatsManager.addLoseStat(player, "red");
				// tell everyone a red player was killed
			} else {
				PVPArena.tellEveryone(PVPArena.lang.parse("killed", ChatColor.BLUE + player.getName() + ChatColor.WHITE));
				PVPArena.blueTeam -= 1;
				PAStatsManager.addLoseStat(player, "blue");
				// tell everyone a blue player was killed
			}
			PVPArena.fightUsersTeam.remove(player.getName()); // no longer fighting!
			PVPArena.fightUsersRespawn.put(player.getName(), PVPArena.fightUsersClass.get(player.getName()));
				// player may respawn
			if (PVPArena.checkEnd())
				return; // if we're at the end => OUT
			
			PVPArena.removePlayer(player, PVPArena.sTPdeath); // teleport to death location
		} else if (lives > 0) {

			event.getDrops().clear(); // don't drop anything
			
			// reset health/food thingies
			player.setHealth(20);
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setSaturation(20);
			player.setExhaustion(0);
			lives--;
			if (PVPArena.fightUsersTeam.get(player.getName()) == "red") {
				PVPArena.tellEveryone(PVPArena.lang.parse("lostlife", ChatColor.RED + player.getName() + ChatColor.WHITE, String.valueOf(lives)));
				PVPArena.goToWaypoint(player, "redspawn");
				// tp to red spawn, announce life loss
			} else {
				PVPArena.tellEveryone(PVPArena.lang.parse("lostlife", ChatColor.BLUE + player.getName() + ChatColor.WHITE, String.valueOf(lives)));
				PVPArena.goToWaypoint(player, "bluespawn");
				// tp to blue spawn, announce life loss
			}
			PVPArena.fightUsersLives.put(player.getName(), lives); // update lives
			return;
		}
	}

	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity p1 = event.getDamager();
		Entity p2 = event.getEntity();
		
		if (event.getCause() == DamageCause.PROJECTILE) {
			p1 = ((Projectile)p1).getShooter(); // override if using e.g. bow
		}
		
		if ((!(PVPArena.fightInProgress))
			|| (p1 == null) || (!(p1 instanceof Player)) || (p2 == null)
			|| (!(p2 instanceof Player)))
			return; // no fight in progress or either entity not a player => OUT
		Player attacker = (Player) p1;
		Player defender = (Player) p2;
		if ((PVPArena.fightUsersTeam.get(attacker.getName()) == null)
			|| (PVPArena.fightUsersTeam.get(defender.getName()) == null))
			return; // either player not fighting => OUT
		
		if ((!PVPArena.teamkilling) && ((String) PVPArena.fightUsersTeam.get(attacker.getName()))
				.equals(PVPArena.fightUsersTeam.get(defender.getName()))) {
			// no team fights => cancel the event!
			event.setCancelled(true);
			return;
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
		if ((!(PVPArena.protection)) || (!(PVPArena.blocktnt)) || (!(event.getEntity() instanceof TNTPrimed)))
			return;  // no protection needed => OUT
		Configuration config = new Configuration(new File("plugins/pvparena",
				"config.yml"));
		config.load();
		if ((config.getKeys("protection.region") == null)
				|| (!(config.getString("protection.region.world").equals(event
						.getEntity().getWorld().getName()))))
			return; // no protection region set => OUT
		boolean inside = PVPArena.contains(new Vector(event.getEntity()
				.getLocation().getX(), event.getEntity().getLocation().getY(),
				event.getEntity().getLocation().getZ()));
		if (!(inside))
			return; // not inside => OUT
		
		event.setCancelled(true); //ELSE => cancel event
	}
}