package net.slipcor.pvparena.events;

import net.slipcor.pvparena.definitions.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 
 * PVP Arena Death Event
 * 
 * -
 * 
 * is thrown when a player dies in the arena
 * 
 * @version 0.6.27
 * 
 * @author slipcor
 * 
 */

public class PADeathEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private Player player;
	private boolean respawn;

	/**
	 * create an arena death event
	 * 
	 * @param a
	 *            the arena where the event is happening in
	 * @param p
	 *            the dying player
	 * @param willRespawn
	 *            true if the player will respawn, false otherwise
	 */
	public PADeathEvent(Arena a, Player p, boolean willRespawn) {
		arena = a;
		player = p;
		respawn = willRespawn;
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
	 * @return the dying player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * hand over the respawn state
	 * 
	 * @return true if the player will respawn, false otherwise
	 */
	public boolean getRespawn() {
		return respawn;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
