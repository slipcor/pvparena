/*
 * language manager class
 * 
 * author: slipcor
 * 
 * version: vv0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     0.3.10 - CraftBukkit #1337 config version, rewrite
 *     v0.3.9 - Permissions, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.6 - CTF Arena
 *     v0.3.5 - Powerups!!
 *     v0.3.4 - Rewrite
 *     v0.3.1 - New Arena! FreeFight
 *     v0.3.0 - Multiple Arenas
 * 	   v0.2.1 - cleanup, comments
 * 	   v0.2.0 - language support
 */

package net.slipcor.pvparena.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import net.slipcor.pvparena.PVPArena;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

//TODO maybe add the respective arena name somewhere? 

public class LanguageManager {
	Map<String, Object> lang = null; // game language map
	Map<String, Object> log = null; // log language map

	public LanguageManager() {
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/lang.yml");
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				PVPArena.instance.log
						.severe("[PVP Arena] Error when creating language file.");
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

		config.addDefault("log.filecreateerror", "Error creating %1% file.");
		config.addDefault("log.teamnotfound", "Unrecognized team: %1%");
		config.addDefault("log.matnotfound", "Unrecognized material: %1%");
		config.addDefault("log.iconomyon", "<3 iConomy");
		config.addDefault("log.iconomyoff", "</3 iConomy");
		config.addDefault("log.nospout",
				"Spout not found, you are missing some features ;)");
		config.addDefault("log.enabled", "enabled (version %1%)");
		config.addDefault("log.disabled", "disabled (version %1%)");
		config.addDefault("log.noperms",
				"Permissions plugin not found, defaulting to OP.");

		config.addDefault("lang.playerleave", "%1% has left the fight!");
		config.addDefault("lang.youleave", "You have left the fight!");
		config.addDefault("lang.dropitem", "Not so fast! No Cheating!");
		config.addDefault("lang.usepatoexit",
				"Please use '/pa leave' to exit the fight!");
		config.addDefault("lang.pos1", "First position set.");
		config.addDefault("lang.pos2", "Second position set.");
		config.addDefault("lang.classperms",
				"You don't have permission for that class.");
		config.addDefault("lang.toomanyplayers",
				"There are too many of this class, pick another class.");
		config.addDefault("lang.notready", "Not everyone has picked a class!");
		config.addDefault("lang.waitequal",
				"Waiting for the teams to have equal player number!");
		config.addDefault("lang.ready", "%1% team is ready!");
		config.addDefault("lang.begin", "Let the fight begin!");
		config.addDefault("lang.killed", "%1% has been killed!");
		config.addDefault("lang.lostlife",
				"%1% has lost a life! %2% remaining.");
		config.addDefault("lang.onlyplayers",
				"Only players may access this command!");
		config.addDefault("lang.arenadisabled",
				"Arena disabled, please try again later!");
		config.addDefault("lang.arenanotsetup",
				"All waypoints must be set up first. %1%");
		config.addDefault("lang.permjoin",
				"You don't have permission to join the arena!");
		config.addDefault("lang.selectteam",
				"You must select a team to join! /pa [arenaname] [team]");
		config.addDefault("lang.notselectteam",
				"You cannot select a team to join! /pa [arenaname]");
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
		config.addDefault("lang.reloaded", "Config reloaded!");
		config.addDefault("lang.noplayer", "No player in the PVP arena.");
		config.addDefault("lang.players", "Players");
		config.addDefault(
				"lang.specwelcome",
				"Welcome to the spectator's area! /pa bet [name] [amount] to bet on team or player");
		config.addDefault("lang.teamstat", "%1% %2% wins, %3% losses");
		config.addDefault("lang.top5win", "Top 5 winners");
		config.addDefault("lang.top5lose", "Top 5 losers");
		config.addDefault("lang.wins", "wins");
		config.addDefault("lang.losses", "losses");
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
		config.addDefault("lang.invalidamount", "Invalid amount: %1%");
		config.addDefault("lang.betplaced", "Your bet on %1% has been placed.");
		config.addDefault("lang.regionset", "Setting region enabled.");
		config.addDefault("lang.regionmodify", "Modifying region enabled.");
		config.addDefault("lang.noregionset", "You must setup a region first.");
		config.addDefault("lang.set2points", "You must set two points first.");
		config.addDefault("lang.regionsaved", "Region saved.");
		config.addDefault("lang.regionremoved", "Region removed.");
		config.addDefault("lang.regionnotremoved", "There is no region setup.");
		config.addDefault("lang.youwon", "You won %1%");
		config.addDefault("lang.awarded", "You have been awarded %1%");
		config.addDefault("lang.invfull",
				"Your inventory was full. You did not receive all rewards!");
		config.addDefault("lang.teamhaswon", "%1% are the Champions!");
		config.addDefault("lang.playerhaswon", "%1% is the Champion!");
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
		config.addDefault("lang.flaghome",
				"Player %1% brought home the flag of team %2%!");
		config.addDefault("lang.flagsave",
				"Player %1% dropped the flag of team %2%!");
		config.addDefault("lang.teamhaswon", "%1% are the Champions!");
		config.addDefault("lang.playerhaswon", "%1% is the Champion!");
		config.addDefault("lang.notready", "Not everyone has picked a class!");

		config.addDefault("lang.checkregionerror",
				"Waiting for a running arena to finish!");

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
		;

	}

	public String parse(String s) {
		return (String) lang.get(s); // hand over map value
	}

	public String parse(String s, String arg) {
		String var = (String) lang.get(s);
		return var.replace("%1%", arg); // hand over replaced map value
	}

	public String parse(String s, String arg1, String arg2) {
		String var = ((String) lang.get(s)).replace("%2%", arg2);
		return var.replace("%1%", arg1); // hand over replaced map value
	}

	public String parse(String s, String arg1, String arg2, String arg3) {
		String var = ((String) lang.get(s)).replace("%2%", arg2);
		var = var.replace("%3%", arg3);
		return var.replace("%1%", arg1); // hand over replaced map value
	}

	public void log_error(String s, String arg) {
		String var = (String) log.get(s);
		PVPArena.instance.log.severe("[PVP Arena] " + var.replace("%1%", arg));
		// log replaced map value
	}

	public void log_warning(String s, String arg) {
		String var = (String) log.get(s);
		PVPArena.instance.log.warning("[PVP Arena] " + var.replace("%1%", arg));
		// log replaced map value
	}

	public void log_info(String s, String arg) {
		String var = (String) log.get(s);
		PVPArena.instance.log.info("[PVP Arena] " + var.replace("%1%", arg));
		// log replaced map value
	}

	public void log_info(String s) {
		String var = (String) log.get(s);
		PVPArena.instance.log.info("[PVP Arena] " + var);
		// log map value
	}
}
