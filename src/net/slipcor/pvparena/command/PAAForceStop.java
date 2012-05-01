package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PAAForceStop extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, null))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("admin")));
			return;
		}
		
		if (arena.fightInProgress) {
			arena.forcestop();
			Arenas.tellPlayer(player, Language.parse("forcestop"), arena);
		} else {
			Arenas.tellPlayer(player, Language.parse("nofight"), arena);
		}
	}

}
