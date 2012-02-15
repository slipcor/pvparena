package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Announcement;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.ArenaPlayer;
import net.slipcor.pvparena.definitions.Announcement.type;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * end manager class
 * 
 * -
 * 
 * provides commands to handle the arena end
 * 
 * @author slipcor
 * 
 * @version v0.6.2
 * 
 */

public class Ends {

	private static final Debug db = new Debug();

	/**
	 * [FLAG] commit the arena end
	 * 
	 * @param team
	 *            the team name
	 * @param win
	 *            winning team?
	 */
	public static void commit(Arena arena, String team, boolean win) {
		db.i("[FLAG] committing end: " + team);
		db.i("win: " + String.valueOf(win));
		Set<String> set = arena.pm.getPlayerTeamMap().keySet();
		Iterator<String> iter = set.iterator();
		if (!team.equals("$%&/")) {
			while (iter.hasNext()) {
				Object o = iter.next();
				db.i("precessing: " + o.toString());
				Player z = Bukkit.getServer().getPlayer(o.toString());
				if (!win
						&& arena.pm.getPlayerTeamMap().get(z.getName())
								.equals(team)) {
					arena.pm.parsePlayer(z).losses++;
					arena.resetPlayer(z, arena.cfg.getString("tp.lose", "old"));
				}
			}
		}

		String winteam = "";
		set = arena.pm.getPlayerTeamMap().keySet();
		iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			db.i("praecessing: " + o.toString());
			Player z = Bukkit.getServer().getPlayer(o.toString());
			if (arena.paLives.containsKey(arena.pm.getPlayerTeamMap().get(
					z.getName()))) {
				if (winteam.equals("")) {
					winteam = arena.pm.getPlayerTeamMap().get(z.getName());
					break;
				}
				/*
				 * pm.parsePlayer(z).wins++; resetPlayer(z,
				 * cfg.getString("tp.win", "old")); giveRewards(z); // if we are
				 * the winning team, give reward! winteam = team;
				 */
			}
		}
		if (arena.paTeams.get(winteam) != null) {
			Announcement.announce(arena, type.WINNER,
					PVPArena.lang.parse("teamhaswon", "Team " + winteam));
			arena.pm.tellEveryone(PVPArena.lang.parse("teamhaswon",
					ChatColor.valueOf(arena.paTeams.get(winteam)) + "Team "
							+ winteam));
		} else {
			System.out.print("WINTEAM NULL!");
		}

		arena.paLives.clear();
		Bukkit.getScheduler().scheduleSyncDelayedTask(
				Bukkit.getServer().getPluginManager().getPlugin("pvparena"),
				new EndRunnable(arena), 15 * 20L);
	}

	/**
	 * checks if the arena is over, if an end has to be committed
	 * 
	 * @return true if we ended the game just yet, false otherwise
	 */
	public static boolean checkAndCommit(Arena arena) {
		if (!arena.cfg.getBoolean("arenatype.teams")) {
			if (arena.pm.getPlayerTeamMap().size() > 1) {
				return false;
			}

			Set<String> set = arena.pm.getPlayerTeamMap().keySet();
			Iterator<String> iter = set.iterator();
			while (iter.hasNext()) {
				Object o = iter.next();

				Announcement.announce(
						arena,
						type.WINNER,
						PVPArena.lang.parse("playerhaswon",
								ChatColor.WHITE + o.toString()));
				arena.pm.tellEveryone(PVPArena.lang.parse("playerhaswon",
						ChatColor.WHITE + o.toString()));
			}
			Bukkit.getScheduler()
					.scheduleSyncDelayedTask(
							Bukkit.getServer().getPluginManager()
									.getPlugin("pvparena"),
							new EndRunnable(arena), 15 * 20L);
			return true;
		}
		if (arena.cfg.getBoolean("arenatype.flags")) {

			if (arena.pm.countPlayersInTeams() < 2) {
				String team = "$%&/";
				if (arena.pm.countPlayersInTeams() != 0)
					for (String t : arena.pm.getPlayerTeamMap().values()) {
						team = t;
						break;
					}
				commit(arena, team, true);
			}
			return false;

		}

		if (!arena.fightInProgress)
			return false;
		List<String> activeteams = new ArrayList<String>(0);
		String team = "";
		HashMap<String, String> test = arena.pm.getPlayerTeamMap();
		for (String sPlayer : test.keySet()) {
			if (activeteams.size() < 1) {
				// fresh map
				team = test.get(sPlayer);
				activeteams.add(team);
				db.i("team set to " + team);
			} else {
				// map contains stuff
				if (!activeteams.contains(test.get(sPlayer))) {
					// second team active => OUT!
					return false;
				}
			}
		}

		Announcement.announce(arena, type.WINNER,
				PVPArena.lang.parse("teamhaswon", "Team " + team));
		arena.pm.tellEveryone(PVPArena.lang.parse("teamhaswon",
				ChatColor.valueOf(arena.paTeams.get(team)) + "Team " + team));

		Set<String> set = arena.pm.getPlayerTeamMap().keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			String sPlayer = iter.next();

			Player z = Bukkit.getServer().getPlayer(sPlayer);
			if (!arena.pm.getPlayerTeamMap().get(z.getName()).equals(team)) {
				arena.pm.parsePlayer(z).losses++;
				arena.resetPlayer(z, arena.cfg.getString("tp.lose", "old"));
			}
		}

		if (PVPArena.eco != null) {
			for (String nKey : arena.pm.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (arena.paTeams.get(nSplit[1]) == null
						|| arena.paTeams.get(nSplit[1]).equals("free"))
					continue;

				if (nSplit[1].equalsIgnoreCase(team)) {
					double amount = arena.pm.paPlayersBetAmount.get(nKey) * 2;

					MethodAccount ma = PVPArena.eco.getAccount(nSplit[0]);
					if (ma == null) {
						db.s("Account not found: " + nSplit[0]);
						return true;
					}
					ma.add(amount);
					try {
						Arenas.tellPlayer(
								Bukkit.getPlayer(nSplit[0]),
								PVPArena.lang.parse("youwon",
										PVPArena.eco.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(
				Bukkit.getServer().getPluginManager().getPlugin("pvparena"),
				new EndRunnable(arena), 15 * 20L);
		return true;
	}

	/**
	 * end the arena due to timing
	 */
	public static void timed(Arena arena) {
		int iKills;
		int iDeaths;

		int max = -1;
		HashSet<String> result = new HashSet<String>();

		for (String sTeam : arena.paTeams.keySet()) {
			iKills = 0;
			iDeaths = 0;

			try {
				iKills = arena.pm.getKills(sTeam);
			} catch (Exception e) {
			}

			try {
				iDeaths = arena.pm.getDeaths(sTeam);
			} catch (Exception e) {
			}

			if ((iKills - iDeaths) > max) {
				result = new HashSet<String>();
				result.add(sTeam);
			} else if ((iKills - iDeaths) == max) {
				result.add(sTeam);
			}
		}

		for (String team : result) {
			if (result.contains(team)) {
				Announcement.announce(arena, type.WINNER,
						PVPArena.lang.parse("teamhaswon", "Team " + team));
				arena.pm.tellEveryone(PVPArena.lang.parse("teamhaswon",
						ChatColor.valueOf(arena.paTeams.get(team)) + "Team "
								+ team));
			}

		}

		for (ArenaPlayer p : arena.pm.getPlayers()) {

			Player z = p.get();
			if (!result.contains(p.team)) {
				arena.pm.parsePlayer(z).losses++;
				arena.resetPlayer(z, arena.cfg.getString("tp.lose", "old"));
			}
			p = null;
		}

		if (PVPArena.eco != null) {
			for (String nKey : arena.pm.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (arena.paTeams.get(nSplit[1]) == null
						|| arena.paTeams.get(nSplit[1]).equals("free"))
					continue;

				if (result.contains(nSplit[1])) {
					double amount = arena.pm.paPlayersBetAmount.get(nKey) * 2;

					MethodAccount ma = PVPArena.eco.getAccount(nSplit[0]);
					if (ma == null) {
						db.s("Account not found: " + nSplit[0]);
						continue;
					}
					ma.add(amount);
					try {
						Arenas.tellPlayer(
								Bukkit.getPlayer(nSplit[0]),
								PVPArena.lang.parse("youwon",
										PVPArena.eco.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(
				Bukkit.getServer().getPluginManager().getPlugin("pvparena"),
				new EndRunnable(arena), 15 * 20L);
	}
}
