package net.slipcor.pvparena.listeners;

import java.io.File;

import net.slipcor.pvparena.PVPArenaPlugin;
import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.managers.ArenaManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.config.Configuration;

/*
 * PlayerListener class
 * 
 * author: slipcor
 * 
 * version: v0.3.2 - Classes now can store up to 6 players
 * 
 * history:
 * 
 *     v0.3.1 - New Arena! FreeFight
 *     v0.3.0 - Multiple Arenas
 * 	   v0.2.1 - cleanup, comments
 * 	   v0.1.10 - config: only start with even teams
 * 	   v0.1.9 - configure teleport locations
 * 	   v0.1.2 - class permission requirement
 * 	   v0.1.1 - ready block configurable
 * 	   v0.0.0 - copypaste
 */

public class PAPlayerListener extends PlayerListener {

	public PAPlayerListener() {}

	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();

		Arena arena = ArenaManager.getArenaByPlayer(player);
		if (arena == null) {
			return; // no fighting player => OUT
		}
		if (!(arena.fightUsersRespawn.containsKey(player.getName()))){
			return; // no fighting player => OUT
		}
		Location l = arena.getCoords(arena.sTPdeath);
		event.setRespawnLocation(l);
		
		arena.removePlayer(player, arena.sTPdeath);
		arena.fightUsersRespawn.remove(player.getName());		
	}

	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Arena arena = ArenaManager.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT
		if (arena.fightUsersTeam.get(player.getName()) == "red") {
			arena.redTeam -= 1;
			arena.tellEveryoneExcept(player,PVPArenaPlugin.lang.parse("playerleave", ChatColor.RED + player.getName() + ChatColor.YELLOW));
		} else if (arena.fightUsersTeam.get(player.getName()) == "blue") {
			arena.blueTeam -= 1;
			arena.tellEveryoneExcept(player,PVPArenaPlugin.lang.parse("playerleave", ChatColor.BLUE + player.getName() + ChatColor.YELLOW));
		} // tell that *colored* player left
		else {
			arena.tellEveryoneExcept(player,PVPArenaPlugin.lang.parse("playerleave", ChatColor.WHITE + player.getName() + ChatColor.YELLOW));
		}
		arena.removePlayer(player, arena.sTPexit);
		arena.checkEnd();
	}

	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		Arena arena = ArenaManager.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT
		
		Arena.tellPlayer(player,(PVPArenaPlugin.lang.parse("dropitem")));
		event.setCancelled(true);
		// cancel the drop event for fighting players, with message
	}

	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();

		Arena arena = ArenaManager.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT
		
		if (arena.fightTelePass.containsKey(player.getName()))
			return; // if allowed => OUT
		
		event.setCancelled(true); // cancel and tell
		Arena.tellPlayer(player, PVPArenaPlugin.lang.parse("usepatoexit"));
	}

	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
	
		Arena arena = ArenaManager.getArenaByName(Arena.regionmodify);
		
		if (arena != null && PVPArenaPlugin.hasAdminPerms(player) && (player.getItemInHand().getTypeId() == arena.wand)) {
			// - modify mode is active
			// - player has admin perms
			// - player has wand in hand
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				arena.pos1 = event.getClickedBlock().getLocation();
				Arena.tellPlayer(player, PVPArenaPlugin.lang.parse("pos1"));
				event.setCancelled(true); // no destruction in creative mode :)
				return; // left click => pos1
			}
	
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				arena.pos2 = event.getClickedBlock().getLocation();
				Arena.tellPlayer(player, PVPArenaPlugin.lang.parse("pos2"));
				return; // right click => pos2
			}
		}
		arena = ArenaManager.getArenaByPlayer(player);

		if (arena == null || arena.fightInProgress)
			return; // not fighting or fight already in progress => OUT
		
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			if (block.getState() instanceof Sign) {
				Sign sign = (Sign) block.getState();

				if ((arena.fightClasses.containsKey(sign.getLine(0)) || (sign.getLine(0).equalsIgnoreCase("custom")))
						&& (arena.fightUsersTeam.containsKey(player.getName()))) {
					
					Configuration config = new Configuration(new File("plugins/pvparena","config_" + arena.name + ".yml"));
					config.load();
					boolean classperms = false;
					if (config.getProperty("general.classperms") != null) {
						try { // fetch classperms setting
							classperms = (Boolean) config.getProperty("general.classperms");
						} catch (Exception e) {
							config.setProperty("general.classperms", false);
						}
					}
					
					if (classperms) {
						if (!PVPArenaPlugin.hasPerms(player, "fight.group." + sign.getLine(0))) {
							player.sendMessage(PVPArenaPlugin.lang.parse("msgprefix") + PVPArenaPlugin.lang.parse("classperms"));
							return; // class permission desired and failed => announce and OUT
						}
					}
					
					int i=0;
					
					if (arena.fightUsersClass.containsKey(player.getName())) {
						// already selected class, remove it!
						Sign sSign = arena.fightSigns.get(player.getName());
						
						for (i=2;i<4;i++) {
							if (sSign.getLine(i).equalsIgnoreCase(player.getName())) {
								sSign.setLine(i, "");
								sSign.update();
								Arena.clearInventory(player);
								break; // remove found player, break!
							}
						}

						sSign = arena.getNext(sSign);
						
						if (sSign != null) {
							for (i=0;i<4;i++) {
								if (sSign.getLine(i).equalsIgnoreCase(player.getName())) {
									sSign.setLine(i, "");
									sSign.update();
									Arena.clearInventory(player);
									break; // remove found player, break!
								}
							}
						}
					}

					for (i=2;i<4;i++) {
						if (sign.getLine(i).equals("")) {
							arena.fightSigns.put(player.getName(), sign);
							arena.fightUsersClass.put(player.getName(),sign.getLine(0));
							sign.setLine(i, player.getName());
							sign.update();
							// select class
							if (sign.getLine(0).equalsIgnoreCase("custom")) {
								arena.setInventory(player); // if custom, give stuff back
							} else {
								arena.giveItems(player);
							}
							return;
						}
					}
					
					Sign nSign = arena.getNext(sign);
					
					if (nSign != null) {
						for (i=0;i<4;i++) {
							if (nSign.getLine(i).equals("")) {
								arena.fightSigns.put(player.getName(), sign);
								arena.fightUsersClass.put(player.getName(),sign.getLine(0));
								nSign.setLine(i, player.getName());
								nSign.update();
								// select class
								if (sign.getLine(0).equalsIgnoreCase("custom")) {
									arena.setInventory(player); // if custom, give stuff back
								} else {
									arena.giveItems(player);
								}
								return;
							}
						}
					}
					player.sendMessage(PVPArenaPlugin.lang.parse("msgprefix") + PVPArenaPlugin.lang.parse("toomanyplayers"));
				}
				return;
			}
		}
		
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			

			Configuration config = new Configuration(new File("plugins/pvparena","config_" + arena.name + ".yml"));
			config.load();
			Material mMat = Material.IRON_BLOCK;
			if (config.getProperty("general.readyblock") != null) {
				try {
					mMat = Material.getMaterial((Integer) config.getProperty("general.readyblock"));
				} catch (Exception e) {
					String sMat = config.getString("general.readyblock");
					try {
						mMat = Material.getMaterial(sMat);
					} catch (Exception e2) {
						PVPArenaPlugin.lang.log_warning("matnotfound", sMat);
					}
				}
			}

			if (block.getTypeId() == mMat.getId()) {				
				if (!arena.fightUsersTeam.containsKey(player.getName()))
					return; // not a fighting player => OUT			
				if (!arena.fightUsersClass.containsKey(player.getName()))
					return; // not a fighting player => OUT
				
				String color = (String) arena.fightUsersTeam.get(player.getName());

				if (!arena.teamReady(color)) {
					player.sendMessage(PVPArenaPlugin.lang.parse("msgprefix") + PVPArenaPlugin.lang.parse("notready"));
					return; // team not ready => announce
				}
				
				if (arena.forceeven) {
					if (arena.redTeam != arena.blueTeam) {
						player.sendMessage(PVPArenaPlugin.lang.parse("msgprefix") + PVPArenaPlugin.lang.parse("waitequal"));
						return; // even teams desired, not done => announce
					}
				}
				
				if (color == "red") {
					arena.redTeamIronClicked = true;
					arena.tellEveryone(PVPArenaPlugin.lang.parse("ready", ChatColor.RED + "Red" + ChatColor.WHITE));

					if ((arena.teamReady("blue"))
							&& (arena.blueTeamIronClicked)) {
						arena.teleportAllToSpawn();
						arena.fightInProgress = true;
						arena.tellEveryone(PVPArenaPlugin.lang.parse("begin"));
					}
					// check blue, start if ready
				} else if (color == "blue") {
					arena.blueTeamIronClicked = true;
					arena.tellEveryone(PVPArenaPlugin.lang.parse("ready", ChatColor.BLUE + "Blue" + ChatColor.WHITE));

					if ((arena.teamReady("red"))
							&& (arena.redTeamIronClicked)) {
						arena.teleportAllToSpawn();
						arena.fightInProgress = true;
						arena.tellEveryone(PVPArenaPlugin.lang.parse("begin"));
					}
					// check red, start if ready
				} else {
					arena.teleportAllToSpawn();
					arena.fightInProgress = true;
					arena.tellEveryone(PVPArenaPlugin.lang.parse("begin"));
				}
			}
		}
	}
}