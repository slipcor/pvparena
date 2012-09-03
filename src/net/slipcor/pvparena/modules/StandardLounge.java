package net.slipcor.pvparena.modules;

import java.util.Iterator;
import java.util.Set;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>Arena Module class "StandardLounge"</pre>
 * 
 * Enables joining to lounges instead of the battlefield
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class StandardLounge extends ArenaModule {
	
	private int priority = 1;

	public StandardLounge() {
		super("StandardLounge");
		db = new Debug(300);
	}
	
	@Override
	public String version() {
		return "0.9.0.0";
	}

	@Override
	public String checkForMissingSpawns(Arena arena, Set<String> list) {
		// not random! we need teams * 2 (lounge + spawn) + exit + spectator
		db.i("parsing not random");
		Iterator<String> iter = list.iterator();
		int lounges = 0;
		while (iter.hasNext()) {
			String s = iter.next();
			db.i("parsing '" + s + "'");
			if (s.endsWith("lounge") && (!s.equals("lounge"))) {
				lounges++;
			}
		}
		if (lounges == arena.getTeams().size()) {
			return null;
		}

		return lounges + "/" + arena.getTeams().size() + "x lounge";
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
			result.setError(Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_JOIN)));
			return result;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (ap.getArena() != null) {
			result.setError(Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, ap.getArena().getName()));
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
		
		String error = String.valueOf(arena.ready());
		
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