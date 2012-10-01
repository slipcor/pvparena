package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena End Event class</pre>
 * 
 * is called when an arena match ends
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class PAEndEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Arena arena;

	/**
	 * create an end event instance
	 * 
	 * @param a
	 *            the ending arena
	 */
	public PAEndEvent(Arena a) {
		arena = a;
	}

	/**
	 * hand over the arena instance
	 * 
	 * @return the ending arena
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
