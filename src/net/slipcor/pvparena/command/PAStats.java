package net.slipcor.pvparena.command;

import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Statistics;

import org.bukkit.command.CommandSender;

public class PAStats extends PA_Command {

	@Override
	public void commit(CommandSender player, String[] args) {
		
		if (!checkArgs(player, args, 2, 3)) {
			return;
		}
		
		Statistics.type type = Statistics.type.getByString(args[1]);

		if (type == null) {
			Arenas.tellPlayer(player,
					Language.parse("invalidstattype", args[1]));
			return;
		}

		ArenaPlayer[] aps = Statistics.getStats(null, type);
		String[] s = Statistics.read(aps, type, true);

		int i = 0;

		for (ArenaPlayer ap : aps) {
			Arenas.tellPlayer(player, ap.get().getName() + ": " + s[i++]);
			if (i > 9) {
				return;
			}
		}
	}

	@Override
	public String getName() {
		return "PAStats";
	}
}
