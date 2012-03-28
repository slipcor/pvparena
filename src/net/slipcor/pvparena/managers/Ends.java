package net.slipcor.pvparena.managers;

import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.definitions.Announcement;
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
 * @version v0.7.0
 * 
 */

public class Ends {

	private static final Debug db = new Debug(28);

	/**
	 * [FLAG] commit the arena end
	 * 
	 * @param sTeam
	 *            the team name
	 * @param win
	 *            winning team?
	 */
	public static void commit(Arena arena, String sTeam, boolean win) {
		if (arena.cfg.getBoolean("arenatype.deathmatch")
				|| arena.cfg.getBoolean("arenatype.domination")) {
			win = !win;
		}
		db.i("[FLAG/DM/DOM] committing end: " + sTeam);
		db.i("win: " + String.valueOf(win));

		String winteam = sTeam;

		for (ArenaTeam team : arena.getTeams()) {
			if (team.getName().equals(sTeam) == win) {
				continue;
			}
			for (ArenaPlayer ap : team.getTeamMembers()) {

				Players.parsePlayer(ap.get()).losses++;
				Players.setTelePass(ap.get(), true);
				Location l = Spawns.getCoords(arena, "spectator");
				ap.get().teleport(l);
				Players.setTelePass(ap.get(), false);
				ap.setSpectator(true);
			}
		}
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.isSpectator()) {
					continue;
				}
				winteam = team.getName();
				break;
			}
		}

		if (arena.getTeam(winteam) != null) {
			Announcement.announce(arena, type.WINNER,
					Language.parse("teamhaswon", "Team " + winteam));
			Players.tellEveryone(
					arena,
					Language.parse("teamhaswon", arena.getTeam(winteam)
							.getColor() + "Team " + winteam));
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
			if (Players.countPlayersInTeams(arena) > 1) {
				db.i("more than one player active => no end :p");
				return false;
			}

			for (ArenaPlayer ap : arena.getPlayers()) {
				if (!ap.isSpectator()) {
					Announcement.announce(
							arena,
							type.WINNER,
							Language.parse("playerhaswon",
									ChatColor.WHITE + ap.getName()));
					Players.tellEveryone(
							arena,
							Language.parse("playerhaswon",
									ChatColor.WHITE + ap.getName()));
				}
			}

			Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
					new EndRunnable(arena), 15 * 20L);
			return true;
		}
		if (arena.cfg.getBoolean("arenatype.flags")) {
			db.i("[FLAG]");

			if (Players.countPlayersInTeams(arena) < 2) {
				String sTeam = "$%&/";
				if (Players.countPlayersInTeams(arena) != 0) {
					for (ArenaTeam team : arena.getTeams()) {
						for (ArenaPlayer ap : team.getTeamMembers()) {
							if (!ap.isSpectator()) {
								commit(arena, team.getName(), true);
								return true;
							}
						}
					}
				}
				commit(arena, sTeam, true);
			}
			return false;

		}
		db.i("[TEAMS]");

		ArenaTeam aTeam = null;

		if (arena.countActiveTeams() > 1) {
			return false;
		}

		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.isSpectator()) {
					aTeam = team;
					break;
				}
			}
		}

		if (aTeam != null) {
			Announcement.announce(arena, type.WINNER,
					Language.parse("teamhaswon", "Team " + aTeam.getName()));
			Players.tellEveryone(
					arena,
					Language.parse("teamhaswon", aTeam.getColor() + "Team "
							+ aTeam.getName()));
		}

		if (PVPArena.eco != null || PVPArena.economy != null) {
			db.i("eConomy set, parse bets");
			for (String nKey : Players.paPlayersBetAmount.keySet()) {
				db.i("bet: " + nKey);
				String[] nSplit = nKey.split(":");

				if (arena.getTeam(nSplit[1]) == null
						|| arena.getTeam(nSplit[1]).getName().equals("free"))
					continue;

				if (nSplit[1].equalsIgnoreCase(aTeam.getName())) {
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
						PVPArena.economy.depositPlayer(nSplit[0], amount);
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
		int i;

		int max = arena.cfg.getBoolean("arenatype.deathmatch") ? 10000 : -1;
		HashSet<String> result = new HashSet<String>();
		db.i("timed end!");

		for (String sTeam : arena.paLives.keySet()) {
			i = arena.paLives.get(sTeam);

			if (arena.cfg.getBoolean("arenatype.deathmatch")) {
				if (i < max) {
					result = new HashSet<String>();
					result.add(sTeam);
					max = i;
				} else if (i == max) {
					result.add(sTeam);
				}
			} else {
				if (i > max) {
					result = new HashSet<String>();
					result.add(sTeam);
					max = i;
				} else if (i == max) {
					result.add(sTeam);
				}
			}

		}

		for (ArenaTeam team : arena.getTeams()) {
			if (result.contains(team.getName())) {
				Announcement.announce(arena, type.WINNER,
						Language.parse("teamhaswon", "Team " + team.getName()));
				Players.tellEveryone(
						arena,
						Language.parse("teamhaswon", team.getColor() + "Team "
								+ team.getName()));
			}
			for (ArenaPlayer p : arena.getPlayers()) {
				if (p.isSpectator()) {
					continue;
				}
				if (!result.contains(team.getName())) {
					p.losses++;
					arena.tpPlayerToCoordName(p.get(), "spectator");
				}
			}
		}

		if (PVPArena.eco != null) {
			for (String nKey : Players.paPlayersBetAmount.keySet()) {
				String[] nSplit = nKey.split(":");
				ArenaTeam team = arena.getTeam(nSplit[1]);
				if (team == null || team.getName().equals("free"))
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
						PVPArena.economy.depositPlayer(nSplit[0], amount);
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
