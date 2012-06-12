package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.command.PAAJoin;
import net.slipcor.pvparena.command.PAAJoinTeam;
import net.slipcor.pvparena.command.PAASpectate;
import net.slipcor.pvparena.core.Debug;

/**
 * player reset runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to warmup a player
 * 
 * @author slipcor
 * 
 * @version v0.8.4
 * 
 */

public class ArenaWarmupRunnable implements Runnable {
	private final ArenaPlayer player;
	private final String teamName;
	private final Arena arena;
	private final boolean spectator;
	private int id;
	private Debug db = new Debug(40);
	
	private int count = 0;
	
	/**
	 * create a timed arena runnable
	 * 
	 * @param p
	 *            the player to reset
	 */
	public ArenaWarmupRunnable(Arena a, ArenaPlayer p, String team, boolean spec, int i, int iid) {
		db.i("ArenaWarmupRunnable constructor");
		id = 0;
		player = p;
		teamName = team;
		arena = a;
		spectator = spec;
		count = i+1;
	}

	/**
	 * the run method, warmup the arena player
	 */
	@Override
	public void run() {
		TimerInfo.spam("warmingupexact", --count, player.get(), null, false);
		if (count <= 0) {
			commit();
		} else {
			id = Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this, 20L);
		}
	}
	
	private void commit() {
		db.i("ArenaWarmupRunnable commiting");
		player.setStatus(Status.WARM);
		if (spectator) {
			(new PAASpectate()).commit(arena, player.get(), null);
		} else if (teamName == null) {
			(new PAAJoin()).commit(arena, player.get(), null);
		} else {
			String[] args = new String[1];
			args[0] = teamName;
			(new PAAJoinTeam()).commit(arena, player.get(), args);
		}
		Bukkit.getScheduler().cancelTask(id);
	}
	
	public void setId(int i) {
		id = i;
	}
}