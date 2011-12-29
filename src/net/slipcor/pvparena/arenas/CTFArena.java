/*
 * capture the flag arena class
 * 
 * author: slipcor
 * 
 * version: v0.5.2 - Bugfixes, configurable player start values
 * 
 * history:
 * 
 *     v0.4.4 - Random spawns per team, not shared
 *     v0.4.1 - command manager, arena information and arena config check
 *     v0.4.0 - mayor rewrite, improved help
 *     v0.3.14 - timed arena modes
 *     v0.3.12 - set flag positions
 *     v0.3.10 - CraftBukkit #1337 config version, rewrite
 *     v0.3.9 - Permissions, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.6 - CTF Arena
 */

package net.slipcor.pvparena.arenas;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.ConfigManager;
import net.slipcor.pvparena.managers.StatsManager;

public class CTFArena extends Arena {
	private HashMap<String, Integer> paTeamLives = new HashMap<String, Integer>(); // flags
																					// "lives"
	private HashMap<String, String> paTeamFlags = new HashMap<String, String>(); // carried
																					// flags

	/*
	 * ctf constructor
	 * 
	 * - open or create a new configuration file - parse the arena config
	 */
	public CTFArena(String sName) {
		super();

		this.name = sName;

		db.i("loading CTF Arena " + name);

		cfg = new Config(new File("plugins/pvparena/config.ctf_" + name
				+ ".yml"));
		cfg.load();
		if (cfg.get("cfgver") == null) {
			ConfigManager.legacyImport(this, cfg);
		}
		ConfigManager.configParse(this, cfg);
		if (cfg.get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			cfg.getYamlConfiguration().addDefault("teams.red", ChatColor.RED.name());
			cfg.getYamlConfiguration().addDefault("teams.blue", ChatColor.BLUE.name());
			cfg.getYamlConfiguration().options().copyDefaults(true);
			cfg.reloadMaps();
		}
		cfg.save();
		Map<String, Object> tempMap = (Map<String, Object>) cfg.getYamlConfiguration()
				.getConfigurationSection("teams").getValues(true);

		for (String sTeam : tempMap.keySet()) {
			this.paTeams.put(sTeam, (String) tempMap.get(sTeam));
			db.i("added team " + sTeam + " => " + this.paTeams.get(sTeam));
		}
	}

	/*
	 * return "only one player/team alive"
	 * 
	 * return false, end triggered from PlayerListener
	 */
	@Override
	public boolean checkEndAndCommit() {
		if (playerManager.countPlayersInTeams() < 2) {
			String team = "$%&/";
			if (playerManager.countPlayersInTeams() != 0)
				for (String t : playerManager.getPlayerTeamMap().values()) {
					team = t;
					break;
				}
			CommitEnd(team);
		}
		return false;
	}

	private void CommitEnd(String team) {
		Set<String> set = playerManager.getPlayerTeamMap().keySet();
		Iterator<String> iter = set.iterator();
		if (!team.equals("$%&/")) {
			while (iter.hasNext()) {
				Object o = iter.next();
				db.i("precessing: " + o.toString());
				Player z = Bukkit.getServer().getPlayer(o.toString());
				if (playerManager.getPlayerTeamMap().get(z.getName())
						.equals(team)) {
					StatsManager.addLoseStat(z, team, this);
					resetPlayer(z, cfg.getString("tp.lose", "old"));
					playerManager.setClass(z, "");
				}
			}

			if (paTeamLives.size() > 1) {
				return;
			}
		}

		String winteam = "";
		set = playerManager.getPlayerTeamMap().keySet();
		iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			db.i("praecessing: " + o.toString());
			Player z = Bukkit.getServer().getPlayer(o.toString());

			if (paTeamLives.containsKey(playerManager.getPlayerTeamMap().get(
					z.getName()))) {
				StatsManager.addWinStat(z, team, this);
				resetPlayer(z, cfg.getString("tp.win", "old"));
				giveRewards(z); // if we are the winning team, give reward!
				playerManager.setClass(z, "");
				winteam = team;
			}
		}

		playerManager.tellEveryone(PVPArena.lang.parse("teamhaswon",
				ChatColor.valueOf(paTeams.get(winteam)) + "Team " + winteam));

		paTeamLives.clear();
		reset();
	}

	/*
	 * for every active team: add lives entry
	 */
	@Override
	public void init_arena() {
		for (String sTeam : this.paTeams.keySet()) {
			if (playerManager.getPlayerTeamMap().containsValue(sTeam)) {
				// team is active
				this.paTeamLives.put(sTeam, this.cfg.getInt("general.lives", 3));
			}
		}
	}

	/*
	 * !CTF-ONLY!
	 * 
	 * reduce the lives of given team, check if a team has won and run a special
	 * checkEndAndCommit
	 */
	private void reduceLivesCheckEndAndCommit(String team) {
		if (paTeamLives.get(team) != null) {
			int i = paTeamLives.get(team) - 1;
			if (i > 0) {
				paTeamLives.put(team, i);
			} else {
				paTeamLives.remove(team);
				CommitEnd(team);
			}
		}
	}

	/*
	 * check the interaction of a player
	 * 
	 * - has flag? - home => point - on active flag? - take flag
	 */
	public void checkInteract(Player player) {
		Vector vLoc;
		String sTeam;
		Vector vSpawn;
		Vector vFlag = null;

		if (paTeamFlags.containsValue(player.getName())) {
			db.i("player " + player.getName() + " has got a flag");
			vLoc = player.getLocation().toVector();
			sTeam = playerManager.getTeam(player);
			vSpawn = this.getCoords(sTeam + "spawn").toVector();
			if (this.getCoords(sTeam + "flag") != null) {
				vFlag = this.getCoords(sTeam + "flag").toVector();
			} else {
				db.i(sTeam + "flag" + " = null");
			}

			db.i("player is in the team " + sTeam);
			if ((vFlag == null && vLoc.distance(vSpawn) < 2)
					|| (vFlag != null && vLoc.distance(vFlag) < 2)) {

				db.i("player is at his spawn");
				String flagTeam = getHeldFlagTeam(player.getName());

				db.i("the flag belongs to team " + flagTeam);

				String scFlagTeam = ChatColor.valueOf(paTeams.get(flagTeam))
						+ flagTeam + ChatColor.YELLOW;
				String scPlayer = ChatColor.valueOf(paTeams.get(sTeam))
						+ player.getName() + ChatColor.YELLOW;

				playerManager.tellEveryone(PVPArena.lang.parse("flaghomeleft",
						scPlayer, scFlagTeam, String.valueOf(paTeamLives.get(flagTeam)-1)));
				paTeamFlags.remove(flagTeam);
				reduceLivesCheckEndAndCommit(flagTeam);
			}
		} else {
			for (String team : paTeams.keySet()) {
				String playerTeam = playerManager.getTeam(player);
				if (team.equals(playerTeam))
					continue;
				if (!playerManager.getPlayerTeamMap().containsValue(team))
					continue; // dont check for inactive teams
				db.i("checking for spawn of team " + team);
				vLoc = player.getLocation().toVector();
				vSpawn = this.getCoords(team + "spawn").toVector();
				if (this.getCoords(team + "flag") != null) {
					vFlag = this.getCoords(team + "flag").toVector();
				}
				if (((vFlag == null) && (vLoc.distance(vSpawn) < 2))
						|| ((vFlag != null) && (vLoc.distance(vFlag) < 2))) {
					db.i("spawn found!");
					db.i("vFlag: " + ((vFlag == null)?"null":vFlag.toString()));
					String scTeam = ChatColor.valueOf(paTeams.get(team)) + team
							+ ChatColor.YELLOW;
					String scPlayer = ChatColor
							.valueOf(paTeams.get(playerTeam))
							+ player.getName() + ChatColor.YELLOW;
					playerManager.tellEveryone(PVPArena.lang.parse("flaggrab",
							scPlayer, scTeam));

					paTeamFlags.put(team, player.getName());
					return;
				}
			}
		}
	}

	/*
	 * get the team name of the flag a player holds
	 */
	private String getHeldFlagTeam(String player) {
		db.i("getting held flag of player "+player);
		for (String sTeam : paTeamFlags.keySet()) {
			db.i("team "+sTeam+" is in "+paTeamFlags.get(sTeam)+"s hands");
			if (player.equals(paTeamFlags.get(sTeam))) {
				return sTeam;
			}
		}
		return null;
	}

	/*
	 * Check a dying player if he held a flag, drop it then
	 */
	public void checkEntityDeath(Player player) {
		String flagTeam = getHeldFlagTeam(player.getName());
		if (flagTeam != null) {
			String scFlagTeam = ChatColor.valueOf(paTeams.get(flagTeam))
					+ flagTeam + ChatColor.YELLOW;
			String scPlayer = ChatColor.valueOf(paTeams.get(playerManager
					.getTeam(player))) + player.getName() + ChatColor.YELLOW;
			PVPArena.lang.parse("flagsave", scPlayer, scFlagTeam);
			paTeamFlags.remove(flagTeam);
		}
	}
	
	@Override
	public String getType() {
		return "ctf";
	}
	
	@Override
	public void respawnPlayer(Player player, byte lives) {

		player.setHealth(cfg.getInt("general.startHealth",0));
		player.setFireTicks(0);
		player.setFoodLevel(cfg.getInt("general.startFoodLevel",20));
		player.setSaturation(cfg.getInt("general.startSaturation",20));
		player.setExhaustion((float) cfg.getDouble("general.start", 0.0));

		
		if (cfg.getBoolean("general.refillInventory") && !playerManager.getClass(player).equals("custom")) {
			clearInventory(player);
			givePlayerFightItems(player);
		}
		
		String sTeam = playerManager.getTeam(player);
		String color = paTeams.get(sTeam);
		playerManager.tellEveryone(PVPArena.lang.parse("killed",
				ChatColor.valueOf(color) + player.getName()
						+ ChatColor.YELLOW));
		tpPlayerToCoordName(player, sTeam + "spawn");
	}
}
