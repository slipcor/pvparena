package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Join Event class</pre>
 * 
 * is called when a player joins the arena
 * 
 * @author slipcor
 * 
 * @version v0.9.1
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

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
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
}
