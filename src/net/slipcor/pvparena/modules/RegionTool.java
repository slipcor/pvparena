package net.slipcor.pvparena.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;

/**
 * <pre>Arena Module class "RegionTool"</pre>
 * 
 * Enables region debug via WAND item
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class RegionTool extends ArenaModule {
	public RegionTool() {
		super("RegionTool");
		debug = new Debug(19);
	}

	@Override
	public String version() {
		return "v0.10.3.0";
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public boolean onPlayerInteract(final PlayerInteractEvent event) {
		if (event.getPlayer().getItemInHand() == null
				|| event.getPlayer().getItemInHand().getType() == Material.AIR) {
			return false;
		}

		if (event.getPlayer().getItemInHand().getType() == Material.AIR) {
			return false;
		}

		for (Arena arena : ArenaManager.getArenas()) {
			Material mMat = Material.STICK;
			if (arena.getArenaConfig().getInt(CFG.GENERAL_WAND) > 0) {
				debug.i("reading wand", event.getPlayer());
				try {
					mMat = Material.getMaterial(arena.getArenaConfig().getInt(CFG.GENERAL_WAND));
				} catch (Exception e) {
					debug.i("exception reading ready block", event.getPlayer());
					final String sMat = arena.getArenaConfig().getString(CFG.GENERAL_WAND);
					Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_MAT_NOT_FOUND, sMat));
					return false;
				}
				debug.i("mMat now is " + mMat.name(), event.getPlayer());
				if (event.getPlayer().getItemInHand().getType() == mMat) {
					PABlockLocation loc = new PABlockLocation(event.getPlayer().getLocation());
					if (event.getClickedBlock() != null) {
						loc = new PABlockLocation(event.getClickedBlock().getLocation());
					}
					for (ArenaRegionShape region : arena.getRegions()) {
						if (region.contains(loc)) {
							Arena.pmsg(event.getPlayer(), "§fArena §b"
									+ arena.getName() + "§f: region §b"
									+ region.getRegionName());
						}
					}
				}
			}
		}
		return false;
	}
}
