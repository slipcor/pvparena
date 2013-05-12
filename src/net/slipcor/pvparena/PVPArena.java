package net.slipcor.pvparena;

import java.io.File;
import java.io.IOException;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.commands.PAA_Reload;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.commands.PAI_Stats;
import net.slipcor.pvparena.commands.AbstractGlobalCommand;
import net.slipcor.pvparena.core.*;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.listeners.*;
import net.slipcor.pvparena.loadables.*;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.metrics.Metrics;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * <pre>
 * Main Plugin class
 * </pre>
 * 
 * contains central elements like plugin handlers and listeners
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class PVPArena extends JavaPlugin {
	public static PVPArena instance = null;

	private final static Debug DEBUG = new Debug(1);

	private ArenaGoalManager agm = null;
	private ArenaModuleManager amm = null;
	private ArenaRegionShapeManager arsm = null;

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
	
	/**
	 * Hand over the jar file name
	 * 
	 * @return the .jar file name
	 */
	public String getFileName() {
		return this.getFile().getName();
	}

	/**
	 * Check if a CommandSender has admin permissions
	 * 
	 * @param sender
	 *            the CommandSender to check
	 * @return true if a CommandSender has admin permissions, false otherwise
	 */
	public static boolean hasAdminPerms(final CommandSender sender) {
		return sender.hasPermission("pvparena.admin");
	}

	/**
	 * Check if a CommandSender has creation permissions
	 * 
	 * @param sender
	 *            the CommandSender to check
	 * @param arena
	 *            the arena to check
	 * @return true if the CommandSender has creation permissions, false
	 *         otherwise
	 */
	public static boolean hasCreatePerms(final CommandSender sender, final Arena arena) {
		return (sender.hasPermission("pvparena.create") && (arena == null || arena
				.getOwner().equals(sender.getName())));
	}

	/**
	 * Check if a CommandSender has permission for an arena
	 * 
	 * @param sender
	 *            the CommandSender to check
	 * @param arena
	 *            the arena to check
	 * @return true if explicit permission not needed or granted, false
	 *         otherwise
	 */
	public static boolean hasPerms(final CommandSender sender, final Arena arena) {
		DEBUG.i("perm check.", sender);
		if (arena.getArenaConfig().getBoolean(CFG.PERMS_EXPLICITARENA)) {
			DEBUG.i(" - explicit: "
					+ (sender.hasPermission("pvparena.join."
							+ arena.getName().toLowerCase())), sender);
		} else {
			DEBUG.i(String.valueOf(sender.hasPermission("pvparena.user")),
					sender);
		}

		return arena.getArenaConfig().getBoolean(CFG.PERMS_EXPLICITARENA) ? sender
				.hasPermission("pvparena.join." + arena.getName().toLowerCase())
				: sender.hasPermission("pvparena.user");
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd,
			final String commandLabel, final String[] args) {

		if (args.length < 1) {
			sender.sendMessage("§e§l|-- PVP Arena --|");
			sender.sendMessage("§e§o--By slipcor--");
			sender.sendMessage("§7§oDo §e/pa help §7§ofor help.");
			return true;
		}
		
		if (args.length > 1 && sender.hasPermission("pvparena.admin")
				&& args[0].equalsIgnoreCase("ALL")) {
				final String [] newArgs = StringParser.shiftArrayBy(args, 1);
				for (Arena arena : ArenaManager.getArenas()) {
					try {
						Bukkit.getServer().dispatchCommand(
								sender,
								"pa " + arena.getName() + " "
										+ StringParser.joinArray(newArgs, " "));
					} catch (Exception e) {
						getLogger().warning("arena null!");
					}
				}
				return true;
			
		}

		final AbstractGlobalCommand pacmd = AbstractGlobalCommand.getByName(args[0]);

		if (pacmd != null
				&& !((ArenaPlayer.parsePlayer(sender.getName()).getArena() != null) && (pacmd
						.getName().contains("PAI_ArenaList")))) {
			DEBUG.i("committing: " + pacmd.getName(), sender);
			pacmd.commit(sender, StringParser.shiftArrayBy(args, 1));
			return true;
		}

		if (args[0].equalsIgnoreCase("-s")
				|| args[0].equalsIgnoreCase("stats")) {
			final PAI_Stats scmd = new PAI_Stats();
			DEBUG.i("committing: " + scmd.getName(), sender);
			scmd.commit(null, sender, StringParser.shiftArrayBy(args, 1));
			return true;
		} else if (args.length > 1 &&
				(args[1].equalsIgnoreCase("-s")
				|| args[1].equalsIgnoreCase("stats"))) {
			final PAI_Stats scmd = new PAI_Stats();
			DEBUG.i("committing: " + scmd.getName(), sender);
			scmd.commit(ArenaManager.getArenaByName(args[0]), sender, StringParser.shiftArrayBy(args, 2));
			return true;
		} else if (args[0].equalsIgnoreCase("!rl")
				|| args[0].toLowerCase().contains("reload")) {
			final PAA_Reload scmd = new PAA_Reload();
			DEBUG.i("committing: " + scmd.getName(), sender);
			
			this.reloadConfig();
			
			final String[] emptyArray = new String[0];
			
			for (Arena a : ArenaManager.getArenas()) {
				scmd.commit(a, sender, emptyArray);
			}
			
			ArenaManager.load_arenas();
			
			return true;
		}

		Arena tempArena = ArenaManager.getArenaByName(args[0]);

		final String name = args[0];
		
		String[] newArgs = args;

		if (tempArena == null) {
			if (sender instanceof Player
					&& ArenaPlayer.parsePlayer(sender.getName()).getArena() != null) {
				tempArena = ArenaPlayer.parsePlayer(sender.getName()).getArena();
			} else if (PAA_Edit.activeEdits.containsKey(sender.getName())) {
				tempArena = PAA_Edit.activeEdits.get(sender.getName());
			} else if (ArenaManager.count() == 1) {
				tempArena = ArenaManager.getFirst();
			} else if (ArenaManager.count() < 1) {
				Arena.pmsg(sender, Language.parse(MSG.ERROR_NO_ARENAS));
				return true;
			}
		} else {
			if (args != null && args.length > 1) {
				newArgs = StringParser.shiftArrayBy(args, 1);
			}
		}

		if (tempArena == null) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ARENA_NOTFOUND, name));
			return true;
		}

		AbstractArenaCommand paacmd = AbstractArenaCommand.getByName(newArgs[0]);
		if (paacmd == null && (PACheck.handleCommand(tempArena, sender, newArgs))) {
			return true;
		}

		if (paacmd == null
				&& tempArena.getArenaConfig().getBoolean(CFG.CMDS_DEFAULTJOIN)) {
			paacmd = new PAG_Join();
			if (newArgs.length > 1) {
				newArgs = StringParser.shiftArrayBy(newArgs, 1);
			}
			DEBUG.i("committing: " + paacmd.getName(), sender);
			paacmd.commit(tempArena, sender, newArgs);
			return true;
		}

		if (paacmd != null) {
			DEBUG.i("committing: " + paacmd.getName(), sender);
			paacmd.commit(tempArena, sender, StringParser.shiftArrayBy(newArgs, 1));
			return true;
		}
		DEBUG.i("cmd null", sender);
		
		return false;
	}

	@Override
	public void onDisable() {
		ArenaManager.reset(true);
		Tracker.stop();
		Language.logInfo(MSG.LOG_PLUGIN_DISABLED, getDescription()
				.getFullName());
	}

	@Override
	public void onEnable() {
		instance = this;
		
		this.saveDefaultConfig();

		getDataFolder().mkdir();
		new File(getDataFolder().getPath() + "/arenas").mkdir();
		new File(getDataFolder().getPath() + "/goals").mkdir();
		new File(getDataFolder().getPath() + "/mods").mkdir();
		new File(getDataFolder().getPath() + "/regionshapes").mkdir();
		new File(getDataFolder().getPath() + "/dumps").mkdir();
		new File(getDataFolder().getPath() + "/files").mkdir();

		agm = new ArenaGoalManager(this);
		amm = new ArenaModuleManager(this);
		arsm = new ArenaRegionShapeManager(this);

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
		ArenaManager.load_arenas();
		new Update(this);

		if (ArenaManager.count() > 0) {

			final Tracker trackMe = new Tracker();
			trackMe.start();

			Metrics metrics;
			try {
				metrics = new Metrics(this);
				final Metrics.Graph atg = metrics.createGraph("Game modes installed");
				for (ArenaGoal at : agm.getAllGoals()) {
					atg.addPlotter(new WrapPlotter(at.getName()));
				}
				final Metrics.Graph amg = metrics
						.createGraph("Enhancement modules installed");
				for (ArenaModule am : amm.getAllMods()) {
					amg.addPlotter(new WrapPlotter(am.getName()));
				}
				final Metrics.Graph acg = metrics.createGraph("Arena count");
				acg.addPlotter(new WrapPlotter("count", ArenaManager
						.getArenas().size()));

				metrics.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		Language.logInfo(MSG.LOG_PLUGIN_ENABLED, getDescription()
				.getFullName());
	}

	private class WrapPlotter extends Metrics.Plotter {
		final private int arenaCount;

		public WrapPlotter(final String name) {
			super(name);
			arenaCount = 1;
		}

		public WrapPlotter(final String name, final int count) {
			super(name);
			arenaCount = count;
		}

		public int getValue() {
			return arenaCount;
		}
	}
}
