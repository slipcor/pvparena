package net.slipcor.pvparena.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * <pre>Update class</pre>
 * 
 * provides access to update check and methods
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class Update extends Thread {

	public static boolean msg = false;
	public static boolean outdated = false;
	public static byte updateState = -1;

	private static String vOnline;
	private static String vThis;
	private static Debug debug = new Debug(19);

	private static String fileURL = null;
	private static String pageURL = null;
	
	private static UpdateMode mode = UpdateMode.BOTH;
	private static UpdateType type = UpdateType.RELEASE;

	enum UpdateMode {
		OFF, ANNOUNCE, DOWNLOAD, BOTH;

		public static UpdateMode getBySetting(final String setting) {
			final String lcSetting = setting.toLowerCase();
			if (lcSetting.contains("ann")) {
				return ANNOUNCE;
			}
			if (lcSetting.contains("down") || lcSetting.contains("load")) {
				return DOWNLOAD;
			}
			if (lcSetting.equals("both")) {
				return BOTH;
			}
			return OFF;
		}
	}

	enum UpdateType {
		ALPHA, BETA, RELEASE;

		public static UpdateType getBySetting(final String setting) {
			if (setting.equalsIgnoreCase("beta")) {
				return BETA;
			}
			if (setting.equalsIgnoreCase("alpha")) {
				return ALPHA;
			}
			return RELEASE;
		}
		
		public static boolean matchType(final String updateType) {
			switch (type) {
				case ALPHA:
					return true;
				case BETA:
					return updateType.equalsIgnoreCase("beta") || updateType.equalsIgnoreCase("release");
				default:
					return updateType.equalsIgnoreCase("release");
			}
		}
	}
	
	public Update(final Plugin plugin) {
		super();
		String setting = plugin.getConfig().getString("update", "both");
		mode = UpdateMode.getBySetting(setting);
		if (mode != UpdateMode.OFF) {
			setting = plugin.getConfig().getString("updatetype", "beta");
			type = UpdateType.getBySetting(setting);
		}
		init();
	}

	/**
	 * calculate the message variables based on the versions
	 */
	private void calculateVersions() {
		debug.i("calculating versions");
		final String[] aOnline = vOnline.split("\\.");
		final String[] aThis = vThis.split("\\.");
		outdated = false;

		for (int i = 0; i < aOnline.length && i < aThis.length; i++) {
			try {
				final int iOnline = Integer.parseInt(aOnline[i]);
				final int iThis = Integer.parseInt(aThis[i]);
				if (iOnline == iThis) {
					msg = false;
					continue;
				}
				msg = true;
				outdated = (iOnline > iThis);
				updateState = (byte) i;
				message(Bukkit.getConsoleSender());
				return;
			} catch (Exception e) {
				calculateRadixString(aOnline[i], aThis[i], i);
				return;
			}
		}
	}

	/**
	 * calculate a version part based on letters
	 * 
	 * @param sOnline
	 *            the online letter(s)
	 * @param sThis
	 *            the local letter(s)
	 */
	private void calculateRadixString(final String sOnline, final String sThis,
			final int pos) {
		debug.i("calculating including letters");
		try {
			final int iOnline = Integer.parseInt(sOnline, 46);
			final int iThis = Integer.parseInt(sThis, 46);
			if (iOnline == iThis) {
				msg = false;
				return;
			}
			msg = true;
			outdated = (iOnline > iThis);
			updateState = (byte) pos;
			message(Bukkit.getConsoleSender());
		} catch (Exception e) {
		}
	}

	/**
	 * colorize a given string based on a char
	 * 
	 * @param s
	 *            the string to colorize
	 * @return a colorized string
	 */
	private static String colorize(final String string) {
		StringBuffer result = null;
		if (updateState == 0) {
			result = new StringBuffer(ChatColor.RED.toString());
		} else if (updateState == 1) {
			result = new StringBuffer(ChatColor.GOLD.toString());
		} else if (updateState == 2) {
			result = new StringBuffer(ChatColor.YELLOW.toString());
		} else if (updateState == 3) {
			result = new StringBuffer(ChatColor.BLUE.toString());
		} else {
			result = new StringBuffer(ChatColor.GREEN.toString());
		}
		result.append(string);
		result.append(ChatColor.WHITE);
		return result.toString();
	}

	/**
	 * message a player if the version is different
	 * 
	 * @param player
	 *            the player to message
	 */
	public static boolean message(final CommandSender player) {
		if (!msg) {
			debug.i("version is up to date!", player);
			return false;
		}

		if (outdated) {
			if (!(player instanceof Player) && (mode != UpdateMode.ANNOUNCE)) {
				// not only announce, download!
				final File folder = Bukkit.getServer().getUpdateFolderFile();
				if (!folder.exists()) {
					folder.mkdirs();
				}
				final File destination = new File(folder, PVPArena.instance.getFileName());
				if (destination.exists()) {
					destination.delete();
				}

				debug.i("Downloading jar file from DBO link", player);
				try {
					final URL url = new URL(fileURL);
					
					final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
					final FileOutputStream output = new FileOutputStream(destination);
					output.getChannel().transferFrom(rbc, 0, 1 << 24);
					output.close();
					
					debug.i("Downloaded jar file from DBO link!", player);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Arena.pmsg(player, "You are using " + colorize("v" + vThis)
					+ ", an outdated version! Latest: §a" + "v" + vOnline);
			if (mode == UpdateMode.ANNOUNCE) {
				Arena.pmsg(player, pageURL);
			} else {
				Arena.pmsg(player, "The plugin has been updated, please restart the server!");
			}
		} else {
			Arena.pmsg(player, "You are using " + colorize("v" + vThis)
					+ ", an experimental version! Latest stable: §a" + "v"
					+ vOnline);
		}
		return true;
	}

	public static void message(final CommandSender sender, final boolean announce) {
		if (!message(sender)) {
			Arena.pmsg(sender, "[PVP Arena] You are on latest version!");
			return;
		}

		if (mode != UpdateMode.DOWNLOAD) {
			Arena.pmsg(sender,
					"http://dev.bukkit.org/server-mods/pvparena/files/");
		}
	}

	public final void init() {
		if (PVPArena.instance.getConfig().getBoolean("modulecheck", true)) {
			try {
				final File destination = PVPArena.instance.getDataFolder();

				final File lib = new File(destination, "install.yml");

				PVPArena.instance.getLogger().info("Downloading module update file...");
				final URL url = new URL(
						"http://pa.slipcor.net/install.yml");
				final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				final FileOutputStream output = new FileOutputStream(lib);
				output.getChannel().transferFrom(rbc, 0, 1 << 24);
				PVPArena.instance.getLogger().info("Downloaded module update file");
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (mode == UpdateMode.OFF) {
			return;
		}
		start();
	}

	@Override
	public void run() {
		debug.i("checking for updates");
		if (!PVPArena.instance.getConfig().getBoolean("updatecheck")) {
			Language.logInfo(MSG.LOG_UPDATE_DISABLED);
			return;
		}
		Language.logInfo(MSG.LOG_UPDATE_ENABLED);
		try {
			final URLConnection connection = new URL("http://api.bukget.org/api2/bukkit/plugin/pvparena").openConnection();
	        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        final StringBuffer stringBuffer = new StringBuffer();
	        String line;
	        while ((line = bufferedReader.readLine()) != null) {
	            stringBuffer.append(line);
	        }
            bufferedReader.close();
            final JSONParser parser=new JSONParser();
            final Object obj=parser.parse(stringBuffer.toString());
	       
            final JSONArray array=(JSONArray)((JSONObject) obj).get("versions");
	        
	        for (int i = 0 ; i < array.size(); i++) {
	        	final JSONObject value = (JSONObject)array.get(i);
	        	final String type = (String) value.get("type");
	        	if (!UpdateType.matchType(type)) {
	        		debug.i(Update.type.name() + " does not match " + type);
	        		continue;
	        	}
	        	
	        	if (incorrectJarFileLink((String) value.get("download"))) {
	        		debug.i("incorrect file link: " + (String) value.get("download"));
	        		continue;
	        	}
	        	
	        	String sOnlineVersion = (String) value.get("version");
	        	final String sThisVersion = PVPArena.instance.getDescription().getVersion();

				if (sOnlineVersion.contains(" ")) {
					final String[] split = sOnlineVersion.split(" ");
					for (int j=0; j< split.length; j++) {
						if (split[j].contains(".")) {
							sOnlineVersion = split[j];
							break;
						}
					}
				}

				vOnline = sOnlineVersion.replace("v", "");
				vThis = sThisVersion.replace("v", "");
				debug.i("online version: " + vOnline);
				debug.i("local version: " + vThis);
				
				if (mode != UpdateMode.DOWNLOAD) {
					// Not ONLY download: announce!
					pageURL = (String) value.get("link");
				}
				
				if (mode != UpdateMode.ANNOUNCE) {
					// Not ONLY announcing: download!
					fileURL = (String) value.get("download");
				}

				calculateVersions();
				return;
	        }
	        
		} catch (Exception e) {
			
		}
	}

	private boolean incorrectJarFileLink(final String link) {
		debug.i("checking link: " + link);
		return (!link.startsWith("http://dev.bukkit.org/") || !link.endsWith(".jar"));
	}
}
