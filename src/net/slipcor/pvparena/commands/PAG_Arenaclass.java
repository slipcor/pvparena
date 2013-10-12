package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.InventoryManager;

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

public class PAG_Arenaclass extends AbstractArenaCommand {
	public PAG_Arenaclass() {
		super(new String[] {"pvparena.user"});
	}

	@Override
	public void commit(final Arena arena, final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender, arena) || !arena.getArenaConfig().getBoolean(CFG.USES_INGAMECLASSSWITCH)) {
			return;
		}
		
		if (!argCountValid(sender, arena, args, new Integer[]{1})) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
			return;
		}
		
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(sender.getName());
		
		final ArenaClass aClass = arena.getClass(args[0]);
		
		if (aClass == null) {
			sender.sendMessage(Language.parse(arena, MSG.ERROR_CLASS_NOT_FOUND, args[0]));
			return;
		}

		InventoryManager.clearInventory(aPlayer.get());
		aPlayer.setArenaClass(aClass);
		ArenaPlayer.givePlayerFightItems(arena, aPlayer.get());

		sender.sendMessage(Language.parse(arena, MSG.CLASS_SELECTED, aClass.getName()));
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.ARENACLASS));
	}
}
