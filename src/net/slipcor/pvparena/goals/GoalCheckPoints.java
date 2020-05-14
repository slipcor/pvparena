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
import org.bukkit.scheduler.BukkitRunnable;

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
        this.debug = new Debug(99);
    }

    @Override
    public String version() {
        return PVPArena.instance.getDescription().getVersion();
    }

    private static final int PRIORITY = 13;

    @Override
    public boolean allowsJoinInBattle() {
        return this.arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
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
        if (!list.contains("spawn")) {
            return "spawn";
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

        final int maxPlayers = this.arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);

        if (maxPlayers > 0 && this.arena.getFighters().size() >= maxPlayers) {
            res.setError(this, Language.parse(this.arena, MSG.ERROR_JOIN_ARENA_FULL));
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

        for (final ArenaPlayer p : this.arena.getFighters()) {
            if (p.get().getLocation().getWorld().getName().equals(loc.getWorld().getName())) {
                if (p.get().getLocation().distance(loc) > distance) {
                    continue;
                }

                result.add(p.getName());
            }
        }

        return result;
    }

    private void checkMove() {

        this.arena.getDebugger().i("------------------");
        this.arena.getDebugger().i("  GCP checkMove();");
        this.arena.getDebugger().i("------------------");

        final int checkDistance = this.arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_CLAIMRANGE);

        for (final PASpawn spawn : SpawnManager.getPASpawnsStartingWith(this.arena, "checkpoint")) {
            final PALocation paLoc = spawn.getLocation();
            final Set<String> players = this.checkLocationPresentPlayers(paLoc.toLocation(),
                    checkDistance);

            this.arena.getDebugger().i("players: " + StringParser.joinSet(players, ", "));

            // players now contains all players near the checkpoint

            if (players.size() < 1) {
                continue;
            }
            int value = Integer.parseInt(spawn.getName().substring(10));
            for (String playerName : players) {
                this.maybeAddScoreAndBroadCast(playerName, value);
            }

        }
    }

    private void maybeAddScoreAndBroadCast(final String playerName, int checkpoint) {

        if (!this.getLifeMap().containsKey(playerName)) {
            return;
        }


        final int max = this.arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_LIVES);

        final int position = max - this.getLifeMap().get(playerName) + 1;

        if (checkpoint == position) {
            this.arena.broadcast(Language.parse(this.arena, MSG.GOAL_CHECKPOINTS_SCORE,
                    playerName, position + "/" + max));
            this.reduceLivesCheckEndAndCommit(this.arena, playerName);
        } else if (checkpoint > position) {
            this.arena.broadcast(Language.parse(this.arena, MSG.GOAL_CHECKPOINTS_YOUMISSED,
                    String.valueOf(position), String.valueOf(checkpoint)));
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

        this.getLifeMap().clear();
        new EndRunnable(arena, arena.getArenaConfig().getInt(
                CFG.TIME_ENDCOUNTDOWN));
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        // 0 = checkpoint , [1 = number]

        if (!(sender instanceof Player)) {
            Arena.pmsg(sender, Language.parse(this.arena, MSG.ERROR_ONLY_PLAYERS));
            return;
        }

        ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
        int cpLives = this.arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_LIVES);

        if (args.length < 2 && this.arena.getFighters().contains(ap)) {
            ap.setTelePass(true);
            int value = cpLives - this.getLifeMap().get(ap.getName());
            if(value == 0) {
                ap.get().teleport(SpawnManager.getSpawnByExactName(this.arena, "spawn").toLocation());
            } else {
                ap.get().teleport(SpawnManager.getSpawnByExactName(this.arena, "checkpoint"+value).toLocation());
            }
            ap.setTelePass(false);
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, this.arena, args, new Integer[]{2})) {
            return;
        }
        int value;
        try {
            value = Integer.parseInt(args[1]);
        } catch (Exception e) {
            this.arena.msg(sender, Language.parse(this.arena, MSG.ERROR_NOT_NUMERIC, args[1]));
            return;
        }
        Player player = (Player) sender;
        String spawnName = "checkpoint"+value;
        if(value > 0 && value <= cpLives) {
            this.arena.spawnSet(spawnName, new PALocation(player.getLocation()));
            this.arena.msg(sender, Language.parse(this.arena, MSG.SPAWN_SET, spawnName));
        } else {
            this.arena.msg(sender, Language.parse(this.arena, MSG.SPAWN_UNKNOWN, spawnName));
        }
    }

    @Override
    public void commitEnd(final boolean force) {
        if (this.arena.realEndRunner != null) {
            this.arena.getDebugger().i("[CP] already ending");
            return;
        }
        this.arena.getDebugger().i("[CP]");

        final PAGoalEvent gEvent = new PAGoalEvent(this.arena, this, "");
        Bukkit.getPluginManager().callEvent(gEvent);

        ArenaPlayer ap = null;

        for (ArenaPlayer aPlayer : this.arena.getFighters()) {
            if (aPlayer.getStatus() == Status.FIGHT) {
                ap = aPlayer;
                break;
            }
        }

        if (ap != null && !force) {
            ArenaModuleManager.announce(
                    this.arena,
                    Language.parse(this.arena, MSG.PLAYER_HAS_WON, ap.getName()), "END");

            ArenaModuleManager.announce(
                    this.arena,
                    Language.parse(this.arena, MSG.PLAYER_HAS_WON, ap.getName()), "WINNER");
            this.arena.broadcast(Language.parse(this.arena, MSG.PLAYER_HAS_WON, ap.getName()));
        }

        if (ArenaModuleManager.commitEnd(this.arena, ap.getArenaTeam())) {
            return;
        }
        new EndRunnable(this.arena, this.arena.getArenaConfig().getInt(CFG.TIME_ENDCOUNTDOWN));
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage("needed points: " +
                this.arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_LIVES));
        sender.sendMessage("claim range: " +
                this.arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_CLAIMRANGE));
        sender.sendMessage("tick interval (ticks): " +
                this.arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_TICKINTERVAL));
    }

    @Override
    public PACheck getLives(final PACheck res, final ArenaPlayer aPlayer) {
        if (res.getPriority() <= PRIORITY + 1000) {
            res.setError(
                    this,
                    String.valueOf(this.getLifeMap().getOrDefault(aPlayer.getArenaTeam().getName(), 0))
            );
        }
        return res;
    }

    @Override
    public boolean hasSpawn(final String string) {
        if (string.startsWith("checkpoint") || string.startsWith("spawn")) {
            return true;
        }
        if (this.arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
            for (final ArenaClass aClass : this.arena.getClasses()) {
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
        if (!this.getLifeMap().containsKey(aPlayer.getName())) {
            this.getLifeMap().put(aPlayer.getName(), this.arena.getArenaConfig()
                    .getInt(CFG.GOAL_CHECKPOINTS_LIVES));
        }
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public void lateJoin(final Player player) {
        this.initate(player);
    }

    @Override
    public void parseStart() {
        this.getLifeMap().clear();
        for (final ArenaPlayer player : this.arena.getFighters()) {
            this.arena.getDebugger().i("adding player " + player.getName());
            this.getLifeMap().put(player.getName(),
                    this.arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_LIVES, 3));
        }

        final CheckPointsMainRunnable cpMainRunner = new CheckPointsMainRunnable(this.arena, this);
        final int tickInterval = this.arena.getArenaConfig().getInt(CFG.GOAL_CHECKPOINTS_TICKINTERVAL);
        cpMainRunner.runTaskTimer(PVPArena.instance, tickInterval, tickInterval);
    }

    private void reduceLivesCheckEndAndCommit(final Arena arena, final String player) {

        arena.getDebugger().i("reducing lives of player " + player);
        if (this.getLifeMap().get(player) != null) {
            final int iLives = this.getLifeMap().get(player) - 1;
            if (iLives > 0) {
                this.getLifeMap().put(player, iLives);
            } else {
                this.getLifeMap().remove(player);
                this.commitWin(arena, player);
            }
        }
    }

    @Override
    public void reset(final boolean force) {
        this.getLifeMap().clear();
    }

    @Override
    public Map<String, Double> timedEnd(final Map<String, Double> scores) {

        for (final ArenaTeam team : this.arena.getTeams()) {
            double score = this.getLifeMap().getOrDefault(team.getName(), 0);
            if (scores.containsKey(team.getName())) {
                scores.put(team.getName(), scores.get(team.getName()) + score);
            } else {
                scores.put(team.getName(), score);
            }
        }

        return scores;
    }

    private class CheckPointsMainRunnable extends BukkitRunnable {
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
            if (!this.arena.isFightInProgress() || this.arena.realEndRunner != null) {
                this.cancel();
            }
            this.goal.checkMove();
        }
    }
}
