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
import net.slipcor.pvparena.core.Update;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaModule;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaType;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PAUpdate extends PA_Command {
	private static Debug db = new Debug(3);

	private boolean checkModuleUpdate(YamlConfiguration cfg, ArenaModule module) {
		String name = module.getName();
		String version = module.version();
		if (cfg.get("modules." + name) != null) {
			if (!cfg.getString("modules." + name).equals(version)) {
				return download("pa_m_" + name + ".jar");
			}
		}
		return false;
	}

	private boolean checkRegionUpdate(YamlConfiguration cfg, ArenaRegion module) {
		String name = module.getName();
		String version = module.version();
		if (cfg.get("regions." + name) != null) {
			if (!cfg.getString("regions." + name).equals(version)) {
				return download("pa_r_" + name + ".jar");
			}
		}
		return false;
	}

	private boolean checkTypeUpdate(YamlConfiguration cfg, ArenaType module) {
		String name = module.getName();
		String version = module.version();
		if (cfg.get("arenas." + name) != null) {
			if (!cfg.getString("arenas." + name).equals(version)) {
				return download("pa_a_" + name + ".jar");
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		// pa update
		// pa update modules
		// pa update arenas
		// pa update plugin

		// pa update ctf
		// pa update unknown

		if (!checkArgs(sender, args, 1, 2)) {
			return;
		}

		db.i("parsing update command of player " + sender.getName()
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

		if (args[1].toLowerCase().equals("arenas")) {
			for (ArenaType at : PVPArena.instance.getAtm().getTypes()) {
				if (at.getName().toLowerCase().equals("teams")) {
					continue;
				}
				if (checkTypeUpdate(config, at)) {
					Arenas.tellPlayer(sender, "updated: " + at.getName());
					continue;
				}
				Arenas.tellPlayer(sender, "error while updating " + at.getName());
				continue;
			}
		} else if (args[1].toLowerCase().equals("modules")) {
			for (ArenaModule am : PVPArena.instance.getAmm().getModules()) {
				if (checkModuleUpdate(config, am)) {
					Arenas.tellPlayer(sender, "updated: " + am.getName());
					continue;
				}
				Arenas.tellPlayer(sender, "error while updating " + am.getName());
				continue;
			}
		} else if (args[1].toLowerCase().equals("regions")) {
			for (ArenaRegion ar : PVPArena.instance.getArm().getRegions()) {
				if (ar.getName().toLowerCase().equals("cuboid")) {
					continue;
				}
				if (checkRegionUpdate(config, ar)) {
					Arenas.tellPlayer(sender, "updated: " + ar.getName());
					continue;
				}
				Arenas.tellPlayer(sender, "error while updating " + ar.getName());
				continue;
			}
		} else if (args[1].toLowerCase().equals("plugin")) {
			Player p = null;
			if (sender instanceof Player) {
				p = (Player) sender;
			}
			Update.message(p, true);
		} else {
			String name = args[1].toLowerCase();
			ArenaType at = PVPArena.instance.getAtm().getType(name);
			if (at != null) {
				if (checkTypeUpdate(config, at)) {
					PVPArena.instance.getAtm().reload();
					Arenas.tellPlayer(sender, "updated: " + at.getName());
					return;
				}
				Arenas.tellPlayer(sender, "error while updating " + at.getName());
				return;
			}
			ArenaModule am = PVPArena.instance.getAmm().getModule(name);
			if (am != null) {
				if (checkModuleUpdate(config, am)) {
					PVPArena.instance.getAmm().reload();
					Arenas.tellPlayer(sender, "updated: " + am.getName());
					return;
				}
				Arenas.tellPlayer(sender, "error while updating " + am.getName());
				return;
			}
			ArenaRegion ar = PVPArena.instance.getArm().getModule(name);
			if (ar != null) {
				if (checkRegionUpdate(config, ar)) {
					PVPArena.instance.getArm().reload();
					Arenas.tellPlayer(sender, "updated: " + ar.getName());
					return;
				}
				Arenas.tellPlayer(sender, "error while updating " + ar.getName());
				return;
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
}
