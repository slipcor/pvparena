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
	private final ArenaPlayer aPlayer;
	private final Player player;

	public PlayerStateCreateRunnable(final ArenaPlayer aPlayer, final Player player) {
		this.aPlayer = aPlayer;
		this.player = player;
	}

	@Override
	public void run() {
		if (aPlayer.getState() == null) {
			
			final Arena arena = aPlayer.getArena();

			final PAJoinEvent event = new PAJoinEvent(arena, player, false);
			Bukkit.getPluginManager().callEvent(event);

			aPlayer.createState(player);
			ArenaPlayer.backupAndClearInventory(arena, player);
			aPlayer.dump();
			
			
			if (aPlayer.getArenaTeam() != null && aPlayer.getArenaClass() == null) {
				final String autoClass = arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);
				if (autoClass != null && !autoClass.equals("none") && arena.getClass(autoClass) != null) {
					arena.chooseClass(player, null, autoClass);
				}
				if (autoClass == null) {
					arena.msg(player, Language.parse(MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
					return;
				}
			}
		}
	}

}
