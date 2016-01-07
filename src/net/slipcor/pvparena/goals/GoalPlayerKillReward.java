package net.slipcor.pvparena.goals;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.events.PAGoalEvent;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * <pre>
 * Arena Goal class "PlayerKillreward"
 * </pre>
 * <p/>
 * This will feature several ways of altering player rewards
 * <p/>
 * get better gear until you reached the final step and then win
 *
 * @author slipcor
 */

public class GoalPlayerKillReward extends ArenaGoal {
    public GoalPlayerKillReward() {
        super("PlayerKillReward");
        debug = new Debug(102);
    }

    private Map<Integer, ItemStack[]> itemMap;

    private EndRunnable endRunner;

    @Override
    public String version() {
        return PVPArena.instance.getDescription().getVersion();
    }

    private static final int PRIORITY = 6;

    @Override
    public boolean allowsJoinInBattle() {
        return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
    }

    @Override
    public PACheck checkCommand(final PACheck res, final String string) {
        if (res.getPriority() < PRIORITY
                && "killrewards".equalsIgnoreCase(string)
                || "!kr".equalsIgnoreCase(string)) {
            res.setPriority(this, PRIORITY);
        }
        return res;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("killrewards");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!kr");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"{int}", "remove"});
        return result;
    }

    @Override
    public PACheck checkEnd(final PACheck res) {
        if (res.getPriority() > PRIORITY) {
            return res;
        }

        if (!arena.isFreeForAll()) {
            final int count = TeamManager.countActiveTeams(arena);

            if (count <= 1) {
                res.setPriority(this, PRIORITY); // yep. only one team left. go!
            }
            return res;
        }

        final int count = getLifeMap().size();

        if (count <= 1) {
            res.setPriority(this, PRIORITY); // yep. only one player left. go!
        }
        if (count == 0) {
            res.setError(this, "");
        }

        return res;
    }

    @Override
    public String checkForMissingSpawns(final Set<String> list) {
        if (!arena.isFreeForAll()) {
            return checkForMissingTeamSpawn(list);
        }

        return checkForMissingSpawn(list);
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
                res.setError(this, Language.parse(arena, MSG.ERROR_JOIN_TEAM_FULL, team.getName()));
                return res;
            }
        }

        res.setPriority(this, PRIORITY);
        return res;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2,
                3})) {
            return;
        }

        // /pa [arena] !kr [number] {remove}

        final int value;

        try {
            value = Integer.parseInt(args[1]);
        } catch (final Exception e) {
            arena.msg(sender, Language.parse(arena, MSG.ERROR_NOT_NUMERIC, args[1]));
            return;
        }
        if (args.length > 2) {
            getItemMap().remove(value);
            arena.msg(sender,
                    Language.parse(arena, MSG.GOAL_KILLREWARD_REMOVED, args[1]));
        } else {
            if (!(sender instanceof Player)) {
                Arena.pmsg(sender, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
                return;
            }
            final Player player = (Player) sender;
            final String contents = StringParser.getStringFromItemStacks(player
                    .getInventory().getArmorContents())
                    + ','
                    + StringParser.getStringFromItemStacks(player.getInventory()
                    .getContents());

            getItemMap().put(value, StringParser.getItemStacksFromString(contents));
            arena.msg(sender, Language.parse(arena, MSG.GOAL_KILLREWARD_ADDED,
                    args[1], contents));

        }

        saveItems();
    }

    @Override
    public void commitEnd(final boolean force) {
        if (endRunner != null) {
            return;
        }
        if (arena.realEndRunner != null) {
            arena.getDebugger().i("[PKW] already ending");
            return;
        }
        final PAGoalEvent gEvent = new PAGoalEvent(arena, this, "");
        Bukkit.getPluginManager().callEvent(gEvent);

        for (final ArenaTeam team : arena.getTeams()) {
            for (final ArenaPlayer ap : team.getTeamMembers()) {
                if (ap.getStatus() != Status.FIGHT) {
                    continue;
                }

                if (arena.isFreeForAll()) {
                    ArenaModuleManager.announce(arena,
                            Language.parse(arena, MSG.PLAYER_HAS_WON, ap.getName()),
                            "END");

                    ArenaModuleManager.announce(arena,
                            Language.parse(arena, MSG.PLAYER_HAS_WON, ap.getName()),
                            "WINNER");

                    arena.broadcast(Language.parse(arena, MSG.PLAYER_HAS_WON,
                            ap.getName()));
                } else {
                    ArenaModuleManager.announce(
                            arena,
                            Language.parse(arena, MSG.TEAM_HAS_WON,
                                    team.getColoredName()), "END");

                    ArenaModuleManager.announce(
                            arena,
                            Language.parse(arena, MSG.TEAM_HAS_WON,
                                    team.getColoredName()), "WINNER");

                    arena.broadcast(Language.parse(arena, MSG.TEAM_HAS_WON,
                            team.getColoredName()));
                    break;
                }
            }

            if (ArenaModuleManager.commitEnd(arena, team)) {
                return;
            }
        }
        endRunner = new EndRunnable(arena, arena.getArenaConfig().getInt(
                CFG.TIME_ENDCOUNTDOWN));
    }

    @Override
    public void parsePlayerDeath(final Player player, final EntityDamageEvent event) {
        if (!getLifeMap().containsKey(player.getName())) {
            return;
        }
        if (arena.getArenaConfig().getBoolean(CFG.GOAL_PLAYERKILLREWARD_GRADUALLYDOWN)) {
            int lives = getLifeMap().get(player.getName());
            lives++;
            getLifeMap().put(player.getName(), lives);
        } else {
            getLifeMap().put(player.getName(), getMaxInt());
        }


        class ResetRunnable implements Runnable {
            private final Player player;

            @Override
            public void run() {
                reset(player);
            }

            ResetRunnable(final Player player) {
                this.player = player;
            }

            private void reset(final Player player) {
                if (!getLifeMap().containsKey(player.getName())) {
                    return;
                }

                final int iLives = getLifeMap().get(player.getName());
                if (ArenaPlayer.parsePlayer(player.getName()).getStatus() != Status.FIGHT) {
                    return;
                }
                if (!arena.getArenaConfig().getBoolean(CFG.GOAL_PLAYERKILLREWARD_ONLYGIVE)) {
                    InventoryManager.clearInventory(player);
                }
                if (getItemMap().containsKey(iLives)) {
                    ArenaClass.equip(player, getItemMap().get(iLives));
                } else {
                    ArenaPlayer.parsePlayer(player.getName()).getArenaClass()
                            .equip(player);
                }
            }

        }
        Bukkit.getScheduler().runTaskLater(PVPArena.instance,
                new ResetRunnable(player), 4L);
        final Player killer = player.getKiller();

        if (killer == null) {
            return;
        }

        int iLives = getLifeMap().get(killer.getName());
        arena.getDebugger().i("kills to go for " + killer.getName() + ": " + iLives, killer);
        if (iLives <= 1) {
            // player has won!
            final PAGoalEvent gEvent = new PAGoalEvent(arena, this, "trigger:" + killer.getName(), "playerKill:" + killer.getName() + ':' + player.getName(), "playerDeath:" + player.getName());
            Bukkit.getPluginManager().callEvent(gEvent);
            final Set<ArenaPlayer> plrs = new HashSet<>();
            for (final ArenaPlayer ap : arena.getFighters()) {
                if (ap.getName().equals(killer.getName())) {
                    continue;
                }
                plrs.add(ap);
            }
            for (final ArenaPlayer ap : plrs) {
                getLifeMap().remove(ap.getName());
				/*
				arena.getDebugger().i("faking player death", ap.get());
				arena.removePlayer(ap.get(), CFG.TP_LOSE.toString(), true,
						false);*/

                ap.setStatus(Status.LOST);
                ap.addLosses();

                //PlayerState.fullReset(arena, ap.get());
            }

            if (ArenaManager.checkAndCommit(arena, false)) {
                return;
            }
            PACheck.handleEnd(arena, false);
        } else {
            final PAGoalEvent gEvent = new PAGoalEvent(arena, this, "playerKill:" + killer.getName() + ':' + player.getName(), "playerDeath:" + player.getName());
            Bukkit.getPluginManager().callEvent(gEvent);
            iLives--;
            getLifeMap().put(killer.getName(), iLives);
            Bukkit.getScheduler().runTaskLater(PVPArena.instance,
                    new ResetRunnable(killer), 4L);
        }
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        for (final int i : getItemMap().keySet()) {
            final ItemStack[] items = getItemMap().get(i);
            sender.sendMessage("kill #" + i + ": " + StringParser.getStringFromItemStacks(items));
        }
    }

    private Map<Integer, ItemStack[]> getItemMap() {
        if (itemMap == null) {
            itemMap = new HashMap<>();
        }
        return itemMap;
    }

    @Override
    public boolean hasSpawn(final String string) {
        if (arena.isFreeForAll()) {

            if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
                for (final ArenaClass aClass : arena.getClasses()) {
                    if (string.toLowerCase().startsWith(
                            aClass.getName().toLowerCase() + "spawn")) {
                        return true;
                    }
                }
            }
            return string.toLowerCase().startsWith("spawn");
        }
        for (final String teamName : arena.getTeamNames()) {
            if (string.toLowerCase().startsWith(
                    teamName.toLowerCase() + "spawn")) {
                return true;
            }

            if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
                for (final ArenaClass aClass : arena.getClasses()) {
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
        getLifeMap().put(player.getName(), getMaxInt());
    }

    private int getMaxInt() {
        int max = 0;
        for (final int i : getItemMap().keySet()) {
            max = Math.max(max, i);
        }
        return max + 1;
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public void parseLeave(final Player player) {
        if (player == null) {
            PVPArena.instance.getLogger().warning(
                    getName() + ": player NULL");
            return;
        }
        if (getLifeMap().containsKey(player.getName())) {
            getLifeMap().remove(player.getName());
        }
    }

    @Override
    public void parseStart() {
        for (final ArenaTeam team : arena.getTeams()) {
            for (final ArenaPlayer ap : team.getTeamMembers()) {
                getLifeMap().put(ap.getName(), getMaxInt());
            }
        }
    }

    @Override
    public void reset(final boolean force) {
        endRunner = null;
        getLifeMap().clear();
    }

    @Override
    public void setDefaults(final YamlConfiguration config) {
        if (!arena.isFreeForAll()) {
            if (config.get("teams.free") != null) {
                config.set("teams", null);
            }
            if (config.get("teams") == null) {
                arena.getDebugger().i("no teams defined, adding custom red and blue!");
                config.addDefault("teams.red", ChatColor.RED.name());
                config.addDefault("teams.blue", ChatColor.BLUE.name());
            }
            if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)
                    && config.get("flagColors") == null) {
                arena.getDebugger().i("no flagheads defined, adding white and black!");
                config.addDefault("flagColors.red", "WHITE");
                config.addDefault("flagColors.blue", "BLACK");
            }
        }


        final ConfigurationSection cs = (ConfigurationSection) config
                .get("goal.playerkillrewards");

        if (cs != null) {
            for (final String line : cs.getKeys(false)) {
                try {
                    getItemMap().put(Integer.parseInt(line.substring(2)), StringParser
                            .getItemStacksFromString(cs.getString(line)));
                } catch (final Exception e) {
                }
            }
        }

        if (getItemMap().size() < 1) {

            getItemMap().put(5,
                    StringParser.getItemStacksFromString("298,299,300,301,268")); // leather
            getItemMap().put(4,
                    StringParser.getItemStacksFromString("302,303,304,305,272")); // chain
            getItemMap().put(3,
                    StringParser.getItemStacksFromString("314,315,316,317,267")); // gold
            getItemMap().put(2,
                    StringParser.getItemStacksFromString("306,307,308,309,276")); // iron
            getItemMap().put(1,
                    StringParser.getItemStacksFromString("310,311,312,313,276")); // diamond

            saveItems();
        }
    }

    private void saveItems() {
        for (final int i : getItemMap().keySet()) {
            arena.getArenaConfig().setManually("goal.playerkillrewards.kr" + i,
                    StringParser.getStringFromItemStacks(getItemMap().get(i)));
        }
        arena.getArenaConfig().save();
    }

    @Override
    public Map<String, Double> timedEnd(final Map<String, Double> scores) {

        for (final ArenaPlayer ap : arena.getFighters()) {
            double score = getMaxInt()
                    - (getLifeMap().containsKey(ap.getName()) ? getLifeMap()
                    .get(ap.getName()) : 0);
            if (scores.containsKey(ap.getName())) {
                scores.put(ap.getName(), scores.get(ap.getName()) + score);
            } else {
                scores.put(ap.getName(), score);
            }
        }

        return scores;
    }

    @Override
    public void unload(final Player player) {
        getLifeMap().remove(player.getName());
    }
}
