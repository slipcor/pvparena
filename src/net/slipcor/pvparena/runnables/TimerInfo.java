package net.slipcor.pvparena.runnables;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

public class TimerInfo {
	protected static HashMap<Integer, String> messages = new HashMap<Integer, String>();;
	static {
		String s = Language.parse("seconds");
		String m = Language.parse("minutes");
		messages.put(1, "1..");
		messages.put(2, "2..");
		messages.put(3, "3..");
		messages.put(4, "4..");
		messages.put(5, "5..");
		messages.put(10, "10 " + s);
		messages.put(20, "20 " + s);
		messages.put(30, "30 " + s);
		messages.put(60, "60 " + s);
		messages.put(120, "2 " + m);
		messages.put(180, "3 " + m);
		messages.put(240, "4 " + m);
		messages.put(300, "5 " + m);
		messages.put(600, "10 " + m);
		messages.put(1200, "20 " + m);
		messages.put(1800, "30 " + m);
		messages.put(2400, "40 " + m);
		messages.put(3000, "50 " + m);
		messages.put(3600, "60 " + m);
	}
	
	/**
	 * Spam the message of the remaining time to... someone, probably:
	 * @param s the Language.parse("**") String to wrap
	 * @param arena the arena to spam to (!global) or to exclude (global)
	 * @param player the player to spam to (!global && !arena) or to exclude (global || arena)
	 * @param i the seconds remaining
	 * @param global the trigger to generally spam to everyone or to specific arenas/players
	 */
	public static void spam(String s, Integer i, Player player, Arena arena, Boolean global) {
		if (messages.get(i) == null) {
			return;
		}
		
		String message = i > 5 ? Language.parse(s, messages.get(i)) : messages.get(i);
		if (global) {
			Player[] players = Bukkit.getOnlinePlayers();
			
			for (Player p : players) {
				try {
					if (arena != null) {
						if (arena.isPartOf(p)) {
							continue;
						}
					}
					if (player != null) {
						if (player.getName().equals(p.getName())) {
							continue;
						}
					}
					Arenas.tellPlayer(p, message);
				} catch (Exception e) {}
			}
			
			return;
		}
		if (arena != null) {
			HashSet<ArenaPlayer> players = arena.getPlayers();
			for (ArenaPlayer ap : players) {
				if (player != null) {
					if (ap.getName().equals(player.getName())) {
						continue;
					}
				}
				if (ap.get() != null) {
					Arenas.tellPlayer(ap.get(), message, arena);
				}
			}
			return;
		}
		if (player != null) {
			Arenas.tellPlayer(player, message);
			return;
		}
		System.out.print("[PA-debug] " + message);
	}
}
