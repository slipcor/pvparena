package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.runnables.EndRunnable;

/**
 * <pre>
 * Arena Goal class "Infect"
 * </pre>
 * 
 * Infected players kill ppl to enhance their team. Configurable lives
 * 
 * @author slipcor
 */

public class GoalInfect extends ArenaGoal {
	public GoalInfect() {
		super("Infect");
		debug = new Debug(108);
	}

	private EndRunnable endRunner = null;

	@Override
	public String version() {
		return "v1.0.1.40";
	}

	private static final int PRIORITY = 9;

	@Override
	public PACheck checkEnd(final PACheck res) {
		if (res.getPriority() > PRIORITY) {
			return res;
		}

		final int count = getLifeMap().size();

		if (count <= 1
				|| anyTeamEmpty()) {
			res.setPriority(this, PRIORITY); // yep. only one player left. go!
		}
		if (count == 0) {
			res.setError(this, MSG.ERROR_NOPLAYERFOUND.toString());
		}

		return res;
	}

	private boolean anyTeamEmpty() {
		for (ArenaTeam team : arena.getTeams()) {
			boolean bbreak = false;
			for (ArenaPlayer player : team.getTeamMembers()) {
				if (player.getStatus() == Status.FIGHT) {
					bbreak = true;
				}
			}
			if (bbreak) {
				continue;
			}
			return true;
		}
		return false;
	}

	@Override
	public String checkForMissingSpawns(final Set<String> list) {
		if (!arena.isFreeForAll()) {
			return null; // teams are handled somewhere else
		}
		
		boolean infected = false;

		int count = 0;
		for (String s : list) {
			if (s.startsWith("infected")) {
				infected = true;
			}
			if (s.startsWith("spawn")) {
				count++;
			}
		}
		if (!infected) {
			return "infected";
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
		if (res.getPriority() <= PRIORITY) {
			res.setPriority(this, PRIORITY);
		}
		return res;
	}

	@Override
	public PACheck checkStart(final PACheck res) {
		if (res.getPriority() < PRIORITY) {
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
				if (ap.getArenaTeam().getName().equals("infected")) {

					ArenaModuleManager.announce(arena,
							Language.parse(MSG.GOAL_INFECTED_WON), "WINNER");

					arena.broadcast(Language.parse(MSG.GOAL_INFECTED_WON));

					ArenaModuleManager.commitEnd(arena, team);
					return;
				} else {

					// String tank = tanks.get(arena);
					ArenaModuleManager.announce(arena,
							Language.parse(MSG.GOAL_INFECTED_LOST), "LOSER");

					arena.broadcast(Language.parse(MSG.GOAL_INFECTED_LOST));

					ArenaModuleManager.commitEnd(arena, team);
					return;
				}
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
		if (!getLifeMap().containsKey(player.getName())) {
			return;
		}
		int iLives = getLifeMap().get(player.getName());
		debug.i("lives before death: " + iLives, player);
		if (iLives <= 1 || ArenaPlayer.parsePlayer(player.getName()).getArenaTeam().getName().equals("infected")) {
			if (iLives <= 1 && ArenaPlayer.parsePlayer(player.getName()).getArenaTeam().getName().equals("infected")) {
					// kill, remove!
				getLifeMap().remove(player.getName());
				if (arena.getArenaConfig().getBoolean(CFG.PLAYER_PREVENTDEATH)) {
					debug.i("faking player death", player);
					PlayerListener.finallyKillPlayer(arena, player, event);
				}
				return;
			} else if (iLives <= 1) {
				// dying player -> infected
				getLifeMap().put(player.getName(), arena.getArenaConfig().getInt(CFG.GOAL_INFECTED_ILIVES));
				arena.msg(player, Language.parse(MSG.GOAL_INFECTED_YOU));
				arena.broadcast(Language.parse(MSG.GOAL_INFECTED_PLAYER, player.getName()));
				
				ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
				
				final ArenaTeam oldTeam = aPlayer.getArenaTeam();
				
				oldTeam.remove(aPlayer);
				
				final ArenaTeam respawnTeam = arena.getTeam("infected");
				respawnTeam.add(aPlayer);
				
				final ArenaClass infectedClass = arena.getClass("%infected%");
				if (infectedClass != null) {
					aPlayer.setArenaClass(infectedClass);
				}
				
				if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
					arena.broadcast(Language.parse(
							MSG.FIGHT_KILLED_BY,
							respawnTeam.colorizePlayer(player) + ChatColor.YELLOW,
							arena.parseDeathCause(player, event.getEntity()
									.getLastDamageCause().getCause(),
									player.getKiller()), String.valueOf(iLives)));
				}

				if (arena.isCustomClassAlive()
						|| arena.getArenaConfig().getBoolean(
								CFG.PLAYER_DROPSINVENTORY)) {
					InventoryManager.drop(player);
					event.getDrops().clear();
				}
				
				PACheck.handleRespawn(arena,
						ArenaPlayer.parsePlayer(player.getName()), event.getDrops());
				
				if (anyTeamEmpty()) {
					PACheck.handleEnd(arena, false);
				}
				return;
			} else {
				// dying infected player, has lives remaining
				iLives--;
				getLifeMap().put(player.getName(), iLives);
			}

			final ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(player.getName())
					.getArenaTeam();
			if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
				arena.broadcast(Language.parse(
						MSG.FIGHT_KILLED_BY_REMAINING,
						respawnTeam.colorizePlayer(player) + ChatColor.YELLOW,
						arena.parseDeathCause(player, event.getEntity()
								.getLastDamageCause().getCause(),
								player.getKiller()), String.valueOf(iLives)));
			}

			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(
							CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(player);
				event.getDrops().clear();
			}
			
			PACheck.handleRespawn(arena,
					ArenaPlayer.parsePlayer(player.getName()), event.getDrops());
			
			// player died => commit death!
			PACheck.handleEnd(arena, false);
		} else {
			iLives--;
			getLifeMap().put(player.getName(), iLives);

			final ArenaTeam respawnTeam = ArenaPlayer.parsePlayer(player.getName())
					.getArenaTeam();
			if (arena.getArenaConfig().getBoolean(CFG.USES_DEATHMESSAGES)) {
				arena.broadcast(Language.parse(
						MSG.FIGHT_KILLED_BY_REMAINING,
						respawnTeam.colorizePlayer(player) + ChatColor.YELLOW,
						arena.parseDeathCause(player, event.getEntity()
								.getLastDamageCause().getCause(),
								player.getKiller()), String.valueOf(iLives)));
			}

			if (arena.isCustomClassAlive()
					|| arena.getArenaConfig().getBoolean(
							CFG.PLAYER_DROPSINVENTORY)) {
				InventoryManager.drop(player);
				event.getDrops().clear();
			}

			PACheck.handleRespawn(arena,
					ArenaPlayer.parsePlayer(player.getName()), event.getDrops());
		}
	}

	@Override
	public void commitStart() {
		for (ArenaTeam team : arena.getTeams()) {
			SpawnManager.distribute(arena, team.getTeamMembers());
		}
	}

	@Override
	public void displayInfo(final CommandSender sender) {
		sender.sendMessage("normal lives: "
				+ arena.getArenaConfig().getInt(CFG.GOAL_INFECTED_NLIVES) + "|" +
				"infected lives: "
				+ arena.getArenaConfig().getInt(CFG.GOAL_INFECTED_ILIVES));
	}

	@Override
	public PACheck getLives(final PACheck res, final ArenaPlayer aPlayer) {
		if (!res.hasError() && res.getPriority() <= PRIORITY) {
			res.setError(
					this,
					String.valueOf(getLifeMap().containsKey(aPlayer.getName()) ? getLifeMap().get(aPlayer
									.getName()) : 0));
		}
		return res;
	}

	@Override
	public String guessSpawn(final String place) {
		// no exact match: assume we have multiple spawnpoints
		final Map<Integer, String> locs = new HashMap<Integer, String>();
		int pos = 0;

		if (!place.equals("infected")) {
			return null;
		}
		
		debug.i("searching for infect spawns: " + place);

		final Map<String, Object> coords = (HashMap<String, Object>) arena
				.getArenaConfig().getYamlConfiguration()
				.getConfigurationSection("spawns").getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(pos++, name);
				debug.i("found match: " + name);
			}
		}

		if (locs.size() < 1) {
			return null;
		}
		final Random random = new Random();

		return locs.get(random.nextInt(locs.size()));
	}

	@Override
	public boolean hasSpawn(final String string) {
		

		if (arena.getArenaConfig().getBoolean(CFG.GENERAL_CLASSSPAWN)) {
			for (ArenaClass aClass : arena.getClasses()) {
				if (string.toLowerCase().startsWith( 
						aClass.getName() + "spawn")) {
					return true;
				}
			}
		}
		
		return (arena.isFreeForAll() && string.toLowerCase()
				.startsWith("spawn")) || string.startsWith("infected");
	}

	@Override
	public void initate(final Player player) {
		getLifeMap().put(player.getName(),
				arena.getArenaConfig().getInt(CFG.GOAL_INFECTED_NLIVES));
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
		if (getLifeMap().containsKey(player.getName())) {
			getLifeMap().remove(player.getName());
		}
	}

	@Override
	public void parseStart() {
		ArenaPlayer infected = null;
		final Random random = new Random();
		for (ArenaTeam team : arena.getTeams()) {
			int pos = random.nextInt(team.getTeamMembers().size());
			debug.i("team " + team.getName() + " random " + pos);
			for (ArenaPlayer ap : team.getTeamMembers()) {
				debug.i("#" + pos + ": " + ap.toString(), ap.getName());
				if (pos-- == 0) {
					infected = ap;
				}
				this.getLifeMap().put(ap.getName(),
						arena.getArenaConfig().getInt(CFG.GOAL_INFECTED_ILIVES));
				break;
			}
		}
		final ArenaTeam infectedTeam = new ArenaTeam("infected", "PINK");
		for (ArenaTeam team : arena.getTeams()) {
			team.remove(infected);
		}
		infectedTeam.add(infected);

		final ArenaClass infectedClass = arena.getClass("%infected%");
		if (infectedClass != null) {
			infected.setArenaClass(infectedClass);
			InventoryManager.clearInventory(infected.get());
			infectedClass.equip(infected.get());
			for (ArenaModule mod : arena.getMods()) {
				mod.parseRespawn(infected.get(), infectedTeam, DamageCause.CUSTOM,
						infected.get());
			}
		}

		arena.msg(infected.get(), Language.parse(MSG.GOAL_INFECTED_YOU, infected.getName()));
		arena.broadcast(Language.parse(MSG.GOAL_INFECTED_PLAYER, infected.getName()));
		arena.tpPlayerToCoordName(infected.get(), "infected");
		arena.getTeams().add(infectedTeam);
	}

	@Override
	public void reset(final boolean force) {
		endRunner = null;
		getLifeMap().clear();
		arena.getTeams().remove(arena.getTeam("infected"));
	}

	@Override
	public void setPlayerLives(final int value) {
		final Set<String> plrs = new HashSet<String>();

		for (String name : getLifeMap().keySet()) {
			plrs.add(name);
		}

		for (String s : plrs) {
			getLifeMap().put(s, value);
		}
	}

	@Override
	public void setPlayerLives(final ArenaPlayer aPlayer, final int value) {
		getLifeMap().put(aPlayer.getName(), value);
	}

	@Override
	public Map<String, Double> timedEnd(final Map<String, Double> scores) {
		double score;

		for (ArenaPlayer ap : arena.getFighters()) {
			score = (getLifeMap().containsKey(ap.getName()) ? getLifeMap().get(ap.getName())
					: 0);
			if (ap.getArenaTeam() != null && ap.getArenaTeam().getName().equals("infected")) {
				score *= arena.getFighters().size();
			}
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
		getLifeMap().remove(player.getName());
	}
}
