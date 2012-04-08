package net.slipcor.pvparena.arena;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.slipcor.pvparena.classes.Effect;
import net.slipcor.pvparena.core.Debug;

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
 * @version v0.7.0
 * 
 */

public class ArenaPlayer {
	private Debug db = new Debug(14);
	private Player player = null;
	private final String name;
	private Arena arena;
	private ArenaClass aClass;
	private PlayerState state;
	private final List<Effect> effects;

	
	public ItemStack[] savedInventory;
	public ItemStack[] savedArmor;

	public Location location;
	
	public String respawn = "";
	public boolean telePass = false;

	public HashSet<PermissionAttachment> tempPermissions = new HashSet<PermissionAttachment>();

	private boolean spectator = false;
	public boolean ready = false;

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
	public ArenaPlayer(Player p, Arena a) {
		db.i("creating arena player: " + p.getName());

		this.name = p.getName();
		this.setArena(a);
		this.effects = new ArrayList<Effect>();
		this.player = p;

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

	/**
	 * save and destroy a player instance
	 */
	public void destroy() {
		db.i("destroying arena player " + player.getName());
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
		if (player.isDead()) {
			return;
		}

		respawn = "";
		telePass = false;
		
		if (state != null) {
			state.reset();
		}
		location = null;
		savedInventory = null;
		savedArmor = null;
		
		spectator = false;
		ready = false;
		if (arena != null) {
			ArenaTeam team = arena.getTeam(this);
			if (team != null) {
				team.remove(this);
			}
			arena.removePlayer(this);
		}
		arena = null;
	}

	public String getName() {
		return name;
	}

	public Arena getArena() {
		return arena;
	}

	public void setArena(Arena arena) {
		this.arena = arena;
	}

	public ArenaClass getaClass() {
		return aClass;
	}

	public void setArenaClass(ArenaClass aClass) {
		this.aClass = aClass;
	}

	public PlayerState getState() {
		return state;
	}
	
	public void createState(Player player) {
		state = new PlayerState(player);
		location = player.getLocation();
	}

	public List<Effect> getEffects() {
		return effects;
	}
	
	public void addEffect(Effect effect) {
		effects.add(effect);
	}
	
	public boolean isSpectator() {
		return spectator;
	}

	public void setSpectator(boolean spectator) {
		this.spectator = spectator;
	}

	public final class EffectScheduler implements Runnable {
		// one runnable for every player.. mhh... yeah... that might improve it ^^
		@Override
		public void run() {
			List<Effect> removalPending = new ArrayList<Effect>();
			
			for (Effect effect : effects) {
				effect.tick();
				if (effect.expired())
					removalPending.add(effect);
			}
			
			effects.removeAll(removalPending);
		}
	}
}
