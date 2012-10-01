package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Lose Event class</pre>
 * 
 * is called when a player loses an arena match
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class PALoseEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Arena a;
	private final Player p;

	public PALoseEvent(Arena arena, Player player) {
		a = arena;
		p = player;
	}

	public Arena getArena() {
		return a;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return p;
	}
}
