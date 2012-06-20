package net.slipcor.pvparena.command;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PAANull extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		
		Arenas.tellPlayer(player, Language.parse("unknowncmd") + ": " + args[0],
				arena);
	}

	@Override
	public String getName() {
		return "PAANull";
	}
}
