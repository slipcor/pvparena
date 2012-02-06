package net.slipcor.pvparena.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * tracker class
 * 
 * -
 * 
 * tracks plugin version
 * 
 * @author slipcor
 * 
 * @version 0.6.0
 * 
 */

public class Tracker implements Runnable {
	private static Plugin plugin;
	private static int taskID = -1;

	public Tracker(Plugin p) {
		plugin = p;
	}

	public void start() {
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this,
				0L, 1200L);
		// TODO: 72000L
	}

	public static void stop() {
		Bukkit.getScheduler().cancelTask(taskID);
	}

	private void callHome() {
		if (!plugin.getConfig().getBoolean("stats", true)) {
			return;
		}

		String url = null;
		try {
			url = String
					.format("http://www.slipcor.net/stats/call.php?port=%s&name=%s&version=%s",
							plugin.getServer().getPort(),
							URLEncoder.encode(plugin.getDescription().getName(), "UTF-8"),
							URLEncoder.encode(plugin.getDescription().getVersion(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			new URL(url).openConnection().getInputStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		callHome();
	}
}
