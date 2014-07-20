package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Join Event class</pre>
 * <p/>
 * is called when a player joins the arena
 *
 * @author slipcor
 * @version v0.9.1
 */

public class PAJoinEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;
    private final Player player;
    private final boolean spectator;
    private boolean cancelled;

    /**
     * create an arena join event
     *
     * @param arena       the arena where the event is happening in
     * @param player      the joining player
     * @param isSpectator true if the player will spectate, false otherwise
     */
    public PAJoinEvent(final Arena arena, final Player player, final boolean isSpectator) {
        super();
        this.arena = arena;
        this.player = player;
        spectator = isSpectator;
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
    public boolean isSpectator() {
        return spectator;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean value) {
        cancelled = value;
    }
}
