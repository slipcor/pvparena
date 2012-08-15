package net.slipcor.pvparena.commands;

import org.bukkit.command.CommandSender;

public class PAA_Update extends PA__Command {

	public PAA_Update() {
		super(new String[0]);
	}

	@Override
	public void commit(CommandSender sender, String[] args) {
		PAA_Uninstall ui = new PAA_Uninstall();
		ui.commit(sender, args);
		PAA_Install i = new PAA_Install();
		i.commit(sender, args);
	}


	@Override
	public String getName() {
		return this.getClass().getName();
	}
}
