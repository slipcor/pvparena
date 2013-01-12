package net.slipcor.pvparena.core;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * <pre>
 * Debug class
 * </pre>
 * 
 * provides methods for logging when in debug mode
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class Debug {
	public static boolean override = false;

	private static String prefix = "[PA-debug] ";
	private static HashSet<Integer> check = new HashSet<Integer>();
	private static HashSet<String> strings = new HashSet<String>();

	private int id = 0;

	/**
	 * Debug constructor
	 * 
	 * @param i
	 *            the debug id to check
	 */
	public Debug(int i) {
		id = i;
	}

	public void debug() {
		PVPArena.instance.getLogger().info("debugger: " + id);
	}

	/**
	 * does this class debug?
	 * 
	 * @return true if debugs, false otherwise
	 */
	private boolean debugs() {
		return override || check.contains(id) || check.contains(666);
	}

	private boolean debugs(String s) {
		return override || strings.contains(s) || check.contains(666);
	}

	/**
	 * log a message as prefixed INFO
	 * 
	 * @param s
	 *            the message
	 */
	public void i(String s) {
		if (!debugs()) {
			return;
		}
		Bukkit.getLogger().info(prefix + s);
	}

	public void i(String string, CommandSender sender) {
		if (sender == null) {
			i(string, "null");
			return;
		}
		if (!debugs(sender.getName())) {
			return;
		}
		Bukkit.getLogger().info(prefix + string);
	}

	public void i(String string, String filter) {
		if (!debugs(filter)) {
			return;
		}
		Bukkit.getLogger().info(prefix + string);
	}

	public static void load(PVPArena instance, CommandSender sender) {
		check.clear();
		strings.clear();
		override = false;
		String debugs = instance.getConfig().getString("debug");
		if (!debugs.equals("none")) {
			if (debugs.equals("all") || debugs.equals("full")) {
				Debug.check.add(666);
				override = true;
				Arena.pmsg(sender, "debugging EVERYTHING");
			} else {
				String[] sIds = debugs.split(",");
				Arena.pmsg(sender, "debugging: " + debugs);
				for (String s : sIds) {
					try {
						Debug.check.add(Integer.valueOf(s));
					} catch (Exception e) {
						strings.add(s);
					}
				}
			}
		} else {
			Arena.pmsg(sender, "debugging: off");
		}
	}
}
