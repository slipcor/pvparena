package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Leave Event class</pre>
 * 
 * is called when a player leaves an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class PALeaveEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private Player player;
	private boolean spectator;

	/**
	 * create an arena leave event
	 * 
	 * @param a
	 *            the arena where the event is happening in
	 * @param p
	 *            the leaving player
	 * @param didSpectate
	 *            true if the player was spectator, false otherwise
	 */
	public PALeaveEvent(Arena a, Player p, boolean didSpectate) {
		arena = a;
		player = p;
		spectator = didSpectate;
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
	 * @return the leaving player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * hand over the spectate state
	 * 
	 * @return true if the player was a spectator, false otherwise
	 */
	public boolean getSpectate() {
		return spectator;
	}
}
