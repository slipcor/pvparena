package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 
 * PVP Arena Start Event
 * 
 * -
 * 
 * is thrown when an arena match starts
 * 
 * @version v0.7.8
 * 
 * @author slipcor
 * 
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
