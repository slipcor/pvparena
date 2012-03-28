package net.slipcor.pvparena.arena;

import java.util.Collection;

import net.slipcor.pvparena.listeners.EntityListener;
import net.slipcor.pvparena.managers.Players;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * player state class
 * 
 * -
 * 
 * contains player state methods and variables in order to save and load a player state
 * 
 * @author slipcor
 * 
 * @version v0.7.0
 * 
 */

public final class PlayerState {
	
	private Player player;
	
	// I'll add saturation and stuff you missed, but I think I understood what you were
	// doing in here ^^
	
	// though I'm wondering what you gain from switching 2*5 lines to a whole new class to
	// do the same-except you dont need the player because you saved it in the first place
	
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
		
		ArenaPlayer ap = Players.parsePlayer(player);
		
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
		

		ArenaPlayer ap = Players.parsePlayer(player);
		player.setFoodLevel(foodlevel);
		player.setHealth(health);
		player.setSaturation(saturation);
		player.setGameMode(GameMode.getByValue(gamemode));
		player.setLevel(explevel);
		player.setExp(experience);
		player.setExhaustion(exhaustion);
		if (ap.getArena().cfg.getBoolean("messages.colorNick", true)) {
			player.setDisplayName(displayname);
		}

		for (PotionEffect pe : player.getActivePotionEffects()) {
			player.removePotionEffect(pe.getType());
		}

		player.addPotionEffects(potionEffects);

		Players.setTelePass(player, false);
		EntityListener.addBurningPlayer(player);
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