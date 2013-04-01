package net.slipcor.pvparena.arena;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
 * <pre>
 * Arena Player class
 * </pre>
 * 
 * contains Arena Player methods and variables for quicker access
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class ArenaPlayer {
	private static Debug debug = new Debug(5);
	private static Map<String, ArenaPlayer> totalPlayers = new HashMap<String, ArenaPlayer>();

	private final String name;
	private boolean telePass = false;
	private boolean ignoreAnnouncements = false;

	private Arena arena;
	private ArenaClass aClass;
	private PlayerState state;
	private PALocation location;
	private Status status = Status.NULL;

	private ItemStack[] savedInventory;
	private ItemStack[] savedArmor;
	private Set<PermissionAttachment> tempPermissions = new HashSet<PermissionAttachment>();
	final private Map<String, PAStatMap> statistics = new HashMap<String, PAStatMap>();

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

	public ArenaPlayer(final String playerName) {
		debug.i("creating offline arena player: " + playerName, playerName);
		name = playerName;

		totalPlayers.put(name, this);
	}

	public ArenaPlayer(final Player player, final Arena arena) {
		debug.i("creating arena player: " + player.getName(), player);

		this.name = player.getName();
		setArena(arena);

		totalPlayers.put(name, this);
	}

	public static int countPlayers() {
		return totalPlayers.size();
	}

	public static Set<ArenaPlayer> getAllArenaPlayers() {
		final Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
		for (ArenaPlayer ap : totalPlayers.values()) {
			players.add(ap);
		}
		return players;
	}

	/**
	 * try to find the last damaging player
	 * 
	 * @param eEvent
	 *            the Event
	 * @return the player instance if found, null otherwise
	 */
	public static Player getLastDamagingPlayer(final Event eEvent) {
		debug.i("trying to get the last damaging player");
		if (eEvent instanceof EntityDamageByEntityEvent) {
			debug.i("there was an EDBEE");
			final EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) eEvent;

			Entity eDamager = event.getDamager();

			if (event.getCause() == DamageCause.PROJECTILE
					&& eDamager instanceof Projectile) {
				eDamager = ((Projectile) eDamager).getShooter();
				debug.i("killed by projectile, shooter is found");
			}

			if (event.getEntity() instanceof Wolf) {
				final Wolf wolf = (Wolf) event.getEntity();
				if (wolf.getOwner() != null) {
					eDamager = (Entity) wolf.getOwner();
					debug.i("tamed wolf is found");
				}
			}

			if (eDamager instanceof Player) {
				debug.i("it was a player!");
				return (Player) eDamager;
			}
		}
		debug.i("last damaging player is null");
		debug.i("last damaging event: " + eEvent.getEventName());
		return null;
	}

	/**
	 * supply a player with class items and eventually wool head
	 * 
	 * @param player
	 *            the player to supply
	 */
	public static void givePlayerFightItems(final Arena arena, final Player player) {
		final ArenaPlayer aPlayer = parsePlayer(player.getName());

		final ArenaClass playerClass = aPlayer.getArenaClass();
		if (playerClass == null) {
			return;
		}
		InventoryManager.DEBUG.i("giving items to player '" + player.getName()
				+ "', class '" + playerClass.getName() + "'", player);

		playerClass.equip(player);

		if (arena.getArenaConfig().getBoolean(CFG.USES_WOOLHEAD)) {
			final ArenaTeam aTeam = aPlayer.getArenaTeam();
			final String color = aTeam.getColor().name();
			InventoryManager.DEBUG.i("forcing woolhead: " + aTeam.getName() + "/"
					+ color, player);
			player.getInventory().setHelmet(
					new ItemStack(Material.WOOL, 1, StringParser
							.getColorDataFromENUM(color)));
		}
	}

	public static void initiate() {
		debug.i("creating offline arena players");

		if (!PVPArena.instance.getConfig().getBoolean("stats")) {
			return;
		}

		final YamlConfiguration cfg = new YamlConfiguration();
		try {
			cfg.load(PVPArena.instance.getDataFolder() + "/players.yml");
			
			final Set<String> arenas = cfg.getKeys(false);
			
			for (String arenaname : arenas) {

				final Set<String> players = cfg.getConfigurationSection(arenaname).getKeys(false);
				for (String player : players) {
					/*
					final Set<String> values = cfg.getConfigurationSection(arenaname+"."+player).getKeys(false);

					ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player);
					
					for (String value : values) {
						final StatisticsManager.type statType = StatisticsManager.type.getByString(value);
						
						if (statType == null) {
							System.out.print("null: " + value);
							continue;
						}
						
						//aPlayer.addStatistic(arenaname, statType, cfg.getInt(arenaname+"."+player+"."+value));
					}
					*/
					totalPlayers.put(player, ArenaPlayer.parsePlayer(player)); 
				}
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * get an ArenaPlayer from a player name
	 * 
	 * @param String
	 *            the playername to use
	 * @return an ArenaPlayer instance belonging to that player
	 */
	public static ArenaPlayer parsePlayer(final String name) {
		synchronized(ArenaPlayer.class) {
			if (totalPlayers.get(name) == null) {
				if (Bukkit.getPlayerExact(name) == null) {
					totalPlayers.put(name, new ArenaPlayer(name));
				} else {
					totalPlayers.put(name,
							new ArenaPlayer(Bukkit.getPlayerExact(name), null));
				}
			}
			return totalPlayers.get(name);
		}
	}

	/**
	 * prepare a player's inventory, back it up and clear it
	 * 
	 * @param player
	 *            the player to save
	 */
	public static void backupAndClearInventory(final Arena arena, final Player player) {
		InventoryManager.DEBUG.i("saving player inventory: " + player.getName(),
				player);

		final ArenaPlayer aPlayer = parsePlayer(player.getName());
		aPlayer.savedInventory = player.getInventory().getContents().clone();
		aPlayer.savedArmor = player.getInventory().getArmorContents().clone();
		InventoryManager.clearInventory(player);
	}

	/**
	 * reload player inventories from saved variables
	 * 
	 * @param player
	 */
	public static void reloadInventory(final Arena arena, final Player player) {

		if (player == null) {
			return;
		}
		debug.i("resetting inventory: " + player.getName(), player);
		if (player.getInventory() == null) {
			debug.i("inventory null!", player);
			return;
		}

		final ArenaPlayer aPlayer = parsePlayer(player.getName());

		if (aPlayer.savedInventory == null) {
			debug.i("saved inventory null!", player);
			return;
		}
		// AIR AIR AIR AIR instead of contents !!!!
		debug.i("adding " + StringParser.getStringFromItemStacks(aPlayer.savedInventory),
				player);
		player.getInventory().setContents(aPlayer.savedInventory);
		debug.i("adding " + StringParser.getStringFromItemStacks(aPlayer.savedArmor),
				player);
		player.getInventory().setArmorContents(aPlayer.savedArmor);
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

	public void addStatistic(final String arenaName, final StatisticsManager.type type,
			final int value) {
		if (!statistics.containsKey(arenaName)) {
			statistics.put(arenaName, new PAStatMap());
		}

		statistics.get(arenaName).incStat(type, value);
	}

	public void addWins() {
		this.getStatistics(arena).incStat(StatisticsManager.type.WINS);
	}

	private void clearDump() {
		debug.i("clearing dump of " + name, this.name);
		debugPrint();
		final File file = new File(PVPArena.instance.getDataFolder().getPath()
				+ "/dumps/" + this.name + ".yml");
		if (!file.exists()) {
			return;
		}
		file.delete();
	}

	/**
	 * save the player state
	 * 
	 * @param player
	 *            the player to save
	 */
	public void createState(final Player player) {
		state = new PlayerState(player);
	}

	public boolean didValidSelection() {
		return selection[0] != null && selection[1] != null;
	}

	public void debugPrint() {
		if (status == null || location == null) {
			debug.i("DEBUG PRINT OUT:", this.name);
			debug.i(String.valueOf(name.toString()), this.name);
			debug.i(String.valueOf(String.valueOf(status)), this.name);
			debug.i(String.valueOf(String.valueOf(location)), this.name);
			debug.i(String.valueOf(String.valueOf(selection[0])), this.name);
			debug.i(String.valueOf(String.valueOf(selection[1])), this.name);
			return;
		}
		debug.i("------------------", this.name);
		debug.i("Player: " + name, this.name);
		debug.i("telepass: " + telePass + " | chatting: "
				+ publicChatting, this.name);
		debug.i("arena: " + (arena == null ? "null" : arena.getName()), this.name);
		debug.i("aClass: " + (aClass == null ? "null" : aClass.getName()),
				this.name);
		debug.i("location: " + ((PALocation) location).toString(), this.name);
		debug.i("status: " + status.name(), this.name);
		debug.i("savedInventory: "
				+ StringParser.getStringFromItemStacks(savedInventory),
				this.name);
		debug.i("savedArmor: " + StringParser.getStringFromItemStacks(savedArmor),
				this.name);
		debug.i("tempPermissions:", this.name);
		for (PermissionAttachment pa : tempPermissions) {
			debug.i("> " + pa.toString(), this.name);
		}
		debug.i("------------------", this.name);
	}

	public void dump() {
		debug.i("dumping...", this.name);
		debugPrint();
		final File file = new File(PVPArena.instance.getDataFolder().getPath()
				+ "/dumps/" + this.name + ".yml");
		try {
			file.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		final YamlConfiguration cfg = new YamlConfiguration();
		cfg.set("arena", arena.getName());
		if (state != null) {
			state.dump(cfg);
		}

		try {
			cfg.set("inventory",
					StringParser.getStringFromItemStacks(savedInventory));
			cfg.set("armor", StringParser.getStringFromItemStacks(savedArmor));
			cfg.set("loc", Config.parseToString(location));
			
			cfg.save(file);
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
		debug.i("reading loc!", this.name);
		if (location != null) {
			debug.i(": " + location.toString(), this.name);
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

	public PAStatMap getStatistics(final Arena arena) {
		if (arena == null) {
			return new PAStatMap();
		}
		if (statistics.get(arena.getName()) == null) {
			statistics.put(arena.getName(), new PAStatMap());
		}
		return statistics.get(arena.getName());
	}

	public Status getStatus() {
		return status;
	}

	/**
	 * hand over a player's tele pass
	 * 
	 * @return true if may pass, false otherwise
	 */
	public boolean isTelePass() {
		return hasTelePass();
	}

	public Set<PermissionAttachment> getTempPermissions() {
		return tempPermissions;
	}

	public int getTotalStatistics(final type statType) {
		int sum = 0;

		for (PAStatMap stat : statistics.values()) {
			sum += stat.getStat(statType);
		}

		return sum;
	}

	public boolean hasTelePass() {
		return telePass;
	}

	public boolean isIgnoringAnnouncements() {
		return ignoreAnnouncements;
	}

	public boolean isPublicChatting() {
		return publicChatting;
	}

	public void readDump() {
		debug.i("reading dump: " + name, this.name);
		debugPrint();
		final File file = new File(PVPArena.instance.getDataFolder().getPath()
				+ "/dumps/" + this.name + ".yml");
		if (!file.exists()) {
			debug.i("no dump!", this.name);
			return;
		}

		final YamlConfiguration cfg = new YamlConfiguration();
		try {
			cfg.load(file);
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
			final String goTo = arena.getArenaConfig().getString(CFG.TP_EXIT);
			if (!"old".equals(goTo)) {
				location = SpawnManager.getCoords(arena, "exit");
			}

			if (Bukkit.getPlayer(name) == null) {
				debug.i("player offline, OUT!", this.name);
				return;
			}
			state = PlayerState.undump(cfg, name);
		}

		file.delete();
		debugPrint();
	}

	/**
	 * save and reset a player instance
	 * 
	 * @param b
	 *            should
	 */
	public void reset() {
		debug.i("destroying arena player " + name, this.name);
		debugPrint();
		final YamlConfiguration cfg = new YamlConfiguration();
		try {
			if (PVPArena.instance.getConfig().getBoolean("stats")) {

				final String file = PVPArena.instance.getDataFolder().toString()
						+ "/players.yml";
				cfg.load(file);

				if (arena != null) {
					final String arenaName = arena.getName();
					cfg.set(arenaName + "." + name + ".losses", getStatistics()
							.getStat(StatisticsManager.type.LOSSES)
							+ getTotalStatistics(StatisticsManager.type.LOSSES));
					cfg.set(arenaName + "." + name + ".wins",
							getStatistics()
									.getStat(StatisticsManager.type.WINS)
									+ getTotalStatistics(StatisticsManager.type.WINS));
					cfg.set(arenaName + "." + name + ".kills",
							getStatistics().getStat(
									StatisticsManager.type.KILLS)
									+ getTotalStatistics(StatisticsManager.type.KILLS));
					cfg.set(arenaName + "." + name + ".deaths", getStatistics()
							.getStat(StatisticsManager.type.DEATHS)
							+ getTotalStatistics(StatisticsManager.type.DEATHS));
					cfg.set(arenaName + "." + name + ".damage", getStatistics()
							.getStat(StatisticsManager.type.DAMAGE)
							+ getTotalStatistics(StatisticsManager.type.DAMAGE));
					cfg.set(arenaName + "." + name + ".maxdamage",
							getStatistics().getStat(
									StatisticsManager.type.MAXDAMAGE)
									+ getTotalStatistics(StatisticsManager.type.MAXDAMAGE));
					cfg.set(arenaName + "." + name + ".damagetake",
							getStatistics().getStat(
									StatisticsManager.type.DAMAGETAKE)
									+ getTotalStatistics(StatisticsManager.type.DAMAGETAKE));
					cfg.set(arenaName + "." + name + ".maxdamagetake",
							getStatistics().getStat(
									StatisticsManager.type.MAXDAMAGETAKE)
									+ getTotalStatistics(StatisticsManager.type.MAXDAMAGETAKE));
				}

				cfg.save(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (get() == null) {
			debug.i("reset() ; out! null", this.name);
			return;
		}

		setTelePass(false);

		if (state != null) {
			state.reset();
			state = null;
		}
		// location = null;

		setStatus(Status.NULL);

		if (arena != null) {
			final ArenaTeam team = this.getArenaTeam();
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
	public final void setArena(final Arena arena) {
		this.arena = arena;
	}

	/**
	 * set the player's arena class
	 * 
	 * @param aClass
	 *            the arena class to set
	 */
	public void setArenaClass(final ArenaClass aClass) {
		this.aClass = aClass;
	}

	/**
	 * set a player's arena class by name
	 * 
	 * @param className
	 *            an arena class name
	 */
	public void setArenaClass(final String className) {

		for (ArenaClass ac : getArena().getClasses()) {
			if (ac.getName().equalsIgnoreCase(className)) {
				setArenaClass(ac);
				return;
			}
		}
		PVPArena.instance.getLogger().warning(
				"[PA-debug] failed to set unknown class " + className + " to player "
						+ name);
	}

	public void setIgnoreAnnouncements(final boolean value) {
		ignoreAnnouncements = value;
	}

	public void setLocation(final PALocation location) {
		this.location = location;
	}

	public void setPublicChatting(final boolean chatPublic) {
		publicChatting = chatPublic;
	}

	public void setSelection(final Location loc, final boolean second) {
		if (second) {
			selection[1] = new PABlockLocation(loc);
		} else {
			selection[0] = new PABlockLocation(loc);
		}
	}

	public void setStatistic(final String arenaName, final StatisticsManager.type type,
			final int value) {
		if (!statistics.containsKey(arenaName)) {
			statistics.put(arenaName, new PAStatMap());
		}

		final PAStatMap map = statistics.get(arenaName);
		map.setStat(type, value);
	}

	public void setStatus(final Status status) {
		debug.i(name + ">" + status.name(), this.name);
		this.status = status;
	}

	/**
	 * hand over a player's tele pass
	 * 
	 * @param canTeleport
	 *            true if may pass, false otherwise
	 */
	public void setTelePass(final boolean canTeleport) {
		telePass = canTeleport;
	}

	public void setTempPermissions(final Set<PermissionAttachment> tempPermissions) {
		this.tempPermissions = tempPermissions;
	}

	@Override
	public String toString() {
		final ArenaTeam team = getArenaTeam();

		return (team == null) ? name : team.getColorCodeString() + name
				+ ChatColor.RESET;
	}

	public void unsetSelection() {
		selection[0] = null;
		selection[1] = null;
	}
}
