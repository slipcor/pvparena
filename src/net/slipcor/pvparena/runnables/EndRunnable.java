package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Arena;

/**
 * arena ending runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to end the arena it is running in
 * 
 * @author slipcor
 * 
 * @version v0.6.3
 * 
 */

public class EndRunnable implements Runnable {
	private final Arena a;
	private Debug db = new Debug();

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public EndRunnable(Arena a) {
		this.a = a;
		db.i("EndRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("EndRunnable commiting");
		a.reset(false);
	}
}
