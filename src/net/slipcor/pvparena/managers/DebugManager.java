/*
 * debug manager class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.3.10 - CraftBukkit #1337 config version, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.7 - Bugfixes
 */

package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.PVPArena;

public class DebugManager {
	public static boolean active;

	/*
	 * info log
	 */
	public void i(String s) {
		if (!active)
			return;
		PVPArena.instance.log.info(s);
	}

	/*
	 * warning log
	 */
	public void w(String s) {
		if (!active)
			return;
		PVPArena.instance.log.warning(s);
	}

	/*
	 * severe log
	 */
	public void s(String s) {
		if (!active)
			return;
		PVPArena.instance.log.severe(s);
	}

	/*
	 * read a string array and return a readable string
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
