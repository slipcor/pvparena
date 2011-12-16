/*
 * arena class
 * 
 * author: slipcor
 * 
 * version: v0.4.3 - max / min bet
 * 
 * history:
 * 
 *     v0.4.1 - command manager, arena information and arena config check
 */
package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import net.slipcor.pvparena.PARegion;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;

public class CommandManager {
	private static DebugManager db = new DebugManager();

	public static boolean parseJoin(Arena arena, Player player) {
		// just /pa or /pvparena
		String error = ConfigManager.isSetup(arena);
		if (error != null) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("arenanotsetup", error));
			return true;
		}
		if (!PVPArena.instance.hasPerms(player)) {
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("permjoin"));
			return true;
		}
		if (!arena.randomlySelectTeams) {
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("selectteam"));
			return true;
		}
		if (arena.savedPlayerVars.containsKey(player)) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("alreadyjoined"));
			return true;
		}
		if (arena.fightInProgress) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("fightinprogress"));
			return true;
		}
		if (arena.tooFarAway(player)) {
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("joinrange"));
			return true;
		}
		if (PVPArena.instance.getMethod() != null) {
			MethodAccount ma = PVPArena.instance.getMethod().getAccount(
					player.getName());
			if (ma == null) {
				db.s("Account not found: " + player.getName());
				return true;
			}
			if (!ma.hasEnough(arena.entryFee)) {
				// no money, no entry!
				ArenaManager.tellPlayer(player, PVPArena.lang.parse(
						"notenough",
						PVPArena.instance.getMethod().format(arena.entryFee)));
				return true;
			}
		}

		arena.prepare(player);
		arena.playerManager.setLives(player, (byte) arena.maxLives);
		if ((PVPArena.instance.getMethod() != null) && (arena.entryFee > 0)) {
			MethodAccount ma = PVPArena.instance.getMethod().getAccount(
					player.getName());
			ma.subtract(arena.entryFee);
		}
		arena.chooseColor(player);
		arena.prepareInventory(player);
		return true;
	}

	public static boolean parseJoinTeam(Arena arena, Player player, String sTeam) {

		// /pa [team] or /pvparena [team]
		String error = ConfigManager.isSetup(arena);
		if (error != null) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("arenanotsetup", error));
			return true;
		}
		if (!PVPArena.instance.hasPerms(player)) {
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("permjoin"));
			return true;
		}
		if (!(arena.manuallySelectTeams)) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("notselectteam"));
			return true;
		}
		if (arena.savedPlayerVars.containsKey(player)) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("alreadyjoined"));
			return true;
		}
		if (arena.fightInProgress) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("fightinprogress"));
			return true;
		}
		if (arena.tooFarAway(player)) {
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("joinrange"));
			return true;
		}

		if (PVPArena.instance.getMethod() != null) {
			MethodAccount ma = PVPArena.instance.getMethod().getAccount(
					player.getName());
			if (ma == null) {
				db.s("Account not found: " + player.getName());
				return true;
			}
			if (!ma.hasEnough(arena.entryFee)) {
				// no money, no entry!
				ArenaManager.tellPlayer(player, PVPArena.lang.parse(
						"notenough",
						PVPArena.instance.getMethod().format(arena.entryFee)));
				return true;
			}
		}

		arena.prepare(player);
		arena.playerManager.setLives(player, (byte) arena.maxLives);

		if ((PVPArena.instance.getMethod() != null) && (arena.entryFee > 0)) {
			MethodAccount ma = PVPArena.instance.getMethod().getAccount(
					player.getName());
			ma.subtract(arena.entryFee);
		}

		arena.tpPlayerToCoordName(player, sTeam + "lounge");
		arena.playerManager.setTeam(player, sTeam);
		ArenaManager.tellPlayer(
				player,
				PVPArena.lang.parse("youjoined",
						ChatColor.valueOf(arena.paTeams.get(sTeam)) + sTeam));
		arena.playerManager.tellEveryoneExcept(
				player,
				PVPArena.lang.parse("playerjoined", player.getName(),
						ChatColor.valueOf(arena.paTeams.get(sTeam)) + sTeam));
		return true;
	}

	public static boolean parseToggle(Arena arena, Player player, String string) {
		if (!PVPArena.instance.hasAdminPerms(player)) {
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("nopermto",
					PVPArena.lang.parse(string)));
			return true;
		}
		arena.enabled = string.equals("enabled");
		ArenaManager.tellPlayer(player, PVPArena.lang.parse(string));
		return true;
	}

	public static boolean parseReload(Player player) {

		if (!PVPArena.instance.hasAdminPerms(player)) {
			ArenaManager.tellPlayer(
					player,
					PVPArena.lang.parse("nopermto",
							PVPArena.lang.parse("reload")));
			return true;
		}
		PVPArena.instance.load_config();
		ArenaManager.tellPlayer(player, PVPArena.lang.parse("reloaded"));
		return true;
	}

	public static boolean parseList(Arena arena, Player player) {
		if (arena.playerManager.countPlayersInTeams() < 1) {
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("noplayer"));
			return true;
		}
		String plrs = arena.playerManager.getTeamStringList(arena.paTeams);
		ArenaManager.tellPlayer(player, PVPArena.lang.parse("players") + ": "
				+ plrs);
		return true;
	}

	public static boolean parseWatch(Arena arena, Player player) {
		String error = ConfigManager.isSetup(arena);
		if (error != null) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("arenanotsetup", error));
			return true;
		}
		if (!arena.playerManager.getTeam(player).equals("")) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("alreadyjoined"));
			return true;
		}
		arena.tpPlayerToCoordName(player, "spectator");
		ArenaManager.tellPlayer(player, PVPArena.lang.parse("specwelcome"));
		return true;
	}

	public static boolean parseTeams(Arena arena, Player player) {

		String team[] = StatsManager.getTeamStats(arena).split(";");
		int i = 0;
		for (String sTeam : arena.paTeams.keySet())
			player.sendMessage(PVPArena.lang.parse("teamstat",
					ChatColor.valueOf(arena.paTeams.get(sTeam)) + sTeam,
					team[i++], team[i++]));
		return true;
	}

	public static boolean parseUsers(Arena arena, Player player) {
		// wins are suffixed with "_"
		Map<String, Integer> players = StatsManager.getPlayerStats(arena);

		int wcount = 0;

		for (String name : players.keySet())
			if (name.endsWith("_"))
				wcount++;

		String[][] wins = new String[wcount][2];
		String[][] losses = new String[players.size() - wcount][2];
		int iw = 0;
		int il = 0;

		for (String name : players.keySet()) {
			if (name.endsWith("_")) {
				// playername_ => win
				wins[iw][0] = name.substring(0, name.length() - 1);
				wins[iw++][1] = String.valueOf(players.get(name));
			} else {
				// playername => lose
				losses[il][0] = name;
				losses[il++][1] = String.valueOf(players.get(name));
			}
		}
		wins = ArenaManager.sort(wins);
		losses = ArenaManager.sort(losses);
		ArenaManager.tellPlayer(player, PVPArena.lang.parse("top5win"));

		for (int w = 0; w < wins.length && w < 5; w++) {
			ArenaManager.tellPlayer(player, wins[w][0] + ": " + wins[w][1]
					+ " " + PVPArena.lang.parse("wins"));
		}

		ArenaManager.tellPlayer(player, "------------");
		ArenaManager.tellPlayer(player, PVPArena.lang.parse("top5lose"));

		for (int l = 0; l < losses.length && l < 5; l++) {
			ArenaManager.tellPlayer(player, losses[l][0] + ": " + losses[l][1]
					+ " " + PVPArena.lang.parse("losses"));
		}
		return true;
	}

	public static boolean parseRegion(Arena arena, Player player) {
		// /pa [name] region
		if (!Arena.regionmodify.equals("")) {
			ArenaManager.tellPlayer(player, PVPArena.lang.parse(
					"regionalreadybeingset", Arena.regionmodify));
			return true;
		}
		Arena.regionmodify = arena.name;
		ArenaManager.tellPlayer(player, PVPArena.lang.parse("regionset"));
		return true;
	}

	public static boolean parseAdminCommand(Arena arena, Player player,
			String cmd) {

		db.i("parsing admin command: " + cmd);
		if (cmd.equalsIgnoreCase("spectator")) {
			arena.setCoords(player, "spectator");
			ArenaManager
					.tellPlayer(player, PVPArena.lang.parse("setspectator"));
		} else if (cmd.equalsIgnoreCase("exit")) {
			arena.setCoords(player, "exit");
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("setexit"));
		} else if (cmd.equalsIgnoreCase("forcestop")) {
			if (arena.fightInProgress) {
				arena.forcestop();
				ArenaManager.tellPlayer(player,
						PVPArena.lang.parse("forcestop"));
			} else {
				ArenaManager.tellPlayer(player, PVPArena.lang.parse("nofight"));
			}
		} else if (cmd.equalsIgnoreCase("forcestop")) {
			if (arena.fightInProgress) {
				arena.forcestop();
				ArenaManager.tellPlayer(player,
						PVPArena.lang.parse("forcestop"));
			} else {
				ArenaManager.tellPlayer(player, PVPArena.lang.parse("nofight"));
			}
		} else if (arena.randomSpawn && (cmd.startsWith("spawn"))) {
			arena.setCoords(player, cmd);
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("setspawn", cmd));
		} else {
			// no random or not trying to set custom spawn
			if ((!isLoungeCommand(arena, player, cmd))
					&& (!isSpawnCommand(arena, player, cmd))
					&& (!isCustomCommand(arena, player, cmd))) {
				return CommandManager.parseJoin(arena, player);
			}
			// else: command lounge or spawn :)
		}
		return true;
	}

	private static boolean isCustomCommand(Arena arena, Player player,
			String cmd) {

		if ((arena.getType().equals("ctf")) && cmd.endsWith("flag")) {
			String sName = cmd.replace("flag", "");
			if (arena.paTeams.get(sName) == null)
				return false;

			arena.setCoords(player, cmd);
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("setflag", sName));
			return true;
		}
		return false;
	}

	/*
	 * returns "is spawn-set command"
	 */
	private static boolean isSpawnCommand(Arena arena, Player player, String cmd) {

		if (arena.getType().equals("free")) {

			if (cmd.startsWith("spawn")) {
				arena.setCoords(player, cmd);
				ArenaManager.tellPlayer(player,
						PVPArena.lang.parse("setspawn", cmd));
				return true;
			}
			return false;
		}

		if (cmd.contains("spawn")) {
			String[] split = cmd.split("spawn");
			String sName = split[0];
			if (arena.paTeams.get(sName) == null)
				return false;

			arena.setCoords(player, cmd);
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("setspawn", sName));
			return true;
		}
		return false;
	}

	/*
	 * returns "is lounge-set command"
	 */
	private static boolean isLoungeCommand(Arena arena, Player player,
			String cmd) {

		if (arena.getType().equals("free")) {
			if (cmd.equalsIgnoreCase("lounge")) {
				arena.setCoords(player, "lounge");
				ArenaManager.tellPlayer(player,
						PVPArena.lang.parse("setlounge"));
				return true;
			}
			return false;
		}

		if (cmd.endsWith("lounge")) {
			String color = cmd.replace("lounge", "");
			if (arena.paTeams.containsKey(color)) {
				arena.setCoords(player, cmd);
				ArenaManager.tellPlayer(player,
						PVPArena.lang.parse("setlounge", color));
				return true;
			}
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("invalidcmd", "506"));
			return true;
		}
		return false;
	}

	public static boolean parseBetCommand(Arena arena, Player player,
			String[] args) {
		// /pa bet [name] [amount]
		if (arena.playerManager.existsPlayer(player)
				&& !arena.playerManager.getTeam(player).equals("")) {
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("betnotyours"));
			return true;
		}

		if (PVPArena.instance.getMethod() == null)
			return true;

		Player p = Bukkit.getPlayer(args[1]);

		if ((arena.paTeams.get(args[1]) == null)
				&& (arena.playerManager.getTeam(p).equals(""))) {
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("betoptions"));
			return true;
		}

		double amount = 0;

		try {
			amount = Double.parseDouble(args[2]);
		} catch (Exception e) {
			ArenaManager.tellPlayer(player,
					PVPArena.lang.parse("invalidamount", args[2]));
			return true;
		}
		MethodAccount ma = PVPArena.instance.getMethod().getAccount(
				player.getName());
		if (ma == null) {
			db.s("Account not found: " + player.getName());
			return true;
		}
		if (!ma.hasEnough(amount)) {
			// no money, no entry!
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("notenough",
					PVPArena.instance.getMethod().format(amount)));
			return true;
		}

		if (amount < arena.minbet
				|| (arena.maxbet > 0 && amount > arena.maxbet)) {
			// wrong amount!
			ArenaManager.tellPlayer(player, PVPArena.lang.parse("wrongamount",
					PVPArena.instance.getMethod().format(arena.minbet),
					PVPArena.instance.getMethod().format(arena.maxbet)));
			return true;
		}

		ma.subtract(amount);
		ArenaManager.tellPlayer(player,
				PVPArena.lang.parse("betplaced", args[1]));
		arena.playerManager.paPlayersBetAmount.put(player.getName() + ":"
				+ args[1], amount);
		return true;
	}

	/*
	 * info command methods
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

	private static String listRegions(HashMap<String, PARegion> paTeams) {
		String s = "";
		for (PARegion p : paTeams.values()) {
			if (!s.equals("")) {
				s += " | ";
			}
			s += p.name;
		}
		return s;
	}

	private static String colorVar(String s, boolean b) {
		return (b ? (ChatColor.GREEN + "") : (ChatColor.RED + "")) + s
				+ ChatColor.WHITE;
	}

	private static String colorVar(String s) {
		if (s == null || s.equals("")) {
			return colorVar("null", false);
		}
		return colorVar(s, true);
	}

	private static String colorVar(int timed) {
		return colorVar(String.valueOf(timed), timed > 0);
	}

	private static String colorVar(boolean b) {
		return colorVar(String.valueOf(b), b);
	}

	public static boolean parseInfo(Arena arena, Player player) {
		String type = arena.getType();
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("       Arena Information about [" + ChatColor.AQUA
				+ arena.name + ChatColor.WHITE + "]");
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("Type: " + ChatColor.AQUA + type + ChatColor.WHITE
				+ " || " + "Teams: " + colorTeams(arena.paTeams));
		player.sendMessage(colorVar("Enabled", arena.enabled) + " || "
				+ colorVar("Fighting", arena.fightInProgress) + " || "
				+ "Wand: " + Material.getMaterial(arena.wand).toString()
				+ " || " + "Timing: " + colorVar(arena.timed) + " || "
				+ "MaxLives: " + colorVar(arena.maxLives));
		player.sendMessage("Regionset: "
				+ colorVar(arena.name.equals(Arena.regionmodify))
				+ " || No Death: " + colorVar(arena.preventDeath) + " || "
				+ "Force: " + colorVar("Even", arena.forceEven) + " | "
				+ colorVar("Woolhead", arena.forceWoolHead));
		player.sendMessage(colorVar("TeamKill", arena.teamKilling)
				+ " || Team Select: "
				+ colorVar("manual", arena.manuallySelectTeams) + " | "
				+ colorVar("random", arena.randomlySelectTeams));
		player.sendMessage("Regions: " + listRegions(arena.regions));
		player.sendMessage("TPs: exit: " + colorVar(arena.sTPexit)
				+ " | death: " + colorVar(arena.sTPdeath) + " | win: "
				+ colorVar(arena.sTPwin) + " | lose: "
				+ colorVar(arena.sTPlose));
		player.sendMessage(colorVar("Powerups", arena.usesPowerups) + "("
				+ colorVar(arena.powerupTrigger) + ")" + " | "
				+ colorVar("randomSpawn", arena.randomSpawn));
		player.sendMessage(colorVar("Protection", arena.usesProtection) + ": "
				+ colorVar("Fire", arena.disableAllFireSpread) + " | "
				+ colorVar("Destroy", arena.disableBlockDamage) + " | "
				+ colorVar("Place", arena.disableBlockPlacement) + " | "
				+ colorVar("Ignite", arena.disableIgnite) + " | "
				+ colorVar("Lava", arena.disableLavaFireSpread) + " | "
				+ colorVar("Explode", arena.disableTnt));
		player.sendMessage(colorVar("Check Regions", arena.checkRegions) + ": "
				+ colorVar("Exit", arena.checkExitRegion) + " | "
				+ colorVar("Lounges", arena.checkLoungesRegion) + " | "
				+ colorVar("Spectator", arena.checkSpectatorRegion));
		player.sendMessage("JoinRange: " + colorVar(arena.joinRange)
				+ " || Entry Fee: " + colorVar(arena.entryFee) + " || Reward: "
				+ colorVar(arena.rewardAmount));

		return true;
	}

	/*
	 * check command methods
	 */

	public static boolean parseCheck(Arena arena, Player player) {
		boolean b = DebugManager.active;
		DebugManager.active = true;

		db.i("-------------------------------");
		db.i("Debug parsing Arena config for arena: " + arena);
		db.i("-------------------------------");

		ArenaManager.loadArena(arena.name, arena.getType());

		db.i("-------------------------------");
		db.i("Debug parsing finished!");
		db.i("-------------------------------");

		DebugManager.active = b;
		return true;
	}
}
