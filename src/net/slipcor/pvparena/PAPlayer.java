package net.slipcor.pvparena;

import org.bukkit.entity.Player;

/**
 * player class
 * 
 * -
 * 
 * contains player methods and variables for quicker access
 * 
 * @author slipcor
 * 
 * @version v0.5.3
 * 
 */

public class PAPlayer {
	private Player player = null;
	private String team = "";
	private String fightClass = "";
	private String respawn = null;
	private Byte lives = 0;
	private boolean telePass = false;

	/**
	 * create a PVP Arena player istance
	 * 
	 * @param p
	 *            the bukkit player
	 */
	public PAPlayer(Player p) {
		player = p;
	}

	/**
	 * return the PVP Arena bukkit player
	 * 
	 * @return the bukkit player instance
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * return the PVP Arena player team
	 * 
	 * @return the team name
	 */
	public String getTeam() {
		return team;
	}

	/**
	 * set the PVP Arena player team
	 * 
	 * @param s
	 *            the team name
	 */
	public void setTeam(String s) {
		team = s;
	}

	/**
	 * get the PVP Arena player class
	 * 
	 * @return the class name
	 */
	public String getFightClass() {
		return fightClass;
	}

	/**
	 * set the PVP Arena player class
	 * 
	 * @param s
	 *            the class name
	 */
	public void setClass(String s) {
		fightClass = s;
	}

	/**
	 * get the PVP Arena player respawn coord
	 * 
	 * @return the coord name
	 */
	public String getRespawn() {
		return respawn;
	}

	/**
	 * set the PVP Arena player respawn coord
	 * 
	 * @param set
	 *            the coord name
	 */
	public void setRespawn(boolean set) {
		respawn = set ? fightClass : null;
	}

	/**
	 * get the PVP Arena player lives
	 * 
	 * @return the lives
	 */
	public byte getLives() {
		return lives;
	}

	/**
	 * set the PVP Arena player lives
	 * 
	 * @param l
	 *            the lives
	 */
	public void setLives(byte l) {
		lives = l;
	}

	/**
	 * set the teleport pass
	 * 
	 * @param b
	 *            the teleport pass
	 */
	public void setTelePass(boolean b) {
		telePass = b;
	}

	/**
	 * get the teleport pass
	 * 
	 * @return true if may pass, false otherwise
	 */
	public boolean getTelePass() {
		return telePass;
	}
}
