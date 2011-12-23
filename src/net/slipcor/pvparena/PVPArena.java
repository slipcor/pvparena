/*
 * main class
 * 
 * author: slipcor
 * 
 * version: v0.4.4 - Random spawns per team, not shared
 * 
 * history:
 * 
 *     v0.4.3 - max / min bet
 *     v0.4.2 - command blacklist
 *     v0.4.1 - command manager, arena information and arena config check
 *     v0.4.0 - mayor rewrite, improved help
 *     v0.3.14 - timed arena modes
 *     v0.3.11 - set regions for lounges, spectator, exit
 *     v0.3.10 - CraftBukkit #1337 config version, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.7 - Bugfixes, Cleanup
 *     v0.3.5 - Powerups!!
 *     v0.3.3 - Random spawns possible for every arena
 *     v0.3.1 - New Arena! FreeFight
 *     v0.3.0 - Multiple Arenas
 *     v0.2.1 - cleanup, comments, iConomy 6 support
 *     v0.2.0 - language support
 *     v0.1.13 - place bets on a match
 *     v0.1.12 - display stats with /pa users | /pa teams
 *     v0.1.11 - config: woolhead: put colored wool on heads!
 *     v0.1.10 - config: only start with even teams
 *     v0.1.9 - teleport location configuration
 *     v0.1.8 - lives!
 *     v0.1.7 - commands to show who is playing and on what team
 *     v0.1.6 - custom class: fight with own items
 *     v0.1.5 - class choosing not toggling
 *     v0.1.4 - arena disable via command * disable / * enable
 *     v0.1.3 - ingame config reload
 *     v0.1.2 - class permission requirement
 *     v0.1.0 - release version
 * 
 */

package net.slipcor.pvparena;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.listeners.PABlockListener;
import net.slipcor.pvparena.listeners.PAEntityListener;
import net.slipcor.pvparena.listeners.PAPlayerListener;
import net.slipcor.pvparena.listeners.PAServerListener;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.DebugManager;
import net.slipcor.pvparena.managers.HelpManager;
import net.slipcor.pvparena.managers.LanguageManager;
import net.slipcor.pvparena.register.payment.Method;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.SpoutManager;

public class PVPArena extends JavaPlugin {
	// global vars for global static access
	public static PVPArena instance;
	public static final LanguageManager lang = new LanguageManager();
	private static DebugManager db = new DebugManager();

	public final Logger log = Logger.getLogger("Minecraft");

	// private handlers
	private Method economyHandler = null;
	private PermissionHandler permissionsHandler;
	private String spoutHandler = null;
	// private listeners
	private final PABlockListener blockListener = new PABlockListener();
	private final PAEntityListener entityListener = new PAEntityListener();
	private final PAPlayerListener playerListener = new PAPlayerListener();
	private final PAServerListener serverListener = new PAServerListener();

	/*
	 * Plugin Methods
	 */

	@Override
	public void onEnable() {
		PVPArena.instance = this;
		com.arandomappdev.bukkitstats.CallHome.load(this);
		setupPermissions();
		PluginDescriptionFile pdfFile = getDescription();

		PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener,
				Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_BURN, this.blockListener,
				Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, this.blockListener,
				Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener,
				Event.Priority.High, this);
		

		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, this.entityListener,
				Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.ENTITY_REGAIN_HEALTH, this.entityListener,
				Event.Priority.Highest, this);
		

		pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.EXPLOSION_PRIME, this.entityListener,
				Event.Priority.Highest, this);

		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.playerListener,
				Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this.playerListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, this.playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this.playerListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, this.playerListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, this.playerListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_VELOCITY, this.playerListener,
				Event.Priority.Highest, this);

		pm.registerEvent(Event.Type.PLUGIN_DISABLE, this.serverListener,
				Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, this.serverListener,
				Event.Priority.Monitor, this);

		List<String> blackList = new ArrayList<String>();
		blackList.add("god");
		blackList.add("time");
		
		getConfig().addDefault("debug", Boolean.valueOf(false));
		getConfig().addDefault("blacklist", blackList);
		getConfig().options().copyDefaults(true);
		saveConfig();

		DebugManager.active = getConfig().getBoolean("debug");

		load_config();
		lang.log_info("enabled", pdfFile.getVersion());
	}

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		ArenaManager.reset();
		lang.log_info("disabled", pdfFile.getVersion());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			lang.parse("onlyplayers");
			return true;
		}

		Player player = (Player) sender;

		if (args == null || args.length < 1)
			return false;
		
		if (args[0].equals("help")) {
			return HelpManager.parseCommand(player, args);
		}

		if ((args.length == 3 || args.length == 2) && args[1].equals("create")) {
			// /pa [name] create [type]
			if (!hasAdminPerms(player)) {
				ArenaManager.tellPlayer(player,
						lang.parse("nopermto", lang.parse("create")));
				return true;
			}
			Arena arena = ArenaManager.getArenaByName(args[0]);
			if (arena != null) {
				ArenaManager.tellPlayer(player, lang.parse("arenaexists"));
				return true;
			}
			if (args.length == 3) {
				ArenaManager.loadArena(args[0], args[2]);
			} else {
				ArenaManager.loadArena(args[0], "teams");
			}
			ArenaManager.getArenaByName(args[0]).setWorld(player.getWorld().getName());
			ArenaManager.tellPlayer(player, lang.parse("created", args[0]));
			return true;
		} else if (args.length == 2 && args[1].equals("remove")) {
			// /pa [name] remove
			if (!hasAdminPerms(player)) {
				ArenaManager.tellPlayer(player,
						lang.parse("nopermto", lang.parse("remove")));
				return true;
			}
			Arena arena = ArenaManager.getArenaByName(args[0]);
			if (arena == null) {
				ArenaManager.tellPlayer(player,
						lang.parse("arenanotexists", args[0]));
				return true;
			}
			ArenaManager.unload(args[0]);
			ArenaManager.tellPlayer(player, lang.parse("removed", args[0]));
			return true;
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!hasAdminPerms(player)) {
				ArenaManager.tellPlayer(player,
						lang.parse("nopermto", lang.parse("reload")));
				return true;
			}
			load_config();
			ArenaManager.tellPlayer(player, lang.parse("reloaded"));
			return true;
		} else if (args[0].equalsIgnoreCase("list")) {
			ArenaManager.tellPlayer(player,
					lang.parse("arenas", ArenaManager.getNames()));
			return true;
		} else if (args[0].equalsIgnoreCase("leave")) {
			Arena arena = ArenaManager.getArenaByPlayer(player);
			if (arena != null) {
				String sName = arena.playerManager.getTeam(player);
				if (!sName.equals("free") && !sName.equals("")) {
					arena.playerManager.tellEveryoneExcept(
							player,
							lang.parse("playerleave", ChatColor.valueOf(arena.paTeams.get(sName))
									+ player.getName() + ChatColor.YELLOW));
				} else {
					arena.playerManager.tellEveryoneExcept(
							player,
							lang.parse("playerleave",
									ChatColor.WHITE + player.getName()
											+ ChatColor.YELLOW));
				}
				ArenaManager.tellPlayer(player, lang.parse("youleave"));
				arena.removePlayer(player, arena.cfg.getString("tp.exit", "exit"));
				arena.checkEndAndCommit();
			} else {
				ArenaManager.tellPlayer(player, lang.parse("notinarena"));
			}
			return true;
		}

		String sName = args[0];

		Arena arena = ArenaManager.getArenaByName(sName);
		if (arena == null) {
			db.i("arena not found, searching...");
			if (ArenaManager.count() == 1) {
				arena = ArenaManager.getFirst();
			db.i("found 1 arena: "+arena.name);
			} else if (ArenaManager.getArenaByName("default") != null) {
				arena = ArenaManager.getArenaByName("default");
			db.i("found default arena!");
			} else {
				ArenaManager.tellPlayer(player,
						lang.parse("arenanotexists", sName));
				return true;
			}
			return arena.parseCommand(player, args);
		}

		String[] newArgs = new String[args.length - 1];
		System.arraycopy(args, 1, newArgs, 0, args.length - 1);
		return arena.parseCommand(player, newArgs);
		
	}

	/*
	 * startup methods
	 */

	public void load_config() {
		if (Bukkit.getPluginManager().getPlugin("Spout") != null)
			spoutHandler = SpoutManager.getInstance().toString();
		else
			lang.log_info("nospout");

		ArenaManager.load_arenas();
	}

	private void setupPermissions() {
		// TODO: remove legacy!
		Plugin test = getServer().getPluginManager().getPlugin("Permissions");
		if (permissionsHandler != null)
			return;
		if (test != null)
			permissionsHandler = ((Permissions) test).getHandler();
		else
			lang.log_info("noperms");
	}

	/*
	 * permission check methods
	 */

	public boolean hasAdminPerms(Player player) {
		return hasPerms(player, "pvparena.admin") ? true : hasPerms(player,
				"fight.admin");
	}

	public boolean hasPerms(Player player) {
		return hasPerms(player, "pvparena.user") ? true : hasPerms(player,
				"fight.user");
	}

	public boolean hasPerms(Player player, String perms) {
		if (player.hasPermission(perms))
			return true;

		if (permissionsHandler != null)
			return permissionsHandler.has(player, perms);

		return false;
	}

	/*
	 * Getters and Setters
	 */

	public Method getMethod() {
		return economyHandler;
	}

	public void setMethod(Method method) {
		economyHandler = method;
	}

	public PAEntityListener getEntityListener() {
		return entityListener;
	}

	public String getSpoutHandler() {
		return spoutHandler;
	}
}