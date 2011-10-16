package craftyn.pvparena;

import java.io.File;

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
 * version: v0.1.10 - config: only start with even teams
 * 
 * history:
 * 		v0.1.9 - configure teleport locations
 * 		v0.1.2 - class permission requirement
 * 		v0.1.1 - ready block configurable
 * 		v0.0.0 - copypaste
 */

public class PAPlayerListener extends PlayerListener {
	public PVPArena plugin;

	public PAPlayerListener(PVPArena instance) {
		this.plugin = instance;
	}

	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();

		if (!(PVPArena.fightUsersRespawn.containsKey(player.getName())))
			return;
		Location l = PVPArena.getCoords("spectator");
		event.setRespawnLocation(l);
		PVPArena.loadPlayer(player, PVPArena.sTPdeath);
		PVPArena.fightUsersRespawn.remove(player.getName());
		PVPArena.fightUsersTeam.remove(player.getName());
		PVPArena.fightUsersClass.remove(player.getName());	
	}

	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (PVPArena.fightUsersTeam.containsKey(player.getName())) {
			if (PVPArena.fightUsersTeam.get(player.getName()) == "red") {
				PVPArena.redTeam -= 1;
				this.plugin.tellEveryoneExcept(player,
						ChatColor.RED + player.getName() + ChatColor.WHITE
								+ " has left the fight!");
			} else {
				PVPArena.blueTeam -= 1;
				this.plugin.tellEveryoneExcept(player,
						ChatColor.BLUE + player.getName() + ChatColor.WHITE
								+ " has left the fight!");
			}
			if (PVPArena.checkEnd())
				return;
			PVPArena.removePlayer(player, PVPArena.sTPexit);
		}
	}

	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		if (PVPArena.fightUsersTeam.containsKey(player.getName())) {
			player.sendMessage(ChatColor.YELLOW + "[PVP Arena] "
					+ ChatColor.WHITE + "Not so fast! No Cheating!");
			event.setCancelled(true);
		}
	}

	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();

		if ((!(PVPArena.fightUsersTeam.containsKey(player.getName())))
				|| (PVPArena.fightTelePass.containsKey(player.getName())))
			return;
		event.setCancelled(true);
		PVPArena.tellPlayer(player, "Please use '/pa leave' to exit the fight!");
	}

	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		event.getAction();
		if ((event.getAction() == Action.LEFT_CLICK_BLOCK)
				&& ((((PVPArena.Permissions == null) && (player.isOp())) || ((PVPArena.Permissions != null)
						&& (PVPArena.Permissions.has(player, "admin"))
						&& (player.getItemInHand().getTypeId() == PVPArena.wand)))) && (PVPArena.regionmodify)) {
			PVPArena.pos1 = event.getClickedBlock().getLocation();
			PVPArena.tellPlayer(player, "First position set.");
			return;
		}

		if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& ((((PVPArena.Permissions == null) && (player.isOp())) || ((PVPArena.Permissions != null)
						&& (PVPArena.Permissions.has(player, "admin"))
						&& (player.getItemInHand().getTypeId() == PVPArena.wand)))) && (PVPArena.regionmodify)) {
			PVPArena.pos2 = event.getClickedBlock().getLocation();
			PVPArena.tellPlayer(player, "Second position set.");
			return;
		}

		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			if (block.getState() instanceof Sign) {
				Sign sign = (Sign) block.getState();

				if (PVPArena.fightInProgress)
					return;
				
				if ((PVPArena.fightClasses.containsKey(sign.getLine(0)) || (sign.getLine(0).equalsIgnoreCase("custom")))
						&& (PVPArena.fightUsersTeam.containsKey(player.getName()))) {
					
					Configuration config = new Configuration(new File("plugins/pvparena",
							"config.yml"));
					config.load();
					boolean classperms = false;
					if (config.getProperty("general.classperms") != null) {
						try {
							classperms = (Boolean) config.getProperty("general.classperms");
						} catch (Exception e) {
							System.out.print("[PVP Arena] unrecognized general.classperms");
						}
					}
					
					if (classperms) {
						if (!PVPArena.hasPerms(player, "fight.group." + sign.getLine(0))) {
							player.sendMessage(ChatColor.YELLOW
									+ "[PVP Arena] "
									+ ChatColor.WHITE
									+ "You don't have permission for that class.");
							return;
						}
					}
					
					int i=0;
					
					if (PVPArena.fightUsersClass.containsKey(player.getName())) {
						// already selected class, remove it!
						Sign sSign = PVPArena.fightSigns.get(player.getName());
						
						for (i=2;i<4;i++) {
							if (sSign.getLine(i).equalsIgnoreCase(player.getName())) {
								sSign.setLine(i, "");
								sSign.update();
								PVPArena.clearInventory(player);
								break;
							}
						}
					}

					for (i=2;i<4;i++) {
						if (sign.getLine(i).equals("")) {
							PVPArena.fightSigns.put(player.getName(), sign);
							PVPArena.fightUsersClass.put(player.getName(),sign.getLine(0));
							sign.setLine(i, player.getName());
							sign.update();
							if (sign.getLine(0).equalsIgnoreCase("custom")) {
								PVPArena.setInventory(player);
							} else {
								PVPArena.giveItems(player);
							}
							return;
						}
					}
					
					player.sendMessage(ChatColor.YELLOW
							+ "[PVP Arena] "
							+ ChatColor.WHITE
							+ "There are too many of this class, pick another class.");
					
				}
				return;
			}
		}

		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			

			Configuration config = new Configuration(new File("plugins/pvparena","config.yml"));
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
						System.out.print("[PVP Arena] unrecognized Material:" + sMat);
					}
				}
			}

			if (block.getTypeId() == mMat.getId()) {

				
				if (!PVPArena.fightUsersTeam.containsKey(player.getName()))
					return;

				if (PVPArena.fightInProgress)
					return;
				
				String color = (String) PVPArena.fightUsersTeam.get(player.getName());

				if (!PVPArena.teamReady(color)) {
					player.sendMessage(ChatColor.YELLOW + "[PVP Arena] "
							+ ChatColor.WHITE
							+ "Not all of your team has picked a class!");
					return;
				}
				
				if (PVPArena.forceeven) {
					if (PVPArena.redTeam != PVPArena.blueTeam) {

						player.sendMessage(ChatColor.YELLOW + "[PVP Arena] "
								+ ChatColor.WHITE
								+ "Waiting for the teams to have equal player number!");
						return;
					}
				}
				
				if (color == "red") {
					PVPArena.redTeamIronClicked = true;
					PVPArena.tellEveryone(ChatColor.RED + "Red "
							+ ChatColor.WHITE + "team is ready!");

					if ((PVPArena.teamReady("blue"))
							&& (PVPArena.blueTeamIronClicked)) {
						this.plugin.teleportAllToSpawn();
						PVPArena.fightInProgress = true;
						PVPArena.tellEveryone("Let the fight begin!");
					}

				} else if (color == "blue") {
					PVPArena.blueTeamIronClicked = true;
					PVPArena.tellEveryone(ChatColor.BLUE + "Blue "
							+ ChatColor.WHITE + "team is ready!");

					if ((PVPArena.teamReady("red"))
							&& (PVPArena.redTeamIronClicked)) {
						this.plugin.teleportAllToSpawn();
						PVPArena.fightInProgress = true;
						PVPArena.tellEveryone("Let the fight begin!");
					}

				}

			}
		}
	}
}