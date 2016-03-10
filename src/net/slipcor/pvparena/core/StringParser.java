package net.slipcor.pvparena.core;

import net.slipcor.pvparena.PVPArena;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    private static final String SAFE_BREAK = "<oOo>";
    private static final String SAFE_PAGE_BREAK = "<oXxOxXo>";
    private static final String SAFE_LORE_BREAK = "<oxXxOxXxo>";

    private static final Debug DEBUG = new Debug(17);

    private StringParser() {
    }

    public static final Set<String> positive = new HashSet<>(Arrays.asList(
            "yes", "on", "true", "1"));
    public static final Set<String> negative = new HashSet<>(Arrays.asList(
            "no", "off", "false", "0"));

    private static String codeCharacters(final String string, final boolean forward) {
        final Map<String, String> findReplace = new HashMap<>();
        String result = string;
        if (forward) {
            findReplace.put(":", "<<colon>>");
            findReplace.put("~", "<<tilde>>");
            findReplace.put("|", "<<pipe>>");
            findReplace.put(",", "<<comma>>");
            findReplace.put(ChatColor.COLOR_CHAR + "", "&");
        } else {
            findReplace.put("<<colon>>", ":");
            findReplace.put("<<tilde>>", "~");
            findReplace.put("<<pipe>>", "|");
            findReplace.put("<<comma>>", ",");
            result = ChatColor.translateAlternateColorCodes('&', result);
            result = ChatColor.translateAlternateColorCodes('?', result);
        }

        for (final Map.Entry<String, String> stringStringEntry : findReplace.entrySet()) {
            result = result.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
        }

        return result;
    }

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
        if (string == null || string != null && string.isEmpty() || "none".equals(string)) {
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

    /**
     * construct an itemstack out of a string
     *
     * @param string the formatted string: [itemid/name][~[dmg]]~[data]:[amount]
     * @return the itemstack
     */
    public static ItemStack getItemStackFromString(final String string) {
        DEBUG.i("parsing itemstack string: " + string);

        // [itemid/name]~[dmg]|[enchantmentID]~level:[amount]


        String[] temp = string.split(":");

        String prefix = null;

        for (final String s : PVPArena.instance.getConfig().getStringList("materialprefixes")) {
            if (string.startsWith(prefix+ ':')) {
                prefix = s;
                temp = string.substring(prefix.length()).split(":");
            }
        }

        String desc = null;
        int amount = 1;
        if (temp.length > 1) {
            try {
                amount = Integer.parseInt(temp[1]);
            } catch (final NumberFormatException e) {
                PVPArena.instance.getLogger().severe("Material error. Maybe add " + temp[0] + " to materialprefixes?");
                return new ItemStack(Material.AIR);
            }
            if (temp.length > 2) {
                desc = temp[2];
            }
        }

        final Map<Enchantment, Integer> enchants = new HashMap<>();
        if (temp[0].contains("|")) {
            DEBUG.i("trying to add enchantment");
            final String[] temp2 = temp[0].split("\\|");
            DEBUG.i("temp2 length: " + temp2.length);
            temp[0] = temp2[0];

            DEBUG.i("correcting item temp to " + temp[0]);

            for (int i = 1; i < temp2.length; i++) {

                final String strEnch = temp2[i];
                if (strEnch.contains("~")) {
                    final String[] arrEnch = strEnch.split("~");
                    final Enchantment ench = Enchantment.getById(Integer
                            .parseInt(arrEnch[0]));
                    final Integer enchLevel = Integer.parseInt(arrEnch[1]);
                    DEBUG.i("adding enchantment " + ench.getName() + " lvl "
                            + enchLevel);
                    enchants.put(ench, enchLevel);
                }
            }
        }

        temp = temp[0].split("~");

        final Material mat = parseMat(prefix == null ? temp[0] : prefix + ':' + temp[0]);
        if (mat != null) {
            if (temp.length == 1) {
                // [itemid/name]:[amount]

                final ItemStack itemStack = new ItemStack(mat, amount);
                for (final Map.Entry<Enchantment, Integer> enchantmentIntegerEntry : enchants.entrySet()) {
                    DEBUG.i("processing enchantment " + enchantmentIntegerEntry.getKey().getName());
                    itemStack.addUnsafeEnchantment(enchantmentIntegerEntry.getKey(), enchantmentIntegerEntry.getValue());
                }

                if (desc != null) {
                    final ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName(codeCharacters(desc, false));
                    itemStack.setItemMeta(meta);
                }

                return itemStack;
            }
            final short dmg = Short.parseShort(temp[1]);
            if (temp.length == 2) {
                // [itemid/name]~[dmg]:[amount]
                final ItemStack itemStack = new ItemStack(mat, amount, dmg);
                for (final Map.Entry<Enchantment, Integer> enchantmentIntegerEntry : enchants.entrySet()) {
                    itemStack.addUnsafeEnchantment(enchantmentIntegerEntry.getKey(), enchantmentIntegerEntry.getValue());
                }

                if (desc != null) {
                    final ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName(codeCharacters(desc, false));
                    itemStack.setItemMeta(meta);
                }

                return itemStack;
            }
            // string: POTION~0~INVISIBILITYx0x300<oOo>~<oxXxOxXxo>Duration 15 seconds.:2:Stealth

            // ---> split(":");

            // temp[0] = POTION~0~INVISIBILITYx0x300<oOo>~<oxXxOxXxo>Duration 15 seconds.
            // temp[1] = 2
            // temp[2] = Stealth

            // ---> split("~");

            // temp[0] = POTION
            // temp[1] = 0
            // temp[2] = INVISIBILITYx0x300<oOo>
            // temp[3] = <oxXxOxXxo>Duration 15 seconds.

            final int location;

            if (temp.length > 3 && temp[3].contains(SAFE_LORE_BREAK)) {
                location = 3;
            } else {
                location = 2;
            }

            final String[] dataSplit = temp[location].split(SAFE_LORE_BREAK);
            String data = dataSplit[0];
            if (temp[2].contains(SAFE_BREAK)) {
                if (mat == Material.POTION || mat == Material.BANNER || mat == Material.SHIELD) {
                    data = temp[2];
                } else {
                    data = temp[2].split(SAFE_BREAK)[0];
                }
            }

            if (data.equals("")) {
                // Data is messed up, let's try to fix it!

                if (mat == Material.INK_SACK || mat == Material.WOOL) {
                    data = temp[2]; // 2 or 3 definately is set as data, should work!
                }
            }

            if (mat == Material.WRITTEN_BOOK) {
                data = temp[2];
            }

            /**
             *
             private static final String SAFE_BREAK = "<oOo>";
             private static final String SAFE_PAGE_BREAK = "<oXxOxXo>";
             private static final String SAFE_LORE_BREAK = "<oxXxOxXxo>";
             *
             */

            final String lore = dataSplit.length > 1 ? dataSplit[1] : null;

            if (temp.length >= 3) {
                // [itemid/name]~[dmg]~[data]:[amount]
                final ItemStack itemStack = new ItemStack(mat, amount, dmg);


                if (desc != null) {
                    final ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName(codeCharacters(desc, false));
                    itemStack.setItemMeta(meta);
                }

                if (mat == Material.INK_SACK) {
                    try {
                        itemStack.setData(new Dye(Byte.parseByte(data)));
                    } catch (final Exception e) {
                        DEBUG.i(
                                "invalid dye data: " + data);
                        return itemStack;
                    }
                } else if (mat == Material.WOOL) {
                    try {
                        itemStack.setData(new Wool(Byte.parseByte(data)));
                    } catch (final Exception e) {
                        PVPArena.instance.getLogger().warning(
                                "invalid wool data: " + data);
                        return itemStack;
                    }
                } else if (mat == Material.WRITTEN_BOOK
                        || mat == Material.BOOK_AND_QUILL) {
                    final BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
                    try {
                        final String[] outer = data.split(SAFE_BREAK);
                        bookMeta.setAuthor(codeCharacters(outer[0], false));
                        bookMeta.setTitle(codeCharacters(outer[1], false));
                        final List<String> pages = new ArrayList<>();
                        final String[] inner = codeCharacters(outer[2], false).split(
                                SAFE_PAGE_BREAK);
                        Collections.addAll(pages, inner);
                        bookMeta.setPages(pages);
                        itemStack.setItemMeta(bookMeta);
                    } catch (final Exception e) {
                        PVPArena.instance.getLogger().warning(
                                "invalid book data: " + data);
                        return itemStack;
                    }
                } else if (itemStack.getType().name().startsWith("LEATHER_")) {
                    try {
                        final LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemStack
                                .getItemMeta();
                        leatherMeta.setColor(Color.fromRGB(Integer.parseInt(data)));
                        itemStack.setItemMeta(leatherMeta);
                    } catch (final Exception e) {
                        DEBUG.i(
                                "invalid leather data: " + data);
                        return itemStack;
                    }
                } else if (itemStack.getType() == Material.SKULL_ITEM) {
                    try {
                        final SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                        skullMeta.setOwner(data);
                        itemStack.setItemMeta(skullMeta);
                    } catch (final Exception e) {
                        PVPArena.instance.getLogger().warning(
                                "invalid skull data: " + data);
                        return itemStack;
                    }
                } else if (itemStack.getType() == Material.POTION) {
                    // data = NAMEx1x100<oOo>NAMEx2x100
                    try {
                        final PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

                        final String[] defs = data.split(SAFE_BREAK);

                        for (final String def : defs) {
                            final String[] vals = def.split("x");
                            potionMeta.addCustomEffect(
                                    new PotionEffect(
                                            PotionEffectType.getByName(vals[0]),
                                            Integer.parseInt(vals[2]),
                                            Integer.parseInt(vals[1])), true);
                        }

                        if (lore != null
                                && !(mat == Material.WRITTEN_BOOK || mat == Material.BOOK_AND_QUILL)) {
                            final List<String> lLore = new ArrayList<>();
                            for (final String line : lore.split(SAFE_BREAK)) {
                                lLore.add(codeCharacters(line, false));
                            }
                            potionMeta.setLore(lLore);
                        }

                        itemStack.setItemMeta(potionMeta);
                    } catch (final Exception e) {
                        PVPArena.instance.getLogger().warning(
                                "invalid potion data: " + data);
                        return itemStack;
                    }
                } else if (itemStack.getType() == Material.BANNER) {
                    // data = ?<oOo>?x?<oOo>?x? { ? are ordinals }
                    try {
                        final BannerMeta bannerMeta = (BannerMeta) itemStack.getItemMeta();

                        final String[] defs = data.split(SAFE_BREAK);

                        boolean firstDone = false;
                        for (final String def : defs) {
                            if (!firstDone) {
                                Integer i = Integer.parseInt(def);
                                bannerMeta.setBaseColor(DyeColor.values()[i]);
                                firstDone = true;
                                continue;
                            }
                            final String[] vals = def.split("x");
                            int dyeColor = Integer.parseInt(vals[0]);
                            int patternType = Integer.parseInt(vals[1]);
                            bannerMeta.addPattern(new Pattern(DyeColor.values()[dyeColor], PatternType.values()[patternType]));

                        }
                        if (lore != null) {
                            final List<String> lLore = new ArrayList<>();
                            for (final String line : lore.split(SAFE_BREAK)) {
                                lLore.add(codeCharacters(line, false));
                            }
                            bannerMeta.setLore(lLore);
                        }
                        itemStack.setItemMeta(bannerMeta);
                    } catch (Exception e) {
                        PVPArena.instance.getLogger().warning(
                                "invalid banner data: " + data);
                        return itemStack;
                    }
                } else if (itemStack.getType() == Material.SHIELD) {
                    // data = ?<oOo>?x?<oOo>?x? { ? are ordinals }
                    try {
                        final BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();
                        Banner bannerState = (Banner) meta.getBlockState();

                        final String[] defs = data.split(SAFE_BREAK);
                        boolean firstDone = false;
                        for (final String def : defs) {
                            if (!firstDone) {
                                Integer i = Integer.parseInt(def);
                                bannerState.setBaseColor(DyeColor.values()[i]);
                                firstDone = true;
                                continue;
                            }
                            final String[] vals = def.split("x");
                            int dyeColor = Integer.parseInt(vals[0]);
                            int patternType = Integer.parseInt(vals[1]);
                            Pattern maybe = new Pattern(DyeColor.values()[dyeColor], PatternType.values()[patternType]);
                            bannerState.addPattern(maybe);
                        }
                        bannerState.update();
                        meta.setBlockState(bannerState);
                        if (lore != null) {
                            final List<String> lLore = new ArrayList<>();
                            for (final String line : lore.split(SAFE_BREAK)) {
                                lLore.add(codeCharacters(line, false));
                            }
                            meta.setLore(lLore);
                        }
                        itemStack.setItemMeta(meta);
                    } catch (Exception e) {
                        PVPArena.instance.getLogger().warning(
                                "invalid banner data: " + data);
                        return itemStack;
                    }
                } else {
                    DEBUG.i("data not available for: " + mat.name());
                }

                if (lore != null
                        && !(mat == Material.WRITTEN_BOOK || mat == Material.BOOK_AND_QUILL)) {
                    final List<String> lLore = new ArrayList<>();
                    for (final String line : lore.split(SAFE_BREAK)) {
                        lLore.add(codeCharacters(line, false));
                    }
                    final ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setLore(lLore);
                    itemStack.setItemMeta(itemMeta);
                }

                for (final Map.Entry<Enchantment, Integer> enchantmentIntegerEntry : enchants.entrySet()) {
                    itemStack.addUnsafeEnchantment(enchantmentIntegerEntry.getKey(), enchantmentIntegerEntry.getValue());
                }

                return itemStack;
            }
        }
        return null;
    }

    public static ItemStack[] getItemStacksFromString(final String string) {
        if ("none".equals(string)) {
            return new ItemStack[0];
        }

        final String[] args = string.split(",");

        final ItemStack[] result = new ItemStack[args.length];

        int pos = 0;

        for (final String s : args) {
            result[pos++] = getItemStackFromString(s);
        }

        return result;
    }

    public static String getStringFromItemStacks(final ItemStack[] isItems) {
        if (isItems == null) {
            return "AIR";
        }
        final String[] split = new String[isItems.length];

        int pos = 0;

        for (final ItemStack is : isItems) {
            split[pos++] = getStringFromItemStack(is);
        }

        return joinArray(trimAir(split), ",");
    }

    private static String[] trimAir(final String[] sArray) {
        final List<String> list = new ArrayList<>();
        for (final String item : sArray) {
            if ("AIR".equals(item)) {
                continue;
            }
            list.add(item);
        }

        if (list.size() < 1) {
            return new String[]{"AIR"};
        }

        final String[] result = new String[list.size()];
        int pos = 0;
        for (final String item : list) {
            result[pos++] = item;
        }

        return result;
    }

    public static String getStringFromItemStack(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return "AIR";
        }
        final StringBuilder temp = new StringBuilder(itemStack.getType().name());
        boolean durability = false;
        if (itemStack.getDurability() != 0) {
            temp.append('~');
            temp.append(itemStack.getDurability());
            durability = true;
        }
        if (itemStack.getType() == Material.INK_SACK || itemStack.getType() == Material.WOOL) {
            if (!durability) {
                temp.append('~');
                temp.append(itemStack.getDurability());
                durability = true;
            }
            temp.append('~');
            temp.append(itemStack.getData().getData());
        } else if (itemStack.getType() == Material.WRITTEN_BOOK
                || itemStack.getType() == Material.BOOK_AND_QUILL) {
            if (!durability) {
                temp.append('~');
                temp.append(itemStack.getDurability());
                durability = true;
            }
            final BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
            if (bookMeta != null && bookMeta.getAuthor() != null && bookMeta.getTitle() != null
                    && bookMeta.getPages() != null) {
                temp.append('~');
                temp.append(codeCharacters(bookMeta.getAuthor(), true));
                temp.append(SAFE_BREAK);
                temp.append(codeCharacters(bookMeta.getTitle(), true));
                temp.append(SAFE_BREAK);
                temp.append(codeCharacters(
                        joinArray(bookMeta.getPages().toArray(),
                                SAFE_PAGE_BREAK), true));

            }
        } else if (itemStack.getType().name().startsWith("LEATHER_")) {
            if (!durability) {
                temp.append('~');
                temp.append(itemStack.getDurability());
                durability = true;
            }
            final LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            temp.append('~');
            temp.append(leatherMeta.getColor().asRGB());
        } else if (itemStack.getType() == Material.SKULL_ITEM) {
            if (!durability) {
                temp.append('~');
                temp.append(itemStack.getDurability());
                durability = true;
            }
            final SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            temp.append('~');
            temp.append(skullMeta.getOwner());
        } else if (itemStack.getType() == Material.POTION) {
            if (!durability) {
                temp.append('~');
                temp.append(itemStack.getDurability());
                durability = true;
            }
            final PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            temp.append('~');
            for (final PotionEffect pe : potionMeta.getCustomEffects()) {
                temp.append(pe.getType().getName()).append('x').append(pe.getAmplifier()).append('x').append(pe.getDuration());
                temp.append(SAFE_BREAK);
            }
        } else if (itemStack.getType() == Material.BANNER) {
            if (!durability) {
                temp.append('~');
                temp.append(itemStack.getDurability());
                durability = true;
            }
            try {
                final BannerMeta bannerMeta = (BannerMeta) itemStack.getItemMeta();
                temp.append('~');
                temp.append(bannerMeta.getBaseColor().ordinal());
                temp.append(SAFE_BREAK);
                for (Pattern p : bannerMeta.getPatterns()) {
                    temp.append(p.getColor().ordinal());
                    temp.append("x");
                    temp.append(p.getPattern().ordinal());
                    temp.append(SAFE_BREAK);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (itemStack.getType() == Material.SHIELD) {
            if (!durability) {
                temp.append('~');
                temp.append(itemStack.getDurability());
                durability = true;
            }
            try {
                BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();
                Banner bannerState = (Banner) meta.getBlockState();
                temp.append('~');
                temp.append(bannerState.getBaseColor().ordinal());
                temp.append(SAFE_BREAK);
                for (Pattern p : bannerState.getPatterns()) {
                    temp.append(p.getColor().ordinal());
                    temp.append("x");
                    temp.append(p.getPattern().ordinal());
                    temp.append(SAFE_BREAK);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
            if (!durability) {
                temp.append('~');
                temp.append(itemStack.getDurability());
                durability = true;
            }

            temp.append('~');
            temp.append(SAFE_LORE_BREAK);
            temp.append(codeCharacters(
                    joinArray(itemStack.getItemMeta().getLore()
                            .toArray(), SAFE_BREAK), true));
        }
        final Map<Enchantment, Integer> enchants = itemStack.getEnchantments();

        if (enchants != null && !enchants.isEmpty()) {
            for (final Map.Entry<Enchantment, Integer> enchantmentIntegerEntry : enchants.entrySet()) {
                temp.append('|');
                temp.append(enchantmentIntegerEntry.getKey().getId());
                temp.append('~');
                temp.append(enchantmentIntegerEntry.getValue());
            }
        }

        if (itemStack.getAmount() > 1 || itemStack.getItemMeta().hasDisplayName()) {
            temp.append(':');
            temp.append(itemStack.getAmount());
        }

        if (itemStack.getItemMeta().hasDisplayName()) {
            temp.append(':');
            temp.append(codeCharacters(itemStack.getItemMeta().getDisplayName(), true));
        }

        return temp.toString().replace(ChatColor.COLOR_CHAR, '&');
    }

    private static String getWoolEnumFromChatColorEnum(final String color) {
        return parseDyeColorToChatColor(color, false);
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

        /**
         * wool colors: ORANGE, MAGENTA, LIGHT_BLUE, LIME, PINK, GRAY, SILVER,
         * PURPLE, BLUE, GREEN, RED, CYAN;
         *
         * chat colors: GOLD, LIGHT_PURPLE, BLUE, GREEN, RED, DARK_GRAY, GRAY,
         * DARK_PURPLE, DARK_BLUE, DARK_GREEN, DARK_RED, DARK_AQUA
         *
         *
         *
         * both colors (ignore): WHITE, YELLOW, BLACK
         *
         * colors not being able to parse:
         *
         * chat-AQUA, wool-brown
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

    /**
     * retrieve a material from a string
     *
     * @param string the string to parse
     * @return the material
     */
    private static Material parseMat(final String string) {
        DEBUG.i("parsing material: " + string);
        Material mat;
        try {
            mat = Material.getMaterial(Integer.parseInt(string));
            if (mat == null) {
                mat = Material.getMaterial(string);
            }
        } catch (final Exception e) {
            mat = Material.getMaterial(string);
        }
        if (mat == null) {
            PVPArena.instance.getLogger().warning(
                    "unrecognized material: " + string);
        }
        return mat;
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

}
