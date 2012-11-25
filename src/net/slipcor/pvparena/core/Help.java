package net.slipcor.pvparena.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * <pre>
 * Help class
 * </pre>
 * 
 * provides methods to display configurable help texts
 * 
 * @author slipcor
 * 
 * @version v0.9.9
 */

public class Help {

	private static String version = "v0.9.9.3";
	
	public static enum HELP {
		BLACKLIST("nulang.help.msg.blacklist", new String[]{
				"Manages block break/place blacklist entries",
				"-------------------------------------------",
				"shorthand command: !bl",
				"-------------------------------------------",
				"/pa [arenaname] blacklist clear",
				"/pa [arenaname] blacklist [type] clear",
				"    - clear the blacklist [of a special type]",
				"/pa [arenaname] blacklist [type] [sub] [blockID]",
				"-------------------------------------------",
				"Valid types: break | place | use",
				"Valid subs: add | remove | show"}),
		CHECK("nulang.help.msg.check", new String[]{
				"Validate an arena configuration",
				"-------------------------------------------",
				"shorthand command: !ch",
				"-------------------------------------------",
				"/pa [arenaname] check"}),
		CLASS("nulang.help.msg.class", new String[]{
				"Manage arena classes",
				"-------------------------------------------",
				"shorthand command: !cl",
				"-------------------------------------------",
				"/pa [arenaname] class load [classname]",
				"/pa [arenaname] class save [classname]",
				"/pa [arenaname] class remove [classname]"}),
		CREATE("nulang.help.msg.create", new String[]{
				"Create an arena",
				"-------------------------------------------",
				"shorthand command: !c",
				"-------------------------------------------",
				"/pa create [arenaname] {legacytype}",
				"-------------------------------------------",
				"Valid legacy types: teams, teamdm, dm, free, ctf, ctp, spleef"}),
		DEBUG("nulang.help.msg.debug", new String[]{
				"Debug PVP Arena",
				"-------------------------------------------",
				"shorthand command: !d",
				"-------------------------------------------",
				"/pa debug [values]",
				"-------------------------------------------",
				"Valid values are class numbers (separated by comma) and &enone&r"}),
		DISABLE("nulang.help.msg.disable", new String[]{
				"Disable an arena",
				"-------------------------------------------",
				"shorthand command: !dis",
				"-------------------------------------------",
				"/pa [arenaname] disable"}),
		EDIT("nulang.help.msg.edit", new String[]{
				"Toggle edit mode of an arena",
				"-------------------------------------------",
				"shorthand command: !e",
				"-------------------------------------------",
				"/pa [arenaname] edit"}),
		ENABLE("nulang.help.msg.enable", new String[]{
				"Enable an arena",
				"-------------------------------------------",
				"shorthand command: !en",
				"-------------------------------------------",
				"/pa [arenaname] enable"}),
		GAMEMODE("nulang.help.msg.gamemode", new String[]{
				"Set the general game mode of an arena. Teams or Free for all",
				"-------------------------------------------",
				"shorthand command: !gm",
				"-------------------------------------------",
				"/pa [arenaname] gamemode [free|teams]"}),
		GOAL("nulang.help.msg.goal", new String[]{
				"Manage arena goals",
				"-------------------------------------------",
				"shorthand command: !g",
				"-------------------------------------------",
				"/pa [arenaname] goal [goalname] {value}",
				"-------------------------------------------",
				"Entering an invalid goal will list available goals",
				"No value will toggle and display the result",
				"Valid values: yes, on, 1, true, no, off, 0, false"}),
		IMPORT("nulang.help.msg.import", new String[]{
				"Import v0.8 arenas",
				"-------------------------------------------",
				"shorthand command: !imp",
				"-------------------------------------------",
				"/pa import | import all arenas",
				"/pa import [arenaname] | import an arena"}),
		INSTALL("nulang.help.msg.install", new String[]{
				"Install modules",
				"-------------------------------------------",
				"shorthand command: !i",
				"-------------------------------------------",
				"/pa install (modules | goals)",
				"/pa install [modulename]",
				"-------------------------------------------",
				"modules / arenas will list available files"}),
		PROTECTION("nulang.help.msg.protection", new String[]{
				"Manage arena region protections",
				"-------------------------------------------",
				"shorthand command: !p",
				"-------------------------------------------",
				"/pa [arenaname] protection [regionname] [protection] {value}",
				"-------------------------------------------",
				"Entering an invalid protection will list available ones",
				"No value will toggle and display the result",
				"Valid values: yes, on, 1, true, no, off, 0, false"}),
		REGION("nulang.help.msg.region", new String[]{
				"Manage arena regions",
				"-------------------------------------------",
				"shorthand command: !rg",
				"-------------------------------------------",
				"/pa [arenaname] region",
				"    - start selecting a region",
				"/pa [arenaname] region [regionname] {regiontype}",
				"    - save region, default regiontype is a cuboid",
				"    - valid regions: &e/pa version regions",
				"/pa [arenaname] region [regionname] border",
				"    - display the region border for a short time",
				"/pa [arenaname] region remove [regionname]",
				"    - remove a region",
				"-------------------------------------------",
				"/pa [arenaname] region [regionname] [key] [value]",
				"    - Enhanced region editing, valid keys include:",
				"    - radius, height, position"}),
		REGIONFLAG("nulang.help.msg.regionflag", new String[]{
				"Manage arena region flags",
				"-------------------------------------------",
				"shorthand command: !rf",
				"-------------------------------------------",
				"/pa [arenaname] regionflag [regionname] [flagtype] {value}",
				"-------------------------------------------",
				"Entering an invalid flagtype will list available ones",
				"No value will toggle and display the result",
				"Valid values: yes, on, 1, true, no, off, 0, false"}),
		REGIONS("nulang.help.msg.regions", new String[]{
				"Debug arena regions",
				"-------------------------------------------",
				"shorthand command: !rs",
				"-------------------------------------------",
				"/pa [arenaname] regions {regionname}",
				"-------------------------------------------",
				"Show all defined regions / detailed information about a region"}),
		REGIONTYPE("nulang.help.msg.regiontype", new String[]{
				"Manage region types",
				"-------------------------------------------",
				"shorthand command: !rt",
				"-------------------------------------------",
				"/pa [arenaname] regiontype [regionname] [type]",
				"-------------------------------------------",
				"Valid region types "}),
		RELOAD("nulang.help.msg.reload", new String[]{
				"Reload an arena",
				"-------------------------------------------",
				"shorthand command: !rl",
				"-------------------------------------------",
				"/pa [arenaname] reload"}),
		REMOVE("nulang.help.msg.remove", new String[]{
				"Remove an arena",
				"-------------------------------------------",
				"shorthand command: !rem",
				"-------------------------------------------",
				"/pa [arenaname] remove"}),
		ROUND("nulang.help.msg.round", new String[]{
				"Manage arena rounds",
				"-------------------------------------------",
				"shorthand command: !rd",
				"-------------------------------------------",
				"/pa [arenaname] round",
				"    - list rounds",
				"/pa [arenaname] round [number]",
				"    - list round goals",
				"/pa [arenaname] round [number] [goal]",
				"    - toggle round goal"}),
		SET("nulang.help.msg.set", new String[]{
				"Set an arena config setting",
				"-------------------------------------------",
				"shorthand command: !s",
				"-------------------------------------------",
				"/pa [arenaname] set [node] [value]",
				"-------------------------------------------",
				"Values will be explained if necessary"}),
		SETOWNER("nulang.help.msg.setowner", new String[]{
				"Set an arena owner",
				"-------------------------------------------",
				"shorthand command: !so",
				"-------------------------------------------",
				"/pa [arenaname] setowner [playername]"}),
		SPAWN("nulang.help.msg.spawn", new String[]{
				"Manage arena spawns",
				"-------------------------------------------",
				"shorthand command: !sp",
				"-------------------------------------------",
				"/pa [arenaname] spawn [spawnname]",
				"/pa [arenaname] spawn [spawnname] remove",
				"-------------------------------------------",
				"Spawn names vary based on installed/active modules!"}),
		START("nulang.help.msg.start", new String[]{
				"Force start an arena",
				"-------------------------------------------",
				"shorthand command: !go",
				"-------------------------------------------",
				"/pa [arenaname] start"}),
		STOP("nulang.help.msg.stop", new String[]{
				"Force stop an arena",
				"-------------------------------------------",
				"shorthand command: !st",
				"-------------------------------------------",
				"/pa [arenaname] stop"}),
		TOGGLEMOD("nulang.help.msg.togglemod", new String[]{
				"Toggle an arena module",
				"-------------------------------------------",
				"shorthand command: !tm",
				"-------------------------------------------",
				"/pa [arenaname] togglemod [name]"}),
		TELEPORT("nulang.help.msg.teleport", new String[]{
				"Teleport to an arena spawn",
				"-------------------------------------------",
				"/pa [arenaname] teleport [spawnname]"}),
		UNINSTALL("nulang.help.msg.uninstall", new String[]{
				"Uninstall a PVP Arena module",
				"-------------------------------------------",
				"shorthand command: !t",
				"-------------------------------------------",
				"/pa uninstall [modulename]"}),
		UPDATE("nulang.help.msg.update", new String[]{
				"Update a PVP Arena module",
				"-------------------------------------------",
				"shorthand command: !u",
				"-------------------------------------------",
				"/pa update [modulename]"}),
		WHITELIST("nulang.help.msg.whitelist", new String[]{
				"Manages block break/place whitelist entries",
				"-------------------------------------------",
				"shorthand command: !wl",
				"-------------------------------------------",
				"/pa [arenaname] whitelist clear",
				"/pa [arenaname] whitelist [type] clear",
				"    - clear the whitelist [of a special type]",
				"/pa [arenaname] whitelist [type] [sub] [blockID]",
				"-------------------------------------------",
				"Valid types: break | place | use",
				"Valid subs: add | remove | show"}),
		
		CHAT("nulang.help.msg.chat", new String[]{
				"Set arena team chat mode",
				"-------------------------------------------",
				"shorthand command: -c",
				"-------------------------------------------",
				"/pa [arenaname] chat {value}",
				"-------------------------------------------",
				"Switch between team and global chat",
				"No value will toggle and display the result",
				"Valid values: yes, on, 1, true, no, off, 0, false"}),
		JOIN("nulang.help.msg.join", new String[]{
				"Join an arena",
				"-------------------------------------------",
				"shorthand command: -j",
				"-------------------------------------------",
				"/pa [arenaname] join {teamname}"}),
		LEAVE("nulang.help.msg.leave", new String[]{
				"Leave the arena",
				"-------------------------------------------",
				"shorthand command: -l",
				"-------------------------------------------",
				"/pa leave"}),
		SPECTATE("nulang.help.msg.spectate", new String[]{
				"Spectate an arena",
				"-------------------------------------------",
				"shorthand command: -s",
				"-------------------------------------------",
				"/pa [arenaname] spectate"}),
		
		ARENALIST("nulang.help.msg.arenalist", new String[]{
				"List available arenas",
				"-------------------------------------------",
				"shorthand command: -ls",
				"-------------------------------------------",
				"/pa list"}),
		HELP("nulang.help.msg.help", new String[]{
				"Receive help",
				"-------------------------------------------",
				"shorthand command: -h",
				"-------------------------------------------",
				"/pa help [topic/command]"}),
		INFO("nulang.help.msg.info", new String[]{
				"Detailed information about an arena",
				"-------------------------------------------",
				"shorthand command: -i",
				"-------------------------------------------",
				"/pa [arenaname] info"}),
		LIST("nulang.help.msg.list", new String[]{
				"List arena players",
				"-------------------------------------------",
				"shorthand command: -ls",
				"-------------------------------------------",
				"/pa {arenaname} list"}),
		
		READY("nulang.help.msg.ready", new String[]{
				"Ready up / show ready players",
				"-------------------------------------------",
				"shorthand command: -r",
				"-------------------------------------------",
				"/pa {arenaname} ready {show}"}),
		STATS("nulang.help.msg.stats", new String[]{
				"Show arena/player statistics",
				"-------------------------------------------",
				"shorthand command: -s",
				"-------------------------------------------",
				"/pa {arenaname} stats [stattype] {amount}",
				"-------------------------------------------",
				"An invalid stattype will list all available ones"}),
		VERSION("nulang.help.msg.version", new String[]{
				"Show detailed version information",
				"-------------------------------------------",
				"shorthand command: -v",
				"-------------------------------------------",
				"/pa version"});
		

		private String node;
		private List<String> value;

		private HELP(String node, List<String> value) {
			this.node = node;
			this.value = value;
		}
		
		private HELP(String node, String[] ss) {
			this.node = node;
			List<String> list = new ArrayList<String>();
			for (String s : ss) {
				list.add(s);
			}
			this.value = list;
		}

		public String getNode() {
			return node;
		}

		public void setValue(List<String> s) {
			value = s;
		}

		public List<String> get() {
			return value;
		}
	}

	/**
	 * create a language manager instance
	 */
	public static void init(String s) {
		PVPArena.instance.getDataFolder().mkdir();
		File configFile = new File(PVPArena.instance.getDataFolder().getPath()
				+ "/help_" + s + ".yml");
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				Bukkit.getLogger().severe(
						"[PVP Arena] Error when creating help language file.");
			}
		
		boolean override = false;

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
			String ver = config.getString("version", "0");
			
			if (!ver.equals(version)) {
				override = true;
				File f = new File(configFile.getParent(), "/help_"+s+"_backup.yml");
				if (!f.exists()) {
					f.createNewFile();
				}
				config.save(f);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (HELP m : HELP.values()) {
			if (!override) {
				config.addDefault(m.getNode(), m.get());
			} else {
				config.set(m.getNode(), m.get());
			}
		}
		
		config.set("version", version);

		config.options().copyDefaults(true);
		try {
			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * read a node from the config and return its value
	 * 
	 * @param s
	 *            the node name
	 * @return the node string
	 */
	public static String[] parse(HELP m) {
		return StringParser.colorize(m.get());
	}
}
