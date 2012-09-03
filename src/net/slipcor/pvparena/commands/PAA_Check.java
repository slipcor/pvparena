package net.slipcor.pvparena.commands;

import java.util.HashMap;
import net.slipcor.pvparena.arena.Arena;
import org.bukkit.command.CommandSender;

/**
 * <pre>PVP Arena CHECK Command class</pre>
 * 
 * A command to check an arena config
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class PAA_Check extends PAA__Command {
	
	public static HashMap<String, Arena> activeSelections = new HashMap<String, Arena>();

	public PAA_Check() {
		super(new String[] {});
	}

	@Override
	public void commit(Arena arena, CommandSender sender, String[] args) {
		if (!this.hasPerms(sender, arena)) {
			return;
		}
		
		if (!this.argCountValid(sender, arena, args, new Integer[]{0})) {
			return;
		}
		
		String s = "";
		//TODO thoroughly check the config for errors
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
