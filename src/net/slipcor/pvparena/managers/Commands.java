package net.slipcor.pvparena.managers;

import java.util.HashMap;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.definitions.Announcement;
import net.slipcor.pvparena.definitions.Announcement.type;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.ArenaPlayer;
import net.slipcor.pvparena.definitions.ArenaRegion;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * command manager class
 * 
 * -
 * 
 * provides command parsing to relieve the main plugin class
 * 
 * @author slipcor
 * 
 * @version v0.6.36
 * 
 */

public class Commands {
	private static Debug db = new Debug(25);

	/**
	 * check and commit chat command
	 * 
	 * @param arena
	 *            the arena to join
	 * @param player
	 *            the player who joins
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseChat(Arena arena, Player player) {
		if (arena.paChat.contains(player.getName())) {
			arena.paChat.remove(player.getName());
			Arenas.tellPlayer(player, "You now talk to the public!");
		} else {
			arena.paChat.add(player.getName());
			Arenas.tellPlayer(player, "You now talk to your team!");
		}
		return true;
	}

	/**
	 * check and commit join command
	 * 
	 * @param arena
	 *            the arena to join
	 * @param player
	 *            the player who joins
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseJoin(Arena arena, Player player) {
		// just /pa or /pvparena

		if (!checkJoin(arena, player)) {
			return true;
		}
		if (!arena.cfg.getBoolean("join.random", true)) {
			Arenas.tellPlayer(player, Language.parse("selectteam"));
			return true;
		}
		int entryfee = arena.cfg.getInt("money.entry", 0);

		if (Teams.calcFreeTeam(arena) == null
				|| ((arena.cfg.getInt("ready.max") > 0) && (arena.cfg
						.getInt("ready.max") <= Players
						.countPlayersInTeams(arena)))) {

			Arenas.tellPlayer(player, Language.parse("arenafull"));
			return true;
		}

		arena.prepare(player, false);
		arena.paLives.put(player.getName(), arena.cfg.getInt("game.lives", 3));

		if (entryfee > 0) {
			if (PVPArena.economy != null) {
				PVPArena.economy.withdrawPlayer(player.getName(), entryfee);
				Arenas.tellPlayer(
						player,
						Language.parse("joinpay",
								PVPArena.economy.format(entryfee)));
			} else if (PVPArena.eco != null) {
				MethodAccount ma = PVPArena.eco.getAccount(player.getName());
				ma.subtract(entryfee);
				Arenas.tellPlayer(player, Language.parse("joinpay",
						PVPArena.eco.format(entryfee)));
			}
		}

		Teams.choosePlayerTeam(arena, player);
		Inventories.prepareInventory(arena, player);

		// process auto classing
		String autoClass = arena.cfg.getString("ready.autoclass");
		if (autoClass != null && !autoClass.equals("none")) {
			if (arena.paClassItems.containsKey(autoClass)) {
				Players.chooseClass(arena, player, null, autoClass);
			} else {
				db.w("autoclass selected that does not exist: " + autoClass);
			}
		}
		return true;
	}

	/**
	 * check and commit team join command
	 * 
	 * @param arena
	 *            the arena to join
	 * @param player
	 *            the player that joins
	 * @param sTeam
	 *            the team to join
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseJoinTeam(Arena arena, Player player, String sTeam) {

		// /pa [team] or /pvparena [team]

		if (!checkJoin(arena, player)) {
			return true;
		}

		if (!(arena.cfg.getBoolean("join.manual", true))) {
			Arenas.tellPlayer(player, Language.parse("notselectteam"));
			return true;
		}
		int entryfee = arena.cfg.getInt("money.entry", 0);

		if (arena.cfg.getInt("ready.max") > 0
				&& arena.cfg.getInt("ready.max") <= Players
						.countPlayersInTeams(arena)) {

			Arenas.tellPlayer(player,
					Language.parse("teamfull", arena.colorizeTeam(sTeam)));
			return true;
		}

		arena.prepare(player, false);
		arena.paLives.put(player.getName(), arena.cfg.getInt("game.lives", 3));

		if (entryfee > 0) {
			if (PVPArena.economy != null) {
				PVPArena.economy.withdrawPlayer(player.getName(), entryfee);
				Arenas.tellPlayer(
						player,
						Language.parse("joinpay",
								PVPArena.economy.format(entryfee)));
			} else if (PVPArena.eco != null) {
				MethodAccount ma = PVPArena.eco.getAccount(player.getName());
				ma.subtract(entryfee);
				Arenas.tellPlayer(player, Language.parse("joinpay",
						PVPArena.eco.format(entryfee)));
			}
		}

		arena.tpPlayerToCoordName(player, sTeam + "lounge");
		Players.setTeam(player, sTeam);
		Inventories.prepareInventory(arena, player);
		if (Players.getPlayerTeamMap(arena).size() < 2) {
			Announcement.announce(
					arena,
					type.START,
					Language.parse("joinarena", arena.name));
		}
		Arenas.tellPlayer(player,
				Language.parse("youjoined", arena.colorizeTeam(sTeam)));
		Announcement.announce(
				arena,
				type.JOIN,
				Language.parse("playerjoined", player.getName(),
						arena.colorizeTeam(sTeam)));
		Players.tellEveryoneExcept(
				arena,
				player,
				Language.parse("playerjoined", player.getName(),
						arena.colorizeTeam(sTeam)));

		// process auto classing
		String autoClass = arena.cfg.getString("ready.autoclass");
		if (autoClass != null && !autoClass.equals("none")) {
			if (arena.paClassItems.containsKey(autoClass)) {
				Players.chooseClass(arena, player, null, autoClass);
			} else {
				db.w("autoclass selected that does not exist: " + autoClass);
			}
		}

		return true;
	}

	/**
	 * check various methods to see if the player may join the arena
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player to check
	 * @return true if the player may join, false otherwise
	 */
	private static boolean checkJoin(Arena arena, Player player) {
		String error = Configs.isSetup(arena);
		if (error != null) {
			Arenas.tellPlayer(player, Language.parse("arenanotsetup", error));
			return false;
		}

		if (!PVPArena.hasPerms(player, arena)) {
			Arenas.tellPlayer(player, Language.parse("permjoin"));
			return false;
		}

		if (Arenas.getArenaByPlayer(player) != null) {
			if (!Players.isPartOf(arena, player)
					|| !Players.parsePlayer(player).spectator) {

				Arenas.tellPlayer(player, Language.parse("alreadyjoined"));
				return false;
			}
		}

		if (player.isInsideVehicle()) {
			Arenas.tellPlayer(player, Language.parse("insidevehicle"));
			return false;
		}

		if (!Arenas.checkJoin(player)) {
			Arenas.tellPlayer(player, Language.parse("notjoinregion"));
			return false;
		}

		if (arena.fightInProgress) {
			if (arena.cfg.getBoolean("arenatype.flags")
					&& arena.cfg.getBoolean("join.inbattle")) {
				return true;
			}
			Arenas.tellPlayer(player, Language.parse("fightinprogress"));
			return false;
		}

		if (Regions.tooFarAway(arena, player)) {
			Arenas.tellPlayer(player, Language.parse("joinrange"));
			return false;
		}

		if (arena.cfg.getInt("money.entry", 0) > 0) {
			if (PVPArena.economy != null) {
				if (!PVPArena.economy.hasAccount(player.getName())) {
					db.s("Account not found: " + player.getName());
					return false;
				}
				if (!PVPArena.economy.has(player.getName(),
						arena.cfg.getInt("money.entry", 0))) {
					// no money, no entry!
					Arenas.tellPlayer(player, Language.parse("notenough",
							PVPArena.economy.format(arena.cfg.getInt(
									"money.entry", 0))));
					return false;
				}
			} else if (PVPArena.eco != null) {
				MethodAccount ma = PVPArena.eco.getAccount(player.getName());
				if (ma == null) {
					db.s("Account not found: " + player.getName());
					return false;
				}
				if (!ma.hasEnough(arena.cfg.getInt("money.entry", 0))) {
					// no money, no entry!
					Arenas.tellPlayer(player, Language.parse("notenough",
							PVPArena.eco.format(arena.cfg.getInt("money.entry",
									0))));
					return false;
				}
			}
		}

		if (arena.START_ID != -1) {
			Bukkit.getScheduler().cancelTask(arena.START_ID);
			db.i("player joining, cancelling start timer");
		}

		return true;
	}

	/**
	 * check and commit enable/disable toggle command
	 * 
	 * @param arena
	 *            the arena to toggle
	 * @param player
	 *            the player committing the command
	 * @param string
	 *            to commit (enabled/disabled)
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseToggle(Arena arena, Player player, String string) {
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse(string)));
			return true;
		}
		arena.cfg.set("general.enabled", string.equals("enabled"));
		arena.cfg.save();
		Arenas.tellPlayer(player, Language.parse(string));
		return true;
	}

	/**
	 * check and commit reload command
	 * 
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseReload(Player player) {

		if (!PVPArena.hasAdminPerms(player)) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("reload")));
			return true;
		}
		Arenas.load_arenas();
		Arenas.tellPlayer(player, Language.parse("reloaded"));
		return true;
	}

	/**
	 * send a list of active players
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseList(Arena arena, Player player) {
		if (Players.countPlayersInTeams(arena) < 1) {
			Arenas.tellPlayer(player, Language.parse("noplayer"));
			return true;
		}
		String plrs = Players.getTeamStringList(arena, arena.paTeams);
		Arenas.tellPlayer(player, Language.parse("players") + ": " + plrs);
		return true;
	}

	/**
	 * check and commit watch command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseSpectate(Arena arena, Player player) {
		String error = Configs.isSetup(arena);
		if (error != null) {
			Arenas.tellPlayer(player, Language.parse("arenanotsetup", error));
			return true;
		}
		if (!Players.getTeam(player).equals("")) {
			Arenas.tellPlayer(player, Language.parse("alreadyjoined"));
			return true;
		}
		if (Regions.tooFarAway(arena, player)) {
			Arenas.tellPlayer(player, Language.parse("joinrange"));
			return true;
		}
		arena.prepare(player, true);
		arena.tpPlayerToCoordName(player, "spectator");
		Inventories.prepareInventory(arena, player);
		Arenas.tellPlayer(player, Language.parse("specwelcome"));
		return true;
	}

	/**
	 * display player stats
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseUsers(Arena arena, Player player) {
		// wins are suffixed with "_"
		ArenaPlayer[] players = Statistics
				.getStats(arena, Statistics.type.WINS);

		Arenas.tellPlayer(player, Language.parse("top5win"));

		int limit = 5;

		for (ArenaPlayer ap : players) {
			if (limit-- < 1) {
				break;
			}
			Arenas.tellPlayer(player, ap.get().getName() + ": " + ap.wins + " "
					+ Language.parse("wins"));
		}

		Arenas.tellPlayer(player, "------------");
		Arenas.tellPlayer(player, Language.parse("top5lose"));

		players = Statistics.getStats(arena, Statistics.type.LOSSES);
		for (ArenaPlayer ap : players) {
			if (limit-- < 1) {
				break;
			}
			Arenas.tellPlayer(player, ap.get().getName() + ": " + ap.losses
					+ " " + Language.parse("losses"));
		}

		return true;
	}

	/**
	 * enable region modifying
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseRegion(Arena arena, Player player) {
		// /pa [name] region
		if (!Arena.regionmodify.equals("")) {
			Arenas.tellPlayer(player,
					Language.parse("regionalreadybeingset", Arena.regionmodify));
			return true;
		}
		Arena.regionmodify = arena.name;
		Arenas.tellPlayer(player, Language.parse("regionset"));
		return true;
	}

	/**
	 * check and commit admin command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command to commit
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseAdminCommand(Arena arena, Player player,
			String cmd) {

		db.i("parsing admin command: " + cmd);
		if (cmd.equalsIgnoreCase("spectator")) {
			if (!player.getWorld().getName().equals(arena.getWorld())) {
				Arenas.tellPlayer(player,
						Language.parse("notsameworld", arena.getWorld()));
				return false;
			}
			Spawns.setCoords(arena, player, "spectator");
			Arenas.tellPlayer(player, Language.parse("setspectator"));
		} else if (cmd.equalsIgnoreCase("exit")) {
			if (!player.getWorld().getName().equals(arena.getWorld())) {
				Arenas.tellPlayer(player,
						Language.parse("notsameworld", arena.getWorld()));
				return false;
			}
			Spawns.setCoords(arena, player, "exit");
			Arenas.tellPlayer(player, Language.parse("setexit"));
		} else if (cmd.equalsIgnoreCase("forcestop")) {
			if (arena.fightInProgress) {
				arena.forcestop();
				Arenas.tellPlayer(player, Language.parse("forcestop"));
			} else {
				Arenas.tellPlayer(player, Language.parse("nofight"));
			}
		} else if (cmd.equalsIgnoreCase("set")) {
			arena.sm.list(player, 1);
		} else if (arena.cfg.getBoolean("arenatype.randomSpawn", false)
				&& (cmd.startsWith("spawn"))) {
			if (!player.getWorld().getName().equals(arena.getWorld())) {
				Arenas.tellPlayer(player,
						Language.parse("notsameworld", arena.getWorld()));
				return false;
			}
			Spawns.setCoords(arena, player, cmd);
			Arenas.tellPlayer(player, Language.parse("setspawn", cmd));
		} else {
			// no random or not trying to set custom spawn
			if ((!isLoungeCommand(arena, player, cmd))
					&& (!isSpawnCommand(arena, player, cmd))
					&& (!isCustomCommand(arena, player, cmd))) {
				return parseJoin(arena, player);
			}
			// else: command lounge or spawn :)
		}
		return true;
	}

	/**
	 * parse commands
	 * 
	 * @param arena
	 *            the arena committing the command
	 * @param player
	 *            the player committing the commands
	 * @param args
	 *            the command arguments
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseCommand(Arena arena, Player player, String[] args) {
		if (!arena.cfg.getBoolean("general.enabled")
				&& !PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Language.parse("arenadisabled");
			return true;
		}
		db.i("parsing command: " + db.formatStringArray(args));

		if (args == null || args.length < 1) {
			return parseJoin(arena, player);
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("enable")) {
				return parseToggle(arena, player, "enabled");
			} else if (args[0].equalsIgnoreCase("disable")) {
				return parseToggle(arena, player, "disabled");
			} else if (args[0].equalsIgnoreCase("reload")) {
				return parseReload(player);
			} else if (args[0].equalsIgnoreCase("edit")) {
				return parseEdit(arena, player);
			} else if (args[0].equalsIgnoreCase("check")) {
				return parseCheck(arena, player);
			} else if (args[0].equalsIgnoreCase("info")) {
				return parseInfo(arena, player);
			} else if (args[0].equalsIgnoreCase("list")) {
				return parseList(arena, player);
			} else if (args[0].equalsIgnoreCase("watch")) {
				return parseSpectate(arena, player);
			} else if (args[0].equalsIgnoreCase("users")) {
				return parseUsers(arena, player);
			} else if (args[0].equalsIgnoreCase("region")) {
				return parseRegion(arena, player);
			} else if (arena.paTeams.get(args[0]) != null) {
				return parseJoinTeam(arena, player, args[0]);
			} else if (PVPArena.hasAdminPerms(player)
					|| (PVPArena.hasCreatePerms(player, arena))) {
				return parseAdminCommand(arena, player, args[0]);
			} else {
				return parseJoin(arena, player);
			}
		} else if (args.length == 3 && args[0].equalsIgnoreCase("bet")) {
			return parseBetCommand(arena, player, args);
		} else if ((args.length == 2 || args.length == 3)
				&& args[0].equalsIgnoreCase("stats")) {
			return parseStats(arena, player, args);
		} else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
			// pa [name] set [node] [value]
			arena.sm.set(player, args[1], args[2]);
			return true;
		} else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
			// pa [name] set [page]
			int i = 1;
			try {
				i = Integer.parseInt(args[1]);
			} catch (Exception e) {
				// nothing
			}
			arena.sm.list(player, i);
			return true;
		}

		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("admin")));
			return false;
		}

		if (!isRegionCommand(arena, args[1])) {
			Arenas.tellPlayer(player, Language.parse("invalidcmd", "504"));
			return false;
		}

		if ((args.length == 2) || (args.length == 3)) {

			if (args[0].equalsIgnoreCase("region")) {

				if (args.length == 2 && args[1].equalsIgnoreCase("remove")) {
					// pa region remove [regionname]
					if (arena.cfg.get("regions." + args[1]) != null) {
						arena.cfg.set("regions." + args[1], null);
						arena.cfg.save();
						Arena.regionmodify = "";
						Arenas.tellPlayer(player,
								Language.parse("regionremoved"));
					} else {
						Arenas.tellPlayer(player,
								Language.parse("regionnotremoved"));
					}
					return true;
				}

				// pa [name] region [regionname] {cuboid/sphere}
				if (Arena.regionmodify.equals("")) {
					Arenas.tellPlayer(player,
							Language.parse("regionnotbeingset", arena.name));
					return true;
				}

				Vector realMin = new Vector(
						Math.min(arena.pos1.getBlockX(), arena.pos2.getBlockX()),
						Math.min(arena.pos1.getBlockY(), arena.pos2.getBlockY()),
						Math.min(arena.pos1.getBlockZ(), arena.pos2.getBlockZ()));
				Vector realMax = new Vector(
						Math.max(arena.pos1.getBlockX(), arena.pos2.getBlockX()),
						Math.max(arena.pos1.getBlockY(), arena.pos2.getBlockY()),
						Math.max(arena.pos1.getBlockZ(), arena.pos2.getBlockZ()));

				String s = realMin.getBlockX() + "," + realMin.getBlockY()
						+ "," + realMin.getBlockZ() + "," + realMax.getBlockX()
						+ "," + realMax.getBlockY() + "," + realMax.getBlockZ();

				ArenaRegion.regionType type;

				if (args.length == 2) {
					type = ArenaRegion.regionType.CUBOID;
				} else {

					if (args[2].startsWith("c")) {
						type = ArenaRegion.regionType.CUBOID;
					} else if (args[2].startsWith("s")) {
						type = ArenaRegion.regionType.SPHERIC;
					} else {
						type = ArenaRegion.regionType.CUBOID;
					}
				}

				// only cuboid if args = 2 | args[2] = cuboid

				arena.cfg.set("regions." + args[1], s);
				arena.regions.put(args[1], new ArenaRegion(args[1], arena.pos1,
						arena.pos2, type));
				arena.pos1 = null;
				arena.pos2 = null;
				arena.cfg.save();

				Arena.regionmodify = "";
				Arenas.tellPlayer(player, Language.parse("regionsaved"));
				return true;

			} else if (args[0].equalsIgnoreCase("remove")) {
				// pa [name] remove [spawnname]
				arena.cfg.set("spawns." + args[1], null);
				arena.cfg.save();
				Arenas.tellPlayer(player,
						Language.parse("spawnremoved", args[1]));
				return true;
			}
		}

		if (args.length != 3) {
			Arenas.tellPlayer(player, Language.parse("invalidcmd", "505"));
			return false;
		}
		return true;
	}

	/**
	 * check and commit stats command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player to check
	 * @param args
	 *            the array {"stats", [stattype] {asc/desc}}
	 * @return false if the command help should be displayed, true otherwise
	 */
	private static boolean parseStats(Arena arena, Player player, String[] args) {

		Statistics.type type = Statistics.type.getByString(args[1]);

		if (type == null) {
			Arenas.tellPlayer(player,
					Language.parse("invalidstattype", args[1]));
			return true;
		}

		ArenaPlayer[] aps = Statistics.getStats(arena, type);
		String[] s = Statistics.read(aps, type);

		int i = 0;

		for (ArenaPlayer ap : aps) {
			Arenas.tellPlayer(player, ap.get().getName() + ": " + s[i++]);
			if (i > 9) {
				return true;
			}
		}

		return true;
	}

	/**
	 * check and commit edit command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player to check
	 * @return false if the command help should be displayed, true otherwise
	 */
	private static boolean parseEdit(Arena arena, Player player) {
		if (!PVPArena.hasAdminPerms(player)) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("edit")));
			return true;
		}

		arena.edit = !arena.edit;
		Arenas.tellPlayer(player,
				Language.parse("edit" + String.valueOf(arena.edit), arena.name));
		return true;
	}

	/**
	 * check if a command is a valid custom command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command to check
	 * @return false if the command help should be displayed, true otherwise
	 */
	private static boolean isCustomCommand(Arena arena, Player player,
			String cmd) {

		if (!arena.cfg.getBoolean("arenatype.flags")) {
			return false;
		}

		String type = arena.getType().equals("pumpkin") ? "pumpkin" : "flag";

		if (!cmd.endsWith(type)) {
			return false;
		}
		if (!player.getWorld().getName().equals(arena.getWorld())) {
			Arenas.tellPlayer(player,
					Language.parse("notsameworld", arena.getWorld()));
			return false;
		}
		String sName = cmd.replace(type, "");
		if (arena.paTeams.get(sName) == null) {
			return false;
		}

		Arena.regionmodify = arena.name + ":" + sName;
		Arenas.tellPlayer(player, Language.parse("toset" + type, sName));
		return true;

	}

	/**
	 * check if a given string is a valid region command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param s
	 *            the string to check
	 * @return true if the command is valid, false otherwise
	 */
	public static boolean isRegionCommand(Arena arena, String s) {
		db.i("checking region command: " + s);
		if (s.equals("exit") || s.equals("spectator")
				|| s.equals("battlefield") || s.equals("join")) {
			return true;
		}
		if (arena.getType().equals("free")) {
			if (s.equals("lounge")) {
				return true;
			}
		} else {
			for (String sName : arena.paTeams.keySet()) {
				if (s.equals(sName + "lounge")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * check if a command is a valid spawn command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command to check
	 * @return false if the command help should be displayed, true otherwise
	 */
	private static boolean isSpawnCommand(Arena arena, Player player, String cmd) {
		if (!player.getWorld().getName().equals(arena.getWorld())) {
			Arenas.tellPlayer(player,
					Language.parse("notsameworld", arena.getWorld()));
			return false;
		}

		if (cmd.startsWith("spawn") && !cmd.equals("spawn")) {

			if (arena.getType().equals("free")) {
				Spawns.setCoords(arena, player, cmd);
				Arenas.tellPlayer(player, Language.parse("setspawn", cmd));
				return true;
			} else {
				Arenas.tellPlayer(player, Language.parse("errorspawnfree", cmd));
				return false;
			}
		}

		if (cmd.contains("spawn") && !cmd.equals("spawn")) {
			String[] split = cmd.split("spawn");
			String sName = split[0];
			if (arena.paTeams.get(sName) == null)
				return false;

			Spawns.setCoords(arena, player, cmd);
			Arenas.tellPlayer(player, Language.parse("setspawn", sName));
			return true;
		}

		if (cmd.startsWith("powerup")) {
			Spawns.setCoords(arena, player, cmd);
			Arenas.tellPlayer(player, Language.parse("setspawn", cmd));
			return true;
		}
		return false;
	}

	/**
	 * check if a command is a valid lounge command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command to check
	 * @return false if the command help should be displayed, true otherwise
	 */
	private static boolean isLoungeCommand(Arena arena, Player player,
			String cmd) {
		if (!player.getWorld().getName().equals(arena.getWorld())) {
			Arenas.tellPlayer(player,
					Language.parse("notsameworld", arena.getWorld()));
			return false;
		}

		if (cmd.equalsIgnoreCase("lounge")) {
			if (arena.getType().equals("free")) {
				Spawns.setCoords(arena, player, "lounge");
				Arenas.tellPlayer(player, Language.parse("setlounge"));
				return true;
			} else {
				Arenas.tellPlayer(player, Language.parse("errorloungefree"));
				return false;
			}
		}

		if (cmd.endsWith("lounge")) {
			String color = cmd.replace("lounge", "");
			if (arena.paTeams.containsKey(color)) {
				Spawns.setCoords(arena, player, cmd);
				Arenas.tellPlayer(player, Language.parse("setlounge", color));
				return true;
			}
			Arenas.tellPlayer(player, Language.parse("invalidcmd", "506"));
			return true;
		}
		return false;
	}

	/**
	 * check and commit bet command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @param args
	 *            an array of [better],[bet]
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseBetCommand(Arena arena, Player player,
			String[] args) {
		// /pa bet [name] [amount]
		if (Players.isPartOf(arena, player)
				&& !Players.getTeam(player).equals("")) {
			Arenas.tellPlayer(player, Language.parse("betnotyours"));
			return true;
		}

		if (PVPArena.economy == null && PVPArena.eco == null)
			return true;

		Player p = Bukkit.getPlayer(args[1]);

		if ((arena.paTeams.get(args[1]) == null)
				&& (Players.getTeam(p).equals(""))) {
			Arenas.tellPlayer(player, Language.parse("betoptions"));
			return true;
		}

		double amount = 0;

		try {
			amount = Double.parseDouble(args[2]);
		} catch (Exception e) {
			Arenas.tellPlayer(player, Language.parse("invalidamount", args[2]));
			return true;
		}
		MethodAccount ma = null;
		if (PVPArena.economy != null) {
			if (!PVPArena.economy.hasAccount(player.getName())) {
				db.s("Account not found: " + player.getName());
				return true;
			}
			if (!PVPArena.economy.has(player.getName(), amount)) {
				// no money, no entry!
				Arenas.tellPlayer(
						player,
						Language.parse("notenough",
								PVPArena.economy.format(amount)));
				return true;
			}
		} else {
			ma = PVPArena.eco.getAccount(player.getName());
			if (ma == null) {
				db.s("Account not found: " + player.getName());
				return true;
			}
			if (!ma.hasEnough(amount)) {
				// no money, no entry!
				Arenas.tellPlayer(player, Language.parse("notenough",
						PVPArena.eco.format(amount)));
				return true;
			}
		}

		if (amount < arena.cfg.getDouble("money.minbet")
				|| (amount > arena.cfg.getDouble("money.maxbet"))) {
			// wrong amount!
			if (PVPArena.economy != null) {
				Arenas.tellPlayer(player, Language.parse("wrongamount",
						PVPArena.economy.format(arena.cfg
								.getDouble("money.minbet")), PVPArena.economy
								.format(arena.cfg.getDouble("money.maxbet"))));
			} else {
				Arenas.tellPlayer(player, Language
						.parse("wrongamount", PVPArena.eco.format(arena.cfg
								.getDouble("money.minbet")), PVPArena.eco
								.format(arena.cfg.getDouble("money.maxbet"))));
			}
			return true;
		}

		if (PVPArena.economy != null) {
			PVPArena.economy.withdrawPlayer(player.getName(), amount);
		} else {
			ma.subtract(amount);
		}
		Arenas.tellPlayer(player, Language.parse("betplaced", args[1]));
		Players.paPlayersBetAmount
				.put(player.getName() + ":" + args[1], amount);
		return true;
	}

	/**
	 * turn a hashmap into a pipe separated string
	 * 
	 * @param map
	 *            the input team map
	 * @return the joined and colored string
	 */
	private static String colorTeams(HashMap<String, String> map) {
		String s = "";
		for (String k : map.keySet()) {
			if (!s.equals("")) {
				s += " | ";
			}
			s += ChatColor.valueOf(map.get(k)) + k + ChatColor.WHITE;
		}
		return s;
	}

	/**
	 * turn a hashmap into a pipe separated strong
	 * 
	 * @param paRegions
	 *            the hashmap of regionname=>region
	 * @return the joined string
	 */
	private static String listRegions(HashMap<String, ArenaRegion> paRegions) {
		String s = "";
		for (ArenaRegion p : paRegions.values()) {
			if (!s.equals("")) {
				s += " | ";
			}
			s += p.name;
		}
		return s;
	}

	/**
	 * color a string based on a given boolean
	 * 
	 * @param s
	 *            the string to color
	 * @param b
	 *            true:green, false:red
	 * @return a colored string
	 */
	private static String colorVar(String s, boolean b) {
		return (b ? (ChatColor.GREEN + "") : (ChatColor.RED + "")) + s
				+ ChatColor.WHITE;
	}

	/**
	 * color a string if set
	 * 
	 * @param s
	 *            the string to color
	 * @return a colored string
	 */
	private static String colorVar(String s) {
		if (s == null || s.equals("")) {
			return colorVar("null", false);
		}
		return colorVar(s, true);
	}

	/**
	 * color an integer if bigger than 0
	 * 
	 * @param timed
	 *            the integer to color
	 * @return a colored string
	 */
	private static String colorVar(int timed) {
		return colorVar(String.valueOf(timed), timed > 0);
	}

	/**
	 * color a boolean based on value
	 * 
	 * @param b
	 *            the boolean to color
	 * @return a colored string
	 */
	private static String colorVar(boolean b) {
		return colorVar(String.valueOf(b), b);
	}

	/**
	 * display detailed arena information
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseInfo(Arena arena, Player player) {
		// TODO reorganize and update
		String type = arena.getType();
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("       Arena Information about [" + ChatColor.AQUA
				+ arena.name + ChatColor.WHITE + "]");
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("Type: " + ChatColor.AQUA + type + ChatColor.WHITE
				+ " || " + "Teams: " + colorTeams(arena.paTeams));
		player.sendMessage(colorVar("Enabled",
				arena.cfg.getBoolean("general.enabled"))
				+ " || "
				+ colorVar("Fighting", arena.fightInProgress)
				+ " || "
				+ "Wand: "
				+ Material.getMaterial(arena.cfg.getInt("setup.wand", 280))
						.toString()
				+ " || "
				+ "Timing: "
				+ colorVar(arena.cfg.getInt("goal.timed"))
				+ " || "
				+ "MaxLives: " + colorVar(arena.cfg.getInt("game.lives", 3)));
		player.sendMessage("Regionset: "
				+ colorVar(arena.name.equals(Arena.regionmodify))
				+ " || No Death: "
				+ colorVar(arena.cfg.getBoolean("game.preventDeath"))
				+ " || "
				+ "Force: "
				+ colorVar("Even",
						arena.cfg.getBoolean("join.forceEven", false))
				+ " | "
				+ colorVar("Woolhead",
						arena.cfg.getBoolean("game.woolHead", false)));
		player.sendMessage(colorVar("TeamKill",
				arena.cfg.getBoolean("game.teamKill", false))
				+ " || Team Select: "
				+ colorVar("manual", arena.cfg.getBoolean("join.manual", true))
				+ " | "
				+ colorVar("random", arena.cfg.getBoolean("join.random", true)));
		player.sendMessage("Regions: " + listRegions(arena.regions));
		player.sendMessage("TPs: exit: "
				+ colorVar(arena.cfg.getString("tp.exit", "exit"))
				+ " | death: "
				+ colorVar(arena.cfg.getString("tp.death", "spectator"))
				+ " | win: " + colorVar(arena.cfg.getString("tp.win", "old"))
				+ " | lose: " + colorVar(arena.cfg.getString("tp.lose", "old")));
		player.sendMessage(colorVar("Powerups", arena.usesPowerups)
				+ "("
				+ colorVar(arena.cfg.getString("game.powerups"))
				+ ")"
				+ " | "
				+ colorVar("randomSpawn",
						arena.cfg.getBoolean("arenatype.randomSpawn", false))
				+ " | "
				+ colorVar("refill",
						arena.cfg.getBoolean("game.refillInventory", false)));
		player.sendMessage(colorVar("Protection",
				arena.cfg.getBoolean("protection.enabled", true))
				+ ": "
				+ colorVar("Fire",
						arena.cfg.getBoolean("protection.firespread", true))
				+ " | "
				+ colorVar("Destroy",
						arena.cfg.getBoolean("protection.blockdamage", true))
				+ " | "
				+ colorVar("Place",
						arena.cfg.getBoolean("protection.blockplace", true))
				+ " | "
				+ colorVar("Ignite",
						arena.cfg.getBoolean("protection.lighter", true))
				+ " | "
				+ colorVar("Lava",
						arena.cfg.getBoolean("protection.lavafirespread", true))
				+ " | "
				+ colorVar("Explode",
						arena.cfg.getBoolean("protection.tnt", true)));
		player.sendMessage(colorVar("Check Regions",
				arena.cfg.getBoolean("periphery.checkRegions", false))
				+ ": "
				+ colorVar("Exit",
						arena.cfg.getBoolean("protection.checkExit", false))
				+ " | "
				+ colorVar("Lounges",
						arena.cfg.getBoolean("protection.checkLounges", false))
				+ " | "
				+ colorVar("Spectator", arena.cfg.getBoolean(
						"protection.checkSpectator", false)));
		player.sendMessage("JoinRange: "
				+ colorVar(arena.cfg.getInt("join.range", 0))
				+ " || Entry Fee: "
				+ colorVar(arena.cfg.getInt("money.entry", 0)) + " || Reward: "
				+ colorVar(arena.cfg.getInt("money.reward", 0)));

		return true;
	}

	/**
	 * check the arena config
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseCheck(Arena arena, Player player) {

		Debug.override = true;

		db.i("-------------------------------");
		db.i("Debug parsing Arena config for arena: " + arena);
		db.i("-------------------------------");

		Arenas.loadArena(arena.name, arena.getType());

		db.i("-------------------------------");
		db.i("Debug parsing finished!");
		db.i("-------------------------------");

		Debug.override = false;
		return true;
	}
}
