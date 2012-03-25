package net.slipcor.pvparena.arena;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public final class PlayerState {
	
	private Player player;
	
	private int fireticks;
	private int foodlevel;
	private int gamemode;
	private int health;
	
	public PlayerState(Player player) {
		this.player = player;
		this.fireticks = player.getFireTicks();
		this.foodlevel = player.getFoodLevel();
		this.gamemode = player.getGameMode().getValue();
		this.health = player.getHealth();
	}
	
	public void load() {
		
	}
	
	public void unload() {
		player.setFireTicks(fireticks);
		player.setFoodLevel(foodlevel);
		player.setGameMode(GameMode.getByValue(gamemode));
		player.setHealth(health);
	}
}