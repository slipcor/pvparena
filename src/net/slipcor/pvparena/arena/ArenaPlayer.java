package net.slipcor.pvparena.arena;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.classes.PAStatMap;
import net.slipcor.pvparena.core.ColorUtils;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.events.PAPlayerClassChangeEvent;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.InventoryManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;

/**
 * <pre>
 * Arena Player class
 * </pre>
 * <p/>
 * contains Arena Player methods and variables for quicker access
 *
 * @author slipcor
 * @version v0.10.2
 */

public class ArenaPlayer {
    private static final Debug debug = new Debug(5);
    private static final Map<String, ArenaPlayer> totalPlayers = new HashMap<>();

    private final String name;
    private boolean telePass;
    private boolean ignoreAnnouncements;
    private boolean teleporting;
    private boolean mayDropInventory;

    private Boolean flying;

    private Arena arena;
    private ArenaClass aClass;
    private ArenaClass naClass;
    private PlayerState state;
    private PALocation location;
    private Status status = Status.NULL;

    private ItemStack[] savedInventory;
    private final Set<PermissionAttachment> tempPermissions = new HashSet<>();
    private final Map<String, PAStatMap> statistics = new HashMap<>();

    private Scoreboard backupBoard;
    private Team backupBoardTeam;

    /**
     * Status
     *
     * <pre>
     * - NULL = not part of an arena
     * - WARM = not part of an arena, warmed up
     * - LOUNGE = inside an arena lobby mode
     * - READY = inside an arena lobby mode, readied up
     * - FIGHT = fighting inside an arena
     * - WATCH = watching a fight from the spectator area
     * - DEAD = dead and soon respawning
     * - LOST = lost and thus spectating
     * </pre>
     */
    public enum Status {
        NULL, WARM, LOUNGE, READY, FIGHT, WATCH, DEAD, LOST
    }


    /**
     * PlayerPrevention
     *
     * <pre>
     * BREAK - Block break
     * PLACE - Block placement
     * TNT - TNT usage
     * TNTBREAK - TNT block break
     * DROP - dropping items
     * INVENTORY - accessing inventory
     * PICKUP - picking up stuff
     * CRAFT - crafting stuff
     * </pre>
     */
    public enum PlayerPrevention {
        BREAK, PLACE, TNT, TNTBREAK, DROP, INVENTORY, PICKUP, CRAFT;
        public static boolean has(int value, PlayerPrevention s) {
            return (((int) Math.pow(2, s.ordinal()) & value) > 0);
        }
    }

    private boolean publicChatting = true;
    private final PABlockLocation[] selection = new PABlockLocation[2];

    private ArenaPlayer(final String playerName) {
        name = playerName;

        totalPlayers.put(name, this);
    }

    private ArenaPlayer(final Player player, final Arena arena) {
        name = player.getName();
        this.arena = arena;

        totalPlayers.put(name, this);
    }

    public static int countPlayers() {
        return totalPlayers.size();
    }

    public static Set<ArenaPlayer> getAllArenaPlayers() {
        final Set<ArenaPlayer> players = new HashSet<>();
        for (final ArenaPlayer ap : totalPlayers.values()) {
            players.add(ap);
        }
        return players;
    }

    public boolean getFlyState() {
        return flying != null && flying;
    }

    /**
     * try to find the last damaging player
     *
     * @param eEvent the Event
     * @return the player instance if found, null otherwise
     */
    public static Player getLastDamagingPlayer(final Event eEvent, final Player damagee) {

        final Debug debug = ArenaPlayer.parsePlayer(damagee.getName()).arena == null ?
                ArenaPlayer.debug : ArenaPlayer.parsePlayer(damagee.getName()).arena.getDebugger();

        debug.i("trying to get the last damaging player", damagee);
        if (eEvent instanceof EntityDamageByEntityEvent) {
            debug.i("there was an EDBEE", damagee);
            final EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) eEvent;

            Entity eDamager = event.getDamager();

            if (event.getCause() == DamageCause.PROJECTILE
                    && eDamager instanceof Projectile) {

                final ProjectileSource p = ((Projectile) eDamager).getShooter();

                if (p instanceof LivingEntity) {

                    eDamager = (LivingEntity) p;


                    debug.i("killed by projectile, shooter is found", damagee);
                }
            }

            if (event.getEntity() instanceof Wolf) {
                final Wolf wolf = (Wolf) event.getEntity();
                if (wolf.getOwner() != null) {
                    eDamager = (Entity) wolf.getOwner();
                    debug.i("tamed wolf is found", damagee);
                }
            }

            if (eDamager instanceof Player) {
                debug.i("it was a player!", damagee);
                return (Player) eDamager;
            }
        }
        debug.i("last damaging player is null", damagee);
        debug.i("last damaging event: " + eEvent.getEventName(), damagee);
        return null;
    }

    /**
     * supply a player with class items and eventually wool head
     *
     * @param player the player to supply
     */
    public static void givePlayerFightItems(final Arena arena, final Player player) {
        final ArenaPlayer aPlayer = parsePlayer(player.getName());

        final ArenaClass playerClass = aPlayer.aClass;
        if (playerClass == null) {
            return;
        }
        arena.getDebugger().i("giving items to player '" + player.getName()
                + "', class '" + playerClass.getName() + '\'', player);

        playerClass.equip(player);

        if (arena.getArenaConfig().getBoolean(CFG.USES_WOOLHEAD)) {
            final ArenaTeam aTeam = aPlayer.getArenaTeam();
            final ChatColor color = aTeam.getColor();
            arena.getDebugger().i("forcing woolhead: " + aTeam.getName() + '/'
                    + color.name(), player);
            player.getInventory().setHelmet(
                    new ItemStack(ColorUtils.getWoolMaterialFromChatColor(color)));
        }
    }

    public static void initiate() {
        debug.i("creating offline arena players");

        if (!PVPArena.instance.getConfig().getBoolean("stats")) {
            return;
        }

        final YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(PVPArena.instance.getDataFolder() + "/players.yml");

            final Set<String> arenas = cfg.getKeys(false);

            for (final String arenaname : arenas) {

                final Set<String> players = cfg.getConfigurationSection(arenaname).getKeys(false);
                for (final String player : players) {
                    totalPlayers.put(player, ArenaPlayer.parsePlayer(player));
                }

            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get an ArenaPlayer from a player name
     *
     * @param name the playername to use
     * @return an ArenaPlayer instance belonging to that player
     */
    public static ArenaPlayer parsePlayer(final String name) {
        synchronized (ArenaPlayer.class) {
            if (totalPlayers.get(name) == null) {
                if (Bukkit.getPlayerExact(name) == null) {
                    totalPlayers.put(name, new ArenaPlayer(name));
                } else {
                    totalPlayers.put(name,
                            new ArenaPlayer(Bukkit.getPlayerExact(name), null));
                }
            }
            return totalPlayers.get(name);
        }
    }

    /**
     * prepare a player's inventory, back it up and clear it
     *
     * @param player the player to save
     */
    public static void backupAndClearInventory(final Arena arena, final Player player) {
        arena.getDebugger().i("saving player inventory: " + player.getName(),
                player);

        final ArenaPlayer aPlayer = parsePlayer(player.getName());
        aPlayer.savedInventory = player.getInventory().getContents().clone();
        InventoryManager.clearInventory(player);
    }

    public static void reloadInventory(final Arena arena, final Player player, final boolean instant) {

        if (player == null) {
            return;
        }
        final Debug debug = arena.getDebugger();
        debug.i("resetting inventory: " + player.getName(), player);
        if (player.getInventory() == null) {
            debug.i("inventory null!", player);
            return;
        }

        final ArenaPlayer aPlayer = parsePlayer(player.getName());

        if (arena.getArenaConfig().getYamlConfiguration().get(CFG.ITEMS_TAKEOUTOFGAME.getNode()) != null) {
            final ItemStack[] items =
                    arena.getArenaConfig().getItems(CFG.ITEMS_TAKEOUTOFGAME);

            final List<Material> allowedMats = new ArrayList<>();

            for (final ItemStack item : items) {
                allowedMats.add(item.getType());
            }

            final List<ItemStack> keepItems = new ArrayList<>();
            for (final ItemStack item : player.getInventory().getContents()) {
                if (item == null) {
                    continue;
                }
                if (allowedMats.contains(item.getType())) {
                    keepItems.add(item.clone());
                }
            }

            class GiveLater implements Runnable {

                @Override
                public void run() {
                    for (final ItemStack item : keepItems) {
                        player.getInventory().addItem(item.clone());
                    }
                    keepItems.clear();
                }

            }

            try {
                Bukkit.getScheduler().runTaskLater(PVPArena.instance, new GiveLater(), 60L);
            } catch (final Exception e) {

            }
        }
        InventoryManager.clearInventory(player);

        if (aPlayer.savedInventory == null) {
            debug.i("saved inventory null!", player);
            return;
        }
        // AIR AIR AIR AIR instead of contents !!!!

        if (instant) {

            debug.i("adding saved inventory", player);
            player.getInventory().setContents(aPlayer.savedInventory);
        } else {
            class GiveLater implements Runnable {
                final ItemStack[] inv;
                GiveLater(final ItemStack[] inv) {
                    this.inv = inv.clone();
                    }
                @Override
                public void run() {
                    debug.i("adding saved inventory",
                            player);
                    player.getInventory().setContents(inv);
                    }
            }
            final GiveLater gl = new GiveLater(aPlayer.savedInventory);
            try {
                Bukkit.getScheduler().runTaskLater(PVPArena.instance, gl, 60L);
            } catch (final Exception e) {
                gl.run();
            }
        }
    }

    public void addDeath() {
        getStatistics(arena).incStat(type.DEATHS);
    }

    public void addKill() {
        getStatistics(arena).incStat(type.KILLS);
    }

    public void addLosses() {
        getStatistics(arena).incStat(type.LOSSES);
    }

    public void addStatistic(final String arenaName, final type type,
                             final int value) {
        if (!statistics.containsKey(arenaName)) {
            statistics.put(arenaName, new PAStatMap());
        }

        statistics.get(arenaName).incStat(type, value);
    }

    public void addWins() {
        getStatistics(arena).incStat(type.WINS);
    }

    private void clearDump() {
        debug.i("clearing dump of " + name, name);
        debugPrint();
        final File file = new File(PVPArena.instance.getDataFolder().getPath()
                + "/dumps/" + name + ".yml");
        if (!file.exists()) {
            return;
        }
        file.delete();
    }

    public void clearFlyState() {
        flying = null;
    }

    /**
     * save the player state
     *
     * @param player the player to save
     */
    public void createState(final Player player) {
        state = new PlayerState(player);
        mayDropInventory = true;
    }

    public boolean didValidSelection() {
        return selection[0] != null && selection[1] != null;
    }

    public void debugPrint() {
        if (status == null || location == null) {
            debug.i("DEBUG PRINT OUT:", name);
            debug.i(name, name);
            debug.i(String.valueOf(status), name);
            debug.i(String.valueOf(location), name);
            debug.i(String.valueOf(selection[0]), name);
            debug.i(String.valueOf(selection[1]), name);
            return;
        }
        debug.i("------------------", name);
        debug.i("Player: " + name, name);
        debug.i("telepass: " + telePass + " | mayDropInv: " + mayDropInventory + " | chatting: "
                + publicChatting, name);
        debug.i("arena: " + (arena == null ? "null" : arena.getName()), name);
        debug.i("aClass: " + (aClass == null ? "null" : aClass.getName()),
                name);
        debug.i("location: " + location, name);
        debug.i("status: " + status.name(), name);
        debug.i("tempPermissions:", name);
        for (final PermissionAttachment pa : tempPermissions) {
            debug.i("> " + pa, name);
        }
        debug.i("------------------", name);
    }

    public void dump() {
        debug.i("dumping...", name);
        debugPrint();
        final File file = new File(PVPArena.instance.getDataFolder().getPath()
                + "/dumps/" + name + ".yml");
        try {
            file.createNewFile();
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }

        final YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("arena", arena.getName());
        if (state != null) {
            state.dump(cfg);
        }

        try {
            cfg.set("inventory", savedInventory);
            cfg.set("loc", Config.parseToString(location));

            cfg.save(file);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * return the PVP Arena bukkit player
     *
     * @return the bukkit player instance
     */
    public Player get() {
        return Bukkit.getPlayerExact(name);
    }

    /**
     * return the arena
     *
     * @return the arena
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * return the arena class
     *
     * @return the arena class
     */
    public ArenaClass getArenaClass() {
        return aClass;
    }
    public ArenaClass getNextArenaClass() {
        return naClass;
    }

    public ArenaTeam getArenaTeam() {
        if (arena == null) {
            return null;
        }
        for (final ArenaTeam team : arena.getTeams()) {
            if (team.getTeamMembers().contains(this)) {
                return team;
            }
        }
        return null;
    }

    public Scoreboard getBackupScoreboard() {
        return backupBoard;
    }

    public Team getBackupScoreboardTeam() {
        return backupBoardTeam;
    }

    public PALocation getSavedLocation() {
        debug.i("reading loc!", name);
        if (location != null) {
            debug.i(": " + location, name);
        }
        return location;
    }

    /**
     * return the player name
     *
     * @return the player name
     */
    public String getName() {
        return name;
    }

    public PABlockLocation[] getSelection() {
        return selection.clone();
    }

    /**
     * return the player state
     *
     * @return the player state
     */
    public PlayerState getState() {
        return state;
    }

    public PAStatMap getStatistics() {
        return getStatistics(arena);
    }

    public PAStatMap getStatistics(final Arena arena) {
        if (arena == null) {
            return new PAStatMap();
        }
        if (statistics.get(arena.getName()) == null) {
            statistics.put(arena.getName(), new PAStatMap());
        }
        return statistics.get(arena.getName());
    }

    public Status getStatus() {
        return status;
    }

    /**
     * hand over a player's tele pass
     *
     * @return true if may pass, false otherwise
     */
    public boolean isTelePass() {
        return hasTelePass();
    }

    public boolean isTeleporting() {
        return teleporting;
    }

    public boolean mayDropInventory() {
        return this.mayDropInventory;
    }

    public Set<PermissionAttachment> getTempPermissions() {
        return tempPermissions;
    }

    public int getTotalStatistics(final type statType) {
        int sum = 0;

        for (final PAStatMap stat : statistics.values()) {
            sum += stat.getStat(statType);
        }

        return sum;
    }

    public boolean hasBackupScoreboard() {
        return backupBoard != null;
    }

    public boolean hasTelePass() {
        return telePass;
    }

    public boolean isIgnoringAnnouncements() {
        return ignoreAnnouncements;
    }

    public boolean isPublicChatting() {
        return publicChatting;
    }

    public boolean hasCustomClass() {
        return this.getArenaClass() != null && "custom".equalsIgnoreCase(this.getArenaClass().getName());
    }

    public void readDump() {
        debug.i("reading dump: " + name, name);
        debugPrint();
        final File file = new File(PVPArena.instance.getDataFolder().getPath()
                + "/dumps/" + name + ".yml");
        if (!file.exists()) {
            debug.i("no dump!", name);
            return;
        }

        final YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(file);
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }

        arena = ArenaManager.getArenaByName(cfg.getString("arena"));
        savedInventory = cfg.getList("inventory").toArray(new ItemStack[0]);
                /*StringParser.getItemStacksFromString(cfg.getString(
                "inventory", "AIR"));*/
        location = Config.parseLocation(cfg.getString("loc"));

        if (arena != null) {
            final String goTo = arena.getArenaConfig().getString(CFG.TP_EXIT);
            if (!"old".equals(goTo)) {
                location = SpawnManager.getSpawnByExactName(arena, "exit");
            }

            if (Bukkit.getPlayer(name) == null) {
                debug.i("player offline, OUT!", name);
                return;
            }
            state = PlayerState.undump(cfg, name);
        }

        file.delete();
        debugPrint();
    }

    /**
     * save and reset a player instance
     */
    public void reset() {
        debug.i("destroying arena player " + name, name);
        debugPrint();
        final YamlConfiguration cfg = new YamlConfiguration();
        try {
            if (PVPArena.instance.getConfig().getBoolean("stats")) {

                final String file = PVPArena.instance.getDataFolder()
                        + "/players.yml";
                cfg.load(file);

                if (arena != null) {
                    final String arenaName = arena.getName();
                    cfg.set(arenaName + '.' + name + ".losses", getStatistics()
                            .getStat(type.LOSSES)
                            + getTotalStatistics(type.LOSSES));
                    cfg.set(arenaName + '.' + name + ".wins",
                            getStatistics()
                                    .getStat(type.WINS)
                                    + getTotalStatistics(type.WINS));
                    cfg.set(arenaName + '.' + name + ".kills",
                            getStatistics().getStat(
                                    type.KILLS)
                                    + getTotalStatistics(type.KILLS));
                    cfg.set(arenaName + '.' + name + ".deaths", getStatistics()
                            .getStat(type.DEATHS)
                            + getTotalStatistics(type.DEATHS));
                    cfg.set(arenaName + '.' + name + ".damage", getStatistics()
                            .getStat(type.DAMAGE)
                            + getTotalStatistics(type.DAMAGE));
                    cfg.set(arenaName + '.' + name + ".maxdamage",
                            getStatistics().getStat(
                                    type.MAXDAMAGE)
                                    + getTotalStatistics(type.MAXDAMAGE));
                    cfg.set(arenaName + '.' + name + ".damagetake",
                            getStatistics().getStat(
                                    type.DAMAGETAKE)
                                    + getTotalStatistics(type.DAMAGETAKE));
                    cfg.set(arenaName + '.' + name + ".maxdamagetake",
                            getStatistics().getStat(
                                    type.MAXDAMAGETAKE)
                                    + getTotalStatistics(type.MAXDAMAGETAKE));
                }

                cfg.save(file);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        if (get() == null) {
            debug.i("reset() ; out! null", name);
            return;
        }

        telePass = false;

        if (state != null) {
            state.reset();
            state = null;
        }
        // location = null;

        setStatus(Status.NULL);
        naClass = null;

        if (arena != null) {
            final ArenaTeam team = getArenaTeam();
            if (team != null) {
                team.remove(this);
            }
        }
        arena = null;
        aClass = null;
        get().setFireTicks(0);
        try {
            Bukkit.getScheduler().runTaskLater(PVPArena.instance, new Runnable() {
                @Override
                public void run() {
                    if (get() != null && get().getFireTicks() > 0) {
                        get().setFireTicks(0);
                    }
                }
            }, 5L);
        } catch (Exception e) {
        }

        clearDump();
    }

    /**
     * set the player's arena
     *
     * @param arena the arena to set
     */
    public final void setArena(final Arena arena) {
        this.arena = arena;
    }

    /**
     * set the player's arena class
     *
     * @param aClass the arena class to set
     */
    public void setArenaClass(final ArenaClass aClass) {
        final PAPlayerClassChangeEvent event = new PAPlayerClassChangeEvent(arena, get(), aClass);
        Bukkit.getServer().getPluginManager().callEvent(event);
        this.aClass = event.getArenaClass();
        if (arena != null) {
            ArenaModuleManager.parseClassChange(arena, get(), this.aClass);
        }
    }

    /**
     * set a player's arena class by name
     *
     * @param className an arena class name
     */
    public void setArenaClass(final String className) {

        for (final ArenaClass ac : arena.getClasses()) {
            if (ac.getName().equalsIgnoreCase(className)) {
                setArenaClass(ac);
                return;
            }
        }
        PVPArena.instance.getLogger().warning(
                "[PA-debug] failed to set unknown class " + className + " to player "
                        + name);
    }

    public void setBackupScoreboard(Scoreboard board) {
        backupBoard = board;
    }

    public void setBackupScoreboardTeam(Team team) {
        backupBoardTeam = team;
    }

    public void setMayDropInventory(boolean value) {
        mayDropInventory = value;
    }

    public void setNextArenaClass(ArenaClass aClass) {
        this.naClass = aClass;
    }

    public void setFlyState(boolean flyState) {
        this.flying = flyState;
    }

    public void setIgnoreAnnouncements(final boolean value) {
        ignoreAnnouncements = value;
    }

    public void setLocation(final PALocation location) {
        this.location = location;
    }

    public void setPublicChatting(final boolean chatPublic) {
        publicChatting = chatPublic;
    }

    public void setSelection(final Location loc, final boolean second) {
        if (second) {
            selection[1] = new PABlockLocation(loc);
        } else {
            selection[0] = new PABlockLocation(loc);
        }
    }

    public void setStatistic(final String arenaName, final type type,
                             final int value) {
        if (!statistics.containsKey(arenaName)) {
            statistics.put(arenaName, new PAStatMap());
        }

        final PAStatMap map = statistics.get(arenaName);
        map.setStat(type, value);
    }

    public void setStatus(final Status status) {
        debug.i(name + '>' + status.name(), name);
        this.status = status;
    }

    /**
     * hand over a player's tele pass
     *
     * @param canTeleport true if may pass, false otherwise
     */
    public void setTelePass(final boolean canTeleport) {
        if (arena != null) {
            arena.getDebugger().i("TelePass := "+canTeleport);
        }
        telePass = canTeleport;
    }

    public void setTeleporting(final boolean isTeleporting) {
        teleporting = isTeleporting;
    }

    public void showBloodParticles() {
        Player player = get();
        player.getLocation()
                .getWorld()
                .playEffect(player.getEyeLocation(), Effect.STEP_SOUND, Material.NETHER_WART_BLOCK);

    }

    @Override
    public String toString() {
        final ArenaTeam team = getArenaTeam();

        return team == null ? name : team.getColorCodeString() + name + ChatColor.RESET;
    }

    public void unsetSelection() {
        selection[0] = null;
        selection[1] = null;
    }
}
