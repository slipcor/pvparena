package net.slipcor.pvparena.command;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Teams;

import org.bukkit.command.CommandSender;

public class PAAList extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		if (Teams.countPlayersInTeams(arena) < 1) {
			Arenas.tellPlayer(player, Language.parse("noplayer"), arena);
			return;
		}
		String plrs = Teams.getTeamStringList(arena);
		Arenas.tellPlayer(player, Language.parse("players") + ": " + plrs,
				arena);
	}

}
