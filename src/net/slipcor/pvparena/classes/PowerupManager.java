package net.slipcor.pvparena.classes;

import java.util.ArrayList;
import java.util.List;


/**
 * PowerupManager Class
 * 
 * -
 * 
 * Manages powerups
 * 
 * @author NodinChan
 * @slipcor approves
 *
 */

public class PowerupManager {
	
	private List<Powerup> powerups;
	
	public PowerupManager() {
		this.powerups = new ArrayList<Powerup>();
	}
	
	public Powerup getPowerup(String name) {
		for (Powerup powerup : powerups) {
			if (powerup.getName().equalsIgnoreCase(name))
				return powerup;
		}
		
		return null;
	}
	
	public void register(Powerup powerup) {
		powerups.add(powerup);
	}
	
	public final class PowerupScheduler implements Runnable {
		
		@Override
		public void run() {
			for (Powerup powerup : powerups) {
				powerup.tick();
			}
		}
	}
}