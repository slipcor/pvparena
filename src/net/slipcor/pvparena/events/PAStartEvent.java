package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Death Event class</pre>
 * 
 * is called when an arena match starts
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAStartEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Arena arena;

	/**
	 * create a start event instance
	 * 
	 * @param a
	 *            the starting arena
	 */
	public PAStartEvent(Arena a) {
		arena = a;
	}

	/**
	 * hand over the arena instance
	 * 
	 * @return the starting arena
	 */
	public Arena getArena() {
		return arena;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}
}
