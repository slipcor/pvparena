package net.slipcor.pvparena.arena;

import java.util.LinkedList;
import java.util.List;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * <pre>Arena Class class</pre>
 * 
 * contains Arena Class methods and variables for quicker access
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public final class ArenaClass {
	
	private static Debug db = new Debug(4);

	private final String name;
	private final ItemStack[] items;

	// private statics: item definitions
	private static final List<Material> ARMORS_TYPE = new LinkedList<Material>();
	private static final List<Material> HELMETS_TYPE = new LinkedList<Material>();
	private static final List<Material> CHESTPLATES_TYPE = new LinkedList<Material>();
	private static final List<Material> LEGGINGS_TYPE = new LinkedList<Material>();
	private static final List<Material> BOOTS_TYPE = new LinkedList<Material>();

	// static filling of the items array
	static {
		HELMETS_TYPE.add(Material.LEATHER_HELMET);
		HELMETS_TYPE.add(Material.GOLD_HELMET);
		HELMETS_TYPE.add(Material.CHAINMAIL_HELMET);
		HELMETS_TYPE.add(Material.IRON_HELMET);
		HELMETS_TYPE.add(Material.DIAMOND_HELMET);
		HELMETS_TYPE.add(Material.WOOL);

		CHESTPLATES_TYPE.add(Material.LEATHER_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.GOLD_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.CHAINMAIL_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.IRON_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.DIAMOND_CHESTPLATE);

		LEGGINGS_TYPE.add(Material.LEATHER_LEGGINGS);
		LEGGINGS_TYPE.add(Material.GOLD_LEGGINGS);
		LEGGINGS_TYPE.add(Material.CHAINMAIL_LEGGINGS);
		LEGGINGS_TYPE.add(Material.IRON_LEGGINGS);
		LEGGINGS_TYPE.add(Material.DIAMOND_LEGGINGS);

		BOOTS_TYPE.add(Material.LEATHER_BOOTS);
		BOOTS_TYPE.add(Material.GOLD_BOOTS);
		BOOTS_TYPE.add(Material.CHAINMAIL_BOOTS);
		BOOTS_TYPE.add(Material.IRON_BOOTS);
		BOOTS_TYPE.add(Material.DIAMOND_BOOTS);

		ARMORS_TYPE.addAll(HELMETS_TYPE);
		ARMORS_TYPE.addAll(CHESTPLATES_TYPE);
		ARMORS_TYPE.addAll(LEGGINGS_TYPE);
		ARMORS_TYPE.addAll(BOOTS_TYPE);
	}
	
	public static void equip(Player player, ItemStack[] items) {
		db.i("Equipping player " + player.getName() + " with items!");
		for (ItemStack item : items) {
			if (ARMORS_TYPE.contains(item.getType())) {
				equipArmor(item, player.getInventory());
			} else {
				player.getInventory().addItem(new ItemStack[] { item });
				db.i("- " + StringParser.getStringFromItemStack(item));
			}
		}
	}

	public void equip(Player player) {
		equip(player, items);
	}

	private static void equipArmor(ItemStack stack, PlayerInventory inv) {
		db.i("- " + StringParser.getStringFromItemStack(stack));
		Material type = stack.getType();
		if (HELMETS_TYPE.contains(type)) {
			inv.setHelmet(stack);
		} else if (CHESTPLATES_TYPE.contains(type)) {
			inv.setChestplate(stack);
		} else if (LEGGINGS_TYPE.contains(type)) {
			inv.setLeggings(stack);
		} else if (BOOTS_TYPE.contains(type)) {
			inv.setBoots(stack);
		}
	}

	public ArenaClass(String className, ItemStack[] classItems) {
		this.name = className;
		this.items = classItems.clone();
	}

	public String getName() {
		return name;
	}

	public ItemStack[] getItems() {
		return items;
	}
}