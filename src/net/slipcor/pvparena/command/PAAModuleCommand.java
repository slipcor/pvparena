package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.neworder.ArenaModule;

import org.bukkit.command.CommandSender;

public class PAAModuleCommand extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			mod.commitCommand(arena, sender, args);
		}
	}

	@Override
	public String getName() {
		return "PAAModuleCommand";
	}
}
