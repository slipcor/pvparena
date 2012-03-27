package net.slipcor.pvparena.arena;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

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