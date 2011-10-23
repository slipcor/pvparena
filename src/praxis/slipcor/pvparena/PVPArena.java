package praxis.slipcor.pvparena;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.getspout.spoutapi.SpoutManager;

import praxis.pvparena.register.payment.Method;
import praxis.pvparena.register.payment.Method.MethodAccount;
import praxis.slipcor.pvparena.listeners.PABlockListener;
import praxis.slipcor.pvparena.listeners.PAEntityListener;
import praxis.slipcor.pvparena.listeners.PAPlayerListener;
import praxis.slipcor.pvparena.listeners.PAServerListener;
import praxis.slipcor.pvparena.managers.ArenaManager;
import praxis.slipcor.pvparena.managers.LanguageManager;
import praxis.slipcor.pvparena.managers.StatsManager;

/*
 * main class
 * 
 * author: slipcor
 * 
 * version: v0.3.0 - Multiple Arenas
 * 
 * history:
 *
 *    v0.2.1 - cleanup, comments, iConomy 6 support
 *    v0.2.0 - language support
 *    v0.1.13 - place bets on a match
 *    v0.1.12 - display stats with /pa users | /pa teams
 *    v0.1.11 - config: woolhead: put colored wool on heads!
 *    v0.1.10 - config: only start with even teams
 *    v0.1.9 - teleport location configuration
 *    v0.1.8 - lives!
 *    v0.1.7 - commands to show who is playing and on what team
 *    v0.1.6 - custom class: fight with own items
 *    v0.1.5 - class choosing not toggling
 *    v0.1.4 - arena disable via command * disable / * enable
 *    v0.1.3 - ingame config reload
 *    v0.1.2 - class permission requirement
 *    v0.1.0 - release version
 * 
 * todo:
 *      lives !!!
 * 		CTF
 * 
 */

public class PVPArena extends JavaPlugin {
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
	
	private void load_config() {
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
		
		if (args.length == 2 && args[1].equals("create")) {
			// /pa [name] create			
			PAArena arena = ArenaManager.getArenaByName(args[0]);			
			if (arena != null) {
				tellPlayer(player, lang.parse("arenaexists"));
				return true;
			}			
			ArenaManager.loadArena(args[1]);
			return true;
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!hasAdminPerms(player)) {
				tellPlayer(player, lang.parse("nopermto", lang.parse("reload")));
				return true;
			}
			load_config();
			tellPlayer(player, lang.parse("reloaded"));
			return true;
		} else if (args[0].equalsIgnoreCase("list")) {
			tellPlayer(player, lang.parse("arenas", ArenaManager.getNames()));
			return true;
		} else if (args[0].equalsIgnoreCase("leave")) {
			PAArena arena = ArenaManager.getArenaByPlayer(player);
			if (arena != null) {
				if (arena.fightUsersTeam.get(player.getName()) == "red") {
					arena.redTeam -= 1;
					arena.tellEveryoneExcept(player, lang.parse("playerleave", ChatColor.RED + player.getName() + ChatColor.WHITE));
				}
				if (arena.fightUsersTeam.get(player.getName()) == "blue") {
					arena.blueTeam -= 1;
					arena.tellEveryoneExcept(player, lang.parse("playerleave", ChatColor.BLUE + player.getName() + ChatColor.WHITE));
				}
				tellPlayer(player, lang.parse("youleave"));					
				
				if (arena.fightInProgress && arena.checkEnd())
					return true;
				arena.removePlayer(player, arena.sTPexit);
			} else {
				tellPlayer(player, lang.parse("notinarena"));
			}
			return true;
		} else if (args[0].equalsIgnoreCase("setup")) {
			if (!hasAdminPerms(player)) {
				tellPlayer(player, lang.parse("nopermto", lang.parse("setup")));
				return true;
			}
			load_config();
			tellPlayer(player, "/pvparena [arenaname] redlounge/bluelounge | Set the lounge spawn of an arena team");
			tellPlayer(player, "/pvparena [arenaname] redspawn/bluespawn | Set the fight spawn of an arena team");
			tellPlayer(player, "/pvparena [arenaname] spectator/exit | Set the spectator/exit spawn if an arena");
			tellPlayer(player, "/pvparena [arenaname] region set | Start setting a region");
			tellPlayer(player, "/pvparena [arenaname] region modify/edit | Modify a region");
			tellPlayer(player, "/pvparena [arenaname] region save | End setting a region, save");
			tellPlayer(player, "/pvparena [arenaname] region remove | Remove a set region");
			return true;
		}

		String sName = args[0];
		String[] newArgs = new String[args.length-1];
		System.arraycopy(args, 1, newArgs, 0, args.length-1);
		
		return parseCommand(player, cmd, newArgs, sName);
	}

	private boolean parseCommand(Player player, Command cmd, String[] args, String sName) {
		PAArena arena = ArenaManager.getArenaByName(sName);
		
		if (arena == null) {
			tellPlayer(player, lang.parse("arenanotexists", sName));
			return true;
		}
		
		if (!arena.enabled && !hasAdminPerms(player)) {
			lang.parse("arenadisabled");
			return true;
		}
		
		if (args.length < 1) {
			// just /pa or /pvparena
			if (!(arena.isSetup().booleanValue())) {
				tellPlayer(player, lang.parse("arenanotsetup"));
				return true;
			}
			if (!hasPerms(player)) {
				tellPlayer(player, lang.parse("permjoin"));
				return true;
			}
			if (!(arena.randomlyselectteams)) {
				tellPlayer(player, lang.parse("selectteam"));
				return true;
			}
			if (arena.savedmisc.containsKey(player)) {
				tellPlayer(player, lang.parse("alreadyjoined"));
				return true;
			}
			if (arena.fightInProgress) {
				tellPlayer(player, lang.parse("fightinprogress"));
				return true;
			}

			if (method != null) {
				MethodAccount ma = method.getAccount(player.getName());
				if (ma == null) {
					log.severe("Account not found: "+player.getName());
					return true;
				}
				if(!ma.hasEnough(arena.entryFee)){
					// no money, no entry!
					tellPlayer(player, lang.parse("notenough", method.format(arena.entryFee)));
					return true;
	            }
			}
			
			arena.prepare(player);
			arena.fightUsersLives.put(player.getName(), (byte) arena.maxlives);
			
			
			if (emptyInventory(player)) {
				if ((method != null) && (arena.entryFee > 0)) {
					MethodAccount ma = method.getAccount(player.getName());
					ma.subtract(arena.entryFee);
				}

				if (!(arena.fightUsersTeam.containsKey(player.getName()))) {
					if (arena.blueTeam > arena.redTeam) {
						arena.goToWaypoint(player, "redlounge");
						arena.fightUsersTeam.put(player.getName(), "red");
						tellPlayer(player, lang.parse("youjoined", ChatColor.RED + "<Red>"));
						arena.tellEveryoneExcept(player, lang.parse("playerjoined", player.getName(), ChatColor.RED + "<Red>"));
						arena.redTeam += 1;
					} else {
						arena.goToWaypoint(player, "bluelounge");
						arena.fightUsersTeam.put(player.getName(), "blue");
						tellPlayer(player, lang.parse("youjoined", ChatColor.BLUE + "<Blue>"));
						arena.tellEveryoneExcept(player, lang.parse("playerjoined", player.getName(), ChatColor.BLUE + "<Blue>"));
						arena.blueTeam += 1;
					}

				} else {
					tellPlayer(player, lang.parse("alreadyjoined"));
				}

			} else {
				tellPlayer(player, lang.parse("alreadyjoined"));
			}
			return true;
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("enable")) {
				if (!hasAdminPerms(player)) {
					tellPlayer(player, lang.parse("nopermto", lang.parse("enable")));
					return true;
				}
				arena.enabled = true;
				tellPlayer(player, lang.parse("enabled"));
				return true;
			} else if (args[0].equalsIgnoreCase("disable")) {
				if (!hasAdminPerms(player)) {
					tellPlayer(player, lang.parse("nopermto", lang.parse("disable")));
					return true;
				}
				arena.enabled = false;
				tellPlayer(player, lang.parse("disabled"));
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!hasAdminPerms(player)) {
					tellPlayer(player, lang.parse("nopermto", lang.parse("reload")));
					return true;
				}
				load_config();
				tellPlayer(player, lang.parse("reloaded"));
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				if ((arena.fightUsersTeam == null) || (arena.fightUsersTeam.size() < 1)) {
					tellPlayer(player, lang.parse("noplayer"));
					return true;
				}
				String plrs = "";
				for (String sPlayer : arena.fightUsersTeam.keySet()) {
					if (!plrs.equals(""))
						plrs +=", ";
					plrs += (arena.fightUsersTeam.get(sPlayer).equals("red")?ChatColor.RED:ChatColor.BLUE) + sPlayer + ChatColor.WHITE;
				}
				tellPlayer(player, lang.parse("players") + ": " + plrs);
				return true;
			} else if (args[0].equalsIgnoreCase("red")) {
				

				// /pa red or /pvparena red
				if (!(arena.isSetup().booleanValue())) {
					tellPlayer(player, lang.parse("arenanotsetup"));
					return true;
				}
				if (!hasPerms(player)) {
					tellPlayer(player, lang.parse("permjoin"));
					return true;
				}
				if (!(arena.manuallyselectteams)) {
					tellPlayer(player, lang.parse("notselectteam"));
					return true;
				}
				if (arena.savedmisc.containsKey(player)) {
					tellPlayer(player, lang.parse("alreadyjoined"));
					return true;
				}
				if (arena.fightInProgress) {
					tellPlayer(player, lang.parse("fightinprogress"));
					return true;
				}
				
				if (method != null) {
					MethodAccount ma = method.getAccount(player.getName());
					if (ma == null) {
						log.severe("Account not found: "+player.getName());
						return true;
					}
					if(!ma.hasEnough(arena.entryFee)){
						// no money, no entry!
						tellPlayer(player, lang.parse("notenough", method.format(arena.entryFee)));
						return true;
		            }
				}


				arena.prepare(player);
				arena.fightUsersLives.put(player.getName(), (byte) arena.maxlives);
				
				if (emptyInventory(player)) {
					if ((method != null) && (arena.entryFee > 0)) {
						MethodAccount ma = method.getAccount(player.getName());
						ma.subtract(arena.entryFee);
					}

					arena.goToWaypoint(player, "redlounge");
					tellPlayer(player, lang.parse("youjoined", ChatColor.RED + "<Red>"));
					arena.tellEveryoneExcept(player, lang.parse("playerjoined", player.getName(), ChatColor.RED + "<Red>"));
					arena.redTeam += 1;

				}

			} else if (args[0].equalsIgnoreCase("blue")) {
				
				// /pa blue or /pvparena blue

				if (!(arena.isSetup().booleanValue())) {
					tellPlayer(player, lang.parse("arenanotsetup"));
					return true;
				}
				if (!hasPerms(player)) {
					tellPlayer(player, lang.parse("permjoin"));
					return true;
				}
				if (!(arena.manuallyselectteams)) {
					tellPlayer(player, lang.parse("notselectteam"));
					return true;
				}
				if (arena.savedmisc.containsKey(player)) {
					tellPlayer(player, lang.parse("alreadyjoined"));
					return true;
				}
				if (arena.fightInProgress) {
					tellPlayer(player, lang.parse("fightinprogress"));
					return true;
				}
				
				if (method != null) {
					MethodAccount ma = method.getAccount(player.getName());
					if (ma == null) {
						log.severe("Account not found: "+player.getName());
						return true;
					}
					if(!ma.hasEnough(arena.entryFee)){
						// no money, no entry!
						tellPlayer(player, lang.parse("notenough", method.format(arena.entryFee)));
						return true;
		            }
				}


				arena.prepare(player);
				arena.fightUsersLives.put(player.getName(), (byte) arena.maxlives);

				if (emptyInventory(player)) {
					if ((method != null) && (arena.entryFee > 0)) {
						MethodAccount ma = method.getAccount(player.getName());
						ma.subtract(arena.entryFee);
					}


					arena.goToWaypoint(player, "bluelounge");
					arena.fightUsersTeam.put(player.getName(), "blue");
					arena.blueTeam += 1;
					tellPlayer(player, lang.parse("youjoined", ChatColor.BLUE + "<Blue>"));
					arena.tellEveryoneExcept(player, lang.parse("playerjoined", player.getName(), ChatColor.BLUE + "<Blue>"));
					

				}
				return true;
			
			} else if (args[0].equalsIgnoreCase("watch")) {

				if (!(arena.isSetup().booleanValue())) {
					tellPlayer(player, lang.parse("arenanotsetup"));
					return true;
				}
				if (arena.fightUsersTeam.containsKey(player.getName())) {
					tellPlayer(player, lang.parse("alreadyjoined"));
					return true;
				}
				arena.goToWaypoint(player, "spectator");
				tellPlayer(player, lang.parse("specwelcome"));
				return true;
			} else if (hasAdminPerms(player)) {
				if (args[0].equalsIgnoreCase("redlounge")) {
					arena.setCoords(player, "redlounge");
					tellPlayer(player, lang.parse("setredlounge"));
					
				} else if (args[0].equalsIgnoreCase("redspawn")) {
					arena.setCoords(player, "redspawn");
					tellPlayer(player, lang.parse("setredspawn"));
				} else if (args[0].equalsIgnoreCase("bluelounge")) {
					arena.setCoords(player, "bluelounge");
					tellPlayer(player, lang.parse("setbluelounge"));
				} else if (args[0].equalsIgnoreCase("bluespawn")) {
					arena.setCoords(player, "bluespawn");
					tellPlayer(player, lang.parse("setbluespawn"));
				} else if (args[0].equalsIgnoreCase("spectator")) {
					arena.setCoords(player, "spectator");
					tellPlayer(player, lang.parse("setspectator"));
				} else if (args[0].equalsIgnoreCase("exit")) {
					arena.setCoords(player, "exit");
					tellPlayer(player, lang.parse("setexit"));
				} else if (args[0].equalsIgnoreCase("forcestop")) {
					if (arena.fightInProgress) {
						tellPlayer(player, lang.parse("forcestop"));
						Set<String> set = arena.fightUsersTeam.keySet();
						Iterator<String> iter = set.iterator();
						while (iter.hasNext()) {
							Object o = iter.next();
							Player z = getServer().getPlayer(o.toString());
							arena.removePlayer(z, "spectator");
						}
						arena.reset();
						arena.fightUsersClass.clear();
					} else {
						tellPlayer(player, lang.parse("nofight"));
					}
				} else {
					tellPlayer(player, lang.parse("invalidcmd","501"));
					return false;
				}
				return true;

			} else {
				tellPlayer(player, lang.parse("invalidcmd","502"));
				return false;
			}
			return true;
		} else if (args.length == 3) {
			// /pa bet [name] [amount]
			if (!args[0].equalsIgnoreCase("bet")) {
				tellPlayer(player, lang.parse("invalidcmd","503"));
				return false;
			}
			if (!arena.fightUsersTeam.containsKey(player.getName())) {
				tellPlayer(player, lang.parse("betnotyours"));
				return true;
			}
			
			if (method == null)
				return true;
			
			if (!args[1].equalsIgnoreCase("red") && !args[1].equalsIgnoreCase("blue") && !arena.fightUsersTeam.containsKey(args[1])) {
				tellPlayer(player, lang.parse("betoptions"));
				return true;
			}
			
			double amount = 0;
			
			try {
				amount = Double.parseDouble(args[2]);
			} catch (Exception e) {
				tellPlayer(player, lang.parse("invalidamount",args[2]));
				return true;
			}
			MethodAccount ma = method.getAccount(player.getName());
			if (ma == null) {
				log.severe("Account not found: "+player.getName());
				return true;
			}
			if(!ma.hasEnough(arena.entryFee)){
				// no money, no entry!
				tellPlayer(player, lang.parse("notenough",method.format(amount)));
				return true;
            }
			ma.subtract(amount);
			tellPlayer(player, lang.parse("betplaced", args[1]));
			arena.bets.put(player.getName() + ":" + args[1], amount);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("teams")) {
			String team[] = StatsManager.getTeamStats(args[1]).split(";");
			player.sendMessage(lang.parse("teamstat", ChatColor.BLUE + lang.parse("blue"), team[0], team[1]));
			player.sendMessage(lang.parse("teamstat", ChatColor.RED + lang.parse("red"), team[2], team[3]));
			return true;
		} else if (args[0].equalsIgnoreCase("users")) {
			// wins are suffixed with "_"
			Map<String, Integer> players = StatsManager.getPlayerStats(args[1]);
			
			int wcount = 0;
	
			for (String name : players.keySet())
				if (name.endsWith("_"))
					wcount++;
			
			String[][] wins = new String[wcount][2];
			String[][] losses = new String[players.size()-wcount][2];
			int iw = 0;
			int il = 0;
		
			for (String name : players.keySet()) {
				if (name.endsWith("_")) {
					// playername_ => win
					wins[iw][0] = name.substring(0, name.length()-1);
					wins[iw++][1] = String.valueOf(players.get(name));
				} else {
					// playername => lose
					losses[il][0] = name;
					losses[il++][1] = String.valueOf(players.get(name));
				}
			}
			wins = sort(wins);
			losses = sort(losses);
			tellPlayer(player, lang.parse("top5wins"));
			
			for (int w=0; w<wins.length && w < 5 ; w++) {
				tellPlayer(player, wins[w][0] + ": " + wins[w][1] + " " + lang.parse("wins"));
			}
			
	
			tellPlayer(player, "------------");
			tellPlayer(player, lang.parse("top5lose"));
			
			for (int l=0; l<losses.length && l < 5 ; l++) {
				tellPlayer(player, losses[l][0] + ": " + losses[l][1] + " " + lang.parse("losses"));
			}
			return true;
		}
		
		if (!hasAdminPerms(player)) {
			tellPlayer(player, lang.parse("invalidcmd","504"));
			return false;
		}
		
		if ((args.length != 2) || (!args[0].equalsIgnoreCase("region"))) {
			tellPlayer(player, lang.parse("invalidcmd","505"));
			return false;
		}

		Configuration config = new Configuration(new File("plugins/pvparena", "config_" + sName + ".yml"));
		config.load();
		
		if (args[1].equalsIgnoreCase("set")) {
			if (!PAArena.regionmodify.equals("")) {
				tellPlayer(player, lang.parse("regionalreadybeingset", sName));
				return true;
			}
			if (config.getKeys("protection.region") == null) {
				PAArena.regionmodify = arena.name;
				tellPlayer(player, lang.parse("regionset"));
			} else {
				tellPlayer(player, lang.parse("regionalreadyset"));
			}
		} else if ((args[1].equalsIgnoreCase("modify"))
				|| (args[1].equalsIgnoreCase("edit"))) {
			if (!PAArena.regionmodify.equals("")) {
				tellPlayer(player, lang.parse("regionalreadybeingset", sName));
				return true;
			}
			if (config.getKeys("protection.region") != null) {
				PAArena.regionmodify = arena.name;
				tellPlayer(player, lang.parse("regionmodify"));
			} else {
				tellPlayer(player, lang.parse("noregionset"));
			}
		} else if (args[1].equalsIgnoreCase("save")) {
			if (PAArena.regionmodify.equals("")) {
				tellPlayer(player, lang.parse("regionnotbeingset", sName));
				return true;
			}
			if ((arena.pos1 == null) || (arena.pos2 == null)) {
				tellPlayer(player, lang.parse("set2points"));
			} else {
				config.setProperty("protection.region.min",
						arena.getMinimumPoint().getX() + ", "
								+ arena.getMinimumPoint().getY() + ", "
								+ arena.getMinimumPoint().getZ());
				config.setProperty("protection.region.max",
						arena.getMaximumPoint().getX() + ", "
								+ arena.getMaximumPoint().getY() + ", "
								+ arena.getMaximumPoint().getZ());
				config.setProperty("protection.region.world", player
						.getWorld().getName());
				config.save();
				PAArena.regionmodify = "";
				tellPlayer(player, lang.parse("regionsaved"));
			}
		} else if (args[1].equalsIgnoreCase("remove")) {
			if (config.getKeys("protection.region") != null) {
				config.removeProperty("protection.region");
				config.save();
				PAArena.regionmodify = "";
				tellPlayer(player, lang.parse("regionremoved"));
			} else {
				tellPlayer(player, lang.parse("regionnotremoved"));
			}

		} else {
			tellPlayer(player, lang.parse("invalidcmd","506"));
			return false;
		}
		
		return true;
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

	public static void tellPublic(String msg) {
		Bukkit.getServer().broadcastMessage(lang.parse("msgprefix") + ChatColor.WHITE + msg);
	}

	public static void tellPlayer(Player player, String msg) {
		player.sendMessage(lang.parse("msgprefix") + ChatColor.WHITE + msg);
	}

	public static void clearInventory(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setBoots(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
	}

	public static boolean emptyInventory(Player player) {
		ItemStack[] invContents = player.getInventory().getContents();
		ItemStack[] armContents = player.getInventory().getArmorContents();
		int invNullCounter = 0;
		int armNullCounter = 0;
		for (int i = 0; i < invContents.length; ++i) {
			if (invContents[i] == null) {
				++invNullCounter;
			}
		}
		for (int i = 0; i < armContents.length; ++i) {
			if (armContents[i].getType() == Material.AIR) {
				++armNullCounter;
			}
		}

		return ((invNullCounter == invContents.length) && (armNullCounter == armContents.length));
	}
	

	public static void colorizePlayer(Player player, String color) {
		if (color.equals("")) {
			
			String rn = player.getName();
			player.setDisplayName(rn);
			

			if (spoutHandler != null) {
				HumanEntity human = player;
				SpoutManager.getAppearanceManager().setGlobalTitle(human,rn);
			}
		    return;
		}
		
		String n = color + player.getName();

		player.setDisplayName(n.replaceAll("(&([a-f0-9]))", "§$2"));
		
		if (spoutHandler != null) {
			HumanEntity human = player;
			SpoutManager.getAppearanceManager().setGlobalTitle(human, n.replaceAll("(&([a-f0-9]))", "§$2"));
		}
	}

	public static String[][] sort(String[][] x) {
		boolean undone=true;
		String temp;
		String itemp;
		
		while (undone){
			undone = false;
			for (int i=0; i < x.length-1; i++) 
				if (Integer.parseInt(x[i][1]) < Integer.parseInt(x[i+1][1])) {                      
					temp       = x[i][1];
					x[i][1]       = x[i+1][1];
					x[i+1][1]     = temp;
                    
					itemp       = x[i][0];
					x[i][0]       = x[i+1][0];
					x[i+1][0]     = itemp;
					undone = true;
				}          
		} 
		return x;
	}
}