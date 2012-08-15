package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


import net.slipcor.pvparena.PA;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionFlag;

import org.bukkit.command.CommandSender;

public class PAA_RegionFlag extends PAA__Command {
	
	public static HashMap<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_RegionFlag() {
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
		
		RegionFlag rf = null;
		
		try {
			rf = RegionFlag.valueOf(args[1].toUpperCase());
		} catch (Exception e) {
			// nothing
		}
		
		if (rf == null) {
			arena.msg(sender, Language.parse("region.flagnotfound", args[1], StringParser.joinArray(RegionFlag.values(), " ")));
			return;
		}
		
		if (args.length < 3) {
			// toggle
			if (region.flagToggle(rf)) {
				arena.msg(sender, Language.parse("region.flag_added", args[1]));
			} else {
				arena.msg(sender, Language.parse("region.flag_removed", args[1]));
			}
			return;
		}

		if (PA.positive.contains(args[2].toLowerCase())) {
			region.flagAdd(rf);
			arena.msg(sender, Language.parse("region.flag_added", args[1]));
			return;
		}
		
		if (PA.negative.contains(args[2].toLowerCase())) {
			region.flagRemove(rf);
			arena.msg(sender, Language.parse("region.flag_removed", args[1]));
			return;
		}
			
		// usage: /pa {arenaname} regionflag [regionname] [regionflag] {value}

		arena.msg(sender, Language.parse("error.valuenotfound", args[2]));
		arena.msg(sender, Language.parse("error.valuepos", StringParser.joinSet(PA.positive, " | ")));
		arena.msg(sender, Language.parse("error.valueneg", StringParser.joinSet(PA.negative, " | ")));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
