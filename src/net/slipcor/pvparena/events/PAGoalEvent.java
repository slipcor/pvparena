package net.slipcor.pvparena.events;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.loadables.ArenaGoal;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * content.length == 1
 * * content[0] = "" => end!
 * content[X].contains(playerDeath) => "playerDeath:playerName"
 * content[X].contains(playerKill) => "playerKill:playerKiller:playerKilled"
 * content[X].contains(trigger) => "trigger:playerName" triggered a score
 * content[X].equals(tank) => player is tank
 * content[X].equals(infected) => player is infected
 * content[X].equals(doesRespawn) => player will respawn
 * content[X].contains(score) => "score:player:team:value"
 * 
 */
public class PAGoalEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Arena arena;
    private final ArenaGoal goal;
    private final String[] content;

    /**
     * create an arena death event
     *
     * @param arena  the arena where the event is happening in
     * @param goal the goal triggering the event
     */
    public PAGoalEvent(final Arena arena, final ArenaGoal goal, final String... content) {
        super();
        this.arena = arena;
        this.goal = goal;
        this.content = content;
    }

    /**
     * hand over the arena instance
     *
     * @return the arena the event is happening in
     */
    public Arena getArena() {
        return arena;
    }

    public ArenaGoal getGoal() {
        return goal;
    }

    public String[] getContents() {
        return content;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
