package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.SpawnManager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 * <pre>PVP Arena TELEPORT Command class</pre>
 * 
 * A command to teleport to an arena spawn
 * 
 * @author slipcor
 * 
 * @version v0.9.6
 */

public class PAA_Teleport extends PAA__Command {

	public PAA_Teleport() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{1})) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse(MSG.ERROR_ONLY_PLAYERS));
			return;
		}

		// usage: /pa {arenaname} teleport [spawnname] | tp to a spawn
		
		PALocation loc = SpawnManager.getCoords(arena, args[0]);
		
		if (loc == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_SPAWN_UNKNOWN, args[0]));
			return;
		}
		
		((Player) sender).teleport(loc.toLocation(), TeleportCause.PLUGIN);
		((Player) sender).setNoDamageTicks(arena.getArenaConfig().getInt(CFG.TIME_TELEPORTPROTECT) * 20);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.TELEPORT));
	}
}
