package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Kill Event class</pre>
 * 
 * is called when a player kills in the arena
 * 
 * @author slipcor
 * 
 * @version v0.9.0
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

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * hand over the player
	 * 
	 * @return the killing player
	 */
	public Player getPlayer() {
		return player;
	}
}
