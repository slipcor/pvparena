package net.slipcor.pvparena.events;

import net.slipcor.pvparena.definitions.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 
 * PVP Arena Join Event
 * 
 * -
 * 
 * is thrown when a player joins the arena
 * 
 * @version 0.6.27
 * 
 * @author slipcor
 * 
 */

public class PAJoinEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private Player player;
	private boolean spectator;

	/**
	 * create an arena join event
	 * 
	 * @param a
	 *            the arena where the event is happening in
	 * @param p
	 *            the joining player
	 * @param willSpectate
	 *            true if the player will spectate, false otherwise
	 */
	public PAJoinEvent(Arena a, Player p, boolean willSpectate) {
		arena = a;
		player = p;
		spectator = willSpectate;
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
	 * @return the joining player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * hand over the spectate state
	 * 
	 * @return true if the player will join as spectator, false otherwise
	 */
	public boolean getSpectate() {
		return spectator;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
