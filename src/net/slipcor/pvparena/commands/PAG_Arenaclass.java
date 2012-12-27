package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>PVP Arena JOIN Command class</pre>
 * 
 * A command to join an arena
 * 
 * @author slipcor
 * 
 * @version v0.10.0
 */

public class PAG_Arenaclass extends PAA__Command {
	
	Debug db = new Debug(200);

	public PAG_Arenaclass() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena) || !arena.getArenaConfig().getBoolean(CFG.USES_INGAMECLASSSWITCH)) {
			return;
		}
		
		if (!argCountValid(sender, arena, args, new Integer[]{1})) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		ArenaClass ac = arena.getClass(args[0]);
		
		if (ac == null) {
			sender.sendMessage(Language.parse(MSG.ERROR_CLASS_NOT_FOUND, args[0]));
			return;
		}
		
		ap.setArenaClass(ac);
		ac.equip(ap.get());

		sender.sendMessage(Language.parse(MSG.CLASS_SELECTED, ac.getName()));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.ARENACLASS));
	}
}
