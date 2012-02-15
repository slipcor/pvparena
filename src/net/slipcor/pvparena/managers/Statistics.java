package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.ArenaPlayer;

/**
 * statistics manager class
 * 
 * -
 * 
 * provides commands to save win/lose stats to a yml file
 * 
 * @author slipcor
 * 
 * @version v0.6.2
 * 
 */

public class Statistics {
	public static enum type {
		WINS, LOSSES, KILLS, DEATHS, DAMAGE, DAMAGETAKE, DMGSUM, DMGSUMTAKE, NULL
	}

	public static ArenaPlayer[] getStats(Arena a, type sortBy) {
		return getStats(a, sortBy, true);
	}

	public static ArenaPlayer[] getStats(Arena a, type sortBy, boolean desc) {
		ArenaPlayer[] aps = new ArenaPlayer[a.pm.getPlayers().size()];
		int i = 0;
		for (ArenaPlayer p : a.pm.getPlayers()) {
			aps[i++] = p;
		}

		sortBy(aps, sortBy, desc);

		return aps;
		// TODO use the result of this function in the leaderboards
	}

	private static void sortBy(ArenaPlayer[] aps, type sortBy, boolean desc) {
		int n = aps.length;
		boolean doMore = true;
		while (doMore) {
			n--;
			doMore = false; // assume this is our last pass over the array
			for (int i = 0; i < n; i++) {
				if (decide(aps, i, sortBy, desc)) {
					// exchange elements
					ArenaPlayer temp = aps[i];
					aps[i] = aps[i + 1];
					aps[i + 1] = temp;
					doMore = true; // after an exchange, must look again
				}
			}
		}
	}

	private static boolean decide(ArenaPlayer[] aps, int pos, type sortBy,
			boolean desc) {
		int a = 0;
		int b = 0;
		if (sortBy.equals(type.DAMAGE)) {
			a = aps[pos].damage;
			b = aps[pos + 1].damage;
		} else if (sortBy.equals(type.DAMAGETAKE)) {
			a = aps[pos].damagetake;
			b = aps[pos + 1].damagetake;
		} else if (sortBy.equals(type.DEATHS)) {
			a = aps[pos].deaths;
			b = aps[pos + 1].deaths;
		} else if (sortBy.equals(type.DMGSUM)) {
			a = aps[pos].damagesum;
			b = aps[pos + 1].damagesum;
		} else if (sortBy.equals(type.DMGSUMTAKE)) {
			a = aps[pos].damagesumtake;
			b = aps[pos + 1].damagesumtake;
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

		return desc ? (a < b) : (a > b);
	}

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

	public static String[] read(ArenaPlayer[] players, type t) {
		String[] result = new String[players.length];
		int i = 0;
		for (ArenaPlayer p : players) {
			if (t.equals(type.DAMAGE)) {
				result[i++] = String.valueOf(p.damage);
			} else if (t.equals(type.DAMAGETAKE)) {
				result[i++] = String.valueOf(p.damagetake);
			} else if (t.equals(type.DEATHS)) {
				result[i++] = String.valueOf(p.deaths);
			} else if (t.equals(type.DMGSUM)) {
				result[i++] = String.valueOf(p.damagesum);
			} else if (t.equals(type.DMGSUMTAKE)) {
				result[i++] = String.valueOf(p.damagesumtake);
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
		return result;
	}
}
