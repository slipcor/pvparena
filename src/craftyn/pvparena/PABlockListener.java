package craftyn.pvparena;

import java.io.File;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

/*
 * BlockListener class
 * 
 * author: craftyn
 * editor: slipcor
 * 
 * version: v0.0.0a - code tweaks
 * 
 * history:
 * 		v0.0.0 - copypaste
 */

public class PABlockListener extends BlockListener {
	public static PVPArena plugin;

	public PABlockListener(PVPArena instance) {
		plugin = instance;
	}

	public void onBlockBreak(BlockBreakEvent event) {
		if ((!(PVPArena.protection)) || (!(PVPArena.disableblockdamage)))
			return; // we don't need protection => OUT!
		Configuration config = new Configuration(new File("plugins/pvparena","config.yml"));
		config.load(); // load config file
		if ((config.getKeys("protection.region") == null) || (!(config.getString("protection.region.world").equals(event.getBlock().getWorld().getName()))))
			return; // no region defined or wrong world => OUT!
		boolean inside = PVPArena.contains(new Vector(event.getBlock()
				.getLocation().getX(), event.getBlock().getLocation().getY(),
				event.getBlock().getLocation().getZ())); // nice inside detection
		if (!(inside))
			return; // not inside => OUT!
		if (PVPArena.blocktnt) {
			event.setCancelled(true);
			return; // if we block TNT (what is the only restriction possible) => CANCEL AND OUT!
		}
		if (event.getBlock().getTypeId() == 46)
			return; // we do not block TNT, so just return if it is TNT
		event.setCancelled(true);
		return; // CANCEL AND OUT! this is protected property xD
	}

	public void onBlockIgnite(BlockIgniteEvent event) {
		BlockIgniteEvent.IgniteCause cause = event.getCause();
		if ((PVPArena.protection) && (
				((PVPArena.disablelavafirespread) && (cause == BlockIgniteEvent.IgniteCause.LAVA))
			 || ((PVPArena.disableallfirespread) && (cause == BlockIgniteEvent.IgniteCause.SPREAD)) 
			 || ((PVPArena.blocklighter)) && (cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL))
			 ) {

			Configuration config = new Configuration(new File(
					"plugins/pvparena", "config.yml"));
			config.load();
			if ((config.getKeys("protection.region") == null)
					|| (!(config.getString("protection.region.world")
							.equals(event.getBlock().getWorld().getName()))))
				return;
			boolean inside = PVPArena.contains(new Vector(event.getBlock()
					.getLocation().getX(), event.getBlock().getLocation()
					.getY(), event.getBlock().getLocation().getZ()));
			if (!(inside))
				return;
			event.setCancelled(true);
		}
	}

	public void onBlockBurn(BlockBurnEvent event) {
		if ((!(PVPArena.protection)) || (!(PVPArena.disableallfirespread)))
			return;
		Configuration config = new Configuration(new File("plugins/pvparena",
				"config.yml"));
		config.load();
		if ((config.getKeys("protection.region") == null)
				|| (!(config.getString("protection.region.world").equals(event
						.getBlock().getWorld().getName()))))
			return;
		boolean inside = PVPArena.contains(new Vector(event.getBlock()
				.getLocation().getX(), event.getBlock().getLocation().getY(),
				event.getBlock().getLocation().getZ()));
		if (!(inside))
			return;
		event.setCancelled(true);
		return;
	}

	public void onBlockPlace(BlockPlaceEvent event) {
		if ((!(PVPArena.protection)) || (!(PVPArena.disableblockplacement)))
			return;
		Configuration config = new Configuration(new File("plugins/pvparena",
				"config.yml"));
		config.load();
		if ((config.getKeys("protection.region") == null)
				|| (!(config.getString("protection.region.world").equals(event
						.getBlock().getWorld().getName()))))
			return;
		boolean inside = PVPArena.contains(new Vector(event.getBlock()
				.getLocation().getX(), event.getBlock().getLocation().getY(),
				event.getBlock().getLocation().getZ()));
		if (!(inside))
			return;
		event.setCancelled(true);
		return;
	}
}