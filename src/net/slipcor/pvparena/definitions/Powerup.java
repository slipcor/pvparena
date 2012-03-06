package net.slipcor.pvparena.definitions;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.PowerupEffect.classes;

/**
 * powerup class
 * 
 * -
 * 
 * contains basic powerup methods and variables
 * 
 * @author slipcor
 * 
 * @version v0.6.15
 * 
 */

public class Powerup {
	public String name; // PowerUp display name
	public Material item; // item that triggers this Powerup
	private PowerupEffect[] effects; // Effects the Powerup has
	private Debug db = new Debug(16);

	/**
	 * construct a powerup instance
	 * 
	 * @param pName
	 *            the powerup name
	 * @param puEffects
	 *            the powerup effects
	 */
	@SuppressWarnings("unchecked")
	public Powerup(String pName, HashMap<String, Object> puEffects) {
		int count = 0;
		this.name = pName;
		db.i("creating powerup " + pName);
		this.item = Material.valueOf((String) puEffects.get("item"));
		db.i("item added: " + this.item.toString());
		for (String eClass : puEffects.keySet()) {
			PowerupEffect.classes pec = PowerupEffect.parseClass(eClass);
			if (pec == null) {
				if (!eClass.equals("item"))
					db.w("unknown effect class: " + eClass);
				continue;
			}
			PowerupEffect pe = new PowerupEffect(eClass,
					(HashMap<String, Object>) puEffects.get(eClass), PowerupEffect.parsePotionEffect(eClass));
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
			PowerupEffect pe = new PowerupEffect(eClass,
					(HashMap<String, Object>) puEffects.get(eClass), PowerupEffect.parsePotionEffect(eClass));
			if (pe.type == null) {
				continue;
			}
			effects[count++] = pe;
		}
	}

	/**
	 * second constructor, referencing instead of creating
	 * 
	 * @param p
	 */
	public Powerup(Powerup p) {
		this.name = p.name;
		this.effects = p.effects;
		this.item = p.item;
	}

	/**
	 * check if a powerup has active effects
	 * 
	 * @return true if an effect still is active, false otherwise
	 */
	public boolean isActive() {
		for (PowerupEffect pe : effects) {
			if (pe.active)
				return true;
		}
		return false;
	}

	/**
	 * check if a powerup running effect is running
	 * 
	 * @param peClass
	 *            the class to check
	 * @return true if an effect still is active, false otherwise
	 */
	public boolean isEffectActive(classes peClass) {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				if (pe.type.equals(peClass))
					return true;
		}
		return false;
	}

	/**
	 * check if any effect can be fired
	 * 
	 * @return true if an event can be fired, false otherwise
	 */
	public boolean canBeTriggered() {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				return true; // one effect still can be triggered
		}
		return false;
	}

	/**
	 * initiate Powerup effects
	 * 
	 * @param player
	 *            the player to commit the effect on
	 */
	public void activate(Player player) {
		db.i("activating! - " + name);
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				pe.init(player);
		}
	}

	/**
	 * commit PowerupEffect in combat
	 * 
	 * @param attacker
	 *            the attacking player to access
	 * @param defender
	 *            the defending player to access
	 * @param event
	 *            the triggering event
	 */
	public void commit(Player attacker, Player defender,
			EntityDamageByEntityEvent event) {

		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				pe.commit(attacker, defender, event);
		}
	}

	/**
	 * commit all PowerupEffects
	 * 
	 * @param event
	 *            the triggering event
	 */
	public void commit(EntityRegainHealthEvent event) {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				if (pe.type.equals(classes.HEAL))
					pe.commit(event);
		}
	}

	/**
	 * commit all PowerupEffects
	 * 
	 * @param event
	 *            the triggering event
	 */
	public void commit(PlayerVelocityEvent event) {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration != 0)
				if (pe.type.equals(classes.HEAL))
					pe.commit(event);
		}
	}

	/**
	 * calculate down the duration
	 */
	public void tick() {
		for (PowerupEffect pe : effects) {
			if (pe.uses != 0 && pe.duration > 0)
				pe.duration--;
		}
	}

	/**
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