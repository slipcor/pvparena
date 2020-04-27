package net.slipcor.pvparena.core;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.*;


/**
 * <pre>
 * Debug class
 * </pre>
 * <p/>
 * provides methods for logging when in debug mode
 *
 * @author slipcor
 */

public class Debug {
    public static boolean override;
    private static boolean server_log;

    private static final String prefix = "[PA-debug] ";
    private static final Set<Integer> check = new HashSet<>();
    private static final Set<String> strings = new HashSet<>();

    private final int debugID;

    private static Logger logger;
    private Logger arenaLogger;

    private static final List<Logger> loggers = new ArrayList<>();
    private static final List<Debug> debugs = new ArrayList<>();
    private Arena arena;
    private boolean active;

    public Debug(final int iID) {
        this(iID, null);
    }

    private static Logger getGlobalLogger() {
        if (logger == null) {
            logger = Logger.getAnonymousLogger();
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);

            for (final Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
            }

            try {
                final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

                final File debugFolder = new File(PVPArena.instance.getDataFolder(), "debug");


                debugFolder.mkdirs();
                final File logFile = new File(debugFolder, dateformat.format(new Date()) + "general.log");
                logFile.createNewFile();

                final FileHandler handler = new FileHandler(logFile.getAbsolutePath());

                handler.setFormatter(LogFileFormatter.newInstance());

                logger.addHandler(handler);

                loggers.add(logger);
            } catch (final IOException | SecurityException ex) {
                PVPArena.instance.getLogger().log(Level.SEVERE, null, ex);
            }
        }

        return logger;
    }

    private Logger getArenaLogger() {
        if (arenaLogger == null) {
            arenaLogger = Logger.getAnonymousLogger();
            arenaLogger.setLevel(Level.ALL);
            arenaLogger.setUseParentHandlers(false);

            for (final Handler handler : arenaLogger.getHandlers()) {
                arenaLogger.removeHandler(handler);
            }

            try {
                final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

                final File debugMainFolder = new File(PVPArena.instance.getDataFolder(), "debug");
                debugMainFolder.mkdirs();

                final File debugFolder = new File(debugMainFolder, arena.getName());
                debugFolder.mkdirs();
                final File logFile = new File(debugFolder, dateformat.format(new Date()) + ".log");

                final FileHandler handler = new FileHandler(logFile.getAbsolutePath());

                handler.setFormatter(LogFileFormatter.newInstance());

                arenaLogger.addHandler(handler);
                loggers.add(arenaLogger);
                debugs.add(this);
            } catch (final IOException | SecurityException ex) {
                PVPArena.instance.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        return arenaLogger;
    }

    /**
     * Debug constructor
     *
     * @param iID the debug id to check
     */
    private Debug(final int iID, final Arena arena) {
        debugID = iID;

        if (arena != null) {
            this.arena = arena;
        }
    }

    public Debug(final Arena arena) {
        this(-1, arena);
    }

    /**
     * does this class debug?
     *
     * @return true if debugs, false otherwise
     */
    private boolean debugs() {
        return override || active || check.contains(debugID) || check.contains(666);
    }

    private boolean debugs(final String term) {
        return override || active || strings.contains(term) || check.contains(666);
    }

    /**
     * log a message as prefixed INFO
     *
     * @param string the message
     */
    public void i(final String string) {
        if (!debugs()) {
            return;
        }
        if (arena == null) {
            getGlobalLogger().info(prefix + System.currentTimeMillis() % 1000 + ' ' + string);
        } else {
            getArenaLogger().info(prefix + System.currentTimeMillis() % 1000 + ' ' + string);
        }
        if (server_log) {
            System.out.print(prefix + System.currentTimeMillis() % 1000 + ' ' + string);
        }
    }

    public void i(final String string, final CommandSender sender) {
        if (arena == null && sender == null) {
            i(string, "null");
            return;
        }
        if (sender == null) {
            arena.getDebugger().i(string);
            return;
        }
        if (!debugs(sender.getName())) {
            return;
        }
        if (arena == null && sender instanceof Player) {
            final ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
            if (ap.getArena() != null) {
                ap.getArena().getDebugger().i(string);
                return;
            }
        }
        if (arena == null) {
            getGlobalLogger().info(prefix + "[p:" + sender.getName() + ']' + System.currentTimeMillis() % 1000 + ' ' + string);
        } else {
            getArenaLogger().info(prefix + "[p:" + sender.getName() + ']' + System.currentTimeMillis() % 1000 + ' ' + string);
        }
        if (server_log) {
            System.out.print(prefix + " [" + arena + "] " + "[p:" + sender.getName() + ']' + System.currentTimeMillis() % 1000 + ' ' + string);
        }
    }

    public void i(final String string, final String filter) {
        if (!debugs(filter)) {
            return;
        }

        final ArenaPlayer ap = ArenaPlayer.parsePlayer(filter);
        if (ap.getArena() != null) {
            ap.getArena().getDebugger().i(string);
            return;
        }

        //Bukkit.getLogger().info(prefix + System.currentTimeMillis()%1000 + " " + string);
        getGlobalLogger().info(prefix + System.currentTimeMillis() % 1000 + ' ' + string);
        if (server_log) {
            System.out.print(prefix + System.currentTimeMillis() % 1000 + ' ' + string);
        }
    }

    public static void load(final PVPArena instance, final CommandSender sender) {
        check.clear();
        strings.clear();
        override = false;
        boolean isPlayer = sender instanceof Player;

        final String debugs = instance.getConfig().getString("debug");

        for (final Debug debug : Debug.debugs) {
            debug.arenaLogger = null;
        }
        logger = null;

        for (Logger logger : loggers) {
            for (Handler handler : logger.getHandlers()) {
                handler.close();
            }
        }

        loggers.clear();

        for (final Arena a : ArenaManager.getArenas()) {
            a.renewDebugger();
        }

        if ("none".equals(debugs)) {
            if (isPlayer) {
                Arena.pmsg(sender, "debugging: off");
            } else {
                PVPArena.instance.getLogger().info("debugging: off");
            }

        } else {

            server_log = instance.getConfig().getBoolean("server_log");
            if ("all".equalsIgnoreCase(debugs) || "full".equalsIgnoreCase(debugs)) {
                Debug.check.add(666);
                override = true;
                if (isPlayer) {
                    Arena.pmsg(sender, "debugging EVERYTHING");
                } else {
                    PVPArena.instance.getLogger().info("debugging EVERYTHING");
                }
            } else {
                final String[] sIds = debugs.split(",");
                if (isPlayer) {
                    Arena.pmsg(sender, "debugging: " + debugs);
                } else {
                    PVPArena.instance.getLogger().info("debugging: " + debugs);
                }
                for (final String s : sIds) {
                    try {
                        Debug.check.add(Integer.valueOf(s));
                    } catch (final Exception e) {
                        strings.add(s);
                        final Arena a = ArenaManager.getArenaByName(s);
                        if (a != null) {
                            a.getDebugger().activate();
                        }
                    }
                }
            }
        }
    }

    private void activate() {
        active = true;
    }

    public static void destroy() {

        for (final Logger log : Debug.loggers) {
            final Handler[] handlers = log.getHandlers().clone();
            for (final Handler hand : handlers) {
                hand.close();
                log.removeHandler(hand);
            }
        }
        Debug.loggers.clear();
    }


    static class LogFileFormatter extends Formatter {

        private final SimpleDateFormat date;

        public static LogFileFormatter newInstance() {
            return new LogFileFormatter();
        }

        private LogFileFormatter() {
            super();
            date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        }

        @Override
        public String format(final LogRecord record) {
            final StringBuilder builder = new StringBuilder();
            final Throwable exception = record.getThrown();

            builder.append(date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(record.getMessage());
            builder.append('\n');

            if (exception != null) {
                final StringWriter writer = new StringWriter();
                exception.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }
}
