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
 * @version v0.10.0
 */

public class Update extends Thread {

	public static boolean msg = false;
	public static boolean outdated = false;
	public static byte v = -1;

	private static String vOnline;
	private static String vThis;
	private static Plugin plugin;
	private static Debug db = new Debug(19);

	private static String fileURL = null;
	private static String pageURL = null;
	
	private static UpdateMode mode = UpdateMode.OFF;
	private static UpdateType type = UpdateType.RELEASE;

	enum UpdateMode {
		OFF, ANNOUNCE, DOWNLOAD, BOTH;

		public static UpdateMode getBySetting(String s) {
			s = s.toLowerCase();
			if (s.contains("ann")) {
				return ANNOUNCE;
			}
			if (s.contains("down") || s.contains("load")) {
				return DOWNLOAD;
			}
			if (s.equals("both")) {
				return BOTH;
			}
			return OFF;
		}
	}

	enum UpdateType {
		ALPHA, BETA, RELEASE;

		public static UpdateType getBySetting(String s) {
			s = s.toLowerCase();
			if (s.equals("beta")) {
				return BETA;
			}
			if (s.equals("alpha")) {
				return ALPHA;
			}
			return RELEASE;
		}
		
		public static boolean matchType(UpdateType t, String s) {
			s = s.toLowerCase();
			switch (t) {
				case ALPHA:
					return true;
				case BETA:
					return t != ALPHA;
				default:
					return (t == RELEASE);
			}
		}
	}
	
	public Update(Plugin p) {
		plugin = p;
		String setting = p.getConfig().getString("update", "both");
		mode = UpdateMode.getBySetting(setting);
		if (mode != UpdateMode.OFF) {
			setting = p.getConfig().getString("updatetype", "beta");
			type = UpdateType.getBySetting(setting);
		}
		init();
	}

	/**
	 * calculate the message variables based on the versions
	 */
	private void calculateVersions() {
		db.i("calculating versions");
		String[] aOnline = vOnline.split("\\.");
		String[] aThis = vThis.split("\\.");
		outdated = false;

		for (int i = 0; i < aOnline.length && i < aThis.length; i++) {
			try {
				int o = Integer.parseInt(aOnline[i]);
				int t = Integer.parseInt(aThis[i]);
				if (o == t) {
					msg = false;
					continue;
				}
				msg = true;
				outdated = (o > t);
				v = (byte) i;
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
	private void calculateRadixString(String sOnline, String sThis,
			int pos) {
		db.i("calculating including letters");
		try {
			int o = Integer.parseInt(sOnline, 46);
			int t = Integer.parseInt(sThis, 46);
			if (o == t) {
				msg = false;
				return;
			}
			msg = true;
			outdated = (o > t);
			v = (byte) pos;
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
	private static String colorize(String s) {
		if (v == 0) {
			s = ChatColor.RED + s + ChatColor.WHITE;
		} else if (v == 1) {
			s = ChatColor.GOLD + s + ChatColor.WHITE;
		} else if (v == 2) {
			s = ChatColor.YELLOW + s + ChatColor.WHITE;
		} else if (v == 3) {
			s = ChatColor.BLUE + s + ChatColor.WHITE;
		} else {
			s = ChatColor.GREEN + s + ChatColor.WHITE;
		}
		return s;
	}

	/**
	 * message a player if the version is different
	 * 
	 * @param player
	 *            the player to message
	 */
	public static boolean message(CommandSender player) {
		if (!msg) {
			db.i("version is up to date!");
			return false;
		}

		if (outdated) {
			if (!(player instanceof Player) && (mode != UpdateMode.ANNOUNCE)) {
				// not only announce, download!
				File folder = Bukkit.getServer().getUpdateFolderFile();
				if (!folder.exists()) {
					folder.mkdirs();
				}
				File destination = new File(folder, "pvparena.jar");
				if (destination.exists()) {
					destination.delete();
				}

				db.i("Downloading jar file from DBO link");
				try {
					URL url = new URL(fileURL);
					
					ReadableByteChannel rbc = Channels.newChannel(url.openStream());
					FileOutputStream output = new FileOutputStream(destination);
					output.getChannel().transferFrom(rbc, 0, 1 << 24);
					
					db.i("Downloaded jar file from DBO link!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Arena.pmsg(player, "You are using " + colorize("v" + vThis)
					+ ", an outdated version! Latest: §a" + "v" + vOnline);
			if (mode != UpdateMode.ANNOUNCE) {
				Arena.pmsg(player, "The plugin has been updated, please restart the server!");
			} else {
				Arena.pmsg(player, pageURL);
			}
		} else {
			Arena.pmsg(player, "You are using " + colorize("v" + vThis)
					+ ", an experimental version! Latest stable: §a" + "v"
					+ vOnline);
		}
		return true;
	}

	public static void message(CommandSender p, boolean b) {
		// b = announce update!
		if (!message(p)) {
			Arena.pmsg(p, "[PVP Arena] You are on latest version!");
			return;
		}

		if (mode != UpdateMode.DOWNLOAD) {
			Arena.pmsg(p,
					"http://dev.bukkit.org/server-mods/pvparena/files/");
		}
	}

	public void init() {
		if (PVPArena.instance.getConfig().getBoolean("modulecheck", true)) {
			try {
				File destination = PVPArena.instance.getDataFolder();

				File lib = new File(destination, "install.yml");

				PVPArena.instance.getLogger().info("Downloading module update file...");
				URL url = new URL(
						"http://pa.slipcor.net/install.yml");
				ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				FileOutputStream output = new FileOutputStream(lib);
				output.getChannel().transferFrom(rbc, 0, 1 << 24);
				PVPArena.instance.getLogger().info("Downloaded module update file");

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
		db.i("checking for updates");
		if (!plugin.getConfig().getBoolean("updatecheck")) {
			Language.log_info(MSG.LOG_UPDATE_DISABLED);
			return;
		}
		Language.log_info(MSG.LOG_UPDATE_ENABLED);
		try {
			final URLConnection connection = new URL("http://api.bukget.org/api2/bukkit/plugin/pvparena").openConnection();
	        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        final StringBuffer stringBuffer = new StringBuffer();
	        String line;
	        while ((line = bufferedReader.readLine()) != null)
	            stringBuffer.append(line);
	            bufferedReader.close();
	        JSONParser parser=new JSONParser();
	        Object obj=parser.parse(stringBuffer.toString());
	       
	        JSONArray array=(JSONArray)((JSONObject) obj).get("versions");
	        
	        for (int i = 0 ; i < array.size(); i++) {
	        	JSONObject value = (JSONObject)array.get(i);
	        	String type = (String) value.get("type");
	        	if (!UpdateType.matchType(Update.type, type)) {
	        		db.i(Update.type.name() + " does not match " + type);
	        		continue;
	        	}
	        	
	        	if (incorrectJarFileLink((String) value.get("download"))) {
	        		db.i("incorrect file link: " + (String) value.get("download"));
	        		continue;
	        	}
	        	
	        	String sOnlineVersion = (String) value.get("version");
				String sThisVersion = plugin.getDescription().getVersion();

				if (sOnlineVersion.contains(" ")) {
					String[] s = sOnlineVersion.split(" ");
					for (int j=0; j< s.length; j++) {
						if (s[j].contains(".")) {
							sOnlineVersion = s[j];
							break;
						}
					}
				}

				vOnline = sOnlineVersion.replace("v", "");
				vThis = sThisVersion.replace("v", "");
				db.i("online version: " + vOnline);
				db.i("local version: " + vThis);
				
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

	private boolean incorrectJarFileLink(String link) {
		db.i("checking link: " + link);
		return (!link.startsWith("http://dev.bukkit.org/") || !link.endsWith(".jar"));
	}
}
