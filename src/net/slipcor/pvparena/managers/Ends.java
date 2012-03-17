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
import net.slipcor.pvparena.core.Language;
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
 * @version v0.6.35
 * 
 */

public class Ends {

	private static final Debug db = new Debug(28);

	/**
	 * [FLAG] commit the arena end
	 * 
	 * @param team
	 *            the team name
	 * @param win
	 *            winning team?
	 */
	public static void commit(Arena arena, String team, boolean win) {
		if (arena.cfg.getBoolean("arenatype.deathmatch")
				|| arena.cfg.getBoolean("arenatype.domination")) {
			win = !win;
		}
		db.i("[FLAG/DM/DOM] committing end: " + team);
		db.i("win: " + String.valueOf(win));
		Set<String> set = Players.getPlayerTeamMap(arena).keySet();
		Iterator<String> iter = set.iterator();
		if (!team.equals("$%&/")) {
			while (iter.hasNext()) {
				Object o = iter.next();
				db.i("precessing: " + o.toString());
				Player z = Bukkit.getServer().getPlayer(o.toString());
				if (!win
						&& Players.getPlayerTeamMap(arena).get(z.getName())
								.equals(team)) {
					// team not winning and player team = team
					Players.parsePlayer(z).losses++;
					arena.removePlayer(z, "spectator");
					Players.parsePlayer(z).destroy();
				} else if (win
						&& !Players.getPlayerTeamMap(arena).get(z.getName())
								.equals(team)) {
					// team winning and other team
					Players.parsePlayer(z).losses++;
					arena.removePlayer(z, "spectator");
					Players.parsePlayer(z).destroy();
				}
			}
		}

		String winteam = win ? team : "";
		set = Players.getPlayerTeamMap(arena).keySet();
		iter = set.iterator();
		while (winteam.equals("") && iter.hasNext()) {
			Object o = iter.next();
			db.i("praecessing: " + o.toString());
			Player z = Bukkit.getServer().getPlayer(o.toString());
			if (arena.paLives.containsKey(Players.getPlayerTeamMap(arena).get(
					z.getName()))) {
				winteam = Players.getPlayerTeamMap(arena).get(z.getName());
			}
		}
		if (arena.paTeams.get(winteam) != null) {
			Announcement.announce(arena, type.WINNER,
					Language.parse("teamhaswon", "Team " + winteam));
			Players.tellEveryone(
					arena,
					Language.parse("teamhaswon",
							ChatColor.valueOf(arena.paTeams.get(winteam))
									+ "Team " + winteam));
		} else {
			System.out.print("[PVP Arena] WINTEAM NULL: "+winteam);
		}

		arena.paLives.clear();
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
				new EndRunnable(arena), 15 * 20L);
	}

	/**
	 * checks if the arena is over, if an end has to be committed
	 * 
	 * @return true if we ended the game just yet, false otherwise
	 */
	public static boolean checkAndCommit(Arena arena) {
		db.i("checking for arena end");
		if (!arena.fightInProgress) {
			db.i("no fight, no end ^^");
			return false;
		}

		if (!arena.cfg.getBoolean("arenatype.teams")) {
			db.i("[FREE]");
			if (Players.getPlayerTeamMap(arena).size() > 1) {
				db.i("more than one team active => no end :p");
				return false;
			}

			Set<String> set = Players.getPlayerTeamMap(arena).keySet();
			Iterator<String> iter = set.iterator();
			while (iter.hasNext()) {
				Object o = iter.next();

				Announcement.announce(
						arena,
						type.WINNER,
						Language.parse("playerhaswon",
								ChatColor.WHITE + o.toString()));
				Players.tellEveryone(
						arena,
						Language.parse("playerhaswon",
								ChatColor.WHITE + o.toString()));
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
					new EndRunnable(arena), 15 * 20L);
			return true;
		}
		if (arena.cfg.getBoolean("arenatype.flags")) {
			db.i("[FLAG]");

			if (Players.countPlayersInTeams(arena) < 2) {
				String team = "$%&/";
				if (Players.countPlayersInTeams(arena) != 0)
					for (String t : Players.getPlayerTeamMap(arena).values()) {
						team = t;
						break;
					}
				commit(arena, team, true);
			}
			return false;

		}
		db.i("[TEAMS]");

		List<String> activeteams = new ArrayList<String>(0);
		String team = "";
		HashMap<String, String> test = Players.getPlayerTeamMap(arena);
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
		if (arena.paTeams.get(team) != null) {
			Announcement.announce(arena, type.WINNER,
					Language.parse("teamhaswon", "Team " + team));
			Players.tellEveryone(
					arena,
					Language.parse("teamhaswon",
							ChatColor.valueOf(arena.paTeams.get(team))
									+ "Team " + team));
		} else {
			Bukkit.getLogger().severe("[PVP Arena] team unknown: " + team);
		}

		Set<String> set = Players.getPlayerTeamMap(arena).keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			String sPlayer = iter.next();

			Player z = Bukkit.getServer().getPlayer(sPlayer);
			if (!Players.getPlayerTeamMap(arena).get(z.getName()).equals(team)) {
				Players.parsePlayer(z).losses++;
				arena.resetPlayer(z, arena.cfg.getString("tp.lose", "old"));
			}
		}

		if (PVPArena.eco != null || PVPArena.economy != null) {
			db.i("eConomy set, parse bets");
			for (String nKey : Players.paPlayersBetAmount.keySet()) {
				db.i("bet: " + nKey);
				String[] nSplit = nKey.split(":");

				if (arena.paTeams.get(nSplit[1]) == null
						|| arena.paTeams.get(nSplit[1]).equals("free"))
					continue;

				if (nSplit[1].equalsIgnoreCase(team)) {
					double teamFactor = arena.cfg
							.getDouble("money.betTeamWinFactor")
							* arena.teamCount;
					if (teamFactor <= 0) {
						teamFactor = 1;
					}
					teamFactor *= arena.cfg.getDouble("money.betWinFactor");

					double amount = Players.paPlayersBetAmount.get(nKey)
							* teamFactor;

					if (PVPArena.economy == null && PVPArena.eco != null) {
						MethodAccount ma = PVPArena.eco.getAccount(nSplit[0]);
						if (ma == null) {
							db.s("Account not found: " + nSplit[0]);
							return true;
						}
						ma.add(amount);
						try {
							Arenas.tellPlayer(
									Bukkit.getPlayer(nSplit[0]),
									Language.parse("youwon",
											PVPArena.eco.format(amount)));
						} catch (Exception e) {
							// nothing
						}
					} else {
						if (!PVPArena.economy.hasAccount(nSplit[0])) {
							db.s("Account not found: " + nSplit[0]);
							return true;
						}
						PVPArena.economy.depositPlayer(nSplit[0],amount);
						try {
							Arenas.tellPlayer(
									Bukkit.getPlayer(nSplit[0]),
									Language.parse("youwon",
											PVPArena.economy.format(amount)));
						} catch (Exception e) {
							// nothing
						}
					}
				}
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
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
		db.i("timed end!");

		for (String sTeam : arena.paTeams.keySet()) {
			iKills = 0;
			iDeaths = 0;

			try {
				iKills = Players.getKills(sTeam);
			} catch (Exception e) {
			}

			try {
				iDeaths = Players.getDeaths(sTeam);
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
						Language.parse("teamhaswon", "Team " + team));
				Players.tellEveryone(
						arena,
						Language.parse("teamhaswon",
								ChatColor.valueOf(arena.paTeams.get(team))
										+ "Team " + team));
			}

		}

		for (ArenaPlayer p : Players.getPlayers(arena)) {

			Player z = p.get();
			if (!result.contains(p.team)) {
				Players.parsePlayer(z).losses++;
				arena.resetPlayer(z, arena.cfg.getString("tp.lose", "old"));
			}
			p = null;
		}

		if (PVPArena.eco != null) {
			for (String nKey : Players.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");

				if (arena.paTeams.get(nSplit[1]) == null
						|| arena.paTeams.get(nSplit[1]).equals("free"))
					continue;

				if (result.contains(nSplit[1])) {
					double teamFactor = arena.cfg
							.getDouble("money.betTeamWinFactor")
							* arena.teamCount;
					if (teamFactor <= 0) {
						teamFactor = 1;
					}
					teamFactor *= arena.cfg.getDouble("money.betWinFactor");

					double amount = Players.paPlayersBetAmount.get(nKey)
							* teamFactor;

					
					if (PVPArena.economy == null && PVPArena.eco != null) {
						MethodAccount ma = PVPArena.eco.getAccount(nSplit[0]);
						if (ma == null) {
							db.s("Account not found: " + nSplit[0]);
							continue;
						}
						ma.add(amount);
						try {
							Arenas.tellPlayer(
									Bukkit.getPlayer(nSplit[0]),
									Language.parse("youwon",
											PVPArena.eco.format(amount)));
						} catch (Exception e) {
							// nothing
						}
					} else {
						if (!PVPArena.economy.hasAccount(nSplit[0])) {
							db.s("Account not found: " + nSplit[0]);
							continue;
						}
						PVPArena.economy.depositPlayer(nSplit[0],amount);
						try {
							Arenas.tellPlayer(
									Bukkit.getPlayer(nSplit[0]),
									Language.parse("youwon",
											PVPArena.economy.format(amount)));
						} catch (Exception e) {
							// nothing
						}
					}
				}
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
				new EndRunnable(arena), 15 * 20L);
	}
}
