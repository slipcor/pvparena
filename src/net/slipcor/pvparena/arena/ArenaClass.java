package net.slipcor.pvparena.arena;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
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
 * @version v0.10.2
 */

public final class ArenaClass {
	
	private static Debug debug = new Debug(4);

	private final String name;
	private final ItemStack[] items;
	
	private static final Map<String, ItemStack[]> globals = new HashMap<String, ItemStack[]>();

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
		HELMETS_TYPE.add(Material.PUMPKIN);
		HELMETS_TYPE.add(Material.JACK_O_LANTERN);
		HELMETS_TYPE.add(Material.SKULL_ITEM);

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
	
	public static void addGlobalClasses() {
		globals.clear();
		File classFile = new File(PVPArena.instance.getDataFolder(), "classes.yml");
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(classFile);
		
		cfg.addDefault("Ranger",
				"261,262:64,298,299,300,301");
		cfg.addDefault("Swordsman", "276,306,307,308,309");
		cfg.addDefault("Tank", "272,310,311,312,313");
		cfg.addDefault("Pyro", "259,46:3,298,299,300,301");
		
		cfg.options().copyDefaults();
		try {
			cfg.save(classFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		for (String className : cfg.getKeys(false)) {
			String sItemList = "";

			try {
				sItemList = (String) cfg.get(className);
			} catch (Exception e) {
				Bukkit.getLogger().severe(
						"[PVP Arena] Error while parsing class, skipping: "
								+ className);
				continue;
			}
			final String[] sItems = sItemList.split(",");
			final ItemStack[] items = new ItemStack[sItems.length];

			for (int i = 0; i < sItems.length; i++) {
				items[i] = StringParser.getItemStackFromString(sItems[i]);
				if (items[i] == null) {
					PVPArena.instance.getLogger().warning(
							"unrecognized item: " + items[i]);
				}
			}
			globals.put(className, items);
		}
	}
	
	public static void addGlobalClasses(Arena arena) {
		for (String teamName : globals.keySet()) {
			arena.addClass(teamName, globals.get(teamName));
		}
	}

	@SuppressWarnings("deprecation")
	public static void equip(final Player player, final ItemStack[] items) {
		debug.i("Equipping player " + player.getName() + " with items!", player);
		for (ItemStack item : items) {
			if (ARMORS_TYPE.contains(item.getType())) {
				equipArmor(item, player.getInventory());
			} else {
				player.getInventory().addItem(new ItemStack[] { item });
				debug.i("- " + StringParser.getStringFromItemStack(item), player);
			}
		}
		player.updateInventory();
	}

	public void equip(final Player player) {
		equip(player, items);
	}

	private static void equipArmor(final ItemStack stack, final PlayerInventory inv) {
		debug.i("- " + StringParser.getStringFromItemStack(stack), (Player) inv.getHolder());
		final Material type = stack.getType();
		if (HELMETS_TYPE.contains(type)) {
			if (inv.getHelmet() != null && inv.getHelmet().getType() != Material.AIR) {
				inv.addItem(stack);
			} else {
				inv.setHelmet(stack);
			}
		} else if (CHESTPLATES_TYPE.contains(type)) {
			if (inv.getChestplate() != null && inv.getChestplate().getType() != Material.AIR) {
				inv.addItem(stack);
			} else {
				inv.setChestplate(stack);
			}
		} else if (LEGGINGS_TYPE.contains(type)) {
			if (inv.getLeggings() != null && inv.getLeggings().getType() != Material.AIR) {
				inv.addItem(stack);
			} else {
				inv.setLeggings(stack);
			}
		} else if (BOOTS_TYPE.contains(type)) {
			if (inv.getBoots() != null && inv.getBoots().getType() != Material.AIR) {
				inv.addItem(stack);
			} else {
				inv.setBoots(stack);
			}
		}
	}

	public ArenaClass(final String className, final ItemStack[] classItems) {
		this.name = className;
		this.items = classItems.clone();
	}

	public String getName() {
		return name;
	}

	public ItemStack[] getItems() {
		return items.clone();
	}
}