package net.slipcor.pvparena.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * <pre>
 * PVP Arena INSTALL Command class
 * </pre>
 * 
 * A command to install modules
 * 
 * @author slipcor
 * 
 * @version v0.9.9
 */

public class PAA_Install extends PA__Command {

	public PAA_Install() {
		super(new String[0]);
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}

		if (!this.argCountValid(sender, args, new Integer[] { 0, 1 })) {
			return;
		}

		// pa install
		// pa install ctf

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(PVPArena.instance.getDataFolder().getPath()
					+ "/install.yml");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if (args.length == 0) {
			listVersions(sender, config, null);
			return;
		}

		if (config.get(args[0]) != null) {
			listVersions(sender, config, args[1]);
			return;
		}

		Set<String> list;

		list = config.getConfigurationSection("goals").getKeys(false);
		if (list.contains(args[0].toLowerCase())) {
			for (String key : list) {
				if (key.equalsIgnoreCase(args[0])) {
					if (download("pa_g_" + key + ".jar")) {
						PVPArena.instance.getAgm().reload();
						Arena.pmsg(sender,
								Language.parse(MSG.INSTALL_DONE, key));
						return;
					}
					Arena.pmsg(sender, Language.parse(MSG.ERROR_INSTALL, key));
					return;
				}
			}
		}

		list = config.getConfigurationSection("mods").getKeys(false);
		if (list.contains(args[0].toLowerCase())) {
			for (String key : list) {
				if (key.equalsIgnoreCase(args[0])) {
					if (download("pa_m_" + key + ".jar")) {
						PVPArena.instance.getAmm().reload();
						Arena.pmsg(sender,
								Language.parse(MSG.INSTALL_DONE, key));
						return;
					}
					Arena.pmsg(sender, Language.parse(MSG.ERROR_INSTALL, key));
					return;
				}
			}
		}
	}

	private void listVersions(CommandSender sender, YamlConfiguration cfg,
			String s) {
		Arena.pmsg(sender, "--- PVP Arena Version Update information ---");
		Arena.pmsg(sender, "[§7uninstalled§r | §einstalled§r]");
		Arena.pmsg(sender, "[§coutdated§r | §alatest version§r]");
		if (s == null || s.toLowerCase().equals("goals")) {
			Arena.pmsg(sender, "§c--- Arena Goals ----> /goals");
			Set<String> entries = cfg.getConfigurationSection("goals").getKeys(
					false);
			for (String key : entries) {
				String value = cfg.getString("goals." + key);
				ArenaGoal goal = PVPArena.instance.getAgm().getType(key);
				boolean installed = (goal != null);
				String version = null;
				if (installed) {
					version = goal.version();
				}
				Arena.pmsg(sender, ((installed) ? "§e" : "§7")
						+ key
						+ "§r - "
						+ (installed ? ((value.equals(version)) ? "§a" : "§c")
								: "") + value);
			}
		}
		if (s == null || s.toLowerCase().equals("mods")) {
			Arena.pmsg(sender, "§a--- Arena Mods ----> /mods");
			Set<String> entries = cfg.getConfigurationSection("mods").getKeys(
					false);
			for (String key : entries) {
				String value = cfg.getString("mods." + key);
				ArenaModule mod = PVPArena.instance.getAmm().getModule(key);
				boolean installed = (mod != null);
				String version = null;
				if (installed) {
					version = mod.version();
				}
				Arena.pmsg(sender, ((installed) ? "§e" : "§7")
						+ key
						+ "§r - "
						+ (installed ? ((value.equals(version)) ? "§a" : "§c")
								: "") + value);
			}

		}
	}

	private boolean download(String file) {

		File source = new File(PVPArena.instance.getDataFolder().getPath()
				+ "/files/" + file);

		if (source == null || !source.exists()) {
			Arena.pmsg(
					Bukkit.getConsoleSender(),
					"§cFile '§r"
							+ file
							+ "§c' not found. Please extract the file to /files before trying to install!");
			return false;
		}

		String folder = null;
		if (file.startsWith("pa_g")) {
			folder = "/goals/";
		} else if (file.startsWith("pa_m")) {
			folder = "/mods/";
		}
		if (folder == null) {
			PVPArena.instance.getLogger()
					.severe("unable to save file: " + file);
			return false;
		}
		try {
			File destination = new File(PVPArena.instance.getDataFolder()
					.getPath() + folder + "/" + file);
			try {
				disableModule(file);
			} catch (Exception e2) {
			}

			FileInputStream stream = new FileInputStream(source);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = stream.read(buffer)) > 0) {
				baos.write(buffer, 0, bytesRead);
			}

			FileOutputStream fos = new FileOutputStream(destination);
			fos.write(baos.toByteArray());
			fos.close();

			/*
			 * FileReader in = new FileReader(source); FileWriter out = new
			 * FileWriter(destination); int c;
			 * 
			 * while ((c = in.read()) != -1) out.write(c);
			 * 
			 * in.close(); out.close();
			 */
			PVPArena.instance.getLogger().info("Installed module " + file);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void disableModule(String file) {
		if (file.startsWith("pa_g")) {
			ArenaGoal g = PVPArena.instance.getAgm().getType(
					file.replace("pa_g_", "").replace(".jar", ""));
			g.unload();
		} else if (file.startsWith("pa_m")) {
			ArenaModule g = PVPArena.instance.getAmm().getModule(
					file.replace("pa_m_", "").replace(".jar", ""));
			g.unload();
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.INSTALL));
	}
}
