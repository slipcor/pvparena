package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PAADisable extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("disable")), arena);
			return;
		}
		arena.forcestop();
		arena.cfg.set("general.enabled", false);
		arena.cfg.save();
		Arenas.tellPlayer(player, Language.parse("disabled"), arena);
	}

	@Override
	public String getName() {
		return "PAADisable";
	}
}
