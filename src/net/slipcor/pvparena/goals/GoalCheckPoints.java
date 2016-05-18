package net.slipcor.pvparena.goals;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.classes.PASpawn;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.events.PAGoalEvent;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.runnables.EndRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * <pre>
 * Arena Goal class "Domination"
 * </pre>
 *
 * @author slipcor
 */

public class GoalCheckPoints extends ArenaGoal {

    public GoalCheckPoints() {
        super("CheckPoints");
        debug = new Debug(99);
    }

    @Override
    public String version() {
        return PVPArena.instance.getDescription().getVersion();
    }

    private static final int PRIORITY = 13;

    @Override
    public boolean allowsJoinInBattle() {
        return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
    }

    @Override
    public PACheck checkCommand(final PACheck res, final String string) {
        if (res.getPriority() > PRIORITY) {
            return res;
        }

        if ("checkpoint".equalsIgnoreCase(string)) {
            res.setPriority(this, PRIORITY);
        }

        return res;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("checkpoint");
    }

    @Override
    public String checkForMissingSpawns(final Set<String> list) {

        final String team = checkForMissingTeamSpawn(list);
        if (team != null) {
            return team;
        }
        int count = 0;
        for (final String s : list) {
            if (s.startsWith("checkpoint")) {
                count++;
            }
        }
        if (count < 1) {
            return "checkpoint: " + count + " / 1";
        }
        return null;
    }

    @Override
    public PACheck checkJoin(final CommandSender sender, final PACheck res, final String[] args) {
        if (res.getPriority() >= PRIORITY) {
            return res;
        }

        final int maxPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);

        if (maxPlayers > 0 && arena.getFighters().size() >= maxPlayers) {
            res.setError(this, Language.parse(arena, MSG.ERROR_JOIN_ARENA_FULL));
            return res;
        }

        return res;
    }

    /**
     * return a hashset of players names being near a specified location
     *
     * @param loc      the location to check
     * @param distance the distance in blocks
     * @return a set of player names
     */
    private Set<String> checkLocationPresentPlayers(final Location loc, final int distance) {
        final Set<String> result = new HashSet<>();

        for (final ArenaPlayer p : arena.getFighters()) {

            if (p.get().getLocation().distance(loc) > distance) {
                continue;
            }

            result.add(p.getName());
        }

        return result;
    }

    void checkMove() {

        arena.getDebugger().i("------------------");
        arena.getDebugger().i("  GCP checkMove();");
        arena.getDebugger().i("------------------");

        final int checkDistance = arena.getArenaConfig().getInt(
                CFG.GOAL_DOM_CLAIMRANGE);

        for (final PASpawn spawn : SpawnManager.getPASpawnsStartingWith(arena, "checkpoint")) {
            final PALocation paLoc = spawn.getLocation();
            final Set<String> players = checkLocationPresentPlayers(paLoc.toLocation(),
                    checkDistance);

            arena.getDebugger().i("players: " + StringParser.joinSet(players, ", "));

            // players now contains all players near the checkpoint

            if (players.size() < 1) {
                continue;
            }
            int value = Integer.parseInt(spawn.getName().substring(10));
            for (String playerName : players) {
                maybeAddScoreAndBroadCast(playerName, value);
            }

        }
    }

    private void maybeAddScoreAndBroadCast(final String playerName, int checkpoint) {

        if (!getLifeMap().containsKey(playerName)) {
            return;
        }


        final int max = arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_LIVES);

        final int position = max - getLifeMap().get(playerName);

        if (checkpoint == position+1) {
            arena.broadcast(Language.parse(arena, MSG.GOAL_CHECKPOINTS_SCORE,
                    playerName, position + "/" + max));
            reduceLivesCheckEndAndCommit(arena, playerName);
        } else if (checkpoint > position) {
            arena.broadcast(Language.parse(arena, MSG.GOAL_CHECKPOINTS_YOUMISSED,
                    String.valueOf(position + 1), String.valueOf(checkpoint)));
        }

    }

    private void commitWin(final Arena arena, final String playerName) {
        if (arena.realEndRunner != null) {
            arena.getDebugger().i("[CP] already ending");
            return;
        }
        arena.getDebugger().i("[CP] committing end: " + playerName);
        ArenaPlayer winner = null;
        for (final ArenaPlayer player : arena.getFighters()) {
            if (player.getName().equals("playerName")) {
                winner = player;
                continue;
            }
            player.addLosses();
            player.setStatus(Status.LOST);
        }

        if (winner != null) {

            ArenaModuleManager
                    .announce(
                            arena,
                            Language.parse(arena, MSG.PLAYER_HAS_WON,
                                    winner.getName()),
                            "WINNER");
            arena.broadcast(Language.parse(arena, MSG.PLAYER_HAS_WON,
                    winner.getName()));
        }

        getLifeMap().clear();
        new EndRunnable(arena, arena.getArenaConfig().getInt(
                CFG.TIME_ENDCOUNTDOWN));
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        // 0 = checkpoint , [1 = number]

        if (!(sender instanceof Player)) {
            Arena.pmsg(sender, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
            return;
        }

        ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());

        if (args.length < 2 && arena.getFighters().contains(ap)) {
            ap.setTelePass(true);
            int value = arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_LIVES) - getLifeMap().get(ap.getName());
            ap.get().teleport(SpawnManager.getSpawnByExactName(arena, "checkpoint"+value).toLocation());
            ap.setTelePass(false);
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2})) {
            return;
        }
        int value = -1;
        try {
            value = Integer.parseInt(args[1]);
            Math.sqrt(value);
        } catch (Exception e) {
            arena.msg(sender, Language.parse(arena, MSG.ERROR_NOT_NUMERIC, args[1]));
            return;
        }
        Player player = (Player) sender;
        String spawnName = "checkpoint"+value;
        arena.spawnSet(spawnName, new PALocation(player.getLocation()));
        arena.msg(sender, Language.parse(arena, MSG.SPAWN_SET, spawnName));
    }

    @Override
    public void commitEnd(final boolean force) {
        if (arena.realEndRunner != null) {
            arena.getDebugger().i("[CP] already ending");
            return;
        }
        arena.getDebugger().i("[CP]");

        final PAGoalEvent gEvent = new PAGoalEvent(arena, this, "");
        Bukkit.getPluginManager().callEvent(gEvent);

        ArenaPlayer ap = null;

        for (ArenaPlayer aPlayer : arena.getFighters()) {
            if (aPlayer.getStatus() == Status.FIGHT) {
                ap = aPlayer;
                break;
            }
        }

        if (ap != null && !force) {
            ArenaModuleManager.announce(
                    arena,
                    Language.parse(arena, MSG.PLAYER_HAS_WON, ap.getName()), "END");

            ArenaModuleManager.announce(
                    arena,
                    Language.parse(arena, MSG.PLAYER_HAS_WON, ap.getName()), "WINNER");
            arena.broadcast(Language.parse(arena, MSG.PLAYER_HAS_WON, ap.getName()));
        }

        if (ArenaModuleManager.commitEnd(arena, ap.getArenaTeam())) {
            return;
        }
        new EndRunnable(arena, arena.getArenaConfig().getInt(
                CFG.TIME_ENDCOUNTDOWN));
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage("needed points: " +
                arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_LIVES));
        sender.sendMessage("claim range: " +
                arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_CLAIMRANGE));
        sender.sendMessage("tick interval (ticks): " +
                arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_TICKINTERVAL));
    }

    @Override
    public PACheck getLives(final PACheck res, final ArenaPlayer aPlayer) {
        if (res.getPriority() <= PRIORITY + 1000) {
            res.setError(
                    this,
                    String.valueOf(getLifeMap().containsKey(aPlayer.getArenaTeam()
                            .getName()) ? getLifeMap().get(aPlayer
                            .getArenaTeam().getName()) : 0));
        }
        return res;
    }

    @Override
    public boolean hasSpawn(final String string) {
        if (string.startsWith("checkpoint") || string.startsWith("spawn")) {
            return true;
        }
        if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
            for (final ArenaClass aClass : arena.getClasses()) {
                if (string.toLowerCase().contains(aClass.getName().toLowerCase() + "spawn")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void initate(final Player player) {
        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
        if (!getLifeMap().containsKey(aPlayer.getName())) {
            getLifeMap().put(aPlayer.getName(), arena.getArenaConfig()
                    .getInt(CFG.GOAL_CHECKPOINTS_LIVES));
        }
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public void parseStart() {
        getLifeMap().clear();
        for (final ArenaPlayer player : arena.getFighters()) {
            arena.getDebugger().i("adding player " + player.getName());
            getLifeMap().put(player.getName(),
                    arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_LIVES, 3));
        }

        final CheckPointsMainRunnable cpMainRunner = new CheckPointsMainRunnable(arena, this);
        final int tickInterval = arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_TICKINTERVAL);
        cpMainRunner.rID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                PVPArena.instance, cpMainRunner, tickInterval, tickInterval);
    }

    private boolean reduceLivesCheckEndAndCommit(final Arena arena, final String player) {

        arena.getDebugger().i("reducing lives of player " + player);
        if (getLifeMap().get(player) != null) {
            final int iLives = getLifeMap().get(player) - 1;
            if (iLives > 0) {
                getLifeMap().put(player, iLives);
            } else {
                getLifeMap().remove(player);
                commitWin(arena, player);
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset(final boolean force) {
        getLifeMap().clear();
    }

    @Override
    public Map<String, Double> timedEnd(final Map<String, Double> scores) {

        for (final ArenaTeam team : arena.getTeams()) {
            double score = getLifeMap().containsKey(team.getName()) ? getLifeMap()
                    .get(team.getName()) : 0;
            if (scores.containsKey(team.getName())) {
                scores.put(team.getName(), scores.get(team.getName()) + score);
            } else {
                scores.put(team.getName(), score);
            }
        }

        return scores;
    }

    class CheckPointsMainRunnable implements Runnable {
        public int rID = -1;
        private final Arena arena;
        //private final Debug debug = new Debug(39);
        private final GoalCheckPoints goal;

        public CheckPointsMainRunnable(final Arena arena, final GoalCheckPoints goal) {
            this.arena = arena;
            this.goal = goal;
            arena.getDebugger().i("CheckPointsMainRunnable constructor");
        }

        /**
         * the run method, commit arena end
         */
        @Override
        public void run() {
            if (!arena.isFightInProgress() || arena.realEndRunner != null) {
                Bukkit.getScheduler().cancelTask(rID);
            }
            goal.checkMove();
        }
    }
}
