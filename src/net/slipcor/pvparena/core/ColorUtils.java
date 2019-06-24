package net.slipcor.pvparena.core;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

import static net.slipcor.pvparena.core.StringParser.joinArray;

public final class ColorUtils {

    private static final Debug DEBUG = new Debug(18);

    private ColorUtils() {
    }

    public static Material getWoolMaterialFromDyeColor(final String color) {
        return getColoredMaterial(DyeColor.valueOf(color), Material.WHITE_WOOL);
    }

    public static Material getWoolMaterialFromChatColor(final ChatColor color) {
        return getColoredMaterialFromChatColor(color, Material.WHITE_WOOL);
    }

    /**
     * Get a colored Material from a chat color and any colorable material
     * @param color Color from chat
     * @param material Colorable material like Wool, Concrete or Stained glass
     * @return
     */
    public static Material getColoredMaterialFromChatColor(final ChatColor color, final Material material) {
        /*
        Unsupported:
        ChatColor.AQUA
        Material.BROWN_WOOL
        */

        DyeColor dyeColor = getDyeColorFromChatColor(color);
        return getColoredMaterial(dyeColor, material);
    }

    public static DyeColor getDyeColorFromChatColor(ChatColor color) {
        try {
            return DyeColor.valueOf(parseDyeColorToChatColor(color.name(), false));
        } catch (IllegalArgumentException e) {
            DEBUG.i("ChatColor " + color.name() + " can't be cast to DyeColor => set BROWN");
            return DyeColor.BROWN;
        }
    }

    public static ChatColor getChatColorFromDyeColor(final String color) {
        return ChatColor.valueOf(parseDyeColorToChatColor(color, true));
    }

    private static String parseDyeColorToChatColor(final String color, final boolean forward) {
        /*
          following colors are the sames (ignore): WHITE, YELLOW, BLACK
          colors not being able to parse: chat-AQUA, wool-brown
         */

        final List<String> wool = Arrays.asList("ORANGE", "MAGENTA", "LIGHT_BLUE", "LIME", "PINK", "GRAY", "LIGHT_GRAY",
                "PURPLE", "BLUE", "GREEN", "RED", "CYAN");
        final List<String> chat = Arrays.asList("GOLD", "LIGHT_PURPLE", "BLUE", "GREEN", "RED", "DARK_GRAY", "GRAY",
                "DARK_PURPLE", "DARK_BLUE", "DARK_GREEN", "DARK_RED", "DARK_AQUA");

        if (forward) {
            if (wool.contains(color)) {
                return chat.get(wool.indexOf(color));
            }
        } else {
            if (chat.contains(color)) {
                return wool.get(chat.indexOf(color));
            }
        }
        return color;
    }

    /**
     * Check if a material can be colored
     * @param type Material to check
     * @return true if material can be colored
     */
    public static boolean isColorableMaterial(Material type) {
        return type.name().endsWith("_WOOL") || type.name().endsWith("_CONCRETE") ||
                type.name().endsWith("_STAINED_GLASS");
    }

    /**
     * Get a colored material applying dye color on a material
     * @param dyeColor DyeColor to get color
     * @param typeMaterial Material used to get type
     * @return colored material
     */
    public static Material getColoredMaterial(DyeColor dyeColor, Material typeMaterial) {
        String color = dyeColor.name();
        String[] typeNameArr = typeMaterial.name().split("_");
        String uncoloredMaterial = joinArray(Arrays.copyOfRange(typeNameArr, 1, typeNameArr.length), "_");
        return Material.valueOf(color + "_" + uncoloredMaterial);
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
