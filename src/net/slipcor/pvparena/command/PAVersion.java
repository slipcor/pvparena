package net.slipcor.pvparena.command;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaModule;
import net.slipcor.pvparena.neworder.ArenaRegion;
import net.slipcor.pvparena.neworder.ArenaType;

import org.bukkit.command.CommandSender;

public class PAVersion extends PA_Command {

	@Override
	public void commit(CommandSender sender, String[] args) {
		Arenas.tellPlayer(sender, "§e§n-- PVP Arena version information --");
		Arenas.tellPlayer(sender, "§ePVP Arena version: §l" + PVPArena.instance.getDescription().getVersion());
		if (args.length < 2 || args[1].toLowerCase().startsWith("gamemode")) {
			Arenas.tellPlayer(sender, "§7-----------------------------------");
			Arenas.tellPlayer(sender, "§cArenaTypes:");
			for (ArenaType at : PVPArena.instance.getAtm().getTypes()) {
				Arenas.tellPlayer(sender,  "§c" + at.getName() + " - " + at.version());
			}
		}
		if (args.length < 2 || args[1].toLowerCase().startsWith("module")) {
		Arenas.tellPlayer(sender, "§7-----------------------------------");
			Arenas.tellPlayer(sender, "§aModules:");
			for (ArenaModule am : PVPArena.instance.getAmm().getModules()) {
				Arenas.tellPlayer(sender,  "§a" + am.getName() + " - " + am.version());
			}
		}
		if (args.length < 2 || args[1].toLowerCase().startsWith("region")) {
			Arenas.tellPlayer(sender, "§7-----------------------------------");
			Arenas.tellPlayer(sender, "§bRegions:");
			for (ArenaRegion ar : PVPArena.instance.getArm().getRegions()) {
				Arenas.tellPlayer(sender,  "§b" + ar.getName() + " - " + ar.version());
			}
		}
	}

	@Override
	public String getName() {
		return "PAVersion";
	}

}
