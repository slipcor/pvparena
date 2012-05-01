package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

/**
 * remove command class
 * 
 * @author slipcor
 * 
 * @version v0.7.18
 * 
 */

public class PAARemove extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("remove")));
			return;
		}
		String name = arena.name;
		Arenas.unload(name);
		Arenas.tellPlayer(player, Language.parse("removed", name));
	}

}
