package net.slipcor.pvparena.core;

import org.bukkit.DyeColor;
import org.bukkit.Material;
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
 * @version v0.6.15
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

		// [itemid/name]~[dmg]~[data]:[amount]

		short dmg = 0;
		byte data = 0;
		int amount = 1;
		Material mat = null;

		String[] temp = s.split(":");

		if (temp.length > 1) {
			amount = Integer.parseInt(temp[1]);
		}
		temp = temp[0].split("~");

		mat = parseMat(temp[0]);
		if (mat != null) {
			if (temp.length == 1) {
				// [itemid/name]:[amount]
				return new ItemStack(mat, amount);
			}
			dmg = Short.parseShort(temp[1]);
			if (temp.length == 2) {
				// [itemid/name]~[dmg]:[amount]
				return new ItemStack(mat, amount, dmg);
			}
			data = Byte.parseByte(temp[2]);
			if (temp.length == 3) {
				// [itemid/name]~[dmg]~[data]:[amount]
				return new ItemStack(mat, amount, dmg, data);
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
