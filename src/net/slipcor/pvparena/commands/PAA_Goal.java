package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaGoal;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * <pre>PVP Arena GOAL Command class</pre>
 * <p/>
 * A command to manage arena goals
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Goal extends AbstractArenaCommand {

    public PAA_Goal() {
        super(new String[]{"pvparena.cmds.goal"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{1, 2})) {
            return;
        }

        final ArenaGoal goal = PVPArena.instance.getAgm().getGoalByName(args[0].toLowerCase());

        if (goal == null) {
            arena.msg(sender, Language.parse(arena, MSG.ERROR_GOAL_NOTFOUND, args[0], StringParser.joinSet(PVPArena.instance.getAgm().getAllGoalNames(), " ")));
            arena.msg(sender, Language.parse(arena, MSG.GOAL_INSTALLING));
            return;
        }

        if (args.length < 2) {
            // toggle
            if (arena.goalToggle(goal)) {
                arena.msg(sender, Language.parse(arena, MSG.GOAL_ADDED, args[0]));
            } else {
                arena.msg(sender, Language.parse(arena, MSG.GOAL_REMOVED, args[0]));
            }
            return;
        }

        if (StringParser.positive.contains(args[1].toLowerCase())) {
            arena.goalAdd(goal);
            arena.msg(sender, Language.parse(arena, MSG.GOAL_ADDED, args[0]));
            return;
        }

        if (StringParser.negative.contains(args[1].toLowerCase())) {
            arena.goalRemove(goal);
            arena.msg(sender, Language.parse(arena, MSG.GOAL_REMOVED, args[0]));
            return;
        }

        // usage: /pa {arenaname} goal [goal] {value}

        arena.msg(sender, Language.parse(arena, MSG.ERROR_INVALID_VALUE, args[1]));
        arena.msg(sender, Language.parse(arena, MSG.ERROR_POSITIVES, StringParser.joinSet(StringParser.positive, " | ")));
        arena.msg(sender, Language.parse(arena, MSG.ERROR_NEGATIVES, StringParser.joinSet(StringParser.negative, " | ")));
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.GOAL));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("goal");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!g");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        if (arena == null) {
            return result;
        }
        for (final ArenaGoal goal : arena.getGoals()) {
            result.define(new String[]{goal.getName()});
        }
        return result;
    }
}
