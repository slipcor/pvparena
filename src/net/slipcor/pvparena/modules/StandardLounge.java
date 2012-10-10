package net.slipcor.pvparena.modules;

import java.util.Iterator;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.runnables.PlayerStateCreateRunnable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>Arena Module class "StandardLounge"</pre>
 * 
 * Enables joining to lounges instead of the battlefield
 * 
 * @author slipcor
 * 
 * @version v0.9.2
 */

public class StandardLounge extends ArenaModule {
	
	private int priority = 1;

	public StandardLounge() {
		super("StandardLounge");
		db = new Debug(300);
	}
	
	@Override
	public String version() {
		return "0.9.1.0";
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
		
		if (ap.getArenaClass() == null) {
			String autoClass = arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);
			if (autoClass != null && !autoClass.equals("none")) {
				if (arena.getClass(autoClass) == null) {
					result.setError(Language.parse(MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
					return result;
				}
			}
		}

		result.setModName(getName());
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

		result.setModName(getName());
		result.setPriority(priority);
		return result;
	}
	
	@Override
	public boolean hasSpawn(Arena arena, String s) {
		if (arena.isFreeForAll()) {
			return s.startsWith("lounge");
		}
		for (ArenaTeam team : arena.getTeams()) {
			if (s.startsWith(team.getName() + "lounge")) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return arena.getArenaConfig().getBoolean(CFG.MODULES_STANDARDLOUNGE_ACTIVE);
	}

	@Override
	public void parseJoin(PACheckResult res, Arena arena, CommandSender sender, ArenaTeam team) {
		if (res == null || !res.getModName().equals(getName())) {
			return;
		}
		
		// standard join --> lounge
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		Bukkit.getScheduler().scheduleAsyncDelayedTask(PVPArena.instance, new PlayerStateCreateRunnable(ap, ap.get()), 2L);
		//ArenaPlayer.prepareInventory(arena, ap.get());
		ap.setLocation(new PALocation(ap.get().getLocation()));
		ap.setArena(arena);
		team.add(ap);
		if (arena.isFreeForAll()) {
			arena.tpPlayerToCoordName(ap.get(), "lounge");
		} else {
			arena.tpPlayerToCoordName(ap.get(), team.getName() + "lounge");
		}
		ap.setStatus(Status.LOUNGE);
		arena.msg(sender, Language.parse(arena, CFG.MSG_LOUNGE));
		if (arena.isFreeForAll()) {
			arena.msg(sender, arena.getArenaConfig().getString(CFG.MSG_YOUJOINED));
			arena.broadcastExcept(sender, Language.parse(arena, CFG.MSG_PLAYERJOINED, sender.getName()));
		} else {
			arena.msg(sender, arena.getArenaConfig().getString(CFG.MSG_YOUJOINEDTEAM).replace("%1%", team.getColoredName()));
			arena.broadcastExcept(sender, Language.parse(arena, CFG.MSG_PLAYERJOINEDTEAM, sender.getName(), team.getColoredName()));
		}
	}
}