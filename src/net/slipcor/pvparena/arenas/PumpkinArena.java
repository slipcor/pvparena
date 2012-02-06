package net.slipcor.pvparena.arenas;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.ArenaConfigs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * capture the pumkin arena class
 * 
 * -
 * 
 * contains >Capture the Pumpkin< arena methods and variables
 * 
 * @author slipcor
 * 
 * @version v0.6.0
 * 
 */

public class PumpkinArena extends CTFArena {
	HashMap<String, ItemStack> paHeadGears = new HashMap<String, ItemStack>();
	/**
	 * create a capture the pumpkin arena
	 * 
	 * @param sName
	 *            the arena name
	 */
	public PumpkinArena(String sName) {
		super();

		this.name = sName;

		db.i("loading Pumpkin Arena " + name);

		cfg = new Config(new File("plugins/pvparena/config.pumpkin_" + name
				+ ".yml"));
		cfg.load();
		ArenaConfigs.configParse(this, cfg);
		if (cfg.get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			cfg.getYamlConfiguration().addDefault("teams.red",
					ChatColor.RED.name());
			cfg.getYamlConfiguration().addDefault("teams.blue",
					ChatColor.BLUE.name());
			cfg.getYamlConfiguration().options().copyDefaults(true);
			cfg.reloadMaps();
		}
		cfg.save();
		Map<String, Object> tempMap = (Map<String, Object>) cfg
				.getYamlConfiguration().getConfigurationSection("teams")
				.getValues(true);

		for (String sTeam : tempMap.keySet()) {
			this.paTeams.put(sTeam, (String) tempMap.get(sTeam));
			db.i("added team " + sTeam + " => " + this.paTeams.get(sTeam));
		}
	}
	
	/**
	 * parse player interaction
	 * 
	 * @param player
	 *            the player to parse
	 */
	@Override
	public void checkInteract(Player player, Block block) {
		if (block == null) {
			return;
		}
		if (!block.getType().equals(Material.PUMPKIN)) {
			return;
		}
		db.i("pumpkin click!");
		
		Vector vLoc;
		String sTeam;
		Vector vFlag = null;

		if (paTeamFlags.containsValue(player.getName())) {
			db.i("player " + player.getName() + " has got a pumpkin");
			vLoc = block.getLocation().toVector();
			sTeam = playerManager.getTeam(player);
			if (this.getCoords(sTeam + "pumpkin") != null) {
				vFlag = this.getCoords(sTeam + "pumpkin").toVector();
			} else {
				db.i(sTeam + "pumpkin" + " = null");
			}

			db.i("player is in the team " + sTeam);
			if ((vFlag != null && vLoc.distance(vFlag) < 2)) {

				db.i("player is at his pumpkin");
				String flagTeam = getHeldFlagTeam(player.getName());

				db.i("the flag belongs to team " + flagTeam);

				String scFlagTeam = ChatColor.valueOf(paTeams.get(flagTeam))
						+ flagTeam + ChatColor.YELLOW;
				String scPlayer = ChatColor.valueOf(paTeams.get(sTeam))
						+ player.getName() + ChatColor.YELLOW;

				playerManager.tellEveryone(PVPArena.lang.parse("pumpkinhomeleft",
						scPlayer, scFlagTeam,
						String.valueOf(paLives.get(flagTeam) - 1)));
				paTeamFlags.remove(flagTeam);
				
				player.getInventory().setHelmet(paHeadGears.get(player.getName()).clone());
				paHeadGears.remove(player.getName());
				
				reduceLivesCheckEndAndCommit(flagTeam);
			}
		} else {
			for (String team : paTeams.keySet()) {
				String playerTeam = playerManager.getTeam(player);
				if (team.equals(playerTeam))
					continue;
				if (!playerManager.getPlayerTeamMap().containsValue(team))
					continue; // dont check for inactive teams
				if (paTeamFlags.containsKey(team)) {
					continue; // already taken
				}
				db.i("checking for pumpkin of team " + team);
				vLoc = player.getLocation().toVector();
				if (this.getCoords(team + "pumpkin") != null) {
					vFlag = this.getCoords(team + "pumpkin").toVector();
				}
				if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
					db.i("spawn found!");
					db.i("vFlag: " + vFlag.toString());
					String scTeam = ChatColor.valueOf(paTeams.get(team)) + team
							+ ChatColor.YELLOW;
					String scPlayer = ChatColor
							.valueOf(paTeams.get(playerTeam))
							+ player.getName() + ChatColor.YELLOW;
					playerManager.tellEveryone(PVPArena.lang.parse("pumpkingrab",
							scPlayer, scTeam));
					paHeadGears.put(player.getName(), player.getInventory().getHelmet().clone());
					player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN,1));
					paTeamFlags.put(team, player.getName());
					return;
				}
			}
		}
	}
	
	/*
	 * set the pumpkin to the selected block
	 */
	public void setPumpkin(Player player, Block block) {
		if (block == null || !block.getType().equals(Material.PUMPKIN)) {
			return;
		}
		String sName = Arena.regionmodify.replace(this.name+":", "");
		
		Location location = block.getLocation();

		Integer x = location.getBlockX();
		Integer y = location.getBlockY();
		Integer z = location.getBlockZ();
		Float yaw = location.getYaw();
		Float pitch = location.getPitch();

		String s = x.toString() + "," + y.toString() + "," + z.toString() + ","
				+ yaw.toString() + "," + pitch.toString();

		cfg.set("spawns." + sName + "pumpkin", s);
		
		cfg.save();
		Arenas.tellPlayer(player, PVPArena.lang.parse("setpumpkin", sName));
		
		Arena.regionmodify = "";
	}

	/**
	 * get the team name of the pumpkin a player holds
	 * 
	 * @param player
	 *            the player to check
	 * @return a team name
	 */
	@Override
	protected String getHeldFlagTeam(String player) {
		//TODO 
		db.i("getting held pumpkin of player " + player);
		for (String sTeam : paTeamFlags.keySet()) {
			db.i("team " + sTeam + " is in " + paTeamFlags.get(sTeam)
					+ "s hands");
			if (player.equals(paTeamFlags.get(sTeam))) {
				return sTeam;
			}
		}
		return null;
	}

	/**
	 * check a dying player if he held a pumpkin, drop it, if so
	 * 
	 * @param player
	 *            the player to check
	 */
	@Override
	public void checkEntityDeath(Player player) {
		String flagTeam = getHeldFlagTeam(player.getName());
		if (flagTeam != null) {
			String scFlagTeam = ChatColor.valueOf(paTeams.get(flagTeam))
					+ flagTeam + ChatColor.YELLOW;
			String scPlayer = ChatColor.valueOf(paTeams.get(playerManager
					.getTeam(player))) + player.getName() + ChatColor.YELLOW;
			PVPArena.lang.parse("pumpkinsave", scPlayer, scFlagTeam);
			paTeamFlags.remove(flagTeam);
		}
	}

	@Override
	public String getType() {
		return "pumpkin";
	}

	@Override
	public void respawnPlayer(Player player, byte lives) {

		playersetHealth(player, cfg.getInt("general.startHealth", 0));
		player.setFireTicks(0);
		player.setFoodLevel(cfg.getInt("general.startFoodLevel", 20));
		player.setSaturation(cfg.getInt("general.startSaturation", 20));
		player.setExhaustion((float) cfg.getDouble("general.start", 0.0));

		if (cfg.getBoolean("general.refillInventory")
				&& !playerManager.getClass(player).equals("custom")) {
			clearInventory(player);
			givePlayerFightItems(player);
		}

		String sTeam = playerManager.getTeam(player);
		String color = paTeams.get(sTeam);
		playerManager
				.tellEveryone(PVPArena.lang.parse("killed",
						ChatColor.valueOf(color) + player.getName()
								+ ChatColor.YELLOW));
		tpPlayerToCoordName(player, sTeam + "spawn");
		
		checkEntityDeath(player);
	}
}
