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
	public String team = "";
	public String aClass = "";
	private String respawn = null;
	public boolean telePass = false;
	
	public HashSet<PermissionAttachment> tempPermissions = new HashSet<PermissionAttachment>();
	
	public ItemStack[] savedInventory;
	public ItemStack[] savedArmor;
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
	public Player get() {
		return player;
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
		respawn = set ? aClass : null;
	}
}
