package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Player Team Change Event class</pre>
 * <p/>
 * is called when a player changes arena team
 *
 * @author slipcor
 */

public class PATeamChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;
    private final Player player;
    private final ArenaTeam fromTeam;
    private final ArenaTeam toTeam;

    /**
     * create an arena teamChange event
     *
     * @param arena  the arena where the event is happening in
     * @param player the changed player
     * @param from   the source team
     * @param to     the destination team
     */
    public PATeamChangeEvent(final Arena arena, final Player player, final ArenaTeam from, final ArenaTeam to) {
        super();
        this.arena = arena;
        this.player = player;
        fromTeam = from;
        toTeam = to;
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

    public ArenaTeam getFrom() {
        return fromTeam;
    }

    public ArenaTeam getTo() {
        return toTeam;
    }
}
