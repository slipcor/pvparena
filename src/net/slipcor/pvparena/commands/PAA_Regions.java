package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
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
 * @version v0.9.0
 */

public class PAA_Regions extends PAA__Command {

	public PAA_Regions() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}

		// /pa [] regions
		// /pa [] regions [regionname]
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0,1})) {
			return;
		}
		
		if (args.length < 1) {
			sender.sendMessage(Language.parse(MSG.REGIONS_LISTHEAD, arena.getName()));
			
			for (ArenaRegionShape ars : arena.getRegions()) {
				sender.sendMessage(Language.parse(MSG.REGIONS_LISTVALUE, ars.getRegionName(), ars.getType().name(), ars.getShape().name()));
			}
		}
		
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
