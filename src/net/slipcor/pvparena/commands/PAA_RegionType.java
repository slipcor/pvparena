package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena REGIONTYPE Command class</pre>
 *
 * A command to set an arena region type
 *
 * @author slipcor
 *
 * @version v0.10.0
 */

public class PAA_RegionType extends AbstractArenaCommand {

	public PAA_RegionType() {
		super(new String[] {});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}

		if (!argCountValid(sender, arena, args, new Integer[]{2})) {
			return;
		}

		final ArenaRegion region = arena.getRegion(args[0]);

		if (region == null) {
			arena.msg(sender, Language.parse(arena, MSG.ERROR_REGION_NOTFOUND, args[0]));
			return;
		}

		RegionType regionType;

		try {
			regionType = RegionType.valueOf(args[1].toUpperCase());
		} catch (Exception e) {
			arena.msg(sender, Language.parse(arena, MSG.ERROR_REGION_TYPE_NOTFOUND, args[1], StringParser.joinArray(RegionType.values(), " ")));
			return;
		}

		region.setType(regionType);
		if (regionType.equals(RegionType.BATTLE)) {
			region.protectionSetAll(true);
		}
		region.saveToConfig();
		arena.msg(sender, Language.parse(arena, MSG.REGION_TYPE_SET, regionType.name()));

	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.REGIONTYPE));
	}
}
