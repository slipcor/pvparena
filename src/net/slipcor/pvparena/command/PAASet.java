package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PAASet extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, null))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("set")));
			return;
		}
		
		if (args.length == 3) {
			// pa set [node] [value]
			arena.sm.set(player, args[1], args[2]);
		} else if (args.length == 2) {
			// pa [name] set [page]
			int i = 1;
			try {
				i = Integer.parseInt(args[1]);
			} catch (Exception e) {
				// nothing
			}
			arena.sm.list(player, i);
		} else {
			Arenas.tellPlayer(player, Language.parse("args", String.valueOf(args.length), "2 or 3"));
		}
	}

	@Override
	public String getName() {
		return "PAASet";
	}
}
