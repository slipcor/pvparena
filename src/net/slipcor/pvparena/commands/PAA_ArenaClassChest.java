package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;


public class PAA_ArenaClassChest extends AbstractArenaCommand {
    public PAA_ArenaClassChest() {
        super(new String[]{"pvparena.cmds.arenaclasschest"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{1})) {
            return;
        }

        if (!(sender instanceof Player)) {
            Arena.pmsg(sender, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
            return;
        }
        final ArenaClass aClass = arena.getClass(args[0]);

        if (aClass == null) {
            sender.sendMessage(Language.parse(arena, MSG.ERROR_CLASS_NOT_FOUND, args[0]));
            return;
        }

        Player player = (Player) sender;

        Block b = player.getTargetBlock((Set<Material>)null, 10);
        if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST) {
            arena.msg(sender,
                    Language.parse(arena, MSG.ERROR_NO_CHEST));
            return;
        }
        PABlockLocation loc = new PABlockLocation(b.getLocation());

        arena.getArenaConfig().setManually("classchests."+aClass.getName(), loc.toString());
        arena.getArenaConfig().save();

        sender.sendMessage(Language.parse(arena, MSG.CLASSCHEST, aClass.getName(), loc.toString()));
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.CLASSCHEST));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("classchest");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!cc");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        if (arena == null) {
            return result;
        }
        for (final ArenaClass aClass : arena.getClasses()) {
            result.define(new String[]{aClass.getName()});
        }
        return result;
    }
}
