package net.slipcor.pvparena;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Tracker;
import net.slipcor.pvparena.core.Update;
import net.slipcor.pvparena.definitions.Announcement;
import net.slipcor.pvparena.definitions.Announcement.type;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.listeners.BlockListener;
import net.slipcor.pvparena.listeners.EntityListener;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.listeners.ServerListener;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.register.payment.Method;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * main class
 * 
 * -
 * 
 * contains central elements like plugin handlers and listeners
 * 
 * @author slipcor
 * 
 * @version v0.6.0
 * 
 */

public class PVPArena extends JavaPlugin {
	
	public static final Language lang = new Language();
	public static final EntityListener entityListener = new EntityListener();
	public static Method eco = null;
	
	private final BlockListener blockListener = new BlockListener();
	private final PlayerListener playerListener = new PlayerListener();
	private final ServerListener serverListener = new ServerListener();
	private final Debug debug = new Debug();
	
	/**
	 * plugin enabling method - register events and load the configs
	 */
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(blockListener, this);
		getServer().getPluginManager().registerEvents(entityListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(serverListener, this);

		List<String> whiteList = new ArrayList<String>();
		whiteList.add("ungod");

		getConfig().addDefault("debug", Boolean.valueOf(false));
		getConfig().addDefault("whitelist", whiteList);
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		File players = new File("plugins/pvparena/players.yml");
		if (!players.exists()) {
			try {
				players.createNewFile();
			} catch (IOException e) {
				Bukkit.getLogger().severe("Could not create players.yml! More errors will be happening!");
				e.printStackTrace();
			}
		}

		Debug.active = getConfig().getBoolean("debug");

		Arenas.load_arenas();

		Update.updateCheck(this);
		
		Tracker trackMe = new Tracker(this);
		trackMe.start();
		
		lang.log_info("enabled", getDescription().getFullName());
	}

	/**
	 * plugin disabling method - reset all arenas, cancel tasks
	 */
	@Override
	public void onDisable() {
		Arenas.reset(true);
		Tracker.stop();
		lang.log_info("disabled", getDescription().getFullName());
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

		if (args == null || args.length < 1) {
			return false;
		}

		if (args[0].equals("help")) {
			return Help.parseCommand(player, args);
		}

		if ((args.length > 1) && args[1].equals("create")) {
			// /pa [name] create {type} {...}
			if (!hasAdminPerms(player) && !(hasCreatePerms(player, null))) {
				Arenas.tellPlayer(player,
						lang.parse("nopermto", lang.parse("create")));
				return true;
			}
			Arena arena = Arenas.getArenaByName(args[0]);
			if (arena != null) {
				Arenas.tellPlayer(player, lang.parse("arenaexists"));
				return true;
			}
			Arena a = null;
			if (args.length > 2) {
				a = Arenas.loadArena(args[0], args[2]);
			} else {
				a = Arenas.loadArena(args[0], "teams");
			}
			a.setWorld(player.getWorld().getName());
			if (!hasAdminPerms(player)) {
				a.owner = player.getName();
			}
			a.cfg.set("general.owner", a.owner);
			a.cfg.save();
			Arenas.tellPlayer(player, lang.parse("created", args[0]));
			return true;
		} else if (args.length == 2 && args[1].equals("remove")) {
			// /pa [name] remove
			if (!hasAdminPerms(player)
					&& !(hasCreatePerms(player,
							Arenas.getArenaByName(args[0])))) {
				Arenas.tellPlayer(player,
						lang.parse("nopermto", lang.parse("remove")));
				return true;
			}
			Arena arena = Arenas.getArenaByName(args[0]);
			if (arena == null) {
				Arenas.tellPlayer(player,
						lang.parse("arenanotexists", args[0]));
				return true;
			}
			Arenas.unload(args[0]);
			Arenas.tellPlayer(player, lang.parse("removed", args[0]));
			return true;
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!hasAdminPerms(player)) {
				Arenas.tellPlayer(player,
						lang.parse("nopermto", lang.parse("reload")));
				return true;
			}
			Arenas.load_arenas();
			Arenas.tellPlayer(player, lang.parse("reloaded"));
			return true;
		} else if (args[0].equalsIgnoreCase("list")) {
			Arenas.tellPlayer(player,
					lang.parse("arenas", Arenas.getNames()));
			return true;
		} else if (args[0].equalsIgnoreCase("leave")) {
			Arena arena = Arenas.getArenaByPlayer(player);
			if (arena != null) {
				String sName = arena.pm.getTeam(player);
				
				Announcement.announce(arena, type.LOSER, lang.parse("playerleave",
						player.getName()));

				arena.pm.tellEveryoneExcept(
						player,
						lang.parse("playerleave",
								ChatColor.valueOf(arena.paTeams.get(sName))
										+ player.getName()
										+ ChatColor.YELLOW));
				Arenas.tellPlayer(player, lang.parse("youleave"));
				arena.removePlayer(player,
						arena.cfg.getString("tp.exit", "exit"));
				arena.checkEndAndCommit();
			} else {
				Arenas.tellPlayer(player, lang.parse("notinarena"));
			}
			return true;
		}

		String sName = args[0];

		Arena arena = Arenas.getArenaByName(sName);
		if (arena == null) {
			debug.i("arena not found, searching...");
			if (Arenas.count() == 1) {
				arena = Arenas.getFirst();
				debug.i("found 1 arena: " + arena.name);
			} else if (Arenas.getArenaByName("default") != null) {
				arena = Arenas.getArenaByName("default");
				debug.i("found default arena!");
			} else {
				Arenas.tellPlayer(player,
						lang.parse("arenanotexists", sName));
				return true;
			}
			return arena.parseCommand(player, args);
		}

		String[] newArgs = new String[args.length - 1];
		System.arraycopy(args, 1, newArgs, 0, args.length - 1);
		return arena.parseCommand(player, newArgs);
	}

	/**
	 * has player admin permissions?
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player has admin permissions, false otherwise
	 */
	public static boolean hasAdminPerms(Player player) {
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
	public static boolean hasCreatePerms(Player player, Arena arena) {
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
	public static boolean hasPerms(Player player) {
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
	public static boolean hasPerms(Player player, String perms) {
		return player.hasPermission(perms);
	}
}