package net.slipcor.pvparena.command;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Statistics;

import org.bukkit.command.CommandSender;

public class PAAUsers extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		ArenaPlayer[] players = Statistics
				.getStats(arena, Statistics.type.WINS);

		Arenas.tellPlayer(player, Language.parse("top5win"), arena);

		int limit = 5;

		for (ArenaPlayer ap : players) {
			if (limit-- < 1) {
				break;
			}
			Arenas.tellPlayer(player, ap.get().getName() + ": " + ap.wins + " "
					+ Language.parse("wins"), arena);
		}

		Arenas.tellPlayer(player, "------------", arena);
		Arenas.tellPlayer(player, Language.parse("top5lose"), arena);

		players = Statistics.getStats(arena, Statistics.type.LOSSES);
		for (ArenaPlayer ap : players) {
			if (limit-- < 1) {
				break;
			}
			Arenas.tellPlayer(player, ap.get().getName() + ": " + ap.losses
					+ " " + Language.parse("losses"), arena);
		}
	}

}
