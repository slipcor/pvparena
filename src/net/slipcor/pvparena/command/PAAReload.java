package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PAAReload extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		if (!PVPArena.hasAdminPerms(player)) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("reload")));
			return;
		}
		for (Arena a : Arenas.getArenas()) {
			a.reset(true);
		}
		Arenas.load_arenas();
		Arenas.tellPlayer(player, Language.parse("reloaded"));
	}

	@Override
	public String getName() {
		return "PAAReload";
	}
}
