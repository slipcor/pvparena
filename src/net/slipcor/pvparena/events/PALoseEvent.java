package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Lose Event class</pre>
 * <p/>
 * is called when a player loses an arena match
 *
 * @author slipcor
 * @version v0.9.1
 */

public class PALoseEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;
    private final Player player;

    public PALoseEvent(final Arena arena, final Player player) {
        super();
        this.arena = arena;
        this.player = player;
    }

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

    public Player getPlayer() {
        return player;
    }
}
