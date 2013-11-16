package net.slipcor.pvparena.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 * <pre>
 * Debug class
 * </pre>
 * 
 * provides methods for logging when in debug mode
 * 
 * @author slipcor
 */

public class Debug {
	public static boolean override = false;
	public static boolean server_log = false;

	private static String prefix = "[PA-debug] ";
	private static Set<Integer> check = new HashSet<Integer>();
	private static Set<String> strings = new HashSet<String>();

	private final int debugID;

	private static Logger logger = null;
	private Logger arenaLogger = null;
	
	private static List<Logger> loggers = new ArrayList<Logger>();
	private static List<Debug> debugs = new ArrayList<Debug>();	
	private Arena arena = null;

	public Debug(final int iID) {
		this(iID, null);
	}
	
	private static Logger getGlobalLogger() {
		if (logger == null) {
	        logger = Logger.getAnonymousLogger();
	        logger.setLevel(Level.ALL);
	        logger.setUseParentHandlers(false);
	        
	        for (Handler handler : logger.getHandlers()) {
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
	        } catch (IOException ex) {
	        	PVPArena.instance.getLogger().log(Level.SEVERE, null, ex);
	        } catch (SecurityException ex) {
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
	        
	        for (Handler handler : arenaLogger.getHandlers()) {
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
	        } catch (IOException ex) {
	        	PVPArena.instance.getLogger().log(Level.SEVERE, null, ex);
	        } catch (SecurityException ex) {
	        	PVPArena.instance.getLogger().log(Level.SEVERE, null, ex);
	        }
    	}
		return arenaLogger;
	}
	
	/**
	 * Debug constructor
	 * 
	 * @param iID
	 *            the debug id to check
	 */
	public Debug(final int iID, final Arena arena) {
		debugID = iID;

		if (arena != null) {
        	this.arena = arena;
        }
	}

	public Debug(Arena arena) {
		this(-1,arena);
	}

	/**
	 * does this class debug?
	 * 
	 * @return true if debugs, false otherwise
	 */
	private boolean debugs() {
		return override || check.contains(debugID) || check.contains(666);
	}

	private boolean debugs(final String term) {
		return override || strings.contains(term) || check.contains(666);
	}

	/**
	 * log a message as prefixed INFO
	 * 
	 * @param string
	 *            the message
	 */
	public void i(final String string) {
		if (!debugs()) {
			return;
		}
		if (arena == null) {
			getGlobalLogger().info(prefix + System.currentTimeMillis()%1000 + " " + string);
		} else {
			getArenaLogger().info(prefix + System.currentTimeMillis()%1000 + " " + string);
		}
		if (server_log) {
			System.out.print(prefix + System.currentTimeMillis()%1000 + " " + string);
		}
	}

	public void i(final String string, final CommandSender sender) {
		if (sender == null) {
			i(string, "null");
			return;
		}
		if (!debugs(sender.getName())) {
			return;
		}
		if (arena == null && (sender instanceof Player)) {
			ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
			if (ap.getArena() != null) {
				ap.getArena().getDebugger().i(string);
				return;
			}
		}
		if (arena == null) {
			getGlobalLogger().info(prefix + "[p:"+sender.getName()+"]" + System.currentTimeMillis()%1000 + " " + string);
		} else {
			getArenaLogger().info(prefix + "[p:"+sender.getName()+"]" + System.currentTimeMillis()%1000 + " " + string);
		}
		if (server_log) {
			System.out.print(prefix + " ["+arena+"] " + "[p:"+sender.getName()+"]" + System.currentTimeMillis()%1000 + " " + string);
		}
	}

	public void i(final String string, final String filter) {
		if (!debugs(filter)) {
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(filter);
		if (ap.getArena() != null) {
			ap.getArena().getDebugger().i(string);
			return;
		}

		//Bukkit.getLogger().info(prefix + System.currentTimeMillis()%1000 + " " + string);
		getGlobalLogger().info(prefix + System.currentTimeMillis()%1000 + " " + string);
		if (server_log) {
			System.out.print(prefix + System.currentTimeMillis()%1000 + " " + string);
		}
	}

	public static void load(final PVPArena instance, final CommandSender sender) {
		check.clear();
		strings.clear();
		override = false;
		
		final String debugs = instance.getConfig().getString("debug");
		
		for (Debug debug : Debug.debugs) {
			debug.arenaLogger = null;
		}
		
		loggers.clear();
		
		if (debugs.equals("none")) {
			Arena.pmsg(sender, "debugging: off");
			
		} else {
			
			server_log = instance.getConfig().getBoolean("server_log");
			if (debugs.equals("all") || debugs.equals("full")) {
				Debug.check.add(666);
				override = true;
				Arena.pmsg(sender, "debugging EVERYTHING");
			} else {
				final String[] sIds = debugs.split(",");
				Arena.pmsg(sender, "debugging: " + debugs);
				for (String s : sIds) {
					try {
						Debug.check.add(Integer.valueOf(s));
					} catch (Exception e) {
						strings.add(s);
					}
				}
			}
		}
	}
	
	public static void destroy() {
		
		for (Logger log : Debug.loggers) {
			Handler[] handlers = log.getHandlers().clone();
			for (Handler hand : handlers) {
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
            this.date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        }

        public String format(final LogRecord record) {
            final StringBuilder builder = new StringBuilder();
            final Throwable exception = record.getThrown();

            builder.append(this.date.format(Long.valueOf(record.getMillis())));
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
