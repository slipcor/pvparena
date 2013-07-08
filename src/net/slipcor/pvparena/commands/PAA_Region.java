package net.slipcor.pvparena.commands;

import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionShape;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena REGION Command class</pre>
 * 
 * A command to manage arena regions
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Region extends AbstractArenaCommand {
	
	public static Map<String, Arena> activeSelections = new HashMap<String, Arena>();

	private static String selector = null;
	
	public PAA_Region() {
		super(new String[] {});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!argCountValid(sender, arena, args, new Integer[]{0,1,2,3})) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
			return;
		}
		
		if (args.length < 1) {
			// usage: /pa {arenaname} region | activate region selection
			
			if (activeSelections.get(sender.getName()) != null) {
				// already selecting!
				if (sender.getName().equals(selector)) {
					arena.msg(sender,  Language.parse(arena, MSG.ERROR_REGION_YOUSELECTEXIT));
					selector = null;
				} else {
					arena.msg(sender, Language.parse(arena, MSG.ERROR_REGION_YOUSELECT, arena.getName()));
					arena.msg(sender,  Language.parse(arena, MSG.ERROR_REGION_YOUSELECT2));
					selector = sender.getName();
				}
				return;
			}
			// selecting now!
			activeSelections.put(sender.getName(), arena);
			arena.msg(sender, Language.parse(arena, MSG.REGION_YOUSELECT, arena.getName()));
			arena.msg(sender, Language.parse(arena, MSG.REGION_SELECT, arena.getName()));
			return;
		} else if (args.length == 2 && args[1].equalsIgnoreCase("border")) {
			// usage: /pa {arenaname} region [regionname] border | check a region border
			final ArenaRegionShape region = arena.getRegion(args[0]);
			
			if (region == null) {
				arena.msg(sender, Language.parse(arena, MSG.ERROR_REGION_NOTFOUND, args[0]));
				return;
			}
			region.showBorder((Player) sender);
			return;
		} else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
			// usage: /pa {arenaname} region remove [regionname] | remove a region
			final ArenaRegionShape region = arena.getRegion(args[1]);
			
			if (region == null) {
				arena.msg(sender, Language.parse(arena, MSG.ERROR_REGION_NOTFOUND, args[1]));
				return;
			}
			arena.getArenaConfig().setManually("arenaregion." + region.getRegionName(), null);
			arena.msg(sender, Language.parse(arena, MSG.REGION_REMOVED, region.getRegionName()));
			
			arena.getRegions().remove(region);
			arena.getArenaConfig().save();
			return;
		} else if (args.length < 3) {
			// usage: /pa {arenaname} region [regionname] {regionshape} | save selected region
			
			final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sender.getName());
			
			if (!aPlayer.didValidSelection()) {
				arena.msg(sender, Language.parse(arena, MSG.REGION_SELECT, arena.getName()));
				return;
			}
			
			final PABlockLocation[] locs = aPlayer.getSelection();
			RegionShape shape;
			
			if (args.length == 2) {
				shape = ArenaRegionShape.getShapeFromShapeName(args[1]);
			} else {
				shape = ArenaRegionShape.RegionShape.CUBOID;
			}
			
			final ArenaRegionShape region = ArenaRegionShape.create(arena, args[0], shape, locs);
			
			arena.addRegion(region);
			region.saveToConfig();
			
			activeSelections.remove(sender.getName());

			aPlayer.unsetSelection();
			
			arena.msg(sender, Language.parse(arena, MSG.REGION_SAVED, args[0]));
			return;
		}
		
		final ArenaRegionShape region = arena.getRegion(args[0]);
		
		if (region == null) {
			arena.msg(sender, Language.parse(arena, MSG.ERROR_REGION_NOTFOUND, args[0]));
			return;
		}
		
		String message = region.update(args[1], args[2]);
		
		if (message != null) {
			if (arena.getArenaConfig().getBoolean(CFG.MODULES_WORLDEDIT_AUTOSAVE)) {
				Bukkit.getServer().dispatchCommand(sender, "pvparena " + arena.getName() + " regsave " + region.getRegionName());
			}
			arena.msg(sender, message);
		}
		
		// usage: /pa {arenaname} region [regionname] radius [number]
		// usage: /pa {arenaname} region [regionname] height [number]
		// usage: /pa {arenaname} region [regionname] position [position]
		
		
		
		// #region name can be anything you want
		// #radius should be clear
		// #height is not needed / parsed for spheric regions
		// #position is the alignment to the battlefield
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.REGION));
	}
}
