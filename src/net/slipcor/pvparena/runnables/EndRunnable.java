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
	private Debug db = new Debug(40);

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 * @param i
	 */
	public EndRunnable(Arena a, int i) {
		super(MSG.TIMER_RESETTING_IN.getNode(), i, null, a, false);
		db.i("EndRunnable constructor");
		if (a.END_ID != null) {
			a.END_ID.cancel();
			a.END_ID = null;
		}
		arena.REALEND_ID = this;
	}

	@Override
	protected void commit() {
		db.i("EndRunnable commiting");
		
		arena.setRound(arena.getRound()+1);
		
		if (arena.getRound() >= arena.getRoundCount()) {
			db.i("rounds done!");
		
			arena.reset(false);
			if (arena.REALEND_ID != null) {
				arena.REALEND_ID = null;
			}
			if (arena.END_ID != null) {
				arena.END_ID.cancel();
				arena.END_ID = null;
			}
		} else {
			db.i("Starting round #" + arena.getRound());
			
			if (arena.REALEND_ID != null) {
				arena.REALEND_ID = null;
			}
			if (arena.END_ID != null) {
				arena.END_ID.cancel();
				arena.END_ID = null;
			}
			
			PACheck.handleStart(arena, null);
			
			for (ArenaPlayer ap : arena.getFighters()) {
				arena.unKillPlayer(ap.get(), ap.get().getLastDamageCause().getCause(),
						ap.get().getLastDamageCause().getEntity());
				
				List<ItemStack> items = new ArrayList<ItemStack>();

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
