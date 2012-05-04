package net.slipcor.pvparena.command;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAALeave extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Arenas.tellPlayer(sender, Language.parse("onlyplayers"));
			return;
		}
		
		Player player = (Player) sender;
		
		if (Arenas.getArenaByPlayer(player) == null) {
			Arenas.tellPlayer(player, Language.parse("notinarena"));
			return;
		}
		
		arena.playerLeave(player);
	}

	@Override
	public String getName() {
		return "PAALeave";
	}

}
