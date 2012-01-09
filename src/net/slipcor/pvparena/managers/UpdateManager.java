package net.slipcor.pvparena.managers;

import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;

import net.slipcor.pvparena.PVPArena;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * update manager class
 * 
 * -
 * 
 * provides access to update check and methods
 * 
 * @author slipcor
 * 
 * @version v0.5.5
 * 
 */

public class UpdateManager {
	
	public static boolean msg = false;
	public static boolean outdated = false;
	private static String vOnline;
	private static String vThis;
	
	/**
	 * check for updates, update variables
	 */
	public static void updateCheck() {
	    String pluginUrlString = "http://dev.bukkit.org/server-mods/pvp-arena/files.rss";
	    try {
	      URL url = new URL(pluginUrlString);
	      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
	      doc.getDocumentElement().normalize();
	      NodeList nodes = doc.getElementsByTagName("item");
	      Node firstNode = nodes.item(0);
	      if (firstNode.getNodeType() == 1) {
	        Element firstElement = (Element)firstNode;
	        NodeList firstElementTagName = firstElement.getElementsByTagName("title");
	        Element firstNameElement = (Element)firstElementTagName.item(0);
	        NodeList firstNodes = firstNameElement.getChildNodes();
	        
	        String sOnlineVersion = firstNodes.item(0).getNodeValue();
	        String sThisVersion = PVPArena.instance.getDescription().getVersion();
	        
	        while(sOnlineVersion.contains(" ")) {
	        	sOnlineVersion = sOnlineVersion.substring(sOnlineVersion.indexOf(" ")+1);
	        }
	        
	        UpdateManager.vOnline = sOnlineVersion.replace("v", "");
	        UpdateManager.vThis = sThisVersion.replace("v", "");
	        
	        calculateVersions();
	        return;
	      }
	    }
	    catch (Exception localException) {
	    }
	}
	
	/**
	 * calculate the message variables based on the versions
	 */
	private static void calculateVersions() {
		String[] aOnline = vOnline.split("\\.");
		String[] aThis = vThis.split("\\.");
		outdated = false;
		
		for (int i=0; i<aOnline.length && i<aThis.length; i++) {
			try {
				int o = Integer.parseInt(aOnline[i]);
				int t = Integer.parseInt(aThis[i]);
				if (o == t) {
					msg = false;
					continue;
				}
				msg = true;
				outdated = (o > t);
				
				UpdateManager.message(null);
				return;
			} catch (Exception e) {
				calculateRadixString(aOnline[i],aThis[i]);
				return;
			}
		}
	}
	
	/**
	 * calculate a version part based on letters
	 * @param sOnline the online letter(s)
	 * @param sThis the local letter(s)
	 */
	private static void calculateRadixString(String sOnline, String sThis) {
		try {
			int o = Integer.parseInt(sOnline,46);
			int t = Integer.parseInt(sThis,46);
			if (o == t) {
				msg = false;
				return;
			}
			msg = true;
			outdated = (o > t);
			
			UpdateManager.message(null);
		} catch (Exception e) {
		}
	}
	
	/**
	 * message a player if the version is different
	 * @param player the player to message
	 */
	public static void message(Player player) {
		if (player == null || !(player instanceof Player)) {
			if (!msg) {
				PVPArena.instance.log.info("[PVP Arena] You are on latest version!");
			} else {
				if (outdated) {
					PVPArena.instance.log.warning("[PVP Arena] You are using v"+vThis+", an outdated version! Latest: "+vOnline);
				} else {
					PVPArena.instance.log.warning("[PVP Arena] You are using v"+vThis+", an experimental version! Latest stable: "+vOnline);
				}
			}
		}
		if (!msg) {
			return;
		}

		if (outdated) {
			ArenaManager.tellPlayer(player, "You are using "+colorize("v"+vThis,'o')+", an outdated version! Latest: "+colorize("v"+vOnline,'s'));
		} else {
			ArenaManager.tellPlayer(player, "You are using "+colorize("v"+vThis,'e')+", an experimental version! Latest stable: "+colorize("v"+vOnline,'s'));
		}
	}
	
	/**
	 * colorize a given string based on a char
	 * @param s the string to colorize
	 * @param c the char that decides what color
	 * @return a colorized string
	 */
	private static String colorize(String s, char c) {
		if (c == 'o') {
			s = ChatColor.RED + s + ChatColor.WHITE;
		} else if (c == 'e') {
			s = ChatColor.GOLD + s + ChatColor.WHITE;
		} else if (c == 's') {
			s = ChatColor.GREEN + s + ChatColor.WHITE;
		}
		return s;
	}
}
