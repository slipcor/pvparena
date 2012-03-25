package net.slipcor.pvparena.classes;

import org.bukkit.entity.Player;

public class Effect {
	
	private final String name;
	
	private final long duration;
	
	public Effect(String name, long duration) {
		this.name = name;
		this.duration = duration + System.currentTimeMillis();
	}
	
	public void apply(Player player) {}
	
	public final String getName() {
		return name;
	}
	
	public boolean expired() {
		return duration - System.currentTimeMillis() <= 0;
	}
	
	public void tick() {}
}