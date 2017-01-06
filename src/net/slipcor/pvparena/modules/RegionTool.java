package net.slipcor.pvparena.modules;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * <pre>Arena Module class "RegionTool"</pre>
 * <p/>
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
        return PVPArena.instance.getDescription().getVersion();
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public boolean onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getPlayer().getEquipment().getItemInMainHand() == null
                || event.getPlayer().getEquipment().getItemInMainHand().getType() == Material.AIR) {
            return false;
        }

        if (event.getPlayer().getEquipment().getItemInMainHand().getType() == Material.AIR) {
            return false;
        }

        if (!PVPArena.hasAdminPerms(event.getPlayer())) {
            return false;
        }

        for (final Arena arena : ArenaManager.getArenas()) {
            if (arena.getArenaConfig().getInt(CFG.GENERAL_WAND) > 0) {
                arena.getDebugger().i("reading wand", event.getPlayer());
                final Material mMat = Material.getMaterial(arena.getArenaConfig().getString(CFG.GENERAL_WAND));
                arena.getDebugger().i("mMat now is " + mMat.name(), event.getPlayer());

                if (event.getPlayer().getEquipment().getItemInMainHand().getType() == mMat) {
                    PABlockLocation loc = new PABlockLocation(event.getPlayer().getLocation());
                    if (event.getClickedBlock() != null) {
                        loc = new PABlockLocation(event.getClickedBlock().getLocation());
                    }
                    for (final ArenaRegion region : arena.getRegions()) {
                        if (region.getShape().contains(loc)) {
                            arena.msg(event.getPlayer(), ChatColor.COLOR_CHAR + "fArena " + ChatColor.COLOR_CHAR + 'b'
                                    + arena.getName() + ChatColor.COLOR_CHAR + "f: region " + ChatColor.COLOR_CHAR + 'b'
                                    + region.getRegionName());
                        }
                    }
                }
            }
        }
        return false;
    }
}
