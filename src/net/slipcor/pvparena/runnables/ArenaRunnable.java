package net.slipcor.pvparena.runnables;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * <pre>Arena Runnable class</pre>
 * 
 * The interface for arena timers
 * 
 * @author slipcor
 * 
 * @version v0.10.0.1
 */

public abstract class ArenaRunnable extends BukkitRunnable {

	protected final static Map<Integer, String> MESSAGES = new HashMap<Integer, String>();
	static {
		final String seconds = Language.parse(MSG.TIME_SECONDS);
		final String minutes = Language.parse(MSG.TIME_MINUTES);
		MESSAGES.put(1, "1..");
		MESSAGES.put(2, "2..");
		MESSAGES.put(3, "3..");
		MESSAGES.put(4, "4..");
		MESSAGES.put(5, "5..");
		MESSAGES.put(10, "10 " + seconds);
		MESSAGES.put(20, "20 " + seconds);
		MESSAGES.put(30, "30 " + seconds);
		MESSAGES.put(60, "60 " + seconds);
		MESSAGES.put(120, "2 " + minutes);
		MESSAGES.put(180, "3 " + minutes);
		MESSAGES.put(240, "4 " + minutes);
		MESSAGES.put(300, "5 " + minutes);
		MESSAGES.put(600, "10 " + minutes);
		MESSAGES.put(1200, "20 " + minutes);
		MESSAGES.put(1800, "30 " + minutes);
		MESSAGES.put(2400, "40 " + minutes);
		MESSAGES.put(3000, "50 " + minutes);
		MESSAGES.put(3600, "60 " + minutes);
	}
	protected String message;
	protected Integer seconds;
	protected String sPlayer;
	protected Arena arena;
	protected Boolean global;
	
	/**
	 * Spam the message of the remaining time to... someone, probably:
	 * @param message the Language.parse("**") String to wrap
	 * @param arena the arena to spam to (!global) or to exclude (global)
	 * @param player the player to spam to (!global && !arena) or to exclude (global || arena)
	 * @param seconds the seconds remaining
	 * @param global the trigger to generally spam to everyone or to specific arenas/players
	 */
	public ArenaRunnable(final String message, final Integer seconds, final Player player, final Arena arena, final Boolean global) {
		super();
		this.message = message;
		this.seconds = seconds;
		this.sPlayer = player == null ? null : player.getName();
		this.arena = arena;
		this.global = global;
		
		runTaskTimer(PVPArena.instance, 20L, 20L);
	}
	
	public void spam() {
		if ((message == null) || (MESSAGES.get(seconds) == null)) {
			return;
		}
		final MSG msg = MSG.getByNode(this.message);
		if (msg == null) {
			PVPArena.instance.getLogger().warning("MSG not found: " + this.message);
			return;
		}
		final String message = seconds > 5 ? Language.parse(msg, MESSAGES.get(seconds)) : MESSAGES.get(seconds);
		if (global) {
			final Player[] players = Bukkit.getOnlinePlayers();
			
			for (Player p : players) {
				try {
					if (arena != null && arena.hasPlayer(p)) {
						continue;
					}
					if (p.getName().equals(sPlayer)) {
						continue;
					}
					Arena.pmsg(p, message);
				} catch (Exception e) {
				}
			}
			
			return;
		}
		if (arena != null) {
			final Set<ArenaPlayer> players = arena.getFighters();
			for (ArenaPlayer ap : players) {
				if (ap.getName().equals(sPlayer)) {
					continue;
				}
				if (ap.get() != null) {
					arena.msg(ap.get(), message);
				}
			}
			return;
		}
		if (Bukkit.getPlayer(sPlayer) != null) {
			Arena.pmsg(Bukkit.getPlayer(sPlayer), message);
			return;
		}
	}
	
	@Override
	public void run() {
		spam();
		if (seconds <= 0) {
			commit();
			try {
				cancel();
			} catch (IllegalStateException e) {
				warn();
			}
		}
		seconds--;
	}
	
	protected abstract void warn();
	protected abstract void commit();
}
