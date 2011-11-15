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
import org.bukkit.util.config.Configuration;

/*
 * Capture the Flag Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.8 - BOSEconomy, rewrite
 * 
 * history:
 *
 *     v0.3.6 - CTF Arena
 * 
 */

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.StatsManager;

public class CTFArena extends Arena{
	private HashMap<String, Integer> fightTeamLives = new HashMap<String, Integer>(); // flags "lives"
	private HashMap<String, String> fightTeamFlags = new HashMap<String, String>(); // carried flags
	
	/*
	 * ctf constructor
	 * 
	 * - open or create a new configuration file
	 * - parse the arena config
	 */
	@SuppressWarnings("unchecked")
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
		
		Map<String, String> fT = new HashMap<String, String>();
		fT.put("red",ChatColor.RED.name());
		fT.put("blue",ChatColor.BLUE.name());
		
		Configuration config = new Configuration(configFile);
		config.load();
		if (config.getProperty("teams.custom") == null) {
			config.setProperty("teams.custom", fT);
			config.save();
		}
		this.fightTeams = (Map<String, String>) config.getProperty("teams.custom");
		
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
		for (String sTeam : this.fightTeams.keySet()) {
			if (this.fightPlayersTeam.containsValue(sTeam)) {
				// team is active
				this.fightTeamLives.put(sTeam, this.maxLives);
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
		if (fightTeamLives.get(team) != null) {
			int i = fightTeamLives.get(team)-1;
			if (i > 0) {
				fightTeamLives.put(team, i);
			} else {
				fightTeamLives.remove(team);
				Set<String> set = fightPlayersTeam.keySet();
				Iterator<String> iter = set.iterator();
				while (iter.hasNext()) {
					Object o = iter.next();
					System.out.print("precessing: "+o.toString());
					Player z = Bukkit.getServer().getPlayer(o.toString());
					if (fightPlayersTeam.get(z.getName()).equals(team)) {
						StatsManager.addLoseStat(z, team, this);
						resetPlayer(z, sTPlose);
						fightPlayersClass.remove(z.getName());
					}
				}
				
				if (fightTeamLives.size() > 1) {
					return;
				}
				String winteam = "";
				set = fightPlayersTeam.keySet();
				iter = set.iterator();
				while (iter.hasNext()) {
					Object o = iter.next();
					System.out.print("praecessing: "+o.toString());
					Player z = Bukkit.getServer().getPlayer(o.toString());

					if (fightTeamLives.containsKey(fightPlayersTeam.get(z.getName()))) {
						StatsManager.addWinStat(z, team, this);
						resetPlayer(z, sTPwin);
						giveRewards(z); // if we are the winning team, give reward!
						fightPlayersClass.remove(z.getName());
						winteam = fightPlayersTeam.get(z.getName());
					}
				}
				
				tellEveryone(PVPArena.lang.parse("haswon",ChatColor.valueOf(fightTeams.get(winteam)) + "Team " + winteam));
				
				fightTeamLives.clear();
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
		
		if (fightTeamFlags.containsValue(player.getName())) {
			System.out.print("player " + player.getName() + " has got a flag");
			vLoc = player.getLocation().toVector();
			sTeam = fightPlayersTeam.get(player.getName());
			vSpawn = this.getCoords(sTeam + "spawn").toVector();

			System.out.print("player is in the team "+sTeam);
			if (vLoc.distance(vSpawn) < 2) {

				System.out.print("player is at his spawn");
				String flagTeam = getHeldFlagTeam(player.getName());
				

				System.out.print("the flag belongs to team " + flagTeam);
				
				String scFlagTeam = ChatColor.valueOf(fightTeams.get(flagTeam)) + flagTeam + ChatColor.YELLOW;
				String scPlayer = ChatColor.valueOf(fightTeams.get(sTeam)) + player.getName() + ChatColor.YELLOW;
				
				
				tellEveryone(PVPArena.lang.parse("flaghome",scPlayer, scFlagTeam));
				fightTeamFlags.remove(flagTeam);
				reduceLivesCheckEndAndCommit(flagTeam);
			}
		} else {
			for (String team : fightTeams.keySet()) {
				if (team.equals(fightPlayersTeam.get(player.getName())))
					continue;
				if (!fightPlayersTeam.containsValue(team))
					continue; // dont check for inactive teams
				System.out.print("checking for spawn of team " + team);
				vLoc = player.getLocation().toVector();
				vSpawn = this.getCoords(team + "spawn").toVector();
				if (vLoc.distance(vSpawn) < 2) {
					System.out.print("spawn found!");
					String scTeam = ChatColor.valueOf(fightTeams.get(team)) + team + ChatColor.YELLOW;
					String scPlayer = ChatColor.valueOf(fightTeams.get(fightPlayersTeam.get(player.getName()))) + player.getName() + ChatColor.YELLOW;
					tellEveryone(PVPArena.lang.parse("flaggrab",scPlayer, scTeam));

					fightTeamFlags.put(team, player.getName());
					return;
				}
			}
		}
	}

	/*
	 * get the team name of the flag a player holds
	 */
	private String getHeldFlagTeam(String player) {
		for (String sTeam : fightTeamFlags.keySet())
			if (player.equals((String) fightTeamFlags.get(sTeam)))
				return sTeam;
				
		return null;
	}

	/*
	 * Check a dying player if he held a flag, drop it then
	 */
	public void checkEntityDeath(Player player) {
		String flagTeam = getHeldFlagTeam(player.getName());
		String scFlagTeam = ChatColor.valueOf(fightTeams.get(flagTeam)) + flagTeam + ChatColor.YELLOW;
		String scPlayer = ChatColor.valueOf(fightTeams.get(fightPlayersTeam.get(player.getName()))) + player.getName() + ChatColor.YELLOW;
		PVPArena.lang.parse("flagsave",scPlayer, scFlagTeam);
		fightTeamFlags.remove(flagTeam);
	}
}
