package net.slipcor.pvparena.classes;

import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class PATimer implements Runnable {
	private static HashMap<Integer, String> messages = new HashMap<Integer, String>();
	static {
		String s = Language.parse("time.seconds");
		String m = Language.parse("time.minutes");
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

	private final String message;
	private final Integer seconds;
	private final Player player;
	private final Arena arena;
	private final Boolean global;
	private final Integer id;
	
	public PATimer(String msg, Integer seconds, Player p, Arena a, Boolean global) {
		this.message = msg;
		this.seconds = seconds;
		this.player = p;
		this.arena = a;
		this.global = global;
		
		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, this, 20L, 20L);
	}
	
	@Override
	public void run() {
		String msg = seconds > 5 ? Language.parse(message, messages.get(seconds)) : messages.get(seconds);
		if (global) {
			Player[] players = Bukkit.getOnlinePlayers();
			
			for (Player p : players) {
				try {
					if (arena != null) {
						if (arena.hasPlayer(p)) {
							continue;
						}
					}
					if (player != null) {
						if (player.getName().equals(p.getName())) {
							continue;
						}
					}
					Arena.pmsg(p, msg);
				} catch (Exception e) {}
			}
			
			return;
		}
		if (arena != null) {
			HashSet<ArenaPlayer> players = arena.getEveryone();
			for (ArenaPlayer ap : players) {
				if (player != null) {
					if (ap.getName().equals(player.getName())) {
						continue;
					}
				}
				if (ap.get() != null) {
					arena.msg(ap.get(), msg);
				}
			}
			return;
		}
		if (player != null) {
			Arena.pmsg(player, msg);
			return;
		}
		System.out.print("[PA-debug] " + msg);
	}
	
	public void stop() {
		Bukkit.getScheduler().cancelTask(id);
	}
}
