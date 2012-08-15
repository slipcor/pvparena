package net.slipcor.pvparena.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.events.PADeathEvent;
import net.slipcor.pvparena.events.PAExitEvent;
import net.slipcor.pvparena.events.PAKillEvent;

/**
 * statistics manager class
 * 
 * -
 * 
 * provides commands to save win/lose stats to a yml file
 * 
 * @author slipcor
 * 
 * @version v0.7.17
 * 
 */

public class Statistics {
	public static final Debug db = new Debug(36);

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
			if (arena.isPartOf(attacker)) {
				db.i("attacker is in the arena, adding damage!");
				ArenaPlayer p = ArenaPlayer.parsePlayer(attacker);
				p.damage += dmg;
				p.maxdamage = (dmg > p.maxdamage) ? dmg : p.maxdamage;
			}
		}
		ArenaPlayer p = ArenaPlayer.parsePlayer(defender);
		p.damagetake += dmg;
		p.maxdamagetake = (dmg > p.maxdamagetake) ? dmg : p.maxdamagetake;
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
		
		if (sortBy.equals(type.MAXDAMAGE)) {
			a = aps[pos].maxdamage;
			b = aps[pos + 1].maxdamage;
		} else if (sortBy.equals(type.MAXDAMAGETAKE)) {
			a = aps[pos].maxdamagetake;
			b = aps[pos + 1].maxdamagetake;
		} else if (sortBy.equals(type.DEATHS)) {
			a = aps[pos].deaths;
			b = aps[pos + 1].deaths;
		} else if (sortBy.equals(type.DAMAGE)) {
			a = aps[pos].damage;
			b = aps[pos + 1].damage;
		} else if (sortBy.equals(type.DAMAGETAKE)) {
			a = aps[pos].damagetake;
			b = aps[pos + 1].damagetake;
		} else if (sortBy.equals(type.KILLS)) {
			a = aps[pos].kills;
			b = aps[pos + 1].kills;
		} else if (sortBy.equals(type.LOSSES)) {
			a = aps[pos].losses;
			b = aps[pos + 1].losses;
		} else if (sortBy.equals(type.WINS)) {
			a = aps[pos].wins;
			b = aps[pos + 1].wins;
		}

		if (global) {
			if (sortBy.equals(type.MAXDAMAGE)) {
				a += aps[pos].totmaxdamage;
				b += aps[pos + 1].totmaxdamage;
			} else if (sortBy.equals(type.MAXDAMAGETAKE)) {
				a += aps[pos].totmaxdamagetake;
				b += aps[pos + 1].totmaxdamagetake;
			} else if (sortBy.equals(type.DEATHS)) {
				a += aps[pos].totdeaths;
				b += aps[pos + 1].totdeaths;
			} else if (sortBy.equals(type.DAMAGE)) {
				a += aps[pos].totdamage;
				b += aps[pos + 1].totdamage;
			} else if (sortBy.equals(type.DAMAGETAKE)) {
				a += aps[pos].totdamagetake;
				b += aps[pos + 1].totdamagetake;
			} else if (sortBy.equals(type.KILLS)) {
				a += aps[pos].totkills;
				b += aps[pos + 1].totkills;
			} else if (sortBy.equals(type.LOSSES)) {
				a += aps[pos].totlosses;
				b += aps[pos + 1].totlosses;
			} else if (sortBy.equals(type.WINS)) {
				a += aps[pos].totwins;
				b += aps[pos + 1].totwins;
			}
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
		
		int count = (a == null)?ArenaPlayer.countPlayers():Teams.countPlayersInTeams(a);
		
		ArenaPlayer[] aps = new ArenaPlayer[count];
		
		int i = 0;
		if (a == null) {
			for (ArenaPlayer p : ArenaPlayer.getPlayers()) {
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
			if (arena.isPartOf(attacker)) {
				PAKillEvent kEvent = new PAKillEvent(arena, attacker);
				Bukkit.getPluginManager().callEvent(kEvent);

				ArenaPlayer p = ArenaPlayer.parsePlayer(attacker);
				p.kills++;
			}
		}
		ArenaPlayer p = ArenaPlayer.parsePlayer(defender);
		p.deaths++;
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
				if (t.equals(type.MAXDAMAGE)) {
					result[i++] = String.valueOf(p.maxdamage+p.totmaxdamage);
				} else if (t.equals(type.MAXDAMAGETAKE)) {
					result[i++] = String.valueOf(p.maxdamagetake+p.totmaxdamagetake);
				} else if (t.equals(type.DEATHS)) {
					result[i++] = String.valueOf(p.deaths+p.totdeaths);
				} else if (t.equals(type.DAMAGE)) {
					result[i++] = String.valueOf(p.damage+p.totdamage);
				} else if (t.equals(type.DAMAGETAKE)) {
					result[i++] = String.valueOf(p.damagetake+p.totdamagetake);
				} else if (t.equals(type.KILLS)) {
					result[i++] = String.valueOf(p.kills+p.totkills);
				} else if (t.equals(type.LOSSES)) {
					result[i++] = String.valueOf(p.losses+p.totlosses);
				} else if (t.equals(type.WINS)) {
					result[i++] = String.valueOf(p.wins+p.totwins);
				} else {
					result[i++] = p.get().getName();
				}
			}
		} else {
			for (ArenaPlayer p : players) {
				if (t.equals(type.MAXDAMAGE)) {
					result[i++] = String.valueOf(p.maxdamage);
				} else if (t.equals(type.MAXDAMAGETAKE)) {
					result[i++] = String.valueOf(p.maxdamagetake);
				} else if (t.equals(type.DEATHS)) {
					result[i++] = String.valueOf(p.deaths);
				} else if (t.equals(type.DAMAGE)) {
					result[i++] = String.valueOf(p.damage);
				} else if (t.equals(type.DAMAGETAKE)) {
					result[i++] = String.valueOf(p.damagetake);
				} else if (t.equals(type.KILLS)) {
					result[i++] = String.valueOf(p.kills);
				} else if (t.equals(type.LOSSES)) {
					result[i++] = String.valueOf(p.losses);
				} else if (t.equals(type.WINS)) {
					result[i++] = String.valueOf(p.wins);
				} else {
					result[i++] = p.get().getName();
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
