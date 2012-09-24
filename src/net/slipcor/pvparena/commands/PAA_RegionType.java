package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena REGIONTYPE Command class</pre>
 * 
 * A command to set an arena region type
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAA_RegionType extends PAA__Command {
	
	public PAA_RegionType() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{2})) {
			return;
		}
		
		ArenaRegionShape region = arena.getRegion(args[0]);
		
		if (region == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_REGION_NOTFOUND, args[0]));
			return;
		}
		
		RegionType rf = null;
		
		try {
			rf = RegionType.valueOf(args[1].toUpperCase());
		} catch (Exception e) {
			// nothing
		}
		
		if (rf == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_REGION_TYPE_NOTFOUND, args[1], StringParser.joinArray(RegionType.values(), " ")));
			return;
		}
		
		region.setType(rf);
		region.saveToConfig();
		arena.msg(sender, Language.parse(MSG.REGION_TYPE_SET, rf.name()));
	
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
