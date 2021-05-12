package net.slipcor.pvparena.managers;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PAStatMap;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PADeathEvent;
import net.slipcor.pvparena.events.PAKillEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.reverseOrder;

/**
 * <pre>Statistics Manager class</pre>
 * <p/>
 * Provides static methods to manage Statistics
 *
 * @author slipcor
 * @version v0.10.2
 */

public final class StatisticsManager {
    private static final Debug DEBUG = new Debug(28);
    private static File playersFile;
    private static YamlConfiguration config;

    private StatisticsManager() {}

    public enum Type {
        WINS("matches won", "Wins"),
        LOSSES("matches lost", "Losses"),
        KILLS("kills", "Kills"),
        DEATHS("deaths", "Deaths"),
        MAXDAMAGE("max damage dealt", "MaxDmg"),
        MAXDAMAGETAKE("max damage taken", "MaxDmgTaken"),
        DAMAGE("full damage dealt", "Damage"),
        DAMAGETAKE("full damage taken", "DamageTagen"),
        NULL("player name", "Player");

        private final String fullName;
        private final String niceDesc;

        Type(final String name, final String desc) {
            this.fullName = name;
            this.niceDesc = desc;
        }

        /**
         * return the next stat type
         *
         * @param tType the type
         * @return the next type
         */
        public static Type next(final Type tType) {
            final Type[] types = Type.values();
            final int ord = tType.ordinal();
            if (ord >= types.length - 2) {
                return types[0];
            }
            return types[ord + 1];
        }

        /**
         * return the previous stat type
         *
         * @param tType the type
         * @return the previous type
         */
        public static Type last(final Type tType) {
            final Type[] types = Type.values();
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
         * @param string the name to find
         * @return the type if found, null otherwise
         */
        public static Type getByString(final String string) {
            for (final Type t : Type.values()) {
                if (t.name().equalsIgnoreCase(string)) {
                    return t;
                }
            }
            return null;
        }

        public String getNiceName() {
            return this.niceDesc;
        }
    }

    /**
     * commit damage
     *
     * @param arena    the arena where that happens
     * @param entity   an eventual attacker
     * @param defender the attacked player
     * @param dmg      the damage value
     */
    public static void damage(final Arena arena, final Entity entity, final Player defender, final double dmg) {

        arena.getDebugger().i("adding damage to player " + defender.getName(), defender);


        if (entity instanceof Player) {
            final Player attacker = (Player) entity;
            arena.getDebugger().i("attacker is player: " + attacker.getName(), defender);
            if (arena.hasPlayer(attacker)) {
                arena.getDebugger().i("attacker is in the arena, adding damage!", defender);
                final ArenaPlayer apAttacker = ArenaPlayer.parsePlayer(attacker.getName());
                final int maxdamage = apAttacker.getStatistics(arena).getStat(Type.MAXDAMAGE);
                apAttacker.getStatistics(arena).incStat(Type.DAMAGE, (int) dmg);
                if (dmg > maxdamage) {
                    apAttacker.getStatistics(arena).setStat(Type.MAXDAMAGE, (int) dmg);
                }
            }
        }
        final ArenaPlayer apDefender = ArenaPlayer.parsePlayer(defender.getName());

        final int maxdamage = apDefender.getStatistics(arena).getStat(Type.MAXDAMAGETAKE);
        apDefender.getStatistics(arena).incStat(Type.DAMAGETAKE, (int) dmg);
        if (dmg > maxdamage) {
            apDefender.getStatistics(arena).setStat(Type.MAXDAMAGETAKE, (int) dmg);
        }
    }

    /**
     * get an array of stats for arena boards and with a given stats type
     *
     * @param arena  the arena to check
     * @param statType the type to sort
     * @return an array of stats values
     */
    public static String[] getStatsValuesForBoard(final Arena arena, final Type statType) {
        DEBUG.i("getting stats values: " + (arena == null ? "global" : arena.getName()) + " sorted by " + statType);

        if (arena == null) {
            return ArenaPlayer.getAllArenaPlayers().stream()
                    .map(ap -> (statType == Type.NULL) ? ap.getName() : String.valueOf(ap.getTotalStatistics(statType)))
                    .sorted(reverseOrder())
                    .limit(8)
                    .toArray(String[]::new);
        }

        return arena.getFighters().stream()
                .map(ap -> (statType == Type.NULL) ? ap.getName() : String.valueOf(ap.getStatistics().getStat(statType)))
                .sorted(reverseOrder())
                .limit(8)
                .toArray(String[]::new);
    }

    /**
     * Get stats map for a given stat type
     * @param arena the arena to check
     * @param statType the kind of stat
     * @return A map with player name and stat value
     */
    public static Map<String, Integer> getStats(final Arena arena, final Type statType) {
        DEBUG.i("getting stats: " + (arena == null ? "global" : arena.getName()) + " sorted by " + statType);

        if (arena == null) {
            return ArenaPlayer.getAllArenaPlayers().stream()
                    .collect(Collectors.toMap(ArenaPlayer::getName, ap -> ap.getTotalStatistics(statType)));
        }

        return arena.getFighters().stream()
                .collect(Collectors.toMap(ArenaPlayer::getName, ap -> ap.getStatistics().getStat(statType)));
    }

    /**
     * get the type by the sign headline
     *
     * @param line the line to determine the type
     * @return the Statistics type
     */
    public static Type getTypeBySignLine(final String line) {
        final String stripped = ChatColor.stripColor(line).replace("[PA]", "").toUpperCase();

        for (final Type t : Type.values()) {
            if (t.name().equals(stripped)) {
                return t;
            }
            if (t.getNiceName().equals(stripped)) {
                return t;
            }
        }
        return Type.NULL;
    }

    public static void initialize() {
        if (!PVPArena.instance.getConfig().getBoolean("stats")) {
            return;
        }
        config = new YamlConfiguration();
        playersFile = new File(PVPArena.instance.getDataFolder(), "players.yml");
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
                Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.STATS_FILE_DONE));
            } catch (final Exception e) {
                Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_STATS_FILE));
                e.printStackTrace();
            }
        }

        try {
            config.load(playersFile);
        } catch (final Exception e) {
            Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.ERROR_STATS_FILE));
            e.printStackTrace();
        }
    }

    /**
     * commit a kill
     *
     * @param arena    the arena where that happens
     * @param entity   an eventual attacker
     * @param defender the attacked player
     */
    public static void kill(final Arena arena, final Entity entity, final Player defender,
                            final boolean willRespawn) {
        final PADeathEvent dEvent = new PADeathEvent(arena, defender, willRespawn, entity instanceof Player);
        Bukkit.getPluginManager().callEvent(dEvent);

        if (entity instanceof Player) {
            final Player attacker = (Player) entity;
            if (arena.hasPlayer(attacker)) {
                final PAKillEvent kEvent = new PAKillEvent(arena, attacker);
                Bukkit.getPluginManager().callEvent(kEvent);

                ArenaPlayer.parsePlayer(attacker.getName()).addKill();
            }
        }
        ArenaPlayer.parsePlayer(defender.getName()).addDeath();
    }

    public static void save() {
        if (config == null) {
            return;
        }
        try {
            config.save(playersFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadStatistics(final Arena arena) {
        if (!PVPArena.instance.getConfig().getBoolean("stats")) {
            return;
        }
        if (config == null) {
            initialize();
        }
        if (config.getConfigurationSection(arena.getName()) == null) {
            return;
        }

        arena.getDebugger().i("loading statistics!");
        boolean foundBroken = false;
        for (final String playerID : config.getConfigurationSection(arena.getName()).getKeys(false)) {


            String playerName = null;

            if (config.getConfigurationSection(arena.getName()).contains(playerID+".name")) {
                playerName = config.getConfigurationSection(arena.getName()).getString(playerID+".name");
            }

            arena.getDebugger().i("loading stats: " + playerName);

            final ArenaPlayer aPlayer;

            try {
                if(playerName != null) {
                    aPlayer = ArenaPlayer.addPlayer(playerName);
                } else {
                    continue;
                }

            } catch (IllegalArgumentException e) {
                PVPArena.instance.getLogger().warning("invalid player ID: " + playerID);
                continue;
            }

            for (final Type ttt : Type.values()) {
                aPlayer.setStatistic(arena.getName(), ttt, 0);
            }

            final int losses = config.getInt(arena.getName() + '.' + playerID + ".losses", 0);
            aPlayer.addStatistic(arena.getName(), Type.LOSSES, losses);

            final int wins = config.getInt(arena.getName() + '.' + playerID + ".wins", 0);
            aPlayer.addStatistic(arena.getName(), Type.WINS, wins);

            final int kills = config.getInt(arena.getName() + '.' + playerID + ".kills", 0);
            aPlayer.addStatistic(arena.getName(), Type.KILLS, kills);

            final int deaths = config.getInt(arena.getName() + '.' + playerID + ".deaths", 0);
            aPlayer.addStatistic(arena.getName(), Type.DEATHS, deaths);

            final int damage = config.getInt(arena.getName() + '.' + playerID + ".damage", 0);
            aPlayer.addStatistic(arena.getName(), Type.DAMAGE, damage);

            final int maxdamage = config.getInt(arena.getName() + '.' + playerID + ".maxdamage", 0);
            aPlayer.addStatistic(arena.getName(), Type.MAXDAMAGE, maxdamage);

            final int damagetake = config.getInt(arena.getName() + '.' + playerID + ".damagetake", 0);
            aPlayer.addStatistic(arena.getName(), Type.DAMAGETAKE, damagetake);

            final int maxdamagetake = config.getInt(arena.getName() + '.' + playerID + ".maxdamagetake", 0);
            aPlayer.addStatistic(arena.getName(), Type.MAXDAMAGETAKE, maxdamagetake);
        }
        if (foundBroken) {
            save();
        }
    }

    public static void update(final Arena arena, final ArenaPlayer aPlayer) {
        if (config == null) {
            return;
        }

        final PAStatMap map = aPlayer.getStatistics(arena);

        String node = aPlayer.getName();

        try {
            node = aPlayer.get().getUniqueId().toString();
        } catch (final Exception ignored) {

        }

        final int losses = map.getStat(Type.LOSSES);
        config.set(arena.getName() + '.' + node + ".losses", losses);

        final int wins = map.getStat(Type.WINS);
        config.set(arena.getName() + '.' + node + ".wins", wins);

        final int kills = map.getStat(Type.KILLS);
        config.set(arena.getName() + '.' + node + ".kills", kills);

        final int deaths = map.getStat(Type.DEATHS);
        config.set(arena.getName() + '.' + node + ".deaths", deaths);

        final int damage = map.getStat(Type.DAMAGE);
        config.set(arena.getName() + '.' + node + ".damage", damage);

        final int maxdamage = map.getStat(Type.MAXDAMAGE);
        config.set(arena.getName() + '.' + node + ".maxdamage", maxdamage);

        final int damagetake = map.getStat(Type.DAMAGETAKE);
        config.set(arena.getName() + '.' + node + ".damagetake", damagetake);

        final int maxdamagetake = map.getStat(Type.MAXDAMAGETAKE);
        config.set(arena.getName() + '.' + node + ".maxdamagetake", maxdamagetake);

        if (!node.equals(aPlayer.getName())) {
            config.set(arena.getName() + '.' + node + ".playerName", aPlayer.getName());
        }

    }
}
