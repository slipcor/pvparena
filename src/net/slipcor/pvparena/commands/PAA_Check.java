package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;

import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena CHECK Command class</pre>
 * 
 * A command to check an arena config
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAA_Check extends AbstractArenaCommand {

	public PAA_Check() {
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
		
		boolean hasError = false;
		
		for (CFG c : CFG.values()) {
			if (c == null || c.getNode() == null) {
				continue;
			}
			try {
				if (c.getType().equals("string")) {
					final String value = arena.getArenaConfig().getString(c);
					arena.msg(sender, "correct " + c.getType() + ": " + value);
				} else if (c.getType().equals("boolean")) {
					final boolean value = arena.getArenaConfig().getBoolean(c);
					arena.msg(sender, "correct " + c.getType() + ": " + value);
				} else if (c.getType().equals("int")) {
					final int value = arena.getArenaConfig().getInt(c);
					arena.msg(sender, "correct " + c.getType() + ": " + value);
				} else if (c.getType().equals("double")) {
					final double value = arena.getArenaConfig().getDouble(c);
					arena.msg(sender, "correct " + c.getType() + ": " + value);
				}
			} catch (Exception e) {
				arena.msg(sender, Language.parse(arena, MSG.ERROR_ERROR, c.getNode()));
				hasError = true;
			}
		}
		
		if (!hasError) {
			arena.msg(sender, Language.parse(arena, MSG.CHECK_DONE));
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.CHECK));
	}
}
