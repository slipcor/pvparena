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
 * @version v0.7.21
 * 
 */

public class Tracker implements Runnable {
	private static Plugin plugin;
	private static int taskID = -1;
	private static Debug db = new Debug(5);

	/**
	 * construct a tracker instance
	 * 
	 * @param p
	 *            the main plugin instance
	 */
	public Tracker(Plugin p) {
		plugin = p;
	}

	/**
	 * call home to save the server/plugin state
	 */
	private void callHome() {
		if (!plugin.getConfig().getBoolean("stats", true)) {
			stop();
			return;
		}
		db.i("calling home...");

		String url = null;
		try {
			url = String
					.format("http://www.slipcor.net/stats/call.php?port=%s&name=%s&version=%s",
							plugin.getServer().getPort(),
							URLEncoder.encode(
									plugin.getDescription().getName(), "UTF-8"),
							URLEncoder.encode(plugin.getDescription()
									.getVersion(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		try {
			new URL(url).openConnection().getInputStream();
		} catch (MalformedURLException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
			System.out
					.print("[PVP Arena] error while connecting to www.slipcor.net");
			return;
		}
		db.i("successfully called home!");
	}

	@Override
	public void run() {
		callHome();
	}

	/**
	 * start tracking
	 */
	public void start() {
		Language.log_info("startTracker");
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this,
				0L, 72000L);
	}

	/**
	 * stop tracking
	 */
	public static void stop() {
		Language.log_info("stopTracker");
		Bukkit.getScheduler().cancelTask(taskID);
	}
}
