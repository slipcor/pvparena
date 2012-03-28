package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.managers.Spawns;

import org.bukkit.Location;
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
 * @version v0.7.0
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

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * return the battlefield center
	 * 
	 * @return the battlefield center location
	 */
	public Location getRegionCenter() {
		return Spawns.getRegionCenter(arena);
	}
}
