package net.slipcor.pvparena.command;

import net.slipcor.pvparena.arena.Arena;

import org.bukkit.command.CommandSender;

public class PAATypeCommand extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		arena.type().commitCommand(arena, sender, args);
	}

	@Override
	public String getName() {
		return "PATypeCommand";
	}
}
