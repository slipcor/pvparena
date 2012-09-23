package net.slipcor.pvparena.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;

public class RegionTool extends ArenaModule {
	public RegionTool() {
		super("RegionTool");
	}

	@Override
	public String version() {
		return "v0.9.0.0";
	}

	@Override
	public boolean isActive(Arena arena) {
		return true;
	}

	@Override
	public boolean onPlayerInteract(PlayerInteractEvent event) {
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
				db.i("reading wand");
				try {
					mMat = Material.getMaterial(arena.getArenaConfig().getInt(CFG.GENERAL_WAND));
				} catch (Exception e) {
					db.i("exception reading ready block");
					String sMat = arena.getArenaConfig().getString(CFG.GENERAL_WAND);
					Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_MAT_NOT_FOUND, sMat));
					return false;
				}
				db.i("mMat now is " + mMat.name());
				if (event.getPlayer().getItemInHand().getType() == mMat) {
					PABlockLocation loc = new PABlockLocation(event.getPlayer().getLocation());
					if (event.getClickedBlock() != null) {
						loc = new PABlockLocation(event.getClickedBlock().getLocation());
					}
					for (ArenaRegionShape region : arena.getRegions()) {
						if (region.contains(loc)) {
							ArenaManager.tellPlayer(event.getPlayer(), "§fArena §b"
									+ arena.getName() + "§f: region §b"
									+ region.getName());
						}
					}
				}
			}
		}
		return false;
	}
}
