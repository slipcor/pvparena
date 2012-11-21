package net.slipcor.pvparena.runnables;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
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
		Bukkit.getScheduler().cancelTask(a.END_ID);
		a.END_ID = -1;
		arena.REALEND_ID = super.id;
		id = super.id;
	}

	@Override
	protected void commit() {
		db.i("EndRunnable commiting");
		
		arena.setRound(arena.getRound()+1);
		
		if (arena.getRound() >= arena.getRoundCount()) {
			db.i("rounds done!");
		
			arena.reset(false);
			Bukkit.getScheduler().cancelTask(arena.REALEND_ID);
			arena.REALEND_ID = -1;
			Bukkit.getScheduler().cancelTask(arena.END_ID);
			arena.END_ID = -1;
			Bukkit.getScheduler().cancelTask(id);
		} else {
			db.i("Starting round #" + arena.getRound());
			
			Bukkit.getScheduler().cancelTask(arena.REALEND_ID);
			arena.REALEND_ID = -1;
			Bukkit.getScheduler().cancelTask(arena.END_ID);
			arena.END_ID = -1;
			Bukkit.getScheduler().cancelTask(id);
			
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
