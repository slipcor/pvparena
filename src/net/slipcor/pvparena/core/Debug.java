package net.slipcor.pvparena.core;

import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.ArenaManager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * <pre>Debug class</pre>
 * 
 * provides methods for logging when in debug mode
 * 
 * @author slipcor
 * 
 * @version v0.9.6
 */

public class Debug {
	public static boolean override = false;

	private static String prefix = "[PA-debug] ";
	private static HashSet<Integer> check = new HashSet<Integer>();

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
		System.out.print("debugger: " + id);
	}

	/**
	 * does this class debug?
	 * 
	 * @return true if debugs, false otherwise
	 */
	private boolean debugs() {
		return override || check.contains(id) || check.contains(666);
	}

	/**
	 * log a message as prefixed INFO
	 * 
	 * @param s
	 *            the message
	 */
	public void i(String s) {
		if (!debugs())// || level < 1
			return;
		Bukkit.getLogger().info(prefix + s);
	}

	/**
	 * log a message as prefixed WARNING
	 * 
	 * @param s
	 *            the message
	 */
	public void w(String s) {
		if (!debugs())// || level < 2
			return;
		Bukkit.getLogger().warning(prefix + s);
	}

	/**
	 * log a message as prefixed SEVERE
	 * 
	 * @param s
	 *            the message
	 */
	public void s(String s) {
		if (!debugs())// || level < 3
			return;
		Bukkit.getLogger().severe(prefix + s);
	}

	public static void load(PVPArena instance, CommandSender sender) {
		Debug.check.clear();
		override = false;
		String debugs = instance.getConfig().getString("debug");
		if (!debugs.equals("none")) {
			if (debugs.equals("all") || debugs.equals("full")) {
				Debug.check.add(666);
				override = true;
				ArenaManager.tellPlayer(sender, "debugging EVERYTHING");
			} else {
				String[] sIds = debugs.split(",");
				ArenaManager.tellPlayer(sender, "debugging: "+debugs);
				for (String s : sIds) {
					try {
						Debug.check.add(Integer.valueOf(s));
					} catch (Exception e) {
						ArenaManager.tellPlayer(sender, "debug load error: " + s);
					}
				}
			}
		} else {
			ArenaManager.tellPlayer(sender, "debugging: off");
		}
	}
}
