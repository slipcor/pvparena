package net.slipcor.pvparena.arena;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.SpawnEgg;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <pre>Arena Class class</pre>
 * <p/>
 * contains Arena Class methods and variables for quicker access
 *
 * @author slipcor
 * @version v0.10.2
 */

public final class ArenaClass {

    private static final Debug debug = new Debug(4);

    private final String name;
    private final ItemStack[] items;
    private final ItemStack[] armors;

    private static final Map<String, ArenaClass> globals = new HashMap<String, ArenaClass>();

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
        final File classFile = new File(PVPArena.instance.getDataFolder(), "classes.yml");
        final YamlConfiguration cfg = YamlConfiguration.loadConfiguration(classFile);

        cfg.addDefault("classes.Ranger",
                "261,262:64,298,299,300,301");
        cfg.addDefault("classes.Swordsman", "276,306,307,308,309");
        cfg.addDefault("classes.Tank", "272,310,311,312,313");
        cfg.addDefault("classes.Pyro", "259,46:3,298,299,300,301");

        cfg.options().copyDefaults();
        try {
            cfg.save(classFile);
        } catch (final IOException e1) {
            e1.printStackTrace();
        }

        for (final String className : cfg.getConfigurationSection("classes").getKeys(false)) {
            final String sItemList;

            try {
                sItemList = (String) cfg.getConfigurationSection("classes").get(className);
            } catch (final Exception e) {
                Bukkit.getLogger().severe(
                        "[PVP Arena] Error while parsing class, skipping: "
                                + className);
                continue;
            }
            final String[] sItems = sItemList.split(",");
            final ItemStack[] items = new ItemStack[sItems.length];
            final ItemStack[] armors = new ItemStack[4];

            for (int i = 0; i < sItems.length; i++) {

                if (sItems[i].contains(">>!<<")) {
                    final String[] split = sItems[i].split(">>!<<");

                    final int id = Integer.parseInt(split[0]);
                    armors[id] = StringParser.getItemStackFromString(split[1]);

                    if (armors[id] == null) {
                        PVPArena.instance.getLogger().warning(
                                "unrecognized armor item: " + split[1]);
                    }

                    sItems[i] = "AIR";
                }

                items[i] = StringParser.getItemStackFromString(sItems[i]);
                if (items[i] == null) {
                    PVPArena.instance.getLogger().warning(
                            "unrecognized item: " + items[i]);
                }
            }
            globals.put(className, new ArenaClass(className, items, armors));
        }
    }

    public static void addGlobalClasses(final Arena arena) {
        for (final Map.Entry<String, ArenaClass> stringArenaClassEntry : globals.entrySet()) {
            arena.addClass(stringArenaClassEntry.getKey(), stringArenaClassEntry.getValue().getItems(), stringArenaClassEntry.getValue().getArmors());
        }
    }

    public static void equip(final Player player, final ItemStack[] items) {
        for (final ItemStack item : items) {
            if (ARMORS_TYPE.contains(item.getType())) {
                equipArmor(item, player.getInventory());
            } else {
                if (item.getType() == Material.MONSTER_EGG && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && "SPAWN".equals(item.getItemMeta().getDisplayName())) {
                    final SpawnEgg egg = (SpawnEgg) item.getData();

                    try {
                        Bukkit.getScheduler().runTaskLater(PVPArena.instance, new Runnable(){
                            @Override
                            public void run() {
                                ArenaPlayer.parsePlayer(player.getName()).getArena().addEntity(player, player.getWorld().spawnEntity(player.getLocation(), egg.getSpawnedType()));
                            }
                        }, 20L);
                    } catch(final IllegalPluginAccessException e) {

                    }
                } else {
                    player.getInventory().addItem(item);
                }
                debug.i("- " + StringParser.getStringFromItemStack(item), player);
            }
        }
        player.updateInventory();
    }

    public void equip(final Player player) {
        debug.i("Equipping player " + player.getName() + " with items!", player);
        player.getInventory().setArmorContents(armors);
        for (final ItemStack item : items) {
            if (item == null) {
                continue;
            }
            if (ARMORS_TYPE.contains(item.getType())) {
                equipArmor(item, player.getInventory());
            } else {
                player.getInventory().addItem(item);
                debug.i("- " + StringParser.getStringFromItemStack(item), player);
            }
        }
        player.updateInventory();
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

    public ArenaClass(final String className, final ItemStack[] classItems, final ItemStack[] armors) {
        name = className;
        items = classItems.clone();
        this.armors = armors.clone();
    }

    public String getName() {
        return name;
    }

    public ItemStack[] getArmors() {
        return armors.clone();
    }

    public ItemStack[] getItems() {
        return items.clone();
    }
}