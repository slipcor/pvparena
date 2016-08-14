package net.slipcor.pvparena.arena;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * <pre>Arena Player State class</pre>
 * <p/>
 * Saves and loads player data before and after the match, respectively
 *
 * @author slipcor
 */

public final class PlayerState {

    private static final Debug debug = new Debug(7);

    private final String name;

    private int fireticks;
    private int foodlevel;
    private int gamemode;
    private double health;
    private double maxhealth;
    private int explevel;

    private float exhaustion;
    private float experience;
    private float saturation;

    private boolean flying;
    private boolean collides;

    private String displayname;
    private Collection<PotionEffect> potionEffects;

    public PlayerState(final Player player) {
        name = player.getName();
        debug.i("creating PlayerState of " + name, player);

        fireticks = player.getFireTicks();
        foodlevel = player.getFoodLevel();
        gamemode = player.getGameMode().getValue();
        health = player.getHealth();
        maxhealth = player.getMaxHealth();

        exhaustion = player.getExhaustion();
        experience = player.getExp();
        explevel = player.getLevel();
        saturation = player.getSaturation();

        potionEffects = player.getActivePotionEffects();

        flying = player.isFlying();
        collides = player.isCollidable();

        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
        final Arena arena = aPlayer.getArena();

        if (arena.getArenaConfig().getBoolean(CFG.CHAT_COLORNICK)) {
            displayname = player.getDisplayName();
        }

        fullReset(arena, player);
        final int time = arena.getArenaConfig().getInt(CFG.GENERAL_TIME);
        if (time != -1) {
            player.setPlayerTime(time, false);
        }
    }

    public void dump(final YamlConfiguration cfg) {
        debug.i("backing up PlayerState of " + name, name);
        cfg.set("state.fireticks", fireticks);
        cfg.set("state.foodlevel", foodlevel);
        cfg.set("state.gamemode", gamemode);
        cfg.set("state.health", health);
        cfg.set("state.maxhealth", maxhealth);
        cfg.set("state.exhaustion", exhaustion);
        cfg.set("state.experience", experience);
        cfg.set("state.explevel", explevel);
        cfg.set("state.saturation", saturation);
        cfg.set("state.displayname", displayname);
        cfg.set("state.flying", flying);
        cfg.set("state.collides", collides);
    }

    public static void fullReset(final Arena arena, final Player player) {
        int iHealth = arena.getArenaConfig().getInt(CFG.PLAYER_HEALTH);

        if (iHealth < 1) {
            iHealth = (int) player.getMaxHealth();
        }

        if (arena.getArenaConfig().getInt(CFG.PLAYER_MAXHEALTH) > 0) {
            player.setMaxHealth(arena.getArenaConfig().getInt(CFG.PLAYER_MAXHEALTH));
        }

        if (iHealth > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        } else {
            playersetHealth(player, iHealth);
        }

        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setVelocity(new Vector());
        player.setFoodLevel(arena.getArenaConfig().getInt(CFG.PLAYER_FOODLEVEL));
        player.setSaturation(arena.getArenaConfig().getInt(CFG.PLAYER_SATURATION));
        player.setExhaustion((float) arena.getArenaConfig().getDouble(
                CFG.PLAYER_EXHAUSTION));
        player.setLevel(0);
        player.setExp(0);
        player.setGameMode(GameMode.getByValue(arena.getArenaConfig().getInt(CFG.GENERAL_GAMEMODE)));
        player.setCollidable(arena.getArenaConfig().getBoolean(CFG.PLAYER_COLLISION));
        PlayerState.removeEffects(player);

        if (arena.getArenaConfig().getBoolean(CFG.CHAT_COLORNICK)) {
            final ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
            String n;
            if (team == null) {
                n = player.getName();
            } else {
                n = team.getColorCodeString() + player.getName();
            }
            n = ChatColor.translateAlternateColorCodes('&', n);

            player.setDisplayName(n);
        }
    }

    public void unload() {
        final Player player = Bukkit.getPlayerExact(name);

        if (player == null) {
            final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(name);
            PVPArena.instance.getAgm().disconnect(aPlayer.getArena(), aPlayer);
            return;
        }
        debug.i("restoring PlayerState of " + name, player);

        player.setFireTicks(fireticks);
        player.setFoodLevel(foodlevel);
        player.setGameMode(GameMode.getByValue(gamemode));

        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
        player.setFoodLevel(foodlevel);

        if (aPlayer.getArena().getArenaConfig().getInt(CFG.PLAYER_MAXHEALTH) > 0) {
            player.setMaxHealth(maxhealth);
        }

        if (player.getMaxHealth() == maxhealth) {
            player.setHealth(health);
        } else {
            final double newHealth = player.getMaxHealth() * health / maxhealth;
            if (newHealth > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            } else {
                player.setHealth(newHealth);
            }

        }
        player.setSaturation(saturation);
        player.setGameMode(GameMode.getByValue(gamemode));
        player.setLevel(explevel);
        player.setExp(experience);
        player.setExhaustion(exhaustion);
        player.setFallDistance(0);
        player.setVelocity(new Vector());
        if (aPlayer.getArena() != null && aPlayer.getArena().getArenaConfig().getBoolean(CFG.CHAT_COLORNICK)) {
            player.setDisplayName(displayname);
        }

        if (aPlayer.getArena() != null) {
            ArenaModuleManager.unload(aPlayer.getArena(), player);
            PVPArena.instance.getAgm().unload(aPlayer.getArena(), player);
        }


        removeEffects(player);
        player.addPotionEffects(potionEffects);

        aPlayer.setTelePass(false);
        player.setFireTicks(fireticks);

        if (aPlayer.getArena() != null) {
            player.setNoDamageTicks(aPlayer.getArena().getArenaConfig().getInt(CFG.TIME_TELEPORTPROTECT) * 20);
        }
        player.resetPlayerTime();
        if (flying && !player.getAllowFlight()) {
            player.setAllowFlight(true);
        }
        player.setFlying(flying);
        player.setCollidable(collides);
    }

    /**
     * health setting method. Implemented for heroes to work right
     *
     * @param player the player to set
     * @param value  the health value
     */
    public static void playersetHealth(final Player player, final int value) {
        debug.i("setting health to " + value + "/20", player);
        if (Bukkit.getServer().getPluginManager().getPlugin("Heroes") == null) {
            player.setHealth(value);
        }
        final double current = player.getHealth();
        final double regain = value - current;

        final EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, regain,
                RegainReason.CUSTOM);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void reset() {
        debug.i("clearing PlayerState of " + name, name);
        fireticks = 0;
        foodlevel = 0;
        gamemode = 0;
        health = 0;
        maxhealth = -1;

        exhaustion = 0;
        experience = 0;
        explevel = 0;
        saturation = 0;
        displayname = null;
        potionEffects = null;
        flying = false;
        collides = false;
    }

    public static void removeEffects(final Player player) {
        for (final PotionEffect pe : player.getActivePotionEffects()) {
            player.removePotionEffect(pe.getType());
        }

    }

    public static PlayerState undump(final YamlConfiguration cfg, final String pName) {
        debug.i("restoring backed up PlayerState of " + pName, pName);
        final PlayerState pState = new PlayerState(Bukkit.getPlayer(pName));

        pState.fireticks = cfg.getInt("state.fireticks", 0);
        pState.foodlevel = cfg.getInt("state.foodlevel", 0);
        pState.gamemode = cfg.getInt("state.gamemode", 0);
        pState.health = cfg.getInt("state.health", 1);
        pState.maxhealth = cfg.getInt("state.maxhealth", -1);
        pState.exhaustion = (float) cfg.getDouble("state.exhaustion", 1);
        pState.experience = (float) cfg.getDouble("state.experience", 0);
        pState.explevel = cfg.getInt("state.explevel", 0);
        pState.saturation = (float) cfg.getDouble("state.saturation", 0);
        pState.displayname = cfg.getString("state.displayname", pName);
        pState.flying = cfg.getBoolean("state.flying", false);
        pState.collides = cfg.getBoolean("state.collides", false);

        return pState;
    }
}