package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.PlayerState;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>
 * Arena Goal class "PlayerDeathMatch"
 * </pre>
 * 
 * The first Arena Goal. Players have lives. When every life is lost, the player
 * is teleported to the spectator spawn to watch the rest of the fight.
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class GoalPlayerDeathMatch extends ArenaGoal {
	public GoalPlayerDeathMatch() {
		super("PlayerDeathMatch");
		debug = new Debug(101);
	}

	private EndRunnable endRunner = null;

	private final Map<String, Integer> lives = new HashMap<String, Integer>();

	@Override
	public String version() {
		return "v0.10.3.0";
	}

	private static final int PRIORITY = 3;

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE);
	}

	@Override
	public PACheck checkEnd(final PACheck res) {
		if (res.getPriority() > PRIORITY) {
			return res;
		}

		final int count = lives.size();

		if (count <= 1) {
			res.setPriority(this, PRIORITY); // yep. only one player left. go!
		}
		if (count == 0) {
			res.setError(this, MSG.ERROR_NOPLAYERFOUND.toString());
		}

		return res;
	}

	@Override
	public String checkForMissingSpawns(final Set<String> list) {
		if (!arena.isFreeForAll()) {
			return null; // teams are handled somewhere else
		}
		int count = 0;
		for (String s : list) {
			if (s.startsWith("spawn")) {
				count++;
			}
		}
		return count > 3 ? null : "need more spawns! (" + count + "/4)";
	}

	@Override
	public PACheck checkJoin(final CommandSender sender, final PACheck res, final String[] args) {
		if (res.getPriority() >= PRIORITY) {
			return res;
		}

		final int maxPlayers = arena.getArenaConfig().getInt(CFG.READY_MAXPLAYERS);
		final int maxTeamPlayers = arena.getArenaConfig().getInt(
				CFG.READY_MAXTEAMPLAYERS);

		if (maxPlayers > 0 && arena.getFighters().size() >= maxPlayers) {
			res.setError(this, Language.parse(MSG.ERROR_JOIN_ARENA_FULL));
			return res;
		}

		if (args == null || args.length < 1) {
			return res;
		}

		if (!arena.isFreeForAll()) {
			final ArenaTeam team = arena.getTeam(args[0]);

			if (team != null && maxTeamPlayers > 0
						&& team.getTeamMembers().size() >= maxTeamPlayers) {
				res.setError(this, Language.parse(MSG.ERROR_JOIN_TEAM_FULL));
				return res;
			}
		}

		res.setPriority(this, PRIORITY);
		return res;
	}

	@Override
	public PACheck checkPlayerDeath(final PACheck res, final Player player) {
		if (res.getPriority() <= PRIORITY && player.getKiller() != null
				&& arena.hasPlayer(player.getKiller())) {
			res.setPriority(this, PRIORITY);
		}
		return res;
	}

	@Override
	public void commitEnd(final boolean force) {
		if (endRunner != null) {
			return;
		}
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.getStatus().equals(Status.FIGHT)) {
					continue;
				}
				ArenaModuleManager.announce(arena,
						Language.parse(MSG.PLAYER_HAS_WON, ap.getName()),
						"WINNER");

				arena.broadcast(Language.parse(MSG.PLAYER_HAS_WON, ap.getName()));
			}
			if (ArenaModuleManager.commitEnd(arena, team)) {
				return;
			}
		}
		endRunner = new EndRunnable(arena, arena.getArenaConfig().getInt(
				CFG.TIME_ENDCOUNTDOWN));
	}

	@Override
	public void commitPlayerDeath(final Player player, final boolean doesRespawn,
			final String error, final PlayerDeathEvent event) {

		if (player.getKiller() == null
				|| !lives.containsKey(player.getKiller().getName())) {
			if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
				final ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(player.getName())
						.getArenaTeam();
				arena.broadcast(Language.parse(
						MSG.FIGHT_KILLED_BY,
						respawnTeam.colorizePlayer(player) + ChatColor.YELLOW,
						arena.parseDeathCause(player, event.getEntity()
								.getLastDamageCause().getCause(), player)));
			}

			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(
							CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(player);
				event.getDrops().clear();
			}

			PACheck.handleRespawn(arena, ArenaPlayer.parsePlayer(player.getName()),
					event.getDrops());

			return;
		}
		final Player killer = player.getKiller();

		int iLives = lives.get(killer.getName());
		debug.i("kills to go: " + iLives, killer);
		if (iLives <= 1) {
			// player has won!
			final Set<ArenaPlayer> plrs = new HashSet<ArenaPlayer>();
			for (ArenaPlayer ap : arena.getFighters()) {
				if (ap.getName().equals(killer.getName())) {
					continue;
				}
				plrs.add(ap);
			}
			for (ArenaPlayer ap : plrs) {
				lives.remove(ap.getName());
				debug.i("faking player death", ap.get());
				arena.removePlayer(ap.get(), CFG.TP_LOSE.toString(), true,
						false);

				ap.setStatus(Status.LOST);
				ap.addLosses();

				PlayerState.fullReset(arena, ap.get());

				if (ArenaManager.checkAndCommit(arena, false)) {
					return;
				}
			}

			PACheck.handleEnd(arena, false);
		} else {
			iLives--;
			lives.put(killer.getName(), iLives);

			final ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(player.getName())
					.getArenaTeam();
			if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
				arena.broadcast(Language.parse(
						MSG.FIGHT_KILLED_BY_REMAINING_FRAGS,
						respawnTeam.colorizePlayer(player) + ChatColor.YELLOW,
						arena.parseDeathCause(player, event.getEntity()
								.getLastDamageCause().getCause(), killer),
						String.valueOf(iLives)));
			}

			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(
							CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(player);
				event.getDrops().clear();
			}

			PACheck.handleRespawn(arena, ArenaPlayer.parsePlayer(player.getName()),
					event.getDrops());

		}
	}

	@Override
	public void displayInfo(final CommandSender sender) {
		sender.sendMessage("lives: "
				+ arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES));
	}

	@Override
	public PACheck getLives(final PACheck res, final ArenaPlayer aPlayer) {
		if (!res.hasError() && res.getPriority() <= PRIORITY) {
			res.setError(
					this,
					String.valueOf(arena.getArenaConfig()
									.getInt(CFG.GOAL_PDM_LIVES) - (lives
									.containsKey(aPlayer.getName()) ? lives.get(aPlayer
									.getName()) : 0)));
		}
		return res;
	}

	@Override
	public boolean hasSpawn(final String string) {
		return arena.isFreeForAll() && string.toLowerCase()
				.startsWith("spawn");
	}

	@Override
	public void initate(final Player player) {
		lives.put(player.getName(),
				arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES));
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public void parseLeave(final Player player) {
		if (player == null) {
			PVPArena.instance.getLogger().warning(
					this.getName() + ": player NULL");
			return;
		}
		if (lives.containsKey(player.getName())) {
			lives.remove(player.getName());
		}
	}

	@Override
	public void parseStart() {
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				this.lives.put(ap.getName(),
						arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES));
			}
		}
	}

	@Override
	public void reset(final boolean force) {
		endRunner = null;
		lives.clear();
	}

	@Override
	public Map<String, Double> timedEnd(final Map<String, Double> scores) {
		double score;

		for (ArenaPlayer ap : arena.getFighters()) {
			score = arena.getArenaConfig().getInt(CFG.GOAL_PDM_LIVES)
					- (lives.containsKey(ap.getName()) ? lives
							.get(ap.getName()) : 0);
			if (scores.containsKey(ap)) {
				scores.put(ap.getName(), scores.get(ap.getName()) + score);
			} else {
				scores.put(ap.getName(), score);
			}
		}

		return scores;
	}

	@Override
	public void unload(final Player player) {
		lives.remove(player.getName());
		if (allowsJoinInBattle()) {
			arena.hasNotPlayed(ArenaPlayer.parsePlayer(player.getName()));
		}
	}
}
