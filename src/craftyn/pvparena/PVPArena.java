package craftyn.pvparena;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

/*
 * main class
 * 
 * author: slipcor
 * 
 * version: v0.1.9 - teleport location configuration
 * 
 * history:
 *
 *    v0.1.8 - lives!
 *    v0.1.7 - commands to show who is playing and on what team
 *    v0.1.6 - custom class: fight with own items
 *    v0.1.5 - class choosing not toggling
 *    v0.1.4 - arena disable via command * disable / * enable
 *    v0.1.3 - ingame config reload
 *    v0.1.2 - class permission requirement
 *    v0.1.0 - release version
 *    v0.0.6 - /pa leave game end check
 *    v0.0.5 - reset signs on arena start
 *    v0.0.4 - iconomy subtracting fix
 *    v0.0.3 - save OLD HP and HUNGER
 *    v0.0.2 - /pa possibility fix
 *    v0.0.1 - everyone not on team saying /pa leave tps to exit
 * 
 * todo:

Additions
	(P)riority: 0 - no => 5 - critical
	(E)ffort: 0 - np => 5 - omg
	
	(P/E)
    (3/2) ability to not allow matches to start with uneven teams.
    (4/3) A way to tell teams apart somehow -> (nospout: wool; spout: as mentioned above)
    (2/2) configurable: after match/spectate return to old position
	(3/3) stats > wins/losses per team/person...
    (2/3) bet on a match.
    (2/4) multilanguage support
    (2/5) Add support for multiple arenas!
    
 *     CTF
 *     
 * 
 * history:
 * 		0.6.0 - copypaste
 */

public class PVPArena extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler permissionHandler;
	public static iConomy iConomy = null;
	public static PermissionHandler Permissions;
	private final PAServerListener serverListener = new PAServerListener(this);
	private final PAEntityListener entityListener = new PAEntityListener(this);
	private final PAPlayerListener playerListener = new PAPlayerListener(this);
	private final PABlockListener blockListener = new PABlockListener(this);

	public static final Map<String, String> fightUsersTeam = new HashMap<String, String>();
	public static final Map<String, String> fightUsersClass = new HashMap<String, String>();
	public static final Map<String, String> fightClasses = new HashMap<String, String>();
	public static final Map<String, Sign> fightSigns = new HashMap<String, Sign>();
	public static final Map<String, String> fightUsersRespawn = new HashMap<String, String>();
	public static final Map<String, String> fightTelePass = new HashMap<String, String>();
	public static final Map<String, Byte> fightUsersLives = new HashMap<String, Byte>();

	public static final HashMap<Player, ItemStack[]> savedinventories = new HashMap<Player, ItemStack[]>();
	public static final HashMap<Player, ItemStack[]> savedarmories = new HashMap<Player, ItemStack[]>();
	public static final HashMap<Player, Object> savedmisc = new HashMap<Player, Object>();

	static int redTeam = 0;
	static int blueTeam = 0;
	static Location pos1;
	static Location pos2;
	static boolean regionmodify;
	static boolean disableblockplacement;
	static boolean disableblockdamage;
	static boolean disableallfirespread;
	static boolean disablelavafirespread;
	static boolean blocktnt;
	static boolean blocklighter;
	static boolean protection;
	static boolean teamkilling;
	static boolean randomlyselectteams;
	static boolean manuallyselectteams;
	static boolean redTeamIronClicked = false;
	static boolean blueTeamIronClicked = false;
	static boolean fightInProgress = false;
	static boolean enabled = true;
	static int wand;
	static int entryFee;
	static int rewardAmount;
	static int maxlives;
	static String rewardItems;
	static String sTPwin;
	static String sTPlose;
	static String sTPexit;
	static String sTPdeath;
	public static final List<Material> ARMORS_TYPE = new LinkedList<Material>();
	public static final List<Material> HELMETS_TYPE = new LinkedList<Material>();
	public static final List<Material> CHESTPLATES_TYPE = new LinkedList<Material>();
	public static final List<Material> LEGGINGS_TYPE = new LinkedList<Material>();
	public static final List<Material> BOOTS_TYPE = new LinkedList<Material>();

	static {
		HELMETS_TYPE.add(Material.LEATHER_HELMET);
		HELMETS_TYPE.add(Material.GOLD_HELMET);
		HELMETS_TYPE.add(Material.CHAINMAIL_HELMET);
		HELMETS_TYPE.add(Material.IRON_HELMET);
		HELMETS_TYPE.add(Material.DIAMOND_HELMET);

		CHESTPLATES_TYPE.add(Material.LEATHER_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.GOLD_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.CHAINMAIL_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.IRON_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.DIAMOND_CHESTPLATE);

		LEGGINGS_TYPE.add(Material.LEATHER_LEGGINGS);
		LEGGINGS_TYPE.add(Material.GOLD_LEGGINGS);
		LEGGINGS_TYPE.add(Material.CHAINMAIL_LEGGINGS);
		LEGGINGS_TYPE.add(Material.IRON_LEGGINGS);
		LEGGINGS_TYPE.add(Material.DIAMOND_LEGGINGS);

		BOOTS_TYPE.add(Material.LEATHER_BOOTS);
		BOOTS_TYPE.add(Material.GOLD_BOOTS);
		BOOTS_TYPE.add(Material.CHAINMAIL_BOOTS);
		BOOTS_TYPE.add(Material.IRON_BOOTS);
		BOOTS_TYPE.add(Material.DIAMOND_BOOTS);

		ARMORS_TYPE.addAll(HELMETS_TYPE);
		ARMORS_TYPE.addAll(CHESTPLATES_TYPE);
		ARMORS_TYPE.addAll(LEGGINGS_TYPE);
		ARMORS_TYPE.addAll(BOOTS_TYPE);
	}

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

		log.info("[PVP Arena] enabled. (version " + pdfFile.getVersion() + ")");

		load_config();
	}
	
	private void load_config() {
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/config.yml");
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				log.info("[PVP Arena] Error when creating config file.");
			}

		Configuration config = new Configuration(configFile);
		config.load();

		if (config.getKeys("classes") == null) {
			config.setProperty("classes.Ranger.items","261,262:64,298,299,300,301");
			config.setProperty("classes.Swordsman.items", "276,306,307,308,309");
			config.setProperty("classes.Tank.items", "272,310,311,312,313");
			config.setProperty("classes.Pyro.items", "259,46:2,298,299,300,301");
			config.save();
		}
		if (config.getKeys("general") == null) {
			config.setProperty("general.readyblock","IRON_BLOCK");
			config.setProperty("general.lives",Integer.valueOf(3));
			config.setProperty("general.woolhead",Boolean.valueOf(false)); // enforce a wool head in case we dont have Spout installed
			config.setProperty("general.language","en");
			config.setProperty("general.tp.win","old"); // old || exit || spectator
			config.setProperty("general.tp.lose","old"); // old || exit || spectator
			config.setProperty("general.tp.exit","exit"); // old || exit || spectator
			config.setProperty("general.tp.death","spectator"); // old || exit || spectator
			config.setProperty("general.classperms",Boolean.valueOf(false)); // require permissions for a class
			config.save();
		}
		if (config.getKeys("rewards") == null) {
			config.load();
			config.setProperty("rewards.entry-fee", Integer.valueOf(0));
			config.setProperty("rewards.amount", Integer.valueOf(0));
			config.setProperty("rewards.items", "none");
			config.save();
		}
		if (config.getKeys("protection") == null) {
			config.load();
			config.setProperty("protection.enabled", Boolean.valueOf(false));
			config.setProperty("protection.wand", Integer.valueOf(280));
			config.setProperty("protection.player.disable-block-placement",Boolean.valueOf(true));
			config.setProperty("protection.player.disable-block-damage",Boolean.valueOf(true));
			config.setProperty("protection.fire.disable-all-fire-spread",Boolean.valueOf(true));
			config.setProperty("protection.fire.disable-lava-fire-spread",Boolean.valueOf(true));
			config.setProperty("protection.ignition.block-tnt",Boolean.valueOf(true));
			config.setProperty("protection.ignition.block-lighter",Boolean.valueOf(true));
			config.save();
		}
		if (config.getKeys("teams") == null) {
			config.load();
			config.setProperty("teams.team-killing-enabled",Boolean.valueOf(false));
			config.setProperty("teams.randomly-select-teams",Boolean.valueOf(true));
			config.setProperty("teams.manually-select-teams",Boolean.valueOf(false));
			config.save();
		}
		if (config.getKeys("team-killing") != null) {
			config.load();
			config.removeProperty("team-killing");
			config.save();
		}
		List<?> classes = config.getKeys("classes");
		fightClasses.clear();
		for (int i = 0; i < classes.size(); ++i) {
			String className = (String) classes.get(i);
			fightClasses.put(className,
					config.getString("classes." + className + ".items", null));
		}

		entryFee = config.getInt("rewards.entry-fee", 0);
		rewardAmount = config.getInt("rewards.amount", 0);
		rewardItems = config.getString("rewards.items", "none");

		teamkilling = config.getBoolean("teams.team-killing-enabled", false);
		randomlyselectteams = config.getBoolean("teams.randomly-select-teams",true);
		manuallyselectteams = config.getBoolean("teams.manually-select-teams",false);

		protection = config.getBoolean("protection.enabled", true);
		wand = config.getInt("protection.wand", 280);
		disableblockplacement = config.getBoolean("protection.player.disable-block-placement", true);
		disableblockdamage = config.getBoolean("protection.player.disable-block-damage", true);
		disableallfirespread = config.getBoolean("protection.fire.disable-all-fire-spread", true);
		disablelavafirespread = config.getBoolean("protection.fire.disable-lava-fire-spread", true);
		blocktnt = config.getBoolean("protection.ignition.block-tnt", true);
		blocklighter = config.getBoolean("protection.ignition.block-lighter",true);
		
		maxlives = config.getInt("general.lives", 3);
		
		sTPwin   = config.getString("general.tp.win","old"); // old || exit || spectate
		sTPlose  = config.getString("general.tp.lose","old"); // old || exit || spectate
		sTPexit  = config.getString("general.tp.exit","exit"); // old || exit || spectate
		sTPdeath = config.getString("general.tp.death","spectate"); // old || exit || spectate
		
		config.save();
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		log.info("[PVP Arena] disabled. (version " + pdfFile.getVersion() + ")");
		arenaReset();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if ((!commandLabel.equalsIgnoreCase("pvparena")) && (!commandLabel.equalsIgnoreCase("pa"))) {
			return true; // none of our business
		}
				
		String[] fightCmd = args;
		Player player = null;
		try {
			player = (Player) sender;
		} catch (Exception e) {
			sender.sendMessage("Only players may access this command!");
			return true;
		}

		if (!enabled && !hasAdminPerms(player)) {
			sender.sendMessage("Arena disabled please try again later!");
			return true;
		}
		
		if (args.length < 1) {
			// just /pa or /pvparena
			if (!(isSetup().booleanValue())) {
				tellPlayer(player, "All waypoints must be set up first.");
				return true;
			}
			if (!hasPerms(player)) {
				tellPlayer(player, "You don't have permission to join the arena!");
				return true;
			}
			if (!(randomlyselectteams)) {
				tellPlayer(player, "You must select a team to join!");
				return true;
			}
			if (savedmisc.containsKey(player)) {
				tellPlayer(player, "You already joined!");
				return true;
			}
			if (fightInProgress) {
				tellPlayer(player, "A fight is already in progress!");
				return true;
			}

			if ((iConomy != null) && !com.iConomy.iConomy.getAccount(player.getName()).getHoldings().hasEnough(entryFee)) {
				tellPlayer(player, "[PVP Arena] You don't have " + com.iConomy.iConomy.format(entryFee) + ".");
				return true;
			}
			cleanSigns();
			saveInventory(player);
			clearInventory(player);
			saveMisc(player); // save player health, fire tick, hunger etc
			player.setHealth(20);
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setSaturation(20);
			player.setExhaustion(0);
			player.setGameMode(GameMode.getByValue(0));
			PVPArena.fightUsersLives.put(player.getName(), (byte) maxlives);
			
			
			if (emptyInventory(player)) {
				if ((iConomy != null) && (entryFee > 0)) {
					Holdings balance = com.iConomy.iConomy.getAccount(player.getName()).getHoldings();

					balance.subtract(entryFee);
				}

				if (!(fightUsersTeam.containsKey(player.getName()))) {
					if (blueTeam > redTeam) {
						goToWaypoint(player, "redlounge");
						fightUsersTeam.put(player.getName(), "red");
						tellPlayer(player, "Welcome! You are on team "
								+ ChatColor.RED + "<Red>");
						tellEveryoneExcept(player, player.getName()
								+ " has joined team " + ChatColor.RED
								+ "<Red>");
						redTeam += 1;
					} else {
						goToWaypoint(player, "bluelounge");
						fightUsersTeam.put(player.getName(), "blue");
						blueTeam += 1;
						tellPlayer(player, "Welcome! You are on team "
								+ ChatColor.BLUE + "<Blue>");
						tellEveryoneExcept(player, player.getName()
								+ " has joined team " + ChatColor.BLUE
								+ "<Blue>");
					}

				} else {
					tellPlayer(player, "You have already joined a team!");
				}

			} else if (fightUsersTeam.containsKey(player.getName())) {
				tellPlayer(player, "You have already joined a team!");
			} else {
				tellPlayer(player,"You cannot join, you already did.");
			}
			return true;
		}

		if (args.length == 1) {

			if (fightCmd[0].equalsIgnoreCase("enable")) {
				if (!hasAdminPerms(player)) {
					tellPlayer(player, "You don't have permission to enable!");
					return true;
				}
				enabled = true;
				tellPlayer(player, "Enabled!");
				return true;
			} else if (fightCmd[0].equalsIgnoreCase("disable")) {
				if (!hasAdminPerms(player)) {
					tellPlayer(player, "You don't have permission to disable!");
					return true;
				}
				enabled = false;
				tellPlayer(player, "Disabled!");
				return true;
			} else if (fightCmd[0].equalsIgnoreCase("reload")) {
				if (!hasAdminPerms(player)) {
					tellPlayer(player, "You don't have permission to reload!");
					return true;
				}
				load_config();
				tellPlayer(player, "Config reloaded!");
				return true;
			} else if (fightCmd[0].equalsIgnoreCase("list")) {
				if ((PVPArena.fightUsersTeam == null) || (PVPArena.fightUsersTeam.size() < 1)) {
					tellPlayer(player, "No player in the PVP Arena!");
					return true;
				}
				String plrs = "";
				for (String sPlayer : PVPArena.fightUsersTeam.keySet()) {
					if (!plrs.equals(""))
						plrs +=", ";
					plrs += (PVPArena.fightUsersTeam.get(sPlayer).equals("red")?ChatColor.RED:ChatColor.BLUE) + sPlayer + ChatColor.WHITE;
				}
				tellPlayer(player, "Players: " + plrs);
				return true;
			} else if (fightCmd[0].equalsIgnoreCase("red")) {
				

				// /pa red or /pvparena red
				if (!(isSetup().booleanValue())) {
					tellPlayer(player, "All waypoints must be set up first.");
					return true;
				}
				if (!hasPerms(player)) {
					tellPlayer(player, "You don't have permission to join the arena!");
					return true;
				}
				if (!(manuallyselectteams)) {
					tellPlayer(player, "You must select a team to join!");
					return true;
				}
				if (fightUsersTeam.containsKey(player.getName())) {
					tellPlayer(player, "You have already joined a team!");
					return true;
				}
				
				if (savedmisc.containsKey(player)) {
					tellPlayer(player, "You already joined!");
					return true;
				}
				if (fightInProgress) {
					tellPlayer(player, "A fight is already in progress!");
					return true;
				}
				
				if ((iConomy != null) && !com.iConomy.iConomy.getAccount(player.getName()).getHoldings().hasEnough(entryFee)) {
					tellPlayer(player, "[PVP Arena] You don't have " + com.iConomy.iConomy.format(entryFee) + ".");
					return true;
				}

				cleanSigns();
				saveInventory(player);
				clearInventory(player);
				saveMisc(player); // save player health, fire tick, hunger etc
				player.setHealth(20);
				player.setFireTicks(0);
				
				if (emptyInventory(player)) {
					if ((iConomy != null) && (entryFee > 0)) {
						Holdings balance = com.iConomy.iConomy.getAccount(
								player.getName()).getHoldings();
						balance.subtract(entryFee);
					}

					if (!(fightUsersTeam.containsKey(player.getName()))) {
						goToWaypoint(player, "redlounge");
						fightUsersTeam.put(player.getName(), "red");
						tellPlayer(player, "Welcome! You are on team "
								+ ChatColor.RED + "<Red>");
						tellEveryoneExcept(player, player.getName()
								+ " has joined team " + ChatColor.RED
								+ "<Red>");
						redTeam += 1;
					} else {
						tellPlayer(player,
								"You have already joined a team!");
					}

				}

			} else if (fightCmd[0].equalsIgnoreCase("blue")) {
				
				// /pa blue or /pvparena blue
				if (!(isSetup().booleanValue())) {
					tellPlayer(player, "All waypoints must be set up first.");
					return true;
				}
				if (!hasPerms(player)) {
					tellPlayer(player, "You don't have permission to join the arena!");
					return true;
				}
				if (!(manuallyselectteams)) {
					tellPlayer(player, "You must select a team to join!");
					return true;
				}
				if (fightUsersTeam.containsKey(player.getName())) {
					tellPlayer(player, "You have already joined a team!");
					return true;
				}
				
				if (savedmisc.containsKey(player)) {
					tellPlayer(player, "You already joined!");
					return true;
				}

				if (fightInProgress) {
					tellPlayer(player, "A fight is already in progress!");
					return true;
				}
				
				if ((iConomy != null) && !com.iConomy.iConomy.getAccount(player.getName()).getHoldings().hasEnough(entryFee)) {
					tellPlayer(player, "[PVP Arena] You don't have " + com.iConomy.iConomy.format(entryFee) + ".");
					return true;
				}

				cleanSigns();
				saveInventory(player);
				clearInventory(player);
				saveMisc(player); // save player health, fire tick, hunger etc
				player.setHealth(20);
				player.setFireTicks(0);

				if (emptyInventory(player)) {
					if ((iConomy != null) && (entryFee > 0)) {
						Holdings balance = com.iConomy.iConomy.getAccount(
								player.getName()).getHoldings();
						balance.subtract(entryFee);
					}

					if (!(fightUsersTeam.containsKey(player.getName()))) {
						goToWaypoint(player, "bluelounge");
						fightUsersTeam.put(player.getName(), "blue");
						blueTeam += 1;
						tellPlayer(player, "Welcome! You are on team "
								+ ChatColor.BLUE + "<Blue>");
						tellEveryoneExcept(player, player.getName()
								+ " has joined team " + ChatColor.BLUE
								+ "<Blue>");
					}

				}
				return true;
			
			} else if (fightCmd[0].equalsIgnoreCase("watch")) {

				if (!(isSetup().booleanValue())) {
					tellPlayer(player, "All waypoints must be set up first.");
					return true;
				}
				
				goToWaypoint(player, "spectator");
				tellPlayer(player, "Welcome to the spectator's area!");
				if (fightUsersTeam.containsKey(player.getName())) {
					if (fightUsersTeam.get(player.getName()) == "red") {
						redTeam -= 1;
						tellEveryoneExcept(player,
								ChatColor.RED + player.getName()
										+ ChatColor.WHITE
										+ " has left the fight!");
					}
					if (fightUsersTeam.get(player.getName()) == "blue") {
						blueTeam -= 1;
						tellEveryoneExcept(player,
								ChatColor.BLUE + player.getName()
										+ ChatColor.WHITE
										+ " has left the fight!");
					}
					fightUsersTeam.remove(player.getName());
					fightUsersClass.remove(player.getName());
					cleanSigns(player.getName());
					clearInventory(player);
					setInventory(player);
				}
				return true;
			} else if (fightCmd[0].equalsIgnoreCase("leave")) {
				if (fightUsersTeam.containsKey(player.getName())) {
					if (fightUsersTeam.get(player.getName()) == "red") {
						redTeam -= 1;
						tellEveryoneExcept(player,
								ChatColor.RED + player.getName()
										+ ChatColor.WHITE
										+ " has left the fight!");
					}
					if (fightUsersTeam.get(player.getName()) == "blue") {
						blueTeam -= 1;
						tellEveryoneExcept(player,
								ChatColor.BLUE + player.getName()
										+ ChatColor.WHITE
										+ " has left the fight!");
					}
					tellPlayer(player, "You have left the fight!");

					if (PVPArena.checkEnd())
						return true;
					PVPArena.removePlayer(player, sTPexit);
					
					
				} else {
					//tellPlayer(player, "You are not in a team.");
					goToWaypoint(player, "exit");
					tellPlayer(player, "You have left the arena.");
				}
				return true;
			} else if (hasAdminPerms(player)) {
				if (fightCmd[0].equalsIgnoreCase("redlounge")) {
					setCoords(player, "redlounge");
					tellPlayer(player, "Red lounge set.");
				} else if (fightCmd[0].equalsIgnoreCase("redspawn")) {
					setCoords(player, "redspawn");
					tellPlayer(player, "Red spawn set.");
				} else if (fightCmd[0].equalsIgnoreCase("bluelounge")) {
					setCoords(player, "bluelounge");
					tellPlayer(player, "Blue lounge set.");
				} else if (fightCmd[0].equalsIgnoreCase("bluespawn")) {
					setCoords(player, "bluespawn");
					tellPlayer(player, "Blue spawn set.");
				} else if (fightCmd[0].equalsIgnoreCase("spectator")) {
					setCoords(player, "spectator");
					tellPlayer(player, "Spectator area set.");
				} else if (fightCmd[0].equalsIgnoreCase("exit")) {
					setCoords(player, "exit");
					tellPlayer(player, "Exit area set.");
				} else if (fightCmd[0].equalsIgnoreCase("forcestop")) {
					if (fightInProgress) {
						tellPlayer(player, "You have forced the fight to stop.");
						Set<String> set = fightUsersTeam.keySet();
						Iterator<String> iter = set.iterator();
						while (iter.hasNext()) {
							Object o = iter.next();
							Player z = getServer().getPlayer(o.toString());
							z.setHealth(20);
							z.setFireTicks(0);
							clearInventory(z);
							goToWaypoint(z, "spectator");
							setInventory(z);
						}
						arenaReset();
					} else {
						tellPlayer(player, "There is no fight in progress.");
					}
				} else {
					tellPlayer(player, "Invalid Command. (502)");
					return false;
				}
				return true;

			} else {
				tellPlayer(player, "Invalid Command. (503)");
				return false;
			}
			return true;
		}

		if (!hasAdminPerms(player)) {
			tellPlayer(player, "Invalid Command. (504)");
			return false;
		}
		
		if ((args.length != 2) || (!fightCmd[0].equalsIgnoreCase("region"))) {
			tellPlayer(player, "Invalid Command. (505)");
			return false;
		}

		Configuration config = new Configuration(new File(
				"plugins/pvparena", "config.yml"));
		config.load();
		
		if (fightCmd[1].equalsIgnoreCase("set")) {
			if (config.getKeys("protection.region") == null) {
				regionmodify = true;
				tellPlayer(player, "Setting region enabled.");
			} else {
				tellPlayer(player, "A region has already been created.");
			}
		} else if ((fightCmd[1].equalsIgnoreCase("modify"))
				|| (fightCmd[1].equalsIgnoreCase("edit"))) {
			if (config.getKeys("protection.region") != null) {
				regionmodify = true;
				tellPlayer(player, "Modifying region enabled.");
			} else {
				tellPlayer(player, "You must setup a region first.");
			}
		} else if (fightCmd[1].equalsIgnoreCase("save")) {
			if ((pos1 == null) || (pos2 == null)) {
				tellPlayer(player, "You must set two points first.");
			} else {
				config.setProperty("protection.region.min",
						getMinimumPoint().getX() + ", "
								+ getMinimumPoint().getY() + ", "
								+ getMinimumPoint().getZ());
				config.setProperty("protection.region.max",
						getMaximumPoint().getX() + ", "
								+ getMaximumPoint().getY() + ", "
								+ getMaximumPoint().getZ());
				config.setProperty("protection.region.world", player
						.getWorld().getName());
				config.save();
				regionmodify = false;
				tellPlayer(player, "Region saved.");
			}
		} else if (fightCmd[1].equalsIgnoreCase("remove")) {
			if (config.getKeys("protection.region") != null) {
				config.removeProperty("protection.region");
				config.save();
				regionmodify = false;
				tellPlayer(player, "Region removed.");
			} else {
				tellPlayer(player, "There is no region setup.");
			}

		} else {
			tellPlayer(player, "Invalid Command. (506)");
			return false;
		}
		
		return true;
	}

	private boolean hasAdminPerms(Player player) {
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

	public static void arenaReset() {
		cleanSigns();
		clearArena();
		fightInProgress = false;
		redTeamIronClicked = false;
		blueTeamIronClicked = false;
		fightUsersTeam.clear();
		fightUsersClass.clear();
		redTeam = 0;
		blueTeam = 0;
		fightSigns.clear();
	}

	public void setCoords(Player player, String place) {
		Location location = player.getLocation();
		File configFile = new File("plugins/pvparena/config.yml");
		Configuration config = new Configuration(configFile);
		config.load();
		config.setProperty("coords." + place + ".world", location.getWorld()
				.getName());
		config.setProperty("coords." + place + ".x",
				Double.valueOf(location.getX()));
		config.setProperty("coords." + place + ".y",
				Double.valueOf(location.getY()));
		config.setProperty("coords." + place + ".z",
				Double.valueOf(location.getZ()));
		config.setProperty("coords." + place + ".yaw",
				Float.valueOf(location.getYaw()));
		config.setProperty("coords." + place + ".pitch",
				Float.valueOf(location.getPitch()));
		config.save();
	}

	public static Location getCoords(String place) {
		File configFile = new File("plugins/pvparena/config.yml");
		Configuration config = new Configuration(configFile);
		config.load();
		Double x = Double.valueOf(config.getDouble("coords." + place + ".x",
				0.0D));
		Double y = Double.valueOf(config.getDouble("coords." + place + ".y",
				0.0D));
		Double z = Double.valueOf(config.getDouble("coords." + place + ".z",
				0.0D));
		Float yaw = new Float(config.getString("coords." + place + ".yaw"));
		Float pitch = new Float(config.getString("coords." + place + ".pitch"));
		World world = Bukkit.getServer().getWorld(
				config.getString("coords." + place + ".world"));
		return new Location(world, x.doubleValue(), y.doubleValue(),
				z.doubleValue(), yaw.floatValue(), pitch.floatValue());
	}

	public Boolean isSetup() {
		File configFile = new File("plugins/pvparena/config.yml");
		Configuration config = new Configuration(configFile);
		config.load();
		if (config.getKeys("coords") == null) {
			return Boolean.valueOf(false);
		}

		List<?> list = config.getKeys("coords");
		if (list.size() == 6) {
			return Boolean.valueOf(true);
		}

		return Boolean.valueOf(false);
	}

	public static void equipArmorPiece(ItemStack stack, PlayerInventory inv) {
		Material type = stack.getType();
		if (HELMETS_TYPE.contains(type))
			inv.setHelmet(stack);
		else if (CHESTPLATES_TYPE.contains(type))
			inv.setChestplate(stack);
		else if (LEGGINGS_TYPE.contains(type))
			inv.setLeggings(stack);
		else if (BOOTS_TYPE.contains(type))
			inv.setBoots(stack);
	}

	public static void giveItems(Player player) {
		String playerClass = (String) fightUsersClass.get(player.getName());
		String rawItems = (String) fightClasses.get(playerClass);

		String[] items = rawItems.split(",");

		for (int i = 0; i < items.length; ++i) {
			String item = items[i];
			String[] itemDetail = item.split(":");
			if (itemDetail.length == 2) {
				int x = Integer.parseInt(itemDetail[0]);
				int y = Integer.parseInt(itemDetail[1]);
				ItemStack stack = new ItemStack(x, y);
				if (ARMORS_TYPE.contains(stack.getType())) {
					equipArmorPiece(stack, player.getInventory());
				} else {
					player.getInventory().addItem(new ItemStack[] { stack });
				}
			} else {
				int x = Integer.parseInt(itemDetail[0]);
				ItemStack stack = new ItemStack(x, 1);
				if (ARMORS_TYPE.contains(stack.getType())) {
					equipArmorPiece(stack, player.getInventory());
				} else {
					player.getInventory().addItem(new ItemStack[] { stack });
				}
			}
		}
	}

	private void setupPermissions() {
		Plugin test = getServer().getPluginManager().getPlugin("Permissions");
		if (Permissions != null)
			return;
		if (test != null)
			Permissions = ((Permissions) test).getHandler();
		else
			System.out.println("[PVP Arena] Permissions plugin not found, defaulting to ops.txt.");
	}

	public static void cleanSigns() {
		Set<String> set = fightSigns.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Sign sign = (Sign) fightSigns.get(o.toString());
			sign.setLine(2, "");
			sign.setLine(3, "");
			sign.update();
		}
	}

	public static void cleanSigns(String player) {
		Set<String> set = fightSigns.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Sign sign = (Sign) fightSigns.get(o.toString());
			if (sign.getLine(2).equals(player)) {
				sign.setLine(2, "");
				sign.update();
			}
			if (sign.getLine(3).equals(player)) {
				sign.setLine(3, "");
				sign.update();
			}
		}
	}

	public static boolean teamReady(String color) {
		int members = 0;
		int membersReady = 0;
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (((String) fightUsersTeam.get(o.toString())).equals(color)) {
				++members;
				if (fightUsersClass.containsKey(o.toString())) {
					++membersReady;
				}
			}
		}
		if ((members == membersReady) && (members > 0)) {
			if (color == "red") {
				return true;
			}
			if (color == "blue")
				return true;
		} else {
			return false;
		}
		return false;
	}

	public static void tellEveryone(String msg) {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = Bukkit.getServer().getPlayer(o.toString());
			z.sendMessage(ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE + msg);
		}
	}

	public void tellEveryoneExcept(Player player, String msg) {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = getServer().getPlayer(o.toString());
			if (!(player.getName().equals(z.getName())))
				z.sendMessage(ChatColor.YELLOW + "[PVP Arena] "
						+ ChatColor.WHITE + msg);
		}
	}

	public void tellTeam(String color, String msg) {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (((String) fightUsersTeam.get(o.toString())).equals(color)) {
				Player z = getServer().getPlayer(o.toString());
				z.sendMessage(ChatColor.YELLOW + "[PVP Arena] " + msg);
			}
		}
	}

	public static void tellPlayer(Player player, String msg) {
		player.sendMessage(ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE
				+ msg);
	}

	public void teleportAllToSpawn() {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (((String) fightUsersTeam.get(o.toString())).equals("red")) {
				Player z = getServer().getPlayer(o.toString());
				goToWaypoint(z, "redspawn");
			}
			if (((String) fightUsersTeam.get(o.toString())).equals("blue")) {
				Player z = getServer().getPlayer(o.toString());
				goToWaypoint(z, "bluespawn");
			}
		}
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

	public static void saveInventory(Player player) {
		savedinventories.put(player, player.getInventory().getContents());
		savedarmories.put(player, player.getInventory().getArmorContents());
	}

	public static void setInventory(Player player) {
		player.getInventory().setContents((ItemStack[]) savedinventories.get(player));
		player.getInventory().setArmorContents((ItemStack[]) savedarmories.get(player));
	}

	private void saveMisc(Player player) {
		HashMap<String, String> tempMap = new HashMap<String, String>();
		
		Location lLoc = player.getLocation();
		String sLoc = lLoc.getWorld().getName() + "/" + lLoc.getBlockX() + "/" + lLoc.getBlockY() + "/" + lLoc.getBlockZ() + "/";
		
		tempMap.put("EXHAUSTION", String.valueOf(player.getExhaustion()));
		tempMap.put("FIRETICKS", String.valueOf(player.getFireTicks()));
		tempMap.put("FOODLEVEL", String.valueOf(player.getFoodLevel()));
		tempMap.put("HEALTH", String.valueOf(player.getHealth()));
		tempMap.put("SATURATION", String.valueOf(player.getSaturation()));
		tempMap.put("LOCATION", sLoc);
		savedmisc.put(player, tempMap);
	}

	public static void giveRewards(Player player) {
		if (rewardAmount <= 0) {
			return;
		}
		if (iConomy != null) {
			Holdings balance = com.iConomy.iConomy.getAccount(player.getName())
					.getHoldings();
			balance.add(rewardAmount);
			tellPlayer(
					player,
					"You have been awarded "
							+ com.iConomy.iConomy.format(rewardAmount));
		}
		if (rewardItems.equals("none"))
			return;
		String[] items = rewardItems.split(",");
		for (int i = 0; i < items.length; ++i) {
			String item = items[i];
			String[] itemDetail = item.split(":");
			if (itemDetail.length == 2) {
				int x = Integer.parseInt(itemDetail[0]);
				int y = Integer.parseInt(itemDetail[1]);
				ItemStack stack = new ItemStack(x, y);
				player.getInventory().setItem(i, stack);
			} else {
				int x = Integer.parseInt(itemDetail[0]);
				ItemStack stack = new ItemStack(x, 1);
				player.getInventory().setItem(i, stack);
			}
		}
	}

	public static void clearArena() {
		Configuration config = new Configuration(new File("plugins/pvparena","config.yml"));
		config.load();
		if (config.getKeys("protection.region") == null)
			return;
		World world = Bukkit.getServer().getWorld(
				config.getString("protection.region.world"));
		for (Entity e : world.getEntities()) {
			if (((!(e instanceof Item)) && (!(e instanceof Arrow)))
					|| (!(contains(new Vector(e.getLocation().getX(), e
							.getLocation().getY(), e.getLocation().getZ())))))
				continue;
			e.remove();
		}
	}

	public static void goToWaypoint(Player player, String place) {
		fightTelePass.put(player.getName(), "yes");
		player.teleport(getCoords(place));
		fightTelePass.remove(player.getName());
	}

	public static Vector getMinimumPoint() {
		return new Vector(Math.min(pos1.getX(), pos2.getX()), Math.min(
				pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
	}

	public static Vector getMaximumPoint() {
		return new Vector(Math.max(pos1.getX(), pos2.getX()), Math.max(
				pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
	}

	public static boolean contains(Vector pt) {
		Configuration config = new Configuration(new File("plugins/pvparena",
				"config.yml"));
		config.load();
		String[] min1 = config.getString("protection.region.min").split(", ");
		String[] max1 = config.getString("protection.region.max").split(", ");
		BlockVector min = new BlockVector(new Double(min1[0]).doubleValue(),
				new Double(min1[1]).doubleValue(),
				new Double(min1[2]).doubleValue());
		BlockVector max = new BlockVector(new Double(max1[0]).doubleValue(),
				new Double(max1[1]).doubleValue(),
				new Double(max1[2]).doubleValue());
		int x = pt.getBlockX();
		int y = pt.getBlockY();
		int z = pt.getBlockZ();

		return ((x >= min.getBlockX()) && (x <= max.getBlockX())
				&& (y >= min.getBlockY()) && (y <= max.getBlockY())
				&& (z >= min.getBlockZ()) && (z <= max.getBlockZ()));
	}

	public static boolean checkEnd() {
		boolean bluewon = false;
		boolean bluemember = false;
		if ((PVPArena.redTeam > 0) && (PVPArena.blueTeam == 0)) {
			tellEveryone(ChatColor.RED
					+ "Red Team are the Champions!");
		} else if ((PVPArena.redTeam == 0) && (PVPArena.blueTeam > 0)) {
			tellEveryone(ChatColor.BLUE
					+ "Blue Team are the Champions!");
			bluewon = true;
		} else {
			return false;
		}
		
		Set<String> set = PVPArena.fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			bluemember =(((String) fightUsersTeam.get(o.toString())).equals("blue"));
			
			Player z = Bukkit.getServer().getPlayer(o.toString());
			if (bluewon == bluemember) {
				loadPlayer(z, sTPwin);
				try {
					giveRewards(Bukkit.getPlayer(o.toString())); // if we are the winning team, give reward!
				} catch (Exception e) {
					// offline => error => no goodies :p
				}
			} else {
				loadPlayer(z, sTPlose);
			}
		}
		arenaReset();
		return true;
	}
	
	public static void removePlayer(Player player, String tploc) {
		loadPlayer(player, tploc);		
		PVPArena.fightUsersTeam.remove(player.getName());
		PVPArena.fightUsersClass.remove(player.getName());
		PVPArena.cleanSigns(player.getName());
	}

	@SuppressWarnings("unchecked")
	public static void loadPlayer(Player player, String string) {

		HashMap<String, String> tSM = (HashMap<String, String>) savedmisc.get(player);
		if (tSM != null) {
			try {
				player.setExhaustion(Float.parseFloat(tSM.get("EXHAUSTION")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid EXHAUSTION entry!");
			}
			try {
				player.setFireTicks(Integer.parseInt(tSM.get("FIRETICKS")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid FIRETICKS entry!");
			}
			try {
				player.setFoodLevel(Integer.parseInt(tSM.get("FOODLEVEL")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid FOODLEVEL entry!");
			}
			try {
				player.setHealth(Integer.parseInt(tSM.get("HEALTH")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid HEALTH entry!");
			}
			try {
				player.setSaturation(Float.parseFloat(tSM.get("SATURATION")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid SATURATION entry!");
			}

			fightTelePass.put(player.getName(), "yes");
			if (string.equalsIgnoreCase("old")) {
				try {
					String sLoc = tSM.get("LOCATION");
					String[] aLoc = sLoc.split("/");
					Location lLoc = new Location(Bukkit.getWorld(aLoc[0]), Double.parseDouble(aLoc[1]), Double.parseDouble(aLoc[2]), Double.parseDouble(aLoc[3]));
					player.teleport(lLoc);
				} catch (Exception e) {
					System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid LOCATION entry!");
				}
			} else {
				Location l = PVPArena.getCoords(string);
				player.teleport(l);
			}
			fightTelePass.remove(player.getName());
			savedmisc.remove(player);
		} else {
			System.out.println("[PVP Arena] player '" + player.getName() + "' had no savedmisc entries!");
		}
		String sClass = "exit";
		if (PVPArena.fightUsersRespawn.get(player.getName()) != null) {
			sClass = PVPArena.fightUsersRespawn.get(player.getName());
		} else if (PVPArena.fightUsersClass.get(player.getName()) != null) {
			PVPArena.fightUsersClass.get(player.getName());
		}
		if (!sClass.equalsIgnoreCase("custom")) {
			PVPArena.clearInventory(player);
			PVPArena.setInventory(player);
		}
	}
}