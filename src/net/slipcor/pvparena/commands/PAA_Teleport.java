package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Spawns;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PAA_Teleport extends PAA__Command {

	public PAA_Teleport() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(1)))) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse("command.onlyplayers"));
			return;
		}

		// usage: /pa {arenaname} teleport [spawnname] | tp to a spawn
		
		PALocation loc = Spawns.getCoords(arena, args[0]);
		
		if (loc == null) {
			arena.msg(sender, Language.parse("spawn.unknown", args[0]));
			return;
		}
		
		((Player) sender).teleport(loc.toLocation(), TeleportCause.PLUGIN);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
