package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.managers.SpawnManager;

/**
 * <pre>
 * Arena Runnable class "Teleport"
 * </pre>
 * 
 * An arena timer to teleport a player back
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class TeleportRunnable implements Runnable {
	private final String spawnName;
	private final String player;
	private final Arena arena;
	private final boolean soft;

	private final static Debug DEBUG = new Debug(77);

	public TeleportRunnable(final Arena arena, final ArenaPlayer player, final String spawn, final boolean soft) {
		DEBUG.i("TeleportRunnable: " + arena.getName() + " | " + player.getName() + " => "
				+ spawn, player.getName());
		this.spawnName = spawn;
		this.player = player.getName();
		this.arena = arena;
		this.soft = soft;
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this,
				10L);
	}

	@Override
	public void run() {
		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player);
		if (aPlayer == null || aPlayer.get() == null) {
			DEBUG.i("ArenaPlayer NULL: " + player, player);
			return;
		}
		if (spawnName.equalsIgnoreCase("old")) {
			DEBUG.i("tping to old", player);
			if (aPlayer.getLocation() != null) {
				DEBUG.i("location is fine", player);
				final PALocation loc = aPlayer.getLocation();
				aPlayer.get().teleport(loc.toLocation());
				aPlayer.get()
						.setNoDamageTicks(
								arena.getArenaConfig().getInt(
										CFG.TIME_TELEPORTPROTECT) * 20);
			}
		} else {
			final PALocation loc = SpawnManager.getCoords(arena, spawnName);
			if (loc == null) {
				PVPArena.instance.getLogger().warning("Spawn null: " + spawnName);
			} else {
				aPlayer.get().teleport(loc.toLocation());
			}
			aPlayer.get()
					.setNoDamageTicks(
							arena.getArenaConfig().getInt(
									CFG.TIME_TELEPORTPROTECT) * 20);
		}
		if (!soft) {
			aPlayer.setLocation(null);
		}
	}

}
