package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
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
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;

/**
 * <pre>Arena Goal class "Flags"</pre>
 * 
 * Well, should be clear. Capture flags, bring them home, get points, win.
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class GoalFlags extends ArenaGoal implements Listener {

	public GoalFlags() {
		super("Flags");
		db = new Debug(100);
	}
	
	private HashMap<String, Integer> paTeamLives = new HashMap<String, Integer>();
	private HashMap<String, String> paTeamFlags = new HashMap<String, String>();
	private HashMap<String, ItemStack> paHeadGears = new HashMap<String, ItemStack>();
	private static HashSet<Material> headFlags = new HashSet<Material>();
	
	private String flagName = "";
	
	static {
		headFlags.add(Material.PUMPKIN);
		headFlags.add(Material.WOOL);
		headFlags.add(Material.JACK_O_LANTERN);
	}
	
	@Override
	public String version() {
		return "v0.10.0.16";
	}

	int priority = 6;
	int killpriority = 1;

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	public PACheck checkCommand(PACheck res, String string) {
		if (res.getPriority() > priority) {
			return res;
		}
		
		if (string.equalsIgnoreCase("flagtype")) {
			res.setPriority(this, priority);
		}
		
		for (ArenaTeam team : arena.getTeams()) {
			String sTeam = team.getName();
			if (string.contains(sTeam + "flag")) {
				res.setPriority(this, priority);
			}
		}
		
		return res;
	}
	
	@Override
	public PACheck checkEnd(PACheck res) {
		
		if (res.getPriority() > priority) {
			return res;
		}
		
		int count = TeamManager.countActiveTeams(arena);

		if (count == 1) {
			res.setPriority(this, priority); // yep. only one team left. go!
		} else if (count == 0) {
			res.setError(this, "No teams playing!");
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(Set<String> list) {
		for (ArenaTeam team : arena.getTeams()) {
			String sTeam = team.getName();
			if (!list.contains(sTeam + "flag")) {
				boolean found = false;
				for (String s : list) {
					if (s.startsWith(sTeam) && s.endsWith("flag")) {
						found = true;
						break;
					}
				}
				if (!found)
					return team.getName() + "flag not set";
			}
		}
		return null;
	}

	/**
	 * hook into an interacting player
	 * @param res 
	 * 
	 * @param player
	 *            the interacting player
	 * @param clickedBlock
	 *            the block being clicked
	 * @return 
	 */
	@Override
	public PACheck checkInteract(PACheck res, Player player, Block block) {
		if (block == null || res.getPriority() > priority) {
			return res;
		}
		db.i("checking interact");

		if (!block.getType().name().equals(arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE))) {
			db.i("block, but not flag");
			return res;
		}
		db.i("flag click!");

		Vector vLoc;
		String sTeam;
		Vector vFlag = null;
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

		if (paTeamFlags.containsValue(player.getName())) {
			db.i("player " + player.getName() + " has got a flag");
			vLoc = block.getLocation().toVector();
			sTeam = ap.getArenaTeam().getName();
			db.i("block: " + vLoc.toString());
			if (SpawnManager.getBlocks(arena, sTeam + "flag").size() > 0) {
				vFlag = SpawnManager.getBlockNearest(
						SpawnManager.getBlocks(arena, sTeam + "flag"),
						new PABlockLocation(player.getLocation())).toLocation().toVector();
			} else {
				db.i(sTeam + "flag = null");
			}

			db.i("player is in the team " + sTeam);
			if ((vFlag != null && vLoc.distance(vFlag) < 2)) {

				db.i("player is at his flag");

				if (paTeamFlags.containsKey(sTeam)) {
					db.i("the flag of the own team is taken!");

					if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_MUSTBESAFE)) {
						db.i("cancelling");

						arena.msg(player, Language.parse(MSG.GOAL_FLAGS_NOTSAFE));
						return res;
					}
				}

				String flagTeam = getHeldFlagTeam(arena, player.getName());

				db.i("the flag belongs to team " + flagTeam);

				try {

					arena.broadcast(Language.parse(MSG.GOAL_FLAGS_BROUGHTHOME, arena
							.getTeam(sTeam).colorizePlayer(player)
							+ ChatColor.YELLOW, arena.getTeam(flagTeam)
							.getColoredName() + ChatColor.YELLOW, String
							.valueOf(paTeamLives.get(flagTeam) - 1)));
					paTeamFlags.remove(flagTeam);
				} catch (Exception e) {
					Bukkit.getLogger().severe(
							"[PVP Arena] team unknown/no lives: " + flagTeam);
					e.printStackTrace();
				}

				takeFlag(arena.getTeam(flagTeam).getColor().name(), false,
						SpawnManager.getCoords(arena, flagTeam + "flag"));
				if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)) {
					if (paHeadGears.get(player.getName()) != null) {
						player.getInventory().setHelmet(
								paHeadGears.get(player.getName()).clone());
						paHeadGears.remove(player.getName());
					} else {
						player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
					}
				}

				reduceLivesCheckEndAndCommit(arena, flagTeam); // TODO move to "commit" ?
			}
		} else {
			ArenaTeam pTeam = ap.getArenaTeam();
			if (pTeam == null) {
				return res;
			}
			for (ArenaTeam team : arena.getTeams()) {
				String aTeam = team.getName();

				if (aTeam.equals(pTeam.getName()))
					continue;
				if (team.getTeamMembers().size() < 1)
					continue; // dont check for inactive teams
				if (paTeamFlags != null && paTeamFlags.containsKey(aTeam)) {
					continue; // already taken
				}
				db.i("checking for flag of team " + aTeam);
				vLoc = block.getLocation().toVector();
				db.i("block: " + vLoc.toString());
				if (SpawnManager.getBlocks(arena, aTeam + "flag").size() > 0) {
					vFlag = SpawnManager.getBlockNearest(
							SpawnManager.getBlocks(arena, aTeam + "flag"),
							new PABlockLocation(player.getLocation())).toLocation().toVector();
				}
				if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
					db.i("flag found!");
					db.i("vFlag: " + vFlag.toString());
					arena.broadcast(Language.parse(MSG.GOAL_FLAGS_GRABBED,
							pTeam.colorizePlayer(player) + ChatColor.YELLOW,
							team.getColoredName() + ChatColor.YELLOW));
					try {
						paHeadGears.put(player.getName(), player
								.getInventory().getHelmet().clone());
					} catch (Exception e) {

					}
					ItemStack is = block.getState().getData().toItemStack()
							.clone();
					if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)) {
						is.setDurability(getFlagOverrideTeamShort(arena, aTeam));
					}
					player.getInventory().setHelmet(is);

					takeFlag(team.getColor().name(), true, new PALocation(block.getLocation()));
					paTeamFlags.put(aTeam, player.getName()); // TODO move to "commit" ?
					return res; 
				}
			}
		}
		
		return res;
	}
	
	@Override
	public PACheck checkJoin(CommandSender sender, PACheck res, String[] args) {
		if (res.getPriority() >= priority) {
			return res;
		}

		int maxPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);
		int maxTeamPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXTEAMPLAYERS);
		
		if (maxPlayers > 0 && arena.getFighters().size() >= maxPlayers) {
			res.setError(this, Language.parse(MSG.ERROR_JOIN_ARENA_FULL));
			return res;
		}

		if (args == null || args.length < 1) {
			return res;
		}

		if (!arena.isFreeForAll()) {
			ArenaTeam team = arena.getTeam(args[0]);
			
			if (team != null) {
			
				if (maxTeamPlayers > 0 && team.getTeamMembers().size() >= maxTeamPlayers) {
					res.setError(this, Language.parse(MSG.ERROR_JOIN_TEAM_FULL));
					return res;
				}
			}
		}
		
		res.setPriority(this, priority);
		return res;
	}

	@Override
	public PACheck checkPlayerDeath(PACheck res, Player player) {
		if (res.getPriority() <= killpriority) {
			res.setPriority(this, killpriority);
		}
		return res;
	}
	
	@Override
	public PACheck checkSetFlag(PACheck res, Player player, Block block) {

		if (res.getPriority() > priority || !PAA_Region.activeSelections.containsKey(player.getName())) {
			return res;
		}
		res.setPriority(this, priority); // success :)
		
		return res;
	}

	private void commit(Arena arena, String sTeam, boolean win) {
		db.i("[CTF] committing end: " + sTeam);
		db.i("win: " + String.valueOf(win));

		String winteam = sTeam;

		for (ArenaTeam team : arena.getTeams()) {
			if (team.getName().equals(sTeam) == win) {
				continue;
			}
			for (ArenaPlayer ap : team.getTeamMembers()) {

				ap.addStatistic(arena.getName(), type.LOSSES, 1);
				arena.tpPlayerToCoordName(ap.get(), "spectator");
				ap.setTelePass(false);
			}
		}
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.getStatus().equals(Status.FIGHT)) {
					continue;
				}
				winteam = team.getName();
				break;
			}
		}

		if (arena.getTeam(winteam) != null) {
			
			
			ArenaModuleManager.announce(arena, Language.parse(MSG.TEAM_HAS_WON,
					arena.getTeam(winteam).getColor() + "Team "
							+ winteam + ChatColor.YELLOW), "WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON,
					arena.getTeam(winteam).getColor() + "Team "
							+ winteam + ChatColor.YELLOW));
		}

		paTeamLives.clear();
		new EndRunnable(arena, arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		if (args[0].equalsIgnoreCase("flagtype")) {
			if (args.length < 2) {
				arena.msg(sender, Language.parse(MSG.ERROR_INVALID_ARGUMENT_COUNT, String.valueOf(args.length), "2"));
				return;
			}
			
			try {
				int i = Integer.parseInt(args[1]);
				arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGTYPE, Material.getMaterial(i).name());
			} catch (Exception e) {
				Material mat = Material.getMaterial(args[1].toUpperCase());
				
				if (mat == null) {
					arena.msg(sender, Language.parse(MSG.ERROR_MAT_NOT_FOUND, args[1]));
					return;
				}
				
				arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGTYPE, mat.name());
			}
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.GOAL_FLAGS_TYPESET, CFG.GOAL_FLAGS_FLAGTYPE.toString()));
			
		} else if (args[0].contains("flag")) {
			for (ArenaTeam team : arena.getTeams()) {
				String sTeam = team.getName();
				if (args[0].contains(sTeam + "flag")) {
					flagName = args[0];
					PAA_Region.activeSelections.put(sender.getName(), arena);


					arena.msg(sender, Language.parse(MSG.GOAL_FLAGS_TOSET, flagName));
				}
			}
		}
	}

	@Override
	public void commitEnd(boolean force) {
		db.i("[FLAGS]");

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
			
			ArenaModuleManager.announce(arena, Language.parse(MSG.TEAM_HAS_WON,
					aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW), "WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON,
					aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW));
		}

		
		if (ArenaModuleManager.commitEnd(arena, aTeam)) {
			return;
		}
		new EndRunnable(arena, arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitPlayerDeath(Player respawnPlayer,
			boolean doesRespawn, String error, PlayerDeathEvent event) {
		
		ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(respawnPlayer.getName()).getArenaTeam();
		
		arena.broadcast(Language.parse(MSG.FIGHT_KILLED_BY,
				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
				arena.parseDeathCause(respawnPlayer, event.getEntity().getLastDamageCause().getCause(), event.getEntity().getKiller())));
	
		new InventoryRefillRunnable(arena, respawnPlayer, event.getDrops());
		
		if (arena.isCustomClassAlive()
				|| arena.getArenaConfig().getBoolean(CFG.PLAYER_DROPSINVENTORY)) {
			InventoryManager.drop(respawnPlayer);
			event.getDrops().clear();
		}

		SpawnManager.distribute(arena,  ArenaPlayer.parsePlayer(respawnPlayer.getName()));
		
		arena.unKillPlayer(respawnPlayer, event.getEntity()
				.getLastDamageCause().getCause(), respawnPlayer.getKiller());
	}
	
	@Override
	public boolean commitSetFlag(Player player, Block block) {
		if (block == null || !block.getType().name().equals(arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE))) {
			return false;
		}
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			return false;
		}

		db.i("trying to set a flag");

		// command : /pa redflag1
		// location: red1flag:

		SpawnManager.setBlock(arena, new PABlockLocation(block.getLocation()), flagName);

		arena.msg(player, Language.parse(MSG.GOAL_FLAGS_SET, flagName));

		PAA_Region.activeSelections.remove(player.getName());
		this.flagName = "";
		
		return false;
	}
	
	@Override
	public void commitStart() {
		
	}

	@Override
	public void configParse(YamlConfiguration config) {
		Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
	}
	
	@Override
	public void disconnect(ArenaPlayer ap) {
		if (paTeamFlags == null) {
			return;
		}
		
		ArenaTeam flagTeam = arena.getTeam(
				getHeldFlagTeam(arena, ap.getName()));
		if (flagTeam != null) {
			arena.broadcast(Language.parse(MSG.GOAL_FLAGS_DROPPED,
					ap.getArenaTeam().getColorCodeString() + ap.getName() + ChatColor.YELLOW, flagTeam.getName() + ChatColor.YELLOW));
			paTeamFlags.remove(flagTeam.getName());
			if (paHeadGears != null
					&& paHeadGears.get(ap.getName()) != null) {
				if (ap.get() != null) {
					ap.get().getInventory().setHelmet(
						paHeadGears.get(ap.getName()).clone());
				}
				paHeadGears.remove(ap.getName());
			}

			takeFlag(flagTeam.getColor().name(), false,
					SpawnManager.getCoords(arena, flagTeam.getName() + "flag"));
		}
	}

	private short getFlagOverrideTeamShort(Arena arena, String team) {
		if (arena.getArenaConfig().getUnsafe("flagColors." + team) == null) {

			return StringParser.getColorDataFromENUM(arena.getTeam(team)
					.getColor().name());
		}
		return StringParser.getColorDataFromENUM((String) arena.getArenaConfig()
				.getUnsafe("flagColors." + team));
	}

	@Override
	public PACheck getLives(PACheck res, ArenaPlayer ap) {
		if (!res.hasError() && res.getPriority() <= priority) {
			res.setError(this, "" + (paTeamLives.containsKey(ap.getArenaTeam().getName())?paTeamLives.get(ap.getArenaTeam().getName()):0));
		}
		return res;
	}

	/**
	 * get the team name of the flag a player holds
	 * 
	 * @param player
	 *            the player to check
	 * @return a team name
	 */
	private String getHeldFlagTeam(Arena arena, String player) {
		if (paTeamFlags.size() < 1) {
			return null;
		}
		
		db.i("getting held FLAG of player " + player);
		for (String sTeam : paTeamFlags.keySet()) {
			db.i("team " + sTeam + " is in " + paTeamFlags.get(sTeam)
					+ "s hands");
			if (player.equals(paTeamFlags.get(sTeam))) {
				return sTeam;
			}
		}
		return null;
	}
	
	@Override
	public String guessSpawn(String place) {
		// no exact match: assume we have multiple spawnpoints
		HashMap<Integer, String> locs = new HashMap<Integer, String>();
		int i = 0;

		db.i("searching for team spawns: " + place);
		
		HashMap<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(i++, name);
				db.i("found match: " + name);
			}
			if (name.endsWith("flag")) {
				for (ArenaTeam team : arena.getTeams()) {
					String sTeam = team.getName();
					if (name.startsWith(sTeam) && place.startsWith(sTeam)) {
						locs.put(i++, name);
						db.i("found match: " + name);
					}
				}
			}
		}

		if (locs.size() < 1) {
			return null;
		}
		Random r = new Random();

		place = locs.get(r.nextInt(locs.size()));

		return place;
	}

	@Override
	public boolean hasSpawn(String string) {
		for (String teamName : arena.getTeamNames()) {
			if (string.toLowerCase().equals(teamName.toLowerCase()+"flag")) {
				return true;
			}
			if (string.toLowerCase().startsWith(teamName.toLowerCase()+"spawn")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void initate(Player player) {
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		ArenaTeam team = ap.getArenaTeam();
		if (!paTeamLives.containsKey(team.getName())) {
			paTeamLives.put(ap.getArenaTeam().getName(), arena.getArenaConfig().getInt(CFG.GOAL_FLAGS_LIVES));

			takeFlag(team.getColor().name(), false,
					SpawnManager.getCoords(arena, team.getName() + "flag"));
		}
	}

	@Override
	public boolean isInternal() {
		return true;
	}
	
	@Override
	public void parsePlayerDeath(Player player,
			EntityDamageEvent lastDamageCause) {
		
		if (paTeamFlags == null) {
			db.i("no flags set!!");
			return;
		}
		ArenaTeam flagTeam = arena.getTeam(
				getHeldFlagTeam(arena, player.getName()));
		if (flagTeam != null) {
			ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
			arena.broadcast(Language.parse(MSG.GOAL_FLAGS_DROPPED,
					ap.getArenaTeam().colorizePlayer(player) + ChatColor.YELLOW, flagTeam.getColoredName() + ChatColor.YELLOW));
			paTeamFlags.remove(flagTeam.getName());
			if (paHeadGears != null
					&& paHeadGears.get(player.getName()) != null) {
				player.getInventory().setHelmet(
						paHeadGears.get(player.getName()).clone());
				paHeadGears.remove(player.getName());
			}

			takeFlag(flagTeam.getColor().name(), false,
					SpawnManager.getCoords(arena, flagTeam.getName() + "flag"));
		}
	}

	@Override
	public void parseStart() {
		paTeamLives.clear();
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().size() > 0) {
				db.i("adding team " + team.getName());
				// team is active
				paTeamLives.put(team.getName(),
						arena.getArenaConfig().getInt(CFG.GOAL_FLAGS_LIVES, 3));
			}
			takeFlag(team.getColor().name(), false,
					SpawnManager.getCoords(arena, team.getName() + "flag"));
		}
	}
	
	private boolean reduceLivesCheckEndAndCommit(Arena arena, String team) {

		db.i("reducing lives of team " + team);
		if (paTeamLives.get(team) != null) {
			int i = paTeamLives.get(team) - 1;
			if (i > 0) {
				paTeamLives.put(team, i);
			} else {
				paTeamLives.remove(team);
				commit(arena, team, false);
				return true;
			}
		}
		return false;
	}

	@Override
	public void reset(boolean force) {
		paTeamFlags.clear();
		paHeadGears.clear();
		paTeamLives.clear();
	}
	
	@Override
	public void setDefaults(YamlConfiguration config) {
		if (arena.isFreeForAll()) {
			return;
		}
		
		if (config.get("teams.free") != null) {
			config.set("teams",null);
		}
		if (config.get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			config.addDefault("teams.red",
					ChatColor.RED.name());
			config.addDefault("teams.blue",
					ChatColor.BLUE.name());
		}
		if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)
				&& (config.get("flagColors") == null)) {
			db.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
	}

	/**
	 * take/reset an arena flag
	 * 
	 * @param flagColor
	 *            the teamcolor to reset
	 * @param take
	 *            true if take, else reset
	 * @param pumpkin
	 *            true if pumpkin, false otherwise
	 * @param lBlock
	 *            the location to take/reset
	 */
	public void takeFlag(String flagColor, boolean take, PALocation lBlock) {
		if (!arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE).equals("WOOL")) {
			lBlock.toLocation().getBlock().setType(take?Material.BEDROCK:Material.valueOf(arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE)));
			return;
		}
		if (take) {
			lBlock.toLocation().getBlock().setData(
					StringParser.getColorDataFromENUM("WHITE"));
		} else {
			lBlock.toLocation().getBlock().setData(
					StringParser.getColorDataFromENUM(flagColor));
		}
	}

	@Override
	public HashMap<String, Double> timedEnd(
			HashMap<String, Double> scores) {
		
		for (String s : paTeamLives.keySet()) {
			double score = scores.containsKey(s) ? scores.get(s) : 0;
			score += paTeamLives.get(s); // every team life is worth 1 point
			
			scores.put(s, score);
		}
		
		return scores;
	}
	
	@Override
	public void unload(Player player) {
		disconnect(ArenaPlayer.parsePlayer(player.getName()));
		if (allowsJoinInBattle())
			arena.hasNotPlayed(ArenaPlayer.parsePlayer(player.getName()));
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();

		Arena arena = ArenaPlayer.parsePlayer(p.getName()).getArena();

		if (arena == null || !arena.getName().equals(this.arena.getName())) {
			return;
		}
		
		if (event.isCancelled() || getHeldFlagTeam(this.arena, p.getName()) == null) {
			return;
		}
		
		if (event.getInventory().getType()
			.equals(InventoryType.CRAFTING)) {
			// we are inside the standard 
			if (event.getRawSlot() != 5) {
				return;
			}
		}

		event.setCancelled(true);
	}
}
