package net.slipcor.pvparena.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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

		if (args.length == 1 || config.get(args[1]) != null) {
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
		String fileFolder = "/files/";
		if (folder == null) {
			System.out.print("[SEVERE] unable to fetch file: " + file);
			return false;
		}

		File destFile = new File(PVPArena.instance.getDataFolder().getPath() + folder + file);
		File sourceFile = new File(PVPArena.instance.getDataFolder().getPath() + fileFolder + file);
		if (destFile.exists()) {
			db.i("file exists");
			return sourceFile.exists() ? destFile.delete() : destFile.renameTo(sourceFile);
		}
		return false;
	}

	@Override
	public String getName() {
		return "PAUpdate";
	}
}
