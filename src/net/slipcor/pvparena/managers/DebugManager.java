package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.PVPArena;

/**
 * debug manager class
 * 
 * -
 * 
 * provides methods for logging when in debug mode
 * 
 * @author slipcor
 * 
 * @version v0.4.0
 * 
 */

public class DebugManager {
	public static boolean active;

	/**
	 * log a message as prefixed INFO
	 * 
	 * @param s
	 *            the message
	 */
	public void i(String s) {
		if (!active)
			return;
		PVPArena.instance.log.info(s);
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
		PVPArena.instance.log.warning(s);
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
		PVPArena.instance.log.severe(s);
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
