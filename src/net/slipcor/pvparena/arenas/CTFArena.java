package net.slipcor.pvparena.arenas;

/*
 * Capture the Flag Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.6 - CTF Arena
 * 
 * history:
 *
 *     v0.3.6 - CTF Arena
 * 
 */

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import net.slipcor.pvparena.PVPArenaPlugin;
import net.slipcor.pvparena.managers.StatsManager;

public class CTFArena extends Arena{
	private HashMap<String, Integer> teamLives = new HashMap<String, Integer>(); // flags "lives"
	private HashMap<String, String> teamFlags = new HashMap<String, String>(); // carried flags
	
	@SuppressWarnings("unchecked")
	public CTFArena(String sName, PVPArenaPlugin plugin) {
		super();

		this.name = sName;
		this.configFile = new File("plugins/pvparena/config.ctf_" + name + ".yml");
		
		new File("plugins/pvparena").mkdir();
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				PVPArenaPlugin.lang.log_error("filecreateerror","config.ctf_" + name);
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
		
		parseConfig("ctf");
	}

	@Override
	public boolean checkEnd() {
		return false;
	}
	
	@Override
	public void init_arena() {
		for (String sTeam : this.fightTeams.keySet()) {
			if (this.fightUsersTeam.containsValue(sTeam)) {
				// team is active
				this.teamLives.put(sTeam, this.maxlives);
			}
		}
	}
	
	private void reduce(String team) {
		if (teamLives.get(team) != null) {
			int i = teamLives.get(team)-1;
			if (i > 0) {
				teamLives.put(team, i);
			} else {
				teamLives.remove(team);
				Set<String> set = fightUsersTeam.keySet();
				Iterator<String> iter = set.iterator();
				while (iter.hasNext()) {
					Object o = iter.next();
					System.out.print("precessing: "+o.toString());
					Player z = Bukkit.getServer().getPlayer(o.toString());
					if (fightUsersTeam.get(z.getName()).equals(team)) {
						StatsManager.addLoseStat(z, team, this);
						loadPlayer(z, sTPlose);
						fightUsersClass.remove(z.getName());
					}
				}
				

				if (teamLives.size() > 1) {
					return;
				}
				String winteam = "";
				set = fightUsersTeam.keySet();
				iter = set.iterator();
				while (iter.hasNext()) {
					Object o = iter.next();
					System.out.print("praecessing: "+o.toString());
					Player z = Bukkit.getServer().getPlayer(o.toString());

					if (teamLives.containsKey(fightUsersTeam.get(z.getName()))) {
						StatsManager.addWinStat(z, team, this);
						loadPlayer(z, sTPwin);
						giveRewards(z); // if we are the winning team, give reward!
						fightUsersClass.remove(z.getName());
						winteam = fightUsersTeam.get(z.getName());
					}
				}
				

				tellEveryone(PVPArenaPlugin.lang.parse("haswon",ChatColor.valueOf(fightTeams.get(winteam)) + "Team " + winteam));
				
				teamLives.clear();
				reset();
			}
		}
	}
	
	public void checkInteract(Player player) {
		Vector vLoc;
		String sTeam;
		Vector vSpawn;
		
		if (teamFlags.containsValue(player.getName())) {
			System.out.print("player " + player.getName() + " has got a flag");
			vLoc = player.getLocation().toVector();
			sTeam = fightUsersTeam.get(player.getName());
			vSpawn = this.getCoords(sTeam + "spawn").toVector();

			System.out.print("player is in the team "+sTeam);
			if (vLoc.distance(vSpawn) < 2) {

				System.out.print("player is at his spawn");
				String flagTeam = getHeldFlagTeam(player.getName());
				

				System.out.print("the flag belongs to team " + flagTeam);
				
				String scFlagTeam = ChatColor.valueOf(fightTeams.get(flagTeam)) + flagTeam + ChatColor.YELLOW;
				String scPlayer = ChatColor.valueOf(fightTeams.get(sTeam)) + player.getName() + ChatColor.YELLOW;
				
				
				tellEveryone(PVPArenaPlugin.lang.parse("flaghome",scPlayer, scFlagTeam));
				teamFlags.remove(flagTeam);
				reduce(flagTeam);
			}
		} else {
			for (String team : fightTeams.keySet()) {
				if (team.equals(fightUsersTeam.get(player.getName())))
					continue;
				if (!fightUsersTeam.containsValue(team))
					continue; // dont check for inactive teams
				System.out.print("checking for spawn of team " + team);
				vLoc = player.getLocation().toVector();
				vSpawn = this.getCoords(team + "spawn").toVector();
				if (vLoc.distance(vSpawn) < 2) {
					System.out.print("spawn found!");
					String scTeam = ChatColor.valueOf(fightTeams.get(team)) + team + ChatColor.YELLOW;
					String scPlayer = ChatColor.valueOf(fightTeams.get(fightUsersTeam.get(player.getName()))) + player.getName() + ChatColor.YELLOW;
					tellEveryone(PVPArenaPlugin.lang.parse("flaggrab",scPlayer, scTeam));

					teamFlags.put(team, player.getName());
					return;
				}
			}
		}
	}

	private String getHeldFlagTeam(String player) {
		for (String sTeam : teamFlags.keySet()) {
			if (player.equals((String) teamFlags.get(sTeam))) {
				return sTeam;
			}
		}
		return null;
	}

	public void checkEntityDeath(Player player) {
		String flagTeam = getHeldFlagTeam(player.getName());
		String scFlagTeam = ChatColor.valueOf(fightTeams.get(flagTeam)) + flagTeam + ChatColor.YELLOW;
		String scPlayer = ChatColor.valueOf(fightTeams.get(fightUsersTeam.get(player.getName()))) + player.getName() + ChatColor.YELLOW;
		PVPArenaPlugin.lang.parse("flagsave",scPlayer, scFlagTeam);
		teamFlags.remove(flagTeam);
	}
}
