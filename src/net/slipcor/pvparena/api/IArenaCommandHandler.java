package net.slipcor.pvparena.api;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.commands.CommandTree;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface IArenaCommandHandler {
    /**
     * Get all main commands
     *
     * @return all valid first sub command arguments
     */
    List<String> getMain();

    /**
     * Get all main shortcuts
     *
     * @return all valid shortcuts of first sub command arguments
     */
    List<String> getShort();

    /**
     * Get the CommandTree
     *
     * @param arena the arena instance
     * @return the CommandTree for this ArenaCommandHandler
     */
    CommandTree<String> getSubs(final Arena arena);

    /**
     * Does the sender have the permissions for this command?
     *
     * @param sender the CommandSender to check
     * @param arena  the arena they are part of
     * @return true if they have the perms, false otherwise
     */
    boolean hasPerms(final CommandSender sender, final Arena arena);
}
