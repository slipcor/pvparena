package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAGoalEvent;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>
 * Arena Goal class "Domination"
 * </pre>
 * 
 *
 * 
 * @author slipcor
 */

public class GoalDomination extends ArenaGoal {

	public GoalDomination() {
		super("Domination");
		debug = new Debug(99);
	}

	private Map<Location, String> flagMap = new HashMap<Location, String>();
	private Map<Location, DominationRunnable> runnerMap = new HashMap<Location, DominationRunnable>();

	private String flagName = "";
	private int offset;
	private int announceOffset;

	@Override
	public String version() {
		return PVPArena.instance.getDescription().getVersion();
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

		String team = checkForMissingTeamSpawn(list);
		if (team != null) {
			return team;
		}
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
			res.setError(this, Language.parse(arena, MSG.ERROR_JOIN_ARENA_FULL));
			return res;
		}

		if (args == null || args.length < 1) {
			return res;
		}

		if (!arena.isFreeForAll()) {
			final ArenaTeam team = arena.getTeam(args[0]);

			if (team != null && maxTeamPlayers > 0
						&& team.getTeamMembers().size() >= maxTeamPlayers) {
				res.setError(this, Language.parse(arena, MSG.ERROR_JOIN_TEAM_FULL));
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

		arena.getDebugger().i("------------------");
		arena.getDebugger().i("   checkMove();");
		arena.getDebugger().i("------------------");

		final int checkDistance = arena.getArenaConfig().getInt(
				CFG.GOAL_DOM_CLAIMRANGE);

		for (PABlockLocation paLoc : SpawnManager.getBlocksStartingWith(arena, "flag")) {
			// arena.getDebugger().info("checking location: " + loc.toString());
			
			Location loc = paLoc.toLocation();

			final Set<String> teams = checkLocationPresentTeams(paLoc.toLocation(),
					checkDistance);

			arena.getDebugger().i("teams: " + StringParser.joinSet(teams, ", "));

			// teams now contains all teams near the flag

			if (teams.size() < 1) {
				// arena.getDebugger().info("=> noone there!");
				// no one there
				if (getRunnerMap().containsKey(loc)) {
					arena.getDebugger().i("flag is being (un)claimed! Cancelling!");
					// cancel unclaiming/claiming if noone's near
					Bukkit.getScheduler().cancelTask(getRunnerMap().get(loc).runID);
					getRunnerMap().remove(loc);
				}
				if (getFlagMap().containsKey(loc)) {
					final String team = getFlagMap().get(loc);

					if (!getLifeMap().containsKey(team)) {
						continue;
					}
					
					// flag claimed! add score!
					maybeAddScoreAndBroadCast(team);
				}
				continue;
			}

			// there are actually teams at the flag
			arena.getDebugger().i("=> at least one team is at the flag!");

			if (getFlagMap().containsKey(loc)) {
				// flag is taken. by whom?
				if (teams.contains(getFlagMap().get(loc))) {
					// owning team is there
					arena.getDebugger().i("  - owning team is there");
					if (teams.size() > 1) {
						// another team is there
						arena.getDebugger().i("    - and another one");
						if (getRunnerMap().containsKey(loc)) {
							// it is being unclaimed
							arena.getDebugger().i("      - being unclaimed. continue!");
						} else {
							// unclaim
							arena.getDebugger().i("      - not being unclaimed. do it!");
							final DominationRunnable domRunner = new DominationRunnable(
									arena, false, loc,
									getFlagMap().get(loc), this);
							domRunner.runID = Bukkit.getScheduler()
									.scheduleSyncRepeatingTask(
											PVPArena.instance, domRunner, 10 * 20L,
											10 * 20L);
							getRunnerMap().put(loc, domRunner);
						}
					} else {
						// just the owning team is there
						arena.getDebugger().i("    - noone else");
						if (getRunnerMap().containsKey(loc)) {
							arena.getDebugger().i("      - being unclaimed. cancel!");
							// it is being unclaimed
							// cancel task!
							Bukkit.getScheduler()
									.cancelTask(getRunnerMap().get(loc).runID);
							getRunnerMap().remove(loc);
						} else {

							final String team = getFlagMap().get(loc);

							if (!getLifeMap().containsKey(team)) {
								continue;
							}

							maybeAddScoreAndBroadCast(team);
						}
					}
					continue;
				}

				arena.getDebugger().i("  - owning team is not there!");
				// owning team is NOT there ==> unclaim!

				if (getRunnerMap().containsKey(loc)) {
					if (getRunnerMap().get(loc).take) {
						arena.getDebugger().i("    - runnable is trying to score, abort");

						Bukkit.getScheduler().cancelTask(getRunnerMap().get(loc).runID);
						getRunnerMap().remove(loc);
					} else {
						arena.getDebugger().i("    - being unclaimed. continue.");
					}
					continue;
				}
				arena.getDebugger().i("    - not yet being unclaimed, do it!");
				// create an unclaim runnable
				final DominationRunnable running = new DominationRunnable(arena,
						false, loc, getFlagMap().get(loc), this);
				final long interval = 20L * 10;

				running.runID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
						PVPArena.instance, running, interval, interval);
				getRunnerMap().put(loc, running);
			} else {
				// flag not taken
				arena.getDebugger().i("- flag not taken");

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
				if (getRunnerMap().containsKey(loc)) {

					arena.getDebugger().i("  - being claimed");

					if (teams.size() < 2) {
						arena.getDebugger().i("  - only one team present");
						if (teams.contains(getRunnerMap().get(loc).team)) {
							// just THE team that is claiming => NEXT
							arena.getDebugger().i("  - claiming team present. next!");
							continue;
						}
					}
					arena.getDebugger().i("  - more than one team or another team. cancel claim!");
					// more than THE team that is claiming => cancel!
					Bukkit.getScheduler().cancelTask(getRunnerMap().get(loc).runID);
				} else {
					arena.getDebugger().i("  - not being claimed");
					// not being claimed
					if (teams.size() < 2) {
						arena.getDebugger().i("  - just one team present");
						for (String sName : teams) {
							arena.getDebugger().i("TEAM " + sName + " IS CLAIMING "
									+ loc.toString());
							final ArenaTeam team = arena.getTeam(sName);
							arena.broadcast(Language.parse(arena,
									MSG.GOAL_DOMINATION_CLAIMING,
									team.getColoredName() + ChatColor.YELLOW));

							final DominationRunnable running = new DominationRunnable(
									arena, true, loc, sName, this);
							final long interval = 20L * 10;
							running.runID = Bukkit.getScheduler()
									.scheduleSyncRepeatingTask(
											PVPArena.instance, running,
											interval, interval);
							getRunnerMap().put(loc, running);
						}
					} else {
						arena.getDebugger().i("  - more than one team present. continue!");
					}
				}
			}
		}
	}

	private void maybeAddScoreAndBroadCast(String team) {
		if (arena.getArenaConfig().getBoolean(CFG.GOAL_DOM_ONLYWHENMORE)) {
			Map<String, Integer> claimed = new HashMap<String, Integer>();
			for (String s : this.getFlagMap().values()) {
				final int toAdd;
				if (claimed.containsKey(s)) {
					toAdd = claimed.get(s)+1;
				} else {
					toAdd = 1;
				}
				claimed.put(s, toAdd);
			}
			for (String otherTeam : claimed.keySet()) {
				if (otherTeam.equals(team)) {
					continue;
				}
				if (claimed.get(otherTeam) >= claimed.get(team)) {
					return;
				}
			}
		}
		
		reduceLivesCheckEndAndCommit(arena, team);
		
		int max = arena.getArenaConfig().getInt(CFG.GOAL_DOM_LIVES);
		if (!getLifeMap().containsKey(team)) {
			return;
		}
		
		offset = ++offset%announceOffset;
		
		if (offset != 0) {
			return;
		}
		
		int lives = this.getLifeMap().get(team);
		
		arena.broadcast(Language.parse(arena, MSG.GOAL_DOMINATION_SCORE,
				arena.getTeam(team).getColoredName()
						+ ChatColor.YELLOW, (max-lives)+"/"+max));
	}

	@Override
	public PACheck checkSetBlock(final PACheck res, final Player player, final Block block) {

		if (res.getPriority() > PRIORITY
				|| !PAA_Region.activeSelections.containsKey(player.getName())) {
			return res;
		}
		if (block == null || !block.getType().equals(Material.WOOL)) {
			return res;
		}
		res.setPriority(this, PRIORITY); // success :)

		return res;
	}

	private void commit(final Arena arena, final String sTeam, final boolean win) {
		if (arena.realEndRunner != null) {
			arena.getDebugger().i("[DOM] already ending");
			return;
		}
		arena.getDebugger().i("[DOM] committing end: " + sTeam);
		arena.getDebugger().i("win: " + win);

		String winteam = sTeam;

		for (ArenaTeam team : arena.getTeams()) {
			if (team.getName().equals(sTeam) == win) {
				continue;
			}
			for (ArenaPlayer ap : team.getTeamMembers()) {

				ap.addLosses();
				/*
				arena.tpPlayerToCoordName(ap.get(), "spectator");
				ap.setTelePass(false);
				*/

				ap.setStatus(Status.LOST);
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
							Language.parse(arena, MSG.TEAM_HAS_WON,
									arena.getTeam(winteam).getColor()
											+ winteam + ChatColor.YELLOW),
							"WINNER");
			arena.broadcast(Language.parse(arena, MSG.TEAM_HAS_WON,
					arena.getTeam(winteam).getColor() + winteam
							+ ChatColor.YELLOW));
		}

		getLifeMap().clear();
		new EndRunnable(arena, arena.getArenaConfig().getInt(
				CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitCommand(final CommandSender sender, final String[] args) {
		if (PAA_Region.activeSelections.containsKey(sender.getName())) {
			PAA_Region.activeSelections.remove(sender.getName());
			arena.msg(sender, Language.parse(arena, MSG.GOAL_FLAGS_SET, "flags"));
		} else {

			PAA_Region.activeSelections.put(sender.getName(), arena);
			arena.msg(sender, Language.parse(arena, MSG.GOAL_FLAGS_TOSET, "flags"));
		}
	}

	@Override
	public void commitEnd(final boolean force) {
		if (arena.realEndRunner != null) {
			arena.getDebugger().i("[DOMINATION] already ending");
			return;
		}
		arena.getDebugger().i("[DOMINATION]");

		PAGoalEvent gEvent = new PAGoalEvent(arena, this, "");
		Bukkit.getPluginManager().callEvent(gEvent);
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
					Language.parse(arena, MSG.TEAM_HAS_WON, aTeam.getColor()
							+ aTeam.getName() + ChatColor.YELLOW), "END");

			ArenaModuleManager.announce(
					arena,
					Language.parse(arena, MSG.TEAM_HAS_WON, aTeam.getColor()
							+ aTeam.getName() + ChatColor.YELLOW), "WINNER");
			arena.broadcast(Language.parse(arena, MSG.TEAM_HAS_WON, aTeam.getColor()
					+ aTeam.getName() + ChatColor.YELLOW));
		}

		if (ArenaModuleManager.commitEnd(arena, aTeam)) {
			return;
		}
		new EndRunnable(arena, arena.getArenaConfig().getInt(
				CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public boolean commitSetFlag(final Player player, final Block block) {

		if (PVPArena.hasAdminPerms(player)
				|| (PVPArena.hasCreatePerms(player, arena))
				&& (player.getItemInHand() != null)
				&& (player.getItemInHand().getTypeId() == arena
						.getArenaConfig().getInt(CFG.GENERAL_WAND))) {

			final Set<PABlockLocation> flags = SpawnManager.getBlocksStartingWith(arena,
					"flag");

			if (flags.contains(new PABlockLocation(block.getLocation()))) {
				return false;
			}

			flagName = "flag" + flags.size();

			SpawnManager.setBlock(arena,
					new PABlockLocation(block.getLocation()), flagName);

			arena.msg(player, Language.parse(arena, MSG.GOAL_FLAGS_SET, flagName));
			return true;
		}
		return false;
	}
	
	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("needed points: " + 
				arena.getArenaConfig().getInt(CFG.GOAL_DOM_LIVES));
		sender.sendMessage("claim range: " + 
				arena.getArenaConfig().getInt(CFG.GOAL_DOM_CLAIMRANGE));
	}

	private Map<Location, String> getFlagMap() {
		if (flagMap == null) {
			flagMap = new HashMap<Location, String>();
		}
		return flagMap;
	}
	
	@Override
	public PACheck getLives(final PACheck res, final ArenaPlayer aPlayer) {
		if (res.getPriority() <= PRIORITY+1000) {
			res.setError(
					this,
					String.valueOf(getLifeMap().containsKey(aPlayer.getArenaTeam()
									.getName()) ? getLifeMap().get(aPlayer
									.getArenaTeam().getName()) : 0));
		}
		return res;
	}

	private Map<Location, DominationRunnable> getRunnerMap() {
		if (runnerMap == null) {
			runnerMap = new HashMap<Location, DominationRunnable>();
		}
		return runnerMap;
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
							aClass.getName().toLowerCase() + "spawn")) {
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
		final ArenaTeam team = aPlayer.getArenaTeam();
		if (!getLifeMap().containsKey(team.getName())) {
			getLifeMap().put(aPlayer.getArenaTeam().getName(), arena.getArenaConfig()
					.getInt(CFG.GOAL_DOM_LIVES));

			final Set<PABlockLocation> spawns = SpawnManager.getBlocksStartingWith(arena, "flag");
			for (PABlockLocation spawn : spawns) {
				takeFlag("WHITE", false, spawn);
			}
		}
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public void parseStart() {
		getLifeMap().clear();
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().size() > 0) {
				arena.getDebugger().i("adding team " + team.getName());
				// team is active
				getLifeMap().put(team.getName(),
						arena.getArenaConfig().getInt(CFG.GOAL_DOM_LIVES, 3));
			}
		}
		final Set<PABlockLocation> spawns = SpawnManager.getBlocksStartingWith(arena, "flag");
		for (PABlockLocation spawn : spawns) {
			takeFlag("WHITE", false, spawn);
		}

		final DominationMainRunnable domMainRunner = new DominationMainRunnable(arena, this);
        final int tickInterval = arena.getArenaConfig().getInt(CFG.GOAL_DOM_TICKINTERVAL);
		domMainRunner.rID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, domMainRunner, tickInterval, tickInterval);
		
		announceOffset = arena.getArenaConfig().getInt(CFG.GOAL_DOM_ANNOUNCEOFFSET);
		offset = 0;
	}

	private boolean reduceLivesCheckEndAndCommit(Arena arena, String team) {

		arena.getDebugger().i("reducing lives of team " + team);
		if (getLifeMap().get(team) != null) {
			final int iLives = getLifeMap().get(team) - arena.getArenaConfig().getInt(CFG.GOAL_DOM_TICKREWARD);
			if (iLives > 0) {
				getLifeMap().put(team, iLives);
			} else {
				getLifeMap().remove(team);
				commit(arena, team, true);
				return true;
			}
		}
		return false;
	}

	@Override
	public void reset(final boolean force) {
		getLifeMap().clear();
		getRunnerMap().clear();
		getFlagMap().clear();
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
			arena.getDebugger().i("no teams defined, adding custom red and blue!");
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
	 * @param paBlockLocation
	 *            the location to take/reset
	 */
	public void takeFlag(final String flagColor, final boolean take, final PABlockLocation paBlockLocation) {
		if (take) {
			paBlockLocation.toLocation().getBlock()
			.setData(StringParser.getColorDataFromENUM(flagColor));
		} else {
			paBlockLocation.toLocation().getBlock()
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
			score = (getLifeMap().containsKey(team.getName()) ? getLifeMap()
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
		//private final Debug debug = new Debug(39);
		private final GoalDomination domination;

		/**
		 * create a domination runnable
		 * 
		 * @param arena
		 *            the arena we are running in
		 */
		public DominationRunnable(final Arena arena, final boolean take, final Location loc2, final String teamName,
				final GoalDomination goal) {
			this.arena = arena;
			this.take = take;
			this.team = teamName;
			this.loc = loc2;
			this.domination = goal;
			arena.getDebugger().i("Domination constructor");
		}

		/**
		 * the run method, commit arena end
		 */
		@Override
		public void run() {
			arena.getDebugger().i("DominationRunnable commiting");
			arena.getDebugger().i("team " + team + ", take: " + take);
			if (take) {
				// claim a flag for the team
				if (domination.getFlagMap().containsKey(loc)) {
					// PVPArena.instance.getLogger().warning("wtf");
				} else {
					// flag unclaimed! claim!
					arena.getDebugger().i("clag unclaimed. claim!");
					domination.getFlagMap().put(loc, team);
					// long interval = 20L * 5;

					arena.broadcast(Language.parse(arena,
							MSG.GOAL_DOMINATION_CLAIMED, arena.getTeam(team)
									.getColoredName() + ChatColor.YELLOW));
					GoalDomination.takeFlag(arena, loc, team);
					domination.getFlagMap().put(loc, team);

					// claim done. end timer
					Bukkit.getScheduler().cancelTask(runID);
					domination.getRunnerMap().remove(loc);
				}
			} else {
				// unclaim
				arena.getDebugger().i("unclaim");
				arena.broadcast(Language.parse(arena, MSG.GOAL_DOMINATION_UNCLAIMING,
						team + ChatColor.YELLOW));
				GoalDomination.takeFlag(arena, loc, "");
				Bukkit.getScheduler().cancelTask(runID);
				domination.getRunnerMap().remove(loc);
				domination.getFlagMap().remove(loc);
			}
		}
	}

	protected class DominationMainRunnable implements Runnable {
		public int rID = -1;
		private final Arena arena;
		//private final Debug debug = new Debug(39);
		private final GoalDomination domination;

		public DominationMainRunnable(Arena arena, GoalDomination goal) {
			this.arena = arena;
			this.domination = goal;
			arena.getDebugger().i("DominationMainRunnable constructor");
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
}
