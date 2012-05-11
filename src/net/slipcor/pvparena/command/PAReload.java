package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PAReload extends PA_Command {

	@Override
	public void commit(CommandSender player, String[] args) {
		if (!PVPArena.hasAdminPerms(player)) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("reload")));
			return;
		}
		Arenas.load_arenas();
		Arenas.tellPlayer(player, Language.parse("reloaded"));
	}

	@Override
	public String getName() {
		return "PAReload";
	}
}
