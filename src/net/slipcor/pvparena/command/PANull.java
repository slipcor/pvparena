package net.slipcor.pvparena.command;

import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PANull extends PA_Command {

	@Override
	public void commit(CommandSender player, String[] args) {
		Arenas.tellPlayer(player, Language.parse("unknownarena") + ": " + args[0]);
	}

	@Override
	public String getName() {
		return "PANull";
	}
}
