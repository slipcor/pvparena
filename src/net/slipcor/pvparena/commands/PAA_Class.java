package net.slipcor.pvparena.commands;

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

        final Player player = (Player) sender;
        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sender.getName());
        String classname;

        if (args.length == 1) {
            // when no 2nd arg, save/remove/load class with name of player's current class
            if (aPlayer.getArenaClass() == null) {
                Arena.pmsg(player, Language.parse(arena, MSG.ERROR_CLASS_NOT_GIVEN));
                return;
            }
            classname = aPlayer.getArenaClass().getName();
        } else {
            classname = args[1];
        }

        if ("load".equalsIgnoreCase(args[0])) {
            if(aPlayer.getArenaClass() == null) {
                ArenaPlayer.backupAndClearInventory(arena, player);
            } else {
                InventoryManager.clearInventory(player);
            }
            arena.selectClass(aPlayer, classname);
        } else if ("save".equalsIgnoreCase(args[0])) {
            ItemStack[] storage = player.getInventory().getStorageContents();
            ItemStack offhand = player.getInventory().getItemInOffHand();
            ItemStack[] armor = player.getInventory().getArmorContents();
    
            arena.getArenaConfig().setManually("classitems." + classname + ".items", getSerializableItemStacks(storage));
            arena.getArenaConfig().setManually("classitems." + classname + ".offhand", getSerializableItemStacks(offhand));
            arena.getArenaConfig().setManually("classitems." + classname + ".armor", getSerializableItemStacks(armor));
            arena.getArenaConfig().save();
    
            arena.addClass(classname, storage, offhand, armor);
            Arena.pmsg(player, Language.parse(arena, MSG.CLASS_SAVED, classname));
        } else if ("remove".equalsIgnoreCase(args[0])) {
            arena.getArenaConfig().setManually("classitems." + classname, null);
            arena.getArenaConfig().save();
            arena.removeClass(classname);
            Arena.pmsg(player, Language.parse(arena, MSG.CLASS_REMOVED, classname));
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
