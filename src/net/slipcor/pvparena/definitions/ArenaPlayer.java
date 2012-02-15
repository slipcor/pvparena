package net.slipcor.pvparena.definitions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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
 * @version v0.6.2
 * 
 */

public class ArenaPlayer {
	private Player player = null;
	public String team = "";
	public String aClass = "";
	public String respawn = "";
	public boolean telePass = false;
	public status state = status.FIGHT;

	public static enum status {
		FIGHT, DEAD, SPECTATE;
	}

	public HashSet<PermissionAttachment> tempPermissions = new HashSet<PermissionAttachment>();

	public ItemStack[] savedInventory;
	public ItemStack[] savedArmor;
	public float exhaustion;
	public int fireticks;
	public int foodlevel;
	public int health;
	public int gamemode;
	public float saturation;
	public Location location;
	public String displayname;
	public boolean dead = false;

	public int losses = 0;
	public int wins = 0;
	public int kills = 0;
	public int deaths = 0;
	public int damage = 0;
	public int maxdamage = 0;
	public int damagetake = 0;
	public int maxdamagetake = 0;

	/**
	 * create a PVP Arena player istance
	 * 
	 * @param p
	 *            the bukkit player
	 */
	public ArenaPlayer(Player p) {
		player = p;

		YamlConfiguration cfg = new YamlConfiguration();
		try {
			cfg.load("plugins/pvparena/players.yml");

			losses = cfg.getInt(p.getName() + ".losses", 0);
			wins = cfg.getInt(p.getName() + ".wins", 0);
			kills = cfg.getInt(p.getName() + ".kills", 0);
			deaths = cfg.getInt(p.getName() + ".deaths", 0);
			damage = cfg.getInt(p.getName() + ".damage", 0);
			damagetake = cfg.getInt(p.getName() + ".damagetake", 0);
			maxdamage = cfg.getInt(p.getName() + ".maxdamage", 0);
			maxdamagetake = cfg.getInt(p.getName() + ".maxdamagetake", 0);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
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

	public void destroy() {
		YamlConfiguration cfg = new YamlConfiguration();
		try {
			String file = "plugins/pvparena/players.yml";
			cfg.load(file);

			cfg.set(player.getName() + ".losses", losses);
			cfg.set(player.getName() + ".wins", wins);
			cfg.set(player.getName() + ".kills", kills);
			cfg.set(player.getName() + ".deaths", deaths);
			cfg.set(player.getName() + ".damage", damage);
			cfg.set(player.getName() + ".maxdamage", maxdamage);
			cfg.set(player.getName() + ".damagetake", damagetake);
			cfg.set(player.getName() + ".maxdamagetake", maxdamagetake);

			cfg.save(file);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}
