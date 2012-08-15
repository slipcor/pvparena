package net.slipcor.pvparena.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.command.CommandSender;
import org.bukkit.Material;

public class PAA_WhiteList extends PAA__Command {
	static HashSet<String> subCommands = new HashSet<String>();
	static HashSet<String> subTypes = new HashSet<String>();
	static {
		subCommands.add("add");
		subCommands.add("remove");
		subCommands.add("show");
		subTypes.add("break");
		subTypes.add("place");
		subTypes.add("use");
	}
	
	public PAA_WhiteList() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(1,2,3)))) {
			return;
		}
		
		//                                  args[0]
		// usage: /pa {arenaname} blacklist clear
		
		if (args.length < 2) {
			if (args[0].equalsIgnoreCase("clear")) {
				arena.getArenaConfig().set("blocks.whitelist", null);
				arena.getArenaConfig().save();
				arena.msg(sender, Language.parse("whitelist.allcleared"));
				return;
			}
			arena.msg(sender, Language.parse("whitelist.help"));
			return;
		} else if (args.length == 2) {
			// usage: /pa {arenaname} blacklist [type] clear
			if (!subTypes.contains(args[0].toLowerCase())) {
				arena.msg(sender, Language.parse("whitelist.unknowntype", StringParser.joinSet(subTypes, "|")));
				return;
			}
			if (args[1].equalsIgnoreCase("clear")) {
				arena.getArenaConfig().set("blocks.whitelist", null);
				arena.getArenaConfig().save();
				arena.msg(sender, Language.parse("whitelist.allcleared"));
				return;
			}
			arena.msg(sender, Language.parse("whitelist.help"));
			return;
		}
		
		if (!subTypes.contains(args[0].toLowerCase())) {
			arena.msg(sender, Language.parse("whitelist.unknowntype", StringParser.joinSet(subTypes, "|")));
			return;
		}
		
		if (!subCommands.contains(args[1].toLowerCase())) {
			arena.msg(sender, Language.parse("whitelist.unknowncommand", StringParser.joinSet(subCommands, "|")));
			return;
		}
		

		List<String> list = new ArrayList<String>();

		list = arena.getArenaConfig().getStringList("blocks.whitelist." + args[0].toLowerCase(), list);
		
		if (args[1].equalsIgnoreCase("add")) {
			list.add(args[2]);
			arena.msg(sender, Language.parse("whitelist.add", args[2], args[0].toLowerCase()));
		} else if (args[1].equalsIgnoreCase("show")) {
			String output = Language.parse("whitelist.show", args[0].toLowerCase());
			for (String s : list) {
				output += ": " + Material.getMaterial(Integer.parseInt(s)).name();
			}
			if (list.size() < 1) {
				output += ": ---------";
			}
			arena.msg(sender, output);
		} else {
			list.remove(args[2]);
			arena.msg(sender, Language.parse("whitelist.remove", args[2]));
		}
		
		arena.getArenaConfig().set("blocks.whitelist." + args[0].toLowerCase(), list);
		arena.getArenaConfig().save();
		
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
