package net.slipcor.pvparena.core;

import org.bukkit.Bukkit;

/**
 * debug manager class
 * 
 * -
 * 
 * provides methods for logging when in debug mode
 * 
 * @author slipcor
 * 
 * @version v0.6.0
 * 
 */

public class Debug {
	public static boolean active;
	public static String prefix = "[PA-debug] "; 

	/**
	 * log a message as prefixed INFO
	 * 
	 * @param s
	 *            the message
	 */
	public void i(String s) {
		if (!active)
			return;
		Bukkit.getLogger().info(prefix+s);
	}

	/**
	 * log a message as prefixed WARNING
	 * 
	 * @param s
	 *            the message
	 */
	public void w(String s) {
		if (!active)
			return;
		Bukkit.getLogger().warning(prefix+s);
	}

	/**
	 * log a message as prefixed SEVERE
	 * 
	 * @param s
	 *            the message
	 */
	public void s(String s) {
		if (!active)
			return;
		Bukkit.getLogger().severe(prefix+s);
	}

	/**
	 * read a string array and return a readable string
	 * 
	 * @param s
	 *            the string array
	 * @return a string, the array elements joined with comma
	 */
	public String formatStringArray(String[] s) {
		if (s == null)
			return "NULL";
		String result = "";
		for (int i = 0; i < s.length; i++) {
			result = result + (result.equals("") ? "" : ",") + s[i];
		}
		return result;
	}
}
