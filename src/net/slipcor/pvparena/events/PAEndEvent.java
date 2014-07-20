package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena End Event class</pre>
 * <p/>
 * is called when an arena match ends
 *
 * @author slipcor
 * @version v0.9.1
 */

public class PAEndEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;

    /**
     * create an end event instance
     *
     * @param arena the ending arena
     */
    public PAEndEvent(final Arena arena) {
        super();
        this.arena = arena;
    }

    /**
     * hand over the arena instance
     *
     * @return the ending arena
     */
    public Arena getArena() {
        return arena;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
