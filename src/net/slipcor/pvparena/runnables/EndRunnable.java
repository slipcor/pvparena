package net.slipcor.pvparena.runnables;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language.MSG;

/**
 * <pre>Arena Runnable class "End"</pre>
 * 
 * An arena timer counting down to the end of an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.8
 */

public class EndRunnable extends ArenaRunnable {
	private final static Debug DEBUG = new Debug(40);

	/**
	 * create a timed arena runnable
	 * 
	 * @param arena
	 *            the arena we are running in
	 * @param seconds
	 */
	public EndRunnable(final Arena arena, final int seconds) {
		super(MSG.TIMER_RESETTING_IN.getNode(), seconds, null, arena, false);
		arena.getDebugger().i("EndRunnable constructor");
		if (arena.endRunner != null) {
			arena.endRunner.cancel();
			arena.endRunner = null;
		}
		arena.realEndRunner = this;
	}

	@Override
	protected void commit() {
		arena.getDebugger().i("EndRunnable commiting");
		
		arena.setRound(arena.getRound()+1);
		
		if (arena.getRound() >= arena.getRoundCount()) {
			arena.getDebugger().i("rounds done!");
		
			arena.reset(false);
			if (arena.realEndRunner != null) {
				arena.realEndRunner = null;
			}
			if (arena.endRunner != null) {
				arena.endRunner.cancel();
				arena.endRunner = null;
			}
		} else {
			arena.getDebugger().i("Starting round #" + arena.getRound());
			
			if (arena.realEndRunner != null) {
				arena.realEndRunner = null;
			}
			if (arena.endRunner != null) {
				arena.endRunner.cancel();
				arena.endRunner = null;
			}
			
			PACheck.handleStart(arena, null);
			
			for (ArenaPlayer ap : arena.getFighters()) {
				arena.unKillPlayer(ap.get(), ap.get().getLastDamageCause().getCause(),
						ap.get().getLastDamageCause().getEntity());
				
				final List<ItemStack> items = new ArrayList<ItemStack>();

				for (ItemStack is : ap.get().getInventory().getArmorContents()) {
					items.add(is);
				}

				for (ItemStack is : ap.get().getInventory().getContents()) {
					items.add(is);
				}
				new InventoryRefillRunnable(arena, ap.get(), items);
			}
		}
	}
	
	@Override
	protected void warn() {
		PVPArena.instance.getLogger().warning("EndRunnable not scheduled yet!");
	}
}
