package net.slipcor.pvparena.core;

import net.slipcor.pvparena.PVPArena;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;

/**
 * <pre>
 * String Parser class
 * </pre>
 * <p/>
 * provides methods to parse Objects to String and back
 *
 * @author slipcor
 * @version v0.10.2
 */

public final class StringParser {

    private static final Debug DEBUG = new Debug(17);

    private StringParser() {
    }

    public static final Set<String> positive = new HashSet<>(Arrays.asList(
            "yes", "on", "true", "1"));
    public static final Set<String> negative = new HashSet<>(Arrays.asList(
            "no", "off", "false", "0"));

    public static String colorize(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string)
                .replace("&&", "&").replace("%%&%%", "&");
    }

    public static String[] colorize(final List<String> stringList) {
        final String[] result = new String[stringList.size()];

        for (int i = 0; i < stringList.size(); i++) {
            result[i] = colorize(stringList.get(i));
        }
        return result;
    }

    /**
     * color an integer if bigger than 0
     *
     * @param timed the integer to color
     * @return a colored string
     */
    public static String colorVar(final int timed) {
        return colorVar(String.valueOf(timed), timed > 0);
    }

    /**
     * color a boolean based on value
     *
     * @param value the boolean to color
     * @return a colored string
     */
    public static String colorVar(final boolean value) {
        return colorVar(String.valueOf(value), value);
    }

    public static String colorVar(final double value) {
        return colorVar(String.valueOf(value), value > 0);
    }

    /**
     * color a string if set
     *
     * @param string the string to color
     * @return a colored string
     */
    public static String colorVar(final String string) {
        if (string == null || string.isEmpty() || "none".equals(string)) {
            return colorVar("null", false);
        }
        return colorVar(string, true);
    }

    /**
     * color a string based on a given boolean
     *
     * @param string the string to color
     * @param value  true:green, false:red
     * @return a colored string
     */
    public static String colorVar(final String string, final boolean value) {
        return (value ? ChatColor.GREEN.toString() : ChatColor.RED.toString()) + string
                + ChatColor.WHITE;
    }

    /**
     * calculate a WOOL byte from a color enum
     *
     * @param color the string to parse
     * @return the color short
     */
    public static byte getColorDataFromENUM(final String color) {

        String wool = getWoolEnumFromChatColorEnum(color);
        if (wool == null) {
            wool = color;
        }
        /*
		 * DyeColor supports: WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME,
		 * PINK, GRAY, SILVER, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK;
		 */

        for (final DyeColor dc : DyeColor.values()) {
            if (dc.name().equalsIgnoreCase(wool)) {
                return (byte) (15 - dc.getDyeData());
            }
        }
        PVPArena.instance.getLogger().warning("unknown color enum: " + wool);

        return (byte) 0;
    }

    public static ChatColor getChatColorFromWoolEnum(final String color) {
        return ChatColor.valueOf(parseDyeColorToChatColor(color, true));
    }


    private static String getWoolEnumFromChatColorEnum(final String color) {
        return parseDyeColorToChatColor(color, false);
    }

    public static Material getWoolFallbackMaterialFromString(final String color) {
        for (Material mat : Material.values()) {
            if (mat.name().contains("WOOL") && mat.name().contains(color.toUpperCase())) {
                return mat;
            }
        }
        DEBUG.i(">> Material defaulting '"+color+"' to BROWN_WOOL!! <<");
        return Material.BROWN_WOOL;
    }

    public static String joinArray(final Object[] array, final String glue) {
        final StringBuilder result = new StringBuilder("");
        for (final Object o : array) {
            result.append(glue);
            result.append(o);
        }
        if (result.length() <= glue.length()) {
            return result.toString();
        }
        return result.substring(glue.length());
    }

    public static String joinList(final List<?> set, final String glue) {
        final StringBuilder result = new StringBuilder("");
        for (final Object o : set) {
            result.append(glue);
            result.append(o);
        }
        if (result.length() <= glue.length()) {
            return result.toString();
        }
        return result.substring(glue.length());
    }

    public static String joinSet(final Set<?> set, final String glue) {
        final StringBuilder result = new StringBuilder("");
        for (final Object o : set) {
            result.append(glue);
            result.append(o);
        }
        if (result.length() <= glue.length()) {
            return result.toString();
        }
        return result.substring(glue.length());
    }

    private static String parseDyeColorToChatColor(final String color, final boolean forward) {

        /*
          wool colors: ORANGE, MAGENTA, LIGHT_BLUE, LIME, PINK, GRAY, SILVER,
          PURPLE, BLUE, GREEN, RED, CYAN;

          chat colors: GOLD, LIGHT_PURPLE, BLUE, GREEN, RED, DARK_GRAY, GRAY,
          DARK_PURPLE, DARK_BLUE, DARK_GREEN, DARK_RED, DARK_AQUA

          both colors (ignore): WHITE, YELLOW, BLACK

          colors not being able to parse:

          chat-AQUA, wool-brown
         */
        final String[] wool = {"ORANGE", "MAGENTA", "LIGHT_BLUE",
                "LIME", "PINK", "GRAY", "SILVER", "PURPLE", "BLUE",
                "GREEN", "RED", "CYAN"};
        final String[] chat = {"GOLD", "LIGHT_PURPLE", "BLUE",
                "GREEN", "RED", "DARK_GRAY", "GRAY", "DARK_PURPLE", "DARK_BLUE",
                "DARK_GREEN", "DARK_RED", "DARK_AQUA"};

        if (forward) {
            for (int i = 0; i < wool.length; i++) {
                if (color.equals(wool[i])) {
                    return chat[i];
                }
            }
        } else {

            for (int i = 0; i < chat.length; i++) {
                if (color.equals(chat[i])) {
                    return wool[i];
                }
            }
        }

        return color;
    }

    public static String[] shiftArrayBy(final String[] args, final int offset) {
        final String[] newArgs = new String[args.length - offset];
        System.arraycopy(args, offset, newArgs, 0, args.length - offset);
        return newArgs;
    }

    public static String[] unShiftArrayBy(final String[] args, final int offset) {
        final String[] newArgs = new String[args.length + offset];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return newArgs;
    }

    public static BlockFace parseToBlockFace(final String string) {
        if (string.startsWith("o") || string.startsWith("i")) {
            // out or in are just the same thing, reference to self with positive/negative values
            return BlockFace.SELF;
        }
        final BlockFace[] faces = {
                BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
                BlockFace.UP, BlockFace.DOWN};
        for (final BlockFace face : faces) {
            if (face.name().startsWith(string.toUpperCase())) {
                return face;
            }
        }
        return null;
    }

    public static String[] splitForScoreBoard(String key) {
        String[] split = new String[]{"","",""};

        int pos = 0;
        StringBuffer buffer = new StringBuffer("");

        for (char c : key.toCharArray()) {
            buffer.append(c);
            if (c == ChatColor.COLOR_CHAR) {
                if (buffer.length() >= 15) {
                    if (pos > 2) {
                        return split;
                    }
                    split[pos++] = buffer.toString();
                    buffer.setLength(0);
                }
                continue;
            }

            if (buffer.length() >= 16) {
                if (pos > 2) {
                    return split;
                }
                split[pos++] = buffer.toString();
                buffer.setLength(0);
            }
        }
        split[pos] = buffer.toString();
        return split;
    }

    public static String getItems(ItemStack[] items) {
        if (items == null || items.length < 1) {
            return "none";
        }
        StringBuffer buff = new StringBuffer();
        for (ItemStack item : items) {
            buff.append(", ");
            try {
                buff.append(item.getType());
                buff.append('x');
                buff.append(item.getAmount());
            } catch (Exception e) {
                buff.append("!error!");
            }
        }
        return buff.substring(2);
    }
}
