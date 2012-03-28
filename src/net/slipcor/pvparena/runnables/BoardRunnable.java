package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.ArenaBoard;
import net.slipcor.pvparena.managers.Arenas;

/**
 * arena ending runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to end the arena it is running in
 * 
 * @author slipcor
 * 
 * @version v0.7.0
 * 
 */

public class BoardRunnable implements Runnable {
	private final Arena a;
	private Debug db = new Debug(38);

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public BoardRunnable(Arena a) {
		this.a = a;
		db.i("BoardRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("BoardRunnable commiting");
		for (ArenaBoard ab : Arenas.boards.values()) {
			db.i("");
			if (ab.arena.name.equals(a.name)) {
				ab.update();
			}
		}
	}
}
