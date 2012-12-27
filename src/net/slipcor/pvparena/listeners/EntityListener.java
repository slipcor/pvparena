package net.slipcor.pvparena.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionProtection;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.runnables.DamageResetRunnable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * <pre>Entity Listener class</pre>
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class EntityListener implements Listener {
	private static Debug db = new Debug(21);
	private static HashMap<PotionEffectType, Boolean> teamEffect = new HashMap<PotionEffectType, Boolean>();
	
	static {
		teamEffect.put(PotionEffectType.BLINDNESS, false);
		teamEffect.put(PotionEffectType.CONFUSION, false);
		teamEffect.put(PotionEffectType.DAMAGE_RESISTANCE, true);
		teamEffect.put(PotionEffectType.FAST_DIGGING, true);
		teamEffect.put(PotionEffectType.FIRE_RESISTANCE, true);
		teamEffect.put(PotionEffectType.HARM, false);
		teamEffect.put(PotionEffectType.HEAL, true);
		teamEffect.put(PotionEffectType.HUNGER, false);
		teamEffect.put(PotionEffectType.INCREASE_DAMAGE, true);
		teamEffect.put(PotionEffectType.JUMP, true);
		teamEffect.put(PotionEffectType.POISON, false);
		teamEffect.put(PotionEffectType.REGENERATION, true);
		teamEffect.put(PotionEffectType.SLOW, false);
		teamEffect.put(PotionEffectType.SLOW_DIGGING, false);
		teamEffect.put(PotionEffectType.SPEED, true);
		teamEffect.put(PotionEffectType.WATER_BREATHING, true);
		teamEffect.put(PotionEffectType.WEAKNESS, false);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		HashSet<SpawnReason> naturals = new HashSet<SpawnReason>();
		naturals.add(SpawnReason.CHUNK_GEN);
		naturals.add(SpawnReason.DEFAULT);
		naturals.add(SpawnReason.NATURAL);
		naturals.add(SpawnReason.SLIME_SPLIT);
		naturals.add(SpawnReason.VILLAGE_INVASION);
		
		if (!naturals.contains(event.getSpawnReason())) {
			// custom generation, this is not our business!
			return;
		}
		
		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getLocation()), RegionProtection.MOBS);
		if (arena == null)
			return; // no arena => out
		
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		db.i("explosion");

		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(new PABlockLocation(event.getLocation()), RegionProtection.TNT);
		if (arena == null)
			return; // no arena => out

		db.i("explosion inside an arena");
		if (!(arena.getArenaConfig().getBoolean(CFG.PROTECT_ENABLED))
				|| (!BlockListener.isProtected(event.getLocation(), event, RegionProtection.TNT))
				|| (!(event.getEntity() instanceof TNTPrimed))) {
			
			ArenaModuleManager.onEntityExplode(arena, event);
			return;
		}

		event.setCancelled(true); // ELSE => cancel event
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		Entity p1 = event.getEntity();

		if ((p1 == null) || (!(p1 instanceof Player)))
			return; // no player

		Arena arena = ArenaPlayer.parsePlayer(((Player) p1).getName()).getArena();
		if (arena == null)
			return;

		db.i("onEntityRegainHealth => fighing player");
		if (!arena.isFightInProgress()) {
			return;
		}

		Player player = (Player) p1;

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		ArenaTeam team = ap.getArenaTeam();

		if (team == null) {
			return;
		}

		
		ArenaModuleManager.onEntityRegainHealth(arena, event);

	}

	/**
	 * parsing of damage: Entity vs Entity
	 * 
	 * @param event
	 *            the triggering event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
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

		Arena arena = ArenaPlayer.parsePlayer(((Player) p2).getName()).getArena();
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
		
		if (attacker.equals(defender)) {
			// player attacking himself. ignore!
			return;
		}

		boolean defTeam = false;
		boolean attTeam = false;
		ArenaPlayer apDefender = ArenaPlayer.parsePlayer(defender.getName());
		ArenaPlayer apAttacker = ArenaPlayer.parsePlayer(attacker.getName());

		for (ArenaTeam team : arena.getTeams()) {
			defTeam = defTeam ? true : team.getTeamMembers().contains(
					apDefender);
			attTeam = attTeam ? true : team.getTeamMembers().contains(
					apAttacker);
		}

		if (!defTeam || !attTeam || arena.REALEND_ID != null) {
			event.setCancelled(true);
			return;
		}

		db.i("both players part of the arena");

		if (PVPArena.instance.getConfig().getBoolean("onlyPVPinArena")) {
			event.setCancelled(false); // uncancel events for regular no PVP
			// servers
		}

		if ((!arena.getArenaConfig().getBoolean(CFG.PERMS_TEAMKILL))
				&& (apAttacker.getArenaTeam()).equals(apDefender.getArenaTeam())) {
			// no team fights!
			db.i("team hit, cancel!");
			event.setCancelled(true);
			return;
		}

		if (!arena.isFightInProgress() || (arena.PVP_ID != null)) {
			// fight not started, cancel!
			event.setCancelled(true);
			return;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new DamageResetRunnable(arena, attacker, defender), 1L);

		if (arena.getArenaConfig().getInt(CFG.PROTECT_SPAWN) > 0) {
			if (SpawnManager.isNearSpawn(arena, defender,
					arena.getArenaConfig().getInt(CFG.PROTECT_SPAWN))) {
				// spawn protection!
				db.i("spawn protection! damage cancelled!");
				event.setCancelled(true);
				return;
			}
		}

		// here it comes, process the damage!

		db.i("processing damage!");


		
		ArenaModuleManager.onEntityDamageByEntity(arena, attacker,
				defender, event);

		StatisticsManager.damage(arena, attacker, defender, event.getDamage());
	}
	

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		Entity p2 = event.getEntity();

		db.i("onEntityDamage: cause: " + event.getCause().name()
				+ " : " + event.getEntity().toString());

		if ((p2 == null) || (!(p2 instanceof Player))) {
			return;
		}

		Arena arena = ArenaPlayer.parsePlayer(((Player) p2).getName()).getArena();
		if (arena == null) {
			// defender no arena player => out
			return;
		}

		Player defender = (Player) p2;

		ArenaPlayer apDefender = ArenaPlayer.parsePlayer(defender.getName());

		if (arena.REALEND_ID != null || (!apDefender.getStatus().equals(Status.NULL) && !apDefender.getStatus().equals(Status.FIGHT))) {
			event.setCancelled(true);
			return;
		}
	}
	

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {

		db.i("onPotionSplash");
		boolean affectTeam = true;
		
		Collection<PotionEffect> pot = event.getPotion().getEffects();
		for (PotionEffect eff : pot) {
			if (teamEffect.containsKey(eff.getType())) {
				affectTeam = teamEffect.get(eff.getType());
				break;
			}
		}
		
		ArenaPlayer ap = null;
			
		try {
			ap = ArenaPlayer.parsePlayer(((Player) event.getEntity().getShooter()).getName());
		} catch (Exception e) {
			return;
		}
		
		db.i("legit player: " + ap);
		
		if (ap == null || ap.getArena() == null || !ap.getStatus().equals(Status.FIGHT)) {
			db.i("something is null!");
			return;
		}
		
		Collection<LivingEntity> entities = event.getAffectedEntities();
		for (LivingEntity e : entities) {
			if (!(e instanceof Player)) {
				continue;
			}
			ArenaPlayer p = ArenaPlayer.parsePlayer(((Player) e).getName());
			boolean sameTeam = p.getArenaTeam().equals(ap.getArenaTeam());
			if (sameTeam != affectTeam) {
				// different team and only team should be affected
				// same team and the other team should be affected
				// ==> cancel!
				event.setIntensity(e, 0);
			}
		}
	}
}