package net.slipcor.pvparena.goals;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAGoalEvent;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager.Type;
import net.slipcor.pvparena.runnables.EndRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * <pre>
 * Arena Goal class "Sabotage"
 * </pre>
 * <p/>
 * The first advanced Arena Goal. Sneak into an other team's base and ignite
 * their TNT.
 *
 * @author slipcor
 */

public class GoalSabotage extends ArenaGoal implements Listener {

    public GoalSabotage() {
        super("Sabotage");
        debug = new Debug(103);
    }

    private String flagName = "";
    private Map<String, String> teamFlags;
    private Map<ArenaTeam, TNTPrimed> teamTNTs;

    @Override
    public String version() {
        return PVPArena.instance.getDescription().getVersion();
    }

    private static final int PRIORITY = 7;

    @Override
    public boolean allowsJoinInBattle() {
        return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
    }

    @Override
    public PACheck checkCommand(final PACheck res, final String string) {
        if (res.getPriority() > PRIORITY) {
            return res;
        }

        for (final ArenaTeam team : arena.getTeams()) {
            final String sTeam = team.getName();
            if (string.contains(sTeam + "tnt")) {
                res.setPriority(this, PRIORITY);
            }
        }

        return res;
    }

    @Override
    public List<String> getMain() {
        final List<String> result = new ArrayList<>();
        if (arena != null) {
            for (final ArenaTeam team : arena.getTeams()) {
                final String sTeam = team.getName();
                result.add(sTeam + "tnt");
            }
        }
        return result;
    }

    @Override
    public String checkForMissingSpawns(final Set<String> list) {
        final String team = checkForMissingTeamSpawn(list);
        if (team != null) {
            return team;
        }
        return checkForMissingTeamCustom(list, "tnt");
    }

    /**
     * hook into an interacting player
     *
     * @param res    the PACheck instance
     * @param player the interacting player
     * @param block  the block being clicked
     * @return the PACheck instance
     */
    @Override
    public PACheck checkInteract(final PACheck res, final Player player, final Block block) {
        if (block == null || res.getPriority() > PRIORITY) {
            return res;
        }
        arena.getDebugger().i("checking interact", player);

        if (block.getType() != Material.TNT) {
            arena.getDebugger().i("block, but not flag", player);
            return res;
        }
        arena.getDebugger().i("flag click!", player);

        if (player.getEquipment().getItemInMainHand() == null
                || player.getEquipment().getItemInMainHand().getType() != Material.FLINT_AND_STEEL) {
            arena.getDebugger().i("block, but no sabotage items", player);
            return res;
        }

        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

        final ArenaTeam pTeam = aPlayer.getArenaTeam();
        if (pTeam == null) {
            return res;
        }
        Vector vFlag = null;
        for (final ArenaTeam team : arena.getTeams()) {
            final String aTeam = team.getName();
            if (team.getTeamMembers().size() < 1) {
                continue; // dont check for inactive teams
            }
            arena.getDebugger().i("checking for tnt of team " + aTeam, player);
            Vector vLoc = block.getLocation().toVector();
            arena.getDebugger().i("block: " + vLoc, player);
            if (!SpawnManager.getBlocksStartingWith(arena, aTeam + "tnt").isEmpty()) {
                vFlag = SpawnManager
                        .getBlockNearest(
                                SpawnManager.getBlocksStartingWith(arena, aTeam + "tnt"),
                                new PABlockLocation(player.getLocation()))
                        .toLocation().toVector();
            }

            if (vFlag != null && vLoc.distance(vFlag) < 2) {
                arena.getDebugger().i("flag found!", player);
                arena.getDebugger().i("vFlag: " + vFlag, player);

                if (aTeam.equals(pTeam.getName())) {
                    res.setError(this, Language.parse(MSG.GOAL_SABOTAGE_YOUCANNOTSELFDESTROY));
                    continue;
                }

                arena.broadcast(Language.parse(arena, MSG.GOAL_SABOTAGE_IGNITED,
                        pTeam.colorizePlayer(player) + ChatColor.YELLOW,
                        team.getColoredName() + ChatColor.YELLOW));

                final PAGoalEvent gEvent = new PAGoalEvent(arena, this, "trigger:" + player.getName());
                Bukkit.getPluginManager().callEvent(gEvent);
                takeFlag(team.getName(), true,
                        new PABlockLocation(block.getLocation()));
                res.setPriority(this, PRIORITY);
                return res;
            }
        }

        return res;
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
    public PACheck checkSetBlock(final PACheck res, final Player player, final Block block) {

        if (res.getPriority() > PRIORITY
                || !PAA_Region.activeSelections.containsKey(player.getName())) {
            return res;
        }
        if (block == null || block.getType() != Material.TNT) {
            return res;
        }

        if (!PVPArena.hasAdminPerms(player)
                && !PVPArena.hasCreatePerms(player, arena)) {
            return res;
        }
        res.setPriority(this, PRIORITY); // success :)

        return res;
    }

    private void commit(final Arena arena, final String sTeam) {
        if (arena.realEndRunner != null) {
            arena.getDebugger().i("[SABOTAGE] already ending");
            return;
        }
        arena.getDebugger().i("[SABOTAGE] committing end: " + sTeam);
        arena.getDebugger().i("win: " + false);

        final PAGoalEvent gEvent = new PAGoalEvent(arena, this, "");
        Bukkit.getPluginManager().callEvent(gEvent);
        String winteam = sTeam;

        for (final ArenaTeam team : arena.getTeams()) {
            if (!team.getName().equals(sTeam)) {
                continue;
            }
            for (final ArenaPlayer ap : team.getTeamMembers()) {

                ap.addStatistic(arena.getName(), Type.LOSSES, 1);
                /*
				arena.tpPlayerToCoordName(ap.get(), "spectator");
				ap.setTelePass(false);*/

                ap.setStatus(Status.LOST);
            }
        }
        for (final ArenaTeam team : arena.getTeams()) {
            for (final ArenaPlayer ap : team.getTeamMembers()) {
                if (ap.getStatus() != Status.FIGHT) {
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

        new EndRunnable(arena, arena.getArenaConfig().getInt(
                CFG.TIME_ENDCOUNTDOWN));
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        if (args[0].contains("tnt")) {
            for (final ArenaTeam team : arena.getTeams()) {
                final String sTeam = team.getName();
                if (args[0].contains(sTeam + "tnt")) {
                    flagName = args[0];
                    PAA_Region.activeSelections.put(sender.getName(), arena);

                    arena.msg(sender, Language.parse(arena,
                            MSG.GOAL_SABOTAGE_TOSETTNT, flagName));
                }
            }
        }
    }

    @Override
    public void commitEnd(final boolean force) {
        if (arena.realEndRunner != null) {
            arena.getDebugger().i("[SABOTAGE] already ending");
            return;
        }
        arena.getDebugger().i("[SABOTAGE]");

        ArenaTeam aTeam = null;

        for (final ArenaTeam team : arena.getTeams()) {
            for (final ArenaPlayer ap : team.getTeamMembers()) {
                if (ap.getStatus() == Status.FIGHT) {
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
    public void commitInteract(final Player player, final Block clickedBlock) {
    }

    @Override
    public boolean commitSetFlag(final Player player, final Block block) {

        arena.getDebugger().i("trying to set a tnt", player);

        // command : /pa redtnt1
        // location: red1tnt:

        SpawnManager.setBlock(arena, new PABlockLocation(block.getLocation()),
                flagName);

        arena.msg(player, Language.parse(arena, MSG.GOAL_SABOTAGE_SETTNT, flagName));

        PAA_Region.activeSelections.remove(player.getName());
        flagName = "";
        return true;
    }

    @Override
    public void configParse(final YamlConfiguration cfg) {
        Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
    }

    @Override
    public void disconnect(final ArenaPlayer aPlayer) {

        final String flag = getHeldFlagTeam(aPlayer.getName());
        if (flag != null) {
            final ArenaTeam flagTeam = arena.getTeam(flag);
            getFlagMap().remove(flag);
            distributeFlag(aPlayer, flagTeam);
        }
    }

    private void distributeFlag(final ArenaPlayer player, final ArenaTeam team) {
        final Set<ArenaPlayer> players = team.getTeamMembers();

        int pos = new Random().nextInt(players.size());

        for (final ArenaPlayer ap : players) {
            arena.getDebugger().i("distributing sabotage: " + ap.getName(), ap.getName());
            if (ap.equals(player)) {
                continue;
            }
            if (--pos <= 1) {
                getFlagMap().put(team.getName(), ap.getName());
                ap.get().getInventory()
                        .addItem(new ItemStack(Material.FLINT_AND_STEEL, 1));
                arena.msg(ap.get(), Language.parse(arena, MSG.GOAL_SABOTAGE_YOUTNT));
                return;
            }
        }
    }

    private String getHeldFlagTeam(final String player) {
        if (getFlagMap().size() < 1) {
            return null;
        }

        arena.getDebugger().i("getting held TNT of player " + player, player);
        for (final String sTeam : getFlagMap().keySet()) {
            arena.getDebugger().i("team " + sTeam + "'s sabotage is carried by "
                    + getFlagMap().get(sTeam) + "s hands", player);
            if (player.equals(getFlagMap().get(sTeam))) {
                return sTeam;
            }
        }
        return null;
    }

    private Map<String, String> getFlagMap() {
        if (teamFlags == null) {
            teamFlags = new HashMap<>();
        }
        return teamFlags;
    }

    private Map<ArenaTeam, TNTPrimed> getTNTmap() {
        if (teamTNTs == null) {
            teamTNTs = new HashMap<>();
        }
        return teamTNTs;
    }

    @Override
    public boolean hasSpawn(final String string) {
        for (final String teamName : arena.getTeamNames()) {
            if (string.toLowerCase().equals(teamName.toLowerCase() + "tnt")) {
                return true;
            }
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
        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
        final ArenaTeam team = aPlayer.getArenaTeam();
        takeFlag(team.getName(), false,
                SpawnManager.getBlockByExactName(arena, team.getName() + "tnt"));
        //TODO: allow multiple TNTs?
        if (!getFlagMap().containsKey(team.getName())) {
            arena.getDebugger().i("adding team " + team.getName(), player);
            distributeFlag(null, team);
        }
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public void parsePlayerDeath(final Player player, final EntityDamageEvent event) {
        final String teamName = getHeldFlagTeam(player.getName());
        final ArenaTeam team = arena.getTeam(teamName);
        if (teamName != null && team != null) {
            final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
            getFlagMap().remove(teamName);
            distributeFlag(aPlayer, team);
        }
    }

    @Override
    public void parseStart() {
        arena.getDebugger().i("initiating arena");
        getFlagMap().clear();
        for (final ArenaTeam team : arena.getTeams()) {
            takeFlag(team.getName(), false,
                    SpawnManager.getBlockByExactName(arena, team.getName() + "tnt"));
            // TODO: allow multiple TNTs?
            if (!getFlagMap().containsKey(team.getName())) {
                arena.getDebugger().i("adding team " + team.getName());
                distributeFlag(null, team);
            }
        }
    }

    @Override
    public void reset(final boolean force) {
        getFlagMap().clear();
        for (final TNTPrimed t : getTNTmap().values()) {
            t.remove();
        }
        getTNTmap().clear();
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
     * @param teamName        the teamcolor to reset
     * @param take            true if take, else reset
     * @param paBlockLocation the location to take/reset
     */
    void takeFlag(final String teamName, final boolean take, final PABlockLocation paBlockLocation) {
        paBlockLocation.toLocation().getBlock()
                .setType(take ? Material.AIR : Material.TNT);
        if (take) {
            final TNTPrimed tnt = (TNTPrimed) Bukkit.getWorld(
                    paBlockLocation.getWorldName())
                    .spawnEntity(paBlockLocation.toLocation(), EntityType.PRIMED_TNT);
            getTNTmap().put(arena.getTeam(teamName), tnt);
        }
    }

    @Override
    public void unload(final Player player) {
        disconnect(ArenaPlayer.parsePlayer(player.getName()));
    }

    @EventHandler
    public void onTNTExplode(final EntityExplodeEvent event) {
        if (event.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }

        final TNTPrimed tnt = (TNTPrimed) event.getEntity();

        for (final ArenaTeam team : getTNTmap().keySet()) {
            if (tnt.getUniqueId().equals(getTNTmap().get(team).getUniqueId())) {
                event.setCancelled(true);
                tnt.remove();
                commit(arena, team.getName());
            }
        }

        final PABlockLocation tLoc = new PABlockLocation(event.getEntity()
                .getLocation());

        for (final String sTeam : arena.getTeamNames()) {
            final Set<PABlockLocation> locs = SpawnManager.getBlocksStartingWith(arena, sTeam + "tnt");

            final PABlockLocation nearest = SpawnManager.getBlockNearest(locs, tLoc);

            if (nearest.getDistanceSquared(tLoc) < 4) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
