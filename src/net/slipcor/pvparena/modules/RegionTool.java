package net.slipcor.pvparena.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

import net.slipcor.pvparena.PVPArena;
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
 */

public class RegionTool extends ArenaModule {
	public RegionTool() {
		super("RegionTool");
		debug = new Debug(19);
	}

	@Override
	public String version() {
		return "v1.0.1.59";
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
		
		if (!PVPArena.hasAdminPerms(event.getPlayer())) {
			return false;
		}

		for (Arena arena : ArenaManager.getArenas()) {
			Material mMat = Material.STICK;
			if (arena.getArenaConfig().getInt(CFG.GENERAL_WAND) > 0) {
				arena.getDebugger().i("reading wand", event.getPlayer());
				try {
					mMat = Material.getMaterial(arena.getArenaConfig().getInt(CFG.GENERAL_WAND));
					arena.getDebugger().i("mMat now is " + mMat.name(), event.getPlayer());
				} catch (Exception e) {
					arena.getDebugger().i("exception reading ready block", event.getPlayer());
					final String sMat = arena.getArenaConfig().getString(CFG.GENERAL_WAND);
					arena.msg(Bukkit.getConsoleSender(), Language.parse(arena, MSG.ERROR_MAT_NOT_FOUND, sMat));
					continue;
				}
				if (event.getPlayer().getItemInHand().getType() == mMat) {
					PABlockLocation loc = new PABlockLocation(event.getPlayer().getLocation());
					if (event.getClickedBlock() != null) {
						loc = new PABlockLocation(event.getClickedBlock().getLocation());
					}
					for (ArenaRegionShape region : arena.getRegions()) {
						if (region.contains(loc)) {
							arena.msg(event.getPlayer(), ChatColor.COLOR_CHAR + "fArena " + ChatColor.COLOR_CHAR + "b"
									+ arena.getName() + ChatColor.COLOR_CHAR + "f: region " + ChatColor.COLOR_CHAR + "b"
									+ region.getRegionName());
						}
					}
				}
			}
		}
		return false;
	}
}
