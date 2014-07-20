package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Death Event class</pre>
 * <p/>
 * is called when a player exits the arena
 *
 * @author slipcor
 * @version v0.9.1
 */

public class PAExitEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;
    private final Player player;

    /**
     * create an arena death event
     *
     * @param arena  the arena where the event is happening in
     * @param player the exiting player
     */
    public PAExitEvent(final Arena arena, final Player player) {
        super();
        this.arena = arena;
        this.player = player;
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
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * hand over the player
     *
     * @return the exiting player
     */
    public Player getPlayer() {
        return player;
    }
}
