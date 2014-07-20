package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Player Class Change Event class</pre>
 * <p/>
 * is called when a player changes his arena class
 *
 * @author slipcor
 */

public class PAPlayerClassChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;
    private final Player player;
    private ArenaClass classToSet;

    /**
     * create an arena classChange event
     *
     * @param arena      the arena where the event is happening in
     * @param player     the changed player
     * @param arenaClass the player class
     */
    public PAPlayerClassChangeEvent(final Arena arena, final Player player, final ArenaClass arenaClass) {
        super();
        this.arena = arena;
        this.player = player;
        classToSet = arenaClass;
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
     * hand over the arena class
     *
     * @return true if the player was a spectator, false otherwise
     */
    public ArenaClass getArenaClass() {
        return classToSet;
    }

    public void setArenaClass(final ArenaClass classToSet) {
        this.classToSet = classToSet;
    }
}
