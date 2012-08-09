package net.slipcor.pvparena.arena;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.managers.Teams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
 * @version v0.8.12
 * 
 */

public class ArenaPlayer {
	private static Debug db = new Debug(14);
	private String sPlayer = null;
	private final String name;
	private Arena arena;
	private ArenaClass aClass;
	private PlayerState state;

	public ItemStack[] savedInventory;
	public ItemStack[] savedArmor;

	public Location location;

	// public String respawn = "";
	public boolean telePass = false;

	public HashSet<PermissionAttachment> tempPermissions = new HashSet<PermissionAttachment>();
	private static HashMap<String, ArenaPlayer> totalPlayers = new HashMap<String, ArenaPlayer>();

	private Status status = Status.EMPTY;
	
	/**
	 *  - EMPTY = not part of an arena
	 *  - WARM = not part of an arena, warmed up
	 *  - LOBBY = inside an arena lobby mode
	 *  - READY = inside an arena lobby mode, readied up
	 *  - FIGHT = fighting inside an arena
	 *  - WATCH = watching a fight from the spectator area
	 *  - DEATH = dead and soon respawning
	 *  - LOSES = lost and thus spectating 
	 * @author slipcor
	 */
	public static enum Status {EMPTY, WARM, LOBBY, READY, FIGHT, WATCH, DEATH, LOSES}

	public int losses = 0;
	public int wins = 0;
	public int kills = 0;
	public int deaths = 0;
	public int damage = 0;
	public int maxdamage = 0;
	public int damagetake = 0;
	public int maxdamagetake = 0;

	public int totlosses = 0;
	public int totwins = 0;
	public int totkills = 0;
	public int totdeaths = 0;
	public int totdamage = 0;
	public int totmaxdamage = 0;
	public int totdamagetake = 0;
	public int totmaxdamagetake = 0;

	public static HashMap<ArenaPlayer, String> deadPlayers = new HashMap<ArenaPlayer, String>();

	/**
	 * create a PVP Arena player istance
	 * 
	 * @param p
	 *            the bukkit player
	 * @param a
	 *            arena instance
	 */
	public ArenaPlayer(Player p, Arena a) {
		db.i("creating arena player: " + p.getName());

		this.name = p.getName();
		this.setArena(a);
		this.sPlayer = p.getName();

		YamlConfiguration cfg = new YamlConfiguration();
		try {
			cfg.load(PVPArena.instance.getDataFolder() + "/players.yml");

			totlosses = cfg.getInt(p.getName() + ".losses", 0);
			totwins = cfg.getInt(p.getName() + ".wins", 0);
			totkills = cfg.getInt(p.getName() + ".kills", 0);
			totdeaths = cfg.getInt(p.getName() + ".deaths", 0);
			totdamage = cfg.getInt(p.getName() + ".damage", 0);
			totdamagetake = cfg.getInt(p.getName() + ".damagetake", 0);
			totmaxdamage = cfg.getInt(p.getName() + ".maxdamage", 0);
			totmaxdamagetake = cfg.getInt(p.getName() + ".maxdamagetake", 0);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * add a kill to a player
	 */
	public void addKill() {
		kills++;
	}

	/**
	 * add a death to a player
	 */
	public void addDeath() {
		deaths++;
	}

	/**
	 * add a dead player to the dead player map
	 * 
	 * @param location
	 *            the location to respawn
	 */
	public void addDeadPlayer(String string) {
		deadPlayers.put(this, string);
	}

	/**
	 * save the player state
	 * 
	 * @param player
	 *            the player to save
	 */
	public void createState(Player player) {
		state = new PlayerState(player);
		location = player.getLocation();
	}

	/**
	 * return the PVP Arena bukkit player
	 * 
	 * @return the bukkit player instance
	 */
	public Player get() {
		return Bukkit.getPlayerExact(sPlayer);
	}

	/**
	 * return the arena class
	 * 
	 * @return the arena class
	 */
	public ArenaClass getaClass() {
		return aClass;
	}

	/**
	 * return the arena
	 * 
	 * @return the arena
	 */
	public Arena getArena() {
		return arena;
	}

	/**
	 * hand over a player's deaths
	 * 
	 * @return the player's death count
	 */
	public int getDeaths() {
		return deaths;
	}

	/**
	 * hand over a player's kills
	 * 
	 * @return the player's kill count
	 */
	public int getKills() {
		return kills;
	}

	/**
	 * try to find the last damaging player
	 * 
	 * @param eEvent
	 *            the Event
	 * @return the player instance if found, null otherwise
	 */
	public static Player getLastDamagingPlayer(Event eEvent) {
		db.i("trying to get the last damaging player");
		if (eEvent instanceof EntityDamageByEntityEvent) {
			db.i("there was an EDBEE");
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) eEvent;

			Entity p1 = event.getDamager();

			if (event.getCause() == DamageCause.PROJECTILE && p1 instanceof Projectile) {
				p1 = ((Projectile) p1).getShooter();
				db.i("killed by projectile, shooter is found");
			}

			if (event.getEntity() instanceof Wolf) {
				Wolf wolf = (Wolf) event.getEntity();
				if (wolf.getOwner() != null) {
					try {
						p1 = (Entity) wolf.getOwner();
						db.i("tamed wolf is found");
					} catch (Exception e) {
						// wolf belongs to dead player or whatnot
					}
				}
			}

			if (p1 instanceof Player) {
				db.i("it was a player!");
				return (Player) p1;
			}
		}
		db.i("last damaging player is null");
		db.i("last damaging event: " + eEvent.getEventName());
		return null;
	}

	/**
	 * return the player name
	 * 
	 * @return the player name
	 */
	public String getName() {
		return name;
	}

	/**
	 * return the player state
	 * 
	 * @return the player state
	 */
	public PlayerState getState() {
		return state;
	}

	public Status getStatus() {
		return status;
	}

	/**
	 * hand over a player's tele pass
	 * 
	 * @return true if may pass, false otherwise
	 */
	public boolean getTelePass() {
		return telePass;
	}

	/**
	 * has a player died in the arena?
	 * 
	 * @return true if the player has died, false otherwise
	 */
	public boolean isDead() {
		return deadPlayers.containsKey(this);
	}

	/**
	 * get an ArenaPlayer from a player
	 * 
	 * @param player
	 *            the player to get
	 * @return an ArenaPlayer instance belonging to that player
	 */
	public static synchronized ArenaPlayer parsePlayer(Player player) {
		if (totalPlayers.get(player.getName()) == null) {
			totalPlayers.put(player.getName(), new ArenaPlayer(player, null));
		}
		return totalPlayers.get(player.getName());
	}
	
	public static int countPlayers() {
		return totalPlayers.size();
	}
	
	public static HashSet<ArenaPlayer> getPlayers() {
		HashSet<ArenaPlayer> ps = new HashSet<ArenaPlayer>();
		for (ArenaPlayer ap : totalPlayers.values()) {
			ps.add(ap);
		}
		return ps;
	}

	/**
	 * save and reset a player instance
	 * @param b should
	 */
	public void reset() {
		db.i("destroying arena player " + sPlayer);
		YamlConfiguration cfg = new YamlConfiguration();
		try {
			String file = PVPArena.instance.getDataFolder().toString()
					+ "/players.yml";
			cfg.load(file);

			cfg.set(sPlayer + ".losses", losses + totlosses);
			cfg.set(sPlayer + ".wins", wins + totwins);
			cfg.set(sPlayer + ".kills", kills + totkills);
			cfg.set(sPlayer + ".deaths", deaths + totdeaths);
			cfg.set(sPlayer + ".damage", damage + totdamage);
			cfg.set(sPlayer + ".maxdamage", maxdamage + totmaxdamage);
			cfg.set(sPlayer + ".damagetake", damagetake + totdamagetake);
			cfg.set(sPlayer + ".maxdamagetake", maxdamagetake + totmaxdamagetake);

			cfg.save(file);

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (get() == null || get().isDead()) {
			return;
		}

		telePass = false;

		if (state != null) {
			state.reset();
			state = null;
		}
		location = null;
		//savedInventory = null;
		//savedArmor = null;

		setStatus(Status.EMPTY);

		if (arena != null) {
			ArenaTeam team = Teams.getTeam(arena, this);
			if (team != null) {
				team.remove(this);
			}
		}
		arena = null;
		aClass = null;
	}

	/**
	 * set the player's arena
	 * 
	 * @param arena
	 *            the arena to set
	 */
	public void setArena(Arena arena) {
		this.arena = arena;
	}

	/**
	 * set the player's arena class
	 * 
	 * @param aClass
	 *            the arena class to set
	 */
	public void setArenaClass(ArenaClass aClass) {
		this.aClass = aClass;
	}

	/**
	 * hand over a player class name
	 * 
	 * @param s
	 *            a player class name
	 */
	public void setClass(String s) {

		for (ArenaClass ac : getArena().getClasses()) {
			if (ac.getName().equalsIgnoreCase(s)) {
				setArenaClass(ac);
				return;
			}
		}
		System.out.print("[PA-debug] failed to set unknown class " + s
				+ " to player " + name);
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * hand over a player's tele pass
	 * 
	 * @param b
	 *            true if may pass, false otherwise
	 */
	public void setTelePass(boolean b) {
		telePass = b;
	}

	public void dump() {
		File f = new File(PVPArena.instance.getDataFolder().getPath() + "/dumps/" + this.name + ".yml");
		try {
			f.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		YamlConfiguration cfg = new YamlConfiguration();
		cfg.set("arena", arena.name);
		if (state != null) {
			state.dump(cfg);
		}

		cfg.set("inventory", StringParser.getStringFromItemStacks(savedInventory));
		cfg.set("armor", StringParser.getStringFromItemStacks(savedArmor));
		
		try {
			cfg.save(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readDump() {
		File f = new File(PVPArena.instance.getDataFolder().getPath() + "/dumps/" + this.name + ".yml");
		if (!f.exists()) {
			return;
		}
		
		YamlConfiguration cfg = new YamlConfiguration();
		try {
			cfg.load(f);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		arena = Arenas.getArenaByName(cfg.getString("arena"));
		savedInventory = StringParser.getItemStacksFromString(cfg.getString("inventory", "AIR"));
		savedArmor = StringParser.getItemStacksFromString(cfg.getString("armor", "AIR"));

		if (arena != null) {
			location = Spawns.getCoords(arena, "exit");
			
			state = PlayerState.undump(cfg, name);
		}
		
		f.delete();
	}
}
