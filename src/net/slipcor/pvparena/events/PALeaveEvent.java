package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Leave Event class</pre>
 * <p/>
 * is called when a player leaves an arena
 *
 * @author slipcor
 * @version v0.9.1
 */

public class PALeaveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;
    private final Player player;
    private final boolean spectator;

    /**
     * create an arena leave event
     *
     * @param arena       the arena where the event is happening in
     * @param player      the leaving player
     * @param isSpectator true if the player was spectator, false otherwise
     */
    public PALeaveEvent(final Arena arena, final Player player, final boolean isSpectator) {
        super();
        this.arena = arena;
        this.player = player;
        spectator = isSpectator;
        player.setNoDamageTicks(arena.getArenaConfig().getInt(CFG.TIME_TELEPORTPROTECT) * 20);
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
     * @return the leaving player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * hand over the spectate state
     *
     * @return true if the player was a spectator, false otherwise
     */
    public boolean isSpectator() {
        return spectator;
    }
}
