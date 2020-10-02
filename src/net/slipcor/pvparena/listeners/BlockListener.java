package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.commands.PAA_Setup;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.Utils;
import net.slipcor.pvparena.loadables.ArenaGoalManager;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionProtection;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.runnables.DamageResetRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Fire;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * <pre>
 * Block Listener class
 * </pre>
 *
 * @author slipcor
 * @version v0.10.2
 */

public class BlockListener implements Listener {
    private static final Debug DEBUG = new Debug(20);

    private boolean willBeSkipped(final Event event, final Location loc, final RegionProtection rp) {
        Arena arena = ArenaManager
                .getArenaByRegionLocation(new PABlockLocation(loc));

        if (arena == null) {
            // no arena at all
            return true;
        }

        if (arena.isLocked() || !arena.isFightInProgress()) {
            if (event instanceof Cancellable) {
                final Cancellable cEvent = (Cancellable) event;
                cEvent.setCancelled(!(PAA_Edit.activeEdits.containsValue(arena) || PAA_Setup.activeSetups.containsValue(arena)));
            }
            return PAA_Edit.activeEdits.containsValue(arena) || PAA_Setup.activeSetups.containsValue(arena);
        }

        arena = ArenaManager.getArenaByProtectedRegionLocation(
                new PABlockLocation(loc), rp);

        if (arena == null) {
            return false;
        }

        return PAA_Edit.activeEdits.containsValue(arena);
    }

    static boolean isProtected(final Location loc, final Cancellable event,
                               final RegionProtection node) {
        final Arena arena = ArenaManager.getArenaByProtectedRegionLocation(
                new PABlockLocation(loc), node);
        if (arena == null) {
            return false;
        }

        if (event instanceof PlayerEvent) {
            final PlayerEvent e = (PlayerEvent) event;

            final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(e.getPlayer().getName());

            if (aPlayer.getArena() != null && aPlayer.getArena() != arena) {
                return false; // players in arenas should be caught by their arenas
            }
        }

        // debug.i("protection " + node.name() + " enabled and thus cancelling " +
        // event.toString());
        event.setCancelled(true);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        DEBUG.i("onBlockBreak", event.getPlayer());
        if (willBeSkipped(event, event.getBlock().getLocation(),
                RegionProtection.BREAK)) {
            DEBUG.i("willbeskipped. GFYS!!!!", event.getPlayer());
            return;
        }

        if (ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.LOST
                || ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.WATCH
                || ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.LOUNGE
                || ArenaPlayer.parsePlayer(event.getPlayer().getName()).getStatus() == Status.READY) {
            event.setCancelled(true);
            return;
        }

        final Arena arena = ArenaManager
                .getArenaByRegionLocation(new PABlockLocation(event.getBlock()
                        .getLocation()));

        final List<String> list = arena.getArenaConfig().getStringList(
                CFG.LISTS_WHITELIST.getNode() + ".break",
                new ArrayList<String>());

        if (!list.isEmpty()
                && !list.contains(String.valueOf(event.getBlock().getType()
                .name()))
                && !list.contains(String.valueOf(event.getBlock().getType()
                .name()))) {
            arena.msg(
                    event.getPlayer(),
                    Language.parse(arena, MSG.ERROR_WHITELIST_DISALLOWED,
                            Language.parse(arena, MSG.GENERAL_BREAK)));
            // not on whitelist. DENY!
            event.setCancelled(true);
            DEBUG.i("whitelist out", event.getPlayer());
            return;
        }

        if (isProtected(event.getBlock().getLocation(), event,
                RegionProtection.BREAK)) {
            DEBUG.i("isprotected!", event.getPlayer());
            return;
        }
        list.clear();
        list.addAll(arena.getArenaConfig().getStringList(
                CFG.LISTS_BLACKLIST.getNode() + ".break",
                new ArrayList<String>()));

        if (list.contains(String.valueOf(event.getBlock().getType().name()))
                || list.contains(String.valueOf(event.getBlock().getType().name()))) {
            arena.msg(
                    event.getPlayer(),
                    Language.parse(arena, MSG.ERROR_BLACKLIST_DISALLOWED,
                            Language.parse(arena, MSG.GENERAL_BREAK)));
            // on blacklist. DENY!
            event.setCancelled(true);
            DEBUG.i("blacklist out", event.getPlayer());
            return;
        }

        PACheck res = ArenaGoalManager.checkBreak(arena, event);

        if (res.hasError()) {
            DEBUG.i("onBlockBreak cancelled by goal: " + res.getModName(), event.getPlayer());
            return;
        }

        DEBUG.i("onBlockBreak !!!", event.getPlayer());

        ArenaModuleManager.onBlockBreak(arena, event.getBlock());


        Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
                new DamageResetRunnable(arena, event.getPlayer(), null), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(final BlockBurnEvent event) {
        final Arena arena = ArenaManager.getArenaByRegionLocation(
                new PABlockLocation(event.getBlock().getLocation()));
        if (arena == null) {
            return; // no arena => out
        }
        if (isProtected(event.getBlock().getLocation(), event,
                RegionProtection.FIRE)) {
            return;
        }

        ArenaModuleManager.onBlockBreak(arena, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDecay(final LeavesDecayEvent event) {
        final Block block = event.getBlock();
        final Arena arena = ArenaManager.getArenaByRegionLocation(
                new PABlockLocation(block.getLocation()));

        if (arena == null) {
            return;
        }

        arena.getDebugger().i("block block decaying inside the arena");

        if (isProtected(event.getBlock().getLocation(), event,
                RegionProtection.NATURE)) {
            return;
        }

        ArenaModuleManager.onBlockBreak(arena, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFade(final BlockFadeEvent event) {
        if (willBeSkipped(event, event.getBlock().getLocation(),
                RegionProtection.NATURE)) {
            return;
        }

        final Block block = event.getBlock();
        final Arena arena = ArenaManager.getArenaByRegionLocation(
                new PABlockLocation(block.getLocation()));

        if (arena == null) {
            return;
        }

        arena.getDebugger().i("block block fading inside the arena");
        if (isProtected(event.getBlock().getLocation(), event,
                RegionProtection.NATURE)) {
            return;
        }

        ArenaModuleManager.onBlockChange(arena, event.getBlock(),
                event.getNewState());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent event) {
        if (willBeSkipped(event, event.getToBlock().getLocation(),
                RegionProtection.NATURE)) {
            return;
        }

        final Block block = event.getToBlock();
        final Arena arena = ArenaManager.getArenaByRegionLocation(
                new PABlockLocation(block.getLocation()));

        if (arena == null) {
            return;
        }

        // arena.getDebugger().info("block fluids inside the arena");

        if (isProtected(block.getLocation(), event, RegionProtection.NATURE)) {
            return;
        }

        ArenaModuleManager.onBlockBreak(arena, event.getBlock());

        ArenaModuleManager.onBlockPlace(arena, block, Material.AIR);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(final BlockFormEvent event) {
        if (willBeSkipped(event, event.getBlock().getLocation(),
                RegionProtection.NATURE)) {
            return;
        }

        final Block block = event.getBlock();
        final Arena arena = ArenaManager.getArenaByRegionLocation(
                new PABlockLocation(block.getLocation()));
        if (arena == null) {
            return;
        }

        if (isProtected(event.getBlock().getLocation(), event,
                RegionProtection.NATURE)) {
            return;
        }

        ArenaModuleManager.onBlockChange(arena, event.getBlock(),
                event.getNewState());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockGrow(final BlockGrowEvent event) {
        Arena arena = ArenaManager.getArenaByProtectedRegionLocation(
                new PABlockLocation(event.getBlock().getLocation()),
                RegionProtection.NATURE);
        Block block = event.getBlock();
        if (arena == null) {
            DEBUG.i("BlockGrowEvent -> no arena");
            return; // no arena => out
        }
        if (isProtected(block.getLocation(), event, RegionProtection.NATURE)) {
            return;
        }

        ArenaModuleManager.onBlockChange(arena, block, block.getState());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockGrow(final StructureGrowEvent event) {
        Arena arena = null;

        for (final BlockState block : event.getBlocks()) {
            arena = ArenaManager.getArenaByRegionLocation(
                    new PABlockLocation(block.getLocation()));
            if (arena != null) {
                break;
            }
        }

        if (arena == null) {
            DEBUG.i("StructureGrowEvent -> no arena");
            return; // no arena => out
        }
        for (final BlockState block : event.getBlocks()) {
            arena = ArenaManager.getArenaByProtectedRegionLocation(
                    new PABlockLocation(block.getLocation()),
                    RegionProtection.NATURE);
            if (arena == null) {
                continue;
            }
            if (isProtected(block.getLocation(), event, RegionProtection.NATURE)) {
                return;
            }

            ArenaModuleManager.onBlockChange(arena, block.getBlock(), block);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(final BlockIgniteEvent event) {
        if (willBeSkipped(event, event.getBlock().getLocation(),
                RegionProtection.FIRE)) {
            return;
        }
        final Arena arena = ArenaManager.getArenaByRegionLocation(
                new PABlockLocation(event.getBlock().getLocation()));
        if (arena == null) {
            return;
        }

        if (arena.getArenaConfig().getBoolean(CFG.PROTECT_ENABLED)
                && (isProtected(event.getBlock().getLocation(), event,
                RegionProtection.FIRE))) {
            return;
        }
        ArenaModuleManager.onBlockBreak(arena, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(final ExplosionPrimeEvent event) {

        if (willBeSkipped(event, event.getEntity().getLocation(),
                RegionProtection.TNT)) {
            return;
        }
        final Arena arena = ArenaManager.getArenaByRegionLocation(
                new PABlockLocation(event.getEntity().getLocation()));
        // all checks done in willBeSkipped
        ArenaModuleManager.onBlockBreak(arena, event.getEntity().getLocation().getBlock());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        Arena arena = null;

        for (final Block block : event.getBlocks()) {
            arena = ArenaManager.getArenaByRegionLocation(
                    new PABlockLocation(block.getLocation()));
            if (arena != null) {
                if (isProtected(event.getBlock().getLocation(), event,
                        RegionProtection.PISTON)) {
                    return;
                }
                break;
            }
        }

        if (arena == null) {
            return; // no arena => out
        }
        arena.getDebugger().i("block piston extend inside the arena");
        for (final Block block : event.getBlocks()) {

            ArenaModuleManager.onBlockPiston(arena, block);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        DEBUG.i("BlockPlace", player);

        if (willBeSkipped(event, block.getLocation(), RegionProtection.PLACE)) {
            return;
        }

        final ArenaPlayer arenaPlayer = ArenaPlayer.parsePlayer(player.getName());
        if (asList(Status.LOST, Status.WATCH, Status.LOUNGE, Status.READY).contains(arenaPlayer.getStatus())) {
            event.setCancelled(true);
            return;
        }

        final Arena arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(block.getLocation()));
        final Block placedBlock = event.getBlockPlaced();

        if (block.getType() == Material.TNT && arena.getArenaConfig().getBoolean(CFG.PLAYER_AUTOIGNITE)) {
            arena.getDebugger().i("autoignite tnt");
            placedBlock.setType(Material.AIR);
            block.getWorld().spawnEntity(Utils.getCenteredLocation(block.getLocation()), EntityType.PRIMED_TNT);
            return;
        }


        List<String> list = arena.getArenaConfig().getStringList(
                CFG.LISTS_WHITELIST.getNode() + ".place",
                new ArrayList<>());

        if (!list.isEmpty() && !list.contains(placedBlock.getType().name())) {
            arena.msg(player, Language.parse(arena, MSG.ERROR_WHITELIST_DISALLOWED, Language.parse(arena, MSG.GENERAL_PLACE)));
            event.setCancelled(true);
            arena.getDebugger().i("not on whitelist. DENY!");
            return;
        }

        if (isProtected(block.getLocation(), event, RegionProtection.PLACE)) {
            if (arena.isFightInProgress() && !isProtected(block.getLocation(), event, RegionProtection.TNT)
                    && block.getType() == Material.TNT) {

                ArenaModuleManager.onBlockPlace(arena, block, event.getBlockReplacedState().getType());
                event.setCancelled(false);
                arena.getDebugger().i("we do not block TNT, so just return if it is TNT");
            } else if (arena.isFightInProgress() && !isProtected(block.getLocation(), event, RegionProtection.FIRE)
                    && block.getBlockData() instanceof Fire) {

                ArenaModuleManager.onBlockPlace(arena, block, event.getBlockReplacedState().getType());
                event.setCancelled(false);
                arena.getDebugger().i("we do not block FIRE, so just return if it is FIRE");
            }
            return;
        }

        list = arena.getArenaConfig().getStringList(
                CFG.LISTS_BLACKLIST.getNode() + ".place",
                new ArrayList<>());

        if (list.contains(placedBlock.getType().name())) {
            arena.msg(player, Language.parse(arena, MSG.ERROR_BLACKLIST_DISALLOWED, Language.parse(arena, MSG.GENERAL_PLACE)));
            event.setCancelled(true);
            arena.getDebugger().i("on blacklist. DENY!");
            return;
        }

        PACheck res = ArenaGoalManager.checkPlace(arena, event);

        if (res.hasError()) {
            DEBUG.i("onBlockPlace cancelled by goal: " + res.getModName(), player);
            return;
        }
        arena.getDebugger().i("BlockPlace not cancelled!");

        ArenaModuleManager.onBlockPlace(arena, block, event.getBlockReplacedState().getType());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(final HangingPlaceEvent event) {
        if (willBeSkipped(event, event.getBlock().getLocation(),
                RegionProtection.PAINTING)) {
            return;
        }

        final Arena arena = ArenaManager.getArenaByProtectedRegionLocation(
                new PABlockLocation(event.getBlock().getLocation()),
                RegionProtection.PAINTING);

        DEBUG.i("painting place", event.getPlayer());

        if (arena == null || isProtected(event.getBlock().getLocation(), event,
                RegionProtection.PAINTING)) {
            return;
        }

        ArenaModuleManager.onBlockPlace(arena, event.getBlock(), event
                .getBlock().getType());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final HangingBreakEvent event) {
        if (willBeSkipped(event, event.getEntity().getLocation(),
                RegionProtection.PAINTING)) {
            return;
        }

        final Arena arena = ArenaManager.getArenaByProtectedRegionLocation(
                new PABlockLocation(event.getEntity().getLocation()),
                RegionProtection.PAINTING);

        if (isProtected(event.getEntity().getLocation(), event,
                RegionProtection.PAINTING)) {
            return;
        }
        if (arena == null) {
            DEBUG.i("painting break inside the arena");
        } else {
            arena.getDebugger().i("painting break inside the arena");
        }
        ArenaModuleManager.onPaintingBreak(arena, event.getEntity(), event
                .getEntity().getType());
    }
}