package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>PVP Arena ENABLE Command class</pre>
 * 
 * A command to enable an arena
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Enable extends AbstractArenaCommand {
	
	public static Map<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Enable() {
		super(new String[] {});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!argCountValid(sender, arena, args, new Integer[]{0})) {
			return;
		}
		
		arena.getArenaConfig().set(CFG.GENERAL_ENABLED, true);
		arena.getArenaConfig().save();
		arena.setLocked(false);
		
		arena.msg(sender, Language.parse(arena, MSG.ARENA_ENABLE_DONE));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.ENABLE));
	}

    @Override
    public List<String> getMain() {
        return Arrays.asList("enable");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!en", "!on");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        return new CommandTree<String>(null);
    }
}
