package net.slipcor.pvparena.modules;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.runnables.ArenaWarmupRunnable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <pre>Arena Module class "BattlefieldJoin"</pre>
 * 
 * Enables direct joining to the battlefield
 * 
 * @author slipcor
 * 
 * @version v0.9.5
 */

public class WarmupJoin extends ArenaModule {
	
	private int priority = 2;

	public WarmupJoin() {
		super("WarmupJoin");
		db = new Debug(300);
	}
	
	@Override
	public String version() {
		return "v0.9.9.9";
	}

	public PACheck checkJoin(Arena arena, CommandSender sender, PACheck result, boolean join) {
		
		if (result.getPriority() > this.priority) {
			return result; // Something already is of higher priority, ignore!
		}
		
		Player p = (Player) sender;
		
		if (arena == null) {
			return result; // arena is null - maybe some other mod wants to handle that? ignore!
		}
		
		if (arena.isLocked() && !p.hasPermission("pvparena.admin") && !(p.hasPermission("pvparena.create") && arena.getOwner().equals(p.getName()))) {
			result.setError(this, Language.parse(MSG.ERROR_DISABLED));
			return result;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(sender.getName());
		
		if (ap.getArena() != null) {
			result.setError(this, Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, ap.getArena().getName()));
			return result;
		}
		
		result.setPriority(this, priority);
		return result;
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return (arena.getArenaConfig().getInt(CFG.TIME_WARMUPCOUNTDOWN) > 0);
	}

	@Override
	public void commitJoin(Arena arena, Player sender, ArenaTeam team) {
		new ArenaWarmupRunnable(arena, ArenaPlayer.parsePlayer(sender.getName()), team.getName(), false, arena.getArenaConfig().getInt(CFG.TIME_WARMUPCOUNTDOWN));
	}

	@Override
	public void commitSpectate(Arena arena, Player sender) {
		new ArenaWarmupRunnable(arena, ArenaPlayer.parsePlayer(sender.getName()), null, true, arena.getArenaConfig().getInt(CFG.TIME_WARMUPCOUNTDOWN));
	}
	
	@Override
	public void toggleActivity(Arena arena) {
		return;
	}
}