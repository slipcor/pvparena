package net.slipcor.pvparena.runnables;

import java.util.HashSet;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.classes.PASpawn;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.managers.SpawnManager;

public class RespawnRunnable implements Runnable {

	private final Arena arena;
	private final ArenaPlayer player;
	private final String coordName;
	
	public RespawnRunnable(final Arena arena, final ArenaPlayer player, final String coord) {
		arena.getDebugger().i("RespawnRunnable constructor to spawn "+ coord, player.get());
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
		arena.getDebugger().i("respawning " + player.getName() + " to " + coordName);
		
		PALocation loc = SpawnManager.getSpawnByExactName(arena, coordName);
		
		if (loc == null) {
			final Set<PASpawn> spawns = new HashSet<PASpawn>();
			if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
				String arenaClass = player.getArenaClass().getName();
				spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, player.getArenaTeam().getName()+arenaClass+"spawn"));
			} else if (arena.isFreeForAll()) {
				if (player.getArenaTeam().getName().equals("free")) {
					spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, "spawn"));
				} else {
					spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, player.getArenaTeam().getName()));
				}
			} else {
				spawns.addAll(SpawnManager.getPASpawnsStartingWith(arena, player.getArenaTeam().getName()+"spawn"));
			}
			
			int pos = spawns.size(); 
			
			for (PASpawn spawn : spawns) {
				if (--pos < 0) {
					arena.tpPlayerToCoordName(player.get(), spawn.getName());
					break;
				}
			}
		} else {
			arena.tpPlayerToCoordName(player.get(), coordName);
		}
	}
	
}
