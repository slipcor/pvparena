package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.PVPArena;

/*
 * Debug manager class
 * 
 * author: slipcor
 * 
 * version: v0.3.8 - BOSEconomy, rewrite
 * 
 * history:
 *
 *     v0.3.7 - Bugfixes
 */

public class DebugManager {
	private boolean active = false;
	
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
}
