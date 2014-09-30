package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>Arena Runnable class "Inventory"</pre>
 * <p/>
 * An arena timer to restore a player's inventory
 *
 * @author slipcor
 * @version v0.10.2
 */

public class InventoryRefillRunnable implements Runnable {
    private final Player player;
    private final List<ItemStack> additions = new ArrayList<ItemStack>();
    private final Arena arena;
    private final boolean refill;

    public InventoryRefillRunnable(final Arena arena, final Player player, final List<ItemStack> itemList) {
        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
        if (arena == null && aPlayer.getArena() == null) {
            this.player = null;
            this.arena = null;
            refill = false;
            return;
        }
        this.arena = arena == null ? aPlayer.getArena() : arena;

        if (!"none".equals(this.arena.getArenaConfig().getString(CFG.ITEMS_KEEPONRESPAWN))) {
            final ItemStack[] items = StringParser.getItemStacksFromString(this.arena.getArenaConfig().getString(CFG.ITEMS_KEEPONRESPAWN));

            for (final ItemStack item : itemList) {
                if (item != null) {
                    for (final ItemStack iItem : items) {
                        if (iItem != null) {
                            if (item.getType() != iItem.getType()) {
                                continue;
                            }

                            if (item.getData().getData() != iItem.getData().getData()) {
                                continue;
                            }

                            additions.add(item);
                            break;
                        }
                    }
                }
            }
        }

        refill = this.arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY);

        Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this, 3L);
        this.player = player;
    }

    @Override
    public void run() {
        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
        arena.getDebugger().i("refilling " + player.getName());
        if (aPlayer.getStatus() == Status.FIGHT) {
            if ("custom".equals(aPlayer.getArenaClass().getName()) && !arena.getArenaConfig().getBoolean(CFG.GENERAL_CUSTOMRETURNSGEAR) || !arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY)) {
                if (refill) {
                    final ItemStack[] items = new ItemStack[additions.size()];
                    int pos = 0;
                    for (final ItemStack item : additions) {
                        items[pos++] = item;
                    }
                    ArenaClass.equip(player, items);
                }
                if (arena.getArenaConfig().getBoolean(CFG.USES_WOOLHEAD)) {
                    final ArenaTeam aTeam = aPlayer.getArenaTeam();
                    final String color = aTeam.getColor().name();
                    arena.getDebugger().i("forcing woolhead: " + aTeam.getName() + '/'
                            + color, player);
                    player.getInventory().setHelmet(
                            new ItemStack(Material.WOOL, 1, StringParser
                                    .getColorDataFromENUM(color)));
                    PVPArena.instance.getAgm().refillInventory(arena, player);
                }
            } else if (refill && "custom".equals(aPlayer.getArenaClass().getName())) {
                InventoryManager.clearInventory(player);
                ArenaPlayer.reloadInventory(arena, player, false);

                for (final ItemStack item : additions) {
                    player.getInventory().addItem(item);
                }
                player.updateInventory();
            } else if (refill) {
                InventoryManager.clearInventory(player);
                ArenaPlayer.givePlayerFightItems(arena, player);

                for (final ItemStack item : additions) {
                    player.getInventory().addItem(item);
                }
                player.updateInventory();
            }
        } else {
            arena.getDebugger().i("NOT");
        }
        player.setFireTicks(0);
    }
}
