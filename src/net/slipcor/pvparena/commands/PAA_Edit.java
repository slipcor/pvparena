package net.slipcor.pvparena.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import org.bukkit.command.CommandSender;

public class PAA_Edit extends PAA__Command {
	
	public static HashMap<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Edit() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new HashSet<Integer>(Arrays.asList(0)))) {
			return;
		}
		
		PAA__Command cmd;
		
		if (arena.isLocked()) {
			cmd = new PAA_Enable();
		} else {
			cmd = new PAA_Disable();
		}
		
		cmd.commit(arena, sender, args);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
