/*
 * player class
 * 
 * author: slipcor
 * 
 * version: v0.4.1 - command manager, arena information and arena config check
 * 
 * history:
 * 
 *     v0.4.0 - mayor rewrite, improved help
 */

package net.slipcor.pvparena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PAPlayer {
	private Player player = null;
	private String team = "";
	private String fightClass = "";
	private String respawn = null;
	private Location location = null;
	private Byte lives = 0;
	private boolean telePass = false;
	
	public PAPlayer(Player p) {
		player = p;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public String getName() {
		return player.getName();
	}
	
	public String getTeam() {
		return team;
	}

	public void setTeam(String s) {
		team = s;
	}

	public String getFightClass() {
		return fightClass;
	}

	public void setClass(String s) {
		fightClass = s;
	}

	public String getRespawn() {
		return respawn;
	}

	public void setRespawn(boolean set) {
		respawn = set?fightClass:null;
	}

	public Location getSignLocation() {
		return location;
	}

	public void setSignLocation(Location l) {
		location = l;
	}

	public byte getLives() {
		return lives;
	}

	public void setLives(byte l) {
		lives = l;
	}
	
	public void setTelePass(boolean b) {
		telePass = b;
	}
	
	public boolean getTelePass() {
		return telePass;
	}
}
