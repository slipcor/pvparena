package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PADebug extends PA_Command {

	@Override
	public void commit(CommandSender player, String[] args) {
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, null))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("admin")));
			return;
		}
		if (args.length < 2) {
			Arenas.tellPlayer(player, "not enough arguments!");
			return;
		}
		PVPArena.instance.getConfig().set("debug", args[1]);
		Debug.load(PVPArena.instance);
	}

	@Override
	public String getName() {
		return "PADebug";
	}

}
