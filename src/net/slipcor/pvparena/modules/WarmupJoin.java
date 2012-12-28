package net.slipcor.pvparena.modules;

import java.util.HashSet;

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
 * @version v0.10.2
 */

public class WarmupJoin extends ArenaModule {
	
	private int priority = 2;
	
	HashSet<ArenaPlayer> joiners = new HashSet<ArenaPlayer>();

	public WarmupJoin() {
		super("WarmupJoin");
		db = new Debug(300);
	}
	
	@Override
	public String version() {
		return "v0.10.2.0";
	}

	@Override
	public PACheck checkJoin(CommandSender sender, PACheck result, boolean join) {
		
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
		
		if (joiners.contains(ap)) {
			return result;
		}
		
		if (ap.getArena() != null) {
			db.i(this.getName(), sender);
			result.setError(this, Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, ap.getArena().getName()));
			return result;
		}
		joiners.add(ap);
		
		result.setPriority(this, priority);
		return result;
	}

	@Override
	public void commitJoin(Player sender, ArenaTeam team) {
		new ArenaWarmupRunnable(arena, ArenaPlayer.parsePlayer(sender.getName()), team.getName(), false, arena.getArenaConfig().getInt(CFG.TIME_WARMUPCOUNTDOWN));
	}

	@Override
	public void commitSpectate(Player sender) {
		new ArenaWarmupRunnable(arena, ArenaPlayer.parsePlayer(sender.getName()), null, true, arena.getArenaConfig().getInt(CFG.TIME_WARMUPCOUNTDOWN));
	}

	@Override
	public boolean isInternal() {
		return true;
	}
	
	@Override
	public void reset(boolean force) {
		joiners.clear();
	}
	
	@Override
	public void parsePlayerLeave(Player player, ArenaTeam team) {
		joiners.remove(ArenaPlayer.parsePlayer(player.getName()));
	}
}