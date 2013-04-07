package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>
 * Arena Goal class "Food"
 * </pre>
 * 
 * Players are equipped with raw food, the goal is to bring back cooked food
 * to their base. The first team having gathered enough wins!
 * 
 * @author slipcor
 */

public class GoalFood extends ArenaGoal implements Listener {
	public GoalFood() {
		super("Food");
		debug = new Debug(105);
	}

	@Override
	public String version() {
		return "v1.0.1.59";
	}

	private final static int PRIORITY = 12;
	
	private Map<ArenaTeam, Material> foodtypes = null;
	private Map<Block, ArenaTeam> chestMap = null;
	static Map<Material, Material> cookmap = new HashMap<Material, Material>();
	
	static {
		cookmap.put(Material.RAW_BEEF, Material.COOKED_BEEF);
		cookmap.put(Material.RAW_CHICKEN, Material.COOKED_CHICKEN);
		cookmap.put(Material.RAW_FISH, Material.COOKED_FISH);
		cookmap.put(Material.POTATO_ITEM, Material.BAKED_POTATO);
		cookmap.put(Material.PORK, Material.GRILLED_PORK);
	}

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	@Override
	public PACheck checkCommand(final PACheck res, final String string) {
		if (res.getPriority() > PRIORITY) {
			return res;
		}

		for (ArenaTeam team : arena.getTeams()) {
			final String sTeam = team.getName();
			if (string.contains(sTeam + "foodchest")) {
				res.setPriority(this, PRIORITY);
			} else if (string.contains(sTeam + "foodfurnace")) {
				res.setPriority(this, PRIORITY);
			}
		}

		return res;
	}

	@Override
	public PACheck checkEnd(final PACheck res) {
		if (res.getPriority() > PRIORITY) {
			return res;
		}

		final int count = TeamManager.countActiveTeams(arena);

		if (count == 1) {
			res.setPriority(this, PRIORITY); // yep. only one team left. go!
		} else if (count == 0) {
			res.setError(this, MSG.ERROR_NOTEAMFOUND.toString());
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(final Set<String> list) {
		String error = this.checkForMissingTeamSpawn(list);
		if (error != null) {
			return error;
		}
		return this.checkForMissingTeamCustom(list, "foodchest");
	}

	@Override
	public PACheck checkJoin(final CommandSender sender, final PACheck res, final String[] args) {
		if (res.getPriority() >= PRIORITY) {
			return res;
		}

		final int maxPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);
		final int maxTeamPlayers = arena.getArenaConfig().getInt(
				CFG.READY_MAXTEAMPLAYERS);

		if (maxPlayers > 0 && arena.getFighters().size() >= maxPlayers) {
			res.setError(this, Language.parse(MSG.ERROR_JOIN_ARENA_FULL));
			return res;
		}

		if (args == null || args.length < 1) {
			return res;
		}

		if (!arena.isFreeForAll()) {
			final ArenaTeam team = arena.getTeam(args[0]);

			if (team != null && maxTeamPlayers > 0
						&& team.getTeamMembers().size() >= maxTeamPlayers) {
				res.setError(this, Language.parse(MSG.ERROR_JOIN_TEAM_FULL));
				return res;
			}
		}

		res.setPriority(this, PRIORITY);
		return res;
	}

	@Override
	public PACheck checkPlayerDeath(final PACheck res, final Player player) {
		if (res.getPriority() <= PRIORITY && player.getKiller() != null) {
			res.setPriority(this, PRIORITY);
		}
		return res;
	}

	@Override
	public PACheck checkSetBlock(final PACheck res, final Player player, final Block block) {

		if (res.getPriority() > PRIORITY
				|| !PAA_Region.activeSelections.containsKey(player.getName())) {
			return res;
		}
		res.setPriority(this, PRIORITY); // success :)

		return res;
	}
	
	String flagName = null;

	@Override
	public void commitCommand(final CommandSender sender, final String[] args) {
		if (args[0].contains("foodchest")) {
			for (ArenaTeam team : arena.getTeams()) {
				final String sTeam = team.getName();
				if (args[0].contains(sTeam + "foodchest")) {
					flagName = args[0];
					PAA_Region.activeSelections.put(sender.getName(), arena);

					arena.msg(sender,
							Language.parse(MSG.GOAL_FOOD_TOSET, flagName));
				}
			}
		} else if (args[0].contains("foodfurnace")) {
			for (ArenaTeam team : arena.getTeams()) {
				final String sTeam = team.getName();
				if (args[0].contains(sTeam + "foodfurnace")) {
					flagName = args[0];
					PAA_Region.activeSelections.put(sender.getName(), arena);

					arena.msg(sender,
							Language.parse(MSG.GOAL_FOODFURNACE_TOSET, flagName));
				}
			}
		}
	}

	@Override
	public void commitEnd(final boolean force) {
		debug.i("[FOOD]");

		ArenaTeam aTeam = null;

		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.FIGHT)) {
					aTeam = team;
					break;
				}
			}
		}

		if (aTeam != null && !force) {

			ArenaModuleManager.announce(
					arena,
					Language.parse(MSG.TEAM_HAS_WON, aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW), "WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON, aTeam.getColor()
					+ "Team " + aTeam.getName() + ChatColor.YELLOW));
		}

		if (ArenaModuleManager.commitEnd(arena, aTeam)) {
			return;
		}
		new EndRunnable(arena, arena.getArenaConfig().getInt(
				CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitPlayerDeath(final Player respawnPlayer, final boolean doesRespawn,
			final String error, final PlayerDeathEvent event) {
		if (respawnPlayer.getKiller() == null) {
			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(
							CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(respawnPlayer);
				event.getDrops().clear();
			}

			PACheck.handleRespawn(arena,
					ArenaPlayer.parsePlayer(respawnPlayer.getName()),
					event.getDrops());

			return;
		}

		final ArenaTeam respawnTeam = ArenaPlayer
				.parsePlayer(respawnPlayer.getName()).getArenaTeam();

			if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
				arena.broadcast(Language.parse(
						MSG.FIGHT_KILLED_BY,
						respawnTeam.colorizePlayer(respawnPlayer)
								+ ChatColor.YELLOW, arena.parseDeathCause(
								respawnPlayer, event.getEntity()
										.getLastDamageCause().getCause(), event
										.getEntity().getKiller())));
			}

			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(
							CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(respawnPlayer);
				event.getDrops().clear();
			}

			PACheck.handleRespawn(arena,
					ArenaPlayer.parsePlayer(respawnPlayer.getName()),
					event.getDrops());

	}

	@Override
	public boolean commitSetFlag(final Player player, final Block block) {
		if (flagName == null || block == null
				|| (block.getType() != Material.CHEST && block.getType() != Material.FURNACE) ) {
			return false;
		}

		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			return false;
		}

		debug.i("trying to set a foodchest/furnace", player);

		// command : /pa redflag1
		// location: red1flag:

		SpawnManager.setBlock(arena, new PABlockLocation(block.getLocation()),
				flagName);

		
		if (flagName.contains("furnace")) {
			if (block.getType() != Material.FURNACE) {
				return false;
			}
			arena.msg(player, Language.parse(MSG.GOAL_FOODFURNACE_SET, flagName));
			
		} else {
			if (block.getType() != Material.CHEST) {
				return false;
			}
			arena.msg(player, Language.parse(MSG.GOAL_FOOD_SET, flagName));
			
		}

		PAA_Region.activeSelections.remove(player.getName());
		flagName = "";

		return true;
	}
	
	@Override
	public void configParse(YamlConfiguration config) {
		Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
	}

	@Override
	public void displayInfo(final CommandSender sender) {
		sender.sendMessage("items needed: "
				+ arena.getArenaConfig().getInt(CFG.GOAL_FOOD_FMAXITEMS));
		sender.sendMessage("items per player: "
				+ arena.getArenaConfig().getInt(CFG.GOAL_FOOD_FPLAYERITEMS));
		sender.sendMessage("items per team: "
				+ arena.getArenaConfig().getInt(CFG.GOAL_FOOD_FTEAMITEMS));
	}
	
	private Map<ArenaTeam, Material> getFoodMap() {
		if (foodtypes == null) {
			foodtypes = new HashMap<ArenaTeam, Material>();
		}
		return foodtypes;
	}

	@Override
	public PACheck getLives(final PACheck res, final ArenaPlayer aPlayer) {
		if (res.getPriority() <= PRIORITY+1000) {
			res.setError(
					this,
					String.valueOf(arena.getArenaConfig()
									.getInt(CFG.GOAL_FOOD_FMAXITEMS) - (getLifeMap()
									.containsKey(aPlayer.getArenaTeam().getName()) ? getLifeMap()
									.get(aPlayer.getArenaTeam().getName()) : 0)));
		}
		return res;
	}

	@Override
	public String guessSpawn(final String place) {
		if (!place.contains("spawn")) {
			debug.i("place not found!");
			return null;
		}
		// no exact match: assume we have multiple spawnpoints
		final Map<Integer, String> locs = new HashMap<Integer, String>();
		int pos = 0;

		debug.i("searching for team spawns");

		final Map<String, Object> coords = (HashMap<String, Object>) arena
				.getArenaConfig().getYamlConfiguration()
				.getConfigurationSection("spawns").getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(pos++, name);
				debug.i("found match: " + name);
			}
		}

		if (locs.size() < 1) {
			return null;
		}
		final Random random = new Random();

		return locs.get(random.nextInt(locs.size()));
	}

	@Override
	public boolean hasSpawn(final String string) {
		for (String teamName : arena.getTeamNames()) {
			if (string.toLowerCase().startsWith(
					teamName.toLowerCase() + "spawn")) {
				return true;
			}
			if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
				for (ArenaClass aClass : arena.getClasses()) {
					if (string.toLowerCase().startsWith(teamName.toLowerCase() + 
							aClass.getName() + "spawn")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void initate(final Player player) {
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		if (getLifeMap().get(aPlayer.getArenaTeam().getName()) == null) {
			getLifeMap().put(aPlayer.getArenaTeam().getName(), arena.getArenaConfig()
					.getInt(CFG.GOAL_FOOD_FMAXITEMS));
		}
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled = true)
	public void onFurnaceClick(PlayerInteractEvent event) {
		if (!event.hasBlock() || event.getClickedBlock().getType() != Material.FURNACE) {
			return;
		}
		
		ArenaPlayer player = ArenaPlayer.parsePlayer(event.getPlayer().getName());
		
		if (player.getArena() == null || !player.getArena().isFightInProgress()) {
			return;
		}
		
		Map<String, PALocation> locs = SpawnManager.getSpawnMap(arena, "foodfurnace");
		
		String teamName = player.getArenaTeam().getName();
		
		if (locs.size() < 1) {
			return;
		}
		
		Set<PALocation> validSpawns = new HashSet<PALocation>();
		
		for (String spawnName : locs.keySet()) {
			if (spawnName.startsWith(teamName + "foodfurnace")) {
				validSpawns.add(locs.get(spawnName));
			}
		}
		
		if (validSpawns.size() < 1) {
			return;
		}
		
		if (!validSpawns.contains(new PALocation (event.getClickedBlock().getLocation()))) {
			arena.msg(player.get(), Language.parse(MSG.GOAL_FOOD_NOTYOURFOOD));
			event.setCancelled(true);
			return;
		}
		
	}
	
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		
		if (arena == null || !arena.isFightInProgress()) {
			return;
		}
		
		InventoryType type = event.getInventory().getType();
	
		if (type != InventoryType.CHEST) {
			return;
		}
		
		if (chestMap == null || !chestMap.containsKey(((Chest) event.getInventory()
				.getHolder()).getBlock())) {
			return;
		}
		
		if (!event.isShiftClick()) {
			event.setCancelled(true);
			return;
		}

		ItemStack stack = event.getCurrentItem();

		ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(event.getWhoClicked().getName());
		
		ArenaTeam team = aPlayer.getArenaTeam();
		
		if (team == null || stack == null || stack.getType() != cookmap.get(getFoodMap().get(team))) {
			return;
		}
		
		SlotType sType = event.getSlotType();
		
		if (sType == SlotType.CONTAINER) {
			// OUT of container
			this.reduceLives(arena, team, -stack.getAmount());
		} else {
			// INTO container
			this.reduceLives(arena, team, stack.getAmount());
		}
	}

	@Override
	public void parseStart() {

		final int pAmount = arena.getArenaConfig().getInt(CFG.GOAL_FOOD_FPLAYERITEMS);
		final int tAmount = arena.getArenaConfig().getInt(CFG.GOAL_FOOD_FTEAMITEMS);
		
		chestMap = new HashMap<Block, ArenaTeam>();
		
		Map<String, PALocation> spawns = SpawnManager.getSpawnMap(arena, "foodchest");
		
		for (ArenaTeam team : arena.getTeams()) {
			int pos = new Random().nextInt(cookmap.size());
			for (Material mat : cookmap.keySet()) {
				if (pos <= 0) {
					getFoodMap().put(team, mat);
					break;
				}
				pos--;
			}
			int totalAmount = pAmount;
			totalAmount += tAmount/team.getTeamMembers().size();
			
			if (totalAmount < 1) {
				totalAmount = 1;
			}
			for (ArenaPlayer player : team.getTeamMembers()) {
				
				player.get().getInventory().addItem(new ItemStack(getFoodMap().get(team), totalAmount));
				player.get().updateInventory();
			}
			chestMap.put(spawns.get(team.getName()+ "foodchest").toLocation().getBlock(), team);
			this.getLifeMap().put(team.getName(),
					arena.getArenaConfig().getInt(CFG.GOAL_FOOD_FMAXITEMS));
		}
	}

	private void reduceLives(final Arena arena, final ArenaTeam team, int amount) {
		final int iLives = this.getLifeMap().get(team.getName());
		
		if (iLives <= amount && amount > 0) {
			for (ArenaTeam otherTeam : arena.getTeams()) {
				if (otherTeam.equals(team)) {
					continue;
				}
				getLifeMap().remove(otherTeam.getName());
				for (ArenaPlayer ap : otherTeam.getTeamMembers()) {
					if (ap.getStatus().equals(Status.FIGHT)) {
						ap.setStatus(Status.LOST);
						arena.removePlayer(ap.get(), CFG.TP_LOSE.toString(),
								true, false);
					}
				}
			}
			PACheck.handleEnd(arena, false);
			return;
		}

		getLifeMap().put(team.getName(), iLives - amount);
	}
	
	@Override
	public void refillInventory(Player player) {
		ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		ArenaTeam team = aPlayer.getArenaTeam();
		if (team == null) {
			return;
		}

		player.getInventory().addItem(new ItemStack(getFoodMap().get(team), arena.getArenaConfig().getInt(CFG.GOAL_FOOD_FPLAYERITEMS)));
		player.updateInventory();
	}


	@Override
	public void reset(final boolean force) {
		getLifeMap().clear();
	}

	@Override
	public void setDefaults(final YamlConfiguration config) {
		if (arena.isFreeForAll()) {
			return;
		}

		if (config.get("teams.free") != null) {
			config.set("teams", null);
		}
		if (config.get("teams") == null) {
			debug.i("no teams defined, adding custom red and blue!");
			config.addDefault("teams.red", ChatColor.RED.name());
			config.addDefault("teams.blue", ChatColor.BLUE.name());
		}
	}

	@Override
	public Map<String, Double> timedEnd(final Map<String, Double> scores) {
		double score;

		for (ArenaTeam team : arena.getTeams()) {
			score = arena.getArenaConfig().getInt(CFG.GOAL_FOOD_FMAXITEMS)
					- (getLifeMap().containsKey(team.getName()) ? getLifeMap().get(team
							.getName()) : 0);
			if (scores.containsKey(team)) {
				scores.put(team.getName(), scores.get(team.getName()) + score);
			} else {
				scores.put(team.getName(), score);
			}
		}

		return scores;
	}

	@Override
	public void unload(final Player player) {
		if (allowsJoinInBattle()) {
			arena.hasNotPlayed(ArenaPlayer.parsePlayer(player.getName()));
		}
	}
}
