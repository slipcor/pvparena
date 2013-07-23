package net.slipcor.pvparena.managers;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PAStatMap;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PADeathEvent;
import net.slipcor.pvparena.events.PAKillEvent;

/**
 * <pre>Statistics Manager class</pre>
 * 
 * Provides static methods to manage Statistics
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class StatisticsManager {
	public static final Debug DEBUG = new Debug(28);
	private static File players;
	private static YamlConfiguration config;

	public static enum type {
		WINS("matches won"), LOSSES("matches lost"), KILLS("kills"), DEATHS(
				"deaths"), MAXDAMAGE("max damage dealt"), MAXDAMAGETAKE(
				"max damage taken"), DAMAGE("full damage dealt"), DAMAGETAKE(
				"full damage taken"), NULL("player name");

		private final String fullName;

		type(final String name) {
			fullName = name;
		}

		/**
		 * return the next stat type
		 * 
		 * @param tType
		 *            the type
		 * @return the next type
		 */
		public static type next(final type tType) {
			final type[] types = type.values();
			final int ord = tType.ordinal();
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
		public static type last(final type tType) {
			final type[] types = type.values();
			final int ord = tType.ordinal();
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
		public static type getByString(final String string) {
			for (type t : type.values()) {
				if (t.name().equalsIgnoreCase(string)) {
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
	 * @param entity
	 *            an eventual attacker
	 * @param defender
	 *            the attacked player
	 * @param dmg
	 *            the damage value
	 */
	public static void damage(final Arena arena, final Entity entity, final Player defender, final int dmg) {

		arena.getDebugger().i("adding damage to player " + defender.getName(), defender);
		

		if ((entity != null) && (entity instanceof Player)) {
			final Player attacker = (Player) entity;
			arena.getDebugger().i("attacker is player: " + attacker.getName(), defender);
			if (arena.hasPlayer(attacker)) {
				arena.getDebugger().i("attacker is in the arena, adding damage!", defender);
				final ArenaPlayer apAttacker = ArenaPlayer.parsePlayer(attacker.getName());
				final int maxdamage = apAttacker.getStatistics(arena).getStat(type.MAXDAMAGE);
				apAttacker.getStatistics(arena).incStat(type.DAMAGE, dmg);
				if (dmg > maxdamage) {
					apAttacker.getStatistics(arena).setStat(type.MAXDAMAGE, dmg);
				}
			}
		}
		final ArenaPlayer apDefender = ArenaPlayer.parsePlayer(defender.getName());

		final int maxdamage = apDefender.getStatistics(arena).getStat(type.MAXDAMAGETAKE);
		apDefender.getStatistics(arena).incStat(type.DAMAGETAKE, dmg);
		if (dmg > maxdamage) {
			apDefender.getStatistics(arena).setStat(type.MAXDAMAGETAKE, dmg);
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
	private static boolean decide(final ArenaPlayer[] aps, final int pos, final type sortBy,
			final boolean desc, final boolean global) {
		int iThis = 0;
		int iNext = 0;
		
		iThis = aps[pos].getStatistics(aps[pos].getArena()).getStat(sortBy);
		iNext = aps[pos + 1].getStatistics(aps[pos].getArena()).getStat(sortBy);

		if (global) {
			iThis = aps[pos].getTotalStatistics(sortBy);
			iNext = aps[pos + 1].getTotalStatistics(sortBy);
		}

		return desc ? (iThis < iNext) : (iThis > iNext);
	}

	/**
	 * get a set of arena players sorted by type
	 * 
	 * @param arena
	 *            the arena to check
	 * @param sortBy
	 *            the type to sort
	 * @return an array of ArenaPlayer
	 */
	public static ArenaPlayer[] getStats(final Arena arena, final type sortBy) {
		return getStats(arena, sortBy, true);
	}

	/**
	 * get a set of arena players sorted by type
	 * 
	 * @param arena
	 *            the arena to check
	 * @param sortBy
	 *            the type to sort
	 * @param desc
	 *            should it be sorted descending?
	 * @return an array of ArenaPlayer
	 */
	public static ArenaPlayer[] getStats(final Arena arena, final type sortBy, final boolean desc) {
		DEBUG.i("getting stats: " + (arena == null?"global":arena.getName()) + " sorted by " + sortBy + " "
				+ (desc ? "desc" : "asc"));
		
		final int count = (arena == null)?ArenaPlayer.countPlayers():arena.getFighters().size();
		
		ArenaPlayer[] aps = new ArenaPlayer[count];
		
		int pos = 0;
		if (arena == null) {
			for (ArenaPlayer p : ArenaPlayer.getAllArenaPlayers()) {
				aps[pos++] = p;
			}
		} else {
			for (ArenaPlayer p : arena.getFighters()) {
				aps[pos++] = p;
			}
		}

		sortBy(aps, sortBy, desc, arena == null);

		return aps;
	}

	/**
	 * get the type by the sign headline
	 * 
	 * @param line
	 *            the line to determine the type
	 * @return the Statistics type
	 */
	public static type getTypeBySignLine(final String line) {
		if (!line.startsWith("[PA]")) {
			return type.NULL;
		}
		final String stripped = line.replace("[PA]", "").toUpperCase();

		for (type t : type.values()) {
			if (t.name().equals(stripped)) {
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
	 * @param entity
	 *            an eventual attacker
	 * @param defender
	 *            the attacked player
	 */
	public static void kill(final Arena arena, final Entity entity, final Player defender,
			final boolean willRespawn) {
		final PADeathEvent dEvent = new PADeathEvent(arena, defender, willRespawn, (entity instanceof Player));
		Bukkit.getPluginManager().callEvent(dEvent);

		if ((entity != null) && (entity instanceof Player)) {
			final Player attacker = (Player) entity;
			if (arena.hasPlayer(attacker)) {
				final PAKillEvent kEvent = new PAKillEvent(arena, attacker);
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
	 * @param tType
	 *            the type to read
	 * @return an Array of String
	 */
	public static String[] read(final ArenaPlayer[] players, final type tType, final boolean global) {
		String[] result = new String[players.length < 8 ? 8 : players.length];
		int pos = 0;
		if (global) {
			for (ArenaPlayer p : players) {
				if (p == null) {
					continue;
				}
				if (tType.equals(type.NULL)) {
					result[pos++] = p.getName();
				} else {
					result[pos++] = String.valueOf(p.getTotalStatistics(tType));
				}
			}
		} else {
			for (ArenaPlayer p : players) {
				if (tType.equals(type.NULL)) {
					result[pos++] = p.getName();
				} else {
					result[pos++] = String.valueOf(p.getStatistics(p.getArena()).getStat(tType));
				}
			}
		}
		while (pos < 8) {
			result[pos++] = "";
		}
		return result;
	}
	
	public static void save() {
		try {
			config.save(players);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	private static void sortBy(final ArenaPlayer[] aps, final type sortBy, final boolean desc, final boolean global) {
		int pos = aps.length;
		boolean doMore = true;
		while (doMore) {
			pos--;
			doMore = false; // assume this is our last pass over the array
			for (int i = 0; i < pos; i++) {
				if (decide(aps, i, sortBy, desc, global)) {
					// exchange elements
					final ArenaPlayer temp = aps[i];
					aps[i] = aps[i + 1];
					aps[i + 1] = temp;
					doMore = true; // after an exchange, must look again
				}
			}
		}
	}

	public static void loadStatistics(Arena arena) {
		if (!PVPArena.instance.getConfig().getBoolean("stats")) {
			return;
		} else if (config == null) {
			initialize();
		}
		if (config.getConfigurationSection(arena.getName()) == null) {
			return;
		}
		arena.getDebugger().i("loading statistics!");
		for (String player : config.getConfigurationSection(arena.getName()).getKeys(false)) {

			arena.getDebugger().i("loading stats: " + player);
			
			ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player);

			final int losses = config.getInt(arena.getName() + "." + player + ".losses", 0);
			aPlayer.addStatistic(arena.getName(), type.LOSSES, losses);

			final int wins = config.getInt(arena.getName() + "." + player + ".wins", 0);
			aPlayer.addStatistic(arena.getName(), type.WINS, wins);

			final int kills = config.getInt(arena.getName() + "." + player + ".kills", 0);
			aPlayer.addStatistic(arena.getName(), type.KILLS, kills);

			final int deaths = config.getInt(arena.getName() + "." + player + ".deaths", 0);
			aPlayer.addStatistic(arena.getName(), type.DEATHS, deaths);

			final int damage = config.getInt(arena.getName() + "." + player + ".damage", 0);
			aPlayer.addStatistic(arena.getName(), type.DAMAGE, damage);

			final int maxdamage = config.getInt(arena.getName() + "." + player + ".maxdamage", 0);
			aPlayer.addStatistic(arena.getName(), type.MAXDAMAGE, maxdamage);

			final int damagetake = config.getInt(arena.getName() + "." + player + ".damagetake", 0);
			aPlayer.addStatistic(arena.getName(), type.DAMAGETAKE, damagetake);

			final int maxdamagetake = config.getInt(arena.getName() + "." + player + ".maxdamagetake", 0);
			aPlayer.addStatistic(arena.getName(), type.MAXDAMAGETAKE, maxdamagetake);
		}
	}

	public static void update(Arena arena, ArenaPlayer aPlayer) {
		PAStatMap map = aPlayer.getStatistics(arena);

		final int losses = map.getStat(type.LOSSES);
		config.set(arena.getName()+"."+aPlayer.getName()+".losses", losses);

		final int wins = map.getStat(type.WINS);
		config.set(arena.getName()+"."+aPlayer.getName()+".wins", wins);
		
		final int kills = map.getStat(type.KILLS);
		config.set(arena.getName()+"."+aPlayer.getName()+".kills", kills);
		
		final int deaths = map.getStat(type.DEATHS);
		config.set(arena.getName()+"."+aPlayer.getName()+".deaths", deaths);
		
		final int damage = map.getStat(type.DAMAGE);
		config.set(arena.getName()+"."+aPlayer.getName()+".damage", damage);
		
		final int maxdamage = map.getStat(type.MAXDAMAGE);
		config.set(arena.getName()+"."+aPlayer.getName()+".maxdamage", maxdamage);
		
		final int damagetake = map.getStat(type.DAMAGETAKE);
		config.set(arena.getName()+"."+aPlayer.getName()+".damagetake", damagetake);
		
		final int maxdamagetake = map.getStat(type.MAXDAMAGETAKE);
		config.set(arena.getName()+"."+aPlayer.getName()+".maxdamagetake", maxdamagetake);
		
	}
}
