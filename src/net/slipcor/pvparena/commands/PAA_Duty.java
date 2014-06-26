package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Arrays;
import java.util.List;

/**
 * <pre>PVP Arena DUTY Command class</pre>
 * 
 * A command to toggle the override permission
 * 
 * @author slipcor
 */

public class PAA_Duty extends AbstractGlobalCommand {

	public PAA_Duty() {
		super(new String[] {});
	}

	@Override
	public void commit(final CommandSender sender, final String[] args) {
		if (!this.hasPerms(sender)) {
			return;
		}
		
		if (!argCountValid(sender, args, new Integer[]{0})) {
			return;
		}
		
		boolean perm = true;
		
		for (PermissionAttachmentInfo pai : sender.getEffectivePermissions()) {
			if (pai.getPermission().equals("pvparena.override")) {
				perm = !pai.getValue();
				sender.removeAttachment(pai.getAttachment());
				break;
			}
		}
		
		sender.addAttachment(PVPArena.instance, "pvparena.override", perm);
		sender.recalculatePermissions();
		
		if (perm) {
			Arena.pmsg(sender, Language.parse(MSG.DUTY_TRUE));
		} else {
			Arena.pmsg(sender, Language.parse(MSG.DUTY_FALSE));
		}
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void displayHelp(final CommandSender sender) {
		Arena.pmsg(sender, Help.parse(HELP.DUTY));
	}

    @Override
    public List<String> getMain() {
        return Arrays.asList("duty");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!du");
    }

    @Override
    public CommandTree<String> getSubs(final Arena nothing) {
        return new CommandTree<String>(null);
    }
}
