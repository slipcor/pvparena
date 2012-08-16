package net.slipcor.pvparena.runnables;

import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class ArenaRunnable implements Runnable {

	protected static HashMap<Integer, String> messages = new HashMap<Integer, String>();
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
	String message;
	Integer count;
	String player;
	Arena arena;
	Boolean global;
	
	int id = -1;
	
	/**
	 * Spam the message of the remaining time to... someone, probably:
	 * @param s the Language.parse("**") String to wrap
	 * @param arena the arena to spam to (!global) or to exclude (global)
	 * @param player the player to spam to (!global && !arena) or to exclude (global || arena)
	 * @param i the seconds remaining
	 * @param global the trigger to generally spam to everyone or to specific arenas/players
	 */
	public ArenaRunnable(String s, Integer i, Player player, Arena arena, Boolean global) {
		this.message = s;
		this.count = i;
		this.player = player.getName();
		this.arena = arena;
		this.global = global;
		
		id = Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this);
	}
	public void spam() {
		if ((message == null) || (messages.get(count) == null)) {
			return;
		}
		String message = count > 5 ? Language.parse(this.message, messages.get(count)) : messages.get(count);
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
						if (player.equals(p.getName())) {
							continue;
						}
					}
					Arenas.tellPlayer(p, message);
				} catch (Exception e) {}
			}
			
			return;
		}
		if (arena != null) {
			HashSet<ArenaPlayer> players = arena.getFighters();
			for (ArenaPlayer ap : players) {
				if (player != null) {
					if (ap.getName().equals(player)) {
						continue;
					}
				}
				if (ap.get() != null) {
					arena.msg(ap.get(), message);
				}
			}
			return;
		}
		if (Bukkit.getPlayer(player) != null) {
			Arenas.tellPlayer(Bukkit.getPlayer(player), message);
			return;
		}
		System.out.print("[PA-debug] " + message);
	}
	
	@Override
	public void run() {
		spam();
		if (count <= 0) {
			commit();
		} else {
			id = Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this, 20L);
		}
	}
	
	protected abstract void commit();

}
