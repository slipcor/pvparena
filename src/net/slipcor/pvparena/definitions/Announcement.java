package net.slipcor.pvparena.definitions;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Players;

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
 * @version v0.6.41
 * 
 */
public class Announcement {
	private static Debug db = new Debug(7);

	public static enum type {
		JOIN, START, END, WINNER, LOSER, PRIZE;
	}

	/**
	 * Announce a message to the public
	 * 
	 * @param a
	 *            the arena from where the announcement should come
	 * @param t
	 *            the type of announcement
	 * @param message
	 *            the message to announce
	 */
	public static void announce(Arena a, type t, String message) {
		if (!sendCheck(a, t)) {
			return; // do not send the announcement type
		}
		db.i("announce [" + a.name + "] type: " + t.name() + " : " + message);
		Bukkit.getServer().getWorld(a.getWorld()).getPlayers();

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (Players.isPartOf(a, p)) {
				continue;
			}
			send(a, p,
					message.replace(ChatColor.WHITE.toString(), ChatColor
							.valueOf(a.cfg.getString("announcements.color"))
							.toString()));
		}
	}

	/**
	 * check the arena for the announcement tyoe
	 * 
	 * @param a
	 *            the arena to check
	 * @param t
	 *            the announcement type to check
	 * @return true if the arena is configured to send this announcement type,
	 *         false otherwise
	 */
	private static boolean sendCheck(Arena a, type t) {
		return a.cfg.getBoolean("announcements." + t.name().toLowerCase());
	}

	/**
	 * send an announcement to a player
	 * 
	 * @param a
	 *            the arena sending the announcement
	 * @param p
	 *            the player to send the message
	 * @param message
	 *            the message to send
	 */
	private static void send(Arena a, Player p, String message) {
		if (a.cfg.getInt("announcements.radius") > 0) {
			if (a.regions.get("battlefield") == null
					|| a.regions.get("battlefield").tooFarAway(
							a.cfg.getInt("announcements.radius"),
							p.getLocation())) {
				return; // too far away: out (checks world!)
			}
		}
		Arenas.tellPlayer(
				p,
				"§f[§a"
						+ a.name
						+ "§f] "
						+ ChatColor.valueOf(a.cfg
								.getString("announcements.color")) + message, a.prefix);
	}

}
