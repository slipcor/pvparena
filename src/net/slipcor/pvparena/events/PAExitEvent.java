package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 
 * PVP Arena Death Event
 * 
 * -
 * 
 * is thrown when a player exits the arena any way
 * 
 * @version v0.7.19
 * 
 * @author slipcor
 * 
 */

public class PAExitEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private Player player;

	/**
	 * create an arena death event
	 * 
	 * @param a
	 *            the arena where the event is happening in
	 * @param p
	 *            the exiting player
	 */
	public PAExitEvent(Arena a, Player p) {
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

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * hand over the player
	 * 
	 * @return the exiting player
	 */
	public Player getPlayer() {
		return player;
	}
}
