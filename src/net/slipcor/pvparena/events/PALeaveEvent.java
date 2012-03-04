package net.slipcor.pvparena.events;

import net.slipcor.pvparena.definitions.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * 
 * PVP Arena Leave Event
 * 
 * -
 * 
 * is thrown when a player leaves the arena
 * 
 * @version 0.6.3.26
 * 
 * @author slipcor
 * 
 */

public class PALeaveEvent extends Event {

	private static final long serialVersionUID = 913817643961941345L;
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
