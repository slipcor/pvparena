package net.slipcor.pvparena;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.*;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.*;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.listeners.BlockListener;
import net.slipcor.pvparena.listeners.EntityListener;
import net.slipcor.pvparena.listeners.InventoryListener;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.loadables.*;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.managers.TabManager;
import net.slipcor.pvparena.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 * Main Plugin class
 * </pre>
 * <p/>
 * contains central elements like plugin handlers and listeners
 *
 * @author slipcor
 */

public class PVPArena extends JavaPlugin {
    public static PVPArena instance;

    private static Debug DEBUG;

    private ArenaGoalManager agm;
    private ArenaModuleManager amm;
    private ArenaRegionShapeManager arsm;

    private final List<AbstractArenaCommand> arenaCommands = new ArrayList<>();
    private final List<AbstractGlobalCommand> globalCommands = new ArrayList<>();

    private Updater updater;
    private boolean shuttingDown;

    /**
     * Hand over the ArenaGoalManager instance
     *
     * @return the ArenaGoalManager instance
     */
    public ArenaGoalManager getAgm() {
        return agm;
    }

    /**
     * Hand over the ArenaModuleManager instance
     *
     * @return the ArenaModuleManager instance
     */
    public ArenaModuleManager getAmm() {
        return amm;
    }

    /**
     * Hand over the ArenaRegionShapeManager instance
     *
     * @return the ArenaRegionShapeManager instance
     */
    public ArenaRegionShapeManager getArsm() {
        return arsm;
    }

    public List<AbstractArenaCommand> getArenaCommands() {
        return arenaCommands;
    }

    public List<AbstractGlobalCommand> getGlobalCommands() {
        return globalCommands;
    }

    public Updater getUpdater() {
        return updater;
    }

    /**
     * Check if a CommandSender has admin permissions
     *
     * @param sender the CommandSender to check
     * @return true if a CommandSender has admin permissions, false otherwise
     */
    public static boolean hasAdminPerms(final CommandSender sender) {
        return sender.hasPermission("pvparena.admin");
    }

    /**
     * Check if a CommandSender has creation permissions
     *
     * @param sender the CommandSender to check
     * @param arena  the arena to check
     * @return true if the CommandSender has creation permissions, false
     * otherwise
     */
    public static boolean hasCreatePerms(final CommandSender sender,
                                         final Arena arena) {
        return sender.hasPermission("pvparena.create") && (arena == null || arena
                .getOwner().equals(sender.getName()));
    }

    public static boolean hasOverridePerms(final CommandSender sender) {
        if (sender instanceof Player) {
            return sender.hasPermission("pvparena.override");
        }

        return instance.getConfig().getBoolean("consoleoffduty")
                != sender.hasPermission("pvparena.override");
    }

    /**
     * Check if a CommandSender has permission for an arena
     *
     * @param sender the CommandSender to check
     * @param arena  the arena to check
     * @return true if explicit permission not needed or granted, false
     * otherwise
     */
    public static boolean hasPerms(final CommandSender sender, final Arena arena) {
        arena.getDebugger().i("perm check.", sender);
        if (arena.getArenaConfig().getBoolean(CFG.PERMS_EXPLICITARENA)) {
            arena.getDebugger().i(
                    " - explicit: "
                            + sender.hasPermission("pvparena.join."
                            + arena.getName().toLowerCase()), sender);
        } else {
            arena.getDebugger().i(
                    String.valueOf(sender.hasPermission("pvparena.user")),
                    sender);
        }

        return arena.getArenaConfig().getBoolean(CFG.PERMS_EXPLICITARENA) ? sender
                .hasPermission("pvparena.join." + arena.getName().toLowerCase())
                : sender.hasPermission("pvparena.user");
    }

    public boolean isShuttingDown() {
        return shuttingDown;
    }

    private void loadArenaCommands() {
        arenaCommands.add(new PAA_ArenaClassChest());
        arenaCommands.add(new PAA_BlackList());
        arenaCommands.add(new PAA_Check());
        arenaCommands.add(new PAA_Class());
        arenaCommands.add(new PAA_Disable());
        arenaCommands.add(new PAA_Edit());
        arenaCommands.add(new PAA_Enable());
        arenaCommands.add(new PAA_ForceWin());
        arenaCommands.add(new PAA_GameMode());
        arenaCommands.add(new PAA_Goal());
        arenaCommands.add(new PAA_PlayerJoin());
        arenaCommands.add(new PAA_PlayerClass());
        arenaCommands.add(new PAA_Protection());
        arenaCommands.add(new PAA_Regions());
        arenaCommands.add(new PAA_Region());
        arenaCommands.add(new PAA_RegionClear());
        arenaCommands.add(new PAA_RegionFlag());
        arenaCommands.add(new PAA_RegionType());
        arenaCommands.add(new PAA_Reload());
        arenaCommands.add(new PAA_Remove());
        arenaCommands.add(new PAA_Round());
        arenaCommands.add(new PAA_Set());
        arenaCommands.add(new PAA_Setup());
        arenaCommands.add(new PAA_SetOwner());
        arenaCommands.add(new PAA_Spawn());
        arenaCommands.add(new PAA_Start());
        arenaCommands.add(new PAA_Stop());
        arenaCommands.add(new PAA_Teams());
        arenaCommands.add(new PAA_Teleport());
        arenaCommands.add(new PAA_Template());
        arenaCommands.add(new PAA_ToggleMod());
        arenaCommands.add(new PAA_WhiteList());
        arenaCommands.add(new PAG_Chat());
        arenaCommands.add(new PAG_Join());
        arenaCommands.add(new PAG_Leave());
        arenaCommands.add(new PAG_Spectate());
        arenaCommands.add(new PAI_List());
        arenaCommands.add(new PAI_Ready());
        arenaCommands.add(new PAI_Shutup());
        arenaCommands.add(new PAG_Arenaclass());
        arenaCommands.add(new PAI_Info());
    }

    private void loadGlobalCommands() {
        globalCommands.add(new PAA_Create());
        globalCommands.add(new PAA_Debug());
        globalCommands.add(new PAA_Duty());
        globalCommands.add(new PAI_Help());
        globalCommands.add(new PAA_Install());
        globalCommands.add(new PAA_Uninstall());
        globalCommands.add(new PAA_Update());
        globalCommands.add(new PAI_ArenaList());
        globalCommands.add(new PAI_Version());
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd,
                             final String commandLabel, final String[] args) {

        if (args.length < 1) {
            sender.sendMessage(ChatColor.COLOR_CHAR + "e"
                    + ChatColor.COLOR_CHAR + "l|-- PVP Arena --|");
            sender.sendMessage(ChatColor.COLOR_CHAR + "e"
                    + ChatColor.COLOR_CHAR + "o--By slipcor--");
            sender.sendMessage(ChatColor.COLOR_CHAR + "7"
                    + ChatColor.COLOR_CHAR + "oDo " + ChatColor.COLOR_CHAR
                    + "e/pa help " + ChatColor.COLOR_CHAR + '7'
                    + ChatColor.COLOR_CHAR + "ofor help.");
            return true;
        }

        if (args.length > 1 && sender.hasPermission("pvparena.admin")
                && "ALL".equalsIgnoreCase(args[0])) {
            final String[] newArgs = StringParser.shiftArrayBy(args, 1);
            for (final Arena arena : ArenaManager.getArenas()) {
                try {
                    Bukkit.getServer().dispatchCommand(
                            sender,
                            "pa " + arena.getName() + ' '
                                    + StringParser.joinArray(newArgs, " "));
                } catch (final Exception e) {
                    getLogger().warning("arena null!");
                }
            }
            return true;

        }

        AbstractGlobalCommand pacmd = null;
        for (final AbstractGlobalCommand agc : globalCommands) {
            if (agc.getMain().contains(args[0]) || agc.getShort().contains(args[0])) {
                pacmd = agc;
                break;
            }
        }
        final ArenaPlayer player = ArenaPlayer.parsePlayer(sender.getName());
        if (pacmd != null
                && !(player.getArena() != null && pacmd.getName()
                .contains("PAI_ArenaList"))) {
            DEBUG.i("committing: " + pacmd.getName(), sender);
            pacmd.commit(sender, StringParser.shiftArrayBy(args, 1));
            return true;
        }

        if ("-s".equalsIgnoreCase(args[0]) || "stats".equalsIgnoreCase(args[0])) {
            final PAI_Stats scmd = new PAI_Stats();
            DEBUG.i("committing: " + scmd.getName(), sender);
            scmd.commit(null, sender, StringParser.shiftArrayBy(args, 1));
            return true;
        }
        if (args.length > 1
                && (args[1].equalsIgnoreCase("-s") || args[1]
                .equalsIgnoreCase("stats"))) {
            final PAI_Stats scmd = new PAI_Stats();
            DEBUG.i("committing: " + scmd.getName(), sender);
            scmd.commit(ArenaManager.getIndirectArenaByName(sender, args[0]), sender,
                    StringParser.shiftArrayBy(args, 2));
            return true;
        }
        if (args[0].equalsIgnoreCase("!rl")
                || args[0].toLowerCase().contains("reload")) {
            final PAA_Reload scmd = new PAA_Reload();
            DEBUG.i("committing: " + scmd.getName(), sender);

            this.reloadConfig();

            final String[] emptyArray = new String[0];

            for (Arena a : ArenaManager.getArenas()) {
                scmd.commit(a, sender, emptyArray);
            }

            ArenaManager.load_arenas();
            if (getConfig().getBoolean("use_shortcuts") ||
                    getConfig().getBoolean("only_shortcuts")) {
                ArenaManager.readShortcuts(getConfig().getConfigurationSection("shortcuts"));
            }

            return true;
        }

        Arena tempArena = ArenaManager.getIndirectArenaByName(sender, args[0]);

        final String name = args[0];

        if (tempArena == null && Arrays.asList(args).contains("vote")) {
            tempArena = ArenaManager.getArenaByName(args[0]); // arenavote shortcut hack
        }

        String[] newArgs = args;

        if (tempArena == null) {
            if (sender instanceof Player
                    && ArenaPlayer.parsePlayer(sender.getName()).getArena() != null) {
                tempArena = ArenaPlayer.parsePlayer(sender.getName())
                        .getArena();
            } else if (PAA_Setup.activeSetups.containsKey(sender.getName())) {
                tempArena = PAA_Setup.activeSetups.get(sender.getName());
            } else if (PAA_Edit.activeEdits.containsKey(sender.getName())) {
                tempArena = PAA_Edit.activeEdits.get(sender.getName());
            } else if (ArenaManager.count() == 1) {
                tempArena = ArenaManager.getFirst();
            } else if (ArenaManager.count() < 1) {
                Arena.pmsg(sender, Language.parse(MSG.ERROR_NO_ARENAS));
                return true;
            } else if (ArenaManager.countAvailable() == 1) {
                tempArena = ArenaManager.getAvailable();
            }
        } else {
            if (args.length > 1) {
                newArgs = StringParser.shiftArrayBy(args, 1);
            }
        }

        latelounge:
        if (tempArena == null) {
            for (final Arena ar : ArenaManager.getArenas()) {
                for (final ArenaModule mod : ar.getMods()) {
                    if (mod.hasSpawn(sender.getName())) {
                        tempArena = ar;
                        break latelounge;
                    }
                }
            }

            Arena.pmsg(sender, Language.parse(MSG.ERROR_ARENA_NOTFOUND, name));
            return true;
        }

        AbstractArenaCommand paacmd = null;
        for (final AbstractArenaCommand aac : arenaCommands) {
            if (aac.getMain().contains(newArgs[0]) || aac.getShort().contains(newArgs[0])) {
                paacmd = aac;
                break;
            }

        }
        if (paacmd == null
                && PACheck.handleCommand(tempArena, sender, newArgs)) {
            return true;
        }

        if (paacmd == null
                && tempArena.getArenaConfig().getBoolean(CFG.CMDS_DEFAULTJOIN)) {
            paacmd = new PAG_Join();
            if (newArgs.length > 1) {
                newArgs = StringParser.shiftArrayBy(newArgs, 1);
            }
            tempArena.getDebugger()
                    .i("committing: " + paacmd.getName(), sender);
            paacmd.commit(tempArena, sender, newArgs);
            return true;
        }

        if (paacmd != null) {
            tempArena.getDebugger()
                    .i("committing: " + paacmd.getName(), sender);
            paacmd.commit(tempArena, sender,
                    StringParser.shiftArrayBy(newArgs, 1));
            return true;
        }
        tempArena.getDebugger().i("cmd null", sender);

        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String alias, final String[] args) {
        return TabManager.getMatches(sender, arenaCommands, globalCommands, args);
    }

    @Override
    public void onDisable() {
        shuttingDown = true;
        ArenaManager.reset(true);
        Tracker.stop();
        Debug.destroy();
        Language.logInfo(MSG.LOG_PLUGIN_DISABLED, getDescription()
                .getFullName());
    }

    @Override
    public void onEnable() {
        shuttingDown = false;
        instance = this;
        DEBUG = new Debug(1);

        saveDefaultConfig();
        if (!getConfig().contains("shortcuts")) {
            final List<String> ffa = new ArrayList<>();
            final List<String> teams = new ArrayList<>();

            ffa.add("arena1");
            ffa.add("arena2");

            teams.add("teamarena1");
            teams.add("teamarena2");

            getConfig().options().copyDefaults(true);
            getConfig().addDefault("shortcuts.freeforall", ffa);
            getConfig().addDefault("shortcuts.teams", teams);

            saveConfig();
        }

        if (!getConfig().contains("update.mode") && getConfig().contains("modulecheck")) {
            getConfig().set("update.mode", getConfig().getString("update", "both"));
            getConfig().set("update.type", getConfig().getString("updatetype", "beta"));
            getConfig().set("update.modules", getConfig().getBoolean("modulecheck", true));

            getConfig().set("modulecheck", null);
            getConfig().set("updatetype", null);

            saveConfig();
        }

        getDataFolder().mkdir();
        new File(getDataFolder().getPath() + "/arenas").mkdir();
        new File(getDataFolder().getPath() + "/goals").mkdir();
        new File(getDataFolder().getPath() + "/mods").mkdir();
        new File(getDataFolder().getPath() + "/regionshapes").mkdir();
        new File(getDataFolder().getPath() + "/dumps").mkdir();
        new File(getDataFolder().getPath() + "/files").mkdir();
        new File(getDataFolder().getPath() + "/templates").mkdir();

        agm = new ArenaGoalManager(this);
        amm = new ArenaModuleManager(this);
        arsm = new ArenaRegionShapeManager(this);

        loadArenaCommands();
        loadGlobalCommands();

        Language.init(getConfig().getString("language", "en"));
        Help.init(getConfig().getString("language", "en"));

        StatisticsManager.initialize();
        ArenaPlayer.initiate();

        getServer().getPluginManager()
                .registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new EntityListener(),
                this);
        getServer().getPluginManager().registerEvents(new PlayerListener(),
                this);
        getServer().getPluginManager().registerEvents(new InventoryListener(),
                this);

        if (getConfig().getInt("ver", 0) < 1) {
            getConfig().options().copyDefaults(true);
            getConfig().set("ver", 1);
            saveConfig();
        }

        Debug.load(this, Bukkit.getConsoleSender());
        ArenaClass.addGlobalClasses();
        ArenaManager.load_arenas();

        if (getConfig().getBoolean("use_shortcuts") ||
                getConfig().getBoolean("only_shortcuts")) {
            ArenaManager.readShortcuts(getConfig().getConfigurationSection("shortcuts"));
        }

        updater = new Updater(this, getFile(), true);

        if (ArenaManager.count() > 0) {
            if (PVPArena.instance.getConfig().getBoolean("tracker", true)) {
                final Tracker trackMe = new Tracker();
                trackMe.start();
            }

            try {
                final Metrics metrics = new Metrics(this);
                final Metrics.Graph atg = metrics
                        .createGraph("Game modes installed");
                for (final ArenaGoal at : agm.getAllGoals()) {
                    atg.addPlotter(new WrapPlotter(at.getName()));
                }
                final Metrics.Graph amg = metrics
                        .createGraph("Enhancement modules installed");
                for (final ArenaModule am : amm.getAllMods()) {
                    amg.addPlotter(new WrapPlotter(am.getName()));
                }
                final Metrics.Graph acg = metrics.createGraph("Arena count");
                acg.addPlotter(new WrapPlotter("count", ArenaManager
                        .getArenas().size()));

                metrics.start();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        Language.logInfo(MSG.LOG_PLUGIN_ENABLED, getDescription().getFullName());
    }

    private static class WrapPlotter extends Metrics.Plotter {
        private final int arenaCount;

        public WrapPlotter(final String name) {
            super(name);
            arenaCount = 1;
        }

        public WrapPlotter(final String name, final int count) {
            super(name);
            arenaCount = count;
        }

        @Override
        public int getValue() {
            return arenaCount;
        }
    }
}
