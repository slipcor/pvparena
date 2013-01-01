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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>Arena Goal class "PhysicalFlags"</pre>
 * 
 * Capture flags by breaking them, bring them home, get points, win.
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class GoalPhysicalFlags extends ArenaGoal implements Listener {
	
	public GoalPhysicalFlags() {
		super("PhysicalFlags");
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
		headFlags.add(Material.SKULL_ITEM);
	}
	
	@Override
	public String version() {
		return "v0.10.2.21";
	}

	int priority = 7;
	int killpriority = 1;

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	public PACheck checkCommand(PACheck res, String string) {
		if (res.getPriority() > priority) {
			return res;
		}
		
		if (string.equalsIgnoreCase("flagtype") || 
				string.equalsIgnoreCase("flageffect") ||
				string.equalsIgnoreCase("touchdown")) {
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
	@SuppressWarnings("deprecation")
	@Override
	public PACheck checkInteract(PACheck res, Player player, Block block) {
		if (block == null || res.getPriority() > priority) {
			return res;
		}
		db.i("checking interact", player);

		if (!block.getType().name().equals(arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE))) {
			db.i("block, but not flag", player);
			return res;
		}
		db.i("flag click!", player);

		Vector vLoc;
		String sTeam;
		Vector vFlag = null;
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

		if (paTeamFlags.containsValue(player.getName())) {
			db.i("player " + player.getName() + " has got a flag", player);
			
			vLoc = block.getLocation().toVector();
			sTeam = ap.getArenaTeam().getName();
			db.i("block: " + vLoc.toString(), player);
			if (SpawnManager.getBlocks(arena, sTeam + "flag").size() > 0) {
				vFlag = SpawnManager.getBlockNearest(
						SpawnManager.getBlocks(arena, sTeam + "flag"),
						new PABlockLocation(player.getLocation())).toLocation().toVector();
			} else {
				db.i(sTeam + "flag = null", player);
			}

			db.i("player is in the team " + sTeam, player);
			if ((vFlag != null && vLoc.distance(vFlag) < 2)) {

				db.i("player is at his flag", player);

				if (paTeamFlags.containsKey(sTeam) || paTeamFlags.containsKey("touchdown")) {
					db.i("the flag of the own team is taken!", player);

					if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_MUSTBESAFE)
							&& !paTeamFlags.containsKey("touchdown")) {
						db.i("cancelling", player);

						arena.msg(player, Language.parse(MSG.GOAL_FLAGS_NOTSAFE));
						return res;
					}
				}

				String flagTeam = getHeldFlagTeam(arena, player.getName());

				db.i("the flag belongs to team " + flagTeam, player);
				

				
				if (player.getItemInHand() == null || !player.getItemInHand().getType().name().equals(arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE))) {
					db.i("player " + player.getName() + " is not holding the flag", player);
					arena.msg(player, Language.parse(MSG.GOAL_PHYSICALFLAGS_HOLDFLAG));
					return res;
				}
				
				player.getInventory().remove(player.getItemInHand());
				player.updateInventory();

				try {
					if (flagTeam.equals("touchdown")) {
						arena.broadcast(Language.parse(MSG.GOAL_FLAGS_TOUCHHOME, arena
								.getTeam(sTeam).colorizePlayer(player)
								+ ChatColor.YELLOW, String
								.valueOf(paTeamLives.get(ap.getArenaTeam().getName()) - 1)));
					} else {
						arena.broadcast(Language.parse(MSG.GOAL_FLAGS_BROUGHTHOME, arena
								.getTeam(sTeam).colorizePlayer(player)
								+ ChatColor.YELLOW, arena.getTeam(flagTeam)
								.getColoredName() + ChatColor.YELLOW, String
								.valueOf(paTeamLives.get(flagTeam) - 1)));
					}
					paTeamFlags.remove(flagTeam);
				} catch (Exception e) {
					Bukkit.getLogger().severe(
							"[PVP Arena] team unknown/no lives: " + flagTeam);
					e.printStackTrace();
				}
				if (flagTeam.equals("touchdown")) {
					takeFlag(ChatColor.BLACK.name(), false,
							SpawnManager.getCoords(arena, "touchdownflag"));
				} else {
					takeFlag(arena.getTeam(flagTeam).getColor().name(), false,
							SpawnManager.getCoords(arena, flagTeam + "flag"));
				}
				removeEffects(player);
				if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)) {
					if (paHeadGears.get(player.getName()) != null) {
						player.getInventory().setHelmet(
								paHeadGears.get(player.getName()).clone());
						paHeadGears.remove(player.getName());
					} else {
						player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
					}
				}
				
				flagTeam = flagTeam.equals("touchdown")?(flagTeam+":"+ap.getArenaTeam().getName()):flagTeam;
				
				reduceLivesCheckEndAndCommit(arena, flagTeam);
			}
		}
		
		return res;
	}

	private void applyEffects(Player player) {
		String value = arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGEFFECT);
		
		if (value.equalsIgnoreCase("none")) {
			return;
		}
		
		PotionEffectType pet = null;
		
		String[] split = value.split("x");
		
		int amp = 1;
		
		if (split.length > 1) {
			try {
				amp = Integer.parseInt(split[1]);
			} catch (Exception e) {
				
			}
		}
		
		for (PotionEffectType x : PotionEffectType.values()) {
			if (x == null) {
				continue;
			}
			if (x.getName().equalsIgnoreCase(split[0])) {
				pet = x;
				break;
			}
		}
		
		if (pet == null) {
			PVPArena.instance.getLogger().warning("Invalid Potion Effect Definition: " + value);
			return;
		}
		
		player.addPotionEffect(new PotionEffect(pet, amp, 2147000));
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
	public PACheck checkSetBlock(PACheck res, Player player, Block block) {

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
			
		} else if (args[0].equalsIgnoreCase("flageffect")) {
			
			// /pa [arena] flageffect SLOW 2
			if (args.length < 2) {
				arena.msg(sender, Language.parse(MSG.ERROR_INVALID_ARGUMENT_COUNT, String.valueOf(args.length), "2"));
				return;
			}

			
			if (args[1].equalsIgnoreCase("none")) {
				arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGEFFECT, args[1]);
				
				arena.getArenaConfig().save();
				arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.GOAL_FLAGS_FLAGEFFECT.getNode(), args[1]));
				return;
			}
			
			PotionEffectType pet = null;
			
			for (PotionEffectType x : PotionEffectType.values()) {
				if (x == null) {
					continue;
				}
				if (x.getName().equalsIgnoreCase(args[1])) {
					pet = x;
					break;
				}
			}
			
			if (pet == null) {
				arena.msg(sender, Language.parse(MSG.ERROR_POTIONEFFECTTYPE_NOTFOUND, args[1]));
				return;
			}
			
			int amp = 1;
			
			if (args.length == 5) {
				try {
					amp = Integer.parseInt(args[2]);
				} catch (Exception e) {
					arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[2]));
					return;
				}
			}
			String value = args[1]+"x"+amp;
			arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGEFFECT, value);
			
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.GOAL_FLAGS_FLAGEFFECT.getNode(), value));
			
		} else if (args[0].contains("flag")) {
			for (ArenaTeam team : arena.getTeams()) {
				String sTeam = team.getName();
				if (args[0].contains(sTeam + "flag")) {
					flagName = args[0];
					PAA_Region.activeSelections.put(sender.getName(), arena);


					arena.msg(sender, Language.parse(MSG.GOAL_FLAGS_TOSET, flagName));
				}
			}
		} else if (args[0].equalsIgnoreCase("touchdown")) {
			flagName = args[0]+"flag";
			PAA_Region.activeSelections.put(sender.getName(), arena);

			arena.msg(sender, Language.parse(MSG.GOAL_FLAGS_TOSET, flagName));
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
	public boolean commitSetFlag(Player player, Block block) {
		if (block == null || !block.getType().name().equals(arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE))) {
			return false;
		}
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			return false;
		}

		db.i("trying to set a flag", player);

		// command : /pa redflag1
		// location: red1flag:

		SpawnManager.setBlock(arena, new PABlockLocation(block.getLocation()), flagName);

		arena.msg(player, Language.parse(MSG.GOAL_FLAGS_SET, flagName));

		PAA_Region.activeSelections.remove(player.getName());
		flagName = "";
		
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
		String sTeam = getHeldFlagTeam(arena, ap.getName());
		ArenaTeam flagTeam = arena.getTeam(sTeam);
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
		} else if (sTeam != null) {
			arena.broadcast(Language.parse(MSG.GOAL_FLAGS_DROPPEDTOUCH,
					ap.getArenaTeam().getColorCodeString() + ap.getName() + ChatColor.YELLOW));
			
			paTeamFlags.remove("touchdown");
			if (paHeadGears != null
					&& paHeadGears.get(ap.getName()) != null) {
				if (ap.get() != null) {
					ap.get().getInventory().setHelmet(
						paHeadGears.get(ap.getName()).clone());
				}
				paHeadGears.remove(ap.getName());
			}

			takeFlag(ChatColor.BLACK.name(), false,
					SpawnManager.getCoords(arena, "touchdownflag"));
			
		}
	}

	private short getFlagOverrideTeamShort(Arena arena, String team) {
		if (arena.getArenaConfig().getUnsafe("flagColors." + team) == null) {
			if (team.equals("touchdown")) {
				return StringParser.getColorDataFromENUM(ChatColor.BLACK
					.name());
			}
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
		
		db.i("getting held FLAG of player " + player, player);
		for (String sTeam : paTeamFlags.keySet()) {
			db.i("team " + sTeam + " is in " + paTeamFlags.get(sTeam)
					+ "s hands", player);
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
			takeFlag(ChatColor.BLACK.name(), false,
					SpawnManager.getCoords(arena, "touchdownflag"));
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
			db.i("no flags set!!", player);
			return;
		}
		String sTeam = getHeldFlagTeam(arena, player.getName());
		ArenaTeam flagTeam = arena.getTeam(sTeam);
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		if (flagTeam != null) {
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
		} else if (sTeam != null) {
			arena.broadcast(Language.parse(MSG.GOAL_FLAGS_DROPPEDTOUCH,
					ap.getArenaTeam().getColorCodeString() + ap.getName() + ChatColor.YELLOW));
			
			paTeamFlags.remove("touchdown");
			if (paHeadGears != null
					&& paHeadGears.get(ap.getName()) != null) {
				if (ap.get() != null) {
					ap.get().getInventory().setHelmet(
						paHeadGears.get(ap.getName()).clone());
				}
				paHeadGears.remove(ap.getName());
			}

			takeFlag(ChatColor.BLACK.name(), false,
					SpawnManager.getCoords(arena, "touchdownflag"));
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
		takeFlag(ChatColor.BLACK.name(), false,
				SpawnManager.getCoords(arena, "touchdownflag"));
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
		} else if (team.contains(":")) {
			String realTeam = team.split(":")[1];
			int i = paTeamLives.get(realTeam) - 1;
			if (i > 0) {
				paTeamLives.put(realTeam, i);
			} else {
				paTeamLives.remove(realTeam);
				commit(arena, realTeam, true);
				return true;
			}
		}
		return false;
	}
	
	private void removeEffects(Player player) {
		String value = arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGEFFECT);
		
		if (value.equalsIgnoreCase("none")) {
			return;
		}
		
		PotionEffectType pet = null;
		
		String[] split = value.split("x");
		
		for (PotionEffectType x : PotionEffectType.values()) {
			if (x == null) {
				continue;
			}
			if (x.getName().equalsIgnoreCase(split[0])) {
				pet = x;
				break;
			}
		}
		
		if (pet == null) {
			PVPArena.instance.getLogger().warning("Invalid Potion Effect Definition: " + value);
			return;
		}

		player.removePotionEffect(pet);
		player.addPotionEffect(new PotionEffect(pet, 0, 1));
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
		if (lBlock == null) {
			return;
		}
		if (!arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE).equals("WOOL")) {
			lBlock.toLocation().getBlock().setType(take?Material.BEDROCK:Material.valueOf(arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE)));
			return;
		}
		if (take) {
			lBlock.toLocation().getBlock().setData(
					StringParser.getColorDataFromENUM("WHITE"));
		} else {
			lBlock.toLocation().getBlock().setTypeIdAndData(Material.valueOf(arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE)).getId(),
					StringParser.getColorDataFromENUM(flagColor), false);
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
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onFlagClaim(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (!arena.hasPlayer(event.getPlayer()) || !event.getBlock().getType().name().equals(arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE))) {

			db.i("block destroy, ignoring", player);
			db.i(String.valueOf(arena.hasPlayer(event.getPlayer())), player);
			db.i(event.getBlock().getType().name(), player);
			return;
		}

		Block block = event.getBlock();
		
		db.i("flag destroy!", player);

		Vector vLoc;
		Vector vFlag = null;
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

		if (paTeamFlags.containsValue(player.getName())) {
			db.i("already carries a flag!", player);
			return;
		} else {
			ArenaTeam pTeam = ap.getArenaTeam();
			if (pTeam == null) {
				return;
			}
			HashSet<ArenaTeam> setTeam = new HashSet<ArenaTeam>();

			for (ArenaTeam team : arena.getTeams()) {
				setTeam.add(team);
			}
			setTeam.add(new ArenaTeam("touchdown","BLACK"));
			for (ArenaTeam team : setTeam) {
				String aTeam = team.getName();

				if (aTeam.equals(pTeam.getName())) {
					db.i("equals!OUT! ", player);
					continue;
				}
				if (team.getTeamMembers().size() < 1 && !team.getName().equals("touchdown")) {
					db.i("size!OUT! ", player);
					continue; // dont check for inactive teams
				}
				if (paTeamFlags != null && paTeamFlags.containsKey(aTeam)) {
					db.i("taken!OUT! ", player);
					continue; // already taken
				}
				db.i("checking for flag of team " + aTeam, player);
				vLoc = block.getLocation().toVector();
				db.i("block: " + vLoc.toString(), player);
				if (SpawnManager.getBlocks(arena, aTeam + "flag").size() > 0) {
					vFlag = SpawnManager.getBlockNearest(
							SpawnManager.getBlocks(arena, aTeam + "flag"),
							new PABlockLocation(player.getLocation())).toLocation().toVector();
				}
				if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
					db.i("flag found!", player);
					db.i("vFlag: " + vFlag.toString(), player);
					
					if (team.getName().equals("touchdown")) {

						arena.broadcast(Language.parse(MSG.GOAL_FLAGS_GRABBEDTOUCH,
								pTeam.colorizePlayer(player) + ChatColor.YELLOW));
					} else {
					
						arena.broadcast(Language.parse(MSG.GOAL_FLAGS_GRABBED,
								pTeam.colorizePlayer(player) + ChatColor.YELLOW,
								team.getColoredName() + ChatColor.YELLOW));
					}
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
					applyEffects(player);

					takeFlag(team.getColor().name(), true, new PALocation(block.getLocation()));
					paTeamFlags.put(aTeam, player.getName());
					
					return; 
				}
			}
		}
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
