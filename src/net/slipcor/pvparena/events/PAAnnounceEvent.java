package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;

import org.bukkit.event.Cancellable;
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

public class PAAnnounceEvent extends Event implements Cancellable {
	
	public static enum AnnounceType {
		JOIN, ADVERT, START, END, WINNER, LOSER, PRIZE, CUSTOM;
	}
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final Arena arena;
	private boolean cancelled;
	private final AnnounceType type;
	private final String message;

	/**
	 * create a start event instance
	 * 
	 * @param a
	 *            the starting arena
	 */
	public PAAnnounceEvent(final Arena arena, AnnounceType type, String message) {
		super(); 
		this.arena = arena;
		this.type = type;
		this.message = message;
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}
}
