package net.slipcor.pvparena.core;

import java.io.File;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.StatisticsManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * <pre>Language class</pre>
 * 
 * provides methods to display configurable texts
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class Language {

	public static enum MSG {
		ANNOUNCE_ARENA_STARTING("nulang.announce.arena", "Arena is starting! Type /pa %1% to join!"),

		ARENA_CREATE_DONE("nulang.arena.create.done", "arena '%1%' created!"),
		ARENA_DISABLE_DONE("nulang.arena.disable.done", "Arena disabled!"),
		ARENA_EDIT_DISABLED("nulang.arena.edit.disabled", "Disabled edit mode for arena: %1%"),
		ARENA_EDIT_ENABLED("nulang.arena.edit.enabled", "Enabled edit mode for arena: %1%"),
		ARENA_ENABLE_DONE("nulang.arena.enable.done", "Arena enabled!"),
		ARENA_LIST("nulang.arena.arenalist", "Arenas: &a%1%&r"),
		ARENA_REGION_SHAPE_UNKNOWN("nulang.arena.regionshapeunknown", "Arena Shape '%1%' unknown. consult the forums: http://goo.gl/IfLOh"),
		ARENA_RELOAD_DONE("nulang.arena.reload.done", "Arena reloaded!"),
		ARENA_REMOVE_DONE("nulang.arena.remove.done", "Arena removed: &e%1%&r"),
		ARENA_STARTING_IN("nulang.arena.startingin", "Enough players ready. Starting in %1% seconds!"),
		ARENA_STOP_DONE("nulang.arena.stop.done", "Arena force stopped!"),
		ARENA_TEAM_UNKNOWN("nulang.arena.teamunknown", "Arena Team '%1%' unknown!"),
		
		AUTOSETUP_AUTOMANUAL("nulang.autosetup.automanual", "Do you want the wizard to be &a%1%&r or &a%2%&r?"),
		AUTOSETUP_AUTOMATIC("nulang.autosetup.automatic", "automatic"),
		AUTOSETUP_MANUAL("nulang.autosetup.manual", "manual"),
		AUTOSETUP_MODESELECTED("nulang.autosetup.modeselected", "&a%1%&r mode selected!"),
		AUTOSETUP_WELCOME("nulang.autosetup.welcome", "Welcome to the PVP Arena setup wizard!\nPlease just type the colored answers into the chat. No commands needed!"),
		
		BLACKLIST_ADDED("nulang.blacklist.added", "Added &a%1%&r to &e%2%&r blacklist!"),
		BLACKLIST_ALLCLEARED("nulang.blacklist.allcleared", "All blacklists cleared!"),
		BLACKLIST_CLEARED("nulang.blacklist.cleared", "Blacklist &e%1%&r cleared!"),
		BLACKLIST_HELP("nulang.blacklist.help", "Usage: blacklist clear | blacklist [type] [clear|add|remove] [id]"),
		BLACKLIST_REMOVED("nulang.blacklist.removed", "Removed &a%1%&r from &e%2%&r blacklist!"),
		BLACKLIST_SHOW("nulang.blacklist.show", "Blacklist &e%1%&r:"),
		
		CLASS_PREVIEW("nulang.class.preview", "You are now previewing the class %1%"),
		CLASS_REMOVED("nulang.class.removed", "Class removed: %1%"),
		CLASS_SAVED("nulang.class.saved", "Class saved: %1%"),
		
		DEATHCAUSE_BLOCK_EXPLOSION("nulang.deathcause.BLOCK_EXPLOSION", "an explosion"),
		DEATHCAUSE_CONTACT("nulang.deathcause.CONTACT", "a cactus"),
		DEATHCAUSE_CUSTOM("nulang.deathcause.CUSTOM", "Herobrine"),
		DEATHCAUSE_DROWNING("nulang.deathcause.DROWNING", "water"),
		DEATHCAUSE_ENTITY_EXPLOSION("nulang.deathcause.ENTITY_EXPLOSION", "a creeper"),
		DEATHCAUSE_FALL("nulang.deathcause.FALL", "gravity"),
		DEATHCAUSE_FIRE_TICK("nulang.deathcause.FIRE_TICK", "fire"),
		DEATHCAUSE_FIRE("nulang.deathcause.FIRE", "a fire"),
		DEATHCAUSE_LAVA("nulang.deathcause.LAVA", "lava"),
		DEATHCAUSE_LIGHTNING("nulang.deathcause.LIGHTNING", "Thor"),
		DEATHCAUSE_MAGIC("nulang.deathcause.MAGIC", "Magical Powers"),
		DEATHCAUSE_POISON("nulang.deathcause.POISON", "Poison"),
		DEATHCAUSE_PROJECTILE("nulang.deathcause.PROJECTILE","something he didn't see coming"),
		DEATHCAUSE_STARVATION("nulang.deathcause.STARVATION", "hunger"),
		DEATHCAUSE_SUFFOCATION("nulang.deathcause.SUFFOCATION", "lack of air"),
		DEATHCAUSE_SUICIDE("nulang.deathcause.SUICIDE", "self"),
		DEATHCAUSE_VOID("nulang.deathcause.VOID", "the Void"),
		
		ERROR_ARENA_ALREADY_PART_OF("nulang.error.arena.alreadyplaying", "You are already part of &a%1%&r"),
		ERROR_ARENA_EXISTS("nulang.error.arenaexists", "Arena already exists!"),
		ERROR_ARENA_NOTFOUND("nulang.error.arenanotexists", "Arena does not exist: %1%"),
		ERROR_ARGUMENT_TYPE("nulang.error.argumenttype", "&cInvalid argument type:&r &e%1%&r is no proper &a%2%&r"),
		ERROR_ARGUMENT("nulang.error.argument", "&cArgument not recognized:&r %1% - possible arguments: &a%2%&r"),
		ERROR_AUTOSETUP_RUNNING("nulang.error.autosetup.running", "There is already an autosetup running! Player: %1%"),
		ERROR_BLACKLIST_UNKNOWN_SUBCOMMAND("nulang.error.blacklist.unknownsubcommand", "Unknown subcommand. Valid commands: &a%1%&r"),
		ERROR_BLACKLIST_UNKNOWN_TYPE("nulang.error.blacklist.unknowntype", "Unknown type. Valid types: &e%1%&r"),
		ERROR_CHOOSE_CLASS_FULL("nulang.error.classfull", "class is full!"),
		ERROR_CLASS_FULL("nulang.error.class.full", "The class &a%1%&r is full!"),
		ERROR_CLASS_NOT_FOUND("nulang.error.class.notfound", "Class not found: &a%1%&r"),
		ERROR_COMMAND_INVALID("nulang.error.invalidcmd", "Invalid command (%1%)"),
		ERROR_COMMAND_UNKNOWN("nulang.error.unknowncmd", "Unknown command"),
		ERROR_DISABLED("nulang.error.arenadisabled", "Arena disabled, please try again later!"),
		ERROR_ERROR("nulang.error.error", "&cError: %1%"),
		ERROR_FIGHT_IN_PROGRESS("nulang.error.fightinprogress", "A fight is already in progress!"),
		ERROR_GOAL_NOTFOUND("nulang.error.goal.goalnotfound", "Goal &a%1%&r unknown. Valid goals: &a%2%&r"),
		ERROR_INSTALL("nulang.error.install", "Error while installing &a%1%&r"),
		ERROR_INVALID_ARGUMENT_COUNT("nulang.error.invalid_argument_count", "&cInvalid number of arguments&r (%1% instead of %2%)!"),
		ERROR_INVALID_STATTYPE("nulang.error.invalidstattype", "Invalid statistics type: %1%"),
		ERROR_INVALID_VALUE("nulang.error.valuenotfound", "Invalid value: &a%1%&r!"),
		ERROR_INVENTORY_FULL("nulang.error.invfull", "Your inventory was full. You did not receive all rewards!"),
		ERROR_JOIN_ARENA_FULL("nulang.error.arenafull", "arena is full!"),
		ERROR_JOIN_NOTSELECT("nulang.error.notselectteam", "You cannot select a team to join! /pa [arenaname]"),
		ERROR_JOIN_PLEASESELECT("nulang.error.selectteam", "You must select a team to join! /pa [arenaname] [team]"),
		ERROR_JOIN_RANGE("nulang.error.joinrange", "You are too far away to join this arena!"),
		ERROR_JOIN_REGION("nulang.error.notjoinregion", "You are not in the join region! Move there to join!"),
		ERROR_JOIN_TEAM_FULL("nulang.error.teamfull", "team %1% is full!"),
		ERROR_TEAMNOTFOUND("nulang.error.join.teamnotfound", "Team not found: &a%1%&r"),
		ERROR_JOIN_VEHICLE("nulang.error.insidevehicle", "You cannot join while on a vehicle!"),
		ERROR_LOUNGEFREE("nulang.error.errorloungefree", "Error! Arena is not of type free. Use '[teamname]lounge'"),
		ERROR_MAT_NOT_FOUND("nulang.error.log.matnotfound", "Unrecognized material: %1%"),
		ERROR_NEGATIVES("nulang.error.valueneg", "Negative values: &c%1%&r"),
		ERROR_NO_FIGHT("nulang.error.nofight", "There is no fight in progress."),
		ERROR_NOPERM_CLASS("nulang.error.classperms", "You do not have permission for class &a%1%&r"),
		ERROR_NOPERM_JOIN("nulang.error.permjoin", "You don't have permission to join the arena!"),
		
		ERROR_NOPERM_X_ADMIN("nulang.nopermto.madmin", "administrate"),
		ERROR_NOPERM_X_CREATE("nulang.nopermto.create", "create an arena"),
		ERROR_NOPERM_X_DISABLE("nulang.nopermto.disable", "disable"),
		ERROR_NOPERM_X_EDIT("nulang.nopermto.edit", "edit an arena"),
		ERROR_NOPERM_X_ENABLE("nulang.nopermto.enable", "enable"),
		ERROR_NOPERM_X_JOIN("nulang.nopermto.nopermjoin", "join an arena"),
		ERROR_NOPERM_X_RELOAD("nulang.nopermto.reload", "reload"),
		ERROR_NOPERM_X_REMOVE("nulang.nopermto.remove", "remove an arena"),
		ERROR_NOPERM_X_SET("nulang.nopermto.set", "set a config node"),
		ERROR_NOPERM_X_SETUP("nulang.nopermto.setup", "setup an arena"),
		ERROR_NOPERM_X_TP("nulang.nopermto.teleport", "teleport to an arena spawn"),
		
		ERROR_NOPERM("nulang.error.noperm", "&cNo permission to %1%"),
		ERROR_NOT_IN_ARENA("nulang.error.notinarena", "You are not part of an arena!"),
		ERROR_NOT_NUMERIC("nulang.error.notnumeric", "&cArgument not numeric:&r %1%"),
		ERROR_NOT_THE_SAME_WORLD("nulang.error.notsameworld", "Not in the same world as the arena (%1%)!"),
		ERROR_ONLY_PLAYERS("nulang.error.onlyplayers", "&cThis command can only be used by players!"),
		ERROR_POSITIVES("nulang.error.positives", "Positive values: &b%1%&r"),
		ERROR_READY_0_ONE_PLAYER_NOT_READY("nulang.error.ready.notready0", "At least one player is not ready!"),
		ERROR_READY_1_ALONE("nulang.error.ready.notready1", "You are alone in the arena!"),
		ERROR_READY_2_TEAM_ALONE("nulang.error.ready.notready2", "Your team is alone in the arena!"),
		ERROR_READY_3_TEAM_MISSING_PLAYERS("nulang.error.ready.notready3", "A team is missing players!"),
		ERROR_READY_4_MISSING_PLAYERS("nulang.error.ready.notready4", "The arena is missing players!"),
		ERROR_READY_5_ONE_PLAYER_NO_CLASS("nulang.error.ready.notready5", "At least one player has not chosen a class!"),
		ERROR_READY_NOCLASS("nulang.error.ready.noclass", "You don't have a class!"),
		ERROR_READY("nulang.error.ready.error", "The arena is not ready! %1%"),
		ERROR_REGION_BEING_CREATED("nulang.error.region.beingcreated", "A region is already being created: %1%"),
		ERROR_REGION_FLAG_NOTFOUND("nulang.error.region.flagnotfound", "RegionFlag &a%1%&r unknown!"),
		ERROR_REGION_NOT_BEING_CREATED("nulang.error.regionnotbeingcreated", "A region is not being created!"),
		ERROR_REGION_NOTFOUND("nulang.error.region.notfound", "Region &a%1%&r not found!"),
		ERROR_REGION_PROTECTION_NOTFOUND("nulang.error.region.protectionnotfound", "RegionProtection &a%1%&r unknown!"),
		ERROR_REGION_REMOVE_NOTFOUND("nulang.error.regionnotremoved", "There is no region setup."),
		ERROR_REGION_SELECT_2("nulang.error.select2","Select two points before trying to save."),
		ERROR_REGION_YOUSELECT("nulang.error.region.youselect", "You are already selecting a region for an arena!"),
		ERROR_SPAWN_UNKNOWN("nulang.error.spawn.unknown", "Unknown spawn: &a%1%&r"),
		ERROR_SPAWNFREE("nulang.error.spawnfree", "Error! Arena is of type free. Use 'spawnX' where X is a digit or letter!"),
		ERROR_STATS_FILE("nulang.error.statsfile", "Error while reading the stats file!"),
		ERROR_UNINSTALL("nulang.error.uninstall", "Error while uninstalling &a%1%&r"),
		ERROR_WHITELIST_UNKNOWN_SUBCOMMAND("nulang.error.whitelist.unknownsubcommand", "Unknown subcommand. Valid commands: &a%1%&r"),
		ERROR_WHITELIST_UNKNOWN_TYPE("nulang.error.whitelist.unknowntype", "Unknown type. Valid types: &e%1%&r"),
		
		FIGHT_BEGINS("nulang.fight.begins", "Let the fight begin!"),
		FIGHT_KILLED_BY_REMAINING("nulang.fight.killedbyremaining", "%1% has been killed by %2%! %3% lives remaining."),
		FIGHT_KILLED_BY_REMAINING_TEAM("nulang.fight.killedbyremaining", "%1% has been killed by %2%! %3% lives remaining for %4%."),
		FIGHT_KILLED_BY("nulang.fight.killedby", "%1% has been killed by %2%!"),
		FIGHT_PLAYER_LEFT("nulang.fight.playerleft", "%1% has left the fight!"),
		
		FORCESTOP_DONE("nulang.forcestop", "You have forced the fight to stop."),
		
		GAMEMODE_FREE("nulang.gamemode.free", "Game mode &afree for all&r set for arena &a%1%&r!"),
		GAMEMODE_TEAM("nulang.gamemode.team", "Game mode &ateam&r set for arena &a%1%&r!"),

		GOAL_ADDED("nulang.goal.added", "Goal added: &a%1%&r"),
		GOAL_INSTALLING("nulang.goal.installing", "Install goals by command: &a/pa install [goalname]&r"),
		GOAL_REMOVED("nulang.goal.removed", "Goal removed: &a%1%&r"),

		INFO_GOAL_ACTIVE("nulang.info.goal_active", "Goal: &a%1%&r"),
		INFO_GOAL_INACTIVE("nulang.info.goal_inactive", "Goal: &b%1%&r &7== INACTIVE =="),
		INFO_HEAD_HEADLINE("nulang.info.head_headlin", "Arena Information about: &a%1%&r | [&a%2%&r]"),
		INFO_HEAD_TEAMS("nulang.info.head_teams", "Teams: &a%1%&r"),
		INFO_MOD_ACTIVE("nulang.info.mod_active", "Module: &a%1%&r"),
		INFO_MOD_INACTIVE("nulang.info.mod_inactive", "Module: &b%1%&r &7== INACTIVE =="),
		
		INSTALL_DONE("nulang.install.installed", "installed: &a%1%&r"),
		
		LIST_ARENAS("nulang.list.arenas", "Available arenas: %1%"),
		LIST_DEAD("nulang.list.dead", "Dead: %1%"),
		LIST_FIGHTING("nulang.list.fighting", "Fighting: %1%"),
		LIST_LOST("nulang.list.lost", "Lost: %1%"),
		LIST_LOUNGE("nulang.list.lounge", "Lounge: %1%"),
		LIST_NULL("nulang.list.null", "Glitched: %1%"),
		LIST_PLAYERS("nulang.list.players", "Players: %1%"),
		LIST_READY("nulang.list.ready", "Ready: %1%"),
		LIST_WARM("nulang.list.warm", "Warm: %1%"),
		LIST_WATCHING("nulang.list.watching", "Watching: %1%"),
		
		LOG_PLUGIN_DISABLED("nulang.log.plugindisabled", "disabled (version %1%)"),
		LOG_PLUGIN_ENABLED("nulang.log.pluginenabled", "enabled (version %1%)"),
		LOG_TRACKER_DISABLED("nulang.log.trickerdisabled", "Plugin tracking disabled. See you soon?"),
		LOG_TRACKER_ENABLED("nulang.log.trackerenabled", "Plugin tracking enabled. Set stats: false inside the main config to disable."),
		LOG_UPDATE_DISABLED("nulang.log.updatedisabled", "Updates deactivated. Please check dev.bukkit for updates."),
		LOG_UPDATE_ENABLED("nulang.log.updateenabled", "Checking for updates..."),
		LOG_WARNING("nulang.log.warning", "%1%"),
		
		MESSAGES_GLOBALOFF("nulang.messages.globaloff", "You are now only talking inside the arena!"),
		MESSAGES_GLOBALON("nulang.messages.globalon", "You are now talking to the public!"),
		
		NO_PLAYER("nulang.noplayer", "No player in the PVP arena."),
		
		NOTICE_AWARDED("nulang.notice.awarded", "You have been awarded %1%"),
		NOTICE_NO_DROP_ITEM("nulang.notice.nodropitem", "Not so fast! No Cheating!"),
		NOTICE_NO_TELEPORT("nulang.notice.noteleport", "Please use '/pa leave' to exit the fight!"),
		NOTICE_WAITING_EQUAL("nulang.notice.waitingequal", "Waiting for the teams to have equal player number!"),
		NOTICE_WAITING_FOR_ARENA("nulang.notice.waitingforarena", "Waiting for a running arena to finish!"),
		NOTICE_WELCOME_SPECTATOR("nulang.notice.welcomespec", "Welcome to the spectator's area! /pa bet [name] [amount] to bet on team or player"),
		NOTICE_YOU_DEATH("nulang.notice.youdeath", "You entered a DEATH region. Goodbye!"),
		NOTICE_YOU_ESCAPED("nulang.notice.youescaped", "You escaped the battlefield. Goodbye!"),
		NOTICE_YOU_LEFT("nulang.notice.youleft", "You left the arena!"),
		NOTICE_YOU_NOCAMP("nulang.notice.younocamp", "You are in a NOCAMP region. Move!"),
		
		PLAYER_JOINED_TEAM("nulang.playerjoined", "%1% has joined team %2%"),
		PLAYER_HAS_WON("nulang.playerhaswon", "%1% is the Champion!"),
		
		PLAYERS_NOTREADY("nulang.notreadyplayers", "Players not ready"),
		
		PLAYERS("nulang.players", "Players"),

		READY_LIST("nulang.ready.list", "Players: %1%"),
		READY_DONE("nulang.ready.done", "You have been flagged as ready!"),
		
		REGION_FLAG_ADDED("nulang.region.flag.added", "Region flag added: &a%1%&r"),
		REGION_FLAG_REMOVED("nulang.region.flag.removed", "Region flag removed: &a%1%&r"),
		REGION_HEIGHT("nulang.region.height", "Region height set to: &a%1%&r"),
		REGION_POS1("nulang.region.pos1", "First position set."),
		REGION_POS2("nulang.region.pos2", "Second position set."),
		REGION_PROTECTION_ADDED("nulang.region.protection_added", "RegionProtection added: &a%1%&r"),
		REGION_PROTECTION_REMOVED("nulang.region.protection_removed", "RegionProtection removed: &a%1%&r"),
		REGION_RADIUS("nulang.region.radius", "Region radius set to: &a%1%&r"),
		REGION_REMOVED("nulang.region.removed", "Region removed: %1%"),
		REGION_SAVED("nulang.region.saved", "Region saved."),
		REGION_SELECT("nulang.region.select", "Select two points with your wand item, left click first and then right click!"),
		REGION_SETTING("nulang.region.setting", "Setting region enabled."),
		REGION_YOUSELECT("nulang.region.youselect", "You are now selecting a region for arena &a%1%&r!"),
		
		RELOAD_DONE("nulang.reloaded", "Config reloaded!"),
		
		SET_DONE("nulang.set.done", "&a%1%&r set to &e%2%&r!"),
		SET_HELP("nulang.set.help", "use /pa {arenaname} set [page] to get a node list"),
		SET_UNKNOWN("nulang.set.unknown", "Unknown node: &e%1%&r!"),
		
		SETOWNER_DONE("nulang.setowner.done", "&a%1%&r is now owner of arena &a%2%&r!"),

		SPAWN_FREELOUNGE("nulang.spawn.freelounge", "Lounge set"),
		SPAWN_TEAMLOUNGE("nulang.spawn.teamlounge", "Lounge set: %1%"),
		SPAWN_NOTSET("nulang.spawn.notset", "Spawn not set: &a%1%&r"),
		SPAWN_REMOVED("nulang.spawn.removed", "Spawn removed: &a%1%&r"),
		SPAWN_SET("nulang.spawn.set", "Spawn set: &a%1%&r"),

		STATS_FILE_DONE("nulang.stats.filedone", "Statistics file loaded!"),
		
		STATTYPE_DAMAGE("nulang.stattype.DAMAGE", StatisticsManager.type.DAMAGE.getName()),
		STATTYPE_DAMAGETAKE("nulang.stattype.DAMAGETAKE", StatisticsManager.type.DAMAGETAKE.getName()),
		STATTYPE_DEATHS("nulang.stattype.DEATHS", StatisticsManager.type.DEATHS.getName()),
		STATTYPE_KILLS("nulang.stattype.KILLS", StatisticsManager.type.KILLS.getName()),
		STATTYPE_LOSSES("nulang.stattype.LOSSES", StatisticsManager.type.LOSSES.getName()),
		STATTYPE_MAXDAMAGE("nulang.stattype.MAXDAMAGE", StatisticsManager.type.MAXDAMAGE.getName()),
		STATTYPE_MAXDAMAGETAKE("nulang.stattype.MAXDAMAGETAKE", StatisticsManager.type.MAXDAMAGETAKE.getName()),
		STATTYPE_NULL("nulang.stattype.NULL", StatisticsManager.type.NULL.getName()),
		STATTYPE_WINS("nulang.stattype.WINS", StatisticsManager.type.WINS.getName()),
		
		TEAM_HAS_WON("nulang.team.haswon", "%1% are the Champions!"),
		TEAM_READY("nulang.team.ready", "%1% team is ready!"),
		
		TIME_MINUTES("nulang.time.minutes", "minutes"),
		TIME_SECONDS("nulang.time.seconds", "seconds"),
		
		TIMER_COUNTDOWN_INTERRUPTED("nulang.timer.countdowninterrupt", "Countdown interrupted! Hit the ready block!"),
		TIMER_ENDING_IN("nulang.timer.ending", "The match will end in %1%!"),
		TIMER_RESETTING_IN("nulang.timer.resetting", "The arena will reset in %1%!"),
		TIMER_STARTING_IN("nulang.timer.starting", "Starting in %1%!"),
		TIMER_WARMINGUP("nulang.timer.warmingup", "Warming up... %1%!"),
		
		UNINSTALL_DONE("nulang.uninstall.done", "uninstalled: &a%1%&r"),
		
		WHITELIST_ADDED("nulang.whitelist.added", "Added &a%1%&r to &e%2%&r whitelist!"),
		WHITELIST_ALLCLEARED("nulang.whitelist.allcleared", "All whitelist cleared!"),
		WHITELIST_CLEARED("nulang.whitelist.cleared", "Whitelist &e%1%&r cleared!"),
		WHITELIST_HELP("nulang.whitelist.help", "Usage: blacklist clear | blacklist [type] [clear|add|remove] [id]"),
		WHITELIST_REMOVED("nulang.whitelist.removed", "Removed &a%1%&r from &e%2%&r whitelist!"),
		WHITELIST_SHOW("nulang.whitelist.show", "Whitelist &e%1%&r:");
		
		private String node;
		private String value;
		
		public static MSG getByNode(String node) {
			for (MSG m : MSG.values()) {
				if (m.getNode().equals(node)) {
					return m;
				}
			}
			return null;
		}
		
		private MSG(String node, String value) {
			this.node = node;
			this.value = value;
		}
		
		public String getNode() {
			return node;
		}
		
		public void setNode(String s) {
			node = s;
		}
		
		public void setValue(String s) {
			value = s;
		}
		
		@Override
	    public String toString() {
	        return value;
	    }
	}
	
	/**
	 * create a language manager instance
	 */
	public static void init(String s) {
		PVPArena.instance.getDataFolder().mkdir();
		File configFile = new File(PVPArena.instance.getDataFolder().getPath() + "/lang_" + s + ".yml");
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				Bukkit.getLogger().severe(
						"[PVP Arena] Error when creating language file.");
			}

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (MSG m : MSG.values()) {
			config.addDefault(m.getNode(), m.toString());
		}
		
		config.options().copyDefaults(true);
		try {
			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * read a node from the config and log its value
	 * 
	 * @param s
	 *            the node name
	 */
	public static void log_info(MSG m) {
		String var = m.toString();
		Bukkit.getLogger().info("[PVP Arena] " + var);
		// log map value
	}
	
	/**
	 * read a node from the config and log its value after replacing
	 * 
	 * @param s
	 *            the node name
	 * @param arg
	 *            a string to replace
	 */
	public static void log_info(MSG m, String arg) {
		String var = m.toString();
		Bukkit.getLogger().info("[PVP Arena] " + var.replace("%1%", arg));
		// log replaced map value
	}
	
	/**
	 * read a node from the config and log its value after replacing
	 * 
	 * @param s
	 *            the node name
	 * @param arg
	 *            a string to replace
	 */
	public static void log_error(MSG m, String arg) {
		String var = m.toString();
		Bukkit.getLogger().severe("[PVP Arena] " + var.replace("%1%", arg));
		// log replaced map value
	}
	
	/**
	 * read a node from the config and log its value after replacing
	 * 
	 * @param s
	 *            the node name
	 * @param arg
	 *            a string to replace
	 */
	public static void log_warning(MSG m, String arg) {
		String var = m.toString();
		Bukkit.getLogger().warning("[PVP Arena] " + var.replace("%1%", arg));
		// log replaced map value
	}

	public static String parse(Arena arena, String arg) {
		return arena.getArenaConfig().getString(arg);
	}

	/**
	 * read a node from the config and return its value
	 * 
	 * @param s
	 *            the node name
	 * @return the node string
	 */
	public static String parse(MSG m) {
		return StringParser.colorize(m.toString());
	}

	/**
	 * read a node from the config and return its value after replacing
	 * 
	 * @param s
	 *            the node name
	 * @param arg
	 *            a string to replace
	 * @return the replaced node string
	 */
	public static String parse(MSG m, String arg) {
		String var = m.toString();
		return StringParser.colorize(var.replace("%1%", arg));
	}

	/**
	 * read a node from the config and return its value after replacing
	 * 
	 * @param s
	 *            the node name
	 * @param arg1
	 *            a string to replace
	 * @param arg2
	 *            a string to replace
	 * @return the replaced node string
	 */
	public static String parse(MSG m, String arg1, String arg2) {
		String var = m.toString().replace("%2%", arg2);
		return StringParser.colorize(var.replace("%1%", arg1));
	}

	/**
	 * read a node from the config and return its value after replacing
	 * 
	 * @param s
	 *            the node name
	 * @param arg1
	 *            a string to replace
	 * @param arg2
	 *            a string to replace
	 * @param arg3
	 *            a string to replace
	 * @return the replaced node string
	 */
	public static String parse(MSG m, String arg1, String arg2, String arg3) {
		String var = m.toString().replace("%2%", arg2);
		var = var.replace("%3%", arg3);
		return StringParser.colorize(var.replace("%1%", arg1));
	}

	public static String parse(MSG m, String arg1,
			String arg2, String arg3, String arg4) {
		String var = m.toString().replace("%2%", arg2);
		var = var.replace("%3%", arg3);
		var = var.replace("%4%", arg4);
		return StringParser.colorize(var.replace("%1%", arg1));
	}
}
