package net.slipcor.pvparena.arena;

import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.ColorUtils;
import net.slipcor.pvparena.core.Debug;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * <pre>Arena Team class</pre>
 * <p/>
 * contains Arena Team methods and variables for quicker access
 *
 * @author slipcor
 * @version v0.10.2
 */

public class ArenaTeam {

    private static final Debug debug = new Debug(6);

    private final Set<ArenaPlayer> players;
    private final ChatColor color;
    private final String name;

    /**
     * create an arena team instance
     *
     * @param name  the arena team name
     * @param color the arena team color string
     */
    public ArenaTeam(final String name, final String color) {
        players = new HashSet<>();
        this.color = ColorUtils.getChatColorFromDyeColor(color);
        this.name = name;
    }

    /**
     * add an arena player to the arena team
     *
     * @param player the player to add
     */
    public void add(final ArenaPlayer player) {
        players.add(player);
        debug.i("Added player " + player.getName() + " to team " + name, player.get());
        player.getArena().increasePlayerCount();
    }

    /**
     * colorize a player name
     *
     * @param player the player to colorize
     * @return the colorized player name
     */
    public String colorizePlayer(final Player player) {
        return color + player.getName();
    }

    /**
     * return the team color
     *
     * @return the team color
     */
    public ChatColor getColor() {
        return color;
    }

    /**
     * colorize the team name
     *
     * @return the colorized team name
     */
    public String getColoredName() {
        return color + name;
    }

    /**
     * return the team color code
     *
     * @return the team color code
     */
    public String getColorCodeString() {
        return '&' + Integer.toHexString(color.ordinal());
    }

    /**
     * return the team name
     *
     * @return the team name
     */
    public String getName() {
        return name;
    }

    /**
     * return the team members
     *
     * @return a HashSet of all arena players
     */
    public Set<ArenaPlayer> getTeamMembers() {
        return players;
    }

    public boolean hasPlayer(final Player player) {
        return players.contains(ArenaPlayer.parsePlayer(player.getName()));
    }

    public boolean isEveryoneReady() {
        for (final ArenaPlayer ap : players) {
            if (ap.getStatus() != Status.READY) {
                return false;
            }
        }
        return true;
    }

    /**
     * remove a player from the team
     *
     * @param player the player to remove
     */
    public void remove(final ArenaPlayer player) {
        players.remove(player);
    }
}