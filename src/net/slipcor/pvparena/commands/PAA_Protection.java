package net.slipcor.pvparena.commands;

import java.util.HashMap;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionProtection;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena PROTECTION Command class</pre>
 * 
 * A command to manage arena region protections
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

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
		
		if (!argCountValid(sender, arena, args, new Integer[]{2,3})) {
			return;
		}
		
		ArenaRegionShape region = arena.getRegion(args[0]);
		
		if (region == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_REGION_NOTFOUND, args[0]));
			return;
		}
		
		RegionProtection rf = null;
		
		try {
			rf = RegionProtection.valueOf(args[1].toUpperCase());
		} catch (Exception e) {
			// nothing
		}
		
		if (rf == null && (!args[1].equalsIgnoreCase("all"))) {
			arena.msg(sender, Language.parse(MSG.ERROR_REGION_FLAG_NOTFOUND, args[1], StringParser.joinArray(RegionProtection.values(), " ")));
			return;
		}
		
		if (args.length < 3) {
			// toggle
			if (region.protectionToggle(rf)) {
				arena.msg(sender, Language.parse(MSG.REGION_FLAG_ADDED, args[1]));
			} else {
				arena.msg(sender, Language.parse(MSG.REGION_FLAG_REMOVED, args[1]));
			}
			region.saveToConfig();
			return;
		}

		if (StringParser.positive.contains(args[2].toLowerCase())) {
			region.protectionAdd(rf);
			arena.msg(sender, Language.parse(MSG.REGION_FLAG_ADDED, args[1]));
			region.saveToConfig();
			return;
		}
		
		if (StringParser.negative.contains(args[2].toLowerCase())) {
			region.protectionRemove(rf);
			arena.msg(sender, Language.parse(MSG.REGION_FLAG_REMOVED, args[1]));
			region.saveToConfig();
			return;
		}
			
		// usage: /pa {arenaname} regionflag [regionname] [regionflag] {value}

		arena.msg(sender, Language.parse(MSG.ERROR_INVALID_VALUE, args[2]));
		arena.msg(sender, Language.parse(MSG.ERROR_POSITIVES, StringParser.joinSet(StringParser.positive, " | ")));
		arena.msg(sender, Language.parse(MSG.ERROR_NEGATIVES, StringParser.joinSet(StringParser.negative, " | ")));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.PROTECTION));
	}
}
