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
import org.bukkit.entity.HumanEntity;
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
import org.getspout.spoutapi.SpoutManager;

/*
 * main class
 * 
 * author: slipcor
 * 
 * version: v0.2.0 - language support
 * 
 * history:
 *
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
 * Add support for multiple arenas!
 * CTF
 * 
 */

public class PVPArena extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler permissionHandler;
	public static String spoutHandler = null;
	public static iConomy iConomy = null;
	public static PermissionHandler Permissions;
	private final PAServerListener serverListener = new PAServerListener(this);
	private final PAEntityListener entityListener = new PAEntityListener(this);
	private final PAPlayerListener playerListener = new PAPlayerListener(this);
	private final PABlockListener blockListener = new PABlockListener(this);
	public static final PALanguage lang = new PALanguage();

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
	public static final HashMap<String, Double> bets = new HashMap<String, Double>();

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
	static boolean forceeven;
	static boolean woolhead;
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

		lang.log_info("enabled", pdfFile.getVersion());

		load_config();
	}
	
	private void load_config() {
		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/config.yml");
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				lang.log_error("filecreateerror","config");
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
			config.setProperty("general.forceeven",Boolean.valueOf(false)); // require even teams
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
		woolhead = config.getBoolean("general.woolhead", false);
		
		sTPwin   = config.getString("general.tp.win","old"); // old || exit || spectator
		sTPlose  = config.getString("general.tp.lose","old"); // old || exit || spectator
		sTPexit  = config.getString("general.tp.exit","exit"); // old || exit || spectator
		sTPdeath = config.getString("general.tp.death","spectator"); // old || exit || spectator
		forceeven = config.getBoolean("general.forceeven", false);
		
		try {
			spoutHandler = org.getspout.spout.Spout.getInstance().toString();
		} catch (Exception e) {
			lang.log_info("nospout");
		}
		
		config.save();
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		lang.log_info("disabled", pdfFile.getVersion());
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
			lang.parse("onlyplayers");
			return true;
		}

		if (!enabled && !hasAdminPerms(player)) {
			lang.parse("arenadisabled");
			return true;
		}
		
		if (args.length < 1) {
			// just /pa or /pvparena
			if (!(isSetup().booleanValue())) {
				tellPlayer(player, lang.parse("arenanotsetup"));
				return true;
			}
			if (!hasPerms(player)) {
				tellPlayer(player, lang.parse("permjoin"));
				return true;
			}
			if (!(randomlyselectteams)) {
				tellPlayer(player, lang.parse("selectteam"));
				return true;
			}
			if (savedmisc.containsKey(player)) {
				tellPlayer(player, lang.parse("alreadyjoined"));
				return true;
			}
			if (fightInProgress) {
				tellPlayer(player, lang.parse("fightinprogress"));
				return true;
			}

			if ((iConomy != null) && !com.iConomy.iConomy.getAccount(player.getName()).getHoldings().hasEnough(entryFee)) {
				tellPlayer(player, lang.parse("notenough", com.iConomy.iConomy.format(entryFee)));
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
						tellPlayer(player, lang.parse("youjoined", ChatColor.RED + "<Red>"));
						tellEveryoneExcept(player, lang.parse("playerjoined", player.getName(), ChatColor.RED + "<Red>"));
						redTeam += 1;
					} else {
						goToWaypoint(player, "bluelounge");
						fightUsersTeam.put(player.getName(), "blue");
						tellPlayer(player, lang.parse("youjoined", ChatColor.BLUE + "<Blue>"));
						tellEveryoneExcept(player, lang.parse("playerjoined", player.getName(), ChatColor.BLUE + "<Blue>"));
						blueTeam += 1;
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

			if (fightCmd[0].equalsIgnoreCase("enable")) {
				if (!hasAdminPerms(player)) {
					tellPlayer(player, lang.parse("nopermto", lang.parse("enable")));
					return true;
				}
				enabled = true;
				tellPlayer(player, lang.parse("enabled"));
				return true;
			} else if (fightCmd[0].equalsIgnoreCase("disable")) {
				if (!hasAdminPerms(player)) {
					tellPlayer(player, lang.parse("nopermto", lang.parse("disable")));
					return true;
				}
				enabled = false;
				tellPlayer(player, lang.parse("disabled"));
			} else if (fightCmd[0].equalsIgnoreCase("reload")) {
				if (!hasAdminPerms(player)) {
					tellPlayer(player, lang.parse("nopermto", lang.parse("reload")));
					return true;
				}
				load_config();
				tellPlayer(player, lang.parse("reloaded"));
				return true;
			} else if (fightCmd[0].equalsIgnoreCase("list")) {
				if ((PVPArena.fightUsersTeam == null) || (PVPArena.fightUsersTeam.size() < 1)) {
					tellPlayer(player, lang.parse("noplayer"));
					return true;
				}
				String plrs = "";
				for (String sPlayer : PVPArena.fightUsersTeam.keySet()) {
					if (!plrs.equals(""))
						plrs +=", ";
					plrs += (PVPArena.fightUsersTeam.get(sPlayer).equals("red")?ChatColor.RED:ChatColor.BLUE) + sPlayer + ChatColor.WHITE;
				}
				tellPlayer(player, lang.parse("players") + ": " + plrs);
				return true;
			} else if (fightCmd[0].equalsIgnoreCase("red")) {
				

				// /pa red or /pvparena red
				if (!(isSetup().booleanValue())) {
					tellPlayer(player, lang.parse("arenanotsetup"));
					return true;
				}
				if (!hasPerms(player)) {
					tellPlayer(player, lang.parse("permjoin"));
					return true;
				}
				if (!(manuallyselectteams)) {
					tellPlayer(player, lang.parse("notselectteam"));
					return true;
				}
				if (savedmisc.containsKey(player)) {
					tellPlayer(player, lang.parse("alreadyjoined"));
					return true;
				}
				if (fightInProgress) {
					tellPlayer(player, lang.parse("fightinprogress"));
					return true;
				}
				
				
				if ((iConomy != null) && !com.iConomy.iConomy.getAccount(player.getName()).getHoldings().hasEnough(entryFee)) {
					tellPlayer(player, lang.parse("notenough", com.iConomy.iConomy.format(entryFee)));
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

					goToWaypoint(player, "redlounge");
					tellPlayer(player, lang.parse("youjoined", ChatColor.RED + "<Red>"));
					tellEveryoneExcept(player, lang.parse("playerjoined", player.getName(), ChatColor.RED + "<Red>"));
					redTeam += 1;

				}

			} else if (fightCmd[0].equalsIgnoreCase("blue")) {
				
				// /pa blue or /pvparena blue

				if (!(isSetup().booleanValue())) {
					tellPlayer(player, lang.parse("arenanotsetup"));
					return true;
				}
				if (!hasPerms(player)) {
					tellPlayer(player, lang.parse("permjoin"));
					return true;
				}
				if (!(manuallyselectteams)) {
					tellPlayer(player, lang.parse("notselectteam"));
					return true;
				}
				if (savedmisc.containsKey(player)) {
					tellPlayer(player, lang.parse("alreadyjoined"));
					return true;
				}
				if (fightInProgress) {
					tellPlayer(player, lang.parse("fightinprogress"));
					return true;
				}
				
				
				
				if ((iConomy != null) && !com.iConomy.iConomy.getAccount(player.getName()).getHoldings().hasEnough(entryFee)) {
					tellPlayer(player, lang.parse("notenough", com.iConomy.iConomy.format(entryFee)));
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


					goToWaypoint(player, "bluelounge");
					fightUsersTeam.put(player.getName(), "blue");
					blueTeam += 1;
					tellPlayer(player, lang.parse("youjoined", ChatColor.BLUE + "<Blue>"));
					tellEveryoneExcept(player, lang.parse("playerjoined", player.getName(), ChatColor.BLUE + "<Blue>"));
					

				}
				return true;
			
			} else if (fightCmd[0].equalsIgnoreCase("watch")) {

				if (!(isSetup().booleanValue())) {
					tellPlayer(player, lang.parse("arenanotsetup"));
					return true;
				}
				if (fightUsersTeam.containsKey(player.getName())) {
					tellPlayer(player, lang.parse("alreadyjoined"));
					return true;
				}
				goToWaypoint(player, "spectator");
				tellPlayer(player, lang.parse("specwelcome"));
				return true;
			} else if (fightCmd[0].equalsIgnoreCase("teams")) {
				String team[] = PAStatsManager.getTeamStats().split(";");
				sender.sendMessage(lang.parse("teamstat", ChatColor.BLUE + lang.parse("blue"), team[0], team[1]));
				sender.sendMessage(lang.parse("teamstat", ChatColor.RED + lang.parse("red"), team[2], team[3]));
				
			} else if (fightCmd[0].equalsIgnoreCase("users")) {
				// wins are suffixed with "_"
				Map<String, Integer> players = PAStatsManager.getPlayerStats();
				
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
				tellPlayer((Player) sender, lang.parse("top5wins"));
				
				for (int w=0; w<wins.length && w < 5 ; w++) {
					tellPlayer((Player) sender, wins[w][0] + ": " + wins[w][1] + " " + lang.parse("wins"));
				}
				

				tellPlayer((Player) sender, "------------");
				tellPlayer((Player) sender, lang.parse("top5lose"));
				
				for (int l=0; l<losses.length && l < 5 ; l++) {
					tellPlayer((Player) sender, losses[l][0] + ": " + losses[l][1] + " " + lang.parse("losses"));
				}
			} else if (fightCmd[0].equalsIgnoreCase("leave")) {
				if (fightUsersTeam.containsKey(player.getName())) {
					if (fightUsersTeam.get(player.getName()) == "red") {
						redTeam -= 1;
						tellEveryoneExcept(player, lang.parse("playerleave", ChatColor.RED + player.getName() + ChatColor.WHITE));
					}
					if (fightUsersTeam.get(player.getName()) == "blue") {
						blueTeam -= 1;
						tellEveryoneExcept(player, lang.parse("playerleave", ChatColor.BLUE + player.getName() + ChatColor.WHITE));
					}
					tellPlayer(player, lang.parse("youleave"));					
					
					if (PVPArena.fightInProgress && PVPArena.checkEnd())
						return true;
					PVPArena.removePlayer(player, sTPexit);
					
					
				} else {
					goToWaypoint(player, "exit");
					tellPlayer(player, lang.parse("youleave"));
				}
				return true;
			} else if (hasAdminPerms(player)) {
				if (fightCmd[0].equalsIgnoreCase("redlounge")) {
					setCoords(player, "redlounge");
					tellPlayer(player, lang.parse("setredlounge"));
					
				} else if (fightCmd[0].equalsIgnoreCase("redspawn")) {
					setCoords(player, "redspawn");
					tellPlayer(player, lang.parse("setredspawn"));
				} else if (fightCmd[0].equalsIgnoreCase("bluelounge")) {
					setCoords(player, "bluelounge");
					tellPlayer(player, lang.parse("setbluelounge"));
				} else if (fightCmd[0].equalsIgnoreCase("bluespawn")) {
					setCoords(player, "bluespawn");
					tellPlayer(player, lang.parse("setbluespawn"));
				} else if (fightCmd[0].equalsIgnoreCase("spectator")) {
					setCoords(player, "spectator");
					tellPlayer(player, lang.parse("setspectator"));
				} else if (fightCmd[0].equalsIgnoreCase("exit")) {
					setCoords(player, "exit");
					tellPlayer(player, lang.parse("setexit"));
				} else if (fightCmd[0].equalsIgnoreCase("forcestop")) {
					if (fightInProgress) {
						tellPlayer(player, lang.parse("forcestop"));
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
			if (!fightCmd[0].equalsIgnoreCase("bet")) {
				tellPlayer(player, lang.parse("invalidcmd","503"));
				return false;
			}
			if (!fightUsersTeam.containsKey(player.getName())) {
				tellPlayer(player, lang.parse("betnotyours"));
				return true;
			}
			
			if (iConomy == null)
				return true;
			
			if (!fightCmd[1].equalsIgnoreCase("red") && !fightCmd[1].equalsIgnoreCase("blue") && !fightUsersTeam.containsKey(fightCmd[1])) {
				tellPlayer(player, lang.parse("betoptions"));
				return true;
			}
			
			double amount = 0;
			
			try {
				amount = Double.parseDouble(fightCmd[2]);
			} catch (Exception e) {
				tellPlayer(player, lang.parse("invalidamount",fightCmd[2]));
				return true;
			}
			
			if (!com.iConomy.iConomy.getAccount(player.getName()).getHoldings().hasEnough(amount)) {
				tellPlayer(player, lang.parse("notenough",com.iConomy.iConomy.format(amount)));
				return true;
			}
			com.iConomy.iConomy.getAccount(player.getName()).getHoldings().subtract(amount);
			tellPlayer(player, lang.parse("betplaced", fightCmd[1]));
			bets.put(player.getName() + ":" + fightCmd[1], amount);
			return true;
		}
		
		if (!hasAdminPerms(player)) {
			tellPlayer(player, lang.parse("invalidcmd","504"));
			return false;
		}
		
		if ((args.length != 2) || (!fightCmd[0].equalsIgnoreCase("region"))) {
			tellPlayer(player, lang.parse("invalidcmd","505"));
			return false;
		}

		Configuration config = new Configuration(new File("plugins/pvparena", "config.yml"));
		config.load();
		
		if (fightCmd[1].equalsIgnoreCase("set")) {
			if (config.getKeys("protection.region") == null) {
				regionmodify = true;
				tellPlayer(player, lang.parse("regionset"));
			} else {
				tellPlayer(player, lang.parse("regionalreadyset"));
			}
		} else if ((fightCmd[1].equalsIgnoreCase("modify"))
				|| (fightCmd[1].equalsIgnoreCase("edit"))) {
			if (config.getKeys("protection.region") != null) {
				regionmodify = true;
				tellPlayer(player, lang.parse("regionmodify"));
			} else {
				tellPlayer(player, lang.parse("noregionset"));
			}
		} else if (fightCmd[1].equalsIgnoreCase("save")) {
			if ((pos1 == null) || (pos2 == null)) {
				tellPlayer(player, lang.parse("set2points"));
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
				tellPlayer(player, lang.parse("regionsaved"));
			}
		} else if (fightCmd[1].equalsIgnoreCase("remove")) {
			if (config.getKeys("protection.region") != null) {
				config.removeProperty("protection.region");
				config.save();
				regionmodify = false;
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
		bets.clear();
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
		if (woolhead) {
			short col = 14;
			if (fightUsersTeam.get(player.getName()).equals("blue")) 
				col = 11;
			
			player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, col));
		}
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
			z.sendMessage(lang.parse("msgprefix") + ChatColor.WHITE + msg);
		}
	}

	public static void tellPublic(String msg) {
		Bukkit.getServer().broadcastMessage(lang.parse("msgprefix") + ChatColor.WHITE + msg);
	}

	public void tellEveryoneExcept(Player player, String msg) {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = getServer().getPlayer(o.toString());
			if (!(player.getName().equals(z.getName())))
				z.sendMessage(lang.parse("msgprefix") + ChatColor.WHITE + msg);
		}
	}

	public void tellTeam(String color, String msg) {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (((String) fightUsersTeam.get(o.toString())).equals(color)) {
				Player z = getServer().getPlayer(o.toString());
				z.sendMessage(lang.parse("msgprefix") + ChatColor.WHITE + msg);
			}
		}
	}

	public static void tellPlayer(Player player, String msg) {
		player.sendMessage(lang.parse("msgprefix") + ChatColor.WHITE + msg);
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
		if (iConomy != null) {
			for (String nKey : bets.keySet()) {
				String[] nSplit = nKey.split(":");
				
				if (nSplit[1].equalsIgnoreCase(player.getName())) {
					double amount = bets.get(nKey)*4;
					
					com.iConomy.iConomy.getAccount(nSplit[0]).getHoldings().add(amount);
					try {
						tellPlayer(Bukkit.getPlayer(nSplit[0]), lang.parse("youwon",com.iConomy.iConomy.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}				
			}			
		}	
		
		if ((rewardAmount > 0) && (iConomy != null)) {
			Holdings balance = com.iConomy.iConomy.getAccount(player.getName())
					.getHoldings();
			balance.add(rewardAmount);
			tellPlayer(player,lang.parse("awarded",com.iConomy.iConomy.format(rewardAmount)));
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
				try {
					player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
				} catch (Exception e) {
					tellPlayer(player,lang.parse("invfull"));
					return;
				}
			} else {
				int x = Integer.parseInt(itemDetail[0]);
				ItemStack stack = new ItemStack(x, 1);
				try {
					player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
				} catch (Exception e) {
					tellPlayer(player,lang.parse("invfull"));
					return;
				}
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
		String color = "";
		if (place.equals("redlounge")) {
			color = "&c";
		} else if (place.equals("bluelounge")) {
			color = "&9";
		}
		
		if (!color.equals(""))
			colorizePlayer(player, color);
		
		fightTelePass.put(player.getName(), "yes");
		player.teleport(getCoords(place));
		fightTelePass.remove(player.getName());
	}

	private static void colorizePlayer(Player player, String color) {
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
			tellEveryone(lang.parse("haswon",ChatColor.RED + "Red Team"));
		} else if ((PVPArena.redTeam == 0) && (PVPArena.blueTeam > 0)) {
			tellEveryone(lang.parse("haswon",ChatColor.BLUE + "Blue Team"));
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
				PAStatsManager.addWinStat(z, fightUsersTeam.get(z.getName()));
				loadPlayer(z, sTPwin);
				giveRewards(z); // if we are the winning team, give reward!
			} else {
				PAStatsManager.addLoseStat(z, fightUsersTeam.get(z.getName()));
				loadPlayer(z, sTPlose);
			}
		}

		if (iConomy != null) {
			for (String nKey : bets.keySet()) {
				String[] nSplit = nKey.split(":");
				
				if (!nSplit[1].equalsIgnoreCase("red") && !nSplit[1].equalsIgnoreCase("blue"))
					continue;
				
				if (nSplit[1].equalsIgnoreCase("red") != bluewon) {
					double amount = bets.get(nKey)*2;
					
					com.iConomy.iConomy.getAccount(nSplit[0]).getHoldings().add(amount);
					try {
						tellPlayer(Bukkit.getPlayer(nSplit[0]), lang.parse("youwon",com.iConomy.iConomy.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}				
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
		PVPArena.colorizePlayer(player, "");
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