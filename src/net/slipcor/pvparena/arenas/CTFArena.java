package net.slipcor.pvparena.arenas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.StatsManager;

/*
 * Capture the Flag Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.10 - CraftBukkit #1337 config version, rewrite
 * 
 * history:
 *
 *     v0.3.9 - Permissions, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.6 - CTF Arena
 * 
 */


public class CTFArena extends Arena{
	private HashMap<String, Integer> paTeamLives = new HashMap<String, Integer>(); // flags "lives"
	private HashMap<String, String> paTeamFlags = new HashMap<String, String>(); // carried flags
	/*
	 * ctf constructor
	 * 
	 * - open or create a new configuration file
	 * - parse the arena config
	 */
	public CTFArena(String sName, PVPArena plugin) {
		super();

		this.name = sName;
		this.configFile = new File("plugins/pvparena/config.ctf_" + name + ".yml");
		
		new File("plugins/pvparena").mkdir();
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				PVPArena.lang.log_error("filecreateerror","config.ctf_" + name);
			}
		this.randomSpawn = false;
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		config.addDefault("teams.custom.red",ChatColor.RED.name());
		config.addDefault("teams.custom.blue",ChatColor.BLUE.name());
		config.options().copyDefaults(true);
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.paTeams = (Map<String, Object>) config.getConfigurationSection("teams.custom").getValues(true);
		
		configParse("ctf");
	}
	
	/*
	 * return "only one player/team alive"
	 * 
	 * return false, end triggered from PlayerListener
	 */
	@Override
	public boolean checkEndAndCommit() {
		return false;
	}
	
	/*
	 * for every active team: add lives entry
	 */
	@Override
	public void init_arena() {
		for (String sTeam : this.paTeams.keySet()) {
			if (this.paPlayersTeam.containsValue(sTeam)) {
				// team is active
				this.paTeamLives.put(sTeam, this.maxLives);
			}
		}
	}
	
	/*
	 * !CTF-ONLY!
	 * 
	 * reduce the lives of given team, check if a team has won
	 * and run a special checkEndAndCommit
	 */
	private void reduceLivesCheckEndAndCommit(String team) {
		if (paTeamLives.get(team) != null) {
			int i = paTeamLives.get(team)-1;
			if (i > 0) {
				paTeamLives.put(team, i);
			} else {
				paTeamLives.remove(team);
				Set<String> set = paPlayersTeam.keySet();
				Iterator<String> iter = set.iterator();
				while (iter.hasNext()) {
					Object o = iter.next();
					db.i("precessing: "+o.toString());
					Player z = Bukkit.getServer().getPlayer(o.toString());
					if (paPlayersTeam.get(z.getName()).equals(team)) {
						StatsManager.addLoseStat(z, team, this);
						resetPlayer(z, sTPlose);
						paPlayersClass.remove(z.getName());
					}
				}
				
				if (paTeamLives.size() > 1) {
					return;
				}
				String winteam = "";
				set = paPlayersTeam.keySet();
				iter = set.iterator();
				while (iter.hasNext()) {
					Object o = iter.next();
					db.i("praecessing: "+o.toString());
					Player z = Bukkit.getServer().getPlayer(o.toString());

					if (paTeamLives.containsKey(paPlayersTeam.get(z.getName()))) {
						StatsManager.addWinStat(z, team, this);
						resetPlayer(z, sTPwin);
						giveRewards(z); // if we are the winning team, give reward!
						paPlayersClass.remove(z.getName());
						winteam = paPlayersTeam.get(z.getName());
					}
				}
				
				tellEveryone(PVPArena.lang.parse("haswon",ChatColor.valueOf((String) paTeams.get(winteam)) + "Team " + winteam));
				
				paTeamLives.clear();
				reset();
			}
		}
	}
	
	/*
	 * check the interaction of a player
	 * 
	 * - has flag?
	 *   - home => point
	 * - on active flag?
	 *   - take flag
	 */
	public void checkInteract(Player player) {
		Vector vLoc;
		String sTeam;
		Vector vSpawn;
		
		if (paTeamFlags.containsValue(player.getName())) {
			db.i("player " + player.getName() + " has got a flag");
			vLoc = player.getLocation().toVector();
			sTeam = paPlayersTeam.get(player.getName());
			vSpawn = this.getCoords(sTeam + "spawn").toVector();

			db.i("player is in the team "+sTeam);
			if (vLoc.distance(vSpawn) < 2) {

				db.i("player is at his spawn");
				String flagTeam = getHeldFlagTeam(player.getName());
				

				db.i("the flag belongs to team " + flagTeam);
				
				String scFlagTeam = ChatColor.valueOf((String) paTeams.get(flagTeam)) + flagTeam + ChatColor.YELLOW;
				String scPlayer = ChatColor.valueOf((String) paTeams.get(sTeam)) + player.getName() + ChatColor.YELLOW;
				
				
				tellEveryone(PVPArena.lang.parse("flaghome",scPlayer, scFlagTeam));
				paTeamFlags.remove(flagTeam);
				reduceLivesCheckEndAndCommit(flagTeam);
			}
		} else {
			for (String team : paTeams.keySet()) {
				if (team.equals(paPlayersTeam.get(player.getName())))
					continue;
				if (!paPlayersTeam.containsValue(team))
					continue; // dont check for inactive teams
				db.i("checking for spawn of team " + team);
				vLoc = player.getLocation().toVector();
				vSpawn = this.getCoords(team + "spawn").toVector();
				if (vLoc.distance(vSpawn) < 2) {
					db.i("spawn found!");
					String scTeam = ChatColor.valueOf((String) paTeams.get(team)) + team + ChatColor.YELLOW;
					String scPlayer = ChatColor.valueOf((String) paTeams.get(paPlayersTeam.get(player.getName()))) + player.getName() + ChatColor.YELLOW;
					tellEveryone(PVPArena.lang.parse("flaggrab",scPlayer, scTeam));

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
		for (String sTeam : paTeamFlags.keySet())
			if (player.equals((String) paTeamFlags.get(sTeam)))
				return sTeam;
				
		return null;
	}

	/*
	 * Check a dying player if he held a flag, drop it then
	 */
	public void checkEntityDeath(Player player) {
		String flagTeam = getHeldFlagTeam(player.getName());
		String scFlagTeam = ChatColor.valueOf((String) paTeams.get(flagTeam)) + flagTeam + ChatColor.YELLOW;
		String scPlayer = ChatColor.valueOf((String) paTeams.get(paPlayersTeam.get(player.getName()))) + player.getName() + ChatColor.YELLOW;
		PVPArena.lang.parse("flagsave",scPlayer, scFlagTeam);
		paTeamFlags.remove(flagTeam);
	}
}
