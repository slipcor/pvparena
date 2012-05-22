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
 * is thrown when a player dies in the arena
 * 
 * @version v0.7.23
 * 
 * @author slipcor
 * 
 */

public class PADeathEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Arena arena;
	private Player player;
	private boolean respawn;
	private boolean pvp;

	/**
	 * create an arena death event
	 * 
	 * @param a
	 *            the arena where the event is happening in
	 * @param p
	 *            the dying player
	 * @param willRespawn
	 *            true if the player will respawn, false otherwise
	 * @param pvp 
	 */
	public PADeathEvent(Arena a, Player p, boolean willRespawn, boolean pv) {
		arena = a;
		player = p;
		respawn = willRespawn;
		pvp = pv;
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

	/**
	 * hand over the pvp state
	 * 
	 * @return true if the kill was due to pvp
	 */
	public boolean isPVP() {
		return pvp;
	}
}
