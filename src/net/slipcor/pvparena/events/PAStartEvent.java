package net.slipcor.pvparena.events;

import net.slipcor.pvparena.definitions.Arena;

import org.bukkit.event.Event;

/**
 * 
 * PVP Arena Start Event
 * 
 * -
 * 
 * is thrown when an arena match starts
 * 
 * @version 0.6.3.26
 * 
 * @author slipcor
 * 
 */

public class PAStartEvent extends Event {
	private static final long serialVersionUID = -2176348449112343188L;
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
	 * @return the starting arena
	 */
	public Arena getArena() {
		return arena;
	}
}
