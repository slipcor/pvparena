package net.slipcor.pvparena.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaModule;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaType;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class PAInstall extends PA_Command {
	private static Debug db = new Debug(3);

	@Override
	public void commit(CommandSender sender, String[] args) {
		// pa install
		// pa install ctf

		if (!checkArgs(sender, args, 1, 2)) {
			return;
		}

		db.i("parsing install command of player " + sender.getName()
				+ StringParser.parseArray(args));

		if (!PVPArena.hasAdminPerms(sender)) {
			Arenas.tellPlayer(sender,
					Language.parse("nopermto", Language.parse("admin")));
			return;
		}

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(PVPArena.instance.getDataFolder().getPath()
					+ "/install.yml");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return;
		}

		if (args.length == 1) {
			listVersions(sender, config, null);
			return;
		}
		
		if (config.get(args[1]) != null) {
			listVersions(sender, config, args[1]);
			return;
		}
		
		Set<String> list;
		
		list = config.getConfigurationSection("arenas").getKeys(false);
		if (list.contains(args[1].toLowerCase())) {
			for (String key : list) {
				if (key.equalsIgnoreCase(args[1])) {
					if(download("pa_a_" + key + ".jar")) {
						PVPArena.instance.getAtm().reload();
						Arenas.tellPlayer(sender, "installed: " + key);
						return;
					}
					Arenas.tellPlayer(sender, "error while installing " + key);
					return;
				}
			}
		}
		
		list = config.getConfigurationSection("modules").getKeys(false);
		if (list.contains(args[1].toLowerCase())) {
			for (String key : list) {
				if (key.equalsIgnoreCase(args[1])) {
					if (download("pa_m_" + key + ".jar")) {
						PVPArena.instance.getAmm().reload();
						Arenas.tellPlayer(sender, "installed: " + key);
						return;
					}
					Arenas.tellPlayer(sender, "error while installing " + key);
					return;
				}
			}
		}
		
		list = config.getConfigurationSection("regions").getKeys(false);
		if (list.contains(args[1].toLowerCase())) {
			for (String key : list) {
				if (key.equalsIgnoreCase(args[1])) {
					if (download("pa_r_" + key + ".jar")) {
						PVPArena.instance.getArm().reload();
						Arenas.tellPlayer(sender, "installed: " + key);
						return;
					}
					Arenas.tellPlayer(sender, "error while installing " + key);
					return;
				}
			}
		}
	}

	private void listVersions(CommandSender sender, YamlConfiguration cfg,
			String s) {
		Arenas.tellPlayer(sender,
				"--- PVP Arena Version Update information ---");
		Arenas.tellPlayer(sender, "[§7uninstalled§r | §einstalled§r]");
		Arenas.tellPlayer(sender, "[§coutdated§r | §alatest version§r]");
		if (s == null || s.toLowerCase().equals("arenas")) {
			Arenas.tellPlayer(sender, "§c--- Arena Game Modes ----> /arenas");
			Set<String> entries = cfg.getConfigurationSection("arenas")
					.getKeys(false);
			for (String key : entries) {
				String value = cfg.getString("arenas." + key);
				ArenaType type = PVPArena.instance.getAtm().getType(key);
				boolean installed = (type != null);
				String version = null;
				if (installed) {
					version = type.version();
				}
				Arenas.tellPlayer(sender, ((installed) ? "§e" : "§7") + key
						+ "§r - " + (installed?((value.equals(version)) ? "§a" : "§c"):"")
						+ value);
			}
		}
		if (s == null || s.toLowerCase().equals("modules")) {
			Arenas.tellPlayer(sender, "§a--- Arena Modules ----> /modules");
			Set<String> entries = cfg.getConfigurationSection("modules")
					.getKeys(false);
			for (String key : entries) {
				String value = cfg.getString("modules." + key);
				ArenaModule mod = PVPArena.instance.getAmm().getModule(key);
				boolean installed = (mod != null);
				String version = null;
				if (installed) {
					version = mod.version();
				}
				Arenas.tellPlayer(sender, ((installed) ? "§e" : "§7") + key
						+ "§r - " + (installed?((value.equals(version)) ? "§a" : "§c"):"")
						+ value);
			}

		}
		if (s == null || s.toLowerCase().equals("regions")) {
			Arenas.tellPlayer(sender,
					"§b--- Arena Region Shapes ----> /regions");
			Set<String> entries = cfg.getConfigurationSection("regions")
					.getKeys(false);
			for (String key : entries) {
				String value = cfg.getString("regions." + key);
				ArenaRegion reg = PVPArena.instance.getArm().getModule(key);
				boolean installed = (reg != null);
				String version = null;
				if (installed) {
					version = reg.version();
				}
				Arenas.tellPlayer(sender, ((installed) ? "§e" : "§7") + key
						+ "§r - " + (installed?((value.equals(version)) ? "§a" : "§c"):"")
						+ value);
			}

		}
	}

	private boolean download(String file) {
		String folder = null;
		if (file.startsWith("pa_a")) {
			folder = "/arenas/";
		} else if (file.startsWith("pa_m")) {
			folder = "/modules/";
		} else if (file.startsWith("pa_r")) {
			folder = "/regions/";
		}
		if (folder == null) {
			System.out.print("[SEVERE] unable to fetch file: " + file);
			return false;
		}
		try {
			File destination = new File(PVPArena.instance.getDataFolder().getPath() + folder);

			File destFile = new File(destination, file);
			if (destFile.exists()) {
				destFile.delete();
			}

			System.out.println("Downloading module '"+file+"'...");
			URL url = new URL(
					"http://www.slipcor.net/public/mc/pafiles/" + file);
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream output = new FileOutputStream(destFile);
			output.getChannel().transferFrom(rbc, 0, 1 << 24);
			System.out.println("Downloaded module!");

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getName() {
		return "PAUpdate";
	}
}
