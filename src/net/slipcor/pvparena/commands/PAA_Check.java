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

public class PAA_Check extends PAA__Command {

	public PAA_Check() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
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
					String s = arena.getArenaConfig().getString(c);
					arena.msg(sender, "correct " + c.getType() + String.valueOf(s));
				} else if (c.getType().equals("boolean")) {
					boolean b = arena.getArenaConfig().getBoolean(c);
					arena.msg(sender, "correct " + c.getType() + String.valueOf(b));
				} else if (c.getType().equals("int")) {
					int i = arena.getArenaConfig().getInt(c);
					arena.msg(sender, "correct " + c.getType() + String.valueOf(i));
				} else if (c.getType().equals("double")) {
					double d = arena.getArenaConfig().getDouble(c);
					arena.msg(sender, "correct " + c.getType() + String.valueOf(d));
				}
			} catch (Exception e) {
				arena.msg(sender, Language.parse(MSG.ERROR_ERROR, c.getNode()));
				hasError = true;
			}
		}
		
		if (!hasError) {
			arena.msg(sender, Language.parse(MSG.CHECK_DONE));
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.CHECK));
	}
}
