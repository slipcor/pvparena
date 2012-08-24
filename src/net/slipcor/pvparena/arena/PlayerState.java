package net.slipcor.pvparena.arena;

import java.util.Collection;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * player state class
 * 
 * -
 * 
 * contains player state methods and variables in order to save and load a
 * player state
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 * 
 */

public final class PlayerState {
	
	private static Debug db = new Debug(7);

	private String sPlayer;

	private int fireticks;
	private int foodlevel;
	private int gamemode;
	private int health;

	private float exhaustion;
	private float experience;
	private int explevel;
	private float saturation;
	private String displayname;
	private Collection<PotionEffect> potionEffects;

	public PlayerState(Player player) {
		this.sPlayer = player.getName();

		this.fireticks = player.getFireTicks();
		this.foodlevel = player.getFoodLevel();
		this.gamemode = player.getGameMode().getValue();
		this.health = player.getHealth();

		this.exhaustion = player.getExhaustion();
		this.experience = player.getExp();
		this.explevel = player.getLevel();
		this.saturation = player.getSaturation();

		this.potionEffects = player.getActivePotionEffects();

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);

		if (ap.getArena().getArenaConfig().getBoolean("messages.colorNick", true)) {
			this.displayname = player.getDisplayName();
		}
	}

	public void dump(YamlConfiguration cfg) {
		cfg.set("state.fireticks", fireticks);
		cfg.set("state.foodlevel", foodlevel);
		cfg.set("state.gamemode", gamemode);
		cfg.set("state.health", health);
		cfg.set("state.exhaustion", exhaustion);
		cfg.set("state.experience", experience);
		cfg.set("state.explevel", explevel);
		cfg.set("state.saturation", saturation);
		cfg.set("state.displayname", displayname);
	}

	public void unload() {
		Player player = Bukkit.getPlayer(sPlayer);
		
		if (player == null) {
			return;
		}
		
		player.setFireTicks(fireticks);
		player.setFoodLevel(foodlevel);
		player.setGameMode(GameMode.getByValue(gamemode));
		player.setHealth(health);

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
		player.setFoodLevel(foodlevel);
		player.setHealth(health);
		player.setSaturation(saturation);
		player.setGameMode(GameMode.getByValue(gamemode));
		player.setLevel(explevel);
		player.setExp(experience);
		player.setExhaustion(exhaustion);
		if (ap.getArena() != null && ap.getArena().getArenaConfig().getBoolean("messages.colorNick", true)) {
			player.setDisplayName(displayname);
		}
		
		if (ap.getArena() != null) {
			PVPArena.instance.getAmm().unload(player);
			PVPArena.instance.getAgm().unload(ap.getArena(), player);
		}
		

		removeEffects(player);
		player.addPotionEffects(potionEffects);

		ap.setTelePass(false);
		player.setFireTicks(fireticks);
		player.setNoDamageTicks(60);
	}

	public void reset() {
		fireticks = 0;
		foodlevel = 0;
		gamemode = 0;
		health = 0;

		exhaustion = 0;
		experience = 0;
		explevel = 0;
		saturation = 0;
		displayname = null;
		potionEffects = null;
	}

	public static void removeEffects(Player player) {
		for (PotionEffect pe : player.getActivePotionEffects()) {
			//player.addPotionEffect(new PotionEffect(pe.getType(), 0, 0));
			player.removePotionEffect(pe.getType());
		}

	}

	public static PlayerState undump(YamlConfiguration cfg, String pName) {
		PlayerState ps = new PlayerState(Bukkit.getPlayer(pName));
		

		ps.fireticks = cfg.getInt("state.fireticks", 0);
		ps.foodlevel = cfg.getInt("state.foodlevel", 0);
		ps.gamemode = cfg.getInt("state.gamemode", 0);
		ps.health = cfg.getInt("state.health", 1);
		ps.exhaustion = (float) cfg.getDouble("state.exhaustion", 1);
		ps.experience = (float) cfg.getDouble("state.experience", 0);
		ps.explevel = cfg.getInt("state.explevel", 0);
		ps.saturation = (float) cfg.getDouble("state.saturation", 0);
		ps.displayname = cfg.getString("state.displayname", pName);
		
		return ps;
	}
}