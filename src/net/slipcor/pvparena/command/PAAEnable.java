package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PAAEnable extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("enable")), arena);
			return;
		}
		arena.cfg.set("general.enabled", true);
		arena.cfg.save();
		Arenas.tellPlayer(player, Language.parse("enabled"), arena);
	}

	@Override
	public String getName() {
		return "PAAEnable";
	}
}
