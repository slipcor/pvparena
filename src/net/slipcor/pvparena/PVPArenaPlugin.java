package net.slipcor.pvparena;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.logging.Logger;

import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.listeners.PABlockListener;
import net.slipcor.pvparena.listeners.PAEntityListener;
import net.slipcor.pvparena.listeners.PAPlayerListener;
import net.slipcor.pvparena.listeners.PAServerListener;
import net.slipcor.pvparena.managers.ArenaManager;
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

/*
 * main class
 * 
 * author: slipcor
 * 
 * version: v0.3.1 - New Arena! FreeFight
 * 
 * history:
 *
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
 * todo:
 *      lives !!!
 * 		CTF
 * 
 */

public class PVPArenaPlugin extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler permissionHandler;
	public static String spoutHandler = null;
	public static Method method = null; // eConomy access
	public static PermissionHandler Permissions;
	private final PAServerListener serverListener = new PAServerListener();
	private final PAEntityListener entityListener = new PAEntityListener();
	private final PAPlayerListener playerListener = new PAPlayerListener();
	private final PABlockListener blockListener = new PABlockListener();
	public static final LanguageManager lang = new LanguageManager();


	public void onEnable() {
		setupPermissions();
		PluginDescriptionFile pdfFile = getDescription();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, this.serverListener,	Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, this.serverListener,Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, this.entityListener,Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener,Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener,Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener,Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, this.playerListener,Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this.playerListener,Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener,Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, this.playerListener,Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener,Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, this.blockListener,Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener,Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_BURN, this.blockListener,Event.Priority.High, this);

		lang.log_info("enabled", pdfFile.getVersion());

		load_config();
	}
	
	public static void load_config() {
		if (Bukkit.getPluginManager().getPlugin("Spout") != null)
			spoutHandler = org.getspout.spout.Spout.getInstance().toString();
		else
			lang.log_info("nospout");
			
		ArenaManager.load_arenas();
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		lang.log_info("disabled", pdfFile.getVersion());
		ArenaManager.reset();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if ((!commandLabel.equalsIgnoreCase("pvparena")) && (!commandLabel.equalsIgnoreCase("pa"))) {
			return true; // none of our business
		}
		
		Player player = null;
		try {
			player = (Player) sender;
		} catch (Exception e) {
			lang.parse("onlyplayers");
			return true;
		}
		
		if (args == null || args.length < 1)
			return false;
		
		if (args.length == 3 && args[1].equals("create")) {
			// /pa [name] create [type]
			if (!hasAdminPerms(player)) {
				Arena.tellPlayer(player, lang.parse("nopermto", lang.parse("create")));
				return true;
			}
			Arena arena = ArenaManager.getArenaByName(args[0]);			
			if (arena != null) {
				Arena.tellPlayer(player, lang.parse("arenaexists"));
				return true;
			}			
			ArenaManager.loadArena(args[0], args[2]);
			Arena.tellPlayer(player, lang.parse("created",args[0]));
			return true;
		} else if (args.length == 2 && args[1].equals("remove")) {
			// /pa [name] remove			
			if (!hasAdminPerms(player)) {
				Arena.tellPlayer(player, lang.parse("nopermto", lang.parse("remove")));
				return true;
			}
			Arena arena = ArenaManager.getArenaByName(args[0]);			
			if (arena == null) {
				Arena.tellPlayer(player, lang.parse("arenanotexists", args[0]));
				return true;
			}			
			ArenaManager.unload(args[0]);
			Arena.tellPlayer(player, lang.parse("removed",args[0]));
			return true;
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!hasAdminPerms(player)) {
				Arena.tellPlayer(player, lang.parse("nopermto", lang.parse("reload")));
				return true;
			}
			load_config();
			Arena.tellPlayer(player, lang.parse("reloaded"));
			return true;
		} else if (args[0].equalsIgnoreCase("list")) {
			Arena.tellPlayer(player, lang.parse("arenas", ArenaManager.getNames()));
			return true;
		} else if (args[0].equalsIgnoreCase("leave")) {
			Arena arena = ArenaManager.getArenaByPlayer(player);
			if (arena != null) {
				if (arena.fightUsersTeam.get(player.getName()) == "red") {
					arena.redTeam -= 1;
					arena.tellEveryoneExcept(player, lang.parse("playerleave", ChatColor.RED + player.getName() + ChatColor.YELLOW));
				} else if (arena.fightUsersTeam.get(player.getName()) == "blue") {
					arena.blueTeam -= 1;
					arena.tellEveryoneExcept(player, lang.parse("playerleave", ChatColor.BLUE + player.getName() + ChatColor.YELLOW));
				} else {
					arena.tellEveryoneExcept(player, lang.parse("playerleave", ChatColor.WHITE + player.getName() + ChatColor.YELLOW));
				}
				Arena.tellPlayer(player, lang.parse("youleave"));					
				arena.removePlayer(player, arena.sTPexit);
				arena.checkEnd();
			} else {
				Arena.tellPlayer(player, lang.parse("notinarena"));
			}
			return true;
		} else if (args[0].equalsIgnoreCase("setup")) {
			if (!hasAdminPerms(player)) {
				Arena.tellPlayer(player, lang.parse("nopermto", lang.parse("setup")));
				return true;
			}
			load_config();
			Arena.tellPlayer(player, "/pvparena [arenaname] redlounge/bluelounge | Set the lounge spawn of an arena team");
			Arena.tellPlayer(player, "/pvparena [arenaname] redspawn/bluespawn | Set the fight spawn of an arena team");
			Arena.tellPlayer(player, "/pvparena [arenaname] spectator/exit | Set the spectator/exit spawn if an arena");
			Arena.tellPlayer(player, "/pvparena [arenaname] region set | Start setting a region");
			Arena.tellPlayer(player, "/pvparena [arenaname] region modify/edit | Modify a region");
			Arena.tellPlayer(player, "/pvparena [arenaname] region save | End setting a region, save");
			Arena.tellPlayer(player, "/pvparena [arenaname] region remove | Remove a set region");
			return true;
		}

		String sName = args[0];
		String[] newArgs = new String[args.length-1];
		System.arraycopy(args, 1, newArgs, 0, args.length-1);
		
		Arena arena = ArenaManager.getArenaByName(sName);
		if (arena == null) {
			Arena.tellPlayer(player, lang.parse("arenanotexists", sName));
			return true;
		}
		return arena.parseCommand(player, cmd, newArgs, sName);
	}
	
	public static boolean hasAdminPerms(Player player) {
		if (Permissions == null)
			return player.isOp();
		return Permissions.has(player, "fight.admin");
	}

	public static boolean hasPerms(Player player) {
		if (Permissions == null)
			return true;
		return Permissions.has(player, "fight.user")?true:player.hasPermission("fight.user");
	}

	public static boolean hasPerms(Player player, String perms) {
		if (Permissions == null)
			return true;
		return Permissions.has(player, perms)?true:player.hasPermission(perms);
	}
	
	private void setupPermissions() {
		Plugin test = getServer().getPluginManager().getPlugin("Permissions");
		if (Permissions != null)
			return;
		if (test != null)
			Permissions = ((Permissions) test).getHandler();
		else
			lang.log_info("noperms");
	}
}