package net.slipcor.pvparena.neworder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nodinchan.loader.Loadable;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.definitions.Announcement;
import net.slipcor.pvparena.definitions.Announcement.type;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Players;
import net.slipcor.pvparena.managers.Spawns;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;
import net.slipcor.pvparena.runnables.EndRunnable;

/*

 import java.lang.reflect.*;

 Param1Type param1;
 Param2Type param2;
 String className = "Class1";
 Class cl = Class.forName(className);
 Constructor con = cl.getConstructor(Param1Type.class, Param2Type.class);
 Object xyz = con.newInstance(param1, param2);

 */
public class ArenaType extends Loadable {
	protected Arena arena;
	protected final static Debug db = new Debug(45);

	public ArenaType(String sName) {
		super(sName);
	}
	
	protected void setArena(Arena arena) {
		this.arena = arena;
	}

	/**
	 * does the arena type allow random spawns?
	 */
	public boolean allowsRandomSpawns() {
		return arena.cfg.getBoolean("arenatype.randomSpawn", false);
	}
	
	public int ready(Arena arena) {
		return 0;
	}

	/**
	 * does the arena type use flags?
	 */
	public boolean usesFlags() {
		return false;
	}

	public String checkSpawns(Set<String> list) {
		// not random! we need teams * 2 (lounge + spawn) + exit + spectator
		db.i("parsing not random");
		Iterator<String> iter = list.iterator();
		int spawns = 0;
		int lounges = 0;
		HashSet<String> setTeams = new HashSet<String>();
		while (iter.hasNext()) {
			String s = iter.next();
			db.i("parsing '" + s + "'");
			db.i("spawns: " + spawns + "; lounges: " + lounges);
			if (s.endsWith("spawn") && (!s.equals("spawn"))) {
				spawns++;
			} else if (s.endsWith("lounge") && (!s.equals("lounge"))) {
				lounges++;
			} else if (s.contains("spawn") && (!s.equals("spawn"))) {
				String[] temp = s.split("spawn");
				if (arena.getTeam(temp[0]) != null) {
					if (setTeams.contains(temp[0])) {
						db.i("team already set");
						continue;
					}
					db.i("adding team");
					setTeams.add(temp[0]);
					spawns++;
				}
			}
		}
		if (spawns == arena.getTeams().size()
				&& lounges == arena.getTeams().size()) {
			return null;
		}

		return spawns + "/" + arena.getTeams().size() + "x spawn ; "
				+ lounges + "/" + arena.getTeams().size() + "x lounge";
	}

	public String checkFlags(Set<String> list) {
		return null;
	}

	public boolean isLoungeCommand(Player player, String cmd) {

		if (!player.getWorld().getName().equals(arena.getWorld())) {
			Arenas.tellPlayer(player,
					Language.parse("notsameworld", arena.getWorld()), arena);
			return false;
		}

		if (cmd.equalsIgnoreCase("lounge")) {
			Arenas.tellPlayer(player, Language.parse("errorloungefree"), arena);
			return false;
		}

		if (cmd.endsWith("lounge")) {
			String sTeam = cmd.replace("lounge", "");
			if (arena.getTeam(sTeam) != null) {
				Spawns.setCoords(arena, player, cmd);
				Arenas.tellPlayer(player, Language.parse("setlounge", sTeam), arena);
				return true;
			}
			Arenas.tellPlayer(player, Language.parse("invalidcmd", "506"), arena);
			return true;
		}
		return false;
	}

	public boolean isSpawnCommand(Player player, String cmd) {
		if (!player.getWorld().getName().equals(arena.getWorld())) {
			Arenas.tellPlayer(player,
					Language.parse("notsameworld", arena.getWorld()), arena);
			return false;
		}

		if (cmd.startsWith("spawn") || cmd.equals("spawn")) {
			Arenas.tellPlayer(player, Language.parse("errorspawnfree", cmd), arena);
			return false;
		}

		if (cmd.contains("spawn")) {
			String[] split = cmd.split("spawn");
			String sName = split[0];
			if (arena.getTeam(sName) == null)
				return false;

			Spawns.setCoords(arena, player, cmd);
			Arenas.tellPlayer(player, Language.parse("setspawn", sName), arena);
			return true;
		}

		if (cmd.startsWith("powerup")) {
			Spawns.setCoords(arena, player, cmd);
			Arenas.tellPlayer(player, Language.parse("setspawn", cmd), arena);
			return true;
		}
		return false;
	}

	public boolean isCustomCommand(Player player, String cmd) {
		return false;
	}

	public void addDefaultTeams(YamlConfiguration config) {
		if (arena.cfg.get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			arena.cfg.getYamlConfiguration().addDefault("teams.red",
					ChatColor.RED.name());
			arena.cfg.getYamlConfiguration().addDefault("teams.blue",
					ChatColor.BLUE.name());
		}
		if (arena.cfg.getBoolean("game.woolFlagHead")
				&& (arena.cfg.get("flagColors") == null)) {
			db.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
	}

	public boolean checkAndCommit() {
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
											PVPArena.eco.format(amount)), arena);
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
											PVPArena.economy.format(amount)), arena);
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

	public void parseRespawn(Player respawnPlayer,
			ArenaTeam respawnTeam, int lives, DamageCause cause, Entity damager) {
		Players.tellEveryone(arena, Language.parse("killedbylives",
				respawnTeam.colorizePlayer(respawnPlayer) + ChatColor.YELLOW,
				Players.parseDeathCause(arena, respawnPlayer, cause, damager),
				String.valueOf(lives)));
		arena.paLives.put(respawnPlayer.getName(), lives);
	}

	public void init() {
		return;
	}

	public void checkEntityDeath(Player player) {
		return;
	}

	public void checkInteract(Player player, Block clickedBlock) {
		return;
	}

	public boolean reduceLivesCheckEndAndCommit(String team) {
		return false;
	}

	protected short getFlagOverrideTeamShort(String team) {
		return 0;
	}

	public int reduceLives(Player player, int lives) {
		lives = arena.paLives.get(player.getName());
		db.i("lives before death: " + lives);
		return lives;
	}

	public boolean allowsJoinInBattle() {
		return false;
	}

	public void configParse() {
		return;
	}

	public static boolean checkSetFlag(Block block, Player player) {
		if (Arena.regionmodify.contains(":")) {
			String[] s = Arena.regionmodify.split(":");
			Arena arena = Arenas.getArenaByName(s[0]);
			if (arena == null) {
				return false;
			}
			db.i("onInteract: flag/pumpkin");

			return arena.type().checkSetFlag(player, block);

		} else if (block != null && block.getType().equals(Material.WOOL)) {
			Arena arena = Arenas.getArenaByRegionLocation(block.getLocation());
			if (arena != null) {
				return arena.type().checkSetFlag(player, block);
			}
		}
		return false;
	}

	protected boolean checkSetFlag(Player player, Block block) {
		return false;
	}

	public String guessSpawn(String place) {
		if (!place.contains("spawn")) {
			db.i("place not found!");
			return null;
		}
		// no exact match: assume we have multiple spawnpoints
		HashMap<Integer, String> locs = new HashMap<Integer, String>();
		int i = 0;

		db.i("searching for team spawns");

		HashMap<String, Object> coords = (HashMap<String, Object>) arena.cfg
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(i++, name);
				db.i("found match: " + name);
			}
		}

		if (locs.size() < 1) {
			return null;
		}
		Random r = new Random();

		place = locs.get(r.nextInt(locs.size()));

		return place;
	}

	public void timed() {
		int i;

		int max = -1;
		HashSet<String> result = new HashSet<String>();
		db.i("timed end!");

		for (String sTeam : arena.paLives.keySet()) {
			i = arena.paLives.get(sTeam);

			if (i > max) {
				result = new HashSet<String>();
				result.add(sTeam);
				max = i;
			} else if (i == max) {
				result.add(sTeam);
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

		pay(result);

		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
				new EndRunnable(arena), 15 * 20L);
	}
	
	protected void pay(HashSet<String> result) {
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
											PVPArena.eco.format(amount)), arena);
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
											PVPArena.economy.format(amount)), arena);
						} catch (Exception e) {
							// nothing
						}
					}
				}
			}
		}
	}

	public void parseMove(Player player) {
		return;
	}

	public ArenaType cloneThis() {
		try {
			return (ArenaType) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean isRegionCommand(String s) {
		db.i("checking region command: " + s);
		if (s.equals("exit") || s.equals("spectator")
				|| s.equals("battlefield") || s.equals("join")) {
			return true;
		}
		for (ArenaTeam team : arena.getTeams()) {
			String sName = team.getName();
			if (s.equals(sName + "lounge")) {
				return true;
			}
		}
		return false;
	}
}
