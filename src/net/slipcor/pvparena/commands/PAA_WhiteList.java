package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * <pre>PVP Arena WHITELIST Command class</pre>
 * <p/>
 * A command to toggle block whitelist entries
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_WhiteList extends AbstractArenaCommand {
    private static final Set<String> SUBCOMMANDS = new HashSet<>();
    private static final Set<String> SUBTYPES = new HashSet<>();

    static {
        SUBCOMMANDS.add("add");
        SUBCOMMANDS.add("remove");
        SUBCOMMANDS.add("show");
        SUBTYPES.add("break");
        SUBTYPES.add("place");
        SUBTYPES.add("use");
    }

    public PAA_WhiteList() {
        super(new String[]{"pvparena.cmds.whitelist"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{1, 2, 3})) {
            return;
        }

        //                                  args[0]
        // usage: /pa {arenaname} blacklist clear

        if (args.length < 2) {
            if ("clear".equalsIgnoreCase(args[0])) {
                arena.getArenaConfig().setManually(CFG.LISTS_WHITELIST.getNode(), null);
                arena.getArenaConfig().save();
                arena.msg(sender, Language.parse(arena, MSG.WHITELIST_ALLCLEARED));
                return;
            }
            arena.msg(sender, Language.parse(arena, MSG.WHITELIST_HELP));
            return;
        }
        if (args.length == 2) {
            // usage: /pa {arenaname} blacklist [type] clear
            if (!SUBTYPES.contains(args[0].toLowerCase())) {
                arena.msg(sender, Language.parse(arena, MSG.ERROR_WHITELIST_UNKNOWN_TYPE, StringParser.joinSet(SUBTYPES, "|")));
                return;
            }
            if (args[1].equalsIgnoreCase("clear")) {
                arena.getArenaConfig().setManually(CFG.LISTS_WHITELIST.getNode(), null);
                arena.getArenaConfig().save();
                arena.msg(sender, Language.parse(arena, MSG.WHITELIST_ALLCLEARED));
                return;
            }
            arena.msg(sender, Language.parse(arena, MSG.WHITELIST_HELP));
            return;
        }

        if (!SUBTYPES.contains(args[0].toLowerCase())) {
            arena.msg(sender, Language.parse(arena, MSG.ERROR_WHITELIST_UNKNOWN_TYPE, StringParser.joinSet(SUBTYPES, "|")));
            return;
        }

        if (!SUBCOMMANDS.contains(args[1].toLowerCase())) {
            arena.msg(sender, Language.parse(arena, MSG.ERROR_WHITELIST_UNKNOWN_SUBCOMMAND, StringParser.joinSet(SUBCOMMANDS, "|")));
            return;
        }


        List<String> list = new ArrayList<>();

        list = arena.getArenaConfig().getStringList(CFG.LISTS_WHITELIST.getNode() + '.' + args[0].toLowerCase(), list);

        if ("add".equalsIgnoreCase(args[1])) {
            list.add(args[2]);
            arena.msg(sender, Language.parse(arena, MSG.WHITELIST_ADDED, args[2], args[0].toLowerCase()));
        } else if ("show".equalsIgnoreCase(args[1])) {
            final StringBuilder output = new StringBuilder(Language.parse(arena, MSG.WHITELIST_SHOW, args[0].toLowerCase()));
            for (final String s : list) {
                output.append(": ");
                output.append(Material.getMaterial(s).name());
            }
            if (list.size() < 1) {
                output.append(": ---------");
            }
            arena.msg(sender, output.toString());
        } else {
            list.remove(args[2]);
            arena.msg(sender, Language.parse(arena, MSG.WHITELIST_REMOVED, args[2], args[1]));
        }

        arena.getArenaConfig().setManually(CFG.LISTS_WHITELIST.getNode() + '.' + args[0].toLowerCase(), list);
        arena.getArenaConfig().save();

    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.WHITELIST));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("whitelist");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!wl");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"clear"});
        for (final String main : SUBTYPES) {
            result.define(new String[]{main, "clear"});
            for (final String sub : SUBCOMMANDS) {
                result.define(new String[]{main, sub, "{Material}"});
            }
        }
        return result;
    }
}
