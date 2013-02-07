package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;

public class RespawnRunnable implements Runnable {

	private final Arena arena;
	private final ArenaPlayer player;
	private final String coordName;
	
	public RespawnRunnable(final Arena arena, final ArenaPlayer player, final String coord) {
		this.arena = arena;
		this.player = player;
		this.coordName = coord;
	}
	
	@Override
	public void run() {
		if (player.get() == null || player.getArenaTeam() == null || arena == null) {
			PVPArena.instance.getLogger().warning("player null!");
			return;
		}
		if (coordName == null) {
			arena.tpPlayerToCoordName(player.get(), (arena.isFreeForAll()?"":player.getArenaTeam().getName()) + "spawn");
		} else {
			arena.tpPlayerToCoordName(player.get(), coordName);
		}
	}
	
}
