package net.slipcor.pvparena.definitions;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

/**
 * player class
 * 
 * -
 * 
 * contains player methods and variables for quicker access
 * 
 * @author slipcor
 * 
 * @version v0.6.1
 * 
 */

public class ArenaPlayer {
	private Player player = null;
	private String team = "";
	private String fightClass = "";
	private String respawn = null;
	private boolean telePass = false;
	public HashSet<PermissionAttachment> tempPermissions = new HashSet<PermissionAttachment>();
	public ItemStack[] savedInventories;
	public ItemStack[] savedArmories;
	public float exhaustion;
	public int fireticks;
	public int foodlevel;
	public int health;
	public float saturation;
	public Location location;
	public int gamemode;
	public String displayname;
	public boolean dead = false;

	/**
	 * create a PVP Arena player istance
	 * 
	 * @param p
	 *            the bukkit player
	 */
	public ArenaPlayer(Player p) {
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
