package net.slipcor.pvparena.goals;

import java.util.HashMap;
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
 * <pre>Arena Goal class "BlockDestroy"</pre>
 * 
 * Win by breaking the other team's block(s).
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class GoalBlockDestroy extends ArenaGoal implements Listener {
	
	public GoalBlockDestroy() {
		super("BlockDestroy");
		db = new Debug(100);
	}
	
	private HashMap<String, Integer> paTeamLives = new HashMap<String, Integer>();
	
	private String blockTeamName = "";
	
	@Override
	public String version() {
		return "v0.10.2.21";
	}

	int priority = 8;
	int killpriority = 1;

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	public PACheck checkCommand(PACheck res, String string) {
		if (res.getPriority() > priority) {
			return res;
		}
		
		if (string.equalsIgnoreCase("blocktype")) {
			res.setPriority(this, priority);
		}
		
		for (ArenaTeam team : arena.getTeams()) {
			String sTeam = team.getName();
			if (string.contains(sTeam + "block")) {
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
			if (!list.contains(sTeam + "block")) {
				boolean found = false;
				for (String s : list) {
					if (s.startsWith(sTeam) && s.endsWith("block")) {
						found = true;
						break;
					}
				}
				if (!found)
					return team.getName() + "block not set";
			}
		}
		return null;
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
		db.i("[BD] committing end: " + sTeam);
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
		if (args[0].equalsIgnoreCase("blocktype")) {
			if (args.length < 2) {
				arena.msg(sender, Language.parse(MSG.ERROR_INVALID_ARGUMENT_COUNT, String.valueOf(args.length), "2"));
				return;
			}
			
			try {
				int i = Integer.parseInt(args[1]);
				arena.getArenaConfig().set(CFG.GOAL_BLOCKDESTROY_BLOCKTYPE, Material.getMaterial(i).name());
			} catch (Exception e) {
				Material mat = Material.getMaterial(args[1].toUpperCase());
				
				if (mat == null) {
					arena.msg(sender, Language.parse(MSG.ERROR_MAT_NOT_FOUND, args[1]));
					return;
				}
				
				arena.getArenaConfig().set(CFG.GOAL_BLOCKDESTROY_BLOCKTYPE, mat.name());
			}
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.GOAL_BLOCKDESTROY_TYPESET, CFG.GOAL_BLOCKDESTROY_BLOCKTYPE.toString()));
			
		} else if (args[0].contains("block")) {
			for (ArenaTeam team : arena.getTeams()) {
				String sTeam = team.getName();
				if (args[0].contains(sTeam + "block")) {
					blockTeamName = args[0];
					PAA_Region.activeSelections.put(sender.getName(), arena);


					arena.msg(sender, Language.parse(MSG.GOAL_BLOCKDESTROY_TOSET, blockTeamName));
				}
			}
		}
	}

	@Override
	public void commitEnd(boolean force) {
		db.i("[BD]");

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
		if (block == null || !block.getType().name().equals(arena.getArenaConfig().getString(CFG.GOAL_BLOCKDESTROY_BLOCKTYPE))) {
			return false;
		}
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			return false;
		}

		db.i("trying to set a block", player);

		// command : /pa redblock1
		// location: red1block:

		SpawnManager.setBlock(arena, new PABlockLocation(block.getLocation()), blockTeamName);

		arena.msg(player, Language.parse(MSG.GOAL_BLOCKDESTROY_SET, blockTeamName));

		PAA_Region.activeSelections.remove(player.getName());
		blockTeamName = "";
		
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
	public PACheck getLives(PACheck res, ArenaPlayer ap) {
		if (!res.hasError() && res.getPriority() <= priority) {
			res.setError(this, "" + (paTeamLives.containsKey(ap.getArenaTeam().getName())?paTeamLives.get(ap.getArenaTeam().getName()):0));
		}
		return res;
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
			if (name.endsWith("block")) {
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
			if (string.toLowerCase().equals(teamName.toLowerCase()+"block")) {
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
			paTeamLives.put(ap.getArenaTeam().getName(), arena.getArenaConfig().getInt(CFG.GOAL_BLOCKDESTROY_LIVES));

			takeBlock(team.getColor().name(), false,
					SpawnManager.getCoords(arena, team.getName() + "block"));
		}
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public void parseStart() {
		paTeamLives.clear();
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().size() > 0) {
				db.i("adding team " + team.getName());
				// team is active
				paTeamLives.put(team.getName(),
						arena.getArenaConfig().getInt(CFG.GOAL_BLOCKDESTROY_LIVES, 1));
			}
			takeBlock(team.getColor().name(), false,
					SpawnManager.getCoords(arena, team.getName() + "block"));
		}
	}
	
	private boolean reduceLivesCheckEndAndCommit(Arena arena, String team) {

		db.i("reducing lives of team " + team);
		int i = paTeamLives.get(team) - 1;
		if (i > 0) {
			paTeamLives.put(team, i);
		} else {
			paTeamLives.remove(team);
			commit(arena, team, false);
			return true;
		}
		return false;
	}

	@Override
	public void reset(boolean force) {
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
	}

	/**
	 * take/reset an arena block
	 * 
	 * @param blockColor
	 *            the teamcolor to reset
	 * @param take
	 *            true if take, else reset
	 * @param pumpkin
	 *            true if pumpkin, false otherwise
	 * @param lBlock
	 *            the location to take/reset
	 */
	public void takeBlock(String blockColor, boolean take, PALocation lBlock) {
		if (lBlock == null) {
			return;
		}
		if (!arena.getArenaConfig().getString(CFG.GOAL_BLOCKDESTROY_BLOCKTYPE).equals("WOOL")) {
			lBlock.toLocation().getBlock().setTypeId(Material.valueOf(arena.getArenaConfig().getString(CFG.GOAL_BLOCKDESTROY_BLOCKTYPE)).getId());
		} else {
			lBlock.toLocation().getBlock().setTypeIdAndData(Material.valueOf(arena.getArenaConfig().getString(CFG.GOAL_BLOCKDESTROY_BLOCKTYPE)).getId(),
					StringParser.getColorDataFromENUM(blockColor), false);
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
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (!arena.hasPlayer(event.getPlayer()) || !event.getBlock().getType().name().equals(arena.getArenaConfig().getString(CFG.GOAL_BLOCKDESTROY_BLOCKTYPE))) {

			db.i("block destroy, ignoring", player);
			db.i(String.valueOf(arena.hasPlayer(event.getPlayer())), player);
			db.i(event.getBlock().getType().name(), player);
			return;
		}

		Block block = event.getBlock();
		
		db.i("block destroy!", player);

		Vector vLoc;
		Vector vBlock = null;
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

	
		ArenaTeam pTeam = ap.getArenaTeam();
		if (pTeam == null) {
			return;
		}

		for (ArenaTeam team : arena.getTeams()) {
			String blockTeam = team.getName();

			if (blockTeam.equals(pTeam.getName())) {
				db.i("equals!OUT! ", player);
				continue;
			}
			if (team.getTeamMembers().size() < 1 && !team.getName().equals("touchdown")) {
				db.i("size!OUT! ", player);
				continue; // dont check for inactive teams
			}
			
			db.i("checking for block of team " + blockTeam, player);
			vLoc = block.getLocation().toVector();
			db.i("block: " + vLoc.toString(), player);
			if (SpawnManager.getBlocks(arena, blockTeam + "block").size() > 0) {
				vBlock = SpawnManager.getBlockNearest(
						SpawnManager.getBlocks(arena, blockTeam + "block"),
						new PABlockLocation(player.getLocation())).toLocation().toVector();
			}
			if ((vBlock != null) && (vLoc.distance(vBlock) < 2)) {
				
				/////////
				
				String sTeam = pTeam.getName();
				
				try {
					arena.broadcast(Language.parse(MSG.GOAL_BLOCKDESTROY_SCORE, arena
							.getTeam(sTeam).colorizePlayer(player)
							+ ChatColor.YELLOW, arena.getTeam(blockTeam)
							.getColoredName() + ChatColor.YELLOW, String
							.valueOf(paTeamLives.get(blockTeam) - 1)));
				} catch (Exception e) {
					Bukkit.getLogger().severe(
							"[PVP Arena] team unknown/no lives: " + blockTeam);
					e.printStackTrace();
				}
				takeBlock(arena.getTeam(blockTeam).getColor().name(), false,
						SpawnManager.getCoords(arena, blockTeam + "block"));
				
				reduceLivesCheckEndAndCommit(arena, blockTeam);
				
				/////////
				
				return; 
			}
		}
	}
}
