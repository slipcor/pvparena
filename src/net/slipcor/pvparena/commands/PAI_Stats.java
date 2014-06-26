package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * <pre>PVP Arena STATS Command class</pre>
 * <p/>
 * A command to display the player statistics
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAI_Stats extends AbstractArenaCommand {

    public PAI_Stats() {
        super(new String[]{"pvparena.user"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!this.hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{1, 2})) {
            return;
        }

        final StatisticsManager.type statType = StatisticsManager.type.getByString(args[0]);

        if (statType == null) {
            Arena.pmsg(sender, Language.parse(arena, MSG.STATS_TYPENOTFOUND, StringParser.joinArray(type.values(), ", ")));
            return;
        }

        final String[] values = StatisticsManager.read(StatisticsManager.getStats(arena, statType), statType, arena == null);
        final String[] names = StatisticsManager.read(StatisticsManager.getStats(arena, statType), type.NULL, arena == null);

        int max = 10;

        if (args.length > 1) {
            try {
                max = Integer.parseInt(args[1]);
            } catch (Exception e) {
                max = 10;
            }
        }

        final String s2 = Language.parse(arena, MSG.getByName("STATTYPE_" + statType.name()));

        final String s1 = Language.parse(arena, MSG.STATS_HEAD, String.valueOf(max), s2);


        Arena.pmsg(sender, s1);

        for (int i = 0; i < max && i < names.length && i < values.length; i++) {
            Arena.pmsg(sender, names[i] + ": " + values[i]);
        }
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.STATS));
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("stats");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("-s");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        for (StatisticsManager.type val : StatisticsManager.type.values()) {
            result.define(new String[]{val.name()});
        }
        return result;
    }
}
