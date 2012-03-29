package net.slipcor.pvparena.events;

import net.slipcor.pvparena.definitions.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 
 * PVP Arena Kill Event
 * 
 * -
 * 
 * is thrown when a player kills in the arena
 * 
 * @version 0.6.36
 * 
 * @author slipcor
 * 
 */

public class PAKillEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private Player player;

	/**
	 * create an arena death event
	 * 
	 * @param a
	 *            the arena where the event is happening in
	 * @param p
	 *            the killing player
	 */
	public PAKillEvent(Arena a, Player p) {
		arena = a;
		player = p;
	}

	/**
	 * hand over the arena instance
	 * 
	 * @return the arena the event is happening in
	 */
	public Arena getArena() {
		return arena;
	}

	/**
	 * hand over the player
	 * 
	 * @return the killing player
	 */
	public Player getPlayer() {
		return player;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
