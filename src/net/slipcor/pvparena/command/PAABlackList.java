package net.slipcor.pvparena.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;

public class PAABlackList extends PAA_Command {
	static HashSet<String> subCommands = new HashSet<String>();
	static {
		subCommands.add("add");
		subCommands.add("remove");
	}
	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!checkArgs(sender, args, 2, 3)) {
			return;
		}
		if (args[1].equalsIgnoreCase("clear")) {
			arena.cfg.set("blocks.blacklist", null);
			return;
		}
		
		if (!subCommands.contains(args[1].toLowerCase())) {
			String output = "unknown subcommand. Valid: clear";
			for (String s : subCommands) {
				output += ", " + s;
			}
			Arenas.tellPlayer(sender, output);
			return;
		}
		
		if (!checkArgs(sender, args, 3)) {
			return;
		}
		
		List<String> list = new ArrayList<String>();

		list = arena.cfg.getStringList("blocks.blacklist", list);
		
		if (args[1].equalsIgnoreCase("add")) {
			list.add(args[2]);
		} else {
			list.remove(args[2]);
		}
		
		arena.cfg.set("blocks.blacklist", list);
	}

	@Override
	public String getName() {
		return "PAABlackList";
	}

}
