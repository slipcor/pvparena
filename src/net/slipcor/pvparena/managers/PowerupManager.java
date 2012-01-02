package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.slipcor.pvparena.powerups.Powerup;

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
 * @version v0.4.0
 * 
 */

public class PowerupManager {
	public HashMap<Player, Powerup> puActive = new HashMap<Player, Powerup>();
	public List<Powerup> puTotal = new ArrayList<Powerup>();
	private DebugManager db = new DebugManager();

	/**
	 * construct a powerup manager instance
	 * 
	 * @param powerUps
	 *            the powerups to add
	 */
	@SuppressWarnings("unchecked")
	public PowerupManager(HashMap<String, Object> powerUps) {
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