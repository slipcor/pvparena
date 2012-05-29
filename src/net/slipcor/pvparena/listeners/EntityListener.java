package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.managers.Statistics;
import net.slipcor.pvparena.managers.Teams;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
 * @version v0.8.4
 * 
 */

public class EntityListener implements Listener {
	private static Debug db = new Debug(20);

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

		// TODO NOT LAGGING

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
	}
}