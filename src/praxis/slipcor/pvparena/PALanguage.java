package praxis.slipcor.pvparena;

import java.io.File;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.util.config.Configuration;

/*
 * Language class
 * 
 * author: slipcor
 * 
 * version: v0.2.1 - cleanup, comments
 * 
 * history:
 * 		v0.2.0 - language support
 */

public class PALanguage {
	Map<String, String> lang = null; // game language map
	Map<String, String> log = null; // log language map
	@SuppressWarnings("unchecked")
	public PALanguage() {
		boolean bNew = false;
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/lang.yml");
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
				bNew = true;
			} catch (Exception e) {
				PVPArena.log.severe("[PVP Arena] Error when creating language file.");
			}

		Configuration config = new Configuration(configFile);
		config.load();

		if (bNew) { // if running for the first time => add all nodes
			config.setProperty("log.filecreateerror","Error creating %1% file.");
			config.setProperty("log.teamnotfound","Unrecognized team: %1%");
			config.setProperty("log.matnotfound","Unrecognized material: %1%");
			config.setProperty("log.iconomyon","<3 iConomy");
			config.setProperty("log.iconomyoff","</3 iConomy");
			config.setProperty("log.nospout","Spout not found, you are missing some features ;)");
			config.setProperty("log.enabled","enabled (version %1%)");
			config.setProperty("log.disabled","disabled (version %1%)");
			config.setProperty("log.noperms","Permissions plugin not found, defaulting to OP.");
			config.setProperty("lang.playerleave","%1% has left the fight!");
			config.setProperty("lang.youleave","You have left the fight!");
			config.setProperty("lang.msgprefix",ChatColor.YELLOW + "[PVP Arena] ");
			config.setProperty("lang.dropitem","Not so fast! No Cheating!");
			config.setProperty("lang.usepatoexit","Please use '/pa leave' to exit the fight!");
			config.setProperty("lang.pos1","First position set.");
			config.setProperty("lang.pos2","Second position set.");
			config.setProperty("lang.classperms","You don't have permission for that class.");
			config.setProperty("lang.toomanyplayers","There are too many of this class, pick another class.");
			config.setProperty("lang.notready","Not all of your team has picked a class!");
			config.setProperty("lang.waitequal","Waiting for the teams to have equal player number!");
			config.setProperty("lang.ready","%1% team is ready!");
			config.setProperty("lang.begin","Let the fight begin!");
			config.setProperty("lang.killed","%1% has been killed!");
			config.setProperty("lang.lostlife","%1% has lost a life! %2% remaining.");
			config.setProperty("lang.onlyplayers","Only players may access this command!");
			config.setProperty("lang.arenadisabled","Arena disabled, please try again later!");
			config.setProperty("lang.arenanotsetup","All waypoints must be set up first.");
			config.setProperty("lang.permjoin","You don't have permission to join the arena!");
			config.setProperty("lang.selectteam","You must select a team to join! /pa red | /pa blue");
			config.setProperty("lang.notselectteam","You cannot select a team to join! /pa");
			config.setProperty("lang.alreadyjoined","You already joined!");
			config.setProperty("lang.fightinprogress","A fight is already in progress!");
			config.setProperty("lang.notenough","You don't have %1%.");
			config.setProperty("lang.youjoined","Welcome! You are on team %1%");
			config.setProperty("lang.playerjoined","%1% has joined team %2%");
			config.setProperty("lang.nopermto","You don't have permission to %1%");
			config.setProperty("lang.enable","enable");
			config.setProperty("lang.disable","disable");
			config.setProperty("lang.reload","reload");
			config.setProperty("lang.enabled","Enabled!");
			config.setProperty("lang.disabled","Disabled!");
			config.setProperty("lang.reloaded","Config reloaded!");
			config.setProperty("lang.noplayer","No player in the PVP arena.");
			config.setProperty("lang.players","Players");
			config.setProperty("lang.specwelcome","Welcome to the spectator's area! /pa bet [name] [amount] to bet on team or player");
			config.setProperty("lang.teamstat","%1% %2% wins, %3% losses");
			config.setProperty("lang.blue","Blue");
			config.setProperty("lang.red","Red");
			config.setProperty("lang.top5win","Top 5 winners");
			config.setProperty("lang.top5lose","Top 5 losers");
			config.setProperty("lang.wins","wins");
			config.setProperty("lang.losses","losses");
			config.setProperty("lang.setredlounge","Red lounge set.");
			config.setProperty("lang.setbluelounge","Blue lounge set.");
			config.setProperty("lang.setredspawn","Red spawn set.");
			config.setProperty("lang.setredblue","Blue spawn set.");
			config.setProperty("lang.setspectator","Spectator spawn set.");
			config.setProperty("lang.setexit","Exit spawn set.");
			config.setProperty("lang.forcestop","You have forced the fight to stop.");
			config.setProperty("lang.nofight","There is no fight in progress.");
			config.setProperty("lang.invalidcmd","Invalid command (%1%)");
			config.setProperty("lang.betnotyours","Cannot place bets on your own match!");
			config.setProperty("lang.betoptions","You can only bet on 'blue', 'red' or arena player!");
			config.setProperty("lang.invalidamount","Invalid amount: %1%");
			config.setProperty("lang.beteplaced","Your bet on %1% has been placed.");
			config.setProperty("lang.regionset","Setting region enabled.");
			config.setProperty("lang.regionalreadyset","A region has already been created.");
			config.setProperty("lang.regionmodify","Modifying region enabled.");
			config.setProperty("lang.noregionset","You must setup a region first.");
			config.setProperty("lang.set2points","You must set two points first.");
			config.setProperty("lang.regionsaved","Region saved.");
			config.setProperty("lang.regionremoved","Region removed.");
			config.setProperty("lang.regionnotremoved","There is no region setup.");
			config.setProperty("lang.youwon","You won %1%");
			config.setProperty("lang.awarded","You have been awarded %1%");
			config.setProperty("lang.invfull","Your inventory was full. You did not receive all rewards!");
			config.setProperty("lang.haswon","%1% are the Champions");

			config.save();
		}
		// write contents to maps
		lang = (Map<String, String>) config.getProperty("lang");
		log = (Map<String, String>) config.getProperty("log");
		
	}
	
	public String parse(String s) {
		return lang.get(s); // hand over map value
	}

	public String parse(String s, String arg) {
		String var = lang.get(s);
		return var.replace("%1%", arg); // hand over replaced map value
	}
	
	public String parse(String s, String arg1, String arg2) {
		String var = lang.get(s).replace("%2%", arg2);
		return var.replace("%1%", arg1); // hand over replaced map value
	}
	
	public String parse(String s, String arg1, String arg2, String arg3) {
		String var = lang.get(s).replace("%2%", arg2);
		var = var.replace("%3%", arg3);
		return var.replace("%1%", arg1); // hand over replaced map value
	}
	
	public void log_error(String s, String arg) {
		String var = log.get(s);
		PVPArena.log.severe("[PVP Arena] " + var.replace("%1%", arg));
		// log replaced map value
	}
	
	public void log_warning(String s, String arg) {
		String var = log.get(s);
		PVPArena.log.warning("[PVP Arena] " + var.replace("%1%", arg));
		// log replaced map value
	}
	
	public void log_info(String s, String arg) {
		String var = log.get(s);
		PVPArena.log.info("[PVP Arena] " + var.replace("%1%", arg));
		// log replaced map value
	}
	
	public void log_info(String s) {
		String var = log.get(s);
		PVPArena.log.info("[PVP Arena] " + var);
		// log map value
	}
}
