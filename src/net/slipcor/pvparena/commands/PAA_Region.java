package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaRegion.RegionShape;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(0,1,2,3)))) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse("command.onlyplayers"));
			return;
		}
		
		if (args.length < 1) {
			// usage: /pa {arenaname} region | activate region selection
			
			if (activeSelections.get(sender.getName()) != null) {
				// already selecting!
				arena.msg(sender, Language.parse("region.you_already", arena.getName()));
				return;
			}
			// selecting now!
			arena.msg(sender, Language.parse("region.you_select", arena.getName()));
			arena.msg(sender, Language.parse("region.select", arena.getName()));
			return;
		} else if (args.length == 2 && args[1].equalsIgnoreCase("border")) {
			// usage: /pa {arenaname} region [regionname] border | check a region border
			ArenaRegion region = arena.getRegion(args[0]);
			
			if (region == null) {
				arena.msg(sender, Language.parse("region.notfound", args[0]));
				return;
			}
			region.showBorder((Player) sender);
		} else if (args.length < 3) {
			// usage: /pa {arenaname} region [regionname] {regionshape} | save selected region
			
			ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
			
			if (!ap.didValidSelection()) {
				arena.msg(sender, Language.parse("region.select", arena.getName()));
				return;
			}
			
			PABlockLocation[] locs = ap.getSelection();
			RegionShape shape;
			
			if (args.length == 2) {
				shape = ArenaRegion.getShapeFromShapeName(args[1]);
			} else {
				shape = ArenaRegion.RegionShape.CUBOID;
			}
			
			arena.addRegion(ArenaRegion.create(arena, args[0], shape, locs));
			return;
		}
		
		ArenaRegion region = arena.getRegion(args[0]);
		
		if (region == null) {
			arena.msg(sender, Language.parse("region.notfound", args[0]));
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
