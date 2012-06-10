package net.slipcor.pvparena.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

public class PAUninstall extends PA_Command {
	private static Debug db = new Debug(3);

	@Override
	public void commit(CommandSender sender, String[] args) {

		if (!checkArgs(sender, args, 1, 2)) {
			return;
		}

		db.i("parsing uninstall command of player " + sender.getName()
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
		
		String name = args[1].toLowerCase();
		ArenaType at = PVPArena.instance.getAtm().getType(name);
		if (at != null) {
			if (remove("pa_a_"+at.getName().toLowerCase() + ".jar")) {
				PVPArena.instance.getAtm().reload();
				Arenas.tellPlayer(sender, "uninstalled: " + at.getName());
				return;
			}
			Arenas.tellPlayer(sender, "error while uninstalling " + at.getName());
			return;
		}
		ArenaModule am = PVPArena.instance.getAmm().getModule(name);
		if (am != null) {
			if (remove("pa_m_"+am.getName().toLowerCase() + ".jar")) {
				PVPArena.instance.getAmm().reload();
				Arenas.tellPlayer(sender, "uninstalled: " + am.getName());
				return;
			}
			Arenas.tellPlayer(sender, "error while uninstalling " + am.getName());
			return;
		}
		ArenaRegion ar = PVPArena.instance.getArm().getModule(name);
		if (ar != null) {
			if (remove("pa_r_"+ar.getName().toLowerCase() + ".jar")) {
				PVPArena.instance.getArm().reload();
				Arenas.tellPlayer(sender, "uninstalled: " + ar.getName());
				return;
			}
			Arenas.tellPlayer(sender, "error while uninstalling " + ar.getName());
			return;
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

	private boolean remove(String file) {
		db.i("removing file " + file);
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
		File destination = new File(PVPArena.instance.getDataFolder().getPath() + folder);

		File destFile = new File(destination, file);
		if (destFile.exists()) {
			db.i("file exists");
			return destFile.delete();
		}
		return false;
	}

	@Override
	public String getName() {
		return "PAUpdate";
	}
}
