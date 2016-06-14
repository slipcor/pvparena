package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <pre>PVP Arena Death Event class</pre>
 * <p/>
 * is called when a player dies
 *
 * @author slipcor
 * @version v0.9.1
 */

public class PADeathEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;
    private final Player player;
    private final boolean respawn;
    private final boolean pvp;

    /**
     * create an arena death event
     *
     * @param arena        the arena where the event is happening in
     * @param player       the dying player
     * @param isRespawning true if the player will respawn, false otherwise
     * @param isDueToPVP   true if the event is caused by PVP
     */
    public PADeathEvent(final Arena arena, final Player player, final boolean isRespawning, final boolean isDueToPVP) {
        super();
        this.arena = arena;
        this.player = player;
        respawn = isRespawning;
        pvp = isDueToPVP;
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
     * @return the dying player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * hand over the respawn state
     *
     * @return true if the player will respawn, false otherwise
     */
    public boolean isRespawning() {
        return respawn;
    }

    /**
     * hand over the pvp state
     *
     * @return true if the kill was due to pvp
     */
    public boolean isPVP() {
        return pvp;
    }
}
