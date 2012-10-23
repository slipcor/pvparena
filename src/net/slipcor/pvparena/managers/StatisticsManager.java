package net.slipcor.pvparena.managers;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PADeathEvent;
import net.slipcor.pvparena.events.PAExitEvent;
import net.slipcor.pvparena.events.PAKillEvent;

/**
 * <pre>Statistics Manager class</pre>
 * 
 * Provides static methods to manage Statistics
 * 
 * @author slipcor
 * 
 * @version v0.9.5
 */

public class StatisticsManager {
	public static final Debug db = new Debug(28);
	private static File players;
	private static YamlConfiguration config;

	public static enum type {
		WINS("matches won"), LOSSES("matches lost"), KILLS("kills"), DEATHS(
				"deaths"), MAXDAMAGE("max damage dealt"), MAXDAMAGETAKE(
				"max damage taken"), DAMAGE("full damage dealt"), DAMAGETAKE(
				"full damage taken"), NULL("player name");

		private final String fullName;

		type(String s) {
			fullName = s;
		}

		/**
		 * return the next stat type
		 * 
		 * @param tType
		 *            the type
		 * @return the next type
		 */
		public static type next(type tType) {
			type[] types = type.values();
			int ord = tType.ordinal();
			if (ord >= types.length - 2) {
				return types[0];
			}
			return types[ord + 1];
		}

		/**
		 * return the previous stat type
		 * 
		 * @param tType
		 *            the type
		 * @return the previous type
		 */
		public static type last(type tType) {
			type[] types = type.values();
			int ord = tType.ordinal();
			if (ord <= 0) {
				return types[types.length - 2];
			}
			return types[ord - 1];
		}

		/**
		 * return the full stat name
		 */
		public String getName() {
			return this.fullName;
		}

		/**
		 * get the stat type by name
		 * 
		 * @param string
		 *            the name to find
		 * @return the type if found, null otherwise
		 */
		public static type getByString(String string) {
			for (type t : type.values()) {
				if (t.name().equals(string.toUpperCase())) {
					return t;
				}
			}
			return null;
		}
	}

	/**
	 * commit damage
	 * 
	 * @param arena
	 *            the arena where that happens
	 * @param e
	 *            an eventual attacker
	 * @param defender
	 *            the attacked player
	 * @param dmg
	 *            the damage value
	 */
	public static void damage(Arena arena, Entity e, Player defender, int dmg) {

		db.i("adding damage to player " + defender.getName());
		

		if ((e != null) && (e instanceof Player)) {
			Player attacker = (Player) e;
			db.i("attacker is player: " + attacker.getName());
			if (arena.hasPlayer(attacker)) {
				db.i("attacker is in the arena, adding damage!");
				ArenaPlayer p = ArenaPlayer.parsePlayer(attacker.getName());
				int maxdamage = p.getStatistics(arena).getStat(type.MAXDAMAGE);
				p.getStatistics(arena).incStat(type.DAMAGE, dmg);
				if (dmg > maxdamage) {
					p.getStatistics(arena).setStat(type.MAXDAMAGE, dmg);
				}
			}
		}
		ArenaPlayer p = ArenaPlayer.parsePlayer(defender.getName());

		int maxdamage = p.getStatistics(arena).getStat(type.MAXDAMAGETAKE);
		p.getStatistics(arena).incStat(type.DAMAGETAKE, dmg);
		if (dmg > maxdamage) {
			p.getStatistics(arena).setStat(type.MAXDAMAGETAKE, dmg);
		}
	}

	/**
	 * decide if a pair has to be sorted
	 * 
	 * @param aps
	 *            the ArenaPlayer array
	 * @param pos
	 *            the position to check
	 * @param sortBy
	 *            the type to sort by
	 * @param desc
	 *            descending order?
	 * @param global 
	 * @return true if pair has to be sorted, false otherwise
	 */
	private static boolean decide(ArenaPlayer[] aps, int pos, type sortBy,
			boolean desc, boolean global) {
		int a = 0;
		int b = 0;
		
		a = aps[pos].getStatistics(aps[pos].getArena()).getStat(sortBy);
		b = aps[pos + 1].getStatistics(aps[pos].getArena()).getStat(sortBy);

		if (global) {
			a = aps[pos].getTotalStatistics(sortBy);
			b = aps[pos + 1].getTotalStatistics(sortBy);
		}

		return desc ? (a < b) : (a > b);
	}

	/**
	 * get a set of arena players sorted by type
	 * 
	 * @param a
	 *            the arena to check
	 * @param sortBy
	 *            the type to sort
	 * @return an array of ArenaPlayer
	 */
	public static ArenaPlayer[] getStats(Arena a, type sortBy) {
		return getStats(a, sortBy, true);
	}

	/**
	 * get a set of arena players sorted by type
	 * 
	 * @param a
	 *            the arena to check
	 * @param sortBy
	 *            the type to sort
	 * @param desc
	 *            should it be sorted descending?
	 * @return an array of ArenaPlayer
	 */
	public static ArenaPlayer[] getStats(Arena a, type sortBy, boolean desc) {
		db.i("getting stats: " + (a == null?"global":a.getName()) + " sorted by " + sortBy + " "
				+ (desc ? "desc" : "asc"));
		
		int count = (a == null)?ArenaPlayer.countPlayers():TeamManager.countPlayersInTeams(a);
		
		ArenaPlayer[] aps = new ArenaPlayer[count];
		
		int i = 0;
		if (a == null) {
			for (ArenaPlayer p : ArenaPlayer.getAllArenaPlayers()) {
				aps[i++] = p;
			}
		} else {
			for (ArenaPlayer p : a.getFighters()) {
				aps[i++] = p;
			}
		}

		sortBy(aps, sortBy, desc, a == null);

		return aps;
	}

	/**
	 * get the type by the sign headline
	 * 
	 * @param line
	 *            the line to determine the type
	 * @return the Statistics type
	 */
	public static type getTypeBySignLine(String line) {
		if (!line.startsWith("[PA]")) {
			return type.NULL;
		}
		line = line.replace("[PA]", "").toUpperCase();

		for (type t : type.values()) {
			if (t.name().equals(line)) {
				return t;
			}
		}
		return type.NULL;
	}

	public static void initialize() {
		if (!PVPArena.instance.getConfig().getBoolean("stats")) {
			return;
		}
		config = new YamlConfiguration();
		players = new File(PVPArena.instance.getDataFolder(), "players.yml");
		if (!players.exists()) {
			try {
				players.createNewFile();
				Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.STATS_FILE_DONE));
			} catch (Exception e) {
				Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_STATS_FILE));
				e.printStackTrace();
			}
		}
		
		try {
			config.load(players);
		} catch (Exception e) {
			Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_STATS_FILE));
			e.printStackTrace();
		}
	}

	/**
	 * commit a kill
	 * 
	 * @param arena
	 *            the arena where that happens
	 * @param e
	 *            an eventual attacker
	 * @param defender
	 *            the attacked player
	 */
	public static void kill(Arena arena, Entity e, Player defender,
			boolean willRespawn) {
		PADeathEvent dEvent = new PADeathEvent(arena, defender, willRespawn, (e != null && e instanceof Player));
		Bukkit.getPluginManager().callEvent(dEvent);
		if (!willRespawn) {
			PAExitEvent exitEvent = new PAExitEvent(arena, defender);
			Bukkit.getPluginManager().callEvent(exitEvent);
		}

		if ((e != null) && (e instanceof Player)) {
			Player attacker = (Player) e;
			if (arena.hasPlayer(attacker)) {
				PAKillEvent kEvent = new PAKillEvent(arena, attacker);
				Bukkit.getPluginManager().callEvent(kEvent);

				ArenaPlayer.parsePlayer(attacker.getName()).addKill();
			}
		}
		ArenaPlayer.parsePlayer(defender.getName()).addDeath();
	}

	/**
	 * gather all type information of an array of ArenaPlayers
	 * 
	 * @param players
	 *            the ArenaPlayer array to check
	 * @param t
	 *            the type to read
	 * @return an Array of String
	 */
	public static String[] read(ArenaPlayer[] players, type t, boolean global) {
		String[] result = new String[players.length < 8 ? 8 : players.length];
		int i = 0;
		if (global) {
			for (ArenaPlayer p : players) {
				if (p == null || p.get() == null) {
					continue;
				}
				if (t.equals(type.NULL)) {
					result[i++] = p.getName();
				} else {
					result[i++] = String.valueOf(p.getTotalStatistics(t));
				}
			}
		} else {
			for (ArenaPlayer p : players) {
				if (t.equals(type.NULL)) {
					result[i++] = p.getName();
				} else {
					result[i++] = String.valueOf(p.getStatistics(p.getArena()).getStat(t));
				}
			}
		}
		while (i < 8) {
			result[i++] = "";
		}
		return result;
	}

	/**
	 * bubble sort an ArenaPlayer array by type
	 * 
	 * @param aps
	 *            the ArenaPlayer array
	 * @param sortBy
	 *            the type to sort by
	 * @param desc
	 *            descending order?
	 * @param global 
	 */
	private static void sortBy(ArenaPlayer[] aps, type sortBy, boolean desc, boolean global) {
		int n = aps.length;
		boolean doMore = true;
		while (doMore) {
			n--;
			doMore = false; // assume this is our last pass over the array
			for (int i = 0; i < n; i++) {
				if (decide(aps, i, sortBy, desc, global)) {
					// exchange elements
					ArenaPlayer temp = aps[i];
					aps[i] = aps[i + 1];
					aps[i + 1] = temp;
					doMore = true; // after an exchange, must look again
				}
			}
		}
	}
}
