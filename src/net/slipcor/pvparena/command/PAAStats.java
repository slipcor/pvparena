package net.slipcor.pvparena.command;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Statistics;

import org.bukkit.command.CommandSender;

public class PAAStats extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		Statistics.type type = Statistics.type.getByString(args[1]);

		if (type == null) {
			Arenas.tellPlayer(player,
					Language.parse("invalidstattype", args[1]), arena);
			return;
		}

		ArenaPlayer[] aps = Statistics.getStats(arena, type);
		String[] s = Statistics.read(aps, type, arena == null);

		int i = 0;

		for (ArenaPlayer ap : aps) {
			Arenas.tellPlayer(player, ap.get().getName() + ": " + s[i++], arena);
			if (i > 9) {
				return;
			}
		}
	}

}
