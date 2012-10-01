package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAJoinEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * <pre>Arena Runnable class "PlayerReset"</pre>
 * 
 * An arena timer to reset a player
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class PlayerStateCreateRunnable implements Runnable {
	final ArenaPlayer a;
	final Player p;

	public PlayerStateCreateRunnable(ArenaPlayer ap, Player player) {
		a = ap;
		p = player;
	}

	@Override
	public void run() {
		if (a.getState() == null) {
			
			Arena arena = a.getArena();

			PAJoinEvent event = new PAJoinEvent(arena, p, false);
			Bukkit.getPluginManager().callEvent(event);

			a.createState(p);
			a.dump();
			
			ArenaPlayer.prepareInventory(arena, p);
			
			if (a.getArenaClass() == null) {
				String autoClass = arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);
				if (autoClass != null && !autoClass.equals("none")) {
					if (arena.getClass(autoClass) != null) {
						arena.chooseClass(p, null, autoClass);
					}
				}
				if (autoClass == null) {
					arena.msg(p, Language.parse(MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
					return;
				}
			}
		}
	}

}
