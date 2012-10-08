package net.slipcor.pvparena;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.commands.PAA__Command;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.commands.PAI_Stats;
import net.slipcor.pvparena.commands.PA__Command;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Tracker;
import net.slipcor.pvparena.core.Update;
import net.slipcor.pvparena.listeners.BlockListener;
import net.slipcor.pvparena.listeners.InventoryListener;
import net.slipcor.pvparena.listeners.EntityListener;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaGoalManager;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegionShapeManager;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.nodinchan.ncbukkit.NCBL;

/**
 * <pre>Main Plugin class</pre>
 * 
 * contains central elements like plugin handlers and listeners
 * 
 * @author slipcor
 * 
 * @version v0.9.2
 */

public class PVPArena extends JavaPlugin {
	public static PVPArena instance = null;

	private final static Debug db = new Debug(1);

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
	 * Check if a CommandSender has admin permissions
	 * 
	 * @param sender
	 *            the CommandSender to check
	 * @return true if a CommandSender has admin permissions, false otherwise
	 */
	public static boolean hasAdminPerms(CommandSender sender) {
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
	public static boolean hasCreatePerms(CommandSender sender, Arena arena) {
		return (sender.hasPermission("pvparena.create") &&
				(arena == null || arena.getOwner().equals(sender.getName())));
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
	public static boolean hasPerms(CommandSender sender, Arena arena) {
		db.i("perm check.");
		if (arena.getArenaConfig().getBoolean(CFG.PERMS_EXPLICITARENA)) {
			db.i(" - explicit: "
					+ String.valueOf(sender.hasPermission("pvparena.join."
							+ arena.getName().toLowerCase())));
		} else {
			db.i(String.valueOf(sender.hasPermission("pvparena.user")));
		}

		return arena.getArenaConfig().getBoolean(CFG.PERMS_EXPLICITARENA) ? sender
				.hasPermission("pvparena.join." + arena.getName().toLowerCase())
				: sender.hasPermission("pvparena.user");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {

		if (args.length < 1) {
			return false;
		}

		PA__Command pacmd = PA__Command.getByName(args[0]);

		if (pacmd != null && ArenaPlayer.parsePlayer(sender.getName()).getArena() != null) {
			db.i("committing: " + pacmd.getName());
			pacmd.commit(sender, StringParser.shiftArrayBy(args, 1));
			return true;
		}

		if (args[0].equalsIgnoreCase("-s")
				|| args[0].toLowerCase().contains("stats")) {
			PAI_Stats scmd = new PAI_Stats();
			db.i("committing: " + scmd.getName());
			scmd.commit(null, sender, new String[0]);
			return true;
		}

		Arena a = ArenaManager.getArenaByName(args[0]);
		
		String name = args[0];
		
		if (a == null) {
			if (sender instanceof Player && ArenaPlayer.parsePlayer(sender.getName()).getArena() != null) {
				a = ArenaPlayer.parsePlayer(sender.getName()).getArena();
			} else if (PAA_Edit.activeEdits.containsKey(sender.getName())) {
				a = PAA_Edit.activeEdits.get(sender.getName());
			} else if (ArenaManager.count() == 1) {
				a = ArenaManager.getFirst();
			} else if (ArenaManager.count() < 1) {
				Arena.pmsg(sender, Language.parse(MSG.ERROR_NO_ARENAS));
				return true;
			}
		} else {
			if (args != null && args.length > 1)
				args = StringParser.shiftArrayBy(args, 1);
		}
		
		if (a == null) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ARENA_NOTFOUND, name));
			return true;
		}
		

		PAA__Command paacmd = PAA__Command.getByName(args[0]);
		if (paacmd == null) {
			ArenaGoal goal = PVPArena.instance.getAgm().checkCommand(a, args[0]);
			if (goal != null) {
				db.i("committing: " + goal.getName());
				goal.commitCommand(sender, args);
				return true;
			}
		}

		if (paacmd == null && a.getArenaConfig().getBoolean(CFG.CMDS_DEFAULTJOIN)) {
			paacmd = new PAG_Join();
		}
		if (paacmd != null) {
			db.i("committing: " + paacmd.getName());
			paacmd.commit(a, sender, StringParser.shiftArrayBy(args, 1));
			return true;
		}
		db.i("cmd null");
		
		return false;
	}

	@Override
	public void onDisable() {
		ArenaManager.reset(true);
		Tracker.stop();
		Language.log_info(MSG.LOG_PLUGIN_DISABLED, getDescription().getFullName());
	}

	@Override
	public void onEnable() {
		instance = this;

		getDataFolder().mkdir();
		new File(getDataFolder().getPath() + "/arenas").mkdir();
		new File(getDataFolder().getPath() + "/goals").mkdir();
		new File(getDataFolder().getPath() + "/mods").mkdir();
		new File(getDataFolder().getPath() + "/regionshapes").mkdir();
		new File(getDataFolder().getPath() + "/dumps").mkdir();
		new File(getDataFolder().getPath() + "/files").mkdir();

		updateLib();

		agm = new ArenaGoalManager(this);
		amm = new ArenaModuleManager(this);
		arsm = new ArenaRegionShapeManager(this);

		Language.init(getConfig().getString("language", "en"));
		
		StatisticsManager.initialize();
		ArenaPlayer.initiate();

		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);

		int config_version = 1;

		if (getConfig().getInt("ver", 0) < config_version) {
			getConfig().options().copyDefaults(true);
			getConfig().set("ver", config_version);
			saveConfig();
		}

		Debug.load(this, Bukkit.getConsoleSender());
		ArenaManager.load_arenas();
		new Update(this);

		if (ArenaManager.count() > 0) {

			Tracker trackMe = new Tracker(this);
			trackMe.start();

			Metrics metrics;
			try {
				metrics = new Metrics(this);
				Metrics.Graph atg = metrics.createGraph("Game modes installed");
				for (ArenaGoal at : agm.getTypes()) {
					atg.addPlotter(new WrapPlotter(at.getName()));
				}
				Metrics.Graph amg = metrics
						.createGraph("Enhancement modules installed");
				for (ArenaModule am : amm.getModules()) {
					amg.addPlotter(new WrapPlotter(am.getName()));
				}
				Metrics.Graph acg = metrics.createGraph("Arena count");
				acg.addPlotter(new WrapPlotter("count", ArenaManager.getArenas()
						.size()));

				metrics.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		amm.onEnable();

		Language.log_info(MSG.LOG_PLUGIN_ENABLED, getDescription().getFullName());
	}

	/**
	 * Checks for update of the library
	 */
	private void updateLib() {
		PluginManager pm = getServer().getPluginManager();

		NCBL libPlugin = (NCBL) pm.getPlugin("NC-BukkitLib");

		File destination = new File(getDataFolder().getParentFile()
				.getParentFile(), "lib");
		destination.mkdirs();

		File lib = new File(destination, "NC-BukkitLib.jar");
		File pluginLib = new File(getDataFolder().getParentFile(),
				"NC-BukkitLib.jar");

		boolean inPlugins = false;
		boolean download = false;

		try {
			URL url = new URL("http://bukget.org/api/plugin/nc-bukkitlib");

			JSONObject jsonPlugin = (JSONObject) new JSONParser()
					.parse(new InputStreamReader(url.openStream()));
			JSONArray versions = (JSONArray) jsonPlugin.get("versions");

			if (libPlugin == null) {
				getLogger().warning("Missing NC-Bukkit lib");
				inPlugins = true;
				download = true;

			} else {
				double currentVer = libPlugin.getVersion();
				double newVer = currentVer;

				for (int ver = 0; ver < versions.size(); ver++) {
					JSONObject version = (JSONObject) versions.get(ver);

					if (version.get("type").equals("Release")) {
						String[] split = ((String) version.get("name"))
								.split(" ");
						newVer = Double
								.parseDouble(split[split.length-1].trim().substring(1));
						break;
					}
				}

				if (newVer > currentVer) {
					getLogger().warning("NC-Bukkit lib outdated");
					download = true;
				}
			}

			if (download) {
				getLogger().info("Downloading NC-Bukkit lib");

				String dl_link = "";

				for (int ver = 0; ver < versions.size(); ver++) {
					JSONObject version = (JSONObject) versions.get(ver);

					if (version.get("type").equals("Release")) {
						dl_link = (String) version.get("dl_link");
						break;
					}
				}

				if (dl_link == null)
					throw new Exception();

				URL link = new URL(dl_link);
				ReadableByteChannel rbc = Channels
						.newChannel(link.openStream());

				if (inPlugins) {
					FileOutputStream output = new FileOutputStream(pluginLib);
					output.getChannel().transferFrom(rbc, 0, 1 << 24);
					libPlugin = (NCBL) pm.loadPlugin(pluginLib);

				} else {
					FileOutputStream output = new FileOutputStream(lib);
					output.getChannel().transferFrom(rbc, 0, 1 << 24);
				}

				libPlugin.hook(this);

				getLogger().info("Downloaded NC-Bukkit lib");
			}

		} catch (Exception e) {
			getLogger().warning("Failed to check for library update");
			e.printStackTrace();
		}
	}

	private class WrapPlotter extends Metrics.Plotter {
		int i = 1;

		public WrapPlotter(String name) {
			super(name);
		}

		public WrapPlotter(String name, int count) {
			super(name);
			i = count;
		}

		public int getValue() {
			return i;
		}
	}
}
