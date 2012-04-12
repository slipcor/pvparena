package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

/**
 * arena start runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to start the arena it belongs to
 * 
 * @author slipcor
 * 
 * @version v0.7.0
 * 
 */

public class StartRunnable implements Runnable {
	private final Arena a;
	private Debug db = new Debug(43);

	/**
	 * create a timed arena start runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public StartRunnable(Arena a) {
		this.a = a;
		db.i("StartRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("StartRunnable commiting");
		a.start();
	}
}
