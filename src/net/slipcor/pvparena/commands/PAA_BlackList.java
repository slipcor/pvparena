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
 * <pre>PVP Arena BLACKLIST Command class</pre>
 * <p/>
 * A command to toggle block blacklist entries
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_BlackList extends AbstractArenaCommand {
    private static final Set<String> SUBCOMMANDS = new HashSet<String>();
    private static final Set<String> SUBTYPES = new HashSet<String>();

    static {
        SUBCOMMANDS.add("add");
        SUBCOMMANDS.add("remove");
        SUBCOMMANDS.add("show");
        SUBTYPES.add("break");
        SUBTYPES.add("place");
        SUBTYPES.add("use");
    }

    public PAA_BlackList() {
        super(new String[]{});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!this.hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{1, 2, 3})) {
            return;
        }

        //                                  args[0]
        // usage: /pa {arenaname} blacklist clear

        if (args.length < 2) {
            if (args[0].equalsIgnoreCase("clear")) {
                arena.getArenaConfig().set(CFG.LISTS_BLACKLIST, null);
                arena.getArenaConfig().save();
                arena.msg(sender, Language.parse(arena, MSG.BLACKLIST_ALLCLEARED));
                return;
            }
            arena.msg(sender, Language.parse(arena, MSG.BLACKLIST_HELP));
            return;
        } else if (args.length == 2) {
            // usage: /pa {arenaname} blacklist [type] clear
            if (!SUBTYPES.contains(args[0].toLowerCase())) {
                arena.msg(sender, Language.parse(arena, MSG.ERROR_BLACKLIST_UNKNOWN_TYPE, StringParser.joinSet(SUBTYPES, "|")));
                return;
            }
            if (args[1].equalsIgnoreCase("clear")) {
                arena.getArenaConfig().set(CFG.LISTS_BLACKLIST, null);
                arena.getArenaConfig().save();
                arena.msg(sender, Language.parse(arena, MSG.BLACKLIST_ALLCLEARED));
                return;
            }
            arena.msg(sender, Language.parse(arena, MSG.BLACKLIST_HELP));
            return;
        }

        if (!SUBTYPES.contains(args[0].toLowerCase())) {
            arena.msg(sender, Language.parse(arena, MSG.ERROR_BLACKLIST_UNKNOWN_TYPE, StringParser.joinSet(SUBTYPES, "|")));
            return;
        }

        if (!SUBCOMMANDS.contains(args[1].toLowerCase())) {
            arena.msg(sender, Language.parse(arena, MSG.ERROR_BLACKLIST_UNKNOWN_SUBCOMMAND, StringParser.joinSet(SUBCOMMANDS, "|")));
            return;
        }


        final List<String> list = arena.getArenaConfig().getStringList(
                CFG.LISTS_BLACKLIST.getNode() + "." + args[0].toLowerCase(), new ArrayList<String>());

        if (args[1].equalsIgnoreCase("add")) {
            list.add(args[2]);
            arena.msg(sender, Language.parse(arena, MSG.BLACKLIST_ADDED, args[2], args[0].toLowerCase()));
        } else if (args[1].equalsIgnoreCase("show")) {
            final StringBuffer output = new StringBuffer(Language.parse(arena, MSG.BLACKLIST_SHOW, args[0].toLowerCase()));
            for (String s : list) {
                output.append(": ");
                output.append(Material.getMaterial(Integer.parseInt(s)).name());
            }
            if (list.size() < 1) {
                output.append(": ---------");
            }
            arena.msg(sender, output.toString());
        } else {
            list.remove(args[2]);
            arena.msg(sender, Language.parse(arena, MSG.BLACKLIST_REMOVED, args[2], args[1]));
        }

        arena.getArenaConfig().setManually(CFG.LISTS_BLACKLIST.getNode() + "." + args[0].toLowerCase(), list);
        arena.getArenaConfig().save();

    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("blacklist");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!bl");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"clear"});
        for (String main : SUBTYPES) {
            result.define(new String[]{main, "clear"});
            for (String sub : SUBCOMMANDS) {
                result.define(new String[]{main, sub, "{Material}"});
            }
        }

        return result;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.BLACKLIST));
    }
}
