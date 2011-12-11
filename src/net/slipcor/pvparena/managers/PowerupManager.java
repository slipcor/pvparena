/*
 * custom runnable class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.6 - CTF Arena
 *     v0.3.4 - Powerups!!
 */

package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.slipcor.pvparena.powerups.Powerup;

import org.bukkit.entity.Player;

public class PowerupManager {
	public HashMap<Player, Powerup> puActive = new HashMap<Player, Powerup>();
	public List<Powerup> puTotal = new ArrayList<Powerup>();
	private DebugManager db = new DebugManager();

	/*
	 * PowerupManager constructor
	 * 
	 * read all powerup settings
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

	/*
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