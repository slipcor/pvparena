package net.slipcor.pvparena.command;

import org.bukkit.command.CommandSender;

public class PAUpdate extends PA_Command {

	@Override
	public void commit(CommandSender sender, String[] args) {
		PAUninstall ui = new PAUninstall();
		ui.commit(sender, args);
		PAInstall i = new PAInstall();
		i.commit(sender, args);
	}

	@Override
	public String getName() {
		return "PAUpdate";
	}
}
