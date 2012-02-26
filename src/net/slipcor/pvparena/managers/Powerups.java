package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Powerup;

import org.bukkit.entity.Player;

/**
 * powerup manager class
 * 
 * -
 * 
 * hands over all active or general powerups
 * 
 * @author slipcor
 * 
 * @version v0.6.15
 * 
 */

public class Powerups {
	public HashMap<Player, Powerup> puActive = new HashMap<Player, Powerup>();
	public List<Powerup> puTotal = new ArrayList<Powerup>();
	private Debug db = new Debug(32);

	/**
	 * construct a powerup manager instance
	 * 
	 * @param powerUps
	 *            the powerups to add
	 */
	@SuppressWarnings("unchecked")
	public Powerups(HashMap<String, Object> powerUps) {
		// receives the config map, adds all plugins
		db.i("initialising powerupmanager");
		Powerup p;
		for (String pName : powerUps.keySet()) {
			db.i("reading powerUps");
			p = new Powerup(pName,
					(HashMap<String, Object>) powerUps.get(pName));
			puTotal.add(p);
		}
	}

	/**
	 * trigger all powerups
	 */
	public void tick() {
		for (Powerup p : puActive.values()) {
			if (p.canBeTriggered()) {
				p.tick();
			}
		}
	}
}