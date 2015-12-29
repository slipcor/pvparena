package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaGoalManager;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionProtection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

/**
 * <pre>Inventory Listener class</pre>
 *
 * @author slipcor
 * @version v0.10.2
 */

public class InventoryListener implements Listener {
    private final static Debug DEBUG = new Debug(22);

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        final Arena arena = ArenaPlayer.parsePlayer(player.getName()).getArena();

        if (arena == null) {
            return;
        }

        arena.getDebugger().i("InventoryClick: arena player", player);

        if (!arena.getArenaConfig().getBoolean(CFG.PLAYER_MAYCHANGEARMOR)) {
            if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getInventory().getType() == InventoryType.CRAFTING) {
            // we are inside the standard
            if (event.getRawSlot() != 5) {
                return;
            }
            if (arena.getArenaConfig().getBoolean(CFG.USES_WOOLHEAD)) {
                event.setCancelled(true);
                return;
            }
        }


        PACheck res = ArenaGoalManager.checkInventory(arena, event);

        if (res.hasError()) {
            DEBUG.i("onInventoryClick cancelled by goal: " + res.getModName(), player);
            return;
        }


        if (!BlockListener.isProtected(event.getWhoClicked().getLocation(), event, RegionProtection.INVENTORY)) {
            // we don't need no protection => out!
            return;
        }

        if (event.isShiftClick()) {
            // we never want shift clicking!
            event.setCancelled(true);
            return;
        }

        arena.getDebugger().i("cancelling!", player);
        // player is carrying a flag
        event.setCancelled(true);
    }
}
