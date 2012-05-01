package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PAAEdit extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		if (!PVPArena.hasAdminPerms(player)) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("edit")), arena);
			return;
		}

		arena.edit = !arena.edit;
		Arenas.tellPlayer(
				player,
				Language.parse("edit" + String.valueOf(arena.edit), arena.name),
				arena);
	}

}
