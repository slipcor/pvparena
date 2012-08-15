package net.slipcor.pvparena.commands;


import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PAA_AutoSetup extends PA__Command {
	public static String active = null;
	public static Boolean manual = null;

	public PAA_AutoSetup() {
		super(new String[] {"pvparena.create"});
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}
		
		if (!(sender instanceof Player)) {
			Arena.pmsg(sender, Language.parse("command.onlyplayers"));
			return;
		}
		
		Player player = (Player) sender;
		
		if (active != null && !active.equals(player.getName())) {
			Arena.pmsg(player, Language.parse("autosetup.running", active));
			return;
		}
		
		// setup running, and we are the player!

		String sAutomatic = Language.parse("autosetup.automatic");
		String sManual = Language.parse("autosetup.manual");
		
		if (active == null) {
			// first start. Announce welcome and first question
			active = player.getName();
			Arena.pmsg(player, Language.parse("autosetup.welcome"));
			Arena.pmsg(player, Language.parse("autosetup.automanual", sAutomatic, sManual));
			return;
		}
		
		if (manual == null) {
			if ((args.length == 1) && (args[0].equals(sAutomatic) || args[0].equals(sManual))) {
				manual = args[0].equals(sManual);
				Arena.pmsg(player, Language.parse("autosetup.modeselected", args[0]));
				
				if (manual) {
					
				} else {
					setupDefaultArena(player);
					manual = null;
					active = null;
				}
				return;
			}
			Arena.pmsg(player, Language.parse("autosetup.automanual", sAutomatic, sManual));
			return;
		}
		
		if (!manual.booleanValue()) {
			return;
		}
		
		// we are here to answer even more questions about the arena!
	}

	private void setupDefaultArena(Player p) {
		PA__Command cmd = new PAA_Create();
		
		// /pa create default
		cmd.commit(p, new String[]{"default"});
		
		Arena a = Arenas.getArenaByName("default");
		
		PAA__Command acmd = new PAA_Region();
		
		// /pa default region
		acmd.commit(a, p, new String[]{});
		
		//TODOsome day call all functions to setup
		// battlefield
		//   shape: cuboid
		//   height: 16 blocks
		//   radius: 16 blocks
		//   border: GLASS
		//   protection:
		//   - full
		//   - ^place
		//   - ^break
		//   - ^ignite
		// spectator
		//   shape: cylinder
		//   height: 8 blocks
		//   radius: 32 blocks
		//   position: around
		//   protection:
		//   - break
		// goals
		//   time: 30m
		//
		
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
