package net.slipcor.pvparena.arena;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static net.slipcor.pvparena.core.ItemStackUtils.getItemStacksFromConfig;

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
    private final ItemStack offHand;
    private final ItemStack[] armors;

    private static final Map<String, ArenaClass> globals = new HashMap<>();

    // private statics: item definitions
    private static final List<Material> ARMORS_TYPE = new LinkedList<>();
    private static final List<Material> HELMETS_TYPE = new LinkedList<>();
    private static final List<Material> CHESTPLATES_TYPE = new LinkedList<>();
    private static final List<Material> LEGGINGS_TYPE = new LinkedList<>();
    private static final List<Material> BOOTS_TYPE = new LinkedList<>();

    // static filling of the items array
    static {
        HELMETS_TYPE.add(Material.LEATHER_HELMET);
        HELMETS_TYPE.add(Material.GOLDEN_HELMET);
        HELMETS_TYPE.add(Material.CHAINMAIL_HELMET);
        HELMETS_TYPE.add(Material.IRON_HELMET);
        HELMETS_TYPE.add(Material.DIAMOND_HELMET);

        HELMETS_TYPE.add(Material.BLACK_WOOL);
        HELMETS_TYPE.add(Material.BLUE_WOOL);
        HELMETS_TYPE.add(Material.BROWN_WOOL);
        HELMETS_TYPE.add(Material.CYAN_WOOL);
        HELMETS_TYPE.add(Material.GRAY_WOOL);
        HELMETS_TYPE.add(Material.GREEN_WOOL);
        HELMETS_TYPE.add(Material.LIGHT_BLUE_WOOL);
        HELMETS_TYPE.add(Material.LIGHT_GRAY_WOOL);
        HELMETS_TYPE.add(Material.LIME_WOOL);
        HELMETS_TYPE.add(Material.MAGENTA_WOOL);
        HELMETS_TYPE.add(Material.ORANGE_WOOL);
        HELMETS_TYPE.add(Material.PINK_WOOL);
        HELMETS_TYPE.add(Material.PURPLE_WOOL);
        HELMETS_TYPE.add(Material.RED_WOOL);
        HELMETS_TYPE.add(Material.WHITE_WOOL);
        HELMETS_TYPE.add(Material.YELLOW_WOOL);

        HELMETS_TYPE.add(Material.PUMPKIN);
        HELMETS_TYPE.add(Material.JACK_O_LANTERN);
        HELMETS_TYPE.add(Material.PLAYER_HEAD);

        CHESTPLATES_TYPE.add(Material.LEATHER_CHESTPLATE);
        CHESTPLATES_TYPE.add(Material.GOLDEN_CHESTPLATE);
        CHESTPLATES_TYPE.add(Material.CHAINMAIL_CHESTPLATE);
        CHESTPLATES_TYPE.add(Material.IRON_CHESTPLATE);
        CHESTPLATES_TYPE.add(Material.DIAMOND_CHESTPLATE);
        CHESTPLATES_TYPE.add(Material.ELYTRA);

        LEGGINGS_TYPE.add(Material.LEATHER_LEGGINGS);
        LEGGINGS_TYPE.add(Material.GOLDEN_LEGGINGS);
        LEGGINGS_TYPE.add(Material.CHAINMAIL_LEGGINGS);
        LEGGINGS_TYPE.add(Material.IRON_LEGGINGS);
        LEGGINGS_TYPE.add(Material.DIAMOND_LEGGINGS);

        BOOTS_TYPE.add(Material.LEATHER_BOOTS);
        BOOTS_TYPE.add(Material.GOLDEN_BOOTS);
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
                "BOW,ARROW:64,LEATHER_HELMET,LEATHER_CHESTPLATE,LEATHER_LEGGINGS,LEATHER_BOOTS");
        cfg.addDefault("classes.Swordsman",
                "DIAMOND_SWORD,IRON_HELMET,IRON_CHESTPLATE,IRON_LEGGINGS,IRON_BOOTS");
        cfg.addDefault("classes.Tank",
                "STONE_SWORD,DIAMOND_HELMET,DIAMOND_CHESTPLATE,DIAMOND_LEGGINGS,DIAMOND_BOOTS");
        cfg.addDefault("classes.Pyro",
                "FLINT_AND_STEEL,TNT:3,LEATHER_HELMET,LEATHER_CHESTPLATE,LEATHER_LEGGINGS,LEATHER_BOOTS");

        cfg.options().copyDefaults();
        try {
            cfg.save(classFile);
        } catch (final IOException e1) {
            e1.printStackTrace();
        }

        for (final String className : cfg.getConfigurationSection("classes").getKeys(false)) {
            ItemStack[] items;
            ItemStack offHand;
            ItemStack[] armors;

            try {
                items = getItemStacksFromConfig(cfg.getConfigurationSection("classes").getConfigurationSection(className)
                        .getList("items"));
                offHand = getItemStacksFromConfig(cfg.getConfigurationSection("classes").getConfigurationSection(className)
                        .getList("items"))[0];
                armors = getItemStacksFromConfig(cfg.getConfigurationSection("classes").getConfigurationSection(className)
                        .getList("items"));
            } catch (final Exception e) {
                Bukkit.getLogger().severe(
                        "[PVP Arena] Error while parsing class, skipping: "
                                + className);
                e.printStackTrace();
                continue;
            }
            final String classChest;
            try {
                classChest = (String) cfg.getConfigurationSection("classchests").get(className);
                PABlockLocation loc = new PABlockLocation(classChest);
                Chest c = (Chest) loc.toLocation().getBlock().getState();
                ItemStack[] contents = c.getInventory().getContents();
                items = Arrays.copyOfRange(contents, 0, contents.length-5);
                offHand = contents[contents.length-5];
                armors = Arrays.copyOfRange(contents, contents.length-4, contents.length);
                globals.put(className, new ArenaClass(className, items, offHand, armors));
            } catch (Exception e) {
                globals.put(className, new ArenaClass(className, items, offHand, armors));
            }
        }
    }

    public static void addGlobalClasses(final Arena arena) {
        for (final Map.Entry<String, ArenaClass> stringArenaClassEntry : globals.entrySet()) {
            arena.addClass(stringArenaClassEntry.getKey(), stringArenaClassEntry.getValue().items, stringArenaClassEntry.getValue().offHand, stringArenaClassEntry.getValue().armors);
        }
    }

    public static void equip(final Player player, final ItemStack[] items) {
        int i = 0;
        for (final ItemStack item : items) {
            if (ARMORS_TYPE.contains(item.getType())) {
                equipArmor(item, player.getInventory());
            } else {
                if (i == items.length - 1) {
                    player.getInventory().setItemInOffHand(item);
                    continue;
                }
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && "SPAWN".equals(item.getItemMeta().getDisplayName())) {
                    final String eggType = item.getType().name().replace("_SPAWN_EGG", "");

                    try {
                        Bukkit.getScheduler().runTaskLater(PVPArena.instance, new Runnable(){
                            @Override
                            public void run() {
                                ArenaPlayer.parsePlayer(player.getName()).getArena().addEntity(
                                        player, player.getWorld().spawnEntity(player.getLocation(), EntityType.valueOf(eggType)));
                            }
                        }, 20L);
                    } catch(final IllegalPluginAccessException e) {

                    }
                } else {
                    player.getInventory().addItem(item);
                }
            }
            i++;
        }
    }

    public static void equip(final Player player, final ItemStack[][] itemArray) {
        player.getInventory().setItemInOffHand(itemArray[1][0]);
        player.getInventory().setArmorContents(itemArray[2]);

        for (final ItemStack item : itemArray[0]) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && "SPAWN".equals(item.getItemMeta().getDisplayName())) {
                final String eggType = item.getType().name().replace("_SPAWN_EGG", "");

                try {
                    Bukkit.getScheduler().runTaskLater(PVPArena.instance, new Runnable(){
                        @Override
                        public void run() {
                            ArenaPlayer.parsePlayer(player.getName()).getArena().addEntity(
                                    player, player.getWorld().spawnEntity(player.getLocation(), EntityType.valueOf(eggType)));
                        }
                    }, 20L);
                } catch(final IllegalPluginAccessException e) {

                }
            } else {
                player.getInventory().addItem(item);
            }
        }
    }

    public void equip(final Player player) {
        debug.i("Equipping player " + player.getName() + " with items!", player);
        for (ItemStack item : armors) {
            if (item != null) {
                equipArmor(item, player.getInventory());
            }
        }
        for (final ItemStack item : items) {
            if (item == null) {
                continue;
            }
            if (ARMORS_TYPE.contains(item.getType())) {
                equipArmor(item, player.getInventory());
            } else {
                player.getInventory().addItem(item);
            }
        }
        player.getInventory().setItemInOffHand(offHand);
    }

    private static void equipArmor(final ItemStack stack, final PlayerInventory inv) {
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

    /**
     * Backwards compatible offhand-less implementation of the constructor
     *
     * @deprecated use {@link #ArenaClass(String, ItemStack[], ItemStack, ItemStack[])} } instead.
     */
    @Deprecated
    public ArenaClass(final String className, final ItemStack[] classItems, final ItemStack[] armors) {
        this(className, classItems, null, armors);
    }

    public ArenaClass(final String className, final ItemStack[] classItems, final ItemStack offHand, final ItemStack[] armors) {
        name = className;
        this.offHand = offHand;
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