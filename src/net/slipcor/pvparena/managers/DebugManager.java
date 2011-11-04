package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.PVPArenaPlugin;

public class DebugManager {
	private boolean active = false; 
	public void i(String s) {
		if (!active)
			return;
		PVPArenaPlugin.log.info(s);
	}
	public void w(String s) {
		if (!active)
			return;
		PVPArenaPlugin.log.warning(s);
	}
	public void s(String s) {
		if (!active)
			return;
		PVPArenaPlugin.log.severe(s);
	}
}
