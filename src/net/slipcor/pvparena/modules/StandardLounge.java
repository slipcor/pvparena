package net.slipcor.pvparena.modules;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.neworder.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StandardLounge extends ArenaModule {
	
	private int priority = 1;

	public StandardLounge() {
		super("StandardLounge");
		// TODO Auto-generated constructor stub
	}

	public PACheckResult checkJoin(Arena arena, CommandSender sender, PACheckResult result, boolean join) {
		if (!join)
			return result; // we only care about joining, ignore spectators
		
		if (result.getPriority() > this.priority) {
			return result; // Something already is of higher priority, ignore!
		}
		
		Player p = (Player) sender;
		
		if (arena == null) {
			return result; // arena is null - maybe some other mod wants to handle that? ignore!
		}
		
		result.setModName(this.getName());
		
		if (arena.isLocked() && !p.hasPermission("pvparena.admin") && !(p.hasPermission("pvparena.create") && arena.getOwner().equals(p.getName()))) {
			result.setPriority(priority+1000);
			result.setError(Language.parse("error.noperm", Language.parse("error.nopermjoin")));
			return result;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (ap.getArena() != null) {
			result.setError(Language.parse("arena.alreadyin", ap.getArena().getName()));
			return result;
		}
		
		result.setPriority(priority);
		return result;
	}

	public PACheckResult checkStart(Arena arena, PACheckResult result) {
		if (result.getPriority() > this.priority) {
			return result; // Something already is of higher priority, ignore!
		}
		
		if (arena == null) {
			return result; // arena is null - maybe some other mod wants to handle that? ignore!
		}
		
		result.setModName(this.getName());
		
		String error = String.valueOf(arena.ready()); //TODO
		
		if (error != null) {
			result.setError(error);
			return result;
		}
		
		result.setPriority(priority);
		return result;
	}

	@Override
	public void parseJoin(Arena arena, CommandSender sender, ArenaTeam team) {
		// standard join --> lounge
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		ArenaPlayer.prepareInventory(arena, ap.get());
		arena.tpPlayerToCoordName(ap.get(), "lounge");
		arena.msg(sender, Language.parse(arena, "lounge"));
		team.add(ap);
	}
}