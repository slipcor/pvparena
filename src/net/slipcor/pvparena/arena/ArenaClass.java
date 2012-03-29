package net.slipcor.pvparena.arena;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ArenaClass {
	
	private final String name;
	
	private final Material[] items;
	// ^^ vv this will be replaced by ItemStack[], my StringParse lib supports enchantments and data values :)
	private final Map<Material, Integer> amounts; 
	
	public ArenaClass(String name, Map<Material, Integer> amounts) {
		this.name = name;
		this.amounts = amounts;
		this.items = amounts.keySet().toArray(new Material[amounts.keySet().size()]);
	}
	
	public String getName() {
		return name;
	}
	
	public void load(Player player) {
		for (Material item : items) {
			player.getInventory().addItem(new ItemStack(item, amounts.get(item)));
		}
	}
}