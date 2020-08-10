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
import net.slipcor.pvparena.events.PAGoalEvent;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static net.slipcor.pvparena.core.Utils.getSerializableItemStacks;

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
        this.debug = new Debug(102);
    }

    private Map<Integer, ItemStack[][]> itemMapCubed;

    private EndRunnable endRunner;

    @Override
    public String version() {
        return PVPArena.instance.getDescription().getVersion();
    }

    private static final int PRIORITY = 6;

    @Override
    public boolean allowsJoinInBattle() {
        return this.arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
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

        if (!this.arena.isFreeForAll()) {
            final int count = TeamManager.countActiveTeams(this.arena);

            if (count <= 1) {
                res.setPriority(this, PRIORITY); // yep. only one team left. go!
            }
            return res;
        }

        final int count = this.getLifeMap().size();

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
        if (!this.arena.isFreeForAll()) {
            return this.checkForMissingTeamSpawn(list);
        }

        return this.checkForMissingSpawn(list);
    }

    @Override
    public PACheck checkJoin(final CommandSender sender, final PACheck res, final String[] args) {
        if (res.getPriority() >= PRIORITY) {
            return res;
        }

        final int maxPlayers = this.arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);
        final int maxTeamPlayers = this.arena.getArenaConfig().getInt(
                CFG.READY_MAXTEAMPLAYERS);

        if (maxPlayers > 0 && this.arena.getFighters().size() >= maxPlayers) {
            res.setError(this, Language.parse(this.arena, MSG.ERROR_JOIN_ARENA_FULL));
            return res;
        }

        if (args == null || args.length < 1) {
            return res;
        }

        if (!this.arena.isFreeForAll()) {
            final ArenaTeam team = this.arena.getTeam(args[0]);

            if (team != null && maxTeamPlayers > 0
                    && team.getTeamMembers().size() >= maxTeamPlayers) {
                res.setError(this, Language.parse(this.arena, MSG.ERROR_JOIN_TEAM_FULL, team.getName()));
                return res;
            }
        }

        res.setPriority(this, PRIORITY);
        return res;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        if (!AbstractArenaCommand.argCountValid(sender, this.arena, args, new Integer[]{2,
                3})) {
            return;
        }

        // /pa [arena] !kr [number] {remove}

        final int value;

        try {
            value = Integer.parseInt(args[1]);
        } catch (final Exception e) {
            this.arena.msg(sender, Language.parse(this.arena, MSG.ERROR_NOT_NUMERIC, args[1]));
            return;
        }
        if (args.length > 2) {
            this.getItemMap().remove(value);
            this.arena.msg(sender,
                    Language.parse(this.arena, MSG.GOAL_KILLREWARD_REMOVED, args[1]));
        } else {
            if (!(sender instanceof Player)) {
                Arena.pmsg(sender, Language.parse(this.arena, MSG.ERROR_ONLY_PLAYERS));
                return;
            }
            final Player player = (Player) sender;

            ItemStack[][] content = new ItemStack[][]{
                    player.getInventory().getStorageContents(),
                    new ItemStack[]{player.getInventory().getItemInOffHand()},
                    player.getInventory().getArmorContents()
            };

            this.getItemMap().put(value, content);
            this.arena.msg(sender, Language.parse(this.arena, MSG.GOAL_KILLREWARD_ADDED,
                    args[1]));

        }

        this.saveItems();
    }

    @Override
    public void commitEnd(final boolean force) {
        if (this.endRunner != null) {
            return;
        }
        if (this.arena.realEndRunner != null) {
            this.arena.getDebugger().i("[PKW] already ending");
            return;
        }
        final PAGoalEvent gEvent = new PAGoalEvent(this.arena, this, "");
        Bukkit.getPluginManager().callEvent(gEvent);

        for (final ArenaTeam team : this.arena.getTeams()) {
            for (final ArenaPlayer ap : team.getTeamMembers()) {
                if (ap.getStatus() != Status.FIGHT) {
                    continue;
                }

                if (this.arena.isFreeForAll()) {
                    ArenaModuleManager.announce(this.arena,
                            Language.parse(this.arena, MSG.PLAYER_HAS_WON, ap.getName()),
                            "END");

                    ArenaModuleManager.announce(this.arena,
                            Language.parse(this.arena, MSG.PLAYER_HAS_WON, ap.getName()),
                            "WINNER");

                    this.arena.broadcast(Language.parse(this.arena, MSG.PLAYER_HAS_WON,
                            ap.getName()));
                } else {
                    ArenaModuleManager.announce(
                            this.arena,
                            Language.parse(this.arena, MSG.TEAM_HAS_WON,
                                    team.getColoredName()), "END");

                    ArenaModuleManager.announce(
                            this.arena,
                            Language.parse(this.arena, MSG.TEAM_HAS_WON,
                                    team.getColoredName()), "WINNER");

                    this.arena.broadcast(Language.parse(this.arena, MSG.TEAM_HAS_WON,
                            team.getColoredName()));
                    break;
                }
            }

            if (ArenaModuleManager.commitEnd(this.arena, team)) {
                return;
            }
        }
        this.endRunner = new EndRunnable(this.arena, this.arena.getArenaConfig().getInt(
                CFG.TIME_ENDCOUNTDOWN));
    }

    @Override
    public void parsePlayerDeath(final Player player, final EntityDamageEvent event) {
        if (!this.getLifeMap().containsKey(player.getName())) {
            return;
        }
        if (!this.arena.getArenaConfig().getBoolean(CFG.GOAL_PLAYERKILLREWARD_GRADUALLYDOWN)) {
            this.getLifeMap().put(player.getName(), this.getDefaultRemainingKills());
        }


        class ResetRunnable implements Runnable {
            private final Player player;

            @Override
            public void run() {
                this.reset(this.player);
            }

            ResetRunnable(final Player player) {
                this.player = player;
            }

            private void reset(final Player player) {
                if (!GoalPlayerKillReward.this.getLifeMap().containsKey(player.getName())) {
                    return;
                }

                final int iLives = GoalPlayerKillReward.this.getLifeMap().get(player.getName());
                if (ArenaPlayer.parsePlayer(player.getName()).getStatus() != Status.FIGHT) {
                    return;
                }
                if (!GoalPlayerKillReward.this.arena.getArenaConfig().getBoolean(CFG.GOAL_PLAYERKILLREWARD_ONLYGIVE)) {
                    InventoryManager.clearInventory(player);
                }
                if (GoalPlayerKillReward.this.getItemMap().containsKey(iLives)) {
                    ArenaClass.equip(player, GoalPlayerKillReward.this.getItemMap().get(iLives));
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

        int iLives = this.getLifeMap().get(killer.getName());
        this.arena.getDebugger().i("kills to go for " + killer.getName() + ": " + iLives, killer);
        if (iLives <= 1) {
            // player has won!
            final PAGoalEvent gEvent = new PAGoalEvent(this.arena, this, "trigger:" + killer.getName(), "playerKill:" + killer.getName() + ':' + player.getName(), "playerDeath:" + player.getName());
            Bukkit.getPluginManager().callEvent(gEvent);
            final Set<ArenaPlayer> plrs = new HashSet<>();
            for (final ArenaPlayer ap : this.arena.getFighters()) {
                if (ap.getName().equals(killer.getName())) {
                    continue;
                }
                plrs.add(ap);
            }
            for (final ArenaPlayer ap : plrs) {
                this.getLifeMap().remove(ap.getName());
				/*
				arena.getDebugger().i("faking player death", ap.get());
				arena.removePlayer(ap.get(), CFG.TP_LOSE.toString(), true,
						false);*/

                ap.setStatus(Status.LOST);
                ap.addLosses();

                //PlayerState.fullReset(arena, ap.get());
            }

            if (ArenaManager.checkAndCommit(this.arena, false)) {
                return;
            }
            PACheck.handleEnd(this.arena, false);
        } else {
            final PAGoalEvent gEvent = new PAGoalEvent(this.arena, this, "playerKill:" + killer.getName() + ':' + player.getName(), "playerDeath:" + player.getName());
            Bukkit.getPluginManager().callEvent(gEvent);
            iLives--;
            this.getLifeMap().put(killer.getName(), iLives);
            Bukkit.getScheduler().runTaskLater(PVPArena.instance,
                    new ResetRunnable(killer), 4L);
        }
    }

    private Map<Integer, ItemStack[][]> getItemMap() {
        if (this.itemMapCubed == null) {
            this.itemMapCubed = new HashMap<>();
        }
        return this.itemMapCubed;
    }

    @Override
    public boolean hasSpawn(final String string) {
        if (this.arena.isFreeForAll()) {

            if (this.arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
                for (final ArenaClass aClass : this.arena.getClasses()) {
                    if (string.toLowerCase().startsWith(
                            aClass.getName().toLowerCase() + "spawn")) {
                        return true;
                    }
                }
            }
            return string.toLowerCase().startsWith("spawn");
        }
        for (final String teamName : this.arena.getTeamNames()) {
            if (string.toLowerCase().startsWith(
                    teamName.toLowerCase() + "spawn")) {
                return true;
            }

            if (this.arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
                for (final ArenaClass aClass : this.arena.getClasses()) {
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
        this.getLifeMap().put(player.getName(), this.getDefaultRemainingKills());
    }

    private int getDefaultRemainingKills() {
        int max = 0;
        for (final int i : this.getItemMap().keySet()) {
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
                    this.getName() + ": player NULL");
            return;
        }
        this.getLifeMap().remove(player.getName());
    }

    @Override
    public void parseStart() {
        for (final ArenaTeam team : this.arena.getTeams()) {
            for (final ArenaPlayer ap : team.getTeamMembers()) {
                this.getLifeMap().put(ap.getName(), this.getDefaultRemainingKills());
            }
        }
    }

    @Override
    public void reset(final boolean force) {
        this.endRunner = null;
        this.getLifeMap().clear();
    }

    @Override
    public void setDefaults(final YamlConfiguration config) {
        if (!this.arena.isFreeForAll()) {
            if (config.get("teams.free") != null) {
                config.set("teams", null);
            }
            if (config.get("teams") == null) {
                this.arena.getDebugger().i("no teams defined, adding custom red and blue!");
                config.addDefault("teams.red", ChatColor.RED.name());
                config.addDefault("teams.blue", ChatColor.BLUE.name());
            }
            if (this.arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)
                    && config.get("flagColors") == null) {
                this.arena.getDebugger().i("no flagheads defined, adding white and black!");
                config.addDefault("flagColors.red", "WHITE");
                config.addDefault("flagColors.blue", "BLACK");
            }
        }


        final ConfigurationSection cs = (ConfigurationSection) config
                .get("goal.playerkillrewards");

        if (cs != null) {
            for (final String line : cs.getKeys(false)) {
                try {
                    this.getItemMap().put(Integer.parseInt(line.substring(2)),
                        new ItemStack[][] {
                            cs.getList(line + ".items").toArray(new ItemStack[0]),
                            cs.getList(line + ".offhand").toArray(new ItemStack[]{new ItemStack(Material.AIR, 1)}),
                            cs.getList(line + ".armor").toArray(new ItemStack[0])
                        });
                } catch (final Exception ignored) {
                }
            }
        }

        if (this.getItemMap().size() < 1) {

            this.getItemMap().put(5, new ItemStack[][]{
                        new ItemStack[]{new ItemStack(Material.WOODEN_SWORD, 1)},
                        new ItemStack[]{new ItemStack(Material.AIR, 1)},
                        new ItemStack[]{
                                new ItemStack(Material.LEATHER_HELMET, 1),
                                new ItemStack(Material.LEATHER_CHESTPLATE, 1),
                                new ItemStack(Material.LEATHER_LEGGINGS, 1),
                                new ItemStack(Material.LEATHER_BOOTS, 1),
                        },
                    });
            this.getItemMap().put(4, new ItemStack[][]{
                    new ItemStack[]{new ItemStack(Material.STONE_SWORD, 1)},
                    new ItemStack[]{new ItemStack(Material.AIR, 1)},
                    new ItemStack[]{
                            new ItemStack(Material.CHAINMAIL_HELMET, 1),
                            new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1),
                            new ItemStack(Material.CHAINMAIL_LEGGINGS, 1),
                            new ItemStack(Material.CHAINMAIL_BOOTS, 1),
                    },
            });
            this.getItemMap().put(3, new ItemStack[][]{
                    new ItemStack[]{new ItemStack(Material.IRON_SWORD, 1)},
                    new ItemStack[]{new ItemStack(Material.AIR, 1)},
                    new ItemStack[]{
                            new ItemStack(Material.GOLDEN_HELMET, 1),
                            new ItemStack(Material.GOLDEN_CHESTPLATE, 1),
                            new ItemStack(Material.GOLDEN_LEGGINGS, 1),
                            new ItemStack(Material.GOLDEN_BOOTS, 1),
                    },
            });
            this.getItemMap().put(2, new ItemStack[][]{
                    new ItemStack[]{new ItemStack(Material.DIAMOND_SWORD, 1)},
                    new ItemStack[]{new ItemStack(Material.AIR, 1)},
                    new ItemStack[]{
                            new ItemStack(Material.IRON_HELMET, 1),
                            new ItemStack(Material.IRON_CHESTPLATE, 1),
                            new ItemStack(Material.IRON_LEGGINGS, 1),
                            new ItemStack(Material.IRON_BOOTS, 1),
                    },
            });
            this.getItemMap().put(1, new ItemStack[][]{
                    new ItemStack[]{new ItemStack(Material.DIAMOND_SWORD, 1)},
                    new ItemStack[]{new ItemStack(Material.AIR, 1)},
                    new ItemStack[]{
                            new ItemStack(Material.DIAMOND_HELMET, 1),
                            new ItemStack(Material.DIAMOND_CHESTPLATE, 1),
                            new ItemStack(Material.DIAMOND_LEGGINGS, 1),
                            new ItemStack(Material.DIAMOND_BOOTS, 1),
                    },
            });

            this.saveItems();
        }
    }

    private void saveItems() {
        for (final int i : this.getItemMap().keySet()) {
            this.arena.getArenaConfig().setManually("goal.playerkillrewards.kr" + i+".items",
                    getSerializableItemStacks(this.getItemMap().get(i)[0]));
            this.arena.getArenaConfig().setManually("goal.playerkillrewards.kr" + i+".offhand",
                    getSerializableItemStacks(this.getItemMap().get(i)[1]));
            this.arena.getArenaConfig().setManually("goal.playerkillrewards.kr" + i+".armor",
                    getSerializableItemStacks(this.getItemMap().get(i)[2]));
        }
        this.arena.getArenaConfig().save();
    }

    @Override
    public Map<String, Double> timedEnd(final Map<String, Double> scores) {

        for (final ArenaPlayer ap : this.arena.getFighters()) {
            double score = this.getDefaultRemainingKills() - (this.getLifeMap().getOrDefault(ap.getName(), 0));
            if (scores.containsKey(ap.getName())) {
                scores.put(ap.getName(), scores.get(ap.getName()) + score);
            } else {
                scores.put(ap.getName(), score);
            }
        }

        return scores;
    }

    @Override
    public PACheck getLives(PACheck res, ArenaPlayer player) {
        if (res.getPriority() <= PRIORITY + 1000) {
            if (this.arena.isFreeForAll()) {
                res.setError(
                        this, String.valueOf(this.getLifeMap().getOrDefault(player.getName(), 0))
                );
            } else {
                if (this.getLifeMap().containsKey(player.getArenaTeam().getName())) {
                    res.setError(this, String.valueOf(this.getLifeMap().get(player.getName())));
                } else {

                    int sum = 0;

                    for (final ArenaPlayer ap : player.getArenaTeam().getTeamMembers()) {
                        if (this.getLifeMap().containsKey(ap.getName())) {
                            sum += this.getLifeMap().get(ap.getName());
                        }
                    }

                    res.setError(this, String.valueOf(sum));
                }
            }
        }
        return res;
    }

    @Override
    public void unload(final Player player) {
        this.getLifeMap().remove(player.getName());
    }
}
