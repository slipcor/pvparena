package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionFlag;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionProtection;

import org.bukkit.command.CommandSender;

public class PAA_Protection extends PAA__Command {
	
	public static HashMap<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Protection() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(2,3)))) {
			return;
		}
		
		ArenaRegion region = arena.getRegion(args[0]);
		
		if (region == null) {
			arena.msg(sender, Language.parse("region.notfound", args[0]));
			return;
		}
		
		RegionProtection rf = null;
		
		try {
			rf = RegionProtection.valueOf(args[1].toUpperCase());
		} catch (Exception e) {
			// nothing
		}
		
		if (rf == null && (!args[1].equalsIgnoreCase("all"))) {
			arena.msg(sender, Language.parse("region.protectionnotfound", args[1], StringParser.joinArray(RegionFlag.values(), " ")));
			return;
		}
		
		if (args.length < 3) {
			// toggle
			if (region.protectionToggle(rf)) {
				arena.msg(sender, Language.parse("region.protection_added", args[1]));
			} else {
				arena.msg(sender, Language.parse("region.protection_removed", args[1]));
			}
			return;
		}

		if (StringParser.positive.contains(args[2].toLowerCase())) {
			region.protectionAdd(rf);
			arena.msg(sender, Language.parse("region.protection_added", args[1]));
			return;
		}
		
		if (StringParser.negative.contains(args[2].toLowerCase())) {
			region.protectionRemove(rf);
			arena.msg(sender, Language.parse("region.protection_removed", args[1]));
			return;
		}
			
		// usage: /pa {arenaname} regionflag [regionname] [regionflag] {value}

		arena.msg(sender, Language.parse("error.valuenotfound", args[2]));
		arena.msg(sender, Language.parse("error.valuepos", StringParser.joinSet(StringParser.positive, " | ")));
		arena.msg(sender, Language.parse("error.valueneg", StringParser.joinSet(StringParser.negative, " | ")));
		
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
