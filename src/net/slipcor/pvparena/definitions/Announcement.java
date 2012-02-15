package net.slipcor.pvparena.definitions;

import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * announcement class
 * 
 * -
 * 
 * provides methods to announce texts publicly
 * 
 * @author slipcor
 * 
 * @version v0.6.2
 * 
 */
public class Announcement {
	public static enum type {
		JOIN, START, END, WINNER, LOSER, PRIZE;
	}

	public static void announce(Arena a, type t, String message) {
		if (!sendCheck(a, t)) {
			return; // do not send the announcement type
		}
		Bukkit.getServer().getWorld(a.getWorld()).getPlayers();

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (a.pm.existsPlayer(p)) {
				continue;
			}
			send(a, p,
					message.replace(ChatColor.WHITE.toString(), ChatColor
							.valueOf(a.cfg.getString("announcements.color"))
							.toString()));
		}
	}

	private static boolean sendCheck(Arena a, type t) {
		return a.cfg.getBoolean("announcements." + t.name().toLowerCase());
	}

	private static void send(Arena a, Player p, String message) {
		if (a.cfg.getInt("announcements.radius") > 0) {
			if (a.regions.get("battlefield").tooFarAway(
					a.cfg.getInt("announcements.radius"), p.getLocation())) {
				return; // too far away: out (checks world!)
			}
		}
		Arenas.tellPlayer(p,
				ChatColor.valueOf(a.cfg.getString("announcements.color"))
						+ message);
	}

}
