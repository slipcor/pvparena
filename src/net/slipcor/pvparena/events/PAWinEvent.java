package net.slipcor.pvparena.events;

import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.arena.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>
 * PVP Arena Win Event class
 * </pre>
 * 
 * is called when a player wins an arena match
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class PAWinEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();
	private final Arena arena;
	private final Player player;
	private final List<String> items;

	public PAWinEvent(final Arena arena, final Player player, final String[] arrItems) {
		super(); 
		this.arena = arena;
		this.player = player;
		items = new ArrayList<String>();

		if (arrItems == null || arrItems.length == 0) {
			return;
		}
		for (String s : arrItems) {
			items.add(s);
		}
	}

	public void addItemString(final String item) {
		items.add(item);
	}

	public Arena getArena() {
		return arena;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public String[] getItems() {
		final String[] output = new String[items.size()];
		int pos = 0;
		for (String s : items) {
			output[pos++] = s;
		}
		return output;
	}

	public Player getPlayer() {
		return player;
	}

	public void removeItemString(final String item) {
		items.remove(item);
	}
}
