package net.slipcor.pvparena.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import net.slipcor.pvparena.core.Language.MSG;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * <pre>Tracker class</pre>
 * 
 * phones home to www.slipcor.net, saving server IP and PVP Arena version
 * 
 * @author slipcor
 * 
 * @version v0.9.5
 */

public class Tracker implements Runnable {
	private static Plugin plugin;
	private static int taskID = -1;
	private static Debug db = new Debug(18);

	public Tracker(Plugin p) {
		plugin = p;
	}

	/**
	 * call home to save the server/plugin state
	 */
	private void callHome() {
		if (!plugin.getConfig().getBoolean("tracker", true)) {
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
		Language.log_info(MSG.LOG_TRACKER_ENABLED);
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this,
				0L, 72000L);
	}

	/**
	 * stop tracking
	 */
	public static void stop() {
		Language.log_info(MSG.LOG_TRACKER_DISABLED);
		Bukkit.getScheduler().cancelTask(taskID);
	}
}
