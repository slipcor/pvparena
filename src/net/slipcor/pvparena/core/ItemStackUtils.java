package net.slipcor.pvparena.core;

import net.slipcor.pvparena.PVPArena;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bukkit.configuration.serialization.ConfigurationSerialization.deserializeObject;

public class ItemStackUtils  {
    /**
     * List of "meta-type" config keys which can be drop
     */
    private enum HandledMetaType {
        POTION, ENCHANTED, BOOK_SIGNED, BOOK, BANNER, MAP, FIREWORK, LEATHER_ARMOR, UNSPECIFIC
    }

    private static boolean isAnHandledMetaType(String metaTypeString) {
        try{
            HandledMetaType.valueOf(metaTypeString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check the meta keyset to deduce which meta-type is associated
     * @param keySet The meta config node values
     * @return Right "meta-type" value
     */
    private static HandledMetaType getRightMetaType(Collection<String> keySet) {
        if(keySet.contains("potion-type")) {
            return HandledMetaType.POTION;
        }
        if(keySet.contains("stored-enchants")) {
            return HandledMetaType.ENCHANTED;
        }
        if(keySet.contains("map-id")) {
            return HandledMetaType.MAP;
        }
        if(keySet.contains("patterns")) {
            return HandledMetaType.BANNER;
        }
        if(keySet.contains("pages")) {
            if(keySet.contains("author")) {
                return HandledMetaType.BOOK_SIGNED;
            }
            return HandledMetaType.BOOK;
        }
        if(keySet.contains("firework-effects")) {
            return HandledMetaType.FIREWORK;
        }
        if(keySet.contains("color")) {
            return HandledMetaType.LEATHER_ARMOR;
        }
        return HandledMetaType.UNSPECIFIC;
    }


    /**
     * Pseudo-serializer which creates a map version of objects in order to prettify the yaml after serialization
     * @param itemStack A bukkit ItemStack object
     * @return Serializable map
     */
    public static Map<String, Object> getItemStackMap(ItemStack itemStack) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", itemStack.getType().name());
        if (itemStack.getAmount() != 1) {
            result.put("amount", itemStack.getAmount());
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (!Bukkit.getItemFactory().equals(meta, null)) {
            Map<String, Object> metaMap = new LinkedHashMap<>(meta.serialize());
            if(isAnHandledMetaType(metaMap.get("meta-type").toString())) {
                metaMap.remove("meta-type");

                // Simplify leather armor colors
                if(meta instanceof LeatherArmorMeta) {
                    metaMap.put("color", ((LeatherArmorMeta) meta).getColor().serialize());
                }

                // Simplify custom potion effects
                if(meta instanceof PotionMeta) {
                    PotionMeta potionMeta = (PotionMeta) meta;

                    if(potionMeta.hasCustomEffects()) {
                        List<Map<String, Object>> customEffectMeta = potionMeta.getCustomEffects().stream()
                                .map(PotionEffect::serialize)
                                .collect(Collectors.toList());
                        metaMap.put("custom-effects", customEffectMeta);
                    }
                }
            }
            result.put("meta", metaMap);
        }

        return result;
    }

    /**
     * A custom deserializer which convert prettified yaml config to bukkit objects
     * Mainly based on the bukkit ConfigurationSerializable deserializer
     * @param args An ItemStack config node
     * @return A bukkit ItemStack object
     */
    private static ItemStack deserialize(Map<String, Object> args) {
        int amount = 1;

        Material type = Material.getMaterial((String) args.get("type"));

        if(type != null) {
            if (args.containsKey("amount")) {
                amount = ((Number) args.get("amount")).intValue();
            }

            ItemStack result = new ItemStack(type, amount);

            if (args.containsKey("meta")) {
                Map<String, Object> metaMap = new LinkedHashMap<>((Map<String, Object>) args.get("meta"));
                metaMap.put("==", ItemMeta.class.getSimpleName());
                if(!metaMap.containsKey("meta-type")) {
                    metaMap.put("meta-type", getRightMetaType(metaMap.keySet()).name());

                    //Simplify Leather armor colors
                    if(metaMap.containsKey("color")) {
                        Color color = Color.deserialize((Map<String, Object>) metaMap.get("color"));
                        metaMap.put("color", color);
                    }

                    // Simplify custom potion effects
                    if(metaMap.containsKey("custom-effects")) {
                        List<PotionEffect> effectList = ((List<?>) metaMap.get("custom-effects")).stream()
                                .map(serializedEffect -> new PotionEffect((Map<String, Object>) serializedEffect))
                                .collect(Collectors.toList());

                        metaMap.put("custom-effects", effectList);
                    }
                }


                ConfigurationSerializable raw = deserializeObject(metaMap);
                if (raw instanceof ItemMeta) {
                    result.setItemMeta((ItemMeta) raw);
                }
            }
            return result;
        } else {
            PVPArena.instance.getLogger().warning("Invalid item type : " + args.get("type"));
            return new ItemStack(Material.AIR);
        }
    }

    /**
     * Deserialize an ItemSTack list config node
     * @param mapList List of ItemStack nodes
     * @return An array of bukkit ItemStack objects
     */
    public static ItemStack[] getItemStacksFromConfig(List<?> mapList) {
        ItemStack[] result = new ItemStack[mapList.size()];
        for(int i = 0; i < mapList.size(); i++) {
            result[i] = deserialize((Map<String, Object>) mapList.get(i));
        }
        return result;
    }
}
