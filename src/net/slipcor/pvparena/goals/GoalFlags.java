package net.slipcor.pvparena.goals;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Utils;
import net.slipcor.pvparena.events.PAGoalEvent;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * <pre>
 * Arena Goal class "Flags"
 * </pre>
 * <p/>
 * Well, should be clear. Capture flags, bring them home, get points, win.
 *
 * @author slipcor
 */

public class GoalFlags extends ArenaGoal implements Listener {

    public GoalFlags() {
        super("Flags");
        debug = new Debug(100);
    }

    private Map<String, String> flagMap;
    private Map<String, ItemStack> headGearMap;

    private String flagName = "";

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
        if (res.getPriority() > PRIORITY) {
            return res;
        }

        if ("flagtype".equalsIgnoreCase(string)
                || "flageffect".equalsIgnoreCase(string)
                || "touchdown".equalsIgnoreCase(string)) {
            res.setPriority(this, PRIORITY);
        }

        for (final ArenaTeam team : arena.getTeams()) {
            final String sTeam = team.getName();
            if (string.contains(sTeam + "flag")) {
                res.setPriority(this, PRIORITY);
            }
        }

        return res;
    }

    @Override
    public List<String> getMain() {
        final List<String> result = Arrays.asList("flagtype", "flageffect", "touchdown");
        if (arena != null) {
            for (final ArenaTeam team : arena.getTeams()) {
                final String sTeam = team.getName();
                result.add(sTeam + "flag");
            }
        }
        return result;
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"{Material}"});
        return result;
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
            arena.getDebugger().i("No teams playing!");
        }

        return res;
    }

    @Override
    public String checkForMissingSpawns(final Set<String> list) {
        final String team = checkForMissingTeamSpawn(list);
        if (team != null) {
            return team;
        }

        return checkForMissingTeamCustom(list, "flag");
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
        if (arena.realEndRunner != null) {
            arena.getDebugger().i("[CTF] already ending!!");
            return res;
        }

        Material flagType = arena.getArenaConfig().getMaterial(CFG.GOAL_FLAGS_FLAGTYPE);
        if (!Utils.isSubType(block.getType(), flagType)) {
            arena.getDebugger().i("block, but not flag", player);
            return res;
        }
        arena.getDebugger().i("flag click!", player);

        Vector vLoc;
        Vector vFlag = null;
        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

        if (getFlagMap().containsValue(player.getName())) {
            arena.getDebugger().i("player " + player.getName() + " has got a flag", player);
            vLoc = block.getLocation().toVector();
            final String sTeam = aPlayer.getArenaTeam().getName();
            arena.getDebugger().i("block: " + vLoc, player);
            if (!SpawnManager.getBlocksStartingWith(arena, sTeam + "flag").isEmpty()) {
                vFlag = SpawnManager
                        .getBlockNearest(
                                SpawnManager.getBlocksStartingWith(arena, sTeam + "flag"),
                                new PABlockLocation(player.getLocation()))
                        .toLocation().toVector();
            } else {
                arena.getDebugger().i(sTeam + "flag = null", player);
            }

            arena.getDebugger().i("player is in the team " + sTeam, player);
            if (vFlag != null && vLoc.distance(vFlag) < 2) {

                arena.getDebugger().i("player is at his flag", player);

                if (getFlagMap().containsKey(sTeam)
                        || getFlagMap().containsKey("touchdown")) {
                    arena.getDebugger().i("the flag of the own team is taken!", player);

                    if (arena.getArenaConfig().getBoolean(
                            CFG.GOAL_FLAGS_MUSTBESAFE)
                            && !getFlagMap().containsKey("touchdown")) {
                        arena.getDebugger().i("cancelling", player);

                        arena.msg(player,
                                Language.parse(arena, MSG.GOAL_FLAGS_NOTSAFE));
                        return res;
                    }
                }

                String flagTeam = getHeldFlagTeam(player.getName());

                arena.getDebugger().i("the flag belongs to team " + flagTeam, player);

                try {
                    if ("touchdown".equals(flagTeam)) {
                        arena.broadcast(Language.parse(arena,
                                MSG.GOAL_FLAGS_TOUCHHOME, arena.getTeam(sTeam)
                                        .colorizePlayer(player)
                                        + ChatColor.YELLOW, String
                                        .valueOf(getLifeMap().get(aPlayer
                                                .getArenaTeam().getName()) - 1)));
                    } else {
                        arena.broadcast(Language.parse(arena,
                                MSG.GOAL_FLAGS_BROUGHTHOME, arena
                                        .getTeam(sTeam).colorizePlayer(player)
                                        + ChatColor.YELLOW,
                                arena.getTeam(flagTeam).getColoredName()
                                        + ChatColor.YELLOW, String
                                        .valueOf(getLifeMap().get(flagTeam) - 1)));
                    }
                    getFlagMap().remove(flagTeam);
                } catch (final Exception e) {
                    Bukkit.getLogger().severe(
                            "[PVP Arena] team unknown/no lives: " + flagTeam);
                    e.printStackTrace();
                }
                if ("touchdown".equals(flagTeam)) {
                    takeFlag(ChatColor.BLACK, false,
                            SpawnManager.getBlockByExactName(arena, "touchdownflag"));
                } else {
                    takeFlag(arena.getTeam(flagTeam).getColor(), false,
                            SpawnManager.getBlockByExactName(arena, flagTeam + "flag"));
                }
                removeEffects(player);
                if (arena.getArenaConfig().getBoolean(
                        CFG.GOAL_FLAGS_WOOLFLAGHEAD)) {
                    if (getHeadGearMap().get(player.getName()) == null) {
                        player.getInventory().setHelmet(
                                new ItemStack(Material.AIR, 1));
                    } else {
                        player.getInventory().setHelmet(
                                getHeadGearMap().get(player.getName()).clone());
                        getHeadGearMap().remove(player.getName());
                    }
                }

                flagTeam = "touchdown".equals(flagTeam) ? flagTeam + ':' + aPlayer
                        .getArenaTeam().getName() : flagTeam;

                reduceLivesCheckEndAndCommit(arena, flagTeam); // TODO move to
                // "commit" ?

                final PAGoalEvent gEvent = new PAGoalEvent(arena, this, "trigger:" + aPlayer.getName());
                Bukkit.getPluginManager().callEvent(gEvent);
            }
        } else {
            final ArenaTeam pTeam = aPlayer.getArenaTeam();
            if (pTeam == null) {
                return res;
            }
            final Set<ArenaTeam> setTeam = new HashSet<>();

            for (final ArenaTeam team : arena.getTeams()) {
                setTeam.add(team);
            }
            setTeam.add(new ArenaTeam("touchdown", "BLACK"));
            for (final ArenaTeam team : setTeam) {
                final String aTeam = team.getName();

                if (aTeam.equals(pTeam.getName())) {
                    arena.getDebugger().i("equals!OUT! ", player);
                    continue;
                }
                if (team.getTeamMembers().size() < 1
                        && !"touchdown".equals(team.getName())) {
                    arena.getDebugger().i("size!OUT! ", player);
                    continue; // dont check for inactive teams
                }
                if (getFlagMap() != null && getFlagMap().containsKey(aTeam)) {
                    arena.getDebugger().i("taken!OUT! ", player);
                    continue; // already taken
                }
                arena.getDebugger().i("checking for flag of team " + aTeam, player);
                vLoc = block.getLocation().toVector();
                arena.getDebugger().i("block: " + vLoc, player);
                if (!SpawnManager.getBlocksStartingWith(arena, aTeam + "flag").isEmpty()) {
                    vFlag = SpawnManager
                            .getBlockNearest(
                                    SpawnManager.getBlocksStartingWith(arena, aTeam
                                            + "flag"),
                                    new PABlockLocation(player.getLocation()))
                            .toLocation().toVector();
                }
                if (vFlag != null && vLoc.distance(vFlag) < 2) {
                    arena.getDebugger().i("flag found!", player);
                    arena.getDebugger().i("vFlag: " + vFlag, player);

                    if ("touchdown".equals(team.getName())) {

                        arena.broadcast(Language.parse(arena,
                                MSG.GOAL_FLAGS_GRABBEDTOUCH,
                                pTeam.colorizePlayer(player) + ChatColor.YELLOW));
                    } else {

                        arena.broadcast(Language
                                .parse(arena, MSG.GOAL_FLAGS_GRABBED,
                                        pTeam.colorizePlayer(player)
                                                + ChatColor.YELLOW,
                                        team.getColoredName()
                                                + ChatColor.YELLOW));
                    }
                    try {
                        getHeadGearMap().put(player.getName(), player.getInventory()
                                .getHelmet().clone());
                    } catch (final Exception e) {
                    }

                    if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)) {
                        final ItemStack itemStack = new ItemStack(getFlagOverrideTeamMaterial(arena, aTeam));
                        player.getInventory().setHelmet(itemStack);
                    }
                    applyEffects(player);

                    takeFlag(team.getColor(), true,
                            new PABlockLocation(block.getLocation()));
                    getFlagMap().put(aTeam, player.getName()); // TODO move to
                    // "commit" ?

                    return res;
                }
            }
        }

        return res;
    }

    private void applyEffects(final Player player) {
        final String value = arena.getArenaConfig().getString(
                CFG.GOAL_FLAGS_FLAGEFFECT);

        if ("none".equalsIgnoreCase(value)) {
            return;
        }

        final String[] split = value.split("x");

        int amp = 1;

        if (split.length > 1) {
            try {
                amp = Integer.parseInt(split[1]);
            } catch (final Exception e) {
            }
        }

        PotionEffectType pet = null;
        for (final PotionEffectType x : PotionEffectType.values()) {
            if (x == null) {
                continue;
            }
            if (x.getName().equalsIgnoreCase(split[0])) {
                pet = x;
                break;
            }
        }

        if (pet == null) {
            PVPArena.instance.getLogger().warning(
                    "Invalid Potion Effect Definition: " + value);
            return;
        }

        player.addPotionEffect(new PotionEffect(pet, 2147000, amp));
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

        Material flagType = arena.getArenaConfig().getMaterial(CFG.GOAL_FLAGS_FLAGTYPE);
        if (block == null || !Utils.isSubType(block.getType(), flagType)) {
            return res;
        }

        if (!PVPArena.hasAdminPerms(player)
                && !PVPArena.hasCreatePerms(player, arena)) {
            return res;
        }
        res.setPriority(this, PRIORITY); // success :)

        return res;
    }

    private void commit(final Arena arena, final String sTeam, final boolean win) {
        if (arena.realEndRunner != null) {
            arena.getDebugger().i("[CTF] already ending");
            return;
        }
        arena.getDebugger().i("[CTF] committing end: " + sTeam);
        arena.getDebugger().i("win: " + win);

        String winteam = sTeam;

        for (final ArenaTeam team : arena.getTeams()) {
            if (team.getName().equals(sTeam) == win) {
                continue;
            }
            for (final ArenaPlayer ap : team.getTeamMembers()) {

                ap.addStatistic(arena.getName(), type.LOSSES, 1);
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

        getLifeMap().clear();
        new EndRunnable(arena, arena.getArenaConfig().getInt(
                CFG.TIME_ENDCOUNTDOWN));
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        if ("flagtype".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                arena.msg(
                        sender,
                        Language.parse(arena, MSG.ERROR_INVALID_ARGUMENT_COUNT,
                                String.valueOf(args.length), "2"));
                return;
            }

            final Material mat = Material.getMaterial(args[1].toUpperCase());

            if (mat == null) {
                arena.msg(sender,
                        Language.parse(arena, MSG.ERROR_MAT_NOT_FOUND, args[1]));
                return;
            }

            arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGTYPE, mat.name());

            arena.getArenaConfig().save();
            arena.msg(sender, Language.parse(arena, MSG.GOAL_FLAGS_TYPESET,
                    CFG.GOAL_FLAGS_FLAGTYPE.toString()));

        } else if ("flageffect".equalsIgnoreCase(args[0])) {

            // /pa [arena] flageffect SLOW 2
            if (args.length < 2) {
                arena.msg(
                        sender,
                        Language.parse(arena, MSG.ERROR_INVALID_ARGUMENT_COUNT,
                                String.valueOf(args.length), "2"));
                return;
            }

            if ("none".equalsIgnoreCase(args[1])) {
                arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGEFFECT, args[1]);

                arena.getArenaConfig().save();
                arena.msg(
                        sender,
                        Language.parse(arena, MSG.SET_DONE,
                                CFG.GOAL_FLAGS_FLAGEFFECT.getNode(), args[1]));
                return;
            }

            PotionEffectType pet = null;

            for (final PotionEffectType x : PotionEffectType.values()) {
                if (x == null) {
                    continue;
                }
                if (x.getName().equalsIgnoreCase(args[1])) {
                    pet = x;
                    break;
                }
            }

            if (pet == null) {
                arena.msg(sender, Language.parse(arena,
                        MSG.ERROR_POTIONEFFECTTYPE_NOTFOUND, args[1]));
                return;
            }

            int amp = 1;

            if (args.length == 5) {
                try {
                    amp = Integer.parseInt(args[2]);
                } catch (final Exception e) {
                    arena.msg(sender,
                            Language.parse(arena, MSG.ERROR_NOT_NUMERIC, args[2]));
                    return;
                }
            }
            final String value = args[1] + 'x' + amp;
            arena.getArenaConfig().set(CFG.GOAL_FLAGS_FLAGEFFECT, value);

            arena.getArenaConfig().save();
            arena.msg(
                    sender,
                    Language.parse(arena, MSG.SET_DONE,
                            CFG.GOAL_FLAGS_FLAGEFFECT.getNode(), value));

        } else if (args[0].contains("flag")) {
            for (final ArenaTeam team : arena.getTeams()) {
                final String sTeam = team.getName();
                if (args[0].contains(sTeam + "flag")) {
                    flagName = args[0];
                    PAA_Region.activeSelections.put(sender.getName(), arena);

                    arena.msg(sender,
                            Language.parse(arena, MSG.GOAL_FLAGS_TOSET, flagName));
                }
            }
        } else if ("touchdown".equalsIgnoreCase(args[0])) {
            flagName = args[0] + "flag";
            PAA_Region.activeSelections.put(sender.getName(), arena);

            arena.msg(sender, Language.parse(arena, MSG.GOAL_FLAGS_TOSET, flagName));
        }
    }

    @Override
    public void commitEnd(final boolean force) {
        if (arena.realEndRunner != null) {
            arena.getDebugger().i("[FLAGS] already ending");
            return;
        }
        arena.getDebugger().i("[FLAGS]");

        final PAGoalEvent gEvent = new PAGoalEvent(arena, this, "");
        Bukkit.getPluginManager().callEvent(gEvent);
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
    public boolean commitSetFlag(final Player player, final Block block) {

        arena.getDebugger().i("trying to set a flag", player);

        // command : /pa redflag1
        // location: red1flag:

        SpawnManager.setBlock(arena, new PABlockLocation(block.getLocation()),
                flagName);

        arena.msg(player, Language.parse(arena, MSG.GOAL_FLAGS_SET, flagName));

        PAA_Region.activeSelections.remove(player.getName());
        flagName = "";

        return true;
    }

    @Override
    public void commitStart() {
        // empty to kill the error ;)
    }

    @Override
    public void configParse(final YamlConfiguration config) {
        Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
    }

    @Override
    public void disconnect(final ArenaPlayer aPlayer) {
        if (getFlagMap() == null) {
            return;
        }
        final String sTeam = getHeldFlagTeam(aPlayer.getName());
        final ArenaTeam flagTeam = arena.getTeam(sTeam);

        if (flagTeam == null) {
            if (sTeam == null) {
                return;
            }
            arena.broadcast(Language.parse(arena, MSG.GOAL_FLAGS_DROPPEDTOUCH, aPlayer
                    .getArenaTeam().getColorCodeString()
                    + aPlayer.getName()
                    + ChatColor.YELLOW));

            getFlagMap().remove("touchdown");
            if (getHeadGearMap() != null && getHeadGearMap().get(aPlayer.getName()) != null) {
                if (aPlayer.get() != null) {
                    aPlayer.get().getInventory()
                            .setHelmet(getHeadGearMap().get(aPlayer.getName()).clone());
                }
                getHeadGearMap().remove(aPlayer.getName());
            }

            takeFlag(ChatColor.BLACK, false,
                    SpawnManager.getBlockByExactName(arena, "touchdownflag"));

            return;
        }
        arena.broadcast(Language.parse(arena, MSG.GOAL_FLAGS_DROPPED, aPlayer
                .getArenaTeam().getColorCodeString()
                + aPlayer.getName()
                + ChatColor.YELLOW, flagTeam.getName() + ChatColor.YELLOW));
        getFlagMap().remove(flagTeam.getName());
        if (getHeadGearMap() != null && getHeadGearMap().get(aPlayer.getName()) != null) {
            if (aPlayer.get() != null) {
                aPlayer.get().getInventory()
                        .setHelmet(getHeadGearMap().get(aPlayer.getName()).clone());
            }
            getHeadGearMap().remove(aPlayer.getName());
        }

        takeFlag(flagTeam.getColor(), false,
                SpawnManager.getBlockByExactName(arena, flagTeam.getName() + "flag"));
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage("flageffect: " +
                arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGEFFECT));
        sender.sendMessage("flagtype: " +
                arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE));
        sender.sendMessage("lives: " +
                arena.getArenaConfig().getInt(CFG.GOAL_FLAGS_LIVES));
        sender.sendMessage(StringParser.colorVar("mustbesafe",
                arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_MUSTBESAFE)) +
                " | " + StringParser.colorVar("flaghead",
                arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)));
    }

    private Map<String, String> getFlagMap() {
        if (flagMap == null) {
            flagMap = new HashMap<>();
        }
        return flagMap;
    }

    private Material getFlagOverrideTeamMaterial(final Arena arena, final String team) {
        if (arena.getArenaConfig().getUnsafe("flagColors." + team) == null) {
            if ("touchdown".equals(team)) {
                return StringParser.getColoredWoolFromChatColor(ChatColor.BLACK.name());
            }
            return StringParser.getColoredWoolFromChatColor(arena.getTeam(team).getColor().name());
        }
        return StringParser.getColoredWoolFromChatColor(
                (String) arena.getArenaConfig().getUnsafe("flagColors." + team));
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

    private Map<String, ItemStack> getHeadGearMap() {
        if (headGearMap == null) {
            headGearMap = new HashMap<>();
        }
        return headGearMap;
    }

    /**
     * get the team name of the flag a player holds
     *
     * @param player the player to check
     * @return a team name
     */
    private String getHeldFlagTeam(final String player) {
        if (getFlagMap().size() < 1) {
            return null;
        }

        arena.getDebugger().i("getting held FLAG of player " + player, player);
        for (final String sTeam : getFlagMap().keySet()) {
            arena.getDebugger().i("team " + sTeam + " is in " + getFlagMap().get(sTeam)
                    + "s hands", player);
            if (player.equals(getFlagMap().get(sTeam))) {
                return sTeam;
            }
        }
        return null;
    }

    @Override
    public boolean hasSpawn(final String string) {
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
        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
        final ArenaTeam team = aPlayer.getArenaTeam();
        if (!getLifeMap().containsKey(team.getName())) {
            getLifeMap().put(aPlayer.getArenaTeam().getName(), arena.getArenaConfig()
                    .getInt(CFG.GOAL_FLAGS_LIVES));

            takeFlag(team.getColor(), false,
                    SpawnManager.getBlockByExactName(arena, team.getName() + "flag"));
            takeFlag(ChatColor.BLACK, false,
                    SpawnManager.getBlockByExactName(arena, "touchdownflag"));
        }
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public void parsePlayerDeath(final Player player,
                                 final EntityDamageEvent lastDamageCause) {

        if (getFlagMap() == null) {
            arena.getDebugger().i("no flags set!!", player);
            return;
        }
        final String sTeam = getHeldFlagTeam(player.getName());
        final ArenaTeam flagTeam = arena.getTeam(sTeam);
        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

        if (flagTeam == null) {
            if (sTeam == null) {
                return;
            }
            arena.broadcast(Language.parse(arena, MSG.GOAL_FLAGS_DROPPEDTOUCH, aPlayer
                    .getArenaTeam().getColorCodeString()
                    + aPlayer.getName()
                    + ChatColor.YELLOW));

            getFlagMap().remove("touchdown");
            if (getHeadGearMap() != null && getHeadGearMap().get(aPlayer.getName()) != null) {
                if (aPlayer.get() != null) {
                    aPlayer.get().getInventory()
                            .setHelmet(getHeadGearMap().get(aPlayer.getName()).clone());
                }
                getHeadGearMap().remove(aPlayer.getName());
            }

            takeFlag(ChatColor.BLACK, false,
                    SpawnManager.getBlockByExactName(arena, "touchdownflag"));
            return;
        }
        arena.broadcast(Language.parse(arena, MSG.GOAL_FLAGS_DROPPED, aPlayer
                        .getArenaTeam().colorizePlayer(player) + ChatColor.YELLOW,
                flagTeam.getColoredName() + ChatColor.YELLOW));
        getFlagMap().remove(flagTeam.getName());
        if (getHeadGearMap() != null
                && getHeadGearMap().get(player.getName()) != null) {
            player.getInventory().setHelmet(
                    getHeadGearMap().get(player.getName()).clone());
            getHeadGearMap().remove(player.getName());
        }

        takeFlag(flagTeam.getColor(), false,
                SpawnManager.getBlockByExactName(arena, flagTeam.getName() + "flag"));
    }

    @Override
    public void parseStart() {
        getLifeMap().clear();
        for (final ArenaTeam team : arena.getTeams()) {
            if (!team.getTeamMembers().isEmpty()) {
                arena.getDebugger().i("adding team " + team.getName());
                // team is active
                getLifeMap().put(team.getName(),
                        arena.getArenaConfig().getInt(CFG.GOAL_FLAGS_LIVES, 3));
            }
            takeFlag(team.getColor(), false,
                    SpawnManager.getBlockByExactName(arena, team.getName() + "flag"));
        }
        takeFlag(ChatColor.BLACK, false,
                SpawnManager.getBlockByExactName(arena, "touchdownflag"));
    }

    private boolean reduceLivesCheckEndAndCommit(final Arena arena, final String team) {

        arena.getDebugger().i("reducing lives of team " + team);

        if (getLifeMap().get(team) == null) {
            if (team.contains(":")) {
                final String realTeam = team.split(":")[1];
                final int pos = getLifeMap().get(realTeam) - 1;
                if (pos > 0) {
                    getLifeMap().put(realTeam, pos);
                } else {
                    getLifeMap().remove(realTeam);
                    commit(arena, realTeam, true);
                    return true;
                }
            }
        } else {
            final int pos = getLifeMap().get(team) - 1;
            if (pos > 0) {
                getLifeMap().put(team, pos);
            } else {
                getLifeMap().remove(team);
                commit(arena, team, false);
                return true;
            }
        }
        return false;
    }

    private void removeEffects(final Player player) {
        final String value = arena.getArenaConfig().getString(
                CFG.GOAL_FLAGS_FLAGEFFECT);

        if ("none".equalsIgnoreCase(value)) {
            return;
        }

        PotionEffectType pet = null;

        final String[] split = value.split("x");

        for (final PotionEffectType x : PotionEffectType.values()) {
            if (x == null) {
                continue;
            }
            if (x.getName().equalsIgnoreCase(split[0])) {
                pet = x;
                break;
            }
        }

        if (pet == null) {
            PVPArena.instance.getLogger().warning(
                    "Invalid Potion Effect Definition: " + value);
            return;
        }

        player.removePotionEffect(pet);
        player.addPotionEffect(new PotionEffect(pet, 0, 1));
    }

    @Override
    public void reset(final boolean force) {
        getFlagMap().clear();
        getHeadGearMap().clear();
        getLifeMap().clear();
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
        if (arena.getArenaConfig().getBoolean(CFG.GOAL_FLAGS_WOOLFLAGHEAD)
                && config.get("flagColors") == null) {
            arena.getDebugger().i("no flagheads defined, adding white and black!");
            config.addDefault("flagColors.red", "WHITE");
            config.addDefault("flagColors.blue", "BLACK");
        }
    }

    /**
     * take/reset an arena flag
     *
     * @param flagColor       the teamcolor to reset
     * @param take            true if take, else reset
     * @param paBlockLocation the location to take/reset
     */
    void takeFlag(final ChatColor flagColor, final boolean take, final PABlockLocation paBlockLocation) {
        if (paBlockLocation == null) {
            return;
        }
        if (!Utils.isSubType(Material.getMaterial(arena.getArenaConfig().getString(CFG.GOAL_FLAGS_FLAGTYPE)), Material.WHITE_WOOL)) {
            paBlockLocation.toLocation()
                    .getBlock()
                    .setType(
                            take ? Material.BEDROCK : Material.valueOf(arena
                                    .getArenaConfig().getString(
                                            CFG.GOAL_FLAGS_FLAGTYPE)));
            return;
        }
        if (take) {
            paBlockLocation.toLocation().getBlock()
                    .setType(Material.WHITE_WOOL);
        } else {
            paBlockLocation.toLocation().getBlock()
                    .setType(StringParser.getWoolFallbackMaterialFromString(flagColor));
        }
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

    @Override
    public void unload(final Player player) {
        disconnect(ArenaPlayer.parsePlayer(player.getName()));
        if (allowsJoinInBattle()) {
            arena.hasNotPlayed(ArenaPlayer.parsePlayer(player.getName()));
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        final Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();

        if (arena == null || !arena.getName().equals(this.arena.getName())) {
            return;
        }

        if (event.isCancelled()
                || getHeldFlagTeam(player.getName()) == null) {
            return;
        }

        if (event.getInventory().getType() == InventoryType.CRAFTING
                && event.getRawSlot() != 5) {
            return;
        }

        if (event.getSlotType() == SlotType.ARMOR && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            event.setCancelled(true);
        }
    }
}
