package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.PVPArena;

public class DebugManager {
	private boolean active = false; 
	public void i(String s) {
		if (!active)
			return;
		PVPArena.instance.log.info(s);
	}
	public void w(String s) {
		if (!active)
			return;
		PVPArena.instance.log.warning(s);
	}
	public void s(String s) {
		if (!active)
			return;
		PVPArena.instance.log.severe(s);
	}
}
