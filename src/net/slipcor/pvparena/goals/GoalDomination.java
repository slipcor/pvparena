package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
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
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;

/**
 * <pre>Arena Goal class "Domination"</pre>
 * 
 * The most fast paced arena goal atm. Lighting a TNT ends the game. BOOM.
 * 
 * @author slipcor
 * 
 * @version v0.9.8
 */

public class GoalDomination extends ArenaGoal {

	public GoalDomination(Arena arena) {
		super(arena, "Domination");
		db = new Debug(99);
	}

	protected HashMap<Location, String> paFlags = new HashMap<Location, String>();
	protected HashMap<String, Integer> paTeamLives = new HashMap<String, Integer>();
	protected HashMap<String, String> paTeamFlags = new HashMap<String, String>();
	protected HashMap<Location, DominationRunnable> paRuns = new HashMap<Location, DominationRunnable>();

	private String flagName = "";
	
	@Override
	public String version() {
		return "v0.9.8.0";
	}

	int priority = 8;
	int killpriority = 1;
	
	@Override
	public GoalDomination clone() {
		return new GoalDomination(arena);
	}

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	public PACheck checkCommand(PACheck res, String string) {
		if (res.getPriority() > priority) {
			return res;
		}
		
		if (string.equals("flag")) {
			res.setPriority(this, priority);
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
		int count = 0;
		for (String s : list) {
			if (s.startsWith("flag")) {
				count++;
			}
		}
		if (count < 4)
			return "flags: " + count + " / 4";
		
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
		
		
		/**
		 * bullshit, this all needs to go, only admin flag setting in here !
		 * 
		 * but the method beyond can be used to retrieve all possible spawns
		 */
		
		db.i("checking interact");

		if (!block.getType().equals(Material.WOOL)) {
			db.i("block, but not flag");
			return res;
		}
		db.i("flag click!");

		
		HashSet<PABlockLocation> flags = SpawnManager.getBlocks(arena, "flags");
		
		if (flags.size() < 4) {
			return res;
		}
		
		Vector vFlag = SpawnManager.getBlockNearest(
				flags,
				new PABlockLocation(player.getLocation())).toLocation().toVector();

		
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

	/**
	 * return a hashset of players names being near a specified location, except
	 * one player
	 * 
	 * @param loc
	 *            the location to check
	 * @param distance
	 *            the distance in blocks
	 * @return a set of player names
	 */
	private HashSet<String> checkLocationPresentTeams(Location loc, int distance) {
		HashSet<String> result = new HashSet<String>();

		for (ArenaPlayer p : arena.getFighters()) {

			if (p.get().getLocation().distance(loc) > distance) {
				continue;
			}

			result.add(p.getArenaTeam().getName());
		}

		return result;
	}
	

	protected void checkMove() {

		/**
		 * possible Situations
		 * 
		 * >>- flag is unclaimed and no one is there - flag is unclaimed and
		 * team a is there - flag is unclaimed and multiple teams are there
		 * 
		 * >>- flag is being claimed by team a, no one is present - flag is
		 * being claimed by team a, team a is present - flag is being claimed by
		 * team a, multiple teams are present - flag is being claimed by team a,
		 * team b is present
		 * 
		 * >>- flag is claimed by team a, no one is present >>- flag is claimed
		 * by team a, team a is present >>- flag is claimed by team a, multiple
		 * teams are present >>- flag is claimed by team a, team b is present
		 * 
		 * >>- flag is claimed by team a and being unclaimed, no one is present
		 * >>- flag is claimed by team a and being unclaimed, team a is present
		 * >>- flag is claimed by team a and being unclaimed, multiple teams are
		 * present >>- flag is claimed by team a and being unclaimed, team b is
		 * present
		 * 
		 */

		db.i("------------------");
		db.i("   checkMove();");
		db.i("------------------");
		
		int checkDistance = arena.getArenaConfig().getInt(CFG.GOAL_DOM_CLAIMRANGE);

		for (PALocation loc : SpawnManager.getSpawns(arena, "flags")) {
			//db.i("checking location: " + loc.toString());
			
			HashSet<String> teams = checkLocationPresentTeams(loc.toLocation(),
					checkDistance);
			
			String sTeams = "teams: ";
			
			for (String team : teams) {
				sTeams += ", " + team;
			}

			db.i(sTeams);

			// teams now contains all teams near the flag

			if (teams.size() < 1) {
				//db.i("=> noone there!");
				// no one there
				if (paRuns.containsKey(loc)) {
					db.i("flag is being (un)claimed! Cancelling!");
					// cancel unclaiming/claiming if noone's near
					Bukkit.getScheduler().cancelTask(paRuns.get(loc).ID);
					paRuns.remove(loc);
				}
				if (paFlags.containsKey(loc)) {
					String team = paFlags.get(loc);
					
					// flag claimed! add score!
					reduceLivesCheckEndAndCommit(arena, team);
					arena.broadcast(
							Language.parse(MSG.GOAL_DOMINATION_SCORE, arena.getTeam(team).getColoredName()
									+ ChatColor.YELLOW));
				}
				continue;
			}

			// there are actually teams at the flag
			db.i("=> at least one team is at the flag!");

			if (paFlags.containsKey(loc)) {
				// flag is taken. by whom?
				if (teams.contains(paFlags.get(loc))) {
					// owning team is there
					db.i("  - owning team is there");
					if (teams.size() > 1) {
						// another team is there
						db.i("    - and another one");
						if (paRuns.containsKey(loc)) {
							// it is being unclaimed
							db.i("      - being unclaimed. continue!");
						} else {
							// unclaim
							db.i("      - not being unclaimed. do it!");
							DominationRunnable dr = new DominationRunnable(
									arena, false, loc.toLocation(), "another team", this);
							dr.ID = Bukkit.getScheduler()
									.scheduleSyncRepeatingTask(PVPArena.instance,
											dr, 10 * 20L, 10 * 20L);
							paRuns.put(loc.toLocation(), dr);
						}
					} else {
						// just the owning team is there
						db.i("    - noone else");
						if (paRuns.containsKey(loc)) {
							db.i("      - being unclaimed. cancel!");
							// it is being unclaimed
							// cancel task!
							Bukkit.getScheduler()
									.cancelTask(paRuns.get(loc).ID);
							paRuns.remove(loc);
						} else {
							
							String team = paFlags.get(loc);
							
							// flag claimed! add score!
							reduceLivesCheckEndAndCommit(arena,team);
							arena.broadcast(
									Language.parse(MSG.GOAL_DOMINATION_SCORE, arena.getTeam(team).getColoredName()
											+ ChatColor.YELLOW));
						}
					}
					continue;
				}

				db.i("  - owning team is not there!");
				// owning team is NOT there ==> unclaim!

				if (paRuns.containsKey(loc)) {
					if (paRuns.get(loc).take) {
						db.i("    - runnable is trying to score, abort");

						Bukkit.getScheduler().cancelTask(paRuns.get(loc).ID);
						paRuns.remove(loc);
					} else {
						db.i("    - being unclaimed. continue.");
					}
					continue;
				}
				db.i("    - not yet being unclaimed, do it!");
				// create an unclaim runnable
				DominationRunnable running = new DominationRunnable(arena,
						false, loc.toLocation(), paFlags.get(loc), this);
				long interval = 20L * 10;

				
				running.ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
						PVPArena.instance, running, interval, interval);
				paRuns.put(loc.toLocation(), running);
			} else {
				// flag not taken
				db.i("- flag not taken");

				/*
				 * check if a runnable yes check if only that team yes =>
				 * continue; no => cancel no check if only that team yes =>
				 * create runnable; no => continue
				 */
				if (paRuns.containsKey(loc)) {
					db.i("  - being claimed");
					if (teams.size() < 2) {
						db.i("  - only one team present");
						if (teams.contains(paRuns.get(loc).team)) {
							// just THE team that is claiming => NEXT
							db.i("  - claiming team present. next!");
							continue;
						}
					}
					db.i("  - more than one team. cancel claim!");
					// more than THE team that is claiming => cancel!
					Bukkit.getScheduler().cancelTask(paRuns.get(loc).ID);
				} else {
					db.i("  - not being claimed");
					// not being claimed
					if (teams.size() < 2) {
						db.i("  - just one team present");
						for (String sName : teams) {
							db.i("TEAM " + sName + " IS CLAIMING " + loc.toString());
							ArenaTeam team = arena.getTeam(sName);
							arena.broadcast(Language.parse(MSG.GOAL_DOMINATION_CLAIMING,
									team.getColoredName()));

							DominationRunnable running = new DominationRunnable(
									arena, true, loc.toLocation(), sName, this);
							long interval = 20L * 10;
							running.ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
									PVPArena.instance, running, interval, interval);
							paRuns.put(loc.toLocation(), running);
						}
					} else {
						db.i("  - more than one team present. continue!");
					}
				}
			}
		}
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
			
			PVPArena.instance.getAmm().announce(arena, Language.parse(MSG.TEAM_HAS_WON,
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
		if (PAA_Region.activeSelections.containsKey(sender.getName())) {
			PAA_Region.activeSelections.remove(sender.getName());
			arena.msg(sender, Language.parse(MSG.GOAL_FLAGS_SET, "flags"));
		} else {
			
			PAA_Region.activeSelections.put(sender.getName(), arena);
			arena.msg(sender, Language.parse(MSG.GOAL_FLAGS_TOSET, "flags"));
		}
	}

	@Override
	public void commitEnd(boolean force) {
		db.i("[DOMINATION]");

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
			PVPArena.instance.getAmm().announce(arena, Language.parse(MSG.TEAM_HAS_WON,
					aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW), "WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON,
					aTeam.getColor() + "Team "
							+ aTeam.getName() + ChatColor.YELLOW));
		}

		if (PVPArena.instance.getAmm().commitEnd(arena, aTeam)) {
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
		
		arena.tpPlayerToCoordName(respawnPlayer, respawnTeam.getName()
				+ "spawn");
		
		arena.unKillPlayer(respawnPlayer, event.getEntity()
				.getLastDamageCause().getCause(), respawnPlayer.getKiller());
	}
	
	@Override
	public boolean commitSetFlag(Player player, Block block) {
		if (block == null || !block.getType().equals(Material.WOOL)) {
			return false;
		}
		
		if (PVPArena.hasAdminPerms(player)
				|| (PVPArena.hasCreatePerms(player, arena))
				&& (player.getItemInHand() != null)
				&& (player.getItemInHand().getTypeId() == arena.getArenaConfig().getInt(
						CFG.GENERAL_WAND))) {

			HashSet<PABlockLocation> flags = SpawnManager.getBlocks(arena, "flags");
			
			if (flags.contains(new PABlockLocation(block.getLocation()))) {
				return false;
			}
			
			flagName = "flag" + flags.size();
			
			SpawnManager.setBlock(arena, new PABlockLocation(block.getLocation()), flagName);

			arena.msg(player, Language.parse(MSG.GOAL_FLAGS_SET, flagName));
			return true;
		}
		return false;
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
			if (name.startsWith("flag")) {
				locs.put(i++, name);
				db.i("found match: " + name);
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
	public void parseStart() {
		paTeamLives.clear();
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().size() > 0) {
				db.i("adding team " + team.getName());
				// team is active
				paTeamLives.put(team.getName(),
						arena.getArenaConfig().getInt(CFG.GOAL_DOM_LIVES, 3));
			}
			takeFlag(team.getColor().name(), false,
					SpawnManager.getCoords(arena, team.getName() + "flag"));
		}

		DominationMainRunnable dmr = new DominationMainRunnable(arena, this);
		dmr.ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, dmr, 3*20L, 3*20L);
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
		paTeamLives.clear();
		paRuns.clear();
		paFlags.clear();
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
		if (take) {
			lBlock.toLocation().getBlock().setData(
					StringParser.getColorDataFromENUM("WHITE"));
		} else {
			lBlock.toLocation().getBlock().setData(
					StringParser.getColorDataFromENUM(flagColor));
		}
	}

	static void takeFlag(Arena arena, Location lBlock, String name) {
		ArenaTeam team = null;
		for (ArenaTeam t : arena.getTeams()) {
			if (t.getName().equals(name)) {
				team = t;
			}
		}
		if (team == null) {
			lBlock.getBlock().setData(StringParser.getColorDataFromENUM("WHITE"));
			return;
		}
		lBlock.getBlock().setData(StringParser.getColorDataFromENUM(team.getColor().name()));
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
	
	protected class DominationRunnable implements Runnable {
		public final boolean take;
		public final Location loc;
		public int ID = -1;
		private final Arena arena;
		public final String team;
		private Debug db = new Debug(39);
		private final GoalDomination domination;

		/**
		 * create a domination runnable
		 * 
		 * @param a
		 *            the arena we are running in
		 * @param domination 
		 */
		public DominationRunnable(Arena a, boolean b, Location l, String s, GoalDomination d) {
			arena = a;
			take = b;
			team = s;
			loc = l;
			domination = d;
			db.i("Domination constructor");
		}
		

		/**
		 * the run method, commit arena end
		 */
		@Override
		public void run() {
			db.i("DominationRunnable commiting");
			db.i("team " + team + ", take: " + String.valueOf(take));
			if (take) {
				// claim a flag for the team
				if (domination.paFlags.containsKey(loc)) {
					PVPArena.instance.getLogger().warning("wtf");
				} else {
					// flag unclaimed! claim!
					db.i("clag unclaimed. claim!");
					domination.paFlags.put(loc, team);
					//long interval = 20L * 5;
					
					arena.broadcast(
							Language.parse(MSG.GOAL_DOMINATION_CLAIMING, arena.getTeam(team).getColoredName()
									+ ChatColor.YELLOW));
					GoalDomination.takeFlag(arena, loc, team);
					
					// claim done. end timer
					Bukkit.getScheduler().cancelTask(ID);
					domination.paRuns.remove(loc);
				}
			} else {
				// unclaim
				db.i("unclaim");
				arena.broadcast(
						Language.parse(MSG.GOAL_DOMINATION_UNCLAIMING, team
								+ ChatColor.YELLOW));
				GoalDomination.takeFlag(arena, loc, "");
				Bukkit.getScheduler().cancelTask(ID);
				domination.paRuns.remove(loc);
				domination.paFlags.remove(loc);
			}
		}

		public boolean noOneThere(int checkDistance) {
			for (ArenaPlayer p : arena.getFighters()) {
				if (p.get().getLocation().distance(loc) < checkDistance) {
					return false;
				}
			}
			return true;
		}
	}
	protected class DominationMainRunnable implements Runnable {
		public int ID = -1;
		private final Arena arena;
		private Debug db = new Debug(39);
		private final GoalDomination domination;

		public DominationMainRunnable(Arena a, GoalDomination d) {
			arena = a;
			domination = d;
			db.i("DominationMainRunnable constructor");
		}

		/**
		 * the run method, commit arena end
		 */
		@Override
		public void run() {
			if (!arena.isFightInProgress() || arena.REALEND_ID != null) {
				Bukkit.getScheduler().cancelTask(ID);
			}
			domination.checkMove();
		}
	}
}
