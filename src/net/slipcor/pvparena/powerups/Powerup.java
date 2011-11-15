package net.slipcor.pvparena.powerups;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import net.slipcor.pvparena.managers.DebugManager;
import net.slipcor.pvparena.powerups.PowerupEffect.classes;

/*
 * powerup class
 * 
 * author: slipcor
 * 
 * version: v0.3.8 - BOSEconomy, rewrite
 * 
 * history:
 *
 *     v0.3.6 - CTF Arena
 *     v0.3.5 - Powerups!!
 */

public class Powerup {
	public String name;   // PowerUp display name
	PowerupEffect[] effects; // Effects the Powerup has
	public Material item; // item that triggers this Powerup
	DebugManager db = new DebugManager();
	
	/*
	 * Powerup constructor
	 * 
	 * initiate Powerup, load PowerupEffects belonging to it
	 */
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
	
	/*
	 * Powerup constructor II
	 * 
	 * simply handing over everything
	 */
	public Powerup(Powerup p) {
		this.name = p.name;
		this.effects = p.effects;
		this.item = p.item;
	}
	
	/*
	 * return "is Powerup active"
	 */
	public boolean isActive() {
		for (PowerupEffect pe : effects) {
			if (pe.active)
				return true;
		}
		return false;
	}
	
	/*
	 * return "can Powerup still be fired"
	 */
	public boolean canBeTriggered() {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				return true; // one effect still can be triggered
		}
		return false;
	}

	/*
	 * initiate Powerup effects
	 */
	public void activate(Player player) {
		db.i("activating! - " + name);
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				pe.init(player);
		}
	}

	/*
	 * activate Powerup effects
	 */
	public void commit(Player attacker, Player defender,
			EntityDamageByEntityEvent event) {

		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				pe.commit(attacker, defender, event);
		}
	}

	/*
	 * return "is Powerup active"
	 */
	public boolean active(classes peClass) {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				if (pe.type.equals(peClass))
					return true;
		}
		return false;
	}

	/*
	 * commit all PowerupEffects
	 */
	public void commit(EntityRegainHealthEvent event) {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				if (pe.type.equals(classes.HEAL))
					pe.commit(event);
		}
	}

	/*
	 * commit all PowerupEffects
	 */
	public void commit(PlayerVelocityEvent event) {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				if (pe.type.equals(classes.HEAL))
					pe.commit(event);
		}
	}

	/*
	 * tick...
	 */
	public void tick() {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration > 0)
				pe.duration--;
		}
	}

	/*
	 * disable all PowerupEffects
	 */
	public void disable() {
		for (PowerupEffect pe : effects) {
			pe.uses = 0;
			pe.duration = 0;
			pe = null;
		}
	}
}