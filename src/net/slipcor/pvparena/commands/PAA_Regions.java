package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaRegion;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * <pre>PVP Arena REGIONS Command class</pre>
 * <p/>
 * A command to debug arena regions
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Regions extends AbstractArenaCommand {

    public PAA_Regions() {
        super(new String[]{"pvparena.cmds.regions"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        // /pa [] regions
        // /pa [] regions [regionname]

        if (!argCountValid(sender, arena, args, new Integer[]{0, 1})) {
            return;
        }

        if (args.length < 1) {
            arena.msg(sender, Language.parse(arena, MSG.REGIONS_LISTHEAD, arena.getName()));

            for (final ArenaRegion ars : arena.getRegions()) {
                arena.msg(sender, Language.parse(arena, MSG.REGIONS_LISTVALUE, ars.getRegionName(), ars.getType().name(), ars.getShape().getName()));
            }
            return;
        }

        final ArenaRegion region = arena.getRegion(args[0]);

        if (region == null) {
            arena.msg(sender, Language.parse(arena, MSG.ERROR_REGION_NOTFOUND, args[0]));
            return;
        }

        arena.msg(sender, Language.parse(arena, MSG.REGIONS_HEAD, arena.getName() + ':' + args[0]));
        arena.msg(sender, Language.parse(arena, MSG.REGIONS_TYPE, region.getType().name()));
        arena.msg(sender, Language.parse(arena, MSG.REGIONS_SHAPE, region.getShape().getName()));
        arena.msg(sender, Language.parse(arena, MSG.REGIONS_FLAGS, StringParser.joinSet(region.getFlags(), ", ")));
        arena.msg(sender, Language.parse(arena, MSG.REGIONS_PROTECTIONS, StringParser.joinSet(region.getProtections(), ", ")));
        arena.msg(sender, "0: " + region.locs[0]);
        arena.msg(sender, "1: " + region.locs[1]);
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.REGIONS));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("regions");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!rs");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        if (arena == null) {
            return result;
        }
        for (final ArenaRegion region : arena.getRegions()) {
            result.define(new String[]{region.getRegionName()});
        }
        return result;
    }
}
