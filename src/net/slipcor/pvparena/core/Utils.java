package net.slipcor.pvparena.core;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class Utils {
    private Utils() {
    }

    public static ItemStack[] getItemStacksFromMaterials(Material... mats) {
        ItemStack[] result = new ItemStack[mats.length];
        for (int i=0; i< mats.length; i++) {
            result[i] = new ItemStack(mats[i], mats[i]==Material.ARROW?64:1);
        }
        return result;
    }

    public static Material getWoolMaterialFromChatColor(final ChatColor color) {
        /*
        Unsupported:
        ChatColor.AQUA
        Material.BROWN_WOOL
        */

        switch (color) {
            case BLACK:
                return Material.BLACK_WOOL;
            case DARK_BLUE:
                return Material.BLUE_WOOL;
            case DARK_GREEN:
                return Material.GREEN_WOOL;
            case DARK_AQUA:
                return Material.CYAN_WOOL;
            case DARK_RED:
                return Material.RED_WOOL;
            case DARK_PURPLE:
                return Material.PURPLE_WOOL;
            case GOLD:
                return Material.ORANGE_WOOL;
            case GRAY:
                return Material.LIGHT_GRAY_WOOL;
            case DARK_GRAY:
                return Material.GRAY_WOOL;
            case BLUE:
                return Material.LIGHT_BLUE_WOOL;
            case GREEN:
                return Material.LIME_WOOL;
            case RED:
                return Material.PINK_WOOL;
            case LIGHT_PURPLE:
                return Material.MAGENTA_WOOL;
            case YELLOW:
                return Material.YELLOW_WOOL;
            case WHITE:
            default:
                return Material.WHITE_WOOL;
        }
    }

    public static boolean isSubType(Material type, Material check) {
        if (type.name().endsWith("_WOOL") && check.name().endsWith("_WOOL")) {
            return true;
        }
        if (type.name().endsWith("_CONCRETE") && check.name().endsWith("_CONCRETE")) {
            return true;
        }
        if (type.name().endsWith("_STAINED_GLASS") && check.name().endsWith("_STAINED_GLASS")) {
            return true;
        }
        return false;
    }
}
