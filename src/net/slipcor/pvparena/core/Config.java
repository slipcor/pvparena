package net.slipcor.pvparena.core;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShapeManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * <pre>Configuration class</pre>
 * 
 * This Config wrapper improves access to config files by storing them in RAM
 * and providing quick, secured, access. Thanks a lot to garbagemule for
 * the start of this config.
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class Config {
	private YamlConfiguration config;
	private File configFile;
	private Map<String, Boolean> booleans;
	private Map<String, Integer> ints;
	private Map<String, Double> doubles;
	private Map<String, String> strings;

	/**
	 * Create a new Config instance that uses the specified file for loading and
	 * saving.
	 * 
	 * @param configFile
	 *            a YAML file
	 */
	public Config(File configFile) {
		this.config = new YamlConfiguration();
		this.configFile = configFile;
		this.booleans = new HashMap<String, Boolean>();
		this.ints = new HashMap<String, Integer>();
		this.doubles = new HashMap<String, Double>();
		this.strings = new HashMap<String, String>();

		this.config.options().indent(4);
	}

	/**
	 * Load the config-file into the YamlConfiguration, and then populate the
	 * value maps.
	 * 
	 * @return true, if the load succeeded, false otherwise.
	 */
	public boolean load() {
		try {
			config.load(configFile);
			reloadMaps();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Iterates through all keys in the config-file, and populates the value
	 * maps. Boolean values are stored in the booleans-map, Strings in the
	 * strings-map, etc.
	 */
	public void reloadMaps() {
		for (String s : config.getKeys(true)) {
			Object o = config.get(s);

			if (o instanceof Boolean) {
				booleans.put(s, (Boolean) o);
			} else if (o instanceof Integer) {
				ints.put(s, (Integer) o);
			} else if (o instanceof Double) {
				doubles.put(s, (Double) o);
			} else if (o instanceof String) {
				strings.put(s, (String) o);
			}
		}
	}

	/**
	 * Save the YamlConfiguration to the config-file.
	 * 
	 * @return true, if the save succeeded, false otherwise.
	 */
	public boolean save() {
		try {
			config.save(configFile);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Delete the config-file.
	 * 
	 * @return true, if the delete succeeded, false otherwise.
	 */
	public boolean delete() {
		return configFile.delete();
	}

	/**
	 * Set the header of the config-file.
	 * 
	 * @param header
	 *            the header
	 */
	public void setHeader(String header) {
		config.options().header(header);
	}

	// /////////////////////////////////////////////////////////////////////////
	// //
	// GETTERS //
	// //
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Get the YamlConfiguration associated with this Config instance. Note that
	 * changes made directly to the YamlConfiguration will cause an
	 * inconsistency with the value maps unless reloadMaps() is called.
	 * 
	 * @return the YamlConfiguration of this Config instance
	 */
	public YamlConfiguration getYamlConfiguration() {
		return config;
	}

	/**
	 * Retrieve a value from the YamlConfiguration.
	 * 
	 * @param path
	 *            the path of the value
	 * @return the value of the path
	 */
	public Object get(String path) {
		return config.get(path);
	}

	/**
	 * Retrieve a boolean from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @return the boolean value of the path if the path exists, false otherwise
	 */
	public boolean getBoolean(String path) {
		return getBoolean(path, false);
	}

	/**
	 * Retrieve a boolean from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @param def
	 *            a default value to return if the value was not in the map
	 * @return the boolean value of the path if it exists, def otherwise
	 */
	public boolean getBoolean(String path, boolean def) {
		Boolean result = booleans.get(path);
		return (result != null ? result : def);
	}

	/**
	 * Retrieve an int from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @return the int value of the path if the path exists, 0 otherwise
	 */
	public int getInt(String path) {
		return getInt(path, 0);
	}

	/**
	 * Retrieve an int from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @param def
	 *            a default value to return if the value was not in the map
	 * @return the int value of the path if it exists, def otherwise
	 */
	public int getInt(String path, int def) {
		Integer result = ints.get(path);
		return (result != null ? result : def);
	}

	/**
	 * Retrieve a double from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @return the double value of the path if the path exists, 0D otherwise
	 */
	public double getDouble(String path) {
		return getDouble(path, 0D);
	}

	/**
	 * Retrieve a double from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @param def
	 *            a default value to return if the value was not in the map
	 * @return the double value of the path if it exists, def otherwise
	 */
	public double getDouble(String path, double def) {
		Double result = doubles.get(path);
		return (result != null ? result : def);
	}

	/**
	 * Retrieve a string from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @return the string value of the path if the path exists, null otherwise
	 */
	public String getString(String path) {
		return getString(path, null);
	}

	/**
	 * Retrieve a string from the value maps.
	 * 
	 * @param path
	 *            the path of the value
	 * @param def
	 *            a default value to return if the value was not in the map
	 * @return the string value of the path if it exists, def otherwise
	 */
	public String getString(String path, String def) {
		String result = strings.get(path);
		return (result != null ? result : def);
	}

	public Set<String> getKeys(String path) {
		if (config.get(path) == null)
			return null;

		ConfigurationSection section = config.getConfigurationSection(path);
		return section.getKeys(false);
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringList(String path, List<String> def) {
		if (config.get(path) == null)
			return def != null ? def : new LinkedList<String>();

		List<?> list = config.getStringList(path);
		return (List<String>) list;
	}

	// /////////////////////////////////////////////////////////////////////////
	// //
	// MUTATORS //
	// //
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Set the value of the given path in both the value maps and the
	 * YamlConfiguration. Note that this will only properly put the value in its
	 * relevant value map, if it is of one of the supported types. The method
	 * can also be used to remove values from their maps and the
	 * YamlConfiguration by passing null for the value.
	 * 
	 * @param path
	 *            the path on which to set the value
	 * @param value
	 *            the value to set
	 */
	public void set(String path, Object value) {
		if (value instanceof Boolean) {
			booleans.put(path, (Boolean) value);
		} else if (value instanceof Integer) {
			ints.put(path, (Integer) value);
		} else if (value instanceof Double) {
			doubles.put(path, (Double) value);
		} else if (value instanceof String) {
			strings.put(path, (String) value);
		}

		if (value == null) {
			booleans.remove(value);
			ints.remove(value);
			doubles.remove(value);
			strings.remove(value);
		}

		config.set(path, value);
	}

	/**
	 * Remove the node at the given path. Uses the set() method with null as the
	 * value.
	 * 
	 * @param path
	 *            the path of the node to remove
	 */
	public void remove(String path) {
		this.set(path, null);
	}

	// /////////////////////////////////////////////////////////////////////////
	// //
	// UTILITY METHODS //
	// //
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Parse an input string of the form "world,x,y,z" to create a Block
	 * Location. This method will only accept strings of the specified form.
	 * 
	 * @param coords
	 *            a string of the form "world,x,y,z"
	 * @return a PABlockLocation in the given world with the given coordinates
	 */
	public static PABlockLocation parseBlockLocation(String coords) {
		String[] parts = coords.split(",");
		if (parts.length != 4)
			throw new IllegalArgumentException(
					"Input string must contain world, x, y, and z: " + coords);

		Integer x = parseInteger(parts[1]);
		Integer y = parseInteger(parts[2]);
		Integer z = parseInteger(parts[3]);

		if (Bukkit.getWorld(parts[0]) == null || x == null || y == null || z == null)
			throw new NullPointerException(
					"Some of the parsed values are null!");

		return new PABlockLocation(parts[0], x, y, z);
	}

	/**
	 * Parse an input string of the form "world,x,y,z,yaw,pitch" to create a Block
	 * Location. This method will only accept strings of the specified form.
	 * 
	 * @param coords
	 *            a string of the form "world,x,y,z,yaw,pitch"
	 * @return a PALocation in the given world with the given coordinates
	 */
	public static PALocation parseLocation(String coords) {
		String[] parts = coords.split(",");
		if (parts.length != 6)
			throw new IllegalArgumentException(
					"Input string must contain world, x, y, z, yaw and pitch: " + coords);
		
		Integer x = parseInteger(parts[1]);
		Integer y = parseInteger(parts[2]);
		Integer z = parseInteger(parts[3]);
		Float yaw = parseFloat(parts[4]);
		Float pitch = parseFloat(parts[5]);

		if (Bukkit.getWorld(parts[0]) == null || x == null || y == null || z == null || yaw == null || pitch == null)
			throw new NullPointerException(
					"Some of the parsed values are null!");

		return new PALocation(parts[0], x, y, z, yaw, pitch);
	}
	
	/**
	 * Parse an input string of the form "world,x1,y1,z1,x2,y2,z2,shape"
	 * to create a Location array. This method will only accept strings of the
	 * specified form.
	 * 
	 * @param coords
	 *            a string of the form "world,x1,y1,z1,x2,y2,z2,shape"
	 * @return a Location array in the given world with the given coordinates
	 */
	public static PABlockLocation[] parseRegion(String coords) {
		String[] parts = coords.split(",");
		if (parts.length < 8)
			throw new IllegalArgumentException(
					"Input string must contain only world, x1, y1, z1, x2, y2, z2, and shape: " + coords);
		
		if (ArenaRegionShapeManager.getShapeByName(parts[7]) == null) {
			throw new IllegalArgumentException(
					"Input string does not contain valid region shape: " + coords);
		}
		
		Integer x1 = parseInteger(parts[1]);
		Integer y1 = parseInteger(parts[2]);
		Integer z1 = parseInteger(parts[3]);
		Integer x2 = parseInteger(parts[4]);
		Integer y2 = parseInteger(parts[5]);
		Integer z2 = parseInteger(parts[6]);

		if (Bukkit.getWorld(parts[0]) == null || x1 == null || y1 == null || z1 == null || x2 == null || y2 == null
				|| z2 == null)
			throw new NullPointerException(
					"Some of the parsed values are null!");
		PABlockLocation[] l = { new PABlockLocation(parts[0], x1, y1, z1),
				new PABlockLocation(parts[0], x2, y2, z2) };
		return l;

	}

	private static Integer parseInteger(String s) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	private static Float parseFloat(String s) {
		try {
			return Float.parseFloat(s.trim());
		} catch (Exception e) {
			return null;
		}
	}

	public static String parseToString(PALocation loc) {
		String[] result = new String[6];
		result[0] = String.valueOf(loc.getWorldName());
		result[1] = String.valueOf(loc.getX());
		result[2] = String.valueOf(loc.getY());
		result[3] = String.valueOf(loc.getZ());
		result[4] = String.valueOf(loc.getYaw());
		result[5] = String.valueOf(loc.getPitch());
		// "world,x,y,z,yaw,pitch"
		return StringParser.joinArray(result, ",");
	}

	public static String parseToString(PABlockLocation loc) {
		String[] result = new String[4];
		result[0] = String.valueOf(loc.getWorldName());
		result[1] = String.valueOf(loc.getX());
		result[2] = String.valueOf(loc.getY());
		result[3] = String.valueOf(loc.getZ());
		// "world,x,y,z"
		return StringParser.joinArray(result, ",");
	}

	public static String parseToString(ArenaRegionShape region) {
		String[] result = new String[8];
		result[0] = region.getWorldName();
		result[1] = String.valueOf(region.getLocs()[0].getX());
		result[2] = String.valueOf(region.getLocs()[0].getY());
		result[3] = String.valueOf(region.getLocs()[0].getZ());
		result[4] = String.valueOf(region.getLocs()[1].getX());
		result[5] = String.valueOf(region.getLocs()[1].getY());
		result[6] = String.valueOf(region.getLocs()[1].getZ());
		result[7] = region.getShape().name();
		// "world,x1,y1,z1,x2,y2,z2,shape"
		return StringParser.joinArray(result, ",");
	}
}
