package net.slipcor.pvparena.core;

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
 * @version v0.6.39
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
		Enchantment ench = null;
		Integer enchLevel = 0;
		if (temp[0].contains("|")) {
			String[] temp2 = temp[0].split("|");
			temp[0] = temp2[0];
			
			String strEnch = temp2[1];
			if (strEnch.contains("~")) {
				String[] arrEnch = strEnch.split("~");
				ench = Enchantment.getById(Integer.parseInt(arrEnch[0]));
				enchLevel = Integer.parseInt(arrEnch[1]);
			}
		}
		
		temp = temp[0].split("~");

		mat = parseMat(temp[0]);
		if (mat != null) {
			if (temp.length == 1) {
				// [itemid/name]:[amount]
				
				ItemStack is = new ItemStack(mat, amount);
				if (ench != null)
					is.addUnsafeEnchantment(ench, enchLevel);
				return is;
			}
			dmg = Short.parseShort(temp[1]);
			if (temp.length == 2) {
				// [itemid/name]~[dmg]:[amount]
				ItemStack is = new ItemStack(mat, amount, dmg);
				if (ench != null)
					is.addUnsafeEnchantment(ench, enchLevel);
				return is;
			}
			data = Byte.parseByte(temp[2]);
			if (temp.length == 3) {
				// [itemid/name]~[dmg]~[data]:[amount]
				ItemStack is = new ItemStack(mat, amount, dmg, data);
				if (ench != null)
					is.addUnsafeEnchantment(ench, enchLevel);
				return is;
			}
		}
		return null;
	}

	/**
	 * retrieve a material from a string
	 * 
	 * @param string
	 *            the string to parse
	 * @return the material
	 */
	public static Material parseMat(String string) {
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
}
