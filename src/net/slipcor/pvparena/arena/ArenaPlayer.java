package net.slipcor.pvparena.arena;

import java.util.ArrayList;
import java.util.List;

import net.slipcor.pvparena.classes.Effect;

import org.bukkit.entity.Player;

public final class ArenaPlayer {
	
	private final String name;
	
	private final Arena arena;
	
	private ArenaClass aClass;
	
	private final PlayerState state;
	
	private final List<Effect> effects;
	
	public ArenaPlayer(Player player, Arena arena) {
		this.name = player.getName();
		this.state = new PlayerState(player);
		this.arena = arena;
		this.effects = new ArrayList<Effect>();
	}
	
	public void addEffect(Effect effect) {
		effects.add(effect);
	}
	
	public Arena getArena() {
		return arena;
	}
	
	public ArenaClass getArenaClass() {
		return aClass;
	}
	
	public List<Effect> getEffects() {
		return effects;
	}
	
	public String getName() {
		return name;
	}
	
	public PlayerState getState() {
		return state;
	}
	
	public void setArenaClass(ArenaClass aClass) {
		this.aClass = aClass;
	}
	
	public final class EffectScheduler implements Runnable {
		// one runnable for every player.. mhh... yeah... that might improve it ^^
		@Override
		public void run() {
			List<Effect> removalPending = new ArrayList<Effect>();
			
			for (Effect effect : effects) {
				effect.tick();
				if (effect.expired())
					removalPending.add(effect);
			}
			
			effects.removeAll(removalPending);
		}
	}
}