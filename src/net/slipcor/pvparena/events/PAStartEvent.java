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
 * @version v0.9.1
 */

public class PAStartEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();
	private final Arena arena;

	/**
	 * create a start event instance
	 * 
	 * @param a
	 *            the starting arena
	 */
	public PAStartEvent(final Arena arena) {
		super();
		this.arena = arena;
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
		return HANDLERS;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}
}
