package net.slipcor.pvparena.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;

/**
 * <pre>String Parser class</pre>
 * 
 * provides methods to parse Objects to String and back
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class StringParser {
	static String SAFE_BREAK = "<oOo>";
	static String SAFE_PAGE_BREAK = "<oXxOxXo>";
	static String SAFE_LORE_BREAK = "<oxXxOxXxo>";

	public static final Debug db = new Debug(17);

	public static HashSet<String> positive = new HashSet<String>(Arrays.asList("yes", "on", "true", "1"));
	public static HashSet<String> negative = new HashSet<String>(Arrays.asList("no", "off", "false", "0"));


	private static String codeCharacters(String string, boolean forward) {
		HashMap<String, String> findReplace = new HashMap<String, String>();
		if (forward) {
			findReplace.put(":", "<<colon>>");
			findReplace.put("~", "<<tilde>>");
			findReplace.put("|", "<<pipe>>");
			findReplace.put(",", "<<comma>>");
		} else {
			findReplace.put("<<colon>>",":");
			findReplace.put("<<tilde>>","~");
			findReplace.put("<<pipe>>","|");
			findReplace.put("<<comma>>",",");
			string = ChatColor.translateAlternateColorCodes('?', string);
		}
		
		for (String s : findReplace.keySet()) {
			string = string.replace(s, findReplace.get(s));
		}
		
		return string;
	}
	
	public static String colorize(String s) {
		return ChatColor.translateAlternateColorCodes('&', s).replace("&&", "&");
	}

	public static String[] colorize(List<String> stringList) {
		String[] result = new String[stringList.size()];
		
		for (int i = 0; i < stringList.size(); i++) {
			result[i] = colorize(stringList.get(i));
		}
		return result;
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
			if (dc.name().equalsIgnoreCase(color)) {
				return dc.getData();
                        }
		}
		PVPArena.instance.getLogger().warning("unknown color enum: " + color);

		return (short) 0;
	}

	public static ChatColor getChatColorFromWoolEnum(String color) {
		return ChatColor.valueOf(parseDyeColorToChatColor(color, true));
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
		String data = null;
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
			String[] dataSplit = temp[2].split(SAFE_LORE_BREAK);
			data = dataSplit[0];
			String lore = dataSplit.length > 1 ? dataSplit[1] : null;
			if (temp.length == 3) {
				// [itemid/name]~[dmg]~[data]:[amount]
				ItemStack is = new ItemStack(mat, amount, dmg);
				
				if (mat == Material.INK_SACK) {
					try {
						is.setData(new Dye(Byte.parseByte(data)));
					} catch (Exception e) {
						PVPArena.instance.getLogger().warning("invalid dye data: " + data);
						return is;
					}
				} else if (mat == Material.WOOL) {
					try {
						is.setData(new Wool(Byte.parseByte(data)));
					} catch (Exception e) {
						PVPArena.instance.getLogger().warning("invalid wool data: " + data);
						return is;
					}
				} else if (mat == Material.WRITTEN_BOOK || mat == Material.BOOK_AND_QUILL) {
					BookMeta bm = (BookMeta) is.getItemMeta();
					try {
						String[] outer = data.split(SAFE_BREAK);
						bm.setAuthor(codeCharacters(outer[0],false));
						bm.setTitle(codeCharacters(outer[1],false));
						List<String> pages = new ArrayList<String>();
						String[] inner = codeCharacters(outer[2],false).split(SAFE_PAGE_BREAK);
						for (String ss : inner) {
							pages.add(ss);
						}
						bm.setPages(pages);
						is.setItemMeta(bm);
					} catch (Exception e) {
						PVPArena.instance.getLogger().warning("invalid book data: " + data);
						return is;
					}
				} else if (is.getType().name().startsWith("LEATHER_")) {
					try {
						LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
						lam.setColor(Color.fromRGB(Integer.parseInt(data)));
						is.setItemMeta(lam);
					} catch (Exception e) {
						PVPArena.instance.getLogger().warning("invalid leather data: " + data);
						return is;
					}
				} else if (is.getType() == Material.SKULL_ITEM) {
					try {
					SkullMeta sm = (SkullMeta) is.getItemMeta();
					sm.setOwner(data);
					is.setItemMeta(sm);
					} catch (Exception e) {
						PVPArena.instance.getLogger().warning("invalid leather data: " + data);
						return is;
					}
				} else {
					PVPArena.instance.getLogger().warning("data not available for: " + mat.name());
				}
				
				if (lore != null && !(mat == Material.WRITTEN_BOOK || mat == Material.BOOK_AND_QUILL)) {
					List<String> lLore = new ArrayList<String>();
					for (String line : lore.split(SAFE_BREAK)) {
						lLore.add(codeCharacters(line, false));
					}
					ItemMeta im = is.getItemMeta();
					im.setLore(lLore);
					is.setItemMeta(im);
				}
				
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

	public static String getStringFromItemStacks(ItemStack[] isItems) {
		if (isItems == null) {
			return "AIR";
		}
		String[] s = new String[isItems.length];
		
		int i = 0;
		
		for (ItemStack is : isItems) {
			s[i++] = getStringFromItemStack(is); 
		}
		
		return joinArray(trimAir(s), ",");
	}
	
	private static String[] trimAir(String[] s) {
		List<String> list = new ArrayList<String>();
		for (String item : s) {
			if (item.equals("AIR")) {
				continue;
			}
			list.add(item);
		}
		
		if (list.size() < 1) {
			return new String[]{"AIR"};
		}
		
		String[] result = new String[list.size()];
		int i = 0;
		for (String item : list) {
			result[i++] = item;
		}
		
		return result;
	}

	public static String getStringFromItemStack(ItemStack is) {
		if (is == null || is.getType().equals(Material.AIR)) {
			return "AIR";
		}
		String temp = is.getType().name();
		boolean durability = false;
		if (is.getDurability() != 0) {
			temp += "~" + String.valueOf(is.getDurability());
			durability = true;
		}
		if (is.getType() == Material.INK_SACK || is.getType() == Material.WOOL) {
			if (!durability) {
				temp += "~" + String.valueOf(is.getDurability());
			}
			temp += "~" + String.valueOf(is.getData().getData());
		} else if (is.getType() == Material.WRITTEN_BOOK || is.getType() == Material.BOOK_AND_QUILL) {
			if (!durability) {
				temp += "~" + String.valueOf(is.getDurability());
			}
			BookMeta bm = (BookMeta) is.getItemMeta();
			if (bm != null) {
				if ((bm.getAuthor() != null) && (bm.getTitle() != null) && (bm.getPages() != null)) {
							temp += "~" + codeCharacters(bm.getAuthor(), true) + SAFE_BREAK + codeCharacters(bm.getTitle(), true) +
									SAFE_BREAK + codeCharacters(joinArray(bm.getPages().toArray(),SAFE_PAGE_BREAK), true);
					
				}
			}
		} else if (is.getType().name().startsWith("LEATHER_")) {
			if (!durability) {
				temp += "~" + String.valueOf(is.getDurability());
			}
			LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
			temp += "~" + lam.getColor().asRGB();
		} else if (is.getType() == Material.SKULL_ITEM) {
			if (!durability) {
				temp += "~" + String.valueOf(is.getDurability());
			}
			SkullMeta sm = (SkullMeta) is.getItemMeta();
			temp += "~" + sm.getOwner();
		}
		
		if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
			if (!durability) {
				temp += "~" + String.valueOf(is.getDurability());
			}

			temp += SAFE_LORE_BREAK + codeCharacters(joinArray(((ItemMeta) is.getItemMeta()).getLore().toArray(), 
					SAFE_BREAK), true);
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

		return temp;
	}

	public static String getWoolEnumFromChatColorEnum(String color) {
		return parseDyeColorToChatColor(color, false);
	}

	public static String joinArray(Object[] array, String glue) {
		String result = "";
		for (Object o : array) {
			result += glue + String.valueOf(o);
		}
		return new String(result.substring(glue.length()));
	}

	public static String joinSet(Set<?> set, String glue) {
		String result = "";
		for (Object o : set) {
			result += glue + String.valueOf(o);
		}
		return result.equals("")?"":new String(result.substring(glue.length()));
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
			PVPArena.instance.getLogger().warning("unrecognized material: " + string);
		}
		return mat;
	}

	public static String[] shiftArrayBy(String[] args, int i) {
		String[] newArgs = new String[args.length - i];
		System.arraycopy(args, i, newArgs, 0, args.length - i);
		args = newArgs;
		
		return args;
	}
	public static String[] unShiftArrayBy(String[] args, int i) {
		String[] newArgs = new String[args.length + i];
		System.arraycopy(args, 0, newArgs, 1, args.length);
		args = newArgs;
		
		return args;
	}

	public static String verify(Object sender) {
		return sender == null ? "null" : sender.toString();
	}
}
