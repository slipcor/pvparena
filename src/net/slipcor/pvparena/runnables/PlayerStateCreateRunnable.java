package net.slipcor.pvparena.runnables;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAJoinEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerStateCreateRunnable implements Runnable {
	final ArenaPlayer a;
	final Player p;

	public PlayerStat eCreateRunnable(ArenaPlayer ap, Player player) {
		a = ap;
		p = player;
	}

	@Override
	public void run() {
		if (a.getState() == null) {
			
			Arena arena = a.getArena();

			PAJoinEvent event = new PAJoinEvent(arena, p, false);
			Bukkit.getPluginManager().callEvent(event);

			a.dump();
			a.createState(p);
			
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
