package net.slipcor.pvparena.command;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Teams;

import org.bukkit.command.CommandSender;

public class PAAAlive extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		if (Teams.countPlayersInTeams(arena) < 1) {
			Arenas.tellPlayer(player, Language.parse("noplayer"), arena);
			return;
		}
		String players = "";
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.FIGHT)) {
					if (!players.equals("")) {
						players += ", ";
					}
					players += team.colorizePlayer(ap.get());
				}
			}
		}
		Arenas.tellPlayer(player, Language.parse("players") + ": " + players,
				arena);
	}

	@Override
	public String getName() {
		return "PAAAlive";
	}
}
