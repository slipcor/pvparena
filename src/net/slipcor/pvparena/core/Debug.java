package net.slipcor.pvparena.core;

import java.util.HashSet;
import java.util.Set;

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
	private static Set<Integer> check = new HashSet<Integer>();
	private static Set<String> strings = new HashSet<String>();

	private final int debugID;

	/**
	 * Debug constructor
	 * 
	 * @param iID
	 *            the debug id to check
	 */
	public Debug(final int iID) {
		debugID = iID;
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
		Bukkit.getLogger().info(prefix + System.currentTimeMillis()%1000 + " " + string);
	}

	public void i(final String string, final CommandSender sender) {
		if (sender == null) {
			i(string, "null");
			return;
		}
		if (!debugs(sender.getName())) {
			return;
		}
		Bukkit.getLogger().info(prefix + System.currentTimeMillis()%1000 + " " + string);
	}

	public void i(final String string, final String filter) {
		if (!debugs(filter)) {
			return;
		}

		Bukkit.getLogger().info(prefix + System.currentTimeMillis()%1000 + " " + string);
	}

	public static void load(final PVPArena instance, final CommandSender sender) {
		check.clear();
		strings.clear();
		override = false;
		final String debugs = instance.getConfig().getString("debug");
		if (debugs.equals("none")) {
			Arena.pmsg(sender, "debugging: off");
		} else {
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
}
