package net.slipcor.pvparena.command;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAAChat extends PAA_Command {

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Language.parse("onlyplayers");
			return;
		}
		
		Player player = (Player) sender;
		
		if (arena.chatters.contains(player.getName())) {
			arena.chatters.remove(player.getName());
			Arenas.tellPlayer(player, Language.parse("chatpublic"), arena);
		} else {
			arena.chatters.add(player.getName());
			Arenas.tellPlayer(player, Language.parse("chatteam"), arena);
		}
	}

}
