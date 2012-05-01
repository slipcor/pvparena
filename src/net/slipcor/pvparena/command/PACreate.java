package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PACreate extends PA_Command {
	private static Debug db = new Debug(3);

	@Override
	public void commit(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Language.parse("onlyplayers");
			return;
		}
		
		Player player = (Player) sender;
		
		db.i("parsing help command of player " + player.getName()
				+ StringParser.parseArray(args));
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, null))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("create")));
			return;
		}

		Arena arena = Arenas.getArenaByName(args[0]);

		if (arena != null) {
			Arenas.tellPlayer(player, Language.parse("arenaexists"));
			return;
		}
		Arena a = null;
		if (args.length > 2) {
			if (PVPArena.instance.getAtm().getType(args[2]) == null) {
				Arenas.tellPlayer(player,
						Language.parse("arenatypeunknown", args[2]));
				return;
			}

			a = Arenas.loadArena(args[0], args[2]);
		} else {
			if (PVPArena.instance.getAtm().getType("teams") == null) {
				Arenas.tellPlayer(player,
						Language.parse("arenatypeunknown", "teams"));
				return;
			}

			a = Arenas.loadArena(args[0], "teams");
		}
		a.setWorld(player.getWorld().getName());
		if (!PVPArena.hasAdminPerms(player)) {
			a.owner = player.getName();
		}
		a.cfg.set("general.owner", a.owner);
		a.cfg.save();
		Arenas.tellPlayer(player, Language.parse("created", args[0]));
	}
}
