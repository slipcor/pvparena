package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
 * <pre>
 * Arena Goal class "Domination"
 * </pre>
 * 
 * The most fast paced arena goal atm. Lighting a TNT ends the game. BOOM.
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class GoalDomination extends ArenaGoal {

	public GoalDomination() {
		super("Domination");
		debug = new Debug(99);
	}

	private final Map<Location, String> paFlags = new HashMap<Location, String>();
	private final Map<String, Integer> paTeamLives = new HashMap<String, Integer>();
	private final Map<String, String> paTeamFlags = new HashMap<String, String>();
	private final Map<Location, DominationRunnable> paRuns = new HashMap<Location, DominationRunnable>();

	private String flagName = "";

	@Override
	public String version() {
		return "v0.10.3.0";
	}

	private static final int PRIORITY = 8;

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	public PACheck checkCommand(final PACheck res, final String string) {
		if (res.getPriority() > PRIORITY) {
			return res;
		}

		if (string.equals("flag")) {
			res.setPriority(this, PRIORITY);
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
			res.setError(this, "No teams playing!");
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(final Set<String> list) {
		int count = 0;
		for (String s : list) {
			if (s.startsWith("flag")) {
				count++;
			}
		}
		if (count < 1) {
			return "flags: " + count + " / 1";
		}
		return null;
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
	private Set<String> checkLocationPresentTeams(final Location loc, final int distance) {
		final Set<String> result = new HashSet<String>();

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
		 * >>- flag is unclaimed and no one is there
		 * >>- flag is unclaimed and team a is there
		 * >>- flag is unclaimed and multiple teams are there
		 * 
		 * >>- flag is being claimed by team a, no one is present
		 * >>- flag is being claimed by team a, team a is present
		 * >>- flag is being claimed by team a, multiple teams are present
		 * >>- flag is being claimed by team a, team b is present
		 * 
		 * >>- flag is claimed by team a, no one is present
		 * >>- flag is claimed by team a, team a is present
		 * >>- flag is claimed by team a, multiple teams are present
		 * >>- flag is claimed by team a, team b is present
		 * 
		 * >>- flag is claimed by team a and being unclaimed, no one is present
		 * >>- flag is claimed by team a and being unclaimed, team a is present
		 * >>- flag is claimed by team a and being unclaimed, multiple teams are present
		 * >>- flag is claimed by team a and being unclaimed, team b is present
		 * 
		 */

		debug.i("------------------");
		debug.i("   checkMove();");
		debug.i("------------------");

		final int checkDistance = arena.getArenaConfig().getInt(
				CFG.GOAL_DOM_CLAIMRANGE);

		for (PALocation paLoc : SpawnManager.getSpawns(arena, "flags")) {
			// debug.i("checking location: " + loc.toString());
			
			Location loc = paLoc.toLocation();

			final Set<String> teams = checkLocationPresentTeams(paLoc.toLocation(),
					checkDistance);

			debug.i("teams: " + StringParser.joinSet(teams, ", "));

			// teams now contains all teams near the flag

			if (teams.size() < 1) {
				// debug.i("=> noone there!");
				// no one there
				if (paRuns.containsKey(loc)) {
					debug.i("flag is being (un)claimed! Cancelling!");
					// cancel unclaiming/claiming if noone's near
					Bukkit.getScheduler().cancelTask(paRuns.get(loc).runID);
					paRuns.remove(loc);
				}
				if (paFlags.containsKey(loc)) {
					final String team = paFlags.get(loc);

					if (!paTeamLives.containsKey(team)) {
						continue;
					}
					
					// flag claimed! add score!
					reduceLivesCheckEndAndCommit(arena, team);
					
					int max = arena.getArenaConfig().getInt(CFG.GOAL_DOM_LIVES);
					int lives = this.paTeamLives.get(team);
					
					arena.broadcast(Language.parse(MSG.GOAL_DOMINATION_SCORE,
							arena.getTeam(team).getColoredName()
									+ ChatColor.YELLOW, (max-lives)+"/"+max));
				}
				continue;
			}

			// there are actually teams at the flag
			debug.i("=> at least one team is at the flag!");

			if (paFlags.containsKey(loc)) {
				// flag is taken. by whom?
				if (teams.contains(paFlags.get(loc))) {
					// owning team is there
					debug.i("  - owning team is there");
					if (teams.size() > 1) {
						// another team is there
						debug.i("    - and another one");
						if (paRuns.containsKey(loc)) {
							// it is being unclaimed
							debug.i("      - being unclaimed. continue!");
						} else {
							// unclaim
							debug.i("      - not being unclaimed. do it!");
							final DominationRunnable domRunner = new DominationRunnable(
									arena, false, loc,
									paFlags.get(loc), this);
							domRunner.runID = Bukkit.getScheduler()
									.scheduleSyncRepeatingTask(
											PVPArena.instance, domRunner, 10 * 20L,
											10 * 20L);
							paRuns.put(loc, domRunner);
						}
					} else {
						// just the owning team is there
						debug.i("    - noone else");
						if (paRuns.containsKey(loc)) {
							debug.i("      - being unclaimed. cancel!");
							// it is being unclaimed
							// cancel task!
							Bukkit.getScheduler()
									.cancelTask(paRuns.get(loc).runID);
							paRuns.remove(loc);
						} else {

							final String team = paFlags.get(loc);

							if (!paTeamLives.containsKey(team)) {
								continue;
							}
							
							// flag claimed! add score!
							reduceLivesCheckEndAndCommit(arena, team);
							
							int max = arena.getArenaConfig().getInt(CFG.GOAL_DOM_LIVES);
							int lives = this.paTeamLives.get(team);
							
							arena.broadcast(Language.parse(MSG.GOAL_DOMINATION_SCORE,
									arena.getTeam(team).getColoredName()
											+ ChatColor.YELLOW, (max-lives)+"/"+max));
						}
					}
					continue;
				}

				debug.i("  - owning team is not there!");
				// owning team is NOT there ==> unclaim!

				if (paRuns.containsKey(loc)) {
					if (paRuns.get(loc).take) {
						debug.i("    - runnable is trying to score, abort");

						Bukkit.getScheduler().cancelTask(paRuns.get(loc).runID);
						paRuns.remove(loc);
					} else {
						debug.i("    - being unclaimed. continue.");
					}
					continue;
				}
				debug.i("    - not yet being unclaimed, do it!");
				// create an unclaim runnable
				final DominationRunnable running = new DominationRunnable(arena,
						false, loc, paFlags.get(loc), this);
				final long interval = 20L * 10;

				running.runID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
						PVPArena.instance, running, interval, interval);
				paRuns.put(loc, running);
			} else {
				// flag not taken
				debug.i("- flag not taken");

				/*
				 * check if a runnable
				 * 	yes
				 * 		check if only that team
				 * 			yes => continue;
				 * 			no => cancel
				 * 	no
				 * 		check if only that team
				 * 			yes => create runnable;
				 * 			no => continue
				 */
				if (paRuns.containsKey(loc)) {

					debug.i("  - being claimed");

					if (teams.size() < 2) {
						debug.i("  - only one team present");
						if (teams.contains(paRuns.get(loc).team)) {
							// just THE team that is claiming => NEXT
							debug.i("  - claiming team present. next!");
							continue;
						}
					}
					debug.i("  - more than one team or another team. cancel claim!");
					// more than THE team that is claiming => cancel!
					Bukkit.getScheduler().cancelTask(paRuns.get(loc).runID);
				} else {
					debug.i("  - not being claimed");
					// not being claimed
					if (teams.size() < 2) {
						debug.i("  - just one team present");
						for (String sName : teams) {
							debug.i("TEAM " + sName + " IS CLAIMING "
									+ loc.toString());
							final ArenaTeam team = arena.getTeam(sName);
							arena.broadcast(Language.parse(
									MSG.GOAL_DOMINATION_CLAIMING,
									team.getColoredName() + ChatColor.YELLOW));

							final DominationRunnable running = new DominationRunnable(
									arena, true, loc, sName, this);
							final long interval = 20L * 10;
							running.runID = Bukkit.getScheduler()
									.scheduleSyncRepeatingTask(
											PVPArena.instance, running,
											interval, interval);
							paRuns.put(loc, running);
						}
					} else {
						debug.i("  - more than one team present. continue!");
					}
				}
			}
		}
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

	private void commit(final Arena arena, final String sTeam, final boolean win) {
		debug.i("[CTF] committing end: " + sTeam);
		debug.i("win: " + win);

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

			ArenaModuleManager
					.announce(
							arena,
							Language.parse(MSG.TEAM_HAS_WON,
									arena.getTeam(winteam).getColor() + "Team "
											+ winteam + ChatColor.YELLOW),
							"WINNER");
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON,
					arena.getTeam(winteam).getColor() + "Team " + winteam
							+ ChatColor.YELLOW));
		}

		paTeamLives.clear();
		new EndRunnable(arena, arena.getArenaConfig().getInt(
				CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitCommand(final CommandSender sender, final String[] args) {
		if (PAA_Region.activeSelections.containsKey(sender.getName())) {
			PAA_Region.activeSelections.remove(sender.getName());
			arena.msg(sender, Language.parse(MSG.GOAL_FLAGS_SET, "flags"));
		} else {

			PAA_Region.activeSelections.put(sender.getName(), arena);
			arena.msg(sender, Language.parse(MSG.GOAL_FLAGS_TOSET, "flags"));
		}
	}

	@Override
	public void commitEnd(final boolean force) {
		debug.i("[DOMINATION]");

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
	public boolean commitSetFlag(final Player player, final Block block) {
		if (block == null || !block.getType().equals(Material.WOOL)) {
			return false;
		}

		if (PVPArena.hasAdminPerms(player)
				|| (PVPArena.hasCreatePerms(player, arena))
				&& (player.getItemInHand() != null)
				&& (player.getItemInHand().getTypeId() == arena
						.getArenaConfig().getInt(CFG.GENERAL_WAND))) {

			final Set<PABlockLocation> flags = SpawnManager.getBlocks(arena,
					"flags");

			if (flags.contains(new PABlockLocation(block.getLocation()))) {
				return false;
			}

			flagName = "flag" + flags.size();

			SpawnManager.setBlock(arena,
					new PABlockLocation(block.getLocation()), flagName);

			arena.msg(player, Language.parse(MSG.GOAL_FLAGS_SET, flagName));
			return true;
		}
		return false;
	}

	@Override
	public PACheck getLives(final PACheck res, final ArenaPlayer aPlayer) {
		if (!res.hasError() && res.getPriority() <= PRIORITY) {
			res.setError(
					this,
					String.valueOf(paTeamLives.containsKey(aPlayer.getArenaTeam()
									.getName()) ? paTeamLives.get(aPlayer
									.getArenaTeam().getName()) : 0));
		}
		return res;
	}

	@Override
	public String guessSpawn(final String place) {
		// no exact match: assume we have multiple spawnpoints
		final Map<Integer, String> locs = new HashMap<Integer, String>();
		int pos = 0;

		debug.i("searching for team spawns: " + place);

		final Map<String, Object> coords = (HashMap<String, Object>) arena
				.getArenaConfig().getYamlConfiguration()
				.getConfigurationSection("spawns").getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(pos++, name);
				debug.i("found match: " + name);
			}
			if (name.startsWith("flag")) {
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
		}
		return false;
	}

	@Override
	public void initate(final Player player) {
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final ArenaTeam team = aPlayer.getArenaTeam();
		if (!paTeamLives.containsKey(team.getName())) {
			paTeamLives.put(aPlayer.getArenaTeam().getName(), arena.getArenaConfig()
					.getInt(CFG.GOAL_DOM_LIVES));

			final Map<String, PALocation> map = SpawnManager.getSpawnMap(arena,
					"flags");
			for (String s : map.keySet()) {
				takeFlag("WHITE", false,
						SpawnManager.getCoords(arena, s));
			}
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
				debug.i("adding team " + team.getName());
				// team is active
				paTeamLives.put(team.getName(),
						arena.getArenaConfig().getInt(CFG.GOAL_DOM_LIVES, 3));
			}
		}
		final Map<String, PALocation> map = SpawnManager.getSpawnMap(arena,
				"flags");
		for (String s : map.keySet()) {
			takeFlag("WHITE", false,
					SpawnManager.getCoords(arena, s));
		}

		final DominationMainRunnable domMainRunner = new DominationMainRunnable(arena, this);
		domMainRunner.rID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, domMainRunner, 3 * 20L, 3 * 20L);
	}

	private boolean reduceLivesCheckEndAndCommit(Arena arena, String team) {

		debug.i("reducing lives of team " + team);
		if (paTeamLives.get(team) != null) {
			final int iLives = paTeamLives.get(team) - 1;
			if (iLives > 0) {
				paTeamLives.put(team, iLives);
			} else {
				paTeamLives.remove(team);
				commit(arena, team, false);
				return true;
			}
		}
		return false;
	}

	@Override
	public void reset(final boolean force) {
		paTeamFlags.clear();
		paTeamLives.clear();
		paRuns.clear();
		paFlags.clear();
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
	public void takeFlag(final String flagColor, final boolean take, final PALocation lBlock) {
		if (take) {
			lBlock.toLocation().getBlock()
			.setData(StringParser.getColorDataFromENUM(flagColor));
		} else {
			lBlock.toLocation().getBlock()
			.setData(StringParser.getColorDataFromENUM("WHITE"));
		}
	}

	private static void takeFlag(final Arena arena, final Location lBlock, final String name) {
		ArenaTeam team = null;
		for (ArenaTeam t : arena.getTeams()) {
			if (t.getName().equals(name)) {
				team = t;
			}
		}
		if (team == null) {
			lBlock.getBlock().setData(
					StringParser.getColorDataFromENUM("WHITE"));
			return;
		}
		lBlock.getBlock().setData(
				StringParser.getColorDataFromENUM(team.getColor().name()));
	}

	@Override
	public Map<String, Double> timedEnd(final Map<String, Double> scores) {
		double score;

		for (ArenaTeam team : arena.getTeams()) {
			score = (paTeamLives.containsKey(team.getName()) ? paTeamLives
					.get(team.getName()) : 0);
			if (scores.containsKey(team)) {
				scores.put(team.getName(), scores.get(team.getName()) + score);
			} else {
				scores.put(team.getName(), score);
			}
		}

		return scores;
	}

	protected class DominationRunnable implements Runnable {
		public final boolean take;
		public final Location loc;
		public int runID = -1;
		private final Arena arena;
		public final String team;
		private final Debug debug = new Debug(39);
		private final GoalDomination domination;

		/**
		 * create a domination runnable
		 * 
		 * @param arena
		 *            the arena we are running in
		 * @param domination
		 */
		public DominationRunnable(final Arena arena, final boolean take, final Location loc2, final String teamName,
				final GoalDomination goal) {
			this.arena = arena;
			this.take = take;
			this.team = teamName;
			this.loc = loc2;
			this.domination = goal;
			debug.i("Domination constructor");
		}

		/**
		 * the run method, commit arena end
		 */
		@Override
		public void run() {
			debug.i("DominationRunnable commiting");
			debug.i("team " + team + ", take: " + take);
			if (take) {
				// claim a flag for the team
				if (domination.paFlags.containsKey(loc)) {
					// PVPArena.instance.getLogger().warning("wtf");
				} else {
					// flag unclaimed! claim!
					debug.i("clag unclaimed. claim!");
					domination.paFlags.put(loc, team);
					// long interval = 20L * 5;

					arena.broadcast(Language.parse(
							MSG.GOAL_DOMINATION_CLAIMED, arena.getTeam(team)
									.getColoredName() + ChatColor.YELLOW));
					GoalDomination.takeFlag(arena, loc, team);
					domination.paFlags.put(loc, team);

					// claim done. end timer
					Bukkit.getScheduler().cancelTask(runID);
					domination.paRuns.remove(loc);
				}
			} else {
				// unclaim
				debug.i("unclaim");
				arena.broadcast(Language.parse(MSG.GOAL_DOMINATION_UNCLAIMING,
						team + ChatColor.YELLOW));
				GoalDomination.takeFlag(arena, loc, "");
				Bukkit.getScheduler().cancelTask(runID);
				domination.paRuns.remove(loc);
				domination.paFlags.remove(loc);
			}
		}
	}

	protected class DominationMainRunnable implements Runnable {
		public int rID = -1;
		private final Arena arena;
		private final Debug debug = new Debug(39);
		private final GoalDomination domination;

		public DominationMainRunnable(Arena arena, GoalDomination goal) {
			this.arena = arena;
			this.domination = goal;
			debug.i("DominationMainRunnable constructor");
		}

		/**
		 * the run method, commit arena end
		 */
		@Override
		public void run() {
			if (!arena.isFightInProgress() || arena.realEndRunner != null) {
				Bukkit.getScheduler().cancelTask(rID);
			}
			domination.checkMove();
		}
	}
	
	protected <V> V get(Map<PALocation, V> map, PALocation find) {
		for (PALocation l : map.keySet()) {
			if (l.getDistance(find) < 1) {
				return map.get(l);
			}
		}
		return null;
	}
}
