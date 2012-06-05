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
			Arenas.tellPlayer(sender, Language.parse("onlyplayers"));
			Arenas.tellPlayer(sender, "§c/pa version gamemodes");
			return;
		}
		
		if (!checkArgs(sender, args, 2, 3)) {
			return;
		}
		
		Player player = (Player) sender;
		
		db.i("parsing create command of player " + player.getName()
				+ StringParser.parseArray(args));
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, null))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("create")));
			return;
		}

		Arena arena = Arenas.getArenaByName(args[1]);

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

			a = Arenas.loadArena(args[1], args[2]);
		} else {
			if (PVPArena.instance.getAtm().getType("teams") == null) {
				Arenas.tellPlayer(player,
						Language.parse("arenatypeunknown", "teams"));
				return;
			}

			a = Arenas.loadArena(args[1], "teams");
		}
		a.setWorld(player.getWorld().getName());
		if (!PVPArena.hasAdminPerms(player)) {
			a.owner = player.getName();
		}
		a.cfg.set("general.owner", a.owner);
		a.cfg.save();
		Arenas.tellPlayer(player, Language.parse("created", args[1]));
	}

	@Override
	public String getName() {
		return "PACreate";
	}
}
