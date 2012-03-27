package net.slipcor.pvparena.classes;


import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Powerup Class
 * 
 * -
 * 
 * For powerups to extend
 * 
 * @author NodinChan
 * @slipcor approves
 *
 */
public class Powerup {
	
	private final String name;
	
	private Effect[] effects;
	
	private Material[] materials;
	
	public Powerup(String name, Effect[] effects, Material[] materials) {
		this.name = name;
		this.effects = effects;
		this.materials = materials;
	}
	
	public void use(Player player) {}
	
	public void applyEffects(Player player) {
		for (Effect effect : effects) {
			effect.apply(player);
		}
	}
	
	public Effect[] getEffects() {
		return effects;
	}
	
	public final Material[] getMaterials() {
		return materials;
	}
	
	public final String getName() {
		return name;
	}
	
	public final void tick() {
		for (Effect effect : effects) {
			effect.tick();
		}
	}
}