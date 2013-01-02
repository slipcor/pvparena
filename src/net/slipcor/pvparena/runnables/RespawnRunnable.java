package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;

public class RespawnRunnable implements Runnable {

	final Arena a;
	final ArenaPlayer p;
	
	public RespawnRunnable(Arena arena, ArenaPlayer ap) {
		a = arena;
		p = ap;
	}
	
	@Override
	public void run() {
		if (p.get() == null || p.getArenaTeam() == null || a == null) {
			return;
		}
		a.tpPlayerToCoordName(p.get(), (a.isFreeForAll()?"":p.getArenaTeam().getName()) + "spawn");
	}
	
}
