package net.slipcor.pvparena.commands;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaGoal;
import net.slipcor.pvparena.neworder.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class PAA_Uninstall extends PA__Command {

	public PAA_Uninstall() {
		super(new String[0]);
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}

		if (!this.argCountValid(sender, args,
				new HashSet<Integer>(Arrays.asList(0, 1)))) {
			return;
		}

		// pa install
		// pa install ctf

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(PVPArena.instance.getDataFolder().getPath() + "/install.yml");
		} catch (Exception e) {
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
		ArenaGoal ag = PVPArena.instance.getAtm().getType(name);
		if (ag != null) {
			if (remove("pa_g_" + ag.getName().toLowerCase() + ".jar")) {
				PVPArena.instance.getAtm().reload();
				Arena.pmsg(sender, Language.parse("install.uninstalled",ag.getName()));
				return;
			}
			Arena.pmsg(sender, Language.parse("install.uninstallerr",ag.getName()));
			return;
		}
		ArenaModule am = PVPArena.instance.getAmm().getModule(name);
		if (am != null) {
			if (remove("pa_m_" + am.getName().toLowerCase() + ".jar")) {
				PVPArena.instance.getAmm().reload();
				Arena.pmsg(sender, Language.parse("install.uninstalled",am.getName()));
				return;
			}
			Arena.pmsg(sender, Language.parse("install.uninstallerr",am.getName()));
			return;
		}
	}

	private void listVersions(CommandSender sender, YamlConfiguration cfg,
			String s) {
		Arena.pmsg(sender, "--- PVP Arena Version Update information ---");
		Arena.pmsg(sender, "[§7uninstalled§r | §einstalled§r]");
		Arena.pmsg(sender, "[§coutdated§r | §alatest version§r]");
		if (s == null || s.toLowerCase().equals("arenas")) {
			Arena.pmsg(sender, "§c--- Arena Goals ----> /goals");
			Set<String> entries = cfg.getConfigurationSection("goals").getKeys(
					false);
			for (String key : entries) {
				String value = cfg.getString("goals." + key);
				ArenaGoal goal = PVPArena.instance.getAtm().getType(key);
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

	private void disableModule(String file) {
		if (file.startsWith("pa_g")) {
			ArenaGoal g = PVPArena.instance.getAtm().getType(file);
			g.unload();
		} else if (file.startsWith("pa_m")) {
			ArenaModule g = PVPArena.instance.getAmm().getModule(file);
			g.unload();
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	private boolean remove(String file) {
		disableModule(file);
		String folder = null;
		if (file.startsWith("pa_g")) {
			folder = "/goals/";
		} else if (file.startsWith("pa_m")) {
			folder = "/mods/";
		}
		if (folder == null) {
			System.out.print("[SEVERE] unable to fetch file: " + file);
			return false;
		}
		File destination = new File(PVPArena.instance.getDataFolder().getPath()
				+ folder);

		File destFile = new File(destination, file);
		if (destFile.exists()) {
			return destFile.delete();
		}
		return false;
	}
}
