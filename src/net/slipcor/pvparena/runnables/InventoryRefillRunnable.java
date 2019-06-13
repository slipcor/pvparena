package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Utils;
import net.slipcor.pvparena.managers.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private final List<ItemStack> additions = new ArrayList<>();
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

        boolean keepAll = this.arena.getArenaConfig().getBoolean(CFG.ITEMS_KEEPALLONRESPAWN);

        if (this.arena.getArenaConfig().getItems(CFG.ITEMS_KEEPONRESPAWN) != null) {
            final ItemStack[] items = this.arena.getArenaConfig().getItems(CFG.ITEMS_KEEPONRESPAWN);

            for (final ItemStack item : itemList) {
                if (item != null) {
                    if (keepAll) {
                        additions.add(item);
                        continue;
                    }
                    for (final ItemStack iItem : items) {
                        if (iItem != null) {
                            if (item.getType() != iItem.getType()) {
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
            if ("custom".equals(aPlayer.getArenaClass().getName()) && !arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLCUSTOMINVENTORY) || !arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY)) {
                if (refill) {
                    final ItemStack[] items = new ItemStack[additions.size()];
                    int pos = 0;
                    for (final ItemStack item : additions) {
                        items[pos++] = item;
                    }
                    if(items.length > 0){
                        ArenaClass.equip(player, items);
                    } else {
                        PVPArena.instance.getLogger().info("Can't refill inventory, please set " + CFG.ITEMS_KEEPONRESPAWN.getNode()
                                + ", " + CFG.ITEMS_KEEPALLONRESPAWN.getNode() + " or " + CFG.PLAYER_REFILLCUSTOMINVENTORY.getNode() + " parameter");
                    }
                }
                if (arena.getArenaConfig().getBoolean(CFG.USES_WOOLHEAD)) {
                    final ArenaTeam aTeam = aPlayer.getArenaTeam();
                    final ChatColor chatColor = aTeam.getColor();
                    arena.getDebugger().i("forcing woolhead: " + aTeam.getName() + '/'
                            + chatColor.name(), player);
                    player.getInventory().setHelmet(
                            new ItemStack(Utils.getWoolMaterialFromChatColor(chatColor), 1));
                    PVPArena.instance.getAgm().refillInventory(arena, player);
                }
            } else if (refill && "custom".equals(aPlayer.getArenaClass().getName())) {
                ArenaPlayer.reloadInventory(arena, player, false);

                for (final ItemStack item : additions) {
                    player.getInventory().addItem(item);
                }
            } else if (refill) {
                InventoryManager.clearInventory(player);
                ArenaPlayer.givePlayerFightItems(arena, player);

                for (final ItemStack item : additions) {
                    player.getInventory().addItem(item);
                }
            }
        } else {
            arena.getDebugger().i("NOT");
        }
        player.setFireTicks(0);
        try {
            Bukkit.getScheduler().runTaskLater(PVPArena.instance, new Runnable() {
                @Override
                public void run() {
                    if (player.getFireTicks() > 0) {
                        player.setFireTicks(0);
                    }
                }
            }, 5L);
        } catch (Exception e) {
        }
    }
}
