package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionProtection;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * <pre>PVP Arena PROTECTION Command class</pre>
 * <p/>
 * A command to manage arena region protections
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Protection extends AbstractArenaCommand {

    public PAA_Protection() {
        super(new String[]{"pvparena.cmds.protection"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{2, 3})) {
            return;
        }

        final ArenaRegion region = arena.getRegion(args[0]);

        if (region == null) {
            arena.msg(sender, Language.parse(arena, MSG.ERROR_REGION_NOTFOUND, args[0]));
            return;
        }

        RegionProtection regionProtection = null;

        try {
            regionProtection = RegionProtection.valueOf(args[1].toUpperCase());
        } catch (final Exception e) {
            if (!"all".equalsIgnoreCase(args[1])) {
                arena.msg(sender, Language.parse(arena, MSG.ERROR_REGION_FLAG_NOTFOUND, args[1], StringParser.joinArray(RegionProtection.values(), " ")));
                return;
            }
        }

        if (args.length < 3) {
            // toggle
            if (region.protectionToggle(regionProtection)) {
                arena.msg(sender, Language.parse(arena, MSG.REGION_FLAG_ADDED, args[1]));
            } else {
                arena.msg(sender, Language.parse(arena, MSG.REGION_FLAG_REMOVED, args[1]));
            }
            region.saveToConfig();
            return;
        }

        if (StringParser.positive.contains(args[2].toLowerCase())) {
            region.protectionAdd(regionProtection);
            arena.msg(sender, Language.parse(arena, MSG.REGION_FLAG_ADDED, args[1]));
            region.saveToConfig();
            return;
        }

        if (StringParser.negative.contains(args[2].toLowerCase())) {
            region.protectionRemove(regionProtection);
            arena.msg(sender, Language.parse(arena, MSG.REGION_FLAG_REMOVED, args[1]));
            region.saveToConfig();
            return;
        }

        // usage: /pa {arenaname} protection [regionname] [regionflag] {value}

        arena.msg(sender, Language.parse(arena, MSG.ERROR_INVALID_VALUE, args[2]));
        arena.msg(sender, Language.parse(arena, MSG.ERROR_POSITIVES, StringParser.joinSet(StringParser.positive, " | ")));
        arena.msg(sender, Language.parse(arena, MSG.ERROR_NEGATIVES, StringParser.joinSet(StringParser.negative, " | ")));
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.PROTECTION));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("protection");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!p");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        if (arena == null) {
            return result;
        }
        for (final ArenaRegion region : arena.getRegions()) {
            result.define(new String[]{region.getRegionName(), "{RegionProtection}", "{Boolean}"});
        }
        return result;
    }
}
