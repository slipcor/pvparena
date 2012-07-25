package net.slipcor.pvparena.core;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * string parser class
 * 
 * -
 * 
 * parses strings to other objects
 * 
 * @author slipcor
 * 
 * @version v0.8.11
 * 
 */

public class StringParser {

	public static final Debug db = new Debug(4);

	/**
	 * calculate a color short from a color enum
	 * 
	 * @param color
	 *            the string to parse
	 * @return the color short
	 */
	public static byte getColorDataFromENUM(String color) {
		
		String wool = getWoolEnumFromChatColorEnum(color);
		
		if (wool != null) {
			color = wool;
		}
		/*
		 * DyeColor supports: WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME,
		 * PINK, GRAY, SILVER, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK;
		 */
		for (DyeColor dc : DyeColor.values()) {
			if (dc.name().equalsIgnoreCase(color))
				return dc.getData();
		}
		db.w("unknown color enum: " + color);

		return (short) 0;
	}

	public static ChatColor getChatColorFromWoolEnum(String color) {
		return ChatColor.valueOf(parseDyeColorToChatColor(color, true));
	}

	public static String getWoolEnumFromChatColorEnum(String color) {
		return parseDyeColorToChatColor(color, false);
	}
	
	private static String parseDyeColorToChatColor(String color, boolean forward) {
		
		/**
		 * wool colors:
		 * ORANGE, MAGENTA, LIGHT_BLUE, LIME, PINK, GRAY,
		 * SILVER, PURPLE, BLUE, GREEN, RED, CYAN;
		 * 
		 * chat colors:
		 * GOLD, LIGHT_PURPLE, BLUE, GREEN, RED, DARK_GRAY,
		 * GRAY, DARK_PURPLE, DARK_BLUE, DARK_GREEN, DARK_RED, DARK_AQUA
		 * 
		 *     
		 * 
		 * both colors (ignore):
		 * WHITE, YELLOW, BLACK
		 * 
		 * colors not being able to parse:
		 * 
		 * chat-AQUA, wool-brown
		 */
		String[] wool = new String[] {"ORANGE","MAGENTA","LIGHT_BLUE","LIME",
				"PINK","GRAY","SILVER","PURPLE",
				"BLUE","GREEN","RED","CYAN"};
		String[] chat = new String[] {"GOLD","LIGHT_PURPLE","BLUE","GREEN",
				"RED","DARK_GRAY","GRAY","DARK_PURPLE",
				"DARK_BLUE","DARK_GREEN","DARK_RED","DARK_AQUA"};
		
		if (forward) {
			for (int i = 0; i<wool.length; i++) {
				if (color.equals(wool[i])) {
					return chat[i];
				}
			}
		} else {

			for (int i = 0; i<chat.length; i++) {
				if (color.equals(chat[i])) {
					return wool[i];
				}
			}
		}
		
		return color;
	}

	/**
	 * construct an itemstack out of a string
	 * 
	 * @param s
	 *            the formatted string: [itemid/name][~[dmg]]~[data]:[amount]
	 * @return the itemstack
	 */
	public static ItemStack getItemStackFromString(String s) {
		db.i("parsing itemstack string: " + s);

		// [itemid/name]~[dmg]|[enchantmentID]~level:[amount]

		short dmg = 0;
		byte data = 0;
		int amount = 1;
		Material mat = null;

		String[] temp = s.split(":");

		if (temp.length > 1) {
			amount = Integer.parseInt(temp[1]);
		}
		HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
		if (temp[0].contains("|")) {
			db.i("trying to add enchantment");
			String[] temp2 = temp[0].split("\\|");
			db.i("temp2 length: " + temp2.length);
			temp[0] = temp2[0];

			db.i("correcting item temp to " + temp[0]);
			
			for (int i = 1; i < temp2.length; i++) {
			
				String strEnch = temp2[i];
				if (strEnch.contains("~")) {
					String[] arrEnch = strEnch.split("~");
					Enchantment ench = Enchantment.getById(Integer.parseInt(arrEnch[0]));
					Integer enchLevel = Integer.parseInt(arrEnch[1]);
					db.i("adding enchantment " + ench.getName() + " lvl " + enchLevel);
					enchants.put(ench, enchLevel);
				}
			}
		}

		temp = temp[0].split("~");

		mat = parseMat(temp[0]);
		if (mat != null) {
			if (temp.length == 1) {
				// [itemid/name]:[amount]

				ItemStack is = new ItemStack(mat, amount);
				for (Enchantment e : enchants.keySet()) {
					db.i("processing enchantment " + e.getName());
					is.addUnsafeEnchantment(e, enchants.get(e));
				}
				return is;
			}
			dmg = Short.parseShort(temp[1]);
			if (temp.length == 2) {
				// [itemid/name]~[dmg]:[amount]
				ItemStack is = new ItemStack(mat, amount, dmg);
				for (Enchantment e : enchants.keySet()) {
					is.addUnsafeEnchantment(e, enchants.get(e));
				}
				return is;
			}
			data = Byte.parseByte(temp[2]);
			if (temp.length == 3) {
				// [itemid/name]~[dmg]~[data]:[amount]
				ItemStack is = new ItemStack(mat, amount, dmg, data);
				for (Enchantment e : enchants.keySet()) {
					is.addUnsafeEnchantment(e, enchants.get(e));
				}
				return is;
			}
		}
		return null;
	}
	
	public static ItemStack[] getItemStacksFromString(String string) {
		String[] args = string.split(",");
		
		ItemStack[] result = new ItemStack[args.length];
		
		int i = 0;
		
		for (String s : args) {
			result[i++] = getItemStackFromString(s);
		}
		
		return result;
	}

	/**
	 * retrieve a material from a string
	 * 
	 * @param string
	 *            the string to parse
	 * @return the material
	 */
	private static Material parseMat(String string) {
		db.i("parsing material: " + string);
		Material mat;
		try {
			mat = Material.getMaterial(Integer.parseInt(string));
			if (mat == null) {
				mat = Material.getMaterial(string);
			}
		} catch (Exception e) {
			mat = Material.getMaterial(string);
		}
		if (mat == null) {
			db.w("unrecognized material: " + string);
		}
		return mat;
	}

	public static String parseArray(String[] args) {
		String result = "";
		for (String s : args) {
			result += " " + s;
		}
		return result;
	}

	/**
	 * color a string based on a given boolean
	 * 
	 * @param s
	 *            the string to color
	 * @param b
	 *            true:green, false:red
	 * @return a colored string
	 */
	public static String colorVar(String s, boolean b) {
		return (b ? (ChatColor.GREEN + "") : (ChatColor.RED + "")) + s
				+ ChatColor.WHITE;
	}
	
	public static String colorize(String toColor)
    {
    	// Removes color codes from a string
        return toColor.replaceAll("&([a-zA-Z0-9])", "§$1").replace("&&", "&");
    }

	/**
	 * color a string if set
	 * 
	 * @param s
	 *            the string to color
	 * @return a colored string
	 */
	public static String colorVar(String s) {
		if (s == null || s.equals("") || s.equals("none")) {
			return colorVar("null", false);
		}
		return colorVar(s, true);
	}

	/**
	 * color an integer if bigger than 0
	 * 
	 * @param timed
	 *            the integer to color
	 * @return a colored string
	 */
	public static String colorVar(int timed) {
		return colorVar(String.valueOf(timed), timed > 0);
	}

	/**
	 * color a boolean based on value
	 * 
	 * @param b
	 *            the boolean to color
	 * @return a colored string
	 */
	public static String colorVar(boolean b) {
		return colorVar(String.valueOf(b), b);
	}

	public static String colorVar(double timed) {
		return colorVar(String.valueOf(timed), timed > 0);
	}

	public static String getStringFromItemStacks(ItemStack[] isItems) {
		String[] s = new String[isItems.length];
		
		int i = 0;
		
		for (ItemStack is : isItems) {
			String temp = is.getType().name();
			if (is.getDurability() != 0) {
				temp += "~" + String.valueOf(is.getDurability());
			}
			Map<Enchantment, Integer> enchants = is.getEnchantments();
			
			if (enchants != null && enchants.size() > 0) {
				for (Enchantment e : enchants.keySet()) {
					temp += "|" + String.valueOf(e.getId()) + "~" + enchants.get(e);
				}
			}
			
			if (is.getAmount() > 1) {
				temp += ":" + is.getAmount();
			}
			s[i++] = temp; 
		}
		
		return parseArray(s);
	}
}
