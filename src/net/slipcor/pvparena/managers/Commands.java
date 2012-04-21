package net.slipcor.pvparena.managers;

import java.util.HashMap;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.definitions.ArenaRegion;
import net.slipcor.pvparena.runnables.ArenaWarmupRunnable;

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
 * @version v0.7.10
 * 
 */

public class Commands {
	private static Debug db = new Debug(25);

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
			Arenas.tellPlayer(player, Language.parse("arenanotsetup", error),
					arena);
			return false;
		}

		if (!PVPArena.hasPerms(player, arena)) {
			Arenas.tellPlayer(player, Language.parse("permjoin"), arena);
			return false;
		}

		if (Arenas.getArenaByPlayer(player) != null) {
			Arenas.tellPlayer(player, Language.parse("alreadyjoined"), arena);
			return false;
		}

		if (player.isInsideVehicle()) {
			Arenas.tellPlayer(player, Language.parse("insidevehicle"), arena);
			return false;
		}

		if (!Arenas.checkJoin(player)) {
			Arenas.tellPlayer(player, Language.parse("notjoinregion"), arena);
			return false;
		}

		if (arena.fightInProgress) {
			if (arena.type().allowsJoinInBattle()) {
				return true;
			}
			Arenas.tellPlayer(player, Language.parse("fightinprogress"), arena);
			return false;
		}

		if (Regions.tooFarAway(arena, player)) {
			Arenas.tellPlayer(player, Language.parse("joinrange"), arena);
			return false;
		}

		if (!PVPArena.instance.getAmm().checkJoin(arena, player)) {
			return false;
		}

		if (arena.START_ID != -1) {
			Bukkit.getScheduler().cancelTask(arena.START_ID);
			db.i("player joining, cancelling start timer");
			if (!arena.cfg.getBoolean("join.onCountdown")) {
				Arenas.tellPlayer(player, Language.parse("fightinprogress"),
						arena);
				return false;
			}
		}

		return true;
	}

	/**
	 * turn a hashmap into a pipe separated string
	 * 
	 * @param arena
	 *            the input team map
	 * @return the joined and colored string
	 */
	private static String colorTeams(Arena arena) {
		String s = "";
		for (ArenaTeam team : arena.getTeams()) {
			if (!s.equals("")) {
				s += " | ";
			}
			s += team.colorize() + ChatColor.WHITE;
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
			s += p.name + " (" + p.getType().name().charAt(0) + ")";
		}
		return s;
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
						Language.parse("notsameworld", arena.getWorld()), arena);
				return false;
			}
			Spawns.setCoords(arena, player, "spectator");
			Arenas.tellPlayer(player, Language.parse("setspectator"), arena);
		} else if (cmd.equalsIgnoreCase("exit")) {
			if (!player.getWorld().getName().equals(arena.getWorld())) {
				Arenas.tellPlayer(player,
						Language.parse("notsameworld", arena.getWorld()), arena);
				return false;
			}
			Spawns.setCoords(arena, player, "exit");
			Arenas.tellPlayer(player, Language.parse("setexit"), arena);
		} else if (cmd.equalsIgnoreCase("forcestop")) {
			if (arena.fightInProgress) {
				arena.forcestop();
				Arenas.tellPlayer(player, Language.parse("forcestop"), arena);
			} else {
				Arenas.tellPlayer(player, Language.parse("nofight"), arena);
			}
		} else if (cmd.equalsIgnoreCase("set")) {
			arena.sm.list(player, 1);
		} else if (arena.type().allowsRandomSpawns()
				&& (cmd.startsWith("spawn"))) {
			if (!player.getWorld().getName().equals(arena.getWorld())) {
				Arenas.tellPlayer(player,
						Language.parse("notsameworld", arena.getWorld()), arena);
				return false;
			}
			Spawns.setCoords(arena, player, cmd);
			Arenas.tellPlayer(player, Language.parse("setspawn", cmd), arena);
		} else {
			// no random or not trying to set custom spawn
			if ((!arena.type().isLoungeCommand(player, cmd))
					&& (!arena.type().isSpawnCommand(player, cmd))
					&& (!arena.type().isCustomCommand(player, cmd))) {
				return parseJoin(arena, player);
			}
			// else: command lounge or spawn :)
		}
		return true;
	}

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
		if (arena.chatters.contains(player.getName())) {
			arena.chatters.remove(player.getName());
			Arenas.tellPlayer(player, "You now talk to the public!", arena);
		} else {
			arena.chatters.add(player.getName());
			Arenas.tellPlayer(player, "You now talk to your team!", arena);
		}
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

		Arenas.loadArena(arena.name, arena.type().getName());

		db.i("-------------------------------");
		db.i("Debug parsing finished!");
		db.i("-------------------------------");

		Debug.override = false;
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
			Arenas.tellPlayer(player, Language.parse("arenadisabled"), arena);
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
			} else if (args[0].equalsIgnoreCase("watch")
					|| args[0].equalsIgnoreCase("spectate")) {
				return parseSpectate(arena, player);
			} else if (args[0].equalsIgnoreCase("users")) {
				return parseUsers(arena, player);
			} else if (args[0].equalsIgnoreCase("region")) {
				return parseRegion(arena, player);
			} else if (Teams.getTeam(arena, args[0]) != null) {
				return parseJoinTeam(arena, player, args[0]);
			} else if (PVPArena.hasAdminPerms(player)
					|| (PVPArena.hasCreatePerms(player, arena))) {
				return parseAdminCommand(arena, player, args[0]);
			} else {
				return parseJoin(arena, player);
			}
		} else if ((args.length == 2 || args.length == 3)
				&& args[0].equalsIgnoreCase("stats")) {
			return parseStats(arena, player, args);
		} else if (args.length == 2 && args[0].equalsIgnoreCase("borders")) {
			ArenaRegion region = arena.regions.get(args[1]);
			if (region == null) {
				Arenas.tellPlayer(player, "Region unknown: " + args[1], arena);
				return true;
			}
			region.showBorder(player);
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

		if (PVPArena.instance.getAmm().parseCommand(arena, player, args)) {
			return true;
		}

		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			Arenas.tellPlayer(player,
					Language.parse("nopermto", Language.parse("admin")), arena);
			return false;
		}

		if (args[0].equalsIgnoreCase("remove")) {
			// pa [name] remove [spawnname]
			arena.cfg.set("spawns." + args[1], null);
			arena.cfg.save();
			Arenas.tellPlayer(player, Language.parse("spawnremoved", args[1]),
					arena);
			return true;
		}

		if (!arena.type().isRegionCommand(args[1])) {
			Arenas.tellPlayer(player, Language.parse("invalidcmd", "504"),
					arena);
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
								Language.parse("regionremoved"), arena);
					} else {
						Arenas.tellPlayer(player,
								Language.parse("regionnotremoved"), arena);
					}
					return true;
				}

				// pa [name] region [regionname] {cuboid/sphere}
				if (Arena.regionmodify.equals("")) {
					Arenas.tellPlayer(player,
							Language.parse("regionnotbeingset", arena.name),
							arena);
					return true;
				}

				if (arena.pos1 == null || arena.pos2 == null) {
					Arenas.tellPlayer(player, Language.parse("select2"), arena);
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
						s += ",spheric";
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
				Arenas.tellPlayer(player, Language.parse("regionsaved"), arena);
				return true;

			}
		}

		if (args.length != 3) {
			Arenas.tellPlayer(player, Language.parse("invalidcmd", "505"),
					arena);
			return false;
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
					Language.parse("nopermto", Language.parse("edit")), arena);
			return true;
		}

		arena.edit = !arena.edit;
		Arenas.tellPlayer(
				player,
				Language.parse("edit" + String.valueOf(arena.edit), arena.name),
				arena);
		return true;
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
		String type = arena.type().getName();
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("       Arena Information about [" + ChatColor.AQUA
				+ arena.name + ChatColor.WHITE + "]");
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("Type: " + ChatColor.AQUA + type + ChatColor.WHITE
				+ " || " + "Teams: " + colorTeams(arena));
		player.sendMessage(StringParser.colorVar("Enabled",
				arena.cfg.getBoolean("general.enabled"))
				+ " || "
				+ StringParser.colorVar("Fighting", arena.fightInProgress)
				+ " || "
				+ "Wand: "
				+ Material.getMaterial(arena.cfg.getInt("setup.wand", 280))
						.toString()
				+ " || "
				+ "Timing: "
				+ StringParser.colorVar(arena.cfg.getInt("goal.timed"))
				+ " || "
				+ "MaxLives: "
				+ StringParser.colorVar(arena.cfg.getInt("game.lives", 3)));
		player.sendMessage("Regionset: "
				+ StringParser.colorVar(arena.name.equals(Arena.regionmodify))
				+ " || No Death: "
				+ StringParser.colorVar(arena.cfg
						.getBoolean("game.preventDeath"))
				+ " || "
				+ "Force: "
				+ StringParser.colorVar("Even",
						arena.cfg.getBoolean("join.forceEven", false))
				+ " | "
				+ StringParser.colorVar("Woolhead",
						arena.cfg.getBoolean("game.woolHead", false)));
		player.sendMessage(StringParser.colorVar("TeamKill",
				arena.cfg.getBoolean("game.teamKill", false))
				+ " || Team Select: "
				+ StringParser.colorVar("manual",
						arena.cfg.getBoolean("join.manual", true))
				+ " | "
				+ StringParser.colorVar("random",
						arena.cfg.getBoolean("join.random", true)));
		player.sendMessage("Regions: " + listRegions(arena.regions));
		player.sendMessage("TPs: exit: "
				+ StringParser.colorVar(arena.cfg.getString("tp.exit", "exit"))
				+ " | death: "
				+ StringParser.colorVar(arena.cfg.getString("tp.death",
						"spectator")) + " | win: "
				+ StringParser.colorVar(arena.cfg.getString("tp.win", "old"))
				+ " | lose: "
				+ StringParser.colorVar(arena.cfg.getString("tp.lose", "old")));
		PVPArena.instance.getAmm().parseInfo(arena, player);
		player.sendMessage(StringParser.colorVar("Protection",
				arena.cfg.getBoolean("protection.enabled", true))
				+ ": "
				+ StringParser.colorVar("Fire",
						arena.cfg.getBoolean("protection.firespread", true))
				+ " | "
				+ StringParser.colorVar("Destroy",
						arena.cfg.getBoolean("protection.blockdamage", true))
				+ " | "
				+ StringParser.colorVar("Place",
						arena.cfg.getBoolean("protection.blockplace", true))
				+ " | "
				+ StringParser.colorVar("Ignite",
						arena.cfg.getBoolean("protection.lighter", true))
				+ " | "
				+ StringParser.colorVar("Lava",
						arena.cfg.getBoolean("protection.lavafirespread", true))
				+ " | "
				+ StringParser.colorVar("Explode",
						arena.cfg.getBoolean("protection.tnt", true)));
		player.sendMessage(StringParser.colorVar("Check Regions",
				arena.cfg.getBoolean("periphery.checkRegions", false))
				+ ": "
				+ StringParser.colorVar("Exit",
						arena.cfg.getBoolean("protection.checkExit", false))
				+ " | "
				+ StringParser.colorVar("Lounges",
						arena.cfg.getBoolean("protection.checkLounges", false))
				+ " | "
				+ StringParser.colorVar("Spectator", arena.cfg.getBoolean(
						"protection.checkSpectator", false)));
		player.sendMessage("JoinRange: "
				+ StringParser.colorVar(arena.cfg.getInt("join.range", 0))
				+ " || Entry Fee: "
				+ StringParser.colorVar(arena.cfg.getInt("money.entry", 0))
				+ " || Reward: "
				+ StringParser.colorVar(arena.cfg.getInt("money.reward", 0))
				+ " || "
				+ StringParser.colorVar("refill",
						arena.cfg.getBoolean("game.refillInventory", false)));

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
			Arenas.tellPlayer(player, Language.parse("selectteam"), arena);
			return true;
		}

		if (Teams.calcFreeTeam(arena) == null
				|| ((arena.cfg.getInt("ready.max") > 0) && (arena.cfg
						.getInt("ready.max") <= Teams
						.countPlayersInTeams(arena)))) {

			Arenas.tellPlayer(player, Language.parse("arenafull"), arena);
			return true;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		
		if (arena.cfg.getInt("join.warmup")>0) {
			if (ap.getStatus().equals(Status.EMPTY)) {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PVPArena.instance, 
						new ArenaWarmupRunnable(arena, ap, null, false),
						20L * arena.cfg.getInt("join.warmup"));
				Arenas.tellPlayer(player, Language.parse("Warming up... stand by..."));
				return true;
			}
		}
		
		arena.prepare(player, false, false);
		arena.lives.put(player.getName(), arena.cfg.getInt("game.lives", 3));

		Teams.choosePlayerTeam(arena, player);
		Inventories.prepareInventory(arena, player);

		PVPArena.instance.getAmm().parseJoin(
				arena,
				player,
				Teams.getTeam(arena, ap)
						.colorize());
		// process auto classing
		String autoClass = arena.cfg.getString("ready.autoclass");
		if (autoClass != null && !autoClass.equals("none")) {
			if (arena.classExists(autoClass)) {
				arena.forceChooseClass(player, null, autoClass);
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
			Arenas.tellPlayer(player, Language.parse("notselectteam"), arena);
			return true;
		}

		if (arena.cfg.getInt("ready.max") > 0
				&& arena.cfg.getInt("ready.max") <= Teams
						.countPlayersInTeams(arena)) {

			Arenas.tellPlayer(player, Language.parse("teamfull",
					Teams.getTeam(arena, sTeam).colorize()), arena);
			return true;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		
		if (arena.cfg.getInt("join.warmup")>0) {
			if (ap.getStatus().equals(Status.EMPTY)) {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PVPArena.instance, 
						new ArenaWarmupRunnable(arena, ap, sTeam, false),
						20L * arena.cfg.getInt("join.warmup"));
				Arenas.tellPlayer(player, Language.parse("Warming up... stand by..."));
				return true;
			}
		}

		arena.prepare(player, false, false);
		arena.lives.put(player.getName(), arena.cfg.getInt("game.lives", 3));

		arena.tpPlayerToCoordName(player, sTeam + "lounge");

		ArenaTeam team = Teams.getTeam(arena, sTeam);

		team.add(ap);

		Inventories.prepareInventory(arena, player);
		String coloredTeam = team.colorize();

		PVPArena.instance.getAmm().parseJoin(arena, player, coloredTeam);

		Arenas.tellPlayer(player, Language.parse("youjoined", coloredTeam),
				arena);
		arena.tellEveryoneExcept(player,
				Language.parse("playerjoined", player.getName(), coloredTeam));

		// process auto classing
		String autoClass = arena.cfg.getString("ready.autoclass");
		if (autoClass != null && !autoClass.equals("none")) {
			if (arena.classExists(autoClass)) {
				arena.forceChooseClass(player, null, autoClass);
			} else {
				db.w("autoclass selected that does not exist: " + autoClass);
			}
		}

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
		if (Teams.countPlayersInTeams(arena) < 1) {
			Arenas.tellPlayer(player, Language.parse("noplayer"), arena);
			return true;
		}
		String plrs = Teams.getTeamStringList(arena);
		Arenas.tellPlayer(player, Language.parse("players") + ": " + plrs,
				arena);
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
			Arenas.tellPlayer(
					player,
					Language.parse("regionalreadybeingset", Arena.regionmodify),
					arena);
			return true;
		}
		Arena.regionmodify = arena.name;
		Arenas.tellPlayer(player, Language.parse("regionset"), arena);
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
			Arenas.tellPlayer(player, Language.parse("arenanotsetup", error),
					arena);
			return true;
		}
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		ArenaTeam team = Teams.getTeam(arena, ap);
		if (team != null) {
			Arenas.tellPlayer(player, Language.parse("alreadyjoined"), arena);
			return true;
		}
		if (Regions.tooFarAway(arena, player)) {
			Arenas.tellPlayer(player, Language.parse("joinrange"), arena);
			return true;
		}
		
		if (arena.cfg.getInt("join.warmup")>0) {
			if (ap.getStatus().equals(Status.EMPTY)) {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PVPArena.instance, 
						new ArenaWarmupRunnable(arena, ap, null, true),
						20L * arena.cfg.getInt("join.warmup"));
				Arenas.tellPlayer(player, Language.parse("Warming up... stand by..."));
				return true;
			}
		}
		
		arena.prepare(player, true, false);
		ap.setArena(arena);
		arena.tpPlayerToCoordName(player, "spectator");
		Inventories.prepareInventory(arena, player);
		Arenas.tellPlayer(player, Language.parse("specwelcome"), arena);
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
	 *            the array {"stats", [stattype], {asc/desc}}
	 * @return false if the command help should be displayed, true otherwise
	 */
	private static boolean parseStats(Arena arena, Player player, String[] args) {

		Statistics.type type = Statistics.type.getByString(args[1]);

		if (type == null) {
			Arenas.tellPlayer(player,
					Language.parse("invalidstattype", args[1]), arena);
			return true;
		}

		ArenaPlayer[] aps = Statistics.getStats(arena, type);
		String[] s = Statistics.read(aps, type);

		int i = 0;

		for (ArenaPlayer ap : aps) {
			Arenas.tellPlayer(player, ap.get().getName() + ": " + s[i++], arena);
			if (i > 9) {
				return true;
			}
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
					Language.parse("nopermto", Language.parse(string)), arena);
			return true;
		}
		arena.cfg.set("general.enabled", string.equals("enabled"));
		arena.cfg.save();
		Arenas.tellPlayer(player, Language.parse(string), arena);
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

		Arenas.tellPlayer(player, Language.parse("top5win"), arena);

		int limit = 5;

		for (ArenaPlayer ap : players) {
			if (limit-- < 1) {
				break;
			}
			Arenas.tellPlayer(player, ap.get().getName() + ": " + ap.wins + " "
					+ Language.parse("wins"), arena);
		}

		Arenas.tellPlayer(player, "------------", arena);
		Arenas.tellPlayer(player, Language.parse("top5lose"), arena);

		players = Statistics.getStats(arena, Statistics.type.LOSSES);
		for (ArenaPlayer ap : players) {
			if (limit-- < 1) {
				break;
			}
			Arenas.tellPlayer(player, ap.get().getName() + ": " + ap.losses
					+ " " + Language.parse("losses"), arena);
		}

		return true;
	}
}
