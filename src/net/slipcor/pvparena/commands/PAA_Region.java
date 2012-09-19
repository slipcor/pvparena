package net.slipcor.pvparena.commands;

import java.util.HashMap;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionShape;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena REGION Command class</pre>
 * 
 * A command to manage arena regions
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAA_Region extends PAA__Command {
	
	public static HashMap<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Region() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0,1,2,3})) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}
		
		if (args.length < 1) {
			// usage: /pa {arenaname} region | activate region selection
			
			if (activeSelections.get(sender.getName()) != null) {
				// already selecting!
				arena.msg(sender, Language.parse(MSG.ERROR_REGION_YOUSELECT, arena.getName()));
				return;
			}
			// selecting now!
			activeSelections.put(sender.getName(), arena);
			arena.msg(sender, Language.parse(MSG.REGION_YOUSELECT, arena.getName()));
			arena.msg(sender, Language.parse(MSG.REGION_SELECT, arena.getName()));
			return;
		} else if (args.length == 2 && args[1].equalsIgnoreCase("border")) {
			// usage: /pa {arenaname} region [regionname] border | check a region border
			ArenaRegionShape region = arena.getRegion(args[0]);
			
			if (region == null) {
				arena.msg(sender, Language.parse(MSG.ERROR_REGION_NOTFOUND, args[0]));
				return;
			}
			region.showBorder((Player) sender);
		} else if (args.length < 3) {
			// usage: /pa {arenaname} region [regionname] {regionshape} | save selected region
			
			ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
			
			if (!ap.didValidSelection()) {
				arena.msg(sender, Language.parse(MSG.REGION_SELECT, arena.getName()));
				return;
			}
			
			PABlockLocation[] locs = ap.getSelection();
			RegionShape shape;
			
			if (args.length == 2) {
				shape = ArenaRegionShape.getShapeFromShapeName(args[1]);
			} else {
				shape = ArenaRegionShape.RegionShape.CUBOID;
			}
			
			ArenaRegionShape region = ArenaRegionShape.create(arena, args[0], shape, locs);
			
			arena.addRegion(region);
			arena.getArenaConfig().setManually("arenaregion." + args[0], Config.parseToString(region));
			return;
		}
		
		ArenaRegionShape region = arena.getRegion(args[0]);
		
		if (region == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_REGION_NOTFOUND, args[0]));
			return;
		}
		
		region.update(args[1], args[2]);
		
		// usage: /pa {arenaname} region [regionname] radius [number]
		// usage: /pa {arenaname} region [regionname] height [number]
		// usage: /pa {arenaname} region [regionname] position [position]
		// usage: /pa {arenaname} region [regionname] flag [flag]
		
		
		
		// #region name can be anything you want
		// #radius should be clear
		// #height is not needed / parsed for spheric regions
		// #position is the alignment to the battlefield
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
