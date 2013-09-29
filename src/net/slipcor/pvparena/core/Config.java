package net.slipcor.pvparena.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionFlag;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionProtection;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;
import net.slipcor.pvparena.loadables.ArenaRegionShapeManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * <pre>
 * Configuration class
 * </pre>
 * 
 * This Config wrapper improves access to config files by storing them in RAM
 * and providing quick, secured, access. Thanks a lot to garbagemule for the
 * start of this config.
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class Config {
	private YamlConfiguration cfg;
	private File configFile;
	private Map<String, Boolean> booleans;
	private Map<String, Integer> ints;
	private Map<String, Double> doubles;
	private Map<String, String> strings;

	public static enum CFG {


		Z("configversion","v0.9.0.0"),
		
		CHAT_COLORNICK("chat.colorNick", true),
		CHAT_DEFAULTTEAM("chat.defaultTeam", false),
		CHAT_ENABLED("chat.enabled", true),
		CHAT_ONLYPRIVATE("chat.onlyPrivate", false),
		
		CMDS_DEFAULTJOIN("cmds.defaultjoin", true),

		DAMAGE_ARMOR("damage.armor", true),
		DAMAGE_SPAWNCAMP("damage.spawncamp", 1),
		DAMAGE_WEAPONS("damage.weapons", true),

		GENERAL_CLASSSPAWN("general.classspawn", false),
		GENERAL_CUSTOMRETURNSGEAR("general.customReturnsGear", false),
		GENERAL_ENABLED("general.enabled", true),
		GENERAL_GAMEMODE("general.gm", 0),
		GENERAL_LEAVEDEATH("general.leavedeath", false),
		GENERAL_LANG("general.lang", "none"),
		GENERAL_OWNER("general.owner", "server"),
		GENERAL_QUICKSPAWN("general.quickspawn", true),
		GENERAL_PREFIX("general.prefix", "PVP Arena"),
		GENERAL_SMARTSPAWN("general.smartspawn", false),
		GENERAL_TIME("general.time", -1),
		GENERAL_TYPE("general.type", "none"),
		GENERAL_WAND("general.wand", 280),

		GOAL_ADDLIVESPERPLAYER("goal.livesPerPlayer", false),
		
		ITEMS_MINPLAYERS("items.minplayers", 2),
		ITEMS_REWARDS("items.rewards", "none", true),
		ITEMS_RANDOM("items.random", true),
		ITEMS_EXCLUDEFROMDROPS("items.excludeFromDrops", "none", true),
		ITEMS_TAKEOUTOFGAME("items.takeOutOfGame", "none", true),
		
		JOIN_RANGE("join.range", 0),
		JOIN_FORCE("join.forceregionjoin", false),
		
		LISTS_BLACKLIST("block.blacklist", new ArrayList<String>()),
		LISTS_CMDWHITELIST("cmds.whitelist", new ArrayList<String>()),
		LISTS_GOALS("goals", new ArrayList<String>()),
		LISTS_MODS("mods", new ArrayList<String>()),
		LISTS_WHITELIST("block.whitelist", new ArrayList<String>()),
		
		MSG_LOUNGE("msg.lounge", "Welcome to the arena lounge! Hit a class sign and then the iron block to flag yourself as ready!"),
		MSG_PLAYERJOINED("msg.playerjoined", "%1% joined the Arena!"),
		MSG_PLAYERJOINEDTEAM("msg.playerjoinedteam", "%1% joined team %2%!"),
		MSG_STARTING("msg.starting", "Arena is starting! Type &e/pa %1% to join!"),
		MSG_YOUJOINED("msg.youjoined", "You have joined the FreeForAll Arena!"),
		MSG_YOUJOINEDTEAM("msg.youjoinedteam", "You have joined team %1%!"),
		
		PERMS_EXPLICITARENA("perms.explicitArenaNeeded", false),
		PERMS_EXPLICITCLASS("perms.explicitClassNeeded", false),
		PERMS_FLY("perms.fly", false),
		PERMS_LOUNGEINTERACT("perms.loungeinteract", false),
		PERMS_JOININBATTLE("perms.joinInBattle", false),
		PERMS_JOINWITHSCOREBOARD("perms.joinWithScoreboard", true),
		PERMS_TEAMKILL("perms.teamkill", true),
		PERMS_SPECTALK("perms.specTalk", true),

		PLAYER_AUTOIGNITE("player.autoIgniteTNT", false),
		PLAYER_DROPSEXP("player.dropsEXP", false),
		PLAYER_DROPSINVENTORY("player.dropsInventory", false),
		PLAYER_EXHAUSTION("player.exhaustion", 0.0),
		PLAYER_FEEDFORKILL("player.hungerforkill", 0),
		PLAYER_FOODLEVEL("player.foodLevel", 20),
		PLAYER_HEALTH("player.health", 20),
		PLAYER_PREVENTDEATH("player.preventDeath", true),
		PLAYER_REFILLINVENTORY("player.refillInventory", true),
		PLAYER_SATURATION("player.saturation", 20),
		PLAYER_QUICKLOOT("player.quickloot", false),
		
		PROTECT_ENABLED("protection.enabled", true),
		PROTECT_PUNISH("protection.punish", false),
		PROTECT_SPAWN("protection.spawn", 0),

		READY_AUTOCLASS("ready.autoClass", "none"),
		READY_BLOCK("ready.block", Material.IRON_BLOCK.getId()),
		READY_CHECKEACHPLAYER("ready.checkEachPlayer", false),
		READY_CHECKEACHTEAM("ready.checkEachTeam", true),
		READY_ENFORCECOUNTDOWN("ready.enforceCountdown", false),
		READY_MINPLAYERS("ready.minPlayers", 2),
		READY_MAXPLAYERS("ready.maxPlayers", 0),
		READY_MAXTEAMPLAYERS("ready.maxTeam", 0),
		READY_NEEDEDRATIO("ready.neededRatio", 0.5),
		
		TIME_ENDCOUNTDOWN("goal.endCountDown", 5),
		TIME_STARTCOUNTDOWN("time.startCountDown", 10),
		TIME_REGIONTIMER("time.regionTimer", 10),
		TIME_TELEPORTPROTECT("time.teleportProtect", 3),
		TIME_WARMUPCOUNTDOWN("time.warmupCountDown", 0),
		TIME_PVP("time.pvp", 0),

		TP_DEATH("tp.death", "old"),
		TP_EXIT("tp.exit", "old"),
		TP_LOSE("tp.lose", "old"),
		TP_WIN("tp.win", "old"),

		USES_CLASSSIGNSDISPLAY("uses.classSignsDisplay", false),
		USES_DEATHMESSAGES("uses.deathMessages", true),
		USES_EVENTEAMS("uses.evenTeams", false),
		USES_INGAMECLASSSWITCH("uses.ingameClassSwitch", false),
		USES_OVERLAPCHECK("uses.overlapCheck", true),
		USES_WOOLHEAD("uses.woolHead", false),
		
		// ----------

		GOAL_BLOCKDESTROY_BLOCKTYPE("goal.blockdestroy.blocktype", "IRON_BLOCK", false),
		GOAL_BLOCKDESTROY_LIVES("goal.blockdestroy.bdlives", 1),

		GOAL_DOM_ANNOUNCEOFFSET("goal.dom.spamoffset", 3),
		GOAL_DOM_CLAIMRANGE("goal.dom.claimrange", 3),
		GOAL_DOM_LIVES("goal.dom.dlives", 10),
		GOAL_DOM_ONLYWHENMORE("goal.dom.onlywhenmore", false),
		
		GOAL_FLAGS_FLAGTYPE("goal.flags.flagType", "WOOL", false),
		GOAL_FLAGS_LIVES("goal.flags.flives", 3),
		GOAL_FLAGS_MUSTBESAFE("goal.flags.mustBeSafe", true),
		GOAL_FLAGS_WOOLFLAGHEAD("goal.flags.woolFlagHead", true),
		GOAL_FLAGS_FLAGEFFECT("goal.flags.effect", "none"),

		GOAL_FOOD_FMAXITEMS("goal.food.fmaxitems", 50),
		GOAL_FOOD_FPLAYERITEMS("goal.food.fplayeritems", 10),
		GOAL_FOOD_FTEAMITEMS("goal.food.fteamitems", 100),

		GOAL_INFECTED_ILIVES("goal.infected.iilives", 1),
		GOAL_INFECTED_NLIVES("goal.infected.inlives", 1),
		
		GOAL_LLIVES_LIVES("goal.liberation.llives", 3),
		GOAL_PDM_LIVES("goal.playerdm.pdlives", 3),
		GOAL_PLIVES_LIVES("goal.playerlives.plives", 3),
		GOAL_TANK_LIVES("goal.tank.tlives", 1),
		GOAL_TDM_LIVES("goal.teamdm.tdlives", 10),
		GOAL_TLIVES_LIVES("goal.teamlives.tlives", 10),
		GOAL_TIME_END("goal.time.timedend", 0),
		GOAL_TIME_WINNER("goal.time.winner", "none"),
		

		GOAL_PILLARS_ANNOUNCETICK("goal.pillars.announcetick", true),
		GOAL_PILLARS_LIVES("goal.pillars.pillives", 10),
		GOAL_PILLARS_ONLYFREE("goal.pillars.onlyfree", true),
		GOAL_PILLARS_BREAKABLE("goal.pillars.breakable", true),
		GOAL_PILLARS_TICKPOINTS("goal.pillars.tickpoints", 1),
		GOAL_PILLARS_INTERVAL("goal.pillars.tickinterval", 20),

		GOAL_PILLARS_ANNOUNCEOFFSET("goal.pillars.announceoffset", 3),
		GOAL_PILLARS_MAXCLICKS("goal.pillars.maxclicks", 10),
		GOAL_PILLARS_MAXHEIGHT("goal.pillars.maxheight", 5),
		GOAL_PILLARS_EMPTYHEIGHT("goal.pillars.emptyheight", 1),
		GOAL_PILLARS_TEAMHEIGHT("goal.pillars.teamheight", 2),
		GOAL_PILLARS_CLAIMALL("goal.pillars.claimall", false),
		
		// -----------
		
		MODULES_AFTERMATCH_AFTERMATCH("modules.aftermatch.aftermatch", "off"),

		MODULES_ANNOUNCEMENTS_RADIUS("modules.announcements.radius", 0),
		MODULES_ANNOUNCEMENTS_COLOR("modules.announcements.color", "AQUA"),
		MODULES_ANNOUNCEMENTS_JOIN("modules.announcements.join", false),
		MODULES_ANNOUNCEMENTS_START("modules.announcements.start", false),
		MODULES_ANNOUNCEMENTS_END("modules.announcements.end", false),
		MODULES_ANNOUNCEMENTS_WINNER("modules.announcements.winner", false),
		MODULES_ANNOUNCEMENTS_LOSER("modules.announcements.loser", false),
		MODULES_ANNOUNCEMENTS_PRIZE("modules.announcements.prize", false),
		MODULES_ANNOUNCEMENTS_CUSTOM("modules.announcements.custom", false),
		MODULES_ANNOUNCEMENTS_ADVERT("modules.announcements.advert", false),

		MODULES_ARENAMAPS_ALIGNTOPLAYER("modules.arenamaps.aligntoplayer", false),
		MODULES_ARENAMAPS_SHOWSPAWNS("modules.arenamaps.showspawns", true),
		MODULES_ARENAMAPS_SHOWPLAYERS("modules.arenamaps.showplayers", true),
		MODULES_ARENAMAPS_SHOWLIVES("modules.arenamaps.showlives", true),

		MODULES_ARENAVOTE_EVERYONE("modules.arenavote.everyone", true),
		MODULES_ARENAVOTE_AUTOSTART("modules.arenavote.autostart", false),
		MODULES_ARENAVOTE_READYUP("modules.arenavote.readyup", 30),
		MODULES_ARENAVOTE_ONLYSPAMTOJOIN("modules.arenavote.onlySpamToJOIN", false),
		MODULES_ARENAVOTE_SECONDS("modules.arenavote.seconds", 30),
		MODULES_ARENAVOTE_WORLD("modules.arenavote.world", "none"),

		MODULES_BATTLEFIELDGUARD_ENTERDEATH("modules.battlefieldguard.enterdeath", false),

		MODULES_BETTERFIGHT_MESSAGES("modules.betterfight.usemessages", false),
		MODULES_BETTERFIGHT_ONEHITITEMS("modules.betterfight.onehititems", "none"),
		MODULES_BETTERFIGHT_RESETKILLSTREAKONDEATH("modules.betterfight.resetkillstreakondeath", true),
		MODULES_BETTERFIGHT_EXPLODEONDEATH("modules.betterfight.explodeondeath", true),

		MODULES_BLOCKRESTORE_HARD("modules.blockrestore.hard", false),
		MODULES_BLOCKRESTORE_OFFSET("modules.blockrestore.offset", 1),
		MODULES_BLOCKRESTORE_RESTOREBLOCKS("modules.blockrestore.restoreblocks", true),
		MODULES_BLOCKRESTORE_RESTORECHESTS("modules.blockrestore.restorechests", false),

		MODULES_COLORTEAMS_HIDENAME("modules.colorteams.hidename", false),
		
		MODULES_FIXINVENTORYLOSS_GAMEMODE("modules.fixinventoryloss.gamemode", false),
		MODULES_FIXINVENTORYLOSS_INVENTORY("modules.fixinventoryloss.inventory", false),

		MODULES_ITEMS_INTERVAL("modules.items.interval", 0),
		MODULES_ITEMS_ITEMS("modules.items.items", "none", true),

		MODULES_RESPAWNRELAY_INTERVAL("modules.respawnrelay.respawnseconds", 10),
		MODULES_RESPAWNRELAY_CHOOSESPAWN("modules.respawnrelay.choosespawn", false),
		
		MODULES_PLAYERFINDER_MAXRADIUS("modules.playerfinder.maxradius", 100),

		MODULES_POWERUPS_DROPSPAWN("modules.powerups.dropspawn", false),
		MODULES_POWERUPS_USAGE("modules.powerups.usage", "off"),

		MODULES_SKINS_VANILLA("modules.skins.vanilla", false),

		MODULES_SPECIALJOIN_SHOWPLAYERS("modules.specialjoin.showplayers", true),

		MODULES_STARTFREEZE_TIMER("modules.startfreeze.freezetimer", 0),

		MODULES_TURRETS_MAXDEGREES("modules.turrets.maxdegrees", 90.0),
		MODULES_TURRETS_MININTERVAL("modules.turrets.mininterval", 0),

		MODULES_VAULT_BETPOT("modules.vault.betpot", false),
		MODULES_VAULT_BETWINFACTOR("modules.vault.betWinFactor", Double.valueOf(1)),
		MODULES_VAULT_BETWINTEAMFACTOR("modules.vault.betWinTeamFactor", Double.valueOf(1)),
		MODULES_VAULT_BETWINPLAYERFACTOR("modules.vault.betWinPlayerFactor", Double.valueOf(1)),
		MODULES_VAULT_ENTRYFEE("modules.vault.entryfee", Integer.valueOf(0)),
		MODULES_VAULT_KILLREWARD("modules.vault.killreward", Double.valueOf(0)),
		MODULES_VAULT_MINPLAYTIME("modules.vault.minplaytime", Integer.valueOf(0)),
		MODULES_VAULT_MINIMUMBET("modules.vault.minbet", Double.valueOf(0)),
		MODULES_VAULT_MAXIMUMBET("modules.vault.maxbet", Double.valueOf(0)),
		MODULES_VAULT_WINPOT("modules.vault.winPot", false),
		MODULES_VAULT_WINFACTOR("modules.vault.winFactor", Double.valueOf(2)),
		MODULES_VAULT_WINREWARD("modules.vault.winreward", Integer.valueOf(0)),

		MODULES_WALLS_MATERIAL("modules.walls.wallmaterial", "SAND"),
		MODULES_WALLS_SECONDS("modules.walls.wallseconds", 300),
		
		MODULES_WORLDEDIT_AUTOLOAD("modules.worldedit.autoload", false),
		MODULES_WORLDEDIT_AUTOSAVE("modules.worldedit.autosave", false);

		private String node;
		private Object value;
		private String type;

		public static CFG getByNode(final String node) {
			for (CFG m : CFG.values()) {
				if (m.getNode().equals(node)) {
					return m;
				}
			}
			return null;
		}

		private CFG(final String node, final String value) {
			this.node = node;
			this.value = value;
			this.type = "string";
		}

		private CFG(final String node, final Boolean value) {
			this.node = node;
			this.value = value;
			this.type = "boolean";
		}

		private CFG(final String node, final Integer value) {
			this.node = node;
			this.value = value;
			this.type = "int";
		}

		private CFG(final String node, final Double value) {
			this.node = node;
			this.value = value;
			this.type = "double";
		}
		
		private CFG(final String node, final String value, final boolean multiple) {
			this.node = node;
			this.value = value;
			this.type = multiple?"items":"material";
		}
		
		private CFG(final String node, final List<String> value) {
			this.node = node;
			this.value = value;
			this.type = "list";
		}

		public String getNode() {
			return node;
		}

		public void setNode(final String value) {
			node = value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public Object getValue() {
			return value;
		}

		public static CFG[] getValues() {
			return values();
		}

		public String getType() {
			return type;
		}
	}

	/**
	 * Create a new Config instance that uses the specified file for loading and
	 * saving.
	 * 
	 * @param configFile
	 *            a YAML file
	 */
	public Config(final File configFile) {
		this.cfg = new YamlConfiguration();
		this.configFile = configFile;
		this.booleans = new HashMap<String, Boolean>();
		this.ints = new HashMap<String, Integer>();
		this.doubles = new HashMap<String, Double>();
		this.strings = new HashMap<String, String>();
	}

	public void createDefaults() {
		this.cfg.options().indent(4);

		for (CFG cfg : CFG.values()) {
			this.cfg.addDefault(cfg.getNode(), cfg.getValue());
		}
		save();
	}

	/**
	 * Load the config-file into the YamlConfiguration, and then populate the
	 * value maps.
	 * 
	 * @return true, if the load succeeded, false otherwise.
	 */
	public boolean load() {
		try {
			cfg.load(configFile);
			reloadMaps();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Iterates through all keys in the config-file, and populates the value
	 * maps. Boolean values are stored in the booleans-map, Strings in the
	 * strings-map, etc.
	 */
	public void reloadMaps() {
		for (String s : cfg.getKeys(true)) {
			final Object object = cfg.get(s);

			if (object instanceof Boolean) {
				booleans.put(s, (Boolean) object);
			} else if (object instanceof Integer) {
				ints.put(s, (Integer) object);
			} else if (object instanceof Double) {
				doubles.put(s, (Double) object);
			} else if (object instanceof String) {
				strings.put(s, (String) object);
			}
		}
	}

	/**
	 * Save the YamlConfiguration to the config-file.
	 * 
	 * @return true, if the save succeeded, false otherwise.
	 */
	public boolean save() {
		try {
			cfg.save(configFile);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Delete the config-file.
	 * 
	 * @return true, if the delete succeeded, false otherwise.
	 */
	public boolean delete() {
		return configFile.delete();
	}

	/**
	 * Set the header of the config-file.
	 * 
	 * @param header
	 *            the header
	 */
	public void setHeader(final String header) {
		cfg.options().header(header);
	}

	// /////////////////////////////////////////////////////////////////////////
	// //
	// GETTERS //
	// //
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Get the YamlConfiguration associated with this Config instance. Note that
	 * changes made directly to the YamlConfiguration will cause an
	 * inconsistency with the value maps unless reloadMaps() is called.
	 * 
	 * @return the YamlConfiguration of this Config instance
	 */
	public YamlConfiguration getYamlConfiguration() {
		return cfg;
	}

	/**
	 * Retrieve a value from the YamlConfiguration.
	 * 
	 * @param string
	 *            the path of the value
	 * @return the value of the path
	 */
	public Object getUnsafe(final String string) {
		return cfg.get(string);
	}

	/**
	 * Retrieve a boolean from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @return the boolean value of the path if the path exists, false otherwise
	 */
	public boolean getBoolean(final CFG cfg) {
		return getBoolean(cfg, (Boolean) cfg.getValue());
	}

	/**
	 * Retrieve a boolean from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @param def
	 *            a default value to return if the value was not in the map
	 * @return the boolean value of the path if it exists, def otherwise
	 */
	private boolean getBoolean(final CFG cfg, final boolean def) {
		final String path = cfg.getNode();
		final Boolean result = booleans.get(path);
		return (result == null ? def : result);
	}

	/**
	 * Retrieve an int from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @return the int value of the path if the path exists, 0 otherwise
	 */
	public int getInt(final CFG cfg) {
		return getInt(cfg, (Integer) cfg.getValue());
	}

	/**
	 * Retrieve an int from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @param def
	 *            a default value to return if the value was not in the map
	 * @return the int value of the path if it exists, def otherwise
	 */
	public int getInt(final CFG cfg, final int def) {
		final String path = cfg.getNode();
		final Integer result = ints.get(path);
		return (result == null ? def : result);
	}

	/**
	 * Retrieve a double from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @return the double value of the path if the path exists, 0D otherwise
	 */
	public double getDouble(CFG cfg) {
		return getDouble(cfg, (Double) cfg.getValue());
	}

	/**
	 * Retrieve a double from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @param def
	 *            a default value to return if the value was not in the map
	 * @return the double value of the path if it exists, def otherwise
	 */
	public double getDouble(CFG cfg, double def) {
		final String path = cfg.getNode();
		final Double result = doubles.get(path);
		return (result == null ? def : result);
	}

	/**
	 * Retrieve a string from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @return the string value of the path if the path exists, null otherwise
	 */
	public String getString(CFG cfg) {
		return getString(cfg, (String) cfg.getValue());
	}

	/**
	 * Retrieve a string from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @param def
	 *            a default value to return if the value was not in the map
	 * @return the string value of the path if it exists, def otherwise
	 */
	public String getString(CFG cfg, String def) {
		final String path = cfg.getNode();
		final String result = strings.get(path);
		return (result == null ? def : result);
	}
	
	public Material getMaterial(CFG cfg) {
		return getMaterial(cfg, Material.valueOf((String)cfg.getValue()));
	}
	
	public Material getMaterial(CFG cfg, Material def) {
		final String path = cfg.getNode();
		final String result = strings.get(path);
		if (result == null || result.equals("none")) {
			return def;
		}
		return Material.valueOf(result);
	}
	
	public ItemStack[] getItems(CFG cfg) {
		return getItems(cfg, StringParser.getItemStacksFromString((String) cfg.getValue()));
	}
	
	public ItemStack[] getItems(CFG cfg, ItemStack[] def) {
		final String path = cfg.getNode();
		final String result = strings.get(path);
		if (result == null || result.equals("none")) {
			return def;
		}
		return StringParser.getItemStacksFromString(result);
	}

	public Set<String> getKeys(String path) {
		if (cfg.get(path) == null) {
			return null;
		}

		ConfigurationSection section = cfg.getConfigurationSection(path);
		return section.getKeys(false);
	}

	public List<String> getStringList(final String path, final List<String> def) {
		if (cfg.get(path) == null) {
			return def == null ? new LinkedList<String>() : def;
		}

		return cfg.getStringList(path);
	}

	// /////////////////////////////////////////////////////////////////////////
	// //
	// MUTATORS //
	// //
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Set the value of the given path in both the value maps and the
	 * YamlConfiguration. Note that this will only properly put the value in its
	 * relevant value map, if it is of one of the supported types. The method
	 * can also be used to remove values from their maps and the
	 * YamlConfiguration by passing null for the value.
	 * 
	 * @param path
	 *            the path on which to set the value
	 * @param value
	 *            the value to set
	 */
	public void setManually(String path, Object value) {
		if (value instanceof Boolean) {
			booleans.put(path, (Boolean) value);
		} else if (value instanceof Integer) {
			ints.put(path, (Integer) value);
		} else if (value instanceof Double) {
			doubles.put(path, (Double) value);
		} else if (value instanceof String) {
			strings.put(path, (String) value);
		}

		if (value == null) {
			booleans.remove(value);
			ints.remove(value);
			doubles.remove(value);
			strings.remove(value);
		}

		cfg.set(path, value);
	}

	public void set(CFG cfg, Object value) {
		setManually(cfg.getNode(), value);
	}

	// /////////////////////////////////////////////////////////////////////////
	// //
	// UTILITY METHODS //
	// //
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Parse an input string of the form "world,x,y,z" to create a Block
	 * Location. This method will only accept strings of the specified form.
	 * 
	 * @param coords
	 *            a string of the form "world,x,y,z"
	 * @return a PABlockLocation in the given world with the given coordinates
	 */
	public static PABlockLocation parseBlockLocation(String coords) {
		String[] parts = coords.split(",");
		if (parts.length != 4) {
			throw new IllegalArgumentException(
					"Input string must only contain world, x, y, and z: " + coords);
		}

		final Integer x = parseInteger(parts[1]);
		final Integer y = parseInteger(parts[2]);
		final Integer z = parseInteger(parts[3]);

		if (Bukkit.getWorld(parts[0]) == null || x == null || y == null
				|| z == null) {
			throw new IllegalArgumentException(
					"Some of the parsed values are null: " + coords);
		}

		return new PABlockLocation(parts[0], x, y, z);
	}

	/**
	 * Parse an input string of the form "world,x,y,z,yaw,pitch" to create a
	 * Block Location. This method will only accept strings of the specified
	 * form.
	 * 
	 * @param coords
	 *            a string of the form "world,x,y,z,yaw,pitch"
	 * @return a PALocation in the given world with the given coordinates
	 */
	public static PALocation parseLocation(final String coords) {
		String[] parts = coords.split(",");

		if (parts.length == 4) {
			parts = (coords + ",0.0,0.0").split(",");
		}

		if (parts.length != 6) {
			throw new IllegalArgumentException(
					"Input string must contain world, x, y, z, yaw and pitch: "
							+ coords);
		}

		final Integer x = parseInteger(parts[1]);
		final Integer y = parseInteger(parts[2]);
		final Integer z = parseInteger(parts[3]);
		final Float yaw = parseFloat(parts[4]);
		final Float pitch = parseFloat(parts[5]);

		if (Bukkit.getWorld(parts[0]) == null || x == null || y == null
				|| z == null || yaw == null || pitch == null) {
			throw new IllegalArgumentException(
					"Some of the parsed values are null: " + coords);
		}
		return new PALocation(parts[0], x, y, z, pitch, yaw);
	}

	/**
	 * 
	 */
	public static ArenaRegionShape parseRegion(final Arena arena,
			final YamlConfiguration config, final String regionName) {

		final String coords = config.getString("arenaregion." + regionName);
		final String[] parts = coords.split(",");

		final ArenaRegionShape.RegionShape shape = ArenaRegionShapeManager
				.getShapeByName(parts[7]);

		if (parts.length < 11) {
			PVPArena.instance.getLogger().severe(arena.getName() + " caused an error while loading region " + regionName);
			throw new IllegalArgumentException(
					"Input string must contain only world, x1, y1, z1, x2, y2, z2, shape and FLAGS: "
							+ coords);
		}
		if (ArenaRegionShapeManager.getShapeByName(parts[7]) == null) {
			PVPArena.instance.getLogger().severe(arena.getName() + " caused an error while loading region " + regionName);
			throw new IllegalArgumentException(
					"Input string does not contain valid region shape: "
							+ coords);
		}
		final Integer x1 = parseInteger(parts[1]);
		final Integer y1 = parseInteger(parts[2]);
		final Integer z1 = parseInteger(parts[3]);
		final Integer x2 = parseInteger(parts[4]);
		final Integer y2 = parseInteger(parts[5]);
		final Integer z2 = parseInteger(parts[6]);
		final Integer flags = parseInteger(parts[8]);
		final Integer prots = parseInteger(parts[9]);

		if (Bukkit.getWorld(parts[0]) == null || x1 == null || y1 == null
				|| z1 == null || x2 == null || y2 == null || z2 == null
				|| flags == null || prots == null) {
			PVPArena.instance.getLogger().severe(arena.getName() + " caused an error while loading region " + regionName);
			throw new IllegalArgumentException(
					"Some of the parsed values are null!");
		}

		final PABlockLocation[] l = { new PABlockLocation(parts[0], x1, y1, z1),
				new PABlockLocation(parts[0], x2, y2, z2) };

		final ArenaRegionShape region = ArenaRegionShape.create(arena, regionName,
				shape, l);
		region.applyFlags(flags);
		region.applyProtections(prots);
		region.setType(RegionType.valueOf(parts[10]));
		region.saveToConfig();

		// "world,x1,y1,z1,x2,y2,z2,shape,FLAGS,PROTS,TYPE"

		return region;
	}

	public static Integer parseInteger(final String string) {
		try {
			return Integer.parseInt(string.trim());
		} catch (Exception e) {
			return null;
		}
	}

	public static Float parseFloat(final String string) {
		try {
			return Float.parseFloat(string.trim());
		} catch (Exception e) {
			return null;
		}
	}

	public static String parseToString(final PALocation loc) {
		final String[] result = new String[6];
		result[0] = String.valueOf(loc.getWorldName());
		result[1] = String.valueOf(loc.getBlockX());
		result[2] = String.valueOf(loc.getBlockY());
		result[3] = String.valueOf(loc.getBlockZ());
		result[4] = String.valueOf(loc.getYaw());
		result[5] = String.valueOf(loc.getPitch());
		// "world,x,y,z,yaw,pitch"
		return StringParser.joinArray(result, ",");
	}

	public static String parseToString(final PABlockLocation loc) {
		final String[] result = new String[4];
		result[0] = String.valueOf(loc.getWorldName());
		result[1] = String.valueOf(loc.getX());
		result[2] = String.valueOf(loc.getY());
		result[3] = String.valueOf(loc.getZ());
		// "world,x,y,z"
		return StringParser.joinArray(result, ",");
	}

	public static String parseToString(final ArenaRegionShape region,
			final Set<RegionFlag> flags, final Set<RegionProtection> protections) {
		final String[] result = new String[11];
		result[0] = region.getWorldName();
		result[1] = String.valueOf(region.getLocs()[0].getX());
		result[2] = String.valueOf(region.getLocs()[0].getY());
		result[3] = String.valueOf(region.getLocs()[0].getZ());
		result[4] = String.valueOf(region.getLocs()[1].getX());
		result[5] = String.valueOf(region.getLocs()[1].getY());
		result[6] = String.valueOf(region.getLocs()[1].getZ());
		result[7] = region.getShape().name();
		result[10] = region.getType().name();

		int sum = 0;

		for (RegionFlag f : flags) {
			sum += Math.pow(2, f.ordinal());
		}

		result[8] = String.valueOf(sum);

		sum = 0;

		for (RegionProtection p : protections) {
			sum += Math.pow(2, p.ordinal());
		}
		result[9] = String.valueOf(sum);

		// "world,x1,y1,z1,x2,y2,z2,shape,FLAGS,PROTS,TYPE"
		return StringParser.joinArray(result, ",");
	}
}
