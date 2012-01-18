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
import net.slipcor.pvparena.managers.UpdateManager;
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

/**
 * main class
 * 
 * -
 * 
 * contains central elements like plugin handlers, listeners and logging
 * 
 * @author slipcor
 * 
 * @version v0.5.8
 * 
 */

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

	/**
	 * plugin enabling method - register events and load the configs
	 */
	@Override
	public void onEnable() {
		PVPArena.instance = this;
		// com.arandomappdev.bukkitstats.CallHome.load(this);
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

		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS,
				this.playerListener, Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this.playerListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener,
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

		List<String> whiteList = new ArrayList<String>();
		whiteList.add("ungod");

		getConfig().addDefault("debug", Boolean.valueOf(false));
		getConfig().addDefault("whitelist", whiteList);
		getConfig().options().copyDefaults(true);
		saveConfig();

		DebugManager.active = getConfig().getBoolean("debug");

		load_config();

		UpdateManager.updateCheck();

		lang.log_info("enabled", pdfFile.getVersion());
	}

	/**
	 * plugin disabling method - reset all arenas, cancel tasks
	 */
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		ArenaManager.reset();
		lang.log_info("disabled", pdfFile.getVersion());
	}

	/**
	 * command handling
	 */
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
			if (!hasAdminPerms(player) && !(hasCreatePerms(player, null))) {
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
			Arena a = ArenaManager.getArenaByName(args[0]);
			a.setWorld(player.getWorld().getName());
			if (!hasAdminPerms(player)) {
				a.owner = player.getName();
			}
			a.cfg.set("general.owner", a.owner);
			a.cfg.save();
			ArenaManager.tellPlayer(player, lang.parse("created", args[0]));
			return true;
		} else if (args.length == 2 && args[1].equals("remove")) {
			// /pa [name] remove
			if (!hasAdminPerms(player)
					&& !(hasCreatePerms(player,
							ArenaManager.getArenaByName(args[0])))) {
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
							lang.parse("playerleave",
									ChatColor.valueOf(arena.paTeams.get(sName))
											+ player.getName()
											+ ChatColor.YELLOW));
				} else {
					arena.playerManager.tellEveryoneExcept(
							player,
							lang.parse("playerleave",
									ChatColor.WHITE + player.getName()
											+ ChatColor.YELLOW));
				}
				ArenaManager.tellPlayer(player, lang.parse("youleave"));
				arena.removePlayer(player,
						arena.cfg.getString("tp.exit", "exit"));
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
				db.i("found 1 arena: " + arena.name);
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

	/**
	 * config loading - load all arenas
	 */
	public void load_config() {
		if (Bukkit.getPluginManager().getPlugin("Spout") != null) {
			spoutHandler = SpoutManager.getInstance().toString();
		} else {
			lang.log_info("nospout");
		}

		ArenaManager.load_arenas();
	}

	/**
	 * setup permissions
	 */
	private void setupPermissions() {
		// TODO: remove legacy!
		Plugin test = getServer().getPluginManager().getPlugin("Permissions");
		if (permissionsHandler != null) {
			return;
		}
		if (test != null) {
			permissionsHandler = ((Permissions) test).getHandler();
		} else {
			lang.log_info("noperms");
		}
	}

	/**
	 * has player admin permissions?
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player has admin permissions, false otherwise
	 */
	public boolean hasAdminPerms(Player player) {
		return hasPerms(player, "pvparena.admin");
	}

	/**
	 * has player creating permissions?
	 * 
	 * @param player
	 *            the player to check
	 * @param arena
	 *            the arena to check
	 * @return true if the player has creating permissions, false otherwise
	 */
	public boolean hasCreatePerms(Player player, Arena arena) {
		return (hasPerms(player, "pvparena.create") && (arena == null || arena.owner
				.equals(player.getName())));
	}

	/**
	 * has player basic permissions?
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player has basic permissions, false otherwise
	 */
	public boolean hasPerms(Player player) {
		return hasPerms(player, "pvparena.user");
	}

	/**
	 * has player permission?
	 * 
	 * @param player
	 *            the player to check
	 * @param perms
	 *            a permission node to check
	 * @return true if the player has the permission, false otherwise
	 */
	public boolean hasPerms(Player player, String perms) {
		if (player.hasPermission(perms))
			return true;

		if (permissionsHandler != null)
			return permissionsHandler.has(player, perms);

		return false;
	}

	/**
	 * get the eConomy handler
	 * 
	 * @return the handler method
	 */
	public Method getMethod() {
		return economyHandler;
	}

	/**
	 * set the eConomy handler
	 * 
	 * @param method
	 *            the handler method
	 */
	public void setMethod(Method method) {
		economyHandler = method;
	}

	/**
	 * get the entitiy listener
	 * 
	 * @return the entity listener
	 */
	public PAEntityListener getEntityListener() {
		return entityListener;
	}

	/**
	 * get the spout handler
	 * 
	 * @return the spout handler string
	 */
	public String getSpoutHandler() {
		return spoutHandler;
	}
}