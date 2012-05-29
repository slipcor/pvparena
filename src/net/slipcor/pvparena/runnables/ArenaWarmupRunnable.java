package net.slipcor.pvparena.runnables;

import java.util.HashMap;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.command.PAAJoin;
import net.slipcor.pvparena.command.PAAJoinTeam;
import net.slipcor.pvparena.command.PAASpectate;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.Arenas;

/**
 * player reset runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to warmup a player
 * 
 * @author slipcor
 * 
 * @version v0.7.19
 * 
 */

public class ArenaWarmupRunnable implements Runnable {
	private final ArenaPlayer player;
	private final String teamName;
	private final Arena arena;
	private final boolean spectator;
	private Debug db = new Debug(40);
	
	private int count = 0;
	private static HashMap<Integer, String> messages = new HashMap<Integer, String>();
	
	static {
		messages.put(1, "1...");
		messages.put(2, "2...");
		messages.put(3, "3...");
		messages.put(4, "4...");
		messages.put(5, "5...");
		messages.put(6, "6...");
		messages.put(7, "7...");
		messages.put(8, "8...");
		messages.put(9, "9...");
		messages.put(10, "10...");
		messages.put(20, "20...");
		messages.put(30, "30...");
		messages.put(60, "60...");
	}

	/**
	 * create a timed arena runnable
	 * 
	 * @param p
	 *            the player to reset
	 */
	public ArenaWarmupRunnable(Arena a, ArenaPlayer p, String team, boolean spec, int i) {
		db.i("ArenaWarmupRunnable constructor");
		player = p;
		teamName = team;
		arena = a;
		spectator = spec;
		count = i;
	}

	/**
	 * the run method, warmup the arena player
	 */
	@Override
	public void run() {
		String msg = messages.get(--count);
		if (msg != null) {
			Arenas.tellPlayer(player.get(), msg, arena);
		}
		if (count <= 0) {
			commit();
		} else {
			Bukkit.getScheduler().scheduleAsyncDelayedTask(PVPArena.instance, this);
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
	}
}