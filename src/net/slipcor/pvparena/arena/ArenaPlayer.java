package net.slipcor.pvparena.arena;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.classes.PAStatMap;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
 * <pre>Arena Player class</pre>
 * 
 * contains Arena Player methods and variables for quicker access
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class ArenaPlayer {
	private static Debug db = new Debug(5);
	private static HashMap<String, ArenaPlayer> totalPlayers = new HashMap<String, ArenaPlayer>();

	private final String name;
	private boolean telePass = false;

	private Arena arena;
	private ArenaClass aClass;
	private PlayerState state;
	private PALocation location;
	private Status status = Status.NULL;

	private ItemStack[] savedInventory;
	private ItemStack[] savedArmor;
	private HashSet<PermissionAttachment> tempPermissions = new HashSet<PermissionAttachment>();
	private HashMap<String, PAStatMap> statistics = new HashMap<String, PAStatMap>();

	/**
	 * <pre>
	 * - NULL = not part of an arena
	 * - WARM = not part of an arena, warmed up
	 * - LOUNGE = inside an arena lobby mode
	 * - READY = inside an arena lobby mode, readied up
	 * - FIGHT = fighting inside an arena
	 * - WATCH = watching a fight from the spectator area
	 * - DEAD = dead and soon respawning
	 * - LOST = lost and thus spectating
	 * </pre>
	 */
	public static enum Status {
		NULL, WARM, LOUNGE, READY, FIGHT, WATCH, DEAD, LOST
	}

	private boolean publicChatting = true;
	private PABlockLocation[] selection = new PABlockLocation[2];

	public ArenaPlayer(String playerName) {
		db.i("creating offline arena player: " + playerName, playerName);
		name = playerName;
		
		totalPlayers.put(name, this);
	}
	
	public ArenaPlayer(Player p, Arena a) {
		db.i("creating arena player: " + p.getName(), p);

		this.name = p.getName();
		this.setArena(a);

		totalPlayers.put(name, this);
	}

	public static int countPlayers() {
		return totalPlayers.size();
	}

	public static HashSet<ArenaPlayer> getAllArenaPlayers() {
		HashSet<ArenaPlayer> ps = new HashSet<ArenaPlayer>();
		for (ArenaPlayer ap : totalPlayers.values()) {
			ps.add(ap);
		}
		return ps;
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

			if (event.getCause() == DamageCause.PROJECTILE
					&& p1 instanceof Projectile) {
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
	 * supply a player with class items and eventually wool head
	 * 
	 * @param player
	 *            the player to supply
	 */
	public static void givePlayerFightItems(Arena arena, Player player) {
		ArenaPlayer ap = parsePlayer(player.getName());

		ArenaClass playerClass = ap.getArenaClass();
		if (playerClass == null) {
			return;
		}
		InventoryManager.db.i("giving items to player '" + player.getName()
				+ "', class '" + playerClass.getName() + "'", player);

		playerClass.equip(player);

		if (arena.getArenaConfig().getBoolean(CFG.USES_WOOLHEAD)) {
			ArenaTeam aTeam = ap.getArenaTeam();
			String color = aTeam.getColor().name();
			InventoryManager.db.i("forcing woolhead: " + aTeam.getName() + "/"
					+ color, player);
			player.getInventory().setHelmet(
					new ItemStack(Material.WOOL, 1, StringParser
							.getColorDataFromENUM(color)));
		}
	}

	public static void initiate() {
		db.i("creating offline arena players");
		
		if (!PVPArena.instance.getConfig().getBoolean("stats")) {
			return;
		}

		YamlConfiguration cfg = new YamlConfiguration();
		try {
			cfg.load(PVPArena.instance.getDataFolder() + "/players.yml");

			Set<String> players = cfg.getKeys(false);
			for (String s : players) {
				totalPlayers.put(s, new ArenaPlayer(s));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get an ArenaPlayer from a player name
	 * 
	 * @param String
	 *            the playername to use
	 * @return an ArenaPlayer instance belonging to that player
	 */
	public static synchronized ArenaPlayer parsePlayer(String name) {
		if (totalPlayers.get(name) == null) {
			if (Bukkit.getPlayerExact(name) == null) {
				totalPlayers.put(name, new ArenaPlayer(name));
			} else {
				totalPlayers.put(name, new ArenaPlayer(Bukkit.getPlayerExact(name), null));
			}
		}
		return totalPlayers.get(name);
	}

	/**
	 * prepare a player's inventory, back it up and clear it
	 * 
	 * @param player
	 *            the player to save
	 */
	public static void backupAndClearInventory(Arena arena, Player player) {
		InventoryManager.db.i("saving player inventory: " + player.getName(), player);

		ArenaPlayer p = parsePlayer(player.getName());
		p.savedInventory = player.getInventory().getContents().clone();
		p.savedArmor = player.getInventory().getArmorContents().clone();
		InventoryManager.clearInventory(player);
	}

	/**
	 * reload player inventories from saved variables
	 * 
	 * @param player
	 */
	public static void reloadInventory(Arena arena, Player player) {

		if (player == null) {
			return;
		}
		db.i("resetting inventory: " + player.getName(), player);
		if (player.getInventory() == null) {
			db.i("inventory null!", player);
			return;
		}

		ArenaPlayer p = parsePlayer(player.getName());

		if (p.savedInventory == null) {
			db.i("saved inventory null!", player);
			return;
		}
		// AIR AIR AIR AIR instead of contents !!!!
		db.i("adding " + StringParser.getStringFromItemStacks(p.savedInventory), player);
		player.getInventory().setContents(p.savedInventory);
		db.i("adding " + StringParser.getStringFromItemStacks(p.savedArmor), player);
		player.getInventory().setArmorContents(p.savedArmor);
	}

	public void addDeath() {
		this.getStatistics(arena).incStat(StatisticsManager.type.DEATHS);
	}

	public void addKill() {
		this.getStatistics(arena).incStat(StatisticsManager.type.KILLS);
	}

	public void addLosses() {
		this.getStatistics(arena).incStat(StatisticsManager.type.LOSSES);
	}

	public void addStatistic(String arenaName, StatisticsManager.type type, int i) {
		if (!statistics.containsKey(arenaName)) {
			statistics.put(arenaName, new PAStatMap(name));
		}

		statistics.get(arenaName).incStat(type, i);
	}

	public void addWins() {
		this.getStatistics(arena).incStat(StatisticsManager.type.WINS);
	}

	private void clearDump() {
		db.i("clearing dump of " + name, this.name);
		debugPrint();
		File f = new File(PVPArena.instance.getDataFolder().getPath()
				+ "/dumps/" + this.name + ".yml");
		if (!f.exists()) {
			return;
		}
		f.delete();
	}

	/**
	 * save the player state
	 * 
	 * @param player
	 *            the player to save
	 */
	public void createState(Player player) {
		state = new PlayerState(player);
	}

	public boolean didValidSelection() {
		return selection[0] != null && selection[1] != null;
	}
	
	public void debugPrint() {
		if (status == null || location == null) {
			db.i("DEBUG PRINT OUT:", this.name);
			db.i(StringParser.verify(name), this.name);
			db.i(StringParser.verify(status), this.name);
			db.i(StringParser.verify(location), this.name);
			db.i(StringParser.verify(selection[0]), this.name);
			db.i(StringParser.verify(selection[1]), this.name);
			return;
		}
		db.i("------------------", this.name);
		db.i("Player: " + name, this.name);
		db.i("telepass: " + String.valueOf(telePass) + " | chatting: "+ String.valueOf(publicChatting), this.name);
		db.i("arena: " + (arena == null ? "null" : arena.getName()), this.name);
		db.i("aClass: " + (aClass == null ? "null" : aClass.getName()), this.name);
		db.i("location: " + ((PALocation) location).toString(), this.name);
		db.i("status: " + status.name(), this.name);
		db.i("savedInventory: " + StringParser.getStringFromItemStacks(savedInventory), this.name);
		db.i("savedArmor: " + StringParser.getStringFromItemStacks(savedArmor), this.name);
		db.i("tempPermissions:", this.name);
		for (PermissionAttachment pa : tempPermissions) {
			db.i("> " + pa.toString(), this.name);
		}
		db.i("------------------", this.name);
	}

	public void dump() {
		db.i("dumping...", this.name);
		debugPrint();
		File f = new File(PVPArena.instance.getDataFolder().getPath()
				+ "/dumps/" + this.name + ".yml");
		try {
			f.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		YamlConfiguration cfg = new YamlConfiguration();
		cfg.set("arena", arena.getName());
		if (state != null) {
			state.dump(cfg);
		}

		cfg.set("inventory",
				StringParser.getStringFromItemStacks(savedInventory));
		cfg.set("armor", StringParser.getStringFromItemStacks(savedArmor));
		cfg.set("loc", Config.parseToString(location));

		try {
			cfg.save(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * return the PVP Arena bukkit player
	 * 
	 * @return the bukkit player instance
	 */
	public Player get() {
		return Bukkit.getPlayerExact(name);
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
	 * return the arena class
	 * 
	 * @return the arena class
	 */
	public ArenaClass getArenaClass() {
		return aClass;
	}

	public ArenaTeam getArenaTeam() {
		if (arena == null) {
			return null;
		}
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().contains(this)) {
				return team;
			}
		}
		return null;
	}

	public PALocation getLocation() {
		db.i("reading loc!", this.name);
		if (location != null) {
			db.i(": " + location.toString(), this.name);
                }
		return location;
	}

	/**
	 * return the player name
	 * 
	 * @return the player name
	 */
	public String getName() {
		return name;
	}

	public PABlockLocation[] getSelection() {
		return selection.clone();
	}

	/**
	 * return the player state
	 * 
	 * @return the player state
	 */
	public PlayerState getState() {
		return state;
	}

	public PAStatMap getStatistics() {
		return getStatistics(arena);
	}

	public PAStatMap getStatistics(Arena a) {
		if (a == null) {
			return new PAStatMap(name);
		}
		if (statistics.get(a.getName()) == null) {
			statistics.put(a.getName(), new PAStatMap(name));
		}
		return statistics.get(a.getName());
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
		return hasTelePass();
	}

	public HashSet<PermissionAttachment> getTempPermissions() {
		return tempPermissions;
	}

	public int getTotalStatistics(type t) {
		int sum = 0;

		for (PAStatMap stat : statistics.values()) {
			sum += stat.getStat(t);
		}

		return sum;
	}

	public boolean hasTelePass() {
		return telePass;
	}

	public boolean isPublicChatting() {
		return publicChatting;
	}

	public void readDump() {
		db.i("reading dump: " + name, this.name);
		debugPrint();
		File f = new File(PVPArena.instance.getDataFolder().getPath()
				+ "/dumps/" + this.name + ".yml");
		if (!f.exists()) {
			db.i("no dump!", this.name);
			return;
		}

		YamlConfiguration cfg = new YamlConfiguration();
		try {
			cfg.load(f);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		arena = ArenaManager.getArenaByName(cfg.getString("arena"));
		savedInventory = StringParser.getItemStacksFromString(cfg.getString(
				"inventory", "AIR"));
		savedArmor = StringParser.getItemStacksFromString(cfg.getString(
				"armor", "AIR"));
		location = Config.parseLocation(cfg.getString("loc"));
		
		if (arena != null) {
			String goTo = arena.getArenaConfig().getString(CFG.TP_EXIT);
			if (!goTo.equals("old")) {
				location = SpawnManager.getCoords(arena, "exit");
			}
			
			if (Bukkit.getPlayer(name) == null) {
				db.i("player offline, OUT!", this.name);
				return;
			}
			state = PlayerState.undump(cfg, name);
		}

		f.delete();
		debugPrint();
	}

	/**
	 * save and reset a player instance
	 * 
	 * @param b
	 *            should
	 */
	public void reset() {
		db.i("destroying arena player " + name, this.name);
		debugPrint();
		YamlConfiguration cfg = new YamlConfiguration();
		try {
			if (PVPArena.instance.getConfig().getBoolean("stats")) {
				
				String file = PVPArena.instance.getDataFolder().toString()
						+ "/players.yml";
				cfg.load(file);
	
				if (arena != null) {
					String a = arena.getName();
					cfg.set(a + "." + name + ".losses",
							getStatistics().getStat(StatisticsManager.type.LOSSES)
									+ getTotalStatistics(StatisticsManager.type.LOSSES));
					cfg.set(a + "." + name + ".wins",
							getStatistics().getStat(StatisticsManager.type.WINS)
									+ getTotalStatistics(StatisticsManager.type.WINS));
					cfg.set(a + "." + name + ".kills",
							getStatistics().getStat(StatisticsManager.type.KILLS)
									+ getTotalStatistics(StatisticsManager.type.KILLS));
					cfg.set(a + "." + name + ".deaths",
							getStatistics().getStat(StatisticsManager.type.DEATHS)
									+ getTotalStatistics(StatisticsManager.type.DEATHS));
					cfg.set(a + "." + name + ".damage",
							getStatistics().getStat(StatisticsManager.type.DAMAGE)
									+ getTotalStatistics(StatisticsManager.type.DAMAGE));
					cfg.set(a + "." + name + ".maxdamage",
							getStatistics().getStat(StatisticsManager.type.MAXDAMAGE)
									+ getTotalStatistics(StatisticsManager.type.MAXDAMAGE));
					cfg.set(a + "." + name + ".damagetake", getStatistics()
							.getStat(StatisticsManager.type.DAMAGETAKE)
							+ getTotalStatistics(StatisticsManager.type.DAMAGETAKE));
					cfg.set(a + "." + name + ".maxdamagetake", getStatistics()
							.getStat(StatisticsManager.type.MAXDAMAGETAKE)
							+ getTotalStatistics(StatisticsManager.type.MAXDAMAGETAKE));
				}
	
				cfg.save(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (get() == null ) {
			db.i("reset() ; out! null", this.name);
			return;
		}

		setTelePass(false);

		if (state != null) {
			state.reset();
			state = null;
		}
		//location = null;

		setStatus(Status.NULL);

		if (arena != null) {
			ArenaTeam team = this.getArenaTeam();
			if (team != null) {
				team.remove(this);
			}
		}
		arena = null;
		aClass = null;
		
		get().setFireTicks(0);

		clearDump();
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
	 * set a player's arena class by name
	 * 
	 * @param s
	 *            an arena class name
	 */
	public void setArenaClass(String s) {

		for (ArenaClass ac : getArena().getClasses()) {
			if (ac.getName().equalsIgnoreCase(s)) {
				setArenaClass(ac);
				return;
			}
		}
		PVPArena.instance.getLogger().warning("[PA-debug] failed to set unknown class " + s
				+ " to player " + name);
	}

	public void setLocation(PALocation location) {
		this.location = location;
	}

	public void setPublicChatting(boolean b) {
		publicChatting = b;
	}

	public void setSelection(Location loc, boolean second) {
		if (second) {
			selection[1] = new PABlockLocation(loc);
		} else {
			selection[0] = new PABlockLocation(loc);
		}
	}

	public void setStatistic(String arenaName, StatisticsManager.type type, int i) {
		if (!statistics.containsKey(arenaName)) {
			statistics.put(arenaName, new PAStatMap(name));
		}

		PAStatMap map = statistics.get(arenaName);
		map.setStat(type, i);
	}

	public void setStatus(Status status) {
		db.i(name + ">" + status.name(), this.name);
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

	public void setTempPermissions(HashSet<PermissionAttachment> tempPermissions) {
		this.tempPermissions = tempPermissions;
	}
	
	@Override
	public String toString() {
		ArenaTeam team = getArenaTeam();

		return (team == null) ? name : team.getColorCodeString() + name + ChatColor.RESET;
	}
	
	public void unsetSelection() {
		selection[0] = null;
		selection[1] = null;
	}
}
