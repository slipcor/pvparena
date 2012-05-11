package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PAACheck extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, null))) {
			Arenas.tellPlayer(sender,
					Language.parse("nopermto", Language.parse("admin")));
			return;
		}
		Debug.override = true;

		db.i("-------------------------------");
		db.i("Debug parsing Arena config for arena: " + arena);
		db.i("-------------------------------");

		Arenas.loadArena(arena.name, arena.type().getName());

		db.i("-------------------------------");
		db.i("Debug parsing finished!");
		db.i("-------------------------------");

		Debug.override = false;
	}

	@Override
	public String getName() {
		return "PAACheck";
	}
}
