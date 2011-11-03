package net.slipcor.pvparena.arenas;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.slipcor.pvparena.PVPArenaPlugin;
import net.slipcor.pvparena.managers.StatsManager;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;
import org.getspout.spoutapi.SpoutManager;

/*
 * Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.1 - New Arena! FreeFight
 * 
 * history:
 *
 *     v0.3.1 - New Arena! FreeFight
 * 
 */

public abstract class Arena {

	public static String regionmodify = ""; // only one Arena can be in modify mode

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
	
	public String name = "default";
	
	public final Map<String, String> fightUsersTeam = new HashMap<String, String>();
	public final Map<String, String> fightUsersClass = new HashMap<String, String>();
	public final Map<String, String> fightClasses = new HashMap<String, String>();
	public final Map<String, Sign> fightSigns = new HashMap<String, Sign>();
	public final Map<String, String> fightUsersRespawn = new HashMap<String, String>();
	public final Map<String, String> fightTelePass = new HashMap<String, String>();
	public final Map<String, Byte> fightUsersLives = new HashMap<String, Byte>();

	public final HashMap<Player, ItemStack[]> savedinventories = new HashMap<Player, ItemStack[]>();
	public final HashMap<Player, ItemStack[]> savedarmories = new HashMap<Player, ItemStack[]>();
	public final HashMap<Player, Object> savedmisc = new HashMap<Player, Object>();
	public final HashMap<String, Double> bets = new HashMap<String, Double>();

	public int redTeam = 0;
	public int blueTeam = 0;
	public Location pos1;
	public Location pos2;

	public boolean disableblockplacement;
	public boolean disableblockdamage;
	public boolean disableallfirespread;
	public boolean disablelavafirespread;
	public boolean blocktnt;
	public boolean blocklighter;
	public boolean forceeven;
	boolean woolhead;
	public boolean protection;
	public boolean teamkilling;
	public boolean manuallyselectteams;
	public boolean redTeamIronClicked = false;
	public boolean blueTeamIronClicked = false;
	public boolean fightInProgress = false;
	public boolean enabled = true;
	
	public int wand;
	public int entryFee;
	int rewardAmount;
	public int maxlives;
	
	String rewardItems;
	String sTPwin;
	String sTPlose;
	public String sTPexit;
	public String sTPdeath;
	protected File configFile;
	
	public Arena() {
		
	}
	
	public Arena(String name) {
		this.name = name;

		new File("plugins/pvparena").mkdir();
		configFile = new File("plugins/pvparena/config_" + name + ".yml");
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				PVPArenaPlugin.lang.log_error("filecreateerror","config_" + name);
			}

		parseConfig("arena");
	}

	protected void parseConfig(String s) {
		Configuration config = new Configuration(configFile);
		config.load();

		if (config.getKeys("classes") == null) {
			config.setProperty("classes.Ranger.items","261,262:64,298,299,300,301");
			config.setProperty("classes.Swordsman.items", "276,306,307,308,309");
			config.setProperty("classes.Tank.items", "272,310,311,312,313");
			config.setProperty("classes.Pyro.items", "259,46:2,298,299,300,301");
		}
		if (config.getKeys("general") == null) {
			config.setProperty("general.readyblock","IRON_BLOCK");
			config.setProperty("general.lives",Integer.valueOf(3));
			config.setProperty("general.language","en");
			config.setProperty("general.tp.win","old"); // old || exit || spectator
			config.setProperty("general.tp.lose","old"); // old || exit || spectator
			config.setProperty("general.tp.exit","exit"); // old || exit || spectator
			config.setProperty("general.tp.death","spectator"); // old || exit || spectator
			config.setProperty("general.classperms",Boolean.valueOf(false)); // require permissions for a class
			if (!s.equals("free")) {
				config.setProperty("general.woolhead",Boolean.valueOf(false)); // enforce a wool head in case we dont have Spout installed
				config.setProperty("general.forceeven",Boolean.valueOf(false)); // require even teams
			}
		}
		if (config.getKeys("rewards") == null) {
			config.setProperty("rewards.entry-fee", Integer.valueOf(0));
			config.setProperty("rewards.amount", Integer.valueOf(0));
			config.setProperty("rewards.items", "none");
		}
		if (config.getKeys("protection") == null) {
			config.setProperty("protection.enabled", Boolean.valueOf(false));
			config.setProperty("protection.wand", Integer.valueOf(280));
			config.setProperty("protection.player.disable-block-placement",Boolean.valueOf(true));
			config.setProperty("protection.player.disable-block-damage",Boolean.valueOf(true));
			config.setProperty("protection.fire.disable-all-fire-spread",Boolean.valueOf(true));
			config.setProperty("protection.fire.disable-lava-fire-spread",Boolean.valueOf(true));
			config.setProperty("protection.ignition.block-tnt",Boolean.valueOf(true));
			config.setProperty("protection.ignition.block-lighter",Boolean.valueOf(true));
		}
		if (!s.equals("free") && config.getKeys("teams") == null) {
			config.setProperty("teams.team-killing-enabled",Boolean.valueOf(false));
			config.setProperty("teams.manually-select-teams",Boolean.valueOf(false));
		}
		config.save();
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
	}

	public void prepare(Player player) {
		saveInventory(player);
		clearInventory(player);
		saveMisc(player); // save player health, fire tick, hunger etc
		player.setHealth(20);
		player.setFireTicks(0);
		player.setFoodLevel(20);
		player.setSaturation(20);
		player.setExhaustion(0);
		player.setGameMode(GameMode.getByValue(0));
	}
	

	public void reset() {
		cleanSigns();
		clearArena();
		fightInProgress = false;
		redTeamIronClicked = false;
		blueTeamIronClicked = false;
		fightUsersTeam.clear();
		fightUsersLives.clear();
		bets.clear();
		redTeam = 0;
		blueTeam = 0;
		fightSigns.clear();
	}


	public void setCoords(Player player, String place) {
		Location location = player.getLocation();
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

	@SuppressWarnings("unchecked")
	public Location getCoords(String place) {
		Configuration config = new Configuration(configFile);
		config.load();
		
		if (place.equals("spawn")) {
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			HashMap<String, Object> coords = (HashMap<String, Object>) config.getProperty("coords");
			for (String name : coords.keySet())
				if (name.startsWith("spawn"))
					locs.put(i++, name);
					
			Random r = new Random();
			
			place = locs.get(r.nextInt(locs.size()));
		}
		System.out.print(place);
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

	public void giveItems(Player player) {
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
	
	public void equipArmorPiece(ItemStack stack, PlayerInventory inv) {
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
	

	public void cleanSigns() {
		Set<String> set = fightSigns.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Sign sign = (Sign) fightSigns.get(o.toString());
			sign.setLine(2, "");
			sign.setLine(3, "");
			sign.update(true);
		}
	}

	public void cleanSigns(String player) {
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

	public boolean teamReady(String color) {
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
			if (color == "free")
				return members > 1; // at least 2 ppl need to be in there for a free arena to start
		} else {
			return false;
		}
		return false;
	}

	public void tellEveryone(String msg) {		
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = Bukkit.getServer().getPlayer(o.toString());
			z.sendMessage(PVPArenaPlugin.lang.parse("msgprefix") + ChatColor.WHITE + msg);
		}
	}
	

	public void tellEveryoneExcept(Player player, String msg) {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = Bukkit.getServer().getPlayer(o.toString());
			if (!(player.getName().equals(z.getName())))
				z.sendMessage(PVPArenaPlugin.lang.parse("msgprefix") + ChatColor.WHITE + msg);
		}
	}
	
	public void teleportAllToSpawn() {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (((String) fightUsersTeam.get(o.toString())).equals("red")) {
				Player z = Bukkit.getServer().getPlayer(o.toString());
				goToWaypoint(z, "redspawn");
			} else if (((String) fightUsersTeam.get(o.toString())).equals("blue")) {
				Player z = Bukkit.getServer().getPlayer(o.toString());
				goToWaypoint(z, "bluespawn");
			} else {
				Player z = Bukkit.getServer().getPlayer(o.toString());
				goToWaypoint(z, "spawn");
			}
		}
	}
	
	public void saveInventory(Player player) {
		savedinventories.put(player, player.getInventory().getContents());
		savedarmories.put(player, player.getInventory().getArmorContents());
	}

	public void setInventory(Player player) {
		player.getInventory().setContents((ItemStack[]) savedinventories.get(player));
		player.getInventory().setArmorContents((ItemStack[]) savedarmories.get(player));
	}

	public void saveMisc(Player player) {
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

	public void giveRewards(Player player) {
		if (PVPArenaPlugin.method != null) {
			for (String nKey : bets.keySet()) {
				String[] nSplit = nKey.split(":");
				
				if (nSplit[1].equalsIgnoreCase(player.getName())) {
					double amount = bets.get(nKey)*4;

					MethodAccount ma = PVPArenaPlugin.method.getAccount(nSplit[0]);
					ma.add(amount);
					try {
						tellPlayer(Bukkit.getPlayer(nSplit[0]), PVPArenaPlugin.lang.parse("youwon",PVPArenaPlugin.method.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}				
			}			
		}	
		if ((PVPArenaPlugin.method != null) && (rewardAmount > 0)) {
			MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
			ma.add(rewardAmount);
			tellPlayer(player,PVPArenaPlugin.lang.parse("awarded",PVPArenaPlugin.method.format(rewardAmount)));
		}

		if (rewardItems.equals("none"))
			return;
		String[] items = rewardItems.split(",");
		for (int i = 0; i < items.length; ++i) {
			String item = items[i];
			String[] itemDetail = item.split(":");
			if (itemDetail.length == 2) {
				int x = parseMat(itemDetail[0]);
				int y = Integer.parseInt(itemDetail[1]);
				ItemStack stack = new ItemStack(x, y);
				try {
					player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
				} catch (Exception e) {
					tellPlayer(player,PVPArenaPlugin.lang.parse("invfull"));
					return;
				}
			} else {
				int x = parseMat(itemDetail[0]);
				ItemStack stack = new ItemStack(x, 1);
				try {
					player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
				} catch (Exception e) {
					tellPlayer(player,PVPArenaPlugin.lang.parse("invfull"));
					return;
				}
			}
		}
	}

	private int parseMat(String s) {
		try {
			int i = Integer.parseInt(s);
			return i;
		} catch (Exception e) {
			try {

				int j = Material.getMaterial(s).getId();
				return j;
			} catch (Exception e2) {
				return 0;
			}
		}
	}

	public void clearArena() {
		Configuration config = new Configuration(configFile);
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

	public void goToWaypoint(Player player, String place) {
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
	

	public Vector getMinimumPoint() {
		return new Vector(Math.min(pos1.getX(), pos2.getX()), Math.min(
				pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
	}

	public Vector getMaximumPoint() {
		return new Vector(Math.max(pos1.getX(), pos2.getX()), Math.max(
				pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
	}
	

	public boolean contains(Vector pt) {
		Configuration config = new Configuration(configFile);
		config.load();
		
		if (config.getString("protection.region.min") == null
				|| config.getString("protection.region.max") == null)
			return false; // no arena, no container
		
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

	public boolean checkEnd() {
		boolean bluewon = false;
		boolean bluemember = false;
		if ((redTeam > 0) && (blueTeam == 0)) {
			tellEveryone(PVPArenaPlugin.lang.parse("haswon",ChatColor.RED + "Red Team"));
		} else if ((redTeam == 0) && (blueTeam > 0)) {
			tellEveryone(PVPArenaPlugin.lang.parse("haswon",ChatColor.BLUE + "Blue Team"));
			bluewon = true;
		} else {
			return false;
		}
		
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			bluemember =(((String) fightUsersTeam.get(o.toString())).equals("blue"));
			
			Player z = Bukkit.getServer().getPlayer(o.toString());
			if (bluewon == bluemember) {
				StatsManager.addWinStat(z, fightUsersTeam.get(z.getName()));
				loadPlayer(z, sTPwin);
				giveRewards(z); // if we are the winning team, give reward!
			} else {
				StatsManager.addLoseStat(z, fightUsersTeam.get(z.getName()));
				loadPlayer(z, sTPlose);
			}
			fightUsersClass.remove(z.getName());
		}

		if (PVPArenaPlugin.method != null) {
			for (String nKey : bets.keySet()) {
				String[] nSplit = nKey.split(":");
				
				if (!nSplit[1].equalsIgnoreCase("red") && !nSplit[1].equalsIgnoreCase("blue"))
					continue;
				
				if (nSplit[1].equalsIgnoreCase("red") != bluewon) {
					double amount = bets.get(nKey)*2;

					MethodAccount ma = PVPArenaPlugin.method.getAccount(nSplit[0]);
					if (ma == null) {
						PVPArenaPlugin.log.severe("Account not found: "+nSplit[0]);
						return true;
					}
					ma.add(amount);
					try {
						tellPlayer(Bukkit.getPlayer(nSplit[0]), PVPArenaPlugin.lang.parse("youwon",PVPArenaPlugin.method.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}				
			}			
		}	
		reset();
		return true;
	}
	
	public void removePlayer(Player player, String tploc) {
		loadPlayer(player, tploc);		
		fightUsersTeam.remove(player.getName());
		fightUsersClass.remove(player.getName());
		cleanSigns(player.getName());
	}

	@SuppressWarnings("unchecked")
	public void loadPlayer(Player player, String string) {

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
				Location l = getCoords(string);
				player.teleport(l);
			}
			fightTelePass.remove(player.getName());
			savedmisc.remove(player);
		} else {
			System.out.println("[PVP Arena] player '" + player.getName() + "' had no savedmisc entries!");
		}
		colorizePlayer(player, "");
		String sClass = "exit";
		if (fightUsersRespawn.get(player.getName()) != null) {
			sClass = fightUsersRespawn.get(player.getName());
		} else if (fightUsersClass.get(player.getName()) != null) {
			sClass = fightUsersClass.get(player.getName());
		}
		if (!sClass.equalsIgnoreCase("custom")) {
			clearInventory(player);
			setInventory(player);
		}
	}

	public void forcestop() {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = Bukkit.getServer().getPlayer(o.toString());
			removePlayer(z, "spectator");
		}
		reset();
		fightUsersClass.clear();
	}

	public void chooseColor(Player player) {
		if (!(fightUsersTeam.containsKey(player.getName()))) {
			if (blueTeam > redTeam) {
				goToWaypoint(player, "redlounge");
				fightUsersTeam.put(player.getName(), "red");
				tellPlayer(player, PVPArenaPlugin.lang.parse("youjoined", ChatColor.RED + "<Red>"));
				tellEveryoneExcept(player, PVPArenaPlugin.lang.parse("playerjoined", player.getName(), ChatColor.RED + "<Red>"));
				redTeam += 1;
			} else {
				goToWaypoint(player, "bluelounge");
				fightUsersTeam.put(player.getName(), "blue");
				tellPlayer(player, PVPArenaPlugin.lang.parse("youjoined", ChatColor.BLUE + "<Blue>"));
				tellEveryoneExcept(player, PVPArenaPlugin.lang.parse("playerjoined", player.getName(), ChatColor.BLUE + "<Blue>"));
				blueTeam += 1;
			}

		} else {
			tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
		}
	}
	

	public boolean parseCommand(Player player, Command cmd, String[] args, String sName) {
		
		
		if (!enabled && !PVPArenaPlugin.hasAdminPerms(player)) {
			PVPArenaPlugin.lang.parse("arenadisabled");
			return true;
		}
		
		if (args.length < 1) {
			// just /pa or /pvparena
			if (!(isSetup().booleanValue())) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("arenanotsetup"));
				return true;
			}
			if (!PVPArenaPlugin.hasPerms(player)) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("permjoin"));
				return true;
			}
			if (manuallyselectteams) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("selectteam"));
				return true;
			}
			if (savedmisc.containsKey(player)) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
				return true;
			}
			if (fightInProgress) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("fightinprogress"));
				return true;
			}

			if (PVPArenaPlugin.method != null) {
				MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
				if (ma == null) {
					PVPArenaPlugin.log.severe("Account not found: "+player.getName());
					return true;
				}
				if(!ma.hasEnough(entryFee)){
					// no money, no entry!
					tellPlayer(player, PVPArenaPlugin.lang.parse("notenough", PVPArenaPlugin.method.format(entryFee)));
					return true;
	            }
			}
			
			prepare(player);
			fightUsersLives.put(player.getName(), (byte) maxlives);
			
			
			if (emptyInventory(player)) {
				if ((PVPArenaPlugin.method != null) && (entryFee > 0)) {
					MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
					ma.subtract(entryFee);
				}

				chooseColor(player);

			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
			}
			return true;
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("enable")) {
				if (!PVPArenaPlugin.hasAdminPerms(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("nopermto", PVPArenaPlugin.lang.parse("enable")));
					return true;
				}
				enabled = true;
				tellPlayer(player, PVPArenaPlugin.lang.parse("enabled"));
				return true;
			} else if (args[0].equalsIgnoreCase("disable")) {
				if (!PVPArenaPlugin.hasAdminPerms(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("nopermto", PVPArenaPlugin.lang.parse("disable")));
					return true;
				}
				enabled = false;
				tellPlayer(player, PVPArenaPlugin.lang.parse("disabled"));
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!PVPArenaPlugin.hasAdminPerms(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("nopermto", PVPArenaPlugin.lang.parse("reload")));
					return true;
				}
				PVPArenaPlugin.load_config();
				tellPlayer(player, PVPArenaPlugin.lang.parse("reloaded"));
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				if ((fightUsersTeam == null) || (fightUsersTeam.size() < 1)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("noplayer"));
					return true;
				}
				String plrs = "";
				for (String sPlayer : fightUsersTeam.keySet()) {
					if (!plrs.equals(""))
						plrs +=", ";
					plrs += colorize(fightUsersTeam.get(sPlayer)) + sPlayer + ChatColor.WHITE;
				}
				tellPlayer(player, PVPArenaPlugin.lang.parse("players") + ": " + plrs);
				return true;
			} else if (args[0].equalsIgnoreCase("red")) {
				

				// /pa red or /pvparena red
				if (!(isSetup().booleanValue())) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("arenanotsetup"));
					return true;
				}
				if (!PVPArenaPlugin.hasPerms(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("permjoin"));
					return true;
				}
				if (!(manuallyselectteams)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("notselectteam"));
					return true;
				}
				if (savedmisc.containsKey(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
					return true;
				}
				if (fightInProgress) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("fightinprogress"));
					return true;
				}
				
				if (PVPArenaPlugin.method != null) {
					MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
					if (ma == null) {
						PVPArenaPlugin.log.severe("Account not found: "+player.getName());
						return true;
					}
					if(!ma.hasEnough(entryFee)){
						// no money, no entry!
						tellPlayer(player, PVPArenaPlugin.lang.parse("notenough", PVPArenaPlugin.method.format(entryFee)));
						return true;
		            }
				}


				prepare(player);
				fightUsersLives.put(player.getName(), (byte) maxlives);
				
				if (emptyInventory(player)) {
					if ((PVPArenaPlugin.method != null) && (entryFee > 0)) {
						MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
						ma.subtract(entryFee);
					}

					goToWaypoint(player, "redlounge");
					fightUsersTeam.put(player.getName(), "red");
					tellPlayer(player, PVPArenaPlugin.lang.parse("youjoined", ChatColor.RED + "<Red>"));
					tellEveryoneExcept(player, PVPArenaPlugin.lang.parse("playerjoined", player.getName(), ChatColor.RED + "<Red>"));
					redTeam += 1;
				}

			} else if (args[0].equalsIgnoreCase("blue")) {
				
				// /pa blue or /pvparena blue

				if (!(isSetup().booleanValue())) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("arenanotsetup"));
					return true;
				}
				if (!PVPArenaPlugin.hasPerms(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("permjoin"));
					return true;
				}
				if (!(manuallyselectteams)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("notselectteam"));
					return true;
				}
				if (savedmisc.containsKey(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
					return true;
				}
				if (fightInProgress) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("fightinprogress"));
					return true;
				}
				
				if (PVPArenaPlugin.method != null) {
					MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
					if (ma == null) {
						PVPArenaPlugin.log.severe("Account not found: "+player.getName());
						return true;
					}
					if(!ma.hasEnough(entryFee)){
						// no money, no entry!
						tellPlayer(player, PVPArenaPlugin.lang.parse("notenough", PVPArenaPlugin.method.format(entryFee)));
						return true;
		            }
				}


				prepare(player);
				fightUsersLives.put(player.getName(), (byte) maxlives);

				if (emptyInventory(player)) {
					if ((PVPArenaPlugin.method != null) && (entryFee > 0)) {
						MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
						ma.subtract(entryFee);
					}

					goToWaypoint(player, "bluelounge");
					fightUsersTeam.put(player.getName(), "blue");
					blueTeam += 1;
					tellPlayer(player, PVPArenaPlugin.lang.parse("youjoined", ChatColor.BLUE + "<Blue>"));
					tellEveryoneExcept(player, PVPArenaPlugin.lang.parse("playerjoined", player.getName(), ChatColor.BLUE + "<Blue>"));
					
				}
				return true;
			
			} else if (args[0].equalsIgnoreCase("watch")) {

				if (!(isSetup().booleanValue())) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("arenanotsetup"));
					return true;
				}
				if (fightUsersTeam.containsKey(player.getName())) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
					return true;
				}
				goToWaypoint(player, "spectator");
				tellPlayer(player, PVPArenaPlugin.lang.parse("specwelcome"));
				return true;
			} else if (PVPArenaPlugin.hasAdminPerms(player)) {
				return parseAdminCommand(args, player);
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","502"));
				return false;
			}
			return true;
		} else if (args.length == 3) {
			// /pa bet [name] [amount]
			if (!args[0].equalsIgnoreCase("bet")) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","503"));
				return false;
			}
			if (!fightUsersTeam.containsKey(player.getName())) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("betnotyours"));
				return true;
			}
			
			if (PVPArenaPlugin.method == null)
				return true;
			
			if (!args[1].equalsIgnoreCase("red") && !args[1].equalsIgnoreCase("blue") && !fightUsersTeam.containsKey(args[1])) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("betoptions"));
				return true;
			}
			
			double amount = 0;
			
			try {
				amount = Double.parseDouble(args[2]);
			} catch (Exception e) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("invalidamount",args[2]));
				return true;
			}
			MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
			if (ma == null) {
				PVPArenaPlugin.log.severe("Account not found: "+player.getName());
				return true;
			}
			if(!ma.hasEnough(entryFee)){
				// no money, no entry!
				tellPlayer(player, PVPArenaPlugin.lang.parse("notenough",PVPArenaPlugin.method.format(amount)));
				return true;
            }
			ma.subtract(amount);
			tellPlayer(player, PVPArenaPlugin.lang.parse("betplaced", args[1]));
			bets.put(player.getName() + ":" + args[1], amount);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("teams")) {
			String team[] = StatsManager.getTeamStats(args[1]).split(";");
			player.sendMessage(PVPArenaPlugin.lang.parse("teamstat", ChatColor.BLUE + PVPArenaPlugin.lang.parse("blue"), team[0], team[1]));
			player.sendMessage(PVPArenaPlugin.lang.parse("teamstat", ChatColor.RED + PVPArenaPlugin.lang.parse("red"), team[2], team[3]));
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
			tellPlayer(player, PVPArenaPlugin.lang.parse("top5wins"));
			
			for (int w=0; w<wins.length && w < 5 ; w++) {
				tellPlayer(player, wins[w][0] + ": " + wins[w][1] + " " + PVPArenaPlugin.lang.parse("wins"));
			}
			
	
			tellPlayer(player, "------------");
			tellPlayer(player, PVPArenaPlugin.lang.parse("top5lose"));
			
			for (int l=0; l<losses.length && l < 5 ; l++) {
				tellPlayer(player, losses[l][0] + ": " + losses[l][1] + " " + PVPArenaPlugin.lang.parse("losses"));
			}
			return true;
		}
		
		if (!PVPArenaPlugin.hasAdminPerms(player)) {
			tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","504"));
			return false;
		}
		
		if ((args.length != 2) || (!args[0].equalsIgnoreCase("region"))) {
			tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","505"));
			return false;
		}

		Configuration config = new Configuration(new File("plugins/pvparena", "config_" + sName + ".yml"));
		config.load();
		
		if (args[1].equalsIgnoreCase("set")) {
			if (!Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionalreadybeingset", sName));
				return true;
			}
			if (config.getKeys("protection.region") == null) {
				Arena.regionmodify = name;
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionset"));
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionalreadyset"));
			}
		} else if ((args[1].equalsIgnoreCase("modify"))
				|| (args[1].equalsIgnoreCase("edit"))) {
			if (!Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionalreadybeingset", sName));
				return true;
			}
			if (config.getKeys("protection.region") != null) {
				Arena.regionmodify = name;
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionmodify"));
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("noregionset"));
			}
		} else if (args[1].equalsIgnoreCase("save")) {
			if (Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionnotbeingset", sName));
				return true;
			}
			if ((pos1 == null) || (pos2 == null)) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("set2points"));
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
				Arena.regionmodify = "";
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionsaved"));
			}
		} else if (args[1].equalsIgnoreCase("remove")) {
			if (config.getKeys("protection.region") != null) {
				config.removeProperty("protection.region");
				config.save();
				Arena.regionmodify = "";
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionremoved"));
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionnotremoved"));
			}

		} else {
			tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","506"));
			return false;
		}
		
		return true;
	}

	boolean parseAdminCommand(String[] args, Player player) {

		if (args[0].equalsIgnoreCase("redlounge")) {
			setCoords(player, "redlounge");
			tellPlayer(player, PVPArenaPlugin.lang.parse("setredlounge"));
			
		} else if (args[0].equalsIgnoreCase("redspawn")) {
			setCoords(player, "redspawn");
			tellPlayer(player, PVPArenaPlugin.lang.parse("setredspawn"));
		} else if (args[0].equalsIgnoreCase("bluelounge")) {
			setCoords(player, "bluelounge");
			tellPlayer(player, PVPArenaPlugin.lang.parse("setbluelounge"));
		} else if (args[0].equalsIgnoreCase("bluespawn")) {
			setCoords(player, "bluespawn");
			tellPlayer(player, PVPArenaPlugin.lang.parse("setbluespawn"));
		} else if (args[0].equalsIgnoreCase("spectator")) {
			setCoords(player, "spectator");
			tellPlayer(player, PVPArenaPlugin.lang.parse("setspectator"));
		} else if (args[0].equalsIgnoreCase("exit")) {
			setCoords(player, "exit");
			tellPlayer(player, PVPArenaPlugin.lang.parse("setexit"));
		} else if (args[0].equalsIgnoreCase("forcestop")) {
			if (fightInProgress) {
				forcestop();
				tellPlayer(player, PVPArenaPlugin.lang.parse("forcestop"));
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("nofight"));
			}
		} else {
			tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","501"));
			return false;
		}
		return true;
	}

	public static void tellPublic(String msg) {
		Bukkit.getServer().broadcastMessage(PVPArenaPlugin.lang.parse("msgprefix") + ChatColor.WHITE + msg);
	}

	public static void tellPlayer(Player player, String msg) {
		player.sendMessage(PVPArenaPlugin.lang.parse("msgprefix") + ChatColor.WHITE + msg);
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
			

			if (PVPArenaPlugin.spoutHandler != null) {
				HumanEntity human = player;
				SpoutManager.getAppearanceManager().setGlobalTitle(human,rn);
			}
		    return;
		}
		
		String n = color + player.getName();

		player.setDisplayName(n.replaceAll("(&([a-f0-9]))", "§$2"));
		
		if (PVPArenaPlugin.spoutHandler != null) {
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
	
	private String colorize(String s) {
		if (s.equals("red"))
			return ChatColor.RED + "";
		else if (s.equals("blue"))
			return ChatColor.BLUE + "";
		else
			return "";
	}
}
