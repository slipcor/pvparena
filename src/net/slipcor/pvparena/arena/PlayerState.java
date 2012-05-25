package net.slipcor.pvparena.arena;

import java.util.Collection;

import net.slipcor.pvparena.PVPArena;
import org.bukkit.GameMode;
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
 * @version v0.7.8
 * 
 */

public final class PlayerState {

	private Player player;

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
		this.player = player;

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

		if (ap.getArena().cfg.getBoolean("messages.colorNick", true)) {
			this.displayname = player.getDisplayName();
		}
	}

	public void load() {

	}

	public void unload() {
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
		if (ap.getArena() != null && ap.getArena().cfg.getBoolean("messages.colorNick", true)) {
			player.setDisplayName(displayname);
		}
		
		if (ap.getArena() != null) {
			PVPArena.instance.getAmm().unload(player);
			ap.getArena().type().unload(player);
		}
		

		for (PotionEffect pe : player.getActivePotionEffects()) {
			//player.addPotionEffect(new PotionEffect(pe.getType(), 0, 0));
			player.removePotionEffect(pe.getType());
		}

		player.addPotionEffects(potionEffects);

		ArenaPlayer.parsePlayer(player).setTelePass(false);
		//EntityListener.addBurningPlayer(player);
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
}