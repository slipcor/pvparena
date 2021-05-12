package net.slipcor.pvparena.core;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.slipcor.pvparena.core.ItemStackUtils.getItemStackMap;

public final class Utils {
    private Utils() {
    }

    public static List<Map<String, Object>> getItemStacksFromMaterials(Material... mats) {
        List<Map<String, Object>> result = new ArrayList<>();
        for(Material mat : mats) {
            result.add(getItemStackMap(new ItemStack(mat, mat == Material.ARROW ? 64 : 1)));
        }
        return result;
    }


    public static List<Map<String, Object>> getSerializableItemStacks(ItemStack[] itemStacks) {
        List<Map<String, Object>> result = new ArrayList<>();
        for(ItemStack itemStack : itemStacks) {
            if(itemStack != null) {
                result.add(getItemStackMap(itemStack));
            }
        }
        return result;
    }

    public static List<Map<String, Object>> getSerializableItemStacks(ItemStack itemStack) {
        return getSerializableItemStacks(new ItemStack[]{itemStack});
    }

    public static Location getCenteredLocation(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() + 0.5, loc.getBlockZ() + 0.5);
    }
}
