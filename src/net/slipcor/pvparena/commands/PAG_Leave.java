package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena LEAVE Command class</pre>
 * 
 * A command to leave an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.4
 */

public class PAG_Leave extends PAA__Command {

	public PAG_Leave() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0,1})) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}

		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (!arena.hasPlayer(ap.get())) {

			arena.msg(sender, Language.parse(MSG.ERROR_NOT_IN_ARENA));
			return;
		}

		for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
			if (mod.isActive(arena)) {
				mod.parseLeave(arena, ap.get());
			}
		}
		
		for (ArenaGoal goal : arena.getGoals()) {
			goal.parseLeave(ap.get());
		}
		
		arena.playerLeave(ap.get(), CFG.TP_EXIT, false);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.LEAVE));
	}
}
