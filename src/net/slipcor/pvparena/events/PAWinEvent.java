package net.slipcor.pvparena.events;

import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.arena.Arena;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Win Event class</pre>
 * 
 * is called when a player wins an arena match
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class PAWinEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Arena a;
	private final Player p;
	private final List<String> items;

	public PAWinEvent(Arena arena, Player player, String[] arrItems) {
		a = arena;
		p = player;
		items = new ArrayList<String>();
		
		if (arrItems == null || arrItems.length == 0) {
			return;
                }
		for (String s : arrItems) {
			items.add(s);
		}
	}
	
	public void addItemString(String s) {
		items.add(s);
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

	public String[] getItems() {
		String[] output = new String[items.size()];
		int i = 0;
		for (String s : items) {
			output[i++] = s;
		}
		return output;
	}

	public Player getPlayer() {
		return p;
	}

	public void removeItemString(String s) {
		items.remove(s);
	}
}
