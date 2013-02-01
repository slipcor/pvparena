package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena REGIONS Command class</pre>
 * 
 * A command to debug arena regions
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Regions extends AbstractArenaCommand {

	public PAA_Regions() {
		super(new String[] {});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}

		// /pa [] regions
		// /pa [] regions [regionname]
		
		if (!argCountValid(sender, arena, args, new Integer[]{0,1})) {
			return;
		}
		
		if (args.length < 1) {
			arena.msg(sender, Language.parse(MSG.REGIONS_LISTHEAD, arena.getName()));
			
			for (ArenaRegionShape ars : arena.getRegions()) {
				arena.msg(sender, Language.parse(MSG.REGIONS_LISTVALUE, ars.getRegionName(), ars.getType().name(), ars.getShape().name()));
			}
			return;
		}
		
		final ArenaRegionShape region = arena.getRegion(args[0]);
		
		if (region == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_REGION_NOTFOUND, args[0]));
			return;
		}

		arena.msg(sender, Language.parse(MSG.REGIONS_HEAD, arena.getName()+":"+args[0]));
		arena.msg(sender, Language.parse(MSG.REGIONS_TYPE, region.getType().name()));
		arena.msg(sender, Language.parse(MSG.REGIONS_SHAPE, region.getShape().name()));
		arena.msg(sender, Language.parse(MSG.REGIONS_FLAGS, StringParser.joinSet(region.getFlags(), ", ")));
		arena.msg(sender, Language.parse(MSG.REGIONS_PROTECTIONS, StringParser.joinSet(region.getProtections(), ", ")));
		arena.msg(sender, "0: " + region.getLocs()[0].toString());
		arena.msg(sender, "1: " + region.getLocs()[1].toString());
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.REGIONS));
	}
}
