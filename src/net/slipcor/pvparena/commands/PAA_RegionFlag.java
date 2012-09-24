package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionFlag;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena REGIONFLAG Command class</pre>
 * 
 * A command to set an arena region flag
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAA_RegionFlag extends PAA__Command {

	public PAA_RegionFlag() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{2,3})) {
			return;
		}
		
		ArenaRegionShape region = arena.getRegion(args[0]);
		
		if (region == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_REGION_NOTFOUND, args[0]));
			return;
		}
		
		RegionFlag rf = null;
		
		try {
			rf = RegionFlag.valueOf(args[1].toUpperCase());
		} catch (Exception e) {
			// nothing
		}
		
		if (rf == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_REGION_FLAG_NOTFOUND, args[1], StringParser.joinArray(RegionFlag.values(), " ")));
			return;
		}
		
		if (args.length < 3) {
			// toggle
			if (region.flagToggle(rf)) {
				arena.msg(sender, Language.parse(MSG.REGION_FLAG_ADDED, args[1]));
			} else {
				arena.msg(sender, Language.parse(MSG.REGION_FLAG_REMOVED, args[1]));
			}
			region.saveToConfig();
			return;
		}

		if (StringParser.positive.contains(args[2].toLowerCase())) {
			region.flagAdd(rf);
			region.saveToConfig();
			arena.msg(sender, Language.parse(MSG.REGION_FLAG_ADDED, args[1]));
			return;
		}
		
		if (StringParser.negative.contains(args[2].toLowerCase())) {
			region.flagRemove(rf);
			region.saveToConfig();
			arena.msg(sender, Language.parse(MSG.REGION_FLAG_REMOVED, args[1]));
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
}
