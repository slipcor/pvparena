package net.slipcor.pvparena.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import net.slipcor.pvparena.managers.Statistics;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * language manager class
 * 
 * -
 * 
 * provides methods to display configurable texts
 * 
 * @author slipcor
 * 
 * @version v0.6.36
 * 
 */
public class Language {
	static Map<String, Object> lang = null; // game language map
	static Map<String, Object> log = null; // log language map

	/**
	 * create a language manager instance
	 */
	public static void init(String s) {
		s = s.equals("en") ? "" : "_" + s;
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/lang" + s + ".yml");
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
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidConfigurationException e1) {
			e1.printStackTrace();
		}

		config.addDefault("log.matnotfound", "Unrecognized material: %1%");
		config.addDefault("log.iconomyon", "<3 eConomy");
		config.addDefault("log.iconomyoff", "</3 eConomy");
		config.addDefault("log.enabled", "enabled (version %1%)");
		config.addDefault("log.disabled", "disabled (version %1%)");
		config.addDefault("log.noperms",
				"Permissions plugin not found, defaulting to OP.");
		config.addDefault("log.nospout",
				"Spout not found, you are missing some features ;)");
		config.addDefault("log.spout", "Hooking into Spout!");
		config.addDefault("log.warn", "%1%");

		config.addDefault("lang.playerleave", "%1% has left the fight!");
		config.addDefault("lang.youleave", "You have left the fight!");
		config.addDefault("lang.dropitem", "Not so fast! No Cheating!");
		config.addDefault("lang.usepatoexit",
				"Please use '/pa leave' to exit the fight!");
		config.addDefault("lang.pos1", "First position set.");
		config.addDefault("lang.pos2", "Second position set.");
		config.addDefault("lang.classperms",
				"You don't have permission for that class.");
		config.addDefault("lang.waitequal",
				"Waiting for the teams to have equal player number!");
		config.addDefault("lang.ready", "%1% team is ready!");
		config.addDefault("lang.begin", "Let the fight begin!");
		config.addDefault("lang.killedby", "%1% has been killed by %2%!");
		config.addDefault("lang.killedbylives",
				"%1% has been killed by %2%! %3% lives remaining.");
		config.addDefault("lang.onlyplayers",
				"Only players may access this command!");
		config.addDefault("lang.arenadisabled",
				"Arena disabled, please try again later!");
		config.addDefault("lang.arenanotsetup",
				"All waypoints must be set up first. %1%");
		config.addDefault("lang.permjoin",
				"You don't have permission to join the arena!");
		config.addDefault("lang.alreadyjoined", "You already joined!");
		config.addDefault("lang.fightinprogress",
				"A fight is already in progress!");
		config.addDefault("lang.notenough", "You don't have %1%.");
		config.addDefault("lang.youjoined", "Welcome! You are on team %1%");
		config.addDefault("lang.playerjoined", "%1% has joined team %2%");
		config.addDefault("lang.nopermto", "You don't have permission to %1%");
		config.addDefault("lang.enable", "enable");
		config.addDefault("lang.disable", "disable");
		config.addDefault("lang.reload", "reload");
		config.addDefault("lang.enabled", "Enabled!");
		config.addDefault("lang.disabled", "Disabled!");
		config.addDefault("lang.admin", "administrate");
		config.addDefault("lang.reloaded", "Config reloaded!");
		config.addDefault("lang.noplayer", "No player in the PVP arena.");
		config.addDefault("lang.players", "Players");
		config.addDefault(
				"lang.specwelcome",
				"Welcome to the spectator's area! /pa bet [name] [amount] to bet on team or player");
		config.addDefault("lang.setspectator", "Spectator spawn set.");
		config.addDefault("lang.setexit", "Exit spawn set.");
		config.addDefault("lang.forcestop",
				"You have forced the fight to stop.");
		config.addDefault("lang.nofight", "There is no fight in progress.");
		config.addDefault("lang.invalidcmd", "Invalid command (%1%)");
		config.addDefault("lang.betnotyours",
				"Cannot place bets on your own match!");
		config.addDefault("lang.betoptions",
				"You can only bet on team name or arena player!");
		config.addDefault("lang.wrongamount",
				"Bet amount must be between %1% and %2%!");
		config.addDefault("lang.invalidamount", "Invalid amount: %1%");
		config.addDefault("lang.betplaced", "Your bet on %1% has been placed.");
		config.addDefault("lang.regionset", "Setting region enabled.");
		config.addDefault("lang.regionsaved", "Region saved.");
		config.addDefault("lang.regionremoved", "Region removed.");
		config.addDefault("lang.regionnotremoved", "There is no region setup.");
		config.addDefault("lang.youwon", "You won %1%");
		config.addDefault("lang.awarded", "You have been awarded %1%");
		config.addDefault("lang.invfull",
				"Your inventory was full. You did not receive all rewards!");
		config.addDefault("lang.arenaexists", "Arena already exists!");
		config.addDefault("lang.arenanotexists", "Arena does not exist: %1%");
		config.addDefault("lang.regionalreadybeingset",
				"A region is already being created: %1%");
		config.addDefault("lang.regionnotbeingset",
				"A region is not being created!");
		config.addDefault("lang.notinarena", "You are not part of an arena!");
		config.addDefault("lang.arenas", "Available arenas: %1%");
		config.addDefault("lang.setup", "setup an arena");
		config.addDefault("lang.create", "create an arena");
		config.addDefault("lang.created", "arena '%1%' created!");
		config.addDefault("lang.remove", "remove an arena");
		config.addDefault("lang.removed", "arena '%1%' removed!");
		config.addDefault("lang.youjoinedfree",
				"Welcome to the FreeFight Arena");
		config.addDefault("lang.playerjoinedfree",
				"%1% has joined the FreeFight Arena");
		config.addDefault("lang.setspawn", "Spawn set: %1%");
		config.addDefault("lang.setflag", "Flag set: %1%");
		config.addDefault("lang.setlounge", "Lounge set.");
		config.addDefault("lang.selectteam",
				"You must select a team to join! /pa [arenaname] [team]");
		config.addDefault("lang.notselectteam",
				"You cannot select a team to join! /pa [arenaname]");
		config.addDefault("lang.joinrange",
				"You are too far away to join this arena!");
		config.addDefault("lang.playerpowerup",
				"Player %1% receives powerup %2%!");
		config.addDefault("lang.serverpowerup", "Powerup %1% deployed!");
		config.addDefault("lang.flaggrab",
				"Player %1% grabbed the flag of team %2%!");
		config.addDefault("lang.flaghomeleft",
				"Player %1% brought home the flag of team %2%! Lives left: %3%");
		config.addDefault("lang.flagsave",
				"Player %1% dropped the flag of team %2%!");
		config.addDefault("lang.teamhaswon", "%1% are the Champions!");
		config.addDefault("lang.playerhaswon", "%1% is the Champion!");
		config.addDefault("lang.notsameworld",
				"Not in the same world as the arena (%1%)!");

		config.addDefault("lang.checkregionerror",
				"Waiting for a running arena to finish!");

		config.addDefault("lang.pumpkinhomeleft",
				"Player %1% brought home the pumpkin of team %2%! Lives left: %3%");
		config.addDefault("lang.pumpkingrab",
				"Player %1% grabbed the pumpkin of team %2%!");
		config.addDefault("lang.pumpkinsave",
				"Player %1% dropped the pumpkin of team %2%!");
		config.addDefault("lang.setpumpkin", "Pumpkin set: %1%");
		config.addDefault("lang.tosetpumpkin", "Pumpkin to set: %1%");
		config.addDefault("lang.tosetflag", "Flag to set: %1%");

		config.addDefault("lang.notready", "At least one player is not ready!");
		config.addDefault("lang.notready1", "You are alone in the arena!");
		config.addDefault("lang.notready2", "Your team is alone in the arena!");
		config.addDefault("lang.notready3", "A team is missing players!");
		config.addDefault("lang.notready4", "The arena is missing players!");
		config.addDefault("lang.notready5",
				"At least one player has not chosen a class!");

		config.addDefault("lang.teamfull", "team %1% is full!");
		config.addDefault("lang.arenafull", "arena is full!");
		config.addDefault("lang.classfull", "class is full!");

		config.addDefault("lang.errorcustomflag",
				"Error! Arena is not of type ctf.");
		config.addDefault("lang.errorcustompumpkin",
				"Error! Arena is not of type pumpkin.");
		config.addDefault("lang.errorspawnfree",
				"Error! Arena is of type free. Use 'spawnX' where X is a digit or letter!");
		config.addDefault("lang.errorloungefree",
				"Error! Arena is not of type free. Use '[teamname]lounge'");

		config.addDefault("lang.createleaderboard", "create a leaderboard");
		config.addDefault("lang.boardexists", "Leaderboard already exists!'");

		config.addDefault("lang.flagnotsafe",
				"Your flag is taken! Cannot bring back an enemy flag!'");
		config.addDefault("lang.pumpkinnotsafe",
				"Your pumpkin is taken! Cannot bring back an enemy pumpkin!'");

		config.addDefault("lang.frag",
				"%1% killed another player! Total frags: %2%.");
		config.addDefault("lang.notjoinregion",
				"You are not in the join region! Move there to join!");

		config.addDefault("lang.edit", "edit an arena");
		config.addDefault("lang.edittrue", "Enabled edit mode for arena: %1%");
		config.addDefault("lang.editfalse", "Disabled edit mode for arena: %1%");

		config.addDefault("lang.joinpay", "You paid %1% to join the arena!");
		config.addDefault("lang.insidevehicle",
				"You cannot join while on a vehicle!");
		config.addDefault("lang.starting",
				"Enough players ready. Starting in 5 seconds!");

		config.addDefault("lang.sortingby", "Arena Board now sorted by %1%");
		config.addDefault("lang.invalidstattype",
				"Invalid statistics type: %1%");

		for (Statistics.type t : Statistics.type.values()) {
			config.addDefault("lang." + t, t.getName());
		}

		/**
		 * death causes : "player was killed by ****"
		 */

		config.addDefault("lang.BLOCK_EXPLOSION".toLowerCase(), "an explosion");
		config.addDefault("lang.CONTACT".toLowerCase(), "a cactus");
		config.addDefault("lang.CUSTOM".toLowerCase(), "Herobrine");
		config.addDefault("lang.DROWNING".toLowerCase(), "water");
		config.addDefault("lang.ENTITY_EXPLOSION".toLowerCase(), "a creeper");
		config.addDefault("lang.FALL".toLowerCase(), "gravity");
		config.addDefault("lang.FIRE".toLowerCase(), "a fire");
		config.addDefault("lang.FIRE_TICK".toLowerCase(), "fire");
		config.addDefault("lang.LAVA".toLowerCase(), "lava");
		config.addDefault("lang.LIGHTNING".toLowerCase(), "Thor");
		config.addDefault("lang.MAGIC".toLowerCase(), "Magical Powers");
		config.addDefault("lang.POISON".toLowerCase(), "Poison");
		config.addDefault("lang.PROJECTILE".toLowerCase(),
				"something he didn't see coming");
		config.addDefault("lang.STARVATION".toLowerCase(), "hunger");
		config.addDefault("lang.SUFFOCATION".toLowerCase(), "lack of air");
		config.addDefault("lang.SUICIDE".toLowerCase(), "self");
		config.addDefault("lang.VOID".toLowerCase(), "the Void");


		config.addDefault("lang.domscore",
				"Team %1% scored a point by holding a flag!");
		config.addDefault("lang.domclaiming",
				"Team %1% is claiming a flag!");
		config.addDefault("lang.domunclaiming",
				"A flag claimed by Team %1% is being unclaimed!");
		config.addDefault("lang.domunclaimingby",
				"A flag claimed by Team %1% is being unclaimed by %2%!");
		config.addDefault("lang.spawnremoved", "Spawn removed: %1%");
		config.addDefault("lang.regionremoved", "Region removed: %1%");

		config.addDefault("lang.joinarena",
				"Arena is starting! Type /pa %1% to join!");
		
		config.options().copyDefaults(true);
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// write contents to maps
		lang = (Map<String, Object>) config.getConfigurationSection("lang")
				.getValues(true);
		log = (Map<String, Object>) config.getConfigurationSection("log")
				.getValues(true);

	}

	/**
	 * read a node from the config and return its value
	 * 
	 * @param s
	 *            the node name
	 * @return the node string
	 */
	public static String parse(String s) {
		return (String) lang.get(s); // hand over map value
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
	public static String parse(String s, String arg) {
		String var = (String) lang.get(s);
		return var.replace("%1%", arg); // hand over replaced map value
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
	public static String parse(String s, String arg1, String arg2) {
		String var = ((String) lang.get(s)).replace("%2%", arg2);
		return var.replace("%1%", arg1); // hand over replaced map value
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
	public static String parse(String s, String arg1, String arg2, String arg3) {
		String var = ((String) lang.get(s)).replace("%2%", arg2);
		var = var.replace("%3%", arg3);
		return var.replace("%1%", arg1); // hand over replaced map value
	}

	/**
	 * read a node from the config and log its value after replacing
	 * 
	 * @param s
	 *            the node name
	 * @param arg
	 *            a string to replace
	 */
	public static void log_error(String s, String arg) {
		String var = (String) log.get(s);
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
	public static void log_warning(String s, String arg) {
		String var = (String) log.get(s);
		Bukkit.getLogger().warning("[PVP Arena] " + var.replace("%1%", arg));
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
	public static void log_info(String s, String arg) {
		String var = (String) log.get(s);
		Bukkit.getLogger().info("[PVP Arena] " + var.replace("%1%", arg));
		// log replaced map value
	}

	/**
	 * read a node from the config and log its value
	 * 
	 * @param s
	 *            the node name
	 */
	public static void log_info(String s) {
		String var = (String) log.get(s);
		Bukkit.getLogger().info("[PVP Arena] " + var);
		// log map value
	}
}
