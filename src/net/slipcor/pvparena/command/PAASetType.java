package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaType;

import org.bukkit.command.CommandSender;

public class PAASetType extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender player, String[] args) {
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("admin")));
			return;
		}
		
		if (args.length == 2) {
			// pa settype [value]
			ArenaType t = null;
			for (ArenaType type : PVPArena.instance.getAtm().getTypes()) {
				if (type.getName().equalsIgnoreCase(args[1])) {
					t = type;
					break;
				}
			}
			if (t == null) {
				Arenas.tellPlayer(player,
						Language.parse("arenatypeunknown", args[1]));
				return;
			}
			arena.forcestop();
			arena.cfg.set("type", t.getName());
			
			Arenas.loadArena(arena.name, t.getName());
			Arenas.tellPlayer(player, Language.parse("settype", arena.name, t.getName()));
		} else {
			Arenas.tellPlayer(player, Language.parse("args", String.valueOf(args.length), "2"));
		}
	}

	@Override
	public String getName() {
		return "PAASet";
	}
}
