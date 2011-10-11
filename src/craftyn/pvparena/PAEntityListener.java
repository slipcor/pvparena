package craftyn.pvparena;

import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

/*
 * EntityListener class
 * 
 * author: slipcor
 * 
 * version: v0.1.8 - lives!
 * 
 * history:
 *
 *    v0.1.5 - class choosing not toggling
 *    v0.1.2 - class permission requirement
 *
 */

public class PAEntityListener extends EntityListener {
	public PVPArena plugin;

	public PAEntityListener(PVPArena instance) {
		this.plugin = instance;
	}

	public void onEntityDeath(EntityDeathEvent event) {
		Entity e = event.getEntity();
		if (e instanceof Player) {
			Player player = (Player) e;

			if (PVPArena.fightUsersTeam.containsKey(player.getName())) {
				event.getDrops().clear();

				if (PVPArena.fightUsersTeam.get(player.getName()) == "red") {
					PVPArena.tellEveryone(ChatColor.RED + player.getName()
							+ ChatColor.WHITE + " has been killed!");
					PVPArena.redTeam -= 1;
				} else {
					PVPArena.tellEveryone(ChatColor.BLUE + player.getName()
							+ ChatColor.WHITE + " has been killed!");
					PVPArena.blueTeam -= 1;
				}
				PVPArena.fightUsersTeam.remove(player.getName());
				PVPArena.fightUsersRespawn.put(player.getName(), PVPArena.fightUsersClass.get(player.getName()));
				if (PVPArena.checkEnd())
					return;
				PVPArena.removePlayer(player);
			}
		}
	}

	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity p1 = event.getDamager();
		Entity p2 = event.getEntity();
		
		if ((!(PVPArena.fightInProgress))
			|| (p1 == null) || (!(p1 instanceof Player)) || (p2 == null)
			|| (!(p2 instanceof Player)))
			return;
		Player attacker = (Player) p1;
		Player defender = (Player) p2;
		if ((PVPArena.fightUsersTeam.get(attacker.getName()) == null)
			|| (PVPArena.fightUsersTeam.get(defender.getName()) == null))
			return;
		
		if ((!PVPArena.teamkilling) && ((String) PVPArena.fightUsersTeam.get(attacker.getName()))
				.equals(PVPArena.fightUsersTeam.get(defender.getName()))) {
			// no team fights!
			event.setCancelled(true);
			return;
		}
			
		// here it comes, process the damage!
		if (event.getDamage() > defender.getHealth()) {
			byte lives = 3;
			
			lives = PVPArena.fightUsersLives.get(defender.getName());
			if (lives < 1) {
				return; // player died spectating wherever
			} else if (lives > 0) {

				defender.setHealth(20);
				defender.setFireTicks(0);
				defender.setFoodLevel(20);
				defender.setSaturation(20);
				defender.setExhaustion(0);

				if (PVPArena.fightUsersTeam.get(defender.getName()) == "red") {
					PVPArena.tellEveryone(ChatColor.RED + defender.getName()
							+ ChatColor.WHITE + " has lost a life! "+ String.valueOf(lives) + " remaining.");
					PVPArena.goToWaypoint(defender, "redspawn");
					
				} else {
					PVPArena.tellEveryone(ChatColor.BLUE + defender.getName()
							+ ChatColor.WHITE + " has lost a life! "+ String.valueOf(lives) + " remaining.");
					PVPArena.goToWaypoint(defender, "bluespawn");
					
				}
				PVPArena.fightUsersLives.put(defender.getName(), --lives);
				event.setCancelled(true);
				return;
			}
		}
		
	}

	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (event instanceof EntityDamageByEntityEvent) {
			onEntityDamageByEntity((EntityDamageByEntityEvent) event);
			return;
		}
	}

	public void onEntityExplode(EntityExplodeEvent event) {
		if ((!(PVPArena.protection)) || (!(PVPArena.blocktnt))
				|| (!(event.getEntity() instanceof TNTPrimed)))
			return;
		Configuration config = new Configuration(new File("plugins/pvparena",
				"config.yml"));
		config.load();
		if ((config.getKeys("protection.region") == null)
				|| (!(config.getString("protection.region.world").equals(event
						.getEntity().getWorld().getName()))))
			return;
		boolean inside = PVPArena.contains(new Vector(event.getEntity()
				.getLocation().getX(), event.getEntity().getLocation().getY(),
				event.getEntity().getLocation().getZ()));
		if (!(inside))
			return;
		event.setCancelled(true);
		return;
	}
}