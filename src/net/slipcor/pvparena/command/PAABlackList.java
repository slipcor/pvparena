package net.slipcor.pvparena.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class PAABlackList extends PAA_Command {
	static HashSet<String> subCommands = new HashSet<String>();
	static {
		subCommands.add("add");
		subCommands.add("remove");
		subCommands.add("show");
	}
	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!checkArgs(sender, args, 2, 3)) {
			return;
		}
		
		if (args[1].equalsIgnoreCase("clear")) {
			arena.cfg.set("blocks.blacklist", null);
			arena.cfg.save();
			Arenas.tellPlayer(sender, Language.parse("blacklistclear"));
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
			Arenas.tellPlayer(sender, Language.parse("blacklistadd", args[2]));
		} else if (args[1].equalsIgnoreCase("show")) {
			String output = Language.parse("blacklist");
			for (String s : list) {
				output += ": " + Material.getMaterial(Integer.parseInt(s)).name();
			}
			if (list.size() < 1) {
				output += ": ---------";
			}
			Arenas.tellPlayer(sender, output);
		} else {
			list.remove(args[2]);
			Arenas.tellPlayer(sender, Language.parse("blacklistremove", args[2]));
		}
		
		arena.cfg.set("blocks.blacklist", list);
		arena.cfg.save();
	}

	@Override
	public String getName() {
		return "PAABlackList";
	}

}
