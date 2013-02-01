package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;

public class RespawnRunnable implements Runnable {

	private final Arena arena;
	private final ArenaPlayer player;
	
	public RespawnRunnable(final Arena arena, final ArenaPlayer player) {
		this.arena = arena;
		this.player = player;
	}
	
	@Override
	public void run() {
		if (player.get() == null || player.getArenaTeam() == null || arena == null) {
			return;
		}
		arena.tpPlayerToCoordName(player.get(), (arena.isFreeForAll()?"":player.getArenaTeam().getName()) + "spawn");
	}
	
}
