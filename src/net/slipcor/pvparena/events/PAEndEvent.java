package net.slipcor.pvparena.events;

import net.slipcor.pvparena.definitions.Arena;

import org.bukkit.event.Event;

/**
 * 
 * PVP Arena End Event
 * 
 * -
 * 
 * is thrown when an arena match ends
 * 
 * @version 0.6.3.26
 * 
 * @author slipcor
 * 
 */

public class PAEndEvent extends Event {
	private static final long serialVersionUID = 5008292443338623123L;
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
	 * @return the ending arena
	 */
	public Arena getArena() {
		return arena;
	}
}
