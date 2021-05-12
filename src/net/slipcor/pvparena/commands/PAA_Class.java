package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.InventoryManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.slipcor.pvparena.core.Utils.getSerializableItemStacks;

/**
 * <pre>
 * PVP Arena CLASS Command class
 * </pre>
 * <p/>
 * A command to manage arena classes
 *
 * @author slipcor
 * @version v0.10.1
 */

public class PAA_Class extends AbstractArenaCommand {

    public PAA_Class() {
        super(new String[]{"pvparena.cmds.class"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{1, 2})) {
            return;
        }

        if (!(sender instanceof Player)) {
            Arena.pmsg(sender, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
            return;
        }

        // /pa {arenaname} class save [name]
        // /pa {arenaname} class load [name]
        // /pa {arenaname} class remove [name]

        if (args.length == 1) {
            final Player player = (Player) sender;
            PVPArena.instance.getLogger().info("Exiting edit mode: " + player.getName());

            final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

            ArenaPlayer.reloadInventory(arena, player, false);

            aPlayer.setArena(null);
            return;
        }

        if ("save".equalsIgnoreCase(args[0])) {
            final Player player = (Player) sender;
            final List<ItemStack> items = new ArrayList<>();

            arena.getArenaConfig().setManually("classitems." + args[1] + ".items", getSerializableItemStacks(player.getInventory().getStorageContents()));
            arena.getArenaConfig().setManually("classitems." + args[1] + ".offhand", getSerializableItemStacks(player.getInventory().getItemInOffHand()));
            arena.getArenaConfig().setManually("classitems." + args[1] + ".armor", getSerializableItemStacks(player.getInventory().getArmorContents()));
            arena.getArenaConfig().save();

            arena.addClass(args[1], player.getInventory().getStorageContents(), player.getInventory().getItemInOffHand(), player.getInventory().getArmorContents());
            Arena.pmsg(player, Language.parse(arena, MSG.CLASS_SAVED, args[1]));
        } else if ("load".equalsIgnoreCase(args[0])) {
            final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sender.getName());
            if(aPlayer.getArenaClass() == null) {
                ArenaPlayer.backupAndClearInventory(arena, aPlayer.get());
            } else {
                InventoryManager.clearInventory(aPlayer.get());
            }
            arena.selectClass(aPlayer, args[1]);
        } else if ("remove".equalsIgnoreCase(args[0])) {
            final Player player = (Player) sender;
            arena.getArenaConfig().setManually("classitems." + args[1], null);
            arena.getArenaConfig().save();
            arena.removeClass(args[1]);
            Arena.pmsg(player, Language.parse(arena, MSG.CLASS_REMOVED, args[1]));
        }
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.CLASS));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("class");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!cl");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"save"});
        if (arena == null) {
            return result;
        }
        for (final ArenaClass aclass : arena.getClasses()) {
            result.define(new String[]{"load", aclass.getName()});
            result.define(new String[]{"remove", aclass.getName()});
        }
        return result;
    }
}
