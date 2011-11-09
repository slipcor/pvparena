package net.slipcor.pvparena.powerups;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import net.slipcor.pvparena.PVPArenaPlugin;
import net.slipcor.pvparena.managers.DebugManager;
import net.slipcor.pvparena.powerups.PowerupEffect.classes;

/*
 * powerup class
 * 
 * author: slipcor
 * 
 * version: v0.3.6 - CTF Arena
 * 
 * history:
 * 
 *     v0.3.5 - Powerups!!
 */

public class Powerup {
	public String name;   // PowerUp display name
	PowerupEffect[] effects; // Effects the Powerup has
	public Material item; // item that triggers this Powerup
	DebugManager db = new DebugManager();
	
	@SuppressWarnings("unchecked")
	public Powerup(String pName, HashMap<String, Object> puEffects) {
		int count = 0;
		this.name = pName;
		db.i("creating powerup "+pName);
		this.item = Material.valueOf((String) puEffects.get("item"));
		db.i("item added: " + this.item.toString());
		for (String eClass : puEffects.keySet()) {
			PowerupEffect.classes pec = PowerupEffect.parseClass(eClass);
			if (pec == null) {
				if (!eClass.equals("item"))
					db.w("unknown effect class: " + eClass);
				continue;
			}
			PowerupEffect pe = new PowerupEffect(eClass, (HashMap<String, Object>) puEffects.get(eClass));
			if (pe.type == null) {
				continue;
			}
			count++;
		}
		db.i("effects found: " + count);
		if (count < 1)
			return;
		
		effects = new PowerupEffect[count];

		count = 0;
		for (String eClass : puEffects.keySet()) {
			PowerupEffect.classes pec = PowerupEffect.parseClass(eClass);
			if (pec == null) {
				continue;
			}
			PowerupEffect pe = new PowerupEffect(eClass, (HashMap<String, Object>) puEffects.get(eClass));
			if (pe.type == null) {
				continue;
			}
			effects[count++] = pe;
		}
	}
	
	public Powerup(Powerup p) {
		this.name = p.name;
		this.effects = p.effects;
		this.item = p.item;
	}
	
	public boolean isActive() {
		for (PowerupEffect pe : effects) {
			if (pe.active)
				return true;
		}
		return false;
	}
	
	public boolean canBeTriggered() {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				return true; // one effect still can be triggered
		}
		return false;
	}

	public void activate(Player player) {
		db.i("activating! - " + name);
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				pe.init(player);
		}
	}

	public void commit(Player attacker, Player defender,
			EntityDamageByEntityEvent event) {

		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				pe.commit(attacker, defender, event);
		}
	}

	public boolean active(classes peClass) {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				if (pe.type.equals(peClass))
					return true;
		}
		return false;
	}

	public void commit(EntityRegainHealthEvent event) {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				if (pe.type.equals(classes.HEAL))
					pe.commit(event);
		}
	}

	public void commit(PlayerVelocityEvent event) {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				if (pe.type.equals(classes.HEAL))
					pe.commit(event);
		}
	}

	public void tick() {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration > 0)
				pe.duration--;
		}
	}

	public void deactivate() {
		for (PowerupEffect pe : effects) {
			pe.uses = 0;
			pe.duration = 0;
			pe = null;
		}
	}
}
/*
 * example config layout
 * 
 * Shield:
 *     dmg_receive:
 *         factor: 0.6
 * Minions:
 *     spawn_mob:
 *         type: skeleton
 *         health: 2.0
 *     spawn_mob:
 *         type: skeleton
 *         duration: 10s
 * Sprint:
 *     sprint:
 *         duration: 10s
 * QuadDamage:
 *     dmg_cause:
 *         factor: 4
 *         duration: 10s
 * Dodge:
 *     dmg_receive:
 *         chance: 0.2
 *         factor: 0
 *         duration: 5s
 * Reflect:
 *     dmg_reflect:
 *         chance: 0.5
 *         factor: 0.3
 *         uses: 5
 * Ignite:
 *     ignite:
 *         chance: 0.66
 *         duration: 10s
 * IceBlock:
 *     freeze:
 *         duration: 8s
 *     dmg_receive:
 *         factor: 0
 *         duration: 8s
 * Invulnerability:
 *     dmg_receive:
 *         factor:0
 *         duration 5s
 * OneUp:
 *     lives:
 *         diff: 1
 * Death:
 *     lives:
 *         diff: -1
 * Slippery:
 *     slip:
 *         duration: 10s
 * Dizzyness:
 *     dizzy:
 *         duration: 10s
 * Rage:
 *     dmg_cause:
 *         factor: 1.5
 *         chance: 0.8
 *         duration: 5s
 *     dmg_cause:
 *         factor: 0
 *         chance: 0.2
 *         duration: 5s
 * Berserk:
 *     dmg_cause:
 *         factor: 1.5
 *         duration: 5s
 *     dmg_receive:
 *         dactor: 1.5
 *         duration: 5s
 * Healing:
 *     heal:
 *         factor: 1.5
 *         duration: 10s
 * Heal:
 *     health:
 *         diff: 3
 * Repair:
 *     repair:
 *         item: helmet
 *         item: chestplate
 *         item: leggins
 *         item: boots
 *         factor: 0.2
 * 
 */